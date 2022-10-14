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

import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import com.sun.mail.smtp.SMTPTransport;
import no.nibio.vips.logic.messaging.distribution.entity.DistributionTypeEnum;
import no.nibio.vips.logic.messaging.distribution.entity.MsgReceiver;
import no.nibio.vips.logic.messaging.distribution.entity.MsgToSend;
import no.nibio.vips.logic.messaging.distribution.entity.VipsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Handles sending off mail messages by SMPTP
 *
 * @author Lars Aksel Opsahl <lars.opsahl@nibio.no>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 *
 */
public class MailMsgDeliveryHandler implements IDeliveryMsgHandler {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(MailMsgDeliveryHandler.class);

    private final String mailserver;
    private final String sender;

    // if this no mail will be sent, but the return result is true
    private final boolean testOnly;

    public MailMsgDeliveryHandler(String mailserver, String sender, boolean testOnly) {
        this.mailserver = mailserver;
        this.sender = sender;
        this.testOnly = testOnly;
    }

    private boolean sendMessage(String subjekt, String leadParagraph, StringBuffer mailText, String mailaddress)
            throws MessagingException, UnsupportedEncodingException {
        boolean result = false;

        SMTPTransport t = null;
        String message = "Failed to send message to " + mailaddress + " using server " + mailserver;
        try {

            Properties props = System.getProperties();
            if (props == null) {
                System.out.println( this.getClass().getName() + " ERRROR: no mail sent System.getProperties() is null");
                return result;
            }
            props.put("mail.smtp.host", mailserver);
            //props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "false");

            props.put("mail.mime.charset", "UTF-8");
            props.put("user.language", "no_NO");
            props.put("sun.jnu.encoding", "UTF-8");
            props.put("file.encoding", "UTF-8");

            // Get a Session object
            Session session = Session.getInstance(props, null);
            session.setDebug(false);

            // construct the message
            MimeMessage msg = new MimeMessage(session);
            LOGGER.debug("Done create SMPTMessage");

            msg.addHeader("charset", "UTF-8");

            msg.setFrom(new InternetAddress(sender));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mailaddress, false));
            String encodeSubjektText = MimeUtility.encodeText(subjekt);
            msg.setSubject(encodeSubjektText);

            msg.setHeader("X-Mailer", "smtpsend");
            String mailMessageBody = leadParagraph;

            if (leadParagraph != null && leadParagraph.length() > 0) {
                mailMessageBody = leadParagraph + " \r\n" + mailText.toString();
            } else {
                mailMessageBody = mailText.toString();

            }

            msg.setText(mailMessageBody, "UTF-8");

            msg.setSentDate(new Date());
            boolean ssl = false;

            LOGGER.debug("Message ready to send : " + mailMessageBody);
            
            t = (SMTPTransport) session.getTransport(ssl ? "smtps" : "smtp");
            t.connect();
            t.sendMessage(msg, msg.getAllRecipients());
            
            LOGGER.debug("Mail was sent successfully. Response: " + t.getLastServerResponse());

        } finally {
            try {
                if (t != null) {
                    t.close();
                }
            } catch (MessagingException e) {
                // TODO Auto-generated catch block
                System.out.println("Failed to close " + mailaddress + " using server " + mailserver);
            }
        }
        return result;

    }

    /**
     * Send a mail message
     *
     * {@link IDeliveryMsgHandler#sendMessage(IMsgContent, SingleMsgSendResult)}
     */
    @Override
    public SingleMsgSentStateEnum sendMessage(VipsMessage vm, MsgReceiver msgReceiver, MsgToSend msgToSend) {
        try {
            if (this.testOnly) {
                System.out.println("Test only is true so this message is not sent " + msgToSend.getMsgBody() + " to " + msgReceiver.msgDeliveryAddress
                        + " using server " + mailserver);
            } else {
                sendMessage(msgToSend.getMsgSubject(), msgToSend.getMsgLeadParagraph(), new StringBuffer(msgToSend.getMsgBody()).append("\n\n")
                        .append(msgToSend.getMsgDownloadUrl() != null ? msgToSend.getMsgDownloadUrl():"")
                                .append(msgToSend.getIncludeNotificationSettingsLink() ? "\n\n" + MessageFormat.format(msgToSend.getNotificationSettingsLinkTpl(),msgReceiver.recipientId): "\n"),
                        msgReceiver.msgDeliveryAddress);
            }
            return SingleMsgSentStateEnum.Ok;
        } catch (UnsupportedEncodingException | MessagingException e) {
            System.out.println("Failed to send message " + msgToSend.getMsgBody() + " to " + msgReceiver.msgDeliveryAddress + " using server " + mailserver);
            return SingleMsgSentStateEnum.FailedNotSentWithException;
        } 

    }

    /**
     * Get content type for this handler
     *
     * {@link IDeliveryMsgHandler#sendMessage(IMsgContent, SingleMsgSendResult)}
     */
    @Override
    public DistributionTypeEnum getDistributionType() {
        return DistributionTypeEnum.Mail;
    }

}
