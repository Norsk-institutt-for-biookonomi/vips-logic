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
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import no.nibio.vips.logic.util.IntegerArrayUserType;


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
    
    @Convert(converter = IntegerArrayUserType.class)
    @Column(name = "message_tag_ids")
    private Integer[] messageTagIds;
    
    @Convert(converter = IntegerArrayUserType.class)
    @Column(name = "crop_category_ids")
    private Integer[] cropCategoryIds;

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
    public Integer[] getMessageTagIds() {
        return messageTagIds;
    }

    /**
     * @param messageTagIds the messageTagIds to set
     */
    public void setMessageTagIds(Integer[] messageTagIds) {
        this.messageTagIds = messageTagIds;
    }
    
    /**
     * For your convenience
     * @param messageTagIds 
     */
    public void setMessageTagIds(List<Integer> messageTagIds)
    {
        this.messageTagIds = messageTagIds.toArray(new Integer[messageTagIds.size()]);
    }

    /**
     * @return the cropCategoryIds
     */
    public Integer[] getCropCategoryIds() {
        return cropCategoryIds;
    }

    /**
     * @param cropCategoryIds the cropCategoryIds to set
     */
    public void setCropCategoryIds(Integer[] cropCategoryIds) {
        this.cropCategoryIds = cropCategoryIds;
    }
    
    public void setCropCategoryIds(List<Integer> cropCategoryIds)
    {
        this.cropCategoryIds = cropCategoryIds.toArray(new Integer[cropCategoryIds.size()]);
    }

}
