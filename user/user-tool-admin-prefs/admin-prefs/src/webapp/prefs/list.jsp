<%-- HTML JSF tag libary --%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%-- Core JSF tag library --%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%-- Sakai JSF tag library --%>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>

<f:view>
	<sakai:view_container title="Preferences">
	<sakai:view_content>
		<h:form id="options_form">

				<h:messages styleClass="alertMessage"/>

				<sakai:instruction_message value="Enter a user id to select that user's preferences for editing." />

				<h:outputText value="User ID: "/><h:inputText value="#{AdminPrefsTool.userId}" />	

				<sakai:button_bar>
					<h:commandButton
							action="#{AdminPrefsTool.processActionEdit}"
							value="Edit" />
				</sakai:button_bar>

		 </h:form>
	</sakai:view_content>
	</sakai:view_container>
</f:view>
