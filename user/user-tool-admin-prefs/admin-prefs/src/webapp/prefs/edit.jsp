<%-- HTML JSF tag libary --%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%-- Core JSF tag library --%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%-- Sakai JSF tag library --%>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>

<f:view>
	<sakai:view_container title="Preferences">
	<sakai:view_content>
		<h:form id="prefs_form">

				<sakai:messages />

				<sakai:instruction_message value="Modify these preferences." />
				<h:outputText value="User: #{AdminPrefsTool.userId}"/>
				<sakai:group_box title="Preferences">

					<%-- the list of preferences --%>
					<sakai:flat_list value="#{AdminPrefsTool.preferences}" var="pref">
						<h:column>
							<f:facet name="header">
								<h:outputText value="key"/>
							</f:facet>
							<h:inputText value="#{pref.key}"/>
						</h:column>
						<h:column>
							<f:facet name="header">
								<h:outputText value="name"/>
							</f:facet>
							<h:inputText value="#{pref.name}"/>
						</h:column>
						<h:column>
							<f:facet name="header">
								<h:outputText value="value"/>
							</f:facet>
							<h:inputText value="#{pref.value}" />
						</h:column>
						<h:column>
							<f:facet name="header">
								<h:outputText value="list"/>
							</f:facet>
							<h:selectBooleanCheckbox value="#{pref.list}"/>
						</h:column>
					</sakai:flat_list>

				</sakai:group_box>
	
					<sakai:button_bar>
						<sakai:button_bar_item
								action="#{AdminPrefsTool.processActionAdd}"
								value="Add" />
						<sakai:button_bar_item
								action="#{AdminPrefsTool.processActionSave}"
								value="Save" />
						<sakai:button_bar_item
								immediate="true"
								action="#{AdminPrefsTool.processActionCancel}"
								value="Cancel" />
					</sakai:button_bar>

		 </h:form>
	</sakai:view_content>
	</sakai:view_container>
</f:view>
