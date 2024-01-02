/*
 * Copyright (c) 2017 NIBIO <http://www.nibio.no/>. 
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

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import no.nibio.vips.entity.ModelConfiguration;
import no.nibio.vips.entity.WeatherObservation;
import no.nibio.vips.logic.entity.ForecastConfiguration;
import no.nibio.vips.logic.entity.Organism;
import no.nibio.vips.logic.entity.PointOfInterestWeatherStation;
import no.nibio.vips.logic.entity.WeatherStationDataSource;
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
public class BremiaLactucaeModelPreprocessorTest {
    
    public BremiaLactucaeModelPreprocessorTest() {
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
     * Test of getModelConfiguration method, of class BremiaLactucaeModelPreprocessor.
     */
    //@Test
    public void testGetModelConfiguration() throws Exception {
        System.out.println("getModelConfiguration");
        ForecastConfiguration configuration = new ForecastConfiguration();
        Organism bremiaL = new Organism();
        configuration.setCropOrganismId(bremiaL);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Oslo"));
        cal.set(2017, Calendar.JUNE, 1, 0, 0, 0);
        configuration.setDateStart(cal.getTime());
        cal.set(Calendar.MONTH, Calendar.AUGUST);
        configuration.setDateEnd(cal.getTime());
        PointOfInterestWeatherStation station = new PointOfInterestWeatherStation();
        station.setTimeZone("Europe/Oslo");
        WeatherStationDataSource wsds = new WeatherStationDataSource();
        wsds.setDatafetchUriExpression("https://lmt.nibio.no/services/rest/vips/getdata/forecastfallback?weatherStationId=%s");
        station.setWeatherStationDataSourceId(wsds);
        station.setWeatherStationRemoteId("30");
        configuration.setWeatherStationPointOfInterestId(station);
        BremiaLactucaeModelPreprocessor instance = new BremiaLactucaeModelPreprocessor();
        
        ModelConfiguration result = instance.getModelConfiguration(configuration);
        assertNotNull(result);
        
        //List<WeatherObservation> obs = (List)result.getConfigParameter("observations");
        //obs.stream().filter(ob->ob.getElementMeasurementTypeId().equals("BTg")).forEach(ob->{System.out.println(ob);});
    }

    
    
}
