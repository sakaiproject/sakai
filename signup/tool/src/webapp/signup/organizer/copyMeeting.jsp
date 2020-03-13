<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t" %>

<f:view>
	<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
	   <jsp:setProperty name="msgs" property="baseName" value="messages"/>
	</jsp:useBean>
	<sakai:view_container title="Signup Tool">
		<style type="text/css">
			@import url("/sakai-signup-tool/css/signupStyle.css");
		</style>
<h:outputText value="#{Portal.latestJQuery}" escape="false"/>
        <script src="/library/js/lang-datepicker/lang-datepicker.js"></script>
		<script src="/sakai-signup-tool/js/signupScript.js"></script>

		<script>

		     var timeslotTag; 
		     var maxAttendeeTag;
		     var originalTsVal; 
		   	 var originalMaxAttendees;
		   	 var keepAttendeesTag;
		   	 var groupMaxAttendeeTag;
		   	 var origGroupMaxAttendees;
		   	 var untilCalendarTag;
		   	 
		   	 //init signupBegin
			 var signupBeginTag;
    	 	//initialization of the page
    	 	jQuery(document).ready(function() {

                localDatePicker({
                    input: '#meeting\\:startTime',
                    useTime: 1,
                    parseFormat: 'YYYY-MM-DD HH:mm:ss',
                    allowEmptyDate: false,
                    val: '<h:outputText value="#{CopyMeetingSignupMBean.signupMeeting.startTime}"><f:convertDateTime pattern="yyyy-MM-dd HH:mm:ss" timeZone="#{UserTimeZone.userTimeZone}" /></h:outputText>',
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
                    val: '<h:outputText value="#{CopyMeetingSignupMBean.signupMeeting.endTime}"><f:convertDateTime pattern="yyyy-MM-dd HH:mm:ss" timeZone="#{UserTimeZone.userTimeZone}" /></h:outputText>',
                    ashidden: {
                            iso8601: 'endTimeISO8601',
                            month:"meeting_endTime_month",
                            day:"meeting_endTime_day",
                            year:"meeting_endTime_year",
                            hour:"meeting_endTime_hours",
                            minute:"meeting_endTime_minutes",
                            ampm:"meeting_endTime_ampm"}
                });

                localDatePicker({
                    input: '#meeting\\:until',
                    useTime: 0,
                    parseFormat: 'YYYY-MM-DD',
                    allowEmptyDate: false,
                    val: '<h:outputText value="#{CopyMeetingSignupMBean.repeatUntilString}"><f:convertDateTime pattern="yyyy-MM-dd" timeZone="#{UserTimeZone.userTimeZone}" /></h:outputText>',
                    ashidden: {
                            iso8601: 'untilISO8601',
                            month:"meeting_until_month",
                            day:"meeting_until_day",
                            year:"meeting_until_year"}
                });

    	 		timeslotTag = document.getElementById("meeting:numberOfSlot");
   	         	maxAttendeeTag = document.getElementById("meeting:numberOfAttendees");
   	         	originalTsVal = timeslotTag? timeslotTag.value : 0;
   	    	 	originalMaxAttendees = maxAttendeeTag? maxAttendeeTag.value : 0;
   	    	 	keepAttendeesTag = document.getElementById('meeting:keepAttendees');
   	    	 	groupMaxAttendeeTag = document.getElementById('meeting:maxAttendee');
   	    	 	origGroupMaxAttendees = groupMaxAttendeeTag? groupMaxAttendeeTag.value : 0;
   	    	 	untilCalendarTag = document.getElementById('meeting:utilCalendar');
    	    	 
	   	    	 //init signupBegin
	   			 signupBeginTag = document.getElementById("meeting:signupBeginsType");
	   			 if(signupBeginTag)
	   			 	isSignUpBeginStartNow(signupBeginTag.value);
   	 		
				 currentSiteSelection();
	   	         otherUserSitesSelection();	         
	   	         initGroupTypeRadioButton();
	   	         userDefinedTsChoice();
	   	         isShowEmailChoice();
		         //setIframeHeight_DueTo_Ckeditor();
		         
	   	      	 initDropDownAndInput('meeting:customLocationLabel','meeting:customLocationLabel_undo','meeting:customLocation','meeting:selectedLocation');
			     initDropDownAndInput('meeting:customCategoryLabel','meeting:customCategoryLabel_undo','meeting:customCategory','meeting:selectedCategory');
				
			     initRepeatCalendar();
		    	 isShowAssignToAllChoice();
		    	 
		    	 sakai.initSignupBeginAndEndsExact();

                var menuLink = $('#signupMainMenuLink');
                menuLink.addClass('current');
                menuLink.html(menuLink.find('a').text());

    	 	});
    	 				 	         	         	    	 
	         function alertTruncatedAttendees(alertMsg,hasTruncated){         	
	         	if(!keepAttendeesTag || keepAttendeesTag && !keepAttendeesTag.checked)
	         		return true;
	         	
	         	
				var groupRadiosTag = getElementsByName_iefix("input","meeting:groupSubradio");
				if(groupRadiosTag){
					if(groupRadiosTag[0] && !groupRadiosTag[0].checked)//unlimited
						return true;												
				}
	
	         	if (!timeslotTag && !maxAttendeeTag && !groupRadiosTag)
	         		return true;
	         	
	         	if (hasTruncated=='true' || timeslotTag && (timeslotTag.value < originalTsVal) || maxAttendeeTag && (maxAttendeeTag.value < originalMaxAttendees) 
	         			|| groupMaxAttendeeTag && (groupMaxAttendeeTag.value < origGroupMaxAttendees) ){
	         		return confirm(alertMsg);
	         		}
	         	
	         	return true;
	         } 
	         
	         function isShowAssignToAllChoice(){
	        	var assignToAllTag = document.getElementById('meeting:assignToAllRecurrences');
				var keepAttendeeCheckBoxTag = document.getElementById('meeting:keepAttendees');
				var assignToAllChkBoxTag = document.getElementById('meeting:assignToAllChkBox');	


        		if(!assignToAllTag || !keepAttendeeCheckBoxTag || !assignToAllChkBoxTag || !untilCalendarTag)
        			return;
			
        		if(!keepAttendeeCheckBoxTag.checked || 
					keepAttendeeCheckBoxTag.checked && untilCalendarTag.style.display =="none"){
        			assignToAllTag.style.display = "none";
        				
        			if(!keepAttendeeCheckBoxTag.checked)				
						assignToAllChkBoxTag.checked = false;
						
				}else if(untilCalendarTag.style.display !="none"){
        				assignToAllTag.style.display = "";
        		}
	        }
	        
	        function isCopyRecurEvents(value){
	        	var recurWarnTag1 = document.getElementById('meeting:recurWarnLabel_1');
	        	var recurWarnTag2 = document.getElementById('meeting:recurWarnLabel_2');
	        	if(recurWarnTag1 && recurWarnTag2){
		        	if(value == 'no_repeat'){
		        		recurWarnTag1.style.display="none";
		        		recurWarnTag2.style.display="none";
	        		}
	        		else{
	        			recurWarnTag1.style.display="";
		        		recurWarnTag2.style.display="";
	        		}
        		}
	        }
	        
	        function initRepeatCalendar(){
				var recurSelectorTag = document.getElementById("meeting:recurSelector");
				if( recurSelectorTag && recurSelectorTag.value !='no_repeat')
					document.getElementById('meeting:utilCalendar').style.display="";
				else
					document.getElementById('meeting:utilCalendar').style.display="none";
			}
			
		</script>
		<sakai:view_content>
			<h:outputText id="iframeId" value="#{CopyMeetingSignupMBean.iframeId}" style="display:none"/>
			<h:form id="meeting">
				<%@ include file="/signup/menu/signupMenu.jsp" %>
				<h:outputText value="#{msgs.event_error_alerts} #{messageUIBean.errorMessage}" styleClass="alertMessage" escape="false" rendered="#{messageUIBean.error}"/>
				<div class="page-header">
					<sakai:view_title value="#{msgs.event_copy_meeting_page_title}"/>
				</div>
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

					<%-- Title --%>
					<div class="form-group row ">
						<h:outputLabel value="#{msgs.event_name}" for="meetingTitle" escape="false" 
									styleClass="col-lg-2 form-required"/>
						<div class="col-lg-10">
							<h:inputText id="meetingTitle" value="#{CopyMeetingSignupMBean.title}" 
								size="40" styleClass="editText form-control">
								<f:validateLength maximum="255" />
							</h:inputText>
							<h:message for="meetingTitle" errorClass="alertMessageInline"/>
						</div>
					</div>
					
					<%-- Organiser --%>
					<div class="form-group row">
						<h:outputLabel value="#{msgs.event_owner}" for="creatorUserId" 
								styleClass="col-lg-2" escape="false"/>
						<div class="col-lg-10">
						 	<h:selectOneMenu id="creatorUserId" value="#{CopyMeetingSignupMBean.creatorUserId}">
								<f:selectItems value="#{CopyMeetingSignupMBean.instructors}"/>
							</h:selectOneMenu>
						</div>
					</div>

					<%-- Location --%>
					<div class="form-group row ">
						<h:outputLabel value="#{msgs.event_location}"  escape="false" styleClass="col-lg-2 form-required" for="selectedLocation" />

						<div class="col-lg-10">
							<!-- Displays all the locations in the dropdown -->
							<h:selectOneMenu id="selectedLocation" value="#{CopyMeetingSignupMBean.selectedLocation}">
								<f:selectItems value="#{CopyMeetingSignupMBean.allLocations}"/>
							</h:selectOneMenu>
							<h:inputText id="customLocation" size="35" value="#{CopyMeetingSignupMBean.customLocation}" style="display:none" styleClass="editText">  
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

					<%--Category --%>
					<div class="form-group row">
						<h:outputLabel value="#{msgs.event_category}"  escape="false" styleClass="col-lg-2" for="selectedCategory" />

						<div class="col-lg-10">
							<!-- Displays all the categories in the dropdown -->
							<h:selectOneMenu id="selectedCategory" value="#{CopyMeetingSignupMBean.selectedCategory}">
								<f:selectItems value="#{CopyMeetingSignupMBean.allCategories}"/>
							</h:selectOneMenu>
							<h:inputText id="customCategory" size="35" value="#{CopyMeetingSignupMBean.customCategory}" style="display:none" styleClass="editText">  
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

					<%--Description --%>
					<div class="form-group row">
						<h:outputLabel value="#{msgs.event_description}" styleClass="col-lg-12" escape="false"/>
						<div class="col-lg-12">
							<sakai:rich_text_area value="#{CopyMeetingSignupMBean.signupMeeting.description}"
								 width="720" height="200" rows="5" columns="80"/>
						</div>
					</div>

					<%-- attachments --%>
					<div>
						<h:panelGroup >
							<t:dataTable value="#{CopyMeetingSignupMBean.signupMeeting.signupAttachments}" var="attach" rendered="#{!CopyMeetingSignupMBean.signupAttachmentEmpty}">
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
								<h:commandButton action="#{CopyMeetingSignupMBean.addRemoveAttachments}" value="#{msgs.add_attachments}" rendered="#{CopyMeetingSignupMBean.signupAttachmentEmpty}"/>		
								<h:commandButton action="#{CopyMeetingSignupMBean.addRemoveAttachments}" value="#{msgs.add_remove_attachments}" rendered="#{!CopyMeetingSignupMBean.signupAttachmentEmpty}"/>		         			
						</h:panelGroup>
					</div>

					<%-- Start time --%>
					<div class="form-group row ">
						<h:outputLabel value="#{msgs.event_start_time}"  styleClass="col-lg-2 form-required" escape="false"/>
						<h:panelGroup styleClass="editText col-lg-10" rendered="#{!CopyMeetingSignupMBean.customTsType}" layout="block" >
							<h:inputText value="#{CopyMeetingSignupMBean.startTimeString}" size="28" id="startTime" 
								onkeyup="setEndtimeMonthDateYear();getSignupDuration();sakai.updateSignupBeginsExact();return false;"
								onchange="sakai.updateSignupBeginsExact();"/>
							<h:message for="startTime" errorClass="alertMessageInline"/>
						</h:panelGroup>
						<h:panelGroup rendered="#{CopyMeetingSignupMBean.customTsType}" layout="block" styleClass="col-lg-6">
							<h:outputText value="#{CopyMeetingSignupMBean.signupMeeting.startTime}" styleClass="longtext">
								<f:convertDateTime pattern="#{UserLocale.fullDateTimeFormat}" timeZone="#{UserTimeZone.userTimeZone}"/>
							</h:outputText>		
						</h:panelGroup>
					</div>

					<%-- End time --%>
					<div class="form-group row ">
						<h:outputLabel value="#{msgs.event_end_time}" escape="false" styleClass="col-lg-2 form-required"/>
						<h:panelGroup styleClass="editText col-lg-10" rendered="#{!CopyMeetingSignupMBean.customTsType}" layout="block">
							<h:inputText value="#{CopyMeetingSignupMBean.endTimeString}" size="28" id="endTime" 
								onkeyup="getSignupDuration(); sakai.updateSignupEndsExact(); return false;" onchange="sakai.updateSignupEndsExact();"/>
							<h:message for="endTime" errorClass="alertMessageInline"/>
						</h:panelGroup>
						<h:panelGroup rendered="#{CopyMeetingSignupMBean.customTsType}" styleClass="col-lg-6" layout="block">
							<h:outputText value="#{CopyMeetingSignupMBean.signupMeeting.endTime}" styleClass="longtext">
								<f:convertDateTime pattern="#{UserLocale.fullDateTimeFormat}" timeZone="#{UserTimeZone.userTimeZone}"/>
							</h:outputText>
						</h:panelGroup>
					</div>

					<div class="row">
						<h:outputText id="recurWarnLabel_1" value="" escape="false" rendered="#{!CopyMeetingSignupMBean.repeatTypeUnknown}"/>
						<h:outputText id="recurWarnLabel_2" value="#{msgs.warn_copy_recurred_event}" styleClass="alertMessage" 
									 escape="false" rendered="#{!CopyMeetingSignupMBean.repeatTypeUnknown}"/>
					</div>
					<%--  Meeting frequency --%>
					<div class="form-group row">
						<h:outputLabel styleClass="col-lg-2" value="#{msgs.event_recurrence}" />
						<div class="col-lg-10">
							<h:selectOneMenu id="recurSelector" value="#{CopyMeetingSignupMBean.repeatType}" styleClass="titleText" onchange="isShowCalendar(value); isShowAssignToAllChoice(); isCopyRecurEvents(value); return false;">
								<f:selectItem itemValue="no_repeat" itemLabel="#{msgs.label_once}"/>
								<f:selectItem itemValue="daily" itemLabel="#{msgs.label_daily}"/>
								<f:selectItem itemValue="wkdays_mon-fri" itemLabel="#{msgs.label_weekdays}"/>
								<f:selectItem itemValue="weekly" itemLabel="#{msgs.label_weekly}"/>
								<f:selectItem itemValue="biweekly" itemLabel="#{msgs.label_biweekly}"/>                           
							</h:selectOneMenu>

							<h:panelGroup id="utilCalendar" style="margin-left:35px;display:none;">
								<h:panelGrid columns="2" >
									<h:outputText value="#{msgs.event_end_after}" style="margin-left:5px" />
										<h:panelGrid columns="2">
											<h:selectOneRadio id="recurNumDateChoice" value="#{CopyMeetingSignupMBean.recurLengthChoice}" styleClass="titleText" layout="pageDirection" >
												<f:selectItem itemValue="0" />
												<f:selectItem itemValue="1" />
											</h:selectOneRadio>
											<h:panelGrid columns="1">
												<h:panelGroup id="numOfRepeat" style="margin-left:3px;">
													<h:inputText id="numRepeat"  value="#{CopyMeetingSignupMBean.occurrences}" maxlength="2" size="1" onkeyup="validateRecurNum();" styleClass="untilCalendar" /> 
													<h:outputText value="#{msgs.event_occurrences}" style="margin-left:10px" />
												</h:panelGroup>
												<h:panelGroup id="endOfDate" style="margin-left:3px;">
													<!-- t:inputCalendar id="ex" value=""  renderAsPopup="true" monthYearRowClass="" renderPopupButtonAsImage="true" dayCellClass=""   styleClass="untilCalendar"/ -->             					
												<h:inputText value="#{CopyMeetingSignupMBean.repeatUntilString}" size="28" id="until" />
													<h:message for="until" errorClass="alertMessageInline" style="margin-left:10px" /> 
												</h:panelGroup>
											</h:panelGrid>
										</h:panelGrid> 
								</h:panelGrid>
							</h:panelGroup>
						</div>
					</div>

					<%-- Signup begin --%>
					<div class="form-group row">
						<h:outputLabel value="#{msgs.event_signup_start}" styleClass="col-lg-2"
								rendered="#{!CopyMeetingSignupMBean.announcementType}" escape="false"/>
						<h:panelGroup layout="block" rendered="#{!CopyMeetingSignupMBean.announcementType}" styleClass="col-lg-10">
							<h:panelGroup>
								<h:inputText id="signupBegins" value="#{CopyMeetingSignupMBean.signupBegins}" size="3" required="true" onkeyup="sakai.updateSignupBeginsExact();">
									<f:validateLongRange minimum="0" maximum="99999"/>
								</h:inputText>
								<h:selectOneMenu id="signupBeginsType" value="#{CopyMeetingSignupMBean.signupBeginsType}" onchange="isSignUpBeginStartNow(value);sakai.updateSignupBeginsExact();">
									<f:selectItem itemValue="minutes" itemLabel="#{msgs.label_minutes}"/>
									<f:selectItem itemValue="hours" itemLabel="#{msgs.label_hours}"/>
									<f:selectItem itemValue="days" itemLabel="#{msgs.label_days}"/>
									<f:selectItem itemValue="startNow" itemLabel="#{msgs.label_startNow}"/>
								</h:selectOneMenu>
							</h:panelGroup>
							<h:panelGroup style="margin-left:18px">
								<h:outputText value="#{msgs.before_event_start}" />
								<h:message for="signupBegins" errorClass="alertMessageInline"/>
									<!--  show exact date, based on above -->
								<h:outputText id="signupBeginsExact" value="" escape="false" styleClass="dateExact" />
							</h:panelGroup>
						</h:panelGroup>
					</div>
					
					<%-- Signup end --%>
					<div class="form-group row">
						<h:outputLabel value="#{msgs.event_signup_deadline}" styleClass="col-lg-2"
								rendered="#{!CopyMeetingSignupMBean.announcementType}" escape="false"/>
						<h:panelGroup layout="block" styleClass="col-lg-10" rendered="#{!CopyMeetingSignupMBean.announcementType}">
							<h:panelGroup>
								<h:inputText id="signupDeadline" value="#{CopyMeetingSignupMBean.deadlineTime}" size="3" required="true" onkeyup="sakai.updateSignupEndsExact();">
									<f:validateLongRange minimum="0" maximum="99999"/>
								</h:inputText>
								<h:selectOneMenu id="signupDeadlineType" value="#{CopyMeetingSignupMBean.deadlineTimeType}" onchange="sakai.updateSignupEndsExact();">
									<f:selectItem itemValue="minutes" itemLabel="#{msgs.label_minutes}"/>
									<f:selectItem itemValue="hours" itemLabel="#{msgs.label_hours}"/>
									<f:selectItem itemValue="days" itemLabel="#{msgs.label_days}"/>
								</h:selectOneMenu>
							</h:panelGroup>
							<h:panelGroup style="margin-left:18px">
								<h:outputText value="#{msgs.before_event_end}" />
								<h:message for="signupDeadline" errorClass="alertMessageInline"/>
								<!--  show exact date, based on above -->
								<h:outputText id="signupEndsExact" value="" escape="false" styleClass="dateExact" />
							</h:panelGroup>
						</h:panelGroup>
					</div>

					<%-- Display site/groups --%>
					<div class="form-group row ">
						<h:outputLabel value ="#{msgs.event_publish_to}" styleClass="col-lg-2 form-required"/>
						<div class="col-lg-10">
							<h:panelGroup rendered="#{CopyMeetingSignupMBean.missingSitGroupWarning}" layout="block">
								<h:panelGrid columns="1">
									<h:outputText value="#{msgs.event_some_orig_sitegroup_unavail_due_to_your_create_permission}" styleClass="alertMessage" 
												escape="false"/>
									<h:panelGroup>	
										<h:outputLabel  id="imageOpen_missingSiteGroup" styleClass="activeTag" onclick="showDetails('meeting:imageOpen_missingSiteGroup','meeting:imageClose_missingSiteGroup','meeting:missingSiteGroups');">
											<h:graphicImage value="/images/open.gif"  alt="open" style="border:none; vertical-align:middle;" styleClass="openCloseImageIcon"/>
											<h:outputText value="#{msgs.event_hide_me_details}" escape="false" />
										</h:outputLabel>
										<h:outputLabel id="imageClose_missingSiteGroup" style="display:none" styleClass="activeTag" onclick="showDetails('meeting:imageOpen_missingSiteGroup','meeting:imageClose_missingSiteGroup','meeting:missingSiteGroups');">
											<h:graphicImage value="/images/closed.gif" alt="close" style="border:none; vertical-align:middle;" styleClass="openCloseImageIcon" />
											<h:outputText value="#{msgs.event_show_me_details}" escape="false" />
										</h:outputLabel>
									</h:panelGroup>
									<%-- display missing ones --%>
									<h:panelGroup id="missingSiteGroups">
										<h:panelGrid columns="2">
											<h:outputText value="#{msgs.event_missing_site}" escape="false" rendered="#{CopyMeetingSignupMBean.missingSitesThere}"/>
											<h:dataTable id="missingSite" value="#{CopyMeetingSignupMBean.missingSites}" var="missingSite" styleClass="meetingTypeTable" rendered="#{CopyMeetingSignupMBean.missingSitesThere}">
												<h:column>
													<h:outputText value="#{missingSite}" style="color:#B11;"/>
												</h:column>
											</h:dataTable>
											<h:outputText value="#{msgs.event_missing_group}" escape="false" rendered="#{CopyMeetingSignupMBean.missingGroupsThere}"/>
											<h:dataTable id="missingGroup" value="#{CopyMeetingSignupMBean.missingGroups}" var="missingGroups" styleClass="meetingTypeTable" rendered="#{CopyMeetingSignupMBean.missingGroupsThere}">
												<h:column>
													<h:outputText value="#{missingGroups}" style="color:#B11;"/>
												</h:column>
											</h:dataTable>
										</h:panelGrid>
									</h:panelGroup>
								</h:panelGrid>
							</h:panelGroup>
							
							<h:panelGroup  layout="block">
								<h:panelGroup>
									<h:selectBooleanCheckbox id="siteSelection" value="#{CopyMeetingSignupMBean.currentSite.selected}" disabled="#{!CopyMeetingSignupMBean.currentSite.allowedToCreate}"
										onclick="currentSiteSelection();"/>
									<h:outputText value="#{CopyMeetingSignupMBean.currentSite.signupSite.title}" styleClass="longtext"/>
									<h:outputText value="#{msgs.event_current_site}" style="margin-left:3px" escape="false"/>
								</h:panelGroup>
								<h:dataTable id="currentSiteGroups" value="#{CopyMeetingSignupMBean.currentSite.signupGroupWrappers}" var="currentGroup" styleClass="meetingTypeTable">
									<h:column>
										<h:panelGroup>
											<h:selectBooleanCheckbox id="groupSelection" value="#{currentGroup.selected}" disabled="#{!currentGroup.allowedToCreate}"/>
											<h:outputText value="#{currentGroup.signupGroup.title}" styleClass="longtext"/>
										</h:panelGroup>
									</h:column>
								</h:dataTable>
								<h:panelGroup>
									<h:outputText value="<span id='imageOpen_otherSites' style='display:none'>"  escape="false"/>
										<h:graphicImage value="/images/minus.gif"  alt="open" style="border:none;cursor:pointer;" styleClass="openCloseImageIcon" onclick="showDetails('imageOpen_otherSites','imageClose_otherSites','otherSites');" />
									<h:outputText value="</span>" escape="false" />
									<h:outputText value="<span id='imageClose_otherSites'>"  escape="false"/>
										<h:graphicImage value="/images/plus.gif" alt="close" style="border:none;cursor:pointer;" styleClass="openCloseImageIcon" onclick="showDetails('imageOpen_otherSites','imageClose_otherSites','otherSites');"/>
									<h:outputText value="</span>" escape="false" />
									<h:outputLabel value="#{msgs.event_other_sites}" style='font-weight:bold; cursor:pointer;' onmouseover='style.color=\"blue\"' onmouseout='style.color=\"black\"' onclick="showDetails('imageOpen_otherSites','imageClose_otherSites','otherSites');" />
								</h:panelGroup>   
								<h:panelGroup>
									<h:outputText value="<div id='otherSites' style='display:none'>" escape="false"/>   
									<h:dataTable id="userSites" value="#{CopyMeetingSignupMBean.otherSites}" var="site" styleClass="meetingTypeTable" style="left:1px;">
										<h:column>
											<h:panelGroup>
												<h:selectBooleanCheckbox id="otherSitesSelection" value="#{site.selected}" disabled="#{!site.allowedToCreate}" onclick="otherUserSitesSelection();"/>
												<h:outputText value="#{site.signupSite.title}" styleClass="editText" escape="false"/>
											</h:panelGroup>
											<h:dataTable id="userGroups" value="#{site.signupGroupWrappers}" var="group" styleClass="meetingTypeTable">
												<h:column>
													<h:panelGroup>
														<h:selectBooleanCheckbox id="otherGroupsSelection" value="#{group.selected}" disabled="#{!group.allowedToCreate}" onclick=""/>
														<h:outputText value="#{group.signupGroup.title}" styleClass="longtext"/>
													</h:panelGroup>
												</h:column>
											</h:dataTable>
										</h:column>
									</h:dataTable>
									<h:outputText value="</div>" escape="false" />
								</h:panelGroup>
							</h:panelGroup>
						</div>
					</div>
					
					<%-- Attendance --%>
					<h:panelGroup styleClass="form-group row" rendered="#{CopyMeetingSignupMBean.attendanceOn}" layout="block">
						<h:outputLabel value="#{msgs.event_signup_attendance}" escape="false" styleClass="col-lg-2"/>
						<div class="col-lg-10">
							<h:selectBooleanCheckbox id="attendanceSelection" value="#{CopyMeetingSignupMBean.signupMeeting.allowAttendance}" />
							<h:outputLabel value="#{msgs.attend_taken}" for="attendanceSelection" styleClass="titleText"/>
							<h:outputText value="#{msgs.attend_track_selected}" escape="false" styleClass="textPanelFooter"/>
						</div>
					</h:panelGroup>

					<%-- Handle meeting types --%>
					<div class="form-group row ">
						<h:outputLabel value ="#{msgs.event_type_title}" styleClass="col-lg-2 form-required"/>
						<div class="col-lg-10">
							<h:outputText value="#{msgs.label_custom_timeslots}"  escape="false" rendered="#{CopyMeetingSignupMBean.customTsType}"/>
							<h:panelGroup rendered="#{!CopyMeetingSignupMBean.customTsType}">                
								<h:panelGroup id="radios" styleClass="rs">
									<h:selectOneRadio id="meetingType" value="#{CopyMeetingSignupMBean.signupMeeting.meetingType}"    layout="pageDirection" styleClass="rs" >
										<f:selectItems value="#{CopyMeetingSignupMBean.meetingTypeRadioBttns}"/>
									</h:selectOneRadio> 
								</h:panelGroup>
								<%-- multiple: --%>
								<h:panelGroup rendered="#{CopyMeetingSignupMBean.individualType}" id="multipleCh" styleClass="mi" layout="block">
									<h:outputText id="maxAttendeesPerSlot" style="display:none" value="#{CopyMeetingSignupMBean.maxAttendeesPerSlot}" />
									<h:outputText id="maxSlots" style="display:none" value="#{CopyMeetingSignupMBean.maxSlots}" />

									<div class="form-group row">
										<h:outputLabel value="#{msgs.event_num_slot_avail_for_signup}" for="numberOfSlot" styleClass="col-lg-4" />
										<h:panelGroup styleClass="col-lg-8" layout="block">
											<h:inputText id="numberOfSlot" value="#{CopyMeetingSignupMBean.numberOfSlots}" size="2" styleClass="editText" onkeyup="getSignupDuration();return false;" />
										</h:panelGroup>
									</div>

									<div class="form-group row">
										<h:outputLabel value="#{msgs.event_num_participant_per_timeslot}" for="numberOfAttendees" styleClass="col-lg-4 titleText" escape="false" />
										<h:panelGroup styleClass="col-lg-8" layout="block">
											<h:inputText id="numberOfAttendees" value="#{CopyMeetingSignupMBean.maxNumOfAttendees}" styleClass="editText" size="2" onkeyup="validateAttendee();return false;" />
										</h:panelGroup>
									</div>

									<div class="form-group row">
										<h:outputLabel value="#{msgs.event_duration_each_timeslot_not_bold}" for="currentTimeslotDuration" styleClass="col-lg-4 titleText" escape="false" />
										<h:panelGroup styleClass="col-lg-8" layout="block">
											<h:inputText id="currentTimeslotDuration" value="0" styleClass='longtext_red' size="2" disabled="true" />
										</h:panelGroup>
									</div>
								</h:panelGroup>

								<%-- single: --%>
								<h:panelGroup rendered="#{CopyMeetingSignupMBean.groupType}" styleClass="si" layout="block">
									<div class="form-group row">
										<t:selectOneRadio id="groupSubradio" value="#{CopyMeetingSignupMBean.unlimited}" onclick="switchSingle(value)" styleClass="meetingRadioBtn" layout="spread">
											<f:selectItem itemValue="#{false}" itemLabel="#{msgs.tab_max_attendee}"/>
											<f:selectItem itemValue="#{true}" itemLabel="#{msgs.unlimited_num_attendee}"/>
										</t:selectOneRadio>
										<div class="form-group row">
											<h:panelGroup styleClass="col-lg-4">
												<t:radio for="groupSubradio" id="radioMaxAttendee" index="0" />
											</h:panelGroup>
											<h:panelGroup styleClass="col-lg-8 meetingMaxAttd" layout="block">
												<h:inputText id="maxAttendee" value="#{CopyMeetingSignupMBean.maxNumOfAttendees}" size="2" styleClass="editText" onkeyup="validateParticipants();return false;"/>
											</h:panelGroup>
										</div>
										<div class="form-group row">
											<h:panelGroup styleClass="col-lg-4">
												<t:radio for="groupSubradio" id="radioUnlimitedAttendee" index="1" />
											</h:panelGroup>
										</div>
									</div>
								</h:panelGroup>
								<h:outputText id="announ" value="&nbsp;" rendered="#{CopyMeetingSignupMBean.announcementType}" styleClass="titleText" escape="false"/>
							</h:panelGroup>
						</div>
					</div>

					<!-- User can switch from individual type to custom_ts type -->
					<h:panelGroup rendered="#{!CopyMeetingSignupMBean.customTsType && !CopyMeetingSignupMBean.announcementType}" 
								styleClass="form-group row" layout="block">
						<h:outputLabel id="userDefTsChoice_1" value="" styleClass="col-lg-2"/>
						<div class="col-lg-10">
							<h:panelGroup id="userDefTsChoice_2"  styleClass="longtext" layout="block">
								<h:panelGrid>
									<h:panelGroup>
										<h:selectBooleanCheckbox id="userDefTsChoice" value="#{CopyMeetingSignupMBean.userDefinedTS}" onclick="userDefinedTsChoice();" />
										<h:outputText value="#{msgs.label_custom_timeslots}"  escape="false"/>
									</h:panelGroup>
									<h:panelGroup id="createEditTS" style="display:none;padding-left:35px;">
										<h:panelGroup>
											<h:commandLink action="#{CopyMeetingSignupMBean.editUserDefTimeSlots}" >
												<h:graphicImage value="/images/cal.gif" alt="close" style="border:none;cursor:pointer; padding-right:5px;" styleClass="openCloseImageIcon" />
												<h:outputText value="#{msgs.label_edit_timeslots}" escape="false" styleClass="activeTag"/>
											</h:commandLink>
										</h:panelGroup>
									</h:panelGroup>	
								</h:panelGrid>	
							</h:panelGroup>
						</div>
					</h:panelGroup>

					<!-- Edit custom defined TS -->
					<h:panelGroup styleClass="form-group row" rendered="#{CopyMeetingSignupMBean.customTsType}" layout="block">
						<h:outputLabel value="#{msgs.event_show_schedule}" styleClass="col-lg-2"/>
						<div class="col-lg-10">
							<h:commandLink action="#{CopyMeetingSignupMBean.editUserDefTimeSlots}" >
								<h:graphicImage value="/images/cal.gif" alt="close" style="border:none;cursor:pointer; padding-right:5px;" styleClass="openCloseImageIcon" />
								<h:outputText value="#{msgs.label_view_edit_ts}" escape="false" styleClass="activeTag"/>
							</h:commandLink>
						</div>
					</h:panelGroup>
					
					<!-- Keep Current participants -->
					<h:panelGroup  rendered="#{!CopyMeetingSignupMBean.announcementType}" layout="block" styleClass="form-group row">
						<h:outputLabel value="#{msgs.event_keep_current_attendees}" styleClass="col-lg-2" escape="false" />
						<div class="col-lg-10">
							<h:panelGrid columns="1" >
								<h:panelGroup styleClass="longtext">
									<h:selectBooleanCheckbox id="keepAttendees" value="#{CopyMeetingSignupMBean.keepAttendees}" onclick="isShowAssignToAllChoice()"/>
									<h:outputText value="#{msgs.event_yes_keep_current_attendees}" escape="false"/>
								</h:panelGroup>
								<h:panelGroup id="assignToAllRecurrences" style="display:none" styleClass="longtext">
									<h:selectBooleanCheckbox  id="assignToAllChkBox" value="#{CopyMeetingSignupMBean.assignParicitpantsToAllRecurEvents}"/>
									<h:outputText value="#{msgs.apply_added_participants_to_allRecur_events}" escape="false"/>
								</h:panelGroup>
							</h:panelGrid>						
						</div>
					</h:panelGroup>
					
					<!-- Publish to calendar -->
					<div class="form-group row">
						<h:outputLabel  value="#{msgs.event_publish_to_calendar}" styleClass="col-lg-2" escape="false" />
						<div class="col-lg-10">
							<h:selectBooleanCheckbox value="#{CopyMeetingSignupMBean.publishToCalendar}"/>
							<h:outputText value="#{msgs.event_yes_publish_to_calendar}" escape="false"/>
						</div>
					</div>

					<!--  Announce Availability -->
					<div class="form-group row">
						<h:outputLabel value="#{msgs.event_create_email_notification}" styleClass="col-lg-2" escape="false"/>
						<h:panelGroup styleClass="col-lg-10" layout="block" rendered="#{!CopyMeetingSignupMBean.publishedSite}">
							<h:selectBooleanCheckbox value="#{CopyMeetingSignupMBean.sendEmail}" disabled="true"/>
							<h:outputText value="#{msgs.event_email_not_send_out_label}" escape="false" style="color:#b11"/>
						</h:panelGroup>
						<h:panelGroup styleClass="col-lg-10" layout="block" rendered="#{CopyMeetingSignupMBean.publishedSite}">
							<h:panelGroup layout="block" >
								<h:selectBooleanCheckbox id="emailChoice" value="#{CopyMeetingSignupMBean.sendEmail}" onclick="isShowEmailChoice()" disabled="#{CopyMeetingSignupMBean.mandatorySendEmail}"/>
								<h:outputText value="#{msgs.event_yes_email_notification}" escape="false"/>
							</h:panelGroup>
							<h:panelGroup id="emailAttendeeOnly" layout="block">
								<h:selectOneRadio  value="#{CopyMeetingSignupMBean.sendEmailToSelectedPeopleOnly}" layout="pageDirection" styleClass="rs" style="margin-left:20px;">
									<f:selectItem id="all_attendees" itemValue="all" itemLabel="#{msgs.label_email_all_people}" itemDisabled="true"/>
									<f:selectItem id="only_organizers" itemValue="organizers_only" itemLabel="#{msgs.label_email_signed_up_ones_Organizers_only}" itemDisabled="true"/>
								</h:selectOneRadio>
							</h:panelGroup>
						</h:panelGroup>
					</div>
				</div>

				<sakai:button_bar>
					<h:commandButton id="copy" styleClass="active" action="#{CopyMeetingSignupMBean.processSaveCopy}" actionListener="#{CopyMeetingSignupMBean.validateCopyMeeting}" value="#{msgs.publish_new_evnt_button}" 
					onclick="return alertTruncatedAttendees('#{msgs.event_alert_truncate_attendee}','#{UserDefineTimeslotBean.truncatedAttendees}')"/> 			
					<h:commandButton id="cancel" action="#{CopyMeetingSignupMBean.doCancelAction}" value="#{msgs.cancel_button}" />  
                </sakai:button_bar>

			 </h:form>
  		</sakai:view_content>
	</sakai:view_container>
	
</f:view> 
