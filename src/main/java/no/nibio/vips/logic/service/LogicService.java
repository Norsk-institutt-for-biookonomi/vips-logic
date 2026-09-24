/*
 * Copyright (c) 2022 NIBIO <http://www.nibio.no/>. 
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import com.webcohesion.enunciate.metadata.Facet;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import no.nibio.vips.entity.WeatherObservation;
import no.nibio.vips.util.CSVPrintUtil;
import no.nibio.vips.util.SolarRadiationUtil;

/**
 * @copyright 2013-2023 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@ApplicationScoped
@Path("")
public class LogicService {

    /**
     * Not ready for production use!
     * @param latitude
     * @param longitude
     * @param startTimeStr
     * @param endTimeStr
     * @param timeZoneStr
     * @param logIntervalId
     * @return 
     */
    @GET
    @Path("weather/calculation/solarradiation/json")
    @Produces("application/json;charset=UTF-8")
    @Facet("restricted")
    public Response getCalculatedSolarRadiationAtLocationAndTimeJSON(
            @QueryParam("latitude") Double latitude, 
            @QueryParam("longitude") Double longitude, 
            @QueryParam("startTime") String startTimeStr, 
            @QueryParam("endTime") String endTimeStr, 
            @QueryParam("timeZone") String timeZoneStr,
            @QueryParam("logIntervalId") Integer logIntervalId
    )
    {
        try
        {

            List<WeatherObservation> radiationValues = getCalculatedSolarRadiationAtLocationAndTime (
                    latitude,
                    longitude,
                    startTimeStr,
                    endTimeStr,
                    timeZoneStr,
                    logIntervalId
            );
            return Response.ok().entity(radiationValues).build();
        }
        catch(ParseException ex)
        {
            return Response.serverError().entity(ex).build();
        }
    }
    
    /**
     * Not ready for production use!
     * @param latitude
     * @param longitude
     * @param startTimeStr
     * @param endTimeStr
     * @param timeZoneStr
     * @param logIntervalId
     * @return 
     */
    @GET
    @Path("weather/calculation/solarradiation/csv")
    @Produces("text/csv;charset=UTF-8")
    @Facet("restricted")
    public Response getCalculatedSolarRadiationAtLocationAndTimeCSV(
            @QueryParam("latitude") Double latitude, 
            @QueryParam("longitude") Double longitude, 
            @QueryParam("startTime") String startTimeStr, 
            @QueryParam("endTime") String endTimeStr, 
            @QueryParam("timeZone") String timeZoneStr,
            @QueryParam("logIntervalId") Integer logIntervalId
    )
    {
        try
        {

            List<WeatherObservation> radiationValues = getCalculatedSolarRadiationAtLocationAndTime (
                    latitude,
                    longitude,
                    startTimeStr,
                    endTimeStr,
                    timeZoneStr,
                    logIntervalId
            );
            //return Response.ok().entity(radiationValues).build();
            TimeZone timeZone = TimeZone.getTimeZone(timeZoneStr);
            return Response.ok(new CSVPrintUtil().printWeatherObservations(radiationValues, timeZone, "\t")).build();
        }
        catch(ParseException ex)
        {
            return Response.serverError().entity(ex).build();
        }
    }
    
    /**
     * Not ready for production use!
     * @param latitude
     * @param longitude
     * @param startTimeStr
     * @param endTimeStr
     * @param timeZoneStr
     * @param logIntervalId
     * @return
     * @throws ParseException 
     */
    private List<WeatherObservation> getCalculatedSolarRadiationAtLocationAndTime (
            Double latitude,
            Double longitude,
            String startTimeStr,
            String endTimeStr,
            String timeZoneStr,
            Integer logIntervalId
    ) throws ParseException
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            TimeZone timeZone = TimeZone.getTimeZone(timeZoneStr);
            Date startTime = format.parse(startTimeStr);
            Date endTime = format.parse(endTimeStr);
            logIntervalId = logIntervalId == null ? WeatherObservation.LOG_INTERVAL_ID_1H : logIntervalId;
            SolarRadiationUtil srUtil = new SolarRadiationUtil();
            return srUtil.getCalculatedSolarRadiation(latitude, longitude, startTime, endTime, timeZone, logIntervalId);
    }
}
