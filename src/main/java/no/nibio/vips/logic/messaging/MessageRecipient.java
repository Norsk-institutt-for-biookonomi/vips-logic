/*
 * Copyright (c) 2015 NIBIO <http://www.nibio.no/>. 
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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
public class MessageRecipient implements Serializable {
    @Column(name = "preferred_locale")
    private String preferredLocale;
    @Column(name = "free_sms")
    private Boolean freeSms;
    private String type;
    @Column(name = "msg_delivery_address")
    private String msgDeliveryAddress;
    private String name;
    @Id
    @Column(name = "recipient_id")
    private String recipientId;
    
    public MessageRecipient(){}
    
    public MessageRecipient(
            String recipientId,
            String name,
            String type,
            String msgDeliveryAddress,
            String preferredLocale,
            boolean freeSms
    )
    {
        this.recipientId = recipientId;
        this.name = name;
        this.type=type;
        this.msgDeliveryAddress = msgDeliveryAddress;
        this.preferredLocale = preferredLocale;
        this.freeSms = freeSms;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the msgDeliveryAddress
     */
    public String getMsgDeliveryAddress() {
        return msgDeliveryAddress;
    }

    /**
     * @param msgDeliveryAddress the msgDeliveryAddress to set
     */
    public void setMsgDeliveryAddress(String msgDeliveryAddress) {
        this.msgDeliveryAddress = msgDeliveryAddress;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the recipientId
     */
    public String getRecipientId() {
        return recipientId;
    }

    /**
     * @param recipientId the recipientId to set
     */
    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    /**
     * @return the preferredLocale
     */
    public String getPreferredLocale() {
        return preferredLocale;
    }

    /**
     * @param preferredLocale the preferredLocale to set
     */
    public void setPreferredLocale(String preferredLocale) {
        this.preferredLocale = preferredLocale;
    }

    /**
     * @return the freeSms
     */
    public Boolean getFreeSms() {
        return freeSms;
    }

    /**
     * @param freeSms the freeSms to set
     */
    public void setFreeSms(Boolean freeSms) {
        this.freeSms = freeSms;
    }
}
