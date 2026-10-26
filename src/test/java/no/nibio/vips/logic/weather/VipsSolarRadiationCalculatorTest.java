package no.nibio.vips.logic.weather;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import no.nibio.vips.entity.WeatherObservation;
import no.nibio.vips.util.SolarRadiationUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class VipsSolarRadiationCalculatorTest {

    @Test
    public void getCalculatedSolarRadiationDelegatesToUtil() {
        List<WeatherObservation> observations = List.of(new WeatherObservation());
        Date startTime = new Date(0);
        Date endTime = new Date(3_600_000);
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        try (MockedConstruction<SolarRadiationUtil> mocked =
                Mockito.mockConstruction(
                        SolarRadiationUtil.class,
                        (mock, context) -> when(mock.getCalculatedSolarRadiation(
                                        59.9, 10.7, startTime, endTime, timeZone, 1))
                                .thenReturn(observations))) {
            List<WeatherObservation> result = new VipsSolarRadiationCalculator()
                    .getCalculatedSolarRadiation(59.9, 10.7, startTime, endTime, timeZone, 1);

            assertSame(observations, result);
            verify(mocked.constructed().get(0))
                    .getCalculatedSolarRadiation(59.9, 10.7, startTime, endTime, timeZone, 1);
        }
    }
}
