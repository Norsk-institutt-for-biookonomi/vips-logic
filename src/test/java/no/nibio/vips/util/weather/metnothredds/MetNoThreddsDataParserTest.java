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
package no.nibio.vips.util.weather.metnothredds;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import no.nibio.vips.entity.PointWeatherObservationList;
import no.nibio.vips.entity.WeatherObservation;
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
public class MetNoThreddsDataParserTest {
    
    public MetNoThreddsDataParserTest() {
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

    //@Test
    public void testGetGridData()
    {
        System.out.println("testGetGridData");
        Coordinate[] coords = new Coordinate[5];
        coords[0] = new Coordinate(10.31,59.91);
        coords[1] = new Coordinate(11.07,59.91);
        coords[2] = new Coordinate(11.07,59.52);
        coords[3] = new Coordinate(10.31,59.52);
        coords[4] = new Coordinate(10.31,59.91);
        GeometryFactory gFac = new GeometryFactory();
        Polygon pol = gFac.createPolygon(coords);
        
        List<String> weatherParameters = Arrays.asList("TM","RR", "Q0", "TX","TN","UM");
        MetNoThreddsDataParser instance = new MetNoThreddsDataParser();
        
        Date start = new Date();
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Oslo"));
        cal.setTime(new Date());
        //cal.add(Calendar.DATE, -2);
        cal.set(Calendar.HOUR_OF_DAY, 5);
        Date dateFrom = new WeatherUtil().normalizeToExactHour(cal.getTime(), TimeZone.getDefault());
        cal.add(Calendar.DATE, 5);
        Date dateTo = cal.getTime();
        
        
        List<PointWeatherObservationList> result = instance.getGridData(pol, 10.0, dateFrom, dateTo, weatherParameters);
        assertNotNull(result);
        Long timeSpent = new Date().getTime() - start.getTime();
        System.out.println("Time spent=" + (new Double(timeSpent)/1000) + " seconds");
        result.stream().forEach(mp->System.out.println(mp));
    }
    
    /**
     * Test of getPointData method, of class MetNoThreddsDataParser.
     */
    //@Test
    public void testGetPointData() {
        System.out.println("getPointData");
        
        Double longitude = 10.7946;
        Double latitude = 59.6652;

        List<String> weatherParameters = Arrays.asList("TM","RR", "Q0", "TX","TN","UM");
        MetNoThreddsDataParser instance = new MetNoThreddsDataParser();
        
        Date start = new Date();
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Oslo"));
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -20);
        cal.set(Calendar.HOUR_OF_DAY, 1);
        Date dateFrom = new WeatherUtil().normalizeToExactHour(cal.getTime(), TimeZone.getDefault());
        cal.add(Calendar.DATE, 20);
        Date dateTo = cal.getTime();
        
        
        List<WeatherObservation> result = instance.getPointData(longitude, latitude, dateFrom, dateTo, weatherParameters);
        Long timeSpent = new Date().getTime() - start.getTime();
        System.out.println("Time spent=" + (new Double(timeSpent)/1000) + " seconds");
        
        Collections.sort(result);
        Long hoursIncluded = 1 + ((result.get(result.size()-1).getTimeMeasured().getTime() - result.get(0).getTimeMeasured().getTime()) / 3600000);
        long expResult = weatherParameters.size() * hoursIncluded;
        result.stream().forEach(obs->System.out.println(obs));
        assertEquals(expResult, result.size());
        assertTrue(result.size() > 10);
        
    }
    
    /**
     * Shorthand for getting weather data for a specific application
     */
    //@Test
    public void testGetPointDataForAnneGrete() {
        System.out.println("getPointData");
        
        String[] placeNames = {
            "2016_59.3916307_10.424473",
            "2017_59.3859_10.433975",
            "As",
            "Saerheim",
            "Ullensvang",
            "Apelsvoll",
            "Ramnes",
            "Tjoelling",
            "Kvithamar",
            "Holt",
            "Favang",
            "Landvik"
        };
        
        Coordinate[] coords = {
            new Coordinate(10.424473, 59.3916307),
            new Coordinate(10.433975, 59.3859),
            new Coordinate(10.781989, 59.660468), // Ås
            new Coordinate(5.65078, 58.76053), // Særheim
            new Coordinate(6.65381, 60.31853), // Ullensvang
            new Coordinate(10.86952, 60.70024), // Apelsvoll
            new Coordinate(10.23923, 59.38081), // Ramnes
            new Coordinate(10.12513, 59.04641), // Tjølling
            new Coordinate(10.87994, 63.48795), // Kvithamar
            new Coordinate(18.90946, 69.65381), // Holt
            new Coordinate(10.1872, 61.45822), // Fåvang
            new Coordinate(8.522554, 58.340071) // Landvik
        };
        
        

        List<String> weatherParameters = Arrays.asList("TM","TX","TN");
        MetNoThreddsDataParser instance = new MetNoThreddsDataParser();
        
        Date start = new Date();
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Oslo"));
        cal.set(2016,Calendar.JANUARY,1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date dateFrom = cal.getTime();
        cal.set(2017,Calendar.DECEMBER,31,23,59,0);
        Date dateTo = cal.getTime();
        
        List<WeatherObservation> result;
        ObjectMapper mapper = new ObjectMapper();

        for(int i=0;i<placeNames.length;i++)
        {
            System.out.println(placeNames[i]);
            result = instance.getPointData(coords[i].x, coords[i].y, dateFrom, dateTo, weatherParameters);
            Collections.sort(result);
            File testOut = new File("/home/treinar/prosjekter/vips/projects/2017_gaardsvarsling/" + placeNames[i]  +  ".json");
            try(FileOutputStream oStream = new FileOutputStream(testOut);)
            {
                
                
                mapper.writeValue(oStream, result);
            }
            catch(IOException ex)
            {
                ex.printStackTrace();
            }
            
        }
        Long timeSpent = new Date().getTime() - start.getTime();
        System.out.println("Time spent=" + (new Double(timeSpent)/1000) + " seconds");
        
    }
    
    @Test
    public void testFindVIPSParamNameFromMetParamName()
    {
        System.out.println("testFindVIPSParamNameFromMetParamName");
        MetNoThreddsDataParser instance = new MetNoThreddsDataParser();
        String expResult = "TM";
        String result = instance.findVIPSParamNameFromMetParamName("air_temperature_2m");
        assertEquals(expResult, result);
        result = instance.findVIPSParamNameFromMetParamName("foo_bar_1000m");
        assertNull(result);
    }
    
    @Test
    public void testGetArchiveDates()
    {
        System.out.println("testGetArchiveDates");
        MetNoThreddsDataParser instance = new MetNoThreddsDataParser();
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Oslo"));
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -2);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        Date dateFrom = new WeatherUtil().normalizeToExactHour(cal.getTime(), TimeZone.getDefault());
        cal.add(Calendar.DATE, 5);
        List<Date> result = instance.getArchiveDates(dateFrom, cal.getTime());
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HHZ");
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        result.stream().forEach(r->System.out.println(f.format(r)));
        assertEquals(4,result.size());
        
    }
    
}
