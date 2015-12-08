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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import no.nibio.vips.logic.entity.Gis;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.GeoJSONFactory;
import org.wololo.jts2geojson.GeoJSONReader;
import org.wololo.jts2geojson.GeoJSONWriter;

/**
 * @copyright 2015 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class GISUtil {

    /**
     * Parses all features from GeoJSON string, returns as JTS Geometry objects
     * @param json
     * @return 
     */
    public List<Geometry> getGeometriesFromGeoJSON(String json)
    {
        FeatureCollection featureCollection = (FeatureCollection) GeoJSONFactory.create(json);
        GeoJSONReader reader = new GeoJSONReader();
        List<Geometry> retVal = new ArrayList<>();
        for(Feature feature: featureCollection.getFeatures())
        {
            retVal.add(reader.read(feature.getGeometry()));
        }
        return retVal;
    }
    
    public List<Gis> getGisFromGeoJSON(String json)
    {
        GeoJSONReader reader = new GeoJSONReader();
        List<Gis> retVal = new ArrayList<>();
        try
        {
            FeatureCollection featureCollection = (FeatureCollection) GeoJSONFactory.create(json);
            for(Feature feature: featureCollection.getFeatures())
            {
                Gis gis = new Gis();
                gis.setGisGeom(reader.read(feature.getGeometry()));
                gis.getGisGeom().setSRID(4326);
                retVal.add(gis);
            }
        }catch(NullPointerException ex) {};
        return retVal;
    }
    
    public String getGeoJSONFromGis(List<Gis> geoinfo)
    {
        if(geoinfo == null || geoinfo.isEmpty())
        {
            return "";
        }
        List<Feature> features = new ArrayList<>();
        Map<String, Object> properties = new HashMap<>();
        GeoJSONWriter writer = new GeoJSONWriter();
        for(Gis gis:geoinfo)
        {
            features.add(new Feature(writer.write(gis.getGisGeom()), properties));
        }

        FeatureCollection json = writer.write(features);
        return json.toString();
    }
    
    public String getGeoJSONFromGeometries(List<Geometry> geometries)
    {
        if(geometries == null || geometries.isEmpty())
        {
            return "";
        }
        List<Feature> features = new ArrayList<>();
        Map<String, Object> properties = new HashMap<>();
        GeoJSONWriter writer = new GeoJSONWriter();
        for(Geometry geometry:geometries)
        {
            features.add(new Feature(writer.write(geometry), properties));
        }

        FeatureCollection json = writer.write(features);
        return json.toString();
    }
}
