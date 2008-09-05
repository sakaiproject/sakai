<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>

<f:view>
<sakai:view>
<script language="JavaScript">
	// open print preview in another browser window so can size approx what actual
	// print out will look like
	function printFriendly(url) {
		window.open(url,'mywindow','width=960,height=1100'); 		
	}
</script>
	<h:form id="msgForum" rendered="#{!ForumTool.selectedTopic.topic.draft || ForumTool.selectedTopic.topic.createdBy == ForumTool.userId}">
<!--jsp/discussionForum/message/dfAllMessages.jsp-->
  		<script type="text/javascript" src="/library/js/jquery.js"></script>
  		<sakai:script contextBase="/sakai-messageforums-tool" path="/js/sak-10625.js"/>
		<sakai:script contextBase="/sakai-messageforums-tool" path="/js/forum.js"/>
		
		<sakai:tool_bar separator="#{msgs.cdfm_toolbar_separator}">
				<sakai:tool_bar_item value="#{msgs.cdfm_container_title_thread}" action="#{ForumTool.processAddMessage}" id="df_compose_message_dfAllMessages"
		  			rendered="#{ForumTool.selectedTopic.isNewResponse && !ForumTool.selectedTopic.locked}" />
      			
      	<sakai:tool_bar_item value="#{msgs.cdfm_flat_view}" action="#{ForumTool.processActionDisplayFlatView}" />
      			
        <sakai:tool_bar_item action="#{ForumTool.processActionTopicSettings}" id="topic_setting" value="#{msgs.cdfm_topic_settings}" 
							rendered="#{ForumTool.selectedTopic.changeSettings}" />
					
				<h:outputLink id="print" value="javascript:printFriendly('#{ForumTool.printFriendlyUrl}');">
					<h:graphicImage url="/../../library/image/silk/printer.png" alt="#{msgs.print_friendly}" title="#{msgs.print_friendly}" />
				</h:outputLink>
 		</sakai:tool_bar>
 			
		
			  <h:panelGrid columns="2" summary="layout" width="100%" styleClass="navPanel specialLink">
			    <h:panelGroup>
					<f:verbatim><div class="breadCrumb specialLink"><h3></f:verbatim>
			      <h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_message_forums}" title=" #{msgs.cdfm_message_forums}"
			      		rendered="#{ForumTool.messagesandForums}" />
			      <h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_discussion_forums}" title=" #{msgs.cdfm_discussion_forums}"
			      		rendered="#{ForumTool.forumsTool}" />
      			  <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
					  <h:commandLink action="#{ForumTool.processActionDisplayForum}" value="#{ForumTool.selectedForum.forum.title}" title=" #{ForumTool.selectedForum.forum.title}" rendered="#{ForumTool.showForumLinksInNav}">
						  <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
					  </h:commandLink>
					  <h:outputText value="#{ForumTool.selectedForum.forum.title}" rendered="#{!ForumTool.showForumLinksInNav}"/>
					  <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
					  <h:outputText value="#{ForumTool.selectedTopic.topic.title}" />
					  <f:verbatim></h3></div></f:verbatim>
				 </h:panelGroup>
				 <h:panelGroup styleClass="itemNav">
				   <h:outputText   value="#{msgs.cdfm_previous_topic}"  rendered="#{!ForumTool.selectedTopic.hasPreviousTopic}" />
					 <h:commandLink action="#{ForumTool.processActionDisplayPreviousTopic}" value="#{msgs.cdfm_previous_topic}"  rendered="#{ForumTool.selectedTopic.hasPreviousTopic}" 
					                title=" #{msgs.cdfm_previous_topic}">
						 <f:param value="#{ForumTool.selectedTopic.previousTopicId}" name="previousTopicId"/>
						 <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
					 </h:commandLink>
					 <f:verbatim><h:outputText  id="blankSpace1" value=" |  " /></f:verbatim>				
					 <h:outputText   value="#{msgs.cdfm_next_topic}" rendered="#{!ForumTool.selectedTopic.hasNextTopic}" />
					 <h:commandLink action="#{ForumTool.processActionDisplayNextTopic}" value="#{msgs.cdfm_next_topic}" rendered="#{ForumTool.selectedTopic.hasNextTopic}" 
					                title=" #{msgs.cdfm_next_topic}">
						<f:param value="#{ForumTool.selectedTopic.nextTopicId}" name="nextTopicId"/>
						<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
					 </h:commandLink>
				 </h:panelGroup>
			  </h:panelGrid>
			<h:outputText escape="false" value="<p class='textPanel'>" rendered="#{ForumTool.selectedTopic.topic.shortDescription} != ''}" />
				<h:outputText   value="#{ForumTool.selectedTopic.topic.shortDescription}" rendered="#{ForumTool.selectedTopic.topic.shortDescription} != ''}" />
			<h:outputText escape="false" value="</p>" rendered="#{ForumTool.selectedTopic.topic.shortDescription} != ''}" />
				<p class="textPanelFooter specialLink">
		
						<%--gsilver: would be good if the returned url from this would include a named internal anchor as the target so that the expando/collapso would go to the top of the viewport and avoid having to scroll and find --%>
					  <h:outputLink id="forum_extended_show" value="#" title="#{msgs.cdfm_read_full_description}" styleClass="show" 
					  		rendered="#{ForumTool.selectedTopic.topic.extendedDescription != '' && ForumTool.selectedTopic.topic.extendedDescription != null}"
					  		onclick="toggleExtendedDescription($(this).next('.hide'), $('div.toggle:first'), $(this));">
					  		<h:outputText value="#{msgs.cdfm_read_full_description}" />
					  </h:outputLink>
					  
					  <h:outputLink id="forum_extended_hide" value="#" title="#{msgs.cdfm_hide_full_description}" style="display:none" styleClass="hide" 
					  		rendered="#{ForumTool.selectedTopic.topic.extendedDescription != '' && ForumTool.selectedTopic.topic.extendedDescription != null}"
					  		onclick="toggleExtendedDescription($(this).prev('.show'), $('div.toggle:first'), $(this));">
					  		<h:outputText value="#{msgs.cdfm_hide_full_description}" />
					  </h:outputLink>
				   
				</p>
			
				<f:verbatim><div class="toggle" style="display:none"></f:verbatim>
					<mf:htmlShowArea  id="forum_fullDescription" hideBorder="false"	 value="#{ForumTool.selectedTopic.topic.extendedDescription}"/> 
			    <f:verbatim></div></f:verbatim>
					
				<h:dataTable styleClass="listHier" value="#{ForumTool.selectedTopic.attachList}" var="eachAttach" rendered="#{!empty ForumTool.selectedTopic.attachList}" cellpadding="0" cellspacing="0" columnClasses="attach,bogus" summary="layout">
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
			  
				<%--<%@include file="dfViewSearchBar.jsp"%> --%>
     <%-- gsilver:need a rendered attribute here that will toggle the display of the table (if messages) or a textblock (class="instruction") if there are no messages--%> 				
   		
		<%-- gsilver:this table outputs a row for each indented message that starts with e <script /> element - not allowed as the child of <tbody />
			Also the threading seems off. Finnaly - the threading indent shoud be done with a 1em padding per indent instead of by use of &nbsp;--%>
		<mf:hierDataTable styleClass="hierItemBlockWrapper listHier lines nolines" id="messagesInHierDataTable" value="#{ForumTool.messages}" var="message" expanded="#{ForumTool.expanded}"
		                  columnClasses="attach,bogus,bogus,bogus" cellspacing="0" cellpadding="0">
			<h:column id="_toggle">
				<f:facet name="header">
					<h:commandLink action="#{ForumTool.processActionToggleExpanded}" immediate="true" title="#{msgs.cdfm_collapse_expand_all}">
						<h:graphicImage value="/images/expand.gif" rendered="#{ForumTool.expanded == 'true'}" />
						<h:graphicImage value="/images/collapse.gif" rendered="#{ForumTool.expanded != 'true'}" />
					</h:commandLink>
				</f:facet>
			</h:column>
			
			<h:column id="_msg_subject">
				<f:facet name="header">
					<h:outputText value="#{msgs.cdfm_thread}" />
				</f:facet>
				<h:outputText escape="false" value="<a id=\"#{message.message.id}\" name=\"#{message.message.id}\"></a>" />
				
				<%-- Display deleted message linked if any child messages (not deleted) --%>
				<h:panelGroup styleClass="inactive" rendered="#{message.deleted && message.depth == 0 && message.childCount > 0}" >
					<h:commandLink action="#{ForumTool.processActionDisplayThread}" immediate="true" title="#{msgs.cdfm_msg_deleted_label}" >
						<h:outputText value="#{msgs.cdfm_msg_deleted_label}" />

       	    			<f:param value="#{message.message.id}" name="messageId"/>
   	    		    	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
    			    	<f:param value="#{ForumTool.selectedTopic.topic.baseForum.id}" name="forumId"/>
					</h:commandLink>
				</h:panelGroup>
				
				<h:panelGroup rendered="#{!message.deleted}">
					<h:outputText value="#{msgs.cdfm_msg_pending_label} " styleClass="highlight" rendered="#{message.msgPending}" />
					<h:outputText value="#{msgs.cdfm_msg_denied_label} " rendered="#{message.msgDenied}" />
				
					<%-- Rendered to view current thread only --%>
					<h:commandLink action="#{ForumTool.processActionDisplayThread}" immediate="true" title="#{message.message.title}"
						rendered="#{message.depth == 0}">
				   		<h:outputText escape="false" value="#{message.message.title}" rendered="#{message.read && message.childUnread == 0 }" />
    	        		<h:outputText styleClass="unreadMsg" value="#{message.message.title}" rendered="#{!message.read || message.childUnread > 0}"/>

	       	    		<f:param value="#{message.message.id}" name="messageId"/>
    	    	    	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
   	    		    	<f:param value="#{ForumTool.selectedTopic.topic.baseForum.id}" name="forumId"/>
	    	      	</h:commandLink>
				</h:panelGroup>


				<%-- Rendered to view current message only --%>
				<h:panelGroup styleClass="inactive" rendered="#{message.deleted && (message.depth != 0 || message.childCount == 0)}" >
					<f:verbatim><span></f:verbatim>
						<h:outputText value="#{msgs.cdfm_msg_deleted_label}" />
					<f:verbatim></span></f:verbatim>
				</h:panelGroup>

				<h:panelGroup rendered="#{!message.deleted}">	          	
					<h:commandLink action="#{ForumTool.processActionDisplayMessage}" immediate="true" title=" #{message.message.title}"
						rendered="#{message.depth != 0}" >
					   	<h:outputText value="#{message.message.title}" rendered="#{message.read}" />
    	    	    	<h:outputText styleClass="unreadMsg" value="#{message.message.title}" rendered="#{!message.read}"/>

    	   	    		<f:param value="#{message.message.id}" name="messageId"/>
       		    		<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
        		    	<f:param value="#{ForumTool.selectedTopic.topic.baseForum.id}" name="forumId"/>
	          		</h:commandLink>

		          	<h:outputText value="  " />
				
					<h:graphicImage value="/../../library/image/silk/email.png" rendered="#{!message.read}" style="cursor:pointer"
							alt="#{msgs.cdfm_mark_as_read}" title="#{msgs.cdfm_mark_as_read}"
   							onclick="doAjax(#{message.message.id}, #{ForumTool.selectedTopic.topic.id}, this);"
			   	       		onmouseover="this.src=this.src.replace(/email\.png/, 'email_open.png');"
			   	       		onmouseout="this.src=this.src.replace(/email_open\.png/, 'email.png');" />
				</h:panelGroup>
               	
   	       		<h:outputText escape="false" value="<br />&nbsp;" rendered="#{message.depth == 0}" />
    	   		<h:outputText escape="false" styleClass="textPanelFooter" rendered="#{message.depth == 0 && message.childCount > 0 && message.childCount < 1 && message.read}"
           	   			value="#{msgs.cdfm_openb} #{message.childCount + 1} #{msgs.cdfm_lowercase_msg} - #{message.childUnread} #{msgs.cdfm_unread} #{msgs.cdfm_closeb}" />
           	   	<h:outputText  escape="false" styleClass="textPanelFooter" rendered="#{message.depth == 0 && message.childCount > 0 && message.childCount < 1 && !message.read}"
           	   			value="#{msgs.cdfm_openb} #{message.childCount + 1} #{msgs.cdfm_lowercase_msg} - #{message.childUnread +1} #{msgs.cdfm_unread} #{msgs.cdfm_closeb}" />		

    	   		<h:outputText  escape="false" styleClass="textPanelFooter" rendered="#{message.depth == 0 && message.childCount >= 1 && message.read}"
           	   			value="#{msgs.cdfm_openb} #{message.childCount + 1} #{msgs.cdfm_lowercase_msgs} - #{message.childUnread} #{msgs.cdfm_unread} #{msgs.cdfm_closeb}" />
           	   	<h:outputText escape="false" styleClass="textPanelFooter" rendered="#{message.depth == 0 && message.childCount >= 1 && !message.read}"
           	   			value="#{msgs.cdfm_openb} #{message.childCount + 1} #{msgs.cdfm_lowercase_msgs} - #{message.childUnread +1} #{msgs.cdfm_unread} #{msgs.cdfm_closeb}" />
			</h:column>

			<h:column>
				<f:facet name="header">
					<h:outputText value="#{msgs.cdfm_authoredby}" />
				</f:facet>
				<h:panelGroup rendered="#{!message.deleted}" >
				 	<h:outputText value="#{message.message.author}" rendered="#{message.read && message.childUnread == 0}"/>
    	        	<h:outputText styleClass="unreadMsg" value="#{message.message.author}" rendered="#{!message.read || message.childUnread > 0}"/>
				</h:panelGroup>
			</h:column>

			<h:column>
				<f:facet name="header">
					<h:outputText value="#{msgs.cdfm_date}" />
				</f:facet>
				<h:panelGroup rendered="#{!message.deleted}" >
				 	<h:outputText value="#{message.message.created}" rendered="#{message.read && message.childUnread == 0}">
					 	<f:convertDateTime pattern="#{msgs.date_format}" />
					</h:outputText>
    	        	<h:outputText styleClass="unreadMsg" value="#{message.message.created}" rendered="#{!message.read || message.childUnread > 0}">
					 	<f:convertDateTime pattern="#{msgs.date_format}" />
				    </h:outputText>    	        	
				</h:panelGroup>
			</h:column>
		</mf:hierDataTable>
		
		<h:inputHidden id="mainOrForumOrTopic" value="dfAllMessages" />
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
	<h:outputText value="#{msgs.cdfm_insufficient_privileges_view_topic}" rendered="#{ForumTool.selectedTopic.topic.draft && ForumTool.selectedTopic.topic.createdBy != ForumTool.userId}" />
</sakai:view>
</f:view>
