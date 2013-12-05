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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import no.bioforsk.vips.logic.authenticate.PasswordValidationException;
import no.bioforsk.vips.logic.entity.Organization;
import no.bioforsk.vips.logic.entity.UserAuthentication;
import no.bioforsk.vips.logic.entity.UserAuthenticationType;
import no.bioforsk.vips.logic.entity.VipsLogicUser;
import no.bioforsk.vips.logic.i18n.SessionLocaleUtil;
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
            String salt = this.getVIPSLogicServerProperties().getProperty("MD5_SALT");
            q.setParameter("password", MD5Encrypter.getMD5HexString((String)loginInfo.get("password"),salt));
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
}
