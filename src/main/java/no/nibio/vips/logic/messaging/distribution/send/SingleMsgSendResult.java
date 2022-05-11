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
package no.nibio.vips.logic.messaging.distribution.send;

import java.util.Date;
import no.nibio.vips.logic.messaging.distribution.entity.MsgReceiver;


/**
 * This contains the info needed to send a message 
 * 
 * @author Lars Aksel Opsahl <lars.opsahl@nibio.no>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 *
 */
public class SingleMsgSendResult {

	
	// infor baout the message
	public MsgReceiver msgReceivier;
	
	// the message send result
	public SingleMsgSentStateEnum  res;

	// when the message was delivered
	public Date delievery;

	public SingleMsgSendResult(MsgReceiver msgReceivier, SingleMsgSentStateEnum  res ) {
		this.msgReceivier = msgReceivier;
		this.res = res;
		delievery = new Date();
	}


}
