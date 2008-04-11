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
			 	<sakai:view_title value="#{msgs.event_modify_meeting_page_title}"/>
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
								<h:inputText id="title" value="#{EditMeetingSignupMBean.signupMeeting.title}" required="true" styleClass="editText">
									<f:validator validatorId="Signup.EmptyStringValidator"/>
								</h:inputText>
								<h:message for="title" errorClass="alertMessageInline"/>
							</h:panelGroup>
							
							<h:outputText value="#{msgs.event_owner}" styleClass="titleText" escape="false"/>
							<h:outputText  value="#{EditMeetingSignupMBean.meetingWrapper.creator}" styleClass="longtext"/>
							
							<h:panelGroup styleClass="titleText">
								<h:outputText value="#{msgs.star_character}" style="color:#B11;"/>
								<h:outputText value="#{msgs.event_location}"  escape="false"/>
							</h:panelGroup>
							<h:panelGroup>
								<h:inputText id="location" value="#{EditMeetingSignupMBean.signupMeeting.location}" required="true" styleClass="editText">
									<f:validator validatorId="Signup.EmptyStringValidator"/>
								</h:inputText>														
								<h:message for="location" errorClass="alertMessageInline"/>
							</h:panelGroup>
							
							
							<h:panelGroup styleClass="titleText">
								<h:outputText value="#{msgs.star_character}" style="color:#B11;"/>
								<h:outputText value="#{msgs.event_start_time}"  escape="false"/>
							</h:panelGroup>	
        					<h:panelGroup styleClass="longtext">
        						<t:inputDate id="startTime" type="both"  ampm="true" value="#{EditMeetingSignupMBean.signupMeeting.startTime}"
        							 required="true" style="color:black;" popupCalendar="true" valueChangeListener="#{EditMeetingSignupMBean.processEventEndTime}" onchange="submit();"/>
								<h:message for="startTime" errorClass="alertMessageInline"/>
							</h:panelGroup>
					
							<h:outputText value="#{msgs.event_end_time}" styleClass="titleText" escape="false"/>
							<h:outputText  value="#{EditMeetingSignupMBean.signupMeeting.endTime}" styleClass="longtext" >
					 				<f:convertDateTime dateStyle="default" timeStyle="short" type="both" />
							</h:outputText>
							
							<h:outputText value="#{msgs.event_total_duration}" styleClass="titleText" rendered="#{!EditMeetingSignupMBean.individualType}" escape="false"/>
							<h:panelGroup styleClass="longtext" rendered="#{!EditMeetingSignupMBean.individualType}">
								<h:inputText id="totalDuration" value="#{EditMeetingSignupMBean.totalEventDuration}" styleClass="editText" 
									required="true" >
									<f:validateLongRange minimum="1" maximum="10000"/>
								</h:inputText>
								<h:message for="totalDuration" errorClass="alertMessageInline"/>
							</h:panelGroup>
							
							<h:outputText value="#{msgs.event_signup_start}"  rendered="#{!EditMeetingSignupMBean.announcementType}" escape="false"/>
							<h:panelGrid columns="2" columnClasses="editText,timeSelectTab" rendered="#{!EditMeetingSignupMBean.announcementType}">
									<h:panelGroup>
										<h:inputText id="signupBegins" value="#{EditMeetingSignupMBean.signupBegins}" size="3" required="true">
											<f:validateLongRange minimum="0" maximum="1000"/>
										</h:inputText>
										<h:selectOneMenu value="#{EditMeetingSignupMBean.signupBeginsType}" >
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
									
							<h:outputText value="#{msgs.event_signup_deadline}" rendered="#{!EditMeetingSignupMBean.announcementType}" escape="false"/>
							<h:panelGrid columns="2" columnClasses="editText,timeSelectTab" rendered="#{!EditMeetingSignupMBean.announcementType}">
									<h:panelGroup>
										<h:inputText id="signupDeadline" value="#{EditMeetingSignupMBean.deadlineTime}" size="3" required="true">
											<f:validateLongRange minimum="0" maximum="1000"/>
										</h:inputText>
										<h:selectOneMenu value="#{EditMeetingSignupMBean.deadlineTimeType}" >
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
					
							<h:outputText value="#{msgs.event_num_timeslot}" styleClass="titleText" escape="false" rendered="#{EditMeetingSignupMBean.individualType}"/>
							<h:outputText value="#{EditMeetingSignupMBean.signupMeeting.noOfTimeSlots}" styleClass="longtext" rendered="#{EditMeetingSignupMBean.individualType}"/>
							
							<h:outputText value="#{msgs.event_duration_each_timeslot}" styleClass="titleText" escape="false" rendered="#{EditMeetingSignupMBean.individualType}" />
							<h:panelGroup rendered="#{EditMeetingSignupMBean.individualType}">
								<h:inputText id="durationOfTS" value="#{EditMeetingSignupMBean.durationOfTslot}" styleClass="editText" 
									required="true" rendered="#{EditMeetingSignupMBean.individualType}" >
									<f:validateLongRange minimum="1" maximum="1000"/>
								</h:inputText>
								<h:message for="durationOfTS" errorClass="alertMessageInline"/>
							</h:panelGroup>
							
							<h:outputText value="#{msgs.event_max_attendee_each_timeslot}" styleClass="titleText" escape="false" rendered="#{EditMeetingSignupMBean.individualType}"/>
							<h:panelGroup rendered="#{EditMeetingSignupMBean.individualType}">
								<h:inputText id="maxOfAttendee" value="#{EditMeetingSignupMBean.maxNumOfAttendees}" styleClass="editText" 
									required="true" rendered="#{EditMeetingSignupMBean.individualType}">
									<f:validateLongRange minimum="1" maximum="500"/>
								</h:inputText>
								<h:message for="maxOfAttendee" errorClass="alertMessageInline"/>
							</h:panelGroup>
							
							<h:outputText value="#{msgs.event_add_more_slots}" styleClass="titleText" rendered="#{EditMeetingSignupMBean.individualType}" escape="false"/>
							<h:panelGroup rendered="#{EditMeetingSignupMBean.individualType}">
								<h:inputText id="addMoreTS"  value="#{EditMeetingSignupMBean.addMoreTimeslots}" styleClass="editText" rendered="#{EditMeetingSignupMBean.individualType}" required="true">
									<f:validateLongRange minimum="0" maximum="100"/>
								</h:inputText>
								<h:message for="addMoreTS" errorClass="alertMessageInline"/>
							</h:panelGroup>
						
							<h:outputText value="#{msgs.event_unlimited_attendees}"  rendered="#{EditMeetingSignupMBean.groupType}" escape="false"/>					
							
							<h:selectOneRadio id="groupSubradio" value="#{EditMeetingSignupMBean.unlimited}"
								 onclick="enableDisableMaxAttendees()" styleClass="meetingRadioBtn" rendered="#{EditMeetingSignupMBean.groupType}" >
								<f:selectItem id="yes" itemValue="#{true}" itemLabel="#{msgs.event_yes_label}"/>								
								<f:selectItem id="false" itemValue="#{false}" itemLabel="#{msgs.event_no_label}" />
							</h:selectOneRadio>
							
							<h:outputText value="#{msgs.event_max_attendees}" styleClass="titleText" escape="false" rendered="#{EditMeetingSignupMBean.groupType}"/>
							<h:panelGroup rendered="#{EditMeetingSignupMBean.groupType}">
								<h:inputText id="groupMaxAttendees" value="#{EditMeetingSignupMBean.maxNumOfAttendees}" styleClass="editText" 
									rendered="#{EditMeetingSignupMBean.groupType}" required="true">
									<f:validateLongRange minimum="1" maximum="500"/>
								</h:inputText>
								<h:message for="groupMaxAttendees" errorClass="alertMessageInline"/>
							</h:panelGroup>
							
							<h:outputText value="&nbsp;" escape="false"/>
							<h:outputText value="&nbsp;" escape="false"/>
							
							<h:outputText value="#{msgs.event_description}" styleClass="titleText" escape="false"/>
							<sakai:rich_text_area value="#{EditMeetingSignupMBean.signupMeeting.description}" height="200" rows="5"  columns="70"/>
						</h:panelGrid>
						
						<h:panelGrid  columns="2" columnClasses="titleColumn,valueColumn">
							<h:outputText value="&nbsp;" escape="false"/>
							<h:outputText value="&nbsp;" escape="false"/>
							
							<h:outputText value="#{msgs.event_show_attendee_public}" styleClass="titleText" rendered="#{!EditMeetingSignupMBean.announcementType}" escape="false"/>
							<h:panelGroup styleClass="editText" rendered="#{!EditMeetingSignupMBean.announcementType}">
								<h:selectBooleanCheckbox value="#{EditMeetingSignupMBean.showAttendeeName}"/>
								<h:outputText value="#{msgs.event_yes_show_attendee_public}" escape="false"/>
							</h:panelGroup>
							
							<h:outputText value="#{msgs.event_receive_notification}" styleClass="titleText" escape="false"/>
							<h:panelGroup styleClass="editText">
								<h:selectBooleanCheckbox value="#{EditMeetingSignupMBean.signupMeeting.receiveEmailByOwner}"/>
								<h:outputText value="#{msgs.event_yes_receive_notification}" escape="false"/>
							</h:panelGroup>
							
							<h:outputText value="#{msgs.event_email_notification}" styleClass="titleText" escape="false"/>
							<h:panelGroup styleClass="editText">
								<h:selectBooleanCheckbox value="#{EditMeetingSignupMBean.sendEmail}"/>
								<h:outputText value="#{msgs.event_yes_email_notification}" escape="false"/>
							</h:panelGroup>
						</h:panelGrid>
				</h:panelGrid>
					
										
				<sakai:button_bar>
					<sakai:button_bar_item id="goNextPage" action="#{EditMeetingSignupMBean.processSaveModify}" value="#{msgs.public_modify_button}"/> 			
					<sakai:button_bar_item id="Cancel" action="organizerMeeting" value="#{msgs.cancel_button}"  immediate="true"/>  
                </sakai:button_bar>

			 </h:form>
  		</sakai:view_content>	
	</sakai:view_container>
	
	<f:verbatim>
		<script>
			enableDisableMaxAttendees();
			
			var origValue;
			function enableDisableMaxAttendees(){
				var unlimitedRadioTag = document.getElementsByName("meeting:groupSubradio");		
				var maxAttInputTag=document.getElementById("meeting:groupMaxAttendees");
				var maxNum = 100000;
				if (!maxAttInputTag || !unlimitedRadioTag)//not existed
					return;
		
				var yesCheckedRadio;//introduced here due to IE 7 and FireFox act differentely
				for (var x = 0;x < unlimitedRadioTag.length; x++){		
					if (unlimitedRadioTag[x].value && unlimitedRadioTag[x].value=='true') 
						yesCheckedRadio=unlimitedRadioTag[x].checked
				}
			
				if(yesCheckedRadio){
					maxAttInputTag.disabled=true;
					maxAttInputTag.value=maxNum;
				}else{
					maxAttInputTag.disabled=false;
					if(!origValue)
						origValue=maxAttInputTag.value;
					if (origValue >= maxNum) //unlimited case
						maxAttInputTag.value=100;//default
					else
						maxAttInputTag.value=origValue;
				}	
			}
			
		</script>
	</f:verbatim>
	
</f:view> 
