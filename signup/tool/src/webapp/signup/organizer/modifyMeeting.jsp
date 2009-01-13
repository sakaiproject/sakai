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
						<h:panelGrid columns="2" columnClasses="titleColumn,valueColumn" onmouseover="delayedRecalculateDateTime();">
							<h:panelGroup styleClass="titleText" rendered="#{EditMeetingSignupMBean.signupMeeting.recurredMeeting}">
								<h:outputText value="#{msgs.star_character}" style="color:#B11;"/>
								<h:outputText value="#{msgs.event_modify_option}" escape="false"/>
							</h:panelGroup>			
							 <h:selectOneRadio  value="#{EditMeetingSignupMBean.convertToNoRecurrent}" layout="pageDirection" styleClass="rs" rendered="#{EditMeetingSignupMBean.signupMeeting.recurredMeeting}">
					                          <f:selectItem id="modify_all" itemValue="#{false}" itemLabel="#{msgs.modify_all}"/>                                              
					                          <f:selectItem id="modify_current" itemValue="#{true}" itemLabel="#{msgs.modify_current}"/>					                                  	                      	         	 
					         </h:selectOneRadio> 
						
							<h:panelGroup styleClass="titleText">
								<h:outputText value="#{msgs.star_character}" style="color:#B11;"/>
								<h:outputText value="#{msgs.event_name}"  escape="false"/>
							</h:panelGroup>
							<h:panelGroup>
								<h:inputText id="title" value="#{EditMeetingSignupMBean.signupMeeting.title}" required="true" size="40" styleClass="editText">
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
								<h:inputText id="location" value="#{EditMeetingSignupMBean.signupMeeting.location}" required="true" size="40" styleClass="editText">
									<f:validator validatorId="Signup.EmptyStringValidator"/>
								</h:inputText>														
								<h:message for="location" errorClass="alertMessageInline"/>
							</h:panelGroup>
							
							<h:outputText value="#{msgs.event_description}" styleClass="titleText" escape="false"/>
							<sakai:rich_text_area value="#{EditMeetingSignupMBean.signupMeeting.description}" width="720" height="200" rows="5"  columns="80"/>
								
							<h:panelGroup styleClass="titleText" style="margin-top: 20px;">
								<h:outputText value="#{msgs.star_character}" style="color:#B11;"/>
								<h:outputText value="#{msgs.event_start_time}"  escape="false"/>
							</h:panelGroup>	
							<h:panelGroup styleClass="editText">
		        						<t:inputDate id="startTime" type="both"  ampm="true" value="#{EditMeetingSignupMBean.signupMeeting.startTime}"
		        							style="color:black;" popupCalendar="true" onkeyup="setEndtimeMonthDateYear();getSignupDuration();return false;" 
		        							onchange="setEndtimeMonthDateYear(); getSignupDuration(); return false;"/>
										<h:message for="startTime" errorClass="alertMessageInline"/>
							</h:panelGroup>
							
							<h:panelGroup styleClass="titleText">
								<h:outputText value="#{msgs.star_character}" style="color:#B11;" />					
								<h:outputText value="#{msgs.event_end_time}" escape="false"/>
							</h:panelGroup>
		        			<h:panelGroup styleClass="editText">
		        						<t:inputDate id="endTime" type="both" ampm="true" value="#{EditMeetingSignupMBean.signupMeeting.endTime}" style="color:black;" popupCalendar="true" 
		        							 onkeyup="getSignupDuration();return false;"
		        							onchange="getSignupDuration(); return false;" />
										<h:message for="endTime" errorClass="alertMessageInline"/>
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
							
							<%-- handle meeting types --%>
				           	<h:panelGroup styleClass="titleText">
				           			<h:outputText value="#{msgs.star_character}"  style="color:#B11;"/>
				            		<h:outputText value ="#{msgs.event_type_title}" />
				           	</h:panelGroup>	
				            <h:panelGrid columns="2" style="vertical-align: top;">                
					                   <h:panelGroup id="radios" styleClass="rs">                  
					                        <h:selectOneRadio id="meetingType" value="#{EditMeetingSignupMBean.signupMeeting.meetingType}"    layout="pageDirection" styleClass="rs" >
					                          	<f:selectItems value="#{EditMeetingSignupMBean.meetingTypeRadioBttns}"/>              	                      	         	 
					                 	   </h:selectOneRadio> 
					                   </h:panelGroup>
				                      
					               	   <h:panelGrid columns="1" style="vertical-align: top;">       
					               			<%-- multiple: --%>           
						               				<h:panelGroup style="vertical-align: top;" rendered="#{EditMeetingSignupMBean.individualType}">            
								                        	<h:panelGrid columns="2" styleClass="mi">                                                
											                        <h:outputText value="#{msgs.event_num_slot_avail_for_signup}" />
												                    <h:inputText  id="numberOfSlot" value="#{EditMeetingSignupMBean.numberOfSlots}" size="2" styleClass="editText" onkeyup="getSignupDuration(); delayedValidMimimunTs('#{EditMeetingSignupMBean.numberOfSlots}','#{msgs.event_warning_no_lower_than_cur_ts_num}'); return false;" style="margin-left:12px" />
											                        <h:outputText value="#{msgs.event_num_participant_per_timeslot}" styleClass="titleText" escape="false"/>                    
													                <h:inputText id="numberOfAttendees" value="#{EditMeetingSignupMBean.maxNumOfAttendees}" styleClass="editText" size="2" style="margin-left:12px" onkeyup="validateAttendee();return false;" />
											                    	<h:outputText value="#{msgs.event_duration_each_timeslot_not_bold}" styleClass="titleText" escape="false"/>
																	<h:inputText id='currentTimeslotDuration' value="0" styleClass='longtext' size="2" onkeyup="this.blur();" onmouseup="this.blur();" style="margin-left:12px;color:#b11" />             
								                			</h:panelGrid>          
						                         
						                			</h:panelGroup>                                
					               
						            				<%-- single: --%>
						               
								                	<h:panelGroup style="vertical-align: top;" rendered="#{EditMeetingSignupMBean.groupType}">                
									                        <h:panelGrid columns="2" rendered="true" styleClass="si">                
												                    <h:selectOneRadio id="groupSubradio" value="#{EditMeetingSignupMBean.unlimited}"  onclick="switchSingle(value)" styleClass="meetingRadioBtn" layout="pageDirection" >
												                        <f:selectItem itemValue="#{false}" itemLabel="#{msgs.tab_max_attendee}"/>                    
												                        <f:selectItem itemValue="#{true}" itemLabel="#{msgs.unlimited_num_attendee}"/>                            
												                    </h:selectOneRadio>
												                                    
										                   			<h:panelGrid columns="1">       
												                     	<h:panelGroup  styleClass="meetingMaxAttd">                      
												                            <h:inputText id="maxAttendee" value="#{EditMeetingSignupMBean.maxNumOfAttendees}" size="2" styleClass="editText" onkeyup="validateParticipants();return false;"/>	                                 
												                        </h:panelGroup>
										                        		<h:outputText value="&nbsp;" styleClass="titleText" escape="false"/>
										                    		</h:panelGrid>
									                		</h:panelGrid>                    
								                	</h:panelGroup>
							                                       
					                 				<h:outputText id="announ" value="&nbsp;" rendered="#{EditMeetingSignupMBean.announcementType}" styleClass="titleText" escape="false"/>
					              		</h:panelGrid>
				              
				              </h:panelGrid>    
							
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
							<h:panelGroup styleClass="editText" rendered="#{EditMeetingSignupMBean.publishedSite}">
								<h:selectBooleanCheckbox value="#{EditMeetingSignupMBean.sendEmail}"/>
								<h:outputText value="#{msgs.event_yes_email_notification}" escape="false"/>
							</h:panelGroup>
							<h:panelGroup styleClass="editText" rendered="#{!EditMeetingSignupMBean.publishedSite}">
								<h:selectBooleanCheckbox value="#{EditMeetingSignupMBean.sendEmail}" disabled="true"/>
								<h:outputText value="#{msgs.event_email_not_send_out_label}" escape="false" style="color:#b11"/>
							</h:panelGroup>
						</h:panelGrid>
				</h:panelGrid>
					
										
				<sakai:button_bar>
					<h:commandButton id="goNextPage" action="#{EditMeetingSignupMBean.processSaveModify}" actionListener="#{EditMeetingSignupMBean.validateModifyMeeting}" value="#{msgs.public_modify_button}"/> 			
					<h:commandButton id="Cancel" action="organizerMeeting" value="#{msgs.cancel_button}"  immediate="true"/>  
                </sakai:button_bar>

			 </h:form>
  		</sakai:view_content>	
	</sakai:view_container>
	
	<f:verbatim>
		<script>
			//for IE browser, it does nothing.
			initGroupTypeRadioButton();
			replaceCalendarImageIcon(); 
			
			var wait=false; 
			var originalTsNumVal = 4;//default
			var warningMsgs="You may not decrease the number of time slots below the original value:";
			function delayedValidMimimunTs(originalTsNum,warningMsg){
				originalTsNumVal = parseInt(originalTsNum);
				warningMsgs=warningMsg;
				if (!wait){
					wait = true;
				  	setTimeout("validateMimTs();wait=false;", 3000);//3 sec
				}	
			}
			
			function validateMimTs(){
				var slotNumTag = document.getElementById("meeting:numberOfSlot");
				if (!slotNumTag || slotNumTag.value.length == 0)
 						return;	 						
 						
				if(slotNumTag.value < originalTsNumVal){
					alert(warningMsgs +" " + originalTsNumVal);
					slotNumTag.value = originalTsNumVal;
					}			
			}
			
		</script>
	</f:verbatim>
	
</f:view> 
