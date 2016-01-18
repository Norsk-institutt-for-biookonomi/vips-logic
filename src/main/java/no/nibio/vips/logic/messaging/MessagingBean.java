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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.ws.rs.core.Response;
import no.nibio.vips.logic.entity.Message;
import no.nibio.vips.logic.entity.MessageLocale;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.util.RESTAuthenticator;
import no.nibio.vips.logic.util.SessionControllerGetter;
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
    
    public void testStoreUniversalMessage()
    {
        List<VipsLogicUser> allUsers=SessionControllerGetter.getUserBean().getAllUsers();
        Message m = SessionControllerGetter.getMessageBean().getMessage(5);
        MessageLocale ml = m.getLocalMessage("nn");
        UniversalMessage um = new UniversalMessage(
                "nn",ml.getHeading(), ml.getLeadParagraph(), ml.getBody(), "http://localhost:8000/messages/" + m.getMessageId()
        );
        um.setExpiresAt(new Date());
        List<MessageRecipient> distributionList = new ArrayList<>();
        for(VipsLogicUser user : allUsers)
        {
            MessageRecipient r = new MessageRecipient(
                    String.valueOf(user.getUserId()),
                    user.getFirstName() + " " + user.getLastName(), 
                    "Mail", 
                    user.getEmail(),
                    user.getPreferredLocale()
            );
            distributionList.add(r);
        }
        um.setDistributionList(distributionList);
        //em.persist(um);
        
        List<UniversalMessage> ums = em.createNamedQuery("UniversalMessage.findAll").getResultList();
        for(UniversalMessage uMe : ums)
        {
            for(MessageLocalVersion lv:uMe.getMessageLocalVersionObjects())
            {
                System.out.println(lv.getMsgSubject());
            }
            
        }
    }
    
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
    
    public void sendUniversalMessage(UniversalMessage uMessage)
    {
        if(uMessage.getDistributionListObjects().isEmpty())
        {
            return;
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
        }
        catch(JsonProcessingException ex)
        {
            // Handle error
        }
    }

    private List<MessageRecipient> getMessageNotificationSubscribers(Message message) {
        
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
                "	SELECT user_id FROM messaging.message_notification_subscription\n" +
                "	WHERE  message_tag_ids && ARRAY" + message.getMessageTagIds().toString() + " \n" + // && is the array_overlaps operator
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
}
