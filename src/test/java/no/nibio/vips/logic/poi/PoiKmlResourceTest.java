package no.nibio.vips.logic.poi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import no.nibio.vips.logic.i18n.SessionLocaleUtil;
import no.nibio.vips.logic.util.Globals;

@ExtendWith(MockitoExtension.class)
public class PoiKmlResourceTest {

    @Mock
    private PointOfInterestService pointOfInterestService;

    @Mock
    private HttpServletRequest request;

    private WeatherStationKmlResource weatherStationKmlResource;
    private PoiKmlResource poiKmlResource;

    @BeforeEach
    public void setUp() {
        weatherStationKmlResource = new WeatherStationKmlResource(pointOfInterestService);
        poiKmlResource = new PoiKmlResource(pointOfInterestService);
    }

    @Test
    public void getWeatherStationsReturnsOkWithKml() {
        Kml kml = KmlFactory.createKml();
        try (MockedStatic<SessionLocaleUtil> localeUtil = Mockito.mockStatic(SessionLocaleUtil.class)) {
            localeUtil.when(() -> SessionLocaleUtil.getI18nBundle(request)).thenReturn(null);
            when(pointOfInterestService.getWeatherStationsKml(
                            1, 2, 3, Globals.SERVER_NAME, null))
                    .thenReturn(kml);

            Response response = weatherStationKmlResource.getWeatherStations(2, 3, 1, request);

            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertSame(kml, response.getEntity());
            verify(pointOfInterestService).getWeatherStationsKml(1, 2, 3, Globals.SERVER_NAME, null);
        }
    }

    @Test
    public void getPoisReturnsOkWithKml() {
        Kml kml = KmlFactory.createKml();
        try (MockedStatic<SessionLocaleUtil> localeUtil = Mockito.mockStatic(SessionLocaleUtil.class)) {
            localeUtil.when(() -> SessionLocaleUtil.getI18nBundle(request)).thenReturn(null);
            when(pointOfInterestService.getPoisKml(1, 4, 5, Globals.SERVER_NAME, null)).thenReturn(kml);

            Response response = poiKmlResource.getPois(4, 5, 1, request);

            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertSame(kml, response.getEntity());
            verify(pointOfInterestService).getPoisKml(1, 4, 5, Globals.SERVER_NAME, null);
        }
    }
}
