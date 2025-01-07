/*
 * Copyright (c) 2015 NIBIO <http://www.nibio.no/>. 
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

package no.nibio.vips.logic.modules.barleynetblotch;

import com.webcohesion.enunciate.metadata.Facet;

import java.util.*;
import jakarta.ejb.EJB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import no.nibio.vips.entity.ModelConfiguration;
import no.nibio.vips.entity.Result;
import no.nibio.vips.entity.WeatherObservation;
import no.nibio.vips.logic.controller.session.ForecastBean;
import no.nibio.vips.logic.entity.*;
import no.nibio.vips.logic.util.RunModelException;
import no.nibio.vips.logic.util.SystemTime;
import no.nibio.vips.observation.ObservationImpl;
import no.nibio.vips.pestmanagement.SprayingImpl;
import no.nibio.vips.util.ParseRESTParamUtil;
import no.nibio.vips.util.WeatherElements;
import no.nibio.vips.util.weather.WeatherDataSourceException;
import no.nibio.vips.util.weather.WeatherDataSourceUtil;
import org.slf4j.LoggerFactory;

/**
 * @copyright 2015 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Path("rest/barleynetblotchmodel")
@Facet("restricted")
public class BarleyNetBlotchModelService {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(BarleyNetBlotchModelService.class);

    private final static String VIPSCOREMANAGER_URL = System.getProperty("no.nibio.vips.logic.VIPSCOREMANAGER_URL");
    
    @PersistenceContext(unitName="VIPSLogic-PU")
    EntityManager em;
    
    @EJB
    ForecastBean forecastBean;

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
            @QueryParam("weatherDataSourceType") String weatherDataSourceType,
            @QueryParam("latitude") String latitude,
            @QueryParam("longitude") String longitude,
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
        // In case it's just a date string
        if(observationDate == null)
        {
            observationDate = parseUtil.parseISODate(observationDateStr, timeZone);
        }
        Double observationValue = parseUtil.parseDouble(observationValueStr);
        Boolean sameCropAsLastSeason = parseUtil.parseCheckbox(sameCropAsLastSeasonStr);
        Boolean plowed =  parseUtil.parseCheckbox(plowedStr);
        Date sprayingDate = parseUtil.parseISODate(sprayingDateStr, timeZone);
        Integer preparationId = parseUtil.parseInteger(preparationIdStr);
        Double preparationDose = parseUtil.parseDouble(preparationDoseStr);
        Double latitudeDouble = parseUtil.parseDouble(latitude);
        Double longitudeDouble = parseUtil.parseDouble(longitude);

        if(weatherDataSourceType == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("WeatherDataSourceType (grid or weather station) is required").build();
        }

        // Must get the VIPSCore user id for this organization
        Organization org = em.find(Organization.class, organizationId);
        Integer VIPSCoreUserId = org.getDefaultVipsCoreUserId();

        // Build model configuration
        ModelConfiguration config = new ModelConfiguration();
        config.setModelId("BARLEYNETB");

        WeatherDataSourceUtil wsdUtil = new WeatherDataSourceUtil();
        Date endDateForWeatherData = calculateEndDateForWeatherData(timeZone, sowingDate);

        List<WeatherObservation> observations = new ArrayList<>();
        if ("weatherstation".equals(weatherDataSourceType.trim())) {
            PointOfInterestWeatherStation weatherStation =
                em.find(PointOfInterestWeatherStation.class, weatherStationId);
            LOGGER.info("Run model with weatherdata from weatherstation {}", weatherStation.getName());
            try {
                observations = wsdUtil.getWeatherObservations(
                    weatherStation,
                    WeatherObservation.LOG_INTERVAL_ID_1H,
                    new String[] {
                        WeatherElements.TEMPERATURE_MEAN,
                        WeatherElements.PRECIPITATION
                    },
                    sowingDate,
                    endDateForWeatherData
                );
            } catch (WeatherDataSourceException ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
            }
            if (observations == null || observations.isEmpty()) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Could not find weather data for weather station with id=" + weatherStationId).build();
            }
        } else if("grid".equals(weatherDataSourceType.trim())) {
            LOGGER.info("Run model with weatherdata for latitude={} and longitude={}", latitude, longitude);
            String datafetchUriExpression =
                org.getDefaultGridWeatherStationDataSource().getDatafetchUriExpression();
            try {
                observations = wsdUtil.getWeatherObservations(String.format(datafetchUriExpression, longitude + "_" + latitude),
                    WeatherObservation.LOG_INTERVAL_ID_1H,
                    new String[] {
                        WeatherElements.TEMPERATURE_MEAN,
                        WeatherElements.PRECIPITATION
                    },
                    sowingDate,
                    endDateForWeatherData,
                    timeZone,
                    Boolean.FALSE,
                    new HashSet<>(Collections.singletonList(WeatherObservation.LOG_INTERVAL_ID_1H))
                );
            } catch (WeatherDataSourceException ex) {
                LOGGER.error("Exception while getting observations for lat={} lon={}", latitude, longitude, ex);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
            }
            if (observations == null || observations.isEmpty()) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(String.format("Could not find weather data for lat=%s lon=%s between %s and %s", latitude, longitude, sowingDate, endDateForWeatherData)).build();
            }
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
            observation.setObservationData("{\"percentInfectedLeaves\": " + observationValue + "}");
            observation.setTimeOfObservation(observationDate);
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
        
        List<Result> results;
        try
        {
             results = forecastBean.runForecast(config, VIPSCoreUserId);
        }
        catch(RunModelException ex)
        {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
        return Response.ok().entity(results).build();
    }

    /**
     * End date for weather data depends on season. We try to add 5 months to the sowing date.
     * If the resulting date is in the future, we add 10 days to today.
     *
     * @param timeZone  The current timezone
     * @param sowingDate The sowing date
     * @return The end date for weather data
     */
    private static Date calculateEndDateForWeatherData(TimeZone timeZone, Date sowingDate) {
        Date dateOfLastWeatherData;
        Calendar cal = Calendar.getInstance(timeZone);
        cal.setTime(sowingDate);
        cal.add(Calendar.MONTH, 5);
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
        return dateOfLastWeatherData;
    }

}
