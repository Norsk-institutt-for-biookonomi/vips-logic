/*
 * Copyright (c) 2018 NIBIO <http://www.nibio.no/>. 
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

package no.nibio.vips.logic.service;

import com.vividsolutions.jts.geom.Coordinate;
import com.webcohesion.enunciate.metadata.Facet;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import no.nibio.vips.entity.ModelConfiguration;
import no.nibio.vips.entity.Result;
import no.nibio.vips.entity.WeatherObservation;
import no.nibio.vips.gis.GISUtil;
import no.nibio.vips.logic.entity.ForecastConfiguration;
import no.nibio.vips.logic.entity.ForecastModelConfiguration;
import no.nibio.vips.logic.entity.ForecastModelConfigurationPK;
import no.nibio.vips.logic.entity.Organization;
import no.nibio.vips.logic.entity.PointOfInterestWeatherStation;
import no.nibio.vips.logic.entity.WeatherStationDataSource;
import no.nibio.vips.logic.scheduling.model.PreprocessorException;
import no.nibio.vips.logic.scheduling.model.preprocessor.SeptoriaApiicolaModelPreprocessor;
import no.nibio.vips.logic.scheduling.model.preprocessor.SeptoriaHumidityModelPreprocessor;
import no.nibio.vips.logic.util.RunModelException;
import no.nibio.vips.logic.util.SessionControllerGetter;
import no.nibio.vips.logic.util.SystemTime;
import no.nibio.vips.util.ParseRESTParamUtil;
import no.nibio.vips.util.WeatherElements;
import no.nibio.vips.util.WeatherUtil;
import no.nibio.vips.util.XDate;
import no.nibio.vips.util.weather.WeatherDataSourceException;
import no.nibio.vips.util.weather.WeatherDataSourceUtil;

/**
 * This is a collection of services for models run from forms (not as part of batch) 
 * @copyright 2018 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Path("rest/modelform")
@Facet("restricted")
public class ModelFormService {

    @GET
    @Path("SEPTORIAHU/runmodel")
    @Produces("application/json;charset=UTF-8")
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
            fModelConf.add(this.getForecastModelConfiguration("dateSpraying1", dateSpraying1));
            fModelConf.add(this.getForecastModelConfiguration("dateSpraying2", dateSpraying2));
            fModelConf.add(this.getForecastModelConfiguration("dateGs31", dateGs31));
            fModelConf.add(this.getForecastModelConfiguration("date3rdUpperLeafEmerging", date3rdUpperLeafEmerging));
            fModelConf.add(this.getForecastModelConfiguration("date2ndUpperLeafEmerging", date2ndUpperLeafEmerging));
            fModelConf.add(this.getForecastModelConfiguration("dateUpperLeafEmerging", dateUpperLeafEmerging));
            fModelConf.add(this.getForecastModelConfiguration("dateGs75", dateGs75));
            fModelConf.add(this.getForecastModelConfiguration("thresholdRelativeHumidity", String.valueOf(thresholdRelativeHumidity)));
            fModelConf.add(this.getForecastModelConfiguration("thresholdLeafWetness", String.valueOf(thresholdLeafWetness)));
            fModelConf.add(this.getForecastModelConfiguration("thresholdPrecipitation", String.valueOf(thresholdPrecipitation)));
            fModelConf.add(this.getForecastModelConfiguration("slidingHoursPast", String.valueOf(slidingHoursPast)));
            fModelConf.add(this.getForecastModelConfiguration("slidingHoursAhead", String.valueOf(slidingHoursAhead)));
            fModelConf.add(this.getForecastModelConfiguration("thresholdHumidPeriodHours", String.valueOf(thresholdHumidPeriodHours)));
            fModelConf.add(this.getForecastModelConfiguration("sprayingProtectionDays", String.valueOf(sprayingProtectionDays)));
            fModelConf.add(this.getForecastModelConfiguration("leafLifeTime", String.valueOf(leafLifeTime)));
            fConf.setForecastModelConfigurationSet(fModelConf);
            
            //ModelConfiguration mConf = new ModelConfiguration();
            //mConf.setModelId("SEPTORIAHU");
            //mConf.setConfigParameter("dateSpraying1", dateSpraying1);
            //mConf.setConfigParameter("dateSpraying2", dateSpraying2);
            //mConf.setConfigParameter("dateGs31", dateGs31);
            //mConf.setConfigParameter("date3rdUpperLeafEmerging", date3rdUpperLeafEmerging);
            //mConf.setConfigParameter("date2ndUpperLeafEmerging", date2ndUpperLeafEmerging);
            //mConf.setConfigParameter("dateUpperLeafEmerging", dateUpperLeafEmerging);
            //mConf.setConfigParameter("dateGs75", dateGs75);
            //mConf.setConfigParameter("thresholdRelativeHumidity", thresholdRelativeHumidity);
            //mConf.setConfigParameter("thresholdLeafWetness", thresholdLeafWetness);
            //mConf.setConfigParameter("thresholdPrecipitation", thresholdPrecipitation);
            //mConf.setConfigParameter("slidingHoursPast", slidingHoursPast);
            //mConf.setConfigParameter("slidingHoursAhead", slidingHoursAhead);
            //mConf.setConfigParameter("thresholdHumidPeriodHours", thresholdHumidPeriodHours);
            //mConf.setConfigParameter("sprayingProtectionDays", sprayingProtectionDays);
            //mConf.setConfigParameter("leafLifeTime", leafLifeTime);

            // Data parsing

            Integer organizationId = Integer.valueOf(organizationId_countryCode.split("_")[0]);
            Organization organization = SessionControllerGetter.getUserBean().getOrganization(organizationId);
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
                WeatherStationDataSource wsds = SessionControllerGetter.getPointOfInterestBean().getWeatherStationDataSource("DMI PointWeb");
                ws = new PointOfInterestWeatherStation();
                ws.setWeatherStationDataSourceId(wsds);
                ws.setWeatherStationRemoteId(coordinate.y + "," + coordinate.x);// For some reason, The transformation switches X/Y
                ws.setTimeZone(organization.getDefaultTimeZone());
                
                //observations.stream().forEach(obs->System.out.println(obs.toString()));
            }
            else
            {
                // Weather station id maps to a regular weather station
                ws = (PointOfInterestWeatherStation) SessionControllerGetter.getPointOfInterestBean().getPointOfInterest(Integer.valueOf(weatherStationId));
            }
            fConf.setWeatherStationPointOfInterestId(ws);
            fConf.setLocationPointOfInterestId(ws);
            
            ModelConfiguration mConf = new SeptoriaHumidityModelPreprocessor().getModelConfiguration(fConf);

            Integer VIPSCoreUserId = organization.getDefaultVipsCoreUserId();

            List<Result>results = SessionControllerGetter.getForecastBean().runForecast(mConf, VIPSCoreUserId);

            return Response.ok().entity(results).build();
        }
        catch(PreprocessorException |RunModelException ex)
        {
            return Response.serverError().entity(ex.getMessage()).build();
        } 
    }
    
    private ForecastModelConfiguration getForecastModelConfiguration(String key, String value)
    {
        ForecastModelConfiguration retVal = new ForecastModelConfiguration(new ForecastModelConfigurationPK(-1, key));
        retVal.setParameterValue(value);
        return retVal;
    }
}
