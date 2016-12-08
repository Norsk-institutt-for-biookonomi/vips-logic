<%-- 
    Document   : testloginsuccess
    Created on : Nov 25, 2013, 4:04:27 PM
    Author     : treinar
--%>
<%@page import="no.nibio.vips.logic.authenticate.OpenIdAuthAttributes"%>
<%@page import="no.nibio.vips.logic.authenticate.OpenIdRelyingParty"%>
<%@page import="org.openid4java.discovery.Identifier"%>
<%@page import="org.openid4java.consumer.ConsumerManager"%>
<%@page import="org.openid4java.consumer.VerificationResult"%>
<%@page import="org.openid4java.discovery.DiscoveryInformation"%>
<%@page import="org.openid4java.message.ParameterList"%>
<%
    OpenIdRelyingParty relP = new OpenIdRelyingParty();
    OpenIdAuthAttributes attributes = relP.verifyResponse(request);
%>


<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <% if(attributes != null) { %>
        <h1>Login success!</h1>
        <ul>
            <li><%= attributes.getOpenId()%></li>
            <li><%= attributes.getEmail() %></li>
        </ul>
        <% } else { %>
        <h1>Login failure</h1>
        <% } %>
    </body>
</html>
