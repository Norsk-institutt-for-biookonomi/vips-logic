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

import no.nibio.vips.util.weather.ALabDataParser;
import java.text.DateFormat;
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
public class ALabDataParserTest {
    
    public ALabDataParserTest() {
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
     * Test of getWeatherObservations method, of class ALabDataParser.
     */
    //@Test
    public void testGetWeatherObservations_5args() throws Exception {
        System.out.println("getWeatherObservations");
        String stationID = "000000000010FDC62301";
        TimeZone timeZone = TimeZone.getTimeZone("Europe/Helsinki");
        DateFormat dFormat = new SimpleDateFormat("yyyy-MM-dd");
        dFormat.setTimeZone(timeZone);
        Date startDate = dFormat.parse("2015-01-01");
        String userName = "fmi";
        String password = "ectoncha";
        ALabDataParser instance = new ALabDataParser();
        List<WeatherObservation> expResult = null;
        List<WeatherObservation> result = instance.getWeatherObservations(stationID, timeZone, startDate, userName, password);
        assertNotNull(result);
        /**for(WeatherObservation obs: result)
        {
            if(obs.getElementMeasurementTypeId().equals("RR"))
            {
                System.out.println(obs);
            }
        }*/
    }

    
    
}
