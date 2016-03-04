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
	<sakai:view title="#{msgs.pvt_pvtcompose}">
		<link rel="stylesheet" href="/library/js/jquery/select2/4.0.0/select2.css" type="text/css" />
		<link rel="stylesheet" href="/messageforums-tool/css/messages.css" type="text/css" />
		<link rel="stylesheet" href="/library/webjars/jquery-ui/1.11.3/jquery-ui.min.css" type="text/css" />
		<script type="text/javascript">includeLatestJQuery("msgcntr");</script>
		<script type="text/javascript" src="/library/js/jquery/select2/4.0.0/select2.min.js"></script>
		<sakai:script contextBase="/messageforums-tool" path="/js/sak-10625.js"/>
		<sakai:script contextBase="/messageforums-tool" path="/js/messages.js"/>
	<h:form id="compose">
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
					clearSelection(document.getElementById('compose:list2'));
					resize();
				}

				function fadeOutBcc(){
					$('.bccLink').fadeIn();
					$('.bcc').fadeOut();
					clearSelection(document.getElementById('compose:list2'));
					resize();
				}

				function resize(){
					mySetMainFrameHeight('<%=org.sakaiproject.util.Web.escapeJavascript(thisId)%>');
				}

				$(document).ready(function() {
				  	if(document.getElementById('compose:list2').selectedIndex != -1){
				  		//BCC has selected items, so show it
				  		fadeInBcc();
				  	}
				  	addTagSelector(document.getElementById('compose:list1'));
				  	addTagSelector(document.getElementById('compose:list2'));
				  	resize();
				});
			</script>
		<!-- compose.jsp -->
  			<div class="page-header">
				<h1>
				  <h:panelGroup rendered="#{PrivateMessagesTool.messagesandForums}" >
				  	<h:commandLink action="#{PrivateMessagesTool.processActionHome}" value="#{msgs.cdfm_message_forums}" title="#{msgs.cdfm_message_forums}"/>
				  	<f:verbatim>&nbsp; / </f:verbatim>
				  </h:panelGroup>
				  <h:commandLink action="#{PrivateMessagesTool.processActionPrivateMessages}" value="#{msgs.cdfm_message_pvtarea}" title=" #{msgs.cdfm_message_pvtarea}"/> /
				  <h:commandLink action="#{PrivateMessagesTool.processDisplayForum}" value="#{(PrivateMessagesTool.msgNavMode == 'pvt_received' || PrivateMessagesTool.msgNavMode == 'pvt_sent' || PrivateMessagesTool.msgNavMode == 'pvt_deleted' || PrivateMessagesTool.msgNavMode == 'pvt_drafts')? msgs[PrivateMessagesTool.msgNavMode]: PrivateMessagesTool.msgNavMode}" title=" #{(PrivateMessagesTool.msgNavMode == 'pvt_received' || PrivateMessagesTool.msgNavMode == 'pvt_sent' || PrivateMessagesTool.msgNavMode == 'pvt_deleted' || PrivateMessagesTool.msgNavMode == 'pvt_drafts')? msgs[PrivateMessagesTool.msgNavMode]: PrivateMessagesTool.msgNavMode}"
									rendered="#{! PrivateMessagesTool.fromMain}" />
				  <h:outputText escape="false" value=" / " rendered="#{! PrivateMessagesTool.fromMain}" />
				  <h:outputText value="#{msgs.pvt_compose1}" />
				</h1>
			</div>
	
	<div class="container_messages">

			<sakai:tool_bar_message value="#{msgs.pvt_pvtcompose}" />

 			<div class="instruction">
  			  <h:outputText value="#{msgs.cdfm_required}"/> <h:outputText value="#{msgs.pvt_star}" styleClass="reqStarInline" />
			</div>

		  <h:outputLink rendered="#{PrivateMessagesTool.renderPrivacyAlert}" value="#{PrivateMessagesTool.privacyAlertUrl}" target="_blank" >
		  	  <sakai:instruction_message value="#{PrivateMessagesTool.privacyAlert}"/>
		  </h:outputLink>

		  <h:messages styleClass="alertMessage" id="errorMessages" rendered="#{! empty facesContext.maximumSeverity}" />

		  <h:outputText style="display:block;" styleClass="messageConfirmation" value="#{msgs.pvt_hiddenGroupsBccMsg}" rendered="#{PrivateMessagesTool.displayHiddenGroupsMsg}" />

		  <h:panelGrid styleClass="jsfFormTable" columns="2">
			  <h:panelGroup styleClass="shorttext required">
					 <h:outputLabel for="list1"><h:outputText value="#{msgs.pvt_star}" styleClass="reqStar"/><h:outputText value="#{msgs.pvt_to}"/></h:outputLabel>
			  </h:panelGroup>
			  <h:panelGroup styleClass="shorttext">
					<h:selectManyListbox id="list1" value="#{PrivateMessagesTool.selectedComposeToList}" size="5" style="width: 100%;">
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
		  	<h:panelGroup styleClass="shorttext bccLink">

		  	</h:panelGroup>
			<h:panelGroup styleClass="shorttext bcc" style="display:none">
				<h:outputLabel for="list2">
					<h:outputText value="#{msgs.pvt_bcc}"/>
					<f:verbatim>
		       			<br>
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

	       		</h:outputLabel>
		  	</h:panelGroup>
		  	<h:panelGroup styleClass="shorttext bcc" style="display:none">
				<h:selectManyListbox id="list2" value="#{PrivateMessagesTool.selectedComposeBccList}" size="5" style="width: 100%;">
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


			 <h:panelGroup styleClass="shorttext" rendered= "#{PrivateMessagesTool.emailCopyOptional || PrivateMessagesTool.emailCopyAlways}">
			   <h:outputLabel><h:outputText styleClass="pvt_send_cc" value="#{msgs.pvt_send_cc}"/></h:outputLabel>
			 </h:panelGroup>

			<h:panelGroup styleClass="checkbox" rendered= "#{PrivateMessagesTool.emailCopyOptional}">
			  <h:selectBooleanCheckbox value="#{PrivateMessagesTool.booleanEmailOut}" id="send_email_out"></h:selectBooleanCheckbox>
			  <h:outputLabel for="send_email_out"><h:outputText value="#{msgs.pvt_send_as_email}"/></h:outputLabel>
			</h:panelGroup>

			<h:outputText value="#{msgs.pvt_send_as_email_always}" rendered= "#{PrivateMessagesTool.emailCopyAlways}"></h:outputText>

				<h:outputLabel for="viewlist"><h:outputText value="#{msgs.pvt_label}" /></h:outputLabel>
				<h:selectOneListbox size="1" id="viewlist" value="#{PrivateMessagesTool.selectedLabel}">
            <f:selectItem itemValue="pvt_priority_normal" itemLabel="#{msgs.pvt_priority_normal}"/>
            <f:selectItem itemValue="pvt_priority_low" itemLabel="#{msgs.pvt_priority_low}"/>
            <f:selectItem itemValue="pvt_priority_high" itemLabel="#{msgs.pvt_priority_high}"/>
        </h:selectOneListbox>

				<h:panelGroup styleClass="form-label required">
				 <h:outputLabel for="subject"><h:outputText value="#{msgs.pvt_star}" styleClass="reqStar"/><h:outputText value="#{msgs.pvt_subject}" /></h:outputLabel>
				</h:panelGroup>
				<h:panelGroup styleClass="shorttext">
					<h:inputText value="#{PrivateMessagesTool.composeSubject}" styleClass="form-control" id="subject" size="45">
					  <f:validateLength minimum="1" maximum="255"/>
					</h:inputText>
				</h:panelGroup>
			</h:panelGrid>

		  <h4><h:outputText value="#{msgs.pvt_message}" /></h4>
			<sakai:inputRichText textareaOnly="#{PrivateMessagesTool.mobileSession}" value="#{PrivateMessagesTool.composeBody}" id="pvt_message_body" rows="#{ForumTool.editorRows}" cols="132">
				<f:validateLength maximum="65000"/>
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
	          <sakai:button_bar_item action="#{PrivateMessagesTool.processAddAttachmentRedirect}" value="#{msgs.cdfm_button_bar_add_attachment_redirect}"
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
							           		immediate="true"
									           onfocus="document.forms[0].onsubmit();"
									           title="#{msgs.pvt_attrem}">
							  <h:outputText value="#{msgs.pvt_attrem}"/>
<%--							<f:param value="#{eachAttach.attachmentId}" name="dfmsg_current_attach"/>--%>
								<f:param value="#{eachAttach.attachment.attachmentId}" name="pvmsg_current_attach"/>
							</h:commandLink>

					</h:column>
					<h:column rendered="#{!empty PrivateMessagesTool.attachments}">
					  <f:facet name="header">
						  <h:outputText value="#{msgs.pvt_attsize}" />
						</f:facet>
						<h:outputText value="#{eachAttach.attachment.attachmentSize}"/>
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

      <sakai:button_bar>
        <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgSend}" value="#{msgs.pvt_send}" accesskey="s"  styleClass="active" />
        <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgPreview}" value="#{msgs.pvt_preview}" accesskey="p"  styleClass="active" />
        <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgSaveDraft}" value="#{msgs.pvt_savedraft}" />
        <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgComposeCancel}" value="#{msgs.pvt_cancel}" accesskey="x" />
      </sakai:button_bar>

  	</div>

    </h:form>

   </sakai:view>
</f:view>
