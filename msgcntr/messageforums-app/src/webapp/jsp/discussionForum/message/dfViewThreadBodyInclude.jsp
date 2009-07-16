<%-- Display single message in threaded view. (included for each message). --%>

<h:outputText escape="false" value="<a id=\"#{message.message.id}\" name=\"#{message.message.id}\"></a>" />
	<f:verbatim><div class="hierItemBlock"></f:verbatim>
	<f:verbatim><h4 class="textPanelHeader"></f:verbatim>
	<f:verbatim><div class="specialLink" style="width:65%;float:left;text-align:left"></f:verbatim>

	<h:panelGroup styleClass="inactive" rendered="#{message.deleted}" >
		<f:verbatim><span></f:verbatim>
			<h:outputText value="#{msgs.cdfm_msg_deleted_label}" />
		<f:verbatim></span></f:verbatim>
	</h:panelGroup>

	<h:panelGroup rendered="#{!message.deleted}">
		<h:outputText value="#{msgs.cdfm_msg_pending_label} " styleClass="highlight" rendered="#{message.msgPending}" />
		<h:outputText value="#{msgs.cdfm_msg_denied_label} " rendered="#{message.msgDenied}" />

		<h:commandLink action="#{ForumTool.processActionDisplayMessage}"immediate="true" title=" #{message.message.title}">
			<h:outputText value="#{message.message.title}" rendered="#{message.read}" />
			<h:outputText styleClass="unreadMsg" value="#{message.message.title}" rendered="#{!message.read}" />

			<f:param value="#{message.message.id}" name="messageId" />
			<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId" />
			<f:param value="#{ForumTool.selectedTopic.topic.baseForum.id}" name="forumId" />
		</h:commandLink>

		<h:outputText value=" - #{message.message.author}" rendered="#{message.read}" />
		<h:outputText styleClass="unreadMsg" value=" - #{message.message.author}" rendered="#{!message.read }" />

		<h:outputText value="#{message.message.created}" rendered="#{message.read}">
			<f:convertDateTime pattern="#{msgs.date_format_paren}" />
		</h:outputText>
		<h:outputText styleClass="unreadMsg" value="#{message.message.created}" rendered="#{!message.read}">
			<f:convertDateTime pattern="#{msgs.date_format_paren}" />
		</h:outputText>

		<h:graphicImage value="/../../library/image/silk/email.png" 
			alt="#{msgs.cdfm_mark_as_read}" 
			title="#{msgs.cdfm_mark_as_read}" 
			rendered="#{!message.read}"
			style="cursor:pointer"
			onclick="doAjax(#{message.message.id}, #{ForumTool.selectedTopic.topic.id}, this);"
			onmouseover="this.src=this.src.replace(/email\.png/, 'email_open.png');"
			onmouseout="this.src=this.src.replace(/email_open\.png/, 'email.png');" />
	
		<h:outputText value="<br />" escape="false" rendered="#{!empty message.attachList}" />
		<h:panelGroup rendered="#{!empty message.attachList}">
			<h:dataTable value="#{message.attachList}" var="eachAttach" styleClass="attachListJSF" rendered="#{!empty message.attachList}">
				<h:column rendered="#{!empty message.message.attachments}">
					<sakai:contentTypeMap fileType="#{eachAttach.attachment.attachmentType}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>									
					<h:graphicImage id="exampleFileIcon" value="#{imagePath}" />					
					<h:outputText escape="false" value="<a target='new_window' href='" />
					<h:outputText value="#{eachAttach.url}" />
					<h:outputText escape="false" value="'>#{eachAttach.attachment.attachmentName}</a>" />
				</h:column>
			</h:dataTable>
		</h:panelGroup>
	
		<f:verbatim></div></f:verbatim>
		<f:verbatim><div style="width:30%;float:right;text-align:right"	class="specialLink"></f:verbatim>
	</h:panelGroup>
	
	<%-- If message actually deleted, don't display links --%>
	<h:panelGroup rendered="#{!message.deleted}" >

		<%-- Reply link --%>
		<h:panelGroup rendered="#{ForumTool.selectedTopic.isNewResponseToResponse && message.msgApproved && !ForumTool.selectedTopic.locked}">
			<h:commandLink action="#{ForumTool.processDfMsgReplyMsgFromEntire}" title="#{msgs.cdfm_reply}"> 
				<h:graphicImage value="/../../library/image/silk/email_go.png" alt="#{msgs.cdfm_button_bar_reply_to_msg}" />
				<h:outputText value="#{msgs.cdfm_reply}" />

				<f:param value="#{message.message.id}" name="messageId" />
				<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId" />
				<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId" />
			</h:commandLink>
		</h:panelGroup>

		<%-- (Hide) Other Actions links --%>
		<h:panelGroup rendered="#{(ForumTool.selectedTopic.isPostToGradebook && ForumTool.gradebookExist) || ForumTool.selectedTopic.isModeratedAndHasPerm || message.revise 
									|| message.userCanDelete}">
			<h:outputText value=" #{msgs.cdfm_toolbar_separator} " rendered="#{ForumTool.selectedTopic.isNewResponseToResponse && message.msgApproved}" />
			<h:outputLink value="#" onclick="toggleDisplay('#{message.message.id}_advanced_box'); toggleHide(this); return false;">
				<h:graphicImage value="/../../library/image/silk/cog.png" alt="#{msgs.cdfm_other_actions}" />
				<h:outputText value="#{msgs.cdfm_other_actions}" />
			</h:outputLink>
		</h:panelGroup>
	</h:panelGroup>
	
	<h:outputText escape="false" value="<div id=\"#{message.message.id}_advanced_box\" style=\"display:none\">" />

	<h:panelGroup rendered="#{ForumTool.selectedTopic.isPostToGradebook && ForumTool.gradebookExist}">
		<h:commandLink action="#{ForumTool.processDfMsgGrdFromThread}" value="#{msgs.cdfm_button_bar_grade}">
			<f:param value="#{message.message.id}" name="messageId" />
			<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId" />
			<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId" />
		</h:commandLink>
		<h:outputText value=" #{msgs.cdfm_toolbar_separator} " />
	</h:panelGroup>

	<%-- Revise other action --%>
	<h:panelGroup rendered="#{message.revise}">
		<h:commandLink action="#{ForumTool.processDfMsgRvsFromThread}" value="#{msgs.cdfm_button_bar_revise}">
			<f:param value="#{message.message.id}" name="messageId" />
			<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId" />
			<f:param value="#{ForumTool.selectedTopic.topic.baseForum.id}" name="forumId" />
		</h:commandLink>
		<h:outputText value=" #{msgs.cdfm_toolbar_separator} " />
	</h:panelGroup>

	<%-- Delete other action --%>
	<h:panelGroup rendered="#{message.userCanDelete}" >
		<h:commandLink action="#{ForumTool.processDfMsgDeleteConfirm}" value="#{msgs.cdfm_button_bar_delete}">
			<f:param value="#{message.message.id}" name="messageId" />
			<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId" />
			<f:param value="#{ForumTool.selectedTopic.topic.baseForum.id}" name="forumId" />
			<f:param value="dfViewThread" name="fromPage" />
		</h:commandLink>
		<h:outputText value=" #{msgs.cdfm_toolbar_separator} " rendered="#{ForumTool.selectedTopic.isModeratedAndHasPerm}" />
	</h:panelGroup>

	<%-- Moderate other action --%>
	<h:panelGroup rendered="#{ForumTool.selectedTopic.isModeratedAndHasPerm}">
		<h:commandLink action="#{ForumTool.processActionDisplayMessage}" immediate="true" title=" #{msgs.cdfm_moderate}">
			<h:outputText value="#{msgs.cdfm_moderate}" />
			<f:param value="#{message.message.id}" name="messageId" />
			<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId" />
			<f:param value="#{ForumTool.selectedTopic.topic.baseForum.id}" name="forumId" />
		</h:commandLink>
	</h:panelGroup>

	<h:outputText escape="false" value="</div>" />
	<f:verbatim></div></f:verbatim>

	<f:verbatim><div style="clear:both;height:.1em;width:100%;"></div></f:verbatim>
	<f:verbatim></h4></f:verbatim>

	<mf:htmlShowArea value="#{message.message.body}" hideBorder="true" rendered="#{!message.deleted}" />

	<f:verbatim></div></f:verbatim>