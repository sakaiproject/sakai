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
		<script TYPE="text/javascript" LANGUAGE="JavaScript" src="/sakai-signup-tool/js/signupScript.js"></script>
		
		<sakai:view_content>
			<h:outputText value="#{msgs.event_error_alerts} #{errorMessageUIBean.errorMessage}" styleClass="alertMessage" escape="false" rendered="#{errorMessageUIBean.error}"/>      			
				
			<h:form id="meeting">
			 	<sakai:view_title value="#{msgs.event_copy_meeting_page_title}"/>
				<sakai:doc_section>
					<h:panelGrid columns="1" styleClass="instruction">						
						<h:panelGroup> 
							<h:outputText value="#{msgs.star_character}" styleClass="reqStarInline" />
							<h:outputText value="&nbsp;#{msgs.required2}" escape="false" /> 
						</h:panelGroup>
						<h:outputText value="&nbsp;" escape="false" />
					</h:panelGrid>
				</sakai:doc_section>	
				
				<h:panelGrid columns="1">
					
						<h:panelGrid columns="2" columnClasses="titleColumn,valueColumn">
							<h:panelGroup styleClass="titleText">
								<h:outputText value="#{msgs.star_character}" style="color:#B11;"/>
								<h:outputText value="#{msgs.event_name}"  escape="false"/>
							</h:panelGroup>
							<h:panelGroup>
								<h:inputText id="meetingTitle" value="#{CopyMeetingSignupMBean.signupMeeting.title}" required="true" styleClass="editText">
									<f:validator validatorId="Signup.EmptyStringValidator"/>
								</h:inputText>
								<h:message for="meetingTitle" errorClass="alertMessageInline"/>
							</h:panelGroup>	
							
							<h:outputText value="#{msgs.event_owner}" styleClass="titleText" escape="false"/>
							<h:outputText  value="#{CopyMeetingSignupMBean.meetingWrapper.creator}" styleClass="longtext"/>
							<h:panelGroup styleClass="titleText">
								<h:outputText value="#{msgs.star_character}" style="color:#B11;"/>
								<h:outputText value="#{msgs.event_location}"  escape="false"/>
							</h:panelGroup>
							<h:panelGroup>
								<h:inputText id="meetingLocation" value="#{CopyMeetingSignupMBean.signupMeeting.location}" required="true" styleClass="editText">
									<f:validator validatorId="Signup.EmptyStringValidator"/>
								</h:inputText>														
								<h:message for="meetingLocation" errorClass="alertMessageInline"/>
							</h:panelGroup>
							
							<h:panelGroup styleClass="titleText">
								<h:outputText value="#{msgs.star_character}" style="color:#B11;"/>
								<h:outputText value="#{msgs.event_start_time}"  escape="false"/>
							</h:panelGroup>
        					<h:panelGroup styleClass="longtext">
        						<t:inputDate id="startTime" type="both"  ampm="true" value="#{CopyMeetingSignupMBean.signupMeeting.startTime}" style="color:black;" 
        							popupCalendar="true" required="true" valueChangeListener="#{CopyMeetingSignupMBean.processEventEndTime}" onchange="submit();"/>
								<h:message for="startTime" errorClass="alertMessageInline"/>
							</h:panelGroup>
							
							<h:outputText value="#{msgs.event_end_time}" styleClass="titleText" escape="false"/>
							<h:outputText id="endTime" value="#{CopyMeetingSignupMBean.signupMeeting.endTime}">
								<f:convertDateTime dateStyle="default" timeStyle="short" type="both"/>
							</h:outputText>
							
							<h:outputText value="#{msgs.event_total_duration}" styleClass="titleText" rendered="#{!CopyMeetingSignupMBean.individualType}" escape="false"/>
							<h:outputText value="#{CopyMeetingSignupMBean.totalEventDuration}" styleClass="longtext" rendered="#{!CopyMeetingSignupMBean.individualType}"/>
							
							<h:outputText value="#{msgs.event_signup_start}" rendered="#{!CopyMeetingSignupMBean.announcementType}" escape="false"/>
							<h:panelGrid columns="2" columnClasses="editText,timeSelectTab" rendered="#{!CopyMeetingSignupMBean.announcementType}" >
									<h:panelGroup>
										<h:inputText id="signupBegins" value="#{CopyMeetingSignupMBean.signupBegins}" size="3" required="true">
											<f:validateLongRange minimum="0" maximum="1000"/>
										</h:inputText>
										<h:selectOneMenu value="#{CopyMeetingSignupMBean.signupBeginsType}" >
											<f:selectItem itemValue="minutes" itemLabel="#{msgs.label_minutes}"/>
											<f:selectItem itemValue="hours" itemLabel="#{msgs.label_hours}"/>
											<f:selectItem itemValue="days" itemLabel="#{msgs.label_days}"/>
										</h:selectOneMenu>
									</h:panelGroup>
									<h:panelGroup>
										<h:outputText value="#{msgs.before_event_start}" />
										<h:message for="signupBegins" errorClass="alertMessageInline"/>
									</h:panelGroup>
							</h:panelGrid>
								
							<h:outputText value="#{msgs.event_signup_deadline}" rendered="#{!CopyMeetingSignupMBean.announcementType}" escape="false"/>
							<h:panelGrid columns="2" columnClasses="editText,timeSelectTab" rendered="#{!CopyMeetingSignupMBean.announcementType}">
									<h:panelGroup>
										<h:inputText id="signupDeadline" value="#{CopyMeetingSignupMBean.deadlineTime}" size="3" required="true">
											<f:validateLongRange minimum="0" maximum="1000"/>
										</h:inputText>
										<h:selectOneMenu value="#{CopyMeetingSignupMBean.deadlineTimeType}" >
											<f:selectItem itemValue="minutes" itemLabel="#{msgs.label_minutes}"/>
											<f:selectItem itemValue="hours" itemLabel="#{msgs.label_hours}"/>
											<f:selectItem itemValue="days" itemLabel="#{msgs.label_days}"/>
										</h:selectOneMenu>
									</h:panelGroup>
									<h:panelGroup>
										<h:outputText value="#{msgs.before_event_end}" />
										<h:message for="signupDeadline" errorClass="alertMessageInline"/>
									</h:panelGroup>
							</h:panelGrid>
								
							<h:outputText value="#{msgs.event_num_timeslot}" styleClass="titleText" rendered="#{CopyMeetingSignupMBean.individualType}" escape="false"/>
							<h:outputText id="numberOfSlots" value="#{CopyMeetingSignupMBean.signupMeeting.noOfTimeSlots}" 
								styleClass="longtext" rendered="#{CopyMeetingSignupMBean.individualType}"/>							
														
							<h:outputText value="#{msgs.event_duration_each_timeslot}" styleClass="titleText" rendered="#{CopyMeetingSignupMBean.individualType}" escape="false"/>
							<h:outputText id="duration" value="#{CopyMeetingSignupMBean.durationOfTimeSlot}" styleClass="longtext" rendered="#{CopyMeetingSignupMBean.individualType}"/>
							
							<h:outputText value="#{msgs.event_max_attendee_each_timeslot}" styleClass="titleText" escape="false" rendered="#{CopyMeetingSignupMBean.individualType}"/>
							<h:outputText value="#{CopyMeetingSignupMBean.maxNumOfAttendees}" styleClass="longtext" rendered="#{CopyMeetingSignupMBean.individualType}"/>
											
							<h:outputText value="#{msgs.event_max_attendees}" escape="false" rendered="#{CopyMeetingSignupMBean.groupType}"/>
							<h:outputText value="#{msgs.event_unlimited}" styleClass="longtext" rendered="#{CopyMeetingSignupMBean.unlimited && CopyMeetingSignupMBean.groupType}" escape="false"/>								
							<h:outputText value="#{CopyMeetingSignupMBean.maxNumOfAttendees}" styleClass="longtext" rendered="#{!CopyMeetingSignupMBean.unlimited && CopyMeetingSignupMBean.groupType}"/>
							
							<h:outputText value="&nbsp;" escape="false"/>
							<h:outputText value="&nbsp;" escape="false"/>
							
							<h:outputText value="#{msgs.event_keep_current_attendees}" styleClass="titleText" escape="false" rendered="#{!CopyMeetingSignupMBean.announcementType}"/>
							<h:panelGroup styleClass="longtext" rendered="#{!CopyMeetingSignupMBean.announcementType}">	
								<h:selectBooleanCheckbox value="#{CopyMeetingSignupMBean.keepAttendees}"/>
								<h:outputText value="#{msgs.event_yes_keep_current_attendees}" escape="false"/>
							</h:panelGroup>
							
							<h:outputText value="#{msgs.event_email_notification}" styleClass="titleText" escape="false"/>
							<h:panelGroup styleClass="longtext">
								<h:selectBooleanCheckbox value="#{CopyMeetingSignupMBean.sendEmail}" />
								<h:outputText value="#{msgs.event_yes_email_notification}" escape="false"/>
							</h:panelGroup>
						
							<h:outputText value="&nbsp;" escape="false"/>
							<h:outputText value="&nbsp;" escape="false"/>
							
							<h:outputText value="#{msgs.event_description}" styleClass="titleText" escape="false"/>
							<sakai:rich_text_area value="#{CopyMeetingSignupMBean.signupMeeting.description}" height="200" rows="5" columns="70"/>
						</h:panelGrid>
						
				</h:panelGrid>
					
										
				<sakai:button_bar>
					<sakai:button_bar_item id="copy" action="#{CopyMeetingSignupMBean.processSaveCopy}" value="#{msgs.publish_new_evnt_button}"/> 			
					<sakai:button_bar_item id="cancel" action="organizerMeeting" value="#{msgs.cancel_button}"  immediate="true"/>  
                </sakai:button_bar>

			 </h:form>
  		</sakai:view_content>	
	</sakai:view_container>
	
</f:view> 
