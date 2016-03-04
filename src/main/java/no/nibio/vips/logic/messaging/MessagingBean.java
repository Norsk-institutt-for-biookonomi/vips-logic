/*
 * Copyright (c) 2015 NIBIO <http://www.nibio.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the NIBIO Open Source License as published by 
 * NIBIO, either version 1 of the License, or (at your option) any
 * later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * NIBIO Open Source License for more details.
 * 
 * You should have received a copy of the NIBIO Open Source License
 * along with VIPSLogic.  If not, see <http://www.nibio.no/licenses/>.
 * 
 */

package no.nibio.vips.logic.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.icu.text.MessageFormat;
import com.ibm.icu.util.ULocale;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.ws.rs.core.Response;
import no.nibio.vips.entity.Result;
import no.nibio.vips.logic.entity.ForecastConfiguration;
import no.nibio.vips.logic.entity.ForecastResult;
import no.nibio.vips.logic.entity.Message;
import no.nibio.vips.logic.entity.MessageLocale;
import no.nibio.vips.logic.entity.Observation;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.util.Globals;
import no.nibio.vips.logic.util.RESTAuthenticator;
import no.nibio.vips.logic.util.SystemTime;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

/**
 * @copyright 2015 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Stateless
public class MessagingBean {
    @PersistenceContext(unitName="VIPSLogic-PU")
    EntityManager em;
    
    public void sendUniversalMessage(Message message)
    {
        String msgDownloadUrlTpl = "http://www.vips-landbruk.no/";
        // Create a universal message from the message
        // TODO: When UniversalMessage has changed, pick
        UniversalMessage uMessage = new UniversalMessage();
        uMessage.setExpiresAt(message.getDateValidTo());
        for(MessageLocale ml : message.getMessageLocaleSet())
        {
            uMessage.addMessageLocalVersion(ml.getMessageLocalePK().getLocale(), ml.getHeading(), ml.getLeadParagraph(), ml.getBody(), msgDownloadUrlTpl);
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
        
        ResteasyClient client = new ResteasyClientBuilder().build();
        //client.register(new RESTAuthenticator("user", "userPass"));
        client.register(new RESTAuthenticator("VIPSLogic", "plmoknijbuhv000"));
        //ResteasyWebTarget target = client.target("http://kart13utv.ad.skogoglandskap.no:8080");
        ResteasyWebTarget target = client.target("http://localhost:8080");
        //ResteasyWebTarget target = client.target("http://logic.testvips2.nibio.no");
        UniversalMessagingServiceClient umClient = target.proxy(UniversalMessagingServiceClient.class);


        try
        {
            //System.out.println(new ObjectMapper().writeValueAsString(um));
            Response r = umClient.sendMessage(new ObjectMapper().writeValueAsString(uMessage));

            // TODO: Handle result

            String result = r.readEntity(String.class);
            System.out.println(result);
        }
        catch(JsonProcessingException ex)
        {
            // Handle error
        }
        
        return uMessage;
    }

    private List<MessageRecipient> getMessageNotificationSubscribers(Message message) {
        
        // TODO??? also filter by crops??
        Query q = em.createNativeQuery(
                "SELECT \n" +
                "	u.preferred_locale,\n" +
                "	umf.format_name AS type,\n" +
                "	CASE mns.universal_message_format_id " + 
                "           WHEN " + UniversalMessageFormat.FORMAT_EMAIL + " THEN u.email " +
                "           WHEN " + UniversalMessageFormat.FORMAT_SMS + " THEN u.phone " +
                "           ELSE '' " +
                "       END AS msg_delivery_address, \n" + // Needs update as more options are added
                "	u.first_name || ' ' || u.last_name AS name,\n" +
                "	u.user_id AS recipient_id\n" +
                "FROM public.vips_logic_user u, messaging.message_notification_subscription mns, messaging.universal_message_format umf\n" +
                "WHERE mns.user_id=u.user_id\n" +
                "AND mns.universal_message_format_id = umf.universal_message_format_id\n" +
                "AND u.user_id IN (\n" +
                "	SELECT mns.user_id FROM messaging.message_notification_subscription mns, public.vips_logic_user u \n" +
                "	WHERE  mns.user_id = u.user_id " +
                (message.getMessageTagIds() != null && !message.getMessageTagIds().isEmpty() ? "       AND mns.message_tag_ids && ARRAY" + message.getMessageTagIds().toString() + " \n" : "" )+ // && is the array_overlaps operator
                "       AND (mns.universal_message_format_id <> " + UniversalMessageFormat.FORMAT_SMS + " OR (mns.universal_message_format_id = " + UniversalMessageFormat.FORMAT_SMS + " AND u.approves_sms_billing IS TRUE))" + 
                ");\n",
                MessageRecipient.class);
        
        //q.setParameter("messageTagIds", message.getMessageTagIds());
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
                + "WHERE f.forecast_configuration_id IN ("
                + "     SELECT forecast_configuration_id FROM forecast_result WHERE result_valid_time between :dateStart AND :dateEnd"
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
        for(ForecastConfiguration conf:forecastConfigurations)
        {
            List<ForecastResult> results = em.createNamedQuery("ForecastResult.findByForecastConfigurationId", ForecastResult.class)
                    .setParameter("forecastConfigurationId", conf.getForecastConfigurationId())
                    .getResultList();
            
            Integer previousWarningStatus = Result.WARNING_STATUS_NO_WARNING;
            
            // There may be several new events for the next few days. 
            // If you find a NEW TO_RED event first, skip the rest
            // If you find a NEW GREEN_TO_YELLOW event first, add it and search for a new TO_RED event
            for(ForecastResult result:results)
            {
                if(result.getResultValidTime().before(systemTime) || result.getResultValidTime().after(in10Days))
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
                            result.getResultValidTime()
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
                    fConf.getPestOrganismId().getLocalName(locale),
                    fConf.getCropOrganismId().getLocalName(locale),
                    fConf.getLocationPointOfInterestId().getName(),
                    format.format(newNotification.getForecastNotificationLogPK().getEventDate()),
                    detailsUrl
                };
                
                uMessage.addMessageLocalVersion(locale, headingTemplate, "",MessageFormat.format(bodyTemplate, templateParts),detailsUrl);
                
            }
            uMessage.setDistributionList(this.getForecastEventNotificationSubscribers(fConf));
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
        String msgDownloadUrlTpl = "http://www.vips-landbruk.no/";
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
        uMessage.addMessageLocalVersion(observer.getPreferredLocale(), observation.getObservationHeading(), "", observation.getObservationText(), msgDownloadUrlTpl);
        
        // Find the suscribers, create distribution list
        uMessage.setDistributionList(this.getObservationNotificationSubscribers(observation));
        
        // Send it
        this.sendUniversalMessage(uMessage);
        // Log it
        
    }

    private List<MessageRecipient> getObservationNotificationSubscribers(Observation observation) {
        Query q = em.createNativeQuery(
                "SELECT \n" +
                "	u.preferred_locale,\n" +
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
                "       AND (ons.universal_message_format_id <> " + UniversalMessageFormat.FORMAT_SMS + " OR (ons.universal_message_format_id = " + UniversalMessageFormat.FORMAT_SMS + " AND u.approves_sms_billing IS TRUE))" + 
                ");\n",
                MessageRecipient.class)
                .setParameter("cropOrganismId", observation.getCropOrganismId());
        
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
