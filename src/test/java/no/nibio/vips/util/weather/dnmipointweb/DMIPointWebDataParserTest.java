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
package no.nibio.vips.util.weather.dnmipointweb;

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
public class DMIPointWebDataParserTest {
    
    public DMIPointWebDataParserTest() {
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
     * Test of getData method, of class DMIPointWebDataParser.
     */
    @Test
    public void testGetData() {
        System.out.println("getData");
        DMIPointWebDataParser instance = new DMIPointWebDataParser();
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Copenhagen"));
        cal.set(2017, Calendar.JULY, 5, 0, 0, 0);
        Date dateFrom = cal.getTime();
        cal.set(Calendar.DATE, 15);
        Date dateTo = cal.getTime();
        Double longitude = 10.0213;
        Double latitude = 56.0199;
        List<WeatherObservation> result = instance.getData(longitude, latitude, dateFrom, dateTo);
        result.stream().forEach(r->System.out.println(r.toString()));
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
