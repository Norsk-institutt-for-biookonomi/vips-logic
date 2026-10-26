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

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.inject.spi.CDI;
import no.nibio.vips.logic.messaging.distribution.entity.DistributionTypeEnum;
import no.nibio.vips.logic.messaging.distribution.entity.MsgReceiver;
import no.nibio.vips.logic.messaging.distribution.entity.MsgToSend;
import no.nibio.vips.logic.messaging.distribution.entity.VipsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;


/**
 * Handles sending off mail messages via Quarkus Mailer.
 *
 * @author Lars Aksel Opsahl <lars.opsahl@nibio.no>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class MailMsgDeliveryHandler implements IDeliveryMsgHandler {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(MailMsgDeliveryHandler.class);

    private final String sender;

    // if this no mail will be sent, but the return result is true
    private final boolean testOnly;

    public MailMsgDeliveryHandler(String mailserver, String sender, boolean testOnly) {
        this.sender = sender;
        this.testOnly = testOnly;
    }

    private void sendMessage(String subjekt, String leadParagraph, StringBuffer mailText, String mailaddress) {
        String mailMessageBody = leadParagraph;
        if (leadParagraph != null && leadParagraph.length() > 0) {
            mailMessageBody = leadParagraph + " \r\n" + mailText.toString();
        } else {
            mailMessageBody = mailText.toString();
        }
        LOGGER.debug("Message ready to send : {}", mailMessageBody);
        CDI.current().select(Mailer.class).get().send(
                Mail.withText(mailaddress, subjekt, mailMessageBody).setFrom(sender));
    }

    @Override
    public SingleMsgSentStateEnum sendMessage(VipsMessage vm, MsgReceiver msgReceiver, MsgToSend msgToSend) {
        try {
            if (this.testOnly) {
                System.out.println("Test only is true so this message is not sent " + msgToSend.getMsgBody() + " to " + msgReceiver.msgDeliveryAddress);
            } else {
                sendMessage(msgToSend.getMsgSubject(), msgToSend.getMsgLeadParagraph(), new StringBuffer(msgToSend.getMsgBody()).append("\n\n")
                        .append(msgToSend.getMsgDownloadUrl() != null ? msgToSend.getMsgDownloadUrl():"")
                                .append(vm.getIncludeNotificationSettingsLink() ? "\n\n" + MessageFormat.format(msgToSend.getNotificationSettingsLinkTpl(),msgReceiver.recipientId): "\n"),
                        msgReceiver.msgDeliveryAddress);
            }
            return SingleMsgSentStateEnum.Ok;
        } catch (Exception e) {
            System.out.println("Failed to send message " + msgToSend.getMsgBody() + " to " + msgReceiver.msgDeliveryAddress);
            return SingleMsgSentStateEnum.FailedNotSentWithException;
        }
    }

    @Override
    public DistributionTypeEnum getDistributionType() {
        return DistributionTypeEnum.Mail;
    }
}
