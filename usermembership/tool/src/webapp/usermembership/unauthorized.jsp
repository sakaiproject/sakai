<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>

<%
	response.setContentType("text/html; charset=UTF-8");
	response.addHeader("Cache-Control", "no-store, no-cache");
%>
 
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.umem.tool.bundle.Messages"/>
</jsp:useBean>

<f:view>
<sakai:view_container title="#{msgs.title}">
  <h2><h:outputText value="#{msgs.unauthorized}"/></h2>
	<h:outputLink value="/portal">
		<h:outputText value="#{msgs.return_to_portal}"/>
	</h:outputLink>
</sakai:view_container>
</f:view>