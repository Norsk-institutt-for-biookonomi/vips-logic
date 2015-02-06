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

import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import no.bioforsk.vips.entity.WeatherObservation;
import no.bioforsk.vips.logic.entity.PointOfInterest;
import no.bioforsk.vips.logic.entity.PointOfInterestWeatherStation;
import no.bioforsk.vips.logic.util.DOMUtils;
import no.bioforsk.vips.util.WeatherElements;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @copyright 2015 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public class YrWeatherForecastProvider implements WeatherForecastProvider{

    private final static String YR_API_URL = "http://api.yr.no/weatherapi/locationforecastlts/1.2/?lat={0};lon={1};msl={2}";
    
    @Override
    public List<WeatherObservation> getWeatherForecasts(PointOfInterest location) throws ParseWeatherDataException 
    {
        return this.getWeatherForecasts(location.getLongitude(), location.getLatitude(), location.getAltitude());
    }
    
    
    public List<WeatherObservation> getWeatherForecasts(Double longitude, Double latitude, Double altitude) throws ParseWeatherDataException 
    {
        List<WeatherObservation> yrValues = new ArrayList<>();
        try {
            URL yrURL = new URL(MessageFormat.format(
                    YrWeatherForecastProvider.YR_API_URL, 
                    latitude,
                    longitude,
                    altitude)
            );
            
            // TODO: Parse with DOM parser
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(yrURL.openStream());
            NodeList nodes = doc.getElementsByTagName("time");
            Long hourDelta = 3600l * 1000l;
            Long threeHoursDelta = 3 * hourDelta;
            Long sixHoursDelta = 6 * hourDelta;
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
            Map<Date, WeatherObservation> RRMap = new HashMap<>();
            for(int i=0;i<nodes.getLength();i++)
            {
                Node node = nodes.item(i);
                Date fromTime = format.parse(node.getAttributes().getNamedItem("from").getNodeValue());
                Date toTime = format.parse(node.getAttributes().getNamedItem("to").getNodeValue());
                Node node2 = DOMUtils.getNode("location", node.getChildNodes());
                
                // TODO: Handle different kinds of elements and durations?
                // The instantaneous measured values
                if(fromTime.compareTo(toTime) == 0 && DOMUtils.getNode("temperature", node2.getChildNodes()) != null)
                {
                    WeatherObservation TT = new WeatherObservation();
                    TT.setTimeMeasured(fromTime);
                    TT.setElementMeasurementTypeId(WeatherElements.TEMPERATURE_INSTANTANEOUS);
                    TT.setLogIntervalId(WeatherObservation.LOG_INTERVAL_ID_1H);
                    TT.setValue(Double.parseDouble(DOMUtils.getNodeAttr("temperature","value",node2.getChildNodes())));
                    yrValues.add(TT);
                }
                if(fromTime.compareTo(toTime) == 0 && DOMUtils.getNode("humidity", node2.getChildNodes()) != null)
                {
                    WeatherObservation UU = new WeatherObservation();
                    UU.setTimeMeasured(fromTime);
                    UU.setElementMeasurementTypeId(WeatherElements.RELATIVE_HUMIDITY_INSTANTANEOUS);
                    UU.setLogIntervalId(WeatherObservation.LOG_INTERVAL_ID_1H);
                    UU.setValue(Double.parseDouble(DOMUtils.getNodeAttr("humidity","value",node2.getChildNodes())));
                    yrValues.add(UU);
                }
                // The aggregated values (duration known, but comes in parallel)
                if(fromTime.compareTo(toTime) != 0 && DOMUtils.getNode("precipitation", node2.getChildNodes()) != null)
                {
                    WeatherObservation RR = new WeatherObservation();
                    RR.setTimeMeasured(fromTime);
                    RR.setElementMeasurementTypeId(WeatherElements.PRECIPITATION);
                    RR.setValue(Double.parseDouble(DOMUtils.getNodeAttr("precipitation","value",node2.getChildNodes())));
                    if(toTime.getTime() - fromTime.getTime() == hourDelta)
                    {
                        RR.setLogIntervalId(WeatherObservation.LOG_INTERVAL_ID_1H);
                    }
                    else if(toTime.getTime() - fromTime.getTime() == threeHoursDelta)
                    {
                        RR.setLogIntervalId(WeatherObservation.LOG_INTERVAL_ID_3H);
                    }
                    else if(toTime.getTime() - fromTime.getTime() == sixHoursDelta)
                    {
                        RR.setLogIntervalId(WeatherObservation.LOG_INTERVAL_ID_6H);
                    }
                    else
                    {
                        continue;
                    }
                    // We keep the rain observation with the highest resolution
                    WeatherObservation obsWithSameTimeStamp = RRMap.get(RR.getTimeMeasured());
                    /*System.out.println("Timediff=" + (toTime.getTime() - fromTime.getTime()));
                    System.out.println(RR.getTimeMeasured() + ": " + RR.getValue());
                    System.out.println(RR.getLogIntervalId());*/
                    if(obsWithSameTimeStamp != null)
                    {
                        if(
                                (RR.getLogIntervalId().equals(WeatherObservation.LOG_INTERVAL_ID_3H) && obsWithSameTimeStamp.getLogIntervalId().equals(WeatherObservation.LOG_INTERVAL_ID_6H))
                                || (RR.getLogIntervalId().equals(WeatherObservation.LOG_INTERVAL_ID_1H) && obsWithSameTimeStamp.getLogIntervalId().equals(WeatherObservation.LOG_INTERVAL_ID_3H))
                                || (RR.getLogIntervalId().equals(WeatherObservation.LOG_INTERVAL_ID_1H) && obsWithSameTimeStamp.getLogIntervalId().equals(WeatherObservation.LOG_INTERVAL_ID_6H))
                         )
                        {
                            RRMap.remove(RR.getTimeMeasured());
                        }
                    }
                    RRMap.put(RR.getTimeMeasured(),RR);
                }
            }
            yrValues.addAll(RRMap.values());
        }
        catch(IOException | ParserConfigurationException | SAXException | ParseException ex)
        {
            throw new ParseWeatherDataException(ex.getMessage());
        }
        return this.createHourlyDataFromYr(yrValues);
    }

    private List<WeatherObservation> createHourlyDataFromYr(List<WeatherObservation> yrValues)
    {
        
        List<WeatherObservation> hourlyData = new ArrayList<>();
        List<WeatherObservation> TM = new ArrayList<>();
        List<WeatherObservation> UM = new ArrayList<>();
        List<WeatherObservation> RR = new ArrayList<>();
        Collections.sort(yrValues);
        Calendar cal = Calendar.getInstance();
        for(WeatherObservation yrValue : yrValues)
        {
            switch(yrValue.getElementMeasurementTypeId())
            {
                case WeatherElements.TEMPERATURE_INSTANTANEOUS:
                    if(!TM.isEmpty())
                    {
                        WeatherObservation previousObs = TM.get(TM.size()-1);
                        TM.addAll(this.getInterpolatedObservations(previousObs, yrValue, WeatherElements.TEMPERATURE_MEAN));
                    }
                    yrValue.setElementMeasurementTypeId(WeatherElements.TEMPERATURE_MEAN);
                    TM.add(yrValue);
                    break;
                case WeatherElements.RELATIVE_HUMIDITY_INSTANTANEOUS:
                    if(!UM.isEmpty())
                    {
                        WeatherObservation previousObs = UM.get(UM.size()-1);
                        UM.addAll(this.getInterpolatedObservations(previousObs, yrValue, WeatherElements.RELATIVE_HUMIDITY_MEAN));
                    }
                    yrValue.setElementMeasurementTypeId(WeatherElements.RELATIVE_HUMIDITY_MEAN);
                    UM.add(yrValue);
                    break;
                case WeatherElements.PRECIPITATION:
                    if(!RR.isEmpty())
                    {
                        WeatherObservation previousObs = RR.get(RR.size()-1);
                        cal.setTime(previousObs.getTimeMeasured());
                        cal.add(Calendar.HOUR_OF_DAY, 1);
                        while(cal.getTime().compareTo(yrValue.getTimeMeasured()) < 0)
                        {
                            WeatherObservation dummy = new WeatherObservation();
                            dummy.setTimeMeasured(cal.getTime());
                            dummy.setLogIntervalId(WeatherObservation.LOG_INTERVAL_ID_1H);
                            dummy.setElementMeasurementTypeId(WeatherElements.PRECIPITATION);
                            dummy.setValue(0.0);
                            RR.add(dummy);
                            cal.add(Calendar.HOUR_OF_DAY,1);
                        }
                    }
                    RR.add(yrValue);
                    break;
                default:
                    break;
            }
        }
        hourlyData.addAll(TM);
        hourlyData.addAll(UM);
        hourlyData.addAll(RR);
        return hourlyData;
    }
    
    private List<WeatherObservation> getInterpolatedObservations(WeatherObservation start, WeatherObservation end, String elementMeasurementTypeId)
    {
        List<WeatherObservation> retVal = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        Double difference = end.getValue() - start.getValue();
        Long steps = (end.getTimeMeasured().getTime() - start.getTimeMeasured().getTime()) / 3600000;
        Double delta = difference/steps;
        cal.setTime(start.getTimeMeasured());
        cal.add(Calendar.HOUR_OF_DAY, 1);
        int counter = 1;
        while(cal.getTime().compareTo(end.getTimeMeasured()) < 0)
        {
            WeatherObservation interpolated = new WeatherObservation();
            interpolated.setElementMeasurementTypeId(elementMeasurementTypeId);
            interpolated.setLogIntervalId(WeatherObservation.LOG_INTERVAL_ID_1H);
            interpolated.setTimeMeasured(cal.getTime());
            interpolated.setValue(start.getValue() + (delta * counter++));
            retVal.add(interpolated);
            cal.add(Calendar.HOUR_OF_DAY, 1);
        }
        return retVal;
    }
    
}
