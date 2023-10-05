/*
 * Copyright (c) 2017 NIBIO <http://www.nibio.no/>. 
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

package no.nibio.vips.logic.test.mock;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import no.nibio.vips.entity.ModelConfiguration;
import no.nibio.vips.entity.PointWeatherObservationList;
import no.nibio.vips.entity.Result;
import no.nibio.vips.entity.ResultImpl;
import no.nibio.vips.model.ConfigValidationException;
import no.nibio.vips.model.Model;
import no.nibio.vips.model.ModelExcecutionException;
import no.nibio.vips.model.ModelId;
import no.nibio.vips.util.WeatherElements;
import no.nibio.vips.util.WeatherUtil;

/**
 * 
 * This is made for testing purposes!
 * @copyright 2017 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class MockModel implements Model{
    
    // The minimum number of hours accepted for a daily aggregate
    private final Integer MINIMUM_HOURLY_VALUES = 16;
    
    private List<PointWeatherObservationList> pointWeatherObservationList;
    private TimeZone timeZone;
    
    public final static ModelId MODEL_ID = new ModelId("MOCKMODEL");

    @Override
    public List<Result> getResult() throws ModelExcecutionException {
        if(this.pointWeatherObservationList != null)
        {
            return this.getGridResult();
        }
        
        throw new UnsupportedOperationException("For now, only grid data are supported");
    }
    
    /**
     * A simple wet hour sum calculation for the grid data
     * @return
     * @throws ModelExcecutionException 
     */
    public List<Result> getGridResult() throws ModelExcecutionException{
        Map<Date, Map<Coordinate, Integer>> resultMap = new HashMap<>();
        
        // Calculate the model for each point
        WeatherUtil wUtil = new WeatherUtil();
        GeometryFactory geometryFactory = new GeometryFactory();
        this.pointWeatherObservationList.stream().forEach(oList->{
            //System.out.println("oList coords=" + oList.getCoordinate());
            // We are going to calculate number of wet (risky) hours for a day 
            // including yesterday and tomorrow (72 hours in total) 
            // Integer[0] = number of input data observation hours (0->24), Integer[1] number of wet hours
            Map<Date, Integer[]> wetHours = new HashMap<>();
            DataMatrix pointMatrix = new DataMatrix();
            oList.getObservations().stream().forEach(obs -> {
                    pointMatrix.setParamDoubleValueForDate(obs.getTimeMeasured(), obs.getElementMeasurementTypeId(), obs.getValue());
                }
            );
            pointMatrix.getSortedDateKeys().stream().forEach(timestamp -> {
                Date dayStamp = wUtil.normalizeToExactDate(timestamp, timeZone);
                Integer[] wetHoursForDay = wetHours.get(dayStamp);
                if(wetHoursForDay == null)
                {
                    wetHoursForDay = new Integer[2];
                    wetHoursForDay[0] = 0;
                    wetHoursForDay[1] = 0;
                }
                Double UM = pointMatrix.getParamDoubleValueForDate(timestamp, WeatherElements.RELATIVE_HUMIDITY_MEAN);
                Double TM = pointMatrix.getParamDoubleValueForDate(timestamp, WeatherElements.TEMPERATURE_MEAN);
                if(UM != null && TM != null)
                {
                    wetHoursForDay[0]++; // We add up the number of hours we actually have weather data for
                }
                Double BT = pointMatrix.getParamDoubleValueForDate(timestamp, WeatherElements.LEAF_WETNESS_DURATION);
                Double RR = pointMatrix.getParamDoubleValueForDate(timestamp, WeatherElements.PRECIPITATION);
                if(TM >= 10.0)
                {
                    wetHoursForDay[1] += (BT != null && RR != null) ? 
                                                (UM >= 88.0 && BT >= 30.0 && RR > 0.2) ? 1 : 0
                                            : UM >= 88.0 ? 1 : 0;
                }
                wetHours.put(dayStamp, wetHoursForDay);
            });
            
            //wetHours.keySet().stream().sorted().forEach(dayStamp -> System.out.println(dayStamp + ": " + wetHours.get(dayStamp)[0] + "/" + wetHours.get(dayStamp)[1]));
            List<Date> dayStamps = new ArrayList(wetHours.keySet());
            Collections.sort(dayStamps);
            for(int i=1;i<dayStamps.size()-1; i++)
            {
                // TODO: Check that each day has enough hourly values as aggregate base
                if(
                    wetHours.get(dayStamps.get(i-1))[0] >= this.MINIMUM_HOURLY_VALUES
                    && wetHours.get(dayStamps.get(i))[0] >= this.MINIMUM_HOURLY_VALUES
                    && wetHours.get(dayStamps.get(i+1))[0] >= this.MINIMUM_HOURLY_VALUES
                )
                {
                    // Wohoo, we have a result!
                    Integer wetHourSumForDayAndPoint = 
                            wetHours.get(dayStamps.get(i-1))[1]
                            + wetHours.get(dayStamps.get(i))[1]
                            + wetHours.get(dayStamps.get(i+1))[1];
                    Map<Coordinate, Integer> pointAndDateResult = resultMap.get(dayStamps.get(i));
                    if(pointAndDateResult == null)
                    {
                        pointAndDateResult = new HashMap<>();
                    }
                    pointAndDateResult.put(oList.getCoordinate(), wetHourSumForDayAndPoint);
                    resultMap.put(dayStamps.get(i), pointAndDateResult);
                }
            }
        });
        
        List<Result> retVal = new ArrayList<>();
        resultMap.entrySet().stream().forEach(timeEntry -> {
            timeEntry.getValue().entrySet().stream().forEach(pointEntry -> {
                Result r = new ResultImpl();
                r.setValidTimeStart(timeEntry.getKey());
                Coordinate c = new Coordinate(pointEntry.getKey().x, pointEntry.getKey().y);
                r.setValidGeometry(geometryFactory.createPoint(c));
                r.setValue(MockModel.MODEL_ID.toString(), DataMatrix.WET_HOUR_SUM, pointEntry.getValue().toString());
                retVal.add(r);
            });
        });
        
        // Iterate through dates
        return retVal;
    }

    @Override
    public ModelId getModelId() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getModelName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getModelName(String language) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getLicense() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getCopyright() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getModelDescription() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getModelDescription(String language) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getWarningStatusInterpretation() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getWarningStatusInterpretation(String language) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getModelUsage() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getModelUsage(String language) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getSampleConfig() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setConfiguration(ModelConfiguration config) throws ConfigValidationException {
        pointWeatherObservationList = (List<PointWeatherObservationList>) config.getConfigParameter("multiPointWeatherObservations");
        this.timeZone = TimeZone.getTimeZone((String)config.getConfigParameter("timeZone"));
    }

}
