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

<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session"> 
<jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.tool.postem.bundle.Messages"/> 
</jsp:useBean>
<f:view>
	<sakai:view title="#{msgs.title_list}">
		<h:form>
            <sakai:tool_bar>
			  	<sakai:tool_bar_item
			    	action="#{PostemTool.processCreateAndEdit}"
					value="#{msgs.bar_create_edit}" 
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
						<sakai:output_date value="#{gradebook.lastUpdated}"/>
					</h:column>
					<h:column>
						<h:commandLink action="#{PostemTool.processGradebookView}">
							<h:outputText value="#{msgs.gradebook_view}" />
						</h:commandLink>
					</h:column>
					<h:column rendered="#{PostemTool.editable}">
						<h:commandLink action="#{PostemTool.processGradebookEdit}">
							<h:outputText value="#{msgs.gradebook_edit}" />
						</h:commandLink>
					</h:column>
					<h:column rendered="#{PostemTool.editable}">
						<h:commandLink action="#{PostemTool.processGradebookDelete}">
							<h:outputText value="#{msgs.gradebook_delete}" />
						</h:commandLink>
					</h:column>
				</sakai:flat_list>
				

			</sakai:viewContent>

		</h:form>
	</sakai:view>
</f:view>