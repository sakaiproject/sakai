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
	
	
	
	  <script TYPE="text/javascript" LANGUAGE="JavaScript" src="/sakai-signup-tool/js/signupScript.js"></script>
	  <script TYPE="text/javascript" LANGUAGE="JavaScript" src="/sakai-signup-tool/js/jquery.js"></script>
		
		<sakai:view_content>
			<h:outputText value="#{msgs.event_error_alerts} #{errorMessageUIBean.errorMessage}" styleClass="alertMessage" escape="false" rendered="#{errorMessageUIBean.error}"/>      			
				
			<h:form id="meeting">
			 	<sakai:view_title value="#{msgs.event_step5_page_title}"/>

				<sakai:messages />
				
				<h:panelGrid columns="1">
					
						<h:panelGrid columns="2"  columnClasses="titleColumn,valueColumn">
								<h:outputText value="#{msgs.event_name}" styleClass="titleText" escape="false"/>
								<h:panelGroup>
									<h:panelGroup rendered="#{NewSignupMeetingBean.recurrence}">
										<h:graphicImage title="#{msgs.event_tool_tips_recurrence}" value="/images/recurrence.gif"  alt="recurrence" style="border:none" />
										<h:outputText value="&nbsp;" escape="false"/>
									</h:panelGroup>
									<h:outputText value="#{NewSignupMeetingBean.signupMeeting.title}" styleClass="longtext" escape="false"/>
								</h:panelGroup>
								
								<h:outputText value="#{msgs.event_description}" styleClass="titleText" escape="false"/>
								<h:outputText value="#{NewSignupMeetingBean.signupMeeting.description}" styleClass="longtext" escape="false"/>
								
								<h:outputText  value="#{msgs.attachments}" styleClass="titleText" escape="false" rendered="#{!NewSignupMeetingBean.attachmentsEmpty}"/>
			         			<h:panelGrid columns="1" rendered="#{!NewSignupMeetingBean.attachmentsEmpty}">
			         				<t:dataTable value="#{NewSignupMeetingBean.attachments}" var="attach" >
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
				         		</h:panelGrid>
								
								<h:outputText value="#{msgs.event_start_time}" styleClass="titleText" escape="false"/>
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
								
								<h:outputText value="#{msgs.event_end_time}" styleClass="titleText" escape="false"/>
								<h:panelGroup rendered="#{!NewSignupMeetingBean.endTimeAutoAdjusted}">
									<h:outputText value="#{NewSignupMeetingBean.meetingEndTime}" styleClass="longtext">
										<f:convertDateTime pattern="EEEEEEEE, " />
									</h:outputText>
									<h:outputText value="#{NewSignupMeetingBean.meetingEndTime}" styleClass="longtext">
										<f:convertDateTime dateStyle="long" />
									</h:outputText>
									<h:outputText value="#{NewSignupMeetingBean.meetingEndTime}" styleClass="longtext">
										<f:convertDateTime pattern=", h:mm a" />
									</h:outputText>
								</h:panelGroup>
								<h:panelGroup rendered="#{NewSignupMeetingBean.endTimeAutoAdjusted}">
									<h:outputText value="#{NewSignupMeetingBean.meetingEndTime}" style="color:#b11;" styleClass="longtext">
										<f:convertDateTime pattern="EEEEEEEE, " />
									</h:outputText>
									<h:outputText value="#{NewSignupMeetingBean.meetingEndTime}" style="color:#b11;" styleClass="longtext">
										<f:convertDateTime dateStyle="long" />
									</h:outputText>
									<h:outputText value="#{NewSignupMeetingBean.meetingEndTime}" style="color:#b11;" styleClass="longtext">
										<f:convertDateTime pattern=", h:mm a" />
									</h:outputText>
								</h:panelGroup>
								
								
								<h:outputText styleClass="titleText" value="#{msgs.event_recurrence}"  rendered="#{NewSignupMeetingBean.recurrence}" escape="false"/> 
								<h:outputText value="#{NewSignupMeetingBean.eventFreqType}" rendered="#{NewSignupMeetingBean.recurrence}" escape="false"/>
								 
								<h:outputText value="#{msgs.event_repeat_until}" styleClass="titleText"  rendered="#{NewSignupMeetingBean.recurrence}" escape="false"/>
								<h:outputText value="#{NewSignupMeetingBean.repeatUntil}" rendered="#{NewSignupMeetingBean.recurrence}">
									<f:convertDateTime dateStyle="full" />
								</h:outputText>
								
								
								<h:outputText value="#{msgs.event_location}" styleClass="titleText" escape="false"/>
								<h:outputText value="#{NewSignupMeetingBean.signupMeeting.location}" styleClass="longtext" escape="false"/>
								
								<h:outputText value="#{msgs.event_signup_start}" styleClass="titleText" rendered="#{!NewSignupMeetingBean.announcementType}" escape="false"/>
								<h:panelGroup rendered="#{!NewSignupMeetingBean.announcementType}">
									<h:outputText value="#{NewSignupMeetingBean.signupBeginInDate}" styleClass="longtext">
										<f:convertDateTime pattern="EEEEEEEE, " />
									</h:outputText>
									<h:outputText value="#{NewSignupMeetingBean.signupBeginInDate}" styleClass="longtext">
										<f:convertDateTime dateStyle="long" />
									</h:outputText>
									<h:outputText value="#{NewSignupMeetingBean.signupBeginInDate}" styleClass="longtext">
										<f:convertDateTime pattern=", h:mm a" />
									</h:outputText>
								</h:panelGroup>
								
								<h:outputText value="#{msgs.event_signup_deadline}" styleClass="titleText" rendered="#{!NewSignupMeetingBean.announcementType}" escape="false"/>
								<h:panelGroup rendered="#{!NewSignupMeetingBean.announcementType}">
									<h:outputText value="#{NewSignupMeetingBean.signupDeadlineInDate}" styleClass="longtext">
										<f:convertDateTime pattern="EEEEEEEE, " />
									</h:outputText>
									<h:outputText value="#{NewSignupMeetingBean.signupDeadlineInDate}" styleClass="longtext">
										<f:convertDateTime dateStyle="long" />
									</h:outputText>
									<h:outputText value="#{NewSignupMeetingBean.signupDeadlineInDate}" styleClass="longtext">
										<f:convertDateTime pattern=", h:mm a" />
									</h:outputText>
								</h:panelGroup>
								
								<h:outputText value="&nbsp;" escape="false"/>
								<h:outputText value="&nbsp;" escape="false"/>
								
								<h:outputText value="#{msgs.event_type_title}" styleClass="titleText"/>
								<h:outputText value="#{NewSignupMeetingBean.displayCurrentMeetingType}" styleClass="longtext" escape="false"/>
								
								<h:outputText value="#{msgs.event_num_timeslot}" styleClass="titleText" rendered="#{NewSignupMeetingBean.individualType}" escape="false"/>
								<h:outputText value="#{NewSignupMeetingBean.numberOfSlots}" styleClass="longtext" rendered="#{NewSignupMeetingBean.individualType}" escape="false"/>																																								
							
								<h:outputText value="#{msgs.event_duration_each_timeslot}" styleClass="titleText" rendered="#{NewSignupMeetingBean.individualType}" escape="false"/>
								<h:outputText value="#{NewSignupMeetingBean.timeSlotDuration}" styleClass="longtext" rendered="#{NewSignupMeetingBean.individualType}" escape="false"/>																							
							
								<h:outputText value="#{msgs.event_max_attendee_each_timeslot}" styleClass="titleText" rendered="#{NewSignupMeetingBean.individualType}" escape="false"/>
								<h:outputText value="#{NewSignupMeetingBean.numberOfAttendees}" styleClass="longtext" rendered="#{NewSignupMeetingBean.individualType}" escape="false"/>																																						
							
								
								<h:outputText value="#{msgs.event_max_attendees}" styleClass="titleText" rendered="#{NewSignupMeetingBean.groupType}" escape="false"/>
								<h:panelGroup rendered="#{NewSignupMeetingBean.groupType}" >
									<h:outputText id="maxAttendee" value="#{NewSignupMeetingBean.maxOfAttendees}" rendered="#{!NewSignupMeetingBean.unlimited}" styleClass="longtext"/>
									<h:outputText value="#{msgs.event_unlimited}" rendered="#{NewSignupMeetingBean.unlimited}" styleClass="longtext"/>					
								</h:panelGroup>
								
								<h:outputText value="#{msgs.event_publish_to}" escape="false"  styleClass="titleText"/>
								<h:dataTable id="userSites" value="#{NewSignupMeetingBean.selectedSignupSites}" var="site"  styleClass="sitegroup">
									<h:column>
										<h:outputText value="#{site.title} #{msgs.event_site_level}" rendered="#{site.siteScope}" styleClass="sitetitle" escape="false"/>
										<h:panelGroup rendered="#{!site.siteScope}">
											<h:outputText value="#{site.title} #{msgs.event_group_level}" styleClass="sitetitle" escape="false"/>
											<h:dataTable id="userGroups" value="#{site.signupGroups}" var="group" styleClass="sitegroup">
												<h:column>
														<h:outputText value=" - #{group.title}" escape="false" styleClass="grouptitle"/>
												</h:column>
											</h:dataTable>
										</h:panelGroup>							
									</h:column>
								</h:dataTable>
																
								<h:outputText value="&nbsp;" escape="false"/>
								<h:outputText value="&nbsp;" escape="false"/>
								
								<h:outputText value="#{msgs.event_publish_attendee_name}" styleClass="titleText" escape="false"/>
								<h:panelGroup styleClass="longtext">
									<h:selectBooleanCheckbox value="#{NewSignupMeetingBean.showParticipants}"/>
									<h:outputText value="#{msgs.event_yes_show_attendee_public}" escape="false"/>
								</h:panelGroup>
								
								<h:outputText value="#{msgs.event_receive_notification}" styleClass="titleText" escape="false"/>
								<h:panelGroup styleClass="longtext">
									<h:selectBooleanCheckbox value="#{NewSignupMeetingBean.receiveEmail}"/>
									<h:outputText value="#{msgs.event_yes_receive_notification}" escape="false"/>
								</h:panelGroup>
								
								<h:outputText value="#{msgs.event_email_notification}" styleClass="titleText" escape="false"/>
								<h:panelGroup styleClass="longtext" rendered="#{NewSignupMeetingBean.publishedSite}">
									<h:selectBooleanCheckbox value="#{NewSignupMeetingBean.sendEmail}"/>
									<h:outputText value="#{msgs.event_yes_email_notification}" escape="false"/>
								</h:panelGroup>
								<h:panelGroup styleClass="longtext" rendered="#{!NewSignupMeetingBean.publishedSite}">
									<h:selectBooleanCheckbox value="#{NewSignupMeetingBean.sendEmail}" disabled="true"/>
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

							   	<h:outputText id="otherSetting_1" style="display:none" value="#{msgs.event_allow_waitList}" styleClass="titleText" escape="false" rendered="#{!NewSignupMeetingBean.announcementType}"/>
								<h:panelGroup id="otherSetting_2" style="display:none" styleClass="longtext" rendered="#{!NewSignupMeetingBean.announcementType}">
									<h:selectBooleanCheckbox value="#{NewSignupMeetingBean.allowWaitList}"/>
									<h:outputText value="#{msgs.event_yes_to_allow_waitList}" escape="false"/>
								</h:panelGroup>
								
								<h:outputText id="otherSetting_3" style="display:none" value="#{msgs.event_allow_addComment}" styleClass="titleText" escape="false" rendered="#{!NewSignupMeetingBean.announcementType}"/>
								<h:panelGroup id="otherSetting_4" style="display:none" styleClass="longtext" rendered="#{!NewSignupMeetingBean.announcementType}">
									<h:selectBooleanCheckbox value="#{NewSignupMeetingBean.allowComment}"/>
									<h:outputText value="#{msgs.event_yes_to_allow_addComment}" escape="false"/>
								</h:panelGroup>
								
								<h:outputText id="otherSetting_5" style="display:none" value="#{msgs.event_use_eid_input_mode}" styleClass="titleText" escape="false" rendered="#{!NewSignupMeetingBean.announcementType && NewSignupMeetingBean.userIdInputModeOptionChoice}"/>
								<h:panelGroup id="otherSetting_6" style="display:none" styleClass="longtext" rendered="#{!NewSignupMeetingBean.announcementType && NewSignupMeetingBean.userIdInputModeOptionChoice}">
									<h:selectBooleanCheckbox value="#{NewSignupMeetingBean.eidInputMode}"/>
									<h:outputText value="#{msgs.event_yes_to_use_eid_input_mode}" escape="false"/>
								</h:panelGroup>
								
								<h:outputText id="otherSetting_7" style="display:none" value="#{msgs.event_email_autoReminder}" styleClass="titleText" escape="false"  rendered="#{!NewSignupMeetingBean.announcementType && NewSignupMeetingBean.autoReminderOptionChoice}"/>
								<h:panelGroup id="otherSetting_8" style="display:none" styleClass="longtext" rendered="#{!NewSignupMeetingBean.announcementType && NewSignupMeetingBean.autoReminderOptionChoice}">
									<h:selectBooleanCheckbox value="#{NewSignupMeetingBean.autoReminder}"/>
									<h:outputText value="#{msgs.event_yes_email_autoReminer_to_attendees}" escape="false"/>
								</h:panelGroup>
								
						</h:panelGrid>
						
						<h:outputText value="&nbsp;" escape="false"/>
						
				</h:panelGrid>
					
										
				<h:inputHidden value="step2" binding="#{NewSignupMeetingBean.currentStepHiddenInfo}"/>
				<sakai:button_bar>
					<sakai:button_bar_item id="goNextPage" action="#{NewSignupMeetingBean.processSave}" value="#{msgs.publish_button}"/> 
					<sakai:button_bar_item id="assignStudents" action="#{NewSignupMeetingBean.proceesPreAssignAttendee}" value="#{msgs.assign_attendee_publish_button}" disabled="#{NewSignupMeetingBean.announcementType}"/> 
					<sakai:button_bar_item id="goBack" action="#{NewSignupMeetingBean.goBack}" value="#{msgs.goback_button}"/>
					<sakai:button_bar_item id="Cancel" action="#{NewSignupMeetingBean.processCancel}" value="#{msgs.cancel_button}" immediate="true"/>  
                </sakai:button_bar>

			 </h:form>
  		</sakai:view_content>	
	</sakai:view_container>
	
	<f:verbatim>
	<script>
			//just introduce jquery slideUp/Down visual effect to overwrite top function
			function switchShowOrHide(tag){
				if(tag){
					if(tag.style.display=="none")
						$(tag).slideDown("fast");
					else
						$(tag).slideUp("fast");
				}
			}			
						
		</script>
	</f:verbatim>
</f:view> 
