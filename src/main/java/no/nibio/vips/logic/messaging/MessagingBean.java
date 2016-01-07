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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import no.nibio.vips.logic.entity.Message;
import no.nibio.vips.logic.entity.MessageLocale;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.util.SessionControllerGetter;

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
                ml.getHeading(), ml.getLeadParagraph(), ml.getBody(), "http://localhost:8000/messages/" + m.getMessageId()
        );
        um.setExpiresAt(new Date());
        List<MessageRecipient> distributionList = new ArrayList<>();
        for(VipsLogicUser user : allUsers)
        {
            MessageRecipient r = new MessageRecipient(
                    String.valueOf(user.getUserId()),
                    user.getFirstName() + " " + user.getLastName(), 
                    "Mail", 
                    user.getEmail()
            );
            distributionList.add(r);
        }
        um.setDistributionList(distributionList);
        //em.persist(um);
        
        List<UniversalMessage> ums = em.createNamedQuery("UniversalMessage.findAll").getResultList();
        for(UniversalMessage uMe : ums)
        {
            System.out.println(uMe.getMsgSubject());
        }
    }
    
    public void sendUniversalMessage(Message message)
    {
        // Create a universal message from the message
        // TODO: When UniversalMessage has changed, pick
        MessageLocale ml = message.getLocalMessageWithFallback("nb", "nb");
        // Find the suscribers, create distribution list
        
        // Store it
        
        // Send it
        
        // Log it
        
    }
}
