<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ page isErrorPage="true" %>
<html>
<head><%= request.getAttribute("html.head") %>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
</head>
<body onload="<%= request.getAttribute("html.body.onload") %>" bgcolor="#ffffff">

<h1>Error page index</h1>

<br>An error occured in the bean. Error Message is: <%= exception.getMessage() %><br>
Stack Trace is : <pre><font color="red"><% 
 java.io.CharArrayWriter cw = new java.io.CharArrayWriter(); 
 java.io.PrintWriter pw = new java.io.PrintWriter(cw,true); 
 exception.printStackTrace(pw); 
 out.println(cw.toString()); 
 %></font></pre>
<br></body>
</html>
