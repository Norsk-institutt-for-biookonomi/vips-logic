/**
 * Project			:	VIPSLogic
 * Organization			:	NIBIO, Ås
 * Department			:	VIPS
 * 
 * Author			:	bhabesh
 *
 * 
 * File Name			:	AltenariaModelPreprocessor.java
 * Package Name			:	no.nibio.vips.logic.scheduling.model.preprocessor
 * Date of creation		:	12 May 2020 10:07:46 am
 * Date of modification :	
 * 
 * Summary			:	
 *
 */
/*
 * 
Error reading included file Templates/Classes/Templates/Licenses/license-nibio_open_source_license.ftl.txt */

package no.nibio.vips.logic.scheduling.model.preprocessor;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import no.nibio.vips.entity.ModelConfiguration;
import no.nibio.vips.entity.WeatherObservation;
import no.nibio.vips.logic.entity.ForecastConfiguration;
import no.nibio.vips.logic.entity.PointOfInterestWeatherStation;
import no.nibio.vips.logic.scheduling.model.ModelRunPreprocessor;
import no.nibio.vips.logic.scheduling.model.PreprocessorException;
import no.nibio.vips.logic.util.SystemTime;
import no.nibio.vips.util.WeatherElements;
import no.nibio.vips.util.WeatherUtil;
import no.nibio.vips.util.weather.WeatherDataSourceException;
import no.nibio.vips.util.weather.WeatherDataSourceUtil;

/**
 *
 * @author bhabesh
 */
public class AltenariaModelPreprocessor extends ModelRunPreprocessor{
    
    /**
     * method name : getModelConfiguration
     * @param      :
     * @return     : no.nibio.vips.entity.ModelConfiguration
     *
     * purpose     :
     *
     * date        : Expression date is undefined on line 20, column 19 in Templates/Classes/Code/GeneratedMethodBody. Expression time is undefined on line 20, column 27 in Templates/Classes/Code/GeneratedMethodBody.
     */
    @Override
    public ModelConfiguration getModelConfiguration(ForecastConfiguration configuration) throws PreprocessorException
    {
        PointOfInterestWeatherStation weatherStation = (PointOfInterestWeatherStation) configuration.getWeatherStationPointOfInterestId();
        // What timezone is the calculation for
        TimeZone timeZone = TimeZone.getTimeZone(weatherStation.getTimeZone());
        // Getting date three days after "today"
        Calendar cal = Calendar.getInstance(timeZone);
        cal.setTime(SystemTime.getSystemTime());
        cal.add(Calendar.DATE, 3);
        WeatherUtil wUtil = new WeatherUtil();
        Date endDate = wUtil.normalizeToExactDate(cal.getTime(), timeZone);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DATE, 1);
        Date startDate = wUtil.normalizeToExactDate(configuration.getDateStart(), timeZone);
        
        ModelConfiguration retVal = new ModelConfiguration();
        WeatherDataSourceUtil wdsUtil = new WeatherDataSourceUtil();
        List<WeatherObservation> observations;
        
        try {
            observations = wdsUtil.getWeatherObservations( 
                    weatherStation,
                    WeatherObservation.LOG_INTERVAL_ID_1D,
                    new String[]{
                        WeatherElements.TEMPERATURE_MEAN, 
                        WeatherElements.SOIL_TEMPERATURE_10CM_MEAN
                    },
                    startDate, 
                    endDate);
        } catch (WeatherDataSourceException ex) {
            throw new PreprocessorException(ex.getMessage());
        }
        
           // If daily observations are not available, try hourly
        if(observations == null || observations.isEmpty())
        {
            try {
                observations = wdsUtil.getWeatherObservations(
                        weatherStation,
                        WeatherObservation.LOG_INTERVAL_ID_1H,
                        new String[]{
                            WeatherElements.TEMPERATURE_MEAN,
                            WeatherElements.SOIL_TEMPERATURE_10CM_MEAN
                        },
                        startDate,
                        endDate);
            } catch (WeatherDataSourceException ex) {
                throw new PreprocessorException(ex.getMessage());
            }
        }
        
        // TODO:  weather data validation
        retVal.setModelId(this.getModelId());
        retVal.setConfigParameter("timeZone", timeZone.getID());
        retVal.setConfigParameter("observations", observations);
            
        return retVal; 
        
    }

    /**
    * method name : getModelId
    * @param      :
    * @return     : java.lang.String
    *
    * purpose     :
    *
    * date        : Expression date is undefined on line 20, column 19 in Templates/Classes/Code/GeneratedMethodBody. Expression time is undefined on line 20, column 27 in Templates/Classes/Code/GeneratedMethodBody.
    */
    @Override
    public String getModelId()
    {

        return "ALTERNARIA";
    }

}
