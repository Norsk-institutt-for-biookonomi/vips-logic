package no.nibio.vips.logic.entity.helpers;

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.ext.Provider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.io.OutputStream;
import java.io.IOException;
import de.micromata.opengis.kml.v_2_2_0.Kml;

@Provider
@ApplicationScoped
@Produces("application/vnd.google-earth.kml+xml")
public class KmlMessageBodyWriter implements MessageBodyWriter<Kml> {

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, 
                               Annotation[] annotations, MediaType mediaType) {
        if (!Kml.class.isAssignableFrom(type)) {
            return false;
        }
        if (mediaType == null) {
            return true;
        }
        return mediaType.isCompatible(MediaType.valueOf("application/vnd.google-earth.kml+xml"));
    }

    @Override
    public void writeTo(Kml kml, Class<?> type, Type genericType, Annotation[] annotations, 
                        MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, 
                        OutputStream entityStream) throws IOException {
        kml.marshal(entityStream);
    }

    // Implement other MessageBodyWriter methods as needed
}
