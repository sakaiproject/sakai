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
			 	<sakai:view_title value="#{msgs.create_new_event} #{msgs.event_type}"/>
				<h:outputText value="#{msgs.event_error_alerts} #{errorMessageUIBean.errorMessage}" styleClass="alertMessage" escape="false" rendered="#{errorMessageUIBean.error}"/> 				
				
				<h:panelGrid id="top" columns="1">
					<h:selectOneRadio value="#{NewSignupMeetingBean.signupMeeting.meetingType}" valueChangeListener="#{NewSignupMeetingBean.processSelectedType}" onclick="submit();" layout="pageDirection">
						<f:selectItem id="individual" itemValue="#{NewSignupMeetingBean.individual}" itemLabel="#{msgs.label_individaul} #{msgs.individual_type}"/>							
						<f:selectItem id="group" itemValue="#{NewSignupMeetingBean.group}" itemLabel="#{msgs.label_group} #{msgs.group_type}"/>							
						<f:selectItem id="announcement" itemValue="#{NewSignupMeetingBean.announcement}" itemLabel="#{msgs.label_announcement} #{msgs.announcement_type}"/>										
					</h:selectOneRadio>														
				</h:panelGrid>
				<h:inputHidden id="startTime" value="#{NewSignupMeetingBean.meetingStarTime}"/>
				<h:panelGrid columns="2" style="margin:5px 5px 5px 50px;" rendered="#{NewSignupMeetingBean.signupMeeting.meetingType == NewSignupMeetingBean.individual}" styleClass="meetingTypeTable">
					<h:outputText value="#{msgs.event_num_timeslot}" styleClass="titleText" escape="false"/>
					<h:panelGroup>
						<h:inputText  id="numberOfSlot" value="#{NewSignupMeetingBean.numberOfSlots}" styleClass="editText" onkeyup="reCalculateTime();return false;">
							<f:validateLongRange minimum="1" maximum="100"/>
						</h:inputText>
						<h:message for="numberOfSlot" errorClass="alertMessageInline"/>
					</h:panelGroup>																																										
				
					<h:outputText value="#{msgs.event_duration_each_timeslot_not_bold}" styleClass="titleText" escape="false"/>
					<h:panelGroup>
						<h:inputText id="timeperiod" value="#{NewSignupMeetingBean.timeSlotDuration}" styleClass="editText" onkeyup="reCalculateTime();return false;">
							<f:validateLongRange minimum="1" maximum="1000000"/>
						</h:inputText>
						<h:message for="timeperiod" errorClass="alertMessageInline"/>
					</h:panelGroup>																								
				
					<h:outputText value="#{msgs.event_max_attendee_each_timeslot_not_bold}" styleClass="titleText" escape="false"/>
					<h:panelGroup>
						<h:inputText id="numberOfAttendees" value="#{NewSignupMeetingBean.numberOfAttendees}" styleClass="editText">
							<f:validateLongRange minimum="1" maximum="500"/>
						</h:inputText>
						<h:message for="numberOfAttendees" errorClass="alertMessageInline"/>
					</h:panelGroup>	
					
					<h:outputText value="#{msgs.event_estimated_endtime}" styleClass="titleText" escape="false"/>
					<h:inputText id='currentEndTime' value="#{NewSignupMeetingBean.meetingEndTimeFormat}" styleClass='longtext' style="color:#b11"/>																																			
				</h:panelGrid>
				
				<h:panelGrid columns="2" style="margin:5px 5px 5px 40px; vertical-align: top;"rendered="#{NewSignupMeetingBean.signupMeeting.meetingType == NewSignupMeetingBean.group}" styleClass="meetingTypeTable">
					<h:selectOneRadio id="groupSubradio" value="#{NewSignupMeetingBean.unlimited}" valueChangeListener="#{NewSignupMeetingBean.processGroup}" onclick="submit();" styleClass="meetingRadioBtn" layout="pageDirection">
						<f:selectItem itemValue="#{false}" itemLabel="#{msgs.tab_max_attendee}"/>						
						<f:selectItem itemValue="#{true}" itemLabel="#{msgs.unlimited_num_attendee}"/>								
					</h:selectOneRadio>
					
					<h:panelGrid columns="1">								
						<h:panelGroup rendered="#{!NewSignupMeetingBean.unlimited}" styleClass="meetingMaxAttd">
							<h:inputText id="maxAttendee" value="#{NewSignupMeetingBean.maxOfAttendees}" size="4" styleClass="editText">
								<f:validateLongRange minimum="1" maximum="500"/>
							</h:inputText>						
							<h:message for="maxAttendee" errorClass="alertMessageInline"/>
						</h:panelGroup>	
						<h:outputText value="&nbsp;" styleClass="titleText" escape="false"/>
					</h:panelGrid>
				</h:panelGrid>
				
				<h:panelGrid columns="1" style="margin:5px 5px 5px 50px;" rendered="#{NewSignupMeetingBean.signupMeeting.meetingType == NewSignupMeetingBean.announcement}" styleClass="meetingTypeTable">
					<h:outputText value="#{msgs.event_no_signup_required}" escape="false" styleClass="longtext"/>											
				</h:panelGrid>

			 	
				<h:inputHidden value="step3" binding="#{NewSignupMeetingBean.currentStepHiddenInfo}"/>
				<sakai:button_bar>
					<sakai:button_bar_item id="goNextPage" action="#{NewSignupMeetingBean.goNext}" value="#{msgs.next_button}"/> 
					<sakai:button_bar_item id="goBack" action="#{NewSignupMeetingBean.goBack}" value="#{msgs.goback_button}"/>
					<sakai:button_bar_item id="Cancel" action="#{NewSignupMeetingBean.processCancel}" value="#{msgs.cancel_button}"  immediate="true"/>  
                </sakai:button_bar>

			 </h:form>
  		</sakai:view_content>	
	</sakai:view_container>
	<f:verbatim>
		<script>
				var currentEndTimeTag = document.getElementById("meeting:currentEndTime");
				var startTimeTag = document.getElementById("meeting:startTime");
				var slotNumTag = document.getElementById("meeting:numberOfSlot");
				var durationTag = document.getElementById("meeting:timeperiod");
				function reCalculateTime(){					
					var slotNum = 1;					
					var duration = 15;
					if (slotNumTag.value.length <1)
						slotNum =0;
					else{	
						slotNum = parseInt(slotNumTag.value);
						//alert("slotNum:"+slotNum );
						if (isNaN(slotNum)){
							alert("You have typed an invalid number and please check!");
							return;
						}
					}
					
					if (durationTag.value.length<1)
						duration=0;
					else{
						duration = parseInt(durationTag.value);
						if (isNaN(duration)){
							alert("You have typed an invalid number and please check!");
							return;
						}
					}
					
					var endTimeMillSec = parseInt(startTimeTag.value) + slotNum*duration*60*1000;
					var endDate = new Date();
					endDate.setTime(endTimeMillSec);
					var month =endDate.getMonth()+ 1; 
					var year =endDate.getFullYear()
					var day =endDate.getDate();
					var hours = endDate.getHours();
					var am_pm = (hours < 12)? am_pm="AM" : am_pm = "PM";
					hours = (hours <= 12) ? hours : hours - 12;
    				var minutes = endDate.getMinutes();
					
					if (currentEndTimeTag){
						currentEndTimeTag.value = ((month < 10) ? '0' + month : month) + "/" 
									+ ((day < 10) ? '0' + day : day) +"/" 
									+ year+ " " 
									+ hours + ":" 
									+ ((minutes < 10) ? '0' + minutes : minutes)
									+ " " + am_pm;
					}
		
				}

		</script>
	</f:verbatim>
</f:view> 
