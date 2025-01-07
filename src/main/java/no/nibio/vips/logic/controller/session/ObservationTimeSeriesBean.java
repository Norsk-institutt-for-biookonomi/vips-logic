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

import java.util.*;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import no.nibio.vips.logic.entity.ObservationTimeSeries;
import no.nibio.vips.logic.entity.PointOfInterest;
import no.nibio.vips.logic.entity.VipsLogicUser;

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
        this.enrichObservationTimeSeriesListWithPointOfInterest(resultList);
        this.enrichObservationTimeSeriesListWithObservers(resultList);
        return resultList;
    }

    /**
     * Get observation time series with given id. Enrich object with user information before returning.
     * @param observationTimeSeriesId the id of the observation time series to retrieve
     * @return the observation time series with the given id
     */
    public ObservationTimeSeries getObservationTimeSeries(Integer observationTimeSeriesId) {
        ObservationTimeSeries ots = em.find(ObservationTimeSeries.class, observationTimeSeriesId);
        if (ots != null) {
            ots.setUser(em.find(VipsLogicUser.class, ots.getUserId()));
            if (ots.getLastModifiedBy() != null) {
                ots.setLastModifiedByUser(em.find(VipsLogicUser.class, ots.getLastModifiedBy()));
            }
        }
        return ots;
    }

    public List<ObservationTimeSeries> getObservationTimeSeriesList(Set<Integer> observationTimeSeriesIds) {
        return em.createNamedQuery("ObservationTimeSeries.findByObservationTimeSeriesIds")
                .setParameter("observationTimeSeriesIds", observationTimeSeriesIds)
                .getResultList();
    }

    /**
     * @param ots the observation time series
     * @return The merged object
     */
    public ObservationTimeSeries storeObservationTimeSeries(ObservationTimeSeries ots) {
        return em.merge(ots);
    }

    public void deleteObservationTimeSeries(Integer id) {
        ObservationTimeSeries observationTimeSeries = em.find(ObservationTimeSeries.class, id);
        if (observationTimeSeries != null) {
            // The app prevents deletion of time series with observations
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

    /**
     * Enrich given observation time series with point of interest information
     *
     * @param ots The observation time series to enrich
     */
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
