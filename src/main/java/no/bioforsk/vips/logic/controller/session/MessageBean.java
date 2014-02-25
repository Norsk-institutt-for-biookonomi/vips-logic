/*
 * Copyright (c) 2014 Bioforsk <http://www.bioforsk.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with VIPSLogic.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package no.bioforsk.vips.logic.controller.session;

import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import no.bioforsk.vips.logic.entity.Message;
import no.bioforsk.vips.logic.entity.MessageIllustration;
import no.bioforsk.vips.logic.entity.MessageIllustrationCaptionLocale;
import no.bioforsk.vips.logic.entity.MessageIllustrationCaptionLocalePK;
import no.bioforsk.vips.logic.entity.MessageIllustrationPK;
import no.bioforsk.vips.logic.entity.MessageLocale;
import no.bioforsk.vips.logic.entity.MessageLocalePK;
import no.bioforsk.vips.logic.entity.MessageTag;
import no.bioforsk.vips.logic.entity.VipsLogicUser;
import no.bioforsk.web.forms.FormField;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

/**
 * @copyright 2014 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
@Stateless
public class MessageBean {
    @PersistenceContext(unitName="VIPSLogic-PU")
    EntityManager em;

    public List<Message> getMessageList(Integer organizationId) {
        return em.createNamedQuery("Message.findByOrganizationId", Message.class).setParameter("organizationId", organizationId).getResultList();
    }

    public Message storeMessage(Map<String, FormField> formFields, VipsLogicUser user) {
        Integer messageId = formFields.get("messageId").getValueAsInteger();
        Message message;
        // New or existing message?
        if(messageId > 0)
        {
            message = em.find(Message.class, messageId);
        }
        else
        {
            message = new Message();
            em.persist(message);
        }
        // Add data
        message.setDatePub(formFields.get("datePub").getValueAsDate());
        message.setDateValidTo(formFields.get("dateValidTo").getValueAsDate());
        message.setOrganizationId(user.getOrganizationId().getOrganizationId());
        
        // Storing language specific fields
        // New or existing language version?
        MessageLocale messageLocale = message.getLocalMessage(formFields.get("locale").getWebValue());
        if(messageLocale == null)
        {
            messageLocale = new MessageLocale();
            messageLocale.setMessageLocalePK(new MessageLocalePK(message.getMessageId(), formFields.get("locale").getWebValue()));
            messageLocale.setCreatedTimestamp(new Date());
        }
        else
        {
            messageLocale.setModifiedTimestamp(new Date());
        }
        // Add data
        messageLocale.setHeading(formFields.get("heading").getWebValue());
        messageLocale.setLeadParagraph(formFields.get("leadParagraph").getWebValue());
        messageLocale.setBody(formFields.get("body").getWebValue());
        em.persist(messageLocale);
        
        // TODO Storing message tags
        Set<MessageTag> tags = new HashSet<>();
        if(formFields.get("messageTagIds").getWebValues() != null)
        {
            for(String messageTagId : formFields.get("messageTagIds").getWebValues())
            {
                tags.add(em.find(MessageTag.class, Integer.valueOf(messageTagId)));
            }
        }
        message.setMessageTagSet(tags);
        
        
        return message;
    }

    public Message storeMessageIllustration(Message message, FileItem item) throws Exception {
        // Create a candidate filename
        // [MESSAGE_ILLUSTRATION_PATH]/[ORGANIZATION_ID]/[MESSAGE_ID]_illustration.[fileExtension]
        String filePath = System.getProperty("no.bioforsk.vips.logic.MESSAGE_ILLUSTRATION_PATH") + "/" 
                        + message.getOrganizationId();
        String fileName = message.getMessageId() + "_illustration." + FilenameUtils.getExtension(item.getName());
        // Check availability, and adapt filename until available
        Integer fileNameSuffix = 1;
        File illustration = new File(filePath + "/" + fileName);
        while(illustration.exists())
        {
            fileName = message.getMessageId() + "_illustration_" + fileNameSuffix + "." + FilenameUtils.getExtension(item.getName());
            illustration = new File(filePath + "/" + fileName);
            fileNameSuffix++;
        }
        File testDirectoryfile = new File(filePath);
        // If directory does not exist, create it
        if(!testDirectoryfile.exists())
        {
            testDirectoryfile.mkdirs();
        }

        // Store file
        item.write(illustration);
        
        // Update MessageIllustrations
        message = em.merge(message);
        // Remove the old illustration(s)
        List <MessageIllustration> formerIllustrations = em.createNamedQuery("MessageIllustration.findByMessageId").setParameter("messageId", message.getMessageId()).getResultList();
        for(MessageIllustration formerIllustration:formerIllustrations)
        {
            System.out.println("removing " + formerIllustration.toString());
            em.remove(formerIllustration);
        }
        // Also remove their relation to message.
        if(message.getMessageIllustrationSet() != null)
        {
            message.getMessageIllustrationSet().clear();
        }
        
        MessageIllustration newIllustration = new MessageIllustration(new MessageIllustrationPK(message.getMessageId(), fileName));
        em.persist(newIllustration);
        
        // Add the new illustration 
        if(message.getMessageIllustrationSet() == null)
        {
            message.setMessageIllustrationSet(new HashSet<MessageIllustration>());
        }
        message.getMessageIllustrationSet().add(newIllustration);
        //message.getMessageIllustrationSet().add(newIllustration);
        return message;
        
    }

    public Message deleteMessageIllustration(Message message) {
        message = em.merge(message);
        
        Set <MessageIllustration> formerIllustrations = message.getMessageIllustrationSet();
        for(MessageIllustration formerIllustration:formerIllustrations)
        {
            em.remove(formerIllustration);
        }
        message.getMessageIllustrationSet().clear();
        return message;
    }

    public void updateMessageIllustrationCaption(Message message, String newCaptionText, String locale) {
        message = em.merge(message);
        if(message.getMessageIllustrationSet() != null && ! message.getMessageIllustrationSet().isEmpty())
        {
            MessageIllustration illustration = message.getMessageIllustrationSet().iterator().next();
            MessageIllustrationCaptionLocale caption = illustration.getLocalMessageIllustrationCaption(locale);
            if(caption == null)
            {
                caption = new MessageIllustrationCaptionLocale();
                MessageIllustrationCaptionLocalePK pk = new MessageIllustrationCaptionLocalePK(
                        illustration.getMessageIllustrationPK().getMessageId(), 
                        illustration.getMessageIllustrationPK().getFileName(), 
                        locale
                );
                caption.setMessageIllustrationCaptionLocalePK(pk);
                em.persist(caption);
            }
            caption.setCaption(newCaptionText);
        }
        //else { System.out.println("No illustration found for messg"); }
    }

    public List<MessageTag> getAllMessageTags() {
        return em.createNamedQuery("MessageTag.findAll").getResultList();
    }

    public Message getMessage(Integer messageId) {
        return em.find(Message.class, messageId);
    }

    public List<Message> getMessageListWithTags(Integer organizationId, List<Integer> tagIds) {
        Query q = em.createNativeQuery("SELECT * FROM message WHERE message_id IN ("
                + "SELECT message_id FROM message_message_tag "
                + "WHERE message_tag_id IN (:tagIds)"
                + ")"
                + "AND organization_id = :organizationId", Message.class);
        q.setParameter("tagIds", tagIds);
        q.setParameter("organizationId", organizationId);
        return q.getResultList();
    }
    

    public List<MessageTag> getMessageTagList() {
        return em.createNamedQuery("MessageTag.findAll", MessageTag.class).getResultList();
    }
    

}
