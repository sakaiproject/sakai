<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>

<f:view>
<sakai:view toolCssHref="/sakai-messageforums-tool/css/msgcntr.css">
<script language="JavaScript">
	// open print preview in another browser window so can size approx what actual
	// print out will look like
	function printFriendly(url) {
		newwindow=window.open(url,'mywindow','width=960,height=1100,scrollbars=yes,resizable=yes');
		if (window.focus) {newwindow.focus()}
	}
</script>

	<h:form id="msgForum" styleClass="specialLink">

	<!--jsp/discussionForum/message/dfFlatView.jsp-->
  		<script type="text/javascript" src="/library/js/jquery.js"></script>
  		<sakai:script contextBase="/sakai-messageforums-tool" path="/js/sak-10625.js"/>
		<sakai:script contextBase="/sakai-messageforums-tool" path="/js/forum.js"/>

	<%--//
		//plugin required below
		<sakai:script contextBase="/sakai-messageforums-tool" path="/js/pxToEm.js"/>

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

		<sakai:tool_bar separator="#{msgs.cdfm_toolbar_separator}">
				<sakai:tool_bar_item value="#{msgs.cdfm_container_title_thread}" action="#{ForumTool.processAddMessage}" id="df_compose_message_dfAllMessages"
		  			rendered="#{ForumTool.selectedTopic.isNewResponse && !ForumTool.selectedTopic.locked}" />
		  			
		  		<h:commandLink action="#{ForumTool.processActionMarkAllAsRead}" rendered="#{ForumTool.selectedTopic.isMarkAsRead}"> 
   	        		<h:outputText value=" #{msgs.cdfm_mark_all_as_read}" />
                </h:commandLink>
                <%--
		  		<sakai:tool_bar_item action="#{ForumTool.processActionMarkAllAsRead}" value="#{msgs.cdfm_mark_all_as_read}" 
					rendered="#{ForumTool.selectedTopic.isMarkAsRead}" />
					--%>
      			
      			<sakai:tool_bar_item value="#{msgs.cdfm_thread_view}" action="#{ForumTool.processActionDisplayThreadedView}" />
      			
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
					  <h:commandLink action="#{ForumTool.processActionDisplayForum}" value="#{ForumTool.selectedForum.forum.title}" title=" #{ForumTool.selectedForum.forum.title}">
						  <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
					  </h:commandLink>
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
	
		<%--rjlowe: Expanded View to show the message bodies, threaded --%>
	<span class="skip" id="firstNewItemTitleHolder"><h:outputText value="#{msgs.cdfm_gotofirstnewtitle}" /></span>
	<span class="skip" id="firstPendingItemTitleHolder"><h:outputText value="#{msgs.cdfm_gotofirstpendingtitle}" /></span>
	<span class="skip" id="nextPendItemTitleHolder"><h:outputText value="#{msgs.cdfm_gotopendtitle}" /></span>
	<span class="skip" id="lastPendItemTitleHolder"><h:outputText value="#{msgs.cdfm_lastpendtitle}" /></span>
	<span class="skip" id="nextNewItemTitleHolder"><h:outputText value="#{msgs.cdfm_gotonewtitle}" /></span>
	<span class="skip" id="lastNewItemTitleHolder"><h:outputText value="#{msgs.cdfm_lastnewtitle}" /></span>
 	<script type="text/javascript">
 			$(document).ready(function() {
					setupMessageNav('messageNew');
					setupMessageNav('messagePending');
				});
 	</script>
		<div id="messNavHolder" style="clear:both;"></div>
		<h:outputText   value="#{msgs.cdfm_no_messages}" rendered="#{empty ForumTool.messages}"   styleClass="instruction" style="display:block" />
		<mf:hierDataTable id="expandedThreadedMessages" value="#{ForumTool.messages}" var="message" 
   	 		noarrows="true" styleClass="listHier messagesThreaded" cellpadding="0" cellspacing="0" width="100%" columnClasses="bogus">
			<h:column id="_msg_subject">
				<%@include file="dfViewThreadBodyInclude.jsp" %>
			</h:column>
		</mf:hierDataTable>
				
		<h:inputHidden id="mainOrForumOrTopic" value="dfFlatView" />
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
