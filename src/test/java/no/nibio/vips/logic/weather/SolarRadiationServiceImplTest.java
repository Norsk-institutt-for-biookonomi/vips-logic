package no.nibio.vips.logic.weather;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import no.nibio.vips.entity.WeatherObservation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SolarRadiationServiceImplTest {

    @Mock
    private SolarRadiationCalculator solarRadiationCalculator;

    private SolarRadiationServiceImpl solarRadiationService;

    @BeforeEach
    public void setUp() {
        solarRadiationService = new SolarRadiationServiceImpl(solarRadiationCalculator);
    }

    @Test
    public void getCalculatedSolarRadiationParsesDatesAndDefaultsLogInterval() throws Exception {
        List<WeatherObservation> observations = List.of(new WeatherObservation());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date startTime = format.parse("2023-05-01 00:00");
        Date endTime = format.parse("2023-05-01 03:00");
        TimeZone timeZone = TimeZone.getTimeZone("Europe/Oslo");
        when(solarRadiationCalculator.getCalculatedSolarRadiation(
                        59.9, 10.7, startTime, endTime, timeZone, WeatherObservation.LOG_INTERVAL_ID_1H))
                .thenReturn(observations);

        assertSame(
                observations,
                solarRadiationService.getCalculatedSolarRadiation(
                        59.9, 10.7, "2023-05-01 00:00", "2023-05-01 03:00", "Europe/Oslo", null));
        verify(solarRadiationCalculator).getCalculatedSolarRadiation(
                59.9, 10.7, startTime, endTime, timeZone, WeatherObservation.LOG_INTERVAL_ID_1H);
    }

    @Test
    public void getCalculatedSolarRadiationKeepsProvidedLogInterval() throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date startTime = format.parse("2023-05-01 00:00");
        Date endTime = format.parse("2023-05-01 03:00");
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        when(solarRadiationCalculator.getCalculatedSolarRadiation(
                        1.0, 2.0, startTime, endTime, timeZone, 3))
                .thenReturn(List.of());

        solarRadiationService.getCalculatedSolarRadiation(
                1.0, 2.0, "2023-05-01 00:00", "2023-05-01 03:00", "UTC", 3);

        verify(solarRadiationCalculator).getCalculatedSolarRadiation(
                1.0, 2.0, startTime, endTime, timeZone, 3);
    }

    @Test
    public void getCalculatedSolarRadiationThrowsWhenStartTimeIsInvalid() {
        assertThrows(
                ParseException.class,
                () -> solarRadiationService.getCalculatedSolarRadiation(
                        59.9, 10.7, "not-a-date", "2023-05-01 03:00", "Europe/Oslo", null));
    }

    @Test
    public void getCalculatedSolarRadiationCsvUsesCalculatorResults() throws Exception {
        when(solarRadiationCalculator.getCalculatedSolarRadiation(
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of());

        String csv = solarRadiationService.getCalculatedSolarRadiationCsv(
                59.9, 10.7, "2023-05-01 00:00", "2023-05-01 03:00", "Europe/Oslo", null);

        assertNotNull(csv);
    }
}
