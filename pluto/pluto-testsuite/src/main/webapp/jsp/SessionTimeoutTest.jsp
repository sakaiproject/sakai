<%@ page import="javax.portlet.WindowState" %>
<%--
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed  under the  License is distributed on an "AS IS" BASIS,
WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
implied.

See the License for the specific language governing permissions and
limitations under the License.
--%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>

<portlet:defineObjects/>

<c:choose>
  <c:when test="${results.inQuestion}">
    
    <%-- Generate portlet render URL: Start =============================== --%>
    <portlet:renderURL windowState="<%=WindowState.MAXIMIZED.toString()%>" secure='<%= renderRequest.isSecure() ? "True" : "False" %>'
                       var="url">
      <portlet:param name="maxInactiveIntervalSet" value="<%= Boolean.TRUE.toString() %>"/>
      <portlet:param name="testId" value="<%= renderRequest.getParameter("testId") %>"/>
    </portlet:renderURL>
    <%-- Generate portlet action URL: End ================================= --%>
  
  
  
    <table>
      <tr>
        <th colspan="2" style="background-color:blue;color:white;">MANUAL TEST</th>
      </tr>
      <tr>
        <th colspan="2">Session Timeout Test</th>
      </tr>
      <tr>
        <td>
          <p>
            This test is to validate that portlet session will expire and be
            invalidated by the portlet container after its max inactive interval
            is passed.
          </p>
          <p>
            This test requires manual intervention. Please wait for at least
            5 seconds and click <a href="<c:out value="${url}"/>">here</a>.
          </p>
            <p>
                NOTE: Clicking the url above will maximize this portlet.  This is required
                to ensure that no other portlets on the current page recreate the session we
                are trying to invalidate.
            </p>
        </td>
      </tr>
    </table>
  </c:when>
  <c:otherwise>
    <%@ include file="test_results.inc" %>
  </c:otherwise>
</c:choose>

<%@ include file="navigation.inc" %>


