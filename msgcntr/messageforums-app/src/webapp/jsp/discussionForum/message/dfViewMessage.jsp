<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>

<f:view>
	<sakai:view toolCssHref="/messageforums-tool/css/msgcntr.css">
		<h:form id="msgForum" styleClass="specialLink">
			<f:verbatim><input type="hidden" id="currentMessageId" name="currentMessageId" value="</f:verbatim><h:outputText value="#{ForumTool.selectedMessage.message.id}"/><f:verbatim>"/></f:verbatim>
			<f:verbatim><input type="hidden" id="currentTopicId" name="currentTopicId" value="</f:verbatim><h:outputText value="#{ForumTool.selectedTopic.topic.id}"/><f:verbatim>"/></f:verbatim>
			<f:verbatim><input type="hidden" id="currentForumId" name="currentForumId" value="</f:verbatim><h:outputText value="#{ForumTool.selectedForum.forum.id}"/><f:verbatim>"/></f:verbatim>
			<script type="text/javascript">includeLatestJQuery("msgcntr");</script>
			<script type="text/javascript" src="/library/js/jquery/qtip/jquery.qtip-latest.min.js"></script>
			<link rel="stylesheet" type="text/css" href="/library/js/jquery/qtip/jquery.qtip-latest.min.css" />
			<sakai:script contextBase="/messageforums-tool" path="/js/forum.js"/>
			<sakai:script contextBase="/messageforums-tool" path="/js/sak-10625.js"/>
			<sakai:script contextBase="/messageforums-tool" path="/js/messages.js"/>
			
			<!--jsp/discussionForum/message/dfViewMessage.jsp-->
			<script type="text/javascript">
				$(document).ready(function() {
					if ($('table.messageActions a').length==0){
						$('.messageActions').hide();
					}
						$('.permaLink').click(function(event){
							event.preventDefault();
                            var url = $(this).attr('href');
                            if (!url)
							    url = this.href;
							$('#permalinkHolder textarea').val(url);
							$('#permalinkHolder').css({
								'top': $(this).position().top,
								'left': $(this).position().left
							});
							$('#permalinkHolder').fadeIn('fast');
							$('#permalinkHolder input').focus().select();
						});
					$('#permalinkHolder .closeMe').click(function(event){
						event.preventDefault();
						$('#permalinkHolder').fadeOut('fast');
					});
					var msgBody = document.getElementById("msgForum:messageBody").innerHTML;
					msgBody = msgBody.replace(/\n/g,',').replace(/\s/g,' ').replace(/  ,/g,',');
					fckeditor_word_count_fromMessage(msgBody, "counttotal");

					});
			</script>

			<%--breadcrumb and thread nav grid--%>
			<div class="navPanel row">
				<div class="col-md-12">
					<f:verbatim><h3></f:verbatim>
						<h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_message_forums}" title=" #{msgs.cdfm_message_forums}"
							rendered="#{ForumTool.messagesandForums}" />
						<h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_discussion_forums}" title=" #{msgs.cdfm_discussion_forums}"
							rendered="#{ForumTool.forumsTool}" />
						<f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
						<h:commandLink action="#{ForumTool.processActionDisplayForum}" 
								title=" #{ForumTool.selectedForum.forum.title}" rendered="#{ForumTool.showForumLinksInNav}" >
							<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
							<h:outputText value="#{ForumTool.selectedForum.forum.title}"/>
						</h:commandLink>
						<h:outputText value="#{ForumTool.selectedForum.forum.title}" rendered="#{!ForumTool.showForumLinksInNav}"/>
						<f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
						<h:commandLink action="#{ForumTool.processActionDisplayTopic}"  
								title=" #{ForumTool.selectedTopic.topic.title}">
								<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
								<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
								<h:outputText value="#{ForumTool.selectedTopic.topic.title}"/>
						</h:commandLink>
						<f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
						<h:commandLink action="#{ForumTool.processActionDisplayThread}"  
								title=" #{ForumTool.selectedThreadHead.message.title}">
							<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
							<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
							<f:param value="#{ForumTool.selectedThreadHead.message.id}" name="messageId"/>
							<h:outputText value="#{ForumTool.selectedThreadHead.message.title}"/>
						</h:commandLink>
					<f:verbatim></h3></f:verbatim>
				</div>
			</div>
			
			<div class="row">
				<div class="col-md-offset-6 col-md-6 view-message-nav">
					<h:panelGroup  styleClass="button formButtonDisabled" rendered="#{!ForumTool.selectedThreadHead.hasPreThread}">
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
				 </div>
			</div>
			<%-- topic short description and long description --%>
			<div class="topicBloc" style="width:80%;padding:0 .5em;margin:0;">
				<p class="textPanel">
					<h:graphicImage url="/images/silk/date_delete.png" title="#{msgs.topic_restricted_message}" alt="#{msgs.topic_restricted_message}" rendered="#{ForumTool.selectedTopic.availability == 'false'}" style="margin-right:.5em"/>
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
			<h:messages globalOnly="true" infoClass="success" errorClass="alertMessage" rendered="#{! empty facesContext.maximumSeverity}"/>
			<f:subview id="wordCountView" rendered="#{ForumTool.selectedTopic.isPostToGradebook && ForumTool.gradebookExist}">
				<f:verbatim>
					<span style="margin-left:1em"><img src="/library/image/silk/table_add.png" />&nbsp;<h:outputText value="#{msgs.cdfm_message_count}" />:&nbsp;<span  id="counttotal"> </span></span>
				</f:verbatim>
			</f:subview>
			<h:panelGrid columns="2" 
					width="100%" 
					columnClasses="specialLink, specialLink otherOtherActions"
					cellpadding="0" cellspacing="0"
					rendered="#{!ForumTool.deleteMsg && !ForumTool.selectedMessage.message.deleted}" 
					styleClass="messageActions"
					style="margin:1em 0 0 0">
				<h:panelGroup  style="display:block">
					<h:commandLink title="#{msgs.cdfm_button_bar_reply_to_msg}" action="#{ForumTool.processDfMsgReplyMsg}" 
							rendered="#{ForumTool.selectedTopic.isNewResponseToResponse && ForumTool.selectedMessage.msgApproved && !ForumTool.selectedTopic.locked && !ForumTool.selectedForum.locked == 'true'}">
						<h:graphicImage value="/../../library/image/silk/email_go.png" alt="#{msgs.cdfm_button_bar_reply_to_msg}" rendered="#{ForumTool.selectedTopic.isNewResponseToResponse}" />
						<h:outputText value=" #{msgs.cdfm_button_bar_reply_to_msg}" />
					</h:commandLink>
					
					<h:commandLink title="#{msgs.cdfm_button_bar_reply_to_thread}" action="#{ForumTool.processDfMsgReplyThread}" 
							rendered="#{ForumTool.selectedTopic.isNewResponseToResponse && ForumTool.selectedThreadHead.msgApproved && !ForumTool.selectedTopic.locked && !ForumTool.selectedForum.locked == 'true'}">
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
					<%-- Email --%>
					<h:outputLink id="createEmail1" value="mailto:#{ForumTool.selectedMessage.authorEmail}" rendered="#{ForumTool.selectedMessage.userCanEmail && ForumTool.selectedMessage.authorEmail != '' && ForumTool.selectedMessage.authorEmail != null}"> 
						<f:param value="Feedback on #{ForumTool.selectedMessage.message.title}" name="subject" />
						<h:graphicImage value="/../../library/image/silk/email_edit.png" alt="#{msgs.cdfm_button_bar_email}" />
  						<h:outputText value=" #{msgs.cdfm_button_bar_email}"/>
					</h:outputLink>			
					<%-- premalink --%>
					<h:outputLink id="permalink1" value="#{ForumTool.messageURL}" styleClass="permaLink" title="#{msgs.cdfm_button_bar_permalink_message}"> 
						<h:graphicImage value="/../../library/image/silk/folder_go.png" alt="#{msgs.cdfm_button_bar_permalink}" />
  						<h:outputText value=" #{msgs.cdfm_button_bar_permalink}"/>
					</h:outputLink>
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
			

			<f:verbatim><div id="permalinkHolder"><a class="closeMe" href="#" style=""><span class="icon-sakai--delete"></span></a></f:verbatim>
				<h:outputText value="#{msgs.cdfm_button_bar_permalink_message}" style="display:block" styleClass="textPanelFooter"/>
				<h:inputTextarea value="" />
			<f:verbatim></div></f:verbatim>

			<%--navigation cell --%>
			<div class="row view-message-nav">
				<div class="col-md-offset-6 col-md-6">
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
				</div>
			</div>
			<f:verbatim><div class="singleMessage">
			</f:verbatim>
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
				<f:verbatim><div class="forumsRank"></f:verbatim>
					<h:outputText escape="false" rendered="#{not empty ForumTool.selectedMessage.authorRank.rankImage.attachmentId}" value="<img src=\"#{ForumTool.selectedMessage.authorRank.rankImage.attachmentUrl}\" class=\"rankImage\" alt=\"Rank Image\" height=\"35\" width=\"35\" />" />
					
					<f:verbatim><div class="forumsRankNameContainer"></f:verbatim>
						<h:outputText value="#{ForumTool.selectedMessage.authorRank.title}" styleClass="forumsRankName"/>
						<h:outputText value="#{msgs.num_of_posts} #{ForumTool.selectedMessage.authorPostCount}" styleClass="forumsRankName" rendered="#{ForumTool.selectedMessage.authorRank.type == 2}"/>
					<f:verbatim></div></f:verbatim>
				<f:verbatim></div></f:verbatim>
				</h:panelGroup>
				<%-- End Rank --%>

				<f:verbatim><div class="textPanel"></f:verbatim>
					<h:outputText escape="false" value="#{ForumTool.selectedMessage.message.body}" id="messageBody" 
							rendered="#{!ForumTool.selectedMessage.message.deleted}" />
				<f:verbatim></div></f:verbatim>
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
			<f:verbatim></div></f:verbatim>
		
			<h:panelGroup rendered="#{ForumTool.deleteMsg && ForumTool.errorSynch}">
				<h:outputText styleClass="alertMessage" 
				value="#{msgs.cdfm_msg_del_has_reply}" />
			</h:panelGroup>
		
			<%-- If deleting, tells where to go back to --%>	
			<h:inputHidden value="#{ForumTool.fromPage}" />

			<p style="padding:0" class="act">
				<h:commandButton id="post" action="#{ForumTool.processDfMsgDeleteConfirmYes}" value="#{msgs.cdfm_button_bar_delete}" accesskey="x" styleClass="active blockMeOnClick"/>
				<h:commandButton action="#{ForumTool.processDfMsgDeleteCancel}" value="#{msgs.cdfm_button_bar_cancel}" immediate="true" accesskey="c" />
                <h:outputText styleClass="messageProgress" style="display:none" value="#{msgs.cdfm_processing_submit_message}" />
			</p>
	
			<f:verbatim><br/><br/></f:verbatim>		
			<div class="row view-message-nav">
				<div class="col-md-offset-6 col-md-6">
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
				</div>
			</div>

			<f:verbatim><br/><br/></f:verbatim>
			<div class="row view-message-nav">
				<div class="col-md-offset-6 col-md-6">
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
				</div>
			 </div>
		</h:form>
	</sakai:view>
</f:view>
