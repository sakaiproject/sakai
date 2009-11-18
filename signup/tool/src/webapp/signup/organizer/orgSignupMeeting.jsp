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
		<style type="text/css" media="print">
			@import url("/sakai-signup-tool/css/print.css");
		</style>
		<script TYPE="text/javascript" LANGUAGE="JavaScript" src="/sakai-signup-tool/js/signupScript.js"></script>
		<script TYPE="text/javascript" LANGUAGE="JavaScript" src="/sakai-signup-tool/js/jquery.js"></script>
		
		<h:form id="modifyMeeting">
			<sakai:tool_bar>
				<sakai:tool_bar_item value="#{msgs.modify_event}" action="#{OrganizerSignupMBean.modifyMeeting}" rendered="#{!OrganizerSignupMBean.meetingWrapper.meeting.meetingExpired}" />
				<sakai:tool_bar_item value="#{msgs.copy_event}" action="#{OrganizerSignupMBean.copyMeeting}" />
				<sakai:tool_bar_item value="#{msgs.event_pageTop_link_for_download}" action="#{DownloadEventBean.downloadOneEvent}" />
				<h:outputLink id="print" value="javascript:window.print();" style="vertical-align:bottom;">
						<h:graphicImage url="/images/printer.png"
							alt="#{msgs.print_friendly}" title="#{msgs.print_friendly}" styleClass="openCloseImageIcon"/>
						<h:outputText value="#{msgs.print_event}" escape="false"/>
				</h:outputLink>
			</sakai:tool_bar>
		</h:form>
		<sakai:view_content>
			
			<h:outputText value="#{msgs.event_error_alerts} #{errorMessageUIBean.errorMessage}" styleClass="alertMessage" escape="false" rendered="#{errorMessageUIBean.error}"/>      			
				
			<h:form id="meeting">
			
				<h:inputHidden id="userActionType" value="#{OrganizerSignupMBean.userActionType}"/>
				<h:inputHidden id="selectedFirstUser"  value="#{OrganizerSignupMBean.selectedFirstUser}"/>
			
			 	<sakai:view_title value="#{msgs.organizer_page_title}"/>
				<sakai:messages/>
				<h:panelGrid columns="1" styleClass="organizerToplevelTable">
					<%-- show title only when collapsed --%>
					<h:panelGrid id="showMeetingTitleOnly" columns="2" columnClasses="titleColumn,valueColumn" styleClass="orgShowTitleOnly">
								<h:outputText value="#{msgs.event_name}" styleClass="titleText" escape="false"/>
									<h:panelGroup>
										<h:panelGroup rendered="#{OrganizerSignupMBean.meetingWrapper.meeting.recurrenceId !=null}">
											<h:graphicImage title="#{msgs.event_tool_tips_recurrence}" value="/images/recurrence.gif"  alt="recurrence" style="border:none" />
											<h:outputText value="&nbsp;" escape="false"/>
										</h:panelGroup>
									<h:outputText value="#{OrganizerSignupMBean.meetingWrapper.meeting.title}" styleClass="longtext"/>
								</h:panelGroup>
					</h:panelGrid>
					<%-- show all meeting details when expanded--%>
					<h:panelGroup id="meetingInfoDetails">
							<h:panelGrid columns="2" columnClasses="titleColumn,valueColumn" >
								<h:outputText value="#{msgs.event_name}" styleClass="titleText" escape="false"/>
									<h:panelGroup>
										<h:panelGroup rendered="#{OrganizerSignupMBean.meetingWrapper.meeting.recurrenceId !=null}">
											<h:graphicImage title="#{msgs.event_tool_tips_recurrence}" value="/images/recurrence.gif"  alt="recurrence" style="border:none" />
											<h:outputText value="&nbsp;" escape="false"/>
										</h:panelGroup>
									<h:outputText value="#{OrganizerSignupMBean.meetingWrapper.meeting.title}" styleClass="longtext"/>
								</h:panelGroup>
								
								<h:outputText value="#{msgs.event_owner}" styleClass="titleText" escape="false"/>
								<h:outputText value="#{OrganizerSignupMBean.meetingWrapper.creator}" styleClass="longtext"/>
								
								<h:outputText value="#{msgs.event_location}" styleClass="titleText" escape="false"/>
								<h:outputText value="#{OrganizerSignupMBean.meetingWrapper.meeting.location}" styleClass="longtext"/>
								
								<h:outputText value="#{msgs.event_date}" styleClass="titleText" escape="false"/>
								<h:panelGroup>
									<h:outputText value="#{OrganizerSignupMBean.meetingWrapper.meeting.startTime}" styleClass="longtext">
									 	<f:convertDateTime pattern="EEEEEEEE, "/>
									</h:outputText>
									<h:outputText value="#{OrganizerSignupMBean.meetingWrapper.meeting.startTime}" styleClass="longtext">
								 	<f:convertDateTime dateStyle="long"/>
								</h:outputText>
								</h:panelGroup>
								
								<h:outputText value="#{msgs.event_time_period}" styleClass="titleText" escape="false"/>
								<h:panelGroup>
									<h:outputText value="#{OrganizerSignupMBean.meetingWrapper.meeting.startTime}">
										<f:convertDateTime pattern="h:mm a" />
									</h:outputText>
									<h:outputText value="#{OrganizerSignupMBean.meetingWrapper.meeting.startTime}" rendered="#{OrganizerSignupMBean.meetingWrapper.meeting.meetingCrossDays}">
										<f:convertDateTime pattern=", EEEEEEEE" />
									</h:outputText>
									<h:outputText value="#{msgs.timeperiod_divider}" escape="false"/>
									<h:outputText value="#{OrganizerSignupMBean.meetingWrapper.meeting.endTime}">
										<f:convertDateTime pattern="h:mm a" />
									</h:outputText>
									<h:panelGroup rendered="#{OrganizerSignupMBean.meetingWrapper.meeting.meetingCrossDays}">
										<h:outputText value="#{OrganizerSignupMBean.meetingWrapper.meeting.endTime}" >
												<f:convertDateTime pattern=", EEEEEEEE, "/>
										</h:outputText>
										<h:outputText value="#{OrganizerSignupMBean.meetingWrapper.meeting.endTime}" >
												<f:convertDateTime dateStyle="long"/>
										</h:outputText>
									</h:panelGroup>
								</h:panelGroup>		
								
								<h:outputText value="#{msgs.event_signup_start}" styleClass="titleText" rendered="#{!OrganizerSignupMBean.announcementType}" escape="false"/>			
								<h:panelGroup rendered="#{!OrganizerSignupMBean.announcementType}">
									<h:outputText value="#{OrganizerSignupMBean.meetingWrapper.meeting.signupBegins}" styleClass="longtext">
									 	<f:convertDateTime pattern="EEEEEEEE, " />
									</h:outputText>
									<h:outputText value="#{OrganizerSignupMBean.meetingWrapper.meeting.signupBegins}" styleClass="longtext">
									 	<f:convertDateTime dateStyle="long" />
									</h:outputText>
									<h:outputText value="#{OrganizerSignupMBean.meetingWrapper.meeting.signupBegins}" styleClass="longtext">
									 	<f:convertDateTime pattern=", h:mm a" />
									</h:outputText>
								</h:panelGroup>
									
								<h:outputText value="#{msgs.event_signup_deadline}" styleClass="titleText" rendered="#{!OrganizerSignupMBean.announcementType}" escape="false"/>
								<h:panelGroup rendered="#{!OrganizerSignupMBean.announcementType}">
									<h:outputText value="#{OrganizerSignupMBean.meetingWrapper.meeting.signupDeadline}" styleClass="longtext">
									 	<f:convertDateTime pattern="EEEEEEEE, " />
									</h:outputText>
									<h:outputText value="#{OrganizerSignupMBean.meetingWrapper.meeting.signupDeadline}" styleClass="longtext">
									 	<f:convertDateTime dateStyle="long" />
									</h:outputText>
									<h:outputText value="#{OrganizerSignupMBean.meetingWrapper.meeting.signupDeadline}" styleClass="longtext">
									 	<f:convertDateTime pattern=", h:mm a" />
									</h:outputText>
								</h:panelGroup>
								
								<h:outputText value="#{msgs.event_status}" styleClass="titleText" rendered="#{OrganizerSignupMBean.meetingWrapper.meeting.meetingExpired}" escape="false"/>
								<h:outputText value="#{msgs.event_isOver}" styleClass="longtext" escape="false"  rendered="#{OrganizerSignupMBean.meetingWrapper.meeting.meetingExpired}"/>
								
								<%-- display published site/groups --%>
								<h:outputText value="#{msgs.event_publish_to}" escape="false"  styleClass="titleText"/>
								<h:panelGrid columns="1" styleClass="published_siteGroupTable">
										<h:panelGroup >	
						   	    				<h:outputLabel  id="imageOpen_publishedSiteGroup" style="display:none" styleClass="activeTag" onclick="showDetails('meeting:imageOpen_publishedSiteGroup','meeting:imageClose_publishedSiteGroup','meeting:publishedSiteGroups');">
							   	    				<h:graphicImage value="/images/open.gif"  alt="open" title="Click to hide details." style="border:none" styleClass="openCloseImageIcon"/>
							   	    				<h:outputText value="#{msgs.event_hide_site_group_detail}" escape="false" />
						   	    				</h:outputLabel>
						   	    				<h:outputLabel id="imageClose_publishedSiteGroup" styleClass="activeTag" onclick="showDetails('meeting:imageOpen_publishedSiteGroup','meeting:imageClose_publishedSiteGroup','meeting:publishedSiteGroups');">
						   	    					<h:graphicImage value="/images/closed.gif" alt="close" title="Click to show details." style="border:none" styleClass="openCloseImageIcon"/>
						   	    					<h:outputText value="#{msgs.event_show_site_group_detail}" escape="false" />
						   	    				</h:outputLabel>
							            </h:panelGroup>
							            <h:panelGroup id="publishedSiteGroups" style="display:none">
												<h:dataTable id="userSites" value="#{OrganizerSignupMBean.publishedSignupSites}" var="site"  styleClass="published_sitegroup">
													<h:column>
														<h:outputText value="#{site.title} #{msgs.event_site_level}" rendered="#{site.siteScope}" styleClass="published_sitetitle" escape="false"/>
														<h:panelGroup rendered="#{!site.siteScope}">
															<h:outputText value="#{site.title} #{msgs.event_group_level}" styleClass="published_sitetitle" escape="false"/>
															<h:dataTable id="userGroups" value="#{site.signupGroups}" var="group" styleClass="published_sitegroup">
																<h:column>
																		<h:outputText value=" - #{group.title}" escape="false" styleClass="published_grouptitle"/>
																</h:column>
															</h:dataTable>
														</h:panelGroup>							
													</h:column>
												</h:dataTable>
										</h:panelGroup>
								</h:panelGrid>
								<%-- end of display published site/groups --%>
								
								<h:outputText value="#{msgs.event_description}" styleClass="titleText" escape="false"/>
								<h:outputText value="#{OrganizerSignupMBean.meetingWrapper.meeting.description}" escape="false" styleClass="longtext"/>																											
								
								<h:outputText  value="#{msgs.attachments}" styleClass="titleText" escape="false" rendered="#{!OrganizerSignupMBean.meetingWrapper.emptyEventMainAttachment}"/>
			         			<h:panelGrid columns="1" rendered="#{!OrganizerSignupMBean.meetingWrapper.emptyEventMainAttachment}">
			         				<t:dataTable value="#{OrganizerSignupMBean.meetingWrapper.eventMainAttachments}" var="attach" >
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
								
								<h:outputText value="&nbsp;" escape="false" rendered="#{!OrganizerSignupMBean.announcementType}"/>
								<h:outputText value="&nbsp;" escape="false" rendered="#{!OrganizerSignupMBean.announcementType}"/>
							
							</h:panelGrid>
					</h:panelGroup>
					
					<%-- control email and the expand-collapse --%>			
					<h:panelGrid  columns="3" rendered="#{!OrganizerSignupMBean.announcementType}" columnClasses="titleColumn,valueColumn,alignRightColumn" styleClass="emailTable">										
						<h:outputText value="#{msgs.event_email_notification}" styleClass="titleText" escape="false"/>
						<h:panelGroup styleClass="longtext" rendered="#{OrganizerSignupMBean.publishedSite}">
							<h:selectBooleanCheckbox value="#{OrganizerSignupMBean.sendEmail}" style="vertical-align:middle;"/>
							<h:outputText value="#{msgs.event_email_yes_label}" escape="false" />
						</h:panelGroup>
						<h:panelGroup styleClass="longtext" rendered="#{!OrganizerSignupMBean.publishedSite}">
							<h:selectBooleanCheckbox value="#{OrganizerSignupMBean.sendEmail}" style="vertical-align:middle;" disabled="true"/>
							<h:outputText value="#{msgs.event_email_not_send_out_label}" escape="false" style="color:#b11"/>
						</h:panelGroup>							
						<h:panelGroup>	
		   	    				<h:outputLabel  id="imageOpen_meetingInfoDetail"  styleClass="activeTag" onclick="showDetails('meeting:imageOpen_meetingInfoDetail','meeting:imageClose_meetingInfoDetail','meeting:meetingInfoDetails');setMeetingCollapseInfo(true);">
			   	    				<h:graphicImage value="/images/openTop.gif"  alt="open" title="#{msgs.event_tool_tips_hide_details}" style="border:none; vertical-align: bottom;" styleClass="openCloseImageIcon"/>
			   	    				<h:outputText value="#{msgs.event_hide_meetingIfo_detail}" escape="false" />
		   	    				</h:outputLabel>
		   	    				<h:outputLabel id="imageClose_meetingInfoDetail" style="display:none" styleClass="activeTag" onclick="showDetails('meeting:imageOpen_meetingInfoDetail','meeting:imageClose_meetingInfoDetail','meeting:meetingInfoDetails');setMeetingCollapseInfo(false);">
		   	    					<h:graphicImage value="/images/closed.gif" alt="close" title="#{msgs.event_tool_tips_show_details}" style="border:none" styleClass="openCloseImageIcon"/>
		   	    					<h:outputText value="#{msgs.event_show_meetingIfo_detail}" escape="false" />
		   	    				</h:outputLabel>
		   	    				<h:inputHidden id="meetingInfoCollapseExpand" value="#{OrganizerSignupMBean.collapsedMeetingInfo}"/>
			            </h:panelGroup>
					
					</h:panelGrid>
					
					<h:panelGrid rendered="#{OrganizerSignupMBean.announcementType}" columns="1" styleClass="annoncement">
						<h:outputText value="#{msgs.event_announcement_notice}" escape="false"/>
					</h:panelGrid>
					
					<%-- Organizer's editing main table --%>
					 <h:dataTable id="timeslots" value="#{OrganizerSignupMBean.timeslotWrappers}" binding="#{OrganizerSignupMBean.timeslotWrapperTable}" var="timeSlotWrapper"
					 rendered="#{!OrganizerSignupMBean.announcementType}"
					 columnClasses="orgTimeslotCol,orgMaxAttsCol,orgSlotStatusCol,orgWaiterStatusCol"	
					 rowClasses="oddRow,evenRow"
					 styleClass="signupTable" style="width:98%">
							<h:column>		   
								<f:facet name="header">
									<h:outputText value="#{msgs.tab_time_slot}"/>
								</f:facet>
								<h:panelGrid columns="1">
										<h:panelGroup id="timeslot">
											<h:graphicImage value="/images/spacer.gif" width="15" height="13" alt="spacer" style="border:none"
												 rendered="#{!timeSlotWrapper.timeSlot.locked && !timeSlotWrapper.timeSlot.canceled && !OrganizerSignupMBean.meetingWrapper.meeting.meetingExpired }"/>
											<h:graphicImage value="/images/lock.gif"  alt="this time slot is locked" style="border:none" 
											 rendered="#{timeSlotWrapper.timeSlot.locked && !timeSlotWrapper.timeSlot.canceled && !OrganizerSignupMBean.meetingWrapper.meeting.meetingExpired}"/>
											<h:graphicImage value="/images/cancelled.gif"  alt="this time slot is canceled" style="border:none" 
												rendered="#{timeSlotWrapper.timeSlot.canceled && !OrganizerSignupMBean.meetingWrapper.meeting.meetingExpired}"/>
											<h:outputLink title="#{msgs.event_tool_tips_lockOrcancel}"  onclick="showEditTimeslot('#{timeSlotWrapper.positionInTSlist}'); return false;"
												rendered="#{!OrganizerSignupMBean.meetingWrapper.meeting.meetingExpired}">
									   			<h:outputText value="#{timeSlotWrapper.timeSlot.startTime}">
													<f:convertDateTime pattern="h:mm a" />
												</h:outputText>
												<h:outputText value="#{timeSlotWrapper.timeSlot.startTime}" rendered="#{OrganizerSignupMBean.meetingWrapper.meeting.meetingCrossDays}">
													<f:convertDateTime pattern=", EEE" />
												</h:outputText>
												<h:outputText value="#{msgs.timeperiod_divider}" escape="false"/>
												<h:outputText value="#{timeSlotWrapper.timeSlot.endTime}">
													<f:convertDateTime pattern="h:mm a" />
												</h:outputText>
												<h:outputText value="#{timeSlotWrapper.timeSlot.endTime}" rendered="#{OrganizerSignupMBean.meetingWrapper.meeting.meetingCrossDays}">
													<f:convertDateTime pattern=", EEE" />
												</h:outputText>
											</h:outputLink>
											<h:panelGroup rendered="#{OrganizerSignupMBean.meetingWrapper.meeting.meetingExpired}">
												<h:outputText value="#{timeSlotWrapper.timeSlot.startTime}">
													<f:convertDateTime pattern="h:mm a" />
												</h:outputText>
												<h:outputText value="#{timeSlotWrapper.timeSlot.startTime}" rendered="#{OrganizerSignupMBean.meetingWrapper.meeting.meetingCrossDays}">
													<f:convertDateTime pattern=", EEE" />
												</h:outputText>
												<h:outputText value="#{msgs.timeperiod_divider}" escape="false"/>
												<h:outputText value="#{timeSlotWrapper.timeSlot.endTime}">
													<f:convertDateTime pattern="h:mm a" />
												</h:outputText>
												<h:outputText value="#{timeSlotWrapper.timeSlot.endTime}" rendered="#{OrganizerSignupMBean.meetingWrapper.meeting.meetingCrossDays}">
													<f:convertDateTime pattern=", EEE" />
												</h:outputText>
											</h:panelGroup>
										</h:panelGroup>
																			
										<h:panelGroup  id="editTimeSlot" style="display: none;">
											<h:panelGrid columns="1"  >
												<h:panelGroup >
													<h:commandLink id="lockTimeslot" action="#{OrganizerSignupMBean.processLockTsAction}" rendered="#{!timeSlotWrapper.timeSlot.locked}" title="#{msgs.event_tool_tips_lock_label}">
														<h:graphicImage value="/images/lock.gif"  alt="lock this time slot" style="border:none" styleClass="openCloseImageIcon"/>
														<h:outputText value="#{msgs.event_lock_timeslot_label}" style="white-space: nowrap;" escape="false"/>
													</h:commandLink>
													<h:commandLink id="lockedTimeslot" action="#{OrganizerSignupMBean.processLockTsAction}" rendered="#{timeSlotWrapper.timeSlot.locked}" title="#{msgs.event_tool_tips_unlock_label}">
														<h:graphicImage value="/images/lock.gif"  alt="unlock this time slot" style="border:none" styleClass="openCloseImageIcon"/>
														<h:outputText value="#{msgs.event_unlock_timeslot_label}" escape="false"/>
													</h:commandLink>
												</h:panelGroup>
												
												<h:panelGroup >
													<h:commandLink id="cancelTimeslot" action="#{OrganizerSignupMBean.initiateCancelTimeslot}" rendered="#{!timeSlotWrapper.timeSlot.canceled}"
													  onmousedown="confirmTsCancel(this,'#{msgs.confirm_cancel}');" title="#{msgs.event_tool_tips_cancel_label}">
														<h:graphicImage value="/images/cancelled.gif"  alt="cancel this time slot" style="border:none" styleClass="openCloseImageIcon"/>
														<h:outputText value="#{msgs.event_cancel_timeslot_label}" style="white-space: nowrap;" escape="false"/>
													</h:commandLink>
													<h:commandLink id="restoreTimeslot" action="#{OrganizerSignupMBean.restoreTimeslot}" rendered="#{timeSlotWrapper.timeSlot.canceled}" title="#{msgs.event_tool_tips_restore_timeslot_label}">
														<h:graphicImage value="/images/cancelled.gif"  alt="restore" style="border:none" styleClass="openCloseImageIcon"/>
														<h:outputText value="#{msgs.event_restore_timeslot_label}" escape="false"/>
													</h:commandLink>
												</h:panelGroup>
												
											</h:panelGrid>
										</h:panelGroup>	
									</h:panelGrid>
					   		</h:column>
					   		
					   		<h:column>		   
								<f:facet name="header">
									<h:outputText value="#{msgs.tab_max_attendee}"/>
								</f:facet>
						   		<h:outputText value="#{timeSlotWrapper.timeSlot.maxNoOfAttendees}" rendered="#{!timeSlotWrapper.timeSlot.unlimitedAttendee}"/>
						   		<h:outputText value="#{msgs.event_unlimited}" rendered="#{timeSlotWrapper.timeSlot.unlimitedAttendee}"/>
					   		</h:column>
					   		
					   		<h:column>		   
								<f:facet name="header">
									<h:outputText value="#{msgs.tab_attendees}"/>
								</f:facet>
								
								<h:panelGroup rendered="#{timeSlotWrapper.timeSlot.canceled}">
									<h:outputText value="#{msgs.event_canceled}" escape="false" styleClass="organizer_canceled"/>
								</h:panelGroup>
								<h:panelGroup rendered="#{!timeSlotWrapper.timeSlot.canceled}">
									<h:panelGrid columns="1" styleClass="organizerAction">
						   				<h:dataTable id="availableSpots" value="#{timeSlotWrapper.attendeeWrappers}" var="attendeeWrapper">
						   					<h:column>
						   						<h:panelGroup rendered="#{attendeeWrapper.signupAttendee.attendeeUserId !=null}" id="editLink">
						   							<h:graphicImage id="editAttendee" value="/images/edit.png" title="#{msgs.event_tool_tips_edit}" styleClass="openCloseImageIcon"
						   								onclick="showHideEditPanel('#{timeSlotWrapper.positionInTSlist}','#{attendeeWrapper.positionIndex}','#{attendeeWrapper.signupAttendee.attendeeUserId}');" 
						   								alt="edit" style="cursor:pointer; border:none" rendered="#{!OrganizerSignupMBean.meetingWrapper.meeting.meetingExpired}"/>
						   							<h:outputText value="&nbsp;" escape="false"/>
						   							<h:commandLink id="deleteAttendee" action="#{OrganizerSignupMBean.removeAttendee}"  onmousedown="assignDeleteClick(this,'#{msgs.delete_attandee_confirmation}');"  title="#{msgs.event_tool_tips_delete}" rendered="#{!OrganizerSignupMBean.meetingWrapper.meeting.meetingExpired}" >
						   								<h:graphicImage value="/images/delete.png"  alt="delete" style="border:none" styleClass="openCloseImageIcon"></h:graphicImage>
						   								<f:param id="deletAttendeeUserId" name="#{OrganizerSignupMBean.attendeeUserId}" value="#{attendeeWrapper.signupAttendee.attendeeUserId}"></f:param>
						   							</h:commandLink>
						   							<h:outputText value="&nbsp;" escape="false" />
						   							<h:commandLink action="#{OrganizerSignupMBean.viewAttendeeComment}">
						   								<f:param id="timeslotId" name="timeslotId" value="#{timeSlotWrapper.timeSlot.id}"/>
						   								<f:param id="attendeeUserId" name="attendeeUserId" value="#{attendeeWrapper.signupAttendee.attendeeUserId}"/>				   										   								
						   								<h:outputText value="#{attendeeWrapper.displayName}" title="#{attendeeWrapper.commentForTooltips}" style="cursor:pointer;" rendered="#{attendeeWrapper.signupAttendee.attendeeUserId !=null}"/>
						   								<h:graphicImage title="Click to view comment" value="/images/comment.gif" width="11" height="11" alt="view comment" style="border:none" styleClass="openCloseImageIcon" rendered="#{attendeeWrapper.comment}" />
						   							</h:commandLink>
						   						</h:panelGroup>
						   						
								   				<h:panelGroup id="editPanel" style="display: none;">
									   					<h:panelGrid columns="1" >
									   							<h:panelGroup rendered="#{!OrganizerSignupMBean.groupType}" >
										   							<h:selectOneRadio id="selcetActionType" value="#{OrganizerSignupMBean.selectedAction}" onclick="showHideActionTypePanel('#{timeSlotWrapper.positionInTSlist}','#{attendeeWrapper.positionIndex}',this.value)">
										   								<f:selectItem id="moveTo" itemValue="#{OrganizerSignupMBean.moveAction}" itemLabel="#{msgs.event_move}" itemDisabled="#{OrganizerSignupMBean.groupType}"/>							
																		<f:selectItem id="replacedBy" itemValue="#{OrganizerSignupMBean.replaceAction}" itemLabel="#{msgs.event_replace}" />							
																		<f:selectItem id="swapWith"  itemValue="#{OrganizerSignupMBean.swapAction}" itemLabel="#{msgs.event_swap}" itemDisabled="#{OrganizerSignupMBean.groupType}"/>	
										   							</h:selectOneRadio>	
									   							</h:panelGroup>
									   									   				
										   						<h:panelGrid id="replaceAction" columns="2" style="display: none;">
										   							<h:outputText value="#{msgs.event_replaceby}" escape="false" rendered="#{!OrganizerSignupMBean.eidInputMode}"/>
										   							<h:outputText value="#{msgs.event_replaceby_Eid}" escape="false" rendered="#{OrganizerSignupMBean.eidInputMode}"/>
										   							<h:panelGroup rendered="#{!OrganizerSignupMBean.eidInputMode}">
											   							<h:selectOneMenu  id="replaceAttendeeList" binding="#{OrganizerSignupMBean.replacedAttendeeEid}" >
										   									<f:selectItems value="#{OrganizerSignupMBean.allAttendees}" />
										   								</h:selectOneMenu>
										   							</h:panelGroup>
										   							<h:inputText id="replaceEidInput" value="#{OrganizerSignupMBean.userInputEid}" rendered="#{OrganizerSignupMBean.eidInputMode}" size="10"/>
									   								
										   						</h:panelGrid>
										   					
										   						<h:panelGrid id="swapAction" columns="2" style="display: none;">
										   							<h:outputText value="#{msgs.event_swapWith}" escape="false"/>
										   							<h:panelGroup>
											   							<h:selectOneMenu  id="swapAttendeeList"  binding="#{OrganizerSignupMBean.attendeeTimeSlotWithId}">
										   									<f:selectItems value="#{timeSlotWrapper.swapDropDownList}"/>
										   								</h:selectOneMenu>
									   								</h:panelGroup>
										   						</h:panelGrid>
										   					
										   						<h:panelGrid id="moveAction" columns="2" style="display: none;">
										   							<h:outputText value="#{msgs.event_moveToTimeslot}" escape="false"/>
										   							<h:panelGroup>
											   							<h:selectOneMenu  id="selectTimeslot" binding="#{OrganizerSignupMBean.selectedTimeslotId}"> 
										   									<f:selectItems value="#{timeSlotWrapper.moveAvailableTimeSlots}"/>
										   								</h:selectOneMenu>
									   								</h:panelGroup>
										   						</h:panelGrid>
										   					
										   						<h:panelGrid columns="2"  style="width:50%;">
										   							<h:commandButton value="#{msgs.ok_button}" action="#{OrganizerSignupMBean.editTimeslotAttendee}"/>
										   							<h:commandButton value="#{msgs.cancel_button}" action="" onclick="clearPanels();return false;" immediate="true"/>
										   						</h:panelGrid>
									   					
									   					</h:panelGrid>
								   				</h:panelGroup>
								   						
						   					</h:column>
						   				</h:dataTable>
						   				
					   					<h:panelGroup id="addAttendee" rendered="#{!OrganizerSignupMBean.meetingWrapper.meeting.meetingExpired}">
					   						<%-- TODO add spacer only if the attendees exist in atleast one timeslot --%>
					   						<h:graphicImage value="/images/spacer.gif" width="18" height="16" alt="spacer" style="border:none"/>
						   					<h:outputLabel onclick="showHideAddPanel('#{timeSlotWrapper.positionInTSlist}');" styleClass="addAttendee">
						   						<h:graphicImage value="/images/add.png"  alt="add an attendee" title="#{msgs.event_tool_tips_add}" style="border:none" styleClass="openCloseImageIcon"/>
						   						<h:outputText value="#{msgs.event_add_attendee}" escape="false" />
						   					</h:outputLabel>	
						   				</h:panelGroup>
							   				
						   				<h:panelGroup id="addPanel" style="display: none;" >
						   					<h:panelGrid id="addNewAttendeeTable" columns="2">
							   					<h:graphicImage value="/images/spacer.gif" width="16" height="16" alt="spacer" style="border:none"/>
							   					<h:panelGrid id="selectAttendees" columns="2">
						   							<h:outputText value="#{msgs.attendee_select}" escape="false" rendered="#{!OrganizerSignupMBean.eidInputMode}"/>
						   							<h:outputText value="#{msgs.attendee_enterEid}" escape="false" rendered="#{OrganizerSignupMBean.eidInputMode}"/>
						   							
						   							<h:panelGroup rendered="#{!OrganizerSignupMBean.eidInputMode}">
							   							<h:selectOneMenu  id="newAttendeeList" binding="#{OrganizerSignupMBean.addNewAttendeeUserEid}" >
						   									<f:selectItems value="#{OrganizerSignupMBean.allAttendees}" />
						   								</h:selectOneMenu>
						   							</h:panelGroup>
						   							<h:inputText  id="addAttendeeEidInput" size="15" value="#{OrganizerSignupMBean.userInputEid}" rendered="#{OrganizerSignupMBean.eidInputMode}" />
					   								
						   						    <h:commandButton value="#{msgs.ok_button}" action="#{OrganizerSignupMBean.addAttendee}"/>
						   							<h:commandButton value="#{msgs.cancel_button}" action="" onclick="clearPanels(); return false;"/>
						   						</h:panelGrid>
					   						</h:panelGrid>
						   				</h:panelGroup>
						   				
						   			</h:panelGrid>
						   		</h:panelGroup>	
					   		</h:column>
					   		
					   		<h:column rendered="#{!OrganizerSignupMBean.meetingWrapper.meeting.allowWaitList}">		   
								<f:facet name="header">
									<h:panelGroup>
										<h:outputText value="#{msgs.tab_waiting_list}" escape="false"/>
										<h:outputText value="#{msgs.tab_waiting_list_disabled}" escape="false" style="color:gray;" />
									</h:panelGroup>
								</f:facet>
								<h:panelGroup style="margin-left: 1px;">
								   		<h:graphicImage value="/images/addDisabled.png"  alt="Disabled: add an waiter" title="#{msgs.event_tool_tips_action_option_disabled_label}" style="border:none" />
								   		<h:outputText value="#{msgs.event_add_attendee}" title="#{msgs.event_tool_tips_action_option_disabled_label}" escape="false" styleClass="disabledAddAttendee" style="color:gray;"/>
								</h:panelGroup>
							</h:column>  						   						   		
					   		<h:column rendered="#{OrganizerSignupMBean.meetingWrapper.meeting.allowWaitList}">		   
								<f:facet name="header">
									<h:outputText value="#{msgs.tab_waiting_list}" escape="false"/>
								</f:facet>
								<h:panelGroup>
										<h:panelGrid columns="1" rendered="#{!timeSlotWrapper.timeSlot.unlimitedAttendee}" styleClass="organizerAction">
									   			<h:dataTable id="waiterSpots" value="#{timeSlotWrapper.waitingList}" binding="#{OrganizerSignupMBean.waiterWrapperTable}" var="waiterWrapper">
									   				<h:column>
									   					<h:panelGroup rendered="#{waiterWrapper.signupAttendee.attendeeUserId !=null}">
									   						<h:commandLink id="removeWaitingList" action="#{OrganizerSignupMBean.removeAttendeeFromWList}" title="#{msgs.event_tool_tips_delete}" rendered="#{!OrganizerSignupMBean.meetingWrapper.meeting.meetingExpired}">
									   							<h:graphicImage value="/images/delete.png"  alt="delete" style="border:none" styleClass="openCloseImageIcon"/>
									   							<f:param id="waiterUserId" name="#{OrganizerSignupMBean.attendeeUserId}" value="#{waiterWrapper.signupAttendee.attendeeUserId}"/>
									   						</h:commandLink>
									   						<h:outputText value="&nbsp;" escape="false" />
									   						<h:outputText value="#{waiterWrapper.displayName}" escape="false"/>				   					
									   					</h:panelGroup>		  
									   				</h:column>				   		
									   			</h:dataTable>
									   			
									   			<h:panelGroup id="addWaiter" rendered="#{!OrganizerSignupMBean.meetingWrapper.meeting.meetingExpired}">
									   				<h:outputLabel rendered="#{!timeSlotWrapper.timeSlot.available}" onclick="showHideAddWaiterPanel('#{timeSlotWrapper.positionInTSlist}');" styleClass="addWaiter">
								   						<h:graphicImage value="/images/add.png"  alt="add an waiter"  title="#{msgs.event_tool_tips_add}" style="border:none"  styleClass="openCloseImageIcon"/>
								   						<h:outputText value="#{msgs.event_add_attendee}" escape="false" />	
								   					</h:outputLabel>
								   					<h:panelGroup rendered="#{timeSlotWrapper.timeSlot.available}" style="margin-left: 1px;">
								   						<h:graphicImage value="/images/addDisabled.png"  alt="add an waiter" title="#{msgs.event_tool_tips_action_disabled_label}" style="border:none" />
								   						<h:outputText value="#{msgs.event_add_attendee}" escape="false" styleClass="disabledAddAttendee"/>
								   					</h:panelGroup>
								   				</h:panelGroup>
								   				
								   				<h:panelGroup id="addWaiterPanel" style="display: none;">
								   					<h:panelGrid columns="1" >
								   							<h:panelGroup>
											   							<h:selectOneRadio id="selcetAddWaiterType"  binding="#{OrganizerSignupMBean.listPendingType}" >
																			<f:selectItem id="bottom"  itemValue="#{OrganizerSignupMBean.onBottomList}" itemLabel="#{msgs.add_to_bottom}" />
																			<f:selectItem id="top" itemValue="#{OrganizerSignupMBean.onTopList}" itemLabel="#{msgs.add_to_top}" />							
											   							</h:selectOneRadio>	
										   					</h:panelGroup>
										   					<h:panelGrid id="newWaiterTable" columns="2" >
									   							<h:outputText value="#{msgs.attendee_select}" escape="false" rendered="#{!OrganizerSignupMBean.eidInputMode}"/>
									   							<h:outputText value="#{msgs.attendee_enterEid}" escape="false" rendered="#{OrganizerSignupMBean.eidInputMode}"/>
									   							
									   							<h:panelGroup rendered="#{!OrganizerSignupMBean.eidInputMode}">
										   							<h:selectOneMenu  id="newWaiterList" binding="#{OrganizerSignupMBean.waiterEid}">
									   									<f:selectItems value="#{OrganizerSignupMBean.allAttendees}" />
									   								</h:selectOneMenu>
									   							</h:panelGroup>
									   							<h:inputText id="addWaiterEidInput" value="#{OrganizerSignupMBean.userInputEid}" rendered="#{OrganizerSignupMBean.eidInputMode}" size="8"/>
								   								
								   								
									   						    <h:commandButton value="#{msgs.ok_button}" action="#{OrganizerSignupMBean.addAttendeeToWList}"/>
									   							<h:commandButton value="#{msgs.cancel_button}" action="" onclick="clearPanels(); return false;" immediate="true"/>
									   						</h:panelGrid>
									   				</h:panelGrid>
								   				</h:panelGroup>
									   			
							   			</h:panelGrid>
						   			
						   			<h:outputText value="#{msgs.no_addWaiter_button_due_to_unlimited}" escape="false" rendered="#{timeSlotWrapper.timeSlot.unlimitedAttendee}"/>			   			
						   		</h:panelGroup>	
					   		 </h:column>
					   	   			   				   		
					   		
					   </h:dataTable>
					   
					   <sakai:doc_section>
							<h:panelGrid columns="2" styleClass="instruction" rendered="#{!OrganizerSignupMBean.announcementType}">
								<h:outputText value="#{msgs.organizer_note_name}" escape="false" />
								<h:outputText value="#{msgs.organizer_instruction_timeslot_link}" escape="false"/> 						
								
								<h:outputText value="&nbsp;" escape="false"/>
								<h:panelGroup>							 
									<h:outputText value="#{msgs.organizer_instruction_click}" escape="false" />
									<h:graphicImage value="/images/edit.png"/>
									<h:outputText value="#{msgs.organizer_instruction_edit_image}" escape="false"/> 
								</h:panelGroup>
								
								<h:outputText value="&nbsp;" escape="false"/>
								<h:panelGroup >							 
									<h:outputText value="#{msgs.organizer_instruction_click}" escape="false"/>
									<h:graphicImage value="/images/delete.png"/>
									<h:outputText value="#{msgs.organizer_instruction_delete_image}" escape="false"/>
								</h:panelGroup>
								
								<h:outputText value="&nbsp;" escape="false"/>
								<h:outputText value="#{msgs.organizer_instruction_max_capacity}" escape="false"/>
							</h:panelGrid>
					</sakai:doc_section>	
							
							 	
					<sakai:button_bar>					
						<sakai:button_bar_item id="goback" action="listMeetings" value="#{msgs.goback_button}"/>					
	                </sakai:button_bar>
	                
	                <h:outputText value="&nbsp;" escape="false"/>
			</h:panelGrid>
		</h:form>
			 
  		</sakai:view_content>	
	</sakai:view_container>
	<f:verbatim>
		<script>
			var firstAttendee = document.getElementById("meeting:selectedFirstUser");
			var userActionType =document.getElementById("meeting:userActionType");
			var lastActivePanel;
			var lastActionEditPl;
			var lastTsEditPanel;
			var lastActiveUserCursor;
			var lastClickedAddImage;
			var lastUserInputEid;
			var defaultColor='black';
			var predefinedByJSF = "meeting:timeslots:";//tag prefix-id form.name + datatable name
			var predefMidPartByJSF=":availableSpots:";//tag id for datatable
			var defaultUserAction ="replaceAction";			
			var hiddenInputCollapeMInfo =document.getElementById("meeting:meetingInfoCollapseExpand");
			var showMInfoTitleTag =document.getElementById("meeting:showMeetingTitleOnly");
			//initialize
			initMeetingInfoDetail();
			
			function initMeetingInfoDetail(){
				var collapseMInfoTag =document.getElementById("meeting:meetingInfoDetails");				
				if(collapseMInfoTag && hiddenInputCollapeMInfo && hiddenInputCollapeMInfo.value == 'true'){
					collapseMInfoTag.style.display="none";
					showMInfoTitleTag.style.display="";
					//reverse the default:show when page refreshed
					showDetails('meeting:imageOpen_meetingInfoDetail','meeting:imageClose_meetingInfoDetail');
				}else{
					collapseMInfoTag.style.display="";
					showMInfoTitleTag.style.display="none";
				}	
			}
			
			function setMeetingCollapseInfo(val){				
				hiddenInputCollapeMInfo.value=val;
				if(val)				  
				  	showMInfoTitleTag.style.display="";
				 else
				  	showMInfoTitleTag.style.display="none";				  
			}
			
			function showHideEditPanel(timeslotId, attendeeIndex, attendee){
				if(lastActivePanel)
					lastActivePanel.style.display="none";
					
				var editPanel = document.getElementById(predefinedByJSF + timeslotId + predefMidPartByJSF +attendeeIndex + ":editPanel");
    			editPanel.style.display = "block";
    			firstAttendee.value = attendee;
    			//alert("firstUser:" +firstAttendee.value);
    			lastActivePanel=editPanel;
    			
    			if (lastClickedAddImage)
					lastClickedAddImage.style.display = "block";
				//pass current AddImageTag
				lastClickedAddImage= document.getElementById(predefinedByJSF + timeslotId +":addAttendee");	
				lastClickedAddImage.style.display = "none";
    			
    			
    			showHideActionTypePanel(timeslotId, attendeeIndex, defaultUserAction);
    			
    			//change color to highlight selected attendee
    			if(lastActiveUserCursor)
					lastActiveUserCursor.style.color=defaultColor;

    			var activeUserCursor = document.getElementById(predefinedByJSF + timeslotId + predefMidPartByJSF +attendeeIndex + ":editLink");
				defaultColor=activeUserCursor.style.color;
				activeUserCursor.style.color="blue";
				lastActiveUserCursor=activeUserCursor;
				
				if (lastUserInputEid)
					lastUserInputEid.value="";
				lastUserInputEid=document.getElementById(predefinedByJSF + timeslotId +predefMidPartByJSF +attendeeIndex + ":replaceEidInput");
			}
			
			function showHideAddPanel(timeslotId, attendeeIndex){				
				clearPanels();
				if (activeUserCursor)
					activeUserCursor.style.color=defaultColor;
				
				lastClickedAddImage= document.getElementById(predefinedByJSF + timeslotId +":addAttendee");	
				lastClickedAddImage.style.display = "none";
						
				var addPanel = document.getElementById(predefinedByJSF + timeslotId +":addPanel");
    			addPanel.style.display = "block";
    			var activeUserCursor = document.getElementById(predefinedByJSF + timeslotId +":addAttendee");
				activeUserCursor.style.color="blue";    			
    			lastActivePanel=addPanel;
    			lastActiveUserCursor=activeUserCursor;
    			lastUserInputEid=document.getElementById(predefinedByJSF + timeslotId +":addAttendeeEidInput");
			}
			
			function showHideActionTypePanel(timeslotId,attendeeIndex,actType){
				if(lastActionEditPl)
					lastActionEditPl.style.display="none";
				if (lastTsEditPanel)
					lastTsEditPanel.style.display="none";
					
				var actTypePanel = document.getElementById(predefinedByJSF + timeslotId +predefMidPartByJSF +attendeeIndex + ":" + actType);
				actTypePanel.style.display = "block";
    			userActionType.value=actType;
    			lastActionEditPl=actTypePanel;
			}
			
			function showHideAddWaiterPanel(timeslotPosIndex){
				clearPanels();
				
				var radiobxns = document.getElementsByName(predefinedByJSF + timeslotPosIndex +":selcetAddWaiterType");
				if(radiobxns)   
					radiobxns[0].checked=true;//set radioBxn to bottom
				
				lastClickedAddImage= document.getElementById(predefinedByJSF + timeslotPosIndex +":addWaiter");	
				lastClickedAddImage.style.display = "none";
				
							
				var waiterPanel = document.getElementById(predefinedByJSF + timeslotPosIndex +":addWaiterPanel");
				waiterPanel.style.display = "block";
				var activeUserCursor = document.getElementById(predefinedByJSF + timeslotPosIndex +":addWaiter");
				activeUserCursor.style.color="blue"; 
				lastActionEditPl=waiterPanel;
				lastActiveUserCursor=activeUserCursor;
				lastUserInputEid=document.getElementById(predefinedByJSF + timeslotPosIndex + ":addWaiterEidInput");
			}
			
			function showEditTimeslot(timeslotPosIndex){
				clearTimeslotPanel();			
				var tsEditPanel = document.getElementById(predefinedByJSF + timeslotPosIndex +":editTimeSlot");
				tsEditPanel.style.display == 'none' ? tsEditPanel.style.display = 'block' : tsEditPanel.style.display = 'none';
				lastTsEditPanel=tsEditPanel;
			
			}
			function clearPanels(){
				if (lastTsEditPanel)
					lastTsEditPanel.style.display="none";
				clearTimeslotPanel();
			}
			function clearTimeslotPanel(){
				if(lastActivePanel)
					lastActivePanel.style.display = "none";
				if(lastActionEditPl)
					lastActionEditPl.style.display="none";
				if(lastActiveUserCursor)
					lastActiveUserCursor.style.color=defaultColor;
				if (lastUserInputEid)		
					lastUserInputEid.value="";
				if (lastClickedAddImage)
					lastClickedAddImage.style.display = "block";
			}
			
			//Due to JSF commandLink don't have onclick() method and this is a way to go around
			var deleteClick;
			var deleteMsg='Are you sure to do this?'; //default          
			function assignDeleteClick(link,msg) {
			  if (link.onclick == confirmDelete) {
			    return;
			  }
			                
			  deleteClick = link.onclick;
			  deleteMsg = msg;
			  link.onclick = confirmDelete;
			}
			            
			function confirmDelete() {
			  var ans = confirm(deleteMsg);
			  if (ans) {
			    return deleteClick();
			  } else {
			    return false;
			  }
			}
			
			function confirmTsCancel(link,msg){
			if (link.onclick == confirmDelete) {
			    return;
			  }
			                
			  deleteClick = link.onclick;
			  deleteMsg = msg;
			  link.onclick = confirmDelete;
			}
			
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
