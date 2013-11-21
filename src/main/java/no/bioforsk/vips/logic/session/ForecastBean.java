package no.bioforsk.vips.logic.session;

import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import no.bioforsk.vips.logic.entity.ForecastResult;
import no.bioforsk.vips.logic.scheduling.model.ForecastConfiguration;

/**
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
@Stateless
public class ForecastBean {

    @PersistenceContext(unitName="VIPSLogic-PU")
    EntityManager em;
    
    /**
     * Returns all forecast results. TODO: put out of use!
     * @return 
     */
    public List<ForecastResult> getForecastResults()
    {
        return em.createNamedQuery("ForecastResult.findAll").getResultList();
    }
    
    public List<ForecastResult> getForecastResults(Long forecastConfigurationId)
    {
        ForecastConfiguration config = this.getForecastConfiguration(forecastConfigurationId);
        Query q = em.createNamedQuery("ForecastResult.findByForecastConfigurationId");
        q.setParameter("forecastConfigurationId", config);
        return q.getResultList();
        
    }
    
    
    
    /**
     * Get all forecast configurations for one user. 
     * TODO: Should be season based, or possibly based on start/stop date
     * @param userId
     * @return 
     */
    public List<ForecastConfiguration> getForecastConfigurationsForUser(Integer userId)
    {
        Query q = em.createNamedQuery("ForecastConfiguration.findByVipsCoreUserId");
        q.setParameter("vipsCoreUserId", userId);
        return q.getResultList();
    }
    
    /**
     * Fetches one specific forecast configuration
     * @param forecastConfigurationId
     * @return 
     */
    public ForecastConfiguration getForecastConfiguration(Long forecastConfigurationId)
    {
        return em.find(ForecastConfiguration.class, forecastConfigurationId);
    }
}
