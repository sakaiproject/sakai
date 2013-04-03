<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/syllabus" prefix="syllabus" %>
<% response.setContentType("text/html; charset=UTF-8"); %>
<script type="text/javascript" src="/library/js/jquery/jquery-ui/js/jquery.js"></script>
<script type="text/javascript" src="/library/js/jquery/jquery-ui/js/jquery-ui.js"></script>
<link type="text/css" href="/library/js/jquery/jquery-ui/css/smoothness/jquery-ui.css" rel="stylesheet" media="screen" />
<script type="text/javascript" src="js/jquery-ui-timepicker-addon.js"></script>

<f:view>
<script>
  var startDateValues = new Array();
  var dateFormat = '<h:outputText value="#{msgs.jqueryDatePickerDateFormat}"/>';
  var timeFormat = '<h:outputText value="#{msgs.jqueryDatePickerTimeFormat}"/>';

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
  });
 </script>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.tool.syllabus.bundle.Messages"/>
</jsp:useBean>

	<sakai:view_container title="#{msgs.title_list}">
	<sakai:view_content>
		<h:form>
		  <sakai:tool_bar>
		  <%-- (gsilver) cannot pass a needed title attribute to these next items --%>
			<sakai:tool_bar_item
			    action="#{SyllabusTool.processListNew}"
					value="#{msgs.bar_new}" 
			    rendered="#{SyllabusTool.editAble == 'true'}" />
 		    <sakai:tool_bar_item
					action="#{SyllabusTool.processRedirect}"
					value="#{msgs.bar_redirect}" 
			    rendered="#{SyllabusTool.editAble == 'true'}" />
 		    <sakai:tool_bar_item
					action="#{SyllabusTool.processStudentView}"
					value="#{msgs.bar_student_view}" 
		   			rendered="#{SyllabusTool.editAble == 'true'}" />
		   	<sakai:tool_bar_item
					action="#{SyllabusTool.processListNewBulk}"
					value="#{msgs.bar_new_bulk}" 
		   			rendered="#{SyllabusTool.editAble == 'true'}" />
   	      </sakai:tool_bar>
   	      <h:messages globalOnly="true" styleClass="alertMessage" rendered="#{!empty facesContext.maximumSeverity}" />
	      <syllabus:syllabus_if test="#{SyllabusTool.syllabusItem.redirectURL}">
		     <sakai:tool_bar_message value="#{msgs.mainEditNotice}" />
		     <syllabus:syllabus_table value="#{SyllabusTool.entries}" var="eachEntry" summary="#{msgs.mainEditListSummary}" styleClass="listHier lines nolines">
<%--						<h:column rendered="#{!empty SyllabusTool.entries}">--%>
						<h:column rendered="#{! SyllabusTool.displayNoEntryMsg}">
							<f:facet name="header">
								<h:outputText value="#{msgs.mainEditHeaderItem}" />
							</f:facet>
							<h:inputText value="#{eachEntry.entry.title}"/>
							<f:verbatim><br/></f:verbatim>
							<h:commandLink action="#{eachEntry.processListRead}" title="#{msgs.goToItem} #{eachEntry.entry.title}">
								<h:outputText value="edit details"/>
							</h:commandLink>
							
							
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
										<input type="checkbox" onchange="$('.calendarBox').attr('checked', this.checked);"/>
									</f:verbatim>
								</h:panelGroup>
							</f:facet>
							<h:selectBooleanCheckbox styleClass="calendarBox" value="#{eachEntry.entry.linkCalendar}" title="#{msgs.selectThisCheckBoxCal}"/>
						</h:column>
						<h:column rendered="#{! SyllabusTool.displayNoEntryMsg}">
							<f:facet name="header">
								<h:panelGroup>
									<h:outputText value="#{msgs.mainEditHeaderStatus}"/>
									<f:verbatim>
										<br/>
										<input type="checkbox" onchange="$('.postBox').attr('checked', this.checked);"/>
									</f:verbatim>
								</h:panelGroup>
							</f:facet>
							<h:selectBooleanCheckbox styleClass="postBox" value="#{eachEntry.posted}" title="#{msgs.selectThisCheckBoxPost}"/>
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
			 </syllabus:syllabus_table>
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
		    <syllabus:syllabus_iframe redirectUrl="#{SyllabusTool.syllabusItem.redirectURL}" width="100%" height="500" />
		  </syllabus:syllabus_ifnot>
		</h:form>
	</sakai:view_content>
	</sakai:view_container>
</f:view>
