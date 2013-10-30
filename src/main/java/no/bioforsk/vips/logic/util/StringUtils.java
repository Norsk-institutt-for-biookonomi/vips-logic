package no.bioforsk.vips.logic.util;

/**
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
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

}
