/*
 * Copyright (c) 2014 NIBIO <http://www.nibio.no/>. 
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

package no.nibio.vips.logic.controller.session;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import no.nibio.vips.logic.entity.Message;
import no.nibio.vips.logic.entity.MessageIllustration;
import no.nibio.vips.logic.entity.MessageIllustrationCaptionLocale;
import no.nibio.vips.logic.entity.MessageIllustrationCaptionLocalePK;
import no.nibio.vips.logic.entity.MessageIllustrationPK;
import no.nibio.vips.logic.entity.MessageLocale;
import no.nibio.vips.logic.entity.MessageLocalePK;
import no.nibio.vips.logic.entity.MessageTag;
import no.nibio.vips.logic.entity.Organization;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.message.MessageService;
import no.nibio.vips.logic.util.UploadedFileStore;
import no.nibio.web.forms.FormField;
import org.apache.commons.fileupload2.core.FileItem;
import org.apache.commons.io.FilenameUtils;

/**
 * @copyright 2014 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@ApplicationScoped
@Transactional
public class MessageBean {
    @Inject
    EntityManager em;

    @Inject
    MessageService messageService;

    public List<Message> getMessageList(Integer organizationId, Date publishedFrom, Date publishedTo)
    {
        return messageService.getMessageList(organizationId, publishedFrom, publishedTo);
    }
    
    
    
    public List<Message> getMessageList(Integer organizationId) {
        Organization mainOrg = em.find(Organization.class, organizationId);
        List<Organization> organizations = em.createNamedQuery("Organization.findAllRelated")
                .setParameter("organizationId",mainOrg).getResultList();
        List<Message> retVal = new ArrayList<Message>();
        for(Organization org : organizations)
        {
            retVal.addAll(em.createNamedQuery("Message.findByOrganizationId", Message.class).setParameter("organizationId", organizationId).getResultList());
        }
        return retVal;
    }
    
    // Returns all messages where the given user is the author
    public List<MessageLocale> getMessageLocaleList(VipsLogicUser user)
    {
        try
        {
            return em.createNamedQuery("MessageLocale.findByCreatedBy")
                    .setParameter("createdBy", user)
                    .getResultList();
        }
        catch(NoResultException ex)
        {
            return null;
        }
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
        message.setOrganizationId(formFields.get("organizationId") != null && formFields.get("organizationId").getWebValue() != null ?
                                    formFields.get("organizationId").getValueAsInteger()
                                    : user.getOrganizationId().getOrganizationId()
        );
        
        // Storing language specific fields
        // New or existing language version?
        MessageLocale messageLocale = message.getLocalMessage(formFields.get("locale").getWebValue());
        if(messageLocale == null)
        {
            messageLocale = new MessageLocale();
            messageLocale.setMessageLocalePK(new MessageLocalePK(message.getMessageId(), formFields.get("locale").getWebValue()));
            messageLocale.setCreatedTimestamp(new Date());
            messageLocale.setCreatedBy(user);
        }
        else
        {
            messageLocale.setModifiedTimestamp(new Date());
        }
        // Add data
        messageLocale.setHeading(formFields.get("heading").getWebValue());
        messageLocale.setLeadParagraph(formFields.get("leadParagraph").getWebValue());
        messageLocale.setBody(formFields.get("body").getWebValue());
        message.addMessageLocale(messageLocale);
        //em.persist(messageLocale);
        
        
        // Storing message tags
        Set<MessageTag> tags = new HashSet<>();
        if(formFields.get("messageTagIds").getWebValues() != null)
        {
            for(String messageTagId : formFields.get("messageTagIds").getWebValues())
            {
                tags.add(em.find(MessageTag.class, Integer.valueOf(messageTagId)));
            }
        }
        message.setMessageTagSet(tags);
        
        // Store crop categories
        List<Integer> cropCategoryIds = new ArrayList<>();
        if(formFields.get("cropCategoryIds").getWebValues() != null)
        {
            for(String cropCategoryId:formFields.get("cropCategoryIds").getWebValues())
            {
                cropCategoryIds.add(Integer.valueOf(cropCategoryId));
            }
        }
        message.setCropCategoryIds(cropCategoryIds);
       
        return message;
    }

    /**
     * 
     * @param message
     * @return [MESSAGE_ILLUSTRATION_PATH]/[ORGANIZATION_ID]/
     */
    private String getFilePath(Message message)
    {
        return UploadedFileStore.requireBasePath("no.nibio.vips.logic.MESSAGE_ILLUSTRATION_PATH")
                        + "/" + message.getOrganizationId();
    }
    
    public Message storeMessageIllustration(Message message, FileItem item) throws Exception {
        // Create a candidate filename
        // [MESSAGE_ILLUSTRATION_PATH]/[ORGANIZATION_ID]/[MESSAGE_ID]_illustration.[fileExtension]
        String filePath = this.getFilePath(message);
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
        UploadedFileStore.write(item, illustration);
        
        // Update MessageIllustrations
        message = em.merge(message);
        // Remove the old illustration(s)
        List <MessageIllustration> formerIllustrations = em.createNamedQuery("MessageIllustration.findByMessageId").setParameter("messageId", message.getMessageId()).getResultList();
        for(MessageIllustration formerIllustration:formerIllustrations)
        {
            //System.out.println("removing " + formerIllustration.toString());
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
        return messageService.getMessage(messageId);
    }

    public List<Message> getFilteredMessages(Integer organizationId, List<Integer> tagIds, Date datePubStart, Date datePubEnd) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("datePubStart", datePubStart);
        parameters.put("datePubEnd", datePubEnd);
        StringBuilder sql = new StringBuilder("SELECT * FROM message \n")
                .append("WHERE date_pub BETWEEN :datePubStart AND :datePubEnd \n");
        if(organizationId != null)
        {
            sql.append("AND organization_id IN (SELECT organization_id FROM organization WHERE organization_id=:organizationId or parent_organization_id = :organizationId) \n");
            parameters.put("organizationId", organizationId);
        }
        
        if(tagIds != null && ! tagIds.isEmpty())
        {
            sql.append("AND message_id IN (")
                    .append("   SELECT message_id FROM message_message_tag ")
                    .append("   WHERE message_tag_id IN (:tagIds)")
                    .append(")");
            parameters.put("tagIds", tagIds);
        }

        Query q = em.createNativeQuery(sql.toString(), Message.class);
        for(String key:parameters.keySet())
        {
            q.setParameter(key, parameters.get(key));
        }
        return q.getResultList();
    }
    
    public List<Message> getFilteredMessages(List<Integer> tagIds) {
        Query q = em.createNativeQuery("SELECT * FROM message WHERE message_id IN ("
                + "SELECT message_id FROM message_message_tag "
                + "WHERE message_tag_id IN (:tagIds)"
                + ")", Message.class);
        q.setParameter("tagIds", tagIds);
        return q.getResultList();
    }
    
    public List<Message> getCurrentFilteredMessagesForOrganization(List<Integer> tagIds, Integer organizationId) {
        return messageService.getCurrentFilteredMessagesForOrganization(tagIds, organizationId);
    }

    public List<MessageTag> getMessageTagList() {
        return messageService.getMessageTagList();
    }

    
    /**
     * Based on given ids from form, returns a double set of tags:
     * the ones selected (key="selected") and the ones not selected ("notSelected")
     * @param parameterValues
     * @return 
     */
    public Map<String, List<MessageTag>> getFilterMessageTags(List<Integer> selectedFilterMessageTagIds) {
        List<MessageTag> allTags, selectedTags, notSelectedTags;
        Map<String,List<MessageTag>> retVal = new HashMap<>();
        allTags = em.createNamedQuery("MessageTag.findAll").getResultList();
        if(selectedFilterMessageTagIds != null)
        {
            selectedTags = new ArrayList<>();
            notSelectedTags = new ArrayList<>();
            for(MessageTag messageTag:allTags)
            {
                if(selectedFilterMessageTagIds.contains(messageTag.getMessageTagId()))
                {
                    selectedTags.add(messageTag);
                }
                else
                {
                    notSelectedTags.add(messageTag);
                }
            }
        }
        else
        {
            selectedTags = allTags;
            notSelectedTags = new ArrayList<>();
        }
        retVal.put("selected", selectedTags);
        retVal.put("notSelected", notSelectedTags);
        return retVal;
    }

    public List<Integer> getAllMessageTagIds() {
        List<Integer> retVal = new ArrayList<>();
        for(MessageTag tag:em.createNamedQuery("MessageTag.findAll", MessageTag.class).getResultList())
        {
            retVal.add(tag.getMessageTagId());
        }
        
        return retVal;
    }

    public void deleteMessage(Integer messageId) {
        Message message = em.find(Message.class, messageId);
        if(message != null)
        {
                // First: Delete the images
                for(MessageIllustration ill : message.getMessageIllustrationSet())
                {
                    String fileName = ill.getMessageIllustrationPK().getFileName();
                    File file = new File(this.getFilePath(message) + "/" + fileName);
                    file.delete();
                }
                // Then: Delete the message
                em.remove(message);
        }
    }

    /**
     * Retreiving connected crop organisms as list of ids, for simplicity
     * @param message
     * @return 
     */
    public List<Integer> getMessageCropOrganismIds(Message message)
    {
        Query q = em.createNativeQuery("SELECT organism_id FROM public.organism_message WHERE message_id = :messageId");
        q.setParameter("messageId", message.getMessageId());
        try
        {
            return q.getResultList();
        }
        catch(NoResultException ex)
        {
            return new ArrayList<>();
        }
    }

    
}
