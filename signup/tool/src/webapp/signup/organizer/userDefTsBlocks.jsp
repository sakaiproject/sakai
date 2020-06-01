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
		<h:outputText value="#{Portal.latestJQuery}" escape="false"/>
		<script src="/library/js/lang-datepicker/lang-datepicker.js"></script>
		<script src="/sakai-signup-tool/js/signupScript.js"></script>

		<script>
			var prefix="meeting_userDefinedTS_";

			function initLocalDatePicker(pos) {
					localDatePicker({
						input: '#meeting\\:userDefinedTS\\:'+pos+'\\:startTime',
						useTime: 1,
						parseFormat: 'YYYY-MM-DD HH:mm:ss',
						allowEmptyDate: false,
						val: $('#meeting\\:userDefinedTS\\:'+pos+'\\:hiddenStartTime').val(),
						ashidden: {
								iso8601: pos + 'startTimeISO8601',
								month: prefix + pos + "_startTime_month",
								day: prefix + pos + "_startTime_day",
								year: prefix + pos + "_startTime_year",
								hour: prefix + pos + "_startTime_hours",
								minute: prefix + pos + "_startTime_minutes",
								ampm: prefix + pos + "_startTime_ampm"}
					});

					localDatePicker({
						input: '#meeting\\:userDefinedTS\\:'+pos+'\\:endTime',
						useTime: 1,
						parseFormat: 'YYYY-MM-DD HH:mm:ss',
						allowEmptyDate: false,
						val: $('#meeting\\:userDefinedTS\\:'+pos+'\\:hiddenEndTime').val(),
						ashidden: {
								iso8601: pos + 'endTimeISO8601',
								month: prefix + pos + "_endTime_month",
								day: prefix + pos + "_endTime_day",
								year: prefix + pos + "_endTime_year",
								hour: prefix + pos + "_endTime_hours",
								minute: prefix + pos + "_endTime_minutes",
								ampm: prefix + pos + "_endTime_ampm"}
					});
			}

			function setCustomEndtimeMonthDateYear(pos) {
				var yearTag = document.getElementById(prefix + pos + "_startTime_year");
				if(!yearTag)
					return;
				
				var year = yearTag.value;
				var month = document.getElementById(prefix + pos + "_startTime_month").value;
				var day = document.getElementById(prefix+ pos + "_startTime_day").value;
				var endyear = document.getElementById(prefix + pos + "_endTime_year").value;
				var endmonth = document.getElementById(prefix + pos + "_endTime_month").value;
				var endday = document.getElementById(prefix + pos + "_endTime_day").value;
						
				if (endyear > year 
						||(endyear == year) && ( endmonth > month) 
						||(endyear == year) && ( endmonth == month)&&( endday >= day) )
						return;//don't modify
					
				document.getElementById(prefix + pos + "_endTime_year").value=year;	
				document.getElementById(prefix + pos + "_endTime_month").value=month;
				document.getElementById(prefix + pos + "_endTime_day").value=day;
			}
		
			var wait=false;
			function delayedRecalcDate(){
				if (!wait){
						wait = true;
						jQuery('.timeSlot').each( function(index, data) {
							var inputId = this.id;
							if (inputId !== '' && inputId.endsWith('startTime')) {
								// Get the input position
								var inputPosition = inputId.replace('meeting:userDefinedTS:', '').replace(':startTime', '');
								setCustomEndtimeMonthDateYear(inputPosition);
							}
							setTimeout("wait=false;", 1500);//1.5 sec
						});
					}
			}

			jQuery(document).ready(function(){
				jQuery('.timeSlot').each( function(index, data) {
					var inputId = this.id;
					if (inputId !== '' && inputId.endsWith('startTime')) {
						// Get the input position
						var inputPosition = inputId.replace('meeting:userDefinedTS:', '').replace(':startTime', '');
						initLocalDatePicker(inputPosition);
					}
				});
				var MIN_ATTENDEES = 1;
				var MAX_ATTENDEES = 500;

				/**
				* check input is only numeric
				*/
				jQuery(".numericOnly").keydown(function(event) {
			        // Allow only backspace, delete and tab 
			        if ( event.keyCode == 46 || event.keyCode == 8 || event.keyCode == 9) {
			            // let it happen, don't do anything
			        }
			        else {
			        	// Ensure that it is a number (keyboard or keypad) and stop the keypress
			           	if (!(event.keyCode >= 48 && event.keyCode <= 57) && 
			            	!(event.keyCode >= 96 && event.keyCode <= 105)) 
			            {
			            	event.preventDefault();
			            }
			        }
			    });
				
				/*
				* check the range of a field after it has been input, and set it to default if out of range
				* Don't do it if it's blank though as that is handled separately.
				*/
				jQuery(".ranged").keyup(function(event) {
					
					var n = jQuery(this).val();
					
					if(n.length>0 && (n < MIN_ATTENDEES || n > MAX_ATTENDEES)) {
						alert("The number of attendees must be between " + MIN_ATTENDEES + " and " + MAX_ATTENDEES + ".");
						jQuery(this).val(MIN_ATTENDEES);
					}
					
				});
				
				/*
				* check if a form field is blank. if it is, set to default
				*/
				
				jQuery(".notblank").blur(function(event) {
					
					var n = jQuery(this).val();
					
					if(n == '') {
						jQuery(this).val(MIN_ATTENDEES);
					}
					
				});

                var menuLink = $('#signupAddMeetingMenuLink');
                menuLink.addClass('current');
                menuLink.html(menuLink.find('a').text());

        	});
    	</script>
				
		<sakai:view_content>
			<script src="/library/js/spinner.js"></script>
			<h:form id="meeting">
				<%@ include file="/signup/menu/signupMenu.jsp" %>
				<h:outputText value="#{msgs.event_error_alerts} #{messageUIBean.errorMessage}" styleClass="alertMessage" escape="false" rendered="#{messageUIBean.error}"/>
				<div class="page-header">
					<sakai:view_title value="#{msgs.event_view_userDefined_Timeslot_page_title}"/>
				</div>

					<h:outputText value="#{msgs.warn_reschedule_event}" styleClass="alertMessage" style="width:85%" escape="false" rendered="#{UserDefineTimeslotBean.someoneSignedUp}"/>
					
						<div class="table-responsive">
					    <t:dataTable id="userDefinedTS" value="#{UserDefineTimeslotBean.timeSlotWrpList}" 
					    	var="tsWrapper"
					    	binding="#{UserDefineTimeslotBean.tsTable}"
					    	styleClass="userDefineTsTable table table-striped table-bordered table-hover" >
								<t:column rendered="#{!tsWrapper.deleted}">
									<f:facet name="header" >								
											<h:outputText value="&nbsp;" escape="false"/>
									</f:facet>
										<h:commandLink action="#{UserDefineTimeslotBean.deleteTSblock}" rendered="#{tsWrapper.newlyAddedTS || UserDefineTimeslotBean.placeOrderBean == UserDefineTimeslotBean.copyBeanOrderName}" actionListener="#{UserDefineTimeslotBean.validateTimeslots}">
											<h:graphicImage value="/images/new.png" alt="New time slot" title="#{msgs.title_tip_delete_this_ts}"  styleClass="openCloseImageIcon" rendered="#{tsWrapper.newTimeslotBlock && UserDefineTimeslotBean.placeOrderBean != UserDefineTimeslotBean.newMeetingBeanOrderName}"/>
							        		<h:graphicImage value="/images/ts_delete.png" alt="delete slot" title="#{msgs.title_tip_delete_this_ts}" style="border:none;cursor:pointer;" styleClass="openCloseImageIcon"/>
							        	</h:commandLink>
							        	<h:commandLink action="#{UserDefineTimeslotBean.deleteTSblock}" rendered="#{!tsWrapper.newlyAddedTS && UserDefineTimeslotBean.placeOrderBean != UserDefineTimeslotBean.copyBeanOrderName }" actionListener="#{UserDefineTimeslotBean.validateTimeslots}" onclick="return confirm('#{msgs.confirm_cancel}');">
							        		<h:graphicImage value="/images/ts_delete.png" alt="delete slot" title="#{msgs.title_tip_delete_this_ts}" style="border:none;cursor:pointer;" styleClass="openCloseImageIcon" />
							        	</h:commandLink>
								</t:column>
						    	<t:column rendered="#{!tsWrapper.deleted}">
						    		<f:facet name="header" >								
										<h:outputText value="#{msgs.tab_start_time}" escape="false"/>
									</f:facet>
						    		<h:panelGrid columns="1">
							    		<h:panelGroup styleClass="titleText">
											<h:inputText styleClass="timeSlot" id="startTime" value="#{tsWrapper.timeSlot.startTimeString}"/>
											<h:inputHidden id="hiddenStartTime" value="#{tsWrapper.timeSlot.startTime}"><f:convertDateTime pattern="yyyy-MM-dd HH:mm:ss" timeZone="#{UserTimeZone.userTimeZone}" /></h:inputHidden>
										</h:panelGroup>
										<h:message for="startTime" errorClass="alertMessageInline"/>
									</h:panelGrid>
						    	</t:column>
						    	<t:column rendered="#{!tsWrapper.deleted}">
						    		<f:facet name="header" >								
										<h:outputText value="#{msgs.tab_end_time}" escape="false"/>
									</f:facet>
									<h:panelGrid columns="1">
										<h:panelGroup styleClass="titleText">
											<h:inputText id="endTime" value="#{tsWrapper.timeSlot.endTimeString}"/>
											<h:inputHidden id="hiddenEndTime" value="#{tsWrapper.timeSlot.endTime}"><f:convertDateTime pattern="yyyy-MM-dd HH:mm:ss" timeZone="#{UserTimeZone.userTimeZone}" /></h:inputHidden>
										</h:panelGroup>
										<h:message for="endTime" errorClass="alertMessageInline"/>
									</h:panelGrid>
						    	</t:column>
						    	<t:column rendered="#{!tsWrapper.deleted}">
									<f:facet name="header" >
											<h:outputText value="#{msgs.tab_max_participants}" escape="false"/>
									</f:facet>
										<h:panelGroup styleClass="titleText" >
							        		<h:inputText id="numOfAtt" value="#{tsWrapper.timeSlot.maxNoOfAttendees}" styleClass="editText numericOnly ranged notblank" size="2" />
										</h:panelGroup>
							</t:column>
					    </t:dataTable></div>
					    
					    <h:outputText id="addMoreTS_1" value ="&nbsp;" escape="false" styleClass="titleText" />
					    <h:panelGrid columns="1" id="addMoreTS_2">
					    	<h:commandLink id="cmdlnk90" onclick="SPNR.disableControlsAndSpin(this, null);" action="#{UserDefineTimeslotBean.addOneTSBlock}" styleClass="activeTag" actionListener="#{UserDefineTimeslotBean.validateTimeslots}">
					    		<h:graphicImage value="/images/plus.gif" alt="close" style="border:none;cursor:pointer;" styleClass="openCloseImageIcon" />
					    	 	<h:outputLabel value="#{msgs.add_more_ts}"  style="font-weight:bold" styleClass="activeTag"/>
					    	 </h:commandLink>					    	  
					    </h:panelGrid>					    

						<h:panelGroup  styleClass="longtext" >
								<h:selectBooleanCheckbox value="#{UserDefineTimeslotBean.putInMultipleCalendarBlocks}"/>
								<h:outputText value="#{msgs.put_In_Multiple_Calendar_Blocks_at_ScheduleTool}" escape="false"/>
						</h:panelGroup>					


				<sakai:button_bar>
					<h:commandButton id="doSave"  action="#{UserDefineTimeslotBean.doSave}" actionListener="#{UserDefineTimeslotBean.validateTimeslots}"   value="#{msgs.continue_button}"/>			
					<h:commandButton id="cancel" action="#{UserDefineTimeslotBean.doCancel}" value="#{msgs.cancel_button}"/>
                </sakai:button_bar>

			 </h:form>
  		</sakai:view_content>	
	</sakai:view_container>
		
</f:view> 
