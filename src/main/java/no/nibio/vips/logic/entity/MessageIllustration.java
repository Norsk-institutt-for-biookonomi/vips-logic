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
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @copyright 2014 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "message_illustration")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "MessageIllustration.findAll", query = "SELECT m FROM MessageIllustration m"),
    @NamedQuery(name = "MessageIllustration.findByMessageId", query = "SELECT m FROM MessageIllustration m WHERE m.messageIllustrationPK.messageId = :messageId"),
    @NamedQuery(name = "MessageIllustration.findByFileName", query = "SELECT m FROM MessageIllustration m WHERE m.messageIllustrationPK.fileName = :fileName")})
public class MessageIllustration implements Serializable {
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "messageIllustration", fetch = FetchType.EAGER)
    private Set<MessageIllustrationCaptionLocale> messageIllustrationCaptionLocaleSet;
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected MessageIllustrationPK messageIllustrationPK;
    @Column(name = "message_id", insertable = false, updatable = false)
    private Integer messageId;

    public MessageIllustration() {
    }

    public MessageIllustration(MessageIllustrationPK messageIllustrationPK) {
        this.messageIllustrationPK = messageIllustrationPK;
    }

    public MessageIllustration(int messageId, String fileName) {
        this.messageIllustrationPK = new MessageIllustrationPK(messageId, fileName);
    }

    public MessageIllustrationPK getMessageIllustrationPK() {
        return messageIllustrationPK;
    }

    public void setMessageIllustrationPK(MessageIllustrationPK messageIllustrationPK) {
        this.messageIllustrationPK = messageIllustrationPK;
    }

    

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (messageIllustrationPK != null ? messageIllustrationPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof MessageIllustration)) {
            return false;
        }
        MessageIllustration other = (MessageIllustration) object;
        if ((this.messageIllustrationPK == null && other.messageIllustrationPK != null) || (this.messageIllustrationPK != null && !this.messageIllustrationPK.equals(other.messageIllustrationPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.entity.MessageIllustration[ messageIllustrationPK=" + messageIllustrationPK + " ]";
    }

    public Set<MessageIllustrationCaptionLocale> getMessageIllustrationCaptionLocaleSet() {
        return messageIllustrationCaptionLocaleSet;
    }

    public void setMessageIllustrationCaptionLocaleSet(Set<MessageIllustrationCaptionLocale> messageIllustrationCaptionLocaleSet) {
        this.messageIllustrationCaptionLocaleSet = messageIllustrationCaptionLocaleSet;
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
    @Transient
    public MessageIllustrationCaptionLocale getLocalMessageIllustrationCaptionWithFallback(String userLanguage, String organizationDefaultLanguage)
    {
        if(this.getMessageIllustrationCaptionLocaleSet() != null && !this.getMessageIllustrationCaptionLocaleSet().isEmpty())
        {
            if(this.getMessageIllustrationCaptionLocaleSet().size() == 1)
            {
                return this.getMessageIllustrationCaptionLocaleSet().iterator().next();
            }
            
            else
            {
                // If more than one localized message, use this priority:
                // 1. Message that matches user's language
                for(MessageIllustrationCaptionLocale ml:this.getMessageIllustrationCaptionLocaleSet())
                {
                    if(ml.getMessageIllustrationCaptionLocalePK().getLocale().equals(userLanguage))
                    {
                        return ml;
                    }
                }
                // 2. Message that matches organization's default language
                for(MessageIllustrationCaptionLocale ml:this.getMessageIllustrationCaptionLocaleSet())
                {
                    if(ml.getMessageIllustrationCaptionLocalePK().getLocale().equals(organizationDefaultLanguage))
                    {
                        return ml;
                    }
                }
                // 3. The first message in the set
                return this.getMessageIllustrationCaptionLocaleSet().iterator().next();
                
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
    @Transient
    public MessageIllustrationCaptionLocale getLocalMessageIllustrationCaption(String language)
    {
        if(this.getMessageIllustrationCaptionLocaleSet() != null && !this.getMessageIllustrationCaptionLocaleSet().isEmpty())
        {
            for(MessageIllustrationCaptionLocale ml:this.getMessageIllustrationCaptionLocaleSet())
            {
                if(ml.getMessageIllustrationCaptionLocalePK().getLocale().equals(language))
                {
                    return ml;
                }
            }
        }
        
        return null;

    }


}
