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
package no.nibio.vips.util.weather;

import java.util.Calendar;
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
public class CebaowangDataParserTest {
    
    public CebaowangDataParserTest() {
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
     * Test of getWeatherObservations method, of class CebaowangDataParser.
     */
    @Test
    public void testGetWeatherObservations() throws Exception {
        System.out.println("getWeatherObservations");
        String stationID = "token=0819208daf3684b6e1b2f7d9b0e1bc64&deviceId=YBQ0000053";
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Shanghai");
        Calendar cal = Calendar.getInstance(timeZone);
        cal.set(2019, Calendar.JANUARY, 10, 0,0);
        Date startDate = cal.getTime();
        cal.add(Calendar.DATE, 20);
        Date endDate = cal.getTime();
        CebaowangDataParser instance = new CebaowangDataParser();
        List<WeatherObservation> expResult = null;
        List<WeatherObservation> result = instance.getWeatherObservations(stationID, timeZone, startDate, endDate);
        //assertEquals(expResult, result);
        assertNotNull(result);
        result.forEach(obs->System.out.println(obs));
        
    }
    
}
