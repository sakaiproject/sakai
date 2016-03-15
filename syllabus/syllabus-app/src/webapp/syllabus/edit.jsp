<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/syllabus" prefix="syllabus" %>
<% response.setContentType("text/html; charset=UTF-8"); %>
<f:view>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
	<jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.tool.syllabus.bundle.Messages"/>
</jsp:useBean>

	<sakai:view_container title="#{msgs.title_edit}">
		<sakai:view_content>

<script>includeLatestJQuery('edit.jsp');</script>
<link rel="stylesheet" href="/library/webjars/jquery-ui/1.11.3/jquery-ui.min.css" type="text/css" />
<script type="text/javascript" src="js/jquery-ui-timepicker-addon.js"></script>


<script type="text/javascript">
  $(function() {
    $('.dateInput').datetimepicker({
    	hour: 8,
		timeFormat: "hh:mm tt",
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
  });
 </script>
			<h:outputText value="#{SyllabusTool.alertMessage}" styleClass="alertMessage" rendered="#{SyllabusTool.alertMessage != null}" />
			<h:messages styleClass="alertMessage" rendered="#{!empty facesContext.maximumSeverity}" />
			<h:form>
		  	<sakai:tool_bar_message value="#{msgs.add_sylla}" /> 
 			<sakai:doc_section>
 				<h:outputText value="#{msgs.newSyllabusForm1}"/>
 				<h:outputText value="*" styleClass="reqStarInline"/>
 				<h:outputText value="#{msgs.newSyllabusForm2}"/>
 			</sakai:doc_section>
 
 			<h:panelGrid columns="2" styleClass="jsfFormTable">
 				<h:panelGroup styleClass="shorttext required">
 					<h:outputText value="*" styleClass="reqStar"/>
 					
 					<h:outputLabel for="title">
 						<h:outputText value="#{msgs.syllabus_title}"/>
 					</h:outputLabel>
 					<h:inputText value="#{SyllabusTool.syllabusDataTitle}" id="title"/>
 				</h:panelGroup>
 			</h:panelGrid>
 
 			<div class="longtext">
 				<label for="" style="float:none;display:block">
 					<h:outputText value="#{msgs.syllabus_content}"/>
 				</label>
				<sakai:inputRichText textareaOnly="#{SyllabusTool.mobileSession}" rows="20" cols="120" id="syllabus_compose_edit" value="#{SyllabusTool.syllabusDataAsset}" />
 			</div>
			<div class="checkbox indnt1">
				<h:selectOneRadio value="#{SyllabusTool.syllabusDataView}" layout="pageDirection" styleClass="checkbox">
					<f:selectItem itemValue="no" itemLabel="#{msgs.noPrivate}" />
					<f:selectItem itemValue="yes" itemLabel="#{msgs.yesPublic}"/>
				</h:selectOneRadio>
			</div>	
				<h4>
					<h:outputText value="#{msgs.attachment}"/>
				</h4>

							<sakai:button_bar>
							<%-- (gsilver) cannot pass a needed title atribute to these next items --%>
								<h:commandButton
									action="#{SyllabusTool.processAddAttachRedirect}" 
									value="#{msgs.add_attach}"/>
							</sakai:button_bar>
							
					<h:dataTable value="#{SyllabusTool.attachments}" var="eachAttach" summary="#{msgs.edit_att_list_summary}" styleClass="listHier lines nolines">
					  <h:column rendered="#{!empty SyllabusTool.attachments}">
							<f:facet name="header">
								<h:outputText value="#{msgs.attachmentTitle}"/>
							</f:facet>
							<f:verbatim><h4></f:verbatim>
							<sakai:contentTypeMap fileType="#{eachAttach.type}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>									
							<h:graphicImage id="exampleFileIcon" value="#{imagePath}" />	
							<h:outputText value="#{eachAttach.name}"/>
							<f:verbatim></h4></f:verbatim>
							<f:verbatim><div class="itemAction"></f:verbatim>
							
							<h:commandLink action="#{SyllabusTool.processDeleteAttach}" 
								onfocus="document.forms[0].onsubmit();" title="#{msgs.removeAttachmentLink} #{eachAttach.name}">
									<h:outputText value="#{msgs.mainEditHeaderRemove}"/>
									<f:param value="#{eachAttach.syllabusAttachId}" name="syllabus_current_attach"/>
							</h:commandLink>
							<f:verbatim></div></f:verbatim>	
					  </h:column>
					  <h:column rendered="#{!empty SyllabusTool.attachments}">
							<f:facet name="header">
								<h:outputText value="#{msgs.size}" />
							</f:facet>
							<h:outputText value="#{eachAttach.size}"/>
					  </h:column>
					  <h:column rendered="#{!empty SyllabusTool.attachments}">
						<f:facet name="header">
			  			    <h:outputText value="#{msgs.type}" />
						</f:facet>
							<h:outputText value="#{eachAttach.type}"/>
					  </h:column>
					  <h:column rendered="#{!empty SyllabusTool.attachments}">
							<f:facet name="header">
								<h:outputText value="#{msgs.created_by}" />
							</f:facet>
							<h:outputText value="#{eachAttach.createdBy}"/>
						</h:column>
					  <h:column rendered="#{!empty SyllabusTool.attachments}">
							<f:facet name="header">
								<h:outputText value="#{msgs.last_modified}" />
							</f:facet>
							<h:outputText value="#{eachAttach.lastModifiedBy}"/>
					  </h:column>
					</h:dataTable>
					
					<!-- Date -->
					<h4>
						<h:outputText value="#{msgs.dateheader}"/>
					</h4>
					<h:panelGrid columns="1" styleClass="jsfFormTable">
						<h:panelGroup styleClass="shorttext">
							<h:outputLabel for="dataStartDate">
								<h:outputText value="#{msgs.startdatetitle}"/>
							</h:outputLabel>
							<h:inputText styleClass="dateInput datInputStart" value="#{SyllabusTool.syllabusDataStartDate}" id="dataStartDate"/>
							<f:verbatim><img src="/library/image/silk/calendar_view_month.png" onclick="$('.datInputStart').focus();"/></f:verbatim>
						</h:panelGroup>
						<h:panelGroup styleClass="shorttext">
							<h:outputLabel for="dataEndDate">
								<h:outputText value="#{msgs.enddatetitle}"/>
							</h:outputLabel>
							<h:inputText styleClass="dateInput datInputEnd" value="#{SyllabusTool.syllabusDataEndDate}" id="dataEndDate"/>
							<f:verbatim><img src="/library/image/silk/calendar_view_month.png" onclick="$('.datInputEnd').focus();"/></f:verbatim>
						</h:panelGroup>
						<h:panelGroup styleClass="shorttext" rendered="#{SyllabusTool.calendarExistsForSite}">
							<h:selectBooleanCheckbox id="linkCalendar" value="#{SyllabusTool.syllabusDataLinkCalendar}" />
							<h:outputLabel for="linkCalendar">
								<h:outputText value="#{msgs.linkcalendartitle}"/>
							</h:outputLabel>
							<h:outputText value="#{msgs.invalid_calendar}" styleClass="alertMessage" 
									rendered="#{SyllabusTool.displayCalendarError}"/>
						</h:panelGroup>
					</h:panelGrid>
					
				<h4>
					<h:outputText value="#{msgs.notificationheader}"/>
				</h4>
				<h:panelGrid columns="1" styleClass="jsfFormTable">
					<h:panelGroup styleClass="shorttext">
						<h:outputLabel for="list1">
							<h:outputText value="#{msgs.email_notify}"/>
						</h:outputLabel>
						<h:selectOneListbox size = "1"  id = "list1" value="#{SyllabusTool.syllabusDataEmailNotification}">
							<f:selectItem itemLabel="#{msgs.notifyNone}" itemValue="none"/>
							<f:selectItem itemLabel="#{msgs.notifyHigh}" itemValue="high"/>
							<f:selectItem itemLabel="#{msgs.notifyLow}" itemValue="low"/>
						</h:selectOneListbox>
					</h:panelGroup>
				</h:panelGrid>
				
				<sakai:button_bar>
					<h:commandButton
						action="#{SyllabusTool.processEditPost}"
						styleClass="active"
						value="#{msgs.bar_publish}"
						accesskey="s"
						title="#{msgs.button_publish}" />
					<h:commandButton
						action="#{SyllabusTool.processEditPreview}"
						value="#{msgs.bar_preview}"
						accesskey="v"
						title="#{msgs.button_preview}"	/>
					<h:commandButton
						action="#{SyllabusTool.processEditSave}"
						value="#{msgs.bar_save_draft}" 
						accesskey="d"
						title="#{msgs.button_save}" />

					<h:commandButton
						action="#{SyllabusTool.processEditCancel}"
						value="#{msgs.cancel}" 
						accesskey="x"
						title="#{msgs.button_cancel}" />

				</sakai:button_bar>
		 </h:form>
		</sakai:view_content>
	</sakai:view_container>
</f:view> 
