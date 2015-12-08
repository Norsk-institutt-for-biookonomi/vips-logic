/*
 * Copyright (c) 2015 NIBIO <http://www.nibio.no/>. 
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
package no.nibio.vips.logic.util;

import com.vividsolutions.jts.geom.Geometry;
import java.util.List;
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
public class GISUtilTest {
    
    public GISUtilTest() {
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
     * Test of getGeometriesFromGeoJSON method, of class GISUtil.
     */
    @Test
    public void testRoundtrip() {
        System.out.println("testRoundtrip");
        String json = "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[10.669097900390623,59.753628151319106],[10.612792968749998,59.70309199431276],[10.726776123046873,59.705863076677105],[10.669097900390623,59.753628151319106]]]},\"properties\":{}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[10.704803466796873,59.64831609639066]},\"properties\":{}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[10.649871826171871,59.67051458978321],[10.791320800781248,59.67328836837126]]},\"properties\":{}}]}";
        GISUtil instance = new GISUtil();
        //List<Geometry> expResult = null;
        List<Geometry> geometries = instance.getGeometriesFromGeoJSON(json);
        String result = instance.getGeoJSONFromGeometries(geometries);
        //System.out.println(result);
        assertEquals(json, result);
    }

    
}
