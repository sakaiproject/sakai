 <%@ page import="java.util.*, javax.faces.context.*, javax.faces.application.*,
                 javax.faces.el.*, org.sakaiproject.tool.messageforums.*,
                 org.sakaiproject.api.app.messageforums.*,
                 org.sakaiproject.site.cover.SiteService,
                 org.sakaiproject.tool.messageforums.ui.MessageForumStatisticsBean,
                 org.sakaiproject.tool.cover.ToolManager" %>
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
 	
	FacesContext context = FacesContext.getCurrentInstance();
	Application app = context.getApplication();
	ValueBinding binding = app.createValueBinding("#{mfStatisticsBean}");
	MessageForumStatisticsBean statsBean = (MessageForumStatisticsBean) binding.getValue(context);
	statsBean.setDefaultSelectedAssign();
 	
%>
<f:view>
  <sakai:view >
  	<h:form id="dfStatisticsForm" rendered="#{ForumTool.instructor}">
<!-- discussionForum/statistics/dfStatisticsList.jsp-->
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
			
			function dialogLinkClick(link){
				var position =  $(link).position();
				dialogutil.openDialog('dialogDiv', 'dialogFrame', position.top);
			}
			var warn = false;
			window.onbeforeunload = function (evt) {
				if(warn && '<h:outputText value="#{mfStatisticsBean.selectedAssign}"/>' != 'Default_0'){
					var message = "<h:outputText value="#{msgs.confirm_navigation}" escape="false"/>";
					if (typeof evt == "undefined") {
					evt = window.event;
					}
					if (evt) {
					evt.returnValue = message;
					}
					return message;
				}
			}
		</script>
       		<script type="text/javascript">includeLatestJQuery("msgcntr");</script>
			<sakai:script contextBase="/messageforums-tool" path="/js/dialog.js"/>
			<script type="text/javascript" src="/library/js/spinner.js"></script>
			<link rel="stylesheet" type="text/css" href="/messageforums-tool/css/dialog.css" />
			<link rel="stylesheet" type="text/css" href="/messageforums-tool/css/msgcntr_statistics.css" />
		<script type="text/javascript">		
			function toggleComments(link){
				if(link.innerHTML == "<h:outputText value="#{msgs.stat_forum_comments_show}" escape="false"/>"){
					$('.comments').fadeIn();
					$('.commentsHidden').fadeOut();
					link.innerHTML = '<h:outputText value="#{msgs.stat_forum_comments_hide}"/>';
				}else{
					$('.comments').fadeOut();
					$('.commentsHidden').fadeIn();
					link.innerHTML = '<h:outputText value="#{msgs.stat_forum_comments_show}"/>';
				}		
				
				resize();
			}
			
			function applyDefaultToUngraded(value){
				$('.gradeInput').each(function(){
					if($(this).val() == null || $(this).val() == ''){
						$(this).val($('.defaultValue').val());
					}
				});
				
				dialogutil.showDiv('gradesNeedSaved');				
			}
			
			$(document).ready(function() {
				$('.selAssignVal').val('<h:outputText value="#{mfStatisticsBean.selectedAssign}"/>');
			});
		</script>
		
		<f:verbatim>
			<div id="dialogDiv" title="Grade Messages" style="display:none">
		       <iframe id="dialogFrame" name="dialogFrame" width="100%" height="100%" frameborder="0"></iframe>
		    </div>
		</f:verbatim>
		<f:verbatim>
	  		<div class="success" id="gradesSavedDiv" class="success" style="display:none">
	  	</f:verbatim>
	  		<h:outputText value="#{msgs.cdfm_grade_successful}"/>
	  	<f:verbatim>
	  		</div>
	  	</f:verbatim>
	  	<f:verbatim>
	  		<div class="information" id="gradesNeedSaved" class="success" style="display:none">
	  	</f:verbatim>
	  		<h:outputText value="#{msgs.stat_forum_default_grade_info}"/>
	  	<f:verbatim>
	  		</div>
	  	</f:verbatim>
	  	<h:messages globalOnly="true" infoClass="success" errorClass="alertMessage" rendered="#{! empty facesContext.maximumSeverity}"/>
		
  		<h:panelGrid columns="2" width="100%" styleClass="navPanel  specialLink">
          <h:panelGroup>
          	 <f:verbatim><h3></f:verbatim>
			      <h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_message_forums}" title=" #{msgs.cdfm_message_forums}"
			      		rendered="#{ForumTool.messagesandForums}" />
			      <h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_discussion_forums}" title=" #{msgs.cdfm_discussion_forums}"
			      		rendered="#{ForumTool.forumsTool}" />
			      <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
			      <h:commandLink action="#{mfStatisticsBean.processActionStatisticsByAllTopics}" value="#{msgs.stat_list}" title="#{msgs.stat_list}"/>
			      <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
			      <h:commandLink action="#{mfStatisticsBean.processActionStatisticsByTopic}" immediate="true">
				    <f:param value="" name="topicId"/>
				    <f:param value="#{mfStatisticsBean.selectedAllTopicsForumId}" name="forumId"/>
				    <h:outputText value="#{mfStatisticsBean.selectedAllTopicsForumTitle}" />
		      	  </h:commandLink>
			      <h:panelGroup rendered="#{!empty mfStatisticsBean.selectedAllTopicsTopicId}">
	    			  <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
			   		  <h:outputText value="#{mfStatisticsBean.selectedAllTopicsTopicTitle}" />
		   		  </h:panelGroup>
			    <f:verbatim></h3></f:verbatim>
          </h:panelGroup> 
          <h:panelGroup styleClass="itemNav">
	      	<h:outputText value="#{msgs.cdfm_select_assign}: "/>
	  		<h:selectOneMenu id="assignment" value="#{mfStatisticsBean.selectedAssign}" valueChangeListener="#{mfStatisticsBean.processGradeAssignChange}" styleClass="selAssignVal"
	          onchange="document.forms[0].submit();">
	           <f:selectItems value="#{mfStatisticsBean.assignments}" />
	        </h:selectOneMenu>          
          </h:panelGroup>  
          <h:panelGroup styleClass="itemNav" rendered="#{!empty mfStatisticsBean.groupsForStatisticsByTopic}">
          
          </h:panelGroup>
          <h:panelGroup styleClass="itemNav" rendered="#{!empty mfStatisticsBean.cachedGroupsForStatisticsByTopic}">
	      	<h:outputText value="#{msgs.filter_by_group}: "/>
	  		<h:selectOneMenu id="grade" value="#{mfStatisticsBean.selectedGroup}" valueChangeListener="#{mfStatisticsBean.processGroupChange}"
	          onchange="document.forms[0].submit();">
	           <f:selectItems value="#{mfStatisticsBean.cachedGroupsForStatisticsByTopic}" />
	        </h:selectOneMenu>          
          </h:panelGroup>  
        </h:panelGrid>
        
        
	  	
	  	<f:subview id="defaultValueView" rendered="#{mfStatisticsBean.selectedAssign != 'Default_0'}">
	  		<div>
	  			<h:inputText styleClass="defaultValue" size="5" value="0" onkeyup="warn = true;"/>
          		<f:verbatim>
          			<input type="button" onclick="applyDefaultToUngraded();" value="</f:verbatim><h:outputText value="#{msgs.stat_forum_default_grade}"/><f:verbatim>"/>
          		</f:verbatim>
          	</div>
	  	</f:subview>
	  	
	  	
		<%--
			Use cahcedTopicStatistics - value is cached from #{!empty mfStatisticsBean.groupForStatisticsByTopic} above.
			Retrieve the topic statistics, then clear the cache since dfStatisticsBean is scoped to the session, and we don't want this data to persist for future requests
		--%>
		<div class="table-responsive">
  		<h:dataTable styleClass="table table-hover table-striped table-bordered lines nolines" id="members" value="#{mfStatisticsBean.gradeStatisticsForStatsListByTopic}" var="stat" rendered="true"
   	 		columnClasses="specialLink,bogus,bogus,bogus,bogus,bogus,bogus" cellpadding="0" cellspacing="0">
  			<h:column>
  				<f:facet name="header">
  					<h:commandLink action="#{mfStatisticsBean.toggleTopicNameSort}" title="#{mfStatisticsBean.pureAnon ? msgs.stat_anon_user : msgs.stat_name}">
					   	<h:outputText value="#{mfStatisticsBean.pureAnon ? msgs.stat_anon_user : msgs.stat_name}" />
						<h:graphicImage value="/images/sortascending.gif" rendered="#{mfStatisticsBean.nameSort && mfStatisticsBean.ascending}" alt="#{msgs.stat_sort_name}"/>
						<h:graphicImage value="/images/sortdescending.gif" rendered="#{mfStatisticsBean.nameSort && !mfStatisticsBean.ascending}" alt="#{msgs.stat_sort_name}"/>
					</h:commandLink>
  				</f:facet>
  				<h:outputLink value="/tool/#{ForumTool.currentToolId}/discussionForum/statistics/dfStatisticsAllAuthoredMsgForOneUser" target="dialogFrame"
					onclick="dialogLinkClick(this);">
					<f:param value="#{stat.siteUserId}" name="siteUserId"/>
  				    <f:param value="dialogDiv" name="dialogDivId"/>
					<f:param value="dialogFrame" name="frameId"/>
					<h:outputText rendered="#{!stat.useAnonId}" value="#{stat.siteUser}" />
					<h:outputText rendered="#{stat.useAnonId}" value="#{stat.siteAnonId}" styleClass="anonymousAuthor"/>
				</h:outputLink>
			</h:column>
			<h:column>
  				<f:facet name="header">
  					
  				</f:facet>
  				<h:commandLink action="#{mfStatisticsBean.processActionStatisticsUser}" immediate="true" styleClass="font-size: small">
  				    <f:param value="#{stat.siteUserId}" name="siteUserId"/>
				   	<h:outputText value="#{msgs.stat_forum_details}" />
	          	</h:commandLink>
  			</h:column>
  			<h:column>
  				<f:facet name="header">
  					<h:commandLink action="#{mfStatisticsBean.toggleTopicAuthoredSort}" title="#{msgs.stat_authored}">
					   	<h:outputText value="#{msgs.stat_authored}" />
						<h:graphicImage value="/images/sortascending.gif" rendered="#{mfStatisticsBean.authoredSort && mfStatisticsBean.ascending}" alt="#{msgs.stat_sort_authored}"/>
						<h:graphicImage value="/images/sortdescending.gif" rendered="#{mfStatisticsBean.authoredSort && !mfStatisticsBean.ascending}" alt="#{msgs.stat_sort_authored}"/>
					</h:commandLink>
  				</f:facet>
  				<h:outputText value="#{stat.authoredForumsAmt}" />
  			</h:column>
  			<h:column>
  				<f:facet name="header">
  					<h:commandLink action="#{mfStatisticsBean.toggleTopicReadSort}" title="#{msgs.stat_read}">
					   	<h:outputText value="#{msgs.stat_read}" />
						<h:graphicImage value="/images/sortascending.gif" rendered="#{mfStatisticsBean.readSort && mfStatisticsBean.ascending}" alt="#{msgs.stat_sort_read}"/>
						<h:graphicImage value="/images/sortdescending.gif" rendered="#{mfStatisticsBean.readSort && !mfStatisticsBean.ascending}" alt="#{msgs.stat_sort_read}"/>
					</h:commandLink>
  				</f:facet>
  				<h:outputText value="#{stat.readForumsAmt}" />
  			</h:column>
  			<h:column>
  				<f:facet name="header">
  					<h:commandLink action="#{mfStatisticsBean.toggleTopicUnreadSort}" title="#{msgs.stat_unread}">
					   	<h:outputText value="#{msgs.stat_unread}" />
						<h:graphicImage value="/images/sortascending.gif" rendered="#{mfStatisticsBean.unreadSort && mfStatisticsBean.ascending}" alt="#{msgs.stat_sort_unread}"/>
						<h:graphicImage value="/images/sortdescending.gif" rendered="#{mfStatisticsBean.unreadSort && !mfStatisticsBean.ascending}" alt="#{msgs.stat_sort_unread}"/>
					</h:commandLink>
  				</f:facet>
  				<h:outputText value="#{stat.unreadForumsAmt}" />
  			</h:column>
  			<h:column>
  				<f:facet name="header">
  					<h:commandLink action="#{mfStatisticsBean.toggleTopicPercentReadSort}" title="#{msgs.stat_percent_read}">
					   	<h:outputText value="#{msgs.stat_percent_read}" />
						<h:graphicImage value="/images/sortascending.gif" rendered="#{mfStatisticsBean.percentReadSort && mfStatisticsBean.ascending}" alt="#{msgs.stat_sort_percent_read}"/>
						<h:graphicImage value="/images/sortdescending.gif" rendered="#{mfStatisticsBean.percentReadSort && !mfStatisticsBean.ascending}" alt="#{msgs.stat_sort_percent_read}"/>
					</h:commandLink>
  				</f:facet>
  				<h:outputText value="#{stat.percentReadForumsAmt}">
  					<f:convertNumber type="percent" />
  				</h:outputText>
  			</h:column>
  			<h:column rendered="#{mfStatisticsBean.selectedAssign == 'Default_0'}">
  				<f:facet name="header">
  				</f:facet>
  				<h:outputLink value="/tool/#{ForumTool.currentToolId}/discussionForum/message/dfMsgGrade" target="dialogFrame"
						onclick="dialogLinkClick(this);">
						<f:param value="#{mfStatisticsBean.selectedAllTopicsForumId}" name="forumId"/>
						<f:param value="#{mfStatisticsBean.selectedAllTopicsTopicId}" name="topicId"/>
						<f:param value="#{stat.siteUserId}" name="userId"/>
						<f:param value="" name="messageId"/>
						<f:param value="dialogDiv" name="dialogDivId"/>
						<f:param value="dialogFrame" name="frameId"/>
						<f:param value="gradesSavedDiv" name="gradesSavedDiv"/>
						<h:graphicImage value="/../../library/image/silk/award_star_gold_1.png" alt="#{msgs.cdfm_button_bar_grade}" />
						<h:outputText value=" #{msgs.cdfm_button_bar_grade}" />
					</h:outputLink>
  			</h:column>
  			<h:column rendered="#{mfStatisticsBean.selectedAssign != 'Default_0'}">
  				<f:facet name="header">
  					<h:commandLink action="#{mfStatisticsBean.toggleTopicGradeSort}" title="#{mfStatisticsBean.selAssignName}">
					   	<h:outputText value="#{mfStatisticsBean.selAssignName}" />
						<h:graphicImage value="/images/sortascending.gif" rendered="#{mfStatisticsBean.gradeSort && mfStatisticsBean.ascending}" alt="#{mfStatisticsBean.selAssignName}"/>
						<h:graphicImage value="/images/sortdescending.gif" rendered="#{mfStatisticsBean.gradeSort && !mfStatisticsBean.ascending}" alt="#{mfStatisticsBean.selAssignName}"/>
						<f:verbatim><br/></f:verbatim>
						<h:outputFormat value=" #{msgs.cdfm_points_possible}" rendered="#{mfStatisticsBean.gradeByPoints}">
							<f:param value="#{mfStatisticsBean.gbItemPointsPossible}"/>
						</h:outputFormat>
					</h:commandLink>
  				</f:facet>
  				<h:inputText size="5" value="#{stat.gradebookAssignment.score}" rendered="#{stat.gradebookAssignment.allowedToGrade}" styleClass="gradeInput" onkeyup="warn = true;"/>
  				<h:outputText value="#{msgs.stat_forum_na}" rendered="#{!stat.gradebookAssignment.allowedToGrade}"/>
  				<h:outputText value=" %" rendered="#{mfStatisticsBean.gradeByPercent && stat.gradebookAssignment.allowedToGrade}" />
  			</h:column>
  			<h:column rendered="#{mfStatisticsBean.selectedAssign != 'Default_0'}"	>
  				<f:facet name="header">  		
			  		<h:outputLink id="toggleComments" onclick="toggleComments(this); return null;" value="#">
			  			<h:outputText value="#{msgs.stat_forum_comments_show}"/>
			  		</h:outputLink>
			  			
			  				  		  				
  				</f:facet>
  				<h:inputTextarea rows="4" cols="35" value="#{stat.gradebookAssignment.comment}" style="display:none" styleClass="comments" rendered="#{stat.gradebookAssignment.allowedToGrade}" onkeyup="warn = true"/>
  				<h:outputText value="#{msgs.stat_forum_comments_hidden}" styleClass="commentsHidden" rendered="#{stat.gradebookAssignment.allowedToGrade}"/>
  				<h:outputText value="#{msgs.stat_forum_na}" rendered="#{!stat.gradebookAssignment.allowedToGrade}"/>
  			</h:column>
  		</h:dataTable>
  		</div>
		<h:panelGrid columns="1" width="100%" styleClass="navPanel  specialLink">
		  <h:panelGroup styleClass="itemNav" rendered="#{mfStatisticsBean.selectedAssign != 'Default_0'}">
		  	<h:commandButton action="#{mfStatisticsBean.proccessActionSubmitGrades}" value="#{msgs.stat_forum_submit_grades}" accesskey="s"
		  		onclick="warn = false;SPNR.disableControlsAndSpin( this, null );" />
		  	<h:commandButton action="" value="#{msgs.stat_forum_submit_grades_cancel}" accesskey="c"
		  		onclick="warn = false;SPNR.disableControlsAndSpin( this, null );" />
		  </h:panelGroup>	
        </h:panelGrid>  
  	</h:form>
  </sakai:view>
</f:view>
