<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>
<link href='/sakai-messageforums-tool/css/msgForums.css' rel='stylesheet' type='text/css' />
<f:view>
<sakai:view>
	<h:form id="DF-1">
		<div class="header-section">
			<div class="left-header-section">
				<h3><h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_message_forums}" /> /
					<h:commandLink action="#{ForumTool.processActionDisplayForum}" value="#{ForumTool.selectedForum.forum.title}" >
						<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
					</h:commandLink> /
					<h:commandLink action="#{ForumTool.processActionDisplayTopic}" value="#{ForumTool.selectedTopic.topic.title}" >
						<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
						<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
					</h:commandLink>
				</h3>
				<sakai:instruction_message value="#{ForumTool.selectedTopic.topic.shortDescription}" />
 				<h:commandLink immediate="true" 
				  action="#{ForumTool.processDfComposeToggle}" 
 				  onmousedown="document.forms[0].onsubmit();"
				  rendered="#{ForumTool.selectedTopic.hasExtendedDesciption}" 
				  value="#{msgs.cdfm_read_full_description}">
				  <f:param value="dfViewMessage" name="redirectToProcessAction"/>
				  <f:param value="true" name="composeExpand"/>
				</h:commandLink>
				<h:inputTextarea rows="5" cols="100" 
					id="topic_extended_description" disabled="true" 
					value="#{ForumTool.selectedTopic.topic.extendedDescription}" 
					rendered="#{ForumTool.selectedTopic.readFullDesciption}"/>
				<f:verbatim><br/></f:verbatim>
				<h:commandLink immediate="true" 
					action="#{ForumTool.processDfComposeToggle}" 
					onmousedown="document.forms[0].onsubmit();"
 					value="#{msgs.cdfm_hide_full_description}" 
 					rendered="#{ForumTool.selectedTopic.readFullDesciption}">
					<f:param value="dfViewMessage" name="redirectToProcessAction"/>
				</h:commandLink>					
			</div>
			<div class="right-header-section">
				<h:outputText   value="#{msgs.cdfm_previous_topic}   "  rendered="#{!ForumTool.selectedTopic.hasPreviousTopic}" />
				<h:commandLink action="#{ForumTool.processActionDisplayPreviousTopic}" value="#{msgs.cdfm_previous_topic}   "  rendered="#{ForumTool.selectedTopic.hasPreviousTopic}" >
					<f:param value="#{ForumTool.selectedTopic.previousTopicId}" name="previousTopicId"/>
					<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
				</h:commandLink>
				<h:outputText   value="#{msgs.cdfm_next_topic}   " rendered="#{!ForumTool.selectedTopic.hasNextTopic}" />
				<h:commandLink action="#{ForumTool.processActionDisplayNextTopic}" value="#{msgs.cdfm_next_topic}   " rendered="#{ForumTool.selectedTopic.hasNextTopic}" >
					<f:param value="#{ForumTool.selectedTopic.nextTopicId}" name="nextTopicId"/>
					<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
				</h:commandLink>
			</div>
		</div>

	<br/>
    <sakai:group_box>
    	<sakai:panel_edit>
      	<sakai:doc_section>  
        </sakai:doc_section>    
      </sakai:panel_edit>
    </sakai:group_box>
		
    <sakai:group_box>
		<table width="100%" align="left" style="background-color:#DDDFE4;">
      <tr>
      	<td align="left">
          <h:outputText style="font-weight:bold"  value="Subject "/>
        </td>
        <td align="left">
          <h:outputText value="#{ForumTool.selectedMessage.message.title}" />  
        </td>  
        <td align="left">
			 		<h:commandLink action="#{ForumTool.processDisplayPreviousMsg}" rendered="#{ForumTool.selectedMessage.hasPre}" >
				 		<h:outputText value="Previous Message   " />
 				  </h:commandLink>
				  <h:outputText   value="Previous Message   "  rendered="#{!ForumTool.selectedMessage.hasPre}" />
				  <h:commandLink action="#{ForumTool.processDfDisplayNextMsg}" rendered="#{ForumTool.selectedMessage.hasNext}" >
	 				  <h:outputText value="Next Message   " />
				  </h:commandLink>
				  <h:outputText   value="Next Message   " rendered="#{!ForumTool.selectedMessage.hasNext}" />
			  </td>
      </tr>
      <tr>
        <td align="left">
          <h:outputText style="font-weight:bold"  value="Authored By "/>
        </td>
        <td align="left">
        	<h:outputText value="#{ForumTool.selectedMessage.message.author}" />  
        	<h:outputText value=" (" />  
         	<h:outputText value="#{ForumTool.selectedMessage.message.created}" />
        	<h:outputText value=")" />  
        </td>
        <td></td>
      </tr>
      <tr>
        <td align="left">
          <h:outputText style="font-weight:bold"  value="Attachments "/>
        </td>
        <td align="left">
          <%-- Attachments --%>
          <h:dataTable value="#{ForumTool.selectedMessage.message.attachments}" var="eachAttach" >
					  <h:column rendered="#{!empty ForumTool.selectedMessage.message.attachments}">
				      <h:graphicImage url="/images/excel.gif" rendered="#{eachAttach.attachmentType == 'application/vnd.ms-excel'}"/>
				      <h:graphicImage url="/images/html.gif" rendered="#{eachAttach.attachmentType == 'text/html'}"/>
				      <h:graphicImage url="/images/pdf.gif" rendered="#{eachAttach.attachmentType == 'application/pdf'}"/>
				      <h:graphicImage url="/sakai-messageforums-tool/images/ppt.gif" rendered="#{eachAttach.attachmentType == 'application/vnd.ms-powerpoint'}"/>
				      <h:graphicImage url="/images/text.gif" rendered="#{eachAttach.attachmentType == 'text/plain'}"/>
				      <h:graphicImage url="/images/word.gif" rendered="#{eachAttach.attachmentType == 'application/msword'}"/>
								  
				      <h:outputLink value="#{eachAttach.attachmentUrl}" target="_new_window">
				  	    <h:outputText value="#{eachAttach.attachmentName}"/>
					    </h:outputLink>
							
				    </h:column>
			    </h:dataTable>
        <%-- Attachments --%>
        </td>
        <td></td>
      </tr>
    </table>    
    </sakai:group_box>
		
		<br/>
    <sakai:group_box>
    	<sakai:panel_edit>
      	<sakai:doc_section>  
        </sakai:doc_section>    
      </sakai:panel_edit>
    </sakai:group_box>

    <sakai:button_bar rendered="#{!ForumTool.deleteMsg}">
	    <sakai:button_bar_item action="#{ForumTool.processDfMsgReplyMsg}" value="Reply to Message" 
	      rendered="#{ForumTool.selectedTopic.isNewResponseToResponse}"/>
	    <sakai:button_bar_item action="#{ForumTool.processDfMsgReplyTp}" value="Reply to Topic" 
		    rendered="#{ForumTool.selectedTopic.isNewResponse}"/>
<%--      <sakai:button_bar_item action="#{ForumTool.processDfMsgGrd}" value="Grade" />--%>
      <sakai:button_bar_item action="#{ForumTool.processDfMsgRvs}" value="Revise"
		    rendered="#{ForumTool.selectedTopic.isReviseAny && !ForumTool.selectedMessage.isOwn}"/>
      <sakai:button_bar_item action="#{ForumTool.processDfMsgRvs}" value="Revise"
		    rendered="#{ForumTool.selectedTopic.isReviseOwn && ForumTool.selectedMessage.isOwn}"/>
<%--      <sakai:button_bar_item action="#{ForumTool.processDfMsgMove}" value="Move" rendered="#{ForumTool.fullAccess}"/>--%>
      <sakai:button_bar_item action="#{ForumTool.processDfMsgDeleteConfirm}" value="Delete" 
      	rendered="#{ForumTool.selectedTopic.isDeleteAny && !ForumTool.selectedMessage.isOwn}"/>
      <sakai:button_bar_item action="#{ForumTool.processDfMsgDeleteConfirm}" value="Delete" 
      	rendered="#{ForumTool.selectedTopic.isDeleteOwn && ForumTool.selectedMessage.isOwn}"/>
    </sakai:button_bar>

<%--		<h:panelGroup rendered="#{ForumTool.deleteMsg && ForumTool.fullAccess}">--%>
		<h:panelGroup rendered="#{ForumTool.deleteMsg && ForumTool.selectedTopic.isDeleteOwn && ForumTool.selectedMessage.isOwn}">
			<h:outputText style="background-color:#FFF8DF;border:1px solid #B8B88A;color:#663300;font-size:x-small;margin:5px 0px 5px 0px;padding:5px 5px 5px 25px;" 
			value="! Are you sure you want to delete this message and any replies? If yes, click Delete to permanently delete this message and its replies." />
		</h:panelGroup>
		<h:panelGroup rendered="#{ForumTool.deleteMsg && ForumTool.selectedTopic.isDeleteAny && !ForumTool.selectedMessage.isOwn}">
			<h:outputText style="background-color:#FFF8DF;border:1px solid #B8B88A;color:#663300;font-size:x-small;margin:5px 0px 5px 0px;padding:5px 5px 5px 25px;" 
			value="! Are you sure you want to delete this message and any replies? If yes, click Delete to permanently delete this message and its replies." />
		</h:panelGroup>

    <sakai:button_bar rendered="#{ForumTool.deleteMsg && ForumTool.selectedTopic.isDeleteOwn && ForumTool.selectedMessage.isOwn}" > 
      <sakai:button_bar_item action="#{ForumTool.processDfMsgDeleteConfirmYes}" value="Delete" />
      <sakai:button_bar_item action="#{ForumTool.processDfMsgDeleteCancel}" value="Cancel" />
    </sakai:button_bar>
    <sakai:button_bar rendered="#{ForumTool.deleteMsg && ForumTool.selectedTopic.isDeleteAny && !ForumTool.selectedMessage.isOwn}" > 
      <sakai:button_bar_item action="#{ForumTool.processDfMsgDeleteConfirmYes}" value="Delete" />
      <sakai:button_bar_item action="#{ForumTool.processDfMsgDeleteCancel}" value="Cancel" />
    </sakai:button_bar>
    
<%--    <sakai:group_box>
      <sakai:panel_edit>
        <sakai:doc_section>            
         <h:inputTextarea value="#{ForumTool.selectedMessage.message.body}" cols="100" rows="5" />--%>
					<mf:htmlShowArea value="#{ForumTool.selectedMessage.message.body}"/>
<%--        </sakai:doc_section>    
      </sakai:panel_edit>
    </sakai:group_box>--%>
		
 	</h:form>
</sakai:view>
</f:view>
