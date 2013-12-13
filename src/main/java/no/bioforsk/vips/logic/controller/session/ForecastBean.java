package no.bioforsk.vips.logic.controller.session;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import no.bioforsk.vips.coremanager.service.ManagerResource;
import no.bioforsk.vips.entity.Result;
import no.bioforsk.vips.logic.entity.ForecastResult;
import no.bioforsk.vips.logic.entity.ModelInformation;
import no.bioforsk.vips.logic.entity.Organization;
import no.bioforsk.vips.logic.entity.PointOfInterest;
import no.bioforsk.vips.logic.entity.PointOfInterestWeatherStation;
import no.bioforsk.vips.logic.entity.VipsLogicUser;
import no.bioforsk.vips.logic.entity.ForecastConfiguration;
import no.bioforsk.vips.logic.entity.ForecastModelConfiguration;
import no.bioforsk.web.forms.FormField;
import org.codehaus.jackson.JsonNode;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

/**
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
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
        ForecastConfiguration config = this.getForecastConfiguration(forecastConfigurationId);
        Query q = em.createNamedQuery("ForecastResult.findByForecastConfigurationId");
        q.setParameter("forecastConfigurationId", config);
        return q.getResultList();
        
    }
    
    /**
     * Deletes all former results for this forecast configuration, stores the new ones
     * @param forecastConfiguration
     * @param results 
     */
    public void storeResults(ForecastConfiguration forecastConfiguration, List<Result> results)
    {
        System.out.println("forecastConfigurationId=" + forecastConfiguration.getForecastConfigurationId());
        Query q = em.createNativeQuery("DELETE FROM public.forecast_result WHERE forecast_configuration_id=:forecastConfigurationId");
        q.setParameter("forecastConfigurationId", forecastConfiguration.getForecastConfigurationId());
        q.executeUpdate();
        for(Result result:results)
        {
            ForecastResult fResult = new ForecastResult(forecastConfiguration,result);
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
        Query q = em.createNamedQuery("ForecastConfiguration.findByVipsCoreUserId");
        q.setParameter("vipsCoreUserId", userId);
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
    
    public List<ForecastConfiguration> getForecastConfigurations(Organization organization)
    {
        List<VipsLogicUser> organizationUsers = em
                                                .createNamedQuery("VipsLogicUser.findByOrganizationId")
                                                .setParameter("organizationId", organization)
                                                .getResultList();
        return em.createNamedQuery("ForecastConfiguration.findByVipsLogicUserIds").setParameter("vipsLogicUserIds", organizationUsers).getResultList();
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
                String modelId = modelIdItem.getValueAsText();
                
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
        em.remove(em.find(ForecastConfiguration.class, forecastConfigurationId));
    }
    
    /**
     * Get the interface for REST resources in VIPSCoreManager
     * @return 
     */
    private ManagerResource getManagerResource()
    {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(System.getProperty("no.bioforsk.vips.coremanager.VIPSCOREMANAGER_URL"));
        ResteasyWebTarget rTarget = (ResteasyWebTarget) target;
        ManagerResource resource = rTarget.proxy(ManagerResource.class);
        return resource;
    }
}
