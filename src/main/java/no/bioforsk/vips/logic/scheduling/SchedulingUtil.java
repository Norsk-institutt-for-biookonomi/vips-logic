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

package no.bioforsk.vips.logic.scheduling;

/**
 * @copyright 2014 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public class SchedulingUtil {
    public static int MESSAGE_STATUS_SUCCESS = 1;
    public static int MESSAGE_STATUS_WARNING = 2;
    public static int MESSAGE_STATUS_DANGER = 3;
    
    /**
     * Generating bootstrap compatible HTML for status messages in scheduling system
     * (primarily displayed through the template taskHistoryDetails.ftl)
     * @param heading
     * @param body
     * @param status
     * @return 
     */
    public static String createSchedulingMessageHTML(String heading, String body, int status)
    {
        String panelType = 
                status == SchedulingUtil.MESSAGE_STATUS_SUCCESS ? "success"
                : status == SchedulingUtil.MESSAGE_STATUS_DANGER ? "danger"
                : status == SchedulingUtil.MESSAGE_STATUS_SUCCESS ? "warning"
                : "primary";
        
        return "<div class=\"panel panel-" + panelType + "\">" +
                "<div class=\"panel-heading\">" + heading + "</div>" +
                "<div class=\"panel-body schedulingMessageBox\">" +
                "<pre>" + body + "</pre>" +
                "</div>\n" +
                "</div>";
    }

}
