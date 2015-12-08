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

import com.vividsolutions.jts.geom.Geometry;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import no.nibio.vips.logic.entity.Gis;
import no.nibio.vips.logic.entity.Observation;
import no.nibio.vips.logic.entity.Organization;
import no.nibio.vips.logic.entity.VipsLogicUser;

/**
 * @copyright 2014-2015 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Stateless
public class ObservationBean {
    @PersistenceContext(unitName="VIPSLogic-PU")
    EntityManager em;
    
    public List<Observation> getObservations(Integer organizationId)
    {
        Organization organization= em.find(Organization.class, organizationId);
        List<VipsLogicUser> users = em.createNamedQuery("VipsLogicUser.findByOrganizationId", VipsLogicUser.class)
                                        .setParameter("organizationId", organization)
                                        .getResultList();
        
        List<Observation> retVal = this.getObservationsWithGeoInfo(em.createNamedQuery("Observation.findAll").getResultList());
        
        return retVal;
    }
    
    public Observation getObservation(Integer observationId)
    {
        Observation retVal = em.find(Observation.class, observationId);
        if(retVal != null)
        {
            retVal.setGeoinfos(this.getGeoinfoForObservation(retVal));
        }
        return retVal;
    }
    
    public List<Gis> getGeoinfoForObservation(Observation obs)
    {
        List<Integer> gisIds = em.createNativeQuery("SELECT gis_id FROM public.gis_observation WHERE observation_id = :observationId")
                .setParameter("observationId", obs.getObservationId())
                .getResultList();
        
        if(gisIds != null && ! gisIds.isEmpty())
        {
            return em.createNamedQuery("Gis.findByGisIds",Gis.class)
                    .setParameter("gisIds", gisIds)
                    .getResultList();
        }
        else
        {
            return null;
        }
    }
    
    public List<Observation> getObservationsWithGeoInfo(List<Observation> observations)
    {
        Query q1 = em.createNamedQuery("Gis.findByGisIds",Gis.class);
        
        Query q2 = em.createNativeQuery("SELECT gis_id FROM public.gis_observation WHERE observation_id = :observationId");
        
        for(Observation obs:observations)
        {
            List<Integer> gisIds = q2.setParameter("observationId", obs.getObservationId()).getResultList();
            if(!gisIds.isEmpty())
            {
                obs.setGeoinfos(q1.setParameter("gisIds", gisIds).getResultList());
            }
            //
        }
        return observations;
    }

    public Observation storeObservation(Observation observation) {
        Observation retVal = em.merge(observation);
        // Store geoinfo
        // First delete all old observations
        em.createNativeQuery("DELETE FROM public.gis where gis_id IN (SELECT gis_id FROM public.gis_observation WHERE observation_id=:observationId)")
                .setParameter("observationId", observation.getObservationId())
                .executeUpdate();
        em.createNativeQuery("DELETE FROM public.gis_observation WHERE observation_id=:observationId")
                .setParameter("observationId", observation.getObservationId())
                .executeUpdate();
        // Then persist the new ones
        if(observation.getGeoinfos() != null && ! observation.getGeoinfos().isEmpty())
        {
            for(Gis gis : observation.getGeoinfos())
            {
                em.persist(gis);
            }
            
            Query q = em.createNativeQuery("INSERT INTO public.gis_observation(gis_id,observation_id) VALUES(:gisId,:observationId)")
                .setParameter("observationId", observation.getObservationId());
            for(Gis gis:observation.getGeoinfos())
            {
                q.setParameter("gisId", gis.getGisId())
                        .executeUpdate();
            }
        }
        /*
        for(Geometry geom:observation.getGeometries())
        {
            Gis gis = new Gis();
            gis.setGisGeom(geom);
            em.persist(gis);
            gises.add(gis);
        }*/
        
        return retVal;
    }

    public void deleteObservation(Integer observationId) {
        Observation observation = em.find(Observation.class, observationId);
        em.remove(observation);
    }
}
