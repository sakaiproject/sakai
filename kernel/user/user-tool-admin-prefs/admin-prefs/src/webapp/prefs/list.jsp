<%-- HTML JSF tag libary --%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%-- Core JSF tag library --%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%-- Sakai JSF tag library --%>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>

<f:view>
	<sakai:view_container title="Preferences">
	<sakai:view_content>
		<h:form id="options_form">

				<sakai:messages />

				<sakai:instruction_message value="Enter a user id to select that user's preferences for editing." />

				<h:outputText value="User ID: "/><h:inputText value="#{AdminPrefsTool.userId}" />	

				<sakai:button_bar>
					<sakai:button_bar_item
							action="#{AdminPrefsTool.processActionEdit}"
							value="Edit" />
				</sakai:button_bar>

		 </h:form>
	</sakai:view_content>
	</sakai:view_container>
</f:view>
