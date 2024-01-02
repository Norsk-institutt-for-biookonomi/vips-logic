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
package no.nibio.vips.logic.messaging.distribution;

import java.util.UUID;
import no.nibio.vips.logic.messaging.distribution.entity.MsgDistResultEnum;
import no.nibio.vips.logic.messaging.distribution.entity.VipsMessage;
import no.nibio.vips.logic.messaging.distribution.entity.VipsSendMsgResult;

/**
 * Handles client communication requests used by the topo client
 * 
 * @author Lars Aksel Opsahl <lars.opsahl@nibio.no>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 * 
 */
public class VipsMsgDummyDistImpl implements IVipsMessageHandler {


	public VipsSendMsgResult sendMessage(VipsMessage vm) throws Exception {


		VipsSendMsgResult vipsSendMsgResult = new VipsSendMsgResult();
		vipsSendMsgResult.status = MsgDistResultEnum.FailedNotSent;

		try {

			UUID uuid = UUID.randomUUID();
			String jobId = uuid.toString();
			vipsSendMsgResult.jobId = jobId;
			vipsSendMsgResult.numRecipients = vm.distributionList.size();

			vipsSendMsgResult.status = MsgDistResultEnum.FailedNotSent;

		} catch (Exception e) {
			vipsSendMsgResult.status = MsgDistResultEnum.FailedNotSentWithException;

			System.out.println("Failed to send message: " + e.getMessage());

		} finally {
		}

		return vipsSendMsgResult;
	}

}
