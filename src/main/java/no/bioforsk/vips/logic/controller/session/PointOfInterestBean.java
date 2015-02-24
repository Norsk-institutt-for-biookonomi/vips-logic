/*
 * Copyright (c) 2014 Bioforsk <http://www.bioforsk.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the Bioforsk Open Source License as published by 
 * Bioforsk, either version 1 of the License, or (at your option) any
 * later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Bioforsk Open Source License for more details.
 * 
 * You should have received a copy of the Bioforsk Open Source License
 * along with VIPSLogic.  If not, see <http://www.bioforsk.no/licenses/>.
 * 
 */

package no.bioforsk.vips.logic.controller.session;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import no.bioforsk.vips.logic.entity.ExternalResource;
import no.bioforsk.vips.logic.entity.ExternalResourceType;
import no.bioforsk.vips.logic.entity.Organization;
import no.bioforsk.vips.logic.entity.PointOfInterest;
import no.bioforsk.vips.logic.entity.PointOfInterestExternalResource;
import no.bioforsk.vips.logic.entity.PointOfInterestWeatherStation;
import no.bioforsk.vips.logic.entity.WeatherStationDataSource;
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

    public Kml getWeatherstationsForOrganization(Integer organizationId, Integer excludeWeatherStationId, Integer highlightWeatherStationId, String serverName, ResourceBundle i18nBundle) {
        String iconPath = "http://" + serverName + "/public/images/";
        
        List<PointOfInterestWeatherStation> weatherStations = new ArrayList<>();
        if(organizationId.equals(-1))
        {
            List<Organization> organizations = em.createNamedQuery("Organization.findAllTopLevelOrganizations").getResultList();
            for(Organization organization:organizations)
            {
                weatherStations.addAll(this.getWeatherstationsForOrganization(organization));
            }
        }
        else
        {
            weatherStations.addAll(this.getWeatherstationsForOrganization(em.find(Organization.class, organizationId)));
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
                    .withHref(iconPath + "anemometer_mono.png");
        
        
        
        String description = "";
        for(PointOfInterestWeatherStation weatherStation:weatherStations)
        {
            if(excludeWeatherStationId != null && excludeWeatherStationId.equals(weatherStation.getPointOfInterestId()))
            {
                continue;
            }
            
            String styleUrl = "#weatherstation_icon"
                            +   (highlightWeatherStationId != null && highlightWeatherStationId.equals(weatherStation.getPointOfInterestId()) ? "_highlighted" :"");
            
            if(weatherStation instanceof PointOfInterestWeatherStation)
            {
                description = i18nBundle.getString("dataSourceName") + 
                                ": <a href=\"" + weatherStation.getWeatherStationDataSourceId().getUri() + "\" target=\"new\">" 
                                +  weatherStation.getWeatherStationDataSourceId().getName() 
                                + "</a>";
            }
            final Placemark placemark = document.createAndAddPlacemark()
            .withName(weatherStation.getName())
            .withDescription(description)
            .withStyleUrl(styleUrl)
             .withId(weatherStation.getPointOfInterestId().toString());
            
            
            final Point point = placemark.createAndSetPoint();
            List<Coordinate> coord = point.createAndSetCoordinates();
            coord.add(new Coordinate(
                    weatherStation.getLongitude(),
                    weatherStation.getLatitude(),
                    weatherStation.getAltitude() != null ? weatherStation.getAltitude() : 0
            ));
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

    public List<PointOfInterestWeatherStation> getWeatherstationsForOrganization(Organization organization) {
        List<PointOfInterestWeatherStation> retVal = em.createNamedQuery("PointOfInterestWeatherStation.findByOrganizationId", PointOfInterestWeatherStation.class)
                                                .setParameter("organizationId", organization)
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
}
