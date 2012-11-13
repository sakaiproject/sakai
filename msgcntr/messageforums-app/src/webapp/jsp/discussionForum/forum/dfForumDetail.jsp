<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>
<f:view>
<sakai:view toolCssHref="/messageforums-tool/css/msgcntr.css">
    <h:form id="msgForum" rendered="#{!ForumTool.selectedForum.forum.draft || ForumTool.selectedForum.forum.createdBy == ForumTool.userId}">
		<script type="text/javascript" src="/library/js/jquery/1.4.2/jquery-1.4.2.min.js"></script>
  		<sakai:script contextBase="/messageforums-tool" path="/js/sak-10625.js"/>
    	<sakai:script contextBase="/messageforums-tool" path="/js/forum.js"/>
<!--jsp/discussionForum/forum/dfForumDetail.jsp-->

			<h:outputText styleClass="showMoreText"  style="display:none" value="#{msgs.cdfm_show_more_full_description}"  />


		<h3 class="specialLink" style="margin-bottom:1em">
          		<%-- Display the proper home page link: either Messages & Forums OR Forums --%>
			      <h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_message_forums}" title=" #{msgs.cdfm_message_forums}"
			      		rendered="#{ForumTool.messagesandForums}" />
			      <h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_discussion_forums}" title=" #{msgs.cdfm_discussion_forums}"
			      		rendered="#{ForumTool.forumsTool}" />
			      <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
			      <h:outputText value="#{ForumTool.selectedForum.forum.title}" />
		</h3>
		<h:panelGrid columns="1" styleClass="forumHeader specialLink">
			<h:panelGroup>
				<h:outputText styleClass="highlight title" id="draft" value="#{msgs.cdfm_draft}" rendered="#{ForumTool.selectedForum.forum.draft == 'true'}"/>
				<h:outputText id="draft_space" value=" -  " rendered="#{ForumTool.selectedForum.forum.draft == 'true'}" styleClass="title"/>
				<h:graphicImage url="/images/silk/date_delete.png" title="#{msgs.forum_restricted_message}" alt="#{msgs.forum_restricted_message}" rendered="#{ForumTool.selectedForum.forum.availability == 'false'}" style="margin-right:.5em"/>
				<h:graphicImage url="/images/silk/lock.png" alt="#{msgs.cdfm_forum_locked}" rendered="#{ForumTool.selectedForum.forum.locked == 'true'}" style="margin-right:.5em"/>	
				<h:outputText value="#{ForumTool.selectedForum.forum.title}" styleClass="title"/>
				<h:outputText value=" "  styleClass="actionLinks"/>
        		<h:commandLink action="#{ForumTool.processActionNewTopic}"  value="#{msgs.cdfm_new_topic}" rendered="#{ForumTool.selectedForum.newTopic}" 
        		               title=" #{msgs.cdfm_new_topic}">
					  <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
				  </h:commandLink>
				  <f:verbatim><h:outputText value=" | " rendered="#{ForumTool.selectedForum.changeSettings}"/></f:verbatim>
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
		  
				<h:outputLink id="forum_extended_show" value="#" title="#{msgs.cdfm_view}"  styleClass="show"
						rendered="#{ForumTool.selectedForum.forum.extendedDescription != '' && ForumTool.selectedForum.forum.extendedDescription != null && ForumTool.selectedForum.forum.extendedDescription != '<br/>'}"
						onclick="resize();$(this).next('.hide').toggle(); $('div.toggle:first', $(this).parents('table.forumHeader')).slideToggle(resize);$(this).toggle();">
					<h:graphicImage url="/images/collapse.gif" /><h:outputText value="#{msgs.cdfm_view}" />
					<h:outputText value=" #{msgs.cdfm_full_description}"  rendered="#{ForumTool.selectedForum.forum.extendedDescription != '' && ForumTool.selectedForum.forum.extendedDescription != null && ForumTool.selectedForum.forum.extendedDescription != '<br/>'}"/>
			  </h:outputLink>
			  
				<h:outputLink id="forum_extended_hide" value="#" title="#{msgs.cdfm_hide}" style="display:none;" styleClass="hide" 
				rendered="#{ForumTool.selectedForum.forum.extendedDescription != '' && ForumTool.selectedForum.forum.extendedDescription != null && ForumTool.selectedForum.forum.extendedDescription != '<br/>'}"
						onclick="resize();$(this).prev('.show').toggle(); $('div.toggle:first', $(this).parents('table.forumHeader')).slideToggle(resize);$(this).toggle();">
					<h:graphicImage url="/images/expand.gif"/> <h:outputText value="#{msgs.cdfm_hide}" />
					<h:outputText value=" #{msgs.cdfm_full_description}"  rendered="#{ForumTool.selectedForum.forum.extendedDescription != '' && ForumTool.selectedForum.forum.extendedDescription != null && ForumTool.selectedForum.forum.extendedDescription != '<br/>'}"/>
			  </h:outputLink>

			 	<f:verbatim><div class="toggle" style="display:none"></f:verbatim>
				<mf:htmlShowArea value="#{ForumTool.selectedForum.forum.extendedDescription}"  
		                     hideBorder="true" />
					<%--//designNote: need thge atts list but it is not in scope --%>
					<%--
						<h:dataTable  value="#{ForumTool.selectedForum.forum.attachList}" var="eachAttach" rendered="#{!empty ForumTool.selectedForum.forum.attachList}" columnClasses="attach,bogus"  summary="layout" style="font-size:.9em;width:auto;margin-left:1em" border="0" cellpadding="3" cellspacing="0">
							<h:column>
								<sakai:contentTypeMap fileType="#{eachAttach.attachment.attachmentType}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>
  								<h:graphicImage id="exampleFileIcon" value="#{imagePath}" />
							</h:column>
							<h:column>
								<h:outputLink value="#{eachAttach.url}" target="_blank">
									<h:outputText value="#{eachAttach.attachment.attachmentName}"  />
								</h:outputLink>			
							</h:column>	
						</h:dataTable>
					--%>	
				<f:verbatim></div></f:verbatim>
			</h:panelGroup>
		</h:panelGrid>
		  
		<%--//designNote: need a rendered atttrib for the folowing predicated on the existence of topics in this forum--%>
		 <h:outputText value="#{msgs.cdfm_no_topics}" rendered="#{empty ForumTool.selectedForum.topics}"    styleClass="instruction" style="display:block"/>
		  
		<h:dataTable id="topics"  rendered="#{!empty ForumTool.selectedForum.topics}" value="#{ForumTool.selectedForum.topics}" var="topic" width="100%"  cellspacing="0" cellpadding="0">
			<h:column rendered="#{! topic.nonePermission}">
				<h:panelGrid columns="1" width="100%"  styleClass="topicBloc specialLink"  cellspacing="0" cellpadding="0">
          <h:panelGroup>

						<h:graphicImage url="/images/folder.gif" alt="Topic Folder" rendered="#{topic.unreadNoMessages == 0 }" styleClass="topicIcon" style="margin-right:.5em"/>
						<h:graphicImage url="/images/folder_unread.gif" alt="Topic Folder" rendered="#{topic.unreadNoMessages > 0 }" styleClass="topicIcon" style="margin-right:.5em"/>


						<h:outputText styleClass="highlight title" id="draft" value="#{msgs.cdfm_draft}" rendered="#{topic.topic.draft == 'true'}"/>
						<h:outputText id="draft_space" value="  - " rendered="#{topic.topic.draft == 'true'}" styleClass="title"/>
						<h:graphicImage url="/images/silk/date_delete.png" title="#{msgs.topic_restricted_message}" alt="#{msgs.topic_restricted_message}" rendered="#{topic.availability == 'false'}" style="margin-right:.5em"/>
						<h:graphicImage url="/images/silk/lock.png" alt="#{msgs.cdfm_forum_locked}" rendered="#{ForumTool.selectedForum.forum.locked == 'true' || topic.locked == 'true'}" style="margin-right:.5em"/>

						<h:commandLink action="#{ForumTool.processActionDisplayTopic}" id="topic_title" value="#{topic.topic.title}" title=" #{topic.topic.title}" styleClass="title">
						  <f:param value="#{topic.topic.id}" name="topicId"/>
						  <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
					  </h:commandLink>
						<h:outputText styleClass="textPanelFooter" id="topic_msg_count" value=" #{msgs.cdfm_openb} #{topic.totalNoMessages} #{msgs.cdfm_lowercase_msg} - #{topic.unreadNoMessages} #{msgs.cdfm_unread}" rendered="#{topic.isRead && topic.totalNoMessages == 1}"/>
						<h:outputText styleClass="textPanelFooter" id="topic_msgs_count" value=" #{msgs.cdfm_openb} #{topic.totalNoMessages} #{msgs.cdfm_lowercase_msgs} - #{topic.unreadNoMessages} #{msgs.cdfm_unread}" rendered="#{topic.isRead && (topic.totalNoMessages > 1 ||  topic.totalNoMessages == 0)}"/>




				  	<h:outputText id="topic_moderated" value="#{msgs.cdfm_topic_moderated_flag}" styleClass="textPanelFooter" rendered="#{topic.moderated == 'true'}" />
    	      <h:outputText value=" #{msgs.cdfm_closeb}" styleClass="textPanelFooter" rendered="#{topic.isRead}" />

						<h:outputText styleClass="childrenNew" value=" #{msgs.cdfm_newflagparent}"  rendered="#{topic.unreadNoMessages > 0 }" />

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
												
						<h:outputLink id="forum_extended_show" value="#" title="#{msgs.cdfm_view}" styleClass="show"
								rendered="#{!empty topic.attachList || topic.topic.extendedDescription != '' && topic.topic.extendedDescription != null && topic.topic.extendedDescription != '<br/>'}"
								onclick="resize();$(this).next('.hide').toggle(); $('td div.toggle', $(this).parents('tr:first').next('tr')).slideToggle(resize);$(this).toggle();">
								<h:graphicImage url="/images/collapse.gif"/><h:outputText value="#{msgs.cdfm_view}" />
								<h:outputText value=" #{msgs.cdfm_full_description}" rendered="#{topic.topic.extendedDescription != '' && topic.topic.extendedDescription != null && topic.topic.extendedDescription != '<br/>'}"/>
								<h:outputText value=" #{msgs.cdfm_and}" rendered="#{!empty topic.attachList && topic.topic.extendedDescription != '' && topic.topic.extendedDescription != null && topic.topic.extendedDescription != '<br/>'}"/>
								<h:outputText value=" #{msgs.cdfm_attach}" rendered="#{!empty topic.attachList}"/>
				    </h:outputLink>  
				  
						<h:outputLink id="forum_extended_hide" value="#" title="#{msgs.cdfm_hide}" style="display:none " styleClass="hide" 
								rendered="#{!empty topic.attachList || topic.topic.extendedDescription != '' && topic.topic.extendedDescription != null && topic.topic.extendedDescription != '<br/>'}"
								onclick="resize();$(this).prev('.show').toggle(); $('td div.toggle', $(this).parents('tr:first').next('tr')).slideToggle(resize);$(this).toggle();">
								<h:graphicImage url="/images/expand.gif"/><h:outputText value="#{msgs.cdfm_hide}" />
								<h:outputText value=" #{msgs.cdfm_full_description}" rendered="#{topic.topic.extendedDescription != '' && topic.topic.extendedDescription != null && topic.topic.extendedDescription != '<br/>'}"/>
								<h:outputText value=" #{msgs.cdfm_and}" rendered="#{!empty topic.attachList && topic.topic.extendedDescription != '' && topic.topic.extendedDescription != null && topic.topic.extendedDescription != '<br/>'}"/>
								<h:outputText value=" #{msgs.cdfm_attach}" rendered="#{!empty topic.attachList}"/>
				    </h:outputLink>

				 </h:panelGroup>
					<h:panelGroup>
					<f:verbatim><div class="toggle" style="display:none"></f:verbatim>
					<mf:htmlShowArea  id="topic_fullDescription" hideBorder="true"	 value="#{topic.topic.extendedDescription}" />
		 			<%--  <sakai:inputRichText rows="5" cols="110" buttonSet="none"  readonly="true" showXPath="false" id="topic_extended_description" value="#{topic.topic.extendedDescription}" rendered="#{topic.readFullDesciption}"/> --%>
				
							<h:dataTable styleClass="listHier attachListTable" value="#{topic.attachList}" var="eachAttach" rendered="#{!empty topic.attachList}" style="font-size:.9em;width:auto;margin-left:1em" border="0" cellpadding="3" cellspacing="0" columnClasses="attach,bogus">
					  <h:column>
									<h:graphicImage url="/images/attachment.gif" />
						</h:column>
						<h:column>
						<h:outputLink value="#{eachAttach.url}" target="_blank">
							<h:outputText value="#{eachAttach.attachment.attachmentName}" />
						</h:outputLink>				  
					</h:column>
			  </h:dataTable>
			<f:verbatim></div></f:verbatim>
					</h:panelGroup>
				</h:panelGrid>
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
			<script type="text/javascript">

			function resize(){
  				mySetMainFrameHeight('<%= org.sakaiproject.util.Web.escapeJavascript(thisId)%>');
  			}
			</script> 
<h:outputText escape="false" value="<script type='text/javascript'>$(document).ready(function() {setupLongDesc()});</script>"  rendered="#{!ForumTool.showShortDescription}"/>
	 </h:form>
	 <h:outputText value="#{msgs.cdfm_insufficient_privileges_view_forum}" rendered="#{ForumTool.selectedForum.forum.draft && ForumTool.selectedForum.forum.createdBy != ForumTool.userId}" />
    </sakai:view>
</f:view>

