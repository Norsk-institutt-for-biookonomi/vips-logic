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

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Data;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.ExtendedData;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import no.nibio.vips.logic.entity.ExternalResource;
import no.nibio.vips.logic.entity.ExternalResourceType;
import no.nibio.vips.logic.entity.Organization;
import no.nibio.vips.logic.entity.PointOfInterest;
import no.nibio.vips.logic.entity.PointOfInterestExternalResource;
import no.nibio.vips.logic.entity.PointOfInterestExternalResourcePK;
import no.nibio.vips.logic.entity.PointOfInterestType;
import no.nibio.vips.logic.entity.PointOfInterestWeatherStation;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.entity.WeatherStationDataSource;
import no.nibio.vips.logic.util.GISEntityUtil;
import no.nibio.vips.logic.util.Globals;
import no.nibio.vips.logic.util.SessionControllerGetter;
import no.nibio.web.forms.FormField;


/**
 * Handles transactions for POIs
 * @copyright 2013 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@LocalBean
@Stateless
public class PointOfInterestBean {
    @PersistenceContext(unitName="VIPSLogic-PU")
    EntityManager em;
    
    @EJB
    ForecastBean forecastBean;
    @EJB
    ObservationBean observationBean;
    
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
    public List<PointOfInterestWeatherStation> getWeatherstationsForUser(VipsLogicUser user)
    {
        Query q = em.createNamedQuery("PointOfInterestWeatherStation.findByUserId");
        q.setParameter("userId", user);
        try
        {
            return q.getResultList();
        }
        catch(NoResultException ex)
        {
            return null;
        }
    }
    
    /**
     * Returns POIs of all types for a given user
     * @param user
     * @return 
     */
    public List<PointOfInterest> getPoisForUser(VipsLogicUser user) {
        Query q = em.createNamedQuery("PointOfInterest.findByUserId");
        q.setParameter("userId", user);
        try
        {
            return q.getResultList();
        }
        catch(NoResultException ex)
        {
            return null;
        }
    }
    
    public List<PointOfInterestWeatherStation> getWeatherstationsForUserOrganization(VipsLogicUser user)
    {
        Query q = em.createNamedQuery("PointOfInterestWeatherStation.findByOrganizationId");
        q.setParameter("organizationId", user.getOrganizationId());
        try
        {
            return q.getResultList();
        }
        catch(NoResultException ex)
        {
            return null;
        }
    }
    
    public List<PointOfInterestWeatherStation> getAllWeatherStations()
    {
        Query q = em.createNamedQuery("PointOfInterestWeatherStation.findAll");
        return q.getResultList();
    }
    
    public List<PointOfInterestWeatherStation> getAllWeatherStations(Boolean active)
    {
        // Default is to find active
        active = active == null ? true : active;
        Query q = em.createNamedQuery("PointOfInterestWeatherStation.findAllByActivity")
                .setParameter("active", active);
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

    public Kml getPoisForOrganization(Integer organizationId, Integer excludePoiId, Integer highlightPoiId, String serverName, ResourceBundle i18nBundle, Integer pointOfInterestTypeId) {
        String iconPath = Globals.PROTOCOL + "://" + serverName + "/public/images/";
        
        List<PointOfInterest> pois = new ArrayList<>();
        if(organizationId.equals(-1))
        {
            List<Organization> organizations = em.createNamedQuery("Organization.findAllTopLevelOrganizations").getResultList();
            for(Organization organization:organizations)
            {
                if(pointOfInterestTypeId != null && pointOfInterestTypeId == PointOfInterestType.POINT_OF_INTEREST_TYPE_WEATHER_STATION)
                {
                    pois.addAll(this.getWeatherstationsForOrganization(organization,true));
                }
                else
                {
                    pois.addAll(this.getPoisForOrganization(organization));
                }
            }
        }
        else
        {
            if(pointOfInterestTypeId != null && pointOfInterestTypeId == PointOfInterestType.POINT_OF_INTEREST_TYPE_WEATHER_STATION)
            {
                pois.addAll(this.getWeatherstationsForOrganization(em.find(Organization.class, organizationId),true));
            }
            else
            {
                pois.addAll(this.getPoisForOrganization(em.find(Organization.class, organizationId)));
            }
        }
 
        
        // Initialization
        final Kml kml = KmlFactory.createKml();
        final Document document = kml.createAndSetDocument()
        .withName("Weather stations").withDescription("Weather stations for ");
        
        document.createAndAddStyle()
            .withId("weatherstation_icon")
        .createAndSetIconStyle()
                .withScale(0.55)
                .createAndSetIcon()
                    .withHref(iconPath + "dot_blue.png");
        
        document.createAndAddStyle()
            .withId("weatherstation_icon_highlighted")
        .createAndSetIconStyle()
                .withScale(0.55)
                .createAndSetIcon()
                    .withHref(iconPath + "Map_pin_icon_green_small.png");
        
        
        
        String description = "";
        GISEntityUtil gisUtil = new GISEntityUtil();
        for(PointOfInterest poi:pois)
        {
            if(excludePoiId != null && excludePoiId.equals(poi.getPointOfInterestId()))
            {
                continue;
            }
            
            String styleUrl = "#weatherstation_icon"
                            +   (highlightPoiId != null && highlightPoiId.equals(poi.getPointOfInterestId()) ? "_highlighted" :"");
            
            String infoUriValue = "";
            if(poi instanceof PointOfInterestWeatherStation)
            {
                description = i18nBundle.getString("dataSourceName") + 
                                ": <a href=\"" + ((PointOfInterestWeatherStation) poi).getWeatherStationDataSourceId().getUri() + "\" target=\"new\">" 
                                +  ((PointOfInterestWeatherStation) poi).getWeatherStationDataSourceId().getName() 
                                + "</a>";
                String infoUriExpression = ((PointOfInterestWeatherStation) poi).getWeatherStationDataSourceId().getInfoUriExpression();
                if(!infoUriExpression.isEmpty())
                {
                    infoUriValue = String.format(infoUriExpression, ((PointOfInterestWeatherStation) poi).getWeatherStationRemoteId());
                }
            }
            Data infoUri = new Data(infoUriValue);
            infoUri.setName("infoUri");
            List<Data> dataList = new ArrayList<>();
            dataList.add(infoUri);
            ExtendedData extendedData = document.createAndSetExtendedData()
                    .withData(dataList);
            
            final Placemark placemark = document.createAndAddPlacemark()
            .withName(poi.getName())
            .withDescription(description)
            .withStyleUrl(styleUrl)
            .withId(poi.getPointOfInterestId().toString())
            .withExtendedData(extendedData);
            
            
            final Point point = placemark.createAndSetPoint();
            List<Coordinate> coord = point.createAndSetCoordinates();
            coord.add(gisUtil.getKMLCoordinateFromJTSCoordinate(poi.getGisGeom().getCoordinate()));
        }
        return kml;
    }

    public List<WeatherStationDataSource> getWeatherStationDataSources() {
        return em.createNamedQuery("WeatherStationDataSource.findAll").getResultList();
    }

    public PointOfInterestWeatherStation storeWeatherStation(PointOfInterestWeatherStation weatherStation) {
        weatherStation = em.merge(weatherStation);
        return weatherStation;
    }  

    /**
     * 
     * @param organization
     * @param active
     * @return 
     */
    public List<PointOfInterestWeatherStation> getWeatherstationsForOrganization(Organization organization, Boolean active) {
        
        if(organization == null)
        {
            return new ArrayList<>();
        }
        // Avoid nulls
        active = active == null ? true : active;
        
        List<PointOfInterestWeatherStation> retVal = em.createNamedQuery("PointOfInterestWeatherStation.findByActivityAndOrganizationId", PointOfInterestWeatherStation.class)
                                                .setParameter("organizationId", organization)
                                                .setParameter("active", active)
                                                .getResultList();
        for(Organization subdivision:organization.getOrganizationSet())
        {
            retVal.addAll(em.createNamedQuery("PointOfInterestWeatherStation.findByOrganizationId", PointOfInterestWeatherStation.class)
                                .setParameter("organizationId", subdivision)
                                .getResultList()
            );
        }
        return retVal;
    }

    public List<ExternalResource> getUnusedExternalResourcesForPointOfInterest(PointOfInterest weatherStation) {
        StringBuilder sql = new StringBuilder()
                .append("SELECT * FROM external_resource ")
                .append("WHERE external_resource_type_id=")
                .append(ExternalResourceType.FORECAST_PROVIDER).append(" \n");
        if(weatherStation.getPointOfInterestExternalResourceSet() == null  || weatherStation.getPointOfInterestExternalResourceSet().isEmpty())
        {
            return em.createNativeQuery(sql.toString(), ExternalResource.class).getResultList();

        }
        else
        {
            sql.append("AND external_resource_id NOT IN (:externalResourceIds)");
            List<Integer> ids = new ArrayList<>();
            for(PointOfInterestExternalResource poier : weatherStation.getPointOfInterestExternalResourceSet())
            {
                ids.add(poier.getExternalResource().getExternalResourceId());
            }
            
            Query q = em.createNativeQuery(sql.toString(), ExternalResource.class);
            q.setParameter("externalResourceIds", ids);
            return q.getResultList();
        }
    }

    public void storePointOfInterestExternalResource(PointOfInterestExternalResource poiExternalResource) {
        em.merge(poiExternalResource);
    }
    
    public void deleteWeatherStation(Integer pointOfInterestId)
    {
        PointOfInterestWeatherStation weatherStation = em.find(PointOfInterestWeatherStation.class, pointOfInterestId);
        forecastBean.deleteForecastConfigurationsForWeatherStation(weatherStation);
        em.remove(weatherStation);
    }

    public void deletePointOfInterestExternalResource(Integer pointOfInterestId, Integer externalResourceId) {
        PointOfInterestExternalResource resource = em.find(PointOfInterestExternalResource.class, new PointOfInterestExternalResourcePK(pointOfInterestId,externalResourceId));
        if(resource != null)
        {
            em.remove(resource);
        }
    }

    public List<PointOfInterest> getAllPois() {
        return em.createNamedQuery("PointOfInterest.findAll", PointOfInterest.class).getResultList();
    }

    public List<PointOfInterest> getPoisForOrganization(Organization organization) {
        return em.createNamedQuery("PointOfInterest.findByOrganizationId", PointOfInterest.class)
                .setParameter("organizationId", organization)
                .getResultList();
    }

    public PointOfInterest storePoi(PointOfInterest poi) {
        if(poi.getPointOfInterestId() == null)
        {
            em.persist(poi);
            return poi;
        }
        else
        {
            return em.merge(poi);
        }
    }

    public void deletePoi(Integer pointOfInterestId) {
        PointOfInterest poi = em.find(PointOfInterest.class, pointOfInterestId);
        forecastBean.deleteForecastConfigurationsForLocation(poi);
        observationBean.deleteObservationsForLocation(poi);
        em.remove(poi);
    }

    public List<PointOfInterest> getRelevantPointOfInterestsForUser(VipsLogicUser user) {
        List<PointOfInterest> retVal;
        // Super users get all locations
        if(user.isSuperUser())
        {
            retVal = em.createNamedQuery("PointOfInterest.findAll", PointOfInterest.class).getResultList();
        }
        // Organization admins get all locations for organization
        else if(user.isOrganizationAdmin())
        {
            retVal = em.createNamedQuery("PointOfInterest.findByOrganizationId", PointOfInterest.class)
                    .setParameter("organizationId", user.getOrganizationId())
                    .getResultList();
        }
        // Regular users get all locations that they have created AND locations shared via groups
        else
        {
            retVal = em.createNamedQuery("PointOfInterest.findByUserId", PointOfInterest.class)
                    .setParameter("userId", user)
                    .getResultList();
            
            // Getting all group membership pois
            try
            {
                // Need to do this in two steps because Hibernate struggles
                // with mapping to subclasses like PointOfInterestWeatherStation when
                // using native query
                List<Integer> memberPoiIds = em.createNativeQuery(
                        "SELECT point_of_interest_id FROM public.point_of_interest "
                        + " WHERE point_of_interest_id IN ("
                                + " SELECT point_of_interest_id FROM public.organization_group_poi "
                                + " WHERE organization_group_id IN ("
                                    + " SELECT organization_group_id FROM public.organization_group_user "
                                    + " WHERE user_id=:userId"
                                    + ")"
                                + ")"
                        + " AND user_id <> :userId"
                        )
                        .setParameter("userId", user.getUserId())
                        .getResultList();
                if(memberPoiIds != null && memberPoiIds.size() > 0)
                {
                    retVal.addAll(em.createNamedQuery("PointOfInterest.findByPointOfInterestIds")
                            .setParameter("pointOfInterestIds", memberPoiIds)
                            .getResultList()
                    );
                }
            }
            catch(NoResultException ex) {}
            // Getting all weather stations for user's organization. Need to avoid
            // double catching of privately owned weather station
            retVal.addAll(this.getWeatherstationsForOrganization(user.getOrganizationId(), Boolean.TRUE)
                    .stream()
                    .filter(weatherStation -> ! weatherStation.getUserId().getUserId().equals(user.getUserId()))
                    .collect(Collectors.toList())
            );
            
        }
        Collections.sort(retVal);
        return retVal;
    }

    public  List<Integer> getPoiGroupIds(PointOfInterest poi) {
        if(poi.getPointOfInterestId() != null)
        {
            return em.createNativeQuery("SELECT organization_group_id FROM public.organization_group_poi "
                    + "WHERE point_of_interest_id = :pointOfInterestId")
                    .setParameter("pointOfInterestId", poi.getPointOfInterestId())
                    .getResultList();
        }
        else
        {
            return new ArrayList<>();
        }
    }

    public void storePointOfInterestOrganizationGroupIds(PointOfInterest poi, String[] organizationGroupIds) {
        // First delete all current group memberships
        em.createNativeQuery("DELETE FROM public.organization_group_poi WHERE point_of_interest_id=:pointOfInterestId")
                .setParameter("pointOfInterestId", poi.getPointOfInterestId())
                .executeUpdate();
        
        if(organizationGroupIds != null)
        {
            Query q = em.createNativeQuery("INSERT INTO public.organization_group_poi (organization_group_id, point_of_interest_id) "
                    + "VALUES(:organizationGroupId, :pointOfInterestId)")
                    .setParameter("pointOfInterestId", poi.getPointOfInterestId());
            // Then add
            for(String groupIdStr:organizationGroupIds)
            {
                try {
                    Integer groupId = Integer.valueOf(groupIdStr);
                    q.setParameter("organizationGroupId", groupId);
                    q.executeUpdate();
                }
                catch(NumberFormatException ex)
                {
                    // Continue
                }
            }
        }
    }

    public List<PointOfInterest> getPois(Set<Integer> locationPointOfInterestIds) {
        return em.createNamedQuery("PointOfInterest.findByPointOfInterestIds")
                .setParameter("pointOfInterestIds", locationPointOfInterestIds)
                .getResultList();
    }

    public PointOfInterest getPointOfInterest(String poiName) {
        try
        {
            return em.createNamedQuery("PointOfInterest.findByNameCaseInsensitive", PointOfInterest.class)
                    .setParameter("name", poiName)
                    .getSingleResult();
        }
        catch(NoResultException ex)
        {
            return null;
        }
                
    }

    public WeatherStationDataSource getWeatherStationDataSource(String dataSourceName){
        return em.createNamedQuery("WeatherStationDataSource.findByName", WeatherStationDataSource.class)
                .setParameter("name", dataSourceName)
                .getSingleResult();
    }
    
    

    /**
     * Utility method to find the nearest weather station
     * @param location
     * @return 
     */
    public PointOfInterestWeatherStation findClosestWeatherStation(com.vividsolutions.jts.geom.Coordinate location)
    {
        return this.findClosestWeatherStation(location.x, location.y);
        
    }
    
    
    
    public PointOfInterestWeatherStation findClosestWeatherStation(Double longitude, Double latitude)
    {
        List<PointOfInterestWeatherStation> weatherStations = em.createNamedQuery("PointOfInterestWeatherStation.findAll", PointOfInterestWeatherStation.class)
                .getResultList();
        return this.findClosestWeatherStation(longitude, latitude, weatherStations);
    }
    
    /**
     * Utility method to find the nearest weather station
     * @param location
     * @return 
     */
    public PointOfInterestWeatherStation findClosestWeatherStation(com.vividsolutions.jts.geom.Coordinate location, List<PointOfInterestWeatherStation> weatherStations)
    {
        return this.findClosestWeatherStation(location.x, location.y, weatherStations);
        
    }
    
    /**
     * Utility method to find the nearest weather station
     * @param longitude
     * @param latitude
     * @return 
     */
    public PointOfInterestWeatherStation findClosestWeatherStation(Double longitude, Double latitude, List<PointOfInterestWeatherStation> weatherStations)
    {
        
        
        PointOfInterestWeatherStation retVal = null;
        Double minimumDistance = null; // km
        
        for(PointOfInterestWeatherStation ws:weatherStations)
        {
            Double distance = this.calculateDistance(latitude, longitude, ws.getLatitude(), ws.getLongitude());
            if(minimumDistance == null || minimumDistance >= distance)
            {
                minimumDistance = distance;
                retVal = ws;
            }
        }
        return retVal;
    }
    
    /**
     * Based on <a href="http://www.movable-type.co.uk/scripts/latlong.html">this</a>. Explanation:
     * <pre>
     * This uses the "haversine" formula to calculate the great-circle distance between two points ? that is, the shortest distance over the earth?s surface ? giving an ?as-the-crow-flies? distance between the points (ignoring any hills, of course!).
        Haversine formula:
                a = sin²(∆lat/2) + cos(lat1).cos(lat2).sin²(∆long/2)
                c = 2 * atan2(√a,√(1-a))
                d = R*c
        where R is earth's radius (mean radius = 6,371km);
        Note that angles need to be in radians to pass to trig functions!
     * </pre>
     * @param lat1 WGS84 latitude of point 1 
     * @param long1 WGS84 longitude of point 1
     * @param lat2 WGS84 latitude of point 2
     * @param long2 WGS84 longitude of point 2
     * @return distance in km
     */
    public Double calculateDistance(Double lat1, Double long1, Double lat2, Double long2)
    {
        Integer R = 6371; // km
        Double dLat = Math.toRadians(lat2-lat1);
        Double dLon = Math.toRadians(long2-long1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        Double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
        Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2); 
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
        Double d = R * c;
        
        return d;
    }
    
}
