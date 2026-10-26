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
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @copyright 2014 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "message_tag")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "MessageTag.findAll", query = "SELECT m FROM MessageTag m"),
    @NamedQuery(name = "MessageTag.findByMessageTagId", query = "SELECT m FROM MessageTag m WHERE m.messageTagId = :messageTagId"),
    @NamedQuery(name = "MessageTag.findByDefaultTagName", query = "SELECT m FROM MessageTag m WHERE m.defaultTagName = :defaultTagName")})
public class MessageTag implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "message_tag_id")
    private Integer messageTagId;
    @Size(max = 255)
    @Column(name = "default_tag_name")
    private String defaultTagName;
    @ManyToMany(mappedBy = "messageTagSet")
    private Set<Message> messageSet;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "messageTagId", fetch = FetchType.EAGER)
    private Set<MessageTagLocale> messageTagLocaleSet;

    public MessageTag() {
    }

    public MessageTag(Integer messageTagId) {
        this.messageTagId = messageTagId;
    }

    public Integer getMessageTagId() {
        return messageTagId;
    }

    public void setMessageTagId(Integer messageTagId) {
        this.messageTagId = messageTagId;
    }

    public String getDefaultTagName() {
        return defaultTagName;
    }

    public void setDefaultTagName(String defaultTagName) {
        this.defaultTagName = defaultTagName;
    }

    @XmlTransient
    @JsonIgnore
    public Set<Message> getMessageSet() {
        return messageSet;
    }

    public void setMessageSet(Set<Message> messageSet) {
        this.messageSet = messageSet;
    }

    public Set<MessageTagLocale> getMessageTagLocaleSet() {
        return messageTagLocaleSet;
    }

    public void setMessageTagLocaleSet(Set<MessageTagLocale> messageTagLocaleSet) {
        this.messageTagLocaleSet = messageTagLocaleSet;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (messageTagId != null ? messageTagId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof MessageTag)) {
            return false;
        }
        MessageTag other = (MessageTag) object;
        if ((this.messageTagId == null && other.messageTagId != null) || (this.messageTagId != null && !this.messageTagId.equals(other.messageTagId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.entity.MessageTag[ messageTagId=" + messageTagId + " ]";
    }
    
    public String getLocalName(String language)
    {
        for(MessageTagLocale localTag:this.getMessageTagLocaleSet())
        {
            if(localTag.getMessageTagLocalePK().getLocale().equals(language))
            {
                return localTag.getLocalName();
            }
        }
        return null;
    }

}
