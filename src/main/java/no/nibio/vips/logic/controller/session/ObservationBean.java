/*
 * Copyright (c) 2018 NIBIO <http://www.nibio.no/>.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package no.nibio.vips.logic.controller.session;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ibm.icu.util.ULocale;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;

import no.nibio.vips.logic.entity.*;
import no.nibio.vips.logic.i18n.SessionLocaleUtil;
import no.nibio.vips.logic.util.SystemTime;
import org.apache.commons.codec.binary.Base64;
import no.nibio.vips.observationdata.ObservationDataSchema;
import no.nibio.vips.observationdata.ObservationDataSchemaPK;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.GeoJSONFactory;

/**
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 * @copyright 2014-2020 <a href="http://www.nibio.no/">NIBIO</a>
 */
@Stateless
public class ObservationBean {
    private static Logger LOGGER = LoggerFactory.getLogger(ObservationBean.class);
    @PersistenceContext(unitName = "VIPSLogic-PU")
    EntityManager em;
    @EJB
    PointOfInterestBean pointOfInterestBean;
    @EJB
    UserBean userBean;

    public List<Observation> getObservations(Integer organizationId) {
        Organization organization = em.find(Organization.class, organizationId);
        List<Observation> observations = em.createNamedQuery("Observation.findByOrganizationId")
                .setParameter("organizationId", organization)
                .getResultList();

        observations = this.getObservationsWithGeoInfo(observations);
        observations = this.getObservationsWithLocations(observations);
        observations = this.getObservationsWithObservers(observations);

        return observations;
    }

    public List<Observation> getObservations(Integer organizationId, Date periodStart, Date periodEnd) {
        Organization organization = em.find(Organization.class, organizationId);
        List<Observation> observations = em.createNamedQuery("Observation.findByOrganizationIdAndPeriod")
                .setParameter("organizationId", organization)
                .setParameter("start", periodStart)
                .setParameter("end", periodEnd)
                .getResultList();

        observations = this.getObservationsWithGeoInfo(observations);
        observations = this.getObservationsWithLocations(observations);
        observations = this.getObservationsWithObservers(observations);

        return observations;
    }

    public List<Observation> getObservations(Integer organizationId, Integer statusTypeId) {
        Organization organization = em.find(Organization.class, organizationId);
        /*List<VipsLogicUser> users = em.createNamedQuery("VipsLogicUser.findByOrganizationId", VipsLogicUser.class)
                                        .setParameter("organizationId", organization)
                                        .getResultList();*/

        List<Observation> retVal = this.getObservationsWithGeoInfo(em.createNamedQuery("Observation.findByOrganizationIdAndStatusTypeId")
                .setParameter("organizationId", organization)
                .setParameter("statusTypeId", statusTypeId)
                .getResultList());

        return retVal;
    }

    public List<Observation> getObservationsForUser(VipsLogicUser user) {
        List<Observation> retVal = this.getObservationsWithGeoInfo(em.createNamedQuery("Observation.findByUserId")
                .setParameter("userId", user.getUserId())
                .getResultList());

        retVal = this.getObservationsWithLocations(retVal);
        retVal = this.getObservationsWithObservers(retVal);

        return retVal;
    }

    public List<Observation> getObservationsLastEditedByUser(VipsLogicUser user) {
        List<Observation> retVal = this.getObservationsWithGeoInfo(em.createNamedQuery("Observation.findByLastEditedBy")
                .setParameter("lastEditedBy", user.getUserId())
                .getResultList());

        retVal = this.getObservationsWithLocations(retVal);
        retVal = this.getObservationsWithObservers(retVal);

        return retVal;
    }

    public List<Observation> getObservationsStatusChangedByUser(VipsLogicUser user) {
        List<Observation> retVal = this.getObservationsWithGeoInfo(em.createNamedQuery("Observation.findByStatusChangedByUserId")
                .setParameter("statusChangedByUserId", user.getUserId())
                .getResultList());

        retVal = this.getObservationsWithLocations(retVal);
        retVal = this.getObservationsWithObservers(retVal);

        return retVal;
    }

    public List<Observation> getObservationsForUser(VipsLogicUser user, Date periodStart, Date periodEnd) {
        List<Observation> retVal = this.getObservationsWithGeoInfo(em.createNamedQuery("Observation.findByUserIdAndPeriod")
                .setParameter("userId", user.getUserId())
                .setParameter("start", periodStart)
                .setParameter("end", periodEnd)
                .getResultList());

        retVal = this.getObservationsWithLocations(retVal);
        retVal = this.getObservationsWithObservers(retVal);

        return retVal;
    }

    public List<Observation> getObservationsForUser(VipsLogicUser user, Integer statusTypeId) {
        List<Observation> retVal = this.getObservationsWithGeoInfo(em.createNamedQuery("Observation.findByUserIdAndStatusTypeId")
                .setParameter("userId", user.getUserId())
                .setParameter("statusTypeId", statusTypeId)
                .getResultList());

        return retVal;
    }

    public Observation getObservation(Integer observationId) {
        Observation retVal = em.find(Observation.class, observationId);
        if (retVal != null) {
            retVal.setGeoinfos(this.getGeoinfoForObservation(retVal));
            retVal.setUser(em.find(VipsLogicUser.class, retVal.getUserId()));
            if (retVal.getLastEditedBy() != null) {
                retVal.setLastEditedByUser(em.find(VipsLogicUser.class, retVal.getLastEditedBy()));
            }
        }
        return retVal;
    }

    public List<Gis> getGeoinfoForObservation(Observation obs) {
        List<Integer> gisIds = em.createNativeQuery("SELECT gis_id FROM public.gis_observation WHERE observation_id = :observationId")
                .setParameter("observationId", obs.getObservationId())
                .getResultList();

        if (gisIds != null && !gisIds.isEmpty()) {
            return em.createNamedQuery("Gis.findByGisIds", Gis.class)
                    .setParameter("gisIds", gisIds)
                    .getResultList();
        } else {
            return null;
        }
    }

    public List<Observation> getObservationsWithGeoInfo(List<Observation> observations) {
        if (observations.isEmpty()) {
            return observations;
        }

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
        if (observation.getGeoinfos() != null && !observation.getGeoinfos().isEmpty()) {
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

        // The GisObservations are not included in the merged object, so we should add them
        retVal.setGeoinfos(this.getGeoinfoForObservation(retVal));

        return retVal;
    }

    public void deleteObservation(Integer observationId) {
        Observation observation = em.find(Observation.class, observationId);
        if (observation != null) {
            // Delete all current group memberships
            em.createNativeQuery("DELETE FROM public.organization_group_observation WHERE observation_id=:observationId")
                    .setParameter("observationId", observation.getObservationId())
                    .executeUpdate();

            // Delete all illustrations (including removing files on disk)
            String[] filesToDelete = observation.getObservationIllustrationSet().stream()
                    .map(ill -> ill.getObservationIllustrationPK().getFileName())
                    .collect(Collectors.toList())
                    .toArray(new String[0]);
            this.deleteObservationIllustration(observation, filesToDelete);
            em.remove(observation);
        }
    }

    /**
     * @param organizationId
     * @param season
     * @return
     */
    public List<Observation> getBroadcastObservations(Integer organizationId, Integer season) {
        Organization organization = em.find(Organization.class, organizationId);
        /*List<VipsLogicUser> users = em.createNamedQuery("VipsLogicUser.findByOrganizationId", VipsLogicUser.class)
                                        .setParameter("organizationId", organization)
        
        .getResultList();*/
        List<Observation> retVal = null;
        if (season == null) {
            retVal = this.getObservationsWithGeoInfo(em.createNamedQuery("Observation.findByOrganizationIdAndStatusTypeIdAndBroadcastMessage")
                    .setParameter("organizationId", organization)
                    .setParameter("statusTypeId", Observation.STATUS_TYPE_ID_APPROVED)
                    .getResultList());
        } else {
            Calendar cal = Calendar.getInstance();
            cal.set(season, Calendar.JANUARY, 1, 0, 0, 0);
            Date start = cal.getTime();
            cal.set(season, Calendar.DECEMBER, 31, 23, 59, 59);
            Date end = cal.getTime();
            retVal = this.getBroadcastObservations(organizationId, start, end);
        }
        return retVal;
    }

    /**
     * @param organizationId
     * @param start          When period starts. Default: Jan 1st 2000
     * @param end            When period ends. Default: 100 years from now
     * @return
     */
    public List<Observation> getBroadcastObservations(Integer organizationId, Date start, Date end) {
        if (start == null || end == null) {
            Calendar cal = Calendar.getInstance();
            if (start == null) // Default Jan 1st 2000
            {
                cal.set(2000, Calendar.JANUARY, 1, 0, 0, 0);
                start = cal.getTime();
            }
            if (end == null) // Default: Today + 100 years
            {
                cal.setTime(SystemTime.getSystemTime());
                cal.add(Calendar.YEAR, 100);
                end = cal.getTime();
            }
        }
        Organization organization = em.find(Organization.class, organizationId);
        return this.getObservationsWithGeoInfo(em.createNamedQuery("Observation.findByOrganizationIdAndStatusTypeIdAndBroadcastMessageAndPeriod")
                .setParameter("organizationId", organization)
                .setParameter("statusTypeId", Observation.STATUS_TYPE_ID_APPROVED)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList());
    }


    /**
     * @param observation
     * @return [OBSERVATION_ILLUSTRATION_PATH]/[ORGANISM_ID]/
     */
    private String getFilePath(Observation observation) {
        return System.getProperty("no.nibio.vips.logic.OBSERVATION_ILLUSTRATION_PATH") + "/"
                + observation.getOrganismId();
    }

    public Observation storeObservationIllustration(Observation observation, String fileName, String base64Data) {
        String[] metaAndData = base64Data.split(",");
        byte[] imageData = Base64.decodeBase64(metaAndData[1]);
        Path path = Paths.get(this.getFilePath(observation) + "/" + fileName);
        try {
            // Make sure the directory exists
            File testDirectoryfile = new File(this.getFilePath(observation));
            if (!testDirectoryfile.exists()) {
                testDirectoryfile.mkdirs();
            }
            Files.write(path, imageData, StandardOpenOption.CREATE);
            ObservationIllustration newIllustration = new ObservationIllustration(new ObservationIllustrationPK(observation.getObservationId(), fileName));
            newIllustration = em.merge(newIllustration);

            // Add the new illustration
            if (observation.getObservationIllustrationSet() == null) {
                observation.setObservationIllustrationSet(new HashSet<ObservationIllustration>());
            }
            observation.getObservationIllustrationSet().add(newIllustration);

            return observation;
        } catch (IOException ex) {
            ex.printStackTrace();
            return observation;
        }
    }

    public Observation storeObservationIllustration(Observation observation, FileItem item) throws Exception {
        // Create a candidate filename
        // [OBSERVATION_ILLUSTRATION_PATH]/[ORGANIZATION_ID]/[OBSERVATION_ID]_illustration.[fileExtension]
        String filePath = this.getFilePath(observation);
        String fileName = observation.getObservationId() + "_illustration." + FilenameUtils.getExtension(item.getName());
        // Check availability, and adapt filename until available
        Integer fileNameSuffix = 1;
        File illustration = new File(filePath + "/" + fileName);
        while (illustration.exists()) {
            fileName = observation.getObservationId() + "_illustration_" + fileNameSuffix + "." + FilenameUtils.getExtension(item.getName());
            illustration = new File(filePath + "/" + fileName);
            fileNameSuffix++;
        }
        File testDirectoryfile = new File(filePath);
        // If directory does not exist, create it
        if (!testDirectoryfile.exists()) {
            testDirectoryfile.mkdirs();
        }

        // Store file
        item.write(illustration);

        // Update MessageIllustrations
        observation = em.merge(observation);
        // Remove the old illustration(s)

        ObservationIllustration newIllustration = new ObservationIllustration(new ObservationIllustrationPK(observation.getObservationId(), fileName));
        em.persist(newIllustration);

        // Add the new illustration 
        if (observation.getObservationIllustrationSet() == null) {
            observation.setObservationIllustrationSet(new HashSet<ObservationIllustration>());
        }
        observation.getObservationIllustrationSet().add(newIllustration);
        return observation;
    }

    public Observation deleteObservationIllustration(Observation observation, String[] deleteIllustrations) {
        observation = em.merge(observation);

        Set<ObservationIllustration> formerIllustrations = observation.getObservationIllustrationSet();
        if (formerIllustrations == null) {
            return observation;
        }

        Set<ObservationIllustration> deleteThese = new HashSet<>();

        for (String deleteIllustration : deleteIllustrations) {
            for (ObservationIllustration formerIllustration : formerIllustrations) {
                if (formerIllustration.getObservationIllustrationPK().getFileName()
                        .equals(deleteIllustration)) {
                    deleteThese.add(formerIllustration);
                }
            }
        }

        for (ObservationIllustration ill : deleteThese) {
            observation.getObservationIllustrationSet().remove(ill);
            em.remove(ill);
            // Physically remove it too
            File fileToDelete = new File(this.getFilePath(observation) + "/" + ill.getObservationIllustrationPK().getFileName());
            fileToDelete.delete();
        }
        return observation;
    }

    /**
     * Fetch observations of a particular organism at a particular place and period
     *
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
                        , Observation.class
                )
                .setParameter("organismId", organismId)
                .setParameter("locationPointOfInterestId", pointOfInterestId)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }

    public List<Observation> getObservationsWithLocations(List<Observation> observations) {
        Set<Integer> locationPointOfInterestIds = new HashSet<>();
        observations.stream().filter((o) -> (o.getLocationPointOfInterestId() != null)).forEach((o) -> {
            locationPointOfInterestIds.add(o.getLocationPointOfInterestId());
        });
        // Nothing to do?
        if (locationPointOfInterestIds.isEmpty()) {
            return observations;
        }
        List<PointOfInterest> pois = pointOfInterestBean.getPois(locationPointOfInterestIds);
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
        // Nothing to do?
        if (userIds.isEmpty()) {
            return observations;
        }
        List<VipsLogicUser> users = userBean.getUsers(userIds);
        Map<Integer, VipsLogicUser> mappedUsers = new HashMap<>();
        users.stream().forEach((user) -> {
            mappedUsers.put(user.getUserId(), user);
        });
        observations.stream().filter((o) -> (o.getUserId() != null)).forEach((o) -> {
            o.setUser(mappedUsers.get(o.getUserId()));
        });
        return observations;
    }

    public List<Observation> getObservationsOfPest(Integer pestOrganismId) {
        List<Observation> observations =
                em.createNamedQuery("Observation.findByOrganism")
                        .setParameter("organism", em.find(Organism.class, pestOrganismId))
                        .getResultList();

        observations = this.getObservationsWithGeoInfo(observations);
        observations = this.getObservationsWithLocations(observations);
        observations = getObservationsWithObservers(observations);
        return observations;
    }

    public List<Observation> getObservationsOfPestForUser(VipsLogicUser user, Integer pestOrganismId) {
        List<Observation> observations =
                em.createNamedQuery("Observation.findByUserIdAndOrganism")
                        .setParameter("userId", user.getUserId())
                        .setParameter("organism", em.find(Organism.class, pestOrganismId))
                        .getResultList();

        observations = this.getObservationsWithGeoInfo(observations);
        observations = this.getObservationsWithLocations(observations);
        observations = this.getObservationsWithObservers(observations);
        return observations;
    }

    public List<Observation> getFilteredObservations(
            Integer organizationId,
            Integer observationTimeSeriesId,
            Integer pestId,
            Integer cropId,
            List<Integer> cropCategoryId,
            Date from,
            Date to,
            Boolean isPositive) {
        // The minimum SQL
        String sql = "SELECT * FROM public.observation \n" +
                "WHERE status_type_id = :statusTypeId \n " +
                "AND user_id IN (SELECT user_id FROM public.vips_logic_user WHERE organization_id = :organizationId) \n";

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("statusTypeId", ObservationStatusType.STATUS_APPROVED);
        parameters.put("organizationId", organizationId);

        // Filter for observation time series
        if (observationTimeSeriesId != null && observationTimeSeriesId > 0) {
            sql += "AND observation_time_series_id = :observationTimeSeriesId \n";
            parameters.put("observationTimeSeriesId", observationTimeSeriesId);
        }
        // Filter for pest
        if (pestId != null && pestId > 0) {
            sql += "AND organism_id = :organismId \n";
            parameters.put("organismId", pestId);
        }
        // Filter either for crop or cropCategoryId
        if (cropId != null && cropId > 0) {
            sql += "AND crop_organism_id = :cropOrganismId \n";
            parameters.put("cropOrganismId", cropId);
        } else if (cropCategoryId != null && !cropCategoryId.isEmpty()) {
            List<CropCategory> cropCategories = em.createNamedQuery("CropCategory.findByCropCategoryIds", CropCategory.class)
                    .setParameter("cropCategoryIds", cropCategoryId)
                    .getResultList();
            List<Integer> cropIds = new ArrayList(cropCategories.stream().flatMap(cC -> Arrays.asList(cC.getCropOrganismIds()).stream()).collect(Collectors.toSet()));

            sql += "AND crop_organism_id IN (:cropOrganismIds) \n";
            parameters.put("cropOrganismIds", cropIds);
        }
        // Filter for dates
        if (from != null) {
            sql += "AND time_of_observation >= :from \n";
            parameters.put("from", from);
        }
        if (to != null) {
            sql += "AND time_of_observation <= :to \n";
            parameters.put("to", to);
        }
        // Filter for positive/negative registrations
        if (isPositive != null) {
            sql += "AND is_positive = :isPositive \n";
            parameters.put("isPositive", isPositive);
        }

        sql += "ORDER BY time_of_observation DESC, observation_id DESC";

        LOGGER.debug(sql);

        Query q = em.createNativeQuery(sql, Observation.class);
        // Setting the parameters one by one
        parameters.keySet().stream().forEach(
                (key) -> {
                    LOGGER.debug(key + ": " + parameters.get(key));
                    q.setParameter(key, parameters.get(key));
                }
        );

        //Date start = new Date();

        List<Observation> observations = q.getResultList();
        //System.out.println("Finding obs took " + (new Date().getTime() - start.getTime()) + " milliseconds");

        //start = new Date();
        observations.stream().forEach(
                (observation) -> observation.setUser(em.find(VipsLogicUser.class, observation.getUserId()))
        );

        //System.out.println("Finding users took " + (new Date().getTime() - start.getTime()) + " milliseconds");

        List<Observation> retVal = new ArrayList<>();
        if (!observations.isEmpty()) {
            //Date start = new Date();
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

    public Observation getObservationFromGeoJSON(String geoJSON) throws IOException {
        //System.out.println(geoJSON);
        FeatureCollection featureCollection = (FeatureCollection) GeoJSONFactory.create(geoJSON);
        Feature firstAndBest = featureCollection.getFeatures()[0];
        Map<String, Object> properties = firstAndBest.getProperties();
        Observation observation = new Observation();
        Integer observationId = (Integer) properties.get("observationId");
        if (observationId > 0) {
            observation = em.find(Observation.class, observationId);
        }
        observation.setObservationData((String) properties.get("observationData"));
        ObjectMapper mapper = new ObjectMapper();
        observation.setCropOrganism(mapper.convertValue(properties.get("cropOrganism"), Organism.class));
        observation.setOrganism(mapper.convertValue(properties.get("organism"), Organism.class));
        observation.setObservationHeading((String) properties.get("observationHeading"));
        observation.setObservationText((String) properties.get("observationText"));
        observation.setTimeOfObservation(new Date((Long) properties.get("timeOfObservation")));
        observation.setGeoinfo(geoJSON);
        observation.setStatusTypeId((Integer) properties.get("statusTypeId"));
        observation.setStatusRemarks((String) properties.get("statusRemarks"));
        observation.setIsQuantified((Boolean) properties.get("isQuantified"));
        observation.setBroadcastMessage((Boolean) properties.get("broadcastMessage"));
        return observation;
    }

    public void deleteGisObservationByGis(Integer gisId) {
        Observation observation = (Observation) em.createNativeQuery("SELECT * FROM observation WHERE observation_id = (SELECT DISTINCT observation_id FROM gis_observation WHERE gis_id = :gisId);", Observation.class)
                .setParameter("gisId", gisId)
                .getSingleResult();
        this.deleteObservation(observation.getObservationId());
    }

    public List<ObservationFormShortcut> getObservationFormShortcuts(Organization organization) {
        return em.createNamedQuery("ObservationFormShortcut.findByOrganizationId").
                setParameter("organizationId", organization.getOrganizationId())
                .getResultList();
    }

    public List<Integer> getOrganizationGroupIds(Observation observation) {
        if (observation.getObservationId() != null) {
            return em.createNativeQuery("SELECT organization_group_id FROM public.organization_group_observation "
                            + "WHERE observation_id = :observationId")
                    .setParameter("observationId", observation.getObservationId())
                    .getResultList();
        } else {
            return new ArrayList<>();
        }
    }

    public void storeOrganizationGroupObservationIds(Observation obs, String[] organizationGroupIds) {
        // First delete all current group memberships
        em.createNativeQuery("DELETE FROM public.organization_group_observation WHERE observation_id=:observationId")
                .setParameter("observationId", obs.getObservationId())
                .executeUpdate();

        if (organizationGroupIds != null) {
            Query q = em.createNativeQuery("INSERT INTO public.organization_group_observation (organization_group_id, observation_id) "
                            + "VALUES(:organizationGroupId, :observationId)")
                    .setParameter("observationId", obs.getObservationId());
            // Then add
            for (String groupIdStr : organizationGroupIds) {
                try {
                    Integer groupId = Integer.valueOf(groupIdStr);
                    q.setParameter("organizationGroupId", groupId);
                    q.executeUpdate();
                } catch (NumberFormatException ex) {
                    // Continue
                }
            }
        }
    }

    /**
     * Returns the first time an observation of the given pest registered in the system was made
     *
     * @param organismId
     * @return
     */
    public Date getFirstObservationTime(Integer organismId) {

        try {
            List<Observation> obs = em.createNamedQuery("Observation.findFirstByOrganism")
                    .setParameter("organism", em.find(Organism.class, organismId))
                    .getResultList();
            return obs.get(0).getTimeOfObservation();
        } catch (NoResultException | IndexOutOfBoundsException ex) {
            return null;
        }

    }

    public PolygonService getPolygonService(Integer polygonServiceId) {
        return em.find(PolygonService.class, polygonServiceId);
    }

    public List<PolygonService> getPolygonServicesForOrganization(Integer organizationId) {
        return em.createNativeQuery("SELECT * FROM polygon_service p WHERE p.polygon_service_id IN (SELECT polygon_service_id FROM public.organization_polygon_service WHERE organization_id=:organizationId)", PolygonService.class)
                .setParameter("organizationId", organizationId)
                .getResultList();
    }

    public List<Observation> getObservationsByLocation(PointOfInterest poi) {
        return em.createNativeQuery("SELECT * FROM Observation WHERE location_point_of_interest_id=:locationPointOfInterestId", Observation.class)
                .setParameter("locationPointOfInterestId", poi.getPointOfInterestId())
                .getResultList();
    }

    /**
     * Part of the cleaning up dependencies procedure for when deleting a POI
     *
     * @param poi
     */
    public void deleteObservationsForLocation(PointOfInterest poi) {
        em.createNamedQuery("Observation.findByLocationPointOfInterestId", Observation.class)
                .setParameter("locationPointOfInterestId", poi.getPointOfInterestId())
                .getResultList().stream()
                .forEach(obs -> em.remove(obs));
    }

    public void deleteObservationsForObservationTimeSeries(ObservationTimeSeries observationTimeSeries) {
        em.createNamedQuery("Observation.findByObservationTimeSeries", Observation.class)
                .setParameter("observationTimeSeries", observationTimeSeries)
                .getResultList()
                .forEach(obs -> em.remove(obs));
    }

    /**
     * Returns the appropriate observation data schema
     * If no existing schema, returns the standard (Requring just a number)
     *
     * @param organizationId
     * @param organismId
     * @return
     */
    public ObservationDataSchema getObservationDataSchema(Integer organizationId, Integer organismId) {
        try {
            return em.createNamedQuery("ObservationDataSchema.findByPK", ObservationDataSchema.class)
                    .setParameter("organizationId", organizationId)
                    .setParameter("organismId", organismId)
                    .getSingleResult();


        } catch (NoResultException ex) {
            //System.out.println("Could not find schema for orgId " + organizationId + " and organismId " + organismId);
            return this.getStandardSchema(organizationId);
        }
    }

    /**
     * If there exist title translations for this schema, it
     *
     * @param schema
     * @param httpServletRequest
     * @return
     */
    public ObservationDataSchema getLocalizedObservationDataSchema(ObservationDataSchema ods, HttpServletRequest httpServletRequest, ULocale locale) throws IOException {
        if (locale != null) {
            SessionLocaleUtil.setCurrentLocale(httpServletRequest, locale);
        }
        ResourceBundle bundle = SessionLocaleUtil.getI18nBundle(httpServletRequest);

        // We iterate the schema, replacing default field labels with
        // translated ones
        // First: Convert to Jackson JsonNode tree
        ObjectMapper m = new ObjectMapper();
        JsonNode rootNode = m.readTree(ods.getDataSchema());
        // Is this the full schema or just the "properties" property?
        JsonNode propertiesNode = rootNode.get("properties") == null ? rootNode : rootNode.get("properties");

        Iterator<Entry<String, JsonNode>> nodeIterator = propertiesNode.fields();

        String fieldKeyPrefix = "observationDataField_";
        // Loop through each field
        while (nodeIterator.hasNext()) {
            Map.Entry<String, JsonNode> schemaPropertyField = (Map.Entry<String, JsonNode>) nodeIterator.next();
            // Get the property field key (e.g. "counting2")
            String fieldKey = schemaPropertyField.getKey();
            // Find a translation.
            if (bundle.containsKey(fieldKeyPrefix + fieldKey)) {
                // If found, replace with translation
                // Get the property field (e.g. {"title":"Counting 2"} )
                JsonNode schemaProperty = schemaPropertyField.getValue();
                ((ObjectNode) schemaProperty).put("title", bundle.getString(fieldKeyPrefix + fieldKey));
                ((ObjectNode) propertiesNode).replace(fieldKey, schemaProperty);
            }
        }

        // I repeat: Is this the full schema or just the "properties" property?
        if (rootNode.get("properties") != null) {
            ((ObjectNode) rootNode).replace("properties", propertiesNode);
        } else {
            rootNode = propertiesNode;
        }
        ods.setDataSchema(m.writeValueAsString(rootNode));
        return ods;
    }

    /**
     * @param organizationId
     * @return
     */
    private ObservationDataSchema getStandardSchema(Integer organizationId) {
        ObservationDataSchema retVal = new ObservationDataSchema();
        retVal.setDataSchema("{\n"
                + "  \"$schema\": \"http://json-schema.org/draft-04/schema#\",\n"
                + "  \"type\": \"object\",\n"
                + "  \"title\": \"Default schema\",\n"
                + "  \"properties\": {"
                + "\"number\":{\"title\":\"Number\"},"
                + "\"unit\":{\"title\":\"Unit\"}"
                + "}"
                + "}");
        retVal.setDataModel("{"
                + "\"number\":0,"
                + "\"unit\":\"Number\""
                + "}");
        ObservationDataSchemaPK pk = new ObservationDataSchemaPK();
        pk.setOrganismId(-1);
        pk.setOrganizationId(organizationId);
        retVal.setObservationDataSchemaPK(pk);
        return retVal;
    }


}
