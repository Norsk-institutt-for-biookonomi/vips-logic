package no.nibio.vips.logic.weather;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import no.nibio.vips.entity.WeatherObservation;
import no.nibio.vips.util.SolarRadiationUtil;

@ApplicationScoped
public class VipsSolarRadiationCalculator implements SolarRadiationCalculator {

    @Override
    public List<WeatherObservation> getCalculatedSolarRadiation(
            Double latitude,
            Double longitude,
            Date startTime,
            Date endTime,
            TimeZone timeZone,
            Integer logIntervalId) {
        return new SolarRadiationUtil().getCalculatedSolarRadiation(
                latitude, longitude, startTime, endTime, timeZone, logIntervalId);
    }
}
