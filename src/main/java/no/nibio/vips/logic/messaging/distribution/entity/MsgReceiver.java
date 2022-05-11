/*
 * Copyright (c) 2022 NIBIO <http://www.nibio.no/>. 
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
package no.nibio.vips.logic.messaging.distribution.entity;

/**
 * This contains the info needed to send a message 
 * 
 * @author Lars Aksel Opsahl <lars.opsahl@nibio.no>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 *
 */
public class MsgReceiver {

	public MsgReceiver() {
	}

	
	public MsgReceiver(DistributionTypeEnum messageDistributionType, String string) {
		type = messageDistributionType;
		msgDeliveryAddress = string;
	}

	// if this should be sent to sms or mail 
	public DistributionTypeEnum type;
	
	// the address for delivery either a mail address or phone number
	public String msgDeliveryAddress;
	
	// the name of the recipient of the message
	public String name;
	
	// a internal id of the recipient 
	public String recipientId; 

	// The user's preffered language for messages
	public String preferredLocale; 
        
        // If SMS messages should be free of charge
        public Boolean freeSms;

	

}
