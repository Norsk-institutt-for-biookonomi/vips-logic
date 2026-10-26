package no.nibio.vips.logic.weather;

import java.text.ParseException;
import java.util.List;
import no.nibio.vips.entity.WeatherObservation;

public interface SolarRadiationService {

    List<WeatherObservation> getCalculatedSolarRadiation(
            Double latitude,
            Double longitude,
            String startTimeStr,
            String endTimeStr,
            String timeZoneStr,
            Integer logIntervalId)
            throws ParseException;

    String getCalculatedSolarRadiationCsv(
            Double latitude,
            Double longitude,
            String startTimeStr,
            String endTimeStr,
            String timeZoneStr,
            Integer logIntervalId)
            throws ParseException;
}
