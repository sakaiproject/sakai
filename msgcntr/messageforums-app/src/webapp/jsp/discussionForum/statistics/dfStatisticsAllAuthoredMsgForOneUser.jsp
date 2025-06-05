<%@ page import="java.util.*, javax.faces.context.*, javax.faces.application.*,
                 javax.faces.el.*, org.sakaiproject.tool.messageforums.*,
                 org.sakaiproject.api.app.messageforums.*,
                 org.sakaiproject.site.cover.SiteService,
                 org.sakaiproject.tool.messageforums.ui.MessageForumStatisticsBean,
                 org.sakaiproject.tool.cover.ToolManager" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>
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

	//Check if user called this page with a popup dialog
	
	String selectedUserId = request.getParameter("siteUserId");
	String frameId = request.getParameter("frameId");
	String dialogDivId = request.getParameter("dialogDivId");
	boolean isDialogBox = false;

	if(selectedUserId != null && !"".equals(selectedUserId)){
		isDialogBox = true;
		statsBean.selectedSiteUserId = selectedUserId;
		//set up default settings:
		statsBean.processActionStatisticsUserHelper();	
	}
%>

<f:view>
  <sakai:view toolCssHref="/messageforums-tool/css/msgcntr.css">
  	<h:form id="dfStatisticsForm">
  	<!-- discussionForum/statistics/dfStatisticsAllAuthoredMsgForOneUser.jsp-->
  		<script>
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

  	       	<script>includeLatestJQuery("msgcntr");</script>
			<script src="/messageforums-tool/js/sak-10625.js"></script>
			<script src="/messageforums-tool/js/forum.js"></script>
			<script src="/messageforums-tool/js/messages.js"></script>
			<script src="/messageforums-tool/js/dialog.js"></script>
  			<link rel="stylesheet" type="text/css" href="/messageforums-tool/css/dialog.css" />
  			<link rel="stylesheet" type="text/css" href="/messageforums-tool/css/msgcntr_statistics.css" />

			<script>
				$(document).ready(function() {
					$(".messageBody").each(function(index){
						let msgBody = $(this).html();
						msgBody = msgBody.replace(/\n/g,',').replace(/\s/g,' ').replace(/  ,/g,',');
						const wordCountId = $(this).attr('id').substring(11, $(this).attr('id').length);
						$("#wordCountSpan" + wordCountId).html(getWordCount(msgBody));
					});
					resize();
                    var menuLink = $('#forumsStatisticsMenuLink');
                    var menuLinkSpan = menuLink.closest('span');
                    menuLinkSpan.addClass('current');
                    menuLinkSpan.html(menuLink.text());
				});

				function dialogLinkClick(link){
					var position =  $(link).position();
					dialogutil.openDialog('dialogDiv', 'dialogFrame', position.top);
				}
			</script>

           <%@ include file="/jsp/discussionForum/menu/forumsMenu.jsp" %>

  			<% if(isDialogBox){ %>
				<h:panelGroup layout="block" styleClass="itemNav">
					<h:panelGroup layout="block" styleClass="breadCrumb">
						<h3><h:outputText value="#{mfStatisticsBean.selectedSiteUser}"/></h3>
					</h:panelGroup>
				</h:panelGroup>
			<% }else {%>
          	 <h:panelGroup layout="block" styleClass="breadCrumb" rendered="#{ForumTool.instructor}"><h3>
			      <h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_message_forums}" title=" #{msgs.cdfm_message_forums}"
			      		rendered="#{ForumTool.messagesandForums}" />
			      <h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_discussions}" title=" #{msgs.cdfm_discussions}"
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
			      <h:commandLink action="#{mfStatisticsBean.processActionBackToUser}" >
					  <h:outputText value="#{mfStatisticsBean.selectedSiteUser}" />
			      </h:commandLink>
			      <h:outputText value=" / "/>
			      <h:outputText value="#{msgs.stat_authored}" />
			  </h3></h:panelGroup>

			<h:panelGroup id="forumsAction">
				<h:outputLink styleClass="button" id="print" value="javascript:printFriendly('#{ForumTool.printFriendlyAllAuthoredMsg}');" title="#{msgs.cdfm_print}">
					<span class="bi bi-printer" aria-hidden="true"></span>
					<span class="sr-only"><h:outputText value="#{msgs.print_friendly}" /></span>
				</h:outputLink>
			</h:panelGroup>

			 <%}%>
			  <div class="success" id="gradesSavedDiv" class="success" style="display:none">
			  	<h:outputText value="#{msgs.cdfm_grade_successful}"/>
			  </div>

			  <div id="dialogDiv" title="Grade Messages" style="display:none">
			    <iframe id="dialogFrame" name="dialogFrame" width="100%" height="100%" frameborder="0"></iframe>
			  </div>

			  <div style="font-weight:bold;" styleClass="specialLink">
					<h:commandLink action="#{mfStatisticsBean.toggleTopicTitleSort3}" title=" #{msgs.stat_sort_by_topic}">
						<h:outputText value="#{msgs.stat_sort_by_topic}" />
						<h:panelGroup rendered="#{mfStatisticsBean.topicTitleSort3 && mfStatisticsBean.ascendingForUser3}">
							<span class="bi bi-sort-up" aria-hidden="true"></span>
							<span class="sr-only"><h:outputText value="#{msgs.stat_topic_title}"/></span>
						</h:panelGroup>
						<h:panelGroup rendered="#{mfStatisticsBean.topicTitleSort3 && !mfStatisticsBean.ascendingForUser3}">
							<span class="bi bi-sort-down" aria-hidden="true"></span>
							<span class="sr-only"><h:outputText value="#{msgs.stat_topic_title}"/></span>
						</h:panelGroup>
					</h:commandLink>
	  				<h:outputText value=" " /><h:outputText value=" | " /><h:outputText value=" " />
					<h:commandLink action="#{mfStatisticsBean.toggleDateSort3}" title=" #{msgs.stat_sort_by_date}">	
						<h:outputText value="#{msgs.stat_sort_by_date}" />
						<h:panelGroup rendered="#{mfStatisticsBean.forumDateSort3 && mfStatisticsBean.ascendingForUser3}">
							<span class="bi bi-sort-up" aria-hidden="true"></span>
							<span class="sr-only"><h:outputText value="#{msgs.stat_forum_date}"/></span>
						</h:panelGroup>
						<h:panelGroup rendered="#{mfStatisticsBean.forumDateSort3 && !mfStatisticsBean.ascendingForUser3}">
							<span class="bi bi-sort-down" aria-hidden="true"></span>
							<span class="sr-only"><h:outputText value="#{msgs.stat_forum_date}"/></span>
						</h:panelGroup>
					</h:commandLink>
			  </div>
  			<h:dataTable id="staticAllMessages" value="#{mfStatisticsBean.userAuthoredStatistics2}" var="stat" styleClass="messagesFlat" columnClasses="bogus">	
   				<h:column>
				<h:panelGroup rendered="#{!stat.msgDeleted}" layout="block"> 
				<h:panelGroup>
					<f:verbatim>
	  					<span id="messageBody</f:verbatim><h:outputText value="#{stat.msgId}"/><f:verbatim>" style="display: none" class="messageBody"> </f:verbatim>
	  					<h:outputText escape="false" value="#{stat.message}"/>
	  					</span>
						<span>
							<span class="fa fa-plus-square" aria-hidden="true"></span>
							<h:outputText value="#{msgs.cdfm_message_count}: " escape="false" />
							<span id="wordCountSpan<h:outputText value="#{stat.msgId}"/>"> </span>
						</span>
						<br>
						<h4 style="border-bottom:1px solid #ccc;padding-bottom:5px;overflow:hidden">
						<p style="width:74%;float:left;margin:0;padding:0;font-size:110%;color:#000">
						<h:outputText value="#{stat.forumTitle}" />
						<h:outputText value=" / " />
						<h:outputText value="#{stat.topicTitle}" />
						<h:outputText value=" / " />
						<h:outputText  value= "#{stat.forumSubject} " />
						<h:outputText value="#{stat.forumDate}" styleClass="textPanelFooter">
							<f:convertDateTime pattern="#{msgs.date_format_paren}" timeZone="#{ForumTool.userTimeZone}" locale="#{ForumTool.userLocale}"/>
						</h:outputText>
						<f:verbatim></p></f:verbatim>
						<% if(!isDialogBox){ %>
						<h:panelGroup rendered="#{ForumTool.instructor}" style="display:block;float:right;width:25%;text-align:right">
							<h:outputLink value="/tool/#{ForumTool.currentToolId}/discussionForum/message/dfMsgGrade" target="dialogFrame"
								onclick="dialogLinkClick(this);" rendered="#{ForumTool.instructor}">
								<f:param value="#{stat.forumId}" name="forumId"/>
								<f:param value="#{stat.topicId}" name="topicId"/>
								<f:param value="#{stat.msgId}" name="messageId"/>
								<f:param value="dialogDiv" name="dialogDivId"/>
								<f:param value="dialogFrame" name="frameId"/>
								<f:param value="gradesSavedDiv" name="gradesSavedDiv"/>
								<f:param value="#{mfStatisticsBean.selectedSiteUserId}" name="userId"/>
								<span class="bi bi-star-fill" aria-hidden="true"></span>
								<h:outputText value=" #{msgs.cdfm_button_bar_grade}" />
							</h:outputLink>
							<h:outputText value=" #{msgs.cdfm_toolbar_separator} " rendered="#{ForumTool.instructor}" />
							<h:commandLink action="#{ForumTool.processActionDisplayInThread}" title=" #{msgs.stat_display_in_thread}" >
								<f:param value="#{stat.forumId}" name="forumId"/>
		  				  		<f:param value="#{stat.topicId}" name="topicId"/>
		  				  		<f:param value="#{stat.msgId}" name="msgId"/>
								<h:outputText value=" #{msgs.stat_display_in_thread}" />
							</h:commandLink>
						</h:panelGroup>
						<%}%>
					<f:verbatim></h4></f:verbatim>
				</h:panelGroup>
				<mf:htmlShowArea value="#{stat.message}" hideBorder="true" />

				<h:panelGroup rendered="#{!empty stat.decoAttachmentsList}" style="display:block;" styleClass="indnt1">
					<h:dataTable value="#{stat.decoAttachmentsList}" var="eachAttach" styleClass="attachListJSF" rendered="#{!empty stat.decoAttachmentsList}">
					<h:column rendered="#{!empty stat.decoAttachmentsList}">
						<sakai:contentTypeMap fileType="#{eachAttach.attachment.attachmentType}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>
						<h:graphicImage id="exampleFileIcon" value="#{imagePath}" />
						<h:outputLink value="#{eachAttach.url}" target="_blank">
							<h:outputText value=" #{eachAttach.attachment.attachmentName}" />
						</h:outputLink>
					</h:column>
					</h:dataTable>
				</h:panelGroup>
				</h:panelGroup>
  			</h:column>

  			<%-- deleted messages  - not implemented

  			<h:column rendered="#{stat.msgDeleted}">
   				<f:verbatim><div class="hierItemBlock"></f:verbatim>
				<f:verbatim><h4 class="textPanelHeader"></f:verbatim>
				<f:verbatim><div class="specialLink" style="width:65%;float:left;text-align:left"></f:verbatim>
				<h:panelGroup>
					<h:panelGroup styleClass="inactive">
						<f:verbatim><span></f:verbatim>
						<h:outputText value="#{msgs.cdfm_msg_deleted_label}" />
						<f:verbatim></span></f:verbatim>
 					</h:panelGroup>
					<f:verbatim></div ></f:verbatim>
					<f:verbatim><div style="width:30%;float:right;text-align:right"	class="specialLink"></f:verbatim>
				</h:panelGroup>
				<h:panelGroup>
					<h:commandLink action="#{ForumTool.processActionDisplayInThread}" title=" #{msgs.stat_display_in_thread}" >
							<f:param value="#{stat.forumId}" name="forumId"/>
  				  			<f:param value="#{stat.topicId}" name="topicId"/>
  				  			<f:param value="#{stat.msgId}" name="msgId"/>
						<h:outputText value="#{msgs.stat_display_in_thread}" />
					</h:commandLink>
				</h:panelGroup>
			
				<f:verbatim></div ></f:verbatim>
				<f:verbatim><div style="clear:both;height:.1em;width:100%;"></div></f:verbatim>
				<f:verbatim></h4></f:verbatim>
				<mf:htmlShowArea value="" hideBorder="true" />
				<f:verbatim></div></f:verbatim>
  			</h:column>
			
			--%>
  		</h:dataTable>

		<% if(!isDialogBox){ %>
		<h:panelGroup styleClass="act" style="display:block">
			<h:commandButton action="#{mfStatisticsBean.processActionBackToUser}" value="#{mfStatisticsBean.buttonUserName}" title="#{mfStatisticsBean.buttonUserName}"/>
		</h:panelGroup>
		<%}%>

<% if(isDialogBox){ %>
    <!-- This is used to keep the dialogbox state when going to the next page (this page) -->
    <f:verbatim>
    <input type="text" id="siteUserId" name="siteUserId" value="<%= selectedUserId%>" style="display: none;"/>
    <input type="text" id="frameId" name="frameId" value="<%=frameId%>" style="display: none;"/>
    <input type="text" id="dialogDivId" name="dialogDivId" value="<%=dialogDivId%>" style="display: none;"/>
    </f:verbatim>
<%}%>
		
  	</h:form>
  </sakai:view>
 </f:view>
