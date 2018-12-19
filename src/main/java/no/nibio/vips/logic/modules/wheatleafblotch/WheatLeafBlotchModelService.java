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

package no.nibio.vips.logic.modules.wheatleafblotch;

import com.webcohesion.enunciate.metadata.Facet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import no.nibio.vips.entity.ModelConfiguration;
import no.nibio.vips.entity.Result;
import no.nibio.vips.entity.WeatherObservation;
import no.nibio.vips.logic.entity.Organism;
import no.nibio.vips.logic.entity.Organization;
import no.nibio.vips.logic.entity.PointOfInterestWeatherStation;
import no.nibio.vips.logic.entity.Preparation;
import no.nibio.vips.logic.util.RunModelException;
import no.nibio.vips.logic.util.SessionControllerGetter;
import no.nibio.vips.logic.util.SystemTime;
import no.nibio.vips.observation.ObservationImpl;
import no.nibio.vips.pestmanagement.SprayingImpl;
import no.nibio.vips.util.ParseRESTParamUtil;
import no.nibio.vips.util.WeatherElements;
import no.nibio.vips.util.weather.WeatherDataSourceException;
import no.nibio.vips.util.weather.WeatherDataSourceUtil;

/**
 * @copyright 2018 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Path("rest/wheatleafblotchmodel")
@Facet("restricted")
public class WheatLeafBlotchModelService {
    private final static String VIPSCOREMANAGER_URL = System.getProperty("no.nibio.vips.logic.VIPSCOREMANAGER_URL");
    
    @PersistenceContext(unitName="VIPSLogic-PU")
    EntityManager em;

    @GET
    @Path("wheatvarieties/{organizationId}")
    @Produces("application/json;charset=UTF-8")
    public Response getWheatVarieties(@PathParam("organizationId") Integer organizationId)
    {
        try
        {
            Organization o = em.find(Organization.class, organizationId);
            List<ResistanceFactor> resistanceFactors = em.createNamedQuery("ResistanceFactor.findByOrganizationId")
                .setParameter("organizationId", o).getResultList();
            
            List<Organism> wheatTypes = em.createNativeQuery("SELECT * FROM public.organism WHERE parent_organism_id IN ("
                    + "SELECT organism_id FROM public.organism WHERE latin_name='Triticum'"
                    + ")", Organism.class).getResultList();
            
            List<Integer> varietyIds = new ArrayList<>();
            for(ResistanceFactor f:resistanceFactors)
            {
                varietyIds.add(f.getCropOrganismId());
            }
            
            List<Organism> wheatVarieties = new ArrayList<>();
            
            if(varietyIds.size() > 0)
            {
                wheatVarieties = em.createNamedQuery("Organism.findByOrganismIds")
                        .setParameter("organismIds", varietyIds).getResultList();
                for(Organism variety:wheatVarieties)
                {
                    for(ResistanceFactor f:resistanceFactors)
                    {
                        if(f.getCropOrganismId().equals(variety.getOrganismId()))
                        {
                            variety.getExtraProperties().put("factors", f);
                        }
                    }
                    for(Organism type:wheatTypes)
                    {
                        if(variety.getParentOrganismId().equals(type.getOrganismId()))
                        {
                            variety.getExtraProperties().put("type", type);
                        }
                    }
                }
            }
            return Response.ok().entity(wheatVarieties).build();
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
                                    + "SELECT preparation_id FROM wheatleafb.wheatleafblotch_preparation_effect_factor"
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
            @QueryParam("wheatType") Integer wheatType,
            @QueryParam("timeZone") String timeZoneStr,
            @QueryParam("weatherStationId") Integer weatherStationId,
            @QueryParam("sowingDate") String sowingDateStr,
            @QueryParam("cropId") Integer cropOrganismId,
            @QueryParam("observationDate") String observationDateStr,
            @QueryParam("observationValue") String observationValueStr,
            @QueryParam("sameCropAsLastSeason") String sameCropAsLastSeasonStr,
            @QueryParam("plowed") String plowedStr,
            @QueryParam("sprayingDate") String sprayingDateStr,
            @QueryParam("preparationId1") String preparationId1Str,
            @QueryParam("preparationDose1") String preparationDose1Str,
            @QueryParam("preparationId2") String preparationId2Str,
            @QueryParam("preparationDose2") String preparationDose2Str,
            @QueryParam("preparationId3") String preparationId3Str,
            @QueryParam("preparationDose3") String preparationDose3Str
    )
    {
        // Some parsing needed...
        ParseRESTParamUtil parseUtil = new ParseRESTParamUtil();
        TimeZone timeZone = TimeZone.getTimeZone(timeZoneStr);
        Date sowingDate = parseUtil.parseISODate(sowingDateStr, timeZone);
        Date observationDate = parseUtil.parseISODateTime(observationDateStr, timeZone);
        // In case it's just a date string
        if(observationDate == null)
        {
            observationDate = parseUtil.parseISODate(observationDateStr, timeZone);
        }
        Double observationValue = parseUtil.parseDouble(observationValueStr);
        Boolean sameCropAsLastSeason = parseUtil.parseCheckbox(sameCropAsLastSeasonStr);
        Boolean plowed =  parseUtil.parseCheckbox(plowedStr);
        Date sprayingDate = parseUtil.parseISODate(sprayingDateStr, timeZone);
        Integer preparationId1 = parseUtil.parseInteger(preparationId1Str);
        Double preparationDose1 = parseUtil.parseDouble(preparationDose1Str);
        Integer preparationId2 = parseUtil.parseInteger(preparationId2Str);
        Double preparationDose2 = parseUtil.parseDouble(preparationDose2Str);
        Integer preparationId3 = parseUtil.parseInteger(preparationId3Str);
        Double preparationDose3 = parseUtil.parseDouble(preparationDose3Str);
        
        // Build model configuration
        ModelConfiguration config = new ModelConfiguration();
        config.setModelId("WLEAFBLTCH");
        // Get weather data from weather station
        PointOfInterestWeatherStation weatherStation = em.find(PointOfInterestWeatherStation.class, weatherStationId);
        WeatherDataSourceUtil wsdUtil = new WeatherDataSourceUtil();
        
        String[] parameterList;
        
        // End date for weather data depends on season
        // We try to add 5 months to the sowing date. If that's in the future,
        // We add 10 days to today
        Date dateOfFirstWeatherData;
        Date dateOfLastWeatherData;
        Calendar cal = Calendar.getInstance(timeZone);
        cal.setTime(sowingDate);
        // If winter wheat, the sowing date is after August 1st. We get weather 
        // data from March 1st the following year. And we need soil temperature
        if(cal.get(Calendar.MONTH) > Calendar.JULY)
        {
            cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) + 1);
            cal.set(Calendar.MONTH, Calendar.MARCH);
            cal.set(Calendar.DATE, 1);
            parameterList = new String[]{
                        WeatherElements.TEMPERATURE_MEAN,
                        WeatherElements.PRECIPITATION,
                        WeatherElements.SOIL_TEMPERATURE_10CM_MEAN
                    };
        }
        else
        {
            cal.add(Calendar.MONTH, -1);
            parameterList = new String[]{
                        WeatherElements.TEMPERATURE_MEAN,
                        WeatherElements.PRECIPITATION
                    };
        }
        dateOfFirstWeatherData = cal.getTime();
        cal.add(Calendar.MONTH, 6);
        Date fiveMonthsAfterSowingDate = cal.getTime();
        if(fiveMonthsAfterSowingDate.after(SystemTime.getSystemTime()))
        {
            cal.setTime(SystemTime.getSystemTime());
            cal.add(Calendar.DATE, 10);
            dateOfLastWeatherData = cal.getTime();
        }
        else
        {
            dateOfLastWeatherData = fiveMonthsAfterSowingDate;
        }

        List<WeatherObservation> observations;
        try {
             observations = wsdUtil.getWeatherObservations(
                    weatherStation,
                    WeatherObservation.LOG_INTERVAL_ID_1H,
                    parameterList,
                    dateOfFirstWeatherData, 
                    dateOfLastWeatherData
            );
        } catch (WeatherDataSourceException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
        
        if(observations == null || observations.isEmpty())
        {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Could not find weather data for weather station with id=" + weatherStationId).build();
        }
        
        // Mandatory parameters
        config.setConfigParameter("wheatType", wheatType);
        config.setConfigParameter("observations", observations);
        config.setConfigParameter("timeZone", timeZone.getID());
        config.setConfigParameter("sowingDate", sowingDateStr);
        config.setConfigParameter("plowed", plowed);
        config.setConfigParameter("previousSeasonCropType", sameCropAsLastSeason ? 1 : 2 );
        // Optional parameters
        if(observationDate != null && observationValue != null)
        {
            ObservationImpl observation = new ObservationImpl();
            observation.setName("Septoria tritici");
            observation.setObservationData("{\"percentInfectedLeaves\": " + observationValue + "}");
            observation.setTimeOfObservation(observationDate);
            config.setConfigParameter("observation", observation);
        }
        if(sprayingDate != null && preparationId1 != null && preparationDose1 != null)
        {
            SprayingImpl spraying = new SprayingImpl();
            spraying.setSprayingDate(sprayingDate);
            WheatLeafBlotchPreparationEffectFactor preparationEffectFactor1 = em.createNamedQuery("WheatLeafBlotchPreparationEffectFactor.findByPreparationId", WheatLeafBlotchPreparationEffectFactor.class)
                    .setParameter("preparationId", preparationId1).getSingleResult();
            WheatLeafBlotchPreparationEffectFactor preparationEffectFactor2 = null;
            WheatLeafBlotchPreparationEffectFactor preparationEffectFactor3 = null;
            Integer denominator = 1;
            if(preparationId2 != null && preparationDose2 != null)
            {
                preparationEffectFactor2 = em.createNamedQuery("WheatLeafBlotchPreparationEffectFactor.findByPreparationId", WheatLeafBlotchPreparationEffectFactor.class)
                    .setParameter("preparationId", preparationId2).getSingleResult();
                denominator++;
            }
            if(preparationId3 != null && preparationDose3 != null)
            {
                preparationEffectFactor3 = em.createNamedQuery("WheatLeafBlotchPreparationEffectFactor.findByPreparationId", WheatLeafBlotchPreparationEffectFactor.class)
                    .setParameter("preparationId", preparationId3).getSingleResult();
                denominator++;
            }
            spraying.setSprayingEffectFactor(
                    (1 - (preparationDose1 * preparationEffectFactor1.getFactorValue() / preparationEffectFactor1.getFullDosisMlDaa())
                    + (preparationEffectFactor2 != null ? (1 - (preparationDose2 * preparationEffectFactor2.getFactorValue() / preparationEffectFactor2.getFullDosisMlDaa())) : 0.0 )
                    + (preparationEffectFactor3 != null ? (1 - (preparationDose3 * preparationEffectFactor3.getFactorValue() / preparationEffectFactor3.getFullDosisMlDaa())) : 0.0)
                    ) / denominator
            );
            //System.out.println("sprayingEffectFactor=" + spraying.getSprayingEffectFactor());
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
