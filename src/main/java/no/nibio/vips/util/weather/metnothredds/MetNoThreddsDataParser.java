/*
 * Copyright (c) 2017 NIBIO <http://www.nibio.no/>. 
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

package no.nibio.vips.util.weather.metnothredds;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import java.util.TimeZone;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import no.nibio.vips.entity.PointWeatherObservationList;
import no.nibio.vips.entity.WeatherObservation;
import no.nibio.vips.gis.SimpleWGS84Coordinate;
import no.nibio.vips.util.WeatherElements;
import no.nibio.vips.util.WeatherUtil;
import org.apache.commons.io.FileUtils;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 * Digging into the Thredds archive of the Norwegian Meteorological Institute,
 * collecting 
 * @copyright 2017 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class MetNoThreddsDataParser {
    
    private final String BASE_URL = "http://thredds.met.no/thredds/";
    private final String NCSS_URL = BASE_URL + "ncss/";
    private final String MEPS25_PATH = "meps25detarchive/";
    private final String MEPS25_FILEBASENAME = "meps_mbr0_extracted_2_5km_";
    private final String NETCDF_EXT = ".nc";
    private final String STANDARD_PARAMS = "?accept=netcdf&temporal=all&&horizStride=1&disableLLSubset=on&timeStride=1";
    private Properties paramInfo;
    private final String TMP_FILE_PATH;
    private final List<String> accumulatedParams = Arrays.asList("Q0", "RR");
    private final SimpleDateFormat pathFormat;
    private final SimpleDateFormat fileTimeStampFormat;
    
    
    private final WeatherUtil weatherUtil;
    
    public MetNoThreddsDataParser()
    {
        paramInfo = new Properties();
        try (InputStream in = this.getClass().getResourceAsStream("parametersources.properties")) {
            paramInfo.load(in);
        }
        catch(IOException ex)
        {
        }
        this.TMP_FILE_PATH = System.getProperty("no.nibio.vips.logic.weather.METNOTHREDDS_TMP_FILE_PATH");
        this.weatherUtil = new WeatherUtil();
        this.pathFormat = new SimpleDateFormat("yyyy/MM/dd/");
        this.pathFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        this.fileTimeStampFormat = new SimpleDateFormat("yyyyMMdd'T'00'Z'");
        this.fileTimeStampFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    
    public List<PointWeatherObservationList> getGridData(Geometry geometry, Date dateFrom, Date dateTo, List<String> VIPSWeatherParameters)
    {
        Set<String> metParamNames = VIPSWeatherParameters.stream().map(
                param -> paramInfo.getProperty(param)
        ).collect(Collectors.toSet());
        
        
        List<Date> archiveDates = getArchiveDates(dateFrom, dateTo);
        Collections.sort(archiveDates);
        //new GeometryFactory().
        Polygon gridBounds =  (Polygon) geometry.getEnvelope();
        System.out.println("geometryType=" + gridBounds.getGeometryType());
        System.out.println("gridBounds=" + gridBounds);
         
        Map<Coordinate, Map<Long, WeatherObservation>> tempRetVal = new HashMap<>();
        archiveDates.stream().forEach(archiveDate -> {
            File tmp = null;
            NetcdfFile ncFile = null;
            try
            {
                URL threddsURL = new URL(NCSS_URL + MEPS25_PATH + 
                        this.pathFormat.format(archiveDate) + 
                        MEPS25_FILEBASENAME + 
                        this.fileTimeStampFormat.format(archiveDate) +
                        NETCDF_EXT + STANDARD_PARAMS +
                        "&north=" + this.getNorth(gridBounds) + "&west=" + this.getWest(gridBounds) + "&east=" + this.getEast(gridBounds) + "&south=" + this.getSouth(gridBounds) +
                        "&var=" + String.join(",", metParamNames) +
                        "&addLatLon=true");
                System.out.println("URL: " + threddsURL.toString());
                Long timestamp = new Date().getTime();
                tmp = new File(this.TMP_FILE_PATH + timestamp + ".nc");
                FileUtils.copyURLToFile(threddsURL, tmp);
                ncFile = NetcdfFile.open(tmp.getAbsolutePath());
                
                Variable time = ncFile.findVariable("time");
                Array times = time.read();
                Date[] timeMeasured = new Date[times.getShape()[0]];
                for(int i=0; i<timeMeasured.length;i++)
                {
                    timeMeasured[i] = new Date(times.getLong(i) * 1000);
                }
                // The points are in a matrix. Get the dims first
                ArrayDouble.D2 longitude = (ArrayDouble.D2) ncFile.findVariable("lon").read();
                ArrayDouble.D2 latitude = (ArrayDouble.D2) ncFile.findVariable("lat").read();
                int[] latLonShape = longitude.getShape(); // Assuming they're identical!
                Coordinate[][] coords = new Coordinate[latLonShape[0]][latLonShape[1]];
                for(int y=0;y<latLonShape[0];y++)
                {
                    for(int x=0;x<latLonShape[1];x++)
                    {
                        double lat = latitude.get(y, x);
                        double lon = longitude.get(y,x);
                        Coordinate c = new Coordinate(lon, lat);
                        coords[y][x] = c;
                        //System.out.println("lat/lon=" + lat + "/" + lon);
                    }
                }
                for(String metParamName:metParamNames)
                {
                    String VIPSParamName = this.findVIPSParamNameFromMetParamName(metParamName);

                    Variable var = ncFile.findVariable(metParamName);
                    // All variables have the dims [time, height, y, x]. All 
                    // variables represent only one height, and can therefore be reduced 
                    // from 4 to three dimensions
                    // Assuming all weather parameters are of type "float"

                    ArrayFloat.D3 varArray = (ArrayFloat.D3) var.read().reduce();
                    // We always skip the two first timestamps, since the model needs 
                    // a couple of hours to spin up
                    for(int i=2;i<timeMeasured.length;i++)
                    {
                        if(timeMeasured[i].compareTo(dateFrom) < 0 || timeMeasured[i].after(dateTo))
                        {
                            continue;
                        }
                        for(int y=0;y<latLonShape[0];y++)
                        {
                            for(int x=0;x<latLonShape[1];x++)
                            {
                                Map<Long, WeatherObservation> obsMapForPoint = tempRetVal.get(coords[y][x]);
                                if(obsMapForPoint == null)
                                {
                                    obsMapForPoint = new HashMap<>();
                                    tempRetVal.put(coords[y][x], obsMapForPoint);
                                }
                                Double value = new Double(varArray.get(i, y, x));
                                // For accumulated parameters, substract previous value in array
                                if(i>0 && this.accumulatedParams.contains(VIPSParamName))
                                {
                                    value = value - new Double(varArray.get(i-1, y, x));
                                }
                                WeatherObservation newObs = new WeatherObservation(
                                        timeMeasured[i],
                                        VIPSParamName,
                                        WeatherObservation.LOG_INTERVAL_ID_1H,
                                        this.getConvertedValue(value, VIPSParamName)
                                );

                                obsMapForPoint.put(newObs.getValiditySignature(), newObs);
                            }   
                        }

                    }
                }
                
            }
            catch(IOException ioe)
            {
                
            }
            finally {
                if(ncFile != null)
                {
                    try
                    {
                        ncFile.close();   
                    }catch(IOException ex) {}
                }
                if(tmp != null)
                {
                    tmp.delete();
                }
            }
        });
        List<PointWeatherObservationList> retVal = new ArrayList<>();
        for(Coordinate c : tempRetVal.keySet())
        {
            Map<Long, WeatherObservation> hashedObsForPoint = tempRetVal.get(c);
            List<WeatherObservation> obsList = new ArrayList(hashedObsForPoint.values());
            Collections.sort(obsList);
            PointWeatherObservationList obsForPoint = new PointWeatherObservationList(c,obsList);
            retVal.add(obsForPoint);
        }
        return retVal;
    }
    
    public List<WeatherObservation> getPointData(Double longitude, Double latitude, Date dateFrom, Date dateTo, List<String> VIPSWeatherParameters)
    {
        Set<String> metParamNames = VIPSWeatherParameters.stream().map(
                param -> paramInfo.getProperty(param)
        ).collect(Collectors.toSet());
        
        List<Date> archiveDates = getArchiveDates(dateFrom, dateTo);
        Collections.sort(archiveDates);
        
        Map<Long, WeatherObservation> retVal = new HashMap<>();
        archiveDates.stream().forEach(archiveDate -> {
            File tmp = null;
            NetcdfFile ncFile = null;
            try
            {
                URL threddsURL = new URL(NCSS_URL + MEPS25_PATH + 
                        this.pathFormat.format(archiveDate) + 
                        MEPS25_FILEBASENAME + 
                        this.fileTimeStampFormat.format(archiveDate) +
                        NETCDF_EXT + STANDARD_PARAMS + "&longitude=" + longitude + "&latitude=" + latitude
                        + "&var=" + String.join(",", metParamNames));
                System.out.println("URL: " + threddsURL.toString());
                Long timestamp = new Date().getTime();
                tmp = new File(this.TMP_FILE_PATH + timestamp + ".nc");
                FileUtils.copyURLToFile(threddsURL, tmp);
                ncFile = NetcdfFile.open(tmp.getAbsolutePath());

                // Set up the array of timestamps
                // Unfortunately, the time value is a Double
                // Dimensions of the time array are [station, timestamp]. Since theres 
                // Only one station, we can reduce the array dims. The reduce() method
                // Simply removes all dims with a length of 1. Handy!
                ArrayDouble.D1 time = (ArrayDouble.D1) ncFile.findVariable("time").read().reduce();
                int[] timeDims = time.getShape();
                // Get the length of the timestamp dimension
                Date[] timeMeasured = new Date[timeDims[0]];
                // Iterate, create parallel array with Dates
                for(int i=0;i<timeDims[0];i++)
                {
                    timeMeasured[i] = new Date(Math.round(time.get(i) * 1000));
                }

                // The position variables have 1 dimension and one element. Simple!
                SimpleWGS84Coordinate coordinate = new SimpleWGS84Coordinate(
                        ((ArrayDouble.D1) ncFile.findVariable("latitude").read()).get(0), 
                        ((ArrayDouble.D1) ncFile.findVariable("longitude").read()).get(0)
                );
                // Can't use stream API here because the ncfile would need to be 
                // final and therefore not closeable in a try-finally statement
                for(String metParamName:metParamNames)
                {
                    String VIPSParamName = this.findVIPSParamNameFromMetParamName(metParamName);

                    Variable var = ncFile.findVariable(metParamName);

                    // The variables have two different dims. The variables that
                    // are height dependent (e.g. temperature) have [station, value, height] 
                    // and the height independent (e.g. precipitation_amount) have 
                    // [station, value]. Either way, all dims except value have a length
                    // of 1 and can be reduced.
                    // Assuming all weather parameters are of type "float"
                    try
                    {
                        ArrayFloat.D1 varArray = (ArrayFloat.D1) var.read().reduce();
                        // We always skip the two first timestamps, since the model needs 
                        // a couple of hours to spin up
                        for(int i=2;i<timeMeasured.length;i++)
                        {
                            if(timeMeasured[i].compareTo(dateFrom) < 0 || timeMeasured[i].after(dateTo))
                            {
                                continue;
                            }
                            Double value = new Double(varArray.get(i));
                            // For accumulated parameters, substract previous value in array
                            if(i>0 && this.accumulatedParams.contains(VIPSParamName))
                            {
                                value = value - new Double(varArray.get(i-1));
                            }
                            WeatherObservation newObs = new WeatherObservation(
                                    timeMeasured[i],
                                    VIPSParamName,
                                    WeatherObservation.LOG_INTERVAL_ID_1H,
                                    this.getConvertedValue(value, VIPSParamName)
                            );
                            retVal.put(newObs.getValiditySignature(), newObs);
                        }
                    }catch(IOException ioe)
                    {
                        ioe.printStackTrace();
                    }
                }
                
                
            }
            catch(IOException ioe)
            {
                
            }
            finally {
                if(ncFile != null)
                {
                    try
                    {
                        ncFile.close();   
                    }catch(IOException ex) {}
                }
                if(tmp != null)
                {
                    tmp.delete();
                }
            }
        });
        return new ArrayList(retVal.values());
    }
    
    public String findVIPSParamNameFromMetParamName(String metParamName)
    {
        // Need to treat the Properties object as a map and search for a key that
        // maps to the given met param name
        Optional<Entry<Object,Object>> opt = this.paramInfo.entrySet().stream().filter(propEntry -> propEntry.getValue().equals(metParamName)).findFirst();
        return opt.isPresent() ? (String) opt.get().getKey() : null;
    }
    
    /**
     * Performing the correct transformations between Met params and VIPS params
     * @param value
     * @param VIPSParamName
     * @return 
     */
    public double getConvertedValue(Double value, String VIPSParamName)
    {
        switch(VIPSParamName){
            case WeatherElements.TEMPERATURE_MEAN:
            case WeatherElements.TEMPERATURE_MAXIMUM:
            case WeatherElements.TEMPERATURE_MINIMUM:
                return this.weatherUtil.getCelciusFromKelvin(value);
            case WeatherElements.RELATIVE_HUMIDITY_MEAN:
            case WeatherElements.RELATIVE_HUMIDITY_INSTANTANEOUS:
                return this.getPercentFromFraction(value);
            case WeatherElements.PRECIPITATION:
                return roundToDecimals(value, 1);
            case WeatherElements.GLOBAL_RADIATION:
                return Math.round(value/3600);
            default:
                return value;
        }
    }
    
    
    
    public double getPercentFromFraction(Double value)
    {
        return value * 100;
    }
    
    public double roundToDecimals(Double value, Integer decimals)
    {
        return Math.round(value * Math.pow(10.0, decimals)) / Math.pow(10.0, decimals);
    }

    /**
     * Provide the list of date paths in which we can find files that contain
     * data for the given period
     * @param dateFrom
     * @param dateTo
     * @return 
     */
    public List<Date> getArchiveDates(Date dateFrom, Date dateTo) {
        Date today = new Date();
        dateTo = dateTo.after(today) ? today : dateTo;
        if(dateFrom.after(dateTo))
        {
            return null;
        }
        TimeZone UTC = TimeZone.getTimeZone("UTC");
        
        // If the date is set at earlier than xx:02:00 UTC
        // Going one day back in time from dateFrom, since we skip the two 
        // first runs of each model
        Calendar cal = Calendar.getInstance(UTC);
        cal.setTime(dateFrom);
        if(cal.get(Calendar.HOUR_OF_DAY) < 2)
        {
            cal.add(Calendar.DATE, -1);
            dateFrom = cal.getTime();
        }
        
        // Adjusting to UTC midnight so that the last date (dateTo) is not
        // overstepped
        dateFrom = this.weatherUtil.normalizeToExactDate(dateFrom, UTC);
        List<Date> retVal = new ArrayList<>();
        while(dateFrom.before(dateTo))
        {
            retVal.add(dateFrom);
            cal.setTime(dateFrom);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.add(Calendar.DATE, 1);
            dateFrom = cal.getTime();
        }
        
        return retVal;
    }
    
    public double getNorth(Polygon envelope)
    {
        return envelope.getCoordinates()[2].y;
    }
    
    public double getSouth(Polygon envelope)
    {
        return envelope.getCoordinates()[0].y;
    }
    
    public double getWest(Polygon envelope)
    {
        return envelope.getCoordinates()[0].x;
    }
    
    public double getEast(Polygon envelope)
    {
        return envelope.getCoordinates()[2].x;
    }
}
