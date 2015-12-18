/*
 * Copyright (c) 2015 NIBIO <http://www.nibio.no/>. 
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import no.nibio.vips.logic.util.PostgresJSONStringConverter;
import no.nibio.vips.logic.util.StringJsonUserType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

/**
 * @copyright 2015 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "universal_message", schema = "messaging")
@XmlRootElement
@TypeDefs( {@TypeDef( name= "StringJsonObject", typeClass = StringJsonUserType.class)})
@NamedQueries({
    @NamedQuery(name = "UniversalMessage.findAll", query = "SELECT u FROM UniversalMessage u"),
    @NamedQuery(name = "UniversalMessage.findByUniversalMessageId", query = "SELECT u FROM UniversalMessage u WHERE u.universalMessageId = :universalMessageId"),
    @NamedQuery(name = "UniversalMessage.findByMsgSubject", query = "SELECT u FROM UniversalMessage u WHERE u.msgSubject = :msgSubject"),
    @NamedQuery(name = "UniversalMessage.findByMsgLeadParagraph", query = "SELECT u FROM UniversalMessage u WHERE u.msgLeadParagraph = :msgLeadParagraph"),
    @NamedQuery(name = "UniversalMessage.findByMsgBody", query = "SELECT u FROM UniversalMessage u WHERE u.msgBody = :msgBody"),
    @NamedQuery(name = "UniversalMessage.findByMsgDownloadUrl", query = "SELECT u FROM UniversalMessage u WHERE u.msgDownloadUrl = :msgDownloadUrl"),
    @NamedQuery(name = "UniversalMessage.findByExpiresAt", query = "SELECT u FROM UniversalMessage u WHERE u.expiresAt = :expiresAt")})
public class UniversalMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "universal_message_id")
    private Integer universalMessageId;
    @Size(max = 2047)
    @Column(name = "msg_subject")
    private String msgSubject;
    @Size(max = 2147483647)
    @Column(name = "msg_lead_paragraph")
    private String msgLeadParagraph;
    @Size(max = 2147483647)
    @Column(name = "msg_body")
    private String msgBody;
    @Size(max = 1023)
    @Column(name = "msg_download_url")
    private String msgDownloadUrl;
    @Column(name = "expires_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expiresAt;
    //@Column(name = "distribution_list", columnDefinition = "json")
    //@Convert(converter = PostgresJSONStringConverter.class)
    // Documentation on StackOverflow: http://stackoverflow.com/questions/15974474/mapping-postgresql-json-column-to-hibernate-value-type
    @Type(type = "StringJsonObject")
    @Column(name = "distribution_list")
    private String distributionList;
    

    public UniversalMessage() {
    }
    
    public UniversalMessage(
            String msgSubject,
            String msgLeadParagraph,
            String msgBody,
            String msgDownloadUrl
    )
    {
        this.msgSubject = msgSubject;
        this.msgLeadParagraph = msgLeadParagraph;
        this.msgBody = msgBody;
        this.msgDownloadUrl = msgDownloadUrl;
    }

    public UniversalMessage(Integer universalMessageId) {
        this.universalMessageId = universalMessageId;
    }

    @JsonIgnore
    public Integer getUniversalMessageId() {
        return universalMessageId;
    }

    public void setUniversalMessageId(Integer universalMessageId) {
        this.universalMessageId = universalMessageId;
    }

    public String getMsgSubject() {
        return msgSubject;
    }

    public void setMsgSubject(String msgSubject) {
        this.msgSubject = msgSubject;
    }

    public String getMsgLeadParagraph() {
        return msgLeadParagraph;
    }

    public void setMsgLeadParagraph(String msgLeadParagraph) {
        this.msgLeadParagraph = msgLeadParagraph;
    }

    public String getMsgBody() {
        return msgBody;
    }

    public void setMsgBody(String msgBody) {
        this.msgBody = msgBody;
    }

    public String getMsgDownloadUrl() {
        return msgDownloadUrl;
    }

    public void setMsgDownloadUrl(String msgDownloadUrl) {
        this.msgDownloadUrl = msgDownloadUrl;
    }

    @JsonIgnore
    public Date getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
    }

    @JsonRawValue
    public String getDistributionList() {
        return distributionList;
    }
    
    public void setDistributionList(String distributionList) {
        this.distributionList = distributionList;
    }
    
    public void setDistributionList(List<MessageRecipient> distributionList) {
        try {
            this.distributionList = new ObjectMapper().writeValueAsString(distributionList);
        } catch (JsonProcessingException ex) {
            Logger.getLogger(UniversalMessage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (universalMessageId != null ? universalMessageId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof UniversalMessage)) {
            return false;
        }
        UniversalMessage other = (UniversalMessage) object;
        if ((this.universalMessageId == null && other.universalMessageId != null) || (this.universalMessageId != null && !this.universalMessageId.equals(other.universalMessageId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.messaging.UniversalMessage[ universalMessageId=" + universalMessageId + " ]";
    }

}
