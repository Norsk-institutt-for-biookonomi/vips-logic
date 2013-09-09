package no.bioforsk.vips.logic.session;

import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import no.bioforsk.vips.logic.entity.PointOfInterest;
import no.bioforsk.vips.logic.entity.PointOfInterestWeatherStation;
import no.bioforsk.vips.logic.util.Globals;


/**
 * Handles transactions for POIs
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
@LocalBean
@Stateless
public class PointOfInterestBean {
    @PersistenceContext(unitName="VIPSLogic-PU")
    EntityManager em;
    
    public List<PointOfInterest> getWeatherstations(String countryCode)
    {
        Query q = em.createNamedQuery("PointOfInterest.findByPointOfInterestTypeAndCountryCode");
        q.setParameter("pointOfInterestTypeId", Globals.POI_TYPE_WEATHERSTATION);
        q.setParameter("countryCode", countryCode);
        return q.getResultList();
    }
    
    /**
     * 
     * @param userId
     * @return all weather stations related to the user
     */
    public List<PointOfInterestWeatherStation> getWeatherstationsForUser(Integer userId)
    {
        Query q = em.createNamedQuery("PointOfInterestWeatherStation.findByUserId");
        q.setParameter("userId", userId);
        return q.getResultList();
    }
    
    public List<PointOfInterestWeatherStation> getAllWeatherStations()
    {
        Query q = em.createNamedQuery("PointOfInterestWeatherStation.findAll");
        return q.getResultList();
    }
    
    /**
     * 
     * @param pointOfInterestId
     * @return the specified point of interest. May be one of the subclasses, e.g. PointOfInterestWeatherstation
     */
    public PointOfInterest getPointOfInterest(Integer pointOfInterestId)
    {
        Query q = em.createNamedQuery("PointOfInterest.findByPointOfInterestId");
        q.setParameter("pointOfInterestId", pointOfInterestId);
        return (PointOfInterest) q.getSingleResult();
        
    }
}
