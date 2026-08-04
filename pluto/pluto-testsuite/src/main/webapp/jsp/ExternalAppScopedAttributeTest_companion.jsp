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


<HTML>
<HEAD>
<STYLE>
BODY, P, TH, TD {
    font-family: arial, helvetica, sans-serif;
    text-align: left;
}

.BODY, .P, .TD {
    font-size: 12px;
}

TABLE {
    width: 100%;
    padding: 2px 3px;
    border-collapse:collapse;
    border-spacing: 3px 3px;
}


.banner, .banner TD, .banner A:link, .banner A:visited, .banner A:hover {
    background-color: #DDDDDD;
    color: #36a;
}

.nav, .nav TD, .nav A:link, .nav A:visited, .nav A:hover {
    background-color: #DDDDDD;
    color: #36a;
}

.small {
    font-size: -1;
}

</STYLE>
</HEAD>
<TABLE style="font-size: -1" border="1">
<c:choose>
<c:when test="${passed}">
<TR><TH colspan="2" style="background-color:green;color:white">PASSED</FONT>
</c:when>
<c:otherwise>
<TR><TH colspan="2" style="background-color:red;color:white">FAILED</TH></TR>
</c:otherwise>
</c:choose>

<TR><TD colspan="2">
    <p>This resource has been requested outside of the portal framework.  It is
    intended to test application scope attribute functions of the PortletSession.
    If the PortletSession is functioning as designed the key set in the PortletSession
    should be readable.  If it were, this test would have passed. This resource has
    also added a key to it's HttpSession which should be viewable through the PortletSession
    attribute.</p>

    <p>Note that this test is not expected to pass on Tomcat 4.x or 5.0.x. On Tomcat 5.5.x
    it requires <i>emptySessionPath="true"</i> to be added to the connector configuration
    in server.xml. See
    <a href="http://portals.apache.org/pluto/faq.html#tomcat5_5" target="_blank">the FAQ</a>
    for details.</p>
</TD></TR>
<TR><TH colspan="2">Session Id Comparison</TH></TR>
<TR><TD>This HttpSession Id</TD>
    <TD><c:out value="${pageContext.request.session.id}"/></TD></TR>
<TR><TD>Requesting Portlet's Session Id</TD>
    <TD><%=request.getParameter("sessionId")%></TD></TR>
    </TABLE>
</BODY>
</HTML>

