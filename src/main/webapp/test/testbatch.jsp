<%-- 
    Document   : testscheduling
    Created on : Dec 13, 2013, 1:02:00 PM
    Author     : treinar
--%>
<%@page import="no.bioforsk.vips.logic.scheduling.model.ModelRunPreprocessorFactory"%>
<%@page import="no.bioforsk.vips.logic.scheduling.model.ModelRunPreprocessor"%>
<%@page import="java.util.Calendar"%>
<%@page import="no.bioforsk.vips.logic.entity.ForecastConfiguration"%>
<%@page import="no.bioforsk.vips.logic.util.SystemTime"%>
<%@page import="java.util.Date"%>
<%@page import="no.bioforsk.vips.logic.controller.session.UserBean"%>
<%@page import="no.bioforsk.vips.logic.entity.Organization"%>
<%@page import="java.util.List"%>
<%@page import="no.bioforsk.vips.logic.controller.session.ForecastBean"%>
<%@page import="no.bioforsk.vips.logic.util.SessionControllerGetter"%>
<%
    ForecastBean forecastBean = SessionControllerGetter.getForecastBean();
    UserBean userBean = SessionControllerGetter.getUserBean();
    Organization organization = userBean.getOrganization(2); // Bioforsk plantehelse
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.MONTH, Calendar.JUNE);
    //Date currentTime = SystemTime.getSystemTime();
    Date currentTime = cal.getTime();
    List<ForecastConfiguration> validForecastConfigs = forecastBean.getForecastConfigurationsValidAtTime(organization, currentTime);
    
    
%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Test batch system</title>
    </head>
    <body>
        <h1>Test batch system</h1>
        <p>This page is a shortcut to testing various parts of the batch system</p>
        <h2>Valid forecast configurations for Bioforsk plantehelse at <%=currentTime%></h2>
        <ul>
            <% for(ForecastConfiguration validConfig : validForecastConfigs) { %>
            <li>
                <%= validConfig.getModelId()%>, <%=validConfig.getLocationPointOfInterestId().getName() %>, <%=validConfig.getDateStart()%>-<%= validConfig.getDateEnd()%>
                <% ModelRunPreprocessor preprocessor = ModelRunPreprocessorFactory.getModelRunPreprocessor(validConfig.getModelId()); %>
                <ul>
                    <li>ConfigparameterNames: <%=preprocessor.getModelConfiguration(validConfig).getConfigParameters().keySet().toString() %></li>
                </ul>
            </li>
            <% } %>
        </ul>
    </body>
</html>
