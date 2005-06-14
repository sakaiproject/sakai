<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>


<f:view>
<f:loadBundle basename="org.sakaiproject.tool.syllabus.bundle.Messages" var="msgs"/>
	<sakai:view_container title="#{msgs.title_list}">
	<sakai:view_content>
		<h:form>

				<br/>
	
				<sakai:flat_list value="#{SyllabusTool.entries}" var="eachEntry">
					<h:column>
						<f:facet name="header">
							<h:outputText style="height: 16px; width=72px" value="Syllabus Item" />
						</f:facet>
						<h:outputText value="#{eachEntry.entry.title}"/>
					</h:column>
					<h:column>
						<f:facet name="header">
							<h:outputText value="Content"/>
						</f:facet>
						<h:outputText value="#{eachEntry.entry.content}"/>
					</h:column>
					<h:column>
						<f:facet name="header">
							<h:outputText value="URL"/>
						</f:facet>
						<h:outputLink value="#{eachEntry.entry.redirectUrl}" target="newWin">
						  <h:outputText value="#{eachEntry.entry.redirectUrl}" />
						</h:outputLink>
					</h:column>
				</sakai:flat_list>
				
				<sakai:button_bar>
					<sakai:button_bar_item
						action="#{SyllabusTool.processEditCancel}"
						value="Back" />
				</sakai:button_bar>


		</h:form>
	</sakai:view_content>
	</sakai:view_container>
</f:view>
