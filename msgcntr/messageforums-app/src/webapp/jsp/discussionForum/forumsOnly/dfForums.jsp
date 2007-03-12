<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
                 
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>

<f:view>
  <sakai:view title="#{msgs.cdfm_discussion_forums}">

	<h:form id="msgForum">
	
		<h:panelGroup rendered="#{PrivateMessagesTool.instructor}">
		  <sakai:tool_bar separator="#{msgs.cdfm_toolbar_separator}">
        <sakai:tool_bar_item value="#{msgs.cdfm_new_forum}" action="#{ForumTool.processActionNewForum}" />
        <sakai:tool_bar_item value=" #{msgs.cdfm_organize}" action="#{ForumTool.processActionTemplateOrganize}"  />
			  <sakai:tool_bar_item value=" #{msgs.cdfm_template_setting}" action="#{ForumTool.processActionTemplateSettings}" />
			  <sakai:tool_bar_item value=" #{msgs.stat_list}" action="#{ForumTool.processActionStatistics}" />
			  <sakai:tool_bar_item value=" #{msgs.cdfm_msg_pending_queue} #{msgs.cdfm_openb}#{ForumTool.numPendingMessages}#{msgs.cdfm_closeb}" action="#{ForumTool.processPendingMsgQueue}" rendered="#{ForumTool.displayPendingMsgQueue}" />
      </sakai:tool_bar>
 		</h:panelGroup>
 	
 	<div><h3><h:outputText value="#{msgs.cdfm_discussion_forums}" /></h3></div>
 	
<!--jsp/discussionForum/area/dfArea.jsp-->
  <h:dataTable id="forums" value="#{ForumTool.forums}" width="100%" var="forum" cellpadding="0" cellspacing="0" summary="layout">
    <h:column rendered="#{! forum.nonePermission}">
    <f:verbatim><div class="hierItemBlockWrapper"></f:verbatim>
    <h:panelGrid columns="2" styleClass="hierItemBlock specialLink" columnClasses="bogus,itemAction" summary="layout" style="width:100%;">
  	    <h:panelGroup>
  	      <h:outputText styleClass="highlight" id="draft" value="#{msgs.cdfm_draft}" rendered="#{forum.forum.draft == 'true'}"/>
	      <h:outputText id="draft_space" value=" -  " rendered="#{forum.forum.draft == 'true'}"/>
		  
	
	      <h:graphicImage url="/images/lock.gif" alt="#{msgs.cdfm_forum_locked}"  rendered="#{forum.locked == 'true'}"/>
	      <h:outputText id="emptyspace" value="  " rendered="#{forum.locked == 'true'}"/>
	      <f:verbatim><h4></f:verbatim>
	        <h:commandLink action="#{ForumTool.processActionDisplayForum}"  value="#{forum.forum.title}" title=" #{forum.forum.title}">
		        <f:param value="#{forum.forum.id}" name="forumId"/>
	        </h:commandLink>
	      <f:verbatim></h4></f:verbatim>
	  	  </h:panelGroup>
	  	  <h:panelGroup styleClass="msgNav">
			  <h:commandLink action="#{ForumTool.processActionNewTopic}" value="#{msgs.cdfm_new_topic}" rendered="#{forum.newTopic}" title="#{msgs.cdfm_new_topic}">
		      <f:param value="#{forum.forum.id}" name="forumId"/>
	      </h:commandLink>
		  <h:outputText  value=" | " rendered="#{forum.changeSettings}"/><%-- gsilver: hiding the pipe when user does not have the ability to change the settings --%>
	   	  <h:commandLink action="#{ForumTool.processActionForumSettings}"  value="#{msgs.cdfm_forum_settings}" rendered="#{forum.changeSettings}" title="#{msgs.cdfm_forum_settings}">
		      <f:param value="#{forum.forum.id}" name="forumId"/>				
	      </h:commandLink>
  	    </h:panelGroup>


		<h:panelGroup>
		

<%-- the forum details --%>
	  
 	  <h:panelGrid columns="1" cellpadding="0" cellspacing="0" summary="layout">  
  	    	<h:panelGroup styleClass="textPanel">
			  <h:outputText id="forum_desc" value="#{forum.forum.shortDescription}" />
  	    </h:panelGroup>
  	    
  	    	<h:panelGroup  styleClass="textPanelFooter specialLink">
				<%--gsilver: would be good if the returned url from this would include a named internal anchor as the target so that the expando/collapso would go to the top of the viewport and avoid having to scroll and find --%>
			  <h:commandLink immediate="true" action="#{ForumTool.processActionToggleDisplayForumExtendedDescription}" rendered="#{forum.hasExtendedDesciption}"
				               id="forum_extended_show" value="#{msgs.cdfm_read_full_description}" title="#{msgs.cdfm_read_full_description}">
				  <f:param value="#{forum.forum.id}" name="forumId_displayExtended"/>
				  <f:param value="displayHome" name="redirectToProcessAction"/>
		    </h:commandLink>
		    <h:commandLink immediate="true" action="#{ForumTool.processActionToggleDisplayForumExtendedDescription}" id="forum_extended_hide"
				               value="#{msgs.cdfm_hide_full_description}" rendered="#{forum.readFullDesciption}" title="#{msgs.cdfm_hide_full_description}">
				  <f:param value="#{forum.forum.id}" name="forumId_hideExtended"/>
				  <f:param value="displayHome" name="redirectToProcessAction"/>
		    </h:commandLink>
			 </h:panelGroup>
			 <h:panelGroup styleClass="textPanel">
			<%--	gsilver: show the text, not the editor... --%>
			<%--	gsilver: h:panelGroup renders  a span for a reason known only to the jsf gods. spans cannot contain block level elements according to the xhtml dtd - and also rendering is whacked when this happens in some browsers - users can create any number of block level elements with the editor du jour, so....--%>
			<mf:htmlShowArea  id="forum_fullDescription" hideBorder="true"	 value="#{forum.forum.extendedDescription}" rendered="#{forum.readFullDesciption}"/>
<%--	 	    <sakai:inputRichText rows="5" cols="110" buttonSet="none" readonly="true" showXPath="false" id="forum_extended_description" value="#{forum.forum.extendedDescription}" rendered="#{forum.readFullDesciption}"/> --%>
  	    </h:panelGroup>
  	  </h:panelGrid>
	  <h:dataTable  value="#{forum.attachList}" var="eachAttach" rendered="#{!empty forum.attachList}" columnClasses="attach,bogus" styleClass="listHier" summary="layout">
			<h:column>
				<h:graphicImage url="/images/excel.gif" rendered="#{eachAttach.attachment.attachmentType == 'application/vnd.ms-excel'}" alt="" />
				<h:graphicImage url="/images/html.gif" rendered="#{eachAttach.attachment.attachmentType == 'text/html'}" alt="" />
				<h:graphicImage url="/images/pdf.gif" rendered="#{eachAttach.attachment.attachmentType == 'application/pdf'}" alt="" />
				<h:graphicImage url="/images/ppt.gif" rendered="#{eachAttach.attachment.attachmentType == 'application/vnd.ms-powerpoint'}" alt="" />
				<h:graphicImage url="/images/text.gif" rendered="#{eachAttach.attachment.attachmentType == 'text/plain'}" alt="" />
				<h:graphicImage url="/images/word.gif" rendered="#{eachAttach.attachment.attachmentType == 'application/msword'}" alt="" />
		  </h:column>
		  <h:column>
<%--				<h:outputLink value="#{eachAttach.attachmentUrl}" target="_blank">
					<h:outputText value="#{eachAttach.attachmentName}"  />
				</h:outputLink>--%>
				<h:outputLink value="#{eachAttach.url}" target="_blank">
					<h:outputText value="#{eachAttach.attachment.attachmentName}"  />
				</h:outputLink>			
			</h:column>	
	  </h:dataTable>
	  
	  </h:panelGroup>
  </h:panelGrid>

	  <%-- the topic list  --%>
	  
	  
	  	<%--gsilver: need a rendered atttrib for the folowing predicated on the existence of topics in this forum--%>
		 <h:dataTable id="topics" value="#{forum.topics}" var="topic" width="100%" styleClass="topicBloc"  cellspacing="0" cellpadding="0" summary="layout">
		   <h:column rendered="#{! topic.nonePermission}">
				<f:verbatim><div class="hierItemBlockChild"></f:verbatim>
		      <h:panelGrid columns="2" summary="layout" width="100%" styleClass="specialLink" cellpadding="0" cellspacing="0" columnClasses="bogus,itemAction">
		      	<h:panelGroup>
						<h:outputText styleClass="highlight" id="draft" value="#{msgs.cdfm_draft}" rendered="#{topic.topic.draft == 'true'}"/>
				      <h:outputText id="draft_space" value="  - " rendered="#{topic.topic.draft == 'true'}"/>
				      <h:graphicImage url="/images/lock.gif" alt="#{msgs.cdfm_forum_locked}"  rendered="#{forum.locked == 'true' || topic.locked == 'true'}"/>
				      <h:outputText id="emptyspace" value="  " rendered="#{topic.locked == 'true'}"/>
						<f:verbatim><h5></f:verbatim>
				      <h:commandLink action="#{ForumTool.processActionDisplayTopic}" id="topic_title" value="#{topic.topic.title}" title=" #{topic.topic.title}">
					      <f:param value="#{topic.topic.id}" name="topicId"/>
					      <f:param value="#{forum.forum.id}" name="forumId"/>
				      </h:commandLink>
					  <f:verbatim></h5></f:verbatim>
				     <h:outputText styleClass="textPanelFooter" id="topic_msg_count55" value=" #{msgs.cdfm_openb} #{topic.totalNoMessages} #{msgs.cdfm_lowercase_msg} - #{topic.unreadNoMessages} #{msgs.cdfm_unread}" 
				                    rendered="#{topic.isRead && topic.totalNoMessages < 2}"/>
					   <h:outputText id="topic_msg_count56" value=" #{msgs.cdfm_openb} #{topic.totalNoMessages} #{msgs.cdfm_lowercase_msgs} - #{topic.unreadNoMessages} #{msgs.cdfm_unread}" 
				                    rendered="#{topic.isRead && topic.totalNoMessages > 1}" styleClass="textPanelFooter" />
				     <h:outputText id="topic_moderated" value="#{msgs.cdfm_topic_moderated_flag}" styleClass="textPanelFooter" rendered="#{topic.moderated == 'true' && topic.isRead}" />
    	        <h:outputText value=" #{msgs.cdfm_closeb}"styleClass="textPanelFooter" rendered="#{topic.isRead}"/>
    	        </h:panelGroup>
    	        <h:panelGroup styleClass="msgNav">
    	         <h:commandLink action="#{ForumTool.processActionTopicSettings}" id="topic_setting" value="#{msgs.cdfm_topic_settings}" rendered="#{topic.changeSettings}"
    	                        title=" #{msgs.cdfm_topic_settings}">
					     <f:param value="#{topic.topic.id}" name="topicId"/>
				       <f:param value="#{forum.forum.id}" name="forumId"/>
				     </h:commandLink>
    	        </h:panelGroup>
    	      </h:panelGrid>
				<h:panelGrid columns="1" width="100%"  cellpadding="0" cellspacing="0" summary="layout">
    	        <h:panelGroup styleClass="textPanel">
    	          <h:outputText id="topic_desc" value="#{topic.topic.shortDescription}" />
    	        </h:panelGroup>
			    <h:panelGroup styleClass="textPanelFooter specialLink">
				    <h:commandLink immediate="true" action="#{ForumTool.processActionToggleDisplayExtendedDescription}" rendered="#{topic.hasExtendedDesciption}" id="topic_extended_show" value="#{msgs.cdfm_read_full_description}" title="#{msgs.cdfm_read_full_description}">
						 <f:param value="#{topic.topic.id}" name="topicId_displayExtended"/>
						 <f:param value="displayHome" name="redirectToProcessAction"/>
					</h:commandLink>
					<h:commandLink  immediate="true" action="#{ForumTool.processActionToggleDisplayExtendedDescription}" id="topic_extended_hide" value="#{msgs.cdfm_hide_full_description}" rendered="#{topic.readFullDesciption}" title="#{msgs.cdfm_hide_full_description}">
						<f:param value="#{topic.topic.id}" name="topicId_hideExtended"/>
						<f:param value="displayHome" name="redirectToProcessAction"/>
					</h:commandLink>
				 </h:panelGroup>
				<%--	gsilver: show the text, not the editor... --%>
				<h:panelGroup styleClass="textPanel">
					<mf:htmlShowArea  id="topic_fullDescription" hideBorder="true"	 value="#{topic.topic.extendedDescription}" rendered="#{topic.readFullDesciption}"/>
		 			<%--  <sakai:inputRichText rows="5" cols="110" buttonSet="none"  readonly="true" showXPath="false" id="topic_extended_description" value="#{topic.topic.extendedDescription}" rendered="#{topic.readFullDesciption}"/> --%>
				</h:panelGroup>
				<h:dataTable styleClass="listHier" value="#{topic.attachList}" var="eachAttach" rendered="#{!empty topic.attachList}" cellpadding="0" cellspacing="0" columnClasses="attach,bogus" summary="layout">
					  <h:column>
					  <%-- gsilver: need to tie in the attachment type to actual  MIME type mapping tables instead of the below (which is prevalent everywhere) or at the very least provide a mechanism for defaults. --%> 
						<h:graphicImage url="/images/excel.gif" rendered="#{eachAttach.attachment.attachmentType == 'application/vnd.ms-excel'}" alt="application/vnd.ms-excel" />
						<h:graphicImage url="/images/html.gif" rendered="#{eachAttach.attachment.attachmentType == 'text/html'}" alt="text/html" />
						<h:graphicImage url="/images/pdf.gif" rendered="#{eachAttach.attachment.attachmentType == 'application/pdf'}" alt="application/pdf" />
						<h:graphicImage url="/images/ppt.gif" rendered="#{eachAttach.attachment.attachmentType == 'application/vnd.ms-powerpoint'}" alt="application/vnd.ms-powerpoint" />
						<h:graphicImage url="/images/text.gif" rendered="#{eachAttach.attachment.attachmentType == 'text/plain'}" alt="text/plain" />
						<h:graphicImage url="/images/word.gif" rendered="#{eachAttach.attachment.attachmentType == 'application/msword'}" alt="application/msword" />
						</h:column>
						<h:column>
<%--						<h:outputLink value="#{eachAttach.attachmentUrl}" target="_blank">
							<h:outputText value="#{eachAttach.attachmentName}" />
						</h:outputLink>--%>
						<h:outputLink value="#{eachAttach.url}" target="_blank">
							<h:outputText value="#{eachAttach.attachment.attachmentName}" />
						</h:outputLink>				  
					</h:column>
			  </h:dataTable>
			  <%-- gsilver: need a render attribute on the dataTable here to avoid putting an empty table in response -- since this looks like a stub that was never worked out to display the messages inside this construct - commenting the whole thing out.
			 <h:dataTable styleClass="indnt2" id="messages" value="#{topics.messages}" var="message">
			  <h:column>
					<h:outputText id="message_title" value="#{message.message.title}"/>
					<f:verbatim><br /></f:verbatim>
					<h:outputText id="message_desc" value="#{message.message.shortDescription}" />
			  </h:column>
			</h:dataTable>
			--%>
  	    </h:panelGrid>
    <f:verbatim></div></f:verbatim>

	 </h:column>
			
      </h:dataTable>			
	<f:verbatim></div><!--end single topic here --></f:verbatim>
	  </h:column>
  </h:dataTable>
 
 		</h:form>
 	</sakai:view>
 </f:view>