package no.nibio.vips.logic.forecast;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Response;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import no.nibio.vips.logic.controller.session.UserBean;
import no.nibio.vips.logic.dto.ForecastResultDto;
import no.nibio.vips.logic.entity.ForecastConfiguration;
import no.nibio.vips.logic.entity.ModelInformation;
import no.nibio.vips.logic.entity.VipsLogicUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import no.nibio.vips.util.ServletUtil;
import org.mockito.Mockito;

@ExtendWith(MockitoExtension.class)
public class ForecastResourceTest {

    @Mock
    private ForecastService forecastService;

    @Mock
    private UserBean userBean;

    @Mock
    private HttpServletRequest httpServletRequest;

    private ForecastResultResource forecastResultResource;
    private ForecastConfigurationResource forecastConfigurationResource;
    private ForecastResource forecastResource;

    @BeforeEach
    public void setUp() {
        forecastResultResource = new ForecastResultResource(forecastService);
        forecastConfigurationResource = new ForecastConfigurationResource(forecastService, userBean);
        forecastResource = new ForecastResource(forecastService, userBean);
    }

    @Test
    public void getForecastResultsReturnsUnauthorizedWhenNotAllowed() {
        when(forecastService.isUserAuthorizedForForecastConfiguration(1L, "token")).thenReturn(false);

        Response response = forecastResultResource.getForecastResults(1L, "token");

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        verifyNoInteractions(userBean);
    }

    @Test
    public void getForecastResultsReturnsOkWhenAuthorized() {
        List<ForecastResultDto> results = List.of(dto(1L));
        when(forecastService.isUserAuthorizedForForecastConfiguration(1L, null)).thenReturn(true);
        when(forecastService.getForecastResultDtos(1L)).thenReturn(results);

        Response response = forecastResultResource.getForecastResults(1L, null);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertSame(results, response.getEntity());
    }

    @Test
    public void getForecastResultsWithInvalidDatesReturnsServerError() {
        Response response = forecastResultResource.getForecastResults(1L, "not-a-date", "2020-01-01");

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        verifyNoInteractions(forecastService);
    }

    @Test
    public void getForecastResultsAggregateReturnsNoContentWhenServiceReturnsNull() {
        when(forecastService.getForecastsAggregateKml(5, List.of(), "u")).thenReturn(null);

        Response response = forecastResultResource.getForecastResultsAggregate(5, List.of(), "u");

        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
    }

    @Test
    public void getPrivateForecastConfigurationsReturnsUnauthorizedWhenUserMissing() {
        UUID uuid = UUID.fromString("11111111-1111-1111-1111-111111111111");
        when(userBean.findVipsLogicUser(uuid)).thenReturn(null);

        Response response = forecastConfigurationResource.getPrivateForecastConfigurations(uuid.toString());

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    public void getPrivateForecastConfigurationsReturnsOkForKnownUser() {
        UUID uuid = UUID.fromString("11111111-1111-1111-1111-111111111111");
        VipsLogicUser user = new VipsLogicUser();
        user.setUserId(9);
        List<ForecastConfiguration> configs = List.of(new ForecastConfiguration());
        when(userBean.findVipsLogicUser(uuid)).thenReturn(user);
        when(forecastService.getPrivateForecastConfigurationsForUser(9)).thenReturn(configs);

        Response response = forecastConfigurationResource.getPrivateForecastConfigurations(uuid.toString());

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertSame(configs, response.getEntity());
    }

    @Test
    public void getModelInformationReturnsNotFoundWhenMissing() {
        when(forecastService.getModelInformation("NOPE")).thenReturn(null);

        Response response = forecastResource.getModelInformation("NOPE");

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("ERROR: Could not find model with id=NOPE", response.getEntity());
    }

    @Test
    public void getModelInformationReturnsOkWhenFound() {
        ModelInformation model = new ModelInformation();
        when(forecastService.getModelInformation("PSILARTEMP")).thenReturn(model);

        Response response = forecastResource.getModelInformation("PSILARTEMP");

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertSame(model, response.getEntity());
    }

    @Test
    public void updateForecastCachesReturnsUnauthorizedForNonLocalClient() {
        try (MockedStatic<ServletUtil> servletUtil = Mockito.mockStatic(ServletUtil.class)) {
            servletUtil.when(() -> ServletUtil.getClientIP(httpServletRequest)).thenReturn("10.0.0.1");

            Response response = forecastResource.updateForecastCaches(httpServletRequest);

            assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
            verifyNoInteractions(forecastService);
        }
    }

    @Test
    public void getLatestForecastResultsForPoiReturnsOk() {
        Map<String, Object> latest = Map.of("results", List.of());
        when(forecastService.getLatestForecastResultsForPoi(3)).thenReturn(latest);

        Response response = forecastResultResource.getLatestForecastResultsForPoi(3);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertSame(latest, response.getEntity());
        verify(forecastService).getLatestForecastResultsForPoi(3);
    }

    private static ForecastResultDto dto(long id) {
        return new ForecastResultDto(id, id, Instant.parse("2020-01-01T00:00:00Z"), null, 0L, "{}");
    }
}
