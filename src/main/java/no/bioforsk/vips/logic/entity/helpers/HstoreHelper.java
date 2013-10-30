package no.bioforsk.vips.logic.entity.helpers;



import java.util.HashMap;
import java.util.Map;
import no.bioforsk.vips.logic.util.StringUtils;

/** courtesy of: http://backtothefront.net/2011/storing-sets-keyvalue-pairs-single-db-column-hibernate-postgresql-hstore-type/
 * Maps between String and Map<String, String> object
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
 
public class HstoreHelper {

    private static final String K_V_SEPARATOR = "=>";

    public static String toString(Map<String, String> m) {
        if (m.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int n = m.size();
        for (String key : m.keySet()) {
            sb.append("\"").append(key).append("\"" + K_V_SEPARATOR + "\"").append(m.get(key)).append("\"");
            if (n > 1) {
                sb.append(", ");
                n--;
            }
        }
        return sb.toString();
    }

    public static Map<String, String> toMap(String s) {
        Map<String, String> m = new HashMap<>();
        if (!StringUtils.containsText(s)) {
            return m;
        }
        String[] tokens = s.split(", ");
        for (String token : tokens) {
            String[] kv = token.split(K_V_SEPARATOR);
            String k = kv[0];
            k = k.trim().substring(1, k.length() - 1);
            String v = kv[1];
            v = v.trim().substring(1, v.length() - 1);
            m.put(k, v);
        }
        return m;
    }
}