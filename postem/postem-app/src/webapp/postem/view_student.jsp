<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>

<f:loadBundle basename="org.sakaiproject.tool.postem.bundle.Messages" var="msgs"/>

<f:view>
	<sakai:view title="#{msgs.title_view}">
		<sakai:view_content>
			<h:form>
				
		  	<sakai:tool_bar_message value="#{msgs.view_student}" />
		  	<sakai:panel_edit>
		  		<h:outputLabel styleClass="shorttext" for="participant"><h:outputText value="#{msgs.choose_username}"/></h:outputLabel>
		  		<h:selectOneMenu value="#{PostemTool.selectedStudent}" id="participant" onchange="submit()">
		  			<f:selectItems value="#{PostemTool.currentGradebook.studentMap}"/>
		  		</h:selectOneMenu>
		  	</sakai:panel_edit>
		  	
		  	<br />
		  	
	    	<h:outputText value="#{PostemTool.selectedStudentGrades}" escape="false" rendered="#{PostemTool.editable}"/>
		  	
				<sakai:button_bar>					
					<sakai:button_bar_item
						action="#{PostemTool.processCancelView}"
						value="#{msgs.back}" />
				</sakai:button_bar>		  	

			</h:form>
		</sakai:view_content>
	</sakai:view>
</f:view>