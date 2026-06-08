<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%
response.setContentType("text/html; charset=UTF-8");
%>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="UserAuditMessages"/>
</jsp:useBean>
<f:view>
	<sakai:view title="#{msgs.title_event_log}">
		<h:form id="useraudit_form">
			<h:outputText value="#{eventLog.initValues}"/>
			<h:messages showSummary="true" showDetail="false" styleClass="sak-banner-error" />
			<h:panelGroup layout="block" styleClass="sakai-table-searchFilter mb-3">
				<h:panelGroup layout="block" styleClass="sakai-table-searchFilterControls d-flex flex-wrap gap-2 align-items-end">
					<h:panelGroup layout="block">
						<h:outputLabel for="userIdFilter" value="#{msgs.event_log_filter_user_id}" />
						<h:inputText id="userIdFilter" value="#{eventLog.userIdFilter}" styleClass="form-control" size="20" />
					</h:panelGroup>
					<h:panelGroup layout="block">
						<h:outputLabel for="fromDateFilter" value="#{msgs.event_log_filter_from_date}" />
						<h:inputText id="fromDateFilter" value="#{eventLog.fromDateFilter}" styleClass="form-control" size="10" maxlength="10" title="#{msgs.event_log_filter_date_hint}" />
					</h:panelGroup>
					<h:panelGroup layout="block">
						<h:outputLabel for="toDateFilter" value="#{msgs.event_log_filter_to_date}" />
						<h:inputText id="toDateFilter" value="#{eventLog.toDateFilter}" styleClass="form-control" size="10" maxlength="10" title="#{msgs.event_log_filter_date_hint}" />
					</h:panelGroup>
					<h:panelGroup layout="block" styleClass="act">
						<h:commandButton id="searchButton" action="#{eventLog.processActionSearch}" value="#{msgs.event_log_filter_search}" styleClass="active" onclick="SPNR.disableControlsAndSpin( this, null );" />
						<h:commandButton id="clearSearchButton" action="#{eventLog.processActionClearSearch}" value="#{msgs.event_log_filter_clear}" immediate="true" onclick="SPNR.disableControlsAndSpin( this, null );" />
					</h:panelGroup>
				</h:panelGroup>
			</h:panelGroup>
			
			<sakai:pager
				id="pager" 
				totalItems="#{eventLog.totalItems}"
				firstItem="#{eventLog.firstItem}"
				pageSize="#{eventLog.pageSize}"
				accesskeys="true"
				immediate="true" />
			<div class="table">
			<t:dataTable    id="userauditTable"
	                        value="#{eventLog.eventLog}"
	                        var="audit"
	                        sortColumn="#{eventLog.sortColumn}"
	                        sortAscending="#{eventLog.sortAscending}"
	                        first="0"
	                        rows="#{eventLog.rowsNumber}"
	                        styleClass="table table table-hover table-striped">
	               <h:column>
	                   <f:facet name="header">
	                       <t:commandSortHeader columnName="userId" immediate="true" arrow="true">
	                           <h:outputText value="#{msgs.event_log_user_id}" />
	                       </t:commandSortHeader>
	                   </f:facet>
	                   <h:outputText value="#{audit.userEid}" />
	               </h:column>
	               <h:column>
	                   <f:facet name="header">
	                       <t:commandSortHeader columnName="roleName" immediate="true" arrow="true">
	                           <h:outputText value="#{msgs.event_log_role}" />
	                       </t:commandSortHeader>
	                   </f:facet>
	                   <h:outputText value="#{audit.roleName}" />
	               </h:column>
	               <h:column>
	                   <f:facet name="header">
	                       <t:commandSortHeader columnName="auditStamp" immediate="true" arrow="true">
	                           <h:outputText value="#{msgs.event_log_date}" />
	                       </t:commandSortHeader>
	                   </f:facet>
	                   <h:outputText value="#{audit.auditStamp}" />
	               </h:column>
	               <h:column>
	                   <f:facet name="header">
	                       <t:commandSortHeader columnName="actionText" immediate="true" arrow="true">
	                           <h:outputText value="#{msgs.event_log_event}" />
	                       </t:commandSortHeader>
	                   </f:facet>
	                   <h:outputText value="#{audit.actionText}" />
	               </h:column>
	               <h:column>
	                   <f:facet name="header">
	                       <t:commandSortHeader columnName="sourceText" immediate="true" arrow="true">
	                           <h:outputText value="#{msgs.event_log_source}" />
	                       </t:commandSortHeader>
	                   </f:facet>
	                   <h:outputText value="#{audit.sourceText}" />
	               </h:column>
	           </t:dataTable>
	           </div>
	       </h:form>
	</sakai:view>
</f:view>
