<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>

<f:loadBundle basename="org.sakaiproject.tool.postem.bundle.Messages" var="msgs"/>

<f:view>
	<sakai:view title="#{msgs.title_list}">
		<h:form>

			<sakai:view_content>
				<br />
				<h:outputText value="#{msgs.gradebook_lastmodified}"/>
				<h:outputText value="#{gradebook.updatedDateTime}"/>
				<br />
				<sakai:flat_list value="#{PostemTool.students}" var="student">
					<h:column>
						<f:facet name="header">
							<h:outputText style="height: 16px; width=72px" value="#{msgs.username}" />
						</f:facet>
						<h:outputText value="#{student.username}" rendered="#{student.readAfterUpdate}"/>
						<h:outputText value="#{student.username}" style="color: red; font-weight: bold;"
						              rendered="#{!student.readAfterUpdate}"/>
					</h:column>
					<h:column>
						<f:facet name="header">
							<h:outputText value="#{PostemTool.currentGradebook.headingsRow}" escape="false"/>
						</f:facet>
						<h:outputText value="#{student.gradesRow}" escape="false"/>
					</h:column>
					<h:column>
						<f:facet name="header">
							<h:outputText value="#{msgs.student_lastchecked}"/>
						</f:facet>
						<h:outputText value="#{student.checkDateTime}"/>
					</h:column>
				</sakai:flat_list>
				<sakai:button_bar>					
					<sakai:button_bar_item
						action="#{PostemTool.processCancelView}"
						value="#{msgs.back}" />
				</sakai:button_bar>

			</sakai:view_content>

		</h:form>
	</sakai:view>
</f:view>