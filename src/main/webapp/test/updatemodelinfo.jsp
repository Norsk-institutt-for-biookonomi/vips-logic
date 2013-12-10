<%-- 
    Document   : updatemodelinfo
    Created on : Dec 6, 2013, 5:11:53 PM
    Author     : treinar
--%>
<%@page import="no.bioforsk.vips.logic.controller.session.ForecastBean"%>
<%@page import="no.bioforsk.vips.logic.util.SessionControllerGetter"%>
<%
    ForecastBean fb = SessionControllerGetter.getForecastBean();
    
    fb.updateModelInformation();
%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Update model info</title>
    </head>
    <body>
        <h1>This page calls the methods for updating model info. It also displays the updated model info(?)</h1>
    </body>
</html>
