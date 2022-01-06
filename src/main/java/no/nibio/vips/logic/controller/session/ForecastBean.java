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

package no.nibio.vips.logic.controller.session;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Calendar;
import java.util.TimeZone;
import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Data;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.ExtendedData;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;
import de.micromata.opengis.kml.v_2_2_0.LabelStyle;
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
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.ejb.EJB;
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
import no.nibio.vips.coremanager.service.ManagerResource;
import no.nibio.vips.entity.ModelConfiguration;
import no.nibio.vips.entity.ModelRunRequest;
import no.nibio.vips.entity.Result;
import no.nibio.vips.logic.entity.ForecastConfiguration;
import no.nibio.vips.logic.entity.ForecastModelConfiguration;
import no.nibio.vips.logic.entity.ForecastResult;
import no.nibio.vips.logic.entity.ForecastSummary;
import no.nibio.vips.logic.entity.ForecastSummaryPK;
import no.nibio.vips.logic.entity.ModelInformation;
import no.nibio.vips.logic.entity.Organism;
import no.nibio.vips.logic.entity.Organization;
import no.nibio.vips.logic.entity.PointOfInterest;
import no.nibio.vips.logic.entity.PointOfInterestWeatherStation;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.scheduling.model.ModelRunPreprocessor;
import no.nibio.vips.logic.scheduling.model.ModelRunPreprocessorFactory;
import no.nibio.vips.logic.scheduling.model.PreprocessorException;
import no.nibio.vips.logic.util.GISEntityUtil;
import no.nibio.vips.logic.util.RunModelException;
import no.nibio.vips.logic.util.SystemTime;
import no.nibio.vips.util.WeatherUtil;
import no.nibio.web.forms.FormField;
import org.apache.commons.lang.StringUtils;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

/**
 * @copyright 2013-2022 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Stateless
public class ForecastBean {

    @PersistenceContext(unitName="VIPSLogic-PU")
    EntityManager em;
    
    @EJB
    ForecastBean forecastBean;
    
    @EJB
    UserBean userBean;
    
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
    
    /**
     * 
     * @param forecastConfiguration
     * @param user
     * @return 
     */
    public boolean isUserAuthorizedForForecastConfiguration(ForecastConfiguration forecastConfiguration, VipsLogicUser user)
    {
        // Public forecasts are always OK for everyone to view
        if(!forecastConfiguration.getIsPrivate())
        {
            return true;
        }
        // Private forecasts are only viewable by owner or super users / orgadmins
        return user != null && (user.isSuperUser() || user.isOrganizationAdmin() || user.getUserId().equals( forecastConfiguration.getVipsLogicUserId().getUserId()));
    }
            
    
    public boolean isUserAuthorizedForForecastConfiguration(Long forecastConfigurationId, String userUUID)
    {
        // Authentication
        ForecastConfiguration fc = em.find(ForecastConfiguration.class, forecastConfigurationId);
        if(fc == null)
        {
            return true;
        }
        if(fc.getIsPrivate())
        {
            if(userUUID == null)
            {
                return false;
            }
            UUID uUUID = UUID.fromString(userUUID);
            VipsLogicUser user = userBean.findVipsLogicUser(uUUID);
            if(user == null || ! user.getUserId().equals( fc.getVipsLogicUserId().getUserId()))
            {
                return false;
            }
        }
        return true;
    }
    
    public List<ForecastResult> getForecastResults(Long forecastConfigurationId, Integer latestDays)
    {
        ForecastResult mostRecentForecastResult = this.getMostRecentForecastResult(forecastConfigurationId);
        if(mostRecentForecastResult == null)
        {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(mostRecentForecastResult.getValidTimeStart());
        cal.add(Calendar.DATE, -latestDays);
        Date startDate = cal.getTime();
        return this.getForecastResults(forecastConfigurationId, startDate, mostRecentForecastResult.getValidTimeStart());
        
    }
    
    public List<ForecastResult> getForecastResults(Long forecastConfigurationId, Date timeStart, Date timeEnd)
    {
        Query q = em.createNamedQuery("ForecastResult.findByForecastConfigurationIdAndPeriod");
        q.setParameter("forecastConfigurationId", forecastConfigurationId);
        q.setParameter("timeStart", timeStart);
        q.setParameter("timeEnd", timeEnd);
        try
        {
            return q.getResultList();
        }
        catch(NoResultException ex)
        {
            return null;
        }
    }
    
    public ForecastResult getMostRecentForecastResult(Long forecastConfigurationId)
    {
        Query q = em.createNativeQuery(
                "SELECT * FROM forecast_result "
                + "WHERE forecast_configuration_id=:forecastConfigurationId "
                + "AND valid_time_start = ("
                        + "SELECT MAX(valid_time_start) "
                        + "FROM forecast_result "
                        + "WHERE forecast_configuration_id=:forecastConfigurationId "
                + ");", ForecastResult.class);
        
        q.setParameter("forecastConfigurationId", forecastConfigurationId);
        try
        {
            return (ForecastResult) q.getSingleResult();
        }
        catch(NoResultException ex)
        {
            return null;
        }
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
     * Get all PUBLIC forecast configurations for one user. 
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
    
    /**
     * Get all PRIVATE forecast configurations for one user. 
     * TODO: Should be season based, or possibly based on start/stop date
     * @param userId
     * @return 
     */
    public List<ForecastConfiguration> getPrivateForecastConfigurationsForUser(Integer userId)
    {
        VipsLogicUser user = em.find(VipsLogicUser.class, userId);
        Query q = em.createNamedQuery("ForecastConfiguration.findPrivateByVipsLogicUserId");
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
        
        if(!organizationUsers.isEmpty() && ! modelIds.isEmpty())
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
    
    public List<ForecastConfiguration> getForecastConfigurations(List<Integer> organizationIds, Date from, Date to)
    {
        List<VipsLogicUser> organizationUsers = em
                .createNamedQuery("VipsLogicUser.findByOrganizationIds")
                .setParameter("organizationIds", organizationIds)
                .getResultList();
        
        
        if(!organizationUsers.isEmpty())
        {
            return em
                    .createNamedQuery("ForecastConfiguration.findByVipsLogicUserIdsAndDate")
                    .setParameter("vipsLogicUserIds", organizationUsers)
                    .setParameter("from", from)
                    .setParameter("to", to)
                    .getResultList();
        }
        else
        {
            return new ArrayList<>();
        }
    }
    
    public List<ForecastConfiguration> getForecastConfigurationsByWeatherStation(PointOfInterestWeatherStation weatherStation)
    {
        return em
                .createNamedQuery("ForecastConfiguration.findByWeatherStationPointOfInterestId", ForecastConfiguration.class)
                .setParameter("weatherStationPointOfInterestId", weatherStation)
                .getResultList();
    }
    
    public List<ForecastConfiguration> getForecastConfigurationsByLocation(PointOfInterest poi)
    {
        return em
                .createNamedQuery("ForecastConfiguration.findByLocationPointOfInterestId", ForecastConfiguration.class)
                .setParameter("locationPointOfInterestId", poi)
                .getResultList();
    }
    
    public List<ForecastConfiguration> getForecastConfigurations(PointOfInterestWeatherStation weatherStation,Date from, Date to)
    {
        return em
                .createNamedQuery("ForecastConfiguration.findByWeatherStationPointOfInterestIdAndDate", ForecastConfiguration.class)
                .setParameter("weatherStationPointOfInterestId", weatherStation)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
    }
    
    /**
     * Deletes all forecasts and results from the given weather station
     * @param weatherStation 
     */
    public void deleteForecastConfigurationsForWeatherStation(PointOfInterestWeatherStation weatherStation)
    {
        List<ForecastConfiguration> forecastConfigurations = this.getForecastConfigurationsByWeatherStation(weatherStation);
        for(ForecastConfiguration forecastConfiguration:forecastConfigurations)
        {
            em.createNativeQuery("DELETE FROM forecast_result WHERE forecast_configuration_id=:forecastConfigurationId")
                    .setParameter("forecastConfigurationId", forecastConfiguration.getForecastConfigurationId())
                    .executeUpdate();
            em.remove(forecastConfiguration);
        }
    }
    
    /**
     * Deletes all forecasts and results from the given location
     * @param weatherStation 
     */
    public void deleteForecastConfigurationsForLocation(PointOfInterest location)
    {
        List<ForecastConfiguration> forecastConfigurations = this.getForecastConfigurationsByLocation(location);
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
    	
	    	Client client = ClientBuilder.newClient();
	        WebTarget target = client.target(System.getProperty("no.nibio.vips.logic.VIPSCOREMANAGER_URL"));
	        ResteasyWebTarget rTarget = (ResteasyWebTarget) target;
	        ManagerResource resource = rTarget.proxy(ManagerResource.class);
            // Get all model Ids from Core Manager
            Response resp = resource.printModelListJSON();
            
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
                
                Response r = resource.printModelName(modelId);
                modelInformation.setDefaultName(r.readEntity(String.class));
                //r.close();
                
                r = resource.printModelDescription(modelId);
                modelInformation.setDefaultDescription(r.readEntity(String.class));
                //r.close();
                
                r = resource.printModelLicense(modelId);
                modelInformation.setLicense(r.readEntity(String.class));
                //r.close();
                
                r = resource.printModelCopyright(modelId);
                modelInformation.setCopyrightHolder(r.readEntity(String.class));
                //r.close();
                
                r = resource.printModelUsage(modelId);
                modelInformation.setUsage(r.readEntity(String.class));
                //r.close();
                
                r = resource.printModelSampleConfig(modelId);
                modelInformation.setSampleConfig(r.readEntity(String.class));
                //r.close();
                
                modelInformation.setDateLastRegistered(new Date());
            }
            resp.close();
            client.close();
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
     * 
     * @return All registered models that has its own preprocessor accessible by ModelId as key
     */
    public Map<String,ModelInformation> getIndexedBatchableModelInformation()
    {
        Map<String, ModelInformation> retVal = new HashMap<>();
        this.getBatchableModels().forEach((mi) -> {
            retVal.put(mi.getModelId(), mi);
        });
        return retVal;
    }
    
    public ModelInformation getModelInformation(String modelId)
    {
        try
        {
            return em.createNamedQuery("ModelInformation.findByModelId", ModelInformation.class).setParameter("modelId", modelId).getSingleResult();
        }
        catch(NoResultException ex)
        {
            return null;
        }
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
        forecastConfiguration.setIsPrivate(formFields.get("isPrivate").getWebValue() != null);
        PointOfInterest locationPoi = em.find(PointOfInterest.class, formFields.get("locationPointOfInterestId").getValueAsInteger());
        forecastConfiguration.setLocationPointOfInterestId(locationPoi);
        PointOfInterest weatherStationPoi = em.find(PointOfInterestWeatherStation.class, formFields.get("weatherStationPointOfInterestId").getValueAsInteger());
        forecastConfiguration.setWeatherStationPointOfInterestId(weatherStationPoi);
        String timeZone = formFields.get("timeZone").getWebValue();
        forecastConfiguration.setTimeZone(timeZone);
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
    public String getDeCamelizedFieldName(String modelId, String camelCaseName)
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
        // in forecast_result (and in forecast summary) referencing the forecast_configuration
        // Explicit deletion of forecast_results rows is therefore necessary.
        ForecastConfiguration forecastConfiguration = em.find(ForecastConfiguration.class, forecastConfigurationId);
        Query q = em.createNativeQuery("DELETE FROM public.forecast_result WHERE forecast_configuration_id=:forecastConfigurationId");
        q.setParameter("forecastConfigurationId", forecastConfiguration.getForecastConfigurationId());
        q.executeUpdate();
        q = em.createNativeQuery("DELETE FROM public.forecast_summary WHERE forecast_configuration_id=:forecastConfigurationId");
        q.setParameter("forecastConfigurationId", forecastConfiguration.getForecastConfigurationId());
        q.executeUpdate();
        em.remove(forecastConfiguration);
        
    }
    
    public List<ForecastConfiguration> getForecastConfigurationsValidAtTime(Organization organization, Date time)
    {
        Query q = em.createNativeQuery(
                        "SELECT * FROM public.forecast_configuration "
                        + "WHERE vips_logic_user_id IN (SELECT user_id FROM public.vips_logic_user WHERE organization_id=:organizationId) "
                        + "AND :time BETWEEN date_start AND date_end "
                        + "ORDER BY weather_station_point_of_interest_id ASC "
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
                loginInfo.put("username",System.getProperty("no.nibio.vips.logic.CORE_BATCH_USERNAME"));
                //loginInfo.put("username","wrongusername");
                loginInfo.put("password",System.getProperty("no.nibio.vips.logic.CORE_BATCH_PASSWORD"));
                request.setLoginInfo(loginInfo);
                // We tell which client this is (the db Id in VIPSCoreManager)
                Integer VIPSCoreUserId = forecastConfiguration.getVipsLogicUserId().getVipsCoreUserIdWithFallback();
                if(VIPSCoreUserId == null)
                {
                    throw new PreprocessorException("No user id found for forecast #" + forecastConfiguration.getForecastConfigurationId() + 
                            ". Possible reason: The user's organization (" 
                            + forecastConfiguration.getVipsLogicUserId().getOrganizationId().getOrganizationName() 
                            + ") hasn't got a VIPSCoreUserId.");
                }
                //System.out.println("VIPSCoreUserId = " + VIPSCoreUserId + ", name=" + forecastConfiguration.getVipsLogicUserId().getLastName());
                request.setVipsCoreUserId(VIPSCoreUserId);
                //System.out.println("RunModel for wsId " + forecastConfiguration.getWeatherStationPointOfInterestId());
                //System.out.println(config.toJSON());
                /* DEBUG STUFF */
                /*ObjectMapper mapper = new ObjectMapper();
                try
                {
                    System.out.println(mapper.writeValueAsString(request));
                }
                catch(JsonProcessingException ex)
                {
                    ex.printStackTrace();
                }*/
                    Response resp = this.getManagerResource().runModel(config.getModelId(), request);         
                    if(resp.getStatus() == Response.Status.OK.getStatusCode())
                    {
                        //System.out.println(resp.readEntity(String.class));
                        List<Result> results = (List<Result>) resp.readEntity(new GenericType<List<Result>>(){});
                        //System.out.println("ForecastConfigId=" + forecastConfiguration.getForecastConfigurationId() + ", resultsize=" + results.size());
                        // We delete all former results before we store the new ones
                        forecastBean.storeResults(forecastConfiguration,results);
                    }
                    else
                    {
                        throw new RunModelException(resp.readEntity(String.class));
                    }
                //System.out.println("Finished runModel for wsId" + forecastConfiguration.getWeatherStationPointOfInterestId());
            
        }
        else
        {
            throw new PreprocessorException("Could not find model with id=|" + forecastConfiguration.getModelId() + "|");
        }
    }
    
    public List<Result> runForecast(ModelConfiguration config, Integer VIPSCoreUserId) throws RunModelException
    {
        ModelRunRequest request = new ModelRunRequest(config);
        Map<String,String> loginInfo = new HashMap<>();
        // VIPSLogic logs in on behalf of client
        loginInfo.put("username",System.getProperty("no.nibio.vips.logic.CORE_BATCH_USERNAME"));
        //loginInfo.put("username","wrongusername");
        loginInfo.put("password",System.getProperty("no.nibio.vips.logic.CORE_BATCH_PASSWORD"));
        request.setLoginInfo(loginInfo);
        //System.out.println("VIPSCoreUserId = " + VIPSCoreUserId + ", name=" + forecastConfiguration.getVipsLogicUserId().getLastName());
        request.setVipsCoreUserId(VIPSCoreUserId);
        //System.out.println("RunModel for wsId" + forecastConfiguration.getWeatherStationPointOfInterestId());
        Response resp = this.getManagerResource().runModel(config.getModelId(), request);

        if(resp.getStatus() == Response.Status.OK.getStatusCode())
        {
            List<Result> results = (List<Result>) resp.readEntity(new GenericType<List<Result>>(){});
            return results;
        }
        else
        {
            throw new RunModelException(resp.readEntity(String.class));
        }
    }
    
    /**
     * Get the interface for REST resources in VIPSCoreManager
     * @return 
     */
    private ManagerResource getManagerResource()
    {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(System.getProperty("no.nibio.vips.logic.VIPSCOREMANAGER_URL"));
        ResteasyWebTarget rTarget = (ResteasyWebTarget) target;
        ManagerResource resource = rTarget.proxy(ManagerResource.class);
        return resource;
    }
    
    public Kml getForecastsAggregateKml(List<Integer> organizationIds, List<Integer> cropOrganismIds, Date theDate, String serverName, VipsLogicUser user)
    {
        //String iconPath = Globals.PROTOCOL + "://" + serverName + "/public/images/";
        String iconPath = "//" + serverName + "/public/images/";
        // Initialization
        final Vec2 hotspot = new Vec2()
                .withX(0.5)
                .withXunits(Units.FRACTION)
                .withY(0)
                .withYunits(Units.FRACTION);
        final Kml kml = KmlFactory.createKml();
        final Document document = kml.createAndSetDocument()
        .withName("Forecast results - aggregates").withDescription("Worst forecasts for each POI");

        LabelStyle noLabel = new LabelStyle().withScale(0.0);
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(SystemTime.getSystemTime());
        
        for(int i=0;i<=4;i++)
        {
            
            document.createAndAddStyle()
                .withId("warning_type_" + i)
                .withLabelStyle(noLabel)
            .createAndSetIconStyle()
                    .withScale(1)
                    .withHotSpot(hotspot)
                    .createAndSetIcon()
                        .withHref(iconPath + "station_icon_status_" + 
                                (cal.get(Calendar.MONTH) <= 1 ? "winter" : 
                                        cal.get(Calendar.MONTH) == Calendar.DECEMBER ? "xmas" : i)
                                + ".png"); 
        }
        
        // Run through forecast configurations 
        //Date benchmark = new Date();
        List<PointOfInterest> poisWithAggregate = new ArrayList<>();
        if(organizationIds.size() == 1 && organizationIds.get(0).equals(-1))
        {
            em.createNamedQuery("Organization.findAll",Organization.class).getResultStream().forEach(
                    org-> poisWithAggregate.addAll(getPointOfInterestForecastsAggregate(org.getOrganizationId(), cropOrganismIds, theDate, user))
                    );
        }
        else
        {
            organizationIds.stream().forEach(
                    orgId-> poisWithAggregate.addAll(getPointOfInterestForecastsAggregate(orgId, cropOrganismIds, theDate, user))
                    );
        }
        
        //System.out.println(this.getClass().getName() + " DEBUG: getPointOfInterestForecastsAggregate took " + (new Date().getTime() - benchmark.getTime()) + " ms to complete.");
        
        GISEntityUtil gisUtil = new GISEntityUtil();
        for(PointOfInterest poiWithAggregate:poisWithAggregate) 
        {
            // If it's an inactive weather station, we don't produce a placemark
            if(poiWithAggregate instanceof PointOfInterestWeatherStation
                    && ((PointOfInterestWeatherStation) poiWithAggregate).getActive().equals(Boolean.FALSE)
                   )
            {
                continue;
            }
            
            // Adding infoUri (direct link to weather station information) as extra attribute
            String infoUriValue = "";
            if(poiWithAggregate instanceof PointOfInterestWeatherStation)
            {
                String infoUriExpression = ((PointOfInterestWeatherStation) poiWithAggregate).getWeatherStationDataSourceId().getInfoUriExpression();
                if(!infoUriExpression.isEmpty())
                {
                    infoUriValue = String.format(infoUriExpression, ((PointOfInterestWeatherStation) poiWithAggregate).getWeatherStationRemoteId());
                }
            }
            Data infoUri = new Data(infoUriValue);
            infoUri.setName("infoUri");
            List<Data> dataList = new ArrayList<>();
            dataList.add(infoUri);
            Data stationName = new Data(poiWithAggregate.getName());
            stationName.setName("stationName");
            dataList.add(stationName);
            ExtendedData extendedData = document.createAndSetExtendedData()
                    .withData(dataList);
            
            final Placemark placemark = document.createAndAddPlacemark()
            //.withName(poiWithAggregate.getName())
            .withDescription("<![CDATA[Mangler informasjon om varsler for " + poiWithAggregate.getName() + "]]>")
            .withStyleUrl("#warning_type_" 
                    + (poiWithAggregate.getProperties().get("forecastsAggregate") != null ? poiWithAggregate.getProperties().get("forecastsAggregate") : "0")
                )
             .withId(poiWithAggregate.getPointOfInterestId().toString())
             .withExtendedData(extendedData);
            
            
            final Point point = placemark.createAndSetPoint();
            List<Coordinate> coord = point.createAndSetCoordinates();
            
            coord.add(gisUtil.getKMLCoordinateFromJTSCoordinate(poiWithAggregate.getGisGeom().getCoordinate()));
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
        // Because we might be in completely different time zones, 
        // Today must stretch from UTC 00:00 -12h to UTC 24:00 +12h, ie 48 hours
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(SystemTime.getSystemTime());
        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DATE, -1);
        Date startTime = cal.getTime();
        cal.add(Calendar.DATE,2);
        Date endTime = cal.getTime();
        String transactionSQL = new StringBuilder()
                .append("BEGIN;")
                .append("   LOCK TABLE forecast_result_cache;")
                .append("   TRUNCATE forecast_result_cache;")
                .append("   INSERT INTO forecast_result_cache ")
                .append("       SELECT * FROM forecast_result ")
                .append("       WHERE valid_time_start between :startTime and :endTime ")
                .append("       AND forecast_configuration_id IN (")
                .append("           SELECT forecast_configuration_id ")
                .append("           FROM public.forecast_configuration ")
                .append("           WHERE date_start <= :currentDate AND date_end >= :currentDate")
                .append("       );")
                .append("END;")
                .toString();
        
        Query query = em.createNativeQuery(transactionSQL);
        query.setParameter("startTime", startTime);
        query.setParameter("endTime", endTime);
        query.setParameter("currentDate", SystemTime.getSystemTime());
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
        // This 
        List<ForecastConfiguration> activeForecasts = em.createNamedQuery("ForecastConfiguration.findAllActiveAtDate")
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
            TimeZone forecastTimeZone = forecastConfiguration.getTimeZone() != null ?
                    TimeZone.getTimeZone(forecastConfiguration.getTimeZone())
                    : TimeZone.getTimeZone(forecastConfiguration.getVipsLogicUserId().getOrganizationId().getDefaultTimeZone())
                    ;
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
                String dayStamp = format.format(result.getValidTimeStart());
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
                    // PostgreSQL date is a mess. Need to convert to default timezone to get it right
                    aggregateDay = new WeatherUtil().changeDateTimeZone(format.parse(dayStamp), forecastTimeZone, TimeZone.getDefault());
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
            //em.flush();
        }
        
        if(activeForecastIds != null && ! activeForecastIds.isEmpty())
        {
            // Delete all summaries from not active forecasts
            em.createNativeQuery("DELETE FROM forecast_summary "
                    + "WHERE forecast_configuration_id NOT IN :activeForecastIds")
                    .setParameter("activeForecastIds", activeForecastIds)
                    .executeUpdate();
        }
        else
        {
            em.createNativeQuery("TRUNCATE forecast_summary").executeUpdate();
        }
    }
    
    public List<ForecastConfiguration> getForecastConfigurationWithSummaries(List<Long> forecastConfigurationIds)
    {
        List<ForecastConfiguration> retVal = em.createNamedQuery("ForecastConfiguration.findByForecastConfigurationIds")
                .setParameter("forecastConfigurationIds", forecastConfigurationIds)
                .getResultList();
        for(ForecastConfiguration config: retVal)
        {
            Query q = em.createNamedQuery("ForecastSummary.findByForecastConfigurationId");
            config.setForecastSummaries(
                    q.setParameter("forecastConfigurationId", config.getForecastConfigurationId())
                    .getResultList()
            );
        }
        return retVal;
    }
    
    /**
     * Finds the forecast configuration summaries for a given organization and that this user has access to
     * @param organizationId
     * @return 
     */
    public List<ForecastConfiguration> getForecastConfigurationSummaries(Integer organizationId, VipsLogicUser user)
    {
        List<ForecastSummary> summaries = em.createNamedQuery("ForecastSummary.findByOrganizationId")
                .setParameter("organizationId", em.find(Organization.class, organizationId))
                .getResultList();
        Map<Long, List<ForecastSummary>> mappedSummaries = new HashMap<>();
        summaries.forEach((s) -> {
            List<ForecastSummary> summaryForForecast = mappedSummaries.get(s.getForecastSummaryPK().getForecastConfigurationId()) != null ?
                    mappedSummaries.get(s.getForecastSummaryPK().getForecastConfigurationId()) : 
                    new ArrayList<>();
            summaryForForecast.add(s);
            mappedSummaries.put(s.getForecastSummaryPK().getForecastConfigurationId(), summaryForForecast);
        });
        if(mappedSummaries.size() > 0)
        {
            List<ForecastConfiguration> configurations = em.createNamedQuery("ForecastConfiguration.findByForecastConfigurationIds").setParameter("forecastConfigurationIds", mappedSummaries.keySet()).getResultList();
            // Do some authorization.
            configurations = configurations.stream().filter(fc -> this.isUserAuthorizedForForecastConfiguration(fc, user)).collect(Collectors.toList());
            
            configurations.forEach((conf) -> {
                
                conf.setForecastSummaries(mappedSummaries.get(conf.getForecastConfigurationId()));
            });
            return configurations;
        }
        else
        {
            return new ArrayList<>();
        }
    }

    /**
     * Selects the "worst" (highest infection risk) warning status for forecasts
     * running at the pois connected to the given organization
     * @param organizationId Filter for organization
     * @param cropOrganismIds Filter for crops
     * @param theDate Filter for date. If theDate=systemDate, data is fetched from the caching table forecast_result_cache
     * @param user if not null: Include private forecasts for this user
     * @return 
     */
    private List<PointOfInterest> getPointOfInterestForecastsAggregate(
            Integer organizationId, 
            List<Integer> cropOrganismIds, 
            Date theDate,
            VipsLogicUser user
    ) {
        // TODO: More precise gathering of POIs...
        List<PointOfInterest> pois;
        if(organizationId != null && organizationId > 0)
        {
            pois = em.createNamedQuery("PointOfInterest.findForecastLocationsByOrganizationId")
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
        Calendar cal = Calendar.getInstance();
        cal.setTime(theDate);
        WeatherUtil wUtil = new WeatherUtil();
        for(PointOfInterest poi: pois)
        {
            Date midnight = wUtil.normalizeToExactDate(theDate, TimeZone.getTimeZone(poi.getTimeZone()));
            cal.setTime(theDate);
            cal.add(Calendar.DATE, 1);
            Date nextMidnight = cal.getTime();
            
            String sql = "SELECT max(warning_status) FROM " + tableName + " \n" +
                        "WHERE forecast_configuration_id IN( \n" +
                        "	SELECT forecast_configuration_id \n" +
                        "	FROM forecast_configuration \n" +
                        "       WHERE forecast_configuration_id > 0 \n" +
                        
                        (user == null ? 
                        "	AND is_private IS FALSE \n" 
                        :"      AND (is_private IS FALSE OR (is_private IS TRUE AND vips_logic_user_id=:vipsLogicUserId))"
                        ) +
                        "       AND location_point_of_interest_id=:locationPointOfInterestId \n" +
                        (cropOrganismIds != null && ! cropOrganismIds.isEmpty() ? "     AND crop_organism_id IN (" + StringUtils.join(cropOrganismIds, ",") + ") " : "") +
                        ")\n" +
                        "AND valid_time_start between :midnight AND :nextMidnight";
            //System.out.println(poi.getName() + " SQL=" + sql);
            Query q = em.createNativeQuery(sql);
            if(user != null)
            {
                q.setParameter("vipsLogicUserId", user);
            }
            q.setParameter("locationPointOfInterestId", poi.getPointOfInterestId());
            q.setParameter("midnight", midnight);
            q.setParameter("nextMidnight", nextMidnight);
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
            if(forecastConfiguration.getIsPrivate())
            {
                continue;
            }
            mappedForecastConfigurations.put(forecastConfiguration.getForecastConfigurationId(), forecastConfiguration);
            Query q = em.createNativeQuery(
                    "SELECT * FROM forecast_result WHERE forecast_configuration_id = :forecastConfigurationId "
                            + "AND valid_time_start = (SELECT max(valid_time_start) FROM forecast_result WHERE forecast_configuration_id = :forecastConfigurationId)", 
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

    public ForecastConfiguration storeNewMultipleForecastConfiguration(Integer weatherStationPointOfInterestId,Map<String, FormField> formFields, Map<String, FormField> modelSpecificFormFields) {
        ForecastConfiguration forecastConfiguration = new ForecastConfiguration();
        forecastConfiguration.setModelId(formFields.get("modelId").getWebValue());
        forecastConfiguration.setCropOrganismId(em.find(Organism.class, formFields.get("cropOrganismId").getValueAsInteger()));
        forecastConfiguration.setPestOrganismId(em.find(Organism.class, formFields.get("pestOrganismId").getValueAsInteger()));
        forecastConfiguration.setIsPrivate(formFields.get("isPrivate").getWebValue() != null);
        // In the multiple form, location and weatherstation is the same
        PointOfInterest locationPoi = em.find(PointOfInterest.class, weatherStationPointOfInterestId);
        forecastConfiguration.setLocationPointOfInterestId(locationPoi);
        PointOfInterest weatherStationPoi = em.find(PointOfInterestWeatherStation.class, weatherStationPointOfInterestId);
        forecastConfiguration.setWeatherStationPointOfInterestId(weatherStationPoi);
        String timeZone = formFields.get("timeZone").getWebValue();
        forecastConfiguration.setTimeZone(timeZone);
        forecastConfiguration.setDateStart(formFields.get("dateStart").getValueAsDate());
        forecastConfiguration.setDateEnd(formFields.get("dateEnd").getValueAsDate());
        VipsLogicUser forecastConfigurationUser = em.find(VipsLogicUser.class, formFields.get("vipsLogicUserId").getValueAsInteger());
        forecastConfiguration.setVipsCoreUserId(forecastConfigurationUser);
        
        forecastConfiguration = em.merge(forecastConfiguration);
        
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

    public List<ForecastConfiguration> getPrivateForecastConfigurationSummaries(VipsLogicUser user) {
         List<ForecastConfiguration> forecastConfigurations = this.getPrivateForecastConfigurationsForUser(user.getUserId());
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
     * 
     * @return only the models that have an existing preprocessor in VIPSLogic
     */
    public List<ModelInformation> getBatchableModels()
    {
        List<ModelInformation> modelInfos = em.createNamedQuery("ModelInformation.findAll").getResultList();
        return modelInfos.stream()
                .filter(modelInfo -> ModelRunPreprocessorFactory.getModelRunPreprocessor(modelInfo.getModelId()) != null)
                .collect(Collectors.toList());
    }

    public void deleteAllPrivateForecastConfigurationsForUser(VipsLogicUser user) {
        
        em.createNativeQuery(
                "DELETE FROM public.forecast_result WHERE forecast_configuration_id IN "
                + "("
                + "     SELECT forecast_configuration_id "
                + "     FROM public.forecast_configuration "
                + "     WHERE is_private IS TRUE "
                + "     AND vips_logic_user_id = :userId "
                + ")"
        )
        .setParameter("userId", user.getUserId())
        .executeUpdate();
        
        em.createNativeQuery(
                "DELETE FROM public.forecast_configuration "
                + "     WHERE is_private IS TRUE "
                + "     AND vips_logic_user_id = :userId"
        )
        .setParameter("userId", user.getUserId())
        .executeUpdate();
    }

    /**
     * 
     * @param modelId
     * @param year
     * @return 
     */
    public List<ForecastConfiguration> getForecastConfigurationsForModel(String modelId, Integer year) {
        return em.createNamedQuery("ForecastConfiguration.findByModelIdAndYear")
                .setParameter("modelId", modelId)
                .setParameter("year", year)
                .getResultList();
    }

}
