<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ page isErrorPage="true" %>
<!-- $Id$
<%--
***********************************************************************************
*
* Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
*
* Licensed under the Educational Community License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.osedu.org/licenses/ECL-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License. 
*
**********************************************************************************/
--%>
-->
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
