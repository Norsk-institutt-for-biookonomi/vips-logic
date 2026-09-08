package no.nibio.vips.logic.entity.helpers;

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.ext.Provider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.io.OutputStream;
import java.io.IOException;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import org.glassfish.jaxb.runtime.marshaller.NamespacePrefixMapper;

@Provider
@ApplicationScoped
@Produces("application/vnd.google-earth.kml+xml")
public class KmlMessageBodyWriter implements MessageBodyWriter<Kml> {

    private static final String KML_NS = "http://www.opengis.net/kml/2.2";
    private static final JAXBContext KML_CONTEXT = createContext();

    private static JAXBContext createContext() {
        try {
            return JAXBContext.newInstance("de.micromata.opengis.kml.v_2_2_0", Kml.class.getClassLoader());
        } catch (JAXBException e) {
            throw new IllegalStateException("Unable to create KML JAXB context", e);
        }
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, 
                               Annotation[] annotations, MediaType mediaType) {
        return Kml.class.isAssignableFrom(type);
    }

    @Override
    public void writeTo(Kml kml, Class<?> type, Type genericType, Annotation[] annotations, 
                        MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, 
                        OutputStream entityStream) throws IOException {
        try {
            Marshaller marshaller = KML_CONTEXT.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty("org.glassfish.jaxb.namespacePrefixMapper", new UnprefixedKmlNamespaceMapper());
            marshaller.marshal(kml, entityStream);
        } catch (JAXBException e) {
            throw new IOException("Failed to marshal KML", e);
        }
    }

    private static final class UnprefixedKmlNamespaceMapper extends NamespacePrefixMapper {
        @Override
        public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
            if (KML_NS.equals(namespaceUri)) {
                return "";
            }
            if (namespaceUri != null && namespaceUri.startsWith("http://www.google.com/kml/ext/")) {
                return "gx";
            }
            if (namespaceUri != null && namespaceUri.matches("http://www.w3.org/\\d{4}/Atom")) {
                return "atom";
            }
            if (namespaceUri != null && namespaceUri.startsWith("urn:oasis:names:tc:ciq:xsdschema:xAL:")) {
                return "xal";
            }
            return suggestion;
        }
    }
}
