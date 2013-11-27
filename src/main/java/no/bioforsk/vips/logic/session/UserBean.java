package no.bioforsk.vips.logic.session;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import no.bioforsk.vips.logic.entity.UserAuthentication;
import no.bioforsk.vips.logic.entity.UserAuthenticationType;
import no.bioforsk.vips.logic.entity.VipsLogicUser;
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
}
