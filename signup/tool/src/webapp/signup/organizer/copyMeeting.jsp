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
			 	<sakai:view_title value="#{msgs.event_copy_meeting_page_title}"/>
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
							<h:panelGroup styleClass="titleText">
								<h:outputText value="#{msgs.star_character}" style="color:#B11;"/>
								<h:outputText value="#{msgs.event_name}"  escape="false"/>
							</h:panelGroup>
							<h:panelGroup>
								<h:inputText id="meetingTitle" value="#{CopyMeetingSignupMBean.signupMeeting.title}" required="true" size="40" styleClass="editText">
									<f:validator validatorId="Signup.EmptyStringValidator"/>
								</h:inputText>
								<h:message for="meetingTitle" errorClass="alertMessageInline"/>
							</h:panelGroup>	
							
							<h:outputText value="#{msgs.event_owner}" styleClass="titleText" escape="false"/>
							<h:outputText  value="#{CopyMeetingSignupMBean.meetingWrapper.creator}" styleClass="longtext"/>
							<h:panelGroup styleClass="titleText">
								<h:outputText value="#{msgs.star_character}" style="color:#B11;"/>
								<h:outputText value="#{msgs.event_location}"  escape="false"/>
							</h:panelGroup>
							<h:panelGroup>
								<h:inputText id="meetingLocation" value="#{CopyMeetingSignupMBean.signupMeeting.location}" required="true" size="40" styleClass="editText">
									<f:validator validatorId="Signup.EmptyStringValidator"/>
								</h:inputText>														
								<h:message for="meetingLocation" errorClass="alertMessageInline"/>
							</h:panelGroup>
							
							<h:outputText value="#{msgs.event_description}" styleClass="titleText" escape="false"/>
							<sakai:rich_text_area value="#{CopyMeetingSignupMBean.signupMeeting.description}" width="720" height="200" rows="5" columns="80"/>
							
							<h:outputText  value="" styleClass="titleText" escape="false" />
		         			<h:panelGrid columns="1">
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
		         			</h:panelGrid>
							
							
							<h:panelGroup styleClass="titleText" style="margin-top: 20px;">
								<h:outputText value="#{msgs.star_character}" style="color:#B11;"/>
								<h:outputText value="#{msgs.event_start_time}"  escape="false"/>
							</h:panelGroup>
							<h:panelGroup styleClass="editText">
		        						<t:inputDate id="startTime" type="both"  ampm="true" value="#{CopyMeetingSignupMBean.signupMeeting.startTime}"
		        							style="color:black;" popupCalendar="true" onkeyup="setEndtimeMonthDateYear();getSignupDuration();return false;"/>
										<h:message for="startTime" errorClass="alertMessageInline"/>
							</h:panelGroup>
							
							<h:panelGroup styleClass="titleText">
								<h:outputText value="#{msgs.star_character}" style="color:#B11;" />					
								<h:outputText value="#{msgs.event_end_time}" escape="false"/>
							</h:panelGroup>
		        			<h:panelGroup styleClass="editText">
		        						<t:inputDate id="endTime" type="both" ampm="true" value="#{CopyMeetingSignupMBean.signupMeeting.endTime}" style="color:black;" popupCalendar="true" 
		        							onkeyup="getSignupDuration();return false;"/>
										<h:message for="endTime" errorClass="alertMessageInline"/>
							</h:panelGroup>
							
							<h:outputText id="recurWarnLabel_1" value="" escape="false" rendered="#{!CopyMeetingSignupMBean.repeatTypeUnknown}"/>
							<h:outputText id="recurWarnLabel_2" value="#{msgs.warn_copy_recurred_event}" styleClass="alertMessage" style="width:95%" escape="false" rendered="#{!CopyMeetingSignupMBean.repeatTypeUnknown}"/>
							
							<h:outputText styleClass="titleText" value="#{msgs.event_recurrence}"  />                                          
		                     <h:panelGroup>                            
		                            <h:selectOneMenu id="recurSelector" value="#{CopyMeetingSignupMBean.repeatType}" styleClass="titleText" onchange="isShowCalendar(value); isShowAssignToAllChoice(); isCopyRecurEvents(value); return false;">
		                                <f:selectItem itemValue="no_repeat" itemLabel="#{msgs.label_once}"/>
		                                <f:selectItem itemValue="daily" itemLabel="#{msgs.label_daily}"/>
		                                <f:selectItem itemValue="wkdays_mon-fri" itemLabel="#{msgs.label_weekdays}"/>
		                                <f:selectItem itemValue="weekly" itemLabel="#{msgs.label_weekly}"/>
		                                <f:selectItem itemValue="biweekly" itemLabel="#{msgs.label_biweekly}"/>                           
		                             </h:selectOneMenu>
		                                                
		                            <h:panelGroup id="utilCalendar" style="margin-left:35px; display:none;">
		                            	<h:outputText value="#{msgs.star_character}" style="color:#B11;" />                    
		                            	<h:outputText value="#{msgs.event_until}" style="font-weight:bold;" styleClass="titleText"/>
		                                <t:inputDate id="until" type="date"  value="#{CopyMeetingSignupMBean.repeatUntil}"  popupCalendar="true"   styleClass="untilCalendar"/>                                  	         	 
		                		        <h:message for="until" errorClass="alertMessageInline" style="margin-left:10px" /> 
		                           </h:panelGroup>                    
		                	 </h:panelGroup>
														
							<h:outputText value="#{msgs.event_signup_start}" rendered="#{!CopyMeetingSignupMBean.announcementType}" escape="false"/>
							<h:panelGrid columns="2" columnClasses="editText,timeSelectTab" rendered="#{!CopyMeetingSignupMBean.announcementType}" >
									<h:panelGroup>
										<h:inputText id="signupBegins" value="#{CopyMeetingSignupMBean.signupBegins}" size="3" required="true">
											<f:validateLongRange minimum="0" maximum="1000"/>
										</h:inputText>
										<h:selectOneMenu value="#{CopyMeetingSignupMBean.signupBeginsType}" >
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
								
							<h:outputText value="#{msgs.event_signup_deadline}" rendered="#{!CopyMeetingSignupMBean.announcementType}" escape="false"/>
							<h:panelGrid columns="2" columnClasses="editText,timeSelectTab" rendered="#{!CopyMeetingSignupMBean.announcementType}">
									<h:panelGroup>
										<h:inputText id="signupDeadline" value="#{CopyMeetingSignupMBean.deadlineTime}" size="3" required="true">
											<f:validateLongRange minimum="0" maximum="1000"/>
										</h:inputText>
										<h:selectOneMenu value="#{CopyMeetingSignupMBean.deadlineTimeType}" >
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
							
							<%-- display site/groups --%>
				            <h:panelGroup styleClass="titleText" style="margin-top:5px">
				            	<h:outputText value="#{msgs.star_character}"  style="color:#B11;"/>
				            	<h:outputText value ="#{msgs.event_publish_to}" />
				            </h:panelGroup>				            
				            <h:panelGroup rendered="#{CopyMeetingSignupMBean.missingSitGroupWarning}">
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
				            <h:outputText value="&nbsp;" escape="false" rendered="#{CopyMeetingSignupMBean.missingSitGroupWarning}"/>
				            
				                               
				            <h:panelGrid  columns="1" styleClass="meetingGpsSitesTable" style="">                    
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
				          	</h:panelGrid>
							
							
							<%-- handle meeting types --%>
				           	<h:panelGroup styleClass="titleText">
				           			<h:outputText value="#{msgs.star_character}"  style="color:#B11;"/>
				            		<h:outputText value ="#{msgs.event_type_title}" />
				           	</h:panelGroup>	
				            <h:panelGrid columns="2" columnClasses="miCol1,miCol2">                
					                   <h:panelGroup id="radios" styleClass="rs">                  
					                        <h:selectOneRadio id="meetingType" value="#{CopyMeetingSignupMBean.signupMeeting.meetingType}"    layout="pageDirection" styleClass="rs" >
					                          	<f:selectItems value="#{CopyMeetingSignupMBean.meetingTypeRadioBttns}"/>                	                      	         	 
					                 	   </h:selectOneRadio> 
					                   </h:panelGroup>
				                      
					               	   <h:panelGrid columns="1" columnClasses="miCol1">       
					               			<%-- multiple: --%>           
						               				<h:panelGroup rendered="#{CopyMeetingSignupMBean.individualType}">            
								                        	<h:panelGrid columns="2" styleClass="mi" columnClasses="miCol1,miCol2">                                                
											                        <h:outputText value="#{msgs.event_num_slot_avail_for_signup}" />
												                    <h:inputText  id="numberOfSlot" value="#{CopyMeetingSignupMBean.numberOfSlots}" size="2" styleClass="editText" onkeyup="getSignupDuration();return false;" style="margin-left:12px" />
											                        <h:outputText value="#{msgs.event_num_participant_per_timeslot}" styleClass="titleText" escape="false"/>                    
													                <h:inputText id="numberOfAttendees" value="#{CopyMeetingSignupMBean.maxNumOfAttendees}" styleClass="editText" size="2" style="margin-left:12px" onkeyup="validateAttendee();return false;" />
											                    	<h:outputText value="#{msgs.event_duration_each_timeslot_not_bold}" styleClass="titleText" escape="false"/>
																	<h:inputText id='currentTimeslotDuration' value="0" styleClass='longtext' size="2" onkeyup="this.blur();" onmouseup="this.blur();" style="margin-left:12px;color:#b11" />             
								                			</h:panelGrid>          
						                         
						                			</h:panelGroup>                                
					               
						            				<%-- single: --%>
						               
								                	<h:panelGroup rendered="#{CopyMeetingSignupMBean.groupType}">                
									                        <h:panelGrid columns="2" rendered="true" styleClass="si" columnClasses="miCol1,miCol2">                
												                    <h:selectOneRadio id="groupSubradio" value="#{CopyMeetingSignupMBean.unlimited}"  onclick="switchSingle(value)" styleClass="meetingRadioBtn" layout="pageDirection" >
												                        <f:selectItem itemValue="#{false}" itemLabel="#{msgs.tab_max_attendee}"/>                    
												                        <f:selectItem itemValue="#{true}" itemLabel="#{msgs.unlimited_num_attendee}"/>                            
												                    </h:selectOneRadio>
												                                    
										                   			<h:panelGrid columns="1" columnClasses="miCol1">       
												                     	<h:panelGroup  styleClass="meetingMaxAttd" >                      
												                            <h:inputText id="maxAttendee" value="#{CopyMeetingSignupMBean.maxNumOfAttendees}" size="2" styleClass="editText" onkeyup="validateParticipants();return false;"/>	                                 
												                        </h:panelGroup>
										                        		<h:outputText value="&nbsp;" styleClass="titleText" escape="false"/>
										                    		</h:panelGrid>
									                		</h:panelGrid>                    
								                	</h:panelGroup>
							                                       
					                 				<h:outputText id="announ" value="&nbsp;" rendered="#{CopyMeetingSignupMBean.announcementType}" styleClass="titleText" escape="false"/>
					              		</h:panelGrid>
				              
				              </h:panelGrid>    
							
							<h:outputText value="&nbsp;" escape="false"/>
							<h:outputText value="&nbsp;" escape="false"/>
							
							<h:outputText value="#{msgs.event_keep_current_attendees}" styleClass="titleText" escape="false" rendered="#{!CopyMeetingSignupMBean.announcementType}"/>
							<h:panelGroup  rendered="#{!CopyMeetingSignupMBean.announcementType}">	
								<h:panelGrid columns="1" style="margin-left:-3px;">
									<h:panelGroup styleClass="longtext">
										<h:selectBooleanCheckbox id="keepAttendees" value="#{CopyMeetingSignupMBean.keepAttendees}" onclick="isShowAssignToAllChoice()"/>
										<h:outputText value="#{msgs.event_yes_keep_current_attendees}" escape="false"/>
									</h:panelGroup>
									<h:panelGroup id="assignToAllRecurrences" style="display:none" styleClass="longtext">
										<h:selectBooleanCheckbox  id="assignToAllChkBox" value="#{CopyMeetingSignupMBean.assignParicitpantsToAllRecurEvents}"/>
										<h:outputText value="#{msgs.apply_added_participants_to_allRecur_events}" escape="false"/>
									</h:panelGroup>
								</h:panelGrid>
							</h:panelGroup>
							
							<h:outputText value="#{msgs.event_email_notification}" styleClass="titleText" escape="false"/>
							<h:panelGroup styleClass="longtext" rendered="#{CopyMeetingSignupMBean.publishedSite}">
								<h:selectBooleanCheckbox value="#{CopyMeetingSignupMBean.sendEmail}" />
								<h:outputText value="#{msgs.event_yes_email_notification}" escape="false"/>
							</h:panelGroup>
							<h:panelGroup styleClass="longtext" rendered="#{!CopyMeetingSignupMBean.publishedSite}">
								<h:selectBooleanCheckbox value="#{CopyMeetingSignupMBean.sendEmail}" disabled="true"/>
								<h:outputText value="#{msgs.event_email_not_send_out_label}" escape="false" style="color:#b11"/>
							</h:panelGroup>
						
							<h:outputText value="&nbsp;" escape="false"/>
							<h:outputText value="&nbsp;" escape="false"/>
							
						</h:panelGrid>
						
				</h:panelGrid>
					
										
				<sakai:button_bar>
					<h:commandButton id="copy" action="#{CopyMeetingSignupMBean.processSaveCopy}" actionListener="#{CopyMeetingSignupMBean.validateCopyMeeting}" value="#{msgs.publish_new_evnt_button}" onclick="return alertTruncatedAttendees('#{msgs.event_alert_truncate_attendee}')"/> 			
					<h:commandButton id="cancel" action="#{CopyMeetingSignupMBean.doCancelAction}" value="#{msgs.cancel_button}" />  
                </sakai:button_bar>

			 </h:form>
  		</sakai:view_content>	
	</sakai:view_container>
	
	<f:verbatim>
    	<script>
    	 	//initialization of the page
			 currentSiteSelection();
	         otherUserSitesSelection();	         
	         replaceCalendarImageIcon();
	         initGroupTypeRadioButton();
	         
	         var timeslotTag = document.getElementById("meeting:numberOfSlot");
	         var maxAttendeeTag = document.getElementById("meeting:numberOfAttendees");
	         var originalTsVal = timeslotTag? timeslotTag.value : 0;
	    	 var originalMaxAttendees = maxAttendeeTag? maxAttendeeTag.value : 0;
	    	 var keepAttendeesTag = document.getElementById('meeting:keepAttendees');
	    	 var groupMaxAttendeeTag = document.getElementById('meeting:maxAttendee');
	    	 var origGroupMaxAttendees = groupMaxAttendeeTag? groupMaxAttendeeTag.value : 0;
	    	 var untilCalendarTag = document.getElementById('meeting:utilCalendar');
	    	 
	    	 initRepeatCalendar();
	    	 isShowAssignToAllChoice();
	    	 
	         function alertTruncatedAttendees(alertMsg){	         	
	         	if(!keepAttendeesTag || keepAttendeesTag && !keepAttendeesTag.checked)
	         		return true;
	         	
	         	
				var groupRadiosTag = getElementsByName_iefix("input","meeting:groupSubradio");
				if(groupRadiosTag){
					if(groupRadiosTag[0] && !groupRadiosTag[0].checked)//unlimited
						return true;												
				}
	
	         	if (!timeslotTag && !maxAttendeeTag && !groupRadiosTag)
	         		return true;
	         	
	         	if ( timeslotTag && (timeslotTag.value < originalTsVal) || maxAttendeeTag && (maxAttendeeTag.value < originalMaxAttendees) 
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
    </f:verbatim>
</f:view> 
