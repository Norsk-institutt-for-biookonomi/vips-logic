package no.bioforsk.vips.logic.entity;

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
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
@Entity
@Table(name = "vips_logic_user")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "VipsLogicUser.findAll", query = "SELECT v FROM VipsLogicUser v"),
    @NamedQuery(name = "VipsLogicUser.findByUserId", query = "SELECT v FROM VipsLogicUser v WHERE v.userId = :userId"),
    @NamedQuery(name = "VipsLogicUser.findByOrganizationId", query = "SELECT v FROM VipsLogicUser v WHERE v.organizationId = :organizationId"),
    @NamedQuery(name = "VipsLogicUser.findByEmail", query = "SELECT v FROM VipsLogicUser v WHERE v.email = :email"),
    @NamedQuery(name = "VipsLogicUser.findByEmailVerificationCode", query = "SELECT v FROM VipsLogicUser v WHERE v.emailVerificationCode = :emailVerificationCode"),
    @NamedQuery(name = "VipsLogicUser.findByFirstName", query = "SELECT v FROM VipsLogicUser v WHERE v.firstName = :firstName"),
    @NamedQuery(name = "VipsLogicUser.findByLastName", query = "SELECT v FROM VipsLogicUser v WHERE v.lastName = :lastName")})
public class VipsLogicUser implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "user_id")
    private Integer userId;
    //if the field contains email address consider using this annotation to enforce field validation
    @Pattern(regexp="[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?", message="Invalid email")
    @Size(max = 1023)
    @Column(name = "email")
    private String email;
    @Size(max = 255)
    @Column(name = "first_name")
    private String firstName;
    @Size(max = 255)
    @Column(name = "last_name")
    private String lastName;
    @Size(max = 2047)
    @Column(name = "approval_application")
    private String approvalApplication;
    @Size(max = 2147483647)
    @Column(name = "remarks")
    private String remarks;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "vipsLogicUser")
    private Set<UserAuthentication> userAuthenticationSet;
    @JoinColumn(name = "organization_id", referencedColumnName = "organization_id")
    @ManyToOne
    private Organization organizationId;
    @Column(name = "user_status_id")
    private Integer userStatusId;
    @Column(name = "email_verification_code")
    private String emailVerificationCode;
    @ManyToMany(fetch=FetchType.EAGER)
    @JoinTable(
            name = "user_vips_logic_role",
            joinColumns = {@JoinColumn(name = "user_id")},
            inverseJoinColumns = {@JoinColumn(name = "vips_logic_role_id")}
    )
    private Set<VipsLogicRole> vipsLogicRoles;
    @Column(name = "vips_core_user_id")
    private Integer vipsCoreUserId;
    
    public VipsLogicUser() {
    }

    public VipsLogicUser(Integer userId) {
        this.userId = userId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @XmlTransient
    @JsonIgnore
    public Set<UserAuthentication> getUserAuthenticationSet() {
        return userAuthenticationSet;
    }

    public void setUserAuthenticationSet(Set<UserAuthentication> userAuthenticationSet) {
        this.userAuthenticationSet = userAuthenticationSet;
    }

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
        return "no.bioforsk.vips.logic.entity.VipsLogicUser[ userId=" + userId + " ]";
    }

    /**
     * @return the approvalApplication
     */
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
    public boolean isSuperUser()
    {
        for(VipsLogicRole role:this.getVipsLogicRoles()){
            if(role.getVipsLogicRoleId().equals(VipsLogicRole.SUPERUSER))
                return true;
        }
        return false;
    }
    
    @JsonIgnore
    public boolean isOrganizationAdmin()
    {
        for(VipsLogicRole role:this.getVipsLogicRoles()){
            if(role.getVipsLogicRoleId().equals(VipsLogicRole.ORGANIZATION_ADMINISTRATOR))
                return true;
        }
        return false;
    }
    
    @JsonIgnore
    public boolean hasRole(Integer... roles)
    {
        for(VipsLogicRole userRole: this.getVipsLogicRoles())
        {
            for(Integer roleId: roles)
            {
                if(userRole.getVipsLogicRoleId().equals(roleId))
                {
                    return true;
                }
            }
        }
        return false;
    }
    
    @JsonIgnore
    public boolean hasRole(VipsLogicRole... roles)
    {
        for(VipsLogicRole userRole: this.getVipsLogicRoles())
        {
            for(VipsLogicRole role: roles)
            {
                if(userRole.equals(role))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @return the vipsCoreUserId
     */
    public Integer getVipsCoreUserId() {
        return vipsCoreUserId;
    }

    /**
     * @param vipsCoreUserId the vipsCoreUserId to set
     */
    public void setVipsCoreUserId(Integer vipsCoreUserId) {
        this.vipsCoreUserId = vipsCoreUserId;
    }

    /**
     * @return the emailVerificationCode
     */
    public String getEmailVerificationCode() {
        return emailVerificationCode;
    }

    /**
     * @param emailVerificationCode the emailVerificationCode to set
     */
    public void setEmailVerificationCode(String emailVerificationCode) {
        this.emailVerificationCode = emailVerificationCode;
    }
}
