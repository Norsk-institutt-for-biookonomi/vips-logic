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

import com.ibm.icu.util.ULocale;
import no.nibio.vips.logic.util.Globals;

import java.net.URL;
import java.text.MessageFormat;
import java.util.ResourceBundle;


/**
 * This is class that contains all info when you send message from vips
 * 
 * @author Lars Aksel Opsahl <lars.opsahl@nibio.no>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 *
 */

// TODO use get and setter
public class MsgToSend {
	// The message subject, used when sending a email
	public String msgSubject; 
	
	// this message laguag
	private String locale;

	// The message to be sent
	public String msgBody;
	
	// The Download URL for the message
	public URL msgDownloadUrl;
	
        // The lead paragraph (AKA "Ingress")
	public String msgLeadParagraph;


	/* (non-Javadoc)
	 * @see no.nibio.vips_msg_dist.dao.Test#getMsgSubject()
	 */
	public String getMsgSubject() {
		return msgSubject;
	}

	/**
	 * @param msgSubject the msgSubject to set
	 */
	public void setMsgSubject(String msgSubject) {
		this.msgSubject = msgSubject;
	}

	/* (non-Javadoc)
	 * @see no.nibio.vips_msg_dist.dao.Test#getMsgBody()
	 */
	public String getMsgBody() {
		return msgBody;
	}

	/**
	 * @param msgBody the msgBody to set
	 */
	public void setMsgBody(String msgBody) {
		this.msgBody = msgBody;
	}

	/* (non-Javadoc)
	 * @see no.nibio.vips_msg_dist.dao.Test#getMsgDownloadUrl()
	 */
	public URL getMsgDownloadUrl() {
		return msgDownloadUrl;
	}

	/**
	 * @param msgDownloadUrl the msgDownloadUrl to set
	 */
	public void setMsgDownloadUrl(URL msgDownloadUrl) {
		this.msgDownloadUrl = msgDownloadUrl;
	}

	/* (non-Javadoc)
	 * @see no.nibio.vips_msg_dist.dao.Test#getMsgLeadParagraph()
	 */
	public String getMsgLeadParagraph() {
		return msgLeadParagraph;
	}

	/**
	 * @param msgLeadParagraph the msgLeadParagraph to set
	 */
	public void setMsgLeadParagraph(String msgLeadParagraph) {
		this.msgLeadParagraph = msgLeadParagraph;
	}

	/**
	 * @return the locale
	 */
	public String getLocale() {
		return locale;
	}

	/**
	 * @param locale the locale to set
	 */
	public void setLocale(String locale) {
		this.locale = locale;
	}


	/**
	 * @return The localized string **template** for generating a link to a user's page for
	 * managing notification subscription settings
	 */
	public String getNotificationSettingsLinkTpl()
	{
		ULocale theLocale = new ULocale(this.getLocale());
		ResourceBundle rb = ResourceBundle.getBundle("no.nibio.vips.logic.i18n.vipslogictexts",theLocale.toLocale());
		return MessageFormat.format(rb.getString("universalMessageSettingsLink_tpl"), Globals.PROTOCOL + "://" + Globals.SERVER_NAME + "/user/notificationsubscription?userId={0}");
	}


}
