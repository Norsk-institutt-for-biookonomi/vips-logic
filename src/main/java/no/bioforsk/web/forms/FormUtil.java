/*
 * Copyright (c) 2014 Bioforsk <http://www.bioforsk.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with VIPSLogic.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package no.bioforsk.web.forms;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.fileupload.FileItem;

/**
 * @copyright 2014 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
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
}
