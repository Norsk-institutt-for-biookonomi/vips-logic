/*
 * Copyright (c) 2019 NIBIO <http://www.nibio.no/>. 
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
package no.nibio.vips.logic.entity.rest;

import java.util.ArrayList;
import java.util.List;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.wololo.geojson.Polygon;

/**
 *
 * @author treinar
 */
public class PointMappingRequestTest {
    
    public PointMappingRequestTest() {
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
     * Test of getPoints method, of class PointMappingRequest.
     */
    //@Test
    public void testGetPoints() {
        System.out.println("getPoints");
        PointMappingRequest instance = new PointMappingRequest();
        List<ReferencedPoint> expResult = null;
        List<ReferencedPoint> result = instance.getPoints();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setPoints method, of class PointMappingRequest.
     */
    //@Test
    public void testSetPoints() {
        System.out.println("setPoints");
        List<ReferencedPoint> points = null;
        PointMappingRequest instance = new PointMappingRequest();
        instance.setPoints(points);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
    @Test
    public void testClient() {
        System.out.println("testClient");
        List<ReferencedPoint> points = new ArrayList<>();
        ReferencedPoint r1 = new ReferencedPoint();
        r1.setId("test1");
        r1.setLon(11.9028835076959);
        r1.setLat(64.455583583917);
        ReferencedPoint r2 = new ReferencedPoint();
        r2.setId("test2");
        r2.setLon(12.588734241028);
        r2.setLat(64.5142545939044);
        
        points.add(r1);
        points.add(r2);
        
        ReferencedPoint[] instance = points.toArray(new ReferencedPoint[points.size()]);
        
        try(Client client = ClientBuilder.newClient()) {
            WebTarget target = client.target("https://proxy1.nibio.no/municipality_cache_ws/pointmapping/1/4326");

            //PointMappingResponse response = 
            PointMappingResponse response =    target.request(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(instance, MediaType.APPLICATION_JSON), PointMappingResponse.class);//.toString();
            //System.out.println(((Polygon)response.getFeatureCollection().getFeatures()[0].getGeometry()).getCoordinates()[0].length);
            
            assertNotNull(response);
        }
        //System.out.println(Entity.entity(instance, MediaType.APPLICATION_JSON));
        
        //System.out.println(target.request(MediaType.APPLICATION_JSON)
        //        .buildPost(Entity.entity(instance, MediaType.APPLICATION_JSON).));

                
        //System.out.println(response.getStatus());
        
           
    }
}
