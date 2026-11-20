/*
 * Copyright (c) 2015 NIBIO <http://www.nibio.no/>. 
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import no.nibio.vips.entity.WeatherObservation;
import no.nibio.vips.logic.entity.PointOfInterest;
import no.nibio.vips.logic.util.DOMUtils;
import no.nibio.vips.util.WeatherElements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Provides weather forecasts from the Norwegian Meteorological Institute (MET) through the locationforecast API.
 * The API is documented at https://api.met.no/weatherapi/locationforecast/2.0/document
 * Hourly values are returned for temperature, relative humidity, wind speed and precipitation.
 * Only the hourly values supplied by the API are returned. They stretch approximately 60 hours ahead.
 * Later six-hour steps are left out, so the series can be used by risk models that require real hourly data.
 * 
 * 
 * @copyright 2015 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class YrWeatherForecastProvider implements WeatherForecastProvider{
    private static Logger LOGGER = LoggerFactory.getLogger(YrWeatherForecastProvider.class);

    private final static String YR_API_URL = "https://api.met.no/weatherapi/locationforecast/2.0/classic?lat={0}&lon={1}";
    
    @Override
    public List<WeatherObservation> getWeatherForecasts(PointOfInterest location) throws ParseWeatherDataException 
    {
        if(location.getGisGeom() != null)
        {
            return this.getWeatherForecasts(location.getGisGeom().getCoordinate().x, location.getGisGeom().getCoordinate().y, location.getGisGeom().getCoordinate().z);
        }
        else
        {
            return this.getWeatherForecasts(location.getLongitude(), location.getLatitude(), location.getAltitude());
        }
    }
    
    
    public List<WeatherObservation> getWeatherForecasts(Double longitude, Double latitude, Double altitude) throws ParseWeatherDataException 
    {
        List<WeatherObservation> yrValues = new ArrayList<>();
        URL yrURL;
        try {
            yrURL = new URL(MessageFormat.format(
                    YrWeatherForecastProvider.YR_API_URL, 
                    latitude,
                    longitude)
            );
            
            URLConnection connection = yrURL.openConnection();
            connection.setRequestProperty("User-Agent", "www.vips-landbruk.no vips@nibio.no");
            connection.addRequestProperty("accept", "application/xml");
            connection.connect();
            
            LOGGER.debug("yrURL=" + yrURL.toString());
            //System.out.println(getStringFromInputStream(yrURL.openStream()));
            
            
            // TODO: Parse with DOM parser
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(connection.getInputStream());
            NodeList nodes = doc.getElementsByTagName("time");
            Long hourDelta = 3600l * 1000l;
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
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
                if(fromTime.compareTo(toTime) == 0 && DOMUtils.getNode("windSpeed", node2.getChildNodes()) != null)
                {
                    WeatherObservation FF = new WeatherObservation();
                    FF.setTimeMeasured(fromTime);
                    FF.setElementMeasurementTypeId(WeatherElements.WIND_SPEED_10MIN_10M);
                    FF.setLogIntervalId(WeatherObservation.LOG_INTERVAL_ID_1H);
                    FF.setValue(Double.parseDouble(DOMUtils.getNodeAttr("windSpeed","mps",node2.getChildNodes())));
                    yrValues.add(FF);
                }
                // The aggregated values (duration known, but comes in parallel)
                if(fromTime.compareTo(toTime) != 0 && DOMUtils.getNode("precipitation", node2.getChildNodes()) != null)
                {
                    WeatherObservation RR = new WeatherObservation();
                    RR.setTimeMeasured(fromTime);
                    RR.setElementMeasurementTypeId(WeatherElements.PRECIPITATION);
                    RR.setValue(Double.parseDouble(DOMUtils.getNodeAttr("precipitation","value",node2.getChildNodes())));
                    //System.out.println("Timediff=" + (toTime.getTime() - fromTime.getTime()));
                    
                    if(toTime.getTime() - fromTime.getTime() != hourDelta)
                    {
                        continue;
                    }
                    RR.setLogIntervalId(WeatherObservation.LOG_INTERVAL_ID_1H);
                    yrValues.add(RR);
                }
            }
        }
        catch(IOException | ParserConfigurationException | SAXException | ParseException ex)
        {
            ex.printStackTrace();
            throw new ParseWeatherDataException(ex.getClass().getName() + ": " + ex.getMessage());
        }
        return this.createHourlyDataFromYr(yrValues);
    }

    private List<WeatherObservation> createHourlyDataFromYr(List<WeatherObservation> yrValues)
    {
        List<WeatherObservation> hourlyData = new ArrayList<>();
        List<WeatherObservation> TM = new ArrayList<>();
        List<WeatherObservation> UM = new ArrayList<>();
        List<WeatherObservation> FF2 = new ArrayList<>();
        List<WeatherObservation> RR = new ArrayList<>();
        Long hourDelta = 3600l * 1000l;

        Collections.sort(yrValues);
        for(WeatherObservation yrValue : yrValues)
        {
            yrValue.setLogIntervalId(WeatherObservation.LOG_INTERVAL_ID_1H);
            switch(yrValue.getElementMeasurementTypeId())
            {
                case WeatherElements.TEMPERATURE_INSTANTANEOUS:
                    if(this.isNextNativeHour(TM, yrValue, hourDelta))
                    {
                        yrValue.setElementMeasurementTypeId(WeatherElements.TEMPERATURE_MEAN);
                        TM.add(yrValue);
                    }
                    break;
                case WeatherElements.RELATIVE_HUMIDITY_INSTANTANEOUS:
                    if(this.isNextNativeHour(UM, yrValue, hourDelta))
                    {
                        yrValue.setElementMeasurementTypeId(WeatherElements.RELATIVE_HUMIDITY_MEAN);
                        UM.add(yrValue);
                    }
                    break;
                case WeatherElements.WIND_SPEED_10MIN_10M:
                    if(this.isNextNativeHour(FF2, yrValue, hourDelta))
                    {
                        yrValue.setElementMeasurementTypeId(WeatherElements.WIND_SPEED_10MIN_2M);
                        FF2.add(yrValue);
                    }
                    break;
                case WeatherElements.PRECIPITATION:
                    if(this.isNextNativeHour(RR, yrValue, hourDelta))
                    {
                        RR.add(yrValue);
                    }
                    break;
                default:
                    break;
            }
        }
        hourlyData.addAll(TM);
        hourlyData.addAll(UM);
        hourlyData.addAll(RR);
        hourlyData.addAll(FF2);
        return hourlyData;
    }

    private boolean isNextNativeHour(List<WeatherObservation> kept, WeatherObservation candidate, Long hourDelta)
    {
        if(kept.isEmpty())
        {
            return true;
        }
        Date previousTime = kept.get(kept.size() - 1).getTimeMeasured();
        return candidate.getTimeMeasured().getTime() - previousTime.getTime() == hourDelta;
    }

    private String getStringFromInputStream(InputStream is) {
        BufferedReader br = null;
		StringBuilder sb = new StringBuilder();

		String line;
		try {

			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return sb.toString();
    }
    
}
