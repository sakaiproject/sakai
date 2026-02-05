<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>
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
		<script>includeLatestJQuery("msgcntr");</script>
		<script>includeWebjarLibrary('bootstrap')</script>
		<script src="/messageforums-tool/js/dialog.js"></script>
		<link rel="stylesheet" type="text/css" href="/messageforums-tool/css/dialog.css" />
		<script src="/messageforums-tool/js/sak-10625.js"></script>
		<script src="/messageforums-tool/js/forum.js"></script>
		<script src="/messageforums-tool/js/threadScrollEvent.js"></script>
		<script>
			$(document).ready(function () {
				var menuLink = $('#forumsMainMenuLink');
				var menuLinkSpan = menuLink.closest('span');
				menuLinkSpan.addClass('current');
				menuLinkSpan.html(menuLink.text());

				setupMessageNav('messagePending');
				setupMessageNav('messageNew');
				if ($('div.hierItemBlock').length >= 1){
					$('.itemNav').clone().addClass('specialLink').appendTo('form')
					$("<br/><br/>").appendTo('form');
				}

			});
			var markAsNotReadText = "<h:outputText value="#{msgs.cdfm_mark_as_not_read}"/>";
			var showThreadChanges = "<h:outputText value="#{ForumTool.showThreadChanges}"/>";
			var statRead = "<h:outputText value="#{msgs.stat_forum_read}"/>";
			var newFlag = "<h:outputText value="#{msgs.cdfm_newflag}"/>";
			var isMarkAsNotReadValue = "<h:outputText value="#{ForumTool.selectedTopic.isMarkAsNotRead}"/>";
		</script>
		<%@ include file="/jsp/discussionForum/menu/forumsMenu.jsp" %>
	<%--//
		//plugin required below
		<script src="/messageforums-tool/js/pxToEm.js"></script>

		/*
		gsilver: get a value representing max indents
	 	from the server configuraiton service or the language bundle, parse 
		all the indented items, and if the item indent goes over the value, flatten to the value 
		*/
		<script>
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

		<div class="headerBar">
		  <div class="progress-container">
		    <div class="progress-bar" id="myBar"></div>
		  </div>
		  <div id="progress-value" class="p-2"></div>
		</div>

			<h:panelGrid columns="2" width="100%" styleClass="specialLink">
			    <h:panelGroup>
				<div class="specialLink">
					<h3>
						<h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_message_forums}" title=" #{msgs.cdfm_message_forums}"
							rendered="#{ForumTool.messagesandForums}" />
						<h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_discussions}" title=" #{msgs.cdfm_discussions}"
							rendered="#{ForumTool.forumsTool}" />
						<h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " />
						<h:commandLink action="#{ForumTool.processActionDisplayForum}" title=" #{ForumTool.selectedForum.forum.title}" rendered="#{ForumTool.showForumLinksInNav}">
							<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
							<h:outputText value="#{ForumTool.selectedForum.forum.title}"/>
						</h:commandLink>
						<h:outputText value="#{ForumTool.selectedForum.forum.title}" rendered="#{!ForumTool.showForumLinksInNav}"/>
						<h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " />
						<h:commandLink action="#{ForumTool.processActionDisplayTopic}" title="#{ForumTool.selectedTopic.topic.title}">
							<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
							<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
							<h:outputText value="#{ForumTool.selectedTopic.topic.title}"/>
						</h:commandLink>
						<h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " />
						<h:panelGroup rendered="#{ForumTool.selectedTopic.availability == 'false'}">
							<span class="bi bi-calendar-x" aria-hidden="true" style="margin-right:.5em"></span>
							<h:outputText styleClass="sr-only" value="#{msgs.topic_restricted_message}" />
						</h:panelGroup>
						<h:panelGroup rendered="#{ForumTool.selectedTopic.locked =='true'}">
							<span class="bi bi-lock-fill" aria-hidden="true" style="margin-right:.5em"></span>
							<h:outputText styleClass="sr-only" value="#{msgs.cdfm_forum_locked}" />
						</h:panelGroup>
						<h:outputText value="#{ForumTool.selectedThreadHead.message.title}" />
					</h3>
				</div>

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

		<h:panelGroup rendered="#{!ForumTool.threadMoved}">
			<h:commandLink styleClass="button" value="#{msgs.cdfm_reply_thread}" id="replyThread" rendered="#{ForumTool.selectedTopic.isNewResponseToResponse && ForumTool.selectedThreadHead.msgApproved && !ForumTool.selectedTopic.locked && !ForumTool.selectedForum.locked == 'true'}"
				action="#{ForumTool.processDfMsgReplyThread}" immediate="true"/>
			<h:outputText value="&#160;" escape="false" />
			<h:commandLink styleClass="button" value=" #{msgs.cdfm_mark_all_as_not_read}" id="markAllNotRead" action="#{ForumTool.processActionMarkAllThreadAsNotRead}" rendered="#{ForumTool.selectedTopic.isMarkAsNotRead and not ForumTool.selectedTopic.topic.autoMarkThreadsRead}"/>
			<h:outputText value="&#160;" escape="false" />
			<h:outputLink styleClass="button" id="print" value="javascript:printFriendly('#{ForumTool.printFriendlyUrlThread}');">
				<span class="bi bi-printer-fill" aria-hidden="true"></span>
				<h:outputText value="#{msgs.print_friendly}" styleClass="sr-only" />
			</h:outputLink>
		</h:panelGroup>

	<div class="modal fade" id="dialogDiv" data-dialog-frame="dialogFrame" tabindex="-1" role="dialog" aria-modal="true" aria-hidden="true" aria-labelledby="dialogDivLabel">
		<div class="modal-dialog modal-xl modal-dialog-centered">
			<div class="modal-content">
				<div class="modal-header">
					<h5 class="modal-title" id="dialogDivLabel">
						<h:outputText value="#{msgs.cdfm_grade_msg}" />
					</h5>
					<button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="<h:outputText value='#{msgs.close_window}' />"></button>
				</div>
				<div class="modal-body">
					<f:verbatim>
						<iframe id="dialogFrame" name="dialogFrame" class="grade-modal-frame" title="</f:verbatim><h:outputText value="#{msgs.cdfm_grade_msg}" /><f:verbatim>"></iframe>
					</f:verbatim>
				</div>
			</div>
		</div>
	</div>
		
				 <%@ include file="dfViewSearchBarThread.jsp"%>
		
		<h:outputText value="#{msgs.cdfm_postFirst_warning}" rendered="#{ForumTool.needToPostFirst}" styleClass="messageAlert"/>
        <%-- a moved message --%>
        <h:panelGroup rendered="#{ForumTool.threadMoved}" >
          <span>
            <h:outputText styleClass="threadMovedMsg" value="<b>#{ForumTool.selectedThreadHead.message.title}</b> " escape="false"/>
            <h:outputText styleClass="threadMovedMsg" value="#{msgs.hasBeen} " />
            <h:commandLink action="#{ForumTool.processActionDisplayTopic}" id="topic_title" styleClass="threadMovedMsg">
              <h:outputText value="#{msgs.moved}" />
                                                    <f:param value="#{ForumTool.selectedThreadHead.message.topic.id}" name="topicId"/>
                                                    <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
                                            </h:commandLink>
            <h:outputText styleClass="threadMovedMsg"  value=" #{msgs.anotherTopic}" />
          </span>
        </h:panelGroup>
		<div id="messNavHolder" style="clear:both;"></div>
		<%--rjlowe: Expanded View to show the message bodies, but not threaded --%>
		<div>
		<h:dataTable id="expandedMessages" value="#{ForumTool.selectedThread}" var="message" rendered="#{!ForumTool.threaded}"
   	 		styleClass="table table-hover table-striped table-bordered messagesFlat specialLink" cellpadding="0" cellspacing="0" width="100%" columnClasses="bogus">
			<h:column>
			
				<%@ include file="dfViewThreadBodyInclude.jsp" %>
			</h:column>
		</h:dataTable>
		</div>
		
		<%--rjlowe: Expanded View to show the message bodies, threaded --%>
		<div>
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
	<script>
		function resize(){
  			mySetMainFrameHeight('<%= org.sakaiproject.util.Web.escapeJavascript(thisId)%>');
  		}
	</script> 
	
	</h:form>
	<h:outputText value="#{msgs.cdfm_insufficient_privileges_view_topic}" rendered="#{ForumTool.selectedTopic.topic.draft && ForumTool.selectedTopic.topic.createdBy != ForumTool.userId}" />
	
				 
	
</sakai:view>
</f:view>
