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

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import com.ibm.icu.util.ULocale;
import no.nibio.vips.logic.messaging.UniversalMessage;
import no.nibio.vips.logic.messaging.distribution.send.IMsgContent;
import no.nibio.vips.logic.util.Globals;


/**
 * This is class that contains all info when you send message from vips
 * 
 * @author Lars Aksel Opsahl <lars.opsahl@nibio.no>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 *
 */

// TODO use get and setter
public class VipsMessage implements IMsgContent {

    // Include by default. If not needed, use constructor that makes it configurable
    private Boolean includeNotificationSettingsLink = Boolean.TRUE;

    /**
     * Create from UniversalMessage
     * TODO: Complete integration of entities!
     * @param universalMessage 
     */
    public VipsMessage(UniversalMessage universalMessage)
    {
        this.expiresAt = universalMessage.getExpiresAt();
        this.setIncludeNotificationSettingsLink(universalMessage.getIncludeNotificationSettingsLink());
        this.distributionList = universalMessage.getDistributionListObjects().stream().map(umRecipient->{
            MsgReceiver msgReceiver = new MsgReceiver();
            msgReceiver.type = umRecipient.getType().toLowerCase().equals("mail") ? DistributionTypeEnum.Mail : DistributionTypeEnum.Sms;
            msgReceiver.freeSms = umRecipient.getFreeSms();
            msgReceiver.msgDeliveryAddress = umRecipient.getMsgDeliveryAddress();
            msgReceiver.name = umRecipient.getName();
            msgReceiver.preferredLocale = umRecipient.getPreferredLocale();
            msgReceiver.recipientId = umRecipient.getRecipientId();
            
            return msgReceiver;
        }).collect(Collectors.toList());
        this.messageLocalVersions = universalMessage.getMessageLocalVersionObjects().stream().map(umLocalV->{
            MsgToSend msgToSend = new MsgToSend();
            msgToSend.msgBody = umLocalV.getMsgBody();
            try
            {
                msgToSend.msgDownloadUrl = new URL(umLocalV.getMsgDownloadUrl());
            }
            catch(MalformedURLException ex)
            {
                
            }
            msgToSend.msgLeadParagraph = umLocalV.getMsgLeadParagraph();
            msgToSend.msgBody = umLocalV.getMsgBody();
            msgToSend.msgSubject = umLocalV.getMsgSubject();
            msgToSend.setLocale(umLocalV.getLocale());
            return msgToSend;
        }).collect(Collectors.toList());
                
    }
	
	
	
	// The list of message to be sent 
	private List<MsgToSend> messageLocalVersions = new ArrayList<MsgToSend>();
		
	// The list of message receiver 
	public List<MsgReceiver> distributionList = new ArrayList<MsgReceiver>();

	// when the message should expiere, after this time the message should not be sent.
	public Date expiresAt;

	/**
	 * @return the messageLocalVersions
	 */
	public List<MsgToSend> getMessageLocalVersions() {
		return messageLocalVersions;
	}

	/**
	 * @param messageLocalVersions the messageLocalVersions to set
	 */
	public void setMessageLocalVersions(List<MsgToSend> messageLocalVersions) {
		this.messageLocalVersions = messageLocalVersions;
	}


    /**
     * If true, the SMS and mail messages will have this type of link appended at the end:
     * "To edit your notification subscriptions, please use this link: https://logic.vips.nibio.no/user/notificationsubscription?userId=1"
     * The server name can be configured with the system properties no.nibio.vips.logic.VIPSLOGIC_PROTOCOL and no.nibio.vips.logic.SERVER_NAME
     * @return
     */
    public Boolean getIncludeNotificationSettingsLink() {
        return includeNotificationSettingsLink;
    }

    public void setIncludeNotificationSettingsLink(Boolean includeNotificationSettingsLink) {
        this.includeNotificationSettingsLink = includeNotificationSettingsLink;
    }

}
