/*
 * Copyright (c) 2019 NIBIO <http://www.nibio.no/>. 
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import org.jboss.resteasy.annotations.GZIP;

/**
 * @copyright 2019 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Path("rest/gs")
public class GrowthStageService {
    @Context
    private HttpServletRequest httpServletRequest;
    
    
    @GET
    @Path("date/{growthStages}/{organismId}")
    @Produces("application/json;charset=UTF-8")
    public Response getDateForGrowthStage(
            @PathParam("organismId") Integer organismId,
            @PathParam("growthStages") String growthStagesStr,
            @QueryParam("location") String location
    )
    {
        //SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        //format.setTimeZone(TimeZone.getTimeZone("Europe/Oslo")); // TODO get from param or default is UTC
        List<Map<Integer,LocalDate>> retVal = new ArrayList<>();
        List<Integer> growthStages = Arrays.stream(growthStagesStr.split(",")).map(str->Integer.valueOf(str)).collect(Collectors.toList());
        for(Integer growthStage: growthStages)
        {
            Map<Integer, LocalDate> gsDate = new HashMap<>();
            
            if(growthStage.equals(32))
            {
                gsDate.put(growthStage, LocalDate.of(LocalDate.now().getYear(), Month.JUNE,10));
            }
            else if(growthStage.equals(71))
            {
                gsDate.put(growthStage, LocalDate.of(LocalDate.now().getYear(), Month.JULY,25));
            }
            else
            {
                gsDate.put(growthStage, LocalDate.of(LocalDate.now().getYear(), Month.JANUARY,1));
            }
            retVal.add(gsDate);
        }
        
        
        return Response.ok().entity(retVal).build();
    }
    
    @GET
    @Path("gs/{date}/{organismId}")
    @Produces("application/json;charset=UTF-8")
    public Response getGrowthStageForDate(
            @PathParam("organismId") Integer organismId,
            @PathParam("date") String dateStr,
            @QueryParam("location") String location
    )
    {
        Integer retVal;
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        LocalDate date = LocalDate.parse(dateStr, formatter);
        if(date.isBefore(LocalDate.of(LocalDate.now().getYear(), Month.JULY,1)))
        {
            retVal = 32;
        }
        else
        {
            retVal = 71;
        }
        return Response.ok().entity(retVal).build();
    }
}
