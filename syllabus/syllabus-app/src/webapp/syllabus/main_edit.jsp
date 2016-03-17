<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/syllabus" prefix="syllabus" %>
<% response.setContentType("text/html; charset=UTF-8"); %>
<f:view>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.tool.syllabus.bundle.Messages"/>
</jsp:useBean>
	<sakai:view_container title="#{msgs.title_list}">
	<sakai:view_content>

<script>includeLatestJQuery('main_edit.jsp');</script>
<link rel="stylesheet" href="/library/webjars/jquery-ui/1.11.3/jquery-ui.min.css" type="text/css" />
<script type="text/javascript" src="js/jquery-ui-timepicker-addon.js"></script>
<script type="text/javascript" src="js/syllabus.js"></script>
<link type="text/css" href="syllabus/css/syllabus.css" rel="stylesheet" media="screen" />
<script type="text/javascript">
  var startDateValues = new Array();
  var dateFormat = '<h:outputText value="#{msgs.jqueryDatePickerDateFormat}"/>';
  var timeFormat = '<h:outputText value="#{msgs.jqueryDatePickerTimeFormat}"/>';
  var dataChanged = false;
  $(function() {
    $('.dateInput').datetimepicker({
    	hour: 8,
		timeFormat: timeFormat,
		currentText: "<h:outputText value="#{msgs.now}"/>",
		closeText: "<h:outputText value="#{msgs.done}"/>",
		amNames: ['<h:outputText value="#{msgs.am}"/>', '<h:outputText value="#{msgs.am2}"/>'],
		pmNames: ['<h:outputText value="#{msgs.pm}"/>', '<h:outputText value="#{msgs.pm2}"/>'],
		timeText: "<h:outputText value="#{msgs.time}"/>",
		hourText: "<h:outputText value="#{msgs.hour}"/>",
		minuteText: "<h:outputText value="#{msgs.minute}"/>",
		monthNames: ["<h:outputText value="#{msgs.jan}"/>",
					  "<h:outputText value="#{msgs.feb}"/>",
					  "<h:outputText value="#{msgs.mar}"/>",
					  "<h:outputText value="#{msgs.apr}"/>",
					  "<h:outputText value="#{msgs.may}"/>",
					  "<h:outputText value="#{msgs.jun}"/>",
					  "<h:outputText value="#{msgs.jul}"/>",
					  "<h:outputText value="#{msgs.aug}"/>",
					  "<h:outputText value="#{msgs.sep}"/>",
					  "<h:outputText value="#{msgs.oct}"/>",
					  "<h:outputText value="#{msgs.nov}"/>",
					  "<h:outputText value="#{msgs.dec}"/>"],
		dayNames: ["<h:outputText value="#{msgs.sunday}"/>",
							"<h:outputText value="#{msgs.monday}"/>",
							"<h:outputText value="#{msgs.tuesday}"/>",
							"<h:outputText value="#{msgs.wednesday}"/>",
							"<h:outputText value="#{msgs.thursday}"/>",
							"<h:outputText value="#{msgs.friday}"/>",
							"<h:outputText value="#{msgs.saturday}"/>"],
		dayNamesMin: ["<h:outputText value="#{msgs.sun}"/>",
							"<h:outputText value="#{msgs.mon}"/>",
							"<h:outputText value="#{msgs.tue}"/>",
							"<h:outputText value="#{msgs.wed}"/>",
							"<h:outputText value="#{msgs.thu}"/>",
							"<h:outputText value="#{msgs.fri}"/>",
							"<h:outputText value="#{msgs.sat}"/>"],
		beforeShow: function (textbox, instance) {
			            instance.dpDiv.css({
			                    marginLeft: textbox.offsetWidth + 'px'
			          });
		
	}
	});
  
  
  	
  	//Setup the current values of the start dates (to compare and adjust the end dates when changed)
  	$(".dateInputStart").each(function(){
		startDateValues[$(this).attr('id')] = $(this).val();
  	});
  	//setup onchange event for startDate changes
  	$(".dateInputStart").change(function(){
  		var startDate = new Date($(this).val());
  		var prevStartDate = new Date(startDateValues[$(this).attr('id')]);
  		var endDate = new Date($(this).closest("tr").find(".dateInputEnd").val());
  		if(isNaN(startDate.getTime()) == false && isNaN(prevStartDate.getTime()) == false && isNaN(endDate.getTime()) == false){
  			//we only want to update if all three of these dates have been set
  			var timeDiff = startDate.getTime() - prevStartDate.getTime();
  			var newEndDate = new Date(endDate.getTime() + timeDiff);
  			var newEndTime = {hour: newEndDate.getHours(), minute: newEndDate.getMinutes()};
  			$(this).closest("tr").find(".dateInputEnd").val($.datepicker.formatDate(dateFormat, newEndDate) + " " + $.datepicker.formatTime(timeFormat, newEndTime));
  		}
  		startDateValues[$(this).attr('id')] = $(this).val();
  	});
  	
  	//setup data change listener
  	$('input').change(function() {
    	dataChanged = true;
	});
  	
  	//disable calendar options that are in draft:
  	disableCalendarOptions();
  	//add listeners to the calendar dates for the calendar checkbox:
  	$(".dateInputStart").change(function(){
  		checkCalendarDates(this);
  	});
  	$(".dateInputEnd").change(function(){
  		checkCalendarDates(this);
  	});
  	
  });
  
	function toggleCalendarCheckbox(postCheckbox){
		$(postCheckbox).parent().parent().find(".calendarBox").each(function(){
			if(postCheckbox.checked){
				$(this).removeAttr("disabled");
			}else{
				$(this).attr("disabled", "disabled");
				this.checked = false;
			}
		});
	}
	
	function checkStartEndDates(calendarCheckbox){
		if(calendarCheckbox.checked){
			//check that this rows has either start or end dates set
			var startTime = $(calendarCheckbox).parent().parent().find(".dateInputStart").val();
			var endTime = $(calendarCheckbox).parent().parent().find(".dateInputEnd").val();
			if((startTime == null || "" == $.trim(startTime))
					&& (endTime == null || "" == $.trim(endTime))){
				showMessage("<h:outputText value="#{msgs.calendarDatesNeeded}"/>", false);
				calendarCheckbox.checked = false;
			}
		}
	}
	
	var deleteClick;
            
	function assignWarningClick(link) {
  		if (link.onclick == confirmPost) {
    		return;
  		}
                
  		deleteClick = link.onclick;
  		link.onclick = confirmPost;
	}

	function confirmPost(){
		if(dataChanged){
			var agree=confirm('<h:outputText value="#{msgs.main_edit_confirmDataChanged}"/>');
			if (agree)
				return deleteClick();
			else
				return false ;
		}else{
			return deleteClick();
		}
	}
	
	function toggleAllCalendarOptions(toggleCheckbox){
		$('.calendarBox').each(function(){
			if(toggleCheckbox.checked){
				//make sure that the post option is checked otherwise don't check it as well as the start or end date isn't null
				if($(this).parent().parent().find(".postBox:checked").length == 1){
					var startTime = $(this).parent().parent().find(".dateInputStart").val();
					var endTime = $(this).parent().parent().find(".dateInputEnd").val();
					if((startTime != null && "" != $.trim(startTime)) || (endTime != null && "" != $.trim(endTime))){
						//at least one date is set
						this.checked = true;
					}else{
						showMessage("<h:outputText value="#{msgs.calendarDatesNeededToggle}"/>", false);
					}
				}
			}else{
				this.checked = false;
			}
		});
	}

	function toggleAllPostOptions(toggleCheckbox){
		$('.postBox').each(function(){
			if(toggleCheckbox.checked){
				this.checked = true;
			}else{
				this.checked = false;
				//make sure calendar option is unchecked
				$(this).parent().parent().find(".calendarBox").removeAttr("checked");
			}
			toggleCalendarCheckbox(this);
		});
	}
	
	function disableCalendarOptions(){
		$('.calendarBox').each(function(){
			if($(this).parent().parent().find(".postBox:checked").length == 0){
				$(this).attr("disabled", "disabled");
			}
		});
	}
	
	//used for when a date is changed, it will check that the calendar checkbox is not checked if
	//both dates are empty
	function checkCalendarDates(dateInput){
		var startTime = $(dateInput).parent().parent().find(".dateInputStart").val();
		var endTime = $(dateInput).parent().parent().find(".dateInputEnd").val();
		if((startTime == null || "" == $.trim(startTime)) && (endTime == null || "" == $.trim(endTime))){
			//both start and end dates are null, so make sure the calendar checkbox is unchecked:
			$(dateInput).parent().parent().find(".calendarBox").removeAttr("checked");
		}
	}
 </script>
<div>
	<span id="successInfo" class="success popupMessage" style="display:none; float: left;"></span>
	<span id="warningInfo" class="alertMessage popupMessage" style="display:none; float: left;"></span>
</div>
        <script type="text/javascript">
        	// if redirected, just open in another window else
        	// open with size approx what actual print out will look like
        	function printFriendly(url) {
        		if (url.indexOf("printFriendly") == -1) {
        			window.open(url,"mywindow");
        		}
        		else {
        			window.open(url,"mywindow","width=960,height=1100,scrollbars=yes");
        		}
        	}
        </script>

		<h:form>
		<h:panelGroup>
		  <f:verbatim><ul class="navIntraTool actionToolbar" role="menu"></f:verbatim> 
		  <%-- (gsilver) cannot pass a needed title attribute to these next items --%>
		  <h:panelGroup rendered="#{SyllabusTool.addItem == 'true'}">
			   <f:verbatim><li role="menuitem" class="firstToolBarItem"> <span></f:verbatim>
				  	<h:commandLink action="#{SyllabusTool.processListNew}" 
				  		styleClass="actionLink" 
				  		onmousedown="assignWarningClick(this);"
					    rendered="#{SyllabusTool.addIteme == 'true'}">
					    	<h:outputText value="#{msgs.bar_new}"/>
					</h:commandLink>
				<f:verbatim></span></li></f:verbatim>
			</h:panelGroup>
			<h:panelGroup rendered="#{SyllabusTool.bulkAddItem == 'true'}">
				<f:verbatim><li role="menuitem" ><span></f:verbatim>
				   	<h:commandLink
							action="#{SyllabusTool.processListNewBulkMainEdit}"
							onmousedown="assignWarningClick(this);"
				   			rendered="#{SyllabusTool.bulkAddItem == 'true'}">
								<h:outputText value="#{msgs.bar_new_bulk}"/>
				   	</h:commandLink>
			   	<f:verbatim></span></li></f:verbatim>
		   	</h:panelGroup>
		   	<h:panelGroup rendered="#{SyllabusTool.addOrEdit == 'true'}">
			   	<f:verbatim><li role="menuitem" ><span></f:verbatim>
				   	<h:commandLink
							action="#{SyllabusTool.processStudentView}"
							onmousedown="assignWarningClick(this);"
				   			rendered="#{SyllabusTool.addOrEdit == 'true'}">
					    		<h:outputText value="#{msgs.bar_student_view}"/>
					</h:commandLink>
				<f:verbatim></span></li></f:verbatim>
			</h:panelGroup>
		   	<f:verbatim></ul></f:verbatim>		
   	      </h:panelGroup>
   	      
   	      <h:messages globalOnly="true" styleClass="alertMessage" rendered="#{!empty facesContext.maximumSeverity}" />
	      <syllabus:syllabus_if test="#{SyllabusTool.syllabusItem.redirectURL}">
		     <sakai:tool_bar_message value="#{msgs.mainEditNotice}" />
		     <h:dataTable value="#{SyllabusTool.entries}" var="eachEntry" summary="#{msgs.mainEditListSummary}" styleClass="listHier lines nolines"
		     				columnClasses="item,move,move,status,status" >
						<h:column rendered="#{! SyllabusTool.displayNoEntryMsg}">
							<f:facet name="header">
								<h:outputText value="#{msgs.mainEditHeaderItem}" />
							</f:facet>
							<h:inputText value="#{eachEntry.entry.title}"/>
							<f:verbatim><br/></f:verbatim>
							<h:commandLink action="#{eachEntry.processListRead}" value="#{msgs.edit_details}" title="#{msgs.goToItem} #{eachEntry.entry.title}" onmousedown="assignWarningClick(this);"/>
							
							
						</h:column>
						<h:column rendered="#{! SyllabusTool.displayNoEntryMsg}">
							<f:facet name="header">
								<h:outputText value="" />
							</f:facet>
							<h:commandLink action="#{eachEntry.processUpMove}" style="text-decoration:none" title="#{msgs.mainEditLinkUpTitle}" rendered="#{SyllabusTool.editAble == 'true'}">
								<h:graphicImage url="/syllabus/moveup.gif" alt="#{msgs.mainEditLinkUpTitle}" />
								<h:outputText value="(#{eachEntry.entry.title})" styleClass="skip"/>
							</h:commandLink>
							<h:outputText value=" "/>
							<h:commandLink action="#{eachEntry.processDownMove}"  style="text-decoration:none" title="#{msgs.mainEditLinkDownTitle}" styleClass="imageLink" rendered="#{SyllabusTool.editAble == 'true'}">
								<h:graphicImage url="/syllabus/movedown.gif" alt="#{msgs.mainEditLinkDownTitle}" />
								<h:outputText value="(#{eachEntry.entry.title})" styleClass="skip"/>
							</h:commandLink>
						</h:column>
						<h:column rendered="#{! SyllabusTool.displayNoEntryMsg}">
							<f:facet name="header">
								<h:outputText value="#{msgs.mainEditHeaderStartTime}"/>
							</f:facet>
							<h:inputText styleClass="dateInput dateInputStart" value="#{eachEntry.entry.startDate}" id="dataStartDate">
								<f:convertDateTime pattern="#{msgs.mainEditHeaderTimeFormat}"/>
							</h:inputText>
							<f:verbatim><img src="/library/image/silk/calendar_view_month.png" onclick="$(this).prev().focus();"/></f:verbatim>
						</h:column>	
						<h:column rendered="#{! SyllabusTool.displayNoEntryMsg}">
							<f:facet name="header">
								<h:outputText value="#{msgs.mainEditHeaderEndTime}"/>
							</f:facet>
							<h:inputText styleClass="dateInput dateInputEnd" value="#{eachEntry.entry.endDate}" id="dataEndDate">
								<f:convertDateTime pattern="#{msgs.mainEditHeaderTimeFormat}"/>
							</h:inputText>
							<f:verbatim><img src="/library/image/silk/calendar_view_month.png" onclick="$(this).prev().focus();"/></f:verbatim>
						</h:column>
						<h:column rendered="#{! SyllabusTool.displayNoEntryMsg && SyllabusTool.calendarExistsForSite}">
							<f:facet name="header">
								<h:panelGroup>
									<h:outputText value="#{msgs.mainEditHeaderInCalendar}"/>
									<f:verbatim>
										<br/>
										<input type="checkbox" onchange="toggleAllCalendarOptions(this);"/>
									</f:verbatim>
								</h:panelGroup>
							</f:facet>
							<h:selectBooleanCheckbox styleClass="calendarBox" value="#{eachEntry.entry.linkCalendar}" title="#{msgs.selectThisCheckBoxCal}" onchange="checkStartEndDates(this)"/>
						</h:column>
						<h:column rendered="#{! SyllabusTool.displayNoEntryMsg}">
							<f:facet name="header">
								<h:panelGroup>
									<h:outputText value="#{msgs.mainEditHeaderStatus}"/>
									<f:verbatim>
										<br/>
										<input type="checkbox" onchange="toggleAllPostOptions(this);"/>
									</f:verbatim>
								</h:panelGroup>
							</f:facet>
							<h:selectBooleanCheckbox styleClass="postBox" value="#{eachEntry.posted}" title="#{msgs.selectThisCheckBoxPublish}" onchange="toggleCalendarCheckbox(this);" />
						</h:column>
						<h:column rendered="#{! SyllabusTool.displayNoEntryMsg}">
							<f:facet name="header">
								<h:panelGroup>
  									<h:outputText value="#{msgs.mainEditHeaderRemove}"/>
  									<f:verbatim>
										<br/>
										<input type="checkbox" onchange="$('.deleteBox').attr('checked', this.checked);"/>
									</f:verbatim>
								</h:panelGroup>
							</f:facet>
							<h:selectBooleanCheckbox styleClass="deleteBox" value="#{eachEntry.selected}" title="#{msgs.selectThisCheckBox}"/>
						</h:column>
			 </h:dataTable>
			 <f:verbatim><p class="act"></f:verbatim>	
				<h:commandButton 
				     value="#{msgs.update}" 
					 action="#{SyllabusTool.processListDelete}"
					 title="#{msgs.update}"
				     rendered="#{! SyllabusTool.displayNoEntryMsg}"
					 accesskey="s" 	/>
				<h:commandButton 
				     value="#{msgs.reset}" 
					 action="#{SyllabusTool.processMainEditCancel}"
					 title="#{msgs.reset}"
				     rendered="#{! SyllabusTool.displayNoEntryMsg}"
					 accesskey="r" 	/>
			<f:verbatim></p></f:verbatim>		  
		  </syllabus:syllabus_if>

          <syllabus:syllabus_ifnot test="#{SyllabusTool.syllabusItem.redirectURL}">
            <sakai:tool_bar_message value="#{msgs.redirect_sylla}" />

            <syllabus:syllabus_if test="#{SyllabusTool.openInNewWindowAsString}">
                <syllabus:syllabus_iframe redirectUrl="#{SyllabusTool.syllabusItem.redirectURL}" width="100%"
                                          height="500"/>
            </syllabus:syllabus_if>
            <syllabus:syllabus_ifnot test="#{SyllabusTool.openInNewWindowAsString}">
                <h:outputText escape="false"
                              value="<script>javascript:printFriendly('#{SyllabusTool.syllabusItem.redirectURL}');</script>"/>
            </syllabus:syllabus_ifnot>
          </syllabus:syllabus_ifnot>

        </h:form>
	</sakai:view_content>
	</sakai:view_container>
</f:view>
