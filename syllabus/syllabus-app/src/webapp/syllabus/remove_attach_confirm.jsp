<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/syllabus" prefix="syllabus" %>
<% response.setContentType("text/html; charset=UTF-8"); %>
<f:view>

<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.tool.syllabus.bundle.Messages"/>
</jsp:useBean>

	<sakai:view_container title="#{msgs.attachment}">
		<sakai:view_content>
			<h:form>
				<h3>
					<h:outputText value="#{msgs.bar_delete_items}" />
				</h3>	
				<div class="alertMessage">
					<h:outputText value="#{msgs.delAttConfAlert}" />
				</div>	
				<h:dataTable value="#{SyllabusTool.prepareRemoveAttach}" var="eachAttach" summary="#{msgs.del_conf_listsummary}">
					<h:column>
						<f:facet name="header">
							<h:outputText value="#{msgs.title}" />
						</f:facet>
						<f:verbatim><h4></f:verbatim>
						<h:outputText value="#{eachAttach.name}"/>
						<f:verbatim></h4></f:verbatim>
						</h:column>
					<h:column>
						<f:facet name="header">
							<h:outputText value="#{msgs.size}" />
						</f:facet>
						<h:outputText value="#{eachAttach.size}"/>
					</h:column>
					<h:column>
						<f:facet name="header">
							<h:outputText value="#{msgs.type}" />
						</f:facet>
							<h:outputText value="#{eachAttach.type}"/>
						</h:column>
					<h:column>
						<f:facet name="header">
							<h:outputText value="#{msgs.created_by}" />
						</f:facet>
						<h:outputText value="#{eachAttach.createdBy}"/>
					</h:column>
					<h:column>
						<f:facet name="header">
							<h:outputText value="#{msgs.last_modified}" />
						</f:facet>
							<h:outputText value="#{eachAttach.lastModifiedBy}"/>
					</h:column>
				</h:dataTable>
				<div class="act">
					<h:commandButton 
					  value="#{msgs.bar_delete}" 
					  styleClass="active"
					  action="#{SyllabusTool.processRemoveAttach}"
					  accesskey="s" />
					<h:commandButton 
					  value="#{msgs.bar_cancel}" 
					  action="#{SyllabusTool.processRemoveAttachCancel}"
					  accesskey="x" />
				</div>	  
			</h:form>
		</sakai:view_content>
	</sakai:view_container>
</f:view>
				
