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

package no.bioforsk.vips.logic.entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @copyright 2014 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
@Entity
@Table(name = "message_tag_locale")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "MessageTagLocale.findAll", query = "SELECT m FROM MessageTagLocale m"),
    @NamedQuery(name = "MessageTagLocale.findByMessageTagId", query = "SELECT m FROM MessageTagLocale m WHERE m.messageTagLocalePK.messageTagId = :messageTagId"),
    @NamedQuery(name = "MessageTagLocale.findByLocale", query = "SELECT m FROM MessageTagLocale m WHERE m.messageTagLocalePK.locale = :locale"),
    @NamedQuery(name = "MessageTagLocale.findByLocalName", query = "SELECT m FROM MessageTagLocale m WHERE m.localName = :localName")})
public class MessageTagLocale implements Serializable {
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected MessageTagLocalePK messageTagLocalePK;
    @Size(max = 255)
    @Column(name = "local_name")
    private String localName;
    @Column(name="message_tag_id", insertable = false, updatable = false)
    private Integer messageTagId;

    public MessageTagLocale() {
    }

    public MessageTagLocale(MessageTagLocalePK messageTagLocalePK) {
        this.messageTagLocalePK = messageTagLocalePK;
    }

    public MessageTagLocale(int messageTagId, String locale) {
        this.messageTagLocalePK = new MessageTagLocalePK(messageTagId, locale);
    }

    public MessageTagLocalePK getMessageTagLocalePK() {
        return messageTagLocalePK;
    }

    public void setMessageTagLocalePK(MessageTagLocalePK messageTagLocalePK) {
        this.messageTagLocalePK = messageTagLocalePK;
    }

    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (messageTagLocalePK != null ? messageTagLocalePK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof MessageTagLocale)) {
            return false;
        }
        MessageTagLocale other = (MessageTagLocale) object;
        if ((this.messageTagLocalePK == null && other.messageTagLocalePK != null) || (this.messageTagLocalePK != null && !this.messageTagLocalePK.equals(other.messageTagLocalePK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.bioforsk.vips.logic.entity.MessageTagLocale[ messageTagLocalePK=" + messageTagLocalePK + " ]";
    }

}
