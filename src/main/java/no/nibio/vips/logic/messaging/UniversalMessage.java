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
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;
import no.nibio.vips.logic.util.StringJsonUserType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

/**
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "universal_message", schema = "messaging")
@XmlRootElement
@TypeDefs( {@TypeDef( name= "StringJsonObject", typeClass = StringJsonUserType.class)})
@NamedQueries({
    @NamedQuery(name = "UniversalMessage.findAll", query = "SELECT u FROM UniversalMessage u"),
    @NamedQuery(name = "UniversalMessage.findByUniversalMessageId", query = "SELECT u FROM UniversalMessage u WHERE u.universalMessageId = :universalMessageId"),
    @NamedQuery(name = "UniversalMessage.findByExpiresAt", query = "SELECT u FROM UniversalMessage u WHERE u.expiresAt = :expiresAt")})
public class UniversalMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "universal_message_id")
    private Integer universalMessageId;
    @Column(name = "expires_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expiresAt;
    //@Column(name = "distribution_list", columnDefinition = "json")
    //@Convert(converter = PostgresJSONStringConverter.class)
    // Documentation on StackOverflow: http://stackoverflow.com/questions/15974474/mapping-postgresql-json-column-to-hibernate-value-type
    @Type(type = "StringJsonObject")
    @Column(name = "distribution_list")
    private String distributionList;
    
    @Type(type = "StringJsonObject")
    @Column(name = "message_local_versions")
    private String messageLocalVersions;
    
    @Transient
    private ObjectMapper objectMapper;
    

    public UniversalMessage() {
    }
    
    public UniversalMessage(
        String locale,
        String msgSubject,
        String msgLeadParagraph,
        String msgBody,
        String msgDownloadUrl
    )
    {
        this.addMessageLocalVersion(locale, msgSubject, msgLeadParagraph, msgBody, msgDownloadUrl);
    }
    
    public final void addMessageLocalVersion(
        String locale,
        String msgSubject,
        String msgLeadParagraph,
        String msgBody,
        String msgDownloadUrl
    )
    {
        
        MessageLocalVersion newLocalVersion = new MessageLocalVersion(locale, msgSubject, msgLeadParagraph, msgBody, msgDownloadUrl);
        List<MessageLocalVersion> existingLocalVersions = this.getMessageLocalVersionObjects();
        existingLocalVersions.add(newLocalVersion);
        this.setMessageLocalVersions(existingLocalVersions);
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
            this.distributionList = this.getObjectMapper().writeValueAsString(distributionList);
        } catch (JsonProcessingException ex) {
            Logger.getLogger(UniversalMessage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @JsonIgnore
    @Transient
    public List<MessageRecipient> getDistributionListObjects()
    {
        List<MessageRecipient> retVal = null;
        try {
            retVal = this.getObjectMapper().readValue(this.getDistributionList(), new TypeReference<List<MessageRecipient>>(){});
        } catch (IOException | NullPointerException ex) {
            Logger.getLogger(UniversalMessage.class.getName()).log(Level.SEVERE, null, ex);
        }
        return retVal != null ? retVal : new ArrayList<MessageRecipient>();
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

    /**
     * @return the messageLocalVersions
     */
    @JsonRawValue
    public String getMessageLocalVersions() {
        return messageLocalVersions != null ? this.messageLocalVersions : "[]";
    }
    
    @JsonIgnore
    @Transient
    public List<MessageLocalVersion> getMessageLocalVersionObjects()
    {
        
        List<MessageLocalVersion> retVal = null;
        try {
            retVal = this.getObjectMapper().readValue(this.getMessageLocalVersions(), new TypeReference<List<MessageLocalVersion>>(){});
        } catch (IOException | NullPointerException ex) {
            Logger.getLogger(UniversalMessage.class.getName()).log(Level.SEVERE, null, ex);
        }
        return retVal != null ? retVal : new ArrayList<MessageLocalVersion>();
    }

    /**
     * @param messageLocalVersions the messageLocalVersions to set
     */
    public void setMessageLocalVersions(String messageLocalVersions) {
        this.messageLocalVersions = messageLocalVersions;
    }
    
    public void setMessageLocalVersions(List<MessageLocalVersion> messageLocalVersions)
    {
        try {
            this.messageLocalVersions = this.getObjectMapper().writeValueAsString(messageLocalVersions);
        } catch (JsonProcessingException ex) {
            Logger.getLogger(UniversalMessage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
    private ObjectMapper getObjectMapper()
    {
        if(this.objectMapper == null)
        {
            this.objectMapper = new ObjectMapper();
        }
        return this.objectMapper;
    }
}
