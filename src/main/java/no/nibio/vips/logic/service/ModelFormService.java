/*
 * Copyright (c) 2018 NIBIO <http://www.nibio.no/>. 
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

package no.nibio.vips.logic.service;

import com.webcohesion.enunciate.metadata.rs.TypeHint;
import org.locationtech.jts.geom.Coordinate;
import com.webcohesion.enunciate.metadata.Facet;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import no.nibio.vips.entity.ModelConfiguration;
import no.nibio.vips.entity.Result;
import no.nibio.vips.entity.WeatherObservation;
import no.nibio.vips.gis.GISUtil;
import no.nibio.vips.logic.controller.session.ForecastBean;
import no.nibio.vips.logic.controller.session.PointOfInterestBean;
import no.nibio.vips.logic.controller.session.UserBean;
import no.nibio.vips.logic.entity.ForecastConfiguration;
import no.nibio.vips.logic.entity.ForecastModelConfiguration;
import no.nibio.vips.logic.entity.ForecastModelConfigurationPK;
import no.nibio.vips.logic.entity.Organization;
import no.nibio.vips.logic.entity.PointOfInterestWeatherStation;
import no.nibio.vips.logic.entity.WeatherStationDataSource;
import no.nibio.vips.logic.scheduling.model.PreprocessorException;
import no.nibio.vips.logic.scheduling.model.preprocessor.SeptoriaHumidityModelPreprocessor;
import no.nibio.vips.logic.util.RunModelException;
import no.nibio.vips.logic.util.SystemTime;
import no.nibio.vips.util.ParseRESTParamUtil;
import no.nibio.vips.util.WeatherUtil;
import no.nibio.vips.util.XDate;
import no.nibio.vips.util.weather.WeatherDataSourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a collection of (one) service(s) for models run from forms (not as part of batch)
 * @copyright 2018-2022 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Path("rest/modelform")
@Facet("restricted")
public class ModelFormService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelFormService.class);


    @EJB
    UserBean userBean;
    @EJB
    PointOfInterestBean pointOfInterestBean;
    @EJB
    ForecastBean forecastBean;

    /**
     * Run the SEPTORIAHU (from SEGES, Denmark) model from a form, data are posted here.
     *
     * See <a href="https://www.vips-landbruk.no/blotch/septoriahumidity/">the form</a> for reference
     *
     * @param organizationId_countryCode
     * @param weatherStationId
     * @param dateSpraying1
     * @param dateSpraying2
     * @param dateGs31
     * @param date3rdUpperLeafEmerging
     * @param date2ndUpperLeafEmerging
     * @param dateUpperLeafEmerging
     * @param dateGs75
     * @param thresholdRelativeHumidity
     * @param thresholdLeafWetness
     * @param thresholdPrecipitation
     * @param slidingHoursPast
     * @param slidingHoursAhead
     * @param thresholdHumidPeriodHours
     * @param sprayingProtectionDays
     * @param leafLifeTime
     * @return
     */
    @GET
    @Path("SEPTORIAHU/runmodel")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(Result.class)
    public Response runSeptoriaHumidityModel(
            @QueryParam("organizationId_countryCode") String organizationId_countryCode,
            @QueryParam("weatherStationId") String weatherStationId, // Could be special ID from Danish system,
            @QueryParam("dateSpraying1") String dateSpraying1,
            @QueryParam("dateSpraying2") String dateSpraying2,
            @QueryParam("dateGs31") String dateGs31,
            @QueryParam("date3rdUpperLeafEmerging") String date3rdUpperLeafEmerging,
            @QueryParam("date2ndUpperLeafEmerging") String date2ndUpperLeafEmerging,
            @QueryParam("dateUpperLeafEmerging") String dateUpperLeafEmerging,
            @QueryParam("dateGs75") String dateGs75,
            @QueryParam("thresholdRelativeHumidity") Double thresholdRelativeHumidity,
            @QueryParam("thresholdLeafWetness") Double thresholdLeafWetness,
            @QueryParam("thresholdPrecipitation") Double thresholdPrecipitation,
            @QueryParam("slidingHoursPast") Integer slidingHoursPast,
            @QueryParam("slidingHoursAhead") Integer slidingHoursAhead,
            @QueryParam("thresholdHumidPeriodHours") Integer thresholdHumidPeriodHours,
            @QueryParam("sprayingProtectionDays") Integer sprayingProtectionDays,
            @QueryParam("leafLifeTime") Integer leafLifeTime
    ){
        try
        {
            ForecastConfiguration fConf = new ForecastConfiguration();
            fConf.setModelId("SEPTORIAHU");
            
            Set<ForecastModelConfiguration> fModelConf = new HashSet<>();
            fModelConf.add(this.getForecastModelConfiguration(fConf.getModelId(),"dateSpraying1", dateSpraying1));
            fModelConf.add(this.getForecastModelConfiguration(fConf.getModelId(),"dateSpraying2", dateSpraying2));
            fModelConf.add(this.getForecastModelConfiguration(fConf.getModelId(),"dateGs31", dateGs31));
            fModelConf.add(this.getForecastModelConfiguration(fConf.getModelId(),"date3rdUpperLeafEmerging", date3rdUpperLeafEmerging));
            fModelConf.add(this.getForecastModelConfiguration(fConf.getModelId(),"date2ndUpperLeafEmerging", date2ndUpperLeafEmerging));
            fModelConf.add(this.getForecastModelConfiguration(fConf.getModelId(),"dateUpperLeafEmerging", dateUpperLeafEmerging));
            fModelConf.add(this.getForecastModelConfiguration(fConf.getModelId(),"dateGs75", dateGs75));
            fModelConf.add(this.getForecastModelConfiguration(fConf.getModelId(),"thresholdRelativeHumidity", String.valueOf(thresholdRelativeHumidity)));
            fModelConf.add(this.getForecastModelConfiguration(fConf.getModelId(),"thresholdLeafWetness", String.valueOf(thresholdLeafWetness)));
            fModelConf.add(this.getForecastModelConfiguration(fConf.getModelId(),"thresholdPrecipitation", String.valueOf(thresholdPrecipitation)));
            fModelConf.add(this.getForecastModelConfiguration(fConf.getModelId(),"slidingHoursPast", String.valueOf(slidingHoursPast)));
            fModelConf.add(this.getForecastModelConfiguration(fConf.getModelId(),"slidingHoursAhead", String.valueOf(slidingHoursAhead)));
            fModelConf.add(this.getForecastModelConfiguration(fConf.getModelId(),"thresholdHumidPeriodHours", String.valueOf(thresholdHumidPeriodHours)));
            fModelConf.add(this.getForecastModelConfiguration(fConf.getModelId(),"sprayingProtectionDays", String.valueOf(sprayingProtectionDays)));
            fModelConf.add(this.getForecastModelConfiguration(fConf.getModelId(),"leafLifeTime", String.valueOf(leafLifeTime)));
            fConf.setForecastModelConfigurationSet(fModelConf);
            
            // Data parsing

            Integer organizationId = Integer.valueOf(organizationId_countryCode.split("_")[0]);
            Organization organization = userBean.getOrganization(organizationId);
            fConf.setTimeZone(organization.getDefaultTimeZone());
            TimeZone timeZone =  TimeZone.getTimeZone(organization.getDefaultTimeZone());
            ParseRESTParamUtil pUtil = new ParseRESTParamUtil();
            // Start time is gs31, easy
            Date gs31 = pUtil.parseISODate(dateGs31,timeZone);
            XDate startTime = new XDate(gs31);
            startTime.addDays(-1);
            // End time is whatever comes first of the day after tomorrow or Gs75
            Date gs75 = pUtil.parseISODate(dateGs75, timeZone);
            XDate dayAfterTomorrow = new XDate(SystemTime.getSystemTime());
            dayAfterTomorrow.addDays(2);
            WeatherUtil wUtil = new WeatherUtil();
            dayAfterTomorrow = new XDate(wUtil.pragmaticAdjustmentToMidnight(dayAfterTomorrow, timeZone));
            // The first check here is to see if the systemtime is too early
            Date endTime = dayAfterTomorrow.after(gs75) ? gs75 : dayAfterTomorrow;
            
            fConf.setDateStart(startTime);
            fConf.setDateEnd(endTime);

            String countryCode = organizationId_countryCode.split("_")[1];
            List<WeatherObservation> observations;
            WeatherDataSourceUtil wdsUtil = new WeatherDataSourceUtil();
            PointOfInterestWeatherStation ws;
            if(countryCode.toLowerCase().equals("dk")){
                // Create a synthetic weather station to pass into the system
                // Weather station id is a UTM32N coordinate, e.g. E552700N6322400
                String[] parts = weatherStationId.split("N");
                Integer UTM32vE = Integer.valueOf(parts[0].substring(1));
                Integer UTM32vN = Integer.valueOf(parts[1]);
                GISUtil gisUtil = new GISUtil();
                Coordinate UTMc = new Coordinate(UTM32vE, UTM32vN); 
                Coordinate coordinate = gisUtil.convertCoordinate(UTMc, "EPSG:32632", "EPSG:4326");
                WeatherStationDataSource wsds = pointOfInterestBean.getWeatherStationDataSource("DMI PointWeb");
                ws = new PointOfInterestWeatherStation();
                ws.setWeatherStationDataSourceId(wsds);
                ws.setWeatherStationRemoteId(coordinate.y + "," + coordinate.x);// For some reason, The transformation switches X/Y
                ws.setTimeZone(organization.getDefaultTimeZone());
                
                //observations.stream().forEach(obs->System.out.println(obs.toString()));
            }
            else
            {
                // Weather station id maps to a regular weather station
                ws = (PointOfInterestWeatherStation) pointOfInterestBean.getPointOfInterest(Integer.valueOf(weatherStationId));
            }
            fConf.setWeatherStationPointOfInterestId(ws);
            fConf.setLocationPointOfInterestId(ws);
            
            ModelConfiguration mConf = new SeptoriaHumidityModelPreprocessor().getModelConfiguration(fConf);

            Integer VIPSCoreUserId = organization.getDefaultVipsCoreUserId();

            List<Result> results = forecastBean.runForecast(mConf, VIPSCoreUserId);

            return Response.ok().entity(results).build();
        }
        catch(PreprocessorException |RunModelException ex)
        {
            LOGGER.error("Exception occurred when attempting to run the septoria humidity model", ex);
            return Response.serverError().entity(ex.getMessage()).build();
        }
    }
    
    private ForecastModelConfiguration getForecastModelConfiguration(String modelId, String key, String value)
    {
        ForecastModelConfiguration retVal = new ForecastModelConfiguration(new ForecastModelConfigurationPK(-1, forecastBean.getDeCamelizedFieldName(modelId, key)));
        retVal.setParameterValue(value);
        return retVal;
    }
}
