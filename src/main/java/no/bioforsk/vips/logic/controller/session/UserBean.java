/*
 * Copyright (c) 2014 Bioforsk <http://www.bioforsk.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with VIPSLogic.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package no.bioforsk.vips.logic.controller.session;

import edu.vt.middleware.password.CharacterCharacteristicsRule;
import edu.vt.middleware.password.DigitCharacterRule;
import edu.vt.middleware.password.LengthRule;
import edu.vt.middleware.password.LowercaseCharacterRule;
import edu.vt.middleware.password.MessageResolver;
import edu.vt.middleware.password.Password;
import edu.vt.middleware.password.PasswordData;
import edu.vt.middleware.password.PasswordValidator;
import edu.vt.middleware.password.Rule;
import edu.vt.middleware.password.RuleResult;
import edu.vt.middleware.password.UppercaseCharacterRule;
import edu.vt.middleware.password.WhitespaceRule;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import no.bioforsk.vips.logic.authenticate.PasswordValidationException;
import no.bioforsk.vips.logic.entity.Organization;
import no.bioforsk.vips.logic.entity.UserAuthentication;
import no.bioforsk.vips.logic.entity.UserAuthenticationPK;
import no.bioforsk.vips.logic.entity.UserAuthenticationType;
import no.bioforsk.vips.logic.entity.VipsLogicUser;
import no.bioforsk.vips.logic.i18n.SessionLocaleUtil;
import no.bioforsk.vips.logic.util.Globals;
import no.bioforsk.vips.logic.util.SimpleMailSender;
import no.bioforsk.vips.logic.util.StringUtils;
import no.bioforsk.vips.util.MD5Encrypter;

/**
 * Handles user stuff, credentials
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
@LocalBean
@Stateless
public class UserBean {
    
    
    @PersistenceContext(unitName="VIPSLogic-PU")
    EntityManager em;
    
    private Properties serverProperties;
    private List<Rule> defaultPasswordValidatorRuleList;
    
    public VipsLogicUser authenticateUser(Map loginInfo)
    {
        
        try
        {
            Query q = em.createNamedQuery("UserAuthentication.findByUsernameAndPassword", UserAuthentication.class);
            q.setParameter("username", loginInfo.get("username"));
            
            q.setParameter("password", this.getMD5EncryptedString((String)loginInfo.get("password")));
            //System.out.println(this.getMD5EncryptedString((String)loginInfo.get("password")));
            UserAuthentication result = (UserAuthentication) q.getSingleResult();
            return result.getVipsLogicUser();
        }
        catch(NoResultException | IOException ex)
        {
            if(ex instanceof IOException)
            {
                // TODO Throw an error instead
                ex.printStackTrace();
            }
            // TODO sensible handling
            return null;
        }
    }
    
    /**
     * Gets a user with get given userName and type of authentication
     * @param userName
     * @param userAuthenticationTypeId
     * @return 
     */
    public VipsLogicUser getUser(String username, Integer userAuthenticationTypeId)
    {
        // If type of authentication is PASSWORD, we return nothing
        if(userAuthenticationTypeId.equals(UserAuthenticationType.TYPE_PASSWORD))
        {
            return null;
        }
        
        // We find the authentication with given name
        Query q = em.createNamedQuery("UserAuthentication.findByUsername");
        q.setParameter("username", username);
        List<UserAuthentication> userAuthentications = q.getResultList();
        for(UserAuthentication userAuth:userAuthentications)
        {
            // We check that only the given combination of user name and authentication type is a full match
            if(userAuth.getUserAuthenticationType().getUserAuthenticationTypeId().equals(userAuthenticationTypeId))
            {
                return userAuth.getVipsLogicUser();
            }
        }
        return null;
    }
    
    public void storeUserFirstTime(VipsLogicUser user,UserAuthentication auth)
    {
        em.persist(user);
        auth.setVipsLogicUser(user);
        UserAuthenticationPK pk = new UserAuthenticationPK(user.getUserId(), auth.getUserAuthenticationType().getUserAuthenticationTypeId());
        auth.setUserAuthenticationPK(pk);
        em.persist(auth);
        
    }
    
    public void sendUserEmailVerification(VipsLogicUser user, ResourceBundle i18nBundle, String serverName)
    {
        // Create verification string (random, alphanumeric, 30 characters)
        user.setEmailVerificationCode(StringUtils.generateRandomAlphanumericString(30));
        em.merge(user);
        String subject = i18nBundle.getString("userRegistrationEmailVerificationMessageSubject");
        String body = getUserEmailVerificationBody(i18nBundle, serverName, user.getEmailVerificationCode() );
        
        // Send email
        String smtpServer = System.getProperty("no.bioforsk.vips.logic.SMTP_SERVER");
        SimpleMailSender mailSender = new SimpleMailSender(smtpServer);
        mailSender.sendMail("noreply@" + serverName, user.getEmail(), subject, body);
        
    }
    
    public String getUserEmailVerificationBody(ResourceBundle i18nBundle, String serverName, String verificationCode)
    {
        String verificationURL = "http://" + serverName + "/user?action=confirmEmail&verificationCode=" + verificationCode;
        String body = MessageFormat.format(i18nBundle.getString("userRegistrationEmailVerificationMessageBody"), serverName,verificationURL);
        return body;
    }
    
    public void storeUser(VipsLogicUser user)
    {
        em.merge(user);
    }
    
    public void deleteUser(VipsLogicUser user)
    {
        user = em.find(VipsLogicUser.class, user.getUserId());
        em.remove(user);
    }
    
    /**
     * 
     * @param email
     * @return The user, or null if no user with given email is registered
     */
    public VipsLogicUser getUserByEmail(String email)
    {
        Query q = em.createNamedQuery("VipsLogicUser.findByEmail",VipsLogicUser.class);
        q.setParameter("email", email);
        try
        {
            return (VipsLogicUser) q.getSingleResult();
        }
        catch(NoResultException ex)
        {
            return null;
        }
    }
    
    public String getMD5EncryptedString(String str) throws IOException
    {
        return MD5Encrypter.getMD5HexString(str,System.getProperty("no.bioforsk.vips.logic.MD5_SALT"));   
    }
    
    /**
     * @return Properties for this server
     */
    private Properties getVIPSLogicServerProperties() throws IOException
    {
        if(this.serverProperties == null)
        {
            this.serverProperties = new Properties();
            try (InputStream in = this.getClass().getResourceAsStream("/server.properties")) {
                this.serverProperties.load(in);
            } 
        }
        return this.serverProperties;
    }
    
    public List<Organization> getOrganizations()
    {
        return em.createNamedQuery("Organization.findAll").getResultList();
    }
    
    public boolean isPasswordValid(String password, Locale errorMessageLocale) throws PasswordValidationException
    {
        // Check if we need localization of error messages
        String propertiesFileSuffix = "";
        if(!errorMessageLocale.equals(SessionLocaleUtil.DEFAULT_LOCALE))
        {
            propertiesFileSuffix = "_" + errorMessageLocale.getLanguage();
        }
        Properties props = new Properties();
        PasswordValidator validator;
        
        // Try to load localized error messages
        try
        {
            props.load(this.getClass().getResourceAsStream("/no/bioforsk/vips/logic/i18n/vtpassword" + propertiesFileSuffix + ".properties"));
            MessageResolver resolver = new MessageResolver(props);
            validator = new PasswordValidator(resolver, this.getDefaultPasswordValidatorRuleList());
        }
        // Something went wrong when attempting to get localized error messages. Skip that.
        catch(IOException ex)
        {
            validator = new PasswordValidator(this.getDefaultPasswordValidatorRuleList());
        }

        PasswordData passwordData = new PasswordData(new Password(password));
        RuleResult result = validator.validate(passwordData);
        if(result.isValid())
        {
            return true;
        }
        else
        {
            StringBuilder errorMsg = new StringBuilder();
            for(String msg:validator.getMessages(result))
            {
                errorMsg.append(msg).append("\n");
            }
            throw new PasswordValidationException(errorMsg.toString());
        }
    }
    
    /**
     * Ensures access to a properly (and uniformly) configured
     * Rule list for the PasswordValidator object
     */
    private List<Rule> getDefaultPasswordValidatorRuleList()
    {
        if(this.defaultPasswordValidatorRuleList == null)
        {
            this.defaultPasswordValidatorRuleList = this.createPasswordValidatorRuleList();
        }
        return this.defaultPasswordValidatorRuleList;
    }
    
    /**
     * Creates a list of rules for a PasswordValidator with with these criteria:
     * <ul>
     * <li>password must be between 8 and 16 chars long<li>
     * <li>don't allow whitespace</li>
     * <li>require at least 1 digit in passwords</li>
     * <li>require at least 1 upper case char</li>
     * <li>require at least 1 lower case char</li>
     * </ul>
     * @return 
     */
    private List<Rule> createPasswordValidatorRuleList()
    {
        // password must be between 8 and 16 chars long
        LengthRule lengthRule = new LengthRule(8, 16);

        // don't allow whitespace
        WhitespaceRule whitespaceRule = new WhitespaceRule();

        // control allowed characters
        CharacterCharacteristicsRule charRule = new CharacterCharacteristicsRule();
        // require at least 1 digit in passwords
        charRule.getRules().add(new DigitCharacterRule(1));
        // require at least 1 upper case char
        charRule.getRules().add(new UppercaseCharacterRule(1));
        // require at least 1 lower case char
        charRule.getRules().add(new LowercaseCharacterRule(1));
        // require all the previous rules be met
        charRule.setNumberOfCharacteristics(3);
        
        // group all rules together in a List
        List<Rule> ruleList = new ArrayList<>();
        ruleList.add(lengthRule);
        ruleList.add(whitespaceRule);
        ruleList.add(charRule);

        return ruleList;
    }
    
    public List<VipsLogicUser> getAllUsers()
    {
        return em.createNamedQuery("VipsLogicUser.findAll").getResultList();
    }
    
    public List<VipsLogicUser> getUsers(Organization organization)
    {
        return em.createNamedQuery("VipsLogicUser.findByOrganizationId").setParameter("organizationId", organization).getResultList();
    }
    
    /**
     * Checks if user has one of the given roles
     * @param user
     * @param roles
     * @return true if user has a role, false otherwise (including if user == null)
     */
    public boolean authorizeUser(VipsLogicUser user, Integer... roles)
    {
        if(user == null)
        {
            return false;
        }
        
        return user.hasRole(roles);
        
    }
    
    public Organization getOrganization(Integer organizationId)
    {
        return em.find(Organization.class, organizationId);
    }
    
    /**
     * Returns the available user status Ids
     * @return 
     */
    public Integer[] getUserStatusIds()
    {
        return new Integer []{
            Globals.USER_STATUS_AWAITING_EMAIL_VERIFICATION,
            Globals.USER_STATUS_AWAITING_APPROVAL,
            Globals.USER_STATUS_REJECTED,
            Globals.USER_STATUS_APPROVED,
            Globals.USER_STATUS_DISABLED
            };
    }

    /**
     * Searches user list by given verification code. If found, verification code
     * is deleted and user status set to waiting for approval
     * @param verificationCode
     * @return The found user, or null if not found
     */
    public VipsLogicUser verifyUserEmail(String emailVerificationCode) {
        try
        {
            VipsLogicUser confirmUser = em.createNamedQuery("VipsLogicUser.findByEmailVerificationCode", VipsLogicUser.class).setParameter("emailVerificationCode", emailVerificationCode).getSingleResult();
            confirmUser.setEmailVerificationCode(null);
            confirmUser.setUserStatusId(Globals.USER_STATUS_AWAITING_APPROVAL);
            return confirmUser;
        }
        catch(NoResultException ex)
        {
            return null;
        }
    }

    public void storeUserAuthentication(UserAuthentication userAuth) {
        em.persist(userAuth);
    }

    public void informAdminOfConfirmedEmail(VipsLogicUser confirmUser, ResourceBundle i18nBundle, String serverName ) {
        List<VipsLogicUser> organizationAdmins = this.findOrganizationUsersByRole(confirmUser.getOrganizationId(), Globals.ROLE_ORGANIZATION_ADMINISTRATOR);
        for(VipsLogicUser organizationAdmin: organizationAdmins)
        {
            String subject = i18nBundle.getString("informAdminOfConfirmedEmailSubject");
            String body = MessageFormat.format(i18nBundle.getString("informAdminOfConfirmedEmailBody"), 
                                confirmUser.getLastName(),
                                "http://" + serverName + "/user?action=viewUser&userId=" + confirmUser.getUserId());
            

            // Send email
            String smtpServer = System.getProperty("no.bioforsk.vips.logic.SMTP_SERVER");
            SimpleMailSender mailSender = new SimpleMailSender(smtpServer);
            mailSender.sendMail("noreply@" + serverName, organizationAdmin.getEmail(), subject, body);
        }
    }

    private List<VipsLogicUser> findOrganizationUsersByRole(Organization organizationId, Integer role) {
        Query q = em.createNativeQuery("SELECT * "
                + "FROM vips_logic_user vlu "
                + "WHERE vlu.user_id IN ("
                + "     SELECT user_id "
                + "     FROM user_vips_logic_role "
                + "     WHERE vips_logic_role_id = :role"
                + ")"
                + "AND vlu.organization_id=:organizationId", VipsLogicUser.class);
        q.setParameter("role", role);
        q.setParameter("organizationId", organizationId.getOrganizationId());
        return q.getResultList();
    }

    private void sendUserApprovalConfirmation(VipsLogicUser user, ResourceBundle i18nBundle, String serverName) 
    {
        String subject = i18nBundle.getString("sendUserApprovalConfirmationSubject");
        String body = MessageFormat.format(i18nBundle.getString("sendUserApprovalConfirmationBody"), 
                            "http://" + serverName + "/");
        // Send email
        String smtpServer = System.getProperty("no.bioforsk.vips.logic.SMTP_SERVER");
        SimpleMailSender mailSender = new SimpleMailSender(smtpServer);
        mailSender.sendMail("noreply@" + serverName, user.getEmail(), subject, body);
    }

    public void handleUserStatusChange(Integer oldUserStatusId, VipsLogicUser updatedUser, ResourceBundle i18nBundle, String serverName) {
        // Check if user status has changed from awaiting approval to approved. 
        // If that's the case, inform the user by email
        if(oldUserStatusId.equals(Globals.USER_STATUS_AWAITING_APPROVAL) && updatedUser.getUserStatusId().equals(Globals.USER_STATUS_APPROVED))
        {
            this.sendUserApprovalConfirmation(updatedUser, i18nBundle, serverName);
        }
    }

    public List<VipsLogicUser> getUsersByOrganization(Integer organizationId) {
        return em.createNamedQuery("VipsLogicUser.findByOrganizationId", VipsLogicUser.class)
                .setParameter("organizationId", em.find(Organization.class, organizationId))
                .getResultList();
    }
}
