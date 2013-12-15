package no.bioforsk.vips.logic.scheduling.model.preprocessor;

import no.bioforsk.vips.logic.entity.ForecastConfiguration;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import no.bioforsk.vips.entity.ModelConfiguration;
import no.bioforsk.vips.entity.WeatherObservation;
import no.bioforsk.vips.logic.entity.PointOfInterestWeatherStation;
import no.bioforsk.vips.logic.scheduling.model.ModelRunPreprocessor;
import no.bioforsk.vips.logic.util.SystemTime;

/**
 * Gathers necessary data to run the Apple Scab Model (APPLESCABM).
 * Also responsible for providing forecast configuration form with the correct input parameters
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public class AppleScabModelPreprocessor extends ModelRunPreprocessor{
    
    public final static String APPLESCABM_START_DATE_ASCOSPORE_MATURITY = "APPLESCABM_START_DATE_ASCOSPORE_MATURITY";

    @Override
    public ModelConfiguration getModelConfiguration(ForecastConfiguration configuration) {
        //configuration.getDateStart();
        PointOfInterestWeatherStation weatherStation = (PointOfInterestWeatherStation) configuration.getLocationPointOfInterestId();
        // What timezone is the calculation for
        TimeZone timeZone = TimeZone.getTimeZone(weatherStation.getTimeZone());
        //System.out.println("timeZone ID in preprocessor=" + timeZone.getID());
        // When do we start calculation ascospore maturity
        // Normally the date for "green tip"
        Date startDateAscosporeMaturity = null;
        // Check to see if forecast has ended
        Date dateEndWeatherData = configuration.getDateEnd().compareTo(SystemTime.getSystemTime()) < 0 ? configuration.getDateEnd() : SystemTime.getSystemTime();
        // We need data until hour 23 of this date, need to fix that
        Calendar cal = Calendar.getInstance(timeZone);
        cal.setTime(dateEndWeatherData);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        dateEndWeatherData = cal.getTime();
        try
        {
            startDateAscosporeMaturity = new SimpleDateFormat("yyyy-MM-dd").parse(configuration.getForecastModelConfigurationValue(APPLESCABM_START_DATE_ASCOSPORE_MATURITY));
        }catch (ParseException ex)
        {
            ex.printStackTrace();
            // TODO: Do something sensible!!!!!
        }

        
        // Use Jackson to parse JSON from server
        // Weather data collections
        List<WeatherObservation> observations = getWeatherObservations(weatherStation.getDataFetchUri(), WeatherObservation.LOG_INTERVAL_ID_1H, new String[]{"TM", "RR", "BT"}, startDateAscosporeMaturity, dateEndWeatherData, timeZone);
        //System.out.println("Observations=" + observations.toString());
        
        // Create the complete model configuration object
        ModelConfiguration retVal = new ModelConfiguration();
        retVal.setModelId(this.getModelId());
        
        retVal.setConfigParameter("startDateAscosporeMaturity", new SimpleDateFormat("yyyy-MM-dd").format(startDateAscosporeMaturity));
        retVal.setConfigParameter("timeZone", timeZone.getID());
        retVal.setConfigParameter("observations", observations);
        
        return retVal;
    }

    @Override
    public String getModelId() {
        return "APPLESCABM";
    }

}
