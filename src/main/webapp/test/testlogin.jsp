<%-- 
    Document   : testlogin
    Created on : Nov 25, 2013, 12:46:33 PM
    Author     : treinar
--%>
<%@page import="no.nibio.vips.logic.authenticate.OpenIdRelyingParty"%>
<%@page import="org.openid4java.message.AuthRequest"%>
<%@page import="org.openid4java.discovery.Identifier"%>
<%@page import="org.openid4java.discovery.DiscoveryInformation"%>
<%@page import="java.util.List"%>
<%@page import="org.openid4java.consumer.ConsumerManager"%>
<%
    String openIdStr = request.getParameter("openId");
    String identifierName = "";
    String loginType = request.getParameter("loginType");
    if(loginType != null)
    {
        
        OpenIdRelyingParty openId = new OpenIdRelyingParty();
        if(loginType.equals("Google"))
        {
            openIdStr = "https://www.google.com/accounts/o8/id";
        }
        openId.authRequest(openIdStr, request, response);
    }
%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Login test</title>
    </head>
    <body>
        <h1>Login test</h1>
        <form action="/test/testlogin.jsp" method="GET">
            <input type="hidden" name="loginType" value="openId"/>
            <label for="openId">My OpenId</label> <a href="" target="new">What is OpenId?</a>
            <input type="text" name="openId"/>
            <input type="submit" value="Log in with my OpenID"/>
        </form>
        <form action="/test/testlogin.jsp" method="GET">
            <input type="hidden" name="loginType" value="Google"/>
            <input type="submit" value="Log in with my Google Account"/>
        </form>
        <p><%=identifierName%></p>
    </body>
</html>
