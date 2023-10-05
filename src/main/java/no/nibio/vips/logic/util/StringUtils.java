/*
 * Copyright (c) 2014 NIBIO <http://www.nibio.no/>. 
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

package no.nibio.vips.logic.util;

import java.util.Random;

/**
 * @copyright 2013 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class StringUtils {
    
    /**
     * Check whether the given String contains actual text. More specifically, returns true if the string not null, its length is greater than 0, and it contains at least one non-whitespace character.
     * <pre>
     *   StringUtils.containsText(null) = false
     *   StringUtils.containsText("") = false
     *   StringUtils.containsText(" ") = false
     *   StringUtils.containsText("12345") = true
     *   StringUtils.containsText(" 12345 ") = true
     * </pre>
     * @param str the String to check (may be null) 
     * @return true if the String is not null, its length is greater than 0, and it does not contain whitespace only
     */
    public static boolean containsText(String str){
        if(str == null) return false;
        return !str.trim().isEmpty();
    }
    
    /**
     * Generates a string random alphanumeric characters
     * @param length The length of the string returnd
     * @return 
     */
    public static synchronized String generateRandomAlphanumericString(Integer length)
    {
        String allowedChars="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTYVWXYZ0123456789";
        StringBuilder buffer = new StringBuilder(length);
        Random r = new Random();
        for(int i=0;i<length;i++)
        {
            int number = r.nextInt(allowedChars.length());
            buffer.append(allowedChars.charAt(number));
        }
        return buffer.toString();
    }

}
