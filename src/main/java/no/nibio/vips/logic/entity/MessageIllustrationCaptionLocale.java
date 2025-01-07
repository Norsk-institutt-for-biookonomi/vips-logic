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
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @copyright 2014 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
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
        return "no.nibio.vips.logic.entity.MessageIllustrationCaptionLocale[ messageIllustrationCaptionLocalePK=" + messageIllustrationCaptionLocalePK + " ]";
    }

}
