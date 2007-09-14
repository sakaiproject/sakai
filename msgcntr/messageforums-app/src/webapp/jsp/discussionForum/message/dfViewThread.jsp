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
	<h:form id="msgForum">
<!--jsp/discussionForum/message/dfAllMessages.jsp-->
		<sakai:script contextBase="/sakai-messageforums-tool" path="/js/forum.js"/>
		<sakai:script contextBase="/library" path="/js/jquery.js" />
		
		<sakai:tool_bar separator="#{msgs.cdfm_toolbar_separator}">
				<sakai:tool_bar_item action="#{ForumTool.processDfMsgReplyThread}" value="#{msgs.cdfm_reply_thread}" 
		  			rendered="#{ForumTool.selectedTopic.isNewResponse && ForumTool.selectedThreadHead.msgApproved && !ForumTool.selectedTopic.locked}" />
		  		
		  		<h:commandLink action="#{ForumTool.processActionMarkAllThreadAsRead}" rendered="#{ForumTool.selectedTopic.isMarkAsRead}"> 
      				<h:graphicImage value="/../../library/image/silk/email.png" alt="#{msgs.msg_is_unread}" 
				   	    onmouseover="this.src=this.src.replace(/email\.png/, 'email_open.png');"
   	        			onmouseout="this.src=this.src.replace(/email_open\.png/, 'email.png');" />
   	        		<h:outputText value=" #{msgs.cdfm_mark_all_as_read}" />
                </h:commandLink>
                <%--
		  		<sakai:tool_bar_item action="#{ForumTool.processActionMarkAllThreadAsRead}" value="#{msgs.cdfm_mark_all_as_read}" 
					rendered="#{ForumTool.selectedTopic.isMarkAsRead}" />
					--%>
					
				<h:outputLink id="print" value="javascript:printFriendly('#{ForumTool.printFriendlyUrlThread}');">
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
				  	  <h:commandLink action="#{ForumTool.processActionDisplayTopic}" value="#{ForumTool.selectedTopic.topic.title}" title="#{ForumTool.selectedTopic.topic.title}">
					  	  <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
					  	  <f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
				  	  </h:commandLink>
				  <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
				  	  <h:outputText value="#{ForumTool.selectedThreadHead.message.title}" />
					  <f:verbatim></h3></div></f:verbatim>
				 </h:panelGroup>
				 
				 <h:panelGroup styleClass="itemNav">
				   <h:outputText   value="#{msgs.cdfm_previous_thread}"  rendered="#{!ForumTool.selectedThreadHead.hasPreThread}" />
					 <h:commandLink action="#{ForumTool.processActionDisplayThread}" value="#{msgs.cdfm_previous_thread}"  rendered="#{ForumTool.selectedThreadHead.hasPreThread}">
						 <f:param value="#{ForumTool.selectedThreadHead.preThreadId}" name="messageId"/>
						 <f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
						 <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
					 </h:commandLink>
					 <f:verbatim><h:outputText  id="blankSpace1" value=" #{msgs.cdfm_toolbar_separator} " /></f:verbatim>				
					 <h:outputText   value="#{msgs.cdfm_next_thread}" rendered="#{!ForumTool.selectedThreadHead.hasNextThread}" />
					 <h:commandLink action="#{ForumTool.processActionDisplayThread}" value="#{msgs.cdfm_next_thread}" rendered="#{ForumTool.selectedThreadHead.hasNextThread}">
						<f:param value="#{ForumTool.selectedThreadHead.nextThreadId}" name="messageId"/>
						<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
						<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
					 </h:commandLink>
				 </h:panelGroup>
			</h:panelGrid>

				 <%@include file="dfViewSearchBarThread.jsp"%>
		
		<%--rjlowe: Expanded View to show the message bodies, but not threaded --%>
		<h:dataTable id="expandedMessages" value="#{ForumTool.selectedThread}" var="message" rendered="#{!ForumTool.threaded}"
   	 		styleClass="listHier" cellpadding="0" cellspacing="0" width="100%" columnClasses="bogus">
			<h:column>
				<%@include file="dfViewThreadBodyInclude.jsp" %>
			</h:column>
		</h:dataTable>
		
		<%--rjlowe: Expanded View to show the message bodies, threaded --%>
		<mf:hierDataTable id="expandedThreadedMessages" value="#{ForumTool.selectedThread}" var="message" rendered="#{ForumTool.threaded}"
   	 		noarrows="true" styleClass="listHier" cellpadding="0" cellspacing="0" width="100%" columnClasses="bogus">
			<h:column id="_msg_subject">
				<%@include file="dfViewThreadBodyInclude.jsp" %>
			</h:column>
		</mf:hierDataTable>
				
		<h:inputHidden id="mainOrForumOrTopic" value="dfViewThread" />

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
</sakai:view>
</f:view>
