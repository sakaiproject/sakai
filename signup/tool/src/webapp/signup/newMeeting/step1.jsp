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
        <script type="text/javascript" src="/sakai-signup-tool/js/signupScript.js"></script>
        <script type="text/javascript" src="/sakai-signup-tool/js/newMeetingStep1.js"></script>  
        
    	<script type="text/javascript">
	         //initialization of the page
	         jQuery.noConflict(); //this requried by calendar dropdown tomhawk
	         jQuery(document).ready(function() {
	        	 initialLayoutsSetup();
		         otherUserSitesSelection();
		         replaceCalendarImageIcon(); 
		         userDefinedTsChoice();
		         //setIframeHeight_DueTo_Ckeditor();
		         
		         initDropDownAndInput('meeting:customLocationLabel','meeting:customLocationLabel_undo','meeting:customLocation','meeting:selectedLocation');
		         initDropDownAndInput('meeting:customCategoryLabel','meeting:customCategoryLabel_undo','meeting:customCategory','meeting:selectedCategory');
				
		         sakai.toggleExactDateVisibility();
	        		sakai.initSignupBeginAndEndsExact();
	         });
			 
		</script>

    	
        <sakai:view_content>
     		<h:outputText value="#{msgs.event_error_alerts} #{messageUIBean.errorMessage}" styleClass="alertMessage" escape="false" rendered="#{messageUIBean.error}"/>
            <h:form id="meeting" >              
				<sakai:view_title value="#{msgs.create_new_event} #{msgs.basic}"/>
                <sakai:doc_section>
                    <h:panelGrid columns="1" styleClass="instruction" style="background:#fff;">
                        <h:outputText value="#{msgs.create_instruction} " escape="false" />                      
                        <h:panelGroup>                           
                            <h:outputText value="#{msgs.star_character}" style="color:#B11;" />
                            <h:outputText value="&nbsp;#{msgs.required2}" escape="false" />
                        </h:panelGroup>
                        <h:outputText value="&nbsp;" escape="false" />
                    </h:panelGrid>
                </sakai:doc_section>
          	    
          	    <h:inputHidden id="iframeId" value="#{NewSignupMeetingBean.iframeId}" />
            <div onmouseover="delayedRecalculateDateTime();" class="container-fluid">
                <%-- Title --%>
                <div class="form-group row">
                    <h:outputLabel value="#{msgs.event_name}" for="name" styleClass="col-lg-2 form-control-label form-required"/>
                    <div class="col-lg-10">
                        <h:inputText id="name" size="40" value="#{NewSignupMeetingBean.signupMeeting.title}" 
                                    styleClass="editText form-control" required="true"  >
                            <f:validator validatorId="Signup.EmptyStringValidator"/>
                            <f:validateLength maximum="255" />
                        </h:inputText>
                        <h:message for="name" errorClass="alertMessageInline"/>
                    </div>
                </div>

                <%-- Organiser --%>
                <div class="form-group row">
                    <h:outputLabel value="#{msgs.event_owner}" styleClass="col-lg-2 form-control-label" for="creatorUserId"/>
                    <div class="col-lg-10">
                        <h:selectOneMenu id="creatorUserId" value="#{NewSignupMeetingBean.creatorUserId}">
                            <f:selectItems value="#{NewSignupMeetingBean.instructors}"/>
                        </h:selectOneMenu>
                    </div>
                </div>
                
                <%-- Location --%>
                <div class="form-group row ">
                        <h:outputLabel value="#{msgs.event_location}" styleClass="col-lg-2 form-control-label form-required"/>
                        
                    <div class="col-lg-10">
                        <!-- Displays all the locations in the dropdown -->
                        <h:selectOneMenu id="selectedLocation" value="#{NewSignupMeetingBean.selectedLocation}" rendered="#{!NewSignupMeetingBean.allLocationsEmpty}">
                            <f:validator validatorId="Signup.EmptyStringValidator"/>
                            <f:selectItems value="#{NewSignupMeetingBean.allLocations}"/>
                        </h:selectOneMenu>
                        <h:inputText id="customLocation" size="30" value="#{NewSignupMeetingBean.customLocation}" styleClass="editText form-control" style="display:none">  
                            <f:validateLength maximum="255" />
                        </h:inputText>
                        
                        <h:outputLabel id="customLocationLabel" for="customLocation" styleClass="activeTag"  onclick="handleDropDownAndInput('meeting:customLocationLabel','meeting:customLocationLabel_undo','meeting:customLocation','meeting:selectedLocation')" rendered="#{!NewSignupMeetingBean.allLocationsEmpty}">
                            <h:graphicImage value="/images/plus.gif"  alt="open" title="#{msgs.tab_event_location_custom}" style="border:none;vertical-align: middle; padding:0 5px 0 15px;" styleClass="openCloseImageIcon"/>
                            <h:outputText value="#{msgs.tab_event_location_custom}" escape="false" style="vertical-align: middle;"/>
                        </h:outputLabel>
                        <h:outputLabel id="customLocationLabel_undo" for="customLocation" styleClass="activeTag" style="display:none" onclick="handleDropDownAndInput('meeting:customLocationLabel','meeting:customLocationLabel_undo','meeting:customLocation','meeting:selectedLocation')" rendered="#{!NewSignupMeetingBean.allLocationsEmpty}">
                            <h:graphicImage value="/images/minus.gif"  alt="undo" title="#{msgs.event_custom_undo_tip}" style="border:none;vertical-align: middle;padding:0 5px 0 15px;" styleClass="openCloseImageIcon"/>
                            <h:outputText value="#{msgs.event_custom_undo}" escape="false" style="vertical-align: middle;"/>
                        </h:outputLabel>
                        <h:outputText value="&nbsp;" escape="false" rendered="#{!NewSignupMeetingBean.allLocationsEmpty}"/>

                        <h:message for="customLocation" errorClass="alertMessageInline"/>
                    </div>
                </div>
                <%-- category --%>
                <div class="form-group row">
                    <h:outputLabel value="#{msgs.event_category}" styleClass="col-lg-2 form-control-label"/>
                    <div class="col-lg-10">
                        <!-- Displays all the categories in the dropdown -->
                        <h:selectOneMenu id="selectedCategory" value="#{NewSignupMeetingBean.selectedCategory}"  rendered="#{NewSignupMeetingBean.categoriesExist}">
                            <f:selectItems value="#{NewSignupMeetingBean.allCategories}"/>
                        </h:selectOneMenu>
                        <h:inputText id="customCategory" size="30" value="#{NewSignupMeetingBean.customCategory}" style="display:none" styleClass="editText form-control">  
                            <f:validator validatorId="Signup.EmptyStringValidator"/>
                            <f:validateLength maximum="255" />
                        </h:inputText>
                        
                        <h:outputLabel id="customCategoryLabel" for="customLocation" styleClass="activeTag"  onclick="handleDropDownAndInput('meeting:customCategoryLabel','meeting:customCategoryLabel_undo','meeting:customCategory','meeting:selectedCategory')" rendered="#{NewSignupMeetingBean.categoriesExist}">
                            <h:graphicImage value="/images/plus.gif"  alt="open" title="#{msgs.event_category_custom}" style="border:none;vertical-align: middle; padding:0 5px 0 15px;" styleClass="openCloseImageIcon"/>
                            <h:outputText value="#{msgs.event_category_custom}" escape="false" style="vertical-align: middle;"/>
                        </h:outputLabel>
                        <h:outputLabel id="customCategoryLabel_undo" for="customLocation" styleClass="activeTag" style="display:none" onclick="handleDropDownAndInput('meeting:customCategoryLabel','meeting:customCategoryLabel_undo','meeting:customCategory','meeting:selectedCategory')" rendered="#{NewSignupMeetingBean.categoriesExist}">
                            <h:graphicImage value="/images/minus.gif"  alt="undo" title="#{msgs.event_custom_undo_tip}" style="border:none;vertical-align: middle;padding:0 5px 0 15px;" styleClass="openCloseImageIcon"/>
                            <h:outputText value="#{msgs.event_custom_undo}" escape="false" style="vertical-align: middle;"/>
                        </h:outputLabel>
                        <h:outputText value="&nbsp;" escape="false" rendered="#{NewSignupMeetingBean.categoriesExist}"/>

                        <h:message for="customCategory" errorClass="alertMessageInline"/>
                    </div>
                </div>
                
                <%-- description, rich text --%>
                <div class="form-group row">
                    <h:outputLabel value="#{msgs.event_description}" styleClass="col-lg-12 form-control-label"  escape="false"/>
                    <div class="col-lg-12">
                        <sakai:rich_text_area value="#{NewSignupMeetingBean.signupMeeting.description}"  width="720" height="180" rows="8" columns="80" />
                    </div>
                </div>

                <%-- attachments --%>
                <div>
                    <h:panelGroup>
                        <t:dataTable value="#{NewSignupMeetingBean.attachments}" var="attach" rendered="#{!NewSignupMeetingBean.attachmentsEmpty}">
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

                        <h:commandButton action="#{NewSignupMeetingBean.addRemoveAttachments}" value="#{msgs.add_attachments}" rendered="#{NewSignupMeetingBean.attachmentsEmpty}"/>        
                        <h:commandButton action="#{NewSignupMeetingBean.addRemoveAttachments}" value="#{msgs.add_remove_attachments}" rendered="#{!NewSignupMeetingBean.attachmentsEmpty}"/>                            
                    </h:panelGroup>
                </div>

                <%-- Start time --%>
                <div class="form-group row ">
                    <h:outputLabel value="#{msgs.event_start_time}" for="startTime"  escape="false" styleClass="col-lg-2 form-control-label form-required"/>
                    <div class="col-lg-10">
                       <t:inputDate id="startTime" type="both" ampm="true" value="#{NewSignupMeetingBean.signupMeeting.startTime}" timeZone="#{UserTimeZone.userTimeZoneStr}"
                           style="color:black;" popupCalendar="true" onkeyup="setEndtimeMonthDateYear(); getSignupDuration(); sakai.updateSignupBeginsExact(); return false;" onchange="sakai.updateSignupBeginsExact();">
                       </t:inputDate>
                       <h:message for="startTime" errorClass="alertMessageInline"/>
                    </div>
                </div>

                <%-- End time --%>
                <div class="form-group row ">
                    <h:outputLabel value="#{msgs.event_end_time}" for="endTime" escape="false" styleClass="col-lg-2 form-control-label form-required"/>
                    <div class="col-lg-10">
                        <t:inputDate id="endTime" type="both" ampm="true" value="#{NewSignupMeetingBean.signupMeeting.endTime}" timeZone="#{UserTimeZone.userTimeZoneStr}"
                            style="color:black;" popupCalendar="true" onkeyup="getSignupDuration(); sakai.updateSignupEndsExact(); return false;" onchange="sakai.updateSignupEndsExact();">
                            </t:inputDate>
                        <h:message for="endTime" errorClass="alertMessageInline"/>
                    </div>
                </div>
                
                <%--  Meeting frequency --%>
                <div class="form-group row ">
                    <h:outputLabel value="#{msgs.event_recurrence}" for="recurSelector" styleClass="col-lg-2 form-control-label form-required"/>

                    <div class="col-lg-10">
                        <h:selectOneMenu id="recurSelector" value="#{NewSignupMeetingBean.repeatType}" styleClass="titleText" onchange="isShowCalendar(value); sakai.toggleExactDateVisibility(); return false;">
                            <f:selectItem itemValue="no_repeat" itemLabel="#{msgs.label_once}"/>
                               <f:selectItem itemValue="daily" itemLabel="#{msgs.label_daily}"/>
                               <f:selectItem itemValue="wkdays_mon-fri" itemLabel="#{msgs.label_weekdays}"/>
                               <f:selectItem itemValue="weekly" itemLabel="#{msgs.label_weekly}"/>
                               <f:selectItem itemValue="biweekly" itemLabel="#{msgs.label_biweekly}"/>                           
                        </h:selectOneMenu>
                            
                        <h:panelGroup id="utilCalendar" style="margin-left:35px;">
                            <h:panelGrid columns="2" >
                               <h:outputText value="#{msgs.event_end_after}" style="margin-left:5px" />
                           
                               <h:panelGrid columns="2">
                                    <h:selectOneRadio id="recurNumDateChoice" value="#{NewSignupMeetingBean.recurLengthChoice}" styleClass="titleText" layout="pageDirection" >
                                        <f:selectItem itemValue="0" />
                                        <f:selectItem itemValue="1" />
                                    </h:selectOneRadio>
                                    <h:panelGrid columns="1">
                                        <h:panelGroup id="numOfRepeat" style="margin-left:3px;">
                                            <h:inputText id="numRepeat"  value="#{NewSignupMeetingBean.occurrences}" maxlength="2" size="1" onkeyup="validateRecurNum();" styleClass="untilCalendar" /> 
                                            <h:outputText value="#{msgs.event_occurrences}" style="margin-left:10px" />
                                        </h:panelGroup>
                                        <h:panelGroup id="endOfDate" style="margin-left:3px;">
                                             <!-- t:inputCalendar id="ex" value=""  renderAsPopup="true" monthYearRowClass="" renderPopupButtonAsImage="true" dayCellClass=""   styleClass="untilCalendar"/ -->                                 
                                            <t:inputDate id="until" type="date"  value="#{NewSignupMeetingBean.repeatUntil}"  popupCalendar="true"   styleClass="untilCalendar"/>
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
                    <h:panelGroup layout="block" styleClass="signupBDeadline" id="signup_beginDeadline_1">
                        <h:outputLabel value="#{msgs.event_signup_begins}" styleClass="titleText col-lg-2 form-control-label" for="signupBegins"/>
                    </h:panelGroup>
                    <h:panelGroup layout="block" styleClass="signupBDeadline col-lg-10" id="signup_beginDeadline_2">
                        <h:inputText id="signupBegins" value="#{NewSignupMeetingBean.signupBegins}" size="5" required="true" onkeyup="sakai.updateSignupBeginsExact();">
                            <f:validateLongRange minimum="0" maximum="99999"/>
                        </h:inputText>
                        <h:selectOneMenu id="signupBeginsType" value="#{NewSignupMeetingBean.signupBeginsType}" onchange="isSignUpBeginStartNow(value); sakai.updateSignupBeginsExact();" style="padding-left:5px; margin-right:5px">
                            <f:selectItem itemValue="minutes" itemLabel="#{msgs.label_minutes}"/>
                            <f:selectItem itemValue="hours" itemLabel="#{msgs.label_hours}"/>
                            <f:selectItem itemValue="days" itemLabel="#{msgs.label_days}"/>
                            <f:selectItem itemValue="startNow" itemLabel="#{msgs.label_startNow}"/>
                        </h:selectOneMenu>
                        <h:outputText value="#{msgs.before_event_start}" escape="false" style="margin-left:18px"/>
                        <h:message for="signupBegins" errorClass="alertMessageInline" />

                        <!--  show exact date, based on above -->
                        <h:outputText id="signupBeginsExact" value="" escape="false" styleClass="dateExact" />

                    </h:panelGroup>
                </div>

                <%-- Signup end --%>
                <div class="form-group row">
                    <h:panelGroup layout="block" styleClass="signupBDeadline" id="signup_beginDeadline_3">
                        <h:outputLabel value="#{msgs.event_signup_deadline2}" styleClass="titleText col-lg-2 form-control-label" for="signupDeadline"/>
                   	</h:panelGroup>
                    <h:panelGroup layout="block" styleClass="signupBDeadline col-lg-10" id="signup_beginDeadline_4">
                        <h:inputText id="signupDeadline" value="#{NewSignupMeetingBean.deadlineTime}" size="5" required="true" onkeyup="sakai.updateSignupEndsExact();">
                            <f:validateLongRange minimum="0" maximum="99999"/>
                        </h:inputText>
                        <h:selectOneMenu id="signupDeadlineType" value="#{NewSignupMeetingBean.deadlineTimeType}" onchange="sakai.updateSignupEndsExact();" >
                            <f:selectItem itemValue="minutes" itemLabel="#{msgs.label_minutes}"/>
                            <f:selectItem itemValue="hours" itemLabel="#{msgs.label_hours}"/>
                            <f:selectItem itemValue="days" itemLabel="#{msgs.label_days}"/>
                        </h:selectOneMenu>                
                        <h:outputText value="#{msgs.before_event_end}" escape="false" style="margin-left:18px"/>
                        <h:message for="signupDeadline" errorClass="alertMessageInline" />
                        <!--  Show exact date, based on above -->
                        <h:outputText id="signupEndsExact" value="" escape="false" styleClass="dateExact" />
                    </h:panelGroup>
                </div>
                <%-- Attendance --%>
                <div class="form-group row">
                    <h:outputLabel value="#{msgs.event_signup_attendance}" escape="false" for="attendanceSelection" styleClass="titleText col-lg-2 form-control-label" 
                                   rendered="#{NewSignupMeetingBean.attendanceOn}"/>
                    <h:panelGroup rendered="#{NewSignupMeetingBean.attendanceOn}" layout="block" styleClass="col-lg-10">
                        <h:selectBooleanCheckbox id="attendanceSelection" value="#{NewSignupMeetingBean.signupMeeting.allowAttendance}" />
                        <h:outputLabel value="#{msgs.attend_taken}" for="attendanceSelection" styleClass="titleText"/>
                        <h:outputText value="#{msgs.attend_track_selected}" escape="false" styleClass="textPanelFooter"/>
                    </h:panelGroup>
                </div>

                <%-- Display site/groups --%>
                <div class="form-group row ">

                    <h:outputLabel value ="#{msgs.event_publish_to}" styleClass="col-lg-2 form-control-label form-required"/>

                    <div class="col-lg-10" >
                        <h:panelGroup>
                            <h:selectBooleanCheckbox id="siteSelection" value="#{NewSignupMeetingBean.currentSite.selected}" disabled="#{!NewSignupMeetingBean.currentSite.allowedToCreate}"
                                onclick="currentSiteSelection();"/>
                            <h:outputLabel for="siteSelection" value="#{NewSignupMeetingBean.currentSite.signupSite.title} #{msgs.event_current_site}"/>
                        </h:panelGroup>
                        <h:dataTable id="currentSiteGroups" value="#{NewSignupMeetingBean.currentSite.signupGroupWrappers}" var="currentGroup" styleClass="meetingTypeTable">
                            <h:column>
                                <h:panelGroup>
                                    <h:selectBooleanCheckbox id="groupSelection" value="#{currentGroup.selected}" disabled="#{!currentGroup.allowedToCreate}"/>
                                    <h:outputText value="#{currentGroup.signupGroup.title}" styleClass="longtext"/>
                                </h:panelGroup>
                            </h:column>
                        </h:dataTable>

                        <h:panelGroup rendered="#{NewSignupMeetingBean.otherSitesAvailability}">
                            <h:outputText value="<span id='imageOpen_otherSites' style='display:none'>"  escape="false"/>
                            <h:graphicImage value="/images/minus.gif"  alt="open" style="border:none;cursor:pointer;" styleClass="openCloseImageIcon" onclick="showDetails('imageOpen_otherSites','imageClose_otherSites','otherSites');" />
                            <h:outputText value="</span>" escape="false" />
                            <h:outputText value="<span id='imageClose_otherSites'>"  escape="false"/>
                            <h:graphicImage value="/images/plus.gif" alt="close" style="border:none;cursor:pointer;" styleClass="openCloseImageIcon" onclick="showDetails('imageOpen_otherSites','imageClose_otherSites','otherSites');"/>
                            <h:outputText value="</span>" escape="false" />
                            <h:outputLabel value="#{msgs.event_other_sites}" style='font-weight:bold;cursor:pointer;' onmouseover='style.color=\"blue\"' onmouseout='style.color=\"black\"' onclick="showDetails('imageOpen_otherSites','imageClose_otherSites','otherSites');"/>
                        </h:panelGroup>   
                        <h:panelGroup>
                            <h:outputText value="<div id='otherSites' style='display:none'>" escape="false"/>   
                            <h:dataTable id="userSites" value="#{NewSignupMeetingBean.otherSites}" var="site" styleClass="meetingTypeTable" style="left:1px;">
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
                    </div>
                </div>
                
                <%-- Handle meeting types --%>
                <div class="form-group row ">
                    <h:outputLabel value ="#{msgs.event_type_title}" styleClass="col-lg-2 form-control-label form-required"/>

                    <div class="col-lg-10" >
                        <h:panelGroup id="radios" styleClass="rs">
                            <h:selectOneRadio id="meetingType" value="#{NewSignupMeetingBean.signupMeeting.meetingType}"  valueChangeListener="#{NewSignupMeetingBean.processSelectedType}" onclick="switMeetingType(value);" layout="pageDirection" styleClass="rs" >
                            <f:selectItems value="#{NewSignupMeetingBean.meetingTypeRadioBttns}"/>
                            </h:selectOneRadio> 
                        </h:panelGroup>
                        <div class="table-responsive">
                        <h:panelGrid columns="1" columnClasses="miCol1" styleClass="table" id="meeting-participants">
                            <%-- Multiple: --%>
                            <h:panelGroup>
                                <h:outputText value="<div id='multiple' styleClass='mi' >" escape="false"/>

                                <h:panelGrid columns="2" id="mutipleCh" styleClass="mi" columnClasses="miCol1,miCol2">
                                    <h:outputText id="maxAttendeesPerSlot" style="display:none" value="#{NewSignupMeetingBean.maxAttendeesPerSlot}"></h:outputText>
                                    <h:outputText id="maxSlots" style="display:none" value="#{NewSignupMeetingBean.maxSlots}"></h:outputText>   
                                    <h:outputLabel value="#{msgs.event_num_slot_avail_for_signup}" for="numberOfSlot"/>
                                    <h:inputText  id="numberOfSlot" value="#{NewSignupMeetingBean.numberOfSlots}" size="2" styleClass="editText" onkeyup="getSignupDuration();return false;" style="margin-left:12px" />
                                    <h:outputLabel value="#{msgs.event_num_participant_per_timeslot}" styleClass="titleText" for="numberOfAttendees"/>                    
                                    <h:inputText id="numberOfAttendees" value="#{NewSignupMeetingBean.numberOfAttendees}" styleClass="editText" size="2" style="margin-left:12px" onkeyup="validateAttendee();return false;" />
                                    <h:outputLabel value="#{msgs.event_duration_each_timeslot_not_bold}" styleClass="titleText" for="currentTimeslotDuration"/>
                                    <h:inputText id='currentTimeslotDuration' value="0" styleClass='longtext_red' size="2" onfocus="this.blur();" style="margin-left:12px" />             
                                </h:panelGrid>

                                <h:outputText value="</div>" escape="false" />
                            </h:panelGroup>
                            
                            <%-- Single: --%>
                            <h:panelGroup>
                                <h:outputText value="<div id='single' style='display:none' styleClass='si' >" escape="false"/>
                                <h:panelGrid columns="2" rendered="true" styleClass="si" columnClasses="miCol1,miCol2">
                                    <h:selectOneRadio id="groupSubradio" value="#{NewSignupMeetingBean.unlimited}" valueChangeListener="#{NewSignupMeetingBean.processGroup}" onclick="switchSingle(value)" styleClass="meetingRadioBtn" layout="pageDirection">
                                        <f:selectItem itemValue="#{false}" itemLabel="#{msgs.tab_max_attendee}"/>
                                        <f:selectItem itemValue="#{true}" itemLabel="#{msgs.unlimited_num_attendee}"/>
                                    </h:selectOneRadio>

                                    <h:panelGrid columns="1">
                                        <h:panelGroup rendered="true" styleClass="meetingMaxAttd">
                                            <h:outputLabel value="#{msgs.tab_max_attendee}" styleClass="titleText skip" for="maxAttendee"/>
                                            <h:inputText id="maxAttendee" value="#{NewSignupMeetingBean.maxOfAttendees}" size="2" styleClass="editText" onkeyup="validateParticipants();return false;"/>                                     
                                        </h:panelGroup>
                                        <h:outputText value="&nbsp;" styleClass="titleText" escape="false"/>
                                    </h:panelGrid>
                                </h:panelGrid>
                                <h:outputText value="</div>" escape="false" />
                            </h:panelGroup>
                            <h:outputText id="announ" value="&nbsp;" style='display:none' styleClass="titleText" escape="false"/>
                        </h:panelGrid>
                        </div>
                    </div>
                </div>

                <%-- User defined timeslots --%>
                <div class="form-group row" id="userdef-add">
                    <h:outputLabel id="userDefTsChoice_1" value="" style="display:none;" styleClass="col-lg-2 form-control-label"/>
                    <h:panelGroup id="userDefTsChoice_2" style="display:none;" styleClass="col-lg-10" layout="block">
                        <h:panelGrid>
                            <h:panelGroup>
                                <h:selectBooleanCheckbox id="userDefTsChoice" value="#{NewSignupMeetingBean.userDefinedTS}" onclick="userDefinedTsChoice();" />
                                <h:outputText value="#{msgs.label_custom_timeslots}"  escape="false"/>
                            </h:panelGroup>
                            <h:panelGroup id="createEditTS" style="display:none;padding-left:35px;">
                                <h:commandLink id="createTS" action="#{NewSignupMeetingBean.createUserDefTimeSlots}" rendered="#{!NewSignupMeetingBean.userDefineTimeslotBean.userEverCreateCTS}">
                                    <h:graphicImage value="/images/cal.gif" alt="close" style="border:none;cursor:pointer; padding-right:5px;" styleClass="openCloseImageIcon" />
                                    <h:outputText value="#{msgs.label_create_timeslots}" escape="false" styleClass="activeTag"/>
                                </h:commandLink>
                                <h:panelGroup rendered="#{NewSignupMeetingBean.userDefineTimeslotBean.userEverCreateCTS}">
                                    <h:commandLink action="#{NewSignupMeetingBean.editUserDefTimeSlots}" >
                                        <h:graphicImage value="/images/cal.gif" alt="close" style="border:none;cursor:pointer; padding-right:5px;" styleClass="openCloseImageIcon" />
                                        <h:outputText value="#{msgs.label_edit_timeslots}" escape="false" styleClass="activeTag"/>
                                    </h:commandLink>
                                </h:panelGroup>
                            </h:panelGroup> 
                        </h:panelGrid>  
                    </h:panelGroup>
                </div>
            </div>


				<%--  form buttons --%>	        	
				<h:panelGrid style="margin-top:10px">   
					<h:inputHidden value="step1" binding="#{NewSignupMeetingBean.currentStepHiddenInfo}"/>
					<sakai:button_bar>
						<h:commandButton id="goNextPage"  onclick="validateMeetingType()" action="#{NewSignupMeetingBean.goNext}" actionListener="#{NewSignupMeetingBean.validateNewMeeting}"   value="#{msgs.next_button}"/>
						<h:commandButton id="Cancel" action="#{NewSignupMeetingBean.processCancel}" value="#{msgs.cancel_button}"  immediate="true"/> 
					</sakai:button_bar>
				</h:panelGrid>           
			</h:form>
		</sakai:view_content> 
	</sakai:view_container>

</f:view>

