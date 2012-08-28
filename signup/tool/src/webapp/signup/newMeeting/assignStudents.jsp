<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t" %>

<f:view locale="#{UserLocale.locale}">
	<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
	   <jsp:setProperty name="msgs" property="baseName" value="messages"/>
	</jsp:useBean>
	<sakai:view_container title="Signup Tool">
		<style type="text/css">
			@import url("/sakai-signup-tool/css/signupStyle.css");
		</style>
		
		<h:outputText value="#{msgs.event_error_alerts} #{messageUIBean.errorMessage}" styleClass="alertMessage" escape="false" rendered="#{messageUIBean.error}"/>      			
						
		<sakai:view_content>
			<h:form id="meeting">
			 	<sakai:view_title value="#{msgs.event_assign_attendee_page_title}"/>

				<h:panelGrid columns="2" style="margin-top:20px;margin-bottom:20px;" columnClasses="titleColumn,valueColumn">
					<h:outputText value="#{msgs.event_date}" styleClass="titleText" escape="false"/>
					<h:panelGroup>
						<h:outputText value="#{NewSignupMeetingBean.signupMeeting.startTime}" styleClass="longtext">
							<f:convertDateTime pattern="EEEEEEEE, " />
				 		</h:outputText>
						<h:outputText value="#{NewSignupMeetingBean.signupMeeting.startTime}" styleClass="longtext">
							<f:convertDateTime dateStyle="long" />
				 		</h:outputText>
				 		<h:outputText value="#{NewSignupMeetingBean.signupMeeting.startTime}" styleClass="longtext">
							<f:convertDateTime pattern=", h:mm a" />
				 		</h:outputText>
				 	</h:panelGroup>
				 	
					<h:outputText styleClass="titleText" value="#{msgs.event_recurrence}"  rendered="#{NewSignupMeetingBean.recurrence}" escape="false"/> 
					<h:outputText value="#{NewSignupMeetingBean.eventFreqType}" rendered="#{NewSignupMeetingBean.recurrence}" escape="false"/>
					 
					<h:outputText value="#{msgs.event_repeat_until}" styleClass="titleText"  rendered="#{NewSignupMeetingBean.recurrence}" escape="false"/>
					<h:outputText value="#{NewSignupMeetingBean.repeatUntil}" rendered="#{NewSignupMeetingBean.recurrence}">
						<f:convertDateTime dateStyle="full" />
					</h:outputText>
				</h:panelGrid>
				
				<h:panelGrid columns="2" columnClasses="titleColumn,valueColumn" styleClass="assingAttendeeTable">
					<h:outputText value="#{msgs.assign_participants_toAllRecurrences}"  escape="false" styleClass="titleText" rendered="#{NewSignupMeetingBean.recurrence}"/>
					<h:panelGroup styleClass="longtext" rendered="#{NewSignupMeetingBean.recurrence}">
			   			<h:selectBooleanCheckbox value="#{NewSignupMeetingBean.assignParicitpantsToAllRecurEvents}" style="vertical-align:middle;"/>
						<h:outputText value="#{msgs.apply_added_participants_to_allRecur_events}" escape="false"/>
			   		</h:panelGroup>
			   		
			   		<h:outputText value="#{msgs.event_create_email_notification}" styleClass="titleText" escape="false" rendered="#{NewSignupMeetingBean.publishedSite && NewSignupMeetingBean.sendEmail}"/>
					<h:panelGroup id="emailAttendeeOnly"  rendered="#{NewSignupMeetingBean.publishedSite && NewSignupMeetingBean.sendEmail}">
						<h:selectOneRadio  value="#{NewSignupMeetingBean.sendEmailToSelectedPeopleOnly}" layout="lineDirection" style="margin:-5px 0px 0px -4px;">
	                       		<f:selectItem id="all_attendees" itemValue="all" itemLabel="#{msgs.label_email_all_people}"/>                                              
		                        <f:selectItem id="only_organizers" itemValue="organizers_only" itemLabel="#{msgs.label_email_signed_up_ones_Organizers_only}" />	
		         		</h:selectOneRadio> 
					</h:panelGroup>
				</h:panelGrid>
			   	    
			   <h:dataTable id="preSignup" value="#{NewSignupMeetingBean.timeSlotWrappers}" var="timeSlot"
			   		rowClasses="oddTimeSlotRow,evenTimeSlotRow"	columnClasses="timeslotCol,orgMaxAttsCol,assignStudentsCol" styleClass="signupTable"  style="width: 55%"
			   		binding="#{NewSignupMeetingBean.timeslotWrapperTable}">
					<h:column>		   
						<f:facet name="header">
							<h:outputText value="#{msgs.tab_time_slot}"/>
						</f:facet>
						<h:panelGroup>
				   			<h:outputText value="#{timeSlot.timeSlot.startTime}" styleClass="longtext">
								<f:convertDateTime pattern="h:mm a"/>
							</h:outputText>
							<h:outputText value="#{timeSlot.timeSlot.startTime}" rendered="#{NewSignupMeetingBean.signupMeeting.meetingCrossDays}">
									<f:convertDateTime pattern=", EEE" />
							</h:outputText>
							<h:outputText value="#{msgs.timeperiod_divider}" escape="false"/>
							<h:outputText value="#{timeSlot.timeSlot.endTime}" styleClass="longtext">
								<f:convertDateTime pattern="h:mm a"/>
							</h:outputText>
							<h:outputText value="#{timeSlot.timeSlot.endTime}" rendered="#{NewSignupMeetingBean.signupMeeting.meetingCrossDays}">
									<f:convertDateTime pattern=", EEE, " />
							</h:outputText>
							<h:outputText value="#{timeSlot.timeSlot.endTime}" rendered="#{NewSignupMeetingBean.signupMeeting.meetingCrossDays}">
									<f:convertDateTime  dateStyle="short"/>
							</h:outputText>
						</h:panelGroup>		
			   		</h:column>
			   		
			   		<h:column rendered="#{!NewSignupMeetingBean.unlimited}">		   
						<f:facet name="header">
							<h:outputText value="#{msgs.tab_max_attendee}"/>
						</f:facet>
						<h:outputText value="#{timeSlot.timeSlot.maxNoOfAttendees}"/>
			   		</h:column>		   		
			   		<h:column rendered="#{NewSignupMeetingBean.unlimited}">		   
						<f:facet name="header">
							<h:outputText value="#{msgs.tab_max_attendee}"/>
						</f:facet>
						<h:outputText value="unlimited" escape="false"/>
			   		</h:column>
			   		
			   		<h:column>		   
						<f:facet name="header">
							<h:outputText value="#{msgs.tab_attendee}"/>
						</f:facet>
						<h:panelGrid columns="1" columnClasses="noWrapCol" style="margin-left:-25px;">
							<h:dataTable id="attendees" value="#{timeSlot.attendeeWrappers}" var="attendeeWrapper" columnClasses="noWrapCol">
								<h:column>
									<h:commandLink id="deleteAttendee" title="#{msgs.event_tool_tips_delete}" action="#{NewSignupMeetingBean.removeAttendee}" >
										<h:graphicImage value="/images/delete.png"  alt="delete" title="#{msgs.event_tool_tips_delete}" style="border:none" styleClass="openCloseImageIcon" />
					   						<f:param id="deletAttendeeUserId" name="#{NewSignupMeetingBean.attendeeUserId}" value="#{attendeeWrapper.signupAttendee.attendeeUserId}"/>
					   				</h:commandLink>
					   				<h:outputText value="&nbsp;" escape="false" />
									<h:outputText value="#{attendeeWrapper.displayName}"/>
								</h:column>
							</h:dataTable>
							<h:panelGroup id="addAttendee">
								<h:outputLabel onclick="showHideAddPanel('#{timeSlot.positionInTSlist}');" styleClass="addAttendee">
						   			<h:graphicImage value="/images/add.png"  alt="add an attendee" title="#{msgs.event_tool_tips_add}" styleClass="addButton" style="border:none" />
						   			<h:outputText value="#{msgs.event_add_attendee}" escape="false"/>
						   		</h:outputLabel>
						   	</h:panelGroup>
						   	
				   			<h:panelGroup id="addPanel" style="display: none;" >
			   					<h:panelGrid id="newAttendeeTable" columns="1">
		   							<h:panelGroup>
			   							<h:selectOneMenu  id="selectNewAttendee"  binding="#{NewSignupMeetingBean.newAttendeeInput}" rendered="#{!NewSignupMeetingBean.eidInputMode}">
		   									<f:selectItems value="#{NewSignupMeetingBean.allAttendees}" />
		   								</h:selectOneMenu>
		   								<h:outputText value="#{msgs.attendee_enterEid}" escape="false" rendered="#{NewSignupMeetingBean.eidInputMode}"/>
		   								<h:inputText id="addAttendeeEidInput" size="40" value="#{NewSignupMeetingBean.userInputEidOrEmail}" rendered="#{NewSignupMeetingBean.eidInputMode}" />
	   								</h:panelGroup>
	   								<h:panelGroup>
		   						    	<h:commandButton value="#{msgs.ok_button}" action="#{NewSignupMeetingBean.addAttendee}" />
		   								<h:commandButton value="#{msgs.cancel_button}" action="" onclick="clearPanel(); return false;" />
		   							</h:panelGroup>
		   						</h:panelGrid>
				   			</h:panelGroup>
				   		</h:panelGrid>
			   		</h:column>
			   </h:dataTable>
			   
						   
			    <sakai:doc_section>
					<h:panelGrid columns="2" styleClass="instruction">
				   		<h:outputText  value="#{msgs.organizer_note_name}" escape="false" />
				   		<h:outputText value="#{msgs.preAssign_note_backButton_message}" escape="false" />
				   		
				   		<h:outputText value="&nbsp;" escape="false"/>
				   		<h:outputText  value="#{msgs.preAssign_note_max_number_message}" escape="false" />
				   		
				   		<h:outputText value="&nbsp;" escape="false"/>
				   		<h:outputText  value="#{msgs.preAssign_note_publish_message}" escape="false" />
			   		</h:panelGrid>
			   </sakai:doc_section>
			   			  
			   
				<h:inputHidden value="assignAttendee" binding="#{NewSignupMeetingBean.currentStepHiddenInfo}"/>
				<sakai:button_bar>
					<sakai:button_bar_item id="publish" action="#{NewSignupMeetingBean.processAssignStudentsAndPublish}" value="#{msgs.publish_button}"/>
				 	<sakai:button_bar_item id="goBack" action="#{NewSignupMeetingBean.goBack}" value="#{msgs.goback_button}"/>
					<sakai:button_bar_item id="cancel" action="#{NewSignupMeetingBean.processCancel}" value="#{msgs.cancel_button}"  immediate="true"/>  
                </sakai:button_bar>

			 </h:form>
  		</sakai:view_content>	
	</sakai:view_container>
	<f:verbatim>
		<script>			
						
			var lastActivePanel;
			var lastClickedAddImage;
			var lastUserInputEid;
			var defaultColor='black';
			var predefinedByJSF = "meeting:preSignup:";//tag prefix-id form.name + datatable name
						
			
			
			function showHideAddPanel(timeslotId, attendeeIndex){				
				clearPanel();
				//hide addImage block
				lastClickedAddImage= document.getElementById(predefinedByJSF + timeslotId +":addAttendee");	
				lastClickedAddImage.style.display = "none";
				
				var addPanel = document.getElementById(predefinedByJSF + timeslotId +":addPanel");
    			addPanel.style.display = "block";   			   			
    			lastActivePanel=addPanel;    			
    			lastUserInputEid=document.getElementById(predefinedByJSF + timeslotId +":addAttendeeEidInput");
			}
															
			function clearPanel(){
				if(lastActivePanel)
					lastActivePanel.style.display = "none";				
				if (lastUserInputEid)		
					lastUserInputEid.value="";
				if (lastClickedAddImage)
					lastClickedAddImage.style.display = "block";
			}
			
			//Remove any "white space" (spaces, form feeds, line feeds, carriage returns, tabs, vertical tabs) before and trailing a string
			function trim(s){
				if((s==null)||(typeof(s)!='string')||!s.length)return'';return s.replace(/^\s+/,'').replace(/\s+$/,'')
				}

		</script>
	</f:verbatim>

</f:view> 
