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
<!--jsp/discussionForum/message/dfAllMessages.jsp-->
		<sakai:script contextBase="/sakai-messageforums-tool" path="/js/forum.js"/>
		
			  <h:panelGrid columns="2" summary="layout" width="100%" styleClass="navPanel specialLink">
			    <h:panelGroup>
					<f:verbatim><div class="breadCrumb specialLink"><h3></f:verbatim>
			      <h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_message_forums}" title=" #{msgs.cdfm_message_forums}"
			      		rendered="#{ForumTool.messagesandForums}" />
			      <h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_discussion_forums}" title=" #{msgs.cdfm_discussion_forums}"
			      		rendered="#{ForumTool.forumsTool}" />
      			  <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
					  <h:commandLink action="#{ForumTool.processActionDisplayForum}" value="#{ForumTool.selectedForum.forum.title}" title=" #{msgs.cdfm_topic_settings}">
						  <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
					  </h:commandLink>
					  <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
					  <h:outputText value="#{ForumTool.selectedTopic.topic.title}" />
					  <f:verbatim></h3></div></f:verbatim>
				 </h:panelGroup>
				 <h:panelGroup styleClass="itemNav">
				   <h:outputText   value="#{msgs.cdfm_previous_topic}"  rendered="#{!ForumTool.selectedTopic.hasPreviousTopic}" />
					 <h:commandLink action="#{ForumTool.processActionDisplayPreviousTopic}" value="#{msgs.cdfm_previous_topic}"  rendered="#{ForumTool.selectedTopic.hasPreviousTopic}" 
					                title=" #{msgs.cdfm_topic_settings}">
						 <f:param value="#{ForumTool.selectedTopic.previousTopicId}" name="previousTopicId"/>
						 <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
					 </h:commandLink>
					 <f:verbatim><h:outputText  id="blankSpace1" value=" |  " /></f:verbatim>				
					 <h:outputText   value="#{msgs.cdfm_next_topic}" rendered="#{!ForumTool.selectedTopic.hasNextTopic}" />
					 <h:commandLink action="#{ForumTool.processActionDisplayNextTopic}" value="#{msgs.cdfm_next_topic}" rendered="#{ForumTool.selectedTopic.hasNextTopic}" 
					                title=" #{msgs.cdfm_topic_settings}">
						<f:param value="#{ForumTool.selectedTopic.nextTopicId}" name="nextTopicId"/>
						<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
					 </h:commandLink>
				 </h:panelGroup>
			  </h:panelGrid>
			  <p class="textPanel">
				<h:outputText   value="#{ForumTool.selectedTopic.topic.shortDescription}" />
				</p>
				<p class="textPanelFooter specialLink">
					<h:commandLink immediate="true" action="#{ForumTool.processActionToggleDisplayExtendedDescription}" rendered="#{ForumTool.selectedTopic.hasExtendedDesciption}"
						id="topic_extended_show" value="#{msgs.cdfm_read_full_description}" title="#{msgs.cdfm_read_full_description}">
						<f:param value="#{topic.topic.id}" name="topicId"/>
						<f:param value="processActionDisplayTopic" name="redirectToProcessAction"/>
					</h:commandLink>
					<h:commandLink immediate="true" action="#{ForumTool.processActionToggleDisplayExtendedDescription}" id="topic_extended_hide"
						 value="#{msgs.cdfm_hide_full_description}" rendered="#{ForumTool.selectedTopic.readFullDesciption}" title="#{msgs.cdfm_hide_full_description}">
						<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
						<f:param value="processActionDisplayTopic" name="redirectToProcessAction"/>
					</h:commandLink>
				</p>	

	<mf:htmlShowArea  id="forum_fullDescription" hideBorder="false"	 value="#{ForumTool.selectedTopic.topic.extendedDescription}" rendered="#{ForumTool.selectedTopic.readFullDesciption}"/> 
				 <%@include file="dfViewSearchBar.jsp"%>
     <%-- gsilver:need a rendered attribute here that will toggle the display of the table (if messages) or a textblock (class="instruction") if there are no messages--%> 				
   		
		<%-- gsilver:this table outputs a row for each indented message that starts with e <script /> element - not allowed as the child of <tbody />
			Also the threading seems off. Finnaly - the threading indent shoud be done with a 1em padding per indent instead of by use of &nbsp;--%>
		<mf:hierDataTable styleClass="hierItemBlockWrapper listHier lines nolines" id="messagesInHierDataTable" value="#{ForumTool.messages}" var="message" expanded="#{ForumTool.expanded}"
		                  columnClasses="attach,bogus,bogus,bogus" cellspacing="0" cellpadding="0">
	<%--
			<h:column>
				<f:facet name="header">
					<h:commandLink action="#{ForumTool.processCheckAll}" value="#{msgs.cdfm_checkall}" title="#{msgs.cdfm_checkall}"/>
				</f:facet>
				<h:selectBooleanCheckbox value="#{message.selected}"  rendered="#{message.read && !ForumTool.displayUnreadOnly}"/>
				<h:selectBooleanCheckbox value="#{message.selected}"  rendered="#{!message.read}"/>
			</h:column>
	--%>
	<%--
			<h:column>
				<f:facet name="header">
					<h:graphicImage value="/images/attachment.gif" alt="#{msgs.msg_has_attach}" />
				</f:facet>
				<h:graphicImage value="/images/attachment.gif" rendered="#{message.hasAttachment}" alt="#{msgs.msg_has_attach}"/>
			</h:column>
	--%>
	<%--
			<h:column>
				<f:facet name="header">
					<h:graphicImage value="/images/silk/email.png" alt="#{msgs.msg_is_unread}" />
				</f:facet>
				<h:commandLink action="#{ForumTool.processDfMsgMarkMsgAsRead}">
	                <f:param value="#{message.message.id}" name="messageId"/>
        	    	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
        	    	<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
	   	        	<h:graphicImage value="/images/silk/email.png" alt="#{msgs.msg_is_unread}" rendered="#{!message.read}" />
               	</h:commandLink>
			</h:column>
	--%>
			<%--
			<h:column rendered="#{message.depth == 0}" id="_thread_line">
					<f:verbatim><div class="hierItemBlockWrapper" style="padding-top:0;margin-top:0;"></f:verbatim>
							<h:commandLink action="#{ForumTool.processActionDisplayThread}" immediate="true" title="#{message.message.title}"
								rendered="#{message.depth == 0}">
							   	<h:outputText value="#{message.message.title}" rendered="#{message.read}" />
			    	        	<h:outputText styleClass="unreadMsg" value="#{message.message.title}" rendered="#{!message.read}"/>
			        	    	<f:param value="#{message.message.id}" name="messageId"/>
			        	    	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
			        	    	<f:param value="#{ForumTool.selectedTopic.topic.baseForum.id}" name="forumId"/>
				          	</h:commandLink>
								
				   	    	<f:verbatim> &nbsp; &ndash; &nbsp; </f:verbatim>
				   	    	<h:outputText value="#{message.message.author}" rendered="#{message.read}" />
				   	    	<h:outputText styleClass="unreadMsg" value="#{message.message.author}" rendered="#{!message.read }" />
				   	        <f:verbatim>&nbsp; (</f:verbatim>
				   	        	<h:outputText value="#{message.message.created}" rendered="#{message.read}">
				   	        		<f:convertDateTime pattern="#{msgs.date_format}" />
				   	       		</h:outputText>
				   	       		<h:outputText styleClass="unreadMsg" value="#{message.message.created}" rendered="#{!message.read}">
				   	        		<f:convertDateTime pattern="#{msgs.date_format}" />
				   	       		</h:outputText>
				   	        <f:verbatim>) &nbsp; </f:verbatim>
				   	 <f:verbatim></div></f:verbatim>
			</h:column>
			--%>
			<h:column id="_toggle">
				<f:facet name="header">
					<h:commandLink action="#{ForumTool.processActionToggleExpanded}" immediate="true">
						<h:graphicImage value="/images/expand.gif" rendered="#{ForumTool.expanded == 'true'}" />
						<h:graphicImage value="/images/collapse.gif" rendered="#{ForumTool.expanded != 'true'}" />
					</h:commandLink>
				</f:facet>
			</h:column>
			
			<h:column id="_msg_subject">
				<f:facet name="header">
					<h:outputText value="Thread" /> <%-- Don't forget --%>
				</f:facet>

				<%-- Rendered to view current thread only --%>
				<h:commandLink action="#{ForumTool.processActionDisplayThread}" immediate="true" title="#{message.message.title}"
					rendered="#{message.depth == 0}">
				   	<h:outputText value="#{message.message.title}" rendered="#{message.read}" />
    	        	<h:outputText styleClass="unreadMsg" value="#{message.message.title}" rendered="#{!message.read}"/>
        	    	<f:param value="#{message.message.id}" name="messageId"/>
        	    	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
        	    	<f:param value="#{ForumTool.selectedTopic.topic.baseForum.id}" name="forumId"/>
	          	</h:commandLink>


				<%-- Rendered to view current message only --%>
				<h:commandLink action="#{ForumTool.processActionDisplayMessage}" immediate="true" title=" #{message.message.title}"
					rendered="#{message.depth != 0}">
				   	<h:outputText value="#{message.message.title}" rendered="#{message.read}" />
    	        	<h:outputText styleClass="unreadMsg" value="#{message.message.title}" rendered="#{!message.read}"/>
        	    	<f:param value="#{message.message.id}" name="messageId"/>
        	    	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
        	    	<f:param value="#{ForumTool.selectedTopic.topic.baseForum.id}" name="forumId"/>
	          	</h:commandLink>
	          	
	          	<h:outputText value="  " />
				
	          	<h:commandLink action="#{ForumTool.processDfMsgMarkMsgAsRead}">
	                <f:param value="#{message.message.id}" name="messageId"/>
        	    	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
        	    	<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
	   	        	<h:graphicImage value="/images/silk/email.png" alt="#{msgs.msg_is_unread}" rendered="#{!message.read}" />
               	</h:commandLink>
               	
               	<h:outputText escape="false" styleClass="textPanelFooter" rendered="#{message.depth == 0}"
               		value="<br /> #{msgs.cdfm_openb} #{topic.totalNoMessages} #{msgs.cdfm_lowercase_msg} - #{topic.unreadNoMessages} #{msgs.cdfm_unread} #{msgs.cdfm_closeb}" />

				<h:outputText escape="false" value="<br />" />
			</h:column>

			<h:column>
				<f:facet name="header">
					<h:outputText value="#{msgs.cdfm_authoredby}" />
				</f:facet>
				 	<h:outputText value="#{message.message.author}" rendered="#{message.read}"/>
    	        	<h:outputText styleClass="unreadMsg" value="#{message.message.author}" rendered="#{!message.read}"/>

			</h:column>

			<h:column>
				<f:facet name="header">
					<h:outputText value="#{msgs.cdfm_date}" />
				</f:facet>
				 	<h:outputText value="#{message.message.created}" rendered="#{message.read}">
					 	<f:convertDateTime pattern="#{msgs.date_format}" />
					</h:outputText>
    	        	<h:outputText styleClass="unreadMsg" value="#{message.message.created}" rendered="#{!message.read}">
					 	<f:convertDateTime pattern="#{msgs.date_format}" />
				    </h:outputText>    	        	

			</h:column>
		</mf:hierDataTable>
		
		<h:inputHidden id="mainOrForumOrTopic" value="dfAllMessages" />
		
	</h:form>
</sakai:view>
</f:view>
