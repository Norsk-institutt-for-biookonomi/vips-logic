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

package no.nibio.vips.logic.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;
import no.nibio.vips.logic.util.IntegerArrayUserType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

/**
 * @copyright 2014 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "message")
@XmlRootElement
@TypeDefs( {@TypeDef( name= "IntegerArray", typeClass = IntegerArrayUserType.class)})
@NamedQueries({
    @NamedQuery(name = "Message.findAll", query = "SELECT m FROM Message m"),
    @NamedQuery(name = "Message.findByMessageId", query = "SELECT m FROM Message m WHERE m.messageId = :messageId"),
    @NamedQuery(name = "Message.findByMessageIds", query = "SELECT m FROM Message m WHERE m.messageId IN(:messageIds)"),
    @NamedQuery(name = "Message.findByOrganizationId", query = "SELECT m FROM Message m WHERE m.organizationId = :organizationId"),
    @NamedQuery(name = "Message.findByOrganizationIdAndPublicationPeriod", query = "SELECT m FROM Message m WHERE m.organizationId = :organizationId AND m.datePub <= :publishedTo AND m.dateValidTo >= :publishedFrom"),
    @NamedQuery(name = "Message.findByOrganizationIdAndMaxPublicationPeriod", query = "SELECT m FROM Message m WHERE m.organizationId = :organizationId AND m.datePub <= :publishedTo "),
    @NamedQuery(name = "Message.findByOrganizationIdAndMinPublicationPeriod", query = "SELECT m FROM Message m WHERE m.organizationId = :organizationId AND  m.dateValidTo >= :publishedFrom"),
    @NamedQuery(name = "Message.findByDatePub", query = "SELECT m FROM Message m WHERE m.datePub = :datePub"),
    @NamedQuery(name = "Message.findByDateValidTo", query = "SELECT m FROM Message m WHERE m.dateValidTo = :dateValidTo")})
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer messageId;
    private Date datePub;
    private Date dateValidTo;
    private Set<MessageTag> messageTagSet;
    private Set<MessageIllustration> messageIllustrationSet;
    private Set<MessageLocale> messageLocaleSet;
    private Integer organizationId;
    private Integer[] cropCategoryIds;

    public Message() {
    }

    public Message(Integer messageId) {
        this.messageId = messageId;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "message_id")
    public Integer getMessageId() {
        return messageId;
    }

    public void setMessageId(Integer messageId) {
        this.messageId = messageId;
    }

    @Column(name = "date_pub")
    @Temporal(TemporalType.DATE)
    public Date getDatePub() {
        return datePub;
    }

    public void setDatePub(Date datePub) {
        this.datePub = datePub;
    }

    @Column(name = "date_valid_to")
    @Temporal(TemporalType.DATE)
    public Date getDateValidTo() {
        return dateValidTo;
    }

    public void setDateValidTo(Date dateValidTo) {
        this.dateValidTo = dateValidTo;
    }
    
    @Column(name = "organization_id")
    public Integer getOrganizationId(){
        return organizationId;
    }
    
    public void setOrganizationId(Integer organizationId)
    {
        this.organizationId = organizationId;
    }

    @JoinTable(name = "message_message_tag", joinColumns = {
        @JoinColumn(name = "message_id", referencedColumnName = "message_id")}, inverseJoinColumns = {
            @JoinColumn(name = "message_tag_id", referencedColumnName = "message_tag_id")})
    @ManyToMany(fetch = FetchType.EAGER)
    public Set<MessageTag> getMessageTagSet() {
        return messageTagSet;
    }
    
    @Transient
    public List<Integer> getMessageTagIds(){
        List<Integer> retVal = new ArrayList<>();
        for(MessageTag tag: this.getMessageTagSet())
        {
            retVal.add(tag.getMessageTagId());
        }
        return retVal;
    }

    public void setMessageTagSet(Set<MessageTag> messageTagSet) {
        this.messageTagSet = messageTagSet;
    }

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "messageId", fetch = FetchType.EAGER)
    public Set<MessageIllustration> getMessageIllustrationSet() {
        return messageIllustrationSet;
    }

    public void setMessageIllustrationSet(Set<MessageIllustration> messageIllustrationSet) {
        this.messageIllustrationSet = messageIllustrationSet;
    }

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "messageId", fetch = FetchType.EAGER)
    public Set<MessageLocale> getMessageLocaleSet() {
        return messageLocaleSet;
    }

    public void setMessageLocaleSet(Set<MessageLocale> messageLocaleSet) {
        this.messageLocaleSet = messageLocaleSet;
    }
    
    public void addMessageLocale(MessageLocale messageLocale)
    {
        if(this.messageLocaleSet == null)
        {
            this.messageLocaleSet = new HashSet<>();
        }
        this.messageLocaleSet.add(messageLocale);
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (messageId != null ? messageId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Message)) {
            return false;
        }
        Message other = (Message) object;
        if ((this.messageId == null && other.messageId != null) || (this.messageId != null && !this.messageId.equals(other.messageId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.entity.Message[ messageId=" + messageId + " ]";
    }
    
    /**
     * If more than one localized message, use this priority
     * <ol>
     * <li>Message that matches user's language</li>
     * <li>Message that matches organizations default language</li>
     * <li>The first message in the set</li>
     * </ol>
     * @param userLanguage
     * @param organizationDefaultLanguage
     * @return 
     */
    public MessageLocale getLocalMessageWithFallback(String userLanguage, String organizationDefaultLanguage)
    {
        if(this.getMessageLocaleSet() != null && !this.getMessageLocaleSet().isEmpty())
        {
            if(this.getMessageLocaleSet().size() == 1)
            {
                return this.getMessageLocaleSet().iterator().next();
            }
            
            else
            {
                // If more than one localized message, use this priority:
                // 1. Message that matches user's language
                for(MessageLocale ml:this.getMessageLocaleSet())
                {
                    if(ml.getMessageLocalePK().getLocale().equals(userLanguage))
                    {
                        return ml;
                    }
                }
                // 2. Message that matches organization's default language
                for(MessageLocale ml:this.getMessageLocaleSet())
                {
                    if(ml.getMessageLocalePK().getLocale().equals(organizationDefaultLanguage))
                    {
                        return ml;
                    }
                }
                // 3. The first message in the set
                return this.getMessageLocaleSet().iterator().next();
                
            }
        }
        else
        {
            return null;
        }
       
    }

    /**
     * 
     * @param language
     * @return local part of message, or NULL if local version in given language does not exist
     */
    public MessageLocale getLocalMessage(String language)
    {
        if(this.getMessageLocaleSet() != null && !this.getMessageLocaleSet().isEmpty())
        {
            for(MessageLocale ml:this.getMessageLocaleSet())
            {
                if(ml.getMessageLocalePK().getLocale().equals(language))
                {
                    return ml;
                }
            }
        }
        
        return null;
        
    }

    /**
     * @return the cropCategoryIds
     */
    @Type(type = "IntegerArray")
    @Column(name="crop_category_ids")
    public Integer[] getCropCategoryIds() {
        return cropCategoryIds;
    }

    /**
     * @param cropCategoryIds the cropCategoryIds to set
     */
    public void setCropCategoryIds(Integer[] cropCategoryIds) {
        this.cropCategoryIds = cropCategoryIds;
    }


}
