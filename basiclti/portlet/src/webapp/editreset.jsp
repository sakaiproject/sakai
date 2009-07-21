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
<a href="<%= yesURL.toString() %>">YES</a>
|
<a href="<%= noURL.toString() %>">NO</a>
</p>
