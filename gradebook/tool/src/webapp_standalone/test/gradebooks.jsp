<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>

<f:loadBundle basename="org.sakaiproject.tool.gradebook.bundle.Messages" var="msgs"/>

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
