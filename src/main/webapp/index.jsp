<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.net.URLDecoder"%>
<%
if(request.getSession().getAttribute("user") != null)
{
    response.sendRedirect("start.jsp");
}
String message = request.getParameter("message") != null ? URLDecoder.decode(request.getParameter("message"), "UTF-8") : "";
String errorMessage = request.getParameter("errorMessage") != null ? URLDecoder.decode(request.getParameter("errorMessage"), "UTF-8") : "";

%>
<!DOCTYPE HTML>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Welcome to VIPSLogic</title>
    </head>
    <body>
        <% if(errorMessage !=null) {%>
        <p style="color: red;"><%=errorMessage %></p>
        <% } %>
        <h1>Welcome to VIPSLogic</h1>
        <h2>Please log in</h2>
        <form action="login" method="POST">
            <label for="username">User name</label>
            <input type="text" name="username" size="15"/><br/>
            <label for="password">Password</label>
            <input type="password" name="password" size="15"/><br/>
            <input type="submit" value="Log in"/>
        </form>
    </body>
</html>
