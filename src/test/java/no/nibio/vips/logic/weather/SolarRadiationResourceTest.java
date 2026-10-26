package no.nibio.vips.logic.weather;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.core.Response;
import java.text.ParseException;
import java.util.List;
import no.nibio.vips.entity.WeatherObservation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SolarRadiationResourceTest {

    @Mock
    private SolarRadiationService solarRadiationService;

    private SolarRadiationResource solarRadiationResource;

    @BeforeEach
    public void setUp() {
        solarRadiationResource = new SolarRadiationResource(solarRadiationService);
    }

    @Test
    public void jsonReturnsOkWithObservations() throws Exception {
        List<WeatherObservation> observations = List.of(new WeatherObservation());
        when(solarRadiationService.getCalculatedSolarRadiation(
                        59.9, 10.7, "2023-05-01 00:00", "2023-05-01 03:00", "Europe/Oslo", null))
                .thenReturn(observations);

        Response response = solarRadiationResource.getCalculatedSolarRadiationAtLocationAndTimeJSON(
                59.9, 10.7, "2023-05-01 00:00", "2023-05-01 03:00", "Europe/Oslo", null);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertSame(observations, response.getEntity());
    }

    @Test
    public void jsonReturnsServerErrorWhenDatesAreInvalid() throws Exception {
        ParseException parseException = new ParseException("bad date", 0);
        when(solarRadiationService.getCalculatedSolarRadiation(
                        59.9, 10.7, "not-a-date", "2023-05-01 03:00", "Europe/Oslo", null))
                .thenThrow(parseException);

        Response response = solarRadiationResource.getCalculatedSolarRadiationAtLocationAndTimeJSON(
                59.9, 10.7, "not-a-date", "2023-05-01 03:00", "Europe/Oslo", null);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertSame(parseException, response.getEntity());
    }

    @Test
    public void csvReturnsOkWithBody() throws Exception {
        when(solarRadiationService.getCalculatedSolarRadiationCsv(
                        59.9, 10.7, "2023-05-01 00:00", "2023-05-01 03:00", "Europe/Oslo", 1))
                .thenReturn("csv-body");

        Response response = solarRadiationResource.getCalculatedSolarRadiationAtLocationAndTimeCSV(
                59.9, 10.7, "2023-05-01 00:00", "2023-05-01 03:00", "Europe/Oslo", 1);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("csv-body", response.getEntity());
    }

    @Test
    public void csvReturnsServerErrorWhenDatesAreInvalid() throws Exception {
        ParseException parseException = new ParseException("bad date", 0);
        when(solarRadiationService.getCalculatedSolarRadiationCsv(
                        59.9, 10.7, "not-a-date", "2023-05-01 03:00", "Europe/Oslo", null))
                .thenThrow(parseException);

        Response response = solarRadiationResource.getCalculatedSolarRadiationAtLocationAndTimeCSV(
                59.9, 10.7, "not-a-date", "2023-05-01 03:00", "Europe/Oslo", null);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertSame(parseException, response.getEntity());
    }
}
