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

package no.nibio.vips.util.weather;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import no.nibio.vips.entity.WeatherObservation;
import no.nibio.vips.logic.util.SystemTime;
import no.nibio.vips.util.WeatherElements;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;

/**
 * 
 * Gets data from the Pessl METOS fieldclimate API.
 * Read about the API here: http://www.fieldclimate.com/api/intro.html
 * 
 * @copyright 2017 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class MetosAPIDataParser {
    public final static String METOS_API_URL_TEMPLATE = "http://www.fieldclimate.com/api/CIDIStationData3/GetFromDate?{0}&group_code=1&dt_from={1}&row_count={2}";

    // Mappping VIPS parameters and Metos sensors
    // Fieldclimate codes should be in decreasing priority
    // TODO; Separate parameter: Soil temperature. Search for name "Soil temperature", higher channel number = deeper sensor
    private final ParamInfo[] PARAM_MAP = {
        new ParamInfo(WeatherElements.PRECIPITATION,        new Integer[] {6},      "sum"),
        new ParamInfo(WeatherElements.LEAF_WETNESS,         new Integer[] {4},      "time"),
        new ParamInfo(WeatherElements.GLOBAL_RADIATION,     new Integer[] {600},    "avg"),
        new ParamInfo(WeatherElements.WIND_SPEED_2M,        new Integer[] {5},      "avg"),
        new ParamInfo(WeatherElements.TEMPERATURE_MEAN,     new Integer[] {16385},  "avg"),
        new ParamInfo(WeatherElements.TEMPERATURE_MAXIMUM,  new Integer[] {16385},  "max"),
        new ParamInfo(WeatherElements.TEMPERATURE_MINIMUM,  new Integer[] {16385},  "min")
    };
    
    public List<WeatherObservation> getWeatherObservations_old(String stationId, TimeZone timeZone, Date startDate) throws ParseWeatherDataException
    {
        List<WeatherObservation> retVal = new ArrayList<>();
        // TODO: Finn ut stasjonens egen timezone
        // http://www.fieldclimate.com/api/CIDIStationConfig2/Get?user_name=XX&user_passw=XX&station_name=XXXXXXXX
        // Antar at respons "f_timezone":"120" representerer minuttoffsett fra GMT (så GMT+2 i dette eksempelet)
        // Denne settes så i begge SimpleDateFormat-klassene
        SimpleDateFormat URLDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        SimpleDateFormat ResponseDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        // Deduce how many rows of data need be returned. Default: From startDate until now
        //Date now = SystemTime.getSystemTime();
        // TODO: Fix the TimeZone here!!!
        LocalDateTime now = LocalDateTime.ofInstant(SystemTime.getSystemTime().toInstant(), ZoneId.systemDefault());
        LocalDateTime then = LocalDateTime.ofInstant(startDate.toInstant(), ZoneId.systemDefault());
        Long hoursBetween = ChronoUnit.HOURS.between(then, now);
        try
        {
            URL metosAPIURL = new URL(MessageFormat.format(MetosAPIDataParser.METOS_API_URL_TEMPLATE, stationId, URLDateFormat.format(startDate), hoursBetween));
            System.out.println(metosAPIURL.toString());
        }
        catch(MalformedURLException ex)
        {
            throw new ParseWeatherDataException(ex.getMessage());
        }
        
        
        return retVal;
    }
    
    /**
     * 
     * @return
     * @throws UnsupportedEncodingException
     * @throws IOException 
     */
    public String getAccessCode() throws UnsupportedEncodingException, IOException
    {
        String accessCode = null;
        
        HttpPost httppost = new HttpPost("https://oauth.fieldclimate.com/authorize?response_type=code&client_id=MetosDemo&state=xyz");
        CookieStore cookieStore = new BasicCookieStore();
        BasicClientCookie cookie = new BasicClientCookie("METOS","metos_srv_5");
        cookie.setPath("/");
        cookie.setDomain("oauth.fieldclimate.com");
        cookieStore.addCookie(cookie);
        CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
        // Request parameters and other properties.
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("username", System.getProperty("no.nibio.vips.logic.weather.FIELDCLIMATE_API_USERNAME")));
        params.add(new BasicNameValuePair("password", System.getProperty("no.nibio.vips.logic.weather.FIELDCLIMATE_API_PASSWORD")));
        params.add(new BasicNameValuePair("authorization", "true"));
        httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        
        //Execute and get the response.
        ResponseHandler<String> responseHandler=new BasicResponseHandler();
        HttpResponse response = httpclient.execute(httppost);
        /*for(Header h:response.getAllHeaders())
        {
            System.out.println(h.getName() + ": " + h.getValue());
        }*/
        Header location = response.getFirstHeader("Location");
        if(location != null && location.getValue().split("code=").length > 1)
        {
            try
            {
                Optional<NameValuePair> code = URLEncodedUtils.parse(new URI(location.getValue()), "UTF-8").stream()
                        .filter(key -> key.getName().equals("code"))
                        .findFirst();
                accessCode = code.isPresent() ? code.get().getValue(): null;
            }
            catch(URISyntaxException ex)
            {
                ex.printStackTrace();
            }
        }
        //System.out.println("accessCode=" + accessCode);
        return accessCode;
    }
    
    public String getToken() throws IOException, AuthenticationException
    {
        
        String accessCode = this.getAccessCode();
        
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost("https://oauth.fieldclimate.com/token");
        
        // Setting up HTTP BASIC authentication
        CredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("MetosDemo", "aa8f4b62b72986bac7c84be78836c2c6");
        
        HttpHost targetHost = new HttpHost("oauth.fieldclimate.com", 80, "https");
        AuthCache authCache = new BasicAuthCache();
        authCache.put(targetHost, new BasicScheme());
        BasicScheme basicScheme = new BasicScheme();
        final HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(provider);
        context.setAuthCache(authCache);
        httppost.addHeader(basicScheme.authenticate(credentials, httppost, context));
                
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("grant_type","authorization_code"));
        params.add(new BasicNameValuePair("code",accessCode));
        httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        
        
        HttpResponse response = httpclient.execute(httppost);
        BufferedReader s = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String line = s.readLine();
        ObjectMapper m = new ObjectMapper();
        JsonNode jn = m.readTree(line);
        return jn.findValue("access_token").asText();
    }
    
    public List<WeatherObservation> getParsedObservations(String jsonTxt, TimeZone timeZone) throws IOException, ParseException
    {
        
        ObjectMapper oMapper = new ObjectMapper();
        JsonNode jNode = oMapper.readTree(jsonTxt);
        List<WeatherObservation> retVal = new ArrayList<>();
        
        JsonNode dates = jNode.get("dates");
        JsonNode data = jNode.get("data");
        SimpleDateFormat dFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dFormat.setTimeZone(timeZone);
        for(ParamInfo paramInfo:this.PARAM_MAP)
        {
            boolean foundParam = false;
            for(Integer code:paramInfo.preferredCodes)
            {
                for(JsonNode aData:data)
                {
                    if(aData.get("code").asInt() == code)
                    {
                        for(int i=0;i< dates.size();i++)
                        {
                            WeatherObservation obs = new WeatherObservation();
                            obs.setTimeMeasured(dFormat.parse(dates.get(i).asText()));
                            obs.setElementMeasurementTypeId(paramInfo.VIPSCode);
                            obs.setValue(aData.get("aggr").get(paramInfo.aggregationType).get(i).asDouble());
                            obs.setLogIntervalId(WeatherObservation.LOG_INTERVAL_ID_1H);
                            retVal.add(obs);
                            //System.out.println(obs.toString());
                        }
                        foundParam = true;
                    }
                    if(foundParam)
                    {
                        break;
                    }
                }
                if(foundParam)
                {
                    break;
                }
            }
        }
        
        return retVal;
    }
    
    
    public List<WeatherObservation> getWeatherObservations(String stationId, TimeZone timeZone, Date startDate) throws ParseWeatherDataException
    {
        try
        {
            String accessToken = this.getToken();
            HttpGet httpget = new HttpGet("https://api.fieldclimate.com/v1/data/optimized/000024A0/hourly/from/" + (startDate.getTime() / 1000)); 

            httpget.addHeader("Accept", "application/json");
            httpget.addHeader("Authorization", "Bearer " + accessToken);
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpResponse response = httpclient.execute(httpget);
            BufferedReader s = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String all = "";
            String line;
            while((line = s.readLine()) != null)
            {
                all += line;
                //System.out.println("Linje");
            }
            
            return this.getParsedObservations(all, timeZone); // TODO TimeZone
        }
        catch(IOException | ParseException | AuthenticationException ex)
        {
            throw new ParseWeatherDataException(ex.getMessage());
        }
    }
    
    /**
     * Data structure for easy parameter mapping
     */
    private class ParamInfo {

        public final String VIPSCode;
        public final Integer[] preferredCodes;
        public final String aggregationType;
        public ParamInfo(String VIPSCode, Integer[] preferredCodes, String aggregationType)
        {
            this.VIPSCode = VIPSCode;
            this.preferredCodes = preferredCodes;
            this.aggregationType = aggregationType;
        }

    }
    
    /**
     * For hacking/experimentation
     * @throws IOException
     * @throws AuthenticationException 
     */
    public void testAPI() throws IOException, AuthenticationException
    {
        
        String accessToken = this.getToken();
        //HttpGet httpget = new HttpGet("https://api.fieldclimate.com/station/000024A0");
        //HttpGet httpget = new HttpGet("https://api.fieldclimate.com/v1/data/optimized/000024A0/hourly/last/5d");
        Calendar cal = Calendar.getInstance();
        cal.set(2017, Calendar.AUGUST, 1, 0, 0, 0);
        HttpGet httpget = new HttpGet("https://api.fieldclimate.com/v1/data/optimized/000024A0/hourly/from/" + (cal.getTime().getTime()/1000));
        //HttpGet httpget = new HttpGet("https://api.fieldclimate.com/v1/system/group/sensors");
        httpget.addHeader("Accept", "application/json");
        httpget.addHeader("Authorization", "Bearer " + accessToken);
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpResponse response = httpclient.execute(httpget);
        BufferedReader s = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String all = "";
        String line = null;
        while((line = s.readLine()) != null)
        {
            all += line;
            //System.out.println("Linje");
        }
        
        
        
        ObjectMapper oMapper = new ObjectMapper();
        JsonNode jNode = oMapper.readTree(all);
        //Iterator keys = jNode.fieldNames();
        /*while(keys.hasNext())
        {
            System.out.println(keys.next());
        }
        
        JsonNode dates = jNode.get("dates");
        JsonNode data = jNode.get("data");
        for(int i=0;i< dates.size();i++)
        {
            //System.out.println(dates.get(i).asText() + ": " + data.get("1_X_X_600").get("name").asText() + " = " + data.get("1_X_X_600").get("aggr").get("avg").get(i).asDouble());
        }*/
        try {
            this.getParsedObservations(all, TimeZone.getTimeZone("Europe/Vilnius"));
        } catch (ParseException ex) {
            Logger.getLogger(MetosAPIDataParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println(all);
    }
}