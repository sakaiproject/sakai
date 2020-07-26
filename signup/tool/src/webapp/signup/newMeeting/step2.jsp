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
	
<h:outputText value="#{Portal.latestJQuery}" escape="false"/>
	  <script src="/sakai-signup-tool/js/signupScript.js"></script>
	  <script>

			function a11yClick(event){
				if(event.type === 'click'){
					return true;
				}
				else if(event.type === 'keypress'){
					var code = event.charCode || event.keyCode;
					if((code === 32)|| (code === 13)){
						return true;
					}
				}
				else{
					return false;
				}
			}

			jQuery(document).ready(function() {
				$('#meeting\\:imageOpen_otherSetting, #meeting\\:imageClose_otherSetting').attr('tabindex', '0');
				$('#meeting\\:imageOpen_otherSetting, #meeting\\:imageClose_otherSetting').on('click keypress', function() {
					if(a11yClick(event) === true){
						showOtherDefaultSettings('meeting:imageOpen_otherSetting','meeting:imageClose_otherSetting','meeting:otherSetting');
					}
				});

				isShowEmailChoice();
				var menuLink = $('#signupAddMeetingMenuLink');
				menuLink.addClass('current');
				menuLink.html(menuLink.find('a').text());
			});

			//just introduce jquery slideUp/Down visual effect to overwrite top function
			function switchShowOrHide(tag){
				if(tag){
					if(tag.style.display=="none")
						jQuery(tag).slideDown("fast");
					else
						jQuery(tag).slideUp("fast");
				}
			}	
	
			function showOtherDefaultSettings(imageFolderName1,imagefolderName2,ContentTagName){
				var detailsTag = document.getElementById(ContentTagName);
				switchShowOrHide(detailsTag);
				
				var image1 = document.getElementById(imageFolderName1);
				switchShowOrHide(image1);
				
				var image2 = document.getElementById(imagefolderName2);
				switchShowOrHide(image2);

				var otherDefaultSettings = document.getElementById("otherDefaultSettings");
				switchShowOrHide(otherDefaultSettings);
			}



		</script>
	  
		
		<sakai:view_content>
			<h:form id="meeting">
				<%@ include file="/signup/menu/signupMenu.jsp" %>
				<h:outputText value="#{msgs.event_error_alerts} #{messageUIBean.errorMessage}" styleClass="alertMessage" escape="false" rendered="#{messageUIBean.error}"/>
				<h:outputText value="#{messageUIBean.infoMessage}" styleClass="information" escape="false" rendered="#{messageUIBean.info}"/>
				<div class="page-header">
			 		<sakai:view_title value="#{msgs.event_step5_page_title}"/>
				</div>
			 	<div class="form">
					<%-- title --%>
					<div class="row">
						<h:panelGroup styleClass="col-xs-12 col-md-3 titleColumn" layout="block">
							<h:outputText value="#{msgs.event_name}" styleClass="titleText" escape="false"/>
						</h:panelGroup>
						<h:panelGroup styleClass="col-xs-12 col-md-9 valueColumn" layout="block">
							<h:panelGroup>
								<h:panelGroup rendered="#{NewSignupMeetingBean.recurrence}">
									<h:graphicImage title="#{msgs.event_tool_tips_recurrence}" value="/images/recurrence.gif"  alt="recurrence" style="border:none" />
									<h:outputText value="&nbsp;" escape="false"/>
								</h:panelGroup>
								<h:outputText value="#{NewSignupMeetingBean.signupMeeting.title}" styleClass="longtext" escape="false"/>
							</h:panelGroup>
						</h:panelGroup>
					</div>

					<%-- organiser --%>
					<div class="row">
						<h:panelGroup styleClass="col-xs-12 col-md-3 titleColumn" layout="block">
							<h:outputText value="#{msgs.event_owner}" styleClass="titleText" escape="false"/>
						</h:panelGroup>
						<h:panelGroup styleClass="col-xs-12 col-md-9 valueColumn" layout="block">
							<h:outputText value="#{NewSignupMeetingBean.instructorName}" styleClass="longtext"/>
						</h:panelGroup>
					</div>

					<%--  description --%>
					<div class="row">
						<h:panelGroup styleClass="col-xs-12 col-md-3 titleColumn" layout="block">
							<h:outputText value="#{msgs.event_description}" styleClass="titleText" escape="false"/>
						</h:panelGroup>
						<h:panelGroup styleClass="col-xs-12 col-md-9 valueColumn" layout="block">
							<h:outputText value="#{NewSignupMeetingBean.signupMeeting.description}" styleClass="longtext" escape="false"/>
						</h:panelGroup>
					</div>

					<%--  attachments --%>
					<div class="row">
						<h:panelGroup styleClass="col-xs-12 col-md-3 titleColumn" layout="block" rendered="#{!NewSignupMeetingBean.attachmentsEmpty}">
							<h:outputText  value="#{msgs.attachments}" styleClass="titleText" escape="false"/>
						</h:panelGroup>
						<h:panelGroup styleClass="col-xs-12 col-md-9 valueColumn" layout="block" rendered="#{!NewSignupMeetingBean.attachmentsEmpty}">
							<h:panelGrid columns="1">
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
						</h:panelGroup>
					</div>

					<%--  start time --%>
					<div class="row">
						<h:panelGroup styleClass="col-xs-12 col-md-3 titleColumn" layout="block">
							<h:outputText value="#{msgs.event_start_time}" styleClass="titleText" escape="false"/>
						</h:panelGroup>
						<h:panelGroup styleClass="col-xs-12 col-md-9 valueColumn" layout="block">
							<h:panelGroup>
								<h:outputText value="#{NewSignupMeetingBean.signupMeeting.startTime}" styleClass="longtext">
									<f:convertDateTime pattern="#{UserLocale.fullDateTimeFormat}" timeZone="#{UserTimeZone.userTimeZone}"/>
								</h:outputText>		
							</h:panelGroup>
						</h:panelGroup>
					</div>

					<%-- end time --%>
					<div class="row">
						<h:panelGroup styleClass="col-xs-12 col-md-3 titleColumn" layout="block">
							<h:outputText value="#{msgs.event_end_time}" styleClass="titleText" escape="false"/>
						</h:panelGroup>
						<h:panelGroup styleClass="col-xs-12 col-md-9 valueColumn" layout="block">
							<h:panelGroup rendered="#{!NewSignupMeetingBean.endTimeAutoAdjusted}">
								<h:outputText value="#{NewSignupMeetingBean.meetingEndTime}" styleClass="longtext">
									<f:convertDateTime pattern="#{UserLocale.fullDateTimeFormat}" timeZone="#{UserTimeZone.userTimeZone}"/>
								</h:outputText>
							</h:panelGroup>
							<h:panelGroup rendered="#{NewSignupMeetingBean.endTimeAutoAdjusted}">
								<h:outputText value="#{NewSignupMeetingBean.meetingEndTime}" style="color:#b11;" styleClass="longtext">
									<f:convertDateTime pattern="#{UserLocale.fullDateTimeFormat}" timeZone="#{UserTimeZone.userTimeZone}"/>
								</h:outputText>
							</h:panelGroup>
						</h:panelGroup>
					</div>

					<%-- recurrence --%>
					<div class="row">
						<h:panelGroup styleClass="col-xs-12 col-md-3 titleColumn" layout="block" rendered="#{NewSignupMeetingBean.recurrence}">
							<h:outputText styleClass="titleText" value="#{msgs.event_recurrence}" escape="false"/> 
						</h:panelGroup>
						<h:panelGroup styleClass="col-xs-12 col-md-9 valueColumn" layout="block" rendered="#{NewSignupMeetingBean.recurrence or (NewSignupMeetingBean.recurrence && NewSignupMeetingBean.recurLengthChoice=='1') or (NewSignupMeetingBean.recurrence && NewSignupMeetingBean.recurLengthChoice=='0')}">
							<h:outputText value="#{NewSignupMeetingBean.eventFreqType}" rendered="#{NewSignupMeetingBean.recurrence}" escape="false"/>
							<h:outputText value="#{msgs.event_repeat_until}" styleClass="titleText"  rendered="#{NewSignupMeetingBean.recurrence && NewSignupMeetingBean.recurLengthChoice=='1'}" escape="false"/>
							<h:outputText value="#{NewSignupMeetingBean.repeatUntil}" rendered="#{NewSignupMeetingBean.recurrence && NewSignupMeetingBean.recurLengthChoice=='1'}">
								<f:convertDateTime dateStyle="full" />
							</h:outputText>
							<h:outputText value="#{msgs.event_repeat_num}" styleClass="titleText"  rendered="#{NewSignupMeetingBean.recurrence && NewSignupMeetingBean.recurLengthChoice=='0'}" escape="false"/>
							<h:outputText value="#{NewSignupMeetingBean.occurrences}" rendered="#{NewSignupMeetingBean.recurrence && NewSignupMeetingBean.recurLengthChoice=='0'}" />
						</h:panelGroup>
					</div>

					<%-- location --%>
					<div class="row">
						<h:panelGroup styleClass="col-xs-12 col-md-3 titleColumn" layout="block">
							<h:outputText value="#{msgs.event_location}" styleClass="titleText" escape="false"/>
						</h:panelGroup>
						<h:panelGroup styleClass="col-xs-12 col-md-9 valueColumn" layout="block">
							<h:outputText value="#{NewSignupMeetingBean.signupMeeting.location}" styleClass="longtext" escape="false"/>
						</h:panelGroup>
					</div>

					<%-- category --%>
					<div class="row">
						<h:panelGroup styleClass="col-xs-12 col-md-3 titleColumn" layout="block">
							<h:outputText value="#{msgs.event_category}" styleClass="titleText" escape="false"/>
						</h:panelGroup>
						<h:panelGroup styleClass="col-xs-12 col-md-9 valueColumn" layout="block">
							<h:outputText value="#{NewSignupMeetingBean.signupMeeting.category}" styleClass="longtext" escape="false"/>
						</h:panelGroup>
					</div>

					<%-- sign up begins --%>
					<div class="row">
						<h:panelGroup styleClass="col-xs-12 col-md-3 titleColumn" layout="block" rendered="#{!NewSignupMeetingBean.announcementType}">
							<h:outputText value="#{msgs.event_signup_start}" styleClass="titleText" escape="false"/>
						</h:panelGroup>
						<h:panelGroup styleClass="col-xs-12 col-md-9 valueColumn" layout="block" rendered="#{!NewSignupMeetingBean.announcementType}">
							<h:panelGroup>
								<h:outputText value="#{NewSignupMeetingBean.signupBeginInDate}" styleClass="longtext">
									<f:convertDateTime pattern="#{UserLocale.fullDateTimeFormat}" timeZone="#{UserTimeZone.userTimeZone}"/>
								</h:outputText>
							</h:panelGroup>
						</h:panelGroup>
					</div>

					<%-- sign up ends --%>
					<div class="row">
						<h:panelGroup styleClass="col-xs-12 col-md-3 titleColumn" layout="block" rendered="#{!NewSignupMeetingBean.announcementType}">
							<h:outputText value="#{msgs.event_signup_deadline}" styleClass="titleText" escape="false"/>
						</h:panelGroup>
						<h:panelGroup styleClass="col-xs-12 col-md-9 valueColumn" layout="block" rendered="#{!NewSignupMeetingBean.announcementType}">
							<h:panelGroup>
								<h:outputText value="#{NewSignupMeetingBean.signupDeadlineInDate}" styleClass="longtext">
									<f:convertDateTime pattern="#{UserLocale.fullDateTimeFormat}" timeZone="#{UserTimeZone.userTimeZone}"/>
								</h:outputText>
							</h:panelGroup>
						</h:panelGroup>
					</div>
					<h:outputText value="&nbsp;" escape="false"/>
					<h:outputText value="&nbsp;" escape="false"/>

					<%-- meeting type --%>
					<div class="row">
						<h:panelGroup styleClass="col-xs-12 col-md-3 titleColumn" layout="block">
							<h:outputText value="#{msgs.event_type_title}" styleClass="titleText"/>
						</h:panelGroup>
						<h:panelGroup styleClass="col-xs-12 col-md-9 valueColumn" layout="block">
							<h:outputText value="#{NewSignupMeetingBean.displayCurrentMeetingType}" styleClass="longtext" escape="false"/>
						</h:panelGroup>
					</div>

					<%-- number of time slots --%>
					<div class="row">
						<h:panelGroup styleClass="col-xs-12 col-md-3 titleColumn" layout="block" rendered="#{NewSignupMeetingBean.individualType}">
							<h:outputText value="#{msgs.event_num_timeslot}" styleClass="titleText" escape="false"/>
						</h:panelGroup>
						<h:panelGroup styleClass="col-xs-12 col-md-9 valueColumn" layout="block" rendered="#{NewSignupMeetingBean.individualType}">
							<h:outputText value="#{NewSignupMeetingBean.numberOfSlots}" styleClass="longtext" escape="false"/>
						</h:panelGroup>
					</div>

					<%-- duration of each time slot --%>
					<div class="row">
						<h:panelGroup styleClass="col-xs-12 col-md-3 titleColumn" layout="block" rendered="#{NewSignupMeetingBean.individualType}">
							<h:outputText value="#{msgs.event_duration_each_timeslot}" styleClass="titleText" escape="false"/>
						</h:panelGroup>
						<h:panelGroup styleClass="col-xs-12 col-md-9 valueColumn" layout="block" rendered="#{NewSignupMeetingBean.individualType}">
							<h:outputText value="#{NewSignupMeetingBean.timeSlotDuration}" styleClass="longtext" escape="false"/>	
						</h:panelGroup>
					</div>

					<%-- max of participants per time slot --%>
					<div class="row">
						<h:panelGroup styleClass="col-xs-12 col-md-3 titleColumn" layout="block" rendered="#{NewSignupMeetingBean.individualType}">
							<h:outputText value="#{msgs.event_max_attendee_each_timeslot}" styleClass="titleText" escape="false"/>
						</h:panelGroup>
						<h:panelGroup styleClass="col-xs-12 col-md-9 valueColumn" layout="block" rendered="#{NewSignupMeetingBean.individualType}">
							<h:outputText value="#{NewSignupMeetingBean.numberOfAttendees}" styleClass="longtext" escape="false"/>
						</h:panelGroup>
					</div>

					<%-- calendar details --%>
					<div class="row">
						<h:panelGroup styleClass="col-xs-12 col-md-3 titleColumn" layout="block" rendered="#{NewSignupMeetingBean.customTimeslotType}">
							<h:outputText value="#{msgs.event_show_schedule}" styleClass="titleText"/>
						</h:panelGroup>
						<h:panelGroup styleClass="col-xs-12 col-md-9 valueColumn" layout="block" rendered="#{NewSignupMeetingBean.customTimeslotType}">
							<h:panelGroup>
								<h:outputLabel  id="imageOpen_schedule" style="display:none" styleClass="activeTag" onclick="showDetails('meeting:imageOpen_schedule','meeting:imageClose_schedule','meeting:scheduleDetail');">
									<h:graphicImage value="/images/open.gif" alt="#{msgs.title_tip_click_hide_schedule}" title="#{msgs.title_tip_click_hide_schedule}" style="border:none;" styleClass="openCloseImageIcon"/>
									<h:outputText value="#{msgs.event_hide_custom_ts}" escape="false" style="vertical-align: top;"/>
								</h:outputLabel>
								<h:outputLabel id="imageClose_schedule" styleClass="activeTag" onclick="showDetails('meeting:imageOpen_schedule','meeting:imageClose_schedule','meeting:scheduleDetail');">
									<h:graphicImage value="/images/closed.gif" alt="#{msgs.title_tip_click_show_schedule}" title="#{msgs.title_tip_click_show_schedule}" style="border:none;vertical-align:top;" styleClass="openCloseImageIcon"/>
									<h:outputText value="#{msgs.event_show_custom_ts}" escape="false" style="vertical-align: top;"/>
								</h:outputLabel>
							</h:panelGroup>
						</h:panelGroup>
					</div>

					<%-- calendar details 1 --%>
					<div class="row">
						<h:panelGroup styleClass="col-xs-12 col-md-3 titleColumn" layout="block" rendered="#{NewSignupMeetingBean.customTimeslotType}">
							<h:outputText id="scheduleDetail_1" value="" styleClass="titleText"/>
						</h:panelGroup>
						<h:panelGroup styleClass="col-xs-12 col-md-9 valueColumn" layout="block" rendered="#{NewSignupMeetingBean.customTimeslotType}">
							<h:dataTable id="scheduleDetail_2" value="#{NewSignupMeetingBean.customTimeSlotWrpList}" var="timeSlot"
							rowClasses="oddTimeSlotRow,evenTimeSlotRow"	columnClasses="timeslotCol,assignStudentsCol" 
							styleClass="signupTable"  style="display:none; width: 55%">
								<h:column>
									<f:facet name="header">
										<h:outputText value="#{msgs.tab_time_slot}"/>
									</f:facet>
									<h:panelGroup>
										<h:outputText value="#{timeSlot.timeSlot.startTime}" styleClass="longtext">
											<f:convertDateTime pattern="#{UserLocale.localizedTimeFormat}" timeZone="#{UserTimeZone.userTimeZone}"/>
										</h:outputText>
										<h:outputText value="#{timeSlot.timeSlot.startTime}" rendered="#{NewSignupMeetingBean.signupMeeting.meetingCrossDays}">
											<f:convertDateTime pattern=", EEE," timeZone="#{UserTimeZone.userTimeZone}"/>
										</h:outputText>
										<h:outputText value="#{timeSlot.timeSlot.startTime}" rendered="#{NewSignupMeetingBean.signupMeeting.meetingCrossDays}">
											<f:convertDateTime  dateStyle="short" pattern="#{UserLocale.dateFormat}" timeZone="#{UserTimeZone.userTimeZone}"/>
										</h:outputText>
										<h:outputText value="#{msgs.timeperiod_divider}" escape="false"/>
										<h:outputText value="#{timeSlot.timeSlot.endTime}" styleClass="longtext">
											<f:convertDateTime pattern="#{UserLocale.localizedTimeFormat}" timeZone="#{UserTimeZone.userTimeZone}"/>
										</h:outputText>
										<h:outputText value="#{timeSlot.timeSlot.endTime}" rendered="#{NewSignupMeetingBean.signupMeeting.meetingCrossDays}">
											<f:convertDateTime pattern=", EEE," timeZone="#{UserTimeZone.userTimeZone}"/>
										</h:outputText>
										<h:outputText value="#{timeSlot.timeSlot.endTime}" rendered="#{NewSignupMeetingBean.signupMeeting.meetingCrossDays}">
											<f:convertDateTime  dateStyle="short" pattern="#{UserLocale.dateFormat}" timeZone="#{UserTimeZone.userTimeZone}"/>
										</h:outputText>
									</h:panelGroup>
								</h:column>
								<h:column>
									<f:facet name="header">
										<h:outputText value="#{msgs.tab_max_attendee}"/>
									</f:facet>
									<h:outputText value="#{timeSlot.timeSlot.maxNoOfAttendees}"/>
								</h:column>
							</h:dataTable>
						</h:panelGroup>
					</div>

					<%-- event max attendees --%>
					<div class="row">
						<h:panelGroup styleClass="col-xs-12 col-md-3 titleColumn" layout="block" rendered="#{NewSignupMeetingBean.groupType}">
							<h:outputText value="#{msgs.event_max_attendees}" styleClass="titleText" escape="false"/>
						</h:panelGroup>
						<h:panelGroup styleClass="col-xs-12 col-md-9 valueColumn" layout="block" rendered="#{NewSignupMeetingBean.groupType}">
							<h:panelGroup>
								<h:outputText id="maxAttendee" value="#{NewSignupMeetingBean.maxOfAttendees}" rendered="#{!NewSignupMeetingBean.unlimited}" styleClass="longtext"/>
								<h:outputText value="#{msgs.event_unlimited}" rendered="#{NewSignupMeetingBean.unlimited}" styleClass="longtext"/>
							</h:panelGroup>
						</h:panelGroup>
					</div>

					<%-- attendance --%>
					<div class="row">
						<h:panelGroup styleClass="col-xs-12 col-md-3 titleColumn" layout="block" rendered="#{NewSignupMeetingBean.attendanceOn}">
							<h:outputText value="#{msgs.attend_view_title}" styleClass="titleText" escape="false"/>
						</h:panelGroup>
						<h:panelGroup styleClass="col-xs-12 col-md-9 valueColumn" layout="block" rendered="#{NewSignupMeetingBean.attendanceOn}">
							<h:panelGroup>
								<h:outputText value="#{msgs.attend_on}" rendered="#{NewSignupMeetingBean.signupMeeting.allowAttendance}" styleClass="longtext"/>
								<h:outputText value="#{msgs.attend_off}" rendered="#{!NewSignupMeetingBean.signupMeeting.allowAttendance}" styleClass="longtext"/>
							</h:panelGroup>
						</h:panelGroup>
					</div>

					<%-- available to --%>
					<div class="row">
						<h:panelGroup styleClass="col-xs-12 col-md-3 titleColumn" layout="block">
							<h:outputText value="#{msgs.event_publish_to}" escape="false" styleClass="titleText"/>
						</h:panelGroup>
						<h:panelGroup styleClass="col-xs-12 col-md-9 valueColumn" layout="block">
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
						</h:panelGroup>
					</div>
					<h:outputText value="&nbsp;" escape="false"/>
					<h:outputText value="&nbsp;" escape="false"/>

					<%-- available to --%>
					<div class="row">
						<h:panelGroup styleClass="col-xs-12 col-md-3 titleColumn" layout="block" rendered="#{!NewSignupMeetingBean.announcementType}">
							<h:outputText value="#{msgs.event_publish_attendee_name}" styleClass="titleText" escape="false"/>
						</h:panelGroup>
						<h:panelGroup styleClass="col-xs-12 col-md-9 valueColumn" layout="block" rendered="#{!NewSignupMeetingBean.announcementType}">
							<h:panelGroup styleClass="longtext">
								<h:selectBooleanCheckbox id="showparticipants" value="#{NewSignupMeetingBean.showParticipants}"/>
								<h:outputLabel for="showparticipants" value="#{msgs.event_yes_show_attendee_public}" escape="false"/>
							</h:panelGroup>
						</h:panelGroup>
					</div>

					<%-- meeting coordinators --%>
					<div class="row">
						<h:panelGroup styleClass="col-xs-12 col-md-3 titleColumn" layout="block">
							<h:outputText value="#{msgs.event_select_coordinators}" escape="false" styleClass="titleText"/>
						</h:panelGroup>
						<h:panelGroup styleClass="col-xs-12 col-md-9 valueColumn" layout="block">
							<h:dataTable id="meeting_coordinators" value="#{NewSignupMeetingBean.allPossibleCoordinators}" var="coUser" styleClass="coordinatorTab">
								<h:column>
									<h:selectBooleanCheckbox id="meetingcoord" value="#{coUser.checked}"/>
									<h:outputLabel for="meetingcoord" value="#{coUser.displayName}" styleClass="longtext"/>
								</h:column>
							</h:dataTable>
						</h:panelGroup>
					</div>

					<%-- notifications of participant actions --%>
					<div class="row">
						<h:panelGroup styleClass="col-xs-12 col-md-3 titleColumn" layout="block" rendered="#{!NewSignupMeetingBean.announcementType}">
							<h:outputText value="#{msgs.event_receive_notification}" styleClass="titleText" escape="false"/>
						</h:panelGroup>
						<h:panelGroup styleClass="col-xs-12 col-md-9 valueColumn" layout="block" rendered="#{!NewSignupMeetingBean.announcementType}">
							<h:panelGroup styleClass="longtext">
								<h:selectBooleanCheckbox id="receiveemail" value="#{NewSignupMeetingBean.receiveEmail}"/>
								<h:outputLabel for="receiveemail" value="#{msgs.event_yes_receive_notification}" escape="false"/>
							</h:panelGroup>
						</h:panelGroup>
					</div>
					<h:outputText value="&nbsp;" escape="false"/>
					<h:outputText value="&nbsp;" escape="false"/>

					<%-- announce availability --%>
					<div class="row">
						<h:panelGroup styleClass="col-xs-12 col-md-3 titleColumn" layout="block">
							<h:outputText value="#{msgs.event_create_email_notification}" styleClass="titleText" escape="false"/>
						</h:panelGroup>
						<h:panelGroup styleClass="col-xs-12 col-md-9 valueColumn" layout="block">
							<h:panelGrid columns="1" style="width:100%;margin-left:-3px;" rendered="#{NewSignupMeetingBean.publishedSite}">
								<h:panelGroup styleClass="editText" >
									<h:selectBooleanCheckbox id="emailChoice" value="#{NewSignupMeetingBean.sendEmail}" onclick="isShowEmailChoice()" disabled="#{NewSignupMeetingBean.mandatorySendEmail}"/>
									<h:outputLabel for="emailChoice" value="#{msgs.event_yes_email_notification}" escape="false"/>
								</h:panelGroup>
								
								<h:panelGroup id="emailAttendeeOnly">
									<h:selectOneRadio  value="#{NewSignupMeetingBean.sendEmailToSelectedPeopleOnly}" layout="pageDirection" styleClass="rs" >
										<f:selectItem id="all_attendees" itemValue="all" itemLabel="#{msgs.label_email_all_people}" itemDisabled="true"/>
										<f:selectItem id="only_organizers" itemValue="organizers_only" itemLabel="#{msgs.label_email_organizers_only}" itemDisabled="true"/>
									</h:selectOneRadio>
								</h:panelGroup>
							</h:panelGrid>
							<h:panelGroup styleClass="longtext" rendered="#{!NewSignupMeetingBean.publishedSite}">
								<h:selectBooleanCheckbox id="sendemail" value="#{NewSignupMeetingBean.sendEmail}" disabled="true"/>
								<h:outputLabel for="sendemail" value="#{msgs.event_email_not_send_out_label}" escape="false" style="color:#b11"/>
							</h:panelGroup>
						</h:panelGroup>
					</div>

					<%-- default notification setting --%>
					<div class="row">
						<h:panelGroup styleClass="col-xs-12 col-md-3 titleColumn" layout="block">
							<h:outputText value="#{msgs.event_meeting_default_notify_setting}" styleClass="titleText" escape="false"/>
						</h:panelGroup>
						<h:panelGroup styleClass="col-xs-12 col-md-9 valueColumn" layout="block">
							<h:panelGroup styleClass="longtext" >
								<h:selectBooleanCheckbox id="sendemailbyowner" value="#{NewSignupMeetingBean.sendEmailByOwner}"/>
								<h:outputLabel for="sendemailbyowner" value="#{msgs.event_yes_meeting_default_notify_setting}" escape="false"/>
							</h:panelGroup>
						</h:panelGroup>
					</div>

					<%-- other default settings --%>
					<div class="row">
						<h:panelGroup styleClass="col-xs-12 col-md-3 titleColumn" layout="block" rendered="#{!NewSignupMeetingBean.announcementType}">
							<h:outputText value="#{msgs.event_other_default_setting}" escape="false" styleClass="titleText"/>
						</h:panelGroup>
						<h:panelGroup styleClass="col-xs-12 col-md-9 valueColumn" layout="block" rendered="#{!NewSignupMeetingBean.announcementType}">
							<h:panelGroup>
								<h:panelGroup id="imageOpen_otherSetting" style="display:none" styleClass="activeTag">
									<h:graphicImage value="/images/open.gif"  alt="open" title="Click to hide details." style="border:none;vertical-align: middle;" styleClass="openCloseImageIcon"/>
									<h:outputText value="#{msgs.event_close_other_default_setting}" escape="false" style="vertical-align: middle;"/>
								</h:panelGroup>
								<h:panelGroup id="imageClose_otherSetting" styleClass="activeTag">
									<h:graphicImage value="/images/closed.gif" alt="close" title="Click to show details." style="border:none;vertical-align:middle;" styleClass="openCloseImageIcon"/>
									<h:outputText value="#{msgs.event_show_other_default_setting}" escape="false" style="vertical-align: middle;"/>
								</h:panelGroup>
							</h:panelGroup>
						</h:panelGroup>
					</div>

					<%-- div other default settings --%>
					<div id="otherDefaultSettings" style="display:none;">
						<%-- allow wait list --%>
						<div class="row">
							<h:panelGroup styleClass="col-xs-12 col-md-3 titleColumn" layout="block" rendered="#{!NewSignupMeetingBean.announcementType}">
								<h:outputText id="otherSetting_1" value="#{msgs.event_allow_waitList}" styleClass="titleText" escape="false"/>
							</h:panelGroup>
							<h:panelGroup styleClass="col-xs-12 col-md-9 valueColumn" layout="block" rendered="#{!NewSignupMeetingBean.announcementType}">
								<h:panelGroup id="otherSetting_2" styleClass="longtext">
									<h:selectBooleanCheckbox id="allowwaitlist" value="#{NewSignupMeetingBean.allowWaitList}"/>
									<h:outputLabel for="allowwaitlist"  value="#{msgs.event_yes_to_allow_waitList}" escape="false"/>
								</h:panelGroup>
							</h:panelGroup>
						</div>

						<%-- allow adding comment --%>
						<div class="row">
							<h:panelGroup styleClass="col-xs-12 col-md-3 titleColumn" layout="block" rendered="#{!NewSignupMeetingBean.announcementType}">
								<h:outputText id="otherSetting_3" value="#{msgs.event_allow_addComment}" styleClass="titleText" escape="false"/>
							</h:panelGroup>
							<h:panelGroup styleClass="col-xs-12 col-md-9 valueColumn" layout="block" rendered="#{!NewSignupMeetingBean.announcementType}">
								<h:panelGroup id="otherSetting_4" styleClass="longtext">
									<h:selectBooleanCheckbox id="allowcomment" value="#{NewSignupMeetingBean.allowComment}"/>
									<h:outputLabel for="allowcomment" value="#{msgs.event_yes_to_allow_addComment}" escape="false"/>
								</h:panelGroup>
							</h:panelGroup>
						</div>

						<%-- User ID Input Mode --%>
						<div class="row">
							<h:panelGroup styleClass="col-xs-12 col-md-3 titleColumn" layout="block" rendered="#{!NewSignupMeetingBean.announcementType && NewSignupMeetingBean.userIdInputModeOptionChoice}">
								<h:outputText id="otherSetting_5" value="#{msgs.event_use_eid_input_mode}" styleClass="titleText" escape="false"/>
							</h:panelGroup>
							<h:panelGroup styleClass="col-xs-12 col-md-9 valueColumn" layout="block" rendered="#{!NewSignupMeetingBean.announcementType && NewSignupMeetingBean.userIdInputModeOptionChoice}">
								<h:panelGroup id="otherSetting_6" styleClass="longtext">
									<h:selectBooleanCheckbox id="eidinputmode" value="#{NewSignupMeetingBean.eidInputMode}"/>
									<h:outputLabel for="eidinputmode" value="#{msgs.event_yes_to_use_eid_input_mode}" escape="false"/>
								</h:panelGroup>
							</h:panelGroup>
						</div>

						<%-- Auto Reminder --%>
						<div class="row">
							<h:panelGroup styleClass="col-xs-12 col-md-3 titleColumn" layout="block" rendered="#{!NewSignupMeetingBean.announcementType && NewSignupMeetingBean.autoReminderOptionChoice}">
								<h:outputText id="otherSetting_7" value="#{msgs.event_email_autoReminder}" styleClass="titleText" escape="false"/>
							</h:panelGroup>
							<h:panelGroup styleClass="col-xs-12 col-md-9 valueColumn" layout="block" rendered="#{!NewSignupMeetingBean.announcementType && NewSignupMeetingBean.autoReminderOptionChoice}">
								<h:panelGroup id="otherSetting_8" styleClass="longtext">
									<h:selectBooleanCheckbox id="autoreminder" value="#{NewSignupMeetingBean.autoReminder}"/>
									<h:outputLabel for="autoreminder" value="#{msgs.event_yes_email_autoReminer_to_attendees}" escape="false"/>
								</h:panelGroup>
							</h:panelGroup>
						</div>

						<%-- Publish to Calendar --%>
						<div class="row">
							<h:panelGroup styleClass="col-xs-12 col-md-3 titleColumn" layout="block">
								<h:outputText id="otherSetting_9" value="#{msgs.event_publish_to_calendar}" styleClass="titleText" escape="false" />
							</h:panelGroup>
							<h:panelGroup styleClass="col-xs-12 col-md-9 valueColumn" layout="block">
								<h:panelGroup id="otherSetting_10" styleClass="longtext">
									<h:selectBooleanCheckbox id="publishtocalendar" value="#{NewSignupMeetingBean.publishToCalendar}"/>
									<h:outputLabel for="publishtocalendar" value="#{msgs.event_yes_publish_to_calendar}" escape="false"/>
								</h:panelGroup>
							</h:panelGroup>
						</div>

						<%-- create groups for timeslots --%>
						<div class="row">
							<h:panelGroup styleClass="col-xs-12 col-md-3 titleColumn" layout="block">
								<h:outputText id="otherSetting_11" value="#{msgs.event_create_groups}" styleClass="titleText" escape="false" />
							</h:panelGroup>
							<h:panelGroup styleClass="col-xs-12 col-md-9 valueColumn" layout="block">
								<h:panelGroup id="otherSetting_12" styleClass="longtext">
									<h:selectBooleanCheckbox id="creategroups" value="#{NewSignupMeetingBean.createGroups}"/>
									<h:outputLabel for="creategroups" value="#{msgs.event_yes_create_groups}" escape="false"/>
								</h:panelGroup>
							</h:panelGroup>
						</div>

						<%-- max of time slots per participant --%>
						<div class="row">
							<h:panelGroup styleClass="col-xs-12 col-md-3 titleColumn" layout="block">
								<h:outputText id="otherSetting_13" value="#{msgs.event_allowed_slots }" styleClass="titleText" escape="false" />
							</h:panelGroup>
							<h:panelGroup styleClass="col-xs-12 col-md-9 valueColumn" layout="block">
								<h:panelGroup id="otherSetting_14" styleClass="longtext">
									<h:selectOneMenu id="slotscomments" value="#{ NewSignupMeetingBean.maxNumOfSlots}">  
										 <f:selectItems  value="#{NewSignupMeetingBean.slots}"   /> 
									</h:selectOneMenu>
									<h:outputLabel for="slotscomments" value="#{msgs.event_allowed_slots_comments}" escape="false"/>
								</h:panelGroup>	
							</h:panelGroup>
						</div>
					</div>
			 	</div>
					
										
				<h:inputHidden value="step2" binding="#{NewSignupMeetingBean.currentStepHiddenInfo}"/>
				<sakai:button_bar>
					<h:commandButton id="goNextPage" styleClass="active" action="#{NewSignupMeetingBean.processSave}" value="#{msgs.publish_button}" onclick='displayProcessingIndicator(this);'/> 
					<h:commandButton id="assignStudents" action="#{NewSignupMeetingBean.proceesPreAssignAttendee}" value="#{msgs.assign_attendee_publish_button}" disabled="#{NewSignupMeetingBean.announcementType}"/> 
					<h:commandButton id="goBack" action="#{NewSignupMeetingBean.goBack}" value="#{msgs.goback_button}"/>
					<h:commandButton id="Cancel" action="#{NewSignupMeetingBean.processCancel}" value="#{msgs.cancel_button}" immediate="true"/>
 
				</sakai:button_bar>
                                <h:outputText styleClass="sak-banner-info" style="display:none" value="#{msgs.publish_processing_submit_message}" />

			 </h:form>
  		</sakai:view_content>	
	</sakai:view_container>
	
</f:view> 
