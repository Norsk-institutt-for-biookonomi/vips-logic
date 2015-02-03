/*
 * Copyright (c) 2015 Bioforsk <http://www.bioforsk.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the Bioforsk Open Source License as published by 
 * Bioforsk, either version 1 of the License, or (at your option) any
 * later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Bioforsk Open Source License for more details.
 * 
 * You should have received a copy of the Bioforsk Open Source License
 * along with VIPSLogic.  If not, see <http://www.bioforsk.no/licenses/>.
 * 
 */

package no.bioforsk.vips.util.weather;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import no.bioforsk.vips.entity.WeatherObservation;

/**
 * Reads/parses data from a Metos weather station
 * For future versions (need authentication per station): http://www.fieldclimate.com/json_manual/ 
 * @copyright 2015 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public class MetosDataParser {
    public final static String METOS_URL_TEMPLATE = "http://www.fieldclimate.com/rimpro/all_sensors_csv.php?s={0}";
    // Structure of file from Metos, including aggregation type
    private final static String[][] elementMeasurementTypes = {
        {"Q0","AVG"},
        {"RR","SUM"},
        {"FM2","AVG"},
        {"BATTERY","AVG"},
        {"BT","SUM"},
        {"TM","AVG"},
        {"UM","AVG"}
    };
    
    /**
     * Using output designed for RIMPro, parsing into WeatherObservations
     * @param stationID the METOS station ID
     * @return 
     */
    public List<WeatherObservation> getWeatherObservations(String stationID) throws ParseWeatherDataException 
    {
        List<WeatherObservation> retVal = new ArrayList<>();
        SimpleDateFormat dFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            URL metosURL = new URL(MessageFormat.format(MetosDataParser.METOS_URL_TEMPLATE, stationID));
            BufferedReader in = new BufferedReader(
            new InputStreamReader(metosURL.openStream()));

            String inputLine;
            Date testTimestamp = null;
            Date timestamp = null;
            Boolean shouldBe00Now = true;
            String[] data00 = null;
            String[] data30 = null;
            while ((inputLine = in.readLine()) != null)
            {
                String[] data = inputLine.split(";");
                // Skip empty lines
                if(data.length <= 1)
                {
                    continue;
                }
                // Check for valid start of line
                try {
                    
                    testTimestamp = dFormat.parse(data[0] + " " + data[1]);
                } catch (ParseException ex) {
                    continue;
                }
                // Data comes in half-hour chunks (resolution = 30 minutes)
                if(data[1].split(":")[1].equals("00") && shouldBe00Now)
                {
                    data00 = data;
                    timestamp = testTimestamp;
                    shouldBe00Now = false;
                    continue; // So that we summarize only after :30 data has been set too
                }
                else if(data[1].split(":")[1].equals("30") && !shouldBe00Now)
                {
                    data30 = data;
                    shouldBe00Now = true;
                }
                else
                {
                    throw new ParseWeatherDataException("Doesn't make sense!");
                }
                
                for(int i=0;i<MetosDataParser.elementMeasurementTypes.length;i++)
                {
                    Double aggregateValue = null;
                    Double value00 = Double.valueOf(data00[i+2].replaceAll(",","."));
                    Double value30 = Double.valueOf(data30[i+2].replaceAll(",","."));
                    if(MetosDataParser.elementMeasurementTypes[i][1].equals("AVG"))
                    {
                        aggregateValue = (value00 + value30) / 2;
                    }
                    else
                    {
                        aggregateValue = (value00 + value30);
                    }
                    
                    WeatherObservation obs = new WeatherObservation();
                    obs.setTimeMeasured(timestamp);
                    obs.setLogIntervalId(WeatherObservation.LOG_INTERVAL_ID_1H);
                    obs.setElementMeasurementTypeId(MetosDataParser.elementMeasurementTypes[i][0]);
                    obs.setValue(aggregateValue);
                    retVal.add(obs);
                }
            }
                
            in.close();
        } catch (IOException ex) {
            throw new ParseWeatherDataException(ex.getMessage());
        }
        return retVal;
    }
}
