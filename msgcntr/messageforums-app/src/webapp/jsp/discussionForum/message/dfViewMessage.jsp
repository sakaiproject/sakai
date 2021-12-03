// <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>

<f:view>
	<sakai:view toolCssHref="/messageforums-tool/css/msgcntr.css">
		<h:form id="msgForum" styleClass="specialLink" prependId = "false">
			<h:inputHidden id="currentMessageId" value="#{ForumTool.selectedMessage.message.id}"/>
			<h:inputHidden id="currentTopicId" value="#{ForumTool.selectedTopic.topic.id}"/>
			<h:inputHidden id="currentForumId" value="#{ForumTool.selectedForum.forum.id}"/>
			<script>includeLatestJQuery("msgcntr");</script>
			<script>includeWebjarLibrary("qtip2");</script>
			<script src="/messageforums-tool/js/forum.js"></script>
			<script src="/messageforums-tool/js/sak-10625.js"></script>
			<script src="/messageforums-tool/js/messages.js"></script>
			
			<!--jsp/discussionForum/message/dfViewMessage.jsp-->
			<script>
				$(document).ready(function() {
					if ($('table.messageActions a').length==0){
						$('.messageActions').hide();
					}
						$('.permaLink').click(function(event){
							event.preventDefault();
							var url = $(this).attr('href');
							if (!url) url = this.href;
							$('#permalinkHolder textarea').val(url);
							$('#permalinkHolder').css({
								'top': $(this).position().top,
								'left': $(this).position().left
							});
							$('#permalinkHolder').fadeIn('fast');
							$('#permalinkHolder textarea').focus().select();
							navigator.clipboard.writeText(url);
						});
					$('#permalinkHolder .closeMe').click(function(event){
						event.preventDefault();
						$('#permalinkHolder').fadeOut('fast');
					});
					var msgBody = document.getElementById("messageBody").innerHTML;
					msgBody = msgBody.replace(/\n/g,',').replace(/\s/g,' ').replace(/  ,/g,',');
					fckeditor_word_count_fromMessage(msgBody, "counttotal");

					var menuLink = $('#forumsMainMenuLink');
					var menuLinkSpan = menuLink.closest('span');
					menuLinkSpan.addClass('current');
					menuLinkSpan.html(menuLink.text());

					});
			</script>
            <%@ include file="/jsp/discussionForum/menu/forumsMenu.jsp" %>

			<%--breadcrumb and thread nav grid--%>
			<h:panelGroup layout="block" styleClass="navPanel row">
				<h:panelGroup layout="block" styleClass="col-md-12">
					<h3>
						<h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_message_forums}" title=" #{msgs.cdfm_message_forums}"
							rendered="#{ForumTool.messagesandForums}" />
						<h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_discussion_forums}" title=" #{msgs.cdfm_discussion_forums}"
							rendered="#{ForumTool.forumsTool}" />
						<h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " />
						<h:commandLink action="#{ForumTool.processActionDisplayForum}" 
								title=" #{ForumTool.selectedForum.forum.title}" rendered="#{ForumTool.showForumLinksInNav}" >
							<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
							<h:outputText value="#{ForumTool.selectedForum.forum.title}"/>
						</h:commandLink>
						<h:outputText value="#{ForumTool.selectedForum.forum.title}" rendered="#{!ForumTool.showForumLinksInNav}"/>
						<h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " />
						<h:commandLink action="#{ForumTool.processActionDisplayTopic}"  
								title=" #{ForumTool.selectedTopic.topic.title}">
								<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
								<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
								<h:outputText value="#{ForumTool.selectedTopic.topic.title}"/>
						</h:commandLink>
						<h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " />
						<h:commandLink action="#{ForumTool.processActionDisplayThread}"  
								title=" #{ForumTool.selectedThreadHead.message.title}">
							<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
							<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
							<f:param value="#{ForumTool.selectedThreadHead.message.id}" name="messageId"/>
							<h:outputText value="#{ForumTool.selectedThreadHead.message.title}"/>
						</h:commandLink>
					</h3>
				</h:panelGroup>
			</h:panelGroup>
			
			<h:panelGroup layout="block" styleClass="row view-message-nav">
				<h:panelGroup layout="block" styleClass="col-md-offset-6 col-md-6 view-message-nav">
					<h:panelGroup styleClass="button formButtonDisabled" rendered="#{!ForumTool.selectedThreadHead.hasPreThread}">
						<h:outputText value="#{msgs.cdfm_previous_thread}"  />
					</h:panelGroup>
					<h:commandLink styleClass="button" action="#{ForumTool.processActionDisplayThread}" value="#{msgs.cdfm_previous_thread}"  rendered="#{ForumTool.selectedThreadHead.hasPreThread}">
						<f:param value="#{ForumTool.selectedThreadHead.preThreadId}" name="messageId"/>
						<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
						<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
					</h:commandLink>
					<h:panelGroup styleClass="button formButtonDisabled" rendered="#{!ForumTool.selectedThreadHead.hasNextThread}">
						<h:outputText value="#{msgs.cdfm_next_thread}"  />
					</h:panelGroup>
					<h:commandLink styleClass="button" action="#{ForumTool.processActionDisplayThread}" value="#{msgs.cdfm_next_thread}" rendered="#{ForumTool.selectedThreadHead.hasNextThread}">
						<f:param value="#{ForumTool.selectedThreadHead.nextThreadId}" name="messageId"/>
						<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
						<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
					</h:commandLink>
				</h:panelGroup>
			</h:panelGroup>
			<%-- topic short description and long description --%>
			<h:panelGroup layout="block" styleClass="topicBloc">
				<h:panelGroup layout="block" styleClass="textPanel">
					<h:graphicImage url="/images/silk/date_delete.png" title="#{msgs.topic_restricted_message}" alt="#{msgs.topic_restricted_message}" rendered="#{ForumTool.selectedTopic.availability == 'false'}" style="margin-right:.5em"/>
					<h:graphicImage url="/images/silk/lock.png" alt="#{msgs.cdfm_forum_locked}" 
						 rendered="#{ForumTool.selectedTopic.locked =='true'}" style="margin-right:.5em"/>
					<h:outputText value="#{ForumTool.selectedTopic.topic.shortDescription}" />
				</h:panelGroup>
				<%-- link to open and close long desc. --%>
				<h:outputText value="#{ForumTool.selectedForum.forum.title} /  #{ForumTool.selectedTopic.topic.title}"  styleClass="title"/> 
				<h:panelGroup layout="block" styleClass="textPanel">
					<h:outputText value="#{ForumTool.selectedTopic.topic.shortDescription}" />
				</h:panelGroup>
				<h:panelGroup layout="block">
					<p id="openLinkBlock" class="toggleParent openLinkBlock display-none">
						<a href="#" id="showMessage" class="toggle show">
							<h:graphicImage url="/images/collapse.gif" alt=""/>
							<h:outputText value=" #{msgs.cdfm_read_full_description}" />
						</a>
					</p>
					<p id="hideLinkBlock" class="toggleParent hideLinkBlock">
						<a href="#" id="hideMessage" class="toggle show">
							<h:graphicImage url="/images/expand.gif" alt="" />
							<h:outputText value=" #{msgs.cdfm_hide_full_description}"/>
						</a>
					</p>
					<h:panelGroup id="fullTopicDescription" layout="block" styleClass="textPanel fullTopicDescription">
						<h:outputText escape="false" value="#{ForumTool.selectedTopic.topic.extendedDescription}" />
					</h:panelGroup>
				</h:panelGroup>
			</h:panelGroup>
			<h:messages globalOnly="true" infoClass="success" errorClass="alertMessage" rendered="#{! empty facesContext.maximumSeverity}"/>
			<h:panelGroup styleClass="margin-left:1em;">
				<h:graphicImage url="/../../library/image/silk/table_add.png" alt="#{msgs.cdfm_message_count}" />&nbsp;<h:outputText value="#{msgs.cdfm_message_count}" />:&nbsp;
				<h:panelGroup id="counttotal"></h:panelGroup>
			</h:panelGroup>
			<h:panelGrid columns="2" 
					width="100%" 
					columnClasses="specialLink, specialLink otherOtherActions"
					cellpadding="0" cellspacing="0"
					rendered="#{!ForumTool.deleteMsg && !ForumTool.selectedMessage.message.deleted}" 
					styleClass="messageActions"
					style="margin:1em 0 0 0">
				<h:panelGroup style="display:block">
					<h:commandLink styleClass="button" title="#{msgs.cdfm_button_bar_reply_to_msg}" action="#{ForumTool.processDfMsgReplyMsg}" 
							rendered="#{ForumTool.selectedTopic.isNewResponseToResponse && ForumTool.selectedMessage.msgApproved && !ForumTool.selectedTopic.locked && !ForumTool.selectedForum.locked == 'true'}">
						<h:graphicImage value="/../../library/image/silk/email_go.png" alt="#{msgs.cdfm_button_bar_reply_to_msg}" rendered="#{ForumTool.selectedTopic.isNewResponseToResponse}" />
						<h:outputText value=" #{msgs.cdfm_button_bar_reply_to_msg}" />
					</h:commandLink>
					
					<h:commandLink styleClass="button"  title="#{msgs.cdfm_button_bar_reply_to_thread}" action="#{ForumTool.processDfMsgReplyThread}" 
							rendered="#{ForumTool.selectedTopic.isNewResponseToResponse && ForumTool.selectedThreadHead.msgApproved && !ForumTool.selectedTopic.locked && !ForumTool.selectedForum.locked == 'true'}">
						<h:graphicImage value="/../../library/image/silk/folder_go.png" alt="#{msgs.cdfm_button_bar_reply_to_thread}" />
						<h:outputText value=" #{msgs.cdfm_button_bar_reply_to_thread}" />
					</h:commandLink>
					
					<h:commandLink styleClass="button"  title="#{msgs.cdfm_button_bar_delete_msg}" action="#{ForumTool.processDfMsgDeleteConfirm}" rendered="#{ForumTool.selectedMessage.userCanDelete}" >
						<h:graphicImage value="/../../library/image/silk/email_delete.png" alt="#{msgs.cdfm_button_bar_delete_msg}" />
						<h:outputText value=" #{msgs.cdfm_button_bar_delete_msg}" />
					</h:commandLink>
					
					<h:commandLink styleClass="button"  title="#{msgs.cdfm_button_bar_revise}" action="#{ForumTool.processDfMsgRvs}" 
							rendered="#{ForumTool.selectedMessage.revise}">
						<h:graphicImage value="/../../library/image/silk/email_edit.png" alt="#{msgs.cdfm_button_bar_revise}" />
						<h:outputText value=" #{msgs.cdfm_button_bar_revise}" />
					</h:commandLink>
					
					<h:commandLink styleClass="button"  title="#{msgs.cdfm_button_bar_grade}" action="#{ForumTool.processDfMsgGrd}" 
							rendered="#{ForumTool.selectedTopic.isPostToGradebook && ForumTool.gradebookExist}">
						<h:graphicImage value="/../../library/image/silk/award_star_gold_1.png" alt="#{msgs.cdfm_button_bar_grade}" />
						<h:outputText value=" #{msgs.cdfm_button_bar_grade}" />
					</h:commandLink>
					<%-- Email --%>
					<h:outputLink styleClass="button"  id="createEmail1" value="mailto:#{ForumTool.selectedMessage.authorEmail}" rendered="#{ForumTool.selectedMessage.userCanEmail && ForumTool.selectedMessage.authorEmail != '' && ForumTool.selectedMessage.authorEmail != null}"> 
						<f:param value="Feedback on #{ForumTool.selectedMessage.message.title}" name="subject" />
						<h:graphicImage value="/../../library/image/silk/email_edit.png" alt="#{msgs.cdfm_button_bar_email}" />
  						<h:outputText value=" #{msgs.cdfm_button_bar_email}"/>
					</h:outputLink>
					<%-- premalink --%>
					<h:outputLink id="permalink1" value="#{ForumTool.messageURL}" styleClass="button permaLink" title="#{msgs.cdfm_button_bar_permalink_message}"> 
						<h:graphicImage value="/../../library/image/silk/folder_go.png" alt="#{msgs.cdfm_button_bar_permalink}" />
  						<h:outputText value=" #{msgs.cdfm_button_bar_permalink}"/>
					</h:outputLink>
				</h:panelGroup>
				<h:panelGroup style="display:block;white-space:nowrap;">
					<h:commandLink styleClass="button" title="#{msgs.cdfm_button_bar_deny}" action="#{ForumTool.processDfMsgDeny}" 
							rendered="#{ForumTool.allowedToDenyMsg}">
						<h:graphicImage value="/../../library/image/silk/cross.png" alt="#{msgs.cdfm_button_bar_deny}" />
						<h:outputText value=" #{msgs.cdfm_button_bar_deny}" />
					</h:commandLink>
					<h:commandLink styleClass="button" title="#{msgs.cdfm_button_bar_add_comment}" action="#{ForumTool.processDfMsgAddComment}" 
							rendered="#{ForumTool.allowedToApproveMsg && ForumTool.selectedMessage.msgDenied}">
						<h:graphicImage value="/../../library/image/silk/comment.png" alt="#{msgs.cdfm_button_bar_add_comment}" />
						<h:outputText value=" #{msgs.cdfm_button_bar_add_comment}" />
					</h:commandLink>
					<h:commandLink styleClass="button" title="#{msgs.cdfm_button_bar_approve}" action="#{ForumTool.processDfMsgApprove}" 
							rendered="#{ForumTool.allowedToApproveMsg}">
						<h:graphicImage value="/../../library/image/silk/tick.png" alt="#{msgs.cdfm_button_bar_approve}" />
						<h:outputText value=" #{msgs.cdfm_button_bar_approve}" />
					</h:commandLink>
				</h:panelGroup>
			</h:panelGrid>
			

			<h:panelGroup layout="block" id="permalinkHolder">
				<h:outputLink styleClass="closeMe" value="#"><h:panelGroup styleClass="icon-sakai--delete"></h:panelGroup></h:outputLink>
				<h:outputText value="#{msgs.cdfm_button_bar_permalink_message}" style="display:block" styleClass="textPanelFooter"/>
				<h:inputTextarea value="" />
			</h:panelGroup>

			<%--navigation cell --%>
			<h:panelGroup layout="block" styleClass="row view-message-nav">
				<h:panelGroup layout="block" styleClass="col-md-offset-6 col-md-6">
					<h:commandLink styleClass="button"
						action="#{ForumTool.processDisplayPreviousMsg}"
						rendered="#{ForumTool.selectedMessage != null && ForumTool.selectedMessage.hasPre}"
						title=" #{msgs.cdfm_prev_msg}">
						<h:outputText value="#{msgs.cdfm_prev_msg}" />
					</h:commandLink>
					<h:panelGroup styleClass="button formButtonDisabled"
						rendered="#{!ForumTool.selectedMessage.hasPre}">
						<h:outputText value="#{msgs.cdfm_prev_msg}" />
					</h:panelGroup>
					<h:commandLink styleClass="button"
						action="#{ForumTool.processDfDisplayNextMsg}"
						rendered="#{ForumTool.selectedMessage != null && ForumTool.selectedMessage.hasNext}"
						title=" #{msgs.cdfm_next_msg}">
						<h:outputText value="#{msgs.cdfm_next_msg}" />
					</h:commandLink>
					<h:panelGroup styleClass="button formButtonDisabled"
						rendered="#{!ForumTool.selectedMessage.hasNext}">
						<h:outputText value="#{msgs.cdfm_next_msg}" />
					</h:panelGroup>
				</h:panelGroup>
			</h:panelGroup>
			<h:panelGroup layout="block" styleClass="singleMessage">
				<%--title, metadata and navigation --%>
				<h:panelGrid columns="1"  style="width: 100%;" border="0">
					<h:outputText rendered="#{ForumTool.selectedMessage.message.deleted && !ForumTool.needToPostFirst}"  value="#{msgs.cdfm_msg_deleted_label}" styleClass="instruction"/>
					<h:outputText value="#{msgs.cdfm_postFirst_warning}" rendered="#{ForumTool.needToPostFirst}" styleClass="messageAlert"/>
					<h:panelGroup rendered="#{!ForumTool.selectedMessage.message.deleted}" style="display:block">
						<h:panelGroup styleClass="authorImage" rendered="#{ForumTool.showProfileInfo && !ForumTool.selectedMessage.useAnonymousId}">
							<h:outputLink value="#{ForumTool.serverUrl}/direct/profile/#{ForumTool.selectedMessage.message.authorId}/formatted" styleClass="authorProfile" rendered="#{ForumTool.showProfileLink}">
								<h:graphicImage value="#{ForumTool.serverUrl}/direct/profile/#{ForumTool.selectedMessage.message.authorId}/image/thumb" alt="#{ForumTool.selectedMessage.message.author}" />
							</h:outputLink>
							<h:graphicImage value="#{ForumTool.serverUrl}/direct/profile/#{ForumTool.selectedMessage.message.authorId}/image/thumb" alt="#{ForumTool.selectedMessage.message.author}" rendered="#{!ForumTool.showProfileLink}"/>
						</h:panelGroup>
						<h:outputText rendered="#{ ForumTool.selectedMessage.msgDenied}" value="#{msgs.cdfm_msg_denied_label}" styleClass="messageDenied"/>
						<h:outputText 	rendered="#{ForumTool.allowedToApproveMsg && ForumTool.allowedToDenyMsg}" value="#{msgs.cdfm_msg_pending_label}" styleClass="messagePending"/>
						<h:outputText value="#{ForumTool.selectedMessage.message.title}"  styleClass="title" />
						<h:outputText value="<br />" escape="false" />
						<h:outputText value="#{ForumTool.selectedMessage.anonAwareAuthor}" styleClass="textPanelFooter #{ForumTool.selectedMessage.useAnonymousId ? 'anonymousAuthor' : ''}" rendered="#{!ForumTool.instructor || ForumTool.selectedMessage.useAnonymousId}"/>
						<h:outputText value=" #{msgs.cdfm_me}" styleClass="textPanelFooter" rendered="#{ForumTool.selectedMessage.currentUserAndAnonymous}" />
						<h:commandLink action="#{mfStatisticsBean.processActionStatisticsUser}" immediate="true" title=" #{ForumTool.selectedMessage.anonAwareAuthor }" styleClass="textPanelFooter #{ForumTool.selectedMessage.useAnonymousId ? 'anonymousAuthor' : ''}" rendered="#{ForumTool.instructor && !ForumTool.selectedMessage.useAnonymousId}">
                        	<f:param value="#{ForumTool.selectedMessage.authorEid}" name="siteUserId"/>
                        	<h:outputText value="#{ForumTool.selectedMessage.anonAwareAuthor}"/>
                        </h:commandLink>
						<h:outputText value=" #{msgs.cdfm_openb} "  styleClass="textPanelFooter" />
						<h:outputText value="#{ForumTool.selectedMessage.message.created}"  styleClass="textPanelFooter" >
							<f:convertDateTime pattern="#{msgs.date_format}" timeZone="#{ForumTool.userTimeZone}" locale="#{ForumTool.userLocale}"/>  
						</h:outputText>
						<h:outputText value=" #{msgs.cdfm_closeb}"  styleClass="textPanelFooter" />
					</h:panelGroup>

				</h:panelGrid>

				<%-- Rank --%>
				<h:panelGroup rendered="#{ForumTool.selectedMessage.authorRank != null}">
				<h:panelGroup layout="block" styleClass="forumsRank">
					<h:outputText escape="false" rendered="#{not empty ForumTool.selectedMessage.authorRank.rankImage.attachmentId}" value="<img src=\"#{ForumTool.selectedMessage.authorRank.rankImage.attachmentUrl}\" class=\"rankImage\" alt=\"Rank Image\" height=\"35\" width=\"35\" />" />
					<h:panelGroup layout="block" styleClass="forumsRankNameContainer">
						<h:outputText value="#{ForumTool.selectedMessage.authorRank.title}" styleClass="forumsRankName"/>
						<h:outputText value="#{msgs.num_of_posts} #{ForumTool.selectedMessage.authorPostCount}" styleClass="forumsRankName" rendered="#{ForumTool.selectedMessage.authorRank.type == 2}"/>
					</h:panelGroup>
				</h:panelGroup>
				</h:panelGroup>
				<%-- End Rank --%>

				<h:panelGroup layout="block" styleClass="textPanel">
					<h:outputText escape="false" value="#{ForumTool.selectedMessage.message.body}" id="messageBody" 
							rendered="#{!ForumTool.selectedMessage.message.deleted}" />
				</h:panelGroup>
				<h:dataTable value="#{ForumTool.selectedMessage.attachList}" var="eachAttach"  cellpadding="3" cellspacing="0" columnClasses="attach,bogus" style="font-size:.9em;width:auto;margin-left:1em" border="0">
					<h:column rendered="#{!empty ForumTool.selectedMessage.message.attachments}">
						  <sakai:contentTypeMap fileType="#{eachAttach.attachment.attachmentType}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>
  						<h:graphicImage id="exampleFileIcon" value="#{imagePath}" />
						<%-- <h:outputLink value="#{eachAttach.attachmentUrl}" target="_blank">
							<h:outputText value="#{eachAttach.attachmentName}"/>
						</h:outputLink>--%>
						<h:outputText value=" "/>
						<h:outputLink value="#{eachAttach.url}" target="_blank">
							<h:outputText value="#{eachAttach.attachment.attachmentName}"/>
						</h:outputLink>
					</h:column>
				</h:dataTable>
			</h:panelGroup>
		
			<h:panelGroup rendered="#{ForumTool.deleteMsg && ForumTool.errorSynch}">
				<h:outputText styleClass="alertMessage" 
				value="#{msgs.cdfm_msg_del_has_reply}" />
			</h:panelGroup>
		
			<%-- If deleting, tells where to go back to --%>	
			<h:inputHidden value="#{ForumTool.fromPage}" />

			<p style="padding:0" class="act">
				<h:commandButton id="post" action="#{ForumTool.processDfMsgDeleteConfirmYes}" value="#{msgs.cdfm_button_bar_delete}" accesskey="x" styleClass="active blockMeOnClick" rendered="#{ForumTool.selectedMessage.userCanDelete}" />
                <h:outputText styleClass="sak-banner-info" style="display:none" value="#{msgs.cdfm_processing_submit_message}" />
			</p>
	
			<h:panelGroup layout="block" styleClass="row view-message-nav">
				<h:panelGroup layout="block" styleClass="col-md-offset-6 col-md-6">
					<h:commandLink styleClass="button" action="#{ForumTool.processDisplayPreviousMsg}" rendered="#{ForumTool.selectedMessage != null && ForumTool.selectedMessage.hasPre}" 
							title=" #{msgs.cdfm_prev_msg}">
						<h:outputText value="#{msgs.cdfm_prev_msg}" />
					</h:commandLink>
					<h:panelGroup rendered="#{!ForumTool.selectedMessage.hasPre}" styleClass="button formButtonDisabled">
						<h:outputText value="#{msgs.cdfm_prev_msg}"  />
					</h:panelGroup>
					<h:commandLink styleClass="button" action="#{ForumTool.processDfDisplayNextMsg}" rendered="#{ForumTool.selectedMessage != null && ForumTool.selectedMessage.hasNext}" 
							title=" #{msgs.cdfm_next_msg}">
						<h:outputText value="#{msgs.cdfm_next_msg}" />
					</h:commandLink>
					<h:panelGroup rendered="#{!ForumTool.selectedMessage.hasNext}"  styleClass="button formButtonDisabled">
						<h:outputText value="#{msgs.cdfm_next_msg}" />
					</h:panelGroup>
				</h:panelGroup>
			</h:panelGroup>
			<h:panelGroup><br /></h:panelGroup>
			<h:panelGroup layout="block" styleClass="row view-message-nav">
				<h:panelGroup layout="block" styleClass="col-md-offset-6 col-md-6">
					<h:panelGroup rendered="#{!ForumTool.selectedThreadHead.hasPreThread}" >
						<h:outputText value="#{msgs.cdfm_previous_thread}"  styleClass="button formButtonDisabled" />
					</h:panelGroup>
					<h:commandLink styleClass="button" action="#{ForumTool.processActionDisplayThread}" value="#{msgs.cdfm_previous_thread}"  rendered="#{ForumTool.selectedThreadHead.hasPreThread}">
						<f:param value="#{ForumTool.selectedThreadHead.preThreadId}" name="messageId"/>
						<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
						<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
					</h:commandLink>
					<h:panelGroup rendered="#{!ForumTool.selectedThreadHead.hasNextThread}" styleClass="button formButtonDisabled" >
						<h:outputText   value="#{msgs.cdfm_next_thread}"  />
					</h:panelGroup>
					<h:commandLink styleClass="button" action="#{ForumTool.processActionDisplayThread}" value="#{msgs.cdfm_next_thread}" rendered="#{ForumTool.selectedThreadHead.hasNextThread}">
						<f:param value="#{ForumTool.selectedThreadHead.nextThreadId}" name="messageId"/>
						<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
						<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
					</h:commandLink>
				</h:panelGroup>
			 </h:panelGroup>
		</h:form>
	</sakai:view>
</f:view>
