<%-- Display single message in threaded view. (included for each message). --%>
<%-- designNote: what does read/unread mean in this context since I am seeing the whole message?--%>

<%
  	String thisId = request.getParameter("panel");
  	if (thisId == null) 
  	{
    	thisId = "Main" + org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement().getId();
 		 }
 	
%>
<script type="text/javascript">


	var iframeId = '<%= org.sakaiproject.util.Web.escapeJavascript(thisId)%>';
	
	function resize(){
		mySetMainFrameHeight('<%= org.sakaiproject.util.Web.escapeJavascript(thisId)%>');
	}
	
	function dialogLinkClick(link){
		var position =  $(link).position();
		dialogutil.openDialog('dialogDiv', 'dialogFrame', position.top);
	}
</script>

<h:outputText escape="false" value="<a id=\"#{message.message.id}\" name=\"#{message.message.id}\"></a>" />
	<f:verbatim><div class="hierItemBlock" ></f:verbatim>
			<%-- author image --%>
			<h:panelGroup rendered="#{!message.deleted && ForumTool.showProfileInfo && !message.useAnonymousId}" styleClass="authorImage">
				<h:outputLink value="#{ForumTool.serverUrl}/direct/profile/#{message.message.authorId}/formatted" styleClass="authorProfile" rendered="#{ForumTool.showProfileLink}" >
					<h:graphicImage value="#{ForumTool.serverUrl}/direct/profile/#{message.message.authorId}/image/thumb" alt="#{message.message.author}" />
				</h:outputLink>
				<h:graphicImage value="#{ForumTool.serverUrl}/direct/profile/#{message.message.authorId}/image/thumb" alt="#{message.message.author}" rendered="#{!ForumTool.showProfileLink}" />
			</h:panelGroup>
			
			<%-- a deleted message --%>
			<h:panelGroup styleClass="inactive" rendered="#{message.deleted}" >
				<f:verbatim><span></f:verbatim>
					<h:outputText value="#{msgs.cdfm_msg_deleted_label}" />
				<f:verbatim></span></f:verbatim>
			</h:panelGroup>
			<%-- non deleted messages --%>
			<h:panelGroup rendered="#{!message.deleted}">
				<h:outputText styleClass="messageNew" value=" #{msgs.cdfm_newflag}" rendered="#{!message.read}"/>
				<%--pending  message flag --%>
				<h:outputText value="#{msgs.cdfm_msg_pending_label}" rendered="#{message.msgPending}" styleClass="messagePending"/>
				<%--denied  message flag --%>
				<h:outputText value="#{msgs.cdfm_msg_denied_label}" rendered="#{message.msgDenied}" styleClass="messageDenied" />
				<%--message subject line --%>
				<h:commandLink action="#{ForumTool.processActionDisplayMessage}" immediate="true" title=" #{message.message.title}" styleClass="title">
					<h:outputText value="#{message.message.title}" rendered="#{message.read}" />
					<h:outputText styleClass="unreadMsg" value="#{message.message.title}" rendered="#{!message.read}" />
					<f:param value="#{message.message.id}" name="messageId" />
					<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId" />
					<f:param value="#{ForumTool.selectedTopic.topic.baseForum.id}" name="forumId" />
				</h:commandLink>
				<h:outputText value="<br /><div class=\"messageMetadata\">" escape="false" />
				<%--author --%>
				
                <h:outputText value="#{message.anonAwareAuthor}" rendered="#{!ForumTool.instructor || message.useAnonymousId}" styleClass="textPanelFooter #{message.read ? '' : 'unreadMsg'} md #{message.useAnonymousId ? 'anonymousAuthor' : ''}"/>
                
                <f:verbatim><span class="md"></f:verbatim>
                <h:commandLink action="#{mfStatisticsBean.processActionStatisticsUser}" immediate="true" title=" #{message.anonAwareAuthor }" rendered="#{ForumTool.instructor && !message.useAnonymousId}" styleClass="textPanelFooter md #{message.read ? '' : 'unreadMsg'} #{message.useAnonymousId ? 'anonymousAuthor' : ''}">
                    <f:param value="#{message.authorEid}" name="siteUserId"/>
                    <h:outputText value="  #{message.anonAwareAuthor}"/>
                </h:commandLink>

                <f:verbatim></span></f:verbatim>
                <h:outputText value=" #{msgs.cdfm_me}" rendered="#{message.currentUserAndAnonymous}" styleClass="textPanelFooter md #{message.read ? '' : 'unreadMsg'}" />

				<%--date --%>
				<h:outputText value="#{message.message.created}" rendered="#{message.read}" styleClass="textPanelFooter md">
					<f:convertDateTime pattern="#{msgs.date_format_paren}" timeZone="#{ForumTool.userTimeZone}" locale="#{ForumTool.userLocale}"/>
				</h:outputText>
				<h:outputText  value="#{message.message.created}" rendered="#{!message.read}" styleClass="unreadMsg textPanelFooter md">
					<f:convertDateTime pattern="#{msgs.date_format_paren}" timeZone="#{ForumTool.userTimeZone}" locale="#{ForumTool.userLocale}"/>
				</h:outputText>
				<h:outputText value="#{msgs.cdfm_readby}" />
				<h:outputText value="#{message.message.numReaders}" />	
		</h:panelGroup>
		<%-- reply and other actions panel --%>
		<%-- If message actually deleted, don't display links --%>
		
		<f:verbatim><div></f:verbatim> <%-- Grouping buttons --%>
		
		<h:panelGroup rendered="#{!message.deleted}" styleClass="itemToolBar">
				<%-- mark as read link --%>
					<h:outputLink value="javascript:void(0);"
						title="#{msgs.cdfm_mark_as_read}" 
						rendered="#{!message.read}"
						styleClass="markAsReadIcon button"
						onclick="doAjax(#{message.message.id}, #{ForumTool.selectedTopic.topic.id}, this);">
						<h:graphicImage value="/images/trans.gif"/>
						<h:outputText value="#{msgs.cdfm_mark_as_read}"/>
					</h:outputLink>
				<%-- Reply link --%>
				<h:panelGroup rendered="#{ForumTool.selectedTopic.isNewResponseToResponse && message.msgApproved && !ForumTool.selectedTopic.locked && !ForumTool.selectedForum.locked == 'true'}">
					<h:commandLink action="#{ForumTool.processDfMsgReplyMsgFromEntire}" title="#{msgs.cdfm_reply}" styleClass="button"> 
						<h:graphicImage value="/../../library/image/silk/email_go.png" styleClass="middle" alt="#{msgs.cdfm_button_bar_reply_to_msg}" />
						<h:outputText value=" #{msgs.cdfm_reply}" />
						<f:param value="#{message.message.id}" name="messageId" />
						<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId" />
						<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId" />
					</h:commandLink>
				</h:panelGroup>
		</h:panelGroup>
				<%-- (Hide) Other Actions links --%>
		<%--
				<h:panelGroup rendered="#{(ForumTool.selectedTopic.isPostToGradebook && ForumTool.gradebookExist) || ForumTool.selectedTopic.isModeratedAndHasPerm || message.revise 
						|| message.userCanDelete}" style="display:none">
						<h:outputText value=" #{msgs.cdfm_toolbar_separator} " rendered="#{ForumTool.selectedTopic.isNewResponseToResponse && message.msgApproved && !ForumTool.selectedTopic.locked}" />
						<h:outputLink value="#" onclick="toggleDisplayInline('#{message.message.id}_advanced_box'); return false;">
						<h:graphicImage value="/../../library/image/silk/cog.png" alt="#{msgs.cdfm_other_actions}" />
						<h:outputText value="#{msgs.cdfm_other_actions}" />
					</h:outputLink>
				</h:panelGroup>
		--%>
		<h:panelGroup rendered="#{!message.deleted}"  >
				<%--//designNote: panel holds other actions, display toggled above (do some testing - do they show up when they should not? Do I get a 
						"moderate" link when it is not a moderated context, or when the message is mine?) --%>
				<h:outputText escape="false" value="<span id=\"#{message.message.id}_advanced_box\" class=\"otherActions\" style=\"margin:2px 0;\">" />


					<%-- Email --%>
                    <h:panelGroup>
                        <%-- Always show separator, or else we see "Reply Grade" --%>
                    	<h:outputLink id="createEmail1" value="mailto:#{message.authorEmail}" rendered="#{message.userCanEmail}" styleClass="button">
                        	<f:param value="Feedback on #{message.message.title}" name="subject" />
                        	<h:outputText value="#{msgs.cdfm_button_bar_email}"/>
                        </h:outputLink>
                    </h:panelGroup>
					<%-- link to grade --%>
					<h:panelGroup rendered="#{ForumTool.selectedTopic.isPostToGradebook && ForumTool.gradebookExist}">
						<h:outputLink value="/tool/#{ForumTool.currentToolId}/discussionForum/message/dfMsgGrade" target="dialogFrame" styleClass="button"
							onclick="dialogLinkClick(this);">
							<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
							<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
							<f:param value="#{message.message.id}" name="messageId"/>
							<f:param value="dialogDiv" name="dialogDivId"/>
							<f:param value="dialogFrame" name="frameId"/>
							<f:param value="gradesSavedDiv" name="gradesSavedDiv"/>
							<f:param value="#{message.message.createdBy}" name="userId"/>
							<h:outputText value=" #{msgs.cdfm_button_bar_grade}" />
						</h:outputLink>
					</h:panelGroup>
					<%-- Revise other action --%>
					<h:panelGroup rendered="#{message.revise}">
						<h:commandLink action="#{ForumTool.processDfMsgRvsFromThread}" value="#{msgs.cdfm_button_bar_revise}" styleClass="button">
							<f:param value="#{message.message.id}" name="messageId" />
							<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId" />
							<f:param value="#{ForumTool.selectedTopic.topic.baseForum.id}" name="forumId" />
						</h:commandLink>
					</h:panelGroup>
					<%-- Delete other action --%>
					<h:panelGroup rendered="#{message.userCanDelete}" >
						<h:commandLink action="#{ForumTool.processDfMsgDeleteConfirm}" value="#{msgs.cdfm_button_bar_delete_message}" styleClass="button">
							<f:param value="#{message.message.id}" name="messageId" />
							<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId" />
							<f:param value="#{ForumTool.selectedTopic.topic.baseForum.id}" name="forumId" />
							<f:param value="dfViewThread" name="fromPage" />
						</h:commandLink>
					</h:panelGroup>
					<%-- Moderate other action --%>
					<h:panelGroup rendered="#{ForumTool.selectedTopic.isModeratedAndHasPerm}">
						<h:commandLink action="#{ForumTool.processActionDisplayMessage}" immediate="true" title=" #{msgs.cdfm_moderate}" value="#{msgs.cdfm_moderate}"  styleClass="button">
							<f:param value="#{message.message.id}" name="messageId" />
							<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId" />
							<f:param value="#{ForumTool.selectedTopic.topic.baseForum.id}" name="forumId" />
						</h:commandLink>
					</h:panelGroup>

			</h:panelGroup>
                        <f:verbatim></span></f:verbatim>
            <f:verbatim></div></f:verbatim>
			<h:outputText value="</div>" escape="false" />

                                        <%-- End of div for messageMetadata --%>

					<%-- Rank --%>
					<h:panelGroup rendered="#{message.authorRank != null}">
					<f:verbatim><div class="forumsRank"></f:verbatim>
						<h:outputText escape="false" rendered="#{not empty message.authorRank.rankImage.attachmentId}" value="<img src=\"#{message.authorRank.rankImage.attachmentUrl}\" class=\"rankImage\" alt=\"Rank Image\" height=\"35\" width=\"35\" />" />
											
						<f:verbatim><div class="forumsRankNameContainer"></f:verbatim>
							<h:outputText value="#{message.authorRank.title}" styleClass="forumsRankName"/>
							<h:outputText value="#{msgs.num_of_posts} #{message.authorPostCount}" styleClass="forumsRankName" rendered="#{message.authorRank.type == 2}"/>
						<f:verbatim></div></f:verbatim>
					<f:verbatim></div></f:verbatim>
					</h:panelGroup>
					<%-- End Rank --%>

			<!-- close the div with class of specialLink -->
	<%--<f:verbatim></div></f:verbatim>-->
	<%-- the message body--%>
	<mf:htmlShowArea value="#{message.message.body}" hideBorder="true" rendered="#{!message.deleted}" />
	<%-- attach list --%>	
	<h:panelGroup rendered="#{!empty message.attachList && !message.deleted}">
		<h:dataTable value="#{message.attachList}" var="eachAttach" styleClass="attachListJSF" rendered="#{!empty message.attachList}" style="font-size:.9em;width:auto;margin-left:1em">
			<h:column rendered="#{!empty message.message.attachments}">
				  <sakai:contentTypeMap fileType="#{eachAttach.attachment.attachmentType}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>
  					<h:graphicImage id="exampleFileIcon" value="#{imagePath}" />
				<h:outputLink value="#{eachAttach.url}" target="_blank">
					<h:outputText value=" " />
					<h:outputText value="#{eachAttach.attachment.attachmentName}" />
				</h:outputLink>
			</h:column>
		</h:dataTable>
	</h:panelGroup>
	<%-- close the div with class of hierItemBlock --%>
<h:outputText escape="false" value="</div>"  rendered="#{!message.deleted}"/>
