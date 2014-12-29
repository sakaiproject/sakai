<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>

<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.tool.gradebook.bundle.Messages"/>
</jsp:useBean>

<f:view>
	<h:dataTable id="gradebooks" value="#{testGradebookTool.gradebooks}" var="gb">
		<h:column>
			<f:facet name="header">
				<h:outputText value="Database ID"/>
			</f:facet>
			<h:outputText value="#{gb.id}" />
		</h:column>
		
		<h:column>
			<f:facet name="header">
				<h:outputText value="Name"/>
			</f:facet>
			<h:commandLink
				action="details"
				actionListener="#{testGradebookTool.selectGradebook}">
					<h:outputText value="#{gb.name}" />
			</h:commandLink>
		</h:column>
	</h:dataTable>
</f:view>
