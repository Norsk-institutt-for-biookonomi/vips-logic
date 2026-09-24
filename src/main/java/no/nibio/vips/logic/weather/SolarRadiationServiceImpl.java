package no.nibio.vips.logic.weather;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import no.nibio.vips.entity.WeatherObservation;
import no.nibio.vips.util.CSVPrintUtil;

@ApplicationScoped
public class SolarRadiationServiceImpl implements SolarRadiationService {

    private final SolarRadiationCalculator solarRadiationCalculator;

    @Inject
    public SolarRadiationServiceImpl(SolarRadiationCalculator solarRadiationCalculator) {
        this.solarRadiationCalculator = solarRadiationCalculator;
    }

    @Override
    public List<WeatherObservation> getCalculatedSolarRadiation(
            Double latitude,
            Double longitude,
            String startTimeStr,
            String endTimeStr,
            String timeZoneStr,
            Integer logIntervalId)
            throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            TimeZone timeZone = TimeZone.getTimeZone(timeZoneStr);
            Date startTime = format.parse(startTimeStr);
            Date endTime = format.parse(endTimeStr);
            logIntervalId = logIntervalId == null ? WeatherObservation.LOG_INTERVAL_ID_1H : logIntervalId;
            return solarRadiationCalculator.getCalculatedSolarRadiation(
                    latitude, longitude, startTime, endTime, timeZone, logIntervalId);
    }

    @Override
    public String getCalculatedSolarRadiationCsv(
            Double latitude,
            Double longitude,
            String startTimeStr,
            String endTimeStr,
            String timeZoneStr,
            Integer logIntervalId)
            throws ParseException {
        List<WeatherObservation> radiationValues = getCalculatedSolarRadiation(
                latitude, longitude, startTimeStr, endTimeStr, timeZoneStr, logIntervalId);
        TimeZone timeZone = TimeZone.getTimeZone(timeZoneStr);
        return new CSVPrintUtil().printWeatherObservations(radiationValues, timeZone, "\t");
    }
}
