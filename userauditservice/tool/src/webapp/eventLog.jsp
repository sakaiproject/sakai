<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai"%>
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
			
			<sakai:pager
				id="pager" 
				totalItems="#{eventLog.totalItems}"
				firstItem="#{eventLog.firstItem}"
				pageSize="#{eventLog.pageSize}"
				accesskeys="true"
				immediate="true" />
			<div class="table-responsive">
			<t:dataTable    id="userauditTable"
	                        value="#{eventLog.eventLog}"
	                        var="audit"
	                        sortColumn="#{eventLog.sortColumn}"
	                        sortAscending="#{eventLog.sortAscending}"
	                        first="#{eventLog.firstItem}"
	                        rows="#{eventLog.rowsNumber}"
	                        styleClass="table table-responsive table-hover table-striped">
	               <h:column>
					<f:facet name="header">
						<t:commandSortHeader columnName="userDisplayName" immediate="true" arrow="true">
							<h:outputText value="#{msgs.event_log_name}" />
						</t:commandSortHeader>
				   	</f:facet>
						<h:outputText value="#{audit.userDisplayName}" />
				</h:column>
	               <h:column>
	                   <f:facet name="header">
	                       <t:commandSortHeader columnName="userId" immediate="true" arrow="true">
	                           <h:outputText value="#{msgs.event_log_user_id}" />
	                       </t:commandSortHeader>
	                   </f:facet>
	                   <h:outputText value="#{audit.user.displayId}" />
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
