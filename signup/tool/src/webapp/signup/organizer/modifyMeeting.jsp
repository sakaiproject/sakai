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

<h:outputText value="#{Portal.latestJQuery}" escape="false"/>
        <script type="text/javascript" src="/library/js/lang-datepicker/lang-datepicker.js"></script>
        <script type="text/javascript" src="/sakai-signup-tool/js/signupScript.js"></script>
        
		<script type="text/javascript">
			jQuery(document).ready(function(){

                localDatePicker({
                    input: '#meeting\\:startTime',
                    useTime: 1,
                    parseFormat: 'YYYY-MM-DD HH:mm:ss',
                    allowEmptyDate: false,
                    val: '<h:outputText value="#{EditMeetingSignupMBean.signupMeeting.startTime}"><f:convertDateTime pattern="yyyy-MM-dd HH:mm:ss"/></h:outputText>',
                    ashidden: {
                            iso8601: 'startTimeISO8601',
                            month:"meeting_startTime_month",
                            day:"meeting_startTime_day",
                            year:"meeting_startTime_year",
                            hour:"meeting_startTime_hours",
                            minute:"meeting_startTime_minutes",
                            ampm:"meeting_startTime_ampm"}
                });

                localDatePicker({
                    input: '#meeting\\:endTime',
                    useTime: 1,
                    parseFormat: 'YYYY-MM-DD HH:mm:ss',
                    allowEmptyDate: false,
                    val: '<h:outputText value="#{EditMeetingSignupMBean.signupMeeting.endTime}"><f:convertDateTime pattern="yyyy-MM-dd HH:mm:ss"/></h:outputText>',
                    ashidden: {
                            iso8601: 'endTimeISO8601',
                            month:"meeting_endTime_month",
                            day:"meeting_endTime_day",
                            year:"meeting_endTime_year",
                            hour:"meeting_endTime_hours",
                            minute:"meeting_endTime_minutes",
                            ampm:"meeting_endTime_ampm"}
                });

        		sakai.initSignupBeginAndEndsExact();
        	});
    	</script>
 
		<script type="text/javascript">
			
			var recurWarnTag1;
	        var recurWarnTag2;

			
	        jQuery(document).ready(function(){
				recurWarnTag1 = document.getElementById('meeting:rescheduleWarnLabel_1');
		        recurWarnTag2 = document.getElementById('meeting:rescheduleWarnLabel_2');

        		sakai.initSignupBeginAndEndsExact();
        		//for IE browser, it does nothing.
        		initGroupTypeRadioButton();
    			userDefinedTsChoice();
    			isShowEmailChoice();
    			setIframeHeight_DueTo_Ckeditor();
    			initDropDownAndInput('meeting:customLocationLabel','meeting:customLocationLabel_undo','meeting:customLocation','meeting:selectedLocation');
    	        initDropDownAndInput('meeting:customCategoryLabel','meeting:customCategoryLabel_undo','meeting:customCategory','meeting:selectedCategory');

        	});
	        
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
			
			function showRescheduleWarning(){
	        	if(recurWarnTag1 && recurWarnTag2){	        	
		        	recurWarnTag1.style.display="";
		        	recurWarnTag2.style.display="";     		
        		}
	        }			
		</script>

		
		
		<sakai:view_content>
			<h:outputText value="#{msgs.event_error_alerts} #{messageUIBean.errorMessage}" styleClass="alertMessage" escape="false" rendered="#{messageUIBean.error}"/>      			
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
												
				<h:inputHidden id="iframeId" value="#{EditMeetingSignupMBean.iframeId}" />
				
				<div onmouseover="delayedRecalculateDateTime();" class="container-fluid">

					<div class="form-group row ">
						<h:outputLabel value="#{msgs.event_modify_option}" escape="false" styleClass="col-lg-2 form-control-label form-required"/>
						<div class="col-lg-10">
							<h:selectOneRadio  value="#{EditMeetingSignupMBean.convertToNoRecurrent}" layout="pageDirection" styleClass="rs" rendered="#{EditMeetingSignupMBean.signupMeeting.recurredMeeting}">
								<f:selectItem id="modify_all" itemValue="#{false}" itemLabel="#{msgs.modify_all}"/>
								<f:selectItem id="modify_current" itemValue="#{true}" itemLabel="#{msgs.modify_current}"/>
							</h:selectOneRadio> 
						</div>
					</div>

					<%-- Title --%>
					<div class="form-group row ">
						<h:outputLabel value="#{msgs.event_name}" for="title" escape="false" styleClass="col-lg-2 form-control-label form-required"/>
						<div class="col-lg-10">
							<h:inputText id="title" value="#{EditMeetingSignupMBean.title}" size="40" 
										styleClass="editText form-control">
								<f:validateLength maximum="255" />
							</h:inputText>
							<h:message for="title" errorClass="alertMessageInline"/>
						</div>
					</div>
					
					<%-- Organiser --%>
					<div class="form-group row">
						<h:outputLabel value="#{msgs.event_owner}" for="creatorUserId" styleClass="col-lg-2 form-control-label" escape="false" />
						<div class="col-lg-10">
						 	<h:selectOneMenu id="creatorUserId" value="#{EditMeetingSignupMBean.creatorUserId}">
								<f:selectItems value="#{EditMeetingSignupMBean.instructors}"/>
							</h:selectOneMenu>
						</div>
					</div>
					
					<%-- Location --%>
					<div class="form-group row ">
						<h:outputLabel value="#{msgs.event_location}" escape="false" styleClass="col-lg-2 form-control-label form-required"/>
						<div class="col-lg-10">
							<!-- Displays all the locations in the dropdown -->
							<h:selectOneMenu id="selectedLocation" value="#{EditMeetingSignupMBean.selectedLocation}">
								<f:selectItems value="#{EditMeetingSignupMBean.allLocations}"/>
							</h:selectOneMenu>
							<h:inputText id="customLocation" size="35" value="#{EditMeetingSignupMBean.customLocation}" style="display:none" styleClass="editText">  
								<f:validator validatorId="Signup.EmptyStringValidator"/>
								<f:validateLength maximum="255" />
							</h:inputText>
							<h:outputLabel id="customLocationLabel" for="customLocation" styleClass="activeTag"  onclick="handleDropDownAndInput('meeting:customLocationLabel','meeting:customLocationLabel_undo','meeting:customLocation','meeting:selectedLocation')">
								<h:graphicImage value="/images/plus.gif"  alt="open" title="#{msgs.tab_event_location_custom}" style="border:none;vertical-align: middle; padding:0 5px 0 15px;" styleClass="openCloseImageIcon"/>
								<h:outputText value="#{msgs.tab_event_location_custom}" escape="false" style="vertical-align: middle;"/>
							</h:outputLabel>
							<h:outputLabel id="customLocationLabel_undo" for="customLocation" styleClass="activeTag" style="display:none" onclick="handleDropDownAndInput('meeting:customLocationLabel','meeting:customLocationLabel_undo','meeting:customLocation','meeting:selectedLocation')">
								<h:graphicImage value="/images/minus.gif"  alt="undo" title="#{msgs.event_custom_undo_tip}" style="border:none;vertical-align: middle;padding:0 5px 0 15px;" styleClass="openCloseImageIcon"/>
								<h:outputText value="#{msgs.event_custom_undo}" escape="false" style="vertical-align: middle;"/>
							</h:outputLabel>
							<h:outputText value="&nbsp;" escape="false" />
							<h:message for="customLocation" errorClass="alertMessageInline"/>
						</div>
					</div>
					
					<%-- Category --%>
					<div class="form-group row">
						<h:outputLabel value="#{msgs.event_category}" escape="false" styleClass="col-lg-2 form-control-label"/>
						<div class="col-lg-10">
							<!-- Displays all the categories in the dropdown -->
							<h:selectOneMenu id="selectedCategory" value="#{EditMeetingSignupMBean.selectedCategory}">
								<f:selectItems value="#{EditMeetingSignupMBean.allCategories}"/>
							</h:selectOneMenu>
							<h:inputText id="customCategory" size="35" value="#{EditMeetingSignupMBean.customCategory}" style="display:none" styleClass="editText">  
								<f:validator validatorId="Signup.EmptyStringValidator"/>
								<f:validateLength maximum="255" />
							</h:inputText>

							<h:outputLabel id="customCategoryLabel" for="customLocation" styleClass="activeTag"  onclick="handleDropDownAndInput('meeting:customCategoryLabel','meeting:customCategoryLabel_undo','meeting:customCategory','meeting:selectedCategory')" >
								<h:graphicImage value="/images/plus.gif"  alt="open" title="#{msgs.event_category_custom}" style="border:none;vertical-align: middle; padding:0 5px 0 15px;" styleClass="openCloseImageIcon"/>
								<h:outputText value="#{msgs.event_category_custom}" escape="false" style="vertical-align: middle;"/>
							</h:outputLabel>
							<h:outputLabel id="customCategoryLabel_undo" for="customLocation" styleClass="activeTag" style="display:none" onclick="handleDropDownAndInput('meeting:customCategoryLabel','meeting:customCategoryLabel_undo','meeting:customCategory','meeting:selectedCategory')">
								<h:graphicImage value="/images/minus.gif"  alt="undo" title="#{msgs.event_custom_undo_tip}" style="border:none;vertical-align: middle;padding:0 5px 0 15px;" styleClass="openCloseImageIcon"/>
								<h:outputText value="#{msgs.event_custom_undo}" escape="false" style="vertical-align: middle;"/>
							</h:outputLabel>
							<h:outputText value="&nbsp;" escape="false"/>
							<h:message for="customCategory" errorClass="alertMessageInline"/>
						</div>
					</div>
					
					<%-- Description --%>
					<div class="form-group row">
						<h:outputLabel value="#{msgs.event_description}" styleClass="col-lg-12 form-control-label" escape="false"/>
					</div>
					<sakai:rich_text_area value="#{EditMeetingSignupMBean.signupMeeting.description}" rows="5"  />
					
					<%-- Attachments --%>
					<div>
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
					</div>

					<h:outputText id="rescheduleWarnLabel_1" value="" escape="false" style="display:none;" rendered="#{EditMeetingSignupMBean.someoneSignedUp}"/>
					<h:outputText id="rescheduleWarnLabel_2" value="#{msgs.warn_reschedule_event}" styleClass="alertMessage" style="display:none;width:95%" escape="false" rendered="#{EditMeetingSignupMBean.someoneSignedUp}"/>

					<%-- Start time --%>
					<div class="form-group row ">
						<h:outputLabel value="#{msgs.event_start_time}"  styleClass="col-lg-2 form-control-label form-required" escape="false"/>
						<h:panelGroup styleClass="col-lg-10" rendered="#{!EditMeetingSignupMBean.customTsType}" layout="block">
							<h:inputText value="#{EditMeetingSignupMBean.startTimeString}" size="28" id="startTime" 
								onfocus="showRescheduleWarning();" onkeyup="getSignupDuration(); sakai.updateSignupBeginsExact(); return false;" onchange="sakai.updateSignupBeginsExact();"/>
							<h:message for="startTime" errorClass="alertMessageInline"/>
						</h:panelGroup>
						<h:panelGroup rendered="#{EditMeetingSignupMBean.customTsType}" styleClass="col-lg-6" layout="block">
							<h:outputText value="#{EditMeetingSignupMBean.signupMeeting.startTime}" styleClass="longtext">
								<f:convertDateTime pattern="EEEEEEEE, " timeZone="#{UserTimeZone.userTimeZone}"/>
							</h:outputText>
							<h:outputText value="#{EditMeetingSignupMBean.signupMeeting.startTime}" styleClass="longtext">
								<f:convertDateTime dateStyle="long" timeZone="#{UserTimeZone.userTimeZone}"/>
							</h:outputText>
								<h:outputText value="#{EditMeetingSignupMBean.signupMeeting.startTime}" styleClass="longtext">
									<f:convertDateTime pattern=", h:mm a" timeZone="#{UserTimeZone.userTimeZone}"/>
							</h:outputText>
						</h:panelGroup>
					</div>

					<%-- End time --%>
					<div class="form-group row ">
						<h:outputLabel value="#{msgs.event_end_time}" styleClass="col-lg-2 form-control-label form-required" escape="false"/>
						<h:panelGroup styleClass="col-lg-10" rendered="#{!EditMeetingSignupMBean.customTsType}" layout="block">
							<h:inputText value="#{EditMeetingSignupMBean.endTimeString}" size="28" id="endTime" 
								onfocus="showRescheduleWarning();" onkeyup="getSignupDuration(); sakai.updateSignupEndsExact(); return false;" onchange="sakai.updateSignupEndsExact();"/>
							<h:message for="endTime" errorClass="alertMessageInline"/>
						</h:panelGroup>
						<h:panelGroup rendered="#{EditMeetingSignupMBean.customTsType}" layout="block" styleClass="col-lg-6">
							<h:outputText value="#{EditMeetingSignupMBean.signupMeeting.endTime}" styleClass="longtext">
								<f:convertDateTime pattern="EEEEEEEE, " timeZone="#{UserTimeZone.userTimeZone}"/>
							</h:outputText>
							<h:outputText value="#{EditMeetingSignupMBean.signupMeeting.endTime}" styleClass="longtext">
								<f:convertDateTime dateStyle="long" timeZone="#{UserTimeZone.userTimeZone}"/>
							</h:outputText>
							<h:outputText value="#{EditMeetingSignupMBean.signupMeeting.endTime}" styleClass="longtext">
								<f:convertDateTime pattern=", h:mm a" timeZone="#{UserTimeZone.userTimeZone}"/>
							</h:outputText>
						</h:panelGroup>
					</div>
					
					<%-- Signup begin --%>
					<div class="form-group row">
						<h:panelGroup styleClass="signupBDeadline col-lg-2" id="signup_beginDeadline_1" layout="block">
							<h:outputLabel value="#{msgs.event_signup_start}" escape="false" styleClass="form-control-label "/>
						</h:panelGroup>
						<h:panelGroup styleClass="signupBDeadline col-lg-10" id="signup_beginDeadline_2" layout="block">
							<h:inputText id="signupBegins" value="#{EditMeetingSignupMBean.signupBegins}" size="5" required="true" onkeyup="sakai.updateSignupBeginsExact();">
								<f:validateLongRange minimum="0" maximum="99999"/>
							</h:inputText>
							<h:selectOneMenu id="signupBeginsType" value="#{EditMeetingSignupMBean.signupBeginsType}" onchange="isSignUpBeginStartNow(value); sakai.updateSignupBeginsExact();">
								<f:selectItem itemValue="minutes" itemLabel="#{msgs.label_minutes}"/>
								<f:selectItem itemValue="hours" itemLabel="#{msgs.label_hours}"/>
								<f:selectItem itemValue="days" itemLabel="#{msgs.label_days}"/>
								<f:selectItem itemValue="startNow" itemLabel="#{msgs.label_startNow}"/>
							</h:selectOneMenu>
							<h:outputText value="#{msgs.before_event_start}" escape="false" style="margin-left:18px"/>
							<h:message for="signupBegins" errorClass="alertMessageInline"/>

							<!--  show exact date, based on above -->
							<h:outputText id="signupBeginsExact" value="" escape="false" styleClass="dateExact" />
						</h:panelGroup>
					</div>
					
					<%-- Signup end --%>
					<div class="form-group row">
						<h:panelGroup styleClass="signupBDeadline col-lg-2" id="signup_beginDeadline_3" layout="block">
							<h:outputLabel value="#{msgs.event_signup_deadline}" escape="false" styleClass="form-control-label"/>
						</h:panelGroup>
						<h:panelGroup styleClass="signupBDeadline col-lg-10" id="signup_beginDeadline_4" layout="block">
							<h:inputText id="signupDeadline" value="#{EditMeetingSignupMBean.deadlineTime}" size="5" required="true" onkeyup="sakai.updateSignupEndsExact();">
								<f:validateLongRange minimum="0" maximum="99999"/>
							</h:inputText>
							<h:selectOneMenu id="signupDeadlineType" value="#{EditMeetingSignupMBean.deadlineTimeType}" onchange="sakai.updateSignupEndsExact();">
								<f:selectItem itemValue="minutes" itemLabel="#{msgs.label_minutes}"/>
								<f:selectItem itemValue="hours" itemLabel="#{msgs.label_hours}"/>
								<f:selectItem itemValue="days" itemLabel="#{msgs.label_days}"/>
							</h:selectOneMenu>
							<h:outputText value="#{msgs.before_event_end}"  style="margin-left:18px"/>
							<h:message for="signupDeadline" errorClass="alertMessageInline"/>
								
							<!--  show exact date, based on above -->
							<h:outputText id="signupEndsExact" value="" escape="false" styleClass="dateExact" />
								
						</h:panelGroup>
					</div>
					<%-- Attendance --%>
					<h:panelGroup rendered="#{EditMeetingSignupMBean.attendanceOn}" layout="block" styleClass="form-group row">
						<h:outputLabel value="#{msgs.event_signup_attendance}" escape="false" styleClass="col-lg-2"/>
						<div class="col-lg-10">
							<h:selectBooleanCheckbox id="attendanceSelection" value="#{EditMeetingSignupMBean.signupMeeting.allowAttendance}" />
							<h:outputLabel value="#{msgs.attend_taken}" for="attendanceSelection" styleClass="titleText"/>
							<h:outputText value="#{msgs.attend_track_selected}" escape="false" styleClass="textPanelFooter"/>
						</div>
					</h:panelGroup>
					
					<%-- Handle meeting types --%>
					<h:panelGroup styleClass="form-group row "  layout="block">
						<h:outputLabel value ="#{msgs.event_type_title}"  styleClass="col-lg-2 form-control-label form-required"/>
						<h:panelGroup layout="block" styleClass="col-lg-6" rendered="#{EditMeetingSignupMBean.customTsType}">
							<h:outputText value="#{msgs.label_custom_timeslots}"  escape="false"/>
						</h:panelGroup>
						<h:panelGroup styleClass="col-lg-10"  layout="block" rendered="#{!EditMeetingSignupMBean.customTsType}">
							<h:panelGroup id="radios" styleClass="rs">
								<h:selectOneRadio id="meetingType" value="#{EditMeetingSignupMBean.signupMeeting.meetingType}" layout="pageDirection" styleClass="rs" >
									<f:selectItems value="#{EditMeetingSignupMBean.meetingTypeRadioBttns}"/>
								</h:selectOneRadio> 
							</h:panelGroup>
							<div class="table-responsive">
								<h:panelGrid columns="1" columnClasses="miCol1">
									<%-- multiple: --%>
									<h:panelGroup rendered="#{EditMeetingSignupMBean.individualType}">
										<h:panelGrid columns="2" id="multipleCh" styleClass="mi" columnClasses="miCol1,miCol2"> 
											<h:outputText id="maxAttendeesPerSlot" style="display:none" value="#{EditMeetingSignupMBean.maxAttendeesPerSlot}"></h:outputText>
											<h:outputText id="maxSlots" style="display:none" value="#{EditMeetingSignupMBean.maxSlots}"></h:outputText>   
											<h:outputText value="#{msgs.event_num_slot_avail_for_signup}" />
											<h:inputText  id="numberOfSlot" value="#{EditMeetingSignupMBean.numberOfSlots}" size="2" styleClass="editText" onfocus="showRescheduleWarning();" onkeyup="getSignupDuration(); delayedValidMimimunTs('#{EditMeetingSignupMBean.numberOfSlots}','#{msgs.event_warning_no_lower_than_cur_ts_num}'); return false;" style="margin-left:12px" />
											<h:outputText value="#{msgs.event_num_participant_per_timeslot}" styleClass="titleText" escape="false"/>                    
											<h:inputText id="numberOfAttendees" value="#{EditMeetingSignupMBean.maxNumOfAttendees}" styleClass="editText" size="2" style="margin-left:12px" onkeyup="validateAttendee();return false;" />
											<h:outputText value="#{msgs.event_duration_each_timeslot_not_bold}" styleClass="titleText" escape="false"/>
											<h:inputText id='currentTimeslotDuration' value="0" styleClass='longtext_red' size="2" onfocus="this.blur();" style="margin-left:12px;" />             
										</h:panelGrid>
									</h:panelGroup>
									<%-- single: --%>
									<h:panelGroup rendered="#{EditMeetingSignupMBean.groupType}">
										<h:panelGrid columns="2" id="singleCh" rendered="true" styleClass="si" columnClasses="miCol1,miCol2">                
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
							</div>
						</h:panelGroup>
					</h:panelGroup>
					
					<%-- User can switch from individual type to custom_ts type --%>
					<h:panelGroup styleClass="form-group row" layout="block"
							rendered="#{!EditMeetingSignupMBean.customTsType && !EditMeetingSignupMBean.announcementType}">
							<h:outputLabel id="userDefTsChoice_1" value="" styleClass="form-control-label col-lg-2"/>
							<div class="col-lg-10" id="userDefTsChoice_2">
								<h:panelGroup>
									<h:selectBooleanCheckbox id="userDefTsChoice" value="#{EditMeetingSignupMBean.userDefinedTS}" onclick="userDefinedTsChoice();" />
									<h:outputLabel for="userDefTsChoice" value="#{msgs.label_custom_timeslots}"  escape="false"/>
								</h:panelGroup>
								<h:panelGroup id="createEditTS" style="display:none;">
									<h:panelGroup>
										<h:commandLink action="#{EditMeetingSignupMBean.editUserDefTimeSlots}" >
											<h:graphicImage value="/images/cal.gif" alt="close" style="border:none;cursor:pointer; padding-right:5px;" styleClass="openCloseImageIcon" />
											<h:outputText value="#{msgs.label_edit_timeslots}" escape="false" styleClass="activeTag"/>
										</h:commandLink>
									</h:panelGroup>
								</h:panelGroup>	
							</div>
					</h:panelGroup>
					
					<%-- Edit custom defined TS --%>
					<h:panelGroup rendered="#{EditMeetingSignupMBean.customTsType}" layout="block" styleClass="form-group row">
						<h:outputLabel value="#{msgs.event_show_schedule}" styleClass="form-control-label col-lg-2" />
						<div class="col-lg-10">
							<h:commandLink action="#{EditMeetingSignupMBean.editUserDefTimeSlots}" >
								<h:graphicImage value="/images/cal.gif" alt="close" style="border:none;cursor:pointer; padding-right:5px;" styleClass="openCloseImageIcon" />
								<h:outputText value="#{msgs.label_view_edit_ts}" escape="false" styleClass="activeTag"/>
							</h:commandLink>
						</div>
					</h:panelGroup>

					<%-- Show Participants To Public --%>
					<h:panelGroup rendered="#{!EditMeetingSignupMBean.announcementType}" layout="block" styleClass="form-group row">
						<h:outputLabel value="#{msgs.event_show_attendee_public}" styleClass="form-control-label col-lg-2" escape="false"/>
						<div class="col-lg-10">
							<h:selectBooleanCheckbox value="#{EditMeetingSignupMBean.showAttendeeName}"/>
							<h:outputText value="#{msgs.event_yes_show_attendee_public}" escape="false"/>
						</div>
					</h:panelGroup>
					
					<%--  Notifications of participant actions --%>
					<h:panelGroup rendered="#{!EditMeetingSignupMBean.announcementType}" layout="block" styleClass="form-group row">
						<h:outputLabel value="#{msgs.event_receive_notification}" styleClass="form-control-label col-lg-2" escape="false"/>
						<div class="col-lg-10">
							<h:selectBooleanCheckbox id="receiveemailbyowner" value="#{EditMeetingSignupMBean.signupMeeting.receiveEmailByOwner}"/>
							<h:outputLabel for="receiveemailbyowner" value="#{msgs.event_yes_receive_notification}" escape="false"/>						
						</div>
					</h:panelGroup>
					
					<%-- Meeting Coordinators --%>
					<h:panelGroup  layout="block" styleClass="form-group row">
						<h:outputLabel value="#{msgs.event_select_coordinators}" escape="false"  styleClass="form-control-label col-lg-2"/>
						<div class="col-lg-10">
							<h:dataTable id="meeting_coordinators" value="#{EditMeetingSignupMBean.allPossibleCoordinators}" var="coUser">
								<h:column>
									<h:selectBooleanCheckbox id="meetingcoorduser" value="#{coUser.checked}"/>
									<h:outputLabel for="meetingcoorduser" value="&nbsp;#{coUser.displayName}" escape="false" styleClass="longtext"/>
								</h:column>
							</h:dataTable>
						</div>
					</h:panelGroup>
					
					<%-- Email notification  --%>
					<div class="form-group row">
						<h:outputLabel value="#{msgs.event_email_notification}" styleClass="col-lg-2 form-control-label" escape="false"/>
						<h:panelGroup layout="block" styleClass="col-lg-10" rendered="#{EditMeetingSignupMBean.publishedSite}">
							<h:panelGroup layout="block" >
								<h:selectBooleanCheckbox id="emailChoice" value="#{EditMeetingSignupMBean.sendEmail}" onclick="isShowEmailChoice()"/>
								<h:outputLabel for="emailChoice" value="#{msgs.event_yes_email_notification_changes}" escape="false"/>
							</h:panelGroup>
								
							<h:panelGroup layout="block" id="emailAttendeeOnly">
								<h:selectOneRadio  value="#{EditMeetingSignupMBean.sendEmailToSelectedPeopleOnly}" layout="pageDirection" styleClass="rs" style="margin-left:20px;">
									<f:selectItem id="all_attendees" itemValue="all" itemLabel="#{msgs.label_email_all_people}" itemDisabled="true"/>
									<f:selectItem id="only_signedUp_ones" itemValue="signup_only" itemLabel="#{msgs.label_email_signed_up_ones_only}" itemDisabled="true"/>
									<f:selectItem id="only_organizers" itemValue="organizers_only" itemLabel="#{msgs.label_email_organizers_only}" itemDisabled="true"/>
								</h:selectOneRadio>
							</h:panelGroup>
						</h:panelGroup>
						<h:panelGroup styleClass="col-lg-10" layout="block" rendered="#{!EditMeetingSignupMBean.publishedSite}">
							<h:selectBooleanCheckbox id="sendemail" value="#{EditMeetingSignupMBean.sendEmail}" disabled="true"/>
							<h:outputLabel for="sendemail" value="#{msgs.event_email_not_send_out_label}" escape="false" style="color:#b11"/>
						</h:panelGroup>
					</div>
					
					<%--  Other Default Settings --%>
					<h:panelGroup styleClass="form-group row" layout="block" rendered="#{!EditMeetingSignupMBean.announcementType}">
						<h:outputLabel value="#{msgs.event_other_default_setting}" escape="false" styleClass="col-lg-2 form-control-label" />
						<div class="col-lg-10">
							<h:outputLabel  id="imageOpen_otherSetting" style="display:none" styleClass="activeTag" onclick="showDetails('meeting:imageOpen_otherSetting','meeting:imageClose_otherSetting','meeting:otherSetting');">
								<h:graphicImage value="/images/open.gif"  alt="open" title="#{msgs.event_tool_tips_hide_details}" style="border:none;vertical-align:middle;" styleClass="openCloseImageIcon"/>
								<h:outputText value="#{msgs.event_close_other_default_setting}" escape="false" style="vertical-align: middle;"/>
							</h:outputLabel>
							<h:outputLabel id="imageClose_otherSetting" styleClass="activeTag" onclick="showDetails('meeting:imageOpen_otherSetting','meeting:imageClose_otherSetting','meeting:otherSetting');">
								<h:graphicImage value="/images/closed.gif" alt="close" title="#{msgs.event_tool_tips_show_details}" style="border:none;vertical-align:middle;" styleClass="openCloseImageIcon"/>
								<h:outputText value="#{msgs.event_show_other_default_setting}" escape="false" style="vertical-align: middle;"/>
							</h:outputLabel>
						</div>
					</h:panelGroup>
					
					<%-- Allow wait list --%>
					<h:panelGroup styleClass="form-group row" layout="block" rendered="#{!EditMeetingSignupMBean.announcementType}">
						<h:outputLabel id="otherSetting_1" style="display:none"  value="#{msgs.event_allow_waitList}" 
									styleClass="col-lg-2 form-control-label" escape="false"/>
						<h:panelGroup id="otherSetting_2" style="display:none" styleClass="col-lg-10" layout="block">
							<h:selectBooleanCheckbox value="#{EditMeetingSignupMBean.signupMeeting.allowWaitList}"/>
							<h:outputText value="#{msgs.event_yes_to_allow_waitList}" escape="false"/>
						</h:panelGroup>
					</h:panelGroup>
					
					<%-- Allow adding comment --%>
					<h:panelGroup styleClass="form-group row" layout="block" rendered="#{!EditMeetingSignupMBean.announcementType}">
						<h:outputLabel id="otherSetting_3" style="display:none"  value="#{msgs.event_allow_addComment}" 
									styleClass="col-lg-2 form-control-label" escape="false"/>
						<h:panelGroup id="otherSetting_4" style="display:none"  styleClass="col-lg-10" layout="block">
							<h:selectBooleanCheckbox id="allowcomment" value="#{EditMeetingSignupMBean.signupMeeting.allowComment}"/>
							<h:outputLabel for="allowcomment" value="#{msgs.event_yes_to_allow_addComment}" escape="false"/>
						</h:panelGroup>
					</h:panelGroup>
					
					<%-- User ID Input Mode --%>
					<h:panelGroup styleClass="form-group row" layout="block" 
						rendered="#{!EditMeetingSignupMBean.announcementType && EditMeetingSignupMBean.userIdInputModeOptionChoice}">
						<h:outputLabel id="otherSetting_5" style="display:none"  value="#{msgs.event_use_eid_input_mode}" 
									styleClass="col-lg-2 form-control-label" escape="false" />
						<h:panelGroup id="otherSetting_6" style="display:none"  styleClass="col-lg-10" layout="block">
							<h:selectBooleanCheckbox id="eidinputmode" value="#{EditMeetingSignupMBean.signupMeeting.eidInputMode}"/>
							<h:outputLabel for="eidinputmode" value="#{msgs.event_yes_to_use_eid_input_mode}" escape="false"/>
						</h:panelGroup>
					</h:panelGroup>

					<%--  Auto Reminder --%>
					<h:panelGroup styleClass="form-group row" layout="block">
						<h:outputLabel id="otherSetting_7" style="display:none" value="#{msgs.event_email_autoReminder}" 
								styleClass="col-lg-2 form-control-label" escape="false" />
						<h:panelGroup id="otherSetting_8" style="display:none"	styleClass="col-lg-10" layout="block">
							<h:selectBooleanCheckbox id="autoreminder" value="#{EditMeetingSignupMBean.signupMeeting.autoReminder}"/>
							<h:outputLabel for="autoreminder" value="#{msgs.event_yes_email_autoReminer_to_attendees}" escape="false"/>
						</h:panelGroup>
					</h:panelGroup>					

					<%--  Publish to Calendar --%>
					<h:panelGroup styleClass="form-group row" layout="block">
						<h:outputLabel id="otherSetting_9" style="display:none" value="#{msgs.event_publish_to_calendar}" 
								styleClass="col-lg-2 form-control-label" escape="false" />
						<h:panelGroup id="otherSetting_10" style="display:none"	styleClass="col-lg-10" layout="block">
							<h:selectBooleanCheckbox id="publishtocalendar" value="#{EditMeetingSignupMBean.publishToCalendar}"/>
							<h:outputLabel for="publishtocalendar" value="#{msgs.event_yes_publish_to_calendar}" escape="false"/>
						</h:panelGroup>
					</h:panelGroup>
					
					<%-- Create groups for timeslots --%>
					<h:panelGroup styleClass="form-group row" layout="block">
						<h:outputLabel id="otherSetting_11" style="display:none" value="#{msgs.event_create_groups}" 
								styleClass="col-lg-2 form-control-label" escape="false" />
						<h:panelGroup id="otherSetting_12" style="display:none" styleClass="col-lg-10" layout="block">
							<h:selectBooleanCheckbox id="creategroups" value="#{EditMeetingSignupMBean.signupMeeting.createGroups}"/>
							<h:outputLabel for="creategroups" value="#{msgs.event_yes_create_groups}" escape="false"/>
						</h:panelGroup>
					</h:panelGroup>
					
					<%--Default Notification setting --%>
					<h:panelGroup styleClass="form-group row" layout="block">
						<h:outputLabel id="otherSetting_13" style="display:none" 
							value="#{msgs.event_meeting_default_notify_setting}" styleClass="col-lg-2 form-control-label" escape="false"/>
						<h:panelGroup id="otherSetting_14" style="display:none" styleClass="col-lg-10" layout="block" >
							<h:selectBooleanCheckbox id="sendemailbyowner" value="#{EditMeetingSignupMBean.sendEmailByOwner}"/>
							<h:outputLabel for="sendemailbyowner" value="#{msgs.event_yes_meeting_default_notify_setting}" escape="false"/>
						</h:panelGroup>							
					</h:panelGroup>
					
					<%-- Max # of time slots per participant --%>
					<h:panelGroup styleClass="form-group row" layout="block">
						<h:outputLabel id="otherSetting_15" style="display:none" value="#{msgs.event_allowed_slots }" 
									styleClass="col-lg-2 form-control-label" escape="false" />
						<h:panelGroup id="otherSetting_16" style="display:none" styleClass="col-lg-10" layout="block">
							<h:selectOneMenu id="signupslots" value="#{ EditMeetingSignupMBean.signupMeeting.maxNumOfSlots}">  
								 <f:selectItems value="#{EditMeetingSignupMBean.slots}" /> 
							</h:selectOneMenu>
							<h:outputLabel for="signupslots" value="#{msgs.event_allowed_slots_comments}" escape="false"/>
						</h:panelGroup>						
					</h:panelGroup>
				</div>

				<sakai:button_bar>
					<h:commandButton id="goNextPage" action="#{EditMeetingSignupMBean.processSaveModify}" actionListener="#{EditMeetingSignupMBean.validateModifyMeeting}" value="#{msgs.public_modify_button}"/> 			
					<h:commandButton id="Cancel" action="#{EditMeetingSignupMBean.doCancelAction}" value="#{msgs.cancel_button}" />  
				</sakai:button_bar>

			 </h:form>
  		</sakai:view_content>	
	</sakai:view_container>
	
</f:view> 
