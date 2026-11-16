/*
 * Copyright (c) 2016 NIBIO <http://www.nibio.no/>. 
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

package no.nibio.vips.logic.messaging;

import java.io.Serializable;
import java.util.List;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Type;
import no.nibio.vips.logic.util.PostgresListArrayType;

/**
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "message_notification_subscription", schema = "messaging")
public class MessageNotificationSubscription implements Serializable {

    @Id
    @Basic(optional = false)
    @Column(name = "user_id")
    private Integer userId;
    
    @Column(name = "universal_message_format_id")
    private Integer universalMessageFormatId;
    
    @Type(PostgresListArrayType.class)
    @Column(name = "message_tag_ids", columnDefinition = "integer[]")
    private List<Integer> messageTagIds;
    
    @Type(PostgresListArrayType.class)
    @Column(name = "crop_category_ids", columnDefinition = "integer[]")
    private List<Integer> cropCategoryIds;

    public Integer getUserId() {
        return this.userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (userId != null ? userId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof MessageNotificationSubscription)) {
            return false;
        }
        MessageNotificationSubscription other = (MessageNotificationSubscription) object;
        if ((this.userId == null && other.userId != null) || (this.userId != null && !this.userId.equals(other.userId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.messaging.MessageNotificationSubscription[ userId=" + userId + " ]";
    }

    /**
     * @return the universalMessageFormatId
     */
    public Integer getUniversalMessageFormatId() {
        return universalMessageFormatId;
    }

    /**
     * @param universalMessageFormatId the universalMessageFormatId to set
     */
    public void setUniversalMessageFormatId(Integer universalMessageFormatId) {
        this.universalMessageFormatId = universalMessageFormatId;
    }

    /**
     * @return the messageTagIds
     */
    public List<Integer> getMessageTagIds() {
        return messageTagIds;
    }

    /**
     * @param messageTagIds the messageTagIds to set
     */
    public void setMessageTagIds(List<Integer> messageTagIds) {
        this.messageTagIds = messageTagIds;
    }
    
    /**
     * @return the cropCategoryIds
     */
    public List<Integer> getCropCategoryIds() {
        return cropCategoryIds;
    }

    /**
     * @param cropCategoryIds the cropCategoryIds to set
     */
    public void setCropCategoryIds(List<Integer> cropCategoryIds) {
        this.cropCategoryIds = cropCategoryIds;
    }

}
