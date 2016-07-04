/*
 * Copyright (c) 2014 NIBIO <http://www.nibio.no/>. 
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

package no.nibio.web.forms;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import no.nibio.vips.logic.util.Globals;
import org.apache.commons.fileupload.FileItem;

/**
 * @copyright 2014 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class FormUtil {

    //  
    /**
     * Creating a parameterMap from Apache Fileupload
     * @param items
     * @param items
     * @return
     * @throws UnsupportedEncodingException 
     */
    public static Map<String, String[]> getParameterMap(List<FileItem> items, String encoding) throws UnsupportedEncodingException
    {
        Map<String, String[]> retVal = new HashMap<>();
        for(FileItem item:items)
        {
            if(item.isFormField())
            {
                // If we have multiple type fields, we have to add to existing key
                if(retVal.containsKey(item.getFieldName()))
                {
                    List<String> oldValue = new ArrayList(Arrays.asList(retVal.get(item.getFieldName())));
                    oldValue.add(item.getString(encoding));
                    String[] newValue = new String[oldValue.size()];
                    retVal.put(item.getFieldName(),oldValue.toArray(newValue));
                }
                else
                {
                    String[] value = {item.getString(encoding)};
                    retVal.put(item.getFieldName(), value);
                }
            }
        }
        return retVal;
    }
    
    /**
     * 
     * @param parameterValues
     * @return 
     */
    public static List<Integer> getIdsFromMultipleSelect(String[] parameterValues)
    {
        List<Integer> retVal = new ArrayList<>();
        if(parameterValues != null)
        {
            for(String parameterValue:parameterValues)
            {
                retVal.add(Integer.valueOf(parameterValue));
            }
        }
        
        return retVal;
    }
    
    public static Date getDate(String dateStr) throws ParseException
    {
        SimpleDateFormat format = new SimpleDateFormat(Globals.defaultDateFormat);
        return format.parse(dateStr);
    }
    
    public static boolean whitespaceFoundInString(String theString)
    {
        Pattern pattern = Pattern.compile("\\s");
        Matcher matcher = pattern.matcher(theString);
        return matcher.find();
    }
}
