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

import java.util.List;
import no.nibio.vips.logic.messaging.distribution.entity.MsgToSend;



/**
 * This the content of a message that will be common for handlers, but not all handler may use all fileds.
 * 
 * @author Lars Aksel Opsahl <lars.opsahl@nibio.no>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 *
 */

public interface IMsgContent {
	
	List<MsgToSend> getMessageLocalVersions();
//	
//
//	/**
//	 * @return the msgSubject
//	 */
//	String getMsgSubject();
//
//	/**
//	 * @return the msgBody
//	 */
//	String getMsgBody();
//
//	/**
//	 * @return the msgDownloadUrl
//	 */
//	URL getMsgDownloadUrl();
//
//	/**
//	 * @return the msgLeadParagraph
//	 */
//	String getMsgLeadParagraph();
//
//	
//	/**
//	 * @return the locale
//	 */
//	String getLocale();

}
