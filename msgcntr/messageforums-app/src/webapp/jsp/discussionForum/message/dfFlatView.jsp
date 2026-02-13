<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>

<f:view>
<sakai:view toolCssHref="/messageforums-tool/css/msgcntr.css">

	<h:form id="msgForum" styleClass="specialLink">

	<!--jsp/discussionForum/message/dfFlatView.jsp-->
		<script>includeLatestJQuery("msgcntr");</script>
  		<link rel="stylesheet" type="text/css" href="/messageforums-tool/css/dialog.css" />
		<script src="/messageforums-tool/js/sak-10625.js"></script>
		<script src="/messageforums-tool/js/forum.js"></script>
		<script src="/messageforums-tool/js/threadScrollEvent.js"></script>
		<script>includeWebjarLibrary('bootstrap')</script>
		<script src="/messageforums-tool/js/dialog.js"></script>
        <script>
            $(document).ready(function(){
                var menuLink = $('#forumsMainMenuLink');
                var menuLinkSpan = menuLink.closest('span');
                menuLinkSpan.addClass('current');
                menuLinkSpan.html(menuLink.text());

                setupMessageNav('messageNew');
                setupMessageNav('messagePending');

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

	<div class="modal fade" id="dialogDiv" data-dialog-frame="dialogFrame" tabindex="-1" role="dialog" aria-modal="true" aria-hidden="true" aria-labelledby="dialogDivLabel">
		<div class="modal-dialog modal-xl modal-dialog-centered">
			<div class="modal-content">
				<div class="modal-header">
					<h5 class="modal-title" id="dialogDivLabel">
						<h:outputText value="#{msgs.cdfm_grade_msg}" />
					</h5>
					<button type="button" class="btn-close" data-bs-dismiss="modal">
						<span class="visually-hidden">
							<h:outputText value="#{msgs.close_window}" />
						</span>
					</button>
				</div>
				<div class="modal-body">
					<f:verbatim>
						<iframe id="dialogFrame" name="dialogFrame" class="grade-modal-frame" title="</f:verbatim><h:outputText value="#{msgs.cdfm_grade_msg}" /><f:verbatim>"></iframe>
					</f:verbatim>
				</div>
			</div>
		</div>
	</div>

		<div class="row">
			<div class="col-md-9 col-xs-12">
				<div class="breadCrumb specialLink">
					<h3>
						<h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_message_forums}" title=" #{msgs.cdfm_message_forums}"
							rendered="#{ForumTool.messagesandForums}" />
						<h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_discussions}" title=" #{msgs.cdfm_discussions}"
							rendered="#{ForumTool.forumsTool}" />
						<h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " />
						<h:commandLink action="#{ForumTool.processActionDisplayForum}" title=" #{ForumTool.selectedForum.forum.title}">
							<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
							<h:outputText value="#{ForumTool.selectedForum.forum.title}" />
						</h:commandLink>
						<h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " />
						<h:outputText value="#{ForumTool.selectedTopic.topic.title}" />
					</h3>
				</div>
			</div>
			<div class="pull-right">
				   <h:outputText  styleClass="button formButtonDisabled"  value="#{msgs.cdfm_previous_topic}"  rendered="#{!ForumTool.selectedTopic.hasPreviousTopic}" />
					 <h:commandLink  styleClass="button" action="#{ForumTool.processActionDisplayPreviousTopic}" value="#{msgs.cdfm_previous_topic}"  rendered="#{ForumTool.selectedTopic.hasPreviousTopic}" 
					                title=" #{msgs.cdfm_previous_topic}">
						 <f:param value="#{ForumTool.selectedTopic.previousTopicId}" name="previousTopicId"/>
						 <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
					 </h:commandLink>
					 <h:outputText styleClass="button formButtonDisabled" value="#{msgs.cdfm_next_topic}" rendered="#{!ForumTool.selectedTopic.hasNextTopic}" />
					 <h:commandLink  styleClass="button" action="#{ForumTool.processActionDisplayNextTopic}" value="#{msgs.cdfm_next_topic}" rendered="#{ForumTool.selectedTopic.hasNextTopic}" 
					                title=" #{msgs.cdfm_next_topic}">
						<f:param value="#{ForumTool.selectedTopic.nextTopicId}" name="nextTopicId"/>
						<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
					 </h:commandLink>
 			</div>
 		</div>
	
		<%--rjlowe: Expanded View to show the message bodies, threaded --%>
	<span class="skip" id="firstNewItemTitleHolder"><h:outputText value="#{msgs.cdfm_gotofirstnewtitle}" /></span>
	<span class="skip" id="firstPendingItemTitleHolder"><h:outputText value="#{msgs.cdfm_gotofirstpendingtitle}" /></span>
	<span class="skip" id="nextPendItemTitleHolder"><h:outputText value="#{msgs.cdfm_gotopendtitle}" /></span>
	<span class="skip" id="lastPendItemTitleHolder"><h:outputText value="#{msgs.cdfm_lastpendtitle}" /></span>
	<span class="skip" id="nextNewItemTitleHolder"><h:outputText value="#{msgs.cdfm_gotonewtitle}" /></span>
	<span class="skip" id="lastNewItemTitleHolder"><h:outputText value="#{msgs.cdfm_lastnewtitle}" /></span>

		<div id="messNavHolder" style="clear:both;">
				<h:commandLink action="#{ForumTool.processActionMarkAllAsNotRead}" rendered="#{ForumTool.selectedTopic.isMarkAsNotRead and not ForumTool.selectedTopic.topic.autoMarkThreadsRead}" styleClass="button">
					<h:outputText value=" #{msgs.cdfm_mark_all_as_not_read}" />
				</h:commandLink>
		</div>

		<h:panelGroup id="forumsActions" layout="block">
			<h:commandLink styleClass="button" value="#{msgs.cdfm_container_title_thread}" id="df_compose_message_dfAllMessages"
				rendered="#{ForumTool.selectedTopic.isNewResponse && !ForumTool.selectedTopic.locked && !ForumTool.selectedForum.locked == 'true'}" action="#{ForumTool.processAddMessage}" immediate="true"/>
			<h:outputText value="&#160;" escape="false" />
			<h:commandLink styleClass="button" value="#{msgs.cdfm_thread_view}"  id="threadView" action="#{ForumTool.processActionDisplayThreadedView}" immediate="true"/>
			<h:outputText value="&#160;" escape="false" />
			<h:commandLink styleClass="button" action="#{ForumTool.processActionTopicSettings}" id="topic_setting" value="#{msgs.cdfm_topic_settings}"
				rendered="#{ForumTool.selectedTopic.changeSettings}">
				<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
			</h:commandLink>
			<h:outputText value="&#160;" escape="false" />
			<h:commandLink styleClass="button" action="#{ForumTool.processActionDeleteTopicConfirm}" id="delete_confirm"
				value="#{msgs.cdfm_button_bar_delete_topic}" accesskey="d" rendered="#{!ForumTool.selectedTopic.markForDeletion && ForumTool.displayTopicDeleteOption}">
				<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
			</h:commandLink>
			<h:outputText value="&#160;" escape="false" />
			<h:outputLink styleClass="button" id="print" value="javascript:printFriendly('#{ForumTool.printFriendlyUrl}');" title="#{msgs.print_friendly}">
				<span class="bi bi-printer-fill" aria-hidden="true"></span>
			</h:outputLink>
		</h:panelGroup>

		<h:outputText  value="#{msgs.cdfm_no_messages}" rendered="#{empty ForumTool.messages}"   styleClass="sak-banner-info" style="display:block" />
		<div class="clear">
			<mf:hierDataTable id="expandedThreadedMessages" value="#{ForumTool.messages}" var="message" 
	   	 		noarrows="true" styleClass="table-hover messagesThreaded" cellpadding="0" cellspacing="0" width="100%" columnClasses="bogus">
				<h:column id="_msg_subject">
					<%@ include file="dfViewThreadBodyInclude.jsp" %>
				</h:column>
			</mf:hierDataTable>
		</div>
		
		<br/><br/>
		<h:panelGrid columns="1" width="100%" styleClass="navPanel specialLink">
				 <h:panelGroup styleClass="itemNav">
				   <h:outputText  styleClass="button formButtonDisabled" value="#{msgs.cdfm_previous_topic}"  rendered="#{!ForumTool.selectedTopic.hasPreviousTopic}" />
					 <h:commandLink  styleClass="button" action="#{ForumTool.processActionDisplayPreviousTopic}" value="#{msgs.cdfm_previous_topic}"  rendered="#{ForumTool.selectedTopic.hasPreviousTopic}" 
					                title=" #{msgs.cdfm_previous_topic}">
						 <f:param value="#{ForumTool.selectedTopic.previousTopicId}" name="previousTopicId"/>
						 <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
					 </h:commandLink>
					 <h:outputText  styleClass="button formButtonDisabled" value="#{msgs.cdfm_next_topic}" rendered="#{!ForumTool.selectedTopic.hasNextTopic}" />
					 <h:commandLink action="#{ForumTool.processActionDisplayNextTopic}" value="#{msgs.cdfm_next_topic}" rendered="#{ForumTool.selectedTopic.hasNextTopic}" 
					                title=" #{msgs.cdfm_next_topic}"  styleClass="button">
						<f:param value="#{ForumTool.selectedTopic.nextTopicId}" name="nextTopicId"/>
						<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
					 </h:commandLink>
				 </h:panelGroup>
			</h:panelGrid>
				
		<h:inputHidden id="mainOrForumOrTopic" value="dfFlatView" />
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

</sakai:view>
</f:view>
