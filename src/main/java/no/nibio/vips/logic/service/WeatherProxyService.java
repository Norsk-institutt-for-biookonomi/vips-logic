/*
 * Copyright (c) 2016 NIBIO <http://www.nibio.no/>. 
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

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import no.nibio.vips.entity.PointWeatherObservationList;
import no.nibio.vips.entity.WeatherObservation;
import no.nibio.vips.logic.util.SessionControllerGetter;
import no.nibio.vips.logic.util.SystemTime;
import no.nibio.vips.util.weather.ALabDataParser;
import no.nibio.vips.util.weather.FruitWebDavisDataParser;
import no.nibio.vips.util.weather.MetosAPIDataParser;
import no.nibio.vips.util.weather.MetosRIMProDataParser;
import no.nibio.vips.util.weather.ParseWeatherDataException;
import no.nibio.vips.util.weather.USPestDataParser;
import no.nibio.vips.util.weather.YrWeatherForecastProvider;
import no.nibio.vips.util.weather.dnmipointweb.DMIPointWebDataParser;
import no.nibio.vips.util.weather.metnothredds.MetNoThreddsDataParser;
import org.jboss.resteasy.annotations.GZIP;

/**
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Path("rest/weather/proxy")
public class WeatherProxyService {
    @GET
    @POST
    @Path("metosrimpro/{stationId}")
    @GZIP
    @Produces("application/json;charset=UTF-8")
    public Response getMetosRIMProWeatherData(
            @PathParam("stationId") String stationId, 
            @FormParam("timeZone") String timeZonePOST, 
            @QueryParam("timeZone") String timeZoneGET, 
            @FormParam("startDate") String startDatePOST,
            @QueryParam("startDate") String startDateGET
            )
    {
        List<WeatherObservation> observations;
        try 
        {
            String timeZoneParam = timeZonePOST != null ? timeZonePOST : timeZoneGET != null ? timeZoneGET : "UTC";
            TimeZone timeZone = TimeZone.getTimeZone(timeZoneParam);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            format.setTimeZone(timeZone);
            String startDateParam = startDatePOST != null ? startDatePOST : startDateGET;
            Date startDate1 = format.parse(startDateParam);
            observations = new MetosRIMProDataParser().getWeatherObservations(stationId, timeZone, startDate1);
        } catch (ParseException | ParseWeatherDataException | NullPointerException ex) {
            return Response.serverError().entity(ex).build();
        }
        return Response.ok().entity(observations).build();
    }
    
    @GET
    @POST
    @Path("metosapi/{stationId}")
    @GZIP
    @Produces("application/json;charset=UTF-8")
    public Response getMetosAPIWeatherData(
            @PathParam("stationId") String stationId, 
            @FormParam("timeZone") String timeZonePOST, 
            @QueryParam("timeZone") String timeZoneGET, 
            @FormParam("startDate") String startDatePOST,
            @QueryParam("startDate") String startDateGET
            )
    {
        List<WeatherObservation> observations;
        try 
        {
            String timeZoneParam = timeZonePOST != null ? timeZonePOST : timeZoneGET != null ? timeZoneGET : "UTC";
            TimeZone timeZone = TimeZone.getTimeZone(timeZoneParam);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            format.setTimeZone(timeZone);
            String startDateParam = startDatePOST != null ? startDatePOST : startDateGET;
            Date startDate1 = format.parse(startDateParam);
            observations = new MetosAPIDataParser().getWeatherObservations(stationId, timeZone, startDate1);
        } catch (ParseException | ParseWeatherDataException | NullPointerException ex) {
            return Response.serverError().entity(ex).build();
        }
        return Response.ok().entity(observations).build();
    }
    
    @GET
    @POST
    @Path("fruitwebdavis/{stationId}")
    @GZIP
    @Produces("application/json;charset=UTF-8")
    public Response getFruitWebDavisWeatherData(
            @PathParam("stationId") String stationId, 
            @FormParam("timeZone") String timeZonePOST, 
            @QueryParam("timeZone") String timeZoneGET, 
            @FormParam("startDate") String startDatePOST,
            @QueryParam("startDate") String startDateGET
            )
    {
        List<WeatherObservation> observations;
        try 
        {
            String timeZoneParam = timeZonePOST != null ? timeZonePOST : timeZoneGET != null ? timeZoneGET : "UTC";
            TimeZone timeZone = TimeZone.getTimeZone(timeZoneParam);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            format.setTimeZone(timeZone);
            String startDateParam = startDatePOST != null ? startDatePOST : startDateGET;
            Date startDate1 = format.parse(startDateParam);
            observations = new FruitWebDavisDataParser().getWeatherObservations(URLDecoder.decode(stationId, "UTF-8"), timeZone, startDate1);
        } catch (ParseException | ParseWeatherDataException | NullPointerException | UnsupportedEncodingException ex) {
            return Response.serverError().entity(ex).build();
        }
        return Response.ok().entity(observations).build();
    }
    
    
    @GET
    @POST
    @Path("alab/{stationId}")
    @GZIP
    @Produces("application/json;charset=UTF-8")
    public Response getALabWeatherData(
            @PathParam("stationId") String stationId, 
            @FormParam("timeZone") String timeZonePOST, 
            @QueryParam("timeZone") String timeZoneGET, 
            @FormParam("startDate") String startDatePOST,
            @QueryParam("startDate") String startDateGET,
            @FormParam("userName") String userNamePOST,
            @QueryParam("userName") String userNameGET,
            @FormParam("password") String passwordPOST,
            @QueryParam("password") String passwordGET
            )
    {
        List<WeatherObservation> observations;
        try 
        {
            String timeZoneParam = timeZonePOST != null ? timeZonePOST : timeZoneGET != null ? timeZoneGET : "UTC";
            TimeZone timeZone = TimeZone.getTimeZone(timeZoneParam);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            format.setTimeZone(timeZone);
            String startDateParam = startDatePOST != null ? startDatePOST : startDateGET;
            Date startDate1 = format.parse(startDateParam);
            String userName = userNamePOST != null ? userNamePOST:userNameGET;
            String password = passwordPOST != null ? passwordPOST:passwordGET;
            observations = new ALabDataParser().getWeatherObservations(stationId, timeZone, startDate1, userName, password);
        } catch (ParseException | ParseWeatherDataException | NullPointerException ex) {
            return Response.serverError().entity(ex).build();
        }
        return Response.ok().entity(observations).build();
    }
    
    @GET
    @POST
    @Path("uspest/{stationId}")
    @GZIP
    @Produces("application/json;charset=UTF-8")
    public Response getUSPestWeatherData(
            @PathParam("stationId") String stationId, 
            @FormParam("timeZone") String timeZonePOST, 
            @QueryParam("timeZone") String timeZoneGET, 
            @FormParam("startDate") String startDatePOST,
            @QueryParam("startDate") String startDateGET
            )
    {
        List<WeatherObservation> observations;
        try 
        {
            String timeZoneParam = timeZonePOST != null ? timeZonePOST : timeZoneGET != null ? timeZoneGET : "UTC";
            TimeZone timeZone = TimeZone.getTimeZone(timeZoneParam);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            format.setTimeZone(timeZone);
            String startDateParam = startDatePOST != null ? startDatePOST : startDateGET;
            Date startDate1 = format.parse(startDateParam);
            observations = new USPestDataParser().getWeatherObservations(stationId, timeZone, startDate1);
        } catch (ParseException | ParseWeatherDataException | NullPointerException ex) {
            return Response.serverError().entity(ex).build();
        }
        return Response.ok().entity(observations).build();
    }
    
    /**
     * Getting data from the Danish Meteorological Institute's PointWeb weather service
     * @param stationId
     * @param timeZonePOST
     * @param timeZoneGET
     * @param startDatePOST
     * @param startDateGET
     * @param endDatePOST
     * @param endDateGET
     * @return 
     */
    @GET
    @POST
    @Path("dmipointweb/{stationId}")
    @GZIP
    @Produces("application/json;charset=UTF-8")
    public Response getDMIPointWebWeatherData(
            @PathParam("stationId") String stationId, 
            @FormParam("timeZone") String timeZonePOST, 
            @QueryParam("timeZone") String timeZoneGET, 
            @FormParam("startDate") String startDatePOST,
            @QueryParam("startDate") String startDateGET,
            @FormParam("endDate") String endDatePOST,
            @QueryParam("endDate") String endDateGET
    )
    {
        // Station is id'ed by decimal degrees position
        // Format is &stationId=long,lat. E.g. &stationId=9.583,56.488 (sample is from Follum, Denmark)
        List<WeatherObservation> observations;
        try 
        {
            String timeZoneParam = timeZonePOST != null ? timeZonePOST : timeZoneGET != null ? timeZoneGET : "UTC";
            TimeZone timeZone = TimeZone.getTimeZone(timeZoneParam);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            format.setTimeZone(timeZone);
            String startDateParam = startDatePOST != null ? startDatePOST : startDateGET;
            Date startDate1 = format.parse(startDateParam);
            String endDateParam = endDatePOST != null ? endDatePOST : endDateGET;
            Date endDate1;
            if(endDateParam != null)
            {
                endDate1 = format.parse(endDateParam);
            }
            else
            {
                // Expecting endDate to be 6 days after today if not specified
                Calendar cal = Calendar.getInstance();
                cal.setTime(SystemTime.getSystemTime());
                cal.add(Calendar.DATE, 6);
                endDate1 = cal.getTime();
            }
            String[] coordinates = stationId.split(",");
            Double longitude = Double.parseDouble(coordinates[0]);
            Double latitude = Double.parseDouble(coordinates[1]);
            observations = new DMIPointWebDataParser().getData(longitude, latitude, startDate1, endDate1);
        } catch (ParseException | NullPointerException ex) {
            return Response.serverError().entity(ex).build();
        }
        return Response.ok().entity(observations).build();
    }
    
    @GET
    @POST
    @Path("forecasts/yr/")
    @GZIP
    @Produces("application/json;charset=UTF-8")
    public Response getYRForecasts(
                    @QueryParam("longitude") Double longitude,
                    @QueryParam("latitude") Double latitude,
                    @QueryParam("altitude") Double altitude
    )
    {
        try
        {
            return Response.ok().entity(new YrWeatherForecastProvider().getWeatherForecasts(longitude, latitude, altitude)).build();
        }
        catch(ParseWeatherDataException ex)
        {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
    }
    
    @GET
    @Path("metno/thredds/point/")
    @GZIP
    @Produces("application/json;charset=UTF-8")
    public Response getMetNoThreddsPointData(
            @QueryParam("longitude") Double longitude,
            @QueryParam("latitude") Double latitude,
            @QueryParam("timeZone") String timeZoneStr,
            @QueryParam("startDate") String startDateStr,
            @QueryParam("startTime") String startTimeStr,
            @QueryParam("endDate") String endDateStr,
            @QueryParam("endTime") String endTimeStr,
            @QueryParam("elementMeasurementTypes") String[] elementMeasurementTypes 
            
    )
    {
        try
        {
            TimeZone timeZone = TimeZone.getTimeZone(timeZoneStr);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH");
            format.setTimeZone(timeZone);
            startTimeStr = startTimeStr != null ? startTimeStr : "00";
            endTimeStr = endTimeStr != null ? endTimeStr : "23";

            Date startDate = format.parse(startDateStr + " " + startTimeStr);
            Date endDate = format.parse(endDateStr + " " + endTimeStr);
            MetNoThreddsDataParser dp = new MetNoThreddsDataParser();
            List<WeatherObservation> retVal = dp.getPointData(longitude, latitude, startDate, endDate, Arrays.asList(elementMeasurementTypes));
            Collections.sort(retVal);
            return Response.ok().entity(retVal).build();
        }
        catch(ParseException pe)
        {
            return Response.status(Response.Status.BAD_REQUEST).entity(pe.getMessage()).build();
        }
    }
    
    /**
     * This is a real memory hog, use only if you know how!!
     * Sample test with wget:
     * wget --header='Accept-Encoding: gzip' "http://logic.vips.nibio.no/rest/weather/proxy/metno/thredds/grid?elementMeasurementTypes=Q0&north=57.91&south=54.36&west=8.04&east=12.83&startDate=2017-03-01&endDate=2017-10-06&timeZone=Europe/Oslo" -O ./denmark_q0.gz
     * @param north
     * @param south
     * @param east
     * @param west
     * @param timeZoneStr
     * @param startDateStr
     * @param startTimeStr
     * @param endDateStr
     * @param endTimeStr
     * @param elementMeasurementTypes
     * @return 
     */
    @GET
    @Path("metno/thredds/grid/")
    @GZIP
    @Produces("application/json;charset=UTF-8")
    public Response getMetNoThreddsGridData(
            @QueryParam("north") Double north,
            @QueryParam("south") Double south,
            @QueryParam("east") Double east,
            @QueryParam("west") Double west,
            @QueryParam("timeZone") String timeZoneStr,
            @QueryParam("startDate") String startDateStr,
            @QueryParam("startTime") String startTimeStr,
            @QueryParam("endDate") String endDateStr,
            @QueryParam("endTime") String endTimeStr,
            @QueryParam("elementMeasurementTypes") String[] elementMeasurementTypes 
            
    )
    {
        try
        {
            TimeZone timeZone = TimeZone.getTimeZone(timeZoneStr);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH");
            format.setTimeZone(timeZone);
            startTimeStr = startTimeStr != null ? startTimeStr : "00";
            endTimeStr = endTimeStr != null ? endTimeStr : "23";

            Date startDate = format.parse(startDateStr + " " + startTimeStr);
            Date endDate = format.parse(endDateStr + " " + endTimeStr);
            MetNoThreddsDataParser dp = new MetNoThreddsDataParser();
            
            // Creating an envelope with the given bounds
            Envelope envelope = new Envelope(west, east, south, north);
            GeometryFactory gf = new GeometryFactory();
            List<PointWeatherObservationList> retVal = dp.getGridData(gf.toGeometry(envelope), 10.0, startDate, endDate, Arrays.asList(elementMeasurementTypes));
            return Response.ok().entity(retVal).build();
        }
        catch(ParseException pe)
        {
            return Response.status(Response.Status.BAD_REQUEST).entity(pe.getMessage()).build();
        }
    }
}
