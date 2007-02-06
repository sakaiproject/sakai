<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>

<f:view>
<sakai:view>
	<h:form id="msgForum">
<!--jsp/discussionForum/message/dfViewMessage.jsp-->
		  <h:panelGrid columns="2" summary="layout" width="100%" styleClass="navPanel">
		    <h:panelGroup styleClass="breadCrumb">
				<f:verbatim><h3></f:verbatim>
			      <h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_message_forums}" title=" #{msgs.cdfm_message_forums}"
			      		rendered="#{ForumTool.messagesandForums}" />
			      <h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_discussion_forums}" title=" #{msgs.cdfm_discussion_forums}"
			      		rendered="#{ForumTool.forumsTool}" />
				<f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
				<h:commandLink action="#{ForumTool.processActionDisplayForum}" value="#{ForumTool.selectedForum.forum.title}" 
                         title=" #{ForumTool.selectedForum.forum.title}">
			      <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
			  </h:commandLink>
          <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
          <h:commandLink action="#{ForumTool.processActionDisplayTopic}" value="#{ForumTool.selectedTopic.topic.title}" 
                         title=" #{ForumTool.selectedForum.forum.title}">
				    <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
				    <f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
				  </h:commandLink>
				  <f:verbatim></h3></f:verbatim>
			 </h:panelGroup>
			 <h:panelGroup styleClass="itemNav">
			   <h:outputText   value="#{msgs.cdfm_previous_topic}   "  rendered="#{!ForumTool.selectedTopic.hasPreviousTopic}" />
				 <h:commandLink action="#{ForumTool.processActionDisplayPreviousTopic}" value="#{msgs.cdfm_previous_topic}   "  
				                rendered="#{ForumTool.selectedTopic.hasPreviousTopic}" title=" #{msgs.cdfm_previous_topic}">
					<f:param value="#{ForumTool.selectedTopic.previousTopicId}" name="previousTopicId"/>
					<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
				 </h:commandLink>
				 <f:verbatim><h:outputText value=" | " /></f:verbatim>
				 <h:outputText   value="#{msgs.cdfm_next_topic}   " rendered="#{!ForumTool.selectedTopic.hasNextTopic}" />
				 <h:commandLink action="#{ForumTool.processActionDisplayNextTopic}" value="#{msgs.cdfm_next_topic}   " 
				                rendered="#{ForumTool.selectedTopic.hasNextTopic}" title=" #{msgs.cdfm_next_topic}">
					<f:param value="#{ForumTool.selectedTopic.nextTopicId}" name="nextTopicId"/>
					<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
				 </h:commandLink>
			 </h:panelGroup>
		  </h:panelGrid>
		<p class="textPanel">
		  <h:outputText value="#{ForumTool.selectedTopic.topic.shortDescription}" />
		  </p>
		<p class="textPanelFooter">
			<h:commandLink immediate="true" 
		                   action="#{ForumTool.processDfComposeToggle}" 
				               onmousedown="document.forms[0].onsubmit();"
			                 rendered="#{ForumTool.selectedTopic.hasExtendedDesciption}" 
			                 value="#{msgs.cdfm_read_full_description}"
			                 title="#{msgs.cdfm_read_full_description}">
			  <f:param value="dfViewMessage" name="redirectToProcessAction"/>
			  <f:param value="true" name="composeExpand"/>
		  </h:commandLink>
		  <h:commandLink immediate="true" 
				  action="#{ForumTool.processDfComposeToggle}" 
				  onmousedown="document.forms[0].onsubmit();"
					value="#{msgs.cdfm_hide_full_description}" 
					rendered="#{ForumTool.selectedTopic.readFullDesciption}"
					title="#{msgs.cdfm_hide_full_description}">
				<f:param value="dfViewMessage" name="redirectToProcessAction"/>
		  </h:commandLink>					
		  </p>
		   
		  <mf:htmlShowArea value="#{ForumTool.selectedTopic.topic.extendedDescription}" 
		                   rendered="#{ForumTool.selectedTopic.readFullDesciption}" 
		                   hideBorder="false" />
			
		<sakai:button_bar rendered="#{!ForumTool.deleteMsg}">
	    <sakai:button_bar_item action="#{ForumTool.processDfMsgReplyMsg}" value="#{msgs.cdfm_button_bar_reply_to_msg}" 
	                           rendered="#{ForumTool.selectedTopic.isNewResponseToResponse}" />
	    <sakai:button_bar_item action="#{ForumTool.processDfMsgReplyTp}" value="#{msgs.cdfm_button_bar_reply_to_topic}" 
		                         rendered="#{ForumTool.selectedTopic.isNewResponse}" />
      <sakai:button_bar_item action="#{ForumTool.processDfMsgGrd}" value="#{msgs.cdfm_button_bar_grade}" 
                             rendered="#{ForumTool.selectedTopic.isPostToGradebook && ForumTool.gradebookExist}"  /> 
      <sakai:button_bar_item action="#{ForumTool.processDfMsgRvs}" value="#{msgs.cdfm_button_bar_revise}"
		                         rendered="#{ForumTool.selectedTopic.isReviseAny}" accesskey="r" />
      <sakai:button_bar_item action="#{ForumTool.processDfMsgRvs}" value="#{msgs.cdfm_button_bar_revise}"
		    rendered="#{!ForumTool.selectedTopic.isReviseAny && 
		    	ForumTool.selectedTopic.isReviseOwn && ForumTool.selectedMessage.isOwn}" accesskey="r" />
<%--      <sakai:button_bar_item action="#{ForumTool.processDfMsgMove}" value="Move" rendered="#{ForumTool.fullAccess}"/>--%>
<%--      <sakai:button_bar_item action="#{ForumTool.processDfMsgDeleteConfirm}" value="Delete" 
      	rendered="#{ForumTool.selectedTopic.isDeleteAny}"/>
      <sakai:button_bar_item action="#{ForumTool.processDfMsgDeleteConfirm}" value="Delete" 
      	rendered="#{!ForumTool.selectedTopic.isDeleteAny && 
      		ForumTool.selectedTopic.isDeleteOwn && ForumTool.selectedMessage.isOwn
      		&& !ForumTool.selectedMessage.hasChild}"/>
      <sakai:button_bar_item action="#{ForumTool.processDfMsgDeleteConfirm}" 
        value="Delete Unavailable" disabled="true"
      	rendered="#{!ForumTool.selectedTopic.isDeleteAny && 
      		ForumTool.selectedTopic.isDeleteOwn && ForumTool.selectedMessage.isOwn
      		&& ForumTool.selectedMessage.hasChild}"/>--%>
    </sakai:button_bar>
    
    <h:panelGrid columns="3" styleClass="itemSummary" style="width: 100%;">
    	<h:outputText value="#{msgs.cdfm_subject}"/>
    	<h:outputText value="#{ForumTool.selectedMessage.message.title}"/>
    	<h:panelGroup styleClass="msgNav">
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
    	
    	<h:outputText value="#{msgs.cdfm_authoredby}"/>
    	<h:outputText value="#{ForumTool.selectedMessage.message.author} #{msgs.cdfm_openb} #{ForumTool.selectedMessage.message.created} #{msgs.cdfm_closeb}" />  
      <h:outputText value="" />
      
      <h:outputText value="#{msgs.cdfm_att}" rendered="#{!empty ForumTool.selectedMessage.attachList}" />
      <h:panelGroup rendered="#{!empty ForumTool.selectedMessage.attachList}">
      	<h:dataTable value="#{ForumTool.selectedMessage.attachList}" var="eachAttach"  styleClass="attachListJSF"  rendered="#{!empty ForumTool.selectedMessage.attachList}">
			  		<h:column rendered="#{!empty ForumTool.selectedMessage.message.attachments}">
				      <h:graphicImage url="/images/excel.gif" rendered="#{eachAttach.attachment.attachmentType == 'application/vnd.ms-excel'}" alt="" />
						  <h:graphicImage url="/images/html.gif" rendered="#{eachAttach.attachment.attachmentType == 'text/html'}" alt="" />
							<h:graphicImage url="/images/pdf.gif" rendered="#{eachAttach.attachment.attachmentType == 'application/pdf'}" alt="" />
							<h:graphicImage url="/sakai-messageforums-tool/images/ppt.gif" rendered="#{eachAttach.attachment.attachmentType == 'application/vnd.ms-powerpoint'}" alt="" />
							<h:graphicImage url="/images/text.gif" rendered="#{eachAttach.attachment.attachmentType == 'text/plain'}" alt="" />
							<h:graphicImage url="/images/word.gif" rendered="#{eachAttach.attachment.attachmentType == 'application/msword'}" alt="" />
								  
<%--							<h:outputLink value="#{eachAttach.attachmentUrl}" target="_blank">
							 	<h:outputText value="#{eachAttach.attachmentName}"/>
							  </h:outputLink>--%>
							<h:outputLink value="#{eachAttach.url}" target="_blank">
							 	<h:outputText value="#{eachAttach.attachment.attachmentName}"/>
						  </h:outputLink>
							  
					  </h:column>

					</h:dataTable>
      </h:panelGroup>
      <h:outputText value="" rendered="#{!empty ForumTool.selectedMessage.attachList}" />
      
      
    </h:panelGrid>
    
		
		<hr class="itemSeparator" />
		
    
			<mf:htmlShowArea value="#{ForumTool.selectedMessage.message.body}" hideBorder="true" />
			
    <sakai:button_bar rendered="#{!ForumTool.deleteMsg}">
	    <sakai:button_bar_item action="#{ForumTool.processDfMsgReplyMsg}" value="#{msgs.cdfm_button_bar_reply_to_msg}" 
	                           rendered="#{ForumTool.selectedTopic.isNewResponseToResponse}" />
	    <sakai:button_bar_item action="#{ForumTool.processDfMsgReplyTp}" value="#{msgs.cdfm_button_bar_reply_to_topic}" 
		                         rendered="#{ForumTool.selectedTopic.isNewResponse}" />
      <sakai:button_bar_item action="#{ForumTool.processDfMsgGrd}" value="#{msgs.cdfm_button_bar_grade}" 
                             rendered="#{ForumTool.selectedTopic.isPostToGradebook && ForumTool.gradebookExist}"  /> 
      <sakai:button_bar_item action="#{ForumTool.processDfMsgRvs}" value="#{msgs.cdfm_button_bar_revise}"
		                         rendered="#{ForumTool.selectedTopic.isReviseAny}" accesskey="r" />
      <sakai:button_bar_item action="#{ForumTool.processDfMsgRvs}" value="#{msgs.cdfm_button_bar_revise}"
		    rendered="#{!ForumTool.selectedTopic.isReviseAny && 
		    	ForumTool.selectedTopic.isReviseOwn && ForumTool.selectedMessage.isOwn}" accesskey="r" />
<%--      <sakai:button_bar_item action="#{ForumTool.processDfMsgMove}" value="Move" rendered="#{ForumTool.fullAccess}"/>--%>
<%--      <sakai:button_bar_item action="#{ForumTool.processDfMsgDeleteConfirm}" value="Delete" 
      	rendered="#{ForumTool.selectedTopic.isDeleteAny}"/>
      <sakai:button_bar_item action="#{ForumTool.processDfMsgDeleteConfirm}" value="Delete" 
      	rendered="#{!ForumTool.selectedTopic.isDeleteAny && 
      		ForumTool.selectedTopic.isDeleteOwn && ForumTool.selectedMessage.isOwn
      		&& !ForumTool.selectedMessage.hasChild}"/>
      <sakai:button_bar_item action="#{ForumTool.processDfMsgDeleteConfirm}" 
        value="Delete Unavailable" disabled="true"
      	rendered="#{!ForumTool.selectedTopic.isDeleteAny && 
      		ForumTool.selectedTopic.isDeleteOwn && ForumTool.selectedMessage.isOwn
      		&& ForumTool.selectedMessage.hasChild}"/>--%>
    </sakai:button_bar>

		  <h:messages styleClass="alertMessage" id="errorMessages"  />
<%--		<h:panelGroup rendered="#{ForumTool.deleteMsg && ForumTool.fullAccess}">--%>
		<h:panelGroup rendered="#{!ForumTool.errorSynch && ForumTool.deleteMsg && ForumTool.selectedTopic.isDeleteOwn && ForumTool.selectedMessage.isOwn}">
			<h:outputText styleClass="alertMessage" 
			value="#{msgs.cdfm_delete_msg}" />
		</h:panelGroup>
		<h:panelGroup rendered="#{!ForumTool.errorSynch && ForumTool.deleteMsg && ForumTool.selectedTopic.isDeleteAny && !ForumTool.selectedMessage.isOwn}">
			<h:outputText styleClass="alertMessage" 
			value="#{msgs.cdfm_delete_msg}" />
		</h:panelGroup>
		<h:panelGroup rendered="#{ForumTool.deleteMsg && ForumTool.errorSynch}">
			<h:outputText styleClass="alertMessage" 
			value="#{msgs.cdfm_msg_del_has_reply}" />
		</h:panelGroup>
		
    <sakai:button_bar rendered="#{ForumTool.deleteMsg && ForumTool.selectedTopic.isDeleteOwn && ForumTool.selectedMessage.isOwn}" > 
      <sakai:button_bar_item action="#{ForumTool.processDfMsgDeleteConfirmYes}" value="#{msgs.cdfm_button_bar_delete}" accesskey="x"/>
      <sakai:button_bar_item action="#{ForumTool.processDfMsgDeleteCancel}" value="#{msgs.cdfm_button_bar_cancel}" accesskey="c"/>
    </sakai:button_bar>
    <sakai:button_bar rendered="#{ForumTool.deleteMsg && ForumTool.selectedTopic.isDeleteAny && !ForumTool.selectedMessage.isOwn}"> 
      <sakai:button_bar_item action="#{ForumTool.processDfMsgDeleteConfirmYes}" value="#{msgs.cdfm_button_bar_delete}" accesskey="x"/>
      <sakai:button_bar_item action="#{ForumTool.processDfMsgDeleteCancel}" value="#{msgs.cdfm_button_bar_cancel}" accesskey="c"/>
    </sakai:button_bar>
    
<%--    <sakai:group_box>
      <sakai:panel_edit>
        <sakai:doc_section>            
         <h:inputTextarea value="#{ForumTool.selectedMessage.message.body}" cols="100" rows="5" />--%>
			
<%--        </sakai:doc_section>    
      </sakai:panel_edit>
    </sakai:group_box>--%>
		
 	</h:form>
</sakai:view>
</f:view>
