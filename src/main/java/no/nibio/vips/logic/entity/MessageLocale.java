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
import java.util.Date;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * @copyright 2014 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "message_locale")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "MessageLocale.findAll", query = "SELECT m FROM MessageLocale m"),
    @NamedQuery(name = "MessageLocale.findByMessageId", query = "SELECT m FROM MessageLocale m WHERE m.messageLocalePK.messageId = :messageId"),
    @NamedQuery(name = "MessageLocale.findByCreatedBy", query = "SELECT m FROM MessageLocale m WHERE m.createdBy = :createdBy"),
    @NamedQuery(name = "MessageLocale.findByLocale", query = "SELECT m FROM MessageLocale m WHERE m.messageLocalePK.locale = :locale"),
    @NamedQuery(name = "MessageLocale.findByHeading", query = "SELECT m FROM MessageLocale m WHERE m.heading = :heading"),
    @NamedQuery(name = "MessageLocale.findByLeadParagraph", query = "SELECT m FROM MessageLocale m WHERE m.leadParagraph = :leadParagraph"),
    @NamedQuery(name = "MessageLocale.findByBody", query = "SELECT m FROM MessageLocale m WHERE m.body = :body"),
    @NamedQuery(name = "MessageLocale.findByCreatedTimestamp", query = "SELECT m FROM MessageLocale m WHERE m.createdTimestamp = :createdTimestamp"),
    @NamedQuery(name = "MessageLocale.findByModifiedTimestamp", query = "SELECT m FROM MessageLocale m WHERE m.modifiedTimestamp = :modifiedTimestamp")})
public class MessageLocale implements Serializable {
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected MessageLocalePK messageLocalePK;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 511)
    @Column(name = "heading")
    private String heading;
    @Size(max = 2147483647)
    @Column(name = "lead_paragraph")
    private String leadParagraph;
    @Size(max = 2147483647)
    @Column(name = "body")
    private String body;
    @Column(name = "created_timestamp")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdTimestamp;
    @JoinColumn(name = "created_by", referencedColumnName = "user_id")
    @ManyToOne(optional = false)
    private VipsLogicUser createdBy;
    @Column(name = "modified_timestamp")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifiedTimestamp;
    @Column(name="message_id", insertable = false, updatable = false)
    private Integer messageId;

    public MessageLocale() {
    }

    public MessageLocale(MessageLocalePK messageLocalePK) {
        this.messageLocalePK = messageLocalePK;
    }

    public MessageLocale(MessageLocalePK messageLocalePK, String heading) {
        this.messageLocalePK = messageLocalePK;
        this.heading = heading;
    }

    public MessageLocale(int messageId, String locale) {
        this.messageLocalePK = new MessageLocalePK(messageId, locale);
    }
    
    @Transient
    public String getMessageLocaleSyntheticPK()
    {
        return this.messageLocalePK.getMessageId() + "_" + this.messageLocalePK.getLocale();
    }

    public MessageLocalePK getMessageLocalePK() {
        return messageLocalePK;
    }

    public void setMessageLocalePK(MessageLocalePK messageLocalePK) {
        this.messageLocalePK = messageLocalePK;
    }

    public String getHeading() {
        return heading;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public String getLeadParagraph() {
        return leadParagraph;
    }

    public void setLeadParagraph(String leadParagraph) {
        this.leadParagraph = leadParagraph;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Date getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Date createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public Date getModifiedTimestamp() {
        return modifiedTimestamp;
    }

    public void setModifiedTimestamp(Date modifiedTimestamp) {
        this.modifiedTimestamp = modifiedTimestamp;
    }

    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (messageLocalePK != null ? messageLocalePK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof MessageLocale)) {
            return false;
        }
        MessageLocale other = (MessageLocale) object;
        if ((this.messageLocalePK == null && other.messageLocalePK != null) || (this.messageLocalePK != null && !this.messageLocalePK.equals(other.messageLocalePK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.entity.MessageLocale[ messageLocalePK=" + messageLocalePK + " ]";
    }

    /**
     * @return the createdBy
     */
    public VipsLogicUser getCreatedBy() {
        return createdBy;
    }

    /**
     * @param createdBy the createdBy to set
     */
    public void setCreatedBy(VipsLogicUser createdBy) {
        this.createdBy = createdBy;
    }

}
