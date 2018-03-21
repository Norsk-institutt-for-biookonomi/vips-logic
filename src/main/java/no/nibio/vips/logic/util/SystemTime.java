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

package no.nibio.vips.logic.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides a single entry point to system time. This is to be able to
 * manipulate the system to run at earlier dates. Define the offset
 * in months in standalone.xml (or other config place) with the parameter
 * no.nibio.vips.logic.SYSTEM_TIME_OFFSET_MONTHS
 * @copyright 2013 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public final class SystemTime {

    private static Integer SYSTEM_TIME_OFFSET_MONTHS;
    private final static Calendar cal = Calendar.getInstance();
    
    public static Date getSystemTime()
    {
        if(SYSTEM_TIME_OFFSET_MONTHS == null)
        {
            SYSTEM_TIME_OFFSET_MONTHS = getOffsetMonths();
        }
        cal.setTime(new Date());
        cal.add(Calendar.MONTH, SYSTEM_TIME_OFFSET_MONTHS);
                    
        return getExactDate() != null ? getExactDate(): cal.getTime();
    }
    
    /**
     * Simple check to see if the system time is offset from now. The threshold
     * is 5 seconds
     * @return 
     */
    public static boolean isSystemTimeOffsetFromNow(){
        Date systemTime = SystemTime.getSystemTime();
        Date now = new Date();
        return Math.abs(now.getTime() - systemTime.getTime()) > 5000;
    }
    
    private static Integer getOffsetMonths()
    {
        return System.getProperty("no.nibio.vips.logic.SYSTEM_TIME_OFFSET_MONTHS") != null ? 
                Integer.valueOf(System.getProperty("no.nibio.vips.logic.SYSTEM_TIME_OFFSET_MONTHS"))
                : 0;
    }
    
    /**
     * To override the month offset, enter an element like this in the system config
     * (standalone.xml on WildFly):
     * <property name="no.nibio.vips.logic.SYSTEM_TIME_EXACT_DATE" value="2017-09-30"/>
     * @return 
     */
    private static Date getExactDate()
    {
        String dateStr = System.getProperty("no.nibio.vips.logic.SYSTEM_TIME_EXACT_DATE");
        if(dateStr != null)
        {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            try {
                return format.parse(dateStr);
            } catch (ParseException ex) {
                Logger.getLogger(SystemTime.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }
        else
        {
            return null;
        }
    }
    
}
