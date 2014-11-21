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
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @copyright 2014 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
@Entity
@Table(name = "message_illustration_caption_locale")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "MessageIllustrationCaptionLocale.findAll", query = "SELECT m FROM MessageIllustrationCaptionLocale m"),
    @NamedQuery(name = "MessageIllustrationCaptionLocale.findByMessageId", query = "SELECT m FROM MessageIllustrationCaptionLocale m WHERE m.messageIllustrationCaptionLocalePK.messageId = :messageId"),
    @NamedQuery(name = "MessageIllustrationCaptionLocale.findByFileName", query = "SELECT m FROM MessageIllustrationCaptionLocale m WHERE m.messageIllustrationCaptionLocalePK.fileName = :fileName"),
    @NamedQuery(name = "MessageIllustrationCaptionLocale.findByLocale", query = "SELECT m FROM MessageIllustrationCaptionLocale m WHERE m.messageIllustrationCaptionLocalePK.locale = :locale"),
    @NamedQuery(name = "MessageIllustrationCaptionLocale.findByCaption", query = "SELECT m FROM MessageIllustrationCaptionLocale m WHERE m.caption = :caption")})
public class MessageIllustrationCaptionLocale implements Serializable {
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected MessageIllustrationCaptionLocalePK messageIllustrationCaptionLocalePK;
    @Size(max = 2147483647)
    @Column(name = "caption")
    private String caption;
    @JoinColumns({
        @JoinColumn(name = "message_id", referencedColumnName = "message_id", insertable = false, updatable = false),
        @JoinColumn(name = "file_name", referencedColumnName = "file_name", insertable = false, updatable = false)})
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private MessageIllustration messageIllustration;


    public MessageIllustrationCaptionLocale() {
    }

    public MessageIllustrationCaptionLocale(MessageIllustrationCaptionLocalePK messageIllustrationCaptionLocalePK) {
        this.messageIllustrationCaptionLocalePK = messageIllustrationCaptionLocalePK;
    }

    public MessageIllustrationCaptionLocale(int messageId, String fileName, String locale) {
        this.messageIllustrationCaptionLocalePK = new MessageIllustrationCaptionLocalePK(messageId, fileName, locale);
    }

    public MessageIllustrationCaptionLocalePK getMessageIllustrationCaptionLocalePK() {
        return messageIllustrationCaptionLocalePK;
    }

    public void setMessageIllustrationCaptionLocalePK(MessageIllustrationCaptionLocalePK messageIllustrationCaptionLocalePK) {
        this.messageIllustrationCaptionLocalePK = messageIllustrationCaptionLocalePK;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    @JsonIgnore
    public MessageIllustration getMessageIllustration() {
        return messageIllustration;
    }

    public void setMessageIllustration(MessageIllustration messageIllustration) {
        this.messageIllustration = messageIllustration;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (messageIllustrationCaptionLocalePK != null ? messageIllustrationCaptionLocalePK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof MessageIllustrationCaptionLocale)) {
            return false;
        }
        MessageIllustrationCaptionLocale other = (MessageIllustrationCaptionLocale) object;
        if ((this.messageIllustrationCaptionLocalePK == null && other.messageIllustrationCaptionLocalePK != null) || (this.messageIllustrationCaptionLocalePK != null && !this.messageIllustrationCaptionLocalePK.equals(other.messageIllustrationCaptionLocalePK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.bioforsk.vips.logic.entity.MessageIllustrationCaptionLocale[ messageIllustrationCaptionLocalePK=" + messageIllustrationCaptionLocalePK + " ]";
    }

}
