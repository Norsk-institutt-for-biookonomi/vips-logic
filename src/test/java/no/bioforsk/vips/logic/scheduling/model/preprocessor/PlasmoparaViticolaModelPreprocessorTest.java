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
package no.bioforsk.vips.logic.scheduling.model.preprocessor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import no.bioforsk.vips.entity.ModelConfiguration;
import no.bioforsk.vips.entity.WeatherObservation;
import no.bioforsk.vips.logic.entity.ForecastConfiguration;
import no.bioforsk.vips.util.weather.WeatherDataSourceUtil;
import org.apache.commons.io.IOUtils;
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
public class PlasmoparaViticolaModelPreprocessorTest {
    
    public PlasmoparaViticolaModelPreprocessorTest() {
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

    
    @Test
    public void testGetWeatherObservations()
    {
        
        PlasmoparaViticolaModelPreprocessor instance = new PlasmoparaViticolaModelPreprocessor();
        try
        {

            URL weatherURL = new URL("http://www.robigus.ba/Vips/GetJsonWeatherData/11?logIntervalId=1&timeZone=Europe/Sarajevo&startDate=2015-01-01&startTime=0&endDate=2015-02-20&endTime=0&elementMeasurementTypes[]=TM&elementMeasurementTypes[]=RR");
            //URL weatherURL = new URL("http://lmt.bioforsk.no/agrometbase/export/getVIPS3JSONWeatherData.php?weatherStationId=5&elementMeasurementTypes%5B%5D=TM&elementMeasurementTypes%5B%5D=UM&logInterval=1h&startDate=2015-01-01&startTime=00&endDate=2015-02-17&endTime=00&timeZone=Europe%2FOslo");
            URLConnection URLConn = weatherURL.openConnection();
            InputStream URLStream = URLConn.getInputStream();
            String URLOutput = IOUtils.toString(URLStream);
            List<WeatherObservation> result =  new WeatherDataSourceUtil().getWeatherObservations(URLOutput);
            
        }catch(IOException ex)
        {
            fail(ex.getMessage());
        }
    }
}
