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

package no.nibio.vips.logic.i18n;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ResourceBundle;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.text.StringEscapeUtils;

/**
 * Provides translation strings in current session language for JavaScript
 * @copyright 2013 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class ResourceBundleJSServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/javascript;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            StringBuilder output = new StringBuilder();
            output.append("var i18Texts={");
            ResourceBundle i18nTexts = SessionLocaleUtil.getI18nBundle(request);
            boolean first = true;
            for(String key:i18nTexts.keySet())
            {
                if(first) first = false;
                else output.append(",");
                output.append("\n\"").append(key).append("\":\"").append(StringEscapeUtils.escapeEcmaScript(i18nTexts.getString(key))).append("\"");
            }
            output.append("\n};\n\n");
            
            // Adding the methods needed to utilize the i18Texts dictionary
            output.append(
                "/**\n" +
                "* Gets a message by key. The message should be translated\n" +
                "* Depends on /js/resourcebundle.js for the dictionary i18Texts\n" +
                "* @param {String} msgKey\n" +
                "* @param {Array} positionalArguments - for string substitution in message\n" +
                "* @returns {String} the message\n" +
                "*/\n" +
                "function getI18nMsg(msgKey,positionalArguments)\n" +
                "{\n" +
                "   if(i18Texts[msgKey] === undefined)\n" +
                "   {\n" +
                "       return getI18nMsg(\"unknownTranslationKey\",[msgKey]);\n" +
                "   }\n" +
                "   return replaceParams(i18Texts[msgKey], positionalArguments);\n" +
                "}\n" +
                " \n" +
                "/**\n" +
                " * Mimic of the MessageFormat.format method in Java.\n" +
                " * Very nicely explained here: http://stackoverflow.com/questions/1353408/messageformat-in-javascript-parameters-in-localized-ui-strings\n" +
                " * @param {String} string\n" +
                " * @param {Array} replacements\n" +
                " * @returns {String}\n" +
                " */\n" +
                "function replaceParams(string, replacements) {\n" +
                
                "    return string.replace(/\\{(\\d+)\\}/g, function() {\n" +
                "        return replacements[arguments[1]];\n" +
                "    });\n" +
                "}\n");
            
            out.print(output.toString());
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
