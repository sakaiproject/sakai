<%@ page import="javax.portlet.RenderRequest" %>
<%@ page import="javax.portlet.RenderResponse" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ page import="javax.portlet.PortletURL" %>
<%@ page import="javax.portlet.PortletMode" %>
<%@ page session="false" %>

<%
RenderResponse rRes = (RenderResponse)request.getAttribute("javax.portlet.response");
PortletURL actionURL = rRes.createActionURL();
actionURL.setPortletMode(new PortletMode("view"));
%>

<portlet:defineObjects/>

Welcome to Help Mode!
<p/>
<FORM NAME=NOTEPAD METHOD="POST" ACTION="<%=actionURL.toString()%>">
        <input type=hidden name=sakai.form.action value=main>
        <input type=submit value="Go Back"> 
</FORM>
