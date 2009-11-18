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
		<script TYPE="text/javascript" LANGUAGE="JavaScript" src="/sakai-signup-tool/js/signupScript.js"></script>
		
		<sakai:view_content>
			<h:outputText value="#{msgs.event_error_alerts} #{errorMessageUIBean.errorMessage}" styleClass="alertMessage" escape="false" rendered="#{errorMessageUIBean.error}"/> 
			<h:form id="items">
			 	<sakai:view_title value="#{msgs.signup_download}"/>

				<sakai:messages />
				
				<h:outputText value="&nbsp;" escape="false"/>
				
				<h:panelGrid columns="1">
					<h:outputText value="#{msgs.events_organizer_download_instruction}"  rendered="#{DownloadEventBean.allowedToUpdate && DownloadEventBean.meetingsAvailable}" escape="false"/>
					<h:outputText value="#{msgs.events_attendee_download_instruction}" rendered="#{!DownloadEventBean.allowedToUpdate && DownloadEventBean.meetingsAvailable}" escape="false"/>
					<h:outputText value="&nbsp;" escape="false"/>
				</h:panelGrid>
				<h:panelGrid columns="2">
					<h:panelGroup>
						<h:outputText value="#{msgs.events_dropdownbox_title}&nbsp;" escape="false"/>
						<h:selectOneMenu id="viewByRange" value="#{DownloadEventBean.viewDateRang}" valueChangeListener="#{DownloadEventBean.processSelectedRange}" onchange="if(validateIEDisabledItem(this)){submit()};">
							<f:selectItems value="#{DownloadEventBean.viewDropDownList}"/>
						</h:selectOneMenu>
					</h:panelGroup>
					<h:panelGroup>
						<h:panelGroup styleClass="expandAllRecurMeetings" rendered="#{DownloadEventBean.enableExpandOption && DownloadEventBean.meetingsAvailable}">
							<h:selectBooleanCheckbox id="expandingchkbox" value="#{DownloadEventBean.showAllRecurMeetings}" valueChangeListener="#{DownloadEventBean.processExpandAllRcurEvents}" onclick="submit();"/>
							<h:outputText value="#{msgs.expand_all_recur_events}" escape="false"/>
						</h:panelGroup>
						<h:outputText value="&nbsp;" escape="false" rendered="#{!DownloadEventBean.enableExpandOption}"/>
					</h:panelGroup>
				</h:panelGrid>
				
				<h:outputText value="&nbsp;" escape="false"/>
				
				<h:panelGrid columns="1" styleClass="downloadSelection"  rendered="#{DownloadEventBean.meetingsAvailable}">
					<h:commandButton id="downloadTop"  action="#{DownloadEventBean.startDownload}" value="#{msgs.event_download_button}"  />
				</h:panelGrid>
					

				<h:panelGrid columns="1" styleClass="noMeetingsWarn" rendered="#{!DownloadEventBean.meetingsAvailable}" >
					<h:outputText value="&nbsp;" escape="false"/>
					<h:outputText value="#{DownloadEventBean.meetingUnavailableMessages}" />
				</h:panelGrid>
					
				<h:panelGroup rendered="#{DownloadEventBean.meetingsAvailable}">								 	
				 	<t:dataTable 
				 		id="eventlist"
				 		value="#{DownloadEventBean.signupMeetings}"
				 		sortColumn="#{DownloadEventBean.signupSorter.sortColumn}"
				 		sortAscending="#{DownloadEventBean.signupSorter.sortAscending}"				 		
				 		var="wrapper" style="width:100%;" 
				 		rowId="#{wrapper.recurId}"
				 		rowStyle="#{wrapper.hideStyle}"
				 		rowClasses="oddRow,evenRow"
				 		columnClasses="titleCol, creatorCol, locationCol, dateCol, timeCol, statusCol, removeCol"
				 		styleClass="signupTable">
	
						<t:column>
							<f:facet name="header">
								<h:panelGroup>
									<h:selectBooleanCheckbox id="selectAll"  onclick="signup_SetAllCheckBoxes()" />
									<h:outputText value="#{msgs.event_tab_check_all}" escape="false"/>
								</h:panelGroup>							
							</f:facet>
							<h:selectBooleanCheckbox id="selectItem" value="#{wrapper.toDownload}" />
						</t:column>		
						
						<t:column defaultSorted="true" sortable="true">
							<f:facet name="header" >
								<t:commandSortHeader columnName="#{DownloadEventBean.signupSorter.titleColumn}" immediate="true" arrow="true">
									<h:outputText value="#{msgs.tab_event_name}" escape="false"/>
								</t:commandSortHeader>
							</f:facet>
							<h:panelGroup rendered="#{wrapper.firstOneRecurMeeting && wrapper.recurEventsSize >1}" style="margin-left:-13px; cursor:pointer;">
								<h:outputText value="<span id='imageOpen_RM_#{wrapper.recurId}' style='display:none'>"  escape="false"/>
		   	    					<h:graphicImage value="/images/minusSmall.gif"  alt="open" styleClass="openCloseImageIcon" title="#{msgs.event_tool_tips_collapse_recur_meeting}" style="border:none" onclick="showDetails('imageOpen_RM_#{wrapper.recurId}','imageClose_RM_#{wrapper.recurId}');showAllRelatedRecurMeetings('#{wrapper.recurId}','#{DownloadEventBean.iframeId}');" />
		   	    				<h:outputText value="</span>" escape="false" />
		   	    			
		   	    				<h:outputText value="<span id='imageClose_RM_#{wrapper.recurId}'>"  escape="false"/>
		   	    					<h:graphicImage title="#{msgs.event_tool_tips_expand_recur_meeting}" value="/images/plusSmall.gif" styleClass="openCloseImageIcon" alt="close" style="border:none" onclick="showDetails('imageOpen_RM_#{wrapper.recurId}','imageClose_RM_#{wrapper.recurId}');showAllRelatedRecurMeetings('#{wrapper.recurId}','#{DownloadEventBean.iframeId}');"/>
		   	    				<h:outputText value="</span>" escape="false" />
		   	    				
		   	    				<h:outputText value="&nbsp;" escape="false"/>
		   	    			</h:panelGroup>
							
							<h:outputText  value="#{wrapper.meeting.title}" />
							
						</t:column>
						
						<t:column sortable="true">
							<f:facet name="header">
								<t:commandSortHeader columnName="#{DownloadEventBean.signupSorter.createColumn}" immediate="true" arrow="true">
									<h:outputText value="#{msgs.tab_event_owner}" escape="false"/>
								</t:commandSortHeader>
							</f:facet>
							<h:outputText value="#{wrapper.creator}"/>												
						</t:column>
						
						<t:column sortable="true">
							<f:facet name="header">
								<t:commandSortHeader columnName="#{DownloadEventBean.signupSorter.locationColumn}" immediate="true" arrow="true">
									<h:outputText value="#{msgs.tab_event_location}" escape="false"/>
								</t:commandSortHeader>
							</f:facet>
							<h:outputText value="#{wrapper.meeting.location}"/>												
						</t:column>
	
						<t:column>
							<f:facet name="header">
								<t:commandSortHeader columnName="#{DownloadEventBean.signupSorter.dateColumn}" immediate="true" arrow="true">
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
						
						<%-- switch the following two according to user selection --%>									
						<t:column rendered="#{!DownloadEventBean.showMyAppointmentTime}">
							<f:facet name="header">
								<h:outputText value="#{msgs.tab_event_time}" escape="false" />
								<h:outputText value="#{msgs.tab_event_your_appointment_time}" escape="false" rendered="#{DownloadEventBean.showMyAppointmentTime}"/>
							</f:facet>
							<h:panelGroup style="white-space: nowrap;">
								<h:outputText value="#{wrapper.meeting.startTime}">
									<f:convertDateTime pattern="h:mm a" />
								</h:outputText>
								<h:outputText value="#{wrapper.meeting.startTime}" rendered="#{wrapper.meeting.meetingCrossDays}">
											<f:convertDateTime pattern=", EEE" />
								</h:outputText>
								<h:outputText value="#{msgs.timeperiod_divider}" escape="false"/>
								<h:outputText value="#{wrapper.meeting.endTime}">
									<f:convertDateTime pattern="h:mm a" />
								</h:outputText>
								<h:outputText value="#{wrapper.meeting.endTime}" rendered="#{wrapper.meeting.meetingCrossDays}">
											<f:convertDateTime pattern=", EEE" />
								</h:outputText>	
							</h:panelGroup>	
						</t:column>
						<t:column rendered="#{DownloadEventBean.showMyAppointmentTime}">
							<f:facet name="header">
								<h:outputText value="#{msgs.tab_event_your_appointment_time}" escape="false"/>
							</f:facet>
							<h:panelGroup style="white-space: nowrap;">
								<h:outputText value="#{wrapper.startTime}">
									<f:convertDateTime pattern="h:mm a" />
								</h:outputText>
								<h:outputText value="#{wrapper.startTime}" rendered="#{wrapper.myAppointmentCrossDays}">
											<f:convertDateTime pattern=", EEE" />
								</h:outputText>
								<h:outputText value="#{msgs.timeperiod_divider}" escape="false"/>
								<h:outputText value="#{wrapper.endTime}">
									<f:convertDateTime pattern="h:mm a" />
								</h:outputText>
								<h:outputText value="#{wrapper.endTime}" rendered="#{wrapper.myAppointmentCrossDays}">
											<f:convertDateTime pattern=", EEE" />
								</h:outputText>	
							</h:panelGroup>	
						</t:column>
						<%-- end of column switch --%>
						
						<t:column>
							<f:facet name="header">
								<t:commandSortHeader columnName="#{DownloadEventBean.signupSorter.statusColumn}" immediate="true" arrow="true">
									<h:outputText value="#{msgs.tab_event_availability}" escape="false"/>
								</t:commandSortHeader>
							</f:facet>
							<h:outputText value="#{wrapper.availableStatus}" style="#{wrapper.statusStyle}" escape="false"/>
						</t:column>	
						
					</t:dataTable>
					<h:panelGrid columns="1" styleClass="checkClearAction">
						<h:panelGroup>
							<h:outputLabel onclick="signup_setAllChkBoxes(true)" styleClass="activeTag">
								<h:outputText value="#{msgs.event_check_all_chkbxn}" escape="false"/>
							</h:outputLabel>
							<h:outputText value=" - " escape="false"/>
							<h:outputLabel onclick="signup_setAllChkBoxes(false)" styleClass="activeTag">
								<h:outputText value="#{msgs.event_clear_all_chkbxn}" escape="false"/>
							</h:outputLabel>
						</h:panelGroup>
						
					</h:panelGrid>
				</h:panelGroup>
					
				<h:panelGrid columns="1">
					<h:outputText value="&nbsp;" escape="false"/>
					<h:panelGroup>
						<h:commandButton id="downloadEventsBttm"  action="#{DownloadEventBean.startDownload}" value="#{msgs.event_download_button}"  rendered="#{DownloadEventBean.meetingsAvailable}"/>
						<h:outputText value="&nbsp;&nbsp;&nbsp;" escape="false"/>
						<h:commandButton id="goToMain" action="listMeetings" value="Back"  />
					</h:panelGroup>
				</h:panelGrid>
				
			</h:form>
  		</sakai:view_content>	
	</sakai:view_container>
	
	<f:verbatim>
		<script>
					
			var origClassNames=new Array();
			var lastActiveId;
			var previousBgColor; 
			var recurRowClass="recurRow";//defined in css
			var evenRowClass = "evenRow";
			var oddRowClass = "oddRow";
			//due to recuring meetings, make sure even/odd Rows display correctly
			reprocessEvenOddRowClasses();

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
				//signup_resetIFrameHeight(iFrameId);			
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
			
			function signup_SetAllCheckBoxes(){
				var inputTags = document.getElementsByTagName("input");	
				var checkBoxAllTag = document.getElementById("items:eventlist:selectAll");
				if (!inputTags || !checkBoxAllTag)
					return;
				var checkValue = checkBoxAllTag.checked;	
				signup_setAllChkBoxes(checkValue);
			}
			
			function signup_setAllChkBoxes(value){
				var inputTags = document.getElementsByTagName("input");					
				if (!inputTags)
					return;
				//ignore the first expanding events chkBox
				for(i=1; i<inputTags.length;i++){
					if(inputTags[i].type=='checkbox')
						inputTags[i].checked = value;					
				}

			}			

		</script>
	</f:verbatim>
</f:view> 
