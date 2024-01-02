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

package no.nibio.vips.logic.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import no.nibio.vips.gis.GISUtil;
import no.nibio.vips.logic.entity.Gis;
import no.nibio.vips.logic.entity.Observation;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.GeoJSONFactory;
import org.wololo.jts2geojson.GeoJSONReader;
import org.wololo.jts2geojson.GeoJSONWriter;

/**
 * @copyright 2015 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class GISEntityUtil {

    public String getGeoJSONFromGis(List<Gis> geoinfo, Map<String, Object> properties)
    {
        if(geoinfo == null || geoinfo.isEmpty())
        {
            return "";
        }
        List<Feature> features = new ArrayList<>();
        GeoJSONWriter writer = new GeoJSONWriter();
        geoinfo.stream().forEach(
                (gis)->features.add(new Feature(writer.write(gis.getGisGeom()), properties))
        );
       
        FeatureCollection json = writer.write(features);
        return json.toString();
    }
    
    /**
     * Extract Gis objects from the provided GeoJSON
     * @param json
     * @return 
     */
    public List<Gis> getGisFromGeoJSON(String json)
    {
        if(json == null)
        {
            return null;
        }
        
        GeoJSONReader reader = new GeoJSONReader();
        List<Gis> retVal = new ArrayList<>();
        try
        {
            FeatureCollection featureCollection = (FeatureCollection) GeoJSONFactory.create(json);
            for(Feature feature: featureCollection.getFeatures())
            {
                Gis gis = new Gis();
                gis.setGisGeom(reader.read(feature.getGeometry()));
                gis.getGisGeom().setSRID(GISUtil.DEFAULT_SRID);
                retVal.add(gis);
            }
        }catch(NullPointerException ex) {};
        return retVal;
    }

    public Gis getGisFromFeature(Feature feature)
    {
        GeoJSONReader reader = new GeoJSONReader();
        Gis gis = new Gis();
        gis.setGisGeom(reader.read(feature.getGeometry()));
        gis.getGisGeom().setSRID(GISUtil.DEFAULT_SRID);
        return gis;
    }
    
    /**
     * Converts a list of observations to a FeatureCollection
     * Other observations properties are stored in (you guessed it) Feature properties
     * @param observations
     * @return 
     */
    public FeatureCollection getGeoJSONFromObservations(List<Observation> observations)
    {
        GeoJSONWriter writer = new GeoJSONWriter();
        List<Feature> features = new ArrayList<>();
        observations.stream().filter(
                        (observation)-> observation.getGeoinfos() != null
                    ).forEach(
                        (observation) -> {
                            features.addAll(this.getFeaturesFromObservation(observation, writer));
                            //retVal.add(writer.write(features));
                        }
                    );
        return writer.write(features);
    }
    
    public FeatureCollection getGeoJSONFromObservation(Observation observation)
    {
        GeoJSONWriter writer = new GeoJSONWriter();
        List<Feature> features =this.getFeaturesFromObservation(observation, writer);
        return writer.write(features);
    }
    
    /**
     * Method overloading
     * @param observation
     * @return 
     */
    public List<Feature> getFeaturesFromObservation(Observation observation){
        return this.getFeaturesFromObservation(observation, new GeoJSONWriter());
    }
    
    public List<Feature> getFeaturesFromObservation(Observation observation, GeoJSONWriter writer)
    {
        List<Feature> features = new ArrayList<>();
        if(observation.getGeoinfos() == null)
        {
            return features;
        }
        Map<String, Object> properties = new HashMap<>();
        properties.put("observationId", observation.getObservationId());
        properties.put("timeOfObservation", observation.getTimeOfObservation());
        properties.put("cropOrganism", observation.getCropOrganism());
        properties.put("organism", observation.getOrganism());
        properties.put("observationHeading", observation.getObservationHeading());
        properties.put("observationText", observation.getObservationText());
        if(observation.getIsQuantified())
        {
            properties.put("observationData", observation.getObservationData());
        }
        observation.getGeoinfos().stream().forEach(
                (gis)->features.add(new Feature(gis.getGisId(),writer.write(gis.getGisGeom()),properties))
        );
        return features;
    }
    
    
    
    public de.micromata.opengis.kml.v_2_2_0.Coordinate getKMLCoordinateFromJTSCoordinate(org.locationtech.jts.geom.Coordinate jtsCoordinate)
    {
        return new de.micromata.opengis.kml.v_2_2_0.Coordinate(
                jtsCoordinate.x, 
                jtsCoordinate.y, 
                Double.isNaN(jtsCoordinate.z) ? 0.0 :  jtsCoordinate.z
        );
    }

    
}
