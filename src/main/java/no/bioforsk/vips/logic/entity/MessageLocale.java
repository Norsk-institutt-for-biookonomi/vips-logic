/*
 * Copyright (c) 2014 Bioforsk <http://www.bioforsk.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the Bioforsk Open Source License as published by 
 * Bioforsk, either version 1 of the License, or (at your option) any
 * later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Bioforsk Open Source License for more details.
 * 
 * You should have received a copy of the Bioforsk Open Source License
 * along with VIPSLogic.  If not, see <http://www.bioforsk.no/licenses/>.
 * 
 */

package no.bioforsk.vips.logic.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @copyright 2014 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
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
        return "no.bioforsk.vips.logic.entity.MessageLocale[ messageLocalePK=" + messageLocalePK + " ]";
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
