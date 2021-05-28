<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>

<f:view>	
	<sakai:view_container title="#{msgs.title_edit}">
		<sakai:view_content>
			<sakai:tool_bar_message value="#{msgs.askQuestion}" /> 				
			<h:form> 
				<sakai:doc_section>
					<h:outputText value="#{msgs.confirmaton}"/>
				</sakai:doc_section>
				<sakai:group_box title="#{msgs.contact_info}">
				<sakai:panel_edit>
							<h:outputText value="#{msgs.lastname}"/>
						    <h:inputText value="#{QuestionTool.lastName}" disabled="true" />

							<h:outputText value="#{msgs.firstname}"/>
							<h:inputText value="#{QuestionTool.firstName}" disabled="true" />
							
							<h:outputText value="#{msgs.username}"/>
						    <h:inputText value="#{QuestionTool.userName}" disabled="true" />

							<h:outputText value="#{msgs.emailAddress}"/>
							<h:inputText value="#{QuestionTool.emailAddress}" disabled="true" />
					</sakai:panel_edit>
				</sakai:group_box>
				<sakai:group_box>
				<sakai:panel_edit>
							<h:outputText value="#{msgs.subject}"/>
						    <h:inputText value="#{QuestionTool.subject}" disabled="true" />
						    
							<h:outputText value="#{msgs.content}"/>
							<h:inputTextarea value="#{QuestionTool.content}" cols="45" rows="10" disabled="true" />		
					</sakai:panel_edit>
				</sakai:group_box>
				<sakai:button_bar>
					<sakai:button_bar_item
						action="#{QuestionTool.reset}"
						value="#{msgs.reset}" />
			   </sakai:button_bar>
		  </h:form>
  	 </sakai:view_content>
  </sakai:view_container>
</f:view>
