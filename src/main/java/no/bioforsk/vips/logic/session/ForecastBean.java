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
    
    public List<ForecastResult> getForecastResults()
    {
        return em.createNamedQuery("ForecastResult.findAll").getResultList();
    }
    
    public List<ForecastResult> getForecastResults(Long forecastResultId)
    {
        Query q = em.createNamedQuery("ForecastResult.findByForecastResultId", ForecastResult.class);
        q.setParameter("forecastResultId", forecastResultId);
        return q.getResultList();
    }
    
    public List<ForecastConfiguration> getForecastConfigurationsForUser(Integer userId)
    {
        Query q = em.createNamedQuery("ForecastConfiguration.findByVipsCoreUserId");
        q.setParameter("vipsCoreUserId", userId);
        return q.getResultList();
    }
}
