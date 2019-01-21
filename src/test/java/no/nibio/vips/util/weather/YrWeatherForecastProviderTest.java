/*
 * Copyright (c) 2015 NIBIO <http://www.nibio.no/>. 
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
package no.nibio.vips.util.weather;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import java.util.Collections;
import java.util.List;
import no.nibio.vips.entity.WeatherObservation;
import no.nibio.vips.logic.entity.PointOfInterestWeatherStation;
import no.nibio.vips.gis.GISUtil;
import no.nibio.vips.util.WeatherUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author treinar
 */
public class YrWeatherForecastProviderTest {
    
    public YrWeatherForecastProviderTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getWeatherForecasts method, of class YrWeatherForecastProvider.
     */
    @Test
    public void testGetWeatherForecasts() throws Exception {
        System.out.println("getWeatherForecasts");
        PointOfInterestWeatherStation weatherStation = new PointOfInterestWeatherStation();
        //Testing in Bosnia (Rodoc, Mostar)
        GISUtil gisUtil = new GISUtil();
        Point p = gisUtil.createPointWGS84(new Coordinate(17.8094, 43.301, 67.0));
        weatherStation.setAltitude(67.0);
        weatherStation.setLongitude(17.8094);
        weatherStation.setLatitude(43.301);
        weatherStation.setGisGeom(p);
        YrWeatherForecastProvider instance = new YrWeatherForecastProvider();
        try
        {
            List<WeatherObservation> result = instance.getWeatherForecasts(weatherStation);
            /*Collections.sort(result);
            for(WeatherObservation obs:result)
            {
                System.out.println(obs.toString());
            }*/
            assertNotNull(result);

            // Testing in Norway
            //weatherStation.setAltitude(110.0);
            //weatherStation.setLongitude(10.83474668);
            //weatherStation.setLatitude(59.63851390);
            weatherStation.setAltitude(102.0);
            weatherStation.setLongitude(11.39);
            weatherStation.setLatitude(59.388);
            result = instance.getWeatherForecasts(weatherStation);
            assertNotNull(result);
            Collections.sort(result);
            for(WeatherObservation obs:result)
            {
                if(obs.getElementMeasurementTypeId().equals("TM"))
                System.out.println(obs.toString());
            }
            
            WeatherUtil wUtil = new WeatherUtil();
            //wUtil.checkForAndFixHourlyTimeSeriesHoles(result);
            
            // Testing Jablanica (Republika Srpska, Bosnia)
            weatherStation.setAltitude(219.0);
            weatherStation.setLongitude(16.988056);
            weatherStation.setLatitude(45.115);
            //result = instance.getWeatherForecasts(weatherStation);
            assertNotNull(result);
            
            /**Collections.sort(result);
            for(WeatherObservation obs:result)
            {
                System.out.println(obs.toString());
            }*/
        }
        catch(ParseWeatherDataException ex)
        {
            if(ex.getMessage().contains("FileNotFoundException"))
            {
                System.out.println("WARNING: YR URL Not active");
            }
        }
        
        
    }
    
}
