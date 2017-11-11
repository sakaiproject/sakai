<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
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
	<sakai:view title="#{msgs.pvt_reply}">
		<link rel="stylesheet" href="/library/js/jquery/select2/4.0.0/select2.css" type="text/css" />
		<link rel="stylesheet" href="/messageforums-tool/css/messages.css" type="text/css" />
		<link rel="stylesheet" href="/library/webjars/jquery-ui/1.12.1/jquery-ui.min.css" type="text/css" />
		<script type="text/javascript">includeLatestJQuery("msgcntr");</script>
		<script type="text/javascript" src="/library/js/jquery/select2/4.0.0/select2.min.js"></script>
		<sakai:script contextBase="/messageforums-tool" path="/js/sak-10625.js"/>
		<sakai:script contextBase="/messageforums-tool" path="/js/messages.js"/>
	<h:form id="pvtMsgReply">
		<script type="text/javascript">
				function clearSelection(selectObject)
				{
					for (var i=0; i<selectObject.options.length; i++)
					{
						selectObject.options[i].selected=false;
					}
					changeSelect(selectObject);
				}
				
				function fadeInBcc(){
					$('.bccLink').fadeOut(); 
					$('.bcc').fadeIn();
					clearSelection(document.getElementById('pvtMsgReply:list2'));
					resize();
				}
				
				function fadeOutBcc(){
					$('.bccLink').fadeIn(); 
					$('.bcc').fadeOut();
					clearSelection(document.getElementById('pvtMsgReply:list2'));
					resize();
				}
				
				function resize(){
					mySetMainFrameHeight('<%=org.sakaiproject.util.Web.escapeJavascript(thisId)%>');
				}
				
				$(document).ready(function() {
				  	if(document.getElementById('pvtMsgReply:list2').selectedIndex != -1){
				  		//BCC has selected items, so show it
				  		fadeInBcc();
				  	}
				  	addTagSelector(document.getElementById('pvtMsgReply:list1'));
				  	addTagSelector(document.getElementById('pvtMsgReply:list2'));
				  	resize();
				});
		</script>

		<h:panelGroup>
          	<f:verbatim><div class="breadCrumb"><h3></f:verbatim>
				  <h:panelGroup rendered="#{PrivateMessagesTool.messagesandForums}" >
				  	<h:commandLink action="#{PrivateMessagesTool.processActionHome}" value="#{msgs.cdfm_message_forums}" title="#{msgs.cdfm_message_forums}"/>
				  	<f:verbatim><h:outputText value=" / " /></f:verbatim>
				  </h:panelGroup>
	  		      <h:commandLink action="#{PrivateMessagesTool.processActionPrivateMessages}" value="#{msgs.pvt_message_nav}" title=" #{msgs.cdfm_message_forums}"/>
	              <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
				<h:commandLink action="#{PrivateMessagesTool.processDisplayForum}" value="#{(PrivateMessagesTool.msgNavMode == 'pvt_received' || PrivateMessagesTool.msgNavMode == 'pvt_sent' || PrivateMessagesTool.msgNavMode == 'pvt_deleted' || PrivateMessagesTool.msgNavMode == 'pvt_drafts')? msgs[PrivateMessagesTool.msgNavMode]: PrivateMessagesTool.msgNavMode}" title=" #{(PrivateMessagesTool.msgNavMode == 'pvt_received' || PrivateMessagesTool.msgNavMode == 'pvt_sent' || PrivateMessagesTool.msgNavMode == 'pvt_deleted' || PrivateMessagesTool.msgNavMode == 'pvt_drafts')? msgs[PrivateMessagesTool.msgNavMode]: PrivateMessagesTool.msgNavMode}"/><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " />
				<h:commandLink action="#{PrivateMessagesTool.processDisplayMessages}" title=" #{PrivateMessagesTool.detailMsg.msg.title}">
					<h:outputText value="#{PrivateMessagesTool.detailMsg.msg.title}"/>
				</h:commandLink>
				<h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " />
				<h:outputText value="#{msgs.pvt_reply}" />
			<f:verbatim></h3></div></f:verbatim>
	</h:panelGroup>	
		
		
	<%--//designNote: gsilver:commenting this header out as redundant--%>		
      <%-- <sakai:tool_bar_message value="#{msgs.pvt_reply}" />--%> 
		<div class="container_messages">
			<div class="instruction">
 			  <h:outputText value="#{msgs.cdfm_required}"/> <h:outputText value="#{msgs.pvt_star}" styleClass="reqStarInline"/>
		  </div>
		  
		  <h:outputLink rendered="#{PrivateMessagesTool.renderPrivacyAlert}" value="#{PrivateMessagesTool.privacyAlertUrl}" target="_blank" >
		  	 <sakai:instruction_message value="#{PrivateMessagesTool.privacyAlert}"/>
		  </h:outputLink>
		  
		  <h:messages styleClass="alertMessage" id="errorMessages" rendered="#{! empty facesContext.maximumSeverity}" /> 
		  
		  <h:outputText style="display:block;" styleClass="messageConfirmation" value="#{msgs.pvt_hiddenGroupsBccMsg}" rendered="#{PrivateMessagesTool.displayHiddenGroupsMsg}" />

		  <div class="composeForm">
				<div class="row">
					<div class="col-xs-12 col-sm-2">
						<h:panelGroup styleClass="shorttext">
							<h:outputLabel for="send_to" >
								<h:outputText value="#{msgs.pvt_to}"/>
							</h:outputLabel>
						</h:panelGroup>
					</div>
					<div class="col-xs-12 col-sm-10">
						<h:panelGroup styleClass="shorttext">
							<h:outputText id="send_to" value="#{PrivateMessagesTool.detailMsg.msg.author}" />
						</h:panelGroup>
					</div>
				</div>
				<div class="row">
					<div class="col-xs-12 col-sm-2">
						<h:panelGroup styleClass="shorttext">
							<h:outputLabel for="list1" >
								<h:outputText value="#{msgs.pvt_select_addtl_recipients}"/>
							</h:outputLabel>
						</h:panelGroup>
					</div>
					<div class="col-xs-12 col-sm-10">
						<h:panelGroup styleClass="shorttext">
							<h:selectManyListbox id="list1" value="#{PrivateMessagesTool.selectedComposeToList}" size="5" style="width: 100%;" title="#{msgs.recipient_placeholder}">
								<f:selectItems value="#{PrivateMessagesTool.totalComposeToList}"/>
							</h:selectManyListbox>
							<f:verbatim>
								<span>
								&nbsp;
							</f:verbatim>
							<h:graphicImage url="/../../library/image/silk/delete.png" title="#{msgs.pvt_bccClear}" alt="#{msgs.pvt_bccClear}"/>
							<f:verbatim>
								<a href="#" onclick="clearSelection(document.getElementById('pvtMsgReply:list1'));">
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
						<h:panelGroup styleClass="shorttext bccLink">
							<h:outputLabel>
								<f:verbatim>
									&nbsp;
								</f:verbatim>
								<h:graphicImage url="/../../library/image/silk/add.png" title="#{msgs.pvt_addBcc}" alt="#{msgs.pvt_addBcc}"/>
								<f:verbatim>
									<a href="#" onclick="fadeInBcc();">
								</f:verbatim>
								<h:outputText value="#{msgs.pvt_addBcc}"/>
								<f:verbatim>
									</a>
								</f:verbatim>
							</h:outputLabel>
						</h:panelGroup>
						<h:panelGroup styleClass="shorttext bcc" style="display:none">
							<h:outputLabel for="list2">
								<f:verbatim>
									<h:outputText value="#{msgs.pvt_bcc}"/>
									<f:verbatim>
										<br>
									</f:verbatim>
									<h:graphicImage url="/../../library/image/silk/cancel.png" title="#{msgs.pvt_removeBcc}" alt="#{msgs.pvt_removeBcc}"/>
									<f:verbatim>
										<a href="#" onclick="fadeOutBcc();">
									</f:verbatim>
									<h:outputText value="#{msgs.pvt_removeBcc}"/>
									<f:verbatim>
										</a>
										&nbsp;
									</f:verbatim>
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
								<a href="#" onclick="clearSelection(document.getElementById('pvtMsgReply:list2'));">
							</f:verbatim>
							<h:outputText value="#{msgs.pvt_bccClear}"/>
							<f:verbatim>
								</a>
							</f:verbatim>
						</h:panelGroup>
					</div>
				</div>
				<div class="row">
					<div class="col-xs-12 col-sm-2">
						<h:panelGroup styleClass="shorttext" rendered= "#{PrivateMessagesTool.emailCopyOptional || PrivateMessagesTool.emailCopyAlways}">
							<h:outputLabel>
								<h:outputText value="#{msgs.pvt_send_cc}"/>
							</h:outputLabel>
						</h:panelGroup>
					</div>
					<div class="col-xs-12 col-sm-10">
						<h:panelGroup styleClass="checkbox" style="white-space: nowrap;" rendered= "#{PrivateMessagesTool.emailCopyOptional}">
							<h:selectBooleanCheckbox value="#{PrivateMessagesTool.booleanEmailOut}" id="send_email_out"></h:selectBooleanCheckbox>
							<h:outputLabel for="send_email_out">
								<h:outputText value="#{msgs.pvt_send_as_email}"/>
							</h:outputLabel>
							<h:outputText value="#{msgs.pvt_send_as_email_always}" rendered= "#{PrivateMessagesTool.emailCopyAlways}"></h:outputText>
						</h:panelGroup> 
					</div>
				</div>
				<div class="row">
					<div class="col-xs-12 col-sm-2">
						<h:panelGroup  styleClass="shorttext">
							<h:outputLabel for="viewlist">
								<h:outputText value="#{msgs.pvt_label}"/>
							</h:outputLabel>
						</h:panelGroup>
					</div>
					<div class="col-xs-12 col-sm-10">
						<h:panelGroup>
							<h:selectOneListbox size="1" id="viewlist" value="#{PrivateMessagesTool.selectedLabel}">
								<f:selectItem itemValue="pvt_priority_normal" itemLabel="#{msgs.pvt_priority_normal}"/>
								<f:selectItem itemValue="pvt_priority_low" itemLabel="#{msgs.pvt_priority_low}"/>
								<f:selectItem itemValue="pvt_priority_high" itemLabel="#{msgs.pvt_priority_high}"/>
							</h:selectOneListbox> 
						</h:panelGroup>
					</div>
				</div>
				<div class="row">
					<div class="col-xs-12 col-sm-2">
						<h:panelGroup styleClass="shorttext required">
							<h:outputLabel for="subject" >
								<h:outputText value="#{msgs.pvt_star}" styleClass="reqStar"/>
								<h:outputText value="#{msgs.pvt_subject}"  />
							</h:outputLabel>
						</h:panelGroup>
					</div>
					<div class="col-xs-12 col-sm-10">
						<h:panelGroup styleClass="shorttext">
							<h:inputText value="#{PrivateMessagesTool.replyToSubject}" id="subject" size="45" styleClass="form-control">
								<f:validateLength minimum="1" maximum="255"/>
							</h:inputText>
						</h:panelGroup>
					</div>
				</div>
		  </div>
		  
			<h4><h:outputText value="#{msgs.pvt_message}" /></h4>
	        <sakai:panel_edit>
	          <sakai:doc_section>
			  <sakai:inputRichText textareaOnly="#{PrivateMessagesTool.mobileSession}" rows="#{ForumTool.editorRows}" cols="132" id="df_compose_body" value="#{PrivateMessagesTool.replyToBody}">	 
			  </sakai:inputRichText>
	         </sakai:doc_section>    
	        </sakai:panel_edit>

<%--********************* Attachment *********************--%>	

	         <h4> <h:outputText value="#{msgs.pvt_att}"/></h4>

	          <%-- Existing Attachments 
              <h:dataTable value="#{PrivateMessagesTool.detailMsg.msg.attachments}" var="existAttach" >
					  		<h:column rendered="#{!empty PrivateMessagesTool.detailMsg.message.attachments}">
								<sakai:contentTypeMap fileType="#{existAttach.attachmentType}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>									
								<h:graphicImage id="exampleFileIcon" value="#{imagePath}" />
								<h:outputText value="#{existAttach.attachmentName}"/>
						
								</h:column>
							</h:dataTable>  
					--%>	
	        	<h:outputText value="#{msgs.pvt_noatt}" rendered="#{empty PrivateMessagesTool.allAttachments}"/>
	          <sakai:button_bar>
	          	<sakai:button_bar_item action="#{PrivateMessagesTool.processAddAttachmentRedirect}" value="#{msgs.cdfm_button_bar_add_attachment_redirect}" accesskey="a" />
	          </sakai:button_bar>
	        	        
					<h:dataTable styleClass="listHier lines nolines" id="attmsgrep" width="100%" cellpadding="0" cellspacing="0" columnClasses="bogus,itemAction specialLink,bogus,bogus"
					             rendered="#{!empty PrivateMessagesTool.allAttachments}" value="#{PrivateMessagesTool.allAttachments}" var="eachAttach" >
					  <h:column >
							<f:facet name="header">
								<h:outputText value="#{msgs.pvt_title}"/>
							</f:facet>
							<sakai:doc_section>
								<sakai:contentTypeMap fileType="#{eachAttach.attachment.attachmentType}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>									
								<h:graphicImage id="exampleFileIcon" value="#{imagePath}" />								
<%--													  <h:outputLink value="#{eachAttach.attachmentUrl}" target="_blank">
									<h:outputText value="#{eachAttach.attachmentName}"/>
								</h:outputLink>--%>
							  <h:outputLink value="#{eachAttach.url}" target="_blank">
									<h:outputText value="#{eachAttach.attachment.attachmentName}"/>
								</h:outputLink>

							</sakai:doc_section>
						</h:column>
						<h:column >
							<sakai:doc_section>
								<h:commandLink action="#{PrivateMessagesTool.processDeleteReplyAttach}" 
									immediate="true"
									onfocus="document.forms[0].onsubmit();">
									<h:outputText value="#{msgs.pvt_attrem}"/>
									<f:param value="#{eachAttach.attachment.attachmentId}" name="remsg_current_attach"/>
								</h:commandLink>
							</sakai:doc_section>
							
						</h:column>
					  <h:column>
							<f:facet name="header">
								<h:outputText value="#{msgs.pvt_attsize}" />
							</f:facet>
							<h:outputText value="#{eachAttach.attachment.attachmentSize}"/>
						</h:column>
					  <h:column >
							<f:facet name="header">
		  			    <h:outputText value="#{msgs.pvt_atttype}" />
							</f:facet>
							<h:outputText value="#{eachAttach.attachment.attachmentType}"/>
						</h:column>
						</h:dataTable>   
					 
 
      <sakai:button_bar>
        <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgReplySend}" value="#{msgs.pvt_send}" accesskey="s" styleClass="active" />
        <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgPreviewReply}" value="#{msgs.pvt_preview}" accesskey="p"  styleClass="active" />
        <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgReplySaveDraft}" value="#{msgs.pvt_savedraft }" />
        <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgCancelToDetailView}" value="#{msgs.pvt_cancel}" accesskey="x" />
      </sakai:button_bar>
      </div>
    </h:form>

  </sakai:view>
</f:view> 

