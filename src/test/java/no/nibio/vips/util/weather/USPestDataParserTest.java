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
package no.nibio.vips.util.weather;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import no.nibio.vips.entity.WeatherObservation;
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
public class USPestDataParserTest {
    
    public USPestDataParserTest() {
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
     * Test of getWeatherObservations method, of class USPestDataParser.
     */
    @Test
    public void testGetWeatherObservations() throws Exception {
        System.out.println("getWeatherObservations");
        String stationID = "FNWO3";
        TimeZone timeZone = TimeZone.getTimeZone("America/Los_Angeles");
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        f.setTimeZone(timeZone);
        Date startDate = f.parse("2016-10-01");
        USPestDataParser instance = new USPestDataParser();
        
        List<WeatherObservation> result = instance.getWeatherObservations(stationID, timeZone, startDate);
        //System.out.println("Result.size()=" + result.size());
        //result.stream().filter(obs->obs.getElementMeasurementTypeId().equals("RR")).forEach(obs->System.out.println(obs.toString()));
        assertNotNull(result);
        
    }
    
}
