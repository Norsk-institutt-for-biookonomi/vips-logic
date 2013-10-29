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
