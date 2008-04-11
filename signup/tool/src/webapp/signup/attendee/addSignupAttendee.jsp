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
			<h:outputText value="#{msgs.event_error_alerts} #{errorMessageUIBean.errorMessage}" styleClass="alertMessage" escape="false" rendered="#{errorMessageUIBean.error}"/> 
			<h:form id="meeting">
			 	<sakai:view_title value="#{msgs.event_addSignup_attendee_page_title}"/>
				<sakai:messages />

				<h:panelGrid columns="2" columnClasses="titleColumn,valueColumn">
					<h:outputText value="#{msgs.event_timeslot}" escape="false"/>
					
					<h:panelGroup>				    
		  		   		<h:outputText value="#{AttendeeSignupMBean.timeslotWrapper.timeSlot.startTime}">
							<f:convertDateTime timeStyle="short" />
						</h:outputText>
						<h:outputText value="#{msgs.timeperiod_divider}" escape="false"/>
						<h:outputText value="#{AttendeeSignupMBean.timeslotWrapper.timeSlot.endTime}">
							<f:convertDateTime timeStyle="short" />
						</h:outputText>
						<h:outputText value=",&nbsp;" escape="false"></h:outputText>
						<h:outputText value="#{AttendeeSignupMBean.timeslotWrapper.timeSlot.startTime}" styleClass="longtext">
							<f:convertDateTime dateStyle="full"/>
						</h:outputText>
					</h:panelGroup>			
				   	
				  <h:outputText value="#{msgs.event_comments}" escape="false"/> 
				  <sakai:rich_text_area value="#{AttendeeSignupMBean.timeslotWrapper.newAttendee.comments}" height="200" rows="5"  columns="70"/>
				</h:panelGrid>
				
				<sakai:button_bar>
					<sakai:button_bar_item id="save" action="#{AttendeeSignupMBean.attendeeSaveSignup}" value="#{msgs.event_button_finish}"/> 
					<sakai:button_bar_item id="Cancel" action="meeting" value="#{msgs.cancel_button}"  immediate="true"/>  
                </sakai:button_bar>

			 </h:form>
  		</sakai:view_content>	
	</sakai:view_container>
</f:view> 
