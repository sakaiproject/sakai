<%-- HTML JSF tag libary --%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%-- Core JSF tag library --%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%-- Sakai JSF tag library --%>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>

<f:view>
	<sakai:view_container title="Preferences">
	<sakai:view_content>
		<h:form id="prefs_form">

				<h:messages styleClass="alertMessage"/>

				<sakai:instruction_message value="Modify these preferences." />
				<h:outputText value="User: #{AdminPrefsTool.userId}"/>
				<sakai:group_box title="Preferences">

					<%-- the list of preferences --%>
					<h:dataTable value="#{AdminPrefsTool.preferences}" var="pref" cellspacing="0" styleClass="listHier">
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
					</h:dataTable>

				</sakai:group_box>
	
					<sakai:button_bar>
						<h:commandButton
								action="#{AdminPrefsTool.processActionAdd}"
								value="Add" />
						<h:commandButton
								action="#{AdminPrefsTool.processActionSave}"
								value="Save" />
						<h:commandButton
								immediate="true"
								action="#{AdminPrefsTool.processActionCancel}"
								value="Cancel" />
					</sakai:button_bar>

		 </h:form>
	</sakai:view_content>
	</sakai:view_container>
</f:view>
