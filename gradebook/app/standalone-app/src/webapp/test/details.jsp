<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>

<html>

<head>
	<style>
		.pageHeader {
			font-size:18pt;
			font-weight:bold;
		}
		.header {
			font-size:16pt;
			font-weight:bold;
		}
	</style>
</head>

<body>

<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.tool.gradebook.bundle.Messages"/>
</jsp:useBean>
<f:view>

	<h:outputText value="#{testGradebookTool.selectedGradebook.name}" styleClass="pageHeader" />

	<f:verbatim>
		<br/>
	</f:verbatim>

	<h:outputText value="Assignments" styleClass="header" />

	<h:dataTable id="assignments" value="#{testGradebookTool.assignments}" var="assignment">
		<h:column>
			<f:facet name="header">
				<h:outputText value="ID"/>
			</f:facet>
  			<h:outputText value="#{assignment.id}" />
		</h:column>
		<h:column>
			<f:facet name="header">
				<h:outputText value="Name"/>
			</f:facet>
			<h:outputText value="#{assignment.name}" />
		</h:column>
		<h:column>
			<f:facet name="header">
				<h:outputText value="Points"/>
			</f:facet>
			<h:outputText value="#{assignment.pointsForDisplay}" />
		</h:column>
		<h:column>
			<f:facet name="header">
				<h:outputText value="Due Date"/>
			</f:facet>
			<h:outputText value="#{assignment.dateForDisplay}" />
		</h:column>
		<h:column>
			<f:facet name="header">
				<h:outputText value="Average"/>
			</f:facet>
			<h:outputText value="#{assignment.mean}" />
		</h:column>
	</h:dataTable>

	<h:outputText value="Enrollments" styleClass="header" />

	<h:dataTable id="enrollments" value="#{testGradebookTool.students}" var="student">
		<h:column>
			<f:facet name="header">
				<h:outputText value="Auth ID"/>
			</f:facet>
  			<h:outputText value="#{student.userUid}" />
		</h:column>
		<h:column>
			<f:facet name="header">
				<h:outputText value="Display UID"/>
			</f:facet>
			<h:outputText value="#{student.displayId}" />
		</h:column>
		<h:column>
			<f:facet name="header">
				<h:outputText value="Display Name"/>
			</f:facet>
			<h:outputText value="#{student.displayName}" />
		</h:column>
	</h:dataTable>
</f:view>

</body>
</html>