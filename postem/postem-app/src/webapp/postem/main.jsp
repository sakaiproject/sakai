<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>

<f:loadBundle basename="org.sakaiproject.tool.postem.bundle.Messages" var="msgs"/>

<f:view>
	<sakai:view title="#{msgs.title_list}">
		<h:form>
            <sakai:tool_bar>
			  	<sakai:tool_bar_item
			    	action="#{PostemTool.processCreateNew}"
					value="#{msgs.bar_new}" 
					rendered="#{PostemTool.editable}"/>
   	        </sakai:tool_bar>
			<sakai:view_content>
				<br/>
	
				<sakai:flat_list value="#{PostemTool.gradebooks}" var="gradebook" binding="#{PostemTool.gradebookTable}">
					<h:column>
						<f:facet name="header">
							<h:outputText style="height: 16px; width=72px" value="#{msgs.gradebook_titles}" />
						</f:facet>
						<h:outputText value="#{gradebook.title}" />
					</h:column>
					<h:column rendered="#{PostemTool.editable}">
						<f:facet name="header">
							<h:outputText value="#{msgs.gradebook_creators}"/>
						</f:facet>
						<h:outputText value="#{gradebook.creator}"/>
					</h:column>
					<h:column rendered="#{PostemTool.editable}">
						<f:facet name="header">
							<h:outputText value="#{msgs.gradebook_lastmodifiedby}"/>
						</f:facet>
						<h:outputText value="#{gradebook.lastUpdater}"/>
					</h:column>
					<h:column>
						<f:facet name="header">
							<h:outputText value="#{msgs.gradebook_lastmodified}"/>
						</f:facet>
						<%-- <sakai:outputDate value="#{gradebook.lastUpdated}" showDate="true" showTime="true"/> --%>
						<h:outputText value="#{gradebook.updatedDateTime}"/>
					</h:column>
					<h:column rendered="#{PostemTool.editable}">
						<f:facet name="header">
							<h:outputText value="#{msgs.released}"/>
						</f:facet>
						<h:outputText rendered="#{gradebook.released}" value="#{msgs.yes}"/>
						<h:outputText rendered="#{!gradebook.released}" value="#{msgs.no}"/>
					</h:column>
					<%-- <h:column rendered="#{PostemTool.editable}">
						<f:facet name="header">
							<h:outputText value="#{msgs.stats}"/>
						</f:facet>
						<h:outputText rendered="#{gradebook.releaseStats}" value="#{msgs.yes}"/>
						<h:outputText rendered="#{!gradebook.releaseStats}" value="#{msgs.no}"/>
					</h:column> --%>
					<h:column>
						<h:commandLink action="#{PostemTool.processGradebookView}" rendered="#{!PostemTool.editable}">
							<h:outputText value="#{msgs.gradebook_view}" />
						</h:commandLink>
						<h:commandLink action="#{PostemTool.processInstructorView}" rendered="#{PostemTool.editable}">
							<h:outputText value="#{msgs.gradebook_view}" />
						</h:commandLink>
					</h:column>
					<h:column>
						<h:commandLink action="#{PostemTool.processGradebookView}" rendered="#{PostemTool.editable}">
							<h:outputText value="#{msgs.gradebook_preview}" />
						</h:commandLink>
					</h:column>
					<h:column rendered="#{PostemTool.editable}">
						<h:commandLink action="#{PostemTool.processGradebookUpdate}">
							<h:outputText value="#{msgs.gradebook_update}" />
						</h:commandLink>
					</h:column>
					<h:column rendered="#{PostemTool.editable}">
						<h:commandLink action="#{PostemTool.processGradebookDelete}">
							<h:outputText value="#{msgs.gradebook_delete}" />
						</h:commandLink>
					</h:column>
					<%-- <h:column rendered="#{PostemTool.editable && gradebook.hasGrades}"> --%>
					<h:column rendered="false">
						<f:facet name="header">
							<h:outputText value="#{msgs.csv_download}"/>
						</f:facet>
						<h:commandLink action="#{PostemTool.processCsvDownload}">
							<h:outputText value="#{msgs.csv}"/>
						</h:commandLink>
					</h:column>
					<%-- <h:column rendered="#{PostemTool.editable && gradebook.hasTemplate}"> --%>
					<h:column rendered="false">
						<f:facet name="header">
							<h:outputText value="#{msgs.template_download}"/>
						</f:facet>
						<h:commandLink action="#{PostemTool.processTemplateDownload}">
							<h:outputText value="#{msgs.template}"/>
						</h:commandLink>
					</h:column>
				</sakai:flat_list>
				

			</sakai:view_content>

		</h:form>
	</sakai:view>
</f:view>
