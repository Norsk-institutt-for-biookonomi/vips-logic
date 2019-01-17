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

package no.nibio.vips.logic.controller.session;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.icu.util.ULocale;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.TimeZone;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import no.nibio.vips.logic.authenticate.PasswordValidationException;
import no.nibio.vips.logic.entity.Country;
import no.nibio.vips.logic.entity.ForecastConfiguration;
import no.nibio.vips.logic.entity.MapLayer;
import no.nibio.vips.logic.entity.MessageLocale;
import no.nibio.vips.logic.entity.Observation;
import no.nibio.vips.logic.entity.Organization;
import no.nibio.vips.logic.entity.OrganizationGroup;
import no.nibio.vips.logic.entity.PointOfInterest;
import no.nibio.vips.logic.entity.PointOfInterestWeatherStation;
import no.nibio.vips.logic.entity.UserAuthentication;
import no.nibio.vips.logic.entity.UserAuthenticationPK;
import no.nibio.vips.logic.entity.UserAuthenticationType;
import no.nibio.vips.logic.entity.UserUuid;
import no.nibio.vips.logic.entity.UserUuidPK;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.entity.misc.UserResources;
import no.nibio.vips.logic.i18n.SessionLocaleUtil;
import no.nibio.vips.logic.util.Globals;
import no.nibio.vips.logic.util.SessionControllerGetter;
import no.nibio.vips.logic.util.SimpleMailSender;
import no.nibio.vips.logic.util.StringUtils;
import no.nibio.vips.util.MD5Encrypter;
import no.nibio.web.forms.FormValidationException;
import org.passay.CharacterCharacteristicsRule;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.LengthRule;
import org.passay.MessageResolver;
import org.passay.PasswordData;
import org.passay.PasswordGenerator;
import org.passay.PasswordValidator;
import org.passay.PropertiesMessageResolver;
import org.passay.Rule;
import org.passay.RuleResult;
import org.passay.WhitespaceRule;

/**
 * Handles user stuff, credentials
 * @copyright 2013 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
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
    
    public VipsLogicUser storeUserFirstTime(VipsLogicUser user,UserAuthentication auth) throws FormValidationException
    {
        try
        {
            em.persist(user);
            auth.setVipsLogicUser(user);
            UserAuthenticationPK pk = new UserAuthenticationPK(user.getUserId(), auth.getUserAuthenticationType().getUserAuthenticationTypeId());
            auth.setUserAuthenticationPK(pk);
            em.persist(auth);
            return user;
        }
        catch(ConstraintViolationException ex)
        {
            
            String errors = "";
            for(ConstraintViolation cv: ex.getConstraintViolations())
            {
                errors += cv.getMessage() + "\n";
            }
            throw new FormValidationException("Constraint error(s): \n" + errors);
        }
    }
    
    public void sendUserEmailVerification(VipsLogicUser user, ResourceBundle i18nBundle, String serverName)
    {
        // Create verification string (random, alphanumeric, 30 characters)
        user.setEmailVerificationCode(StringUtils.generateRandomAlphanumericString(30));
        em.merge(user);
        String subject = i18nBundle.getString("userRegistrationEmailVerificationMessageSubject");
        String body = getUserEmailVerificationBody(i18nBundle, serverName, user.getEmailVerificationCode() );
        
       
        // Send email
        String smtpServer = System.getProperty("no.nibio.vips.logic.SMTP_SERVER");
        SimpleMailSender mailSender = new SimpleMailSender(smtpServer);
        mailSender.sendMail("noreply@" + serverName, user.getEmail(), subject, body);
        
    }
    
    public String getUserEmailVerificationBody(ResourceBundle i18nBundle, String serverName, String verificationCode)
    {
        String verificationURL = Globals.PROTOCOL + "://" + serverName + "/user?action=confirmEmail&verificationCode=" + verificationCode;
        String body = MessageFormat.format(i18nBundle.getString("userRegistrationEmailVerificationMessageBody"), serverName,verificationURL);
        return body;
    }
    
    public void storeUser(VipsLogicUser user)
    {
        em.merge(user);
    }
    
    /**
     * 
     * @param user 
     */
    public void deleteUser(VipsLogicUser user)  throws DeleteUserException
    {
        // Must double check: NO user can be force deleted without first
        // transferring resources to another user
        UserResources userResources = SessionControllerGetter.getUserBean().getUserResources(user);
        if(! userResources.isEmpty())
        {
            throw new DeleteUserException("User still has resources connected to them.");
        }
        user = em.find(VipsLogicUser.class, user.getUserId());
        // If it's an archive user, do NOT delete it!
        if(user.getOrganizationId().getArchiveUser().getUserId().equals(user.getUserId()))
        {
            throw new DeleteUserException("User is an archive user for organization " + user.getOrganizationId().getOrganizationName() + ". Can't delete it");
        }
        // Remove all notification subscriptions
        SessionControllerGetter.getMessagingBean().deleteAllNotificationSubscriptions(user);
        // Remove all User UUIDs
        this.deleteAllUserUuidsForUser(user);
        
        // Delete all of the user's private forecast configurations and results
        SessionControllerGetter.getForecastBean().deleteAllPrivateForecastConfigurationsForUser(user);
        
        em.remove(user);
    }
    
    public UserResources getUserResources(VipsLogicUser user)
    {
        UserResources retVal = new UserResources();
        retVal.setPois(SessionControllerGetter.getPointOfInterestBean().getPoisForUser(user));
        retVal.setMessageLocales(SessionControllerGetter.getMessageBean().getMessageLocaleList(user));
        retVal.setForecastConfigurations(SessionControllerGetter.getForecastBean().getForecastConfigurationsForUser(user.getUserId()));
        List<Observation> observations = SessionControllerGetter.getObservationBean().getObservationsForUser(user);
        observations.addAll(SessionControllerGetter.getObservationBean().getObservationsLastEditedByUser(user));
        observations.addAll(SessionControllerGetter.getObservationBean().getObservationsStatusChangedByUser(user));
        retVal.setObservations(observations);
        return retVal;
    }
    
    /**
     * Transfers all user resources (may need to update this method to keep up with the system)
     * <ul>
     *  <li>Weather Stations</li>
     *  <li>Messages</li>
     *  <li>Forecast Configurations</li>
     *  <li>Observations</li>
     * </ul>
     * @param fromUser
     * @param toUser 
     */
    public void transferUserResources(VipsLogicUser fromUser, VipsLogicUser toUser) {
        UserResources userResources = this.getUserResources(fromUser);
        (userResources.getPois() != null ? userResources.getPois() : Collections.<PointOfInterest>emptyList()).forEach((ws) -> {
            ws.setUserId(toUser);
        });
        (userResources.getMessageLocales() != null ? userResources.getMessageLocales() : Collections.<MessageLocale>emptyList()).forEach((ml) -> {
            ml.setCreatedBy(toUser);
        });
        (userResources.getForecastConfigurations() != null ? userResources.getForecastConfigurations() : Collections.<ForecastConfiguration>emptyList()).forEach((fc) -> {
            fc.setVipsCoreUserId(toUser);
        });
        //HMMMMMMM - noe med transaksjoner som ikke er oppdatert elns????
        (userResources.getObservations() != null ? userResources.getObservations() : Collections.<Observation>emptyList()).forEach((obs) -> {
            if(obs.getUserId().equals(fromUser.getUserId()))
            {
                obs.setUserId(toUser.getUserId());
                obs.setUser(toUser);
            }
            if(obs.getLastEditedBy() != null && obs.getLastEditedBy().equals(fromUser.getUserId()))
            {
                obs.setLastEditedBy(toUser.getUserId());
                obs.setLastEditedByUser(toUser);
            }
            if(obs.getStatusChangedByUserId() != null && obs.getStatusChangedByUserId().equals(fromUser.getUserId()))
            {
                obs.setStatusChangedByUserId(toUser.getUserId());
            }
        });
        
    }
    
    /**
     * 
     * @param email
     * @return The user, or null if no user with given email is registered
     */
    public VipsLogicUser getUserByEmail(String email) throws NonUniqueResultException
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
        return MD5Encrypter.getMD5HexString(str,System.getProperty("no.nibio.vips.logic.MD5_SALT"));   
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
    
    public boolean isPasswordValid(String password, ULocale errorMessageLocale) throws PasswordValidationException
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
            props.load(this.getClass().getResourceAsStream("/no/nibio/vips/logic/i18n/passay" + propertiesFileSuffix + ".properties"));
            MessageResolver resolver = new PropertiesMessageResolver(props);
            validator = new PasswordValidator(resolver, this.getDefaultPasswordValidatorRuleList());
        }
        // Something went wrong when attempting to get localized error messages. Skip that.
        catch(IOException | NullPointerException ex)
        {
            validator = new PasswordValidator(this.getDefaultPasswordValidatorRuleList());
        }

        PasswordData passwordData = new PasswordData(password);
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
     * 
     * @return a password that should meet all the validation demands
     */
    public String generateValidPassword()
    {
        PasswordGenerator generator = new PasswordGenerator();
        return generator.generatePassword(10, this.getDefaultCharacterRules());
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
        charRule.setRules(this.getDefaultCharacterRules());
        // require all the rules be met
        charRule.setNumberOfCharacteristics(charRule.getRules().size());
        
        // group all rules together in a List
        List<Rule> ruleList = new ArrayList<>();
        ruleList.add(lengthRule);
        ruleList.add(whitespaceRule);
        ruleList.add(charRule);

        return ruleList;
    }
    
    /**
     * 
     * @return 
     */
    private List<CharacterRule> getDefaultCharacterRules()
    {
        List<CharacterRule> defaultCharacterRules = new ArrayList<>();
        // require at least 1 digit in passwords
        defaultCharacterRules.add(new CharacterRule(EnglishCharacterData.Digit,1));
        // require at least 1 upper case char
        defaultCharacterRules.add(new CharacterRule(EnglishCharacterData.UpperCase, 1));
        // require at least 1 lower case char
        defaultCharacterRules.add(new CharacterRule(EnglishCharacterData.LowerCase, 1));
        return defaultCharacterRules;
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
     * 
     * @return only the organizations without parent
     */
    public List<Organization> getTopLevelOrganizations()
    {
        return em.createNamedQuery("Organization.findAllTopLevelOrganizations").getResultList();
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
     * @param emailVerificationCode
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
        em.merge(userAuth);
    }

    /**
     * Informing all organization admins about the new registered user and the
     * application that needs approval
     * @param confirmUser
     * @param i18nBundle
     * @param serverName 
     */
    public void informAdminOfConfirmedEmail(VipsLogicUser confirmUser, ResourceBundle i18nBundle, String serverName ) {
        List<VipsLogicUser> organizationAdmins = this.findOrganizationUsersByRole(confirmUser.getOrganizationId(), Globals.ROLE_ORGANIZATION_ADMINISTRATOR);
        for(VipsLogicUser organizationAdmin: organizationAdmins)
        {
            String subject = i18nBundle.getString("informAdminOfConfirmedEmailSubject");
            String body = MessageFormat.format(i18nBundle.getString("informAdminOfConfirmedEmailBody"), 
                                confirmUser.getLastName(),
                                confirmUser.getApprovalApplication(),
                                Globals.PROTOCOL + "://" + serverName + "/user?action=viewUser&userId=" + confirmUser.getUserId(),
                                Globals.PROTOCOL + "://" + serverName + "/user?action=approveUser&userId=" + confirmUser.getUserId()
                            );
            
            //System.out.println(body);
            // Send email
            String smtpServer = System.getProperty("no.nibio.vips.logic.SMTP_SERVER");
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
                            Globals.PROTOCOL + "://" + serverName + "/");
        // Send email
        String smtpServer = System.getProperty("no.nibio.vips.logic.SMTP_SERVER");
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

    public void sendUsernameAndPasswordToUser(VipsLogicUser viewUser, String username, String password, ResourceBundle i18nBundle, String serverName) {

        String subject = i18nBundle.getString("sendUsernameAndPasswordToUserSubject");
        String body = MessageFormat.format(i18nBundle.getString("sendUsernameAndPasswordToUserBody"), username, password, serverName);
        
        // Send email
        String smtpServer = System.getProperty("no.nibio.vips.logic.SMTP_SERVER");
        SimpleMailSender mailSender = new SimpleMailSender(smtpServer);
        mailSender.sendMail("noreply@" + serverName, viewUser.getEmail(), subject, body);
    }
    
    public String getUserName(VipsLogicUser user, Integer userAuthenticationTypeId)
    {
        for(UserAuthentication auth: user.getUserAuthenticationSet())
        {
            if(Objects.equals(auth.getUserAuthenticationType().getUserAuthenticationTypeId(), userAuthenticationTypeId))
            {
                return auth.getUsername();
            }
        }
        return null;
    }

    /**
     * 
     * @param resetPasswordUser
     * @param i18nBundle
     * @param serverName
     * @return NULL or empty string if all is OK, errorMessage if not
     */
    public String createPasswordResetCodeAndSendToUser(VipsLogicUser resetPasswordUser, ResourceBundle i18nBundle, String serverName) {
        resetPasswordUser = em.merge(resetPasswordUser);
        String resetCode = StringUtils.generateRandomAlphanumericString(30);
        UserAuthentication auth = resetPasswordUser.getPasswordAuthentication();
        if(auth == null)
        {
            return i18nBundle.getString("missingUsernameAndPassword");
        }
        
        auth.setPasswordResetCode(resetCode);
        auth.setPasswordResetCodeCreationTime(new Date());
        
        String subject = i18nBundle.getString("createPasswordResetCodeAndSendToUserSubject");
        String body = MessageFormat.format(i18nBundle.getString("createPasswordResetCodeAndSendToUserBody"), 
                this.getUserName(resetPasswordUser, UserAuthenticationType.TYPE_PASSWORD),
                Globals.PROTOCOL + "://" + serverName + "/user?action=resetPassword&passwordResetCode=" + resetCode);
        
        // Send email
        String smtpServer = System.getProperty("no.nibio.vips.logic.SMTP_SERVER");
        SimpleMailSender mailSender = new SimpleMailSender(smtpServer);
        mailSender.sendMail("noreply@" + serverName, resetPasswordUser.getEmail(), subject, body);
        return null;
    }

    /**
     * 
     * @param uuid
     * @return 
     */
    public VipsLogicUser findVipsLogicUser(UUID uuid)
    {
        try
        {
            Query q = em.createNamedQuery("UserUuid.findByUserUUID", UserUuid.class);
            q.setParameter("userUUID", uuid);
            UserUuid uUuid = (UserUuid) q.getSingleResult();
            // If the uuid credential has expired, delete it and return nothing(null)
            if(uUuid.getExpiresAt().before(new Date()))
            {
                this.deleteUserUuid(uuid);
                return null;
            }
            q = em.createNamedQuery("VipsLogicUser.findByUserId", VipsLogicUser.class);
            q.setParameter("userId", uUuid.getUserUuidPK().getUserId());
            VipsLogicUser user = (VipsLogicUser) q.getSingleResult(); 
            user.setUserUuid(uuid);
            return user;
        }
        catch(NoResultException ex)
        {
            return null;
        }
    }
    
    /**
     * 
     * @param user
     * @return 
     */
    public UserUuid createAndPersistUserUuid(VipsLogicUser user)
    {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(user.getOrganizationId().getDefaultTimeZone()));
        cal.setTime(new Date());
        cal.add(Calendar.DATE, Globals.DEFAULT_UUID_VALIDITY_DURATION_DAYS);
        UserUuid u = new UserUuid();
        u.setExpiresAt(cal.getTime());
        UserUuidPK uPK = new UserUuidPK(UUID.randomUUID(), user.getUserId());
        u.setUserUuidPK(uPK);
        em.persist(u);
        return u;
    }
    
    /**
     * 
     * @param uuid 
     */
    public void deleteUserUuid(UUID uuid)
    {
        try
        {
            UserUuid u = (UserUuid) em.createNamedQuery("UserUuid.findByUserUUID").setParameter("userUUID", uuid).getSingleResult();
            em.remove(u);
        }
        catch(NoResultException ex) {}
    }
    
    /**
     * Cleanup method. Should be run daily as a batch job
     */
    public void deleteAllExpiredUserUuids()
    {
        em.createNativeQuery("DELETE FROM public.user_uuid WHERE expires_at < now()").executeUpdate();
    }
    
    public void deleteAllUserUuidsForUser(VipsLogicUser user){
        em.createNativeQuery("DELETE FROM public.user_uuid WHERE user_id=:userId").setParameter("userId", user.getUserId()).executeUpdate();
    }
    
    public String getMapLayerJSONForUser(VipsLogicUser user)
    {
        try {
            return new ObjectMapper().writeValueAsString(this.getMapLayersForUser(user));
        } catch (JsonProcessingException ex) {
            Logger.getLogger(UserBean.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        }
    }
    
    public List<MapLayer> getMapLayersForUser(VipsLogicUser user)
    {
        List<MapLayer> retVal;
        if(user.isSuperUser())
        {
            retVal = em.createNamedQuery("MapLayer.findAll", MapLayer.class).getResultList();
        }
        else
        {
            retVal = this.getMapLayersForOrganization(user.getOrganizationId());
        }
        return retVal;
    }
    
    public List<MapLayer> getMapLayersForOrganization(Organization organization)
    {
        Query q = em.createNativeQuery("SELECT * FROM public.map_layer WHERE map_layer_id IN ("
                + "     SELECT map_layer_id FROM organization_map_layer WHERE organization_id=:organizationId "
                + ")",
                MapLayer.class
        );
        q.setParameter("organizationId", organization.getOrganizationId());
        return q.getResultList();
    }

    public VipsLogicUser getUserByPhoneNumber(String sourceaddr) {
        try
        {
            return em.createNamedQuery("VipsLogicUser.findByCompletePhoneNumber", VipsLogicUser.class)
                .setParameter("completePhoneNumber", sourceaddr)
                .getSingleResult();
        }
        catch(NoResultException ex)
        {
            return null;
        }
    }
    
    public List<Country> getUserCountries()
    {
        try
        {
            return em.createNamedQuery("Country.findByCountryCodes")
                    .setParameter("countryCodes",Arrays.asList(System.getProperty("no.nibio.vips.logic.USER_COUNTRY_CODES").split(",")))
                    .getResultList();
        }
        catch(NullPointerException ex)
        {
            return new ArrayList<>();
        }
    }
    
    public List<Country> getCountries()
    {
        return em.createNamedQuery("Country.findAll").getResultList();
    }

    public List<OrganizationGroup> getOrganizationGroups(Organization organization) {
        try
        {
            return em.createNamedQuery("OrganizationGroup.findByOrganizationId")
                    .setParameter("organizationId", organization.getOrganizationId())
                    .getResultList();
        }
        catch(NoResultException ex)
        {
            return new ArrayList<>();
        }
    }
    
    public List<OrganizationGroup> getOrganizationGroups(VipsLogicUser user) {
        try
        {
            return em.createNativeQuery(
                    "SELECT * FROM public.organization_group WHERE organization_group_id IN ("
                    + " SELECT organization_group_id FROM organization_group_user WHERE user_id = :userId)",OrganizationGroup.class)
                    .setParameter("userId", user.getUserId())
                    .getResultList();
        }
        catch(NoResultException ex)
        {
            return new ArrayList<>();
        }
    }

    public OrganizationGroup getOrganizationGroup(Integer organizationGroupId) {
        return em.find(OrganizationGroup.class, organizationGroupId);
    }

    public List<Integer> getOrganizationGroupMemberIds(OrganizationGroup oGroup) {
        if(oGroup.getOrganizationGroupId() != null)
        {
            return em.createNativeQuery("SELECT user_id FROM public.organization_group_user "
                    + "WHERE organization_group_id = :organizationGroupId")
                    .setParameter("organizationGroupId", oGroup.getOrganizationGroupId())
                    .getResultList();
        }
        else
        {
            return new ArrayList<>();
        }
    }

    public OrganizationGroup storeOrganizationGroup(OrganizationGroup oGroup, String[] userIdsFromForm) {
        if(oGroup.getOrganizationGroupId() != null && oGroup.getOrganizationGroupId() > 0)
        {
            oGroup = em.merge(oGroup);
        }
        else
        {
            em.persist(oGroup);
            
        }
        // Delete all former and insert all current members
        em.createNativeQuery("DELETE FROM public.organization_group_user WHERE organization_group_id = :organizationGroupId")
                .setParameter("organizationGroupId", oGroup.getOrganizationGroupId())
                .executeUpdate();
        
        Query q = em.createNativeQuery(
                    "INSERT INTO public.organization_group_user(organization_group_id,user_id) "
                    + "VALUES(:organizationGroupId, :userId)"
                )
                .setParameter("organizationGroupId", oGroup.getOrganizationGroupId());
        
        if(userIdsFromForm != null)
        {
            for(String userIdStr : userIdsFromForm)
            {
                q.setParameter("userId", Integer.valueOf(userIdStr));
                q.executeUpdate();
            }
        }
        return oGroup;
    }

    /**
     * Removes group from VIPS. TODO: Take all relationships into account. E.g. places
     * @param oGroup 
     */
    public void deleteOrganizationGroup(OrganizationGroup oGroup) {
        // Delete all memberships
        em.createNativeQuery("DELETE FROM public.organization_group_user WHERE organization_group_id = :organizationGroupId")
                .setParameter("organizationGroupId", oGroup.getOrganizationGroupId())
                .executeUpdate();
        
        em.remove(em.find(OrganizationGroup.class, oGroup.getOrganizationGroupId()));
        
    }

    public VipsLogicUser getVipsLogicUser(Integer userId) {
        return em.find(VipsLogicUser.class, userId);
    }

    public List<VipsLogicUser> getUsers(Set<Integer> userIds) {
        return em.createNamedQuery("VipsLogicUser.findByUserIds")
                .setParameter("userIds", userIds)
                .getResultList();
    }
    
    public List<Organization> getOrganizationsWithActiveForecastConfigurations(Date theDate)
    {
        try
        {
        return em.createNativeQuery("SELECT * FROM public.organization WHERE organization_id IN("
                + "SELECT DISTINCT organization_id FROM public.vips_logic_user WHERE user_id IN ("
                    + "SELECT DISTINCT vips_logic_user_id FROM public.forecast_configuration "
                    + "WHERE date_start <= :currentDate AND date_end >= :currentDate"
                    + ")"
                + ")", Organization.class)
                .setParameter("currentDate", theDate)
                .getResultList();
        }
        catch(NoResultException ex)
        {
            return null;
        }
    }

    public List<OrganizationGroup> getOrganizationGroups() {
        return em.createNamedQuery("OrganizationGroup.findAll").getResultList();
    }

    public Country getCountry(String webValue) {
        return em.find(Country.class, webValue);
    }

    public Organization storeOrganization(Organization organization) {
        return em.merge(organization);
    }
}
