<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t" %>

<f:view>
	<f:loadBundle basename="messages" var="msgs"/>
	<sakai:view_container title="Signup Tool">
		<style type="text/css">
			@import url("/sakai-signup-tool/css/signupStyle.css");
		</style>
		
		<sakai:view_content>
			<h:form id="meeting">
			 	<sakai:view_title value="#{msgs.create_new_meeting} #{msgs.recurrence}"/>
				<sakai:messages />

				<h:panelGrid columns="1">
					
					<h:outputText value=" Under Construction!" style="font-weight:bold;color:red;"/>
				
				</h:panelGrid>
			 	
				<h:inputHidden value="step2" binding="#{NewSignupMeetingBean.currentStepHiddenInfo}"/>
				<sakai:button_bar>
					<sakai:button_bar_item id="goNextPage" action="#{NewSignupMeetingBean.goNext}" value="Next"/> 
					<sakai:button_bar_item id="goBack" action="#{NewSignupMeetingBean.goBack}" value="Back"/>
					<sakai:button_bar_item id="Cancel" action="#{NewSignupMeetingBean.processCancel}" value="Cancel"  immediate="true"/>  					
                </sakai:button_bar>

			 </h:form>
  		</sakai:view_content>	
	</sakai:view_container>
</f:view> 
