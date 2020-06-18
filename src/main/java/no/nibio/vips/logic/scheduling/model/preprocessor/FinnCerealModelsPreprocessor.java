package no.nibio.vips.logic.scheduling.model.preprocessor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import no.nibio.vips.entity.ModelConfiguration;
import no.nibio.vips.entity.WeatherObservation;
import no.nibio.vips.logic.entity.ForecastConfiguration;
import no.nibio.vips.logic.entity.PointOfInterestWeatherStation;
import no.nibio.vips.logic.scheduling.model.ModelRunPreprocessor;
import no.nibio.vips.logic.scheduling.model.PreprocessorException;
import no.nibio.vips.util.WeatherElements;
import no.nibio.vips.util.WeatherObservationListException;
import no.nibio.vips.util.WeatherUtil;
import no.nibio.vips.util.weather.WeatherDataSourceException;
import no.nibio.vips.util.weather.WeatherDataSourceUtil;

public class FinnCerealModelsPreprocessor extends ModelRunPreprocessor {

	public static final String SOWING_DATE ="FINNCEREAL_FIELD_SOWING_DATE";
	public static final String PREVIOUS_CROP ="FINNCEREAL_PREVIOUS_CROP";
	public static final String TILLAGE ="FINNCEREAL_TILLAGE_METHOD";
	public static final String SUSCEPTIBILITY ="FINNCEREAL_CROP_SUSCEPTIBILITY";
	public static final String MODEL ="FINNCEREAL_MODEL_NAME";

	/**
	 * This method implementation is based on the DOWNCASTModelPreprocessor implemention of getModelCOnfiguration.
	 */
	@Override
	public ModelConfiguration getModelConfiguration(ForecastConfiguration configuration) throws PreprocessorException {
		ModelConfiguration config = new ModelConfiguration();
        PointOfInterestWeatherStation weatherStation =  (PointOfInterestWeatherStation) configuration.getWeatherStationPointOfInterestId();
        WeatherDataSourceUtil wdsUtil = new WeatherDataSourceUtil();
        WeatherUtil wUtil = new WeatherUtil();
        
        List<WeatherObservation> observations;
        Date sowingDate = null;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        format.setTimeZone(TimeZone.getTimeZone(configuration.getTimeZone()));
        try {
        	sowingDate = format.parse(configuration.getForecastModelConfigurationValue(SOWING_DATE));
            observations = wdsUtil.getWeatherObservations(
                    weatherStation,
                    WeatherObservation.LOG_INTERVAL_ID_1H,
                    new String[]{
                        WeatherElements.WIND_SPEED_2M,
                        WeatherElements.WIND_SPEED_10MIN_2M,
                        WeatherElements.RELATIVE_HUMIDITY_MEAN,
                        WeatherElements.PRECIPITATION, 
                        WeatherElements.TEMPERATURE_MEAN 
                    },
                    sowingDate,
                    configuration.getDateEndInTimeZone());
                    Collections.sort(observations);
            observations = wUtil.checkForAndFixHourlyTimeSeriesHoles(observations);
			List<WeatherObservation> temperature = observations.stream().filter(t -> t.getElementMeasurementTypeId().equals(WeatherElements.TEMPERATURE_MEAN))
					.collect(Collectors.toList());
			List<WeatherObservation> rainfall = observations.stream().filter(t -> t.getElementMeasurementTypeId().equals(WeatherElements.PRECIPITATION))
					.collect(Collectors.toList());
			List<WeatherObservation> rh = observations.stream().filter(t -> t.getElementMeasurementTypeId().equals(WeatherElements.RELATIVE_HUMIDITY_MEAN))
					.collect(Collectors.toList());
			List<WeatherObservation> windSpeedAll = observations.stream().filter(t -> t.getElementMeasurementTypeId().equals(WeatherElements.WIND_SPEED_2M))
					.collect(Collectors.toList());
                        
                        // Dealing with wind, which may be dodgy with regards to forecasts etc.
			if(windSpeedAll.isEmpty() || windSpeedAll.size() != temperature.size()) {
                            List<WeatherObservation> windFallback = observations.stream()
                                    .filter(t -> t.getElementMeasurementTypeId().equals(WeatherElements.WIND_SPEED_10MIN_2M))
                                    .collect(Collectors.toList());
                            if(windSpeedAll.isEmpty())
                            {
                                windSpeedAll.addAll(windFallback);
                            }
                            else
                            {
                                Collections.sort(windSpeedAll);
                                Date lastWindTime = windSpeedAll.get(windSpeedAll.size()-1).getTimeMeasured();
                                windSpeedAll.addAll(windFallback.stream().filter(o->o.getTimeMeasured().after(lastWindTime)).collect(Collectors.toList()));
                            }
			}
                        
                        
                        
                        Collections.sort(temperature);
                        /*
                        System.out.println("First temp is at " + temperature.get(0).getTimeMeasured());
                        System.out.println("Last temp is at " + temperature.get(temperature.size()-1).getTimeMeasured());
                        System.out.println("Observations size = " + observations.size());
                        System.out.println("temperature size=" + temperature.size());
                        System.out.println("rainfall size=" + rainfall.size());
                        System.out.println("rh size =" + rh.size());
                        System.out.println("windSpeedAll=" + windSpeedAll.size());
*/
	        config.setModelId(this.getModelId());
	        config.setConfigParameter("timeZone", weatherStation.getTimeZone());
	        config.setConfigParameter("temperature", temperature);
	        config.setConfigParameter("rainfall", rainfall);
	        config.setConfigParameter("rh", rh);
	        config.setConfigParameter("windspeed", windSpeedAll);
	        config.setConfigParameter("precedingCrop", configuration.getForecastModelConfigurationValue(PREVIOUS_CROP));
	        config.setConfigParameter("tillageMethod", configuration.getForecastModelConfigurationValue(TILLAGE));
	        config.setConfigParameter("diseaseSusceptibility", configuration.getForecastModelConfigurationValue(SUSCEPTIBILITY));
	        config.setConfigParameter("model", configuration.getForecastModelConfigurationValue(MODEL));
        } catch (WeatherDataSourceException | WeatherObservationListException | ParseException ex ) {
            throw new PreprocessorException(ex.getMessage());
        }
        return config;
	}

	@Override
	public String getModelId() {
		return "FINNCEREAL";
	}

}
