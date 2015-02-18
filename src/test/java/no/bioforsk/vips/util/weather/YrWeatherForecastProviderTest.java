/*
 * Copyright (c) 2015 Bioforsk <http://www.bioforsk.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the Bioforsk Open Source License as published by 
 * Bioforsk, either version 1 of the License, or (at your option) any
 * later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Bioforsk Open Source License for more details.
 * 
 * You should have received a copy of the Bioforsk Open Source License
 * along with VIPSLogic.  If not, see <http://www.bioforsk.no/licenses/>.
 * 
 */
package no.bioforsk.vips.util.weather;

import java.util.List;
import no.bioforsk.vips.entity.WeatherObservation;
import no.bioforsk.vips.logic.entity.PointOfInterestWeatherStation;
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
        weatherStation.setAltitude(67.0);
        weatherStation.setLongitude(17.8094);
        weatherStation.setLatitude(43.301);
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
            weatherStation.setAltitude(110.0);
            weatherStation.setLongitude(10.83474668);
            weatherStation.setLatitude(59.63851390);
            result = instance.getWeatherForecasts(weatherStation);
            assertNotNull(result);
        }
        catch(ParseWeatherDataException ex)
        {
            if(ex.getMessage().contains("FileNotFoundException"))
            {
                System.out.println("WARNING: YR URL Not active");
            }
        }
        
        /*Collections.sort(result);
        for(WeatherObservation obs:result)
        {
            System.out.println(obs.toString());
        }*/
    }
    
}
