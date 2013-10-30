<%-- 
    Document   : TestPreprocessor
    Created on : Oct 29, 2013, 1:48:14 PM
    Author     : treinar
--%>
<%@page import="no.bioforsk.vips.entity.Result"%>
<%@page import="java.util.List"%>
<%@page import="no.bioforsk.vips.logic.util.SystemTime"%>
<%@page import="no.bioforsk.vips.logic.session.SessionControllerGetter"%>
<%@page import="no.bioforsk.vips.logic.session.SchedulingBean"%>
<%@page import="no.bioforsk.vips.logic.scheduling.model.ForecastConfiguration"%>
<%@page import="no.bioforsk.vips.logic.scheduling.model.AppleScabModelPreprocessor"%>
<%
    SchedulingBean sBean = SessionControllerGetter.getSchedulingBean();
    List<Result> results = sBean.testPreprocessor();
%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>TestPreprocessor</title>
    </head>
    <body>
        <h1>TestPreprocessor</h1>
        <p>System time = <%= SystemTime.getSystemTime()%></p>
        <h2>Result</h2>
        <table>
            <% for(Result result:results) { %>
            <tr><td><%=result.getResultValidTime()%></td><td><%=result.getWarningStatus()%></td></tr>
            <% } %>
        </table>
    </body>
</html>
