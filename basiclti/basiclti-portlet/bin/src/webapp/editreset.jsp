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

<p class="messageConfirmation" style="clear:none;width:30%"><%= rb.getString("are.you.sure") %></p>
<p class="act">
	<input type="button" onclick="window.location='<%= yesURL.toString() %>'" value="<%= rb.getString("edit.clear.yes") %>" />
	<input type="button" onclick="window.location='<%= noURL.toString() %>'" value="<%= rb.getString("edit.clear.no") %>" />
	<!--//leaving these here in case 
	<a href="<%= yesURL.toString() %>"><%= rb.getString("edit.clear.yes") %></a>
	<a href="<%= noURL.toString() %>"><%= rb.getString("edit.clear.no") %></a>
	-->
</p>
