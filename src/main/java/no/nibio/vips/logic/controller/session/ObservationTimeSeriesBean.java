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

import no.nibio.vips.logic.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.*;

@Stateless
public class ObservationTimeSeriesBean {

    @PersistenceContext(unitName = "VIPSLogic-PU")
    EntityManager em;
    @EJB
    PointOfInterestBean pointOfInterestBean;
    @EJB
    UserBean userBean;
    @EJB
    ObservationBean observationBean;

    public List<ObservationTimeSeries> getObservationTimeSeriesListForUser(VipsLogicUser user) {
        List<ObservationTimeSeries> resultList = em.createNamedQuery("ObservationTimeSeries.findByUserId", ObservationTimeSeries.class)
                .setParameter("userId", user.getUserId())
                .getResultList();
        this.enrichObservationTimeSeriesListWithGeoInfo(resultList);
        this.enrichObservationTimeSeriesListWithPointOfInterest(resultList);
        this.enrichObservationTimeSeriesListWithObservers(resultList);
        return resultList;
    }

    private void enrichObservationTimeSeriesListWithGeoInfo(List<ObservationTimeSeries> otsList) {
        if (otsList.isEmpty()) {
            return;
        }

        // Indexing observations
        Map<Integer, ObservationTimeSeries> obsBucket = new HashMap<>();
        otsList.forEach((obs) -> {
            obsBucket.put(obs.getObservationTimeSeriesId(), obs);
        });

        // Getting many-to-many relations
        Query q = em.createNativeQuery("SELECT gis_id, observation_time_series_id FROM public.gis_observation_time_series WHERE observation_time_series_id IN :observationTimeSeriesIds");
        q.setParameter("observationTimeSeriesIds", obsBucket.keySet());
        List<Object[]> gisObservationTimeSeriesIds = q.getResultList();

        // Collecting and indexing geoInfo
        Query q2 = em.createNativeQuery("SELECT * FROM public.gis WHERE gis_id IN (SELECT gis_id FROM public.gis_observation_time_series WHERE observation_time_series_id IN :observationTimeSeriesIds)", Gis.class);
        List<Gis> geoInfos = q2.setParameter("observationTimeSeriesIds", obsBucket.keySet()).getResultList();
        Map<Integer, Gis> gisBucket = new HashMap<>();
        geoInfos.forEach((geoInfo) -> {
            gisBucket.put(geoInfo.getGisId(), geoInfo);
        });

        // Iterating the many-to-many relations,
        // adding geoinfo to the correct observations
        gisObservationTimeSeriesIds.forEach((gisOtsIds) -> {
            Integer gisId = (Integer) gisOtsIds[0];
            Integer observationId = (Integer) gisOtsIds[1];
            obsBucket.get(observationId).addGeoInfo(gisBucket.get(gisId));
        });
    }

    public ObservationTimeSeries getObservationTimeSeries(Integer id) {
        ObservationTimeSeries ots = em.find(ObservationTimeSeries.class, id);
        if (ots != null) {
            ots.setGeoInfoList(this.getGeoInfoForObservationTimeSeries(ots));
            ots.setUser(em.find(VipsLogicUser.class, ots.getUserId()));
            if (ots.getLastModifiedBy() != null) {
                ots.setLastModifiedByUser(em.find(VipsLogicUser.class, ots.getLastModifiedBy()));
            }
        }
        return ots;
    }

    public List<Gis> getGeoInfoForObservationTimeSeries(ObservationTimeSeries ots) {
        List<Integer> gisIds = em.createNativeQuery("SELECT gis_id FROM public.gis_observation_time_series WHERE observation_time_series_id = :observationTimeSeriesId")
                .setParameter("observationTimeSeriesId", ots.getObservationTimeSeriesId())
                .getResultList();

        if (gisIds != null && !gisIds.isEmpty()) {
            return em.createNamedQuery("Gis.findByGisIds", Gis.class)
                    .setParameter("gisIds", gisIds)
                    .getResultList();
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * @param ots the observation time series
     * @return The merged object
     */
    public ObservationTimeSeries storeObservationTimeSeries(ObservationTimeSeries ots) {
        ObservationTimeSeries mergedOts = em.merge(ots);
        em.createNativeQuery("DELETE FROM public.gis_observation_time_series WHERE observation_time_series_id=:observationTimeSeriesId")
                .setParameter("observationTimeSeriesId", mergedOts.getObservationTimeSeriesId())
                .executeUpdate();
        em.createNativeQuery("DELETE FROM public.gis where gis_id IN (SELECT gis_id FROM public.gis_observation_time_series WHERE observation_time_series_id=:observationTimeSeriesId)")
                .setParameter("observationTimeSeriesId", mergedOts.getObservationTimeSeriesId())
                .executeUpdate();
        // Then persist the new ones
        if (ots.getGeoInfoList() != null && !ots.getGeoInfoList().isEmpty()) {
            ots.getGeoInfoList().forEach((gis) -> {
                em.persist(gis);
            });
            // Prepare query for inserting gis_observation_time_series
            Query q = em.createNativeQuery("INSERT INTO public.gis_observation_time_series(gis_id,observation_time_series_id) VALUES(:gisId,:observationTimeSeriesId)")
                    .setParameter("observationTimeSeriesId", mergedOts.getObservationTimeSeriesId());
            // Add gis_id and execute query
            ots.getGeoInfoList().forEach((gis) -> {
                q.setParameter("gisId", gis.getGisId()).executeUpdate();
            });
        }
        // The GisObservations are not included in the merged object, so we should add them
        mergedOts.setGeoInfoList(this.getGeoInfoForObservationTimeSeries(mergedOts));
        return mergedOts;
    }

    public void deleteObservationTimeSeries(Integer id) {
        ObservationTimeSeries observationTimeSeries = em.find(ObservationTimeSeries.class, id);
        if (observationTimeSeries != null) {
            observationBean.deleteObservationsForObservationTimeSeries(observationTimeSeries);
            em.remove(observationTimeSeries);
        }
    }

    /**
     * Enrich given list of observation time series with point of interest information
     *
     * @param otsList The list of observation time series to enrich
     */
    public void enrichObservationTimeSeriesListWithPointOfInterest(List<ObservationTimeSeries> otsList) {
        Set<Integer> locationPoiIds = new HashSet<>();
        otsList.stream().filter((o) -> (o.getLocationPointOfInterestId() != null)).forEach((o) -> {
            locationPoiIds.add(o.getLocationPointOfInterestId());
        });
        if (locationPoiIds.isEmpty()) {
            return;
        }
        List<PointOfInterest> pois = pointOfInterestBean.getPois(locationPoiIds);
        Map<Integer, PointOfInterest> mappedPois = new HashMap<>();
        pois.stream().forEach((poi) -> {
            mappedPois.put(poi.getPointOfInterestId(), poi);
        });
        otsList.stream().filter((o) -> (o.getLocationPointOfInterestId() != null)).forEach((o) -> {
            o.setLocationPointOfInterest(mappedPois.get(o.getLocationPointOfInterestId()));
        });
    }

    public void enrichObservationTimeSeriesWithPointOfInterest(ObservationTimeSeries ots) {
        if (ots == null || ots.getLocationPointOfInterestId() == null) {
            return;
        }
        ots.setLocationPointOfInterest(pointOfInterestBean.getPointOfInterest(ots.getLocationPointOfInterestId()));
    }

    /**
     * Enrich given list of observation time series with user information
     *
     * @param otsList The list of observation time series to enrich
     */
    private void enrichObservationTimeSeriesListWithObservers(List<ObservationTimeSeries> otsList) {
        Set<Integer> userIds = new HashSet<>();
        otsList.stream().filter((o) -> (o.getUserId() != null)).forEach((o) -> {
            userIds.add(o.getUserId());
        });
        if (userIds.isEmpty()) {
            return;
        }
        List<VipsLogicUser> users = userBean.getUsers(userIds);
        Map<Integer, VipsLogicUser> mappedUsers = new HashMap<>();
        users.stream().forEach((user) -> {
            mappedUsers.put(user.getUserId(), user);
        });
        otsList.stream().filter((o) -> (o.getUserId() != null)).forEach((o) -> {
            o.setUser(mappedUsers.get(o.getUserId()));
        });
    }
}
