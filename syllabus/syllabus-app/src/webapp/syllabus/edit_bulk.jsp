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
		$(function() {
			$(".dateInput").datepicker({
				beforeShow: function (textbox, instance) {
					            instance.dpDiv.css({
					                    marginLeft: textbox.offsetWidth + 'px'
					            });
					        }
			});
			$('.timeInput').timepicker({
		    	hour: 8,
		    	defaultValue: "08:00 <h:outputText value="#{msgs.am}"/>",
		    	timeOnlyTitle: "<h:outputText value="#{msgs.choose_time}"/>",
				timeFormat: "hh:mm tt",
				currentText: "<h:outputText value="#{msgs.now}"/>",
				closeText: "<h:outputText value="#{msgs.done}"/>",
				amNames: ['<h:outputText value="#{msgs.am}"/>', '<h:outputText value="#{msgs.am2}"/>'],
				pmNames: ['<h:outputText value="#{msgs.pm}"/>', '<h:outputText value="#{msgs.pm2}"/>'],
				timeText: "<h:outputText value="#{msgs.time}"/>",
				hourText: "<h:outputText value="#{msgs.hour}"/>",
				minuteText: "<h:outputText value="#{msgs.minute}"/>",
				beforeShow: function (textbox, instance) {
					            instance.dpDiv.css({
					                    marginLeft: textbox.offsetWidth + 'px'
								});
							}
			});
		});
	</script>
	<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
		<jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.tool.syllabus.bundle.Messages"/>
	</jsp:useBean>
	<sakai:view_container title="#{msgs.title_edit_bulk}">
		<sakai:view_content>
			<h:outputText value="#{SyllabusTool.alertMessage}" styleClass="alertMessage" rendered="#{SyllabusTool.alertMessage != null}" />
				
			<sakai:tool_bar_message value="#{msgs.add_sylla_bulk}" /> 
			<sakai:doc_section>
				<h:outputText value="#{msgs.newSyllabusBulkForm}"/>
			</sakai:doc_section>
			<h:form>
				<h:panelGrid columns="1" styleClass="jsfFormTable">
					<h:panelGroup styleClass="shorttext">
	 					<h:outputLabel for="title">
	 						<h:outputText value="*" styleClass="reqStar"/>
	 						<h:outputText value="#{msgs.syllabus_title}"/>
	 					</h:outputLabel>
	 					<h:inputText value="#{SyllabusTool.bulkEntry.title}" id="title"/>
					</h:panelGroup>
					<h:panelGroup styleClass="shorttext">
						<h:outputLabel for="dataStartDate">
							<h:outputText value="*" styleClass="reqStar"/>
							<h:outputText value="#{msgs.startdatetitle}"/>
						</h:outputLabel>
						<h:inputText styleClass="dateInput datInputStart" value="#{SyllabusTool.bulkEntry.startDateString}" id="dataStartDate"/>
						<f:verbatim><img src="/library/image/silk/calendar_view_month.png" onclick="$('.datInputStart').focus();"/></f:verbatim>
					</h:panelGroup>
					<h:panelGroup styleClass="shorttext">
						<h:outputLabel for="dataEndDate">
							<h:outputText value="*" styleClass="reqStar"/>
							<h:outputText value="#{msgs.enddatetitle}"/>
						</h:outputLabel>
						<h:inputText styleClass="dateInput datInputEnd" value="#{SyllabusTool.bulkEntry.endDateString}" id="dataEndDate"/>
						<f:verbatim><img src="/library/image/silk/calendar_view_month.png" onclick="$('.datInputEnd').focus();"/></f:verbatim>
					</h:panelGroup>
					<h:panelGroup styleClass="shorttext">
						<h:outputLabel for="dataStartTime">
							<h:outputText value="*" styleClass="reqStar"/>
							<h:outputText value="#{msgs.starttimetitle}"/>
						</h:outputLabel>
						<h:inputText styleClass="timeInput timeInputStart" value="#{SyllabusTool.bulkEntry.startTimeString}" id="dataStartTime"/>
						<f:verbatim><img src="/library/image/silk/calendar_view_month.png" onclick="$('.timeInputStart').focus();"/></f:verbatim>
					</h:panelGroup>
					<h:panelGroup styleClass="shorttext">
						<h:outputLabel for="dataEndTime">
							<h:outputText value="#{msgs.endtimetitle}"/>
						</h:outputLabel>
						<h:inputText styleClass="timeInput timeInputEnd" value="#{SyllabusTool.bulkEntry.endTimeString}" id="dataEndTime"/>
						<f:verbatim><img src="/library/image/silk/calendar_view_month.png" onclick="$('.timeInputEnd').focus();"/></f:verbatim>
					</h:panelGroup>
					<h:panelGroup styleClass="shorttext" rendered="#{SyllabusTool.calendarExistsForSite}">
						<h:selectBooleanCheckbox id="linkCalendar" value="#{SyllabusTool.bulkEntry.linkCalendar}" />
						<h:outputLabel for="linkCalendar">
							<h:outputText value="#{msgs.linkcalendartitle}"/>
						</h:outputLabel>
					</h:panelGroup>
					<h:panelGroup styleClass="shorttext" rendered="#{SyllabusTool.calendarExistsForSite}">
						<h:selectBooleanCheckbox id="monday" value="#{SyllabusTool.bulkEntry.monday}" />
						<h:outputText value="#{msgs.monday}"/>
						<h:outputText value=" | "/>
						<h:selectBooleanCheckbox id="tuesday" value="#{SyllabusTool.bulkEntry.tuesday}" />
						<h:outputText value="#{msgs.tuesday}"/>
						<h:outputText value=" | "/>
						<h:selectBooleanCheckbox id="wednesday" value="#{SyllabusTool.bulkEntry.wednesday}" />
						<h:outputText value="#{msgs.wednesday}"/>
						<h:outputText value=" | "/>
						<h:selectBooleanCheckbox id="thursday" value="#{SyllabusTool.bulkEntry.thursday}" />
						<h:outputText value="#{msgs.thursday}"/>
						<h:outputText value=" | "/>
						<h:selectBooleanCheckbox id="friday" value="#{SyllabusTool.bulkEntry.friday}" />
						<h:outputText value="#{msgs.friday}"/>
						<h:outputText value=" | "/>
						<h:selectBooleanCheckbox id="saturday" value="#{SyllabusTool.bulkEntry.saturday}" />
						<h:outputText value="#{msgs.saturday}"/>
						<h:outputText value=" | "/>
						<h:selectBooleanCheckbox id="sunday" value="#{SyllabusTool.bulkEntry.sunday}" />
						<h:outputText value="#{msgs.sunday}"/>
						<h:outputLabel for="monday">
							<h:outputText value="*" styleClass="reqStar"/>
							<h:outputText value="#{msgs.classMeetingDays}"/>
						</h:outputLabel>
					</h:panelGroup>
				</h:panelGrid>
				<sakai:button_bar>
					<sakai:button_bar_item
						action="#{SyllabusTool.processEditBulkPost}"
						styleClass="active"
						value="#{msgs.bar_post}" 
						accesskey="s"
						title="#{msgs.button_post}" />
					<sakai:button_bar_item
						action="#{SyllabusTool.processEditBulkCancel}"
						value="#{msgs.cancel}" 
						accesskey="x"
						title="#{msgs.button_cancel}" />
				</sakai:button_bar>
			</h:form>
		</sakai:view_content>
	</sakai:view_container>

</f:view>