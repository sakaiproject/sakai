<%@ page import="javax.portlet.RenderRequest" %>
<%@ page import="javax.portlet.RenderResponse" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ page import="javax.portlet.PortletURL" %>
<%@ page import="javax.portlet.PortletMode" %>
<%@ page session="false" %>

<%
RenderResponse rRes = (RenderResponse)request.getAttribute("javax.portlet.response");
PortletURL yesURL = rRes.createActionURL();
yesURL.setParameter("sakai.action","edit.do.reset");
PortletURL noURL = rRes.createActionURL();
noURL.setParameter("sakai.action","edit");
%>

<portlet:defineObjects/>

Are you sure your want to remove the settings for this Tool Interoperability
Placement?
<p>
<a href="<%= yesURL.toString() %>">YES</a>
|
<a href="<%= noURL.toString() %>">NO</a>
</p>
