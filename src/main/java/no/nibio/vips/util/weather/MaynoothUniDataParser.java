/*
 * Copyright (c) 2016 NIBIO <http://www.nibio.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the NIBIO Open Source License as published by 
 * NIBIO, either version 1 of the License, or (at your option) any
 * later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * NIBIO Open Source License for more details.
 * 
 * You should have received a copy of the NIBIO Open Source License
 * along with VIPSLogic.  If not, see <http://www.nibio.no/licenses/>.
 * 
 */

package no.nibio.vips.util.weather;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import no.nibio.vips.entity.WeatherObservation;
import no.nibio.vips.util.WeatherUtil;

/**
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class MaynoothUniDataParser {
    public final static String MAYNOOTH_URL_TEMPLATE = "file:///home/treinar/prosjekter/vips/projects/2016_irland/hly375_test.csv";
    // Pparameters, including name and aggregation type
    private final static String[][] elementMeasurementTypes = {
        {"rain","RR","SUM"},
        {"temp","TM","AVG"},
        {"wetb","WETBULB","AVG"},
        {"dewp","DEWPOINT","AVG"},
        {"vappr","EM2MC","AVG"},
        {"rhum","UM","AVG"},
        {"msl","PPP","AVG"},
        {"wdsp","FM2","AVG"},
        {"wddir","DM2","AVG"},
        {"soglrad","Q0","AVG"}
    };
    
    /**
     * Makes sure only data after the requested date is returned
     * @param stationID
     * @param timeZone
     * @param startDate
     * @return
     * @throws ParseWeatherDataException 
     */
    public List<WeatherObservation> getWeatherObservations(String stationID, TimeZone timeZone, Date startDate) throws ParseWeatherDataException 
    {
        // TODO: The endDate is introduced for testing purposes
        Calendar cal = Calendar.getInstance(timeZone);
        cal.setTime(startDate);
        cal.set(Calendar.MONTH, Calendar.NOVEMBER);
        Date endDate = cal.getTime();
        List<WeatherObservation> retVal = new ArrayList<>();
        List<WeatherObservation> allObservations = this.getWeatherObservations(stationID, timeZone);
        for(WeatherObservation obs: allObservations)
        {
            if(obs.getTimeMeasured().compareTo(startDate) >= 0 && obs.getTimeMeasured().compareTo(endDate) <= 0)
            {
                retVal.add(obs);
            }
        }
        return retVal;
    }
    
    public List<WeatherObservation> getWeatherObservations(String stationID, TimeZone timeZone) throws ParseWeatherDataException 
    {
        List<WeatherObservation> retVal = new ArrayList<>();
        SimpleDateFormat dFormat = new SimpleDateFormat("M/d/yyyy H:mm");
        dFormat.setTimeZone(timeZone);
        // Assuming 1 hour resolution until we find a timestamp that says :30
        Integer logIntervalId = WeatherObservation.LOG_INTERVAL_ID_1H;
        List<String[]> data = new ArrayList<>();
        String[] headers;
        Map<Integer, Integer> elementOrdering = new HashMap<>();
        try {
            URL maynoothURL = new URL(MessageFormat.format(MaynoothUniDataParser.MAYNOOTH_URL_TEMPLATE, stationID));
            BufferedReader in = new BufferedReader(
            new InputStreamReader(maynoothURL.openStream()));

            String inputLine;
            Date testTimestamp = null;

            // We need to collect all the lines first, because we need to analyze
            // If the resolution is 30 minutes or 1 hour
            while ((inputLine = in.readLine()) != null)
            {
                //System.out.println(inputLine);
                String[] lineData = inputLine.split(",");
                // Skip empty lines
                if(lineData.length <= 1)
                {
                    continue;
                }
                // Check for valid start of line
                try {
                    testTimestamp = dFormat.parse(lineData[0]);
                    data.add(lineData);
                } catch (ParseException ex) {
                    
                    // Is this the heading line?
                    // Then we parse it to set the ordering of elements
                    if(lineData[0].equals("date"))
                    {
                        headers = lineData;
                        // Datum and Zeit should always be the two first ones
                        for(int i=1;i<lineData.length;i++)
                        {
                            for(int j=0;j<elementMeasurementTypes.length;j++)
                            {
                                if(elementMeasurementTypes[j][0].equals(lineData[i]))
                                {
                                    elementOrdering.put(i,j);
                                }
                            }
                        }
                    }
                }
            }
            in.close();
        } catch (IOException ex) {
            throw new ParseWeatherDataException(ex.getMessage());
        }

        Date timestamp = null;
        WeatherUtil wUtil = new WeatherUtil();
        for(String[] lineData: data)
        {
            // If minute != 00 we skip the line
            if(!lineData[0].split(":")[1].equals("00"))
            {
                continue;
            }
            try
            {
                timestamp = dFormat.parse(lineData[0]);
            }
            catch(ParseException ex)
            {
                throw new ParseWeatherDataException("Error with time stamp in weather data from Maynooth Uni station: " + ex.getMessage());
            }
            for(Integer i=1;i<lineData.length;i++)
            {
                // Skip empty columns
                Double value;
                if(lineData[i].trim().isEmpty())
                {
                    value = 0.0;
                }
                else
                {
                    value = Double.valueOf(lineData[i].replaceAll(",","."));
                }
                Integer elementMeasurementTypeIndex = elementOrdering.get(i);
                // This means there is an element type we don't collect
                if(elementMeasurementTypeIndex == null)
                {
                    continue;
                }
                WeatherObservation obs = new WeatherObservation();
                obs.setTimeMeasured(timestamp);
                obs.setLogIntervalId(WeatherObservation.LOG_INTERVAL_ID_1H);
                obs.setElementMeasurementTypeId(MaynoothUniDataParser.elementMeasurementTypes[elementMeasurementTypeIndex][1]);
                if(obs.getElementMeasurementTypeId().equals("Q0"))
                {
                    // Solar radiation in data set is in Joule / square centimeter / hour
                    obs.setValue(wUtil.calculateAverageHourWattsPerSquareMeterFromJoulePerSquareCentimeter(value));
                }
                else
                {
                    obs.setValue(value);
                }
                retVal.add(obs);
            }
        }

        return retVal;
    }
}
