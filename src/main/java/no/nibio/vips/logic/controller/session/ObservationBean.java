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
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import no.nibio.vips.logic.entity.Gis;
import no.nibio.vips.logic.entity.Message;
import no.nibio.vips.logic.entity.MessageIllustration;
import no.nibio.vips.logic.entity.MessageIllustrationPK;
import no.nibio.vips.logic.entity.Observation;
import no.nibio.vips.logic.entity.ObservationIllustration;
import no.nibio.vips.logic.entity.ObservationIllustrationPK;
import no.nibio.vips.logic.entity.Organization;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.observationdata.ObservationDataSchema;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;

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
    
    public List<Observation> getObservations(Integer organizationId, Integer statusTypeId)
    {
        Organization organization= em.find(Organization.class, organizationId);
        /*List<VipsLogicUser> users = em.createNamedQuery("VipsLogicUser.findByOrganizationId", VipsLogicUser.class)
                                        .setParameter("organizationId", organization)
                                        .getResultList();*/
        
        List<Observation> retVal = this.getObservationsWithGeoInfo(em.createNamedQuery("Observation.findByOrganizationIdAndStatusTypeId")
                .setParameter("organizationId", organization)
                .setParameter("statusTypeId", statusTypeId)
                .getResultList());
        
        return retVal;
    }
    
    public Observation getObservation(Integer observationId)
    {
        Observation retVal = em.find(Observation.class, observationId);
        if(retVal != null)
        {
            retVal.setGeoinfos(this.getGeoinfoForObservation(retVal));
            //ObservationDataSchema schema = 
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

    /**
     * 
     * @param observation
     * @return The merged object
     */
    public Observation storeObservation(Observation observation) {
        Observation retVal = em.merge(observation);
        // Store geoinfo
        // First delete all old observations

        em.createNativeQuery("DELETE FROM public.gis where gis_id IN (SELECT gis_id FROM public.gis_observation WHERE observation_id=:observationId)")
                .setParameter("observationId", retVal.getObservationId())
                .executeUpdate();
        em.createNativeQuery("DELETE FROM public.gis_observation WHERE observation_id=:observationId")
                .setParameter("observationId", retVal.getObservationId())
                .executeUpdate();
        // Then persist the new ones
        if(observation.getGeoinfos() != null && ! observation.getGeoinfos().isEmpty())
        {
            for(Gis gis : observation.getGeoinfos())
            {
                em.persist(gis);
            }
            
            Query q = em.createNativeQuery("INSERT INTO public.gis_observation(gis_id,observation_id) VALUES(:gisId,:observationId)")
                .setParameter("observationId", retVal.getObservationId());
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

    public Object getBroadcastObservations(Integer organizationId) {
        Organization organization= em.find(Organization.class, organizationId);
        /*List<VipsLogicUser> users = em.createNamedQuery("VipsLogicUser.findByOrganizationId", VipsLogicUser.class)
                                        .setParameter("organizationId", organization)
                                        .getResultList();*/
        
        List<Observation> retVal = this.getObservationsWithGeoInfo(em.createNamedQuery("Observation.findByOrganizationIdAndStatusTypeIdAndBroadcastMessage")
                .setParameter("organizationId", organization)
                .setParameter("statusTypeId", Observation.STATUS_TYPE_ID_APPROVED)
                .getResultList());
        
        return retVal;
    }
    /**
     * 
     * @param message
     * @return [OBSERVATION_ILLUSTRATION_PATH]/[ORGANIZATION_ID]/
     */
    private String getFilePath(Observation observation)
    {
        return System.getProperty("no.nibio.vips.logic.OBSERVATION_ILLUSTRATION_PATH") + "/" 
                        + observation.getOrganismId();
    }

    public Observation storeObservationIllustration(Observation observation, FileItem item) throws Exception {
        // Create a candidate filename
        // [MESSAGE_ILLUSTRATION_PATH]/[ORGANIZATION_ID]/[MESSAGE_ID]_illustration.[fileExtension]
        String filePath = this.getFilePath(observation);
        String fileName = observation.getObservationId() + "_illustration." + FilenameUtils.getExtension(item.getName());
        // Check availability, and adapt filename until available
        Integer fileNameSuffix = 1;
        File illustration = new File(filePath + "/" + fileName);
        while(illustration.exists())
        {
            fileName = observation.getObservationId() + "_illustration_" + fileNameSuffix + "." + FilenameUtils.getExtension(item.getName());
            illustration = new File(filePath + "/" + fileName);
            fileNameSuffix++;
        }
        File testDirectoryfile = new File(filePath);
        // If directory does not exist, create it
        if(!testDirectoryfile.exists())
        {
            testDirectoryfile.mkdirs();
        }

        // Store file
        item.write(illustration);
        
        // Update MessageIllustrations
        observation = em.merge(observation);
        // Remove the old illustration(s)
        List <ObservationIllustration> formerIllustrations = em.createNamedQuery("ObservationIllustration.findByObservationId").setParameter("observationId", observation.getObservationId()).getResultList();
        for(ObservationIllustration formerIllustration:formerIllustrations)
        {
            System.out.println("removing " + formerIllustration.toString());
            em.remove(formerIllustration);
        }
        // Also remove their relation to message.
        if(observation.getObservationIllustrationSet() != null)
        {
            observation.getObservationIllustrationSet().clear();
        }
        
        ObservationIllustration newIllustration = new ObservationIllustration(new ObservationIllustrationPK(observation.getObservationId(), fileName));
        em.persist(newIllustration);
        
        // Add the new illustration 
        if(observation.getObservationIllustrationSet() == null)
        {
            observation.setObservationIllustrationSet(new HashSet<ObservationIllustration>());
        }
        observation.getObservationIllustrationSet().add(newIllustration);
        //message.getMessageIllustrationSet().add(newIllustration);
        return observation;
    }

    public Observation deleteObservationIllustration(Observation observation) {
        observation = em.merge(observation);
        
        Set <ObservationIllustration> formerIllustrations = observation.getObservationIllustrationSet();
        for(ObservationIllustration formerIllustration:formerIllustrations)
        {
            em.remove(formerIllustration);
        }
        observation.getObservationIllustrationSet().clear();
        return observation;
    }

    /**
     * Fetch observations of a particular organism at a particular place and period
     * @param organismId
     * @param pointOfInterestId
     * @param startDateGrowth
     * @param endDateCalculation
     * @return 
     */
    public List<no.nibio.vips.observation.Observation> getObservations(Integer organismId, Integer pointOfInterestId, Date startDateGrowth, Date endDateCalculation) {
        return em.createNativeQuery(
                "SELECT * FROM public.observation "
                + "WHERE organism_id = :organismId "
                + "AND location_point_of_interest_id = :locationPointOfInterestId "
                + "AND time_of_observation BETWEEN :startDateGrowth AND :endDateCalculation"
                ,Observation.class
        )
                .setParameter("organismId", organismId)
                .setParameter("locationPointOfInterestId",pointOfInterestId)
                .setParameter("startDateGrowth", startDateGrowth)
                .setParameter("endDateCalculation", endDateCalculation)
                .getResultList();
    }
}
