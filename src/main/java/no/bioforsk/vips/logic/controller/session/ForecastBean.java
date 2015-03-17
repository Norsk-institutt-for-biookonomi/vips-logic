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

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Calendar;
import java.util.TimeZone;
import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Point;
import de.micromata.opengis.kml.v_2_2_0.Units;
import de.micromata.opengis.kml.v_2_2_0.Vec2;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import no.bioforsk.vips.coremanager.service.ManagerResource;
import no.bioforsk.vips.entity.ModelConfiguration;
import no.bioforsk.vips.entity.ModelRunRequest;
import no.bioforsk.vips.entity.Result;
import no.bioforsk.vips.logic.entity.ForecastConfiguration;
import no.bioforsk.vips.logic.entity.ForecastModelConfiguration;
import no.bioforsk.vips.logic.entity.ForecastResult;
import no.bioforsk.vips.logic.entity.ForecastSummary;
import no.bioforsk.vips.logic.entity.ForecastSummaryPK;
import no.bioforsk.vips.logic.entity.ModelInformation;
import no.bioforsk.vips.logic.entity.Organism;
import no.bioforsk.vips.logic.entity.Organization;
import no.bioforsk.vips.logic.entity.PointOfInterest;
import no.bioforsk.vips.logic.entity.PointOfInterestWeatherStation;
import no.bioforsk.vips.logic.entity.VipsLogicUser;
import no.bioforsk.vips.logic.scheduling.model.ModelRunPreprocessor;
import no.bioforsk.vips.logic.scheduling.model.ModelRunPreprocessorFactory;
import no.bioforsk.vips.logic.scheduling.model.PreprocessorException;
import no.bioforsk.vips.logic.util.RunModelException;
import no.bioforsk.vips.logic.util.SessionControllerGetter;
import no.bioforsk.vips.logic.util.SystemTime;
import no.bioforsk.web.forms.FormField;
import org.apache.commons.lang.StringUtils;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

/**
 * @copyright 2013-2014 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
@Stateless
public class ForecastBean {

    @PersistenceContext(unitName="VIPSLogic-PU")
    EntityManager em;
    
    /**
     * Returns all forecast results.
     * @return 
     */
    public List<ForecastResult> getForecastResults()
    {
        return em.createNamedQuery("ForecastResult.findAll").getResultList();
    }
    
    public List<ForecastResult> getForecastResults(Long forecastConfigurationId)
    {
        //ForecastConfiguration config = this.getForecastConfiguration(forecastConfigurationId);
        Query q = em.createNamedQuery("ForecastResult.findByForecastConfigurationId");
        q.setParameter("forecastConfigurationId", forecastConfigurationId);
        return q.getResultList();
        
    }
    
    public List<ForecastResult> getForecastResults(Long forecastConfigurationId, Integer latestDays)
    {
        ForecastResult mostRecentForecastResult = this.getMostRecentForecastResult(forecastConfigurationId);
        Calendar cal = Calendar.getInstance();
        cal.setTime(mostRecentForecastResult.getResultValidTime());
        cal.add(Calendar.DATE, -latestDays);
        Date startDate = cal.getTime();
        return this.getForecastResults(forecastConfigurationId, startDate, mostRecentForecastResult.getResultValidTime());
        
    }
    
    public List<ForecastResult> getForecastResults(Long forecastConfigurationId, Date timeStart, Date timeEnd)
    {
        Query q = em.createNamedQuery("ForecastResult.findByForecastConfigurationIdAndPeriod");
        q.setParameter("forecastConfigurationId", forecastConfigurationId);
        q.setParameter("timeStart", timeStart);
        q.setParameter("timeEnd", timeEnd);
        return q.getResultList();
    }
    
    public ForecastResult getMostRecentForecastResult(Long forecastConfigurationId)
    {
        Query q = em.createNativeQuery(
                "SELECT * FROM forecast_result "
                + "WHERE forecast_configuration_id=:forecastConfigurationId "
                + "AND result_valid_time = ("
                        + "SELECT MAX(result_valid_time) "
                        + "FROM forecast_result "
                        + "WHERE forecast_configuration_id=:forecastConfigurationId "
                + ");", ForecastResult.class);
        
        q.setParameter("forecastConfigurationId", forecastConfigurationId);
        return (ForecastResult) q.getSingleResult();
    }
    
    /**
     * Deletes all former results for this forecast configuration, stores the new ones
     * @param forecastConfiguration
     * @param results 
     */
    public void storeResults(ForecastConfiguration forecastConfiguration, List<Result> results)
    {
        //System.out.println("forecastConfigurationId=" + forecastConfiguration.getForecastConfigurationId());
        Query q = em.createNativeQuery("DELETE FROM public.forecast_result WHERE forecast_configuration_id=:forecastConfigurationId");
        q.setParameter("forecastConfigurationId", forecastConfiguration.getForecastConfigurationId());
        q.executeUpdate();
        for(Result result:results)
        {
            ForecastResult fResult = new ForecastResult(forecastConfiguration.getForecastConfigurationId(),result);
            em.persist(fResult);
        }
    }
    
    
    /**
     * Get all forecast configurations for one user. 
     * TODO: Should be season based, or possibly based on start/stop date
     * @param userId
     * @return 
     */
    public List<ForecastConfiguration> getForecastConfigurationsForUser(Integer userId)
    {
        VipsLogicUser user = em.find(VipsLogicUser.class, userId);
        Query q = em.createNamedQuery("ForecastConfiguration.findByVipsLogicUserId");
        q.setParameter("vipsLogicUserId", user);
        return q.getResultList();
    }
    
    public List<ForecastConfiguration> getForecastConfigurationsForUserAndDate(Integer userId, Date from, Date to)
    {
        VipsLogicUser user = em.find(VipsLogicUser.class, userId);
        Query q = em.createNamedQuery("ForecastConfiguration.findByVipsLogicUserIdAndDate");
        q.setParameter("vipsLogicUserId", user);
        q.setParameter("from", from);
        q.setParameter("to", to);
        return q.getResultList();
    }
    
    public List<ForecastConfiguration> getForecastConfigurationsForUserAndCrops(Integer userId, List<Integer> cropOrganismIds)
    {
        VipsLogicUser user = em.find(VipsLogicUser.class, userId);
        Query q = em.createNamedQuery("ForecastConfiguration.findByVipsLogicUserIdAndCropOrganismId");
        q.setParameter("vipsLogicUserId", user);
        q.setParameter("cropOrganismIds", cropOrganismIds);
        return q.getResultList();
    }
    public List<ForecastConfiguration> getForecastConfigurationsForUserAndCropsAndDate(Integer userId, List<Integer> cropOrganismIds, Date from, Date to)
    {
        VipsLogicUser user = em.find(VipsLogicUser.class, userId);
        Query q = em.createNamedQuery("ForecastConfiguration.findByVipsLogicUserIdAndCropOrganismIdsAndDate");
        q.setParameter("vipsLogicUserId", user);
        q.setParameter("cropOrganismIds", cropOrganismIds);
        q.setParameter("from", from);
        q.setParameter("to", to);
        return q.getResultList();
    }
    
    
    /**
     * Returns _ALL_ forecasts. Not for the faint hearted
     * @return 
     */
    public List<ForecastConfiguration> getForecastConfigurations()
    {
        return em.createNamedQuery("ForecastConfiguration.findAll").getResultList();
    }
    
    /**
     * 
     * @return 
     */
    public List<ForecastConfiguration> getForecastConfigurations(List<String> modelIds)
    {
        return em.createNamedQuery("ForecastConfiguration.findByModelIds")
                .setParameter("modelIds", modelIds)
                .getResultList();
    }
    
    public List<ForecastConfiguration> getForecastConfigurations(Organization organization)
    {
        List<VipsLogicUser> organizationUsers = em
                .createNamedQuery("VipsLogicUser.findByOrganizationId")
                .setParameter("organizationId", organization)
                .getResultList();
        return em
                .createNamedQuery("ForecastConfiguration.findByVipsLogicUserIds")
                .setParameter("vipsLogicUserIds", organizationUsers)
                .getResultList();
    }
    
    public List<ForecastConfiguration> getForecastConfigurations(Organization organization, List<String> modelIds, Date from, Date to)
    {
        List<VipsLogicUser> organizationUsers = em
                .createNamedQuery("VipsLogicUser.findByOrganizationId")
                .setParameter("organizationId", organization)
                .getResultList();
        
        if(!organizationUsers.isEmpty())
        {
            return em
                    .createNamedQuery("ForecastConfiguration.findByVipsLogicUserIdsAndModelIdsAndDate")
                    .setParameter("vipsLogicUserIds", organizationUsers)
                    .setParameter("modelIds", modelIds)
                    .setParameter("from", from)
                    .setParameter("to", to)
                    .getResultList();
        }
        else
        {
            return new ArrayList<>();
        }
    }
    
    public List<ForecastConfiguration> getForecastConfigurations(List<Integer> organizationIds, List<String> modelIds, Date from, Date to)
    {
        List<VipsLogicUser> organizationUsers = em
                .createNamedQuery("VipsLogicUser.findByOrganizationIds")
                .setParameter("organizationIds", organizationIds)
                .getResultList();
        
        if(!organizationUsers.isEmpty())
        {
            return em
                    .createNamedQuery("ForecastConfiguration.findByVipsLogicUserIdsAndModelIdsAndDate")
                    .setParameter("vipsLogicUserIds", organizationUsers)
                    .setParameter("modelIds", modelIds)
                    .setParameter("from", from)
                    .setParameter("to", to)
                    .getResultList();
        }
        else
        {
            return new ArrayList<>();
        }
    }
    
    public List<ForecastConfiguration> getForecastConfigurations(PointOfInterestWeatherStation weatherStation)
    {
        return em
                .createNamedQuery("ForecastConfiguration.findByWeatherStationPointOfInterestId", ForecastConfiguration.class)
                .setParameter("weatherStationPointOfInterestId", weatherStation)
                .getResultList();
    }
    
    /**
     * Deletes all forecasts and results from the given weather station
     * @param weatherStation 
     */
    public void deleteForecastConfigurations(PointOfInterestWeatherStation weatherStation)
    {
        List<ForecastConfiguration> forecastConfigurations = this.getForecastConfigurations(weatherStation);
        for(ForecastConfiguration forecastConfiguration:forecastConfigurations)
        {
            em.createNativeQuery("DELETE FROM forecast_result WHERE forecast_configuration_id=:forecastConfigurationId")
                    .setParameter("forecastConfigurationId", forecastConfiguration.getForecastConfigurationId())
                    .executeUpdate();
            em.remove(forecastConfiguration);
        }
    }
    
    /**
     * Fetches one specific forecast configuration
     * @param forecastConfigurationId
     * @return 
     */
    public ForecastConfiguration getForecastConfiguration(Long forecastConfigurationId)
    {
        return em.find(ForecastConfiguration.class, forecastConfigurationId);
    }
    
    /**
     * Requests all info about models currently available in VIPSCoreManager
     * Stores in local db for easy access. 
     */
    public void updateModelInformation()
    {
            // Get all model Ids from Core Manager
            Response resp = this.getManagerResource().printModelListJSON();
            for(JsonNode modelIdItem: resp.readEntity(JsonNode.class).findValues("modelId"))
            {
                String modelId = modelIdItem.asText();
                
                // We get the corresponding modelInformation entry 
                ModelInformation modelInformation = em.find(ModelInformation.class, modelId);
                if(modelInformation == null)
                {
                    modelInformation = new ModelInformation(modelId);
                    em.persist(modelInformation);
                    modelInformation.setDateFirstRegistered(new Date());
                }
                
                // Retrieve and store information
                modelInformation.setDefaultName(this.getManagerResource().printModelName(modelId).readEntity(String.class));
                modelInformation.setDefaultDescription(this.getManagerResource().printModelDescription(modelId).readEntity(String.class));
                modelInformation.setLicense(this.getManagerResource().printModelLicense(modelId).readEntity(String.class));
                modelInformation.setCopyrightHolder(this.getManagerResource().printModelCopyright(modelId).readEntity(String.class));
                modelInformation.setUsage(this.getManagerResource().printModelUsage(modelId).readEntity(String.class));
                modelInformation.setSampleConfig(this.getManagerResource().printModelSampleConfig(modelId).readEntity(String.class));
                modelInformation.setDateLastRegistered(new Date());
            }
            
    }
    
    /**
     * 
     * @return All registered models accessible by ModelId as key
     */
    public Map<String,ModelInformation> getIndexedModelInformation()
    {
        Map<String, ModelInformation> retVal = new HashMap<>();
        for(ModelInformation mi: (List<ModelInformation>) em.createNamedQuery("ModelInformation.findAll").getResultList())
        {
            retVal.put(mi.getModelId(), mi);
        }
        return retVal;
    }
    
    /**
     * Stores a forecast configuration, including model specific form fields
     * @param forecastConfiguration
     * @param formFields
     * @param modelSpecificFormFields
     * @return the updated (or freshly created, with brand new Id) forecast configuration
     */
    public ForecastConfiguration storeForecastConfiguration(ForecastConfiguration forecastConfiguration, Map<String, FormField> formFields, Map<String, FormField> modelSpecificFormFields)
    {
        forecastConfiguration.setModelId(formFields.get("modelId").getWebValue());
        forecastConfiguration.setCropOrganismId(em.find(Organism.class, formFields.get("cropOrganismId").getValueAsInteger()));
        forecastConfiguration.setPestOrganismId(em.find(Organism.class, formFields.get("pestOrganismId").getValueAsInteger()));
        PointOfInterest locationPoi = em.find(PointOfInterest.class, formFields.get("locationPointOfInterestId").getValueAsInteger());
        forecastConfiguration.setLocationPointOfInterestId(locationPoi);
        PointOfInterest weatherStationPoi = em.find(PointOfInterestWeatherStation.class, formFields.get("weatherStationPointOfInterestId").getValueAsInteger());
        forecastConfiguration.setWeatherStationPointOfInterestId(weatherStationPoi);
        forecastConfiguration.setDateStart(formFields.get("dateStart").getValueAsDate());
        forecastConfiguration.setDateEnd(formFields.get("dateEnd").getValueAsDate());
        VipsLogicUser forecastConfigurationUser = em.find(VipsLogicUser.class, formFields.get("vipsLogicUserId").getValueAsInteger());
        forecastConfiguration.setVipsCoreUserId(forecastConfigurationUser);
        
        forecastConfiguration = em.merge(forecastConfiguration);
        
        // Reset all model configurations, then store the new ones
        // As for now: We keep the old ones.
        // Reason: If anybody screws up by changing a form, old configurations could get lost
        /*List<ForecastModelConfiguration> configsToRemove = em.createNamedQuery("ForecastModelConfiguration.findByForecastConfigurationId").setParameter("forecastConfigurationId", forecastConfiguration.getForecastConfigurationId()).getResultList();
        for(ForecastModelConfiguration configToRemove: configsToRemove)
        {
            em.remove(configToRemove);
        }
        em.flush();*/
        
        // Store new values
        for(FormField field : modelSpecificFormFields.values())
        {
            String deCamelizedFieldName = getDeCamelizedFieldName(forecastConfiguration.getModelId(), field.getName());
            ForecastModelConfiguration forecastModelConfiguration = new ForecastModelConfiguration(forecastConfiguration.getForecastConfigurationId(), deCamelizedFieldName);
            forecastModelConfiguration.setParameterValue(field.getWebValue());
            em.merge(forecastModelConfiguration);
        }
        
        return forecastConfiguration;
    }
    
    /**
     * 
     * @param modelId
     * @param camelCaseName
     * @return MODELID_CAMEL_CASE_NAME
     */
    private String getDeCamelizedFieldName(String modelId, String camelCaseName)
    {
        StringBuilder deCamelizedFieldName = new StringBuilder(modelId.toUpperCase());
        for(String phrase : camelCaseName.split("(?=\\p{Lu})"))
        {
            deCamelizedFieldName.append("_").append(phrase.toUpperCase());
        }
        
        return deCamelizedFieldName.toString();
    }
    
    
    public List<ForecastModelConfiguration> getForecastModelConfigurations(Long forecastConfigurationId)
    {
        return em.createNamedQuery("ForecastModelConfiguration.findByForecastConfigurationId").setParameter("forecastConfigurationId", forecastConfigurationId).getResultList();
    }
    
    /**
     * Deletes a forecast configuration and all results
     * @param forecastConfigurationId 
     */
    public void deleteForecastConfiguration(Long forecastConfigurationId)
    {
        // The entity relationship between ForecastConfiguration and ForecastResult
        // is not explicit in EJB model, but in the database there is a foreign key 
        // in forecast_result referencing the forecast_configuration
        // Explicit deletion of forecast_results rows is therefore necessary.
        ForecastConfiguration forecastConfiguration = em.find(ForecastConfiguration.class, forecastConfigurationId);
        Query q = em.createNativeQuery("DELETE FROM public.forecast_result WHERE forecast_configuration_id=:forecastConfigurationId");
        q.setParameter("forecastConfigurationId", forecastConfiguration.getForecastConfigurationId());
        q.executeUpdate();
        em.remove(forecastConfiguration);
        
    }
    
    public List<ForecastConfiguration> getForecastConfigurationsValidAtTime(Organization organization, Date time)
    {
        Query q = em.createNativeQuery(
                        "SELECT * FROM public.forecast_configuration "
                        + "WHERE vips_logic_user_id IN (SELECT user_id FROM public.vips_logic_user WHERE organization_id=:organizationId) "
                        + "AND :time BETWEEN date_start AND date_end"
        ,ForecastConfiguration.class);
        q.setParameter("organizationId", organization.getOrganizationId());
        q.setParameter("time", time);
        return q.getResultList();
    }
    
    public void runForecast(ForecastConfiguration forecastConfiguration) throws PreprocessorException, RunModelException
    {
        ModelRunPreprocessor preprocessor = ModelRunPreprocessorFactory.getModelRunPreprocessor(forecastConfiguration.getModelId());
        if(preprocessor != null)
        {
                ModelConfiguration config = preprocessor.getModelConfiguration(forecastConfiguration);
                ModelRunRequest request = new ModelRunRequest(config);
                Map<String,String> loginInfo = new HashMap<>();
                // VIPSLogic logs in on behalf of client
                loginInfo.put("username",System.getProperty("no.bioforsk.vips.logic.CORE_BATCH_USERNAME"));
                //loginInfo.put("username","wrongusername");
                loginInfo.put("password",System.getProperty("no.bioforsk.vips.logic.CORE_BATCH_PASSWORD"));
                request.setLoginInfo(loginInfo);
                // We tell which client this is (the db Id in VIPSCoreManager)
                request.setVipsCoreUserId(forecastConfiguration.getVipsLogicUserId().getVipsCoreUserIdWithFallback());
                //System.out.println("RunModel for wsId" + forecastConfiguration.getWeatherStationPointOfInterestId());
                Response resp = this.getManagerResource().runModel(config.getModelId(), request);
                
                if(resp.getStatus() == Response.Status.OK.getStatusCode())
                {
                    List<Result> results = (List<Result>) resp.readEntity(new GenericType<List<Result>>(){});
                    // We delete all former results before we store the new ones
                    SessionControllerGetter.getForecastBean().storeResults(forecastConfiguration,results);
                }
                else
                {
                    throw new RunModelException(resp.readEntity(String.class));
                }
                //System.out.println("Finished runModel for wsId" + forecastConfiguration.getWeatherStationPointOfInterestId());
            
        }
    }
    
    /**
     * Get the interface for REST resources in VIPSCoreManager
     * @return 
     */
    private ManagerResource getManagerResource()
    {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(System.getProperty("no.bioforsk.vips.logic.VIPSCOREMANAGER_URL"));
        ResteasyWebTarget rTarget = (ResteasyWebTarget) target;
        ManagerResource resource = rTarget.proxy(ManagerResource.class);
        return resource;
    }
    
    public Kml getForecastsAggregateKml(Integer organizationId, List<Integer> cropOrganismIds, Date theDate, String serverName)
    {
        String iconPath = "http://" + serverName + "/public/images/";
        // Initialization
        final Vec2 hotspot = new Vec2()
                .withX(0.5)
                .withXunits(Units.FRACTION)
                .withY(0)
                .withYunits(Units.FRACTION);
        final Kml kml = KmlFactory.createKml();
        final Document document = kml.createAndSetDocument()
        .withName("Forecast results - aggregates").withDescription("Worst forecasts for each POI");

        document.createAndAddStyle()
            .withId("warning_type_0")
        .createAndSetIconStyle()
                .withScale(1)
                .withHotSpot(hotspot)
                .createAndSetIcon()
                    .withHref(iconPath + "station_icon_status_0.png");
        
        document.createAndAddStyle()
            .withId("warning_type_1")
        .createAndSetIconStyle()
                .withScale(1)
                .withHotSpot(hotspot)
                .createAndSetIcon()
                    .withHref(iconPath + "station_icon_status_1.png");
        
        document.createAndAddStyle()
            .withId("warning_type_2")
        .createAndSetIconStyle()
                .withScale(1)
                .withHotSpot(hotspot)
                .createAndSetIcon()
                    .withHref(iconPath + "station_icon_status_2.png");
        
        document.createAndAddStyle()
            .withId("warning_type_3")
        .createAndSetIconStyle()
                .withScale(1)
                .withHotSpot(hotspot)
                .createAndSetIcon()
                    .withHref(iconPath + "station_icon_status_3.png");
        
        document.createAndAddStyle()
            .withId("warning_type_4")
        .createAndSetIconStyle()
                .withScale(1)
                .withHotSpot(hotspot)
                .createAndSetIcon()
                    .withHref(iconPath + "station_icon_status_4.png");

        // Run through forecast configurations
        //Date benchmark = new Date();
        List<PointOfInterest> poisWithAggregate = getPointOfInterestForecastsAggregate(organizationId, cropOrganismIds, theDate);
        //System.out.println(this.getClass().getName() + " DEBUG: getPointOfInterestForecastsAggregate took " + (new Date().getTime() - benchmark.getTime()) + " ms to complete.");
        for(PointOfInterest poiWithAggregate:poisWithAggregate)
        {
            final Placemark placemark = document.createAndAddPlacemark()
            .withName(poiWithAggregate.getName())
            .withDescription("<![CDATA[Mangler informasjon om varsler for " + poiWithAggregate.getName() + "]]>")
            .withStyleUrl("#warning_type_" 
                    + (poiWithAggregate.getProperties().get("forecastsAggregate") != null ? poiWithAggregate.getProperties().get("forecastsAggregate") : "0")
                )
             .withId(poiWithAggregate.getPointOfInterestId().toString());
            
            
            final Point point = placemark.createAndSetPoint();
            List<Coordinate> coord = point.createAndSetCoordinates();
            coord.add(new Coordinate(
                    poiWithAggregate.getLongitude(),
                    poiWithAggregate.getLatitude(),
                    poiWithAggregate.getAltitude() != null ? poiWithAggregate.getAltitude() : 0
            ));
        }
        //System.out.println(kml.marshal());
        return kml;
    }
    
    /**
     * The table forecast_result_cache always should contain the forecast
     * results from TODAY (The system's time, which is configurable)
     */
    public void updateForecastResultCacheTable()
    {
        String dateFormat = "yyyy-MM-dd";
        SimpleDateFormat format = new SimpleDateFormat(dateFormat);
        String transactionSQL = new StringBuilder()
                .append("BEGIN;")
                .append("   LOCK TABLE forecast_result_cache;")
                .append("   TRUNCATE forecast_result_cache;")
                .append("   INSERT INTO forecast_result_cache ")
                .append("       SELECT * FROM forecast_result ")
                .append("       WHERE to_char(result_valid_time, '").append(dateFormat).append("') = :dateStr ;")
                .append("END;")
                .toString();
        
        Query query = em.createNativeQuery(transactionSQL);
        query.setParameter("dateStr", format.format(SystemTime.getSystemTime()));
        query.executeUpdate();
    }
    
    /**
     * The table forecast_summary should contain forecast summaries
     * from +/- 10 days related to TODAY (The system's time, which is configurable)
     */
    public void updateForecastSummaryTable(Date currentDate)
    {
        // 
        // Collect all forecasts that are active TODAY
        List<ForecastConfiguration> activeForecasts = em.createNamedQuery("ForecastConfiguration.findActiveAtDate")
                .setParameter("currentDate", currentDate).getResultList();
        // Loop through them
        List<Long> activeForecastIds = new ArrayList<>();
        Query deleteQ = em.createNativeQuery(
                    "DELETE FROM forecast_summary "
                    + "WHERE forecast_configuration_id = :forecastConfigurationId");
        Query findQ = em.createNamedQuery("ForecastResult.findByForecastConfigurationIdAndPeriod");
        for(ForecastConfiguration forecastConfiguration:activeForecasts)
        {
            activeForecastIds.add(forecastConfiguration.getForecastConfigurationId());
            //System.out.println("forecastConfig id=" + forecastConfiguration.getForecastConfigurationId());
            // Delete previous summaries for the specific forecast (one at a time,
            // to prevent loss of data in case of software error)
            
            deleteQ.setParameter("forecastConfigurationId", forecastConfiguration.getForecastConfigurationId())
            .executeUpdate();
            
            // Get all results that are within +/- 10 days from TODAY (taking 
            // time zone into account)
            TimeZone forecastTimeZone = TimeZone.getTimeZone(
                    forecastConfiguration.getVipsLogicUserId().getOrganizationId().getDefaultTimeZone()
            );
            Calendar cal = Calendar.getInstance(forecastTimeZone);
            cal.setTime(currentDate);
            cal.add(Calendar.DATE, -10);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE,0);
            cal.set(Calendar.SECOND,0);
            cal.set(Calendar.MILLISECOND,0);
            Date tenDaysAgo = cal.getTime();
            cal.add(Calendar.DATE, 20);
            Date tenDaysAhead = cal.getTime();
            List<ForecastResult> results = findQ
                    .setParameter("timeStart", tenDaysAgo)
                    .setParameter("timeEnd", tenDaysAhead)
                    .setParameter("forecastConfigurationId", forecastConfiguration.getForecastConfigurationId())
                    .getResultList();
                    
            // Loop through each day (take timezone into account!), find worst warning
            Collections.sort(results);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            format.setTimeZone(forecastTimeZone);
            Map<String, Integer> resultDailyAggregate = new HashMap<>();
            for(ForecastResult result:results)
            {
                String dayStamp = format.format(result.getResultValidTime());
                Integer currentWorstCode = resultDailyAggregate.get(dayStamp);
                resultDailyAggregate.put(dayStamp, 
                    Math.max(
                        currentWorstCode != null ? currentWorstCode : 0, 
                        result.getWarningStatus()
                    )
                );
                
            }
            for(String dayStamp: resultDailyAggregate.keySet())
            {
                Date aggregateDay;
                try {
                    aggregateDay = format.parse(dayStamp);
                    ForecastSummaryPK summaryPK = new ForecastSummaryPK(
                        forecastConfiguration.getForecastConfigurationId(),
                        aggregateDay
                    );
                    ForecastSummary summary = new ForecastSummary(summaryPK);
                    summary.setSummaryCreatedTime(new Date());
                    summary.setWarningStatus(resultDailyAggregate.get(dayStamp));
                    em.persist(summary);
                } catch (ParseException ex) {
                    Logger.getLogger(ForecastBean.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println(this.getClass().getName() + " [updateForecastSummaryTable]: Error parsing date " + dayStamp);
                }
                
            }
        }
        // Delete all summaries from not active forecasts
        em.createNativeQuery("DELETE FROM forecast_summary "
                + "WHERE forecast_configuration_id NOT IN :activeForecastIds")
                .setParameter("activeForecastIds", activeForecastIds)
                .executeUpdate();
    }
    
    public List<ForecastConfiguration> getForecastConfigurationSummaries(Integer organizationId)
    {
        List<ForecastConfiguration> forecastConfigurations = this.getForecastConfigurations(em.find(Organization.class, organizationId));
        // TODO: Filter forecastconfigurations based on criteria (activity, crops, geography etc)
        List<ForecastConfiguration> filteredConfigs = new ArrayList<>();
        Query q = em.createNamedQuery("ForecastSummary.findByForecastConfigurationId");
        for(ForecastConfiguration config: forecastConfigurations)
        {
            config.setForecastSummaries(
                    
                    q.setParameter("forecastConfigurationId", config.getForecastConfigurationId())
                    .getResultList()
            );
            if(config.getForecastSummaries() != null && !config.getForecastSummaries().isEmpty())
            {
                filteredConfigs.add(config);
            }
        }
        return filteredConfigs;
    }

    /**
     * Selects the "worst" (highest infection risk) warning status for forecasts
     * running at the pois connected to the given organization
     * @param organizationId Filter for organization
     * @param cropOrganismIds Filter for crops
     * @param theDate Filter for date. If theDate=systemDate, data is fetched from the caching table forecast_result_cache
     * @return 
     */
    private List<PointOfInterest> getPointOfInterestForecastsAggregate(Integer organizationId, List<Integer> cropOrganismIds, Date theDate) {
        // TODO: More precise gathering of POIs...
        List<PointOfInterest> pois;
        if(organizationId != null && organizationId > 0)
        {
            pois = em.createNamedQuery("PointOfInterest.findByOrganizationId")
                                .setParameter("organizationId", em.find(Organization.class, organizationId))
                                .getResultList();
        }
        else
        {
            pois = em.createNamedQuery("PointOfInterest.findAll").getResultList();
        }
        
        String dateFormat = "yyyy-MM-dd";
        SimpleDateFormat format = new SimpleDateFormat(dateFormat);
        // If theDate=systemDate, data is fetched from the caching table forecast_result_cache
        String tableName = (format.format(theDate).equals(format.format(SystemTime.getSystemTime()))) ? "forecast_result_cache" : "forecast_result";
        //this.updateForecastResultCacheTable();
        for(PointOfInterest poi: pois)
        {
            String sql = "SELECT max(warning_status) FROM " + tableName + " \n" +
                        "WHERE forecast_configuration_id IN( \n" +
                        "	SELECT forecast_configuration_id \n" +
                        "	FROM forecast_configuration \n" +
                        "	WHERE location_point_of_interest_id=:locationPointOfInterestId \n" +
                        (cropOrganismIds != null && ! cropOrganismIds.isEmpty() ? "     AND crop_organism_id IN (" + StringUtils.join(cropOrganismIds, ",") + ") " : "") +
                        ")\n" +
                        "AND to_char(result_valid_time, '" + dateFormat + "') = :dateStr";
            
            Query q = em.createNativeQuery(sql);
            q.setParameter("locationPointOfInterestId", poi.getPointOfInterestId());
            q.setParameter("dateStr", format.format(theDate));
            Integer result = (Integer) q.getSingleResult();
            poi.getProperties().put("forecastsAggregate", result);
            
        }
        return pois;
    }

    /**
     * Returns the latest forecast results for given point of interest
     * @param poiId
     * @return 
     */
    public Map<String, Object> getLatestForecastResultsForPoi(Integer poiId) {
        Map<String, Object> retVal = new HashMap<>();
        
        PointOfInterest poi = em.find(PointOfInterest.class, poiId);
        List<ForecastConfiguration> forecastConfigurations = 
                                        em.createNamedQuery("ForecastConfiguration.findByLocationPointOfInterestId")
                                        .setParameter("locationPointOfInterestId", poi)
                                        .getResultList();
        
        HashMap<Long,ForecastConfiguration> mappedForecastConfigurations = new HashMap<>();
        
        List<ForecastResult> results = new ArrayList<>();
        for(ForecastConfiguration forecastConfiguration:forecastConfigurations)
        {
            mappedForecastConfigurations.put(forecastConfiguration.getForecastConfigurationId(), forecastConfiguration);
            Query q = em.createNativeQuery(
                    "SELECT * FROM forecast_result WHERE forecast_configuration_id = :forecastConfigurationId "
                            + "AND result_valid_time = (SELECT max(result_valid_time) FROM forecast_result WHERE forecast_configuration_id = :forecastConfigurationId)", 
                    ForecastResult.class
            );
            q.setParameter("forecastConfigurationId", forecastConfiguration.getForecastConfigurationId());
            try
            {
                results.add((ForecastResult)q.getSingleResult());
            }
            catch(NoResultException ex)
            {
                // This means that the forecast exists, but that there have not been any valid forecast results created
                // For example, it might be too early to start any calculation
            }
        }
        
        retVal.put("forecastConfigurations", mappedForecastConfigurations);
        retVal.put("results", results);
        retVal.put("pointOfInterest", poi);
        
        return retVal;
        
    }
    
}
