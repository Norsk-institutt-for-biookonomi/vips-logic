/*
 * Copyright (c) 2022 NIBIO <http://www.nibio.no/>. 
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
package no.nibio.vips.logic.messaging.distribution.send;

import no.nibio.vips.logic.messaging.distribution.entity.DistributionTypeEnum;
import no.nibio.vips.logic.messaging.distribution.entity.MsgReceiver;
import no.nibio.vips.logic.messaging.distribution.entity.MsgToSend;
import no.nibio.vips.logic.messaging.distribution.entity.VipsMessage;



/**
 * This is the interface implemented by the different distribution channels like sm, mail.....
 * 
 * @author Lars Aksel Opsahl <lars.opsahl@nibio.no>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 *
 */
public interface IDeliveryMsgHandler {

	
	
	/**
	 * Returns what kind distribution this handler accepts
	 *  
	 * @return
	 */
	public DistributionTypeEnum getDistributionType();


	/**
	 * Send given message to given user and update the logfile
	 * 
	 * @param vm the message content
	 * @param msgReceiver the reciver of the message
	 * @return
	 */
	public SingleMsgSentStateEnum sendMessage(VipsMessage vm, MsgReceiver msgReceiver, MsgToSend msgToSend);
}
