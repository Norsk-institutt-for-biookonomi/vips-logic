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
