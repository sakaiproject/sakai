<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>

<f:view>
<sakai:view toolCssHref="/messageforums-tool/css/msgcntr.css">
	<span class="skip" id="firstPendingItemTitleHolder"><h:outputText value="#{msgs.cdfm_gotofirstpendingtitle}" /></span>
	<span class="skip" id="nextPendingItemTitleHolder"><h:outputText value="#{msgs.cdfm_gotopendtitle}" /></span>
	<span class="skip" id="lastPendingItemTitleHolder"><h:outputText value="#{msgs.cdfm_lastpendtitle}" /></span>
	<span class="skip" id="firstNewItemTitleHolder"><h:outputText value="#{msgs.cdfm_gotofirstnewtitle}" /></span>
	<span class="skip" id="nextNewItemTitleHolder"><h:outputText value="#{msgs.cdfm_gotonewtitle}" /></span>
	<span class="skip" id="lastNewItemTitleHolder"><h:outputText value="#{msgs.cdfm_lastnewtitle}" /></span>
	<h:form id="msgForum" rendered="#{!ForumTool.selectedTopic.topic.draft || ForumTool.selectedTopic.topic.createdBy == ForumTool.userId}">

		<!--jsp/discussionForum/message/dfViewThread.jsp-->
       		<script type="text/javascript">includeLatestJQuery("msgcntr");</script>
  			<sakai:script contextBase="/messageforums-tool" path="/js/dialog.js"/>
			<script type="text/javascript" src="/library/js/jquery/qtip/jquery.qtip-latest.min.js"></script>
			<link rel="stylesheet" type="text/css" href="/library/js/jquery/qtip/jquery.qtip-latest.min.css" />
  			<link rel="stylesheet" type="text/css" href="/messageforums-tool/css/dialog.css" />	
       		<sakai:script contextBase="/messageforums-tool" path="/js/sak-10625.js"/>
		<sakai:script contextBase="/messageforums-tool" path="/js/forum.js"/>
		
	<%--//
		//plugin required below
		<sakai:script contextBase="/messageforums-tool" path="/js/pxToEm.js"/>

		/*
		gsilver: get a value representing max indents
	 	from the server configuraiton service or the language bundle, parse 
		all the indented items, and if the item indent goes over the value, flatten to the value 
		*/
		<script type="text/javascript">
		$(document).ready(function() {
			// pick value from element (that gets it from language bundle)
			maxThreadDepth =$('#maxthreaddepth').text()
			// double check that this is a number
			if (isNaN(maxThreadDepth)){
				maxThreadDepth=10
			}
			// for each message, if the message is indented more than the value above
			// void that and set the new indent to the value
			$(".messagesThreaded td").each(function (i) {
				paddingDepth= $(this).css('padding-left').split('px');
				if ( paddingDepth[0] > parseInt(maxThreadDepth.pxToEm ({scope:'body', reverse:true}))){
					$(this).css ('padding-left', maxThreadDepth + 'em');
				}
			});
		});
		</script>	
		
		// element into which the value gets insert and retrieved from
		<span class="highlight"  id="maxthreaddepth" class="skip"><h:outputText value="#{msgs.cdfm_maxthreaddepth}" /></span>
//--%>

<script type="text/javascript">
 			$(document).ready(function() {
				setupMessageNav('messagePending');
				setupMessageNav('messageNew');
				if ($('div.hierItemBlock').size() >= 1){
					$('.itemNav').clone().addClass('specialLink').appendTo('form')
					$("<br/><br/>").appendTo('form');					
				}
			});
</script>		
		<sakai:tool_bar separator="#{msgs.cdfm_toolbar_separator}" rendered="#{!ForumTool.threadMoved}">
				<sakai:tool_bar_item action="#{ForumTool.processDfMsgReplyThread}" value="#{msgs.cdfm_reply_thread}" 
		  			rendered="#{ForumTool.selectedTopic.isNewResponseToResponse && ForumTool.selectedThreadHead.msgApproved && !ForumTool.selectedTopic.locked && !ForumTool.selectedForum.locked == 'true'}" />
		  		
		  		<h:commandLink action="#{ForumTool.processActionMarkAllThreadAsRead}" rendered="#{ForumTool.selectedTopic.isMarkAsRead and not ForumTool.selectedTopic.topic.autoMarkThreadsRead}"> 
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
			<h:panelGrid columns="2" width="100%" styleClass="specialLink">
			    <h:panelGroup>
					<f:verbatim><div class="specialLink"><h3></f:verbatim>
			      <h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_message_forums}" title=" #{msgs.cdfm_message_forums}"
			      		rendered="#{ForumTool.messagesandForums}" />
			      <h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_discussion_forums}" title=" #{msgs.cdfm_discussion_forums}"
			      		rendered="#{ForumTool.forumsTool}" />
      			  <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
					  <h:commandLink action="#{ForumTool.processActionDisplayForum}" title=" #{ForumTool.selectedForum.forum.title}" rendered="#{ForumTool.showForumLinksInNav}">
						  <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
						  <h:outputText value="#{ForumTool.selectedForum.forum.title}"/>
					  </h:commandLink>
					  <h:outputText value="#{ForumTool.selectedForum.forum.title}" rendered="#{!ForumTool.showForumLinksInNav}"/>
				  <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
				  	  <h:commandLink action="#{ForumTool.processActionDisplayTopic}" title="#{ForumTool.selectedTopic.topic.title}">
					  	  <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
					  	  <f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
					  	  <h:outputText value="#{ForumTool.selectedTopic.topic.title}"/>
				  	  </h:commandLink>
				  <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
				  	<h:graphicImage url="/images/silk/date_delete.png" title="#{msgs.topic_restricted_message}" alt="#{msgs.topic_restricted_message}" rendered="#{ForumTool.selectedTopic.availability == 'false'}" style="margin-right:.5em"/>
				  	<h:graphicImage url="/images/silk/lock.png" alt="#{msgs.cdfm_forum_locked}" 
						 rendered="#{ForumTool.selectedTopic.locked =='true'}" style="margin-right:.5em"/>
				  	  <h:outputText value="#{ForumTool.selectedThreadHead.message.title}" />
					  <f:verbatim></h3></div></f:verbatim>

				 </h:panelGroup>
				 
				 <h:panelGroup styleClass="itemNav">
				 	<h:panelGroup styleClass="button formButtonDisabled" rendered="#{!ForumTool.selectedThreadHead.hasPreThread}" >
						<h:outputText  value="#{msgs.cdfm_previous_thread}"/>
					</h:panelGroup>
					 <h:commandLink styleClass="button" action="#{ForumTool.processActionDisplayThread}" value="#{msgs.cdfm_previous_thread}"  rendered="#{ForumTool.selectedThreadHead.hasPreThread}">
						 <f:param value="#{ForumTool.selectedThreadHead.preThreadId}" name="messageId"/>
						 <f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
						 <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
					 </h:commandLink>
					 <h:panelGroup styleClass="button formButtonDisabled" rendered="#{!ForumTool.selectedThreadHead.hasNextThread}">
					 	<h:outputText value="#{msgs.cdfm_next_thread}"/>
					 </h:panelGroup>
					 <h:commandLink styleClass="button" action="#{ForumTool.processActionDisplayThread}" value="#{msgs.cdfm_next_thread}" rendered="#{ForumTool.selectedThreadHead.hasNextThread}">
						<f:param value="#{ForumTool.selectedThreadHead.nextThreadId}" name="messageId"/>
						<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
						<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
					 </h:commandLink>
				 </h:panelGroup>
			</h:panelGrid>
		
	  	<f:verbatim>
			<div id="dialogDiv" title="Grade Messages" style="display:none">
	    		<iframe id="dialogFrame" name="dialogFrame" width="100%" height="100%" frameborder="0"></iframe>
	    	</div>
		</f:verbatim>
		
				 <%@ include file="dfViewSearchBarThread.jsp"%>
		
		<h:outputText value="#{msgs.cdfm_postFirst_warning}" rendered="#{ForumTool.needToPostFirst}" styleClass="messageAlert"/>
        <%-- a moved message --%>
        <h:panelGroup rendered="#{ForumTool.threadMoved}" >
          <f:verbatim><span></f:verbatim>
            <h:outputText styleClass="threadMovedMsg" value="<b>#{ForumTool.selectedThreadHead.message.title}</b> " escape="false"/>
            <h:outputText styleClass="threadMovedMsg" value="#{msgs.hasBeen} " />
            <h:commandLink action="#{ForumTool.processActionDisplayTopic}" id="topic_title" styleClass="threadMovedMsg">
              <h:outputText value="#{msgs.moved}" />
                                                    <f:param value="#{ForumTool.selectedThreadHead.message.topic.id}" name="topicId"/>
                                                    <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
                                            </h:commandLink>
            <h:outputText styleClass="threadMovedMsg"  value=" #{msgs.anotherTopic}" />
          <f:verbatim></span></f:verbatim>
        </h:panelGroup>
		<div id="messNavHolder" style="clear:both;"></div>
		<%--rjlowe: Expanded View to show the message bodies, but not threaded --%>
		<div class="table-responsive">
		<h:dataTable id="expandedMessages" value="#{ForumTool.selectedThread}" var="message" rendered="#{!ForumTool.threaded}"
   	 		styleClass="table table-hover table-striped table-bordered messagesFlat specialLink" cellpadding="0" cellspacing="0" width="100%" columnClasses="bogus">
			<h:column>
			
				<%@ include file="dfViewThreadBodyInclude.jsp" %>
			</h:column>
		</h:dataTable>
		</div>
		
		<%--rjlowe: Expanded View to show the message bodies, threaded --%>
		<div class="table-responsive">
		<mf:hierDataTable id="expandedThreadedMessages" value="#{ForumTool.selectedThread}" var="message" rendered="#{ForumTool.threaded}"
   	 		noarrows="true" styleClass="table table-hover table-striped table-bordered messagesThreaded specialLink" border="0" cellpadding="0" cellspacing="0" width="100%" columnClasses="bogus">
			<h:column id="_msg_subject">
				<%@ include file="dfViewThreadBodyInclude.jsp" %>
			</h:column>
		</mf:hierDataTable>
		</div>
				
		<h:inputHidden id="mainOrForumOrTopic" value="dfViewThread" />
		<%--//designNote:  need a message if no messages (as in when there are no unread ones)  --%>

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
