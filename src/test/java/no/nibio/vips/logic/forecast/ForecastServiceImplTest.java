package no.nibio.vips.logic.forecast;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import no.nibio.vips.logic.controller.session.ForecastBean;
import no.nibio.vips.logic.controller.session.OrganismBean;
import no.nibio.vips.logic.controller.session.UserBean;
import no.nibio.vips.logic.dto.ForecastConfigurationDto;
import no.nibio.vips.logic.dto.ForecastResultDto;
import no.nibio.vips.logic.entity.ForecastConfiguration;
import no.nibio.vips.logic.entity.ForecastResult;
import no.nibio.vips.logic.entity.VipsLogicUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ForecastServiceImplTest {

    @Mock
    private ForecastRepository forecastRepository;

    @Mock
    private ForecastBean forecastBean;

    @Mock
    private UserBean userBean;

    @Mock
    private OrganismBean organismBean;

    private ForecastServiceImpl forecastService;

    @BeforeEach
    public void setUp() {
        forecastService = new ForecastServiceImpl(forecastRepository, forecastBean, userBean, organismBean);
    }

    @Test
    public void isUserAuthorizedWhenConfigurationMissing() {
        when(forecastBean.getForecastConfigurationDto(1L)).thenReturn(null);

        assertTrue(forecastService.isUserAuthorizedForForecastConfiguration(1L, null));
    }

    @Test
    public void isUserAuthorizedRejectsPrivateForecastWithoutUser() {
        ForecastConfigurationDto dto = new ForecastConfigurationDto();
        dto.setIsPrivate(true);
        when(forecastBean.getForecastConfigurationDto(1L)).thenReturn(dto);

        assertFalse(forecastService.isUserAuthorizedForForecastConfiguration(1L, null));
        verifyNoInteractions(userBean);
    }

    @Test
    public void isUserAuthorizedAcceptsPrivateForecastForOwner() {
        ForecastConfigurationDto dto = new ForecastConfigurationDto();
        dto.setIsPrivate(true);
        dto.setVipsLogicUserId(12);
        UUID uuid = UUID.fromString("11111111-1111-1111-1111-111111111111");
        VipsLogicUser user = new VipsLogicUser();
        user.setUserId(12);
        when(forecastBean.getForecastConfigurationDto(1L)).thenReturn(dto);
        when(userBean.findVipsLogicUser(uuid)).thenReturn(user);

        assertTrue(forecastService.isUserAuthorizedForForecastConfiguration(1L, uuid.toString()));
    }

    @Test
    public void getForecastResultDtosReturnsEmptyListWhenRepositoryReturnsNull() {
        when(forecastRepository.findResultDtosByConfigurationId(2L)).thenReturn(null);

        assertEquals(List.of(), forecastService.getForecastResultDtos(2L));
    }

    @Test
    public void getForecastResultDtosFiltersLatestDays() {
        ForecastResultDto older = dtoAt(Instant.parse("2020-01-01T00:00:00Z"));
        ForecastResultDto newer = dtoAt(Instant.parse("2020-01-10T00:00:00Z"));
        when(forecastRepository.findResultDtosByConfigurationId(2L)).thenReturn(new ArrayList<>(List.of(older, newer)));

        List<ForecastResultDto> results = forecastService.getForecastResultDtos(2L, 2);

        assertEquals(List.of(newer), results);
    }

    @Test
    public void getForecastResultsCsvReturnsEmptyStringWhenNoResults() {
        when(forecastRepository.findResultsByConfigurationId(3L)).thenReturn(List.of());

        assertEquals("", forecastService.getForecastResultsCsv(3L));
    }

    @Test
    public void getForecastResultsCsvBuildsHeaderAndRow() {
        ForecastResult result = org.mockito.Mockito.mock(ForecastResult.class);
        when(result.getKeys()).thenReturn(java.util.Set.of("WEATHER.TM"));
        when(result.getValidTimeStart()).thenReturn(new Date(0));
        when(result.getValidTimeEnd()).thenReturn(null);
        when(result.getWarningStatus()).thenReturn(2);
        when(result.getAllValuesAsMap()).thenReturn(java.util.Map.of("WEATHER.TM", "11.5"));
        when(forecastRepository.findResultsByConfigurationId(3L)).thenReturn(List.of(result));

        String csv = forecastService.getForecastResultsCsv(3L);

        assertTrue(csv.startsWith("Valid time start,Valid time end,Warning status,WEATHER.TM\n"));
        assertTrue(csv.contains(",2,11.5"));
    }

    @Test
    public void getForecastConfigurationSummariesSkipsSameIncludeOrganization() {
        List<ForecastConfiguration> summaries = new ArrayList<>();
        summaries.add(new ForecastConfiguration());
        when(forecastBean.getForecastConfigurationSummaries(eq(1), any())).thenReturn(summaries);

        List<ForecastConfiguration> result = forecastService.getForecastConfigurationSummaries(1, "1,x", null);

        assertSame(summaries, result);
        verify(forecastBean).getForecastConfigurationSummaries(1, null);
        verify(forecastBean, never()).getForecastConfigurationSummaries(eq(2), any());
    }

    @Test
    public void getForecastsAggregateKmlReturnsNullWhenNoCropCategories() {
        assertEquals(null, forecastService.getForecastsAggregateKml(1, List.of(), "u"));
        verifyNoInteractions(forecastBean, organismBean);
    }

    @Test
    public void getForecastConfigurationsForOrganizationFiltersNegativeIds() {
       
        ForecastConfiguration publicConfig = new ForecastConfiguration();
        publicConfig.setForecastConfigurationId(10L);
        ForecastConfiguration specialized = new ForecastConfiguration();
        specialized.setForecastConfigurationId(-5L);

        when(forecastRepository.findByOrganizationAndDate(eq(4), any(), any()))
                .thenReturn(List.of(publicConfig, specialized));

        List<ForecastConfiguration> results =
                forecastService.getForecastConfigurationsForOrganization(4, List.of(), new Date(), new Date());

        assertEquals(List.of(publicConfig), results);
    }

    private static ForecastResultDto dtoAt(Instant validTimeStart) {
        return new ForecastResultDto(1L, 1L, validTimeStart, null, 0L, "{}");
    }
}
