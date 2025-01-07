/*
 * Copyright (c) 2015 NIBIO <http://www.nibio.no/>. 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package no.nibio.vips.logic.messaging;

import com.ibm.icu.text.MessageFormat;
import com.ibm.icu.util.ULocale;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import no.nibio.vips.entity.Result;
import no.nibio.vips.logic.entity.ForecastConfiguration;
import no.nibio.vips.logic.entity.ForecastResult;
import no.nibio.vips.logic.entity.Message;
import no.nibio.vips.logic.entity.MessageLocale;
import no.nibio.vips.logic.entity.Observation;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.messaging.distribution.IVipsMessageHandler;
import no.nibio.vips.logic.messaging.distribution.VipsMessageInputHandler;
import no.nibio.vips.logic.messaging.distribution.entity.VipsMessage;
import no.nibio.vips.logic.util.Globals;
import no.nibio.vips.logic.util.SystemTime;
import org.slf4j.LoggerFactory;

/**
 * @copyright 2015-2022 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Stateless
public class MessagingBean {
    @PersistenceContext(unitName="VIPSLogic-PU")
    EntityManager em;
    
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MessagingBean.class);
    
    private Map<Integer, UniversalMessageFormat> universalMessageFormats;
    
    public UniversalMessageFormat getUniversalMessageFormat(Integer universalMessageFormatId)
    {
        if(this.universalMessageFormats == null)
        {
            this.universalMessageFormats = new HashMap<>();
            this.universalMessageFormats.put(UniversalMessageFormat.FORMAT_SMS, em.find(UniversalMessageFormat.class, UniversalMessageFormat.FORMAT_SMS));
            this.universalMessageFormats.put(UniversalMessageFormat.FORMAT_EMAIL, em.find(UniversalMessageFormat.class, UniversalMessageFormat.FORMAT_EMAIL));
        }
        return this.universalMessageFormats.get(universalMessageFormatId);
    }
    
    public void sendUniversalMessage(Message message)
    {
        // TODO: Make URL relative
        String msgDownloadUrlTpl = "https://www.vips-landbruk.no/messages/" + message.getMessageId() + "/";
        // Create a universal message from the message
        // TODO: When UniversalMessage has changed, pick
        UniversalMessage uMessage = new UniversalMessage();
        uMessage.setExpiresAt(message.getDateValidTo());
        for(MessageLocale ml : message.getMessageLocaleSet())
        {
            if(ml.getHeading() == null || ml.getHeading().isEmpty())
            {
                continue;
            }
            uMessage.addMessageLocalVersion(ml.getMessageLocalePK().getLocale(), ml.getHeading(), ml.getLeadParagraph(), ml.getBody(), msgDownloadUrlTpl);
        }
        
        // If no headings in any locale, skip this
        if(uMessage.getMessageLocalVersionObjects() == null || uMessage.getMessageLocalVersionObjects().isEmpty())
        {
            return;
        }
        
        // Find the suscribers, create distribution list
        uMessage.setDistributionList(this.getMessageNotificationSubscribers(message));
        
        // Send it
        this.sendUniversalMessage(uMessage);
        // Log it
        
    }
    
    public UniversalMessage sendUniversalMessage(UniversalMessage uMessage)
    {
        if(uMessage.getDistributionListObjects().isEmpty())
        {
            return uMessage;
        }
        
        // Store it
        em.persist(uMessage);
        
        
        // TODO Handle errors better!
        try
        {
            if  (
                System.getProperty("no.nibio.vips.logic.DISABLE_MESSAGING_SYSTEM") == null 
                || System.getProperty("no.nibio.vips.logic.DISABLE_MESSAGING_SYSTEM").equals("false")
            )
            {
                IVipsMessageHandler vipsMessageHandler = new VipsMessageInputHandler();
                vipsMessageHandler.sendMessage(new VipsMessage(uMessage));
            }
            else
            {
                LOGGER.debug("Messaging system disabled. This message was not sent: " + uMessage.toString());
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        return uMessage;
    }

    /**
     * Based on the message characteristics (type(s) of message and what crop(s) it concerns), 
     * pull the subscribing users from the database
     * @param message
     * @return
     */
    private List<MessageRecipient> getMessageNotificationSubscribers(Message message) {
        String sql = 
        
                "SELECT \n" +
                "	u.preferred_locale,\n" +
                "       u.free_sms, \n" + 
                "	umf.format_name AS type,\n" +
                "	CASE mns.universal_message_format_id " + 
                "           WHEN " + UniversalMessageFormat.FORMAT_EMAIL + " THEN u.email " +
                "           WHEN " + UniversalMessageFormat.FORMAT_SMS + " THEN u.phone " +
                "           ELSE '' " +
                "       END AS msg_delivery_address, \n" + // Needs update as more options are added
                "	u.first_name || ' ' || u.last_name AS name, \n" +
                "	u.user_id AS recipient_id \n" +
                "FROM public.vips_logic_user u, messaging.message_notification_subscription mns, messaging.universal_message_format umf \n" +
                "WHERE mns.user_id=u.user_id \n" +
                "AND mns.universal_message_format_id = umf.universal_message_format_id \n" +
                "AND u.user_id IN ( \n" +
                "	SELECT mns.user_id FROM messaging.message_notification_subscription mns, public.vips_logic_user u \n" +
                "	WHERE  mns.user_id = u.user_id \n" +
                "       and u.organization_id = " + message.getOrganizationId() +
                (message.getCropCategoryIds() != null && message.getCropCategoryIds().size() != 0 ? "       AND mns.crop_category_ids && ARRAY" + Arrays.asList(message.getCropCategoryIds()).toString() + " \n" : "") +
                (message.getMessageTagIds() != null && !message.getMessageTagIds().isEmpty() ? "       AND mns.message_tag_ids && ARRAY" + message.getMessageTagIds().toString() + " \n" : "" )+ // && is the array_overlaps operator
                "       AND (mns.universal_message_format_id <> " + UniversalMessageFormat.FORMAT_SMS + " OR (mns.universal_message_format_id = " + UniversalMessageFormat.FORMAT_SMS + " AND u.approves_sms_billing IS TRUE))" + 
                "); \n";
        LOGGER.debug(sql);
        Query q = em.createNativeQuery(sql,
                MessageRecipient.class);
        
        return q.getResultList();
    }
    
    public MessageNotificationSubscription getMessageNotificationSubscription(Integer userId)
    {
        Query q = em.createNativeQuery(
                "SELECT * FROM messaging.message_notification_subscription m "
                + "WHERE m.user_id=:userId",
                MessageNotificationSubscription.class
        ).setParameter("userId", userId);
        try
        {
            return (MessageNotificationSubscription) q.getSingleResult();
        }
        catch(NoResultException ex)
        {
            return null;
        }
    }
    
    public void storeMessageNotificationSubscription(MessageNotificationSubscription subscription)
    {
        em.merge(subscription);
    }
    
    public List<UniversalMessageFormat> getAllUniversalMessageFormats()
    {
        return em.createNamedQuery("UniversalMessageFormat.findAll").getResultList();
    }
    
    // For getting all available notification translations
    public final static String[] AVAILABLE_FORECAST_NOTIFICATION_LOCALES = {"nb","en"};
    
    public void sendForecastEventNotifications()
    {
        // Find forecast configurations with a change from green to yellow or [all colors] to red
        // in the future, and when that is
        // Meaning: Create a list of forecast_notification_logs items
        // First: Find all forecast configurations with results for the next 10 days
        Date systemTime = SystemTime.getSystemTime();
        Calendar cal = Calendar.getInstance();
        cal.setTime(systemTime);
        cal.add(Calendar.DATE, 10);
        Date in10Days = cal.getTime();
        List<ForecastConfiguration> forecastConfigurations = em.createNativeQuery(
                "SELECT * FROM public.forecast_configuration f "
                + "WHERE NOT f.is_private AND f.forecast_configuration_id IN ("
                + "     SELECT forecast_configuration_id FROM forecast_result WHERE valid_time_start between :dateStart AND :dateEnd"
                + ")",
                ForecastConfiguration.class
            )
            .setParameter("dateStart", systemTime)
            .setParameter("dateEnd", in10Days)
            .getResultList();
        
        List<ForecastNotificationLog> newNotifications = new ArrayList<>();
        ForecastEvent toRed = em.find(ForecastEvent.class, ForecastEvent.TO_RED);
        ForecastEvent greenToYellow = em.find(ForecastEvent.class, ForecastEvent.GREEN_TO_YELLOW);
        ForecastEvent currentEvent = null;
        Query findConfQ = em.createNamedQuery("ForecastResult.findByForecastConfigurationId", ForecastResult.class);
        for(ForecastConfiguration conf:forecastConfigurations)
        {
            LOGGER.debug("Now working with forecastConfiguration with id=" + conf.getForecastConfigurationId());
            List<ForecastResult> results = findConfQ
                    .setParameter("forecastConfigurationId", conf.getForecastConfigurationId())
                    .getResultList();
            
            Integer previousWarningStatus = Result.WARNING_STATUS_NO_WARNING;
            
            // There may be several new events for the next few days. 
            // If you find a NEW TO_RED event first, skip the rest
            // If you find a NEW GREEN_TO_YELLOW event first, add it and search for a new TO_RED event
            for(ForecastResult result:results)
            {
                if(result.getValidTimeStart().before(systemTime) || result.getValidTimeStart().after(in10Days))
                {
                    continue;
                }
                // TO_RED event detected
                if( 
                        result.getWarningStatus().equals(Result.WARNING_STATUS_HIGH_RISK)
                        && !previousWarningStatus.equals(Result.WARNING_STATUS_HIGH_RISK) 
                )
                {
                    currentEvent = toRed;
                }
                // GREEN_TO_YELLOW event detehcted
                else if(
                        result.getWarningStatus().equals(Result.WARNING_STATUS_MINOR_RISK)
                        && ! previousWarningStatus.equals(Result.WARNING_STATUS_MINOR_RISK)
                        && ! previousWarningStatus.equals(Result.WARNING_STATUS_HIGH_RISK)
                        )
                {
                    currentEvent = greenToYellow;
                }
                else
                {
                    currentEvent = null;
                }
                
                if(currentEvent != null)
                {
                    ForecastNotificationLogPK pk = new ForecastNotificationLogPK(
                            conf.getForecastConfigurationId(), 
                            currentEvent.getForecastEventId(), 
                            result.getValidTimeStart()
                    );
                    
                    // Could not find same event. We persist it so we won't
                    // have any more of the same events the same day
                    if(em.find(ForecastNotificationLog.class, pk) == null)
                    {
                        ForecastNotificationLog newNotification = new ForecastNotificationLog(pk);
                        newNotification.setCreatedTime(SystemTime.getSystemTime());
                        em.persist(newNotification);
                        newNotifications.add(newNotification);
                    }
                    if(currentEvent.getForecastEventId().equals(ForecastEvent.TO_RED))
                    {
                        break;
                    }
                }
                previousWarningStatus = result.getWarningStatus();
            }
        }
        
        SimpleDateFormat format = new SimpleDateFormat(Globals.defaultDateFormat);
        
        // For the new ones: Create and send Universal messages
        for(ForecastNotificationLog newNotification:newNotifications)
        {
            ForecastConfiguration fConf = em.find(ForecastConfiguration.class, newNotification.getForecastNotificationLogPK().getForecastConfigurationId());
            UniversalMessage uMessage = new UniversalMessage();
            // Get all translations for this, create allMessageLocalVersions
            for(String locale:MessagingBean.AVAILABLE_FORECAST_NOTIFICATION_LOCALES)
            {
                ResourceBundle localBundle = ResourceBundle.getBundle("no.nibio.vips.logic.i18n.vipslogictexts",new ULocale(locale).toLocale());
                 String headingTemplate = localBundle.getString("forecastNotificationMessageHeadingTpl_" + 
                        newNotification.getForecastNotificationLogPK().getForecastEventId()
                );
                String bodyTemplate = localBundle.getString("forecastNotificationMessageBodyTpl_" + 
                        newNotification.getForecastNotificationLogPK().getForecastEventId()
                );
                
                // Template is as follows (in English):
                // Forecast warning status has turned to high risk of infection 
                //for {0} in {1} at location {2} at date {3}. Model: {4}. To 
                //read details, please visit: {5}
                String detailsUrl = fConf.getVipsLogicUserId().getOrganizationId().getVipswebUrl() + "forecasts/" + fConf.getForecastConfigurationId();
                Object[] templateParts = {
                    (fConf.getPestOrganismId().getLocalName(locale).isEmpty() ? fConf.getPestOrganismId().getLatinName() : fConf.getPestOrganismId().getLocalName(locale)),
                    (fConf.getCropOrganismId().getLocalName(locale).isEmpty() ? fConf.getCropOrganismId().getLatinName() : fConf.getCropOrganismId().getLocalName(locale)),
                    fConf.getLocationPointOfInterestId().getName(),
                    format.format(newNotification.getForecastNotificationLogPK().getEventDate()),
                    detailsUrl
                };
                
                uMessage.addMessageLocalVersion(locale, headingTemplate, "",MessageFormat.format(bodyTemplate, templateParts),detailsUrl);
                
            }
            List<MessageRecipient> distributionList = this.getForecastEventNotificationSubscribers(fConf);
            if(distributionList == null || distributionList.isEmpty())
            {
                em.remove(newNotification);
            }
            uMessage.setDistributionList(distributionList);
            cal.setTime(systemTime);
            cal.add(Calendar.DATE, 1);
            uMessage.setExpiresAt(cal.getTime());
            
            this.sendUniversalMessage(uMessage);
            // If sending was successful, we also store the universalMessage id in the events
            if(uMessage.getUniversalMessageId() != null)
            {
                newNotification.setUniversalMessageId(uMessage.getUniversalMessageId());
            }
            // Otherwise, we remove the events!
            else
            {
                em.remove(newNotification);
            }
        }
    }
    
    public List<MessageRecipient> getForecastEventNotificationSubscribers(ForecastConfiguration config)
    {
        String sql = "SELECT \n" +
                "	u.preferred_locale,\n" +
                "       u.free_sms, \n" + 
                "	umf.format_name AS type,\n" +
                "	CASE fns.universal_message_format_id " + 
                "           WHEN " + UniversalMessageFormat.FORMAT_EMAIL + " THEN u.email " +
                "           WHEN " + UniversalMessageFormat.FORMAT_SMS + " THEN u.phone " +
                "           ELSE '' " +
                "       END AS msg_delivery_address, \n" + // Needs update as more options are added
                "	u.first_name || ' ' || u.last_name AS name,\n" +
                "	u.user_id AS recipient_id\n" +
                "FROM public.vips_logic_user u, messaging.forecast_event_notification_subscription fns, messaging.universal_message_format umf \n" +
                "WHERE fns.user_id=u.user_id\n" +
                "AND fns.universal_message_format_id = umf.universal_message_format_id\n" +
                "AND u.user_id IN (\n" +
                "	SELECT u.user_id FROM messaging.forecast_event_notification_subscription fens, public.vips_logic_user u \n" +
                "	WHERE :weatherStationId = ANY(fens.weather_station_ids) \n" +
                "       AND fens.crop_category_ids && ARRAY(SELECT crop_category_id FROM public.crop_category WHERE :cropOrganismId = ANY(crop_organism_ids))" + 
                "       AND fens.user_id = u.user_id \n" +
                "       AND u.organization_id = " + config.getVipsLogicUserId().getOrganizationId().getOrganizationId() +
                "       AND (fens.universal_message_format_id <> " + UniversalMessageFormat.FORMAT_SMS + " OR (fens.universal_message_format_id = " + UniversalMessageFormat.FORMAT_SMS + " AND u.approves_sms_billing IS TRUE))" + 
                ");\n";
        Query q = em.createNativeQuery(sql, MessageRecipient.class);
        
        q.setParameter("weatherStationId", config.getWeatherStationPointOfInterestId().getPointOfInterestId());
        q.setParameter("cropOrganismId", config.getCropOrganismId().getOrganismId());
        
        return q.getResultList();
    }

    public ForecastEventNotificationSubscription getForecastEventNotificationSubscription(Integer userId) {
        Query q = em.createNativeQuery(
                "SELECT * FROM messaging.forecast_event_notification_subscription m "
                + "WHERE m.user_id=:userId",
                ForecastEventNotificationSubscription.class
        ).setParameter("userId", userId);
        try
        {
            return (ForecastEventNotificationSubscription) q.getSingleResult();
        }
        catch(NoResultException ex)
        {
            return null;
        }
    }
    
    public void storeForecastEventNotificationSubscription(ForecastEventNotificationSubscription subscription)
    {
        em.merge(subscription);
    }

    public ObservationNotificationSubscription getObservationNotificationSubscription(Integer userId) {
        return em.find(ObservationNotificationSubscription.class, userId);
    }

    public void storeObservationNotificationSubscription(ObservationNotificationSubscription oSubscription) {
        em.merge(oSubscription);
    }
    
    public void sendUniversalMessage(Observation observation)
    {
        // Don't send empty messages
        if(observation.getObservationHeading() == null || observation.getObservationHeading().isEmpty())
        {
            return;
        }
        String msgDownloadUrlTpl = "https://www.vips-landbruk.no/observations/" + observation.getObservationId() + "/";
        // Create a universal message from the message
        // TODO: When UniversalMessage has changed, pick
        UniversalMessage uMessage = new UniversalMessage();
        // Expires by default 1 week after observation time
        Calendar cal = Calendar.getInstance();
        cal.setTime(observation.getTimeOfObservation());
        cal.add(Calendar.DATE, 7);
        uMessage.setExpiresAt(cal.getTime());
        // For locale, we assume observer's language
        VipsLogicUser observer = em.find(VipsLogicUser.class, observation.getUserId());
        uMessage.addMessageLocalVersion(observer.getPreferredLocale(), observation.getObservationHeading(), "", 
                observation.getObservationText()
                , msgDownloadUrlTpl);
        
        // Find the suscribers, create distribution list
        uMessage.setDistributionList(this.getObservationNotificationSubscribers(observation));
        
        // Send it
        this.sendUniversalMessage(uMessage);
        // Log it
        
    }

    private List<MessageRecipient> getObservationNotificationSubscribers(Observation observation) {
        VipsLogicUser observer = em.find(VipsLogicUser.class, observation.getUserId());
        String sql = "SELECT \n" +
                "	u.preferred_locale,\n" +
                "       u.free_sms, \n" + 
                "	umf.format_name AS type,\n" +
                "	CASE ons.universal_message_format_id " + 
                "           WHEN " + UniversalMessageFormat.FORMAT_EMAIL + " THEN u.email " +
                "           WHEN " + UniversalMessageFormat.FORMAT_SMS + " THEN u.phone " +
                "           ELSE '' " +
                "       END AS msg_delivery_address, \n" + // Needs update as more options are added
                "	u.first_name || ' ' || u.last_name AS name,\n" +
                "	u.user_id AS recipient_id\n" +
                "FROM public.vips_logic_user u, messaging.observation_notification_subscription ons, messaging.universal_message_format umf\n" +
                "WHERE ons.user_id=u.user_id\n" +
                "AND ons.universal_message_format_id = umf.universal_message_format_id\n" +
                "AND u.user_id IN (\n" +
                "	SELECT ons.user_id FROM messaging.observation_notification_subscription ons, public.vips_logic_user u \n" +
                "	WHERE  ons.crop_category_ids && ARRAY(SELECT crop_category_id FROM public.crop_category WHERE :cropOrganismId = ANY(crop_organism_ids)) \n" + 
                "       AND ons.user_id = u.user_id \n" +
                "       and u.organization_id = " + observer.getOrganizationId().getOrganizationId() +
                "       AND (ons.universal_message_format_id <> " + UniversalMessageFormat.FORMAT_SMS + " OR (ons.universal_message_format_id = " + UniversalMessageFormat.FORMAT_SMS + " AND u.approves_sms_billing IS TRUE))" + 
                ");\n";
        LOGGER.debug(sql);
        Query q = em.createNativeQuery(
                sql,
                MessageRecipient.class)
                .setParameter("cropOrganismId", observation.getCropOrganismId());
        LOGGER.debug(q.toString());
        return q.getResultList();
    }

    /**
     * Remove all traces of notification subscriptions from user
     * @param user 
     */
    public void deleteAllNotificationSubscriptions(VipsLogicUser user) {
        ObservationNotificationSubscription o = em.find(ObservationNotificationSubscription.class, user.getUserId());
        if(o != null)
        {
            em.remove(o);
        }
        MessageNotificationSubscription m = em.find(MessageNotificationSubscription.class, user.getUserId());
        if(m != null)
        {
            em.remove(m);
        }
        ForecastEventNotificationSubscription f = em.find(ForecastEventNotificationSubscription.class, user.getUserId());
        if(f != null)
        {
            em.remove(f);
        }
    }
}
