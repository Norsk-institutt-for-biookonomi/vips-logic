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
package no.nibio.vips.util.weather;

import org.locationtech.jts.geom.Coordinate;
import java.util.Collections;
import java.util.List;
import no.nibio.vips.entity.WeatherObservation;
import no.nibio.vips.logic.entity.PointOfInterestWeatherStation;
import no.nibio.vips.gis.GISUtil;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author treinar
 */
public class YrWeatherForecastProviderTest {

    @Test
    public void testGetWeatherForecasts() throws Exception {
        PointOfInterestWeatherStation weatherStation = new PointOfInterestWeatherStation();
        weatherStation.setLongitude(11.39);
        weatherStation.setLatitude(59.388);
        weatherStation.setAltitude(102.0);
        weatherStation.setGisGeom(new GISUtil().createPointWGS84(new Coordinate(11.39, 59.388, 102.0)));

        List<WeatherObservation> result = new YrWeatherForecastProvider().getWeatherForecasts(weatherStation);
        Collections.sort(result);

        WeatherObservation first = result.get(0);
        WeatherObservation last = result.get(result.size() - 1);
        long spanHours = (last.getTimeMeasured().getTime() - first.getTimeMeasured().getTime()) / (3600L * 1000L);

        System.out.println("observations=" + result.size());
        System.out.println("first=" + first.getElementMeasurementTypeId() + " " + first.getTimeMeasured() + " " + first.getValue());
        System.out.println("last=" + last.getElementMeasurementTypeId() + " " + last.getTimeMeasured() + " " + last.getValue());
        System.out.println("spanHours=" + spanHours);

        assertFalse(result.isEmpty());
        assertTrue(spanHours > 24);
        assertTrue(spanHours < 72);
    }
}
