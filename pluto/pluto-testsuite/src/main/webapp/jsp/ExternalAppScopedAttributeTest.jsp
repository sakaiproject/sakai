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

<script name="JavaScript">
function openCompanionWindow() {
	w = 500;
	h = 400;
	x = (screen.width - w) / 2;
	y = (screen.height - h) / 2;
    window.open('about:blank', 'companionWindow', 'resizable=yes,scrollb        ars=yes,status=yes,width=' + w + ',height=' + h + ',screenX=' + x + ',sc        reenY=' + y + ',left=' + x + ',top=' + y);
}
</script>

<c:choose>
  <c:when test="${results.inQuestion}">
    <table>
      <tr>
        <th colspan="2" style="background-color:blue;color:white;">MANUAL TEST</th>
      </tr>
      <tr>
        <th colspan="2">Application Scoped Session Attributes Test</th>
      </tr>
      <tr>
        <td>
          <p>
            This test is to validate that application scoped attributes can be
            viewed by resources outside of the portlet. Additionally, it tests
            to make sure that session attributes set by other resources may be
            viewed by the portlet as an application scoped session attribute.
          </p>
          <p>
            This test requires manual intervention. Click
            <a href="<%= renderResponse.encodeURL(renderRequest.getContextPath() + "/test/ExternalAppScopedAttributeTest_Servlet?sessionId=" + request.getSession().getId()) %>"
               target="companionWindow" onclick="openCompanionWindow()">
              here
            </a>
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


