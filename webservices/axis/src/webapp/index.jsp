<html>
<%@ page contentType="text/html; charset=utf-8" %>
<%
/*
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
%>

<%@ include file="i18nLib.jsp" %>

<%
    // initialize a private HttpServletRequest
    setRequest(request);

    // set a resouce base
    setResouceBase("i18n");
%>

<head>
 <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
 <title>Apache-Axis</title>
</head>

<body bgcolor="#FFFFFF">

<h1 align="center">Apache-AXIS</h1>

<%= getLocaleChoice() %>

<%
  out.print(getMessage("welcomeMessage")+"<p/>");
  out.print(getMessage("operationType"));
%>

<ul>

  <li>
    <%
      out.print("<a href=\""+ getMessage("validationURL") +"\">");
      out.print(getMessage("validation") +"</a> - ");
      out.print(getMessage("validationFootnote00") +"<br>");
      out.print("<i>"+ getMessage("validationFootnote01") +"</i>");
    %>
  </li>

  <li>
    <%
      out.print("<a href=\""+ getMessage("serviceListURL") +"\">");
      out.print(getMessage("serviceList") +"</a> - ");
      out.print(getMessage("serviceListFootnote"));
    %>
  </li>

  <li>
    <%
      out.print("<a href=\""+ getMessage("visitURL") +"\">");
      out.print(getMessage("visit") +"</a> - ");
      out.print(getMessage("visitFootnote"));
    %>
  </li>

  <li>
    <%
      out.print("<a href=\""+ getMessage("adminURL") +"\">");
      out.print(getMessage("admin") +"</a> - ");
      out.print(getMessage("adminFootnote"));
    %>
  </li>

  <li>
    <%
      out.print("<a href=\""+ getMessage("soapMonitorURL") +"\">");
      out.print(getMessage("soapMonitor") +"</a> - ");
      out.print(getMessage("soapMonitorFootnote"));
    %>
  </li>

</ul>

<%
  out.print(getMessage("sideNote") +"<p/>");
%>

<%
  out.print("<h3>"+ getMessage("validatingAxis") +"</h3>");

  out.print(getMessage("validationNote00") +"<p/>");
  out.print(getMessage("validationNote01"));
%>
</body>
</html>
