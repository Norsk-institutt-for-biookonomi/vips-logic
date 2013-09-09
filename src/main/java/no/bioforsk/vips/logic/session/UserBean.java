package no.bioforsk.vips.logic.session;

import java.util.List;
import java.util.Map;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import no.bioforsk.vips.logic.entity.UserVipslogicRole;
import no.bioforsk.vips.logic.util.Globals;

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
    
    /**
     * 
     * @param userId
     * @return list of roles for this user
     */
    public List<UserVipslogicRole> getUserVipslogicRole(Integer userId)
    {
        Query q = em.createNamedQuery("UserVipslogicRole.findByUserId", UserVipslogicRole.class);
        q.setParameter("userId", userId);
        return q.getResultList();
    }
    
    public boolean isSuperUser(Map userInfo)
    {
        List<UserVipslogicRole> roles = (List<UserVipslogicRole>) userInfo.get("roles");
        for(UserVipslogicRole role : roles)
        {
            if(role.getVipslogicRole().getVipslogicRoleId().equals(Globals.ROLE_SUPERUSER))
                return true;
        }
        return false;
    }
}
