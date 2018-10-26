<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai"%>

<f:view locale="#{UserLocale.locale}">
	<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader"
		scope="session">
		<jsp:setProperty name="msgs" property="baseName" value="messages" />
	</jsp:useBean>
	<sakai:view_container title="#{msgs.attend_view_title} #{AttendanceSignupBean.meetingWrapper.meeting.title}">
		<style type="text/css">
			@import url("/sakai-signup-tool/css/signupStyle.css");
		</style>
<h:outputText value="#{Portal.latestJQuery}" escape="false"/>
		<script TYPE="text/javascript" LANGUAGE="JavaScript"
			src="/sakai-signup-tool/js/signupScript.js"></script>
		<script type="text/javascript">
			jQuery(document).ready(function(){
	            sakai.setupSelectListMultiple('availableSpots', 'selectAllThese', 'selectedSelected');
	            sakai.setupPrintPreview();
				sakai.setupWaitListed();
				jQuery('a.print-window').click(function(){
					javascript:window.print();
	                return false;
	            });
	        });
	    </script>
	    
		<ul class="navIntraTool actionToolbar">
			<li class="firstToolBarItem" role="menuitem"><span>
				<a
				class="print-window" href="#">
				<h:outputText
					value="#{msgs.print_friendly}" /></a></span></li>
		</ul>
		<sakai:view_content>
			<div class="toggle specialLink noPrint" style="display:none"><a href="#"><h:outputText value="#{msgs.attend_view_toggle}" /></a></div>
			<%--//TODO: the value and conditions for the generic error messages will need to change--%>
			<h:outputText
				value="#{msgs.event_error_alerts} #{messageUIBean.errorMessage}"
				styleClass="alertMessage" escape="false"
				rendered="#{messageUIBean.error}" />
			<h:form id="attendanceView">

				<%--//TODO: attend.event.title below needs to resolve to the event title--%>
				<h3><h:outputText value="#{msgs.attend_view_title}" /> <h:outputText
					value="#{AttendanceSignupBean.meetingWrapper.meeting.title}"
					styleClass="highlight" /></h3>



				<%--//TODO: this datatable needs to be bound to the participants for an event 
					the value, binding attributes need to reflect this--%>
				<h:dataTable id="attendanceList"
					value="#{AttendanceSignupBean.timeslotWrappers}"
					binding="#{AttendanceSignupBean.timeslotWrapperTable}"
					var="timeSlotWrapper" style="width:50%;margin-top:0"
					styleClass="listHier centerlines"
					headerClass="skip"
					summary="#{msgs.attend_view_list_summary}">

					<h:column>
						<f:facet name="header">
							<h:outputText value="#{msgs.attend_view_list_head}" />
						</f:facet>
						<h:panelGroup id="timeslot">
							<f:verbatim><h4 style="font-weight:bold;margin:.5em 0"></f:verbatim>
								<h:outputText value="#{timeSlotWrapper.timeSlot.startTime}">
									<f:convertDateTime pattern="h:mm a" timeZone="#{UserTimeZone.userTimeZone}"/>
								</h:outputText>
								<h:outputText value="#{timeSlotWrapper.timeSlot.startTime}"
									rendered="#{AttendanceSignupBean.meetingWrapper.meeting.meetingCrossDays}">
									<f:convertDateTime pattern=", EEE" timeZone="#{UserTimeZone.userTimeZone}"/>
								</h:outputText>
								<h:outputText value="#{msgs.timeperiod_divider}" escape="false" />
								<h:outputText value="#{timeSlotWrapper.timeSlot.endTime}">
									<f:convertDateTime pattern="h:mm a" timeZone="#{UserTimeZone.userTimeZone}"/>
								</h:outputText>
								<h:outputText value="#{timeSlotWrapper.timeSlot.endTime}"
									rendered="#{AttendanceSignupBean.meetingWrapper.meeting.meetingCrossDays}">
									<f:convertDateTime pattern=", EEE, " timeZone="#{UserTimeZone.userTimeZone}"/>
								</h:outputText>
								<h:outputText value="#{timeSlotWrapper.timeSlot.endTime}"
									rendered="#{AttendanceSignupBean.meetingWrapper.meeting.meetingCrossDays}">
									<f:convertDateTime dateStyle="short" timeZone="#{UserTimeZone.userTimeZone}"/>
								</h:outputText>
							<f:verbatim><h4></f:verbatim>
						</h:panelGroup>
						
						<h:panelGroup rendered="#{!timeSlotWrapper.timeSlot.canceled}">
							<h:outputText rendered="#{empty timeSlotWrapper.attendeeWrappers}" value="#{msgs.attend_view_list_slot_list_empty_msg}" styleClass="instruction" style="display:block;padding:1em 2em"/>
							<h:dataTable id="availableSpots"
								rowClasses="oddRow,evenRow" styleClass="listHier lines nolines centerlines availableSpots"
								style="margin:0 2em;width:90%"
								value="#{timeSlotWrapper.attendeeWrappers}"
								var="attendeeWrapper"
								headerClass="subListHeader noPrint"
								summary="#{msgs.attend_view_list_slot_list_summary}"
								rendered="#{!empty timeSlotWrapper.attendeeWrappers}"
								>	
								
								<h:column>
									<f:facet name="header">
										<h:outputText escape="false" value="<label><input type='checkbox' class='selectAllThese'/> #{msgs.attend_view_select_all} </label>"/>
									</f:facet>
									<h:panelGroup>
									<h:selectBooleanCheckbox value="#{attendeeWrapper.attended}" id="attendee"/>
										<h:outputLabel value="#{attendeeWrapper.displayName}"
											for="attendee"
											rendered="#{attendeeWrapper.signupAttendee.attendeeUserId !=null}" />

									</h:panelGroup>
								</h:column>
							</h:dataTable>
						</h:panelGroup>
						
						<h:panelGroup rendered="#{!timeSlotWrapper.timeSlot.canceled}">
							<h:dataTable id="waitList"
								rowClasses="oddRow,evenRow"
								styleClass="listHier lines nolines centerlines waitListed"
								style="margin:0 2em;width:90%"
								value="#{timeSlotWrapper.waitingList}"
								var="waitingList"
								headerClass="subListHeader"
								summary="#{msgs.attend_view_list_slot_list_summary}"
								rendered="#{!empty timeSlotWrapper.waitingList}"
								>	
								
								<h:column>
									<f:facet name="header">
										<h:outputText escape="true" value=" #{msgs.attend_view_list_slot_list_wait_head}"/>
									</f:facet>
									<h:panelGroup>
									<h:selectBooleanCheckbox value="#{waitingList.attended}" id="attendee"/>
										<h:outputLabel value="#{waitingList.displayName}"
											for="attendee"
											rendered="#{waitingList.signupAttendee.attendeeUserId !=null}" />

									</h:panelGroup>
								</h:column>
							</h:dataTable>
						</h:panelGroup>
						
						<h:panelGroup rendered="#{timeSlotWrapper.timeSlot.canceled}">
								<h:outputText value="#{msgs.attend_view_list_slot_list_canceled}" styleClass="instruction" style="display:block;padding:1em 2em"/>
						</h:panelGroup>
					</h:column>

				</h:dataTable>

				<div class="act noPrint">
					<h:commandButton action="#{AttendanceSignupBean.doSave}"
						value="#{msgs.save_button}" />
					<h:commandButton action="#{AttendanceSignupBean.doCancel}"
						value="#{msgs.cancel_button}" />
				</div>
			</h:form>
		</sakai:view_content>
	</sakai:view_container>
</f:view>
