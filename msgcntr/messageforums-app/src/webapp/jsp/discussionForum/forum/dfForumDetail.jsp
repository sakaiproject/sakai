<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>
<f:view>
  <sakai:view>

    <h:form id="msgForum" rendered="#{!ForumTool.selectedForum.forum.draft || ForumTool.selectedForum.forum.createdBy == ForumTool.userId}">
  		<script type="text/javascript" src="/library/js/jquery.js"></script>
  		<sakai:script contextBase="/sakai-messageforums-tool" path="/js/sak-10625.js"/>
    	<sakai:script contextBase="/sakai-messageforums-tool" path="/js/forum.js"/>
<!--jsp/discussionForum/forum/dfForumDetail.jsp-->
      <h:panelGrid columns="2" summary="layout" width="100%" styleClass="navPanel  specialLink">
        <h:panelGroup>
          	<f:verbatim><div class="breadCrumb"><h3></f:verbatim>
          		<%-- Display the proper home page link: either Messages & Forums OR Forums --%>
			      <h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_message_forums}" title=" #{msgs.cdfm_message_forums}"
			      		rendered="#{ForumTool.messagesandForums}" />
			      <h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_discussion_forums}" title=" #{msgs.cdfm_discussion_forums}"
			      		rendered="#{ForumTool.forumsTool}" />
			      <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
			      <h:outputText value="#{ForumTool.selectedForum.forum.title}" />
			    <f:verbatim></h3></div></f:verbatim>
        </h:panelGroup>
        <h:panelGroup styleClass="itemNav">
        		<h:commandLink action="#{ForumTool.processActionNewTopic}"  value="#{msgs.cdfm_new_topic}" rendered="#{ForumTool.selectedForum.newTopic}" 
        		               title=" #{msgs.cdfm_new_topic}">
					  <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
				  </h:commandLink>
				  <f:verbatim><h:outputText value=" | " rendered="#{ForumTool.selectedForum.changeSettings}"/></f:verbatim>
				  <h:commandLink action="#{ForumTool.processActionForumSettings}" value="#{msgs.cdfm_forum_settings}" rendered="#{ForumTool.selectedForum.changeSettings}"
				                 title=" #{msgs.cdfm_forum_settings}">
					  <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
				  </h:commandLink>
        </h:panelGroup>
      </h:panelGrid>
      <f:verbatim><div class="hierItemBlockWrapper"></f:verbatim>
      	<f:verbatim><div class="hierItemBlock"></f:verbatim>
		  <p class="textPanel">
		  
		  <h4>
		  <h:outputText value="#{ForumTool.selectedForum.forum.title}" />
		  </h4>
		  
		  <h:outputText value="#{ForumTool.selectedForum.forum.shortDescription}" />
		  </p>
		  <p class="textPanelFooter specialLink">

			  <h:outputLink id="forum_extended_show" value="#" title="#{msgs.cdfm_read_full_description}" styleClass="show" 
			  		rendered="#{ForumTool.selectedForum.forum.extendedDescription != '' && ForumTool.selectedForum.forum.extendedDescription != null}"
			  		onclick="toggleExtendedDescription($(this).next('.hide'), $('div.toggle:first', $(this).parents('div.hierItemBlock')), $(this));">
			  		<h:outputText value="#{msgs.cdfm_read_full_description}" />
			  </h:outputLink>
			  
			  <h:outputLink id="forum_extended_hide" value="#" title="#{msgs.cdfm_hide_full_description}" style="display:none" styleClass="hide" 
			  		rendered="#{ForumTool.selectedForum.forum.extendedDescription != '' && ForumTool.selectedForum.forum.extendedDescription != null}"
			  		onclick="toggleExtendedDescription($(this).prev('.show'), $('div.toggle:first', $(this).parents('div.hierItemBlock')), $(this));">
			  		<h:outputText value="#{msgs.cdfm_hide_full_description}" />
			  </h:outputLink>

		    
		</p>

			 	<f:verbatim><div class="toggle" style="display:none"></f:verbatim>
				<mf:htmlShowArea value="#{ForumTool.selectedForum.forum.extendedDescription}"  
		                     hideBorder="true" />

				<f:verbatim></div></f:verbatim>
		  
		  


		<f:verbatim></div></f:verbatim>
		<h:dataTable id="topics" styleClass="topicBloc" value="#{ForumTool.selectedForum.topics}" var="topic" width="100%"  cellspacing="0" cellpadding="0">
			<h:column rendered="#{! topic.nonePermission}">
			<f:verbatim><div class="hierItemBlockChild"></f:verbatim>	
        <h:panelGrid columns="2" summary="layout" width="100%"  styleClass="topicHeadings specialLink" columnClasses="bogus,itemAction" cellspacing="0" cellpadding="0">
          <h:panelGroup>
			 	<f:verbatim><h4></f:verbatim>
				    <h:commandLink action="#{ForumTool.processActionDisplayTopic}" id="topic_title" value="#{topic.topic.title}" title=" #{topic.topic.title}">
						  <f:param value="#{topic.topic.id}" name="topicId"/>
						  <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
					  </h:commandLink>
					 <f:verbatim></h4></f:verbatim> 
						<h:outputText styleClass="textPanelFooter" id="topic_msg_count" value=" #{msgs.cdfm_openb} #{topic.totalNoMessages} #{msgs.cdfm_lowercase_msg} - #{topic.unreadNoMessages} #{msgs.cdfm_unread}" rendered="#{topic.isRead && topic.totalNoMessages < 2}"/>
						<h:outputText styleClass="textPanelFooter" id="topic_msgs_count" value=" #{msgs.cdfm_openb} #{topic.totalNoMessages} #{msgs.cdfm_lowercase_msgs} - #{topic.unreadNoMessages} #{msgs.cdfm_unread}" rendered="#{topic.isRead && topic.totalNoMessages > 1}"/>
				  	<h:outputText id="topic_moderated" value="#{msgs.cdfm_topic_moderated_flag}" styleClass="textPanelFooter" rendered="#{topic.moderated == 'true'}" />
    	      <h:outputText value=" #{msgs.cdfm_closeb}"styleClass="textPanelFooter" />
				  </h:panelGroup>
				  <h:panelGroup styleClass="msgNav">
						<h:commandLink action="#{ForumTool.processActionTopicSettings}" id="topic_setting" value="#{msgs.cdfm_topic_settings}"
						rendered="#{topic.changeSettings}" title=" #{msgs.cdfm_topic_settings}">
							<f:param value="#{topic.topic.id}" name="topicId"/>
							<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
						</h:commandLink>
				  </h:panelGroup>
			  </h:panelGrid>
			<f:verbatim><div class="textPanel"></f:verbatim>  
			<h:panelGroup><h:outputText id="topic_desc" value="#{topic.topic.shortDescription}" /></h:panelGroup>
			<f:verbatim></div></f:verbatim>
			<f:verbatim><div class="textPanel"></f:verbatim>
			<h:panelGroup styleClass="textPanelFooter specialLink">
			    	<h:outputLink id="forum_extended_show" value="#" title="#{msgs.cdfm_read_full_description}" styleClass="show" 
			    		rendered="#{topic.topic.extendedDescription != '' && topic.topic.extendedDescription != null}"
				  		onclick="toggleExtendedDescription($(this).next('.hide'), $('div.toggle:first', $(this).parents('div.textPanel')), $(this));">
				  		<h:outputText value="#{msgs.cdfm_read_full_description}" />
				    </h:outputLink>  
				  
				    <h:outputLink id="forum_extended_hide" value="#" title="#{msgs.cdfm_hide_full_description}" style="display:none" styleClass="hide" 
				    	rendered="#{topic.topic.extendedDescription != '' && topic.topic.extendedDescription != null}"
				  		onclick="toggleExtendedDescription($(this).prev('.show'), $('div.toggle:first', $(this).parents('div.textPanel')), $(this));">
				  		<h:outputText value="#{msgs.cdfm_hide_full_description}" />
				    </h:outputLink>

				 </h:panelGroup>
				<%--	gsilver: show the text, not the editor... --%>
				<h:panelGroup styleClass="textPanel">
					<f:verbatim><div class="toggle" style="display:none"></f:verbatim>
					<mf:htmlShowArea  id="topic_fullDescription" hideBorder="true"	 value="#{topic.topic.extendedDescription}" />
		 			<%--  <sakai:inputRichText rows="5" cols="110" buttonSet="none"  readonly="true" showXPath="false" id="topic_extended_description" value="#{topic.topic.extendedDescription}" rendered="#{topic.readFullDesciption}"/> --%>
					<f:verbatim></div></f:verbatim>
				</h:panelGroup>
				
				<h:dataTable styleClass="listHier" value="#{topic.attachList}" var="eachAttach" rendered="#{!empty topic.attachList}" cellpadding="0" cellspacing="0" columnClasses="attach,bogus" summary="layout">
					  <h:column>
					  <%-- gsilver: need to tie in the attachment type to actual  MIME type mapping tables instead of the below (which is prevalent everywhere) or at the very least provide a mechanism for defaults. --%> 
						<sakai:contentTypeMap fileType="#{eachAttach.attachment.attachmentType}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>									
						<h:graphicImage id="exampleFileIcon" value="#{imagePath}" />						
						</h:column>
						<h:column>
						<h:outputLink value="#{eachAttach.url}" target="_blank">
							<h:outputText value="#{eachAttach.attachment.attachmentName}" />
						</h:outputLink>				  
					</h:column>
			  </h:dataTable>
				
				
			<f:verbatim></div></f:verbatim>
			
			<%-- gsilver:need a rendered attribute on this next block - or is this just a forgotten bit of cruft? a placeholder to display responses in context?--%>
			<%-- %>
			<h:dataTable id="messages" value="#{topics.messages}" var="message">
			<h:column>
				<h:outputText id="message_title" value="#{message.message.title}"/>
				<f:verbatim><br /></f:verbatim>
				<h:outputText id="message_desc" value="#{message.message.shortDescription}" />
			</h:column>
			</h:dataTable>
			--%>
				<f:verbatim></div></f:verbatim>
				</h:column>
		</h:dataTable>
		<f:verbatim></div></f:verbatim>
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
	 </h:form>
	 <h:outputText value="#{msgs.cdfm_insufficient_privileges_view_forum}" rendered="#{ForumTool.selectedForum.forum.draft && ForumTool.selectedForum.forum.createdBy != ForumTool.userId}" />
    </sakai:view>
</f:view>

