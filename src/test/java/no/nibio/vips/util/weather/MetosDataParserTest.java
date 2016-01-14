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

import no.nibio.vips.util.weather.MetosDataParser;
import java.util.List;
import java.util.TimeZone;
import no.nibio.vips.entity.WeatherObservation;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author treinar
 */
public class MetosDataParserTest {
    
    public MetosDataParserTest() {
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
     * Test of getWeatherObservations method, of class MetosDataParser.
     * Could be time consuming, so we test only when needed
     */
    @Ignore // It appears that the current metos stations do not deliver data (as of 2016-01-14)
    @Test
    public void testGetWeatherObservations() throws Exception {
        System.out.println("getWeatherObservations");
        String stationID = "00002085";
        MetosDataParser instance = new MetosDataParser();
        //List<WeatherObservation> expResult = null;
        Integer expResult = 0;
        List<WeatherObservation> result = instance.getWeatherObservations(stationID, TimeZone.getTimeZone("Europe/Sofia"));
        assertNotNull(result);
        assertNotSame(expResult, result.size());
        //System.out.println("Result sample: " + result.get(10).toString());
        
        // Testing different station with different parameters
        stationID = "00000428";
        result = instance.getWeatherObservations(stationID, TimeZone.getTimeZone("Europe/Sarajevo"));
        assertNotNull(result);
        assertNotSame(expResult, result.size());
        //System.out.println("Result sample: " + result.get(10).toString());
        /*for(WeatherObservation obs:result)
        {
            System.out.println(obs.toString());
        }*/
    }
    
}
