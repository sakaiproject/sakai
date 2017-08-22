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

<script>includeLatestJQuery('read.jsp');</script>
<link rel="stylesheet" href="/library/webjars/jquery-ui/1.12.1/jquery-ui.min.css" type="text/css" />
<script type="text/javascript" src="/library/js/lang-datepicker/lang-datepicker.js"></script>
<style>
	.ui-datepicker { 
	  margin-left: 100px;
	  z-index: 1000;
	}
</style>

<script type="text/javascript">
	jQuery(document).ready(function() {
		localDatePicker({
			input: '#readview\\:dataStartDate',
			useTime: 1,
			parseFormat: 'YYYY-MM-DD HH:mm:ss',
			allowEmptyDate: true,
			val: '<h:outputText value="#{SyllabusTool.syllabusDataStartDate}"/>',
			ashidden: {
					iso8601: 'dataStartDateISO8601'}
		});
		localDatePicker({
			input: '#readview\\:dataEndDate',
			useTime: 1,
			parseFormat: 'YYYY-MM-DD HH:mm:ss',
			allowEmptyDate: true,
			val: '<h:outputText value="#{SyllabusTool.syllabusDataEndDate}"/>',
			ashidden: {
					iso8601: 'dataEndDateISO8601'}
		});
	});
 </script>
			<h:outputText value="#{SyllabusTool.alertMessage}" styleClass="alertMessage" rendered="#{SyllabusTool.alertMessage != null}" />
			<h:form id="readview">
		  	<sakai:tool_bar_message value="#{msgs.editNotice}" /> 
				<sakai:doc_section>
					<h:outputText value="#{msgs.newSyllabusForm1}"/>
					<h:outputText value="*" styleClass="reqStarInline"/>
					<h:outputText value="#{msgs.newSyllabusForm2}"/>
				</sakai:doc_section>

			<h:panelGrid columns="1" styleClass="jsfFormTable">
				<h:panelGroup styleClass="shorttext required">
					<h:outputText value="*" styleClass="reqStar"/>
					<h:outputLabel for="title">
						<h:outputText value="#{msgs.syllabus_title}"/>
					</h:outputLabel>
					<h:inputText value="#{SyllabusTool.syllabusDataTitle}" id="title"/>
				</h:panelGroup>
			</h:panelGrid>

			<p class="longtext">
				<label for="" style="float:none;display:block"> <%-- outputLabel needed here instead but there is no target to id --%>
					<h:outputText value="#{msgs.syllabus_content}"/>
				</label>
				<sakai:inputRichText textareaOnly="#{SyllabusTool.mobileSession}" rows="20" cols="120" id="syllabus_compose_read" value="#{SyllabusTool.syllabusDataAsset}" />
			</p>	
			<div class="checkbox">
				<h:selectOneRadio value="#{SyllabusTool.syllabusDataView}"  layout="pageDirection" title="#{msgs.publicPrivate}">
					<f:selectItem itemValue="no" itemLabel="#{msgs.noPrivate}"/>
					<f:selectItem itemValue="yes" itemLabel="#{msgs.yesPublic}"/>
				</h:selectOneRadio>
			</div>
			<h4>
				<h:outputText value="#{msgs.attachment}"/>
			</h4>	
<%--						<h:outputText value="#{msgs.attachment}"/>
						<h:outputText value=" "/>--%>

<%--							<h:commandLink action="#{SyllabusTool.processAddAttWithOldItem}" onfocus="document.forms[0].onsubmit();">
									<h:outputText value="#{msgs.attachment_local} "/>
							</h:commandLink>--%>
<%--							<h:outputText value=" "/>
							<h:outputLink value="sakai.filepicker.helper/tool" onfocus="document.forms[0].onsubmit(); document.forms[0].submit();return false;">
							  <h:outputText id="file_picker" value=" #{msgs.file_picker}"/>
							</h:outputLink>--%>
					
					<sakai:button_bar>
<%-- (gsilver) cannot pass a needed title atribute to this next item --%>					
						<h:commandButton
							action="#{SyllabusTool.processAddAttachRedirect}" 
							value="#{msgs.add_attach}"/>
					</sakai:button_bar>							
	
					<h:dataTable value="#{SyllabusTool.allAttachments}" var="eachAttach" summary="#{msgs.edit_att_list_summary}" styleClass="listHier lines nolines">
					  <h:column rendered="#{!empty SyllabusTool.allAttachments}">
							<f:facet name="header">
								<h:outputText value="#{msgs.attachmentTitle}" />
							</f:facet>
							<f:verbatim><h5></f:verbatim>
							<sakai:contentTypeMap fileType="#{eachAttach.type}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>									
							<h:graphicImage id="exampleFileIcon" value="#{imagePath}" />
							<h:outputText value="#{eachAttach.name}"/>
							<f:verbatim></h5></f:verbatim>
							<f:verbatim><div class="itemAction"></f:verbatim>

							<h:commandLink action="#{SyllabusTool.processDeleteAttach}" 
								onfocus="document.forms[0].onsubmit();" title="#{msgs.removeAttachmentLink} #{eachAttach.name}">
								<h:outputText value="#{msgs.mainEditHeaderRemove}"/>
								<f:param value="#{eachAttach.syllabusAttachId}" name="syllabus_current_attach"/>
							</h:commandLink>
							<f:verbatim></div></f:verbatim>
						</h:column>
					  <h:column rendered="#{!empty SyllabusTool.allAttachments}">
							<f:facet name="header">
								<h:outputText value="#{msgs.size}" />
							</f:facet>
							<h:outputText value="#{eachAttach.size}"/>
						</h:column>
					  <h:column rendered="#{!empty SyllabusTool.allAttachments}">
							<f:facet name="header">
		  			    <h:outputText value="#{msgs.type}" />
							</f:facet>
							<h:outputText value="#{eachAttach.type}"/>
						</h:column>
					  <h:column rendered="#{!empty SyllabusTool.allAttachments}">
							<f:facet name="header">
								<h:outputText value="#{msgs.created_by}" />
							</f:facet>
							<h:outputText value="#{eachAttach.createdBy}"/>
						</h:column>
					  <h:column rendered="#{!empty SyllabusTool.allAttachments}">
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
							<h:inputText styleClass="datInputStart" value="#{SyllabusTool.syllabusDataStartDate}" id="dataStartDate"/>
						</h:panelGroup>
						<h:panelGroup styleClass="shorttext">
							<h:outputLabel for="dataEndDate">
								<h:outputText value="#{msgs.enddatetitle}"/>
							</h:outputLabel>
							<h:inputText styleClass="datInputEnd" value="#{SyllabusTool.syllabusDataEndDate}" id="dataEndDate"/>
						</h:panelGroup>
						<h:panelGroup styleClass="shorttext" rendered="#{SyllabusTool.calendarExistsForSite}">
							<h:selectBooleanCheckbox id="linkCalendar" value="#{SyllabusTool.syllabusDataLinkCalendar}" />
							<h:outputLabel for="linkCalendar">
								<h:outputText value="#{msgs.linkcalendartitle}"/>
							</h:outputLabel>
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
						action="#{SyllabusTool.processReadPost}"
						styleClass="active"
						value="#{msgs.bar_publish}"
						accesskey="s" />
					<h:commandButton
						action="#{SyllabusTool.processReadPreview}"
						value="#{msgs.bar_preview}" 
						accesskey="v"	/>
					<h:commandButton
						action="#{SyllabusTool.processReadSave}"
						value="#{msgs.bar_save_draft}" 
						accesskey="d" />
					<h:commandButton
						action="#{SyllabusTool.processReadCancel}"
						value="#{msgs.cancel}" 
						accesskey="x" />
				</sakai:button_bar>

			</h:form>
		</sakai:view_content>
	</sakai:view_container>
</f:view>
