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
import no.bioforsk.vips.logic.entity.ForecastResult;
import no.bioforsk.vips.logic.entity.ModelInformation;
import no.bioforsk.vips.logic.entity.Organization;
import no.bioforsk.vips.logic.entity.VipsLogicUser;
import no.bioforsk.vips.logic.scheduling.model.ForecastConfiguration;
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
     * Returns all forecast results. TODO: put out of use!
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
    
    public void storeForecastConfiguration(ForecastConfiguration forecastConfiguration)
    {
        em.merge(forecastConfiguration);
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
