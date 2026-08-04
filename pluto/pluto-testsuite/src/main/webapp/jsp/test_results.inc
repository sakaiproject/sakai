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

<%@ page import="org.apache.pluto.testsuite.TestResult" %>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>

<%
    pageContext.setAttribute("PASSED_CODE", new Integer(TestResult.PASSED));
    pageContext.setAttribute("FAILED_CODE", new Integer(TestResult.FAILED));
    pageContext.setAttribute("WARNING_CODE", new Integer(TestResult.WARNING));
%>

<portlet:defineObjects/>

<table>
  <c:choose>
    <c:when test="${results == null}">
      <tr>
        <th colspan="2" style="background-color:red; color:white;">
          TEST RESULT NOT FOUND
        </th>
      </tr>
      <tr>
        <td valign="top">
          <img src="<%= renderResponse.encodeURL(renderRequest.getContextPath() + "/images/question.gif") %>"
               border="0" />
        </td>
        <td>
          <p>
            The test results could not be found in the request scope. If this
            is an ActionTest (test logic resides in the processAction() method),
            one possible reason may be that the original portlet session where
            the test results reside was invalidated by another portlet.
          </p>
          <p>
            Please return to the introduction page and retry.
          </p>
        </td>
      </tr>
    </c:when>
    <c:when test="${results.failed}">
      <tr>
        <th colspan="2" style="background-color:red; color:white;">
          TEST FAILED
        </th>
      </tr>
    </c:when>
    <c:when test="${results.inQuestion}">
      <tr>
        <th colspan="2" style="background-color:yellow;">
          WARNING
        </th>
      </tr>
    </c:when>
    <c:otherwise>
      <tr>
        <th colspan="2" style="background-color:green; color:white;">
          TEST PASSED
        </th>
      </tr>
    </c:otherwise>
  </c:choose>
  
  <tr>
    <th />
    <th><c:out value="${results.name}"/></th>
  </tr>
  
  <c:forEach var="result" varStatus="status" items="${results.collection}">
    <tr>
      
      <!-- Result Icon ===================================================== -->
      <td valign="top">
        <c:choose>
          <c:when test="${result.returnCode == PASSED_CODE}">
            <img src="<%= renderResponse.encodeURL(renderRequest.getContextPath() + "/images/yes.gif") %>"
                 border="0" />
          </c:when>
          <c:when test="${result.returnCode == FAILED_CODE}">
            <img src="<%= renderResponse.encodeURL(renderRequest.getContextPath() + "/images/no.gif") %>"
                 border="0" />
          </c:when>
          <c:otherwise>
            <img src="<%= renderResponse.encodeURL(renderRequest.getContextPath() + "/images/question.gif") %>"
                 border="0" />
          </c:otherwise>
        </c:choose>
      </td>
      
      <td style="white-space:nowrap;">
        PLT <c:out value="${result.specPLT}"/> - <c:out value="${result.name}"/>
      </td>
    </tr>
    
    <c:choose>
      <c:when test="${result.returnCode == PASSED_CODE}">
        <tr>
          <td />
          <td style="font-size:smaller;">
            <c:out value="${result.description}"/>
          </td>
        </tr>
      </c:when>
      <c:otherwise>
        <tr>
          <td />
          <td style="font-size:smaller;">
            <c:out value="${result.description}"/>
            <br/>
            <span style="color:#FF0000;">
              <c:out value="${result.resultMessage}"/>
            </span>
          </td>
        </tr>
      </c:otherwise>
    </c:choose>
  </c:forEach>
  
</table>

