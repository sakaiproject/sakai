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

<%@ page import="java.util.Map" %>
<%@ page import="javax.servlet.jsp.jstl.core.LoopTagStatus" %>
<%@ page import="org.apache.pluto.testsuite.TestConfig" %>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>

<portlet:defineObjects/>

<p>
  This portlet is a portlet specification compatibility test portlet.
  It provides several tests of verying complexities which will assist in
  evaluating compliance with the portlet specification. It was originally
  developed for testing Apache Pluto, however, it does not utilize any
  proprietary APIs and should work with all compliant portlet containers.
</p>

<p>
  Please select one of the following tests:
  <table>
    <c:forEach var="testConfig" items="${tests}" varStatus="status">
      <tr>
        <td>
          # <c:out value="${status.index}"/>.
        </td>
        <td>
          <c:out value="${testConfig.name}"/>
        </td>
        
        <%-- Generate portlet action URL: Start =========================== --%>
        <portlet:actionURL secure='<%= renderRequest.isSecure() ? "True" : "False" %>'
                           var="url">
          <portlet:param name="testId"
                         value='<%= ((LoopTagStatus) pageContext.getAttribute("status")).getIndex() + "" %>'/>
          <c:forEach var="param" items="${testConfig.actionParameters}">
            <%
                TestConfig.Parameter parameter = (TestConfig.Parameter)
                        pageContext.findAttribute("param");
                String paramName = parameter.getName();
                String paramValue = parameter.getValue();
            %>
            <portlet:param name="<%= paramName %>"
                           value="<%= paramValue %>"/>
          </c:forEach>
        </portlet:actionURL>
        <%-- Generate portlet action URL: End ============================= --%>
        
        <td>
          <a href="<c:out value="${url}"/>">Test</a>
        </td>
      </tr>
    </c:forEach>
  </table>
</p>



