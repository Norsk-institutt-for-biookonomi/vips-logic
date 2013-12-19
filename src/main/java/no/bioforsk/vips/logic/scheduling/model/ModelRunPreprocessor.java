package no.bioforsk.vips.logic.scheduling.model;

import no.bioforsk.vips.logic.entity.ForecastConfiguration;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import no.bioforsk.vips.entity.ModelConfiguration;
import no.bioforsk.vips.entity.WeatherObservation;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

/**
 * All model preprocessors must implement this
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public abstract class ModelRunPreprocessor {
    
    public abstract ModelConfiguration getModelConfiguration(ForecastConfiguration configuration) throws PreprocessorException;
    
    public abstract String getModelId();
    
    /**
     * Collects weather observations from a data source
     * @param fetchURI Base URI. E.g. http://lmt.bioforsk.no/agrometbase/export/getVIPS3JSONWeatherData.php?weatherStationId=13
     * @param logIntervalId
     * @param elementMeasurementTypes
     * @param startTime
     * @param endTime
     * @param timeZone
     * @return 
     */
    protected List<WeatherObservation> getWeatherObservations(String fetchURI, Integer logIntervalId, String[] elementMeasurementTypes, Date startTime, Date endTime, TimeZone timeZone) throws PreprocessorException
    {
        SimpleDateFormat dateOutput = new SimpleDateFormat("yyyy-MM-dd");
        dateOutput.setTimeZone(timeZone);
        SimpleDateFormat hourOutput = new SimpleDateFormat("H");
        hourOutput.setTimeZone(timeZone);
        
        StringBuilder URL = new StringBuilder(fetchURI)
                .append("&logIntervalId=").append(logIntervalId)
                .append("&timeZone=").append(timeZone.getID())
                .append("&startDate=").append(dateOutput.format(startTime))
                .append("&startTime=").append(hourOutput.format(startTime))
                .append("&endDate=").append(dateOutput.format(endTime))
                .append("&endTime=").append(hourOutput.format(endTime));
        for(String type: elementMeasurementTypes)
        {
                URL.append("&elementMeasurementTypes[]=").append(type);
        }
        //System.out.println("URL = " + URL.toString());
        try
        {
            return new ObjectMapper().readValue(new URL(URL.toString()), new TypeReference<List<WeatherObservation>>(){});
        }catch(IOException ex)
        {
            throw new PreprocessorException("Could not fetch weather observations from URI " + URL.toString());
        }
        
    }
    
}
