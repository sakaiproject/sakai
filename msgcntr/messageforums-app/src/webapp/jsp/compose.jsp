<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>
<%
	String thisId = request.getParameter("panel");
	if (thisId == null) {
		thisId = "Main"	+ org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement().getId();
	}
%>


<f:view>
	<sakai:view title="#{msgs.pvt_pvtcompose}">
		<link rel="stylesheet" href="/messageforums-tool/css/messages.css" type="text/css" />
		<link rel="stylesheet" href="/library/webjars/jquery-ui/1.12.1/jquery-ui.min.css" type="text/css" />
		<script>includeLatestJQuery("msgcntr");</script>
		<script src="/messageforums-tool/js/datetimepicker.js"></script>
		<script src="/library/js/lang-datepicker/lang-datepicker.js"></script>
		<script src="/messageforums-tool/js/sak-10625.js"></script>
		<script src="/messageforums-tool/js/messages.js"></script>
		<script>includeWebjarLibrary('select2');</script>

	<h:form id="compose">
		<script>
				function clearSelection(selectObject)
				{
					for (var i=0; i<selectObject.options.length; i++)
					{
						selectObject.options[i].selected=false;
					}
					changeSelect(selectObject);
				}

				function fadeInBcc(clearSelected){
					$('.bccLink').fadeOut();
					$('.bcc').fadeIn();
					if (clearSelected) {
						clearSelection(document.getElementById('compose:list2'));
					}
					resize();
				}

				function fadeOutBcc(clearSelected){
					$('.bccLink').fadeIn();
					$('.bcc').fadeOut();
					if (clearSelected) {
						clearSelection(document.getElementById('compose:list2'));
					}
					resize();
				}

				function resize(){
					mySetMainFrameHeight('<%=org.sakaiproject.util.Web.escapeJavascript(thisId)%>');
				}

				$(document).ready(function() {
				  	if(document.getElementById('compose:list2').selectedIndex != -1){
				  		//BCC has selected items, so show it
				  		fadeInBcc(false);
				  	}
				  	addTagSelector(document.getElementById('compose:list1'));
				  	addTagSelector(document.getElementById('compose:list2'));
				  	resize();
					var menuLink = $('#messagesComposeMenuLink');
					var menuLinkSpan = menuLink.closest('span');
					menuLinkSpan.addClass('current');
					menuLinkSpan.html(menuLink.text());

					<f:verbatim rendered="#{PrivateMessagesTool.canUseTags}">
						initTagSelector("compose");
					</f:verbatim>
				});
			</script>
			<%@ include file="/jsp/privateMsg/pvtMenu.jsp" %>
		<!-- compose.jsp -->
	
	<div class="container_messages">
		<div class="page-header">
			<sakai:tool_bar_message value="#{msgs.pvt_pvtcompose}" />
		</div>
		
		<div class="instruction">
			<h:outputText value="#{msgs.cdfm_required}"/> <h:outputText value="#{msgs.pvt_star}" styleClass="reqStarInline" />
		</div>

		  <h:outputLink rendered="#{PrivateMessagesTool.renderPrivacyAlert}" value="#{PrivateMessagesTool.privacyAlertUrl}" target="_blank" >
		  	  <sakai:instruction_message value="#{PrivateMessagesTool.privacyAlert}"/>
		  </h:outputLink>

		  <h:messages styleClass="alertMessage" id="errorMessages" rendered="#{! empty facesContext.maximumSeverity}" />

		  <h:outputText styleClass="sak-banner-warn" value="#{msgs.pvt_hiddenGroupsBccMsg}" rendered="#{PrivateMessagesTool.displayHiddenGroupsMsg}" />
		  <h:outputText styleClass="sak-banner-warn" value="#{msgs.pvt_draftRecipientsNotFoundMsg}" rendered="#{PrivateMessagesTool.displayDraftRecipientsNotFoundMsg}" />

		  <div class="composeForm">
				<div class="row d-flex">
					<div class="col-xs-12 col-sm-2">
						<h:panelGroup styleClass="shorttext required form-control-label">
							<h:outputLabel for="list1">
								<h:outputText value="#{msgs.pvt_star}" styleClass="reqStar"/>
								<h:outputText value="#{msgs.pvt_to}"/>
							</h:outputLabel>
						</h:panelGroup>
					</div>
					<div class="col-xs-12 col-sm-10">
						<h:panelGroup styleClass="shorttext">
							<h:selectManyListbox id="list1" value="#{PrivateMessagesTool.selectedComposeToList}" size="5" style="width: 100%;" title="#{msgs.recipient_placeholder}">
								<f:selectItems value="#{PrivateMessagesTool.totalComposeToList}"/>
							</h:selectManyListbox>
							<f:verbatim>
								<span class="delete_selection">
									&nbsp;
							</f:verbatim>
							<h:graphicImage url="/../../library/image/silk/delete.png" title="#{msgs.pvt_bccClear}" alt="#{msgs.pvt_bccClear}"/>
							<f:verbatim>
								<a href="#" onclick="clearSelection(document.getElementById('compose:list1'));">
							</f:verbatim>
							<h:outputText value="#{msgs.pvt_bccClear}"/>
							<f:verbatim>
								</a>
								</span>
							</f:verbatim>
						</h:panelGroup>
					</div>
				</div>
				<div class="row bcc-row">
					<div class="col-xs-12 col-sm-2">
						<h:panelGroup styleClass="shorttext bccLink form-control-label">
							<h:outputLabel>
								<f:verbatim>
									&nbsp;
								</f:verbatim>
								<h:graphicImage url="/../../library/image/silk/add.png" title="#{msgs.pvt_addBcc}" alt="#{msgs.pvt_addBcc}"/>
								<f:verbatim>
									<a href="#" onclick="fadeInBcc(true);">
								</f:verbatim>
								<h:outputText value="#{msgs.pvt_addBcc}"/>
								<f:verbatim>
									</a>
								</f:verbatim>
							</h:outputLabel>
						</h:panelGroup>
						<h:panelGroup styleClass="shorttext bcc" style="display:none">
							<h:outputLabel for="list2">
								<h:outputText value="#{msgs.pvt_bcc}"/>
								<f:verbatim>
									<br>
								</f:verbatim>
								<h:graphicImage url="/../../library/image/silk/cancel.png" title="#{msgs.pvt_removeBcc}" alt="#{msgs.pvt_removeBcc}"/>
								<f:verbatim>
									<a href="#" onclick="fadeOutBcc(true);">
								</f:verbatim>
								<h:outputText value="#{msgs.pvt_removeBcc}"/>
								<f:verbatim>
									</a>
									&nbsp;
								</f:verbatim>
							</h:outputLabel>
						</h:panelGroup>
					</div>
					<div class="col-xs-12 col-sm-10">
						<h:panelGroup styleClass="shorttext bccLink"></h:panelGroup>
						<h:panelGroup styleClass="shorttext bcc" style="display:none">
							<h:selectManyListbox id="list2" value="#{PrivateMessagesTool.selectedComposeBccList}" size="5" style="width: 100%;" title="#{msgs.recipient_placeholder}">
								<f:selectItems value="#{PrivateMessagesTool.totalComposeToBccList}"/>
							</h:selectManyListbox>
							<f:verbatim>
								&nbsp;
							</f:verbatim>
							<h:graphicImage url="/../../library/image/silk/delete.png" title="#{msgs.pvt_bccClear}" alt="#{msgs.pvt_bccClear}"/>
							<f:verbatim>
								<a href="#" onclick="clearSelection(document.getElementById('compose:list2'));">
							</f:verbatim>
							<h:outputText value="#{msgs.pvt_bccClear}"/>
							<f:verbatim>
								</a>
							</f:verbatim>
						</h:panelGroup>
					</div>
				</div>
				<div class="row d-flex">
					<div class="col-xs-12 col-sm-2">
						<h:panelGroup styleClass="shorttext form-control-label">
							<h:outputLabel>
								<h:outputText styleClass="pvt_send_cc" value="#{msgs.pvt_send_cc}"/>
							</h:outputLabel>
						</h:panelGroup>
					</div>
					<div class="col-xs-12 col-sm-10">
						<h:panelGroup>
							<h:selectBooleanCheckbox value="#{PrivateMessagesTool.booleanEmailOut}" id="send_email_out" disabled="#{!PrivateMessagesTool.emailCopyOptional}"></h:selectBooleanCheckbox>
							<h:outputLabel for="send_email_out">
								<h:outputText value="#{msgs.pvt_send_as_email}"/>
							</h:outputLabel>
						</h:panelGroup>
					</div>
				</div>
				<div class="row d-flex">
					<div class="col-xs-12 col-sm-2 form-control-label">
						<h:panelGroup styleClass="shorttext">
							<h:outputLabel>
								<h:outputText styleClass="pvt_read_receipt" value="#{msgs.pvt_read_receipt_label}"/>
							</h:outputLabel>
						</h:panelGroup>
					</div>
					<div class="col-xs-12 col-sm-10">
						<h:panelGroup>
							<h:selectBooleanCheckbox value="#{PrivateMessagesTool.booleanReadReceipt}" id="read_receipt" ></h:selectBooleanCheckbox>
							<h:outputLabel for="read_receipt">
								<h:outputText value="#{msgs.pvt_read_receipt_text}"/>
							</h:outputLabel>
						</h:panelGroup>
					</div>
				</div>
				<div class="row">
					<div class="col-xs-12 col-sm-2">
						<h:outputLabel for="viewlist">
							<h:outputText value="#{msgs.pvt_label}" />
						</h:outputLabel>
					</div>
					<div class="col-xs-12 col-sm-10">
						<h:selectOneListbox size="1" id="viewlist" value="#{PrivateMessagesTool.selectedLabel}">
							<f:selectItem itemValue="pvt_priority_normal" itemLabel="#{msgs.pvt_priority_normal}"/>
							<f:selectItem itemValue="pvt_priority_low" itemLabel="#{msgs.pvt_priority_low}"/>
							<f:selectItem itemValue="pvt_priority_high" itemLabel="#{msgs.pvt_priority_high}"/>
						</h:selectOneListbox>
					</div>
				</div>
				<div class="row d-flex">
					<div class="col-xs-12 col-sm-2">
						<h:panelGroup styleClass="shorttext form-control-label">
							<h:outputLabel>
								<h:outputText styleClass="pvt_send_cc" value="#{msgs.pvt_scheduler_send}"/>
							</h:outputLabel>
						</h:panelGroup>
					</div>
					<div class="col-xs-12 col-sm-10">
						<h:panelGroup>
							<h:selectBooleanCheckbox value="#{PrivateMessagesTool.booleanSchedulerSend}" id="scheduler_send_email" onclick = "document.getElementById('compose:openDateSpan').classList.toggle('d-none')"></h:selectBooleanCheckbox>
							<h:outputLabel for="scheduler_send_email">
								<h:outputText value="#{msgs.pvt_scheduler_send_as_email}"/>
							</h:outputLabel>
						</h:panelGroup>
					</div>
				</div>

				<h:panelGroup id="openDateSpan" styleClass="indnt9 openDateSpan calWidget d-none" >
					<h:outputLabel value="#{msgs.pvt_scheduler_send_date} " for="openDate" />
					<h:inputText id="openDate" styleClass="openDate" value="#{PrivateMessagesTool.schedulerSendDateString}" />
				</h:panelGroup>
				<script>
					localDatePicker({
						input:'.openDate',
						allowEmptyDate:true,
						ashidden: { iso8601: 'openDateISO8601' },
						getval:'.openDate',
						useTime:1
					});
					if(document.getElementById('compose:scheduler_send_email').checked) {
						document.getElementById('compose:openDateSpan').classList.remove('d-none');
					}

				</script>
				<div class="row d-flex">
					<div class="col-xs-12 col-sm-2">
						<h:panelGroup styleClass="form-control-label required">
							<h:outputLabel for="subject">
								<h:outputText value="#{msgs.pvt_star}" styleClass="reqStar"/><h:outputText value="#{msgs.pvt_subject}" />
							</h:outputLabel>
						</h:panelGroup>
					</div>
					<div class="col-xs-12 col-sm-10">
						<h:panelGroup styleClass="shorttext">
							<h:inputText value="#{PrivateMessagesTool.composeSubject}" styleClass="form-control" id="subject" size="45">
								<f:validateLength maximum="255"/>
							</h:inputText>
						</h:panelGroup>
					</div>
				</div>
		  </div>
                  <f:verbatim><input type="hidden" id="ckeditor-autosave-context" name="ckeditor-autosave-context" value="messages_compose" /></f:verbatim>
                  <h:panelGroup rendered="#{PrivateMessagesTool.currentMsgUuid!=null}"><f:verbatim><input type="hidden" id="ckeditor-autosave-entity-id" name="ckeditor-autosave-entity-id" value="</f:verbatim><h:outputText value="#{PrivateMessagesTool.currentMsgUuid}"/><f:verbatim>"/></f:verbatim></h:panelGroup>

		  <h4><h:outputText value="#{msgs.pvt_star}" styleClass="reqStar"/><h:outputText value="#{msgs.pvt_message}" /></h4>
			<sakai:inputRichText textareaOnly="#{PrivateMessagesTool.mobileSession}" value="#{PrivateMessagesTool.composeBody}" id="pvt_message_body" rows="#{ForumTool.editorRows}" cols="132">
			</sakai:inputRichText>

<%--********************* Attachment *********************--%>
		  <h4>
	        <h:outputText value="#{msgs.pvt_att}"/>
	      </h4>

	      <sakai:doc_section>
	        	<h:outputText value="#{msgs.pvt_noatt}" rendered="#{empty PrivateMessagesTool.attachments}"/>
	      </sakai:doc_section>

	      <sakai:doc_section>
	        <sakai:button_bar>
	          <h:commandButton action="#{PrivateMessagesTool.processAddAttachmentRedirect}" value="#{msgs.cdfm_button_bar_add_attachment_redirect}"
	                                 accesskey="a" />
	        </sakai:button_bar>
	      </sakai:doc_section>
	        
	        <%--designNote: copying the redenred attribute used in the first h:column to the dataTable - is there are no attachmetns - do not render the table at all.--%>
			<h:dataTable styleClass="listHier lines nolines" cellpadding="0" cellspacing="0"
			columnClasses="attach,bogus,itemAction specialLink,bogus,bogus"  id="attmsg" width="100%" value="#{PrivateMessagesTool.attachments}" var="eachAttach"   rendered="#{!empty PrivateMessagesTool.attachments}">
		      <h:column rendered="#{!empty PrivateMessagesTool.attachments}">
				    	<f:facet name="header">

						</f:facet>
						<sakai:contentTypeMap fileType="#{eachAttach.attachment.attachmentType}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>
						<h:graphicImage id="exampleFileIcon" value="#{imagePath}" />
						</h:column>
						<h:column>
				    	<f:facet name="header">
						  <h:outputText value="#{msgs.pvt_title}"/>
						</f:facet>

						<h:outputText value="#{eachAttach.attachment.attachmentName}"/>

						</h:column>
						<h:column>

						  <h:commandLink action="#{PrivateMessagesTool.processDeleteAttach}"
							           		immediate="true" title="#{msgs.pvt_attrem}">
							  <h:outputText value="#{msgs.pvt_attrem}"/>
<%--							<f:param value="#{eachAttach.attachmentId}" name="dfmsg_current_attach"/>--%>
								<f:param value="#{eachAttach.attachment.attachmentId}" name="pvmsg_current_attach"/>
							</h:commandLink>

					</h:column>
					<h:column rendered="#{!empty PrivateMessagesTool.attachments}">
					  <f:facet name="header">
						  <h:outputText value="#{msgs.pvt_attsize}" />
						</f:facet>
						<h:outputText
								value="#{PrivateMessagesTool.getAttachmentReadableSize(eachAttach.attachment.attachmentSize)}"/>
					</h:column>
					<h:column rendered="#{!empty PrivateMessagesTool.attachments}">
					  <f:facet name="header">
		  			    <h:outputText value="#{msgs.pvt_atttype}" />
						</f:facet>
						<h:outputText value="#{eachAttach.attachment.attachmentType}"/>
					</h:column>
						<%--
					  <h:column rendered="#{!empty PrivateMessagesTool.attachments}">
							<f:facet name="header">
								<h:outputText value="#{msgs.pvt_noatt}Created by" />
							</f:facet>
							<h:outputText value="#{eachAttach.attachment.createdBy}"/>
						</h:column>
					  <h:column rendered="#{!empty PrivateMessagesTool.attachments}">
							<f:facet name="header">
								<h:outputText value="Last modified by" />
							</f:facet>
							<h:outputText value="#{eachAttach.attachment.lastModifiedBy}"/>
						</h:column>
						--%>
				</h:dataTable>

      <h:panelGroup rendered="#{PrivateMessagesTool.canUseTags}">
        <h4><h:outputText value="#{msgs.pvt_tags_header}" /></h4>
        <h:inputHidden value="#{PrivateMessagesTool.selectedTags}" id="tag_selector"></h:inputHidden>
        <sakai-tag-selector
            id="tag-selector"
            selected-temp='<h:outputText value="#{PrivateMessagesTool.selectedTags}"/>'
            collection-id='<h:outputText value="#{PrivateMessagesTool.getUserId()}"/>'
            item-id='<h:outputText value="#{PrivateMessagesTool.detailMsg.msg.id}"/>'
            site-id='<h:outputText value="#{PrivateMessagesTool.getSiteId()}"/>'
            tool='<h:outputText value="#{PrivateMessagesTool.getTagTool()}"/>'
            add-new="true"
        ></sakai-tag-selector>
      </h:panelGroup>

      <sakai:button_bar>
        <h:commandButton action="#{PrivateMessagesTool.processPvtMsgSend}" value="#{msgs.pvt_send}" accesskey="s"  styleClass="active" />
        <h:commandButton action="#{PrivateMessagesTool.processPvtMsgPreview}" value="#{msgs.pvt_preview}" accesskey="p" />
        <h:commandButton action="#{PrivateMessagesTool.processPvtMsgSaveDraft}" value="#{msgs.pvt_savedraft}" />
        <h:commandButton immediate="true" action="#{PrivateMessagesTool.processPvtMsgComposeCancel}" value="#{msgs.pvt_cancel}" accesskey="x" />
      </sakai:button_bar>

  	</div>

    </h:form>

   </sakai:view>
</f:view>
