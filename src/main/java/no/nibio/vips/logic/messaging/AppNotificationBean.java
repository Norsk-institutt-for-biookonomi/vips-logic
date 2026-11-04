package no.nibio.vips.logic.messaging;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import no.nibio.vips.logic.entity.rest.AppNotificationPrefsDto;
import no.nibio.vips.logic.entity.rest.UserDeviceDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@Transactional
public class AppNotificationBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppNotificationBean.class);

    private final EntityManager em;
    private final FcmClient fcmClient;

    @Inject
    public AppNotificationBean(EntityManager em, FcmClient fcmClient) {
        this.em = em;
        this.fcmClient = fcmClient;
    }

    public AppNotificationPrefsDto getPrefs(Integer userId) {
        AppNotificationPreference stored = em.find(AppNotificationPreference.class, userId);
        AppNotificationPrefsDto dto = new AppNotificationPrefsDto();
        if (stored == null) {
            return dto;
        }
        dto.setForecastStatusEnabled(stored.isForecastStatusEnabled());
        dto.setForecastConfigurationIds(copyLongs(stored.getForecastConfigurationIds()));
        dto.setMessageEnabled(stored.isMessageEnabled());
        dto.setMessageCropCategoryIds(copyInts(stored.getMessageCropCategoryIds()));
        return dto;
    }

    public AppNotificationPrefsDto storePrefs(Integer userId, AppNotificationPrefsDto dto) {
        AppNotificationPreference stored = em.find(AppNotificationPreference.class, userId);
        if (stored == null) {
            stored = new AppNotificationPreference();
            stored.setUserId(userId);
        }
        stored.setForecastStatusEnabled(dto.isForecastStatusEnabled());
        stored.setForecastConfigurationIds(copyLongs(dto.getForecastConfigurationIds()));
        stored.setMessageEnabled(dto.isMessageEnabled());
        stored.setMessageCropCategoryIds(copyInts(dto.getMessageCropCategoryIds()));
        stored.setUpdatedAt(new Date());
        em.merge(stored);
        return getPrefs(userId);
    }

    public UserDevice storeDevice(Integer userId, UserDeviceDto dto) {
        String token = dto.getFcmToken().trim();
        String platform = dto.getPlatform().trim().toUpperCase();
        UserDevice device = findByToken(token);
        if (device == null) {
            device = new UserDevice();
            device.setFcmToken(token);
        }
        device.setUserId(userId);
        device.setPlatform(platform);
        device.setUpdatedAt(new Date());
        return em.merge(device);
    }

    public boolean deleteDevice(Integer userId, String fcmToken) {
        if (fcmToken == null || fcmToken.isBlank()) {
            return false;
        }
        UserDevice device = findByToken(fcmToken.trim());
        if (device == null || !userId.equals(device.getUserId())) {
            return false;
        }
        em.remove(device);
        return true;
    }

    public void deleteAllForUser(Integer userId) {
        AppNotificationPreference prefs = em.find(AppNotificationPreference.class, userId);
        if (prefs != null) {
            em.remove(prefs);
        }
        em.createNamedQuery("UserDevice.deleteByUserId")
            .setParameter("userId", userId)
            .executeUpdate();
    }

    public boolean notifyForecast(
            Long forecastConfigurationId,
            Integer organizationId,
            Function<String, PushText> textForLocale) {
        if (forecastConfigurationId == null || organizationId == null || !fcmClient.isEnabled()) {
            return false;
        }
        List<DeviceTarget> devices = deviceTargets(em.createNativeQuery(
                "SELECT d.user_device_id, d.fcm_token, u.preferred_locale "
                    + "FROM messaging.user_device d "
                    + "JOIN messaging.app_notification_preference p ON p.user_id = d.user_id "
                    + "JOIN public.vips_logic_user u ON u.user_id = d.user_id "
                    + "WHERE p.forecast_status_enabled "
                    + "AND :forecastId = ANY(p.forecast_configuration_ids) "
                    + "AND u.organization_id = :organizationId")
            .setParameter("forecastId", forecastConfigurationId)
            .setParameter("organizationId", organizationId)
            .getResultList());
        return sendLocalized(devices, textForLocale, "forecast", String.valueOf(forecastConfigurationId));
    }

    public boolean notifyMessage(
            Integer messageId,
            List<Integer> cropCategoryIds,
            Integer organizationId,
            Function<String, PushText> textForLocale) {
        if (messageId == null || organizationId == null || !fcmClient.isEnabled()) {
            return false;
        }
        List<Integer> cropIds = cropCategoryIds == null
            ? List.of()
            : cropCategoryIds.stream().filter(id -> id != null).collect(Collectors.toList());
        if (cropIds.isEmpty()) {
            return false;
        }
        List<DeviceTarget> devices = deviceTargets(em.createNativeQuery(
                "SELECT d.user_device_id, d.fcm_token, u.preferred_locale "
                    + "FROM messaging.user_device d "
                    + "JOIN messaging.app_notification_preference p ON p.user_id = d.user_id "
                    + "JOIN public.vips_logic_user u ON u.user_id = d.user_id "
                    + "WHERE p.message_enabled "
                    + "AND EXISTS ("
                    + "    SELECT 1 FROM unnest(p.message_crop_category_ids) AS cid "
                    + "    WHERE cid IN (:cropIds)"
                    + ") "
                    + "AND u.organization_id = :organizationId")
            .setParameter("cropIds", cropIds)
            .setParameter("organizationId", organizationId)
            .getResultList());
        return sendLocalized(devices, textForLocale, "message", String.valueOf(messageId));
    }

    public static String appLocale(String preferredLocale) {
        if (preferredLocale != null && preferredLocale.toLowerCase(Locale.ROOT).startsWith("en")) {
            return "en";
        }
        return "nb";
    }

    private boolean sendLocalized(
            List<DeviceTarget> devices,
            Function<String, PushText> textForLocale,
            String type,
            String id) {
        Map<String, List<DeviceTarget>> byLocale = new LinkedHashMap<>();
        for (DeviceTarget device : devices) {
            byLocale.computeIfAbsent(device.locale, key -> new ArrayList<>()).add(device);
        }
        boolean sent = false;
        for (Map.Entry<String, List<DeviceTarget>> entry : byLocale.entrySet()) {
            PushText text = textForLocale.apply(entry.getKey());
            if (text == null || text.title == null || text.title.isBlank()) {
                continue;
            }
            if (sendToDevices(entry.getValue(), text.title, text.body, type, id)) {
                sent = true;
            }
        }
        return sent;
    }

    private boolean sendToDevices(List<DeviceTarget> devices, String title, String body, String type, String id) {
        boolean sent = false;
        for (DeviceTarget device : devices) {
            try {
                FcmClient.SendResult result = fcmClient.send(device.fcmToken, title, body, type, id);
                if (result == FcmClient.SendResult.SENT) {
                    sent = true;
                } else if (result == FcmClient.SendResult.INVALID_TOKEN) {
                    em.createNamedQuery("UserDevice.deleteByFcmToken")
                        .setParameter("fcmToken", device.fcmToken)
                        .executeUpdate();
                }
            } catch (Exception ex) {
                LOGGER.warn("FCM send failed for device {}", device.userDeviceId, ex);
            }
        }
        return sent;
    }

    private static List<DeviceTarget> deviceTargets(List<?> rows) {
        List<DeviceTarget> devices = new ArrayList<>();
        for (Object row : rows) {
            Object[] values = (Object[]) row;
            Number deviceId = (Number) values[0];
            String token = values[1] == null ? "" : values[1].toString();
            String preferredLocale = values[2] == null ? null : values[2].toString();
            devices.add(new DeviceTarget(deviceId.intValue(), token, appLocale(preferredLocale)));
        }
        return devices;
    }

    private static final class DeviceTarget {
        private final int userDeviceId;
        private final String fcmToken;
        private final String locale;

        private DeviceTarget(int userDeviceId, String fcmToken, String locale) {
            this.userDeviceId = userDeviceId;
            this.fcmToken = fcmToken;
            this.locale = locale;
        }
    }

    public static final class PushText {
        private final String title;
        private final String body;

        public PushText(String title, String body) {
            this.title = title;
            this.body = body;
        }
    }

    private UserDevice findByToken(String fcmToken) {
        try {
            return em.createNamedQuery("UserDevice.findByFcmToken", UserDevice.class)
                .setParameter("fcmToken", fcmToken)
                .getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    private static List<Long> copyLongs(List<Long> values) {
        return values == null ? new ArrayList<>() : new ArrayList<>(values);
    }

    private static List<Integer> copyInts(List<Integer> values) {
        return values == null ? new ArrayList<>() : new ArrayList<>(values);
    }
}
