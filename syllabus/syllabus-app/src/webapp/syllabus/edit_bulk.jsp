<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/syllabus" prefix="syllabus" %>
<% response.setContentType("text/html; charset=UTF-8"); %>
<f:view>
	<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
		<jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.tool.syllabus.bundle.Messages"/>
	</jsp:useBean>
	<sakai:view_container title="#{msgs.bar_create_edit}">
		<sakai:view_content>

<script>includeLatestJQuery('edit_bulk.jsp');</script>
<link rel="stylesheet" href="/library/webjars/jquery-ui/1.12.1/jquery-ui.min.css" type="text/css" />
<script type="text/javascript" src="/library/js/lang-datepicker/lang-datepicker.js"></script>


	<script>
		jQuery(document).ready(function() {
			localDatePicker({
				input: '#syllabusEdit\\:dataStartDate',
				useTime: 0,
				parseFormat: 'YYYY-MM-DD',
				allowEmptyDate: true,
				val: '<h:outputText value="#{SyllabusTool.bulkEntry.startDate}"><f:convertDateTime pattern="yyyy-MM-dd"/></h:outputText>',
				ashidden: {iso8601: 'dataStartDateISO8601'}
			});
			localDatePicker({
				input: '#syllabusEdit\\:dataEndDate',
				useTime: 0,
				parseFormat: 'YYYY-MM-DD',
				allowEmptyDate: true,
				val: '<h:outputText value="#{SyllabusTool.bulkEntry.endDate}"><f:convertDateTime pattern="yyyy-MM-dd"/></h:outputText>',
				ashidden: {iso8601: 'dataEndDateISO8601'}
			});

			var menuLink = $('#syllabusMenuBulkAddItemLink');
			menuLink.addClass('current');
			menuLink.find('a').removeAttr('href');

		});
		$(function() {
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

			$('input.active[type="submit"]').click(function(event) {
					// Check if the input box is empty
					if($('#syllabusEdit\\:title').val().trim() === '') {
						// If the input box is empty, prevent the form from submitting and show an alert
						event.preventDefault(); // Prevent the form from submitting
						$('#syllabusEdit\\:emptyTitleAlert').removeClass('d-none');
						$('#syllabusEdit\\:title').addClass('is-invalid').focus();
					}
					else if ( $('#syllabusEdit\\:radioByDate\\:0').is(':checked') ) {
						if (!validateDateElements()) {
							event.preventDefault(); // Prevent form submission
						}
					}
			});

			function validateDateElements() {
				const elementIds = [
					'syllabusEdit\\:dataStartDate',
					'syllabusEdit\\:dataEndDate',
					'syllabusEdit\\:dataStartTime'
				];

				for (let i = 0; i < elementIds.length; i++) {
					const element = $('#' + elementIds[i]);
					if (element.val().trim() === '') {
						element.focus();
						element.parent().find('.invalid-feedback').show();
						return false; // Form is invalid
					}
				}

				const days = [
					'syllabusEdit\\:monday',
					'syllabusEdit\\:tuesday',
					'syllabusEdit\\:wednesday',
					'syllabusEdit\\:thursday',
					'syllabusEdit\\:friday',
					'syllabusEdit\\:saturday',
					'syllabusEdit\\:sunday'
				];

				const isAnyDaySelected = days.some(dayId => $('#' + dayId).is(':checked'));

				if (!isAnyDaySelected) {
					$('#' + days[0]).focus(); // Focus on the first checkbox 
					$('#syllabusEdit\\:dayOfWeek-feedback').show();
					return false; // Form is invalid
				}
				return true;
			}

			//radio options:
			$('.radioByDate input:radio').change(
				function(){
					$('.radioByItems input:radio').each(function(i){
						this.checked = false;
					});
					$('.radioSingleItem input:radio').each(function(i){
						this.checked = false;
					});
					$('.radioOption').each(function(i){
						$(this).removeClass("radioOptionSelected");
					});
					$('.radioByDate').each(function(i){
						$(this).addClass("radioOptionSelected");
					});
					$('.bulkAddByItemsPanel').slideUp();
					$('.bulkAddByDatePanel').slideDown();
					$('#syllabusEdit\\:dataStartDate').focus();
					resizeFrame('grow');
				}
			);
			$('.radioByItems input:radio').change(
				function(){
					$('.radioByDate input:radio').each(function(i){
						this.checked = false;
					});
					$('.radioSingleItem input:radio').each(function(i){
						this.checked = false;
					});
					$('.radioOption').each(function(i){
						$(this).removeClass("radioOptionSelected");
					});
					$('.radioByItems').each(function(i){
						$(this).addClass("radioOptionSelected");
					});
					$('.bulkAddByItemsPanel').slideDown();
					$('.bulkAddByDatePanel').slideUp();
					resizeFrame('shrink');
				}
			);
			$('.radioSingleItem input:radio').change(
				function () {
					$('.radioByDate input:radio').each(function () {
						this.checked = false;
					});
					$('.radioByItems input:radio').each(function () {
						this.checked = false;
					});
					$('.radioOption').each(function () {
						$(this).removeClass("radioOptionSelected");
					});
					$('.radioSingleItem').each(function () {
						$(this).addClass("radioOptionSelected");
					});
					$('.bulkAddByItemsPanel').slideUp();
					$('.bulkAddByDatePanel').slideUp();
					resizeFrame('shrink');
				}
			);
		});

		//this function needs jquery 1.1.2 or later - it resizes the parent iframe without bringing the scroll to the top
		function resizeFrame(updown){
			if (top.location !== self.location) {
				const frame = parent.document.getElementById(window.name);
				if (frame) {
					let clientH;
					if (updown == 'shrink') {
						clientH = document.body.clientHeight - 200;
					} else {
						clientH = document.body.clientHeight + 200;
					}
					$(frame).height(clientH);
				} else {
					throw ("resizeFrame did not get the frame (using name=" + window.name + ")");
				}
			}
		}
	</script>
			<h:form id="syllabusEdit">
				<%@ include file="mainMenu.jsp" %>
				<h:outputText value="#{SyllabusTool.alertMessage}" styleClass="sak-banner-error" rendered="#{SyllabusTool.alertMessage != null}" />
				<sakai:tool_bar_message value="#{msgs.add_sylla_bulk}" /> 
				<sakai:doc_section>
					<h:outputText value="#{msgs.newSyllabusBulkForm}"/>
				</sakai:doc_section>
				<h:panelGrid columns="1" styleClass="jsfFormTable">
					<h:panelGroup styleClass="shorttext">
						<h:outputLabel for="title" styleClass="form-label">
							<h:outputText value="*" styleClass="reqStar"/>
							<h:outputText value="#{msgs.syllabus_title}"/>
						</h:outputLabel>
						<h:inputText value="#{SyllabusTool.bulkEntry.title}" id="title" styleClass="form-control" />
						<h:panelGroup layout="block" id="emptyTitleAlert" styleClass="invalid-feedback d-none">
							<h:outputText value="#{msgs.empty_title_validate}" escape="false" />
						</h:panelGroup>
					</h:panelGroup>
					<h:panelGroup>
						<h:selectOneRadio id="radioSingleItem" value="#{SyllabusTool.bulkEntry.addSingleItem}" styleClass="radioSingleItem radioOption radioOptionSelected">
							<f:selectItem id="singleItem" itemLabel="#{msgs.bulkAddSingleItem}" itemValue="1" />
						</h:selectOneRadio>
						<h:panelGroup layout="block" styleClass="instruction">
							<h:outputText value="#{msgs.newSyllabusSingle}" />
						</h:panelGroup>
					</h:panelGroup>
					<h:panelGroup>
						<h:selectOneRadio id="radioByItems" value="#{SyllabusTool.bulkEntry.addByItems}" styleClass="radioByItems radioOption">
							<f:selectItem id="byItems" itemLabel="#{msgs.bulkAddByItems}" itemValue="1" />
						</h:selectOneRadio>
						<h:panelGroup layout="block" styleClass="instruction">
							<h:outputText value="#{msgs.newSyllabusByNumber}" />
						</h:panelGroup>
					</h:panelGroup>

					<!-- Add X Bulk Entries -->
					<h:panelGrid columns="1" styleClass="jsfFormTable indnt1 bulkAddByItemsPanel" style="display: none">
						<h:panelGroup styleClass="shorttext">
							<h:outputLabel for="numOfItems">
								<h:outputText value="*" styleClass="reqStar"/>
								<h:outputText value="#{msgs.numberOfItems}"/>
							</h:outputLabel>
							<h:selectOneMenu value="#{SyllabusTool.bulkEntry.bulkItems}" id="numOfItems">
								<f:selectItem itemValue = "2" itemLabel = "2" />
								<f:selectItem itemValue = "3" itemLabel = "3" />
								<f:selectItem itemValue = "4" itemLabel = "4" />
								<f:selectItem itemValue = "5" itemLabel = "5" />
								<f:selectItem itemValue = "6" itemLabel = "6" />
								<f:selectItem itemValue = "7" itemLabel = "7" />
								<f:selectItem itemValue = "8" itemLabel = "8" />
								<f:selectItem itemValue = "9" itemLabel = "9" />
								<f:selectItem itemValue = "10" itemLabel = "10" />
								<f:selectItem itemValue = "11" itemLabel = "11" />
								<f:selectItem itemValue = "12" itemLabel = "12" />
								<f:selectItem itemValue = "13" itemLabel = "13" />
								<f:selectItem itemValue = "14" itemLabel = "14" />
								<f:selectItem itemValue = "15" itemLabel = "15" />
								<f:selectItem itemValue = "16" itemLabel = "16" />
								<f:selectItem itemValue = "17" itemLabel = "17" />
								<f:selectItem itemValue = "18" itemLabel = "18" />
								<f:selectItem itemValue = "19" itemLabel = "19" />
								<f:selectItem itemValue = "20" itemLabel = "20" />
							</h:selectOneMenu>
						</h:panelGroup>
					</h:panelGrid>
					<h:panelGroup>
						<h:selectOneRadio id="radioByDate" value="#{SyllabusTool.bulkEntry.addByDate}" styleClass="radioByDate radioOption ">
							<f:selectItem id="byDate" itemLabel="#{msgs.bulkAddByDate}" itemValue="1" />
						</h:selectOneRadio>
						<h:panelGroup layout="block" styleClass="instruction">
							<h:outputText value="#{msgs.newSyllabusByDate}" />
						</h:panelGroup>
					</h:panelGroup>
					<!-- Add Bulk Entries by date -->
					<h:panelGrid columns="1" styleClass="jsfFormTable indnt1 bulkAddByDatePanel" style="display: none">
						<h:panelGroup styleClass="shorttext">
							<h:outputLabel for="dataStartDate">
								<h:outputText value="*" styleClass="reqStar"/>
								<h:outputText value="#{msgs.startdatetitle}"/>
							</h:outputLabel>
							<h:inputText styleClass="datInputStart" value="#{SyllabusTool.bulkEntry.startDateString}" id="dataStartDate"/>
							<h:panelGroup layout="block" id="dataStartDate-feedback" styleClass="invalid-feedback">
								<h:outputText value="#{msgs.start_date_required}" escape="false" />
							</h:panelGroup>
						</h:panelGroup>
						<h:panelGroup styleClass="shorttext">
							<h:outputLabel for="dataEndDate">
								<h:outputText value="*" styleClass="reqStar"/>
								<h:outputText value="#{msgs.enddatetitle}"/>
							</h:outputLabel>
							<h:inputText styleClass="datInputEnd" value="#{SyllabusTool.bulkEntry.endDateString}" id="dataEndDate"/>
							<h:panelGroup layout="block" id="dataEndDate-feedback" styleClass="invalid-feedback">
								<h:outputText value="#{msgs.end_date_required}" escape="false" />
							</h:panelGroup>
						</h:panelGroup>
						<h:panelGroup styleClass="shorttext">
							<h:outputLabel for="dataStartTime">
								<h:outputText value="*" styleClass="reqStar"/>
								<h:outputText value="#{msgs.starttimetitle}"/>
							</h:outputLabel>
							<h:inputText styleClass="timeInput timeInputStart" value="#{SyllabusTool.bulkEntry.startTimeString}" id="dataStartTime"/>
							<f:verbatim><span class="fa fa-calendar-times-o" onclick="$('.timeInputStart').focus();"></span></f:verbatim>
							<h:panelGroup layout="block" id="dataStartTime-feedback" styleClass="invalid-feedback">
								<h:outputText value="#{msgs.start_time_required}" escape="false" />
							</h:panelGroup>
						</h:panelGroup>
						<h:panelGroup styleClass="shorttext">
							<h:outputLabel for="dataEndTime">
								<h:outputText value="#{msgs.endtimetitle}"/>
							</h:outputLabel>
							<h:inputText styleClass="timeInput timeInputEnd" value="#{SyllabusTool.bulkEntry.endTimeString}" id="dataEndTime"/>
							<f:verbatim><span class="fa fa-calendar-times-o" onclick="$('.timeInputEnd').focus();"></span></f:verbatim>
						</h:panelGroup>
						<h:panelGroup styleClass="shorttext" rendered="#{SyllabusTool.calendarExistsForSite}">
							<h:selectBooleanCheckbox id="linkCalendar" value="#{SyllabusTool.bulkEntry.linkCalendar}" />
							<h:outputLabel for="linkCalendar">
								<h:outputText value="#{msgs.linkcalendartitle}"/>
							</h:outputLabel>
						</h:panelGroup>

						<h:panelGroup styleClass="row gx-2">
							<h:panelGroup styleClass="col-auto form-check form-check-inline">
								<h:selectBooleanCheckbox id="monday" styleClass="form-check-input" value="#{SyllabusTool.bulkEntry.monday}" />
								<h:outputLabel for="monday" styleClass="form-check-label" value="#{msgs.monday}"></h:outputLabel>
							</h:panelGroup>

							<h:panelGroup styleClass="col-auto form-check form-check-inline">
								<h:selectBooleanCheckbox id="tuesday" styleClass="form-check-input" value="#{SyllabusTool.bulkEntry.tuesday}" />
								<h:outputLabel for="tuesday" styleClass="form-check-label" value="#{msgs.tuesday}"></h:outputLabel>
							</h:panelGroup>

							<h:panelGroup styleClass="col-auto form-check form-check-inline">
								<h:selectBooleanCheckbox id="wednesday" styleClass="form-check-input" value="#{SyllabusTool.bulkEntry.wednesday}" />
								<h:outputLabel for="wednesday" styleClass="form-check-label" value="#{msgs.wednesday}"></h:outputLabel>
							</h:panelGroup>

							<h:panelGroup styleClass="col-auto form-check form-check-inline">
								<h:selectBooleanCheckbox id="thursday" styleClass="form-check-input" value="#{SyllabusTool.bulkEntry.thursday}" />
								<h:outputLabel for="thursday" styleClass="form-check-label" value="#{msgs.thursday}"></h:outputLabel>
							</h:panelGroup>

							<h:panelGroup styleClass="col-auto form-check form-check-inline">
								<h:selectBooleanCheckbox id="friday" styleClass="form-check-input" value="#{SyllabusTool.bulkEntry.friday}" />
								<h:outputLabel for="friday" styleClass="form-check-label" value="#{msgs.friday}"></h:outputLabel>
							</h:panelGroup>

							<h:panelGroup styleClass="col-auto form-check form-check-inline">
								<h:selectBooleanCheckbox id="saturday" styleClass="form-check-input" value="#{SyllabusTool.bulkEntry.saturday}" />
								<h:outputLabel for="saturday" styleClass="form-check-label" value="#{msgs.saturday}"></h:outputLabel>
							</h:panelGroup>

							<h:panelGroup styleClass="col-auto form-check form-check-inline">
								<h:selectBooleanCheckbox id="sunday" styleClass="form-check-input" value="#{SyllabusTool.bulkEntry.sunday}" />
								<h:outputLabel for="sunday" styleClass="form-check-label" value="#{msgs.sunday}"></h:outputLabel>
							</h:panelGroup>

							<h:panelGroup styleClass="col-auto form-check form-check-inline">
								<h:outputText value="*" styleClass="reqStar"/>
								<h:outputText value="#{msgs.classMeetingDays}"/>
							</h:panelGroup>

							<h:panelGroup layout="block" id="dayOfWeek-feedback" styleClass="invalid-feedback">
								<h:outputText value="#{msgs.dayOfWeekRequired}" escape="false" />
							</h:panelGroup>

						</h:panelGroup>

					</h:panelGrid>
				</h:panelGrid>
				<sakai:button_bar>
					<h:commandButton
						action="#{SyllabusTool.processEditBulkDraft}"
						styleClass="active"
						value="#{msgs.bar_new}" 
						accesskey="s"
						title="#{msgs.button_save}" />
					<h:commandButton
						action="#{SyllabusTool.processEditBulkCancel}"
						value="#{msgs.cancel}" 
						accesskey="x"
						title="#{msgs.button_cancel}" />
				</sakai:button_bar>
			</h:form>
		</sakai:view_content>
	</sakai:view_container>

</f:view>
