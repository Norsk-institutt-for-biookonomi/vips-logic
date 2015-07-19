/*
 * Copyright (c) 2014 NIBIO <http://www.nibio.no/>. 
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

package no.nibio.vips.logic.util;


import java.util.Iterator;
import java.util.List;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;


/**
 * TODO: Better error handling!!!!
 * Simple utility class for sending a mail using the JavaMail API
 * @copyright 2014 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class SimpleMailSender {
    
    private final String SMTPserver;
    
    private final boolean DEBUG=false;
    
    /** Creates a new instance of SimpleMailSender
     * @param SMTPserver the url of the SMTP server to relay mail through
     */
    public SimpleMailSender(String SMTPserver) {
        this.SMTPserver = SMTPserver;
    }
    
    /** <pre> Sends a mail message with the specified contents </pre>
     * @param from the sender
     * @param to the recipient
     * @param subject the subject
     * @param body the body text
     *
     */
    public void sendMail(String from, String to, String subject, String body)
    {
        if(DEBUG)
            System.out.println("[SimpleMailSender] DEBUG from=" + from + ",to=" + to + ",SMTPserver=" + this.SMTPserver);
        try
        {
            Properties props = System.getProperties();
            props.put("mail.smtp.host", SMTPserver);
            Session session = Session.getDefaultInstance(props, null);

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
        }
        catch(javax.mail.internet.AddressException ae){ae.printStackTrace();}
        catch(javax.mail.MessagingException me) {me.printStackTrace();}
    }

    /**
     * UNTESTED
     * @param from
     * @param to
     * @param bccRecipients
     * @param subject
     * @param body
     */
    public void sendMail(String from, String to, List<String> bccRecipients, String subject, String body)
    {
        if(DEBUG)
            System.out.println("[SimpleMailSender] DEBUG from=" + from + ",to=" + to + ",SMTPserver=" + this.SMTPserver);
        try
        {
            Properties props = System.getProperties();
            props.put("mail.smtp.host", SMTPserver);
            Session session = Session.getDefaultInstance(props, null);

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(subject);
            message.setText(body);

            for(Iterator<String> i = bccRecipients.iterator();i.hasNext();)
            {
                String recipient = i.next();
                message.addRecipient(Message.RecipientType.BCC, new InternetAddress(recipient));
            }

            Transport.send(message);
        }
        catch(javax.mail.internet.AddressException ae){ae.printStackTrace();}
        catch(javax.mail.MessagingException me) {me.printStackTrace();}
    }
    
}