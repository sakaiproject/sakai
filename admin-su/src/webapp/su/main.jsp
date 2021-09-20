<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>

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
		<sakai:instruction_message value="#{msgs.instructions_da}" rendered="#{SuTool.delegatedAccessUser && !SuTool.superUser}"/>
		<p><h:message for="su" errorClass="sak-banner-error" infoClass="sak-banner-info" showSummary="true" showDetail="false"/></p>
		<h:outputText rendered="#{SuTool.userinfo == null}" escape="false" value="<div style=\"display:none\">" />
		<div class="page-header"><h1><h:outputText value="#{msgs.userinfoheader}"/></h1></div>
		<table class="table table-striped table-hover table-bordered" summary="<h:outputText value="#{msgs.userinfoheader}"/>">
			<thead>
				<tr>
					<th><h:outputText value="#{msgs.name}"/></th>
					<th><h:outputText value="#{msgs.email}"/></th>
					<th><h:outputText value="#{msgs.eid}"/></th>
					<th><h:outputText value="#{msgs.id}"/></th>
					<th><h:outputText value="#{msgs.type}"/></th>
					<th><h:outputText value="#{msgs.created}"/></th>
				</tr>
			</thead>
			<tbody>
				<tr>
					<td><h:outputText value="#{SuTool.userinfo.displayName}" /></td>
					<td><h:outputText value="#{SuTool.userinfo.email}" /></td>
					<td><h:outputText value="#{SuTool.userinfo.eid}" /></td>
					<td><h:outputText value="#{SuTool.userinfo.id}" /></td>
					<td><h:outputText value="#{SuTool.userinfo.type}" /></td>
					<td><h:outputText value="#{SuTool.userCreatedTime}" /></td>
				</tr>
			</tbody>
		</table>
		<h:outputText rendered="#{SuTool.userinfo == null}" escape="false" value="</div>" />
		<div class="form-group row">
			<div class="col-sm-6">
				<h:message for="username" errorClass="alertMessage" infoClass="instruction" />
				<h:outputLabel for="username" value="#{msgs.eid}" /><h:outputText value="&nbsp;&nbsp;" escape="false"/>
				<h:inputText styleClass="form-control" id="username" value="#{SuTool.username}" required="true" />
			</div>
		</div>
		<p class="act">
			<h:commandButton id="become" styleClass="active" action="#{SuTool.confirm}" value="#{msgs.become_user_button}" /> 
			&nbsp;
			<h:commandButton id="view" action="#{SuTool.su}" value="#{msgs.view_user_info_button}" />
		</p>
	</h:form>
</sakai:view_container>
</f:view>
