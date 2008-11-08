<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>

<%
	response.setContentType("text/html; charset=UTF-8");
	response.addDateHeader("Expires", System.currentTimeMillis() - (1000L * 60L * 60L * 24L * 365L));
	response.addDateHeader("Last-Modified", System.currentTimeMillis());
	response.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
	response.addHeader("Pragma", "no-cache");
%>

<f:view>
<sakai:view_container title="#{msgs.title}">

	<f:subview id="allowed" rendered="#{SuTool.allowed}">
		<h:message for="allowed" errorClass="alertMessage" infoClass="instruction" />
	</f:subview>

	<h:form id="su">
	  <sakai:instruction_message value="#{msgs.instructions}" />

		<p><h:message for="su" errorClass="alertMessage" infoClass="instruction" showSummary="true" showDetail="false"/></p>

		<h:panelGrid columns="2" rendered="#{SuTool.userinfo != null}" headerClass="listHier" >
		  <f:facet name="header">
			<h:outputText value="#{msgs.userinfoheader}"/>
		  </f:facet>
		  <h:outputText value="#{msgs.name}" />
		  <h:outputText value="#{SuTool.userinfo.displayName}" />
		  <h:outputText value="#{msgs.email}" />
		  <h:outputText value="#{SuTool.userinfo.email}" />
		  <h:outputText value="#{msgs.eid}" />
		  <h:outputText value="#{SuTool.userinfo.eid}" />
		  <h:outputText value="#{msgs.id}" />
		  <h:outputText value="#{SuTool.userinfo.id}" />
		  <h:outputText value="#{msgs.type}" />
		  <h:outputText value="#{SuTool.userinfo.type}" />
		  <h:outputText value="#{msgs.created}" />
		  <h:outputText value="#{SuTool.userinfo.createdTime.display}" />
		</h:panelGrid>

		<h:message for="username" errorClass="alertMessage" infoClass="instruction" />
		<p><h:inputText id="username" value="#{SuTool.username}" required="true" /></p>

		<p class="act">
			<h:commandButton id="become" styleClass="active" action="#{SuTool.confirm}" value="#{msgs.become_user_button}" /> 
			&nbsp;
			<h:commandButton id="view" action="#{SuTool.su}" value="#{msgs.view_user_info_button}" />
		</p>

	</h:form>

</sakai:view_container>
</f:view>
