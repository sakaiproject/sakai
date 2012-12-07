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
		<script type="text/javascript" src="/library/js/jquery/1.4.2/jquery-1.4.2.min.js"></script>	
		<script TYPE="text/javascript" LANGUAGE="JavaScript" src="/sakai-signup-tool/js/signupScript.js"></script>
		<script type="text/javascript">
			jQuery.noConflict();
			jQuery(document).ready(function(){
        		sakai.initSignupBeginAndEndsExact();
        	});
    	</script>
		
		<sakai:view_content>
			<h:outputText value="#{msgs.event_error_alerts} #{messageUIBean.errorMessage}" styleClass="alertMessage" escape="false" rendered="#{messageUIBean.error}"/>      			
			<h:outputText id="iframeId" value="#{CopyMeetingSignupMBean.iframeId}" style="display:none"/>	
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
				
				<h:inputHidden id="iframeId" value="#{EditMeetingSignupMBean.iframeId}" />
				<h:panelGrid columns="1">
					
						<h:panelGrid columns="2" columnClasses="titleColumn,valueColumn" onmouseover="delayedRecalculateDateTime();">
							<h:panelGroup styleClass="titleText">
								<h:outputText value="#{msgs.star_character}" style="color:#B11;"/>
								<h:outputText value="#{msgs.event_name}"  escape="false"/>
							</h:panelGroup>
							<h:panelGroup>
								<h:inputText id="meetingTitle" value="#{CopyMeetingSignupMBean.signupMeeting.title}" required="true" size="40" styleClass="editText">
									<f:validator validatorId="Signup.EmptyStringValidator"/>
									<f:validateLength maximum="255" />
								</h:inputText>
								<h:message for="meetingTitle" errorClass="alertMessageInline"/>
							</h:panelGroup>	
							
							<%-- organiser --%>
							<h:outputText value="#{msgs.event_owner}" styleClass="titleText" escape="false"/>
							<h:panelGroup>
						 		<h:selectOneMenu id="creatorUserId" value="#{CopyMeetingSignupMBean.creatorUserId}">
									<f:selectItems value="#{CopyMeetingSignupMBean.instructors}"/>
								</h:selectOneMenu>
							</h:panelGroup>
							
							
							
							
							<%-- location --%>
							<h:panelGroup styleClass="titleText">
								<h:outputText value="#{msgs.star_character}" style="color:#B11;"/>
								<h:outputText value="#{msgs.event_location}"  escape="false"/>
							</h:panelGroup>
							
							<h:panelGroup>
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
		                    </h:panelGroup> 
		                    
		                    <%--category --%>
		                    <h:panelGroup styleClass="titleText">
								<h:outputText value="#{msgs.event_category}"  escape="false"/>
							</h:panelGroup>
							
		                    <h:panelGroup>
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
							<h:panelGroup styleClass="editText" rendered="#{!CopyMeetingSignupMBean.customTsType}">
		        						<t:inputDate id="startTime" type="both"  ampm="true" value="#{CopyMeetingSignupMBean.signupMeeting.startTime}"
		        							style="color:black;" popupCalendar="true" onkeyup="setEndtimeMonthDateYear();getSignupDuration();sakai.updateSignupBeginsExact();return false;"
		        							onchange="sakai.updateSignupBeginsExact();"/>
										<h:message for="startTime" errorClass="alertMessageInline"/>
							</h:panelGroup>
							<h:panelGroup rendered="#{CopyMeetingSignupMBean.customTsType}">
									<h:outputText value="#{CopyMeetingSignupMBean.signupMeeting.startTime}" styleClass="longtext">
				 						<f:convertDateTime pattern="EEEEEEEE, " />
				 					</h:outputText>
									<h:outputText value="#{CopyMeetingSignupMBean.signupMeeting.startTime}" styleClass="longtext">
				 						<f:convertDateTime dateStyle="long" />
				 					</h:outputText>
				 					<h:outputText value="#{CopyMeetingSignupMBean.signupMeeting.startTime}" styleClass="longtext">
				 						<f:convertDateTime pattern=", h:mm a" />
				 					</h:outputText>		
							</h:panelGroup>
							
							<h:panelGroup styleClass="titleText">
								<h:outputText value="#{msgs.star_character}" style="color:#B11;" />					
								<h:outputText value="#{msgs.event_end_time}" escape="false"/>
							</h:panelGroup>
		        			<h:panelGroup styleClass="editText" rendered="#{!CopyMeetingSignupMBean.customTsType}">
		        						<t:inputDate id="endTime" type="both" ampm="true" value="#{CopyMeetingSignupMBean.signupMeeting.endTime}" style="color:black;" popupCalendar="true" 
		        							onkeyup="getSignupDuration(); sakai.updateSignupEndsExact(); return false;" onchange="sakai.updateSignupEndsExact();"/>
										<h:message for="endTime" errorClass="alertMessageInline"/>
							</h:panelGroup>
							<h:panelGroup rendered="#{CopyMeetingSignupMBean.customTsType}" >
									<h:outputText value="#{CopyMeetingSignupMBean.signupMeeting.endTime}" styleClass="longtext">
										<f:convertDateTime pattern="EEEEEEEE, " />
									</h:outputText>
									<h:outputText value="#{CopyMeetingSignupMBean.signupMeeting.endTime}" styleClass="longtext">
										<f:convertDateTime dateStyle="long" />
									</h:outputText>
									<h:outputText value="#{CopyMeetingSignupMBean.signupMeeting.endTime}" styleClass="longtext">
										<f:convertDateTime pattern=", h:mm a" />
									</h:outputText>
							</h:panelGroup>
							
							<h:outputText id="recurWarnLabel_1" value="" escape="false" rendered="#{!CopyMeetingSignupMBean.repeatTypeUnknown}"/>
							<h:outputText id="recurWarnLabel_2" value="#{msgs.warn_copy_recurred_event}" styleClass="alertMessage" style="width:93%" escape="false" rendered="#{!CopyMeetingSignupMBean.repeatTypeUnknown}"/>
							
							 <h:panelGroup>
		                     	<h:outputText value="#{msgs.star_character}" style="color:#B11;" />  
		                     	<h:outputText styleClass="titleText" value="#{msgs.event_recurrence}"  />  
		                     </h:panelGroup>  
		                     <h:panelGroup>                            
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
							                                <t:inputDate id="until" type="date"  value="#{CopyMeetingSignupMBean.repeatUntil}"  popupCalendar="true"   styleClass="untilCalendar"/>
							                		        <h:message for="until" errorClass="alertMessageInline" style="margin-left:10px" /> 
						                		        </h:panelGroup>
					                		        </h:panelGrid>
					                		      </h:panelGrid> 
					                		  </h:panelGrid>
				                       </h:panelGroup>
			                       
		                	 </h:panelGroup>
														
							<h:outputText value="#{msgs.event_signup_start}" rendered="#{!CopyMeetingSignupMBean.announcementType}" escape="false"/>
							<h:panelGrid columns="2" columnClasses="editText,timeSelectTab" rendered="#{!CopyMeetingSignupMBean.announcementType}" >
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
									<h:panelGroup>
										<h:outputText value="#{msgs.before_event_start}" />
										<h:message for="signupBegins" errorClass="alertMessageInline"/>
										
										<!--  show exact date, based on above -->
										<h:outputText id="signupBeginsExact" value="" escape="false" styleClass="dateExact" />
									</h:panelGroup>
							</h:panelGrid>
								
							<h:outputText value="#{msgs.event_signup_deadline}" rendered="#{!CopyMeetingSignupMBean.announcementType}" escape="false"/>
							<h:panelGrid columns="2" columnClasses="editText,timeSelectTab" rendered="#{!CopyMeetingSignupMBean.announcementType}">
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
									<h:panelGroup>
										<h:outputText value="#{msgs.before_event_end}" />
										<h:message for="signupDeadline" errorClass="alertMessageInline"/>
										<!--  show exact date, based on above -->
										<h:outputText id="signupEndsExact" value="" escape="false" styleClass="dateExact" />
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
				          	
				          	<h:panelGroup rendered="#{CopyMeetingSignupMBean.attendanceOn}">
								<h:outputText value="Attendance" escape="false" styleClass="titleText"/>
							  </h:panelGroup>
              				<h:panelGroup rendered="#{CopyMeetingSignupMBean.attendanceOn}">
								<h:selectBooleanCheckbox id="attendanceSelection" value="#{CopyMeetingSignupMBean.signupMeeting.allowAttendance}" />
								<h:outputLabel value="#{msgs.attend_taken}" for="attendanceSelection" styleClass="titleText"/>
								<h:outputText value="#{msgs.attend_track_selected}" escape="false" styleClass="textPanelFooter"/>
							</h:panelGroup>
							
							
							<%-- handle meeting types --%>
				           	<h:panelGroup styleClass="titleText">
				           			<h:outputText value="#{msgs.star_character}"  style="color:#B11;"/>
				            		<h:outputText value ="#{msgs.event_type_title}" />
				           	</h:panelGroup>
				           	<h:outputText value="#{msgs.label_custom_timeslots}"  escape="false" rendered="#{CopyMeetingSignupMBean.customTsType}"/>	
				            <h:panelGrid columns="2" columnClasses="miCol1,miCol2" rendered="#{!CopyMeetingSignupMBean.customTsType}">                
					                   <h:panelGroup id="radios" styleClass="rs">                  
					                        <h:selectOneRadio id="meetingType" value="#{CopyMeetingSignupMBean.signupMeeting.meetingType}"    layout="pageDirection" styleClass="rs" >
					                          	<f:selectItems value="#{CopyMeetingSignupMBean.meetingTypeRadioBttns}"/>                	                      	         	 
					                 	   </h:selectOneRadio> 
					                   </h:panelGroup>
				                      
					               	   <h:panelGrid columns="1" columnClasses="miCol1">       
					               			<%-- multiple: --%>           
						               				<h:panelGroup rendered="#{CopyMeetingSignupMBean.individualType}">            
								                        	<h:panelGrid columns="2" id="mutipleCh" styleClass="mi" columnClasses="miCol1,miCol2"> 
								                        			<h:outputText id="maxAttendeesPerSlot" style="display:none" value="#{CopyMeetingSignupMBean.maxAttendeesPerSlot}"></h:outputText>
																	<h:outputText id="maxSlots" style="display:none" value="#{CopyMeetingSignupMBean.maxSlots}"></h:outputText>   
					                                                <h:outputText value="#{msgs.event_num_slot_avail_for_signup}" />
												                    <h:inputText  id="numberOfSlot" value="#{CopyMeetingSignupMBean.numberOfSlots}" size="2" styleClass="editText" onkeyup="getSignupDuration();return false;" style="margin-left:12px" />
											                        <h:outputText value="#{msgs.event_num_participant_per_timeslot}" styleClass="titleText" escape="false"/>                    
													                <h:inputText id="numberOfAttendees" value="#{CopyMeetingSignupMBean.maxNumOfAttendees}" styleClass="editText" size="2" style="margin-left:12px" onkeyup="validateAttendee();return false;" />
											                    	<h:outputText value="#{msgs.event_duration_each_timeslot_not_bold}" styleClass="titleText" escape="false"/>
																	<h:inputText id='currentTimeslotDuration' value="0" styleClass='longtext_red' size="2" onkeyup="this.blur();" onmouseup="this.blur();" style="margin-left:12px;" />             
								                			</h:panelGrid>          
						                         
						                			</h:panelGroup>                                
					               
						            				<%-- single: --%>
						               
								                	<h:panelGroup rendered="#{CopyMeetingSignupMBean.groupType}">                
									                        <h:panelGrid columns="2" id="singleCh" rendered="true" styleClass="si" columnClasses="miCol1,miCol2">                
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
				              
				              <!-- User can switch from individual type to custom_ts type -->
				            <h:outputText id="userDefTsChoice_1" value=""  rendered="#{!CopyMeetingSignupMBean.customTsType && !CopyMeetingSignupMBean.announcementType}"/>
				            <h:panelGroup id="userDefTsChoice_2"  styleClass="longtext" rendered="#{!CopyMeetingSignupMBean.customTsType && !CopyMeetingSignupMBean.announcementType}">
				              		<h:panelGrid style="margin:-10px 0px 0px 25px;">
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
				            
				            <!-- edit custom defined TS -->
				            <h:outputText value="#{msgs.event_show_schedule}" styleClass="titleText" rendered="#{CopyMeetingSignupMBean.customTsType}"/>
				            <h:panelGroup rendered="#{CopyMeetingSignupMBean.customTsType}">
								<h:commandLink action="#{CopyMeetingSignupMBean.editUserDefTimeSlots}" >
									<h:graphicImage value="/images/cal.gif" alt="close" style="border:none;cursor:pointer; padding-right:5px;" styleClass="openCloseImageIcon" />
									<h:outputText value="#{msgs.label_view_edit_ts}" escape="false" styleClass="activeTag"/>
								</h:commandLink>
							</h:panelGroup>    
							
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
							
							<h:outputText value="#{msgs.event_create_email_notification}" styleClass="titleText" escape="false"/>
							<h:panelGrid columns="1" style="width:100%;margin-left:-3px;" rendered="#{CopyMeetingSignupMBean.publishedSite}">
								<h:panelGroup styleClass="editText" >
									<h:selectBooleanCheckbox id="emailChoice" value="#{CopyMeetingSignupMBean.sendEmail}" onclick="isShowEmailChoice()" disabled="#{CopyMeetingSignupMBean.mandatorySendEmail}"/>
									<h:outputText value="#{msgs.event_yes_email_notification}" escape="false"/>
								</h:panelGroup>
								
								<h:panelGroup id="emailAttendeeOnly" style="display:none" >
									<h:selectOneRadio  value="#{CopyMeetingSignupMBean.sendEmailToSelectedPeopleOnly}" layout="lineDirection" styleClass="rs" style="margin-left:20px;">
					                          <f:selectItem id="all_attendees" itemValue="all" itemLabel="#{msgs.label_email_all_people}"/>                                              
					                          <f:selectItem id="only_organizers" itemValue="organizers_only" itemLabel="#{msgs.label_email_signed_up_ones_Organizers_only}"/>	
					         		</h:selectOneRadio> 
								</h:panelGroup>
							</h:panelGrid>
							<h:panelGroup styleClass="longtext" rendered="#{!CopyMeetingSignupMBean.publishedSite}">
								<h:selectBooleanCheckbox value="#{CopyMeetingSignupMBean.sendEmail}" disabled="true"/>
								<h:outputText value="#{msgs.event_email_not_send_out_label}" escape="false" style="color:#b11"/>
							</h:panelGroup>
							
							<h:outputText  value="#{msgs.event_publish_to_calendar}" styleClass="titleText" escape="false" />
								<h:panelGroup styleClass="longtext">
									<h:selectBooleanCheckbox value="#{CopyMeetingSignupMBean.publishToCalendar}"/>
									<h:outputText value="#{msgs.event_yes_publish_to_calendar}" escape="false"/>
							</h:panelGroup>
						
							<h:outputText value="&nbsp;" escape="false"/>
							<h:outputText value="&nbsp;" escape="false"/>
							
						</h:panelGrid>
						
				</h:panelGrid>
					
										
				<sakai:button_bar>
					<h:commandButton id="copy" action="#{CopyMeetingSignupMBean.processSaveCopy}" actionListener="#{CopyMeetingSignupMBean.validateCopyMeeting}" value="#{msgs.publish_new_evnt_button}" 
					onclick="return alertTruncatedAttendees('#{msgs.event_alert_truncate_attendee}','#{UserDefineTimeslotBean.truncatedAttendees}')"/> 			
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
	         userDefinedTsChoice();
	         isShowEmailChoice();
	         setIframeHeight_DueTo_Ckeditor();
			 initDropDownAndInput('meeting:customLocationLabel','meeting:customLocationLabel_undo','meeting:customLocation','meeting:selectedLocation');
		     initDropDownAndInput('meeting:customCategoryLabel','meeting:customCategoryLabel_undo','meeting:customCategory','meeting:selectedCategory');

	         	         
	         var timeslotTag = document.getElementById("meeting:numberOfSlot");
	         var maxAttendeeTag = document.getElementById("meeting:numberOfAttendees");
	         var originalTsVal = timeslotTag? timeslotTag.value : 0;
	    	 var originalMaxAttendees = maxAttendeeTag? maxAttendeeTag.value : 0;
	    	 var keepAttendeesTag = document.getElementById('meeting:keepAttendees');
	    	 var groupMaxAttendeeTag = document.getElementById('meeting:maxAttendee');
	    	 var origGroupMaxAttendees = groupMaxAttendeeTag? groupMaxAttendeeTag.value : 0;
	    	 var untilCalendarTag = document.getElementById('meeting:utilCalendar');
	    	 
	    	 //init signupBegin
			 var signupBeginTag = document.getElementById("meeting:signupBeginsType");
			 if(signupBeginTag)
			 	isSignUpBeginStartNow(signupBeginTag.value);
	    	 
	    	 initRepeatCalendar();
	    	 isShowAssignToAllChoice();
	    	 
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
    </f:verbatim>
</f:view> 
