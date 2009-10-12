<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>

<f:view>
	<sakai:view toolCssHref="/sakai-messageforums-tool/css/msgcntr.css">
		<h:form id="msgForum" styleClass="specialLink">
	       		<script type="text/javascript" src="/library/js/jquery.js"></script>
       		<sakai:script contextBase="/sakai-messageforums-tool" path="/js/sak-10625.js"/>
<!--jsp/discussionForum/message/dfViewMessage.jsp-->
			<script type="text/javascript">
				$(document).ready(function() {
					if ($('table.messageActions a').length==0){
						$('.messageActions').hide()
					}
					});
			</script>
		

			<%--breadcrumb and thread nav grid--%>
		  <h:panelGrid columns="2" summary="layout" width="100%" styleClass="navPanel">
				<h:panelGroup>
				<f:verbatim><h3></f:verbatim>
			      <h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_message_forums}" title=" #{msgs.cdfm_message_forums}"
			      		rendered="#{ForumTool.messagesandForums}" />
			      <h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_discussion_forums}" title=" #{msgs.cdfm_discussion_forums}"
			      		rendered="#{ForumTool.forumsTool}" />
				<f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
				<h:commandLink action="#{ForumTool.processActionDisplayForum}" value="#{ForumTool.selectedForum.forum.title}" 
                         title=" #{ForumTool.selectedForum.forum.title}" rendered="#{ForumTool.showForumLinksInNav}" >
			      <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
				</h:commandLink>
				<h:outputText value="#{ForumTool.selectedForum.forum.title}" rendered="#{!ForumTool.showForumLinksInNav}"/>
				<f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
				<h:commandLink action="#{ForumTool.processActionDisplayTopic}" value="#{ForumTool.selectedTopic.topic.title}" 
                         title=" #{ForumTool.selectedForum.forum.title}">
				    <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
				    <f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
				</h:commandLink>
			  	<f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
			  	<h:commandLink action="#{ForumTool.processActionDisplayThread}" value="#{ForumTool.selectedThreadHead.message.title}" 
                         title=" #{ForumTool.selectedThreadHead.message.title}">
				    <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
				    <f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
				    <f:param value="#{ForumTool.selectedThreadHead.message.id}" name="messageId"/>
				</h:commandLink>
				<f:verbatim></h3></f:verbatim>
			 </h:panelGroup>
			 <h:panelGroup styleClass="itemNav">
				   <h:outputText   value="#{msgs.cdfm_previous_thread}"  rendered="#{!ForumTool.selectedThreadHead.hasPreThread}" />
					 <h:commandLink action="#{ForumTool.processActionDisplayThread}" value="#{msgs.cdfm_previous_thread}"  rendered="#{ForumTool.selectedThreadHead.hasPreThread}">
						 <f:param value="#{ForumTool.selectedThreadHead.preThreadId}" name="messageId"/>
						 <f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
						 <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
					 </h:commandLink>
					 <f:verbatim><h:outputText  id="blankSpace1" value=" |  " /></f:verbatim>				
					 <h:outputText   value="#{msgs.cdfm_next_thread}" rendered="#{!ForumTool.selectedThreadHead.hasNextThread}" />
					 <h:commandLink action="#{ForumTool.processActionDisplayThread}" value="#{msgs.cdfm_next_thread}" rendered="#{ForumTool.selectedThreadHead.hasNextThread}">
						<f:param value="#{ForumTool.selectedThreadHead.nextThreadId}" name="messageId"/>
						<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
						<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
					 </h:commandLink>
			 </h:panelGroup>
		  </h:panelGrid>
			<%-- topic short description and long description --%>
			<div class="topicBloc" style="width:80%;padding:0 .5em;margin:0;">
		<p class="textPanel">
					<h:graphicImage url="/images/silk/lock.png" alt="#{msgs.cdfm_forum_locked}" 
						 rendered="#{ForumTool.selectedTopic.locked =='true'}" style="margin-right:.5em"/>
		  <h:outputText value="#{ForumTool.selectedTopic.topic.shortDescription}" />
		  </p>
				<%-- link to open and close long desc. --%>
				<p>
			<h:commandLink immediate="true" 
		                   action="#{ForumTool.processDfComposeToggle}" 
				           onmousedown="document.forms[0].onsubmit();"
			               rendered="#{ForumTool.selectedTopic.hasExtendedDesciption}" 
							title="#{msgs.cdfm_full_description}"
							styleClass="show">
			  <f:param value="dfViewMessage" name="redirectToProcessAction"/>
			  <f:param value="true" name="composeExpand"/>
						<h:graphicImage url="/images/collapse.gif" />
						<h:outputText value="#{msgs.cdfm_read_full_description}" />
		  </h:commandLink>
		  <h:commandLink immediate="true" 
				  action="#{ForumTool.processDfComposeToggle}" 
				  onmousedown="document.forms[0].onsubmit();"
					rendered="#{ForumTool.selectedTopic.readFullDesciption}"
							title="#{msgs.cdfm_full_description}"
							styleClass="hide">
				<f:param value="dfViewMessage" name="redirectToProcessAction"/>
						<h:graphicImage url="/images/expand.gif"/>
						<h:outputText value="#{msgs.cdfm_hide_full_description}" />
		  </h:commandLink>					
		  </p>
		  <mf:htmlShowArea value="#{ForumTool.selectedTopic.topic.extendedDescription}" 
		                   rendered="#{ForumTool.selectedTopic.readFullDesciption}" 
					hideBorder="true"/>
			</div>
	  <h:messages globalOnly="true" infoClass="success" errorClass="alertMessage" />
	

			<h:panelGrid columns="2" 
					width="100%" 
					summary="layout" 
					columnClasses="specialLink, specialLink otherOtherActions"
					cellpadding="0" cellspacing="0"
					rendered="#{!ForumTool.deleteMsg && !ForumTool.selectedMessage.message.deleted}" 
					styleClass="messageActions"
					style="margin:1em 0 0 0">
				<h:panelGroup  style="display:block">
					<h:commandLink title="#{msgs.cdfm_button_bar_reply_to_msg}" action="#{ForumTool.processDfMsgReplyMsg}" 
		  				rendered="#{ForumTool.selectedTopic.isNewResponseToResponse && ForumTool.selectedMessage.msgApproved && !ForumTool.selectedTopic.locked}">
		  				<h:graphicImage value="/../../library/image/silk/email_go.png" alt="#{msgs.cdfm_button_bar_reply_to_msg}" rendered="#{ForumTool.selectedTopic.isNewResponseToResponse}" />
		  				<h:outputText value=" #{msgs.cdfm_button_bar_reply_to_msg}" />
		  			</h:commandLink>
      			
		      		<h:commandLink title="#{msgs.cdfm_button_bar_reply_to_thread}" action="#{ForumTool.processDfMsgReplyThread}" 
			  			rendered="#{ForumTool.selectedTopic.isNewResponse && ForumTool.selectedThreadHead.msgApproved && !ForumTool.selectedTopic.locked}">
		  				<h:graphicImage value="/../../library/image/silk/folder_go.png" alt="#{msgs.cdfm_button_bar_reply_to_thread}" />
		  				<h:outputText value=" #{msgs.cdfm_button_bar_reply_to_thread}" />
		  			</h:commandLink>
		  		
			  		<h:commandLink title="#{msgs.cdfm_button_bar_delete_msg}" action="#{ForumTool.processDfMsgDeleteConfirm}" rendered="#{ForumTool.selectedMessage.userCanDelete}" >
		  				<h:graphicImage value="/../../library/image/silk/email_delete.png" alt="#{msgs.cdfm_button_bar_delete_msg}" />
		  				<h:outputText value=" #{msgs.cdfm_button_bar_delete_msg}" />
		  			</h:commandLink>
		  		
			  		<h:commandLink title="#{msgs.cdfm_button_bar_revise}" action="#{ForumTool.processDfMsgRvs}" 
			  			rendered="#{ForumTool.selectedMessage.revise}">
			  			<h:graphicImage value="/../../library/image/silk/email_edit.png" alt="#{msgs.cdfm_button_bar_revise}" />
		  				<h:outputText value=" #{msgs.cdfm_button_bar_revise}" />
		  			</h:commandLink>
		  		
			  		<h:commandLink title="#{msgs.cdfm_button_bar_grade}" action="#{ForumTool.processDfMsgGrd}" 
			  			rendered="#{ForumTool.selectedTopic.isPostToGradebook && ForumTool.gradebookExist}">
			  			<h:graphicImage value="/../../library/image/silk/award_star_gold_1.png" alt="#{msgs.cdfm_button_bar_grade}" />
		  				<h:outputText value=" #{msgs.cdfm_button_bar_grade}" />
		  			</h:commandLink>
			  	</h:panelGroup>
				<h:panelGroup  style="display:block;white-space:nowrap;">
			  		<h:commandLink title="#{msgs.cdfm_button_bar_deny}" action="#{ForumTool.processDfMsgDeny}" 
		  							rendered="#{ForumTool.allowedToDenyMsg}">
			  			<h:graphicImage value="/../../library/image/silk/cross.png" alt="#{msgs.cdfm_button_bar_deny}" />
			  			<h:outputText value=" #{msgs.cdfm_button_bar_deny}" />
			  		</h:commandLink>
		  			<h:commandLink title="#{msgs.cdfm_button_bar_add_comment}" action="#{ForumTool.processDfMsgAddComment}" 
		  				rendered="#{ForumTool.allowedToApproveMsg && ForumTool.selectedMessage.msgDenied}">
			  			<h:graphicImage value="/../../library/image/silk/comment.png" alt="#{msgs.cdfm_button_bar_add_comment}" />
			  			<h:outputText value=" #{msgs.cdfm_button_bar_add_comment}" />
			  		</h:commandLink>
		  			<h:commandLink title="#{msgs.cdfm_button_bar_approve}" action="#{ForumTool.processDfMsgApprove}" 
		  				rendered="#{ForumTool.allowedToApproveMsg}">
		  				<h:graphicImage value="/../../library/image/silk/tick.png" alt="#{msgs.cdfm_button_bar_approve}" />
			  			<h:outputText value=" #{msgs.cdfm_button_bar_approve}" />
			  		</h:commandLink>
			  	</h:panelGroup>
		  	</h:panelGrid>

			<f:verbatim><div class="singleMessage">
			</f:verbatim>
				<%--title, metadata and navigation --%>
				<h:panelGrid columns="2"  style="width: 100%;" border="0">
					<h:outputText rendered="#{ForumTool.selectedMessage.message.deleted}"  value="#{msgs.cdfm_msg_deleted_label}" styleClass="instruction"/>
					<h:panelGroup rendered="#{!ForumTool.selectedMessage.message.deleted}" style="display:block">
						<h:outputText rendered="#{ ForumTool.selectedMessage.msgDenied}" value="#{msgs.cdfm_msg_denied_label}" styleClass="messageDenied"/>
						<h:outputText 	rendered="#{ForumTool.allowedToApproveMsg && ForumTool.allowedToDenyMsg}" value="#{msgs.cdfm_msg_pending_label}" styleClass="messagePending"/>
						<h:outputText value="#{ForumTool.selectedMessage.message.title}"  styleClass="title" />
						<h:outputText value="<br />" escape="false" />
						<h:outputText value="#{ForumTool.selectedMessage.message.author}" styleClass="textPanelFooter"/>
						<h:outputText value=" #{msgs.cdfm_openb} "  styleClass="textPanelFooter" />
						<h:outputText value="#{ForumTool.selectedMessage.message.created}"  styleClass="textPanelFooter" >
							<f:convertDateTime pattern="#{msgs.date_format}" />  
						</h:outputText>
						<h:outputText value=" #{msgs.cdfm_closeb}"  styleClass="textPanelFooter" />
 		</h:panelGroup>
					<%--navigation cell --%>
					<h:panelGroup styleClass="itemNav">
    		<h:commandLink action="#{ForumTool.processDisplayPreviousMsg}" rendered="#{ForumTool.selectedMessage.hasPre}" 
                       title=" #{msgs.cdfm_prev_msg}">
			      <h:outputText value="#{msgs.cdfm_prev_msg}" />
 			    </h:commandLink>
			    <h:outputText value="#{msgs.cdfm_prev_msg}"  rendered="#{!ForumTool.selectedMessage.hasPre}" />
			    <f:verbatim><h:outputText value=" | " /></f:verbatim>
			    <h:commandLink action="#{ForumTool.processDfDisplayNextMsg}" rendered="#{ForumTool.selectedMessage.hasNext}" 
			                   title=" #{msgs.cdfm_next_msg}">
	 		      <h:outputText value="#{msgs.cdfm_next_msg}" />
			    </h:commandLink>
			    <h:outputText value="#{msgs.cdfm_next_msg}" rendered="#{!ForumTool.selectedMessage.hasNext}" />
    	</h:panelGroup>
				</h:panelGrid>
				<f:verbatim><div class="textPanel"></f:verbatim>
					<h:outputText escape="false" value="#{ForumTool.selectedMessage.message.body}" 
							rendered="#{!ForumTool.selectedMessage.message.deleted}" />
				<f:verbatim></div></f:verbatim>
				<h:dataTable value="#{ForumTool.selectedMessage.attachList}" var="eachAttach"  cellpadding="3" cellspacing="0" columnClasses="attach,bogus" summary="layout"  style="font-size:.9em;width:auto;margin-left:1em" border="0">
			  		<h:column rendered="#{!empty ForumTool.selectedMessage.message.attachments}">
				      	<sakai:contentTypeMap fileType="#{eachAttach.attachment.attachmentType}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>									
						<h:graphicImage id="exampleFileIcon" value="#{imagePath}" />	
<%--							<h:outputLink value="#{eachAttach.attachmentUrl}" target="_blank">
							 	<h:outputText value="#{eachAttach.attachmentName}"/>
							  </h:outputLink>--%>
						<h:outputText value=" "/>
							<h:outputLink value="#{eachAttach.url}" target="_blank">
							 	<h:outputText value="#{eachAttach.attachment.attachmentName}"/>
						  </h:outputLink>
					  </h:column>
					</h:dataTable>
		<f:verbatim></div></f:verbatim>
			
	<h:panelGroup rendered="#{ForumTool.deleteMsg && ForumTool.errorSynch}">
		<h:outputText styleClass="alertMessage" 
		value="#{msgs.cdfm_msg_del_has_reply}" />
	</h:panelGroup>
	
	<%-- If deleting, tells where to go back to --%>	
	<h:inputHidden value="#{ForumTool.fromPage}" />
	
    <sakai:button_bar rendered="#{ForumTool.deleteMsg}" > 
      <sakai:button_bar_item action="#{ForumTool.processDfMsgDeleteConfirmYes}" value="#{msgs.cdfm_button_bar_delete}" accesskey="x" />
      <sakai:button_bar_item action="#{ForumTool.processDfMsgDeleteCancel}" value="#{msgs.cdfm_button_bar_cancel}" accesskey="c" />
    </sakai:button_bar>

 	</h:form>
</sakai:view>
</f:view>
