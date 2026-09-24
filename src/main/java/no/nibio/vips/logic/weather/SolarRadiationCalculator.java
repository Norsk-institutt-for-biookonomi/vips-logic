package no.nibio.vips.logic.weather;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import no.nibio.vips.entity.WeatherObservation;

/**
 * Outbound port for calculated solar radiation. There is no persistence; this
 * wraps VIPSCommon {@code SolarRadiationUtil}.
 */
public interface SolarRadiationCalculator {

    List<WeatherObservation> getCalculatedSolarRadiation(
            Double latitude,
            Double longitude,
            Date startTime,
            Date endTime,
            TimeZone timeZone,
            Integer logIntervalId);
}
