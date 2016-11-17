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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import no.nibio.vips.logic.entity.CropCategory;
import no.nibio.vips.logic.entity.Gis;
import no.nibio.vips.logic.entity.Message;
import no.nibio.vips.logic.entity.MessageIllustration;
import no.nibio.vips.logic.entity.MessageIllustrationPK;
import no.nibio.vips.logic.entity.Observation;
import no.nibio.vips.logic.entity.ObservationIllustration;
import no.nibio.vips.logic.entity.ObservationIllustrationPK;
import no.nibio.vips.logic.entity.ObservationStatusType;
import no.nibio.vips.logic.entity.Organism;
import no.nibio.vips.logic.entity.Organization;
import no.nibio.vips.logic.entity.PointOfInterest;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.util.SessionControllerGetter;
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
        Organization organization = em.find(Organization.class,organizationId);
        List<Observation> observations = em.createNamedQuery("Observation.findByOrganizationId")
                .setParameter("organizationId", organization)
                .getResultList();
        
        observations = this.getObservationsWithGeoInfo(observations);
        observations = this.getObservationsWithLocations(observations);
        observations = getObservationsWithObservers(observations);
        
        return observations;
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
            retVal.setUser(em.find(VipsLogicUser.class, retVal.getUserId()));
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
        
        // Using this method as opposed to db query for each observation, we 
        // slim the time down from 24 seconds to 270 milliseconds! DB connections are expensive....
        
        // Indexing observations
        Map<Integer, Observation> obsBucket = new HashMap<>();
        observations.stream().forEach((obs) -> {
            obsBucket.put(obs.getObservationId(), obs);
        });
        
        // Getting many-to-many relations
        Query q = em.createNativeQuery("SELECT gis_id, observation_id FROM public.gis_observation WHERE observation_id IN :observationIds");
        q.setParameter("observationIds", obsBucket.keySet());
        List<Object[]> gisObservationIds = q.getResultList();
        
        // Collecting and indexing geoInfo
        Query q2 = em.createNativeQuery("SELECT * FROM public.gis WHERE gis_id IN (SELECT gis_id FROM public.gis_observation WHERE observation_id IN :observationIds)", Gis.class);
        List<Gis> geoInfos = q2.setParameter("observationIds", obsBucket.keySet()).getResultList();
        Map<Integer, Gis> gisBucket = new HashMap<>();
        geoInfos.stream().forEach((geoinfo) -> {
            gisBucket.put(geoinfo.getGisId(), geoinfo);
        });
        
        // Iterating the many-to-many relations,
        // adding geoinfo to the correct observations
        gisObservationIds.stream().forEach((gisObsIds) -> {
            Integer gisId = (Integer) gisObsIds[0];
            Integer observationId = (Integer) gisObsIds[1];
            obsBucket.get(observationId).addGeoInfo(gisBucket.get(gisId));
        });
        
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
            observation.getGeoinfos().stream().forEach((gis) -> {
                em.persist(gis);
            });
            
            Query q = em.createNativeQuery("INSERT INTO public.gis_observation(gis_id,observation_id) VALUES(:gisId,:observationId)")
                .setParameter("observationId", retVal.getObservationId());
            observation.getGeoinfos().stream().forEach((gis) -> {
                q.setParameter("gisId", gis.getGisId())
                        .executeUpdate();
            });
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
     * @param startDate
     * @param endDate
     * @return 
     */
    public List<no.nibio.vips.observation.Observation> getObservations(Integer organismId, Integer pointOfInterestId, Date startDate, Date endDate) {
        /*System.out.println("organismId = " + organismId);
        System.out.println("pointOfInterestId = " + pointOfInterestId);
        System.out.println("period= " + startDate + "-" + endDate);*/
        return em.createNativeQuery(
                "SELECT * FROM public.observation "
                + "WHERE organism_id = :organismId "
                + "AND location_point_of_interest_id = :locationPointOfInterestId "
                + "AND time_of_observation BETWEEN :startDate AND :endDate"
                ,Observation.class
        )
                .setParameter("organismId", organismId)
                .setParameter("locationPointOfInterestId",pointOfInterestId)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }

    private List<Observation> getObservationsWithLocations(List<Observation> observations) {
         Set<Integer> locationPointOfInterestIds = new HashSet<>();
         observations.stream().filter((o) -> (o.getLocationPointOfInterestId() != null)).forEach((o) -> {
             locationPointOfInterestIds.add(o.getLocationPointOfInterestId());
        });
         // Nothing to do?
        if(locationPointOfInterestIds.isEmpty())
        {
            return observations;
        }
         List<PointOfInterest> pois = SessionControllerGetter.getPointOfInterestBean().getPois(locationPointOfInterestIds);
         Map<Integer, PointOfInterest> mappedPois = new HashMap<>();
         pois.stream().forEach((poi) -> {
             mappedPois.put(poi.getPointOfInterestId(), poi);
        });
         observations.stream().filter((o) -> (o.getLocationPointOfInterestId() != null)).forEach((o) -> {
             o.setLocation(mappedPois.get(o.getLocationPointOfInterestId()));
        });
         return observations;
    }

    private List<Observation> getObservationsWithObservers(List<Observation> observations) {
        Set<Integer> userIds = new HashSet<>();
        observations.stream().filter((o) -> (o.getUserId() != null)).forEach((o) -> {
            userIds.add(o.getUserId());
        });
         List<VipsLogicUser> users = SessionControllerGetter.getUserBean().getUsers(userIds);
         Map<Integer, VipsLogicUser> mappedUsers = new HashMap<>();
         users.stream().forEach((user) -> {
             mappedUsers.put(user.getUserId(), user);
        });
         observations.stream().filter((o) -> (o.getUserId() != null)).forEach((o) -> {
             o.setUser(mappedUsers.get(o.getUserId()));
        });
         return observations;
    }
    
    public List<Observation> getObservationsOfPest(Integer pestOrganismId)
    {
        return em.createNamedQuery("Observation.findByOrganism")
                .setParameter("organism", em.find(Organism.class, pestOrganismId))
                .getResultList();
    }

    public List<Observation> getFilteredObservations(
            Integer organizationId, 
            Integer pestId, 
            Integer cropId, 
            Integer cropCategoryId,
            Date from, 
            Date to
    ) 
    {
        // The minimum SQL
        String sql = "SELECT * FROM public.observation \n" +
                     "WHERE status_type_id = :statusTypeId \n " + 
                     "AND user_id IN (SELECT user_id FROM public.vips_logic_user WHERE organization_id = :organizationId) \n";
       
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("statusTypeId", ObservationStatusType.STATUS_APPROVED);
        parameters.put("organizationId", organizationId);
        
        // Filter for pest
        if(pestId != null && pestId > 0)
        {
            sql += "AND organism_id = :organismId \n";
            parameters.put("organismId", pestId);
        }
        // Filter either for crop or cropCategoryId
        if(cropId != null && cropId > 0)
        {
            sql += "AND crop_organism_id = :cropOrganismId \n";
            parameters.put("cropOrganismId", cropId);
        }
        else if(cropCategoryId != null && cropCategoryId > 0)
        {
            CropCategory cropCategory = em.createNamedQuery("CropCategory.findByCropCategoryId", CropCategory.class)
                    .setParameter("cropCategoryId", cropCategoryId)
                    .getSingleResult();
            List<Integer> cropIds = Arrays.asList(cropCategory.getCropOrganismIds());
                    
            sql += "AND crop_organism_id IN (:cropOrganismIds) \n";
            parameters.put("cropOrganismIds", cropIds);
        }
        // Filter for dates
        if(from != null)
        {
            sql += "AND time_of_observation >= :from \n";
            parameters.put("from", from);
        }
        if(to != null)
        {
            sql += "AND time_of_observation <= :to \n";
            parameters.put("to", to);
        }
        
        Query q = em.createNativeQuery(sql, Observation.class);
        // Setting the parameters one by one
        parameters.keySet().stream().forEach(
                (key)->q.setParameter(key, parameters.get(key))
        );
        
        //Date start = new Date();
        
        List<Observation> observations = q.getResultList();
        
        //System.out.println("Finding obs took " + (new Date().getTime() - start.getTime()) + " milliseconds");
        
        //start = new Date();
        observations.stream().forEach(
                (observation)->observation.setUser(em.find(VipsLogicUser.class, observation.getUserId()))
        );
        
        //System.out.println("Finding users took " + (new Date().getTime() - start.getTime()) + " milliseconds");
        
        List<Observation> retVal = new ArrayList<>();
        if(! observations.isEmpty())
        {
            //start = new Date();
            retVal = this.getObservationsWithGeoInfo(observations);
            //System.out.println("Finding geoinfo took " + (new Date().getTime() - start.getTime()) + " milliseconds");
            //start = new Date();
            retVal = this.getObservationsWithLocations(retVal);
            //System.out.println("Finding locations took " + (new Date().getTime() - start.getTime()) + " milliseconds");
        }
        
        return retVal;
        
    }

    public List<Organism> getObservedPests(Integer organizationId) {
        Query q = em.createNativeQuery("SELECT DISTINCT organism_id FROM public.observation WHERE user_id IN ("
                + " SELECT user_id FROM vips_logic_user WHERE organization_id = :organizationId"
                + ")");
        List<Integer> pestIds = q.setParameter("organizationId", organizationId).getResultList();
        return em.createNamedQuery("Organism.findByOrganismIds")
                .setParameter("organismIds", pestIds)
                .getResultList();
        
    }
    
    public List<Organism> getObservedCrops(Integer organizationId) {
        Query q = em.createNativeQuery("SELECT DISTINCT crop_organism_id FROM public.observation WHERE user_id IN ("
                + " SELECT user_id FROM vips_logic_user WHERE organization_id = :organizationId"
                + ")");
        List<Integer> cropIds = q.setParameter("organizationId", organizationId).getResultList();
        return em.createNamedQuery("Organism.findByOrganismIds")
                .setParameter("organismIds", cropIds)
                .getResultList();
        
    }

    
    
}
