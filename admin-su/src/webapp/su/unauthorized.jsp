<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>

<%
	response.setContentType("text/html; charset=UTF-8");
	response.addHeader("Cache-Control", "no-store");
%>

<f:view>
<sakai:view_container title="#{msgs.title}">
  <h2><h:outputText value="#{SuTool.message}" /></h2>
  	<h:form>
  		<h:commandLink title="#{msgs.return_to_portal}" action="failed" immediate="true">
    		<h:outputText value="#{msgs.return_to_portal}" />
    	</h:commandLink>
    </h:form>
</sakai:view_container>
</f:view>
