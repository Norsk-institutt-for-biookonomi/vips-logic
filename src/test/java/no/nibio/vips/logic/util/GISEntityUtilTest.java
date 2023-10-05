/*
 * Copyright (c) 2016 NIBIO <http://www.nibio.no/>. 
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
package no.nibio.vips.logic.util;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.GeoJSONFactory;

/**
 *
 * @author treinar
 */
public class GISEntityUtilTest {
    
    public GISEntityUtilTest() {
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

    
    
    
    // Actually testing the helper API
    @Test
    public void testJTSToGeoJSON()
    {
        System.out.println("testJTSToGeoJSON");
        String json = "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"id\":-1,\"geometry\":{\"type\":\"Point\",\"coordinates\":[9.12109375,61.93990427940116,0]},\"properties\":{\"name\":\"Ny observasjon\",\"observationId\":-1,\"observationData\":\"{\\\"symptom\\\":\\\"\\\",\\\"tiltak\\\":\\\"\\\",\\\"forekomststorrelse\\\":\\\"\\\"}\",\"cropOrganism\":{},\"observationText\":\"\",\"timeOfObservation\":1479865788123}}]}";
        FeatureCollection result = (FeatureCollection) GeoJSONFactory.create(json);
        assertNotNull(result);
    }
    
}
