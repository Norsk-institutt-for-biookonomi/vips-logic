/*
 * Copyright (c) 2016 NIBIO <http://www.nibio.no/>. 
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

package no.nibio.vips.logic.messaging;

/**
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class MessageLocalVersion {
    private String locale, msgSubject, msgLeadParagraph, msgBody, msgDownloadUrl;

    public MessageLocalVersion(){}
    
    public MessageLocalVersion(
        String locale,
        String msgSubject,
        String msgLeadParagraph,
        String msgBody,
        String msgDownloadUrl
    )
    {
        this.locale = locale;
        this.msgSubject = msgSubject;
        this.msgLeadParagraph = msgLeadParagraph;
        this.msgBody = msgBody;
        this.msgDownloadUrl = msgDownloadUrl;
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
     * @return the msgSubject
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

    /**
     * @return the msgLeadParagraph
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
     * @return the msgBody
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

    /**
     * @return the msgDownloadUrl
     */
    public String getMsgDownloadUrl() {
        return msgDownloadUrl;
    }

    /**
     * @param msgDownloadUrl the msgDownloadUrl to set
     */
    public void setMsgDownloadUrl(String msgDownloadUrl) {
        this.msgDownloadUrl = msgDownloadUrl;
    }
    
}
