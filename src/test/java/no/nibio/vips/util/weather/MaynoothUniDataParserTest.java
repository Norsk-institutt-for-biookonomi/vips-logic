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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileOutputStream;
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
public class MaynoothUniDataParserTest {
    
    public MaynoothUniDataParserTest() {
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
     * Test of getWeatherObservations method, of class MaynoothUniDataParser.
     */
    @Test
    public void testGetWeatherObservations_3args() throws Exception {
        System.out.println("getWeatherObservations");
        
        TimeZone timeZone = TimeZone.getTimeZone("Europe/Dublin");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        format.setTimeZone(timeZone);
        
        MaynoothUniDataParser instance = new MaynoothUniDataParser();
        ObjectMapper mapper = new ObjectMapper();
        // 2016-10-26: This is for printing out data from Mladen
        for(int i=2006;i<=2016;i++)
        {
            String stationID = String.valueOf(i);
            Date startDate = format.parse(stationID + "-05-01");
            List<WeatherObservation> result = instance.getWeatherObservations(stationID, timeZone, startDate);
            
            FileOutputStream out = new FileOutputStream(new File("/home/treinar/prosjekter/vips/projects/2016_irland/maynooth_" +  stationID + ".json"));
            mapper.writeValue(out, result);
            assertNotNull(result);
        }
        //System.out.println("Number of observations=" + result.size());
        //result.stream().forEach((observation) -> System.out.println(observation));
        //assertNotNull(result);
        
    }

}
