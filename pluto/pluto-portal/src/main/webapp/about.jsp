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
<%@ taglib uri="http://portals.apache.org/pluto" prefix="pluto" %>

<html>
<head>
    <title>Pluto Portal</title>
    <style type="text/css" title="currentStyle" media="screen">
        @import "<c:out value="${pageContext.request.contextPath}"/>/pluto.css";
        @import "<c:out value="${pageContext.request.contextPath}"/>/portlet-spec-1.0.css";
    </style>
    <script type="text/javascript" src="<c:out value="${pageContext.request.contextPath}"/>/pluto.js">
    </script>
</head>

<body>

<div id="portal">

    <!-- Header block: the Apache Pluto banner image and description -->
    <div id="header">
        <h1>Apache Pluto</h1>

        <p>An Apache Portals Project</p>
    </div>

    <!-- Logout link -->
    <c:if test="${pageContext.request.remoteUser != null}">
        <div id="logout">
            <a href="<c:url value='/Logout'/>">Logout</a>
        </div>
    </c:if>

    <!-- Navigation block: links to portal pages -->
    <jsp:include page="/WEB-INF/themes/navigation.jsp"/>

    <!-- Content block: portlets are divided into two columns/groups -->
    <div id="content">
        <pluto:isMaximized var="isMax"/>

        <!-- Left column -->
        <c:choose>
            <c:when test="${isMax}">
                <c:set var="portlet" value="/pluto.AboutPortlet!A" scope="request"/>
                <jsp:include page="/WEB-INF/themes/portlet-skin.jsp"/>
                <c:set var="portlet" value="/pluto.AboutPortlet!B" scope="request"/>
                <jsp:include page="/WEB-INF/themes/portlet-skin.jsp"/>
            </c:when>

            <c:otherwise>
                <div id="portlets-left-column">
                    <c:set var="portlet" value="/pluto.AboutPortlet" scope="request"/>
                    <jsp:include page="/WEB-INF/themes/portlet-skin.jsp"/>
                </div>

                <!-- Right column -->
                <div id="portlets-right-column">
                    <c:set var="portlet" value="/testsuite.TestPortlet1" scope="request"/>
                    <jsp:include page="/WEB-INF/themes/portlet-skin.jsp"/>
                </div>
            </c:otherwise>
        </c:choose>

    </div>

    <!-- Footer block: copyright -->
    <div id="footer">
        &copy; 2003-2005 Apache Software Foundation
    </div>

</div>

</body>

</html>


