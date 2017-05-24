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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import no.nibio.vips.entity.WeatherObservation;
import no.nibio.vips.util.weather.ALabDataParser;
import no.nibio.vips.util.weather.FruitWebDavisDataParser;
import no.nibio.vips.util.weather.MetosRIMProDataParser;
import no.nibio.vips.util.weather.ParseWeatherDataException;
import no.nibio.vips.util.weather.USPestDataParser;
import org.jboss.resteasy.annotations.GZIP;

/**
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Path("rest/weather/proxy")
public class WeatherProxyService {
    @GET
    @POST
    @Path("metos/{stationId}")
    @GZIP
    @Produces("application/json;charset=UTF-8")
    public Response getMetosWeatherData(
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
    
}
