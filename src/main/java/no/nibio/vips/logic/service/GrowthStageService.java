/*
 * Copyright (c) 2020 NIBIO <http://www.nibio.no/>. 
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import no.nibio.vips.logic.controller.session.PointOfInterestBean;
import no.nibio.vips.logic.entity.Organism;
import no.nibio.vips.logic.entity.PointOfInterestWeatherStation;
import no.nibio.vips.logic.modules.wheatleafblotch.GrowthStageLocationDate;

/**
 * @copyright 2020-2022 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Path("rest/gs")
public class GrowthStageService {
    @Context
    private HttpServletRequest httpServletRequest;
    @PersistenceContext(unitName="VIPSLogic-PU")
    EntityManager em;
    
    @EJB
    PointOfInterestBean pointOfInterestBean;
    
    /**
     * TODO Add response example
     * At what dates can we expect the given crop to be at the given growth stages
     * at the given location?
     * @param organismId
     * @param growthStagesStr
     * @param location
     * @param season
     * @return 
     */
    @GET
    @Path("date/{growthStages}/{organismId}")
    @Produces("application/json;charset=UTF-8")
    public Response getDateForGrowthStage(
            @PathParam("organismId") Integer organismId,
            @PathParam("growthStages") String growthStagesStr,
            @QueryParam("location") String location,
            @QueryParam("season") Integer season
    )
    {
        List<GrowthStageLocationDate> growthStageDatesForLocationAndCrop = this.getGrowthStageDatesForLocationAndCrop(location, organismId);
        //SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        //format.setTimeZone(TimeZone.getTimeZone("Europe/Oslo")); // TODO get from param or default is UTC
        List<Map<Integer,LocalDate>> retVal = new ArrayList<>();
        List<Integer> growthStages = Arrays.stream(growthStagesStr.split(",")).map(str->Integer.valueOf(str)).collect(Collectors.toList());
        growthStages.stream().map((growthStage) -> {
            Map<Integer, LocalDate> gsDate = new HashMap<>();
            gsDate.put(growthStage, this.getDateForGrowthStage(growthStageDatesForLocationAndCrop, growthStage, season));
            return gsDate;
        }).forEachOrdered((gsDate) -> {
            retVal.add(gsDate);
        });        
        return Response.ok().entity(retVal).build();
    }
    
    /**
     * TODO Add response example
     * At what GS is the given organism at the given date and location?
     * @param organismId
     * @param dateStr
     * @param location
     * @return 
     */
    @GET
    @Path("gs/{date}/{organismId}")
    @Produces("application/json;charset=UTF-8")
    public Response getGrowthStageForDate(
            @PathParam("organismId") Integer organismId,
            @PathParam("date") String dateStr,
            @QueryParam("location") String location
    )
    {
        
        
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        LocalDate date = LocalDate.parse(dateStr, formatter);
        List<GrowthStageLocationDate> growthStageDatesForLocationAndCrop = this.getGrowthStageDatesForLocationAndCrop(location, organismId);
        //Integer[] retVal = {this.getGrowthStageForDate(growthStageDatesForLocationAndCrop, date)};
        List<Map<LocalDate,Integer>> retVal = new ArrayList<>();
        Map<LocalDate,Integer> actualRetVal = new HashMap<>();
        actualRetVal.put(date, this.getGrowthStageForDate(growthStageDatesForLocationAndCrop, date));
        retVal.add(actualRetVal);
        return Response.ok().entity(retVal).build();
    }

    /**
     * 
     * @param location
     * @return 
     */
    private Coordinate getLocationCoordinate(String location)
    {
        String[] lonLat = location.split(",");
        Double longitude = Double.valueOf(lonLat[0]);
        Double latitude = Double.valueOf(lonLat[1]);
        Coordinate retVal = new Coordinate(longitude, latitude);
        return retVal;
    }
    
    /**
     * 
     * @param datesForGrowthStages
     * @param date
     * @return 
     */
    private Integer getGrowthStageForDate(List<GrowthStageLocationDate> datesForGrowthStages, LocalDate date)
    {
        Integer dateDayNum = date.getDayOfYear();
        Collections.sort(datesForGrowthStages);
        for(int i=0;i<datesForGrowthStages.size();i++)
        {
            Integer gsDayNum1 = datesForGrowthStages.get(i).getDayNumber();
            // Case 1: Date is before the earliest registered GSDate in DB
            // Case 2: Date is after the latest registered GSDate in DB
            if((i==0 && dateDayNum < gsDayNum1 ) || (i == datesForGrowthStages.size()-1 && dateDayNum > gsDayNum1))
            {
                return null;
            }
            // Case 3: date equals the current list item GSDate: Return as is
            if(dateDayNum.equals(gsDayNum1))
            {
                return datesForGrowthStages.get(i).getGrowthStageLocationDatePK().getGrowthStage();
            }
            
            Integer gsDayNum2 = datesForGrowthStages.get(i+1).getDayNumber();
            // Case 4 date is between current and next GSDate
            if(dateDayNum > gsDayNum1 && dateDayNum < gsDayNum2)
            {
                // We do linear interpolation to find the gs
                Float deltaGS = (float) (datesForGrowthStages.get(i+1).getGrowthStageLocationDatePK().getGrowthStage() - datesForGrowthStages.get(i).getGrowthStageLocationDatePK().getGrowthStage()) 
                        / (gsDayNum2 - gsDayNum1);
                // How many days per gs between the two items?
                //Float deltaDay = (float) (datesForGrowthStages.get(i+1).getDayNumber() - datesForGrowthStages.get(i).getDayNumber()) / (gs2-gs1);
                Integer addedGS = Math.round((dateDayNum - gsDayNum1) * deltaGS);
                return datesForGrowthStages.get(i).getGrowthStageLocationDatePK().getGrowthStage() + addedGS;
            }
            // How many gs per day between the two items?
            //Double deltaGS = (double) (gs2-gs1) / (datesForGrowthStages.get(i+1).getDayNumber() - datesForGrowthStages.get(i).getDayNumber());
        }
        return null;
    }
    
    /**
     * 
     * @param datesForGrowthStages
     * @param growthStage
     * @param season
     * @return 
     */
    private LocalDate getDateForGrowthStage(List<GrowthStageLocationDate> datesForGrowthStages, Integer growthStage, Integer season)
    {
        Collections.sort(datesForGrowthStages);
        for(int i=0;i<datesForGrowthStages.size();i++)
        {
            Integer gs1 = datesForGrowthStages.get(i).getGrowthStageLocationDatePK().getGrowthStage();
            // Case 1: GS is lower than the lowest registered stage in DB
            // Case 2: GS is higher than the highest registered stage in DB
            if((i==0 && growthStage < gs1) || (i == datesForGrowthStages.size()-1 && growthStage > gs1))
            {
                return null;
            }
            // Case 3: GS equals the current list item GS: Return as is
            if(growthStage.equals(gs1))
            {
                return LocalDate.ofYearDay(season, datesForGrowthStages.get(i).getDayNumber());
            }
            
            Integer gs2 = datesForGrowthStages.get(i+1).getGrowthStageLocationDatePK().getGrowthStage();
            // Case 4 GS is between current and next GS
            if(growthStage > gs1 && growthStage < gs2)
            {
                // We do linear interpolation to find the date
            
                // How many days per gs between the two items?
                Float deltaDay = (float) (datesForGrowthStages.get(i+1).getDayNumber() - datesForGrowthStages.get(i).getDayNumber()) / (gs2-gs1);
                Integer addedDays = Math.round((growthStage - gs1) * deltaDay);
                return LocalDate.ofYearDay(season, datesForGrowthStages.get(i).getDayNumber() + addedDays);
            }
            // How many gs per day between the two items?
            //Double deltaGS = (double) (gs2-gs1) / (datesForGrowthStages.get(i+1).getDayNumber() - datesForGrowthStages.get(i).getDayNumber());
        }
        return null;
    }
    
    /**
     * 
     * @param location
     * @param organismId
     * @return 
     */
    private List<GrowthStageLocationDate> getGrowthStageDatesForLocationAndCrop(String location, Integer organismId)
    {
        List<PointOfInterestWeatherStation> stationsWithGrowthStageInfoForCrop = em.createNativeQuery(
        "SELECT poi.*, poiws.* FROM public.point_of_interest_weather_station poiws, public.point_of_interest poi "
        + "WHERE poi.point_of_interest_id IN("
        + "     SELECT point_of_interest_id FROM wheatleafb.growth_stage_location_date "
        + "     WHERE crop_organism_id = :cropOrganismId "
        + ")"
        + "AND poiws.point_of_interest_id = poi.point_of_interest_id", 
        PointOfInterestWeatherStation.class)
        .setParameter("cropOrganismId", organismId)
        .getResultList();
        
        PointOfInterestWeatherStation closestStation = pointOfInterestBean.findClosestWeatherStation(this.getLocationCoordinate(location), stationsWithGrowthStageInfoForCrop);
        return em.createNamedQuery("GrowthStageLocationDate.findByCropOrganismIdAndPointOfInterestId")
                .setParameter("cropOrganismId", em.find(Organism.class, organismId))
                .setParameter("pointOfInterestId", closestStation)
                .getResultList();
    }
}
