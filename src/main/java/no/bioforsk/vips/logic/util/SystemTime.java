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

package no.bioforsk.vips.logic.util;

import java.util.Calendar;
import java.util.Date;

/**
 * Provides a single entry point to system time. This is to be able to
 * manipulate the system to run at earlier dates. Define the offset
 * in months in standalone.xml (or other config place) with the parameter
 * no.bioforsk.vips.logic.SYSTEM_TIME_OFFSET_MONTHS
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
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
        return cal.getTime();
    }
    
    private static Integer getOffsetMonths()
    {
        return Integer.valueOf(System.getProperty("no.bioforsk.vips.logic.SYSTEM_TIME_OFFSET_MONTHS"));
    }
    
}
