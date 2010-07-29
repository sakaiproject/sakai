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
			<script TYPE="text/javascript" LANGUAGE="JavaScript" src="/sakai-signup-tool/js/signupScript.js"></script>
		
		<sakai:view_content>
			<h:outputText value="#{msgs.event_error_alerts} #{errorMessageUIBean.errorMessage}" styleClass="alertMessage" escape="false" rendered="#{errorMessageUIBean.error}"/>      			
				
			<h:form id="meeting">
			 	<sakai:view_title value="#{msgs.event_view_userDefined_Timeslot_page_title}"/>
				
							
				<h:panelGrid columns="2" columnClasses="titleColumn,valueColumn" onmouseover="delayedRecalcDate();">
				
					<h:outputText value="&nbsp;" escape="false"/>
					<h:outputText value="&nbsp;" escape="false"/>
					
					<h:outputText value="&nbsp;" escape="false" rendered="#{UserDefineTimeslotBean.someoneSignedUp}"/>
					<h:outputText value="#{msgs.warn_reschedule_event}" styleClass="alertMessage" style="width:85%" escape="false" rendered="#{UserDefineTimeslotBean.someoneSignedUp}"/>
					
					<h:panelGroup styleClass="titleText">		           			
					           <h:outputText value ="&nbsp;" escape="false"/>
					    </h:panelGroup>
					    <t:dataTable id="userDefinedTS" value="#{UserDefineTimeslotBean.timeSlotWrpList}" 
					    	var="tsWrapper"
					    	binding="#{UserDefineTimeslotBean.tsTable}"
					    	styleClass="userDefineTsTable" 
							rowClasses="oddRow,evenRow"
							rowStyle="#{tsWrapper.errorStyle}"
							columnClasses="delTSCol, startTSCol, endTSCol, numAttnTSCol" >
								<t:column rendered="#{!tsWrapper.deleted}">
									<f:facet name="header" >								
											<h:outputText value="&nbsp;" escape="false"/>
									</f:facet>
										<h:commandLink action="#{UserDefineTimeslotBean.deleteTSblock}" rendered="#{tsWrapper.newlyAddedTS || UserDefineTimeslotBean.placeOrderBean == UserDefineTimeslotBean.copyBeanOrderName}">
											<h:graphicImage value="/images/new.png" alt="New time slot" title="#{msgs.title_tip_delete_this_ts}"  styleClass="openCloseImageIcon" rendered="#{tsWrapper.newTimeslotBlock}"/>
							        		<h:graphicImage value="/images/ts_delete.png" alt="delete slot" title="#{msgs.title_tip_delete_this_ts}" style="border:none;cursor:pointer;" styleClass="openCloseImageIcon"/>
							        	</h:commandLink>
							        	<h:commandLink action="#{UserDefineTimeslotBean.deleteTSblock}" rendered="#{!tsWrapper.newlyAddedTS && UserDefineTimeslotBean.placeOrderBean != UserDefineTimeslotBean.copyBeanOrderName }" onmousedown="confirmTsCancel(this,'#{msgs.confirm_cancel}');">
							        		<h:graphicImage value="/images/ts_delete.png" alt="delete slot" title="#{msgs.title_tip_delete_this_ts}" style="border:none;cursor:pointer;" styleClass="openCloseImageIcon" />
							        	</h:commandLink>
								</t:column>
						    	<t:column rendered="#{!tsWrapper.deleted}">
						    		<f:facet name="header" >								
										<h:outputText value="#{msgs.tab_start_time}" escape="false"/>
									</f:facet>
						    		<h:panelGrid columns="1">
							    		<h:panelGroup styleClass="titleText">
					        				<t:inputDate id="startTime" type="both"  ampm="true" value="#{tsWrapper.timeSlot.startTime}" 
					        							 style="color:black;" popupCalendar="true" />
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
					        				<t:inputDate id="endTime" type="both"  ampm="true" value="#{tsWrapper.timeSlot.endTime}"
					        							 style="color:black;" popupCalendar="true" />											
										</h:panelGroup>
										<h:message for="endTime" errorClass="alertMessageInline"/>
									</h:panelGrid>
						    	</t:column>
						    	<t:column rendered="#{!tsWrapper.deleted}">
									<f:facet name="header" >
											<h:outputText value="#{msgs.tab_max_participants}" escape="false"/>
									</f:facet>
										<h:panelGroup styleClass="titleText" >
							        			<h:inputText id="numOfAtt" value="#{tsWrapper.timeSlot.maxNoOfAttendees}" styleClass="editText" size="2" style="margin-left:12px" 
							        				onkeyup="validateMaxParticipants('#{tsWrapper.positionInTSlist}');return false;" />
										</h:panelGroup>
							</t:column>
					    </t:dataTable>
					    
					    <h:outputText id="addMoreTS_1" value ="&nbsp;" escape="false" styleClass="titleText" />
					    <h:panelGrid columns="2" id="addMoreTS_2">
					    	<h:commandLink action="#{UserDefineTimeslotBean.addOneTSBlock}" styleClass="activeTag" actionListener="#{UserDefineTimeslotBean.validateTimeslots}">
					    		<h:graphicImage value="/images/plus.gif" alt="close" style="border:none;cursor:pointer;" styleClass="openCloseImageIcon" />
					    	 	<h:outputLabel value="#{msgs.add_more_ts}"  style="font-weight:bold" styleClass="activeTag"/>
					    	 </h:commandLink>
					    	  
					    	 <h:selectOneRadio layout="lineDirection" value="#{UserDefineTimeslotBean.bottom}" styleClass="meetingRadioBtn">
					    	 	<f:selectItem itemValue="#{false}" itemLabel="#{msgs.add_to_top}"/>                    
								<f:selectItem itemValue="#{true}" itemLabel="#{msgs.add_to_bottom}"/>   
					    	 </h:selectOneRadio>
					    </h:panelGrid>
					    
					    <h:outputText value="&nbsp;" escape="false"/>
					    <h:outputText value="&nbsp;" escape="false"/>
					    
					    <h:outputText value="&nbsp;" escape="false"/>
						<h:panelGroup  styleClass="longtext" >
								<h:selectBooleanCheckbox value="#{UserDefineTimeslotBean.putInMultipleCalendarBlocks}"/>
								<h:outputText value="#{msgs.put_In_Multiple_Calendar_Blocks_at_ScheduleTool}" escape="false"/>
						</h:panelGroup>
						
						<h:outputText value="&nbsp;" escape="false"/>
						<h:outputText value="&nbsp;" escape="false"/>
				</h:panelGrid>
				
				<sakai:button_bar>
					<h:commandButton id="doSave"  action="#{UserDefineTimeslotBean.doSave}" actionListener="#{UserDefineTimeslotBean.validateTimeslots}"   value="#{msgs.continue_button}"/>			
					<sakai:button_bar_item id="cancel" action="#{UserDefineTimeslotBean.doCancel}" value="#{msgs.cancel_button}"/>
                </sakai:button_bar>

			 </h:form>
  		</sakai:view_content>	
	</sakai:view_container>
	<f:verbatim>
		<script>
			replaceCalendarImageIcon();
			var prefix="meeting:userDefinedTS:";
			
			function setCustomEndtimeMonthDateYear(pos){
				var yearTag = document.getElementById(prefix + pos + ":startTime.year");
				if(!yearTag)
					return;
				
				var year = yearTag.value;
				var month = document.getElementById(prefix + pos + ":startTime.month").value;
				var day = document.getElementById(prefix+ pos + ":startTime.day").value;			
				var endyear = document.getElementById(prefix + pos + ":endTime.year").value;
				var endmonth = document.getElementById(prefix + pos + ":endTime.month").value;
				var endday = document.getElementById(prefix + pos + ":endTime.day").value;
						
				if (endyear >= year && endmonth >=month && endday >= day)
					return;//don't modify
					
				document.getElementById(prefix + pos + ":endTime.year").value=year;	
				document.getElementById(prefix + pos + ":endTime.month").value=month;
				document.getElementById(prefix + pos + ":endTime.day").value=day;
			}
		
			var wait=false;
			function delayedRecalcDate(){
				if (!wait){
						wait = true;
						for(i=0; i<30; i++){//control 30 ts
						 setCustomEndtimeMonthDateYear(i);
						}
					  	setTimeout("wait=false;", 1500);//1.5 sec
					}			
			}
							
			var prev_attendeeNum=1;//default
			function validateMaxParticipants(pos){
				var maxAttnRowTag = document.getElementById(prefix + pos + ':numOfAtt');
				if(maxAttnRowTag){
					prev_attendeeNum = signup_ValidateNumber(prev_attendeeNum,maxAttnRowTag,500);
				}

			}
			//JSF issue for onclick, this goes around 
			function confirmTsCancel(link,msg){
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
	
		</script>
	</f:verbatim>
		
</f:view> 
