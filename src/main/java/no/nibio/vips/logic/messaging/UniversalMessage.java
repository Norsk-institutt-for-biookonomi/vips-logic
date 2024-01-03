/*
 * Copyright (c) 2015 NIBIO <http://www.nibio.no/>. 
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
import java.util.ResourceBundle;
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

import com.ibm.icu.util.ULocale;
import javax.persistence.Convert;
import no.nibio.vips.logic.util.StringJsonUserType;


/**
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "universal_message", schema = "messaging")
@XmlRootElement
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
    @Convert(converter = StringJsonUserType.class)
    @Column(name = "distribution_list")
    private String distributionList;
    
    @Convert(converter = StringJsonUserType.class)
    @Column(name = "message_local_versions")
    private String messageLocalVersions;
    
    @Transient
    private ObjectMapper objectMapper;

    // Include by default. If not needed, use constructor that makes it configurable
    @Transient
    private Boolean includeNotificationSettingsLink = Boolean.TRUE;
    

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

    public UniversalMessage(
            String locale,
            String msgSubject,
            String msgLeadParagraph,
            String msgBody,
            String msgDownloadUrl,
            Boolean includeNotificationSettingsLink
    )
    {
        this(locale, msgSubject, msgLeadParagraph, msgBody, msgDownloadUrl);
        this.setIncludeNotificationSettingsLink(includeNotificationSettingsLink);
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
        
        return "no.nibio.vips.logic.messaging.UniversalMessage[ universalMessageId=" + universalMessageId + " ]\n"
                + (this.getMessageLocalVersionObjects().size() > 0 ? 
                    "Subject = " + this.getMessageLocalVersionObjects().get(0).getMsgSubject() + "\n" +
                    "Recipients = " + this.getDistributionList()
                : "");
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

    /**
     * @param locale two-letter language string
     * @return The localized string **template** for generating a link to a user's page for
     * managing notification subscription settings
     */
    @Transient
    public String getNotificationSettingsLinkTpl(String locale)
    {
        ULocale theLocale = new ULocale(locale);
        ResourceBundle rb = ResourceBundle.getBundle("no.nibio.vips.logic.i18n.vipslogictexts",theLocale.toLocale());
        return rb.getString("universalMessageSettingsLink_tpl");
    }
    
    private ObjectMapper getObjectMapper()
    {
        if(this.objectMapper == null)
        {
            this.objectMapper = new ObjectMapper();
        }
        return this.objectMapper;
    }

    /**
     * If true, the SMS and mail messages will have this type of link appended at the end:
     * "To edit your notification subscriptions, please use this link: https://logic.vips.nibio.no/user/notificationsubscription?userId=1"
     *  The server name can be configured with the system properties no.nibio.vips.logic.VIPSLOGIC_PROTOCOL and no.nibio.vips.logic.SERVER_NAME
     * @return
     */
    @Transient
    public Boolean getIncludeNotificationSettingsLink() {
        return includeNotificationSettingsLink;
    }

    public void setIncludeNotificationSettingsLink(Boolean includeNotificationSettingsLink) {
        this.includeNotificationSettingsLink = includeNotificationSettingsLink;
    }
}
