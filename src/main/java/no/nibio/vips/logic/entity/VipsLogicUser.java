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
package no.nibio.vips.logic.entity;

import java.io.Serializable;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.UUID;

/**
 * @copyright 2015 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "vips_logic_user")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "VipsLogicUser.findAll", query = "SELECT v FROM VipsLogicUser v"),
    @NamedQuery(name = "VipsLogicUser.findByUserId", query = "SELECT v FROM VipsLogicUser v WHERE v.userId = :userId"),
    @NamedQuery(name = "VipsLogicUser.findByOrganizationId", query = "SELECT v FROM VipsLogicUser v WHERE v.organizationId = :organizationId OR  v.organizationId IN(SELECT o.organizationId FROM Organization o WHERE o.parentOrganizationId = :organizationId)"),
    @NamedQuery(name = "VipsLogicUser.findByOrganizationIds", query = "SELECT v FROM VipsLogicUser v WHERE v.organizationId.organizationId IN (:organizationIds)"),
    @NamedQuery(name = "VipsLogicUser.findByEmail", query = "SELECT v FROM VipsLogicUser v WHERE v.email = :email"),
    @NamedQuery(name = "VipsLogicUser.findByEmailVerificationCode", query = "SELECT v FROM VipsLogicUser v WHERE v.emailVerificationCode = :emailVerificationCode"),
    @NamedQuery(name = "VipsLogicUser.findByFirstName", query = "SELECT v FROM VipsLogicUser v WHERE v.firstName = :firstName"),
    @NamedQuery(name = "VipsLogicUser.findByLastName", query = "SELECT v FROM VipsLogicUser v WHERE v.lastName = :lastName")})
public class VipsLogicUser implements Serializable {

    private static final long serialVersionUID = 1L;
    private Integer userId;
    //if the field contains email address consider using this annotation to enforce field validation
    @Pattern(regexp = "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?", message = "Invalid email")
    @Size(max = 1023)
    private String email;
    @Size(max = 63)
    private String phone;
    @Size(max = 255)
    private String firstName;
    @Size(max = 255)
    private String lastName;
    @Size(max = 2047)
    private String approvalApplication;
    @Size(max = 2147483647)
    private String remarks;
    @Size(max = 10)
    private String preferredLocale;
    private Set<UserAuthentication> userAuthenticationSet;
    private Organization organizationId;
    private Integer userStatusId;
    private String emailVerificationCode;
    private Set<VipsLogicRole> vipsLogicRoles;
    private Integer vipsCoreUserId;
    
    private UUID userUuid;

    public VipsLogicUser() {
    }

    public VipsLogicUser(Integer userId) {
        this.userId = userId;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "user_id")
    @JsonIgnore
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    @Column(name = "email")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Column(name = "first_name")
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Column(name = "last_name")
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "vipsLogicUser")
    @XmlTransient
    @JsonIgnore
    public Set<UserAuthentication> getUserAuthenticationSet() {
        return userAuthenticationSet;
    }

    public void setUserAuthenticationSet(Set<UserAuthentication> userAuthenticationSet) {
        this.userAuthenticationSet = userAuthenticationSet;
    }

    @JoinColumn(name = "organization_id", referencedColumnName = "organization_id")
    @ManyToOne
    @JsonIgnore
    public Organization getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Organization organizationId) {
        this.organizationId = organizationId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (userId != null ? userId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof VipsLogicUser)) {
            return false;
        }
        VipsLogicUser other = (VipsLogicUser) object;
        if ((this.userId == null && other.userId != null) || (this.userId != null && !this.userId.equals(other.userId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.entity.VipsLogicUser[ userId=" + userId + " ]";
    }

    /**
     * @return the approvalApplication
     */
    @Column(name = "approval_application")
    @JsonIgnore
    public String getApprovalApplication() {
        return approvalApplication;
    }

    /**
     * @param approvalApplication the approvalApplication to set
     */
    public void setApprovalApplication(String approvalApplication) {
        this.approvalApplication = approvalApplication;
    }

    /**
     * @return the remarks
     */
    @Column(name = "remarks")
    @JsonIgnore
    public String getRemarks() {
        return remarks;
    }

    /**
     * @param remarks the remarks to set
     */
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     * @return the userStatusId
     */
    @JsonIgnore
    @Column(name = "user_status_id")
    public Integer getUserStatusId() {
        return userStatusId;
    }

    /**
     * @param userStatusId the userStatusId to set
     */
    public void setUserStatusId(Integer userStatusId) {
        this.userStatusId = userStatusId;
    }

    /**
     * @return the vipsLogicRoles
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JsonIgnore
    @JoinTable(
            name = "user_vips_logic_role",
            joinColumns = {
                @JoinColumn(name = "user_id")},
            inverseJoinColumns = {
                @JoinColumn(name = "vips_logic_role_id")}
    )
    public Set<VipsLogicRole> getVipsLogicRoles() {
        return vipsLogicRoles;
    }

    /**
     * @param vipsLogicRoles the vipsLogicRoles to set
     */
    public void setVipsLogicRoles(Set<VipsLogicRole> vipsLogicRoles) {
        this.vipsLogicRoles = vipsLogicRoles;
    }

    @JsonIgnore
    @Transient
    public boolean isSuperUser() {
        for (VipsLogicRole role : this.getVipsLogicRoles()) {
            if (role.getVipsLogicRoleId().equals(VipsLogicRole.SUPERUSER)) {
                return true;
            }
        }
        return false;
    }

    @JsonIgnore
    @Transient
    public boolean isOrganizationAdmin() {
        for (VipsLogicRole role : this.getVipsLogicRoles()) {
            if (role.getVipsLogicRoleId().equals(VipsLogicRole.ORGANIZATION_ADMINISTRATOR)) {
                return true;
            }
        }
        return false;
    }
    
    @JsonIgnore
    @Transient
    public boolean isObservationAuthority(){
        for (VipsLogicRole role : this.getVipsLogicRoles()) {
            if (role.getVipsLogicRoleId().equals(VipsLogicRole.OBSERVATION_AUTHORITY)) {
                return true;
            }
        }
        return false;
    }

    @JsonIgnore
    public boolean hasRole(Integer... roles) {
        if (this.getVipsLogicRoles() == null) {
            return false;
        }

        for (VipsLogicRole userRole : this.getVipsLogicRoles()) {
            for (Integer roleId : roles) {
                if (userRole.getVipsLogicRoleId().equals(roleId)) {
                    return true;
                }
            }
        }
        return false;
    }

    @JsonIgnore
    public boolean hasRole(VipsLogicRole... roles) {
        if (this.getVipsLogicRoles() == null) {
            return false;
        }

        for (VipsLogicRole userRole : this.getVipsLogicRoles()) {
            for (VipsLogicRole role : roles) {
                if (userRole.equals(role)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @return the vipsCoreUserId
     */
    @Column(name = "vips_core_user_id")
    @JsonIgnore
    public Integer getVipsCoreUserId() {
        return vipsCoreUserId;
    }

    /**
     * @param vipsCoreUserId the vipsCoreUserId to set
     */
    public void setVipsCoreUserId(Integer vipsCoreUserId) {
        this.vipsCoreUserId = vipsCoreUserId;
    }

    @Transient
    @JsonIgnore
    public Integer getVipsCoreUserIdWithFallback() {
        return this.getVipsCoreUserId() != null ? this.getVipsCoreUserId() : this.getOrganizationId().getDefaultVipsCoreUserId();
    }

    /**
     * @return the emailVerificationCode
     */
    @Column(name = "email_verification_code")
    @JsonIgnore
    public String getEmailVerificationCode() {
        return emailVerificationCode;
    }

    /**
     * @param emailVerificationCode the emailVerificationCode to set
     */
    public void setEmailVerificationCode(String emailVerificationCode) {
        this.emailVerificationCode = emailVerificationCode;
    }

    @Transient
    @JsonIgnore
    public UserAuthentication getPasswordAuthentication() {
        if (this.getUserAuthenticationSet() == null) {
            return null;
        }
        for (UserAuthentication auth : this.getUserAuthenticationSet()) {
            if (auth.getUserAuthenticationType().getUserAuthenticationTypeId().equals(UserAuthenticationType.TYPE_PASSWORD)) {
                return auth;
            }
        }
        return null;
    }

    /**
     * @return the userUuid
     */
    @Transient
    public UUID getUserUuid() {
        return userUuid;
    }

    /**
     * @param userUuid the userUuid to set
     */
    public void setUserUuid(UUID userUuid) {
        this.userUuid = userUuid;
    }

    /**
     * @return the preferredLocale
     */
    @Column(name = "preferred_locale")
    public String getPreferredLocale() {
        return preferredLocale;
    }

    /**
     * @param preferredLocale the preferredLocale to set
     */
    public void setPreferredLocale(String preferredLocale) {
        this.preferredLocale = preferredLocale;
    }

    /**
     * @return the phone
     */
    public String getPhone() {
        return phone;
    }

    /**
     * @param phone the phone to set
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }
}
