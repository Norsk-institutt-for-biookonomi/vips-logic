 
package no.nibio.vips.logic.scheduling.model.preprocessor;


import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import no.nibio.vips.entity.ModelConfiguration;
import no.nibio.vips.entity.WeatherObservation;
import no.nibio.vips.logic.entity.ForecastConfiguration;
import no.nibio.vips.logic.entity.PointOfInterestWeatherStation;
import no.nibio.vips.logic.scheduling.model.ModelRunPreprocessor;
import no.nibio.vips.logic.scheduling.model.PreprocessorException;
import no.nibio.vips.logic.util.Plant;
import no.nibio.vips.logic.util.Plantation;
import no.nibio.vips.logic.util.SessionControllerGetter;
import no.nibio.vips.logic.util.SystemTime;
import no.nibio.vips.model.ConfigValidationException;
import no.nibio.vips.util.WeatherElements;
import no.nibio.vips.util.WeatherObservationListException;
import no.nibio.vips.util.WeatherUtil;
import no.nibio.vips.util.weather.WeatherDataSourceException;
import no.nibio.vips.util.weather.WeatherDataSourceUtil;

/**
 *
 * @author wildfly
 */


public class PhenologyModelPreprocessor extends ModelRunPreprocessor{
     public final           Logger      LOGGER                  =   Logger.getLogger(this.getClass().getName());
    private final           boolean     DEBUG                   =   false;
    private final   static  String      MODEL_ID                =   "PHENOLOGYM";
    private final   static  String      SPECIES_NAME            =   "speciesName";
    private final   static  String      TYPE_NAME               =   "typeName";
    private final   static  String      SOWING_DATE             =   "sowingDate";
    
    private final   static  String      PLANTATION_DATA         =   "plantationData";
    private final   static  String      FILE_PLANTATION         =   "/dataset/plantationData.json";
    
    private final   static  String      NODE_SPECIES            =   "species";
    private final   static  String      NODE_SPECIES_LATIN_NAME =   "speciesLatinName";
    private final   static  String      NODE_BASE_TEMP          =   "baseTemp";
    private final   static  String      NODE_TYPE_NAMES         =   "typeNames";
    private final   static  String      NODE_TYPE_NAME          =   "typeName";
    private final   static  String      NODE_PHASE_INFO         =   "phaseInfo";
    private final   static  String      NODE_PHASE_NAME         =   "phaseName";
    private final   static  String      NODE_HEAT_REQ           =   "heatReq";
    
    @Override
    public ModelConfiguration getModelConfiguration(ForecastConfiguration configuration) throws PreprocessorException {
        String      PHENOLOGY_SPECIES_NAME                  = SessionControllerGetter.getForecastBean().getDeCamelizedFieldName(MODEL_ID, SPECIES_NAME);  
        String      PHENOLOGY_TYPE_NAME                     = SessionControllerGetter.getForecastBean().getDeCamelizedFieldName(MODEL_ID, TYPE_NAME);
        String      PHENOLOGY_SOWING_DATE                   = SessionControllerGetter.getForecastBean().getDeCamelizedFieldName(MODEL_ID, SOWING_DATE);
        
        String      paramPhenologySpeciesName               = null;          
        String      paramPhenologyTypeName                  = null;          
        Date        paramSowingDate                         = null;
        
        
        String                         paramConfigValue     =   null;
        
        ModelConfiguration              retVal              =   new ModelConfiguration();
        PointOfInterestWeatherStation   weatherStation      =   (PointOfInterestWeatherStation)  configuration.getWeatherStationPointOfInterestId();
        WeatherDataSourceUtil           wdsUtil             =   new WeatherDataSourceUtil();
        WeatherUtil                     wUtil               =   new WeatherUtil();
        TimeZone                        timeZone            =   TimeZone.getTimeZone(weatherStation.getTimeZone());
        SimpleDateFormat                format              =   new SimpleDateFormat("yyyy-MM-dd");
        format.setTimeZone(timeZone);
        // Getting date three days after "today"
        Calendar cal = Calendar.getInstance(timeZone);
        cal.setTime(SystemTime.getSystemTime());
        cal.add(Calendar.DATE, 3);
        Date endDate = wUtil.normalizeToExactDate(configuration.getDateEnd(), timeZone);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DATE, 1);
        
        Plant         plant  = new Plant();
        
        try
        {
        paramConfigValue    = configuration.getForecastModelConfigurationValue(PHENOLOGY_SOWING_DATE);
        paramSowingDate     = format.parse(paramConfigValue);
        }
        catch(ParseException ex)
        {
            throw new PreprocessorException("Could not parse date for Sowing Date for "+MODEL_ID+". Given string was: "+ paramConfigValue);
        }
        
        Date                            startDate       =   wUtil.normalizeToExactDate(
                                                                        paramSowingDate
                                                                    ,   timeZone
                                                            );
        
        List<WeatherObservation>        observations    =   null;
        
        
        
        try {
            observations    = wdsUtil.getWeatherObservations(
                                                              weatherStation
                                                            , WeatherObservation.LOG_INTERVAL_ID_1H
                                                            , new String[]{
                                                                            WeatherElements.TEMPERATURE_MEAN,
                                                                            WeatherElements.PRECIPITATION,
                                                                            WeatherElements.LEAF_WETNESS_DURATION,
                                                                            WeatherElements.RELATIVE_HUMIDITY_MEAN
                                                                        }
                                                            , startDate
                                                            , endDate);
        }
        catch(WeatherDataSourceException ex)
        {
            throw new PreprocessorException(ex.getMessage());
        }
        
        paramPhenologySpeciesName   = configuration.getForecastModelConfigurationValue(PHENOLOGY_SPECIES_NAME);
        paramPhenologyTypeName      = configuration.getForecastModelConfigurationValue(PHENOLOGY_TYPE_NAME);
    
 
        Collections.sort(observations);

        try {
            List<WeatherObservation>  calcObs  = wdsUtil.getWeatherObservations(
                                                                                    weatherStation
                                                                                ,   WeatherObservation.LOG_INTERVAL_ID_1H
                                                                                ,   new String[] {
                                                                                                WeatherElements.GLOBAL_RADIATION,
                                                                                                WeatherElements.WIND_SPEED_2M
                                                                                                }
                                                                                , startDate
                                                                                , endDate
                                                                                );
            observations.addAll(calcObs);
        }catch (WeatherDataSourceException ex)
        {
            // TODO nothing reported
        }
        
       if(DEBUG)
        {
            LOGGER.log(Level.CONFIG, "Finished getting weather data at  "+new Date().toString());
        }
        try {
            observations = validateAndSanitizeObservations(observations, startDate);
        } catch (ConfigValidationException | WeatherObservationListException ex) {
            //ex.printStackTrace();
            throw new PreprocessorException(ex.getMessage());
        } 
        if(DEBUG)
        {        
            LOGGER.log(Level.CONFIG, "Observations=" + observations.toString());
        }
    
        Plant   plantDBData   =   getConfigurationPlantation(FILE_PLANTATION,paramPhenologySpeciesName,paramPhenologyTypeName);

        if(!plantDBData.getPlantations().isEmpty())
        {
            plant = plantDBData;
            plant.setStartDate(paramSowingDate);
        }
        
        retVal.setModelId(this.getModelId());
        retVal.setConfigParameter("timeZone", timeZone.getID());
        retVal.setConfigParameter("observations", observations);
        retVal.setConfigParameter(PLANTATION_DATA, plant);
        
        return retVal;
    }

    
    
    
    @Override
    public String getModelId() {
        return MODEL_ID;
    }
    
    
       private List<WeatherObservation> validateAndSanitizeObservations(List<WeatherObservation> observations, Date firstTimeStamp) throws ConfigValidationException, WeatherObservationListException 
       {
       
         if(DEBUG)
        {
            LOGGER.log(Level.CONFIG, "validateAndSanitizeObservations");
        }
        
        WeatherUtil wUtil = new WeatherUtil();
        
        // First we remove all duplicates
        observations = wUtil.removeDuplicateWeatherObservations(observations, null);
        
        // Fix weather data
        List<WeatherObservation> fixedObservations = wUtil.fixHourlyValuesForParameters(
                observations, 
                new HashSet(Arrays.asList("TM","RR")), 
                firstTimeStamp, 
                null
        );
        
        
        // Now we need to validate and possibly try to fix the weather data
        List<WeatherObservation> TM = new ArrayList<>();
        List<WeatherObservation> RR = new ArrayList<>();
        
        for(WeatherObservation o:fixedObservations)
        {
            switch(o.getElementMeasurementTypeId())
            {
                case WeatherElements.TEMPERATURE_MEAN:
                    TM.add(o); 
                    break;
                case WeatherElements.PRECIPITATION:
                    RR.add(o); 
                    break;
                    
            }
        }
        
        List<WeatherObservation> BT = new ArrayList<>();
        List<WeatherObservation> UM = new ArrayList<>();
        List<WeatherObservation> Q0 = new ArrayList<>();
        List<WeatherObservation> FM2 = new ArrayList<>();
        for(WeatherObservation o:observations)
        {
            switch(o.getElementMeasurementTypeId())
            {
                case WeatherElements.LEAF_WETNESS_DURATION:
                    BT.add(o); 
                    break;
                case WeatherElements.RELATIVE_HUMIDITY_MEAN:
                    UM.add(o); 
                    break;
                case WeatherElements.GLOBAL_RADIATION:
                    Q0.add(o);
                    break;
                case WeatherElements.WIND_SPEED_2M:
                    FM2.add(o);
                    break;
                default:
                    // Let it pass in silence 
                    break;
            }
        }
        
        // Problems with weather observations
        
        // Holes in series
        if(DEBUG)
        {
            LOGGER.log(Level.CONFIG, "checkForAndFixHourlyTimeSeriesHoles");
        }

        
        
        // Unequal length of lists
        if  (        
                RR.size() != TM.size()
                || BT.size() != TM.size()
                || RR.size() != TM.size()
            )
        {
            UM = wUtil.fixHourlyValuesForParameters(
                    UM, 
                    new HashSet(Arrays.asList("UM")), 
                    firstTimeStamp, 
                    null
            );
            
            
            
            // Fallback if missing leaf wetness: If we have relative humidity. leaf wetness may be calculated
        /*
            if((BT.size() != TM.size()) && (UM.size() == TM.size()))
            {
                BT = wUtil.calculateLeafWetnessHourSeriesBestEffort(BT,TM, RR, Q0, FM2, UM);
                
                if(BT.size() != TM.size())
                {
                   throw new ConfigValidationException("Missing leaf wetness data. Also, attempting to calculate leaf wetness from other weather parameters failed.");
                }
            }
            else
            {
                LOGGER.log(Level.WARNING, "TM starts " + TM.get(0).getTimeMeasured() + ", ends " + TM.get(TM.size()-1).getTimeMeasured());
                throw new ConfigValidationException("Incorrect number of weather data. TM.size() = " + TM.size() + ", BT.size()=" + BT.size() +  ", RR.size()=" + RR.size() + ", UM.size()=" + UM.size() );
            }
            */
        }
        List<WeatherObservation> retVal = new ArrayList<>();
        retVal.addAll(TM);
       // retVal.addAll(RR);
       // retVal.addAll(BT);
       Collections.sort(retVal);
        return retVal;
    }
    
    private Plant getConfigurationPlantation(String fileName,String strParamSpeciesName, String strParamTypeName)
    {
        Plant   plant   = new Plant();
                
        
        List<Plantation> plantations    = new ArrayList<Plantation>();
        
        try {
            BufferedInputStream inputStream = new BufferedInputStream(this.getClass().getResourceAsStream(fileName));
            JsonFactory f = new MappingJsonFactory();
            JsonParser jp = f.createParser(inputStream);
            
            JsonNode all = jp.readValueAsTree();
            
            JsonNode nodeAllSpecies = all.path(NODE_SPECIES);
            
            if(nodeAllSpecies.isArray())
            {
                for(JsonNode nodeSpecies : nodeAllSpecies)
                {
                    if(nodeSpecies.get(NODE_SPECIES_LATIN_NAME).asText().trim().equalsIgnoreCase(strParamSpeciesName))
                    {
                        plant.setSpeciesLatinName(nodeSpecies.get(NODE_SPECIES_LATIN_NAME).asText());
                        if(null != nodeSpecies.get(NODE_BASE_TEMP).asText())
                        {
                            plant.setBaseTemp(nodeSpecies.get(NODE_BASE_TEMP).asDouble());
                        }
                       JsonNode nodeTypeNames = nodeSpecies.path(NODE_TYPE_NAMES); 
                       
                        if(nodeTypeNames.isArray())
                        {
                             for(JsonNode nodeTypeName : nodeTypeNames)
                             {
                                 if(nodeTypeName.get(NODE_TYPE_NAME).asText().trim().equalsIgnoreCase(strParamTypeName))
                                 {
                                    plant.setTypeName(strParamTypeName);
                                    if(null != nodeTypeName.get(NODE_BASE_TEMP).asText())
                                    {
                                        plant.setBaseTemp(nodeTypeName.get(NODE_BASE_TEMP).asDouble());
                                    }

                                    JsonNode nodeAllPhaseInfo = nodeTypeName.path(NODE_PHASE_INFO);
                                    if(nodeAllPhaseInfo.isArray())
                                    {
                                         for(JsonNode nodePhaseInfo : nodeAllPhaseInfo)
                                         {
                                            Plantation plantation  = new Plantation();
                                            plantation.setTypeName(nodeTypeName.get(NODE_TYPE_NAME).asText());


                                             plantation.setPhaseName(nodePhaseInfo.get(NODE_PHASE_NAME).asText());
                                             plantation.setHeatReq(nodePhaseInfo.get(NODE_HEAT_REQ).asDouble());

                                             plantations.add(plantation);
                                         }

                                    }
                                 } 

                             }

                        }
                       
                       
                    }
                }

            }
     
            plant.setPlantations(plantations);
            
            
        } catch (IOException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
        
        return plant;
    }
       
}
