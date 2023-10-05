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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.MessageFormat;

import javax.mail.MessagingException;
import no.nibio.vips.logic.messaging.distribution.entity.MsgReceiver;
import no.nibio.vips.logic.messaging.distribution.entity.MsgToSend;
import no.nibio.vips.logic.messaging.distribution.entity.VipsMessage;
import no.nibio.vips.logic.messaging.distribution.entity.DistributionTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles sending off sms messages
 *
 * @author Lars Aksel Opsahl <lars.opsahl@nibio.no>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 *
 */
public class SmsMsgDeliveryHandler implements IDeliveryMsgHandler {

    private final static Logger LOGGER = LoggerFactory.getLogger(SmsMsgDeliveryHandler.class);

    // properties needed by the smsc connection handler
    private final String smscServer;
    private final String smscUsername;
    private final String smscPassword;
    private final String smscSenderSrc;

    // if this no mail will be sent, but the return result is true
    private final boolean testOnly;

    public SmsMsgDeliveryHandler(String smscServer, String smscUsername, String smscPassword, String smscSenderSrc, boolean testOnly) {
        this.smscServer = smscServer;
        this.smscUsername = smscUsername;
        this.smscPassword = smscPassword;
        this.smscSenderSrc = smscSenderSrc;
        this.testOnly = testOnly;
    }

    /**
     * This is used to check to Authorization
     *
     * @param userToken
     * @return
     * @throws MalformedURLException
     * @throws IOException
     */
    private boolean sendMessage(String subjekt, String smsText, String smsAddress, String pricegroup) throws MessagingException, MalformedURLException, IOException {
        boolean result = false;
        InputStream response = null;
        URLConnection connection = null;
        try {
            LOGGER.debug("Message ready to send : " + smsText);

            // http://smsc.vianett.no/v3/send.ashx?src=xxxxx&dst=xxxxx&msg=Hello+world&username=xxxxx&password=xxxxx&pricegroup=xx
            String charset = "UTF-8"; // Or in Java 7 and later, use the constant: java.nio.charset.StandardCharsets.UTF_8.name()
            String query = String.format("src=%s", URLEncoder.encode(smscSenderSrc, charset)) + "&"
                    + String.format("dst=%s", URLEncoder.encode(smsAddress, charset)) + "&" + String.format("msg=%s", URLEncoder.encode(smsText, charset)) + "&"
                    + String.format("username=%s", URLEncoder.encode(smscUsername, charset)) + "&"
                    + String.format("password=%s", URLEncoder.encode(smscPassword, charset)) + "&"
                    + String.format("pricegroup=%s", URLEncoder.encode(pricegroup, charset));

            String sendUrl = "https://" + smscServer + "/v3/send.ashx";

            connection = new URL(sendUrl + "?" + query).openConnection();
            connection.setRequestProperty("Accept-Charset", charset);
            response = connection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(response));
            // Works with java 1.8 only
            // String collect = br.lines().collect(Collectors.joining(System.lineSeparator()));

            // Works with java 1.7
            StringBuilder collectBuffer = new StringBuilder();
            int value = 0;
            while ((value = br.read()) != -1) {
                // converts int to character
                char c = (char) value;
                collectBuffer.append(c);
            }

            String collect = collectBuffer.toString();

            if ("200|OK".equals(collect)) {
                result = true;
            } else {
                LOGGER.error("Result was invalid, got '" + collect + "' from " + sendUrl + " to " + smsAddress);
            }

            LOGGER.debug("Result was " + result + " from " + sendUrl + " to " + smsAddress);
			
        } finally {
            if (response != null) {
                response.close();
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
        String smsText = msgToSend.getMsgSubject() + "\n"
                + msgToSend.getMsgLeadParagraph() + "\n"
                + msgToSend.getMsgBody()
                + (msgToSend.getMsgDownloadUrl() != null ? "\n" + msgToSend.getMsgDownloadUrl().toString(): "\n")
                + (vm.getIncludeNotificationSettingsLink() ? "\n\n" + MessageFormat.format(msgToSend.getNotificationSettingsLinkTpl(), msgReceiver.recipientId): "\n");
        LOGGER.debug(smsText);
        LOGGER.debug("Teksten er på " + smsText.length() + " tegn.");
        LOGGER.debug("Mottaker: " + msgReceiver.msgDeliveryAddress);

        try {
            // TODO: Make the price part of VipsMessage
            String pricegroup = msgReceiver.freeSms ? "00" : "100";
/*
            if (smsText.length() > 179) {
                smsText = msgToSend.getMsgSubject() + "\n"
                        + msgToSend.getMsgLeadParagraph() + "\n"
                        + "Les mer: " + msgToSend.getMsgDownloadUrl();
            }

            if (smsText.length() > 179) {
                smsText = msgToSend.getMsgSubject() + "\n"
                        + "Les mer: " + msgToSend.getMsgDownloadUrl().toString();
            }

            if (smsText.length() > 179) {
                smsText = msgToSend.getMsgSubject() + "\n"
                        + "Les mer: " + msgToSend.getMsgDownloadUrl().toString();
            }

            if (smsText.length() > 179) {
                smsText = "Les melding fra Vips:" + msgToSend.getMsgDownloadUrl().toString();
            }
*/
            if (testOnly) {

                LOGGER.info("Test only is true so this message is not sent " + smsText + " to " + msgReceiver.msgDeliveryAddress + " using server " + smscServer);

            } else {

                sendMessage(msgToSend.getMsgSubject(), smsText, msgReceiver.msgDeliveryAddress, pricegroup);
            }
            return SingleMsgSentStateEnum.Ok;
        } catch (MessagingException | IOException e) {
            LOGGER.error("Failed to send message " + smsText + " to " + msgReceiver.msgDeliveryAddress + " using server " + smscServer,e);
            return SingleMsgSentStateEnum.FailedNotSentWithException;
        } catch (Throwable e1) {
            LOGGER.error("Failed to send message " + smsText + " to " + msgReceiver.msgDeliveryAddress + " using server " + smscServer,e1);
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
        return DistributionTypeEnum.Sms;
    }

}
