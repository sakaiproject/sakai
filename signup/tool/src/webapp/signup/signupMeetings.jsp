<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<f:view locale="#{UserLocale.locale}">
	<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
	   <jsp:setProperty name="msgs" property="baseName" value="messages"/>
	</jsp:useBean>
	<sakai:view_container title="Signup Tool">
		<style type="text/css">
				@import url("/sakai-signup-tool/css/signupStyle.css");
		</style>
<h:outputText value="#{Portal.latestJQuery}" escape="false"/>
		<script TYPE="text/javascript" src="/sakai-signup-tool/js/signupScript.js"></script>
		
		<script type="text/javascript">
	         //initialization of the page
	         jQuery(document).ready(function() {
		         //due to recuring meetings, make sure even/odd Rows display correctly
		         reprocessEvenOddRowClasses();

				});
	         var origClassNames=new Array();
	         var lastActiveId;
	         var previousBgColor; 
	         var recurRowClass="recurRow";//defined in css
	         var evenRowClass = "evenRow";
	         var oddRowClass = "oddRow";
	         function reprocessEvenOddRowClasses(){
		         	var trRowTags = document.getElementsByTagName("tr");
		         	rowNum=0;
		         	if (!trRowTags)
		         		return;
		         		
		         	for(i=0; i<trRowTags.length;i++){
		         		if(trRowTags[i].style.display !="none"
		         			&& (trRowTags[i].className == evenRowClass || trRowTags[i].className == oddRowClass) ){
		         			if(rowNum % 2 == 0)
		         				trRowTags[i].className = oddRowClass;
		         			else
		         				trRowTags[i].className = evenRowClass;
		         			
		         			rowNum++;
		         		}

		         	}

		         }

		         function showAllRelatedRecurMeetings(id,iFrameId) {
		         	var activeOne = document.getElementById(id);
		         	if (!activeOne)
		         		return;

		         	if ( activeOne.className != recurRowClass)
		         		updateOrigClassNames(id,activeOne.className)					
		         		

		         	if (activeOne.className!=recurRowClass) 
		         		activeOne.className=recurRowClass;
		         	else
		         		activeOne.className=origClassNames[id];
		         	
		         	//hide last active one
		         	if (lastActiveId && lastActiveId !=id){
		         		resetRecurRows(lastActiveId);
		         	}
		         	lastActiveId = id;
		         						
		         	var i=0
		         	//alert("id:" +id);
		         	while (document.getElementById(id+"_" + i)!=null){					
		         		var row = document.getElementById(id+"_" +i)
		         		if (row !=null){					
		         			if (row.style.display == "none"){
		         				row.className=recurRowClass;
		         				row.style.display = "";
		         			}else
		         				row.style.display = "none";
		         		}
		         		i++;
		         	}
		         	//reSize the iFrame
		         	//signup_resetIFrameHeight(iFrameId);//no refresh			
		         }

		         function resetRecurRows(recurRowId){
		         	row = document.getElementById(recurRowId);
		         	if (row)
		         		row.className=origClassNames[recurRowId];

		         	var i=0;
		         	while (document.getElementById(recurRowId+"_" + i)!=null){					
		         		var row = document.getElementById(recurRowId+"_" +i)
		         		if (row !=null){					
		         			if (row.style.display == "")
		         				row.style.display = "none";
		         		}
		         		i++;
		         	}
		         	
		         	document.getElementById('imageOpen_RM_'+ recurRowId).style.display="none";
		         	document.getElementById('imageClose_RM_'+ recurRowId).style.display="";				
		         }

		         function updateOrigClassNames(id, className){
		         	if (!origClassNames[id])				
		         		origClassNames[id]=className;
		         }

		         //IE browser will not show the disabled option item
		         var orignSelectorTag=document.getElementById('items:viewByRange');
		         var origSelectorIndex = orignSelectorTag? orignSelectorTag.selectedIndex : 2;//default
		         function validateIEDisabledItem(selector){
		         	if(!orignSelectorTag)
		         		return true;

		         	if (selector.value !="none"){
		         		origSelectorIndex = orignSelectorTag.selectedIndex;
		         		return true;
		         	}else{
		         		selector.selectedIndex = origSelectorIndex;//restore				
		         	}
		         	
		         	return false;
		         }

		         /* Determines what delete message to use in the confirm box. 
		          * If we are only deleting singles then you get the normal message, but if any of the selections are the first one in a recurring meeting
		          * then the msg changes.
		          */
		         var deleteMultipleCount = 0;
		         function determineDeleteMessage(elem, multiple) {
		         	
		         	if(multiple) {
		         		if (elem.checked == true) {
		         			deleteMultipleCount++;
		         		} else {
		         			deleteMultipleCount--;
		         		}
		         	}
		         }

		         /* If we have selected one or more checkboxes that contain multiples to delete, then return the appropriate message */
		         function getDeleteMessage() {
		         	
		         	if(deleteMultipleCount > 0) {
		         		return '<h:outputText escape="false" value="#{msgs.meeting_confirmation_to_remove_multiple}" />';
		         	}
		         	return '<h:outputText escape="false" value="#{msgs.meeting_confirmation_to_remove}" />';
		         };

		         // Provide user a chance to cancel or confirm the deletion of meetings. 
		         function confirmDelete(el) {
		         	var answer = confirm(getDeleteMessage());

		         	if (answer == true){
		         		displayProcessingIndicator(el);
		         		return true;
		         	}
		         	return false;
		         };		         	
		</script>
			
		<h:form id="addMeeting">
			<h:panelGroup>
				<f:verbatim><ul class="navIntraTool actionToolbar" role="menu"></f:verbatim> 
				<h:panelGroup rendered="#{SignupMeetingsBean.allowedToCreate}">
						<f:verbatim><li role="menuitem" class="firstToolBarItem"> <span></f:verbatim>
							<h:commandLink value="#{msgs.add_new_event}" action="#{SignupMeetingsBean.addMeeting}" rendered="#{SignupMeetingsBean.allowedToCreate}"/>
					<f:verbatim></span></li></f:verbatim>
				 </h:panelGroup>
				 
				<h:panelGroup rendered="#{SignupPermissionsUpdateBean.showPermissionLink}"> 	
					<f:verbatim><li role="menuitem" ><span></f:verbatim>	
						<h:commandLink value="#{msgs.permission_feature_link}" action="#{SignupPermissionsUpdateBean.processPermission}" rendered="#{SignupPermissionsUpdateBean.showPermissionLink}"/>
					<f:verbatim></span></li></f:verbatim>
				</h:panelGroup>
				
				<h:panelGroup>
					<f:verbatim><li role="menuitem" ><span></f:verbatim>
						<h:commandLink value="#{msgs.event_pageTop_link_for_download}" action="#{DownloadEventBean.downloadSelections}" />
					<f:verbatim></span></li></f:verbatim>
				</h:panelGroup>
				
			  <f:verbatim></ul></f:verbatim>
			</h:panelGroup>
		</h:form>

		<sakai:view_content>
			<h:outputText value="#{msgs.event_error_alerts} #{messageUIBean.errorMessage}" styleClass="alertMessage" escape="false" rendered="#{messageUIBean.error}"/> 
			<h:form id="items">
			 	<sakai:view_title value="#{msgs.signup_tool}"/>

				<h:panelGroup styleClass="" rendered="#{(SignupMeetingsBean.allowedToUpdate && SignupMeetingsBean.meetingsAvailable) or (!SignupMeetingsBean.allowedToUpdate && SignupMeetingsBean.meetingsAvailable)}">
					<h:outputText value="#{msgs.events_organizer_instruction}"  rendered="#{SignupMeetingsBean.allowedToUpdate && SignupMeetingsBean.meetingsAvailable}" escape="false"/>
					<h:outputText value="&nbsp;" escape="false"/>
					<h:outputText value="#{msgs.events_attendee_instruction}" rendered="#{!SignupMeetingsBean.allowedToUpdate && SignupMeetingsBean.meetingsAvailable}" escape="false"/>
				</h:panelGroup>
				
				
				<div class="form-group row">
					<!-- view range dropdown -->
					<h:outputLabel value="#{msgs.events_dropdownbox_title} "  for="viewByRange" styleClass="form-control-label col-lg-1 col-md-1"/>
					<div class="col-lg-3 col-md-3">
						<h:selectOneMenu id="viewByRange" value="#{SignupMeetingsBean.viewDateRang}" valueChangeListener="#{SignupMeetingsBean.processSelectedRange}" onchange="if(validateIEDisabledItem(this)){submit()};">
							<f:selectItems value="#{SignupMeetingsBean.viewDropDownList}"/>
						</h:selectOneMenu>
					</div>
					<!-- filter by category dropdown -->
					<h:outputLabel value="#{msgs.filter_by_category} " for="viewByCategory" styleClass="form-control-label col-lg-2 col-md-2"/>
					<div class="col-lg-2 col-md-2">
						<h:selectOneMenu id="viewByCategory" value="#{SignupMeetingsBean.categoryFilter}" valueChangeListener="#{SignupMeetingsBean.processSelectedCategory}" onchange="if(validateIEDisabledItem(this)){submit()};">
							<f:selectItems value="#{SignupMeetingsBean.allCategoriesForFilter}"/>
						</h:selectOneMenu>
					</div>
					<!--  expand all recurring meetings -->
					<h:panelGroup layout="block" styleClass="col-lg-4 col-md-4" rendered="#{SignupMeetingsBean.enableExpandOption && SignupMeetingsBean.meetingsAvailable}">
						<h:panelGroup >
								<h:selectBooleanCheckbox id="showallrecurmeeting" value="#{SignupMeetingsBean.showAllRecurMeetings}" valueChangeListener="#{SignupMeetingsBean.processExpandAllRcurEvents}" onclick="submit();"/>
								<h:outputLabel for="showallrecurmeeting" value="#{msgs.expand_all_recur_events}" escape="false"/>
						</h:panelGroup>
						<h:outputText value="&nbsp;" escape="false" rendered="#{!SignupMeetingsBean.enableExpandOption}"/>
					</h:panelGroup>
				</div>

				<h:panelGroup styleClass="noMeetingsWarn" rendered="#{!SignupMeetingsBean.meetingsAvailable}">
					<h:outputText value="#{SignupMeetingsBean.meetingUnavailableMessages}" escape="false" rendered="#{SignupMeetingsBean.userLoggedInStatus}"/>
					<h:outputText value=" #{msgs.you_need_to_login}" rendered="#{!SignupMeetingsBean.userLoggedInStatus}" escape="false"/>
				</h:panelGroup>
				<h:panelGroup rendered="#{SignupMeetingsBean.meetingsAvailable}">
					<div class="table-responsive">
				 	<t:dataTable 
				 		id="meetinglist"
				 		value="#{SignupMeetingsBean.signupMeetings}"
				 		binding="#{SignupMeetingsBean.meetingTable}"
				 		sortColumn="#{SignupMeetingsBean.signupSorter.sortColumn}"
				 		sortAscending="#{SignupMeetingsBean.signupSorter.sortAscending}"				 		
				 		var="wrapper" style="width:100%;" 
				 		rowId="#{wrapper.recurId}"
				 		rowStyle="#{wrapper.hideStyle}"
				 		rowClasses="oddRow,evenRow"
				 		columnClasses="removeCol, titleCol, creatorCol, locationCol, dateCol, timeCol, statusCol"
				 		styleClass="table table-bordered table-hover table-striped">
	
						<t:column defaultSorted="true" sortable="true">
							<f:facet name="header" >
								<t:commandSortHeader columnName="#{SignupMeetingsBean.signupSorter.titleColumn}" immediate="true" arrow="true">
									<h:outputText value="#{msgs.tab_event_name}" escape="false"/>
								</t:commandSortHeader>
							</f:facet>
							<h:panelGroup rendered="#{wrapper.firstOneRecurMeeting && wrapper.recurEventsSize >1}" styleClass="toggleMeetings">
								<h:outputText value="<span id='imageOpen_RM_#{wrapper.recurId}' style='display:none'>"  escape="false"/>
								<h:outputLink value="javascript:showDetails('imageOpen_RM_#{wrapper.recurId}','imageClose_RM_#{wrapper.recurId}');showAllRelatedRecurMeetings('#{wrapper.recurId}','#{SignupMeetingsBean.iframeId}');">
									<h:graphicImage value="/images/minusSmall.gif"  alt="open" styleClass="openCloseImageIcon" title="#{msgs.event_tool_tips_collapse_recur_meeting}" style="border:none" />
								</h:outputLink>
		   	    				<h:outputText value="</span>" escape="false" />
		   	    			
		   	    				<h:outputText value="<span id='imageClose_RM_#{wrapper.recurId}'>"  escape="false"/>
								<h:outputLink value="javascript:showDetails('imageOpen_RM_#{wrapper.recurId}','imageClose_RM_#{wrapper.recurId}');showAllRelatedRecurMeetings('#{wrapper.recurId}','#{SignupMeetingsBean.iframeId}');">
									<h:graphicImage title="#{msgs.event_tool_tips_expand_recur_meeting}" value="/images/plusSmall.gif" styleClass="openCloseImageIcon" alt="close" style="border:none" />
								</h:outputLink>
		   	    				<h:outputText value="</span>" escape="false" />
		   	    				
		   	    				<h:outputText value="&nbsp;" escape="false"/>
		   	    			</h:panelGroup>
									<h:commandLink id="cmdlink90" action="#{SignupMeetingsBean.processSignup}" >
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
						
						<%-- category --%>
						<t:column sortable="true">
							<f:facet name="header">
								<t:commandSortHeader columnName="#{SignupMeetingsBean.signupSorter.categoryColumn}" immediate="true" arrow="true">
									<h:outputText value="#{msgs.tab_event_category}" escape="false"/>
								</t:commandSortHeader>
							</f:facet>
							<h:outputText value="#{wrapper.meeting.category}"/>												
						</t:column>
	
						<t:column>
							<f:facet name="header">
								<t:commandSortHeader columnName="#{SignupMeetingsBean.signupSorter.dateColumn}" immediate="true" arrow="true">
									<h:outputText value="#{msgs.tab_event_date}" escape="false"/>
								</t:commandSortHeader>
							</f:facet>
							<h:panelGroup>
								<h:outputText value="#{wrapper.meeting.startTime}">
									<f:convertDateTime  pattern="EEE, " timeZone="#{UserTimeZone.userTimeZone}"/>
								</h:outputText>
								<h:outputText value="#{wrapper.meeting.startTime}">
									<f:convertDateTime  dateStyle="short" pattern="#{UserLocale.dateFormat}" timeZone="#{UserTimeZone.userTimeZone}"/>
								</h:outputText>
							</h:panelGroup>
						</t:column>
						
						<%-- switch the following two according to user selection --%>									
						<t:column rendered="#{!SignupMeetingsBean.showMyAppointmentTime}">
							<f:facet name="header">
								<h:outputText value="#{msgs.tab_event_time}" escape="false" />
								<h:outputText value="#{msgs.tab_event_your_appointment_time}" escape="false" rendered="#{SignupMeetingsBean.showMyAppointmentTime}"/>
							</f:facet>
							<h:panelGroup style="white-space: nowrap;">
								<h:outputText value="#{wrapper.meeting.startTime}">
									<f:convertDateTime pattern="h:mm a" timeZone="#{UserTimeZone.userTimeZone}"/>
								</h:outputText>
								<h:outputText value="#{wrapper.meeting.startTime}" rendered="#{wrapper.meeting.meetingCrossDays}">
											<f:convertDateTime pattern=", EEE" timeZone="#{UserTimeZone.userTimeZone}"/>
								</h:outputText>
								<h:outputText value="#{msgs.timeperiod_divider}" escape="false"/>
								<h:outputText value="#{wrapper.meeting.endTime}">
									<f:convertDateTime pattern="h:mm a" timeZone="#{UserTimeZone.userTimeZone}"/>
								</h:outputText>
								<h:outputText value="#{wrapper.meeting.endTime}" rendered="#{wrapper.meeting.meetingCrossDays}">
											<f:convertDateTime pattern=", EEE" timeZone="#{UserTimeZone.userTimeZone}"/>
								</h:outputText>	
							</h:panelGroup>	
						</t:column>
						<t:column rendered="#{SignupMeetingsBean.showMyAppointmentTime}">
							<f:facet name="header">
								<h:outputText value="#{msgs.tab_event_your_appointment_time}" escape="false"/>
							</f:facet>
							<h:panelGroup style="white-space: nowrap;">
								<h:outputText value="#{wrapper.startTime}">
									<f:convertDateTime pattern="h:mm a" timeZone="#{UserTimeZone.userTimeZone}"/>
								</h:outputText>
								<h:outputText value="#{wrapper.startTime}" rendered="#{wrapper.myAppointmentCrossDays}">
											<f:convertDateTime pattern=", EEE" timeZone="#{UserTimeZone.userTimeZone}"/>
								</h:outputText>
								<h:outputText value="#{msgs.timeperiod_divider}" escape="false"/>
								<h:outputText value="#{wrapper.endTime}">
									<f:convertDateTime pattern="h:mm a" timeZone="#{UserTimeZone.userTimeZone}"/>
								</h:outputText>
								<h:outputText value="#{wrapper.endTime}" rendered="#{wrapper.myAppointmentCrossDays}">
											<f:convertDateTime pattern=", EEE" timeZone="#{UserTimeZone.userTimeZone}"/>
								</h:outputText>	
							</h:panelGroup>	
						</t:column>
						<%-- end of column switch --%>
						
						<t:column>
							<f:facet name="header">
								<t:commandSortHeader columnName="#{SignupMeetingsBean.signupSorter.statusColumn}" immediate="true" arrow="true">
									<h:outputText value="#{msgs.tab_event_availability}" escape="false"/>
								</t:commandSortHeader>
							</f:facet>
							<h:outputText value="#{wrapper.availableStatus}" style="#{wrapper.statusStyle}" escape="false"/>
							<h:panelGroup styleClass="itemAction" style="margin-left:2em" rendered="#{SignupMeetingsBean.allowedToUpdate && SignupMeetingsBean.attendanceOn}">
							  	<h:commandLink id="attendanceView" action="#{SignupMeetingsBean.processSignupAttendance}" value="#{msgs.event_attendance}"  rendered="#{wrapper.meeting.allowAttendance}"/>  <%--rendered="if ((in progress OR completed) AND attendance is a selected option for meeting) --%>
							</h:panelGroup>
						</t:column>	
						
						<t:column rendered="#{SignupMeetingsBean.allowedToDelete}">
							<f:facet name="header">
								<h:outputText value="#{msgs.tab_event_remove}" escape="false"/>
							</f:facet>
							<h:selectBooleanCheckbox value="#{wrapper.selected}" rendered="#{wrapper.meeting.permission.delete}" onclick="determineDeleteMessage(this, #{wrapper.recurEventsSize >1});"/>							
						</t:column>				
						
					</t:dataTable></div>
					
					<h:panelGrid columns="1">
						<h:outputText value="&nbsp;" escape="false"/>
						<h:panelGroup styleClass="act">
							<h:commandButton id="removeMeetings" action="#{SignupMeetingsBean.removeMeetings}" value="#{msgs.event_removeButton}" onclick='return confirmDelete(this);' rendered="#{SignupMeetingsBean.allowedToDelete}"/>
							<h:outputText styleClass="messageProgress" style="display:none" value="#{msgs.publish_processing_submit_message}" />
						</h:panelGroup>
					</h:panelGrid>
				</h:panelGroup>
			 </h:form>
  		</sakai:view_content>	
	</sakai:view_container>
	

</f:view> 
