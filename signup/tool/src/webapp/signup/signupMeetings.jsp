<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<f:view>
<f:loadBundle basename="messages" var="msgs"/>
	<sakai:view_container title="Signup Tool">
		<style type="text/css">
				@import url("/sakai-signup-tool/css/signupStyle.css");
		</style>
			
		<h:form id="addMeeting">
			<sakai:tool_bar>
				<sakai:tool_bar_item value="#{msgs.add_new_event}" action="#{SignupMeetingsBean.addMeeting}" rendered="#{SignupMeetingsBean.allowedToCreate}"/>
			</sakai:tool_bar>
		</h:form>

		<sakai:view_content>
			<h:outputText value="#{msgs.event_error_alerts} #{errorMessageUIBean.errorMessage}" styleClass="alertMessage" escape="false" rendered="#{errorMessageUIBean.error}"/> 
			<h:form id="items">
			 	<sakai:view_title value="#{msgs.signup_tool}"/>

				<sakai:messages />
				<h:panelGrid columns="1">
					<h:outputText value="#{msgs.events_organizer_instruction}"  rendered="#{SignupMeetingsBean.allowedToUpdate && SignupMeetingsBean.meetingsAvailable}" escape="false"/>
					<h:outputText value="#{msgs.events_attendee_instruction}" rendered="#{!SignupMeetingsBean.allowedToUpdate && SignupMeetingsBean.meetingsAvailable}" escape="false"/>
					<h:outputText value="&nbsp;" escape="false"/>
				</h:panelGrid>
				<h:panelGrid columns="1">
					<h:panelGroup>
						<h:outputText value="#{msgs.events_dropdownbox_title}&nbsp;" escape="false"/>
						<h:selectOneMenu id="viewByRange" value="#{SignupMeetingsBean.viewDateRang}" valueChangeListener="#{SignupMeetingsBean.processSelectedRange}" onchange="submit();">
							<f:selectItem itemValue="30" itemLabel="#{msgs.view_current_month}"/>
							<f:selectItem itemValue="90" itemLabel="#{msgs.view_current_three_months}"/>
							<!-- f:selectItem itemValue="180" itemLabel="#{msgs.view_semi_year}"/ -->
							<f:selectItem itemValue="10000" itemLabel="#{msgs.view_all_future_meetings}"/>
							<!-- f:selectItem itemValue="-1" itemLabel="#{msgs.view_previous_events}"/ -->
							<f:selectItem itemValue="all" itemLabel="#{msgs.view_all}"/>
						</h:selectOneMenu>
					</h:panelGroup>
					
					<h:outputText value="&nbsp;" escape="false"/>
				</h:panelGrid>
				<h:panelGrid columns="1" styleClass="noMeetingsWarn">
					<h:outputText value="#{msgs.no_events_in_future_organizer}" rendered="#{!SignupMeetingsBean.meetingsAvailable && SignupMeetingsBean.allowedToCreate && SignupMeetingsBean.selectedViewFutureMeetings}" escape="false"/>
					<h:outputText value="#{msgs.no_events_in_timeframe_organizer}" rendered="#{!SignupMeetingsBean.meetingsAvailable && SignupMeetingsBean.allowedToCreate && SignupMeetingsBean.selectedViewAllMeetings}" escape="false"/>
					<h:outputText value="#{msgs.no_events_in_timeframe_attendee}"  rendered="#{!SignupMeetingsBean.meetingsAvailable && !SignupMeetingsBean.allowedToCreate && SignupMeetingsBean.selectedViewAllMeetings}" escape="false"/>
					<h:outputText value="#{msgs.no_events_in_this_period_attendee_orgnizer}"  rendered="#{!SignupMeetingsBean.meetingsAvailable && !SignupMeetingsBean.selectedViewAllMeetings && !SignupMeetingsBean.selectedViewFutureMeetings}" escape="false"/>
					<h:outputText value="#{msgs.no_events_in_this_period_attendee_orgnizer}"  rendered="#{!SignupMeetingsBean.allowedToCreate && !SignupMeetingsBean.meetingsAvailable && SignupMeetingsBean.selectedViewFutureMeetings}" escape="false"/>				
				</h:panelGrid>	
				<h:panelGroup rendered="#{SignupMeetingsBean.meetingsAvailable}">
								 	
				 	<t:dataTable 
				 		id="meetinglist"
				 		value="#{SignupMeetingsBean.signupMeetings}"
				 		binding="#{SignupMeetingsBean.meetingTable}"
				 		sortColumn="#{SignupMeetingsBean.signupSorter.sortColumn}"
				 		sortAscending="#{SignupMeetingsBean.signupSorter.sortAscending}"				 		
				 		var="wrapper" style="width:100%;" 
				 		rowClasses="oddRow,evenRow"
				 		styleClass="signupTable">
	
						<t:column defaultSorted="true" sortable="true">
							<f:facet name="header" >
								<t:commandSortHeader columnName="#{SignupMeetingsBean.signupSorter.titleColumn}" immediate="true" arrow="true">
									<h:outputText value="#{msgs.tab_event_name}" escape="false"/>
								</t:commandSortHeader>
							</f:facet>
							<h:commandLink action="#{SignupMeetingsBean.processSignup}">
								<h:outputText value="#{wrapper.meeting.title}" />
							</h:commandLink>												
						</t:column>
						
						<t:column sortable="true">
							<f:facet name="header">
								<t:commandSortHeader columnName="#{SignupMeetingsBean.signupSorter.createColumn}" immediate="true" arrow="true">
									<h:outputText value="#{msgs.tab_event_owner}" escape="false"/>
								</t:commandSortHeader>
							</f:facet>
							<h:outputText value="#{wrapper.creator}"/>												
						</t:column>
						
						<t:column sortable="true">
							<f:facet name="header">
								<t:commandSortHeader columnName="#{SignupMeetingsBean.signupSorter.locationColumn}" immediate="true" arrow="true">
									<h:outputText value="#{msgs.tab_event_location}" escape="false"/>
								</t:commandSortHeader>
							</f:facet>
							<h:outputText value="#{wrapper.meeting.location}"/>												
						</t:column>
	
						<t:column>
							<f:facet name="header">
								<t:commandSortHeader columnName="#{SignupMeetingsBean.signupSorter.dateColumn}" immediate="true" arrow="true">
									<h:outputText value="#{msgs.tab_event_date}" escape="false"/>
								</t:commandSortHeader>
							</f:facet>
							<h:panelGroup>
								<h:outputText value="#{wrapper.meeting.startTime}">
									<f:convertDateTime  pattern="EEE, " />
								</h:outputText>
								<h:outputText value="#{wrapper.meeting.startTime}">
									<f:convertDateTime  dateStyle="short" />
								</h:outputText>
							</h:panelGroup>
						</t:column>
																
						<t:column>
							<f:facet name="header">
								<h:outputText value="#{msgs.tab_event_time}" escape="false"/>
							</f:facet>
							<h:panelGroup style="white-space: nowrap;">
								<h:outputText value="#{wrapper.meeting.startTime}">
									<f:convertDateTime timeStyle="short" />
								</h:outputText>
								<h:outputText value="#{wrapper.meeting.startTime}" rendered="#{wrapper.meeting.meetingCrossDays}">
											<f:convertDateTime pattern=", EEE" />
								</h:outputText>
								<h:outputText value="#{msgs.timeperiod_divider}" escape="false"/>
								<h:outputText value="#{wrapper.meeting.endTime}">
									<f:convertDateTime timeStyle="short" />
								</h:outputText>
								<h:outputText value="#{wrapper.meeting.endTime}" rendered="#{wrapper.meeting.meetingCrossDays}">
											<f:convertDateTime pattern=", EEE" />
								</h:outputText>	
							</h:panelGroup>	
						</t:column>
						
						<t:column>
							<f:facet name="header">
								<h:outputText value="#{msgs.tab_event_availability}" escape="false"/>
							</f:facet>
							<h:outputText value="#{wrapper.availableStatus}" escape="false"/>												
						</t:column>	
						
						<t:column rendered="#{SignupMeetingsBean.allowedToDelete}">
							<f:facet name="header">
								<h:outputText value="#{msgs.tab_event_remove}" escape="false"/>
							</f:facet>
							<h:selectBooleanCheckbox value="#{wrapper.selected}" rendered="#{wrapper.meeting.permission.delete}"/>
						</t:column>				
						
					</t:dataTable>
					
					<h:panelGrid columns="1">
						<h:outputText value="&nbsp;" escape="false"/>
						<h:commandButton id="removeMeetings" action="#{SignupMeetingsBean.removeMeetings}" value="#{msgs.event_removeButton}" onclick='return confirm("#{msgs.meeting_confirmation_to_remove}");' rendered="#{SignupMeetingsBean.allowedToDelete}"/>
					</h:panelGrid>
				</h:panelGroup>	
			 </h:form>
  		</sakai:view_content>	
	</sakai:view_container>
</f:view> 
