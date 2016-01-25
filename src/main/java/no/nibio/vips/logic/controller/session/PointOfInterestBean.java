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
import java.util.List;
import java.util.ResourceBundle;
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
import no.nibio.vips.logic.entity.PointOfInterestWeatherStation;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.entity.WeatherStationDataSource;
import no.nibio.vips.logic.util.Globals;
import no.nibio.vips.logic.util.SessionControllerGetter;


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

    public Kml getWeatherstationsForOrganization(Integer organizationId, Integer excludeWeatherStationId, Integer highlightWeatherStationId, String serverName, ResourceBundle i18nBundle) {
        String iconPath = "http://" + serverName + "/public/images/";
        
        List<PointOfInterestWeatherStation> weatherStations = new ArrayList<>();
        if(organizationId.equals(-1))
        {
            List<Organization> organizations = em.createNamedQuery("Organization.findAllTopLevelOrganizations").getResultList();
            for(Organization organization:organizations)
            {
                weatherStations.addAll(this.getWeatherstationsForOrganization(organization,null));
            }
        }
        else
        {
            weatherStations.addAll(this.getWeatherstationsForOrganization(em.find(Organization.class, organizationId),null));
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
            
            String infoUriValue = "";
            if(weatherStation instanceof PointOfInterestWeatherStation)
            {
                description = i18nBundle.getString("dataSourceName") + 
                                ": <a href=\"" + weatherStation.getWeatherStationDataSourceId().getUri() + "\" target=\"new\">" 
                                +  weatherStation.getWeatherStationDataSourceId().getName() 
                                + "</a>";
                String infoUriExpression = ((PointOfInterestWeatherStation) weatherStation).getWeatherStationDataSourceId().getInfoUriExpression();
                if(!infoUriExpression.isEmpty())
                {
                    infoUriValue = String.format(infoUriExpression, ((PointOfInterestWeatherStation) weatherStation).getWeatherStationRemoteId());
                }
            }
            Data infoUri = new Data(infoUriValue);
            infoUri.setName("infoUri");
            List<Data> dataList = new ArrayList<>();
            dataList.add(infoUri);
            ExtendedData extendedData = document.createAndSetExtendedData()
                    .withData(dataList);
            
            final Placemark placemark = document.createAndAddPlacemark()
            .withName(weatherStation.getName())
            .withDescription(description)
            .withStyleUrl(styleUrl)
            .withId(weatherStation.getPointOfInterestId().toString())
            .withExtendedData(extendedData);
            
            
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

    public List<PointOfInterestWeatherStation> getWeatherstationsForOrganization(Organization organization, Boolean active) {
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
        SessionControllerGetter.getForecastBean().deleteForecastConfigurations(weatherStation);
        em.remove(weatherStation);
    }
}
