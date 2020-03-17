<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>
<f:view>
<sakai:view toolCssHref="/messageforums-tool/css/msgcntr.css">
    <h:form id="msgForum" rendered="#{!ForumTool.selectedForum.forum.draft || ForumTool.selectedForum.forum.createdBy == ForumTool.userId}">
		<script>includeLatestJQuery("msgcntr");</script>
		<script src="/messageforums-tool/js/sak-10625.js"></script>
		<script src="/messageforums-tool/js/forum.js"></script>
		<script src="/rubrics-service/webcomponents/sakai-rubrics-utils.js<h:outputText value="#{ForumTool.CDNQuery}" />"></script>
		<script type="module" src="/rubrics-service/webcomponents/rubric-association-requirements.js<h:outputText value="#{ForumTool.CDNQuery}" />"></script>
		<script>
			$(document).ready(function() {
				var menuLink = $('#forumsMainMenuLink');
				var menuLinkSpan = menuLink.closest('span');
				menuLinkSpan.addClass('current');
				menuLinkSpan.html(menuLink.text());

				setupLongDesc();

			});
		</script>
<!--jsp/discussionForum/forum/dfForumDetail.jsp-->

		<%@ include file="/jsp/discussionForum/menu/forumsMenu.jsp" %>
			<h:outputText styleClass="showMoreText"  style="display:none" value="#{msgs.cdfm_show_more_full_description}"  />


		<h3 class="specialLink" style="margin-bottom:1em">
          		<%-- Display the proper home page link: either Messages & Forums OR Forums --%>
			      <h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_message_forums}" title=" #{msgs.cdfm_message_forums}"
			      		rendered="#{ForumTool.messagesandForums}" />
			      <h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_discussion_forums}" title=" #{msgs.cdfm_discussion_forums}"
			      		rendered="#{ForumTool.forumsTool}" />
			      <h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " />
			      <h:outputText value="#{ForumTool.selectedForum.forum.title}" />
		</h3>
		<h:panelGrid columns="1" styleClass="forumHeader specialLink">
			<h:panelGroup>
				<h:outputText styleClass="highlight title" id="draft" value="#{msgs.cdfm_draft}" rendered="#{ForumTool.selectedForum.forum.draft == 'true'}"/>
				<h:outputText id="draft_space" value=" -  " rendered="#{ForumTool.selectedForum.forum.draft == 'true'}" styleClass="title"/>
				<h:graphicImage url="/images/silk/date_delete.png" title="#{msgs.forum_restricted_message}" alt="#{msgs.forum_restricted_message}" rendered="#{ForumTool.selectedForum.forum.availability == 'false'}" style="margin-right:.5em"/>
				<h:graphicImage url="/images/silk/lock.png" alt="#{msgs.cdfm_forum_locked}" rendered="#{ForumTool.selectedForum.forum.locked == 'true'}" style="margin-right:.5em"/>	
				<%-- Rubrics marker --%>
				<h:panelGroup rendered="#{ForumTool.selectedForum.hasRubric == 'true'}">
					<sakai-rubric-student-preview-button
						display="icon"
						token="<h:outputText value="#{ForumTool.rbcsToken}" />"
						tool-id="sakai.gradebookng"
						entity-id="<h:outputText value="#{ForumTool.selectedForum.gradeAssign}" />">
					</sakai-rubric-student-preview-button>
				</h:panelGroup>
				<h:outputText value="#{ForumTool.selectedForum.forum.title}" styleClass="title"/>
				<h:outputText value=" "  styleClass="actionLinks"/>
        		<h:commandLink action="#{ForumTool.processActionNewTopic}"  value="#{msgs.cdfm_new_topic}" rendered="#{ForumTool.selectedForum.newTopic}" 
        		               title=" #{msgs.cdfm_new_topic}">
					  <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
				  </h:commandLink>
				  <h:outputText value=" | " rendered="#{ForumTool.selectedForum.changeSettings}"/>
				  <h:commandLink action="#{ForumTool.processActionForumSettings}" value="#{msgs.cdfm_forum_settings}" rendered="#{ForumTool.selectedForum.changeSettings}"
				                 title=" #{msgs.cdfm_forum_settings}">
					  <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
				  </h:commandLink>
				  <h:outputText  value=" | " rendered="#{ForumTool.instructor}"/>
					<h:commandLink action="#{mfStatisticsBean.processActionStatisticsByTopic}" immediate="true" rendered="#{ForumTool.instructor}">
	  				    <f:param value="" name="topicId"/>
	  				    <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
	  				    <h:outputText value="#{msgs.cdfm_button_bar_grade}" />
		          	</h:commandLink>
				<%--
				<h:outputText  value=" | "   rendered="#{ForumTool.selectedForum.changeSettings}"/>
				<h:outputText  value=" Delete "  styleClass="todo"  rendered="#{ForumTool.selectedForum.changeSettings}"/>
				--%>
				<h:outputText value="#{ForumTool.selectedForum.forum.shortDescription}" styleClass="shortDescription"/>
		  		<f:subview id="longDesc" rendered="#{!empty forum.attachList || (ForumTool.selectedForum.forum.extendedDescription != '' &&  ForumTool.selectedForum.forum.extendedDescription != null && ForumTool.selectedForum.forum.extendedDescription != '<br/>')}">

				<h:panelGroup>
					<h:panelGroup layout="block" id="openLinkBlock" styleClass="toggleParent openLinkBlock #{ForumTool.alwaysShowFullDesc ? 'display-none' : ''}">
						<a href="#" id="showMessage" class="toggle show">
							<h:graphicImage url="/images/expand.gif" alt=""/>
							<h:outputText value=" #{msgs.cdfm_read_full_description}" />
							<h:outputText value=" #{msgs.cdfm_and}" rendered="#{!empty ForumTool.selectedForum.attachList}"/>
							<h:outputText value=" #{msgs.cdfm_attach}" rendered="#{!empty ForumTool.selectedForum.attachList}"/>
						</a>
					</h:panelGroup>
					<h:panelGroup layout="block" id="hideLinkBlock" styleClass="toggleParent hideLinkBlock #{ForumTool.alwaysShowFullDesc ? '' : 'display-none'}">
						<a href="#" id="hideMessage" class="toggle show">
							<h:graphicImage url="/images/collapse.gif" alt="" />
							<h:outputText value=" #{msgs.cdfm_hide_full_description}"/>
							<h:outputText value=" #{msgs.cdfm_and}" rendered="#{!empty ForumTool.selectedForum.attachList}" />
							<h:outputText value=" #{msgs.cdfm_attach}" rendered="#{!empty ForumTool.selectedForum.attachList}"/>
						</a>
					</h:panelGroup>
				</h:panelGroup>

				<h:panelGroup layout="block" id="fullTopicDescription" styleClass="textPanel fullTopicDescription #{ForumTool.alwaysShowFullDesc ? 'display-none' : ''}">
					<h:outputText escape="false" value="#{ForumTool.selectedForum.forum.extendedDescription}" />

					<%-- attachments --%>
					<h:dataTable  styleClass="attachListTable" value="#{ForumTool.selectedForum.attachList}" var="eachAttach" rendered="#{!empty ForumTool.selectedForum.attachList}" columnClasses="attach,bogus" border="0" cellpadding="3" cellspacing="0">
						<h:column>
							<sakai:contentTypeMap fileType="#{eachAttach.attachment.attachmentType}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>
							<h:graphicImage id="exampleFileIcon" value="#{imagePath}" alt="" />
						</h:column>
						<h:column>
							<h:outputLink value="#{eachAttach.url}" target="_blank">
								<h:outputText value="#{eachAttach.attachment.attachmentName}" />
							</h:outputLink>
						</h:column>
					</h:dataTable>
				</h:panelGroup>
				
				<f:verbatim></div></f:verbatim>
				</f:subview>
			</h:panelGroup>
		</h:panelGrid>
		  
		<%--//designNote: need a rendered atttrib for the folowing predicated on the existence of topics in this forum--%>
		 <h:outputText value="#{msgs.cdfm_no_topics}" rendered="#{empty ForumTool.selectedForum.topics}"    styleClass="instruction" style="display:block"/>
		  
		<h:dataTable id="topics"  rendered="#{!empty ForumTool.selectedForum.topics}" value="#{ForumTool.selectedForum.topics}" var="topic" width="100%"  cellspacing="0" cellpadding="0">
			<h:column>
				<h:panelGroup layout="block" rendered="#{! topic.nonePermission}">
				<h:panelGrid columns="1" width="100%"  styleClass="topicBloc specialLink"  cellspacing="0" cellpadding="0">
          <h:panelGroup>

						<h:graphicImage url="/images/folder.gif" alt="Topic Folder" rendered="#{topic.unreadNoMessages == 0 }" styleClass="topicIcon" style="margin-right:.5em"/>
						<h:graphicImage url="/images/folder_unread.gif" alt="Topic Folder" rendered="#{topic.unreadNoMessages > 0 }" styleClass="topicIcon" style="margin-right:.5em"/>

						<h:outputText styleClass="highlight title" id="draft" value="#{msgs.cdfm_draft}" rendered="#{topic.topic.draft == 'true'}"/>
						<h:outputText id="draft_space" value="  - " rendered="#{topic.topic.draft == 'true'}" styleClass="title"/>
						<h:graphicImage url="/images/silk/date_delete.png" title="#{msgs.topic_restricted_message}" alt="#{msgs.topic_restricted_message}" rendered="#{topic.availability == 'false'}" style="margin-right:.5em"/>
						<h:graphicImage url="/images/silk/lock.png" alt="#{msgs.cdfm_forum_locked}" rendered="#{ForumTool.selectedForum.forum.locked == 'true' || topic.locked == 'true'}" style="margin-right:.5em"/>
						<%-- Rubrics marker --%>
						<h:panelGroup rendered="#{topic.hasRubric == 'true'}">
							<sakai-rubric-student-preview-button
								display="icon"
								token="<h:outputText value="#{ForumTool.rbcsToken}" />"
								tool-id="sakai.gradebookng"
								entity-id="<h:outputText value="#{topic.gradeAssign}" />">
							</sakai-rubric-student-preview-button>
						</h:panelGroup>
						<h:commandLink action="#{ForumTool.processActionDisplayTopic}" id="topic_title" title=" #{topic.topic.title}" styleClass="title">
						  <f:param value="#{topic.topic.id}" name="topicId"/>
						  <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
						  <h:outputText value="#{topic.topic.title}"/>
					  </h:commandLink>
                      <%-- // display  singular ('unread message') if unread message is  1 --%> 
                      <h:outputText styleClass="childrenNew" id="topic_msg_count_unread" value="  #{topic.unreadNoMessages} #{msgs.cdfm_lowercase_unread_msg}" 
                                     rendered="#{topic.isRead && topic.unreadNoMessages >= 1}"/>		
                
                      <%-- // display  plural ('unread messages') with different style sheet if unread message is 0 --%>  
                      <h:outputText styleClass="childrenNewZero" id="topic_msgs_count0_unread" value="  #{topic.unreadNoMessages} #{msgs.cdfm_lowercase_unread_msg}" 
                                    rendered="#{topic.isRead && topic.unreadNoMessages == 0}"/> 
                            
                      <%-- // display singular ('message') if total message is 1--%>                   
                      <h:outputText styleClass="textPanelFooter" id="topic_msg_count" value="#{msgs.cdfm_of} #{topic.totalNoMessages} #{msgs.cdfm_lowercase_msg}"
                                    rendered="#{topic.isRead && topic.totalNoMessages == 1}"/>
                                                  
                      <%-- // display singular ('message') if total message is 0 or more than 1--%>                   
                      <h:outputText styleClass="textPanelFooter" id="topic_msgs_count" value="#{msgs.cdfm_of} #{topic.totalNoMessages} #{msgs.cdfm_lowercase_msgs}"
                                    rendered="#{topic.isRead && (topic.totalNoMessages > 1 || topic.totalNoMessages == 0)}"/>
                                             
                      <h:outputText id="topic_moderated" value=" #{msgs.cdfm_forum_moderated_flag}" styleClass="textPanelFooter" rendered="#{topic.moderated == 'true' && topic.isRead}" />

						<h:outputText value=" "  styleClass="actionLinks"/>
						<h:commandLink action="#{ForumTool.processActionTopicSettings}" id="topic_setting" value="#{msgs.cdfm_topic_settings}"
						rendered="#{topic.changeSettings}" title=" #{msgs.cdfm_topic_settings}">
							<f:param value="#{topic.topic.id}" name="topicId"/>
							<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
						</h:commandLink>

						<h:outputText  value=" | "   rendered="#{ForumTool.selectedForum.newTopic}" />
						<h:commandLink action="#{ForumTool.processActionDuplicateTopicMainConfirm}" id="duplicate_confirm" value="#{msgs.cdfm_duplicate_topic}" 
							 rendered="#{ForumTool.selectedForum.newTopic}">							
									<f:param value="#{topic.topic.id}" name="topicId"/>
									<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
							</h:commandLink>
							
						<h:outputText  value=" | " rendered="#{ForumTool.instructor}"/>
						<h:commandLink action="#{mfStatisticsBean.processActionStatisticsByTopic}" immediate="true" rendered="#{ForumTool.instructor}">
		  				    <f:param value="#{topic.topic.id}" name="topicId"/>
		  				    <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
		  				    <h:outputText value="#{msgs.cdfm_button_bar_grade}" />
			          	</h:commandLink>
                                    			
						<h:outputText  value=" | "   rendered="#{topic.changeSettings}" />
						<h:commandLink action="#{ForumTool.processActionDeleteTopicMainConfirm}" id="delete_confirm" value="#{msgs.cdfm_button_bar_delete_topic}" 
							accesskey="d" rendered="#{topic.changeSettings}">							
									<f:param value="#{topic.topic.id}" name="topicId"/>
									<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
							</h:commandLink>
									
						
						<h:outputText id="topic_desc" value="#{topic.topic.shortDescription}" styleClass="shortDescription" />
						<f:subview id="longDescTopic" rendered="#{!empty topic.attachList || (topic.topic.extendedDescription != '' &&  topic.topic.extendedDescription != null && topic.topic.extendedDescription != '<br/>')}">

						<h:panelGroup>
							<h:panelGroup layout="block" id="openLinkBlock" styleClass="toggleParent openLinkBlock #{ForumTool.alwaysShowFullDesc ? 'display-none' : ''}">
								<a href="#" id="showMessage" class="toggle show">
									<h:graphicImage url="/images/expand.gif" alt=""/>
									<h:outputText value=" #{msgs.cdfm_read_full_description}" />
									<h:outputText value=" #{msgs.cdfm_and}" rendered="#{!empty topic.attachList}"/>
									<h:outputText value=" #{msgs.cdfm_attach}" rendered="#{!empty topic.attachList}"/>
								</a>
							</h:panelGroup>
							<h:panelGroup layout="block" id="hideLinkBlock" styleClass="toggleParent hideLinkBlock #{ForumTool.alwaysShowFullDesc ? '' : 'display-none'}">
								<a href="#" id="hideMessage" class="toggle show">
									<h:graphicImage url="/images/collapse.gif" alt="" />
									<h:outputText value=" #{msgs.cdfm_hide_full_description}"/>
									<h:outputText value=" #{msgs.cdfm_and}" rendered="#{!empty topic.attachList}" />
									<h:outputText value=" #{msgs.cdfm_attach}" rendered="#{!empty topic.attachList}"/>
								</a>
							</h:panelGroup>
						</h:panelGroup>

					<h:panelGroup layout="block" id="fullTopicDescription" styleClass="textPanel #{ForumTool.alwaysShowFullDesc ? 'display-none' : ''}">
						<h:outputText escape="false" value="#{topic.topic.extendedDescription}" />

						<div class="table-responsive">
							<h:panelGroup rendered="#{!empty topic.attachList}">
								<h:dataTable styleClass="table table-hover table-striped table-bordered attachListTable" value="#{topic.attachList}" var="eachAttach" border="0" cellpadding="3" cellspacing="0" columnClasses="attach,bogus">
									<h:column>
										<h:graphicImage url="/images/attachment.gif" alt="" />
									</h:column>
									<h:column>
										<h:outputLink value="#{eachAttach.url}" target="_blank">
											<h:outputText value="#{eachAttach.attachment.attachmentName}" />
										</h:outputLink>
									</h:column>
								</h:dataTable>
							</h:panelGroup>
						</div>
					</h:panelGroup>
			<f:verbatim></div></f:verbatim>
			</f:subview>
					</h:panelGroup>
				</h:panelGrid>
				</h:panelGroup>
			</h:column>
			</h:dataTable>
		<h:inputHidden id="mainOrForumOrTopic" value="dfForumDetail" />
		<%
  String thisId = request.getParameter("panel");
  if (thisId == null) 
  {
    thisId = "Main" + org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement().getId();
  }
%>
			<script>

			function resize(){
  				mySetMainFrameHeight('<%= org.sakaiproject.util.Web.escapeJavascript(thisId)%>');
  			}
			</script> 
	 </h:form>
	 <h:outputText value="#{msgs.cdfm_insufficient_privileges_view_forum}" rendered="#{ForumTool.selectedForum.forum.draft && ForumTool.selectedForum.forum.createdBy != ForumTool.userId}" />
    </sakai:view>
</f:view>
