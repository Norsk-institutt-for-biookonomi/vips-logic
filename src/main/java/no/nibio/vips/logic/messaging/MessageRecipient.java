/*
 * Copyright (c) 2015 NIBIO <http://www.nibio.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the NIBIO Open Source License as published by 
 * NIBIO, either version 1 of the License, or (at your option) any
 * later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * NIBIO Open Source License for more details.
 * 
 * You should have received a copy of the NIBIO Open Source License
 * along with VIPSLogic.  If not, see <http://www.nibio.no/licenses/>.
 * 
 */

package no.nibio.vips.logic.messaging;

/**
 * @copyright 2015 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class MessageRecipient {
    private String type;
    private String msgDeliveryAddress;
    private String name;
    private String recipientId;
    
    public MessageRecipient(
            String recipientId,
            String name,
            String type,
            String msgDeliveryAddress
    )
    {
        this.recipientId = recipientId;
        this.name = name;
        this.type=type;
        this.msgDeliveryAddress = msgDeliveryAddress;
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
}
