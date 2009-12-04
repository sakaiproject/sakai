<%--
    $URL$
    $Id$

    Copyright (c) 2009 The Sakai Foundation

    Licensed under the Educational Community License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

                http://www.osedu.org/licenses/ECL-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

--%>

<%@ page import="javax.portlet.RenderRequest" %>
<%@ page import="javax.portlet.RenderResponse" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ page import="javax.portlet.PortletURL" %>
<%@ page import="javax.portlet.PortletMode" %>
<%@ page import="org.sakaiproject.util.ResourceLoader" %>
<%@ page session="false" %>
<%!
  private static ResourceLoader rb = new ResourceLoader("basiclti");
%>

<%
RenderResponse rRes = (RenderResponse)request.getAttribute("javax.portlet.response");
PortletURL yesURL = rRes.createActionURL();
yesURL.setParameter("sakai.action","edit.do.reset");
PortletURL noURL = rRes.createActionURL();
noURL.setParameter("sakai.action","edit");
%>

<portlet:defineObjects/>

<%= rb.getString("are.you.sure") %>
<p>
<a href="<%= yesURL.toString() %>"><%= rb.getString("edit.clear.yes") %></a>
|
<a href="<%= noURL.toString() %>"><%= rb.getString("edit.clear.no") %></a>
</p>
