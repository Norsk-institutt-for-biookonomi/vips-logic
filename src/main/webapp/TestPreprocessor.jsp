<%-- 
    Document   : TestPreprocessor
    Created on : Oct 29, 2013, 1:48:14 PM
    Author     : treinar
--%>
<%@page import="no.nibio.vips.entity.Result"%>
<%@page import="java.util.List"%>
<%@page import="no.nibio.vips.logic.util.SystemTime"%>
<%@page import="no.nibio.vips.logic.util.SessionControllerGetter"%>
<%@page import="no.nibio.vips.logic.controller.session.SchedulingBean"%>
<%@page import="no.nibio.vips.logic.entity.ForecastConfiguration"%>
<%@page import="no.nibio.vips.logic.scheduling.model.preprocessor.NaerstadModelPreprocessor"%>
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
