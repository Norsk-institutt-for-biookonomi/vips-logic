package no.nibio.vips.logic.messaging;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

    /**
     * @return true if at least one FCM send succeeded
     */
    public boolean notifyForecast(Long forecastConfigurationId, Integer organizationId, String title, String body) {
        if (forecastConfigurationId == null || organizationId == null || !fcmClient.isEnabled()) {
            return false;
        }
        List<UserDevice> devices = em.createNativeQuery(
                "SELECT d.user_device_id, d.user_id, d.fcm_token, d.platform, d.updated_at "
                    + "FROM messaging.user_device d "
                    + "JOIN messaging.app_notification_preference p ON p.user_id = d.user_id "
                    + "JOIN public.vips_logic_user u ON u.user_id = d.user_id "
                    + "WHERE p.forecast_status_enabled "
                    + "AND :forecastId = ANY(p.forecast_configuration_ids) "
                    + "AND u.organization_id = :organizationId",
                UserDevice.class)
            .setParameter("forecastId", forecastConfigurationId)
            .setParameter("organizationId", organizationId)
            .getResultList();
        return sendToDevices(devices, title, body, "forecast", String.valueOf(forecastConfigurationId));
    }

    /**
     * @return true if at least one FCM send succeeded
     */
    public boolean notifyMessage(Integer messageId, List<Integer> cropCategoryIds, Integer organizationId, String title, String body) {
        if (messageId == null || organizationId == null || !fcmClient.isEnabled()) {
            return false;
        }
        List<Integer> cropIds = cropCategoryIds == null
            ? List.of()
            : cropCategoryIds.stream().filter(id -> id != null).collect(Collectors.toList());
        if (cropIds.isEmpty()) {
            return false;
        }
        List<UserDevice> devices = em.createNativeQuery(
                "SELECT d.user_device_id, d.user_id, d.fcm_token, d.platform, d.updated_at "
                    + "FROM messaging.user_device d "
                    + "JOIN messaging.app_notification_preference p ON p.user_id = d.user_id "
                    + "JOIN public.vips_logic_user u ON u.user_id = d.user_id "
                    + "WHERE p.message_enabled "
                    + "AND EXISTS ("
                    + "    SELECT 1 FROM unnest(p.message_crop_category_ids) AS cid "
                    + "    WHERE cid IN (:cropIds)"
                    + ") "
                    + "AND u.organization_id = :organizationId",
                UserDevice.class)
            .setParameter("cropIds", cropIds)
            .setParameter("organizationId", organizationId)
            .getResultList();
        return sendToDevices(devices, title, body, "message", String.valueOf(messageId));
    }

    private boolean sendToDevices(List<UserDevice> devices, String title, String body, String type, String id) {
        boolean sent = false;
        for (UserDevice device : devices) {
            try {
                FcmClient.SendResult result = fcmClient.send(device.getFcmToken(), title, body, type, id);
                if (result == FcmClient.SendResult.SENT) {
                    sent = true;
                } else if (result == FcmClient.SendResult.INVALID_TOKEN) {
                    em.createNamedQuery("UserDevice.deleteByFcmToken")
                        .setParameter("fcmToken", device.getFcmToken())
                        .executeUpdate();
                }
            } catch (Exception ex) {
                LOGGER.warn("FCM send failed for device {}", device.getUserDeviceId(), ex);
            }
        }
        return sent;
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
