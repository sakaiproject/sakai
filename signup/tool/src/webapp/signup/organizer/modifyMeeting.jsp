<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t" %>

<f:view locale="#{UserLocale.localeExcludeCountryForIE}">
	<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
	   <jsp:setProperty name="msgs" property="baseName" value="messages"/>
	</jsp:useBean>
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
							
							<h:outputText  value="" styleClass="titleText" escape="false" />
		         			<h:panelGrid columns="1">
		         				<t:dataTable value="#{EditMeetingSignupMBean.tempAttachmentCopyList}" var="attach" rendered="#{!EditMeetingSignupMBean.signupAttachmentEmpty}">
		         					<t:column>
        								<%@ include file="/signup/common/mimeIcon.jsp" %>
      								</t:column>
		         					<t:column>
		         						<h:outputLink  value="#{attach.location}" target="new_window">
		         							<h:outputText value="#{attach.filename}"/>
		         						</h:outputLink>
		         					</t:column>
		         					<t:column>
		         						<h:outputText escape="false" value="(#{attach.fileSize}kb)" rendered="#{!attach.isLink}"/>
		         					</t:column>
		         				</t:dataTable>
		         				
		         				<h:commandButton action="#{EditMeetingSignupMBean.addRemoveAttachments}" value="#{msgs.add_attachments}" rendered="#{EditMeetingSignupMBean.signupAttachmentEmpty}"/>		
		         				<h:commandButton action="#{EditMeetingSignupMBean.addRemoveAttachments}" value="#{msgs.add_remove_attachments}" rendered="#{!EditMeetingSignupMBean.signupAttachmentEmpty}"/>		         			
		         			</h:panelGrid>
							
							<h:outputText id="rescheduleWarnLabel_1" value="" escape="false" style="display:none;" rendered="#{EditMeetingSignupMBean.someoneSignedUp}"/>
							<h:outputText id="rescheduleWarnLabel_2" value="#{msgs.warn_reschedule_event}" styleClass="alertMessage" style="display:none;width:95%" escape="false" rendered="#{EditMeetingSignupMBean.someoneSignedUp}"/>
								
							<h:panelGroup styleClass="titleText" style="margin-top: 20px;">
								<h:outputText value="#{msgs.star_character}" style="color:#B11;"/>
								<h:outputText value="#{msgs.event_start_time}"  escape="false"/>
							</h:panelGroup>	
							<h:panelGroup styleClass="editText">
		        						<t:inputDate id="startTime" type="both"  ampm="true" value="#{EditMeetingSignupMBean.signupMeeting.startTime}"
		        							style="color:black;" popupCalendar="true" onfocus="showRescheduleWarning();" onkeyup="setEndtimeMonthDateYear();getSignupDuration();return false;"/>
										<h:message for="startTime" errorClass="alertMessageInline"/>
							</h:panelGroup>
							
							<h:panelGroup styleClass="titleText">
								<h:outputText value="#{msgs.star_character}" style="color:#B11;" />					
								<h:outputText value="#{msgs.event_end_time}" escape="false"/>
							</h:panelGroup>
		        			<h:panelGroup styleClass="editText">
		        						<t:inputDate id="endTime" type="both" ampm="true" value="#{EditMeetingSignupMBean.signupMeeting.endTime}" style="color:black;" popupCalendar="true" 
		        						onfocus="showRescheduleWarning();" 	 onkeyup="getSignupDuration();return false;"/>
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
				            <h:panelGrid columns="2" columnClasses="miCol1,miCol2">                
					                   <h:panelGroup id="radios" styleClass="rs">                  
					                        <h:selectOneRadio id="meetingType" value="#{EditMeetingSignupMBean.signupMeeting.meetingType}"    layout="pageDirection" styleClass="rs" >
					                          	<f:selectItems value="#{EditMeetingSignupMBean.meetingTypeRadioBttns}"/>              	                      	         	 
					                 	   </h:selectOneRadio> 
					                   </h:panelGroup>
				                      
					               	   <h:panelGrid columns="1" columnClasses="miCol1">       
					               			<%-- multiple: --%>           
						               				<h:panelGroup rendered="#{EditMeetingSignupMBean.individualType}">            
								                        	<h:panelGrid columns="2" styleClass="mi" columnClasses="miCol1,miCol2">                                                
											                        <h:outputText value="#{msgs.event_num_slot_avail_for_signup}" />
												                    <h:inputText  id="numberOfSlot" value="#{EditMeetingSignupMBean.numberOfSlots}" size="2" styleClass="editText" onfocus="showRescheduleWarning();" onkeyup="getSignupDuration(); delayedValidMimimunTs('#{EditMeetingSignupMBean.numberOfSlots}','#{msgs.event_warning_no_lower_than_cur_ts_num}'); return false;" style="margin-left:12px" />
											                        <h:outputText value="#{msgs.event_num_participant_per_timeslot}" styleClass="titleText" escape="false"/>                    
													                <h:inputText id="numberOfAttendees" value="#{EditMeetingSignupMBean.maxNumOfAttendees}" styleClass="editText" size="2" style="margin-left:12px" onkeyup="validateAttendee();return false;" />
											                    	<h:outputText value="#{msgs.event_duration_each_timeslot_not_bold}" styleClass="titleText" escape="false"/>
																	<h:inputText id='currentTimeslotDuration' value="0" styleClass='longtext' size="2" onkeyup="this.blur();" onmouseup="this.blur();" style="margin-left:12px;color:#b11" />             
								                			</h:panelGrid>          
						                         
						                			</h:panelGroup>                                
					               
						            				<%-- single: --%>
						               
								                	<h:panelGroup rendered="#{EditMeetingSignupMBean.groupType}">                
									                        <h:panelGrid columns="2" rendered="true" styleClass="si" columnClasses="miCol1,miCol2">                
												                    <h:selectOneRadio id="groupSubradio" value="#{EditMeetingSignupMBean.unlimited}"  onclick="switchSingle(value)" styleClass="meetingRadioBtn" layout="pageDirection" >
												                        <f:selectItem itemValue="#{false}" itemLabel="#{msgs.tab_max_attendee}"/>                    
												                        <f:selectItem itemValue="#{true}" itemLabel="#{msgs.unlimited_num_attendee}"/>                            
												                    </h:selectOneRadio>
												                                    
										                   			<h:panelGrid columns="1" columnClasses="miCol1">       
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
												
							<h:outputText value="&nbsp;" escape="false"/>
							<h:outputText value="&nbsp;" escape="false"/>
							
							<h:outputText value="#{msgs.event_other_default_setting}" escape="false" styleClass="titleText"/>
							<h:panelGroup >	
			   	    				<h:outputLabel  id="imageOpen_otherSetting" style="display:none" styleClass="activeTag" onclick="showDetails('meeting:imageOpen_otherSetting','meeting:imageClose_otherSetting','meeting:otherSetting');">
				   	    				<h:graphicImage value="/images/open.gif"  alt="open" title="Click to hide details." style="border:none;" styleClass="openCloseImageIcon"/>
				   	    				<h:outputText value="#{msgs.event_close_other_default_setting}" escape="false" style="vertical-align: top;"/>
			   	    				</h:outputLabel>
			   	    				<h:outputLabel id="imageClose_otherSetting" styleClass="activeTag" onclick="showDetails('meeting:imageOpen_otherSetting','meeting:imageClose_otherSetting','meeting:otherSetting');">
			   	    					<h:graphicImage value="/images/closed.gif" alt="close" title="Click to show details." style="border:none;vertical-align:top;" styleClass="openCloseImageIcon"/>
			   	    					<h:outputText value="#{msgs.event_show_other_default_setting}" escape="false" style="vertical-align: top;"/>
			   	    				</h:outputLabel>
						   </h:panelGroup>

							<h:outputText id="otherSetting_1" style="display:none"  value="#{msgs.event_allow_waitList}" styleClass="titleText" escape="false" rendered="#{!EditMeetingSignupMBean.announcementType}"/>
							<h:panelGroup id="otherSetting_2" style="display:none"  styleClass="longtext" rendered="#{!EditMeetingSignupMBean.announcementType}">
								<h:selectBooleanCheckbox value="#{EditMeetingSignupMBean.signupMeeting.allowWaitList}"/>
								<h:outputText value="#{msgs.event_yes_to_allow_waitList}" escape="false"/>
							</h:panelGroup>
						
							<h:outputText id="otherSetting_3" style="display:none"  value="#{msgs.event_allow_addComment}" styleClass="titleText" escape="false" rendered="#{!EditMeetingSignupMBean.announcementType}"/>
							<h:panelGroup id="otherSetting_4" style="display:none"  styleClass="longtext" rendered="#{!EditMeetingSignupMBean.announcementType}">
								<h:selectBooleanCheckbox value="#{EditMeetingSignupMBean.signupMeeting.allowComment}"/>
								<h:outputText value="#{msgs.event_yes_to_allow_addComment}" escape="false"/>
							</h:panelGroup>
							
							<h:outputText id="otherSetting_5" style="display:none"  value="#{msgs.event_use_eid_input_mode}" styleClass="titleText" escape="false" rendered="#{!EditMeetingSignupMBean.announcementType && EditMeetingSignupMBean.userIdInputModeOptionChoice}"/>
							<h:panelGroup id="otherSetting_6" style="display:none"  styleClass="longtext" rendered="#{!EditMeetingSignupMBean.announcementType && EditMeetingSignupMBean.userIdInputModeOptionChoice}">
								<h:selectBooleanCheckbox value="#{EditMeetingSignupMBean.signupMeeting.eidInputMode}"/>
								<h:outputText value="#{msgs.event_yes_to_use_eid_input_mode}" escape="false"/>
							</h:panelGroup>  
							
							<h:outputText id="otherSetting_7" style="display:none" value="#{msgs.event_email_autoReminder}" styleClass="titleText" escape="false"  rendered="#{!EditMeetingSignupMBean.announcementType && EditMeetingSignupMBean.autoReminderOptionChoice}"/>
							<h:panelGroup id="otherSetting_8" style="display:none" styleClass="longtext" rendered="#{!EditMeetingSignupMBean.announcementType && EditMeetingSignupMBean.autoReminderOptionChoice}">
								<h:selectBooleanCheckbox value="#{EditMeetingSignupMBean.signupMeeting.autoReminder}"/>
								<h:outputText value="#{msgs.event_yes_email_autoReminer_to_attendees}" escape="false"/>
							</h:panelGroup>
							
						</h:panelGrid>
				</h:panelGrid>
					
										
				<sakai:button_bar>
					<h:commandButton id="goNextPage" action="#{EditMeetingSignupMBean.processSaveModify}" actionListener="#{EditMeetingSignupMBean.validateModifyMeeting}" value="#{msgs.public_modify_button}"/> 			
					<h:commandButton id="Cancel" action="#{EditMeetingSignupMBean.doCancelAction}" value="#{msgs.cancel_button}" />  
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
			
			var recurWarnTag1 = document.getElementById('meeting:rescheduleWarnLabel_1');
	        var recurWarnTag2 = document.getElementById('meeting:rescheduleWarnLabel_2');
			function showRescheduleWarning(){
	        	if(recurWarnTag1 && recurWarnTag2){	        	
		        	recurWarnTag1.style.display="";
		        	recurWarnTag2.style.display="";     		
        		}
	        }
			
		</script>
	</f:verbatim>
	
</f:view> 
