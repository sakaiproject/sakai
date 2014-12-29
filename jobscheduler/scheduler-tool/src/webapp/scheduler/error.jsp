<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ page isErrorPage="true" %>


<f:view>
	<sakai:view_container>		
    <sakai:view_content>
  	  <h:outputText value="#{requestScope['error']}" styleClass="alertMessage"/>
	  </sakai:view_content>
	</sakai:view_container>
</f:view>
