/*
 * Copyright (c) 2020 NIBIO <http://www.nibio.no/>. 
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

package no.nibio.vips.logic.modules.barkbeetle;

import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import no.nibio.vips.logic.util.SessionControllerGetter;
import org.locationtech.jts.geom.Point;

/**
 * @copyright 2020 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@LocalBean
@Stateless
public class BarkbeetleBean {
    @PersistenceContext(unitName="VIPSLogic-PU")
    EntityManager em;
    
    /**
     * Get the list of trapsites for the given season
     * @param season
     * @return 
     */
    public List<SeasonTrapsite> getSeasonTrapsites(Integer season){
        return em.createNamedQuery("SeasonTrapsite.findBySeason")
                .setParameter("season", season)
                .getResultList();
    }
    
    /**
     * Get a specific trapsite
     * @param seasonTrapsiteId
     * @return 
     */
    public SeasonTrapsite getSeasonTrapsite(Integer seasonTrapsiteId)
    {
        return em.find(SeasonTrapsite.class, seasonTrapsiteId);
    }
    
    public SeasonTrapsite storeSeasonTrapsite(SeasonTrapsite seasonTrapsite)
    {
        if(seasonTrapsite.getSeasonTrapsiteId() == null)
        {
            em.persist(seasonTrapsite);
            return seasonTrapsite;
        }
        else
        {
            return em.merge(seasonTrapsite);
        }
    }
    
    public List<TrapsiteType> getTrapsiteTypes()
    {
        return em.createNamedQuery("TrapsiteType.findAll").getResultList();
    }
    
    public TrapsiteType getTrapsiteType(Integer trapsiteTypeId)
    {
        return em.find(TrapsiteType.class, trapsiteTypeId);
    }
    
    /**
     * Util method for getting this bean through JNDI
     * @return 
     */
    public static BarkbeetleBean getInstance()
    {
        try
        {
            InitialContext ic = new InitialContext();
            BarkbeetleBean retVal = (BarkbeetleBean) ic.lookup(SessionControllerGetter.getJndiPath(BarkbeetleBean.class));

            return retVal;
        }catch(NamingException ne)
        {
            System.out.println("Could not find " + BarkbeetleBean.class.getSimpleName());
            return null;
        }
    }

    public List<TrapsiteRegistration> getRegistrationsForSite(SeasonTrapsite trapsite) {
        return em.createNamedQuery("TrapsiteRegistration.findBySeasonTrapsiteId")
                .setParameter("seasonTrapsiteId", trapsite.getSeasonTrapsiteId())
                .getResultList();
    }

    public List<RegistrationStatusType> getRegistrationStatusTypes() {
        return em.createNamedQuery("RegistrationStatusType.findAll").getResultList();
    }

    public SeasonTrapsite storeTrapsiteRegistrations(Integer seasonTrapsiteId, List<TrapsiteRegistration> registrations) {
        SeasonTrapsite trapsite = em.find(SeasonTrapsite.class, seasonTrapsiteId);
        trapsite.setTrapsiteRegistrationCollection(registrations);
        return em.merge(trapsite);
    }
}
