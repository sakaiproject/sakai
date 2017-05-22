<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
	<jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>

<%
  	String thisId = request.getParameter("panel");
  	if (thisId == null) 
  	{
    	thisId = "Main" + org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement().getId();
 		 }
 	
%>

<f:view>
  <sakai:view>
  	<h:form id="dfStatisticsForm" rendered="#{ForumTool.instructor}">
		<!--discussionForum/statistics/dfStatisticsUser.jsp-->
		<script type="text/javascript">

	
			var iframeId = '<%= org.sakaiproject.util.Web.escapeJavascript(thisId)%>';
			
			function resize(){
				mySetMainFrameHeight('<%= org.sakaiproject.util.Web.escapeJavascript(thisId)%>');
			}
		
		
			function mySetMainFrameHeight(id)
			{
				// run the script only if this window's name matches the id parameter
				// this tells us that the iframe in parent by the name of 'id' is the one who spawned us
				if (typeof window.name != "undefined" && id != window.name) return;
			
				var frame = parent.document.getElementById(id);
				if (frame)
				{
			
					var objToResize = (frame.style) ? frame.style : frame;
			  
			    // SAK-11014 revert           if ( false ) {
			
					var height; 		
					var offsetH = document.body.offsetHeight;
					var innerDocScrollH = null;
			
					if (typeof(frame.contentDocument) != 'undefined' || typeof(frame.contentWindow) != 'undefined')
					{
						// very special way to get the height from IE on Windows!
						// note that the above special way of testing for undefined variables is necessary for older browsers
						// (IE 5.5 Mac) to not choke on the undefined variables.
			 			var innerDoc = (frame.contentDocument) ? frame.contentDocument : frame.contentWindow.document;
						innerDocScrollH = (innerDoc != null) ? innerDoc.body.scrollHeight : null;
					}
				
					if (document.all && innerDocScrollH != null)
					{
						// IE on Windows only
						height = innerDocScrollH;
					}
					else
					{
						// every other browser!
						height = offsetH;
					}
			   // SAK-11014 revert		} 
			
			   // SAK-11014 revert             var height = getFrameHeight(frame);
			
					// here we fudge to get a little bigger
					var newHeight = height + 40;
			
					// but not too big!
					if (newHeight > 32760) newHeight = 32760;
			
					// capture my current scroll position
					var scroll = findScroll();
			
					// resize parent frame (this resets the scroll as well)
					objToResize.height=newHeight + "px";
			
					// reset the scroll, unless it was y=0)
					if (scroll[1] > 0)
					{
						var position = findPosition(frame);
						parent.window.scrollTo(position[0]+scroll[0], position[1]+scroll[1]);
					}
				}
			}
			
		</script>
		
  	    <script type="text/javascript">includeLatestJQuery("msgcntr");</script>
		<sakai:script contextBase="/messageforums-tool" path="/js/dialog.js"/>
		<sakai:script contextBase="/messageforums-tool" path="/js/forum.js"/>
		<link rel="stylesheet" type="text/css" href="/messageforums-tool/css/dialog.css" />
		<link rel="stylesheet" type="text/css" href="/messageforums-tool/css/msgcntr_statistics.css" />
       	
  	
  		<script type="text/javascript">
  			$(document).ready(function() {
				$(".messageBody").each(function(index){
					var msgBody = $(this).html();
					msgBody = msgBody.replace(/\n/g,',').replace(/\s/g,' ').replace(/  ,/g,',');
					var wordCountId = $(this).attr('id').substring(11, $(this).attr('id').length);
					$("#wordCountSpan" + wordCountId).html(getWordCount(msgBody));
	  				//fckeditor_word_count_fromMessage(msgBody,'wordCountSpan' + wordCountId);
				});
			});
			
			function dialogLinkClick(link){
				var position =  $(link).position();
				dialogutil.openDialog('dialogDiv', 'dialogFrame', position.top);
			}
		</script>
  		<f:verbatim>
			<div id="dialogDiv" title="Grade Messages" style="display:none">
		       <iframe id="dialogFrame" name="dialogFrame" width="100%" height="100%" frameborder="0"></iframe>
		    </div>
		</f:verbatim>
  		<h:panelGrid columns="2" width="100%" styleClass="navPanel  specialLink">
          <h:panelGroup>
          	 <f:verbatim><h3></f:verbatim>
			      <h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_message_forums}" title=" #{msgs.cdfm_message_forums}"
			      		rendered="#{ForumTool.messagesandForums}" />
			      <h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_discussion_forums}" title=" #{msgs.cdfm_discussion_forums}"
			      		rendered="#{ForumTool.forumsTool}" />
			      <h:outputText value=" / "/>
			      <h:commandLink action="#{ForumTool.processActionStatistics}" value="#{msgs.stat_list}" title="#{msgs.stat_list}" rendered="#{empty mfStatisticsBean.selectedAllTopicsTopicId && empty mfStatisticsBean.selectedAllTopicsForumId}"/>
			      <h:commandLink action="#{mfStatisticsBean.processActionStatisticsByAllTopics}" value="#{msgs.stat_list}" title="#{msgs.stat_list}" rendered="#{!empty mfStatisticsBean.selectedAllTopicsTopicId || !empty mfStatisticsBean.selectedAllTopicsForumId}"/>
			      <h:panelGroup rendered="#{!empty mfStatisticsBean.selectedAllTopicsForumId}">
				      <h:outputText value=" / "/>
				      <h:commandLink action="#{mfStatisticsBean.processActionStatisticsByTopic}" immediate="true">
	  				    <f:param value="" name="topicId"/>
	  				    <f:param value="#{mfStatisticsBean.selectedAllTopicsForumId}" name="forumId"/>
	  				    <h:outputText value="#{mfStatisticsBean.selectedAllTopicsForumTitle}" />
		          	  </h:commandLink>
				  </h:panelGroup>
				  <h:panelGroup rendered="#{!empty mfStatisticsBean.selectedAllTopicsTopicId}">
			      	  <h:outputText value=" / "/>
				      <h:commandLink action="#{mfStatisticsBean.processActionStatisticsByTopic}" immediate="true">
	  				    <f:param value="#{mfStatisticsBean.selectedAllTopicsTopicId}" name="topicId"/>
	  				    <f:param value="#{mfStatisticsBean.selectedAllTopicsForumId}" name="forumId"/>
	  				    <h:outputText value="#{mfStatisticsBean.selectedAllTopicsTopicTitle}" />
		          	  </h:commandLink>
		          </h:panelGroup>  
			      <h:outputText value=" / "/>
			      <h:outputText value="#{mfStatisticsBean.selectedSiteUser}" />
			    <f:verbatim></div></f:verbatim>
          </h:panelGroup>
          <h:panelGroup styleClass="itemNav specialLink">	
				<h:commandButton action="#{mfStatisticsBean.processDisplayPreviousParticipant}" value="#{msgs.stat_forum_prev_participant}"  
			                rendered="#{!mfStatisticsBean.isFirstParticipant}" title=" #{msgs.stat_forum_prev_participant}">				
				</h:commandButton>
				<h:commandButton action="#{mfStatisticsBean.processDisplayPreviousParticipant}" value="#{msgs.stat_forum_prev_participant}"  
			                rendered="#{mfStatisticsBean.isFirstParticipant}" title=" #{msgs.stat_forum_prev_participant}" disabled = "true">				
				</h:commandButton>
				
				<h:commandButton action="#{mfStatisticsBean.processDisplayNextParticipant}" value="#{msgs.stat_forum_next_participant}" 
				  		                  rendered="#{!mfStatisticsBean.isLastParticipant}" title=" #{msgs.stat_forum_next_participant}">					
				</h:commandButton>
				<h:commandButton action="#{mfStatisticsBean.processDisplayNextParticipant}" value="#{msgs.stat_forum_next_participant}" 
				  		                  rendered="#{mfStatisticsBean.isLastParticipant}" title=" #{msgs.stat_forum_next_participant}" disabled="true">					
				</h:commandButton>
				
			
	
		</h:panelGroup>

        </h:panelGrid>
		<f:verbatim>
	  		<div class="success" id="gradesSavedDiv" class="success" style="display:none">
	  	</f:verbatim>
	  		<h:outputText value="#{msgs.cdfm_grade_successful}"/>
	  	<f:verbatim>
	  		</div>
	  	</f:verbatim>

	  	<h:outputText rendered="#{ForumTool.anonymousEnabled && ForumTool.siteHasAnonymousTopics && !mfStatisticsBean.pureAnon}" value="#{msgs.stat_forum_anonymous_omitted}" styleClass="instruction" />

	  	<h:panelGrid columns="2" width="100%" style="margin:0">
   			<h:panelGroup>
    			<f:verbatim><h4 style="margin:0;padding:0"></f:verbatim>
		   		<h:outputText value="#{msgs.stat_forum_authored}" />
    			<f:verbatim></h4></f:verbatim>
			</h:panelGroup>
			<h:panelGroup styleClass="itemNav specialLink">
				<h:commandLink action="#{ForumTool.processActionShowFullTextForAll}" value="#{msgs.stat_show_all}" title=" #{msgs.stat_show_all}" rendered="#{!empty mfStatisticsBean.userAuthoredStatistics}"/>	
			</h:panelGroup>
		</h:panelGrid>
		<h:outputText rendered="#{empty mfStatisticsBean.userAuthoredStatistics}" value="#{msgs.stat_no_authored_message}" styleClass="instruction" style="display:block"/>

		<div class="table-responsive">
  		<h:dataTable styleClass="table table-hover table-striped table-bordered lines nolines" id="members" value="#{mfStatisticsBean.userAuthoredStatistics}" var="stat" rendered="#{!empty mfStatisticsBean.userAuthoredStatistics}"
   	 		columnClasses="bogus,bogus,bogus,bogus,bogus" cellpadding="0" cellspacing="0">
  			<h:column>
  				<f:facet name="header"> 				
  					<h:commandLink action="#{mfStatisticsBean.toggleForumTitleSort}" title="#{msgs.stat_forum_title}">
					   	<h:outputText value="#{msgs.stat_forum_title}" />
						<h:graphicImage value="/images/sortascending.gif" rendered="#{mfStatisticsBean.forumTitleSort && mfStatisticsBean.ascendingForUser}" alt="#{msgs.stat_forum_title}"/>
						<h:graphicImage value="/images/sortdescending.gif" rendered="#{mfStatisticsBean.forumTitleSort && !mfStatisticsBean.ascendingForUser}" alt="#{msgs.stat_forum_title}"/>
					</h:commandLink>
   				</f:facet>
			   	<h:outputText value="#{stat.forumTitle}" />
			</h:column>
			<h:column>
  				<f:facet name="header"> 				
  					<h:commandLink action="#{mfStatisticsBean.toggleTopicTitleSort}" title="#{msgs.stat_topic_title}">
					   	<h:outputText value="#{msgs.stat_topic_title}" />
						<h:graphicImage value="/images/sortascending.gif" rendered="#{mfStatisticsBean.topicTitleSort && mfStatisticsBean.ascendingForUser}" alt="#{msgs.stat_topic_title}"/>
						<h:graphicImage value="/images/sortdescending.gif" rendered="#{mfStatisticsBean.topicTitleSort && !mfStatisticsBean.ascendingForUser}" alt="#{msgs.stat_topic_title}"/>
					</h:commandLink>
   				</f:facet>
			   	<h:outputText value="#{stat.topicTitle}" />
			</h:column>
  			<h:column>
  				<f:facet name="header">
				   <h:commandLink action="#{mfStatisticsBean.toggleDateSort}" title="#{msgs.stat_forum_date}">
					   	<h:outputText value="#{msgs.stat_forum_date}" />
						<h:graphicImage value="/images/sortascending.gif" rendered="#{mfStatisticsBean.forumDateSort && mfStatisticsBean.ascendingForUser}" alt="#{msgs.stat_forum_date}"/>
						<h:graphicImage value="/images/sortdescending.gif" rendered="#{mfStatisticsBean.forumDateSort && !mfStatisticsBean.ascendingForUser}" alt="#{msgs.stat_forum_date}"/>
					</h:commandLink>
  				</f:facet>
  				<h:outputText value="#{stat.forumDate}">
  					<f:convertDateTime pattern="#{msgs.date_format}" timeZone="#{ForumTool.userTimeZone}" locale="#{ForumTool.userLocale}"/>
  				</h:outputText>
  			</h:column>
  			<h:column>
  				<f:facet name="header">
				   <h:commandLink action="#{mfStatisticsBean.toggleSubjectSort}" title="#{msgs.stat_forum_subject}">
					   	<h:outputText value="#{msgs.stat_forum_subject}"  />
						<h:graphicImage value="/images/sortascending.gif" rendered="#{mfStatisticsBean.forumSubjectSort && mfStatisticsBean.ascendingForUser}" alt="#{msgs.stat_forum_subject}"/>
						<h:graphicImage value="/images/sortdescending.gif" rendered="#{mfStatisticsBean.forumSubjectSort && !mfStatisticsBean.ascendingForUser}" alt="#{msgs.stat_forum_subject}"/>						
					</h:commandLink>
  				</f:facet>
  				<h:commandLink action="#{mfStatisticsBean.processActionDisplayMsgBody}" value="#{stat.forumSubject}">
  							<f:param value="#{stat.msgId}" name="msgId"/> 				  			
  				 </h:commandLink>
  				 </h:column>

  				 <h:column>
  					<h:outputLink value="/tool/#{ForumTool.currentToolId}/discussionForum/message/dfMsgGrade" target="dialogFrame"
						onclick="dialogLinkClick(this);">
						<f:param value="#{stat.forumId}" name="forumId"/>
						<f:param value="#{stat.topicId}" name="topicId"/>
						<f:param value="#{stat.msgId}" name="messageId"/>
						<f:param value="#{mfStatisticsBean.selectedSiteUserId}" name="userId"/>						
						<f:param value="dialogDiv" name="dialogDivId"/>
						<f:param value="dialogFrame" name="frameId"/>
						<f:param value="gradesSavedDiv" name="gradesSavedDiv"/>
						<h:graphicImage value="/../../library/image/silk/award_star_gold_1.png" alt="#{msgs.cdfm_button_bar_grade}" />
						<h:outputText value=" #{msgs.cdfm_button_bar_grade}" />
					</h:outputLink>
					<h:outputText value=" #{msgs.cdfm_toolbar_separator} " />
					<h:commandLink action="#{ForumTool.processActionDisplayInThread}" value="#{msgs.stat_display_in_thread}" title=" #{msgs.stat_display_in_thread}">	
		  				  		<f:param value="#{stat.topicId}" name="topicId"/>
		  				  		<f:param value="#{stat.forumId}" name="forumId"/>
		  				  		<f:param value="#{stat.msgId}" name="msgId"/>
		  				  		
		  			</h:commandLink>
  			</h:column>
  		</h:dataTable>
  		</div>
  		
  			<f:verbatim><h4></f:verbatim>
		   <h:outputText value="#{msgs.stat_forum_read}" />
  			<f:verbatim></h4></f:verbatim>
		<h:outputText rendered="#{empty mfStatisticsBean.userReadStatistics}" value="#{msgs.stat_no_read_message}" styleClass="instruction" style="display:block"/>
  		<div class="table-responsive">
  		<h:dataTable styleClass="table table-hover table-striped table-bordered lines nolines" id="members2" value="#{mfStatisticsBean.userReadStatistics}" var="stat2" rendered="#{!empty mfStatisticsBean.userReadStatistics}"
   	 	 columnClasses="bogus,bogus,bogus,bogus,bogus,bogus" cellpadding="0" cellspacing="0">
  			<h:column>
  				<f:facet name="header">
		   			<h:commandLink action="#{mfStatisticsBean.toggleForumTitleSort2}" title="#{msgs.stat_forum_title}">
					   	<h:outputText value="#{msgs.stat_forum_title}" />
						<h:graphicImage value="/images/sortascending.gif" rendered="#{mfStatisticsBean.forumTitleSort2 && mfStatisticsBean.ascendingForUser2}" alt="#{msgs.stat_forum_title}"/>
						<h:graphicImage value="/images/sortdescending.gif" rendered="#{mfStatisticsBean.forumTitleSort2 && !mfStatisticsBean.ascendingForUser2}" alt="#{msgs.stat_forum_title}"/>
					</h:commandLink>
  				</f:facet>
			   	<h:outputText value="#{stat2.forumTitle}" />
			</h:column>
			<h:column>
  				<f:facet name="header"> 				
  					<h:commandLink action="#{mfStatisticsBean.toggleTopicTitleSort2}" title="#{msgs.stat_topic_title}">
					   	<h:outputText value="#{msgs.stat_topic_title}" />
						<h:graphicImage value="/images/sortascending.gif" rendered="#{mfStatisticsBean.topicTitleSort2 && mfStatisticsBean.ascendingForUser2}" alt="#{msgs.stat_topic_title}"/>
						<h:graphicImage value="/images/sortdescending.gif" rendered="#{mfStatisticsBean.topicTitleSort2 && !mfStatisticsBean.ascendingForUser2}" alt="#{msgs.stat_topic_title}"/>
					</h:commandLink>
   				</f:facet>
			   	<h:outputText value="#{stat2.topicTitle}" />
			</h:column>
			
  			<h:column>
  				<f:facet name="header">
				 <h:commandLink action="#{mfStatisticsBean.toggleDateSort2}" title="#{msgs.stat_forum_date}">
					   	<h:outputText value="#{msgs.stat_forum_date}" />
						<h:graphicImage value="/images/sortascending.gif" rendered="#{mfStatisticsBean.forumDateSort2 && mfStatisticsBean.ascendingForUser2}" alt="#{msgs.stat_forum_date}"/>
						<h:graphicImage value="/images/sortdescending.gif" rendered="#{mfStatisticsBean.forumDateSort2 && !mfStatisticsBean.ascendingForUser2}" alt="#{msgs.stat_forum_date}"/>
					</h:commandLink>
  				</f:facet>
  				<h:outputText value="#{stat2.forumDate}">
  					<f:convertDateTime pattern="#{msgs.date_format}" timeZone="#{ForumTool.userTimeZone}" locale="#{ForumTool.userLocale}"/>
  				</h:outputText>
  			</h:column>
  			<h:column>
  				<f:facet name="header">
				   	<h:commandLink action="#{mfStatisticsBean.toggleSubjectSort2}" title="#{msgs.stat_forum_subject}">
					   	<h:outputText value="#{msgs.stat_forum_subject}" />
						<h:graphicImage value="/images/sortascending.gif" rendered="#{mfStatisticsBean.forumSubjectSort2 && mfStatisticsBean.ascendingForUser2}" alt="#{msgs.stat_forum_subject}"/>
						<h:graphicImage value="/images/sortdescending.gif" rendered="#{mfStatisticsBean.forumSubjectSort2 && !mfStatisticsBean.ascendingForUser2}" alt="#{msgs.stat_forum_subject}"/>
					</h:commandLink>
  				</f:facet>
  				<h:outputText value="#{stat2.forumSubject}" />
  			</h:column>
  		</h:dataTable>
  		</div>
  	</h:form>
  </sakai:view>
 </f:view>
