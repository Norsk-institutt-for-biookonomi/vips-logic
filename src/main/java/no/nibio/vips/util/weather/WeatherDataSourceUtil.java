/*
 * Copyright (c) 2017 NIBIO <http://www.nibio.no/>. 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package no.nibio.vips.util.weather;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import no.nibio.vips.entity.WeatherObservation;
import no.nibio.vips.logic.entity.PointOfInterestWeatherStation;
import no.nibio.vips.logic.scheduling.model.PreprocessorException;
import no.nibio.vips.logic.util.SystemTime;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @copyright 2022 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class WeatherDataSourceUtil {
    
    private static Logger LOGGER = LoggerFactory.getLogger(WeatherDataSourceUtil.class);

    /**
     * Fetches measured data from the stations weather data source, and optionally
     * a weather forecast provider (if so specified in the weather station configuration).
     * Regarding weather forecast parameters: All requested parameters need be present in the
     * forecast in order for any parameters to be fetched. So if you request e.g. TJM5 and TM,
     * you won't get forecast values for any of them, because TJM5 is not present. Solve this
     * by calling this method twice: Once for the parameters with forecasts, and one for the
     * remaining.
     *
     * @param station The WeatherStation to fetch data from
     * @param logIntervalId hourly/daily etc.
     * @param elementMeasurementTypes Which parameters should be fetched
     * @param startTime When to start
     * @param endTime When to stop
     * @param ignoreErrors if true, accepts errors and missing data from source
     * @return
     * @throws PreprocessorException
     */
    public List<WeatherObservation> getWeatherObservations(PointOfInterestWeatherStation station, Integer logIntervalId, String[] elementMeasurementTypes, Date startTime, Date endTime, Boolean ignoreErrors, Set<Integer> toleratedLogIntervalIds) throws WeatherDataSourceException {
        // Get measured (and possibly forecasted, depending on the data source) observations
        LOGGER.info("Get weather observations for {} with URL {}", station.getName(), station.getDataFetchUri());
        List<WeatherObservation> observations = this.getWeatherObservations(station.getDataFetchUri(), logIntervalId, elementMeasurementTypes, startTime, endTime, TimeZone.getTimeZone(station.getTimeZone()), ignoreErrors, toleratedLogIntervalIds);
        Collections.sort(observations);
        // Append forecasts, if available
        Date latestTimeOfMeasuredObservations = observations.isEmpty() ? null : observations.get(observations.size() - 1).getTimeMeasured();
        //System.out.println("latestTimeOfMeasuredObservations = " + latestTimeOfMeasuredObservations);
        // Todo: We don't collect forecast data when the endTime is before the actual, current date (not systemdate)
        if (station.getWeatherForecastProviderId() != null && ! SystemTime.isSystemTimeOffsetFromNow()) 
        {
            try {
                WeatherForecastProvider forecastProvider = WeatherStationProviderFactory.getWeatherForecastProvider(station.getWeatherForecastProviderId().getWeatherForecastProviderId());
                List<WeatherObservation> forecasts = forecastProvider.getWeatherForecasts(station);
                Map<String, List<WeatherObservation>> obsMap = new HashMap<>();
                for (String elementMeasurementType : elementMeasurementTypes) {
                    obsMap.put(elementMeasurementType, new ArrayList<>());
                }
                forecasts.stream().filter((obs) -> (
                        toleratedLogIntervalIds.contains(obs.getLogIntervalId()) && obsMap.get(obs.getElementMeasurementTypeId()) != null)
                    ).forEachOrdered((obs) -> {
                        obsMap.get(obs.getElementMeasurementTypeId()).add(obs);
                    });
                Date latestCommonDate = null;
                for (String elementMeasurementType : elementMeasurementTypes) {
                    List<WeatherObservation> paramObs = obsMap.get(elementMeasurementType);
                    if (paramObs.isEmpty()) {
                        continue;
                    }
                    Collections.sort(paramObs);
                    //System.out.println("elementMeasurementType " + elementMeasurementType + " first date = " + paramObs.get(0).getTimeMeasured());
                    latestCommonDate = latestCommonDate == null ? paramObs.get(paramObs.size() - 1).getTimeMeasured() : new Date(Math.min(latestCommonDate.getTime(), paramObs.get(paramObs.size() - 1).getTimeMeasured().getTime()));
                }
                Integer previousObsSize = null;
                for (String elementMeasurementType : elementMeasurementTypes) {
                    List<WeatherObservation> paramForecastObs = obsMap.get(elementMeasurementType);
                    List<WeatherObservation> forecastsOutsideScope = new ArrayList<>();
                    for (WeatherObservation obs : paramForecastObs) {
                        if (obs.getTimeMeasured().compareTo(latestCommonDate) > 0 || (latestTimeOfMeasuredObservations != null && obs.getTimeMeasured().compareTo(latestTimeOfMeasuredObservations) <= 0)) {
                            forecastsOutsideScope.add(obs);
                        }
                    }
                    paramForecastObs.removeAll(forecastsOutsideScope);
                    if (previousObsSize != null && previousObsSize != paramForecastObs.size()) {
                        throw new WeatherDataSourceException("Weather forecast from " + station.getWeatherForecastProviderId().getName() + " does not provide consistent data. Array length differ " + "between parameters for same time span. First deviation " + "was found for " + elementMeasurementType + ", it has " + paramForecastObs.size() + " elements, compared to " + previousObsSize + " for the previous parameter. End of the period is " + latestCommonDate);
                    }
                }
                for (String elementMeasurementType : elementMeasurementTypes) {
                    observations.addAll(obsMap.get(elementMeasurementType));
                }
            } catch (ParseWeatherDataException ex) {
                throw new WeatherDataSourceException(ex.getMessage());
            }
        }
        return observations;
    }
    
    /**
     * The intolerant version. Only accept observations with same logIntervalId as requested
     * @param station
     * @param logIntervalId
     * @param elementMeasurementTypes
     * @param startTime
     * @param endTime
     * @param ignoreErrors
     * @return
     * @throws WeatherDataSourceException 
     */
    public List<WeatherObservation> getWeatherObservations(PointOfInterestWeatherStation station, Integer logIntervalId, String[] elementMeasurementTypes, Date startTime, Date endTime, Boolean ignoreErrors) throws WeatherDataSourceException {
        return this.getWeatherObservations(station, logIntervalId, elementMeasurementTypes, startTime, endTime, ignoreErrors, new HashSet<>(Arrays.asList(logIntervalId)));
    }

    /**
     * Fetches measured data from the stations weather data source, and optionally
     * a weather forecast provider (if so specified in the weather station configuration).
     * Regarding weather forecast parameters: All requested parameters need be present in the
     * forecast in order for any parameters to be fetched. So if you request e.g. TJM5 and TM,
     * you won't get forecast values for any of them, because TJM5 is not present. Solve this
     * by calling this method twice: Once for the parameters with forecasts, and one for the
     * remaining.
     * 
     * Requires the source to provide a complete data set (still, no guarantee)
     *
     * @param station The WeatherStation to fetch data from
     * @param logIntervalId hourly/daily etc.
     * @param elementMeasurementTypes Which parameters should be fetched
     * @param startTime When to start
     * @param endTime When to stop
     * @return
     * @throws PreprocessorException
     */
    public List<WeatherObservation> getWeatherObservations(PointOfInterestWeatherStation station, Integer logIntervalId, String[] elementMeasurementTypes, Date startTime, Date endTime) throws WeatherDataSourceException
    {
        return this.getWeatherObservations(station, logIntervalId, elementMeasurementTypes, startTime, endTime, Boolean.FALSE);
    }
    
    public List<WeatherObservation> getWeatherObservations(String JSONtext) throws IOException {
        return new ObjectMapper().readValue(JSONtext, new TypeReference<List<WeatherObservation>>() {
        });
    }
    
    /**
     * The intolerant version. Only accept observations with same logIntervalId as requested
     * @param fetchURI
     * @param logIntervalId
     * @param elementMeasurementTypes
     * @param startTime
     * @param endTime
     * @param timeZone
     * @param ignoreErrors
     * @return
     * @throws WeatherDataSourceException 
     */
    public List<WeatherObservation> getWeatherObservations(String fetchURI, Integer logIntervalId, String[] elementMeasurementTypes, Date startTime, Date endTime, TimeZone timeZone, Boolean ignoreErrors) throws WeatherDataSourceException {
        return this.getWeatherObservations(fetchURI, logIntervalId, elementMeasurementTypes, startTime, endTime, timeZone, ignoreErrors, new HashSet<>(Arrays.asList(logIntervalId)));
    }

    /**
     * Collects weather observations from a data source
     * @param fetchURI Base URI. E.g. http://lmt.nibio.no/agrometbase/export/getVIPS3JSONWeatherData.php?weatherStationId=13
     * @param logIntervalId
     * @param elementMeasurementTypes
     * @param startTime
     * @param endTime
     * @param timeZone
     * @return
     */
    public List<WeatherObservation> getWeatherObservations(String fetchURI, Integer logIntervalId, String[] elementMeasurementTypes, Date startTime, Date endTime, TimeZone timeZone, Boolean ignoreErrors, Set<Integer> toleratedLogIntervalIds) throws WeatherDataSourceException {
        SimpleDateFormat dateOutput = new SimpleDateFormat("yyyy-MM-dd");
        dateOutput.setTimeZone(timeZone);
        SimpleDateFormat hourOutput = new SimpleDateFormat("H");
        hourOutput.setTimeZone(timeZone);
        StringBuilder URL = new StringBuilder(fetchURI)
                .append(fetchURI.contains("?") ? "&" : "?")
                .append("logIntervalId=").append(logIntervalId)
                .append("&timeZone=").append(timeZone.getID())
                .append("&startDate=").append(dateOutput.format(startTime))
                .append("&startTime=").append(hourOutput.format(startTime))
                .append("&endDate=").append(dateOutput.format(endTime))
                .append("&endTime=").append(hourOutput.format(endTime));
        for (String type : elementMeasurementTypes) {
            URL.append("&elementMeasurementTypes[]=").append(type);
        }
        if(ignoreErrors)
        {
            URL.append("&ignoreErrors=true");
        }
        URLConnection URLConn = null;
        InputStream URLStream = null;
        InputStream error = null;
        String URLOutput;
        int connectTimeout = 30;
            int readTimeout = 120;
        try {
            URL weatherURL = new URL(URL.toString());
            LOGGER.debug("URL=" + weatherURL); 
            URLConn = weatherURL.openConnection();
            
            URLConn.setConnectTimeout(1000 * connectTimeout); // If weather data server does not reply in 30 seconds, abort
            URLConn.setReadTimeout(1000 * readTimeout); // If weather data server is not done delivering the data in two minutes, abort
            URLStream = URLConn.getInputStream();
            URLOutput = IOUtils.toString(URLStream);
            List<WeatherObservation> preliminaryResult = this.getWeatherObservations(URLOutput);
            List<WeatherObservation> filteredObservations = new ArrayList<>();
            //System.out.println(this.getClass().getName() + "/preliminaryResult.size()=" + preliminaryResult.size());
            preliminaryResult.stream().filter((candidateObs) -> (
                    toleratedLogIntervalIds.contains(candidateObs.getLogIntervalId())
                    && Arrays.asList(elementMeasurementTypes).contains(candidateObs.getElementMeasurementTypeId())
                    )
                ).forEachOrdered((candidateObs) -> {
                    filteredObservations.add(candidateObs);
                });
            //System.out.println(this.getClass().getName() + "/filteredObservations.size()=" + filteredObservations.size());
            return filteredObservations;
        } catch (IOException ex) {
            StringBuilder errorMessage = new StringBuilder().append("Could not fetch weather observations from URI ").append(URL.toString()).append(".\n");
            String errorOutput = "";
            try {
                error = ((HttpURLConnection) URLConn).getErrorStream();
                errorOutput = IOUtils.toString(error);
            } catch (IOException | NullPointerException ex2) {
            }
            if(ex instanceof SocketTimeoutException)
            {
                errorMessage.append("The weather data server is deemed unresponsive. The connection times out after " + connectTimeout + " seconds of no reply and " + readTimeout + " seconds of no data received after established connection.\n");
            }
            if (errorOutput.isEmpty()) {
                errorMessage.append("There was no output from weather data server to explain this.");
            } else {
                errorMessage.append("Output from weather data server was: \n").append(errorOutput);
            }
            errorMessage.append("\nFirst error output from code was: \n").append(ex.getMessage());
            throw new WeatherDataSourceException(errorMessage.toString());
        } finally {
            if (URLStream != null) {
                IOUtils.closeQuietly(URLStream);
            }
            if (error != null) {
                IOUtils.closeQuietly(error);
            }
        }
    }

}
