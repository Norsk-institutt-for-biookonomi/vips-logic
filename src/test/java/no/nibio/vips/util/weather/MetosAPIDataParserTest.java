/*
 * Copyright (c) 2017 NIBIO <http://www.nibio.no/>. 
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


import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import no.nibio.vips.entity.WeatherObservation;
import org.apache.http.auth.AuthenticationException;
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
public class MetosAPIDataParserTest {
    
    public MetosAPIDataParserTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() throws NoSuchAlgorithmException, KeyStoreException {
        
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getWeatherObservations method, of class MetosAPIDataParser.
     */
    @Test
    public void testGetWeatherObservations() throws Exception {
        System.out.println("getWeatherObservations");
        String stationId = "000024A0";
        TimeZone timeZone = TimeZone.getTimeZone("Europe/Vilnius");
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(timeZone);
        cal.set(2017,Calendar.MAY,20,0,0,0);
        Date startDate = cal.getTime();
        MetosAPIDataParser instance = new MetosAPIDataParser();
        List<WeatherObservation> result = instance.getWeatherObservations(stationId, timeZone, startDate);
        assertNotNull( result);
        //result.stream().forEach(obs -> System.out.println(obs.toString()));
        
    }

    /**
     * Test of getAccessCode method, of class MetosAPIDataParser.
     */
    //@Test
    public void testGetAccessCode() {
        try {
            System.out.println("getAccessCode");
            MetosAPIDataParser instance = new MetosAPIDataParser();
            String expResult = "";
            String result = instance.getAccessCode();
            assertNotNull(result);
            
        } catch (IOException ex) {
            Logger.getLogger(MetosAPIDataParserTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //@Test 
    public void testGetToken()
    {
        System.out.println("getToken");
        try {
            MetosAPIDataParser instance = new MetosAPIDataParser();
            
            String result = instance.getToken();
            //System.out.println("access_token = " + result);
            assertNotNull(result);
        } catch (IOException | AuthenticationException ex) {
            Logger.getLogger(MetosAPIDataParserTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //@Test
    public void testAPI(){
        System.out.println("testAPI");
        MetosAPIDataParser instance = new MetosAPIDataParser();
        try {
            instance.testAPI();
        } catch (IOException ex) {
            fail(ex.getMessage());
        } catch (AuthenticationException ex) {
            Logger.getLogger(MetosAPIDataParserTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
