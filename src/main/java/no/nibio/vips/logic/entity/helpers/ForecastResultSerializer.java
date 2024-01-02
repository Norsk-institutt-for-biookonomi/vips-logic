/*
 * Copyright (c) 2018 NIBIO <http://www.nibio.no/>. 
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

package no.nibio.vips.logic.entity.helpers;

import com.bedatadriven.jackson.datatype.jts.JtsModule;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import java.io.IOException;
import no.nibio.vips.gis.GISUtil;
import no.nibio.vips.logic.entity.ForecastResult;

/**
 * Unfortunately, we have to do this, since Resteasy does not currently support the org.locationtech.jts package
 * Ref http://www.baeldung.com/jackson-custom-serialization
 * @copyright 2019 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class ForecastResultSerializer extends StdSerializer<ForecastResult>{
    ObjectMapper objectMapper;
    GeometryFactory gf;
    GISUtil gisUtil;
    public ForecastResultSerializer() {
        this(null);
        this.gisUtil = new GISUtil();
    }
   
    public ForecastResultSerializer(Class<ForecastResult> t) {
        super(t);
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        this.objectMapper.registerModule(new JtsModule());
    }
    
    @Override
    public void serialize(ForecastResult t, JsonGenerator jg, SerializerProvider sp) throws IOException {
        jg.writeStartObject();
        jg.writeStringField("validTimeStart", this.objectMapper.writeValueAsString(t.getValidTimeStart()).replaceAll("\"", ""));
        jg.writeStringField("validTimeEnd", this.objectMapper.writeValueAsString(t.getValidTimeEnd()).replaceAll("\"", ""));
        String geoJSON = t.getValidGeometry() == null ? "null" 
                : t.getValidGeometry().getGeometryType().equals("Point") ? objectMapper.writeValueAsString(this.gisUtil.getVividPoint((Point) t.getValidGeometry()))
                : t.getValidGeometry().getGeometryType().equals("LineString") ? objectMapper.writeValueAsString((LineString) t.getValidGeometry())
                :t.getValidGeometry().getGeometryType().equals("Polygon") ? objectMapper.writeValueAsString((Polygon) t.getValidGeometry())
                : "null";
        
        //jg.writeObjectField("validGeometry", t.getValidGeometry());
        //System.out.println(geoJSON);
        jg.writeRaw(",\"validGeometry\":" + geoJSON);
        //
        jg.writeNumberField("warningStatus", t.getWarningStatus());
        jg.writeNumberField("forecastResultId", t.getForecastResultId());
        jg.writeNumberField("forecastConfigurationId", t.getForecastConfigurationId());
        //System.out.println(t.getAllValues());
        jg.writeRaw(",\"allValues\": " + t.getAllValues());
        jg.writeRaw(",\"keys\": [" + "\"" + String.join("\",\"", t.getKeys()) + "\"]");
        jg.writeEndObject();
    }
    
    

}
