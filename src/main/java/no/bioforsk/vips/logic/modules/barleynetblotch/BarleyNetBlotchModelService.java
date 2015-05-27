/*
 * Copyright (c) 2015 NIBIO <http://www.nibio.no/>. 
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

package no.bioforsk.vips.logic.modules.barleynetblotch;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import no.bioforsk.vips.coremanager.service.ManagerResource;
import no.bioforsk.vips.entity.ModelConfiguration;
import no.bioforsk.vips.entity.Result;
import no.bioforsk.vips.entity.WeatherObservation;
import no.bioforsk.vips.logic.entity.Organism;
import no.bioforsk.vips.logic.entity.Organization;
import no.bioforsk.vips.logic.entity.PointOfInterestWeatherStation;
import no.bioforsk.vips.logic.entity.Preparation;
import no.bioforsk.vips.logic.util.RunModelException;
import no.bioforsk.vips.logic.util.SessionControllerGetter;
import no.bioforsk.vips.logic.util.SystemTime;
import no.bioforsk.vips.observation.ObservationImpl;
import no.bioforsk.vips.pestmanagement.SprayingImpl;
import no.bioforsk.vips.util.ParseRESTParamUtil;
import no.bioforsk.vips.util.WeatherElements;
import no.bioforsk.vips.util.weather.WeatherDataSourceException;
import no.bioforsk.vips.util.weather.WeatherDataSourceUtil;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

/**
 * @copyright 2015 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Path("rest/barleynetblotchmodel")
public class BarleyNetBlotchModelService {
    private final static String VIPSCOREMANAGER_URL = System.getProperty("no.bioforsk.vips.logic.VIPSCOREMANAGER_URL");
    
    @PersistenceContext(unitName="VIPSLogic-PU")
    EntityManager em;

    @GET
    @Path("barleyvarieties/{organizationId}")
    @Produces("application/json;charset=UTF-8")
    public Response getBarleyVarieties(@PathParam("organizationId") Integer organizationId)
    {
        try
        {
            Organization o = em.find(Organization.class, organizationId);
            List<Factors> barleyVarietyFactors = em.createNamedQuery("Factors.findByOrganizationId")
                .setParameter("organizationId", o).getResultList();
            
            List<Integer> varietyIds = new ArrayList<>();
            for(Factors f:barleyVarietyFactors)
            {
                varietyIds.add(f.getCropOrganismId());
            }
            
            List<Organism> barleyVarieties = new ArrayList<>();
            if(varietyIds.size() > 0)
            {
                barleyVarieties = em.createNamedQuery("Organism.findByOrganismIds")
                        .setParameter("organismIds", varietyIds).getResultList();
                for(Organism variety:barleyVarieties)
                {
                    for(Factors f:barleyVarietyFactors)
                    {
                        if(f.getCropOrganismId().equals(variety.getOrganismId()))
                        {
                            variety.getExtraProperties().put("factors", f);
                        }
                    }
                }
            }
            return Response.ok().entity(barleyVarieties).build();
        }
        catch(NoResultException ex)
        {
            return Response.ok().entity(ex.getMessage()).build();
        }
        
    }
    
    @GET
    @Path("preparations/{organizationId}")
    @Produces("application/json;charset=UTF-8")
    public Response getPreparations(@PathParam("organizationId") Integer organizationId)
    {
        try
        {
            // TODO: Get all organization children as well
            // Must get the preparations first
            List<Preparation> preparations = em.createNativeQuery(
                            "SELECT * FROM preparation "
                            + "WHERE organization_id = :organizationId "
                            + "AND preparation_id IN ("
                                    + "SELECT preparation_id FROM barleynetb.preparation_effect_factor"
                            + ")",
                    Preparation.class)
                    .setParameter("organizationId", organizationId)
                    .getResultList();
            return Response.ok().entity(preparations).build();
        }
        catch(NoResultException ex)
        {
            return Response.ok().entity(ex.getMessage()).build();
        }
    }
    
    @GET
    @Path("runmodel/{organizationId}")
    @Produces("application/json;charset=UTF-8")
    public Response runModel(
            @PathParam("organizationId") Integer organizationId,
            @QueryParam("timeZone") String timeZoneStr,
            @QueryParam("weatherStationId") Integer weatherStationId,
            @QueryParam("sowingDate") String sowingDateStr,
            @QueryParam("cropId") Integer cropOrganismId,
            @QueryParam("observationDate") String observationDateStr,
            @QueryParam("observationValue") String observationValueStr,
            @QueryParam("sameCropAsLastSeason") String sameCropAsLastSeasonStr,
            @QueryParam("plowed") String plowedStr,
            @QueryParam("sprayingDate") String sprayingDateStr,
            @QueryParam("preparationId") String preparationIdStr,
            @QueryParam("preparationDose") String preparationDoseStr
    )
    {
        // Some parsing needed...
        ParseRESTParamUtil parseUtil = new ParseRESTParamUtil();
        TimeZone timeZone = TimeZone.getTimeZone(timeZoneStr);
        Date sowingDate = parseUtil.parseISODate(sowingDateStr, timeZone);
        Date observationDate = parseUtil.parseISODateTime(observationDateStr, timeZone);
        Double observationValue = parseUtil.parseDouble(observationValueStr);
        Boolean sameCropAsLastSeason = parseUtil.parseCheckbox(sameCropAsLastSeasonStr);
        Boolean plowed =  parseUtil.parseCheckbox(plowedStr);
        Date sprayingDate = parseUtil.parseISODate(sprayingDateStr, timeZone);
        Integer preparationId = parseUtil.parseInteger(preparationIdStr);
        Double preparationDose = parseUtil.parseDouble(preparationDoseStr);
        
        // Build model configuration
        ModelConfiguration config = new ModelConfiguration();
        config.setModelId("BARLEYNETB");
        // Get weather data from weather station
        PointOfInterestWeatherStation weatherStation = em.find(PointOfInterestWeatherStation.class, weatherStationId);
        WeatherDataSourceUtil wsdUtil = new WeatherDataSourceUtil();
        Calendar cal = Calendar.getInstance(timeZone);
        cal.setTime(SystemTime.getSystemTime());
        cal.add(Calendar.DATE, 10);
        Date nearFuture = cal.getTime();
        List<WeatherObservation> observations;
        try {
             observations = wsdUtil.getWeatherObservations(
                    weatherStation,
                    WeatherObservation.LOG_INTERVAL_ID_1H,
                    new String[]{
                        WeatherElements.TEMPERATURE_MEAN,
                        WeatherElements.PRECIPITATION
                    },
                    sowingDate, 
                    nearFuture
            );
        } catch (WeatherDataSourceException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
        
        if(observations == null || observations.isEmpty())
        {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Could not find weather data for weather station with id=" + weatherStationId).build();
        }
        
        // Mandatory parameters
        config.setConfigParameter("observations", observations);
        config.setConfigParameter("timeZone", timeZone.getID());
        config.setConfigParameter("sowingDate", sowingDate);
        config.setConfigParameter("plowed", plowed);
        config.setConfigParameter("sameCropAsLastSeason", sameCropAsLastSeason);
        // Optional parameters
        if(observationDate != null && observationValue != null)
        {
            ObservationImpl observation = new ObservationImpl();
            observation.setName("Pyrenophora teres");
            observation.setObservedValue(observationValue);
            observation.setDenominator(100);
            config.setConfigParameter("observation", observation);
        }
        if(sprayingDate != null && preparationId != null && preparationDose != null)
        {
            SprayingImpl spraying = new SprayingImpl();
            spraying.setSprayingDate(sprayingDate);
            PreparationEffectFactor preparationEffectFactor = em.createNamedQuery("PreparationEffectFactor.findByPreparationId", PreparationEffectFactor.class)
                    .setParameter("preparationId", preparationId).getSingleResult();
            spraying.setSprayingEffectFactor(preparationEffectFactor.getFactorValue().doubleValue());
            config.setConfigParameter("spraying", spraying);
        }
        
        
        // Must get the VIPSCore user id for this organization
        Organization org = em.find(Organization.class, organizationId);
        Integer VIPSCoreUserId = org.getDefaultVipsCoreUserId();
        
        List<Result> results;
        try
        {
             results = SessionControllerGetter.getForecastBean().runForecast(config, VIPSCoreUserId);
        }
        catch(RunModelException ex)
        {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
        return Response.ok().entity(results).build();
    }
    
    
}
