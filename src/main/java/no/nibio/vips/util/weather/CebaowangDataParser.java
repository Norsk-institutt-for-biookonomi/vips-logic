/*
 * Copyright (c) 2019 NIBIO <http://www.nibio.no/>. 
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.icu.text.MessageFormat;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import no.nibio.vips.entity.WeatherObservation;
import no.nibio.vips.util.WeatherElements;

/**
 * Parses data from the Chinese weather service http://www.cebaowang.com:8089, used in the
 * SINOGRAIN2 project
 * 
 * @copyright 2019 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class CebaowangDataParser {
    public final static String CEBAOWANG_URL_PATH = "http://www.cebaowang.com:8089/external/data/upload";
    public final static String CEBAOWANG_URL_QUERY_TEMPLATE = "?startDate={0}&endDate={1}&{2}";
    private Map<String, String> parameters = Collections.unmodifiableMap((Map<String,String>) Stream.of
        (
            new SimpleEntry<>("kongQiWenDu", WeatherElements.TEMPERATURE_MEAN),
            new SimpleEntry<>("xiangDuiShiDu", WeatherElements.RELATIVE_HUMIDITY_MEAN),
            new SimpleEntry<>("luDianWenDu",WeatherElements.DEW_POINT_TEMPERATURE), 
            //new SimpleEntry<>("tianJiangYuLiang", null), // Daily rainfall
            new SimpleEntry<>("xiaoShiJiangYuLiang", WeatherElements.PRECIPITATION),
            //new SimpleEntry<>("dianChiDianYa", null), // Voltage
            new SimpleEntry<>("shiRunShiJian", WeatherElements.LEAF_WETNESS_DURATION),
            //new SimpleEntry<>("riZhaoShiShu", null), // Sunshine hours the current day
            new SimpleEntry<>("fiveTuRangHanShuiLiang", WeatherElements.SOIL_WATER_CONTENT_5CM),
            new SimpleEntry<>("tenTuRangHanShuiLiang", WeatherElements.SOIL_WATER_CONTENT_10CM),
            new SimpleEntry<>("twentyFiveTuRangHanShuiLiang", WeatherElements.SOIL_WATER_CONTENT_25CM),
            new SimpleEntry<>("fiftyTuRangHanShuiLiang", WeatherElements.SOIL_WATER_CONTENT_50CM),
            new SimpleEntry<>("fiveTuRangWenDu", WeatherElements.SOIL_TEMPERATURE_5CM_MEAN),
            new SimpleEntry<>("tenTuRangWenDu", WeatherElements.SOIL_TEMPERATURE_10CM_MEAN),
            new SimpleEntry<>("twentyFiveTuRangWenDu", WeatherElements.SOIL_TEMPERATURE_25CM_MEAN),
            new SimpleEntry<>("fiftyTuRangWenDu", WeatherElements.SOIL_TEMPERATURE_50CM_MEAN),
            new SimpleEntry<>("tuRangDaoDianLv", WeatherElements.SOIL_CONDUCTIVITY_5CM)
        )
        .collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue()))
    );
    
    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    public List<WeatherObservation> getWeatherObservations(String stationID, TimeZone timeZone, Date startDate, Date endDate) throws ParseWeatherDataException 
    {
        format.setTimeZone(timeZone);
        List<WeatherObservation> retVal = null;
        try
        {
            URL cebaowangURL = new URL(CEBAOWANG_URL_PATH + MessageFormat.format(
                    CEBAOWANG_URL_QUERY_TEMPLATE, 
                    URLEncoder.encode(format.format(startDate),"UTF-8"), 
                    URLEncoder.encode(format.format(endDate),"UTF-8"),
                    stationID
                )
            );
            //System.out.println(cebaowangURL);
            BufferedReader in = new BufferedReader(
                new InputStreamReader(cebaowangURL.openStream())
            );
            String inputLine;
            Date testTimestamp;
            String all = new String();
            // Reading data into String of JSON data
            while ((inputLine = in.readLine()) != null)
            {
                all += inputLine;
            }
            // Get objects from JSON parser method
            retVal = getParsedObservations(all, timeZone);
        }
        catch(ParseException | IOException ex)
        {ex.printStackTrace();
            throw new ParseWeatherDataException(ex.getMessage());
        }
        
        return retVal;
    }
    
    public List<WeatherObservation> getParsedObservations(String jsonTxt, TimeZone timeZone) throws IOException, ParseException
    {
        ObjectMapper oMapper = new ObjectMapper();
        JsonNode jNode = oMapper.readTree(jsonTxt);
        List<WeatherObservation> retVal = new ArrayList<>();
        JsonNode data = jNode.get("data");
        
        for(JsonNode dataForAGivenTime:data)
        {
            Date timeMeasured = this.format.parse(dataForAGivenTime.get("collectTime").asText());
            //System.out.println(timeMeasured + "=" + dataForAGivenTime.get("collectTime").asText());
            this.parameters.keySet().forEach(paramName->{
                WeatherObservation obs = new WeatherObservation();
                obs.setTimeMeasured(timeMeasured);
                obs.setElementMeasurementTypeId(this.parameters.get(paramName));
                obs.setLogIntervalId(WeatherObservation.LOG_INTERVAL_ID_1H); // TODO: Check this!! Is every 2nd hour as of now
                obs.setValue(dataForAGivenTime.get(paramName).asDouble());
                retVal.add(obs);
            });
        }
        
        return retVal;
    }
}
