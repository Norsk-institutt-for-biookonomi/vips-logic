/*
 * Copyright (c) 2015 NIBIO <http://www.nibio.no/>. 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */
package no.nibio.vips.logic.scheduling.model.preprocessor;

import no.nibio.vips.logic.scheduling.model.preprocessor.PlasmoparaViticolaModelPreprocessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import no.nibio.vips.entity.ModelConfiguration;
import no.nibio.vips.entity.WeatherObservation;
import no.nibio.vips.logic.entity.ForecastConfiguration;
import no.nibio.vips.util.weather.WeatherDataSourceUtil;
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

            //URL weatherURL = new URL("http://www.robigus.ba/Vips/GetJsonWeatherData/11?logIntervalId=1&timeZone=Europe/Sarajevo&startDate=2015-01-01&startTime=0&endDate=2015-02-20&endTime=0&elementMeasurementTypes[]=TM&elementMeasurementTypes[]=RR");
            URL weatherURL = new URL("https://lmt.nibio.no/services/rest/vips/getdata/forecastfallback?weatherStationId=5&elementMeasurementTypes[]=TM&elementMeasurementTypes[]=UM&logInterval=1h&startDate=2015-01-01&startTime=00&endDate=2015-02-17&endTime=00&timeZone=Europe/Oslo");
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
