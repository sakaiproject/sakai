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
<%@ page import="javax.portlet.PortletMode"%>
<%@ taglib uri='http://java.sun.com/portlet' prefix='portlet'%>

<portlet:defineObjects/>

<TABLE style="font-size: -1">
<TR><TH colspan="2" style="background-color:blue;color:white;">MANUAL TEST</TH></TR>
<TR><TH></TH>
    <TH>Portlet Mode Test</TH></TR>
<TR><TD colspan="2">This test requires manual intervention
        to ensure that it passes.  Click on the links below
        and make sure that the specified mode is displayed.</TD></TR>

<portlet:actionURL portletMode="<%=PortletMode.HELP.toString()%>" secure="<%=renderRequest.isSecure()?"True":"False"%>" var="url">
	<portlet:param name="testId" value="<%=renderRequest.getParameter("testId")%>"/>
</portlet:actionURL>

<TR><TD style="font-size: 12px" valign="top"><A href="<%=url%>">Help</A></TD>
    <TD style="font-size: 10px;">The help mode provides help info.  Click to ensure that help info is
        displayed.</TD></TR>

<portlet:actionURL portletMode="<%=PortletMode.EDIT.toString()%>" secure="<%=renderRequest.isSecure()?"True":"False"%>" var="url">
	<portlet:param name="testId" value="<%=renderRequest.getParameter("testId")%>"/>
</portlet:actionURL>

<TR><TD style="font-size: 12px" valign="top"><A href="<%=url%>">Edit</A></TD>
    <TD style="font-size: 10px;">The edit mode allows you to edit preferences. Click to view all preferences
    currently stored in this portlet.</TD></TR>

<portlet:actionURL portletMode="<%=PortletMode.VIEW.toString()%>" secure="<%=renderRequest.isSecure()?"True":"False"%>" var="url">
	<portlet:param name="testId" value="<%=renderRequest.getParameter("testId")%>"/>
</portlet:actionURL>

<TR><TD style="font-size: 12px"><A href="<%=url%>">View</A></TD>
    <TD style="font-size: 10px;" valign="top">You are currently looking at the view.  Verify that clicking on this link
    simply refreshes this page.</TD></TR>

</TABLE>

<%@ include file="navigation.inc" %>