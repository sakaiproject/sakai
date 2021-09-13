<%@ page import="java.util.*, javax.faces.context.*, javax.faces.application.*,
                 javax.faces.el.*, org.sakaiproject.tool.messageforums.*"%>
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
%>
<f:view>
	<sakai:view toolCssHref="/messageforums-tool/css/msgcntr.css">
      <h:form id="revise">
                <script>includeLatestJQuery("msgcntr");</script>

       		<script src="/messageforums-tool/js/forum.js"></script>
			<%--			--%>
       		<script src="/messageforums-tool/js/sak-10625.js"></script>
       		<script src="/messageforums-tool/js/messages.js"></script>

		<script>
			$(document).ready(function(){
				//fade permission block 
				// $('#permissionReadOnly').fadeTo("fast", 0.50);
				// and then disable all the inputs/selects in the permission include so as not to confuse people
				// cannot seem to be able to submit this if these inputs are disabled :-(
				// $('#permissionReadOnly input, #permissionReadOnly select').attr('disabled', 'disabled');
				//toggle the long description, hiding the hide link, then toggling the hide, show links and description
				$('a#hide').hide();
				$('#toggle').hide();
				$('a#show,a#hide').click(function(){
					$('#toggle,a#hide,a#show').toggle();
					resizeFrame('grow');
					return false;
				});
				var menuLink = $('#forumsMainMenuLink');
				var menuLinkSpan = menuLink.closest('span');
				menuLinkSpan.addClass('current');
				menuLinkSpan.html(menuLink.text());
			});
		</script>
		<%@ include file="/jsp/discussionForum/menu/forumsMenu.jsp" %>
<!--jsp/discussionForum/forum/dfForumSettings.jsp-->
		<%--<sakai:tool_bar_message value="#{msgs.cdfm_delete_forum_title}" />--%>
		<%--//designNote: this just feels weird - presenting somehting that sort of looks like the form used to create the forum (with an editable permissions block!) to comfirm deletion --%>
		<h:outputText id="alert-delete" styleClass="sak-banner-warn" value="#{msgs.cdfm_delete_forum}" rendered="#{ForumTool.selectedForum.markForDeletion}" style="display:block" />	
        <h:outputText styleClass="sak-banner-warn" value="#{msgs.cdfm_duplicate_forum_confirm}" rendered="#{ForumTool.selectedForum.markForDuplication}" style="display:block" />
		<table class="forumHeader">
			  <tr>
					<td><h:messages styleClass="sak-banner-warn" id="errorMessages" rendered="#{! empty facesContext.maximumSeverity}" />
				<h:graphicImage url="/images/silk/lock.png" alt="#{msgs.cdfm_forum_locked}" rendered="#{ForumTool.selectedForum.locked=='true'}"  style="margin-right:.3em"/>
				<h:graphicImage url="/images/silk/lock_open.png" alt="#{msgs.cdfm_forum_locked}" rendered="#{ForumTool.selectedForum.locked=='false'}"  style="margin-right:.3em"/>
				<span class="title">
                    <h:outputText value="#{ForumTool.selectedForum.forum.title}" rendered="#{!ForumTool.selectedForum.markForDuplication}"/>
				    <h:inputText size="50" value="#{ForumTool.selectedForum.forum.title}" id="forum_title" rendered="#{ForumTool.selectedForum.markForDuplication}">
                        <f:validateLength minimum="1" maximum="255" />
                    </h:inputText>
				</span>
				<h:outputText   value="#{msgs.cdfm_openb}"/>
					<h:outputText   value="#{msgs.cdfm_moderated}"  rendered="#{ForumTool.selectedForum.moderated=='true'}" />
					<h:outputText   value="#{msgs.cdfm_notmoderated}"  rendered="#{ForumTool.selectedForum.moderated=='false'}" />
				<h:outputText   value="#{msgs.cdfm_closeb}"/>
			<p class="textPanel">
				    <h:outputText id="forum_shortDescription"  value="#{ForumTool.selectedForum.forum.shortDescription}"/>
			</p>

			<h:panelGroup>
				<h:panelGroup layout="block" id="openLinkBlock" styleClass="toggleParent openLinkBlock display-none">
					<a href="#" id="showMessage" class="toggle show">
						<h:graphicImage url="/images/collapse.gif" alt=""/>
						<h:outputText value=" #{msgs.cdfm_read_full_description}" />
					</a>
				</h:panelGroup>
				<h:panelGroup layout="block" id="hideLinkBlock" styleClass="toggleParent hideLinkBlock">
					<a href="#" id="hideMessage" class="toggle show">
						<h:graphicImage url="/images/expand.gif" alt="" />
						<h:outputText value=" #{msgs.cdfm_hide_full_description}"/>
					</a>
				</h:panelGroup>
			</h:panelGroup>

			<h:panelGroup layout="block" id="fullTopicDescription" styleClass="textPanel fullTopicDescription">
				<h:outputText escape="false" value="#{ForumTool.selectedForum.forum.extendedDescription}" />

				<h:dataTable value="#{ForumTool.selectedForum.attachList}" var="eachAttach" rendered="#{!empty ForumTool.selectedForum.attachList}" styleClass="table table-hover table-striped table-bordered" columnClasses="bogus">
					<h:column>
						<sakai:contentTypeMap fileType="#{eachAttach.attachment.attachmentType}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>
						<h:graphicImage id="exampleFileIcon" value="#{imagePath}" alt="" />
						<h:outputLink value="#{eachAttach.url}" target="_new_window">
							<h:outputText value="#{eachAttach.attachment.attachmentName}"  style="text-decoration:underline;"/>
						</h:outputLink>
					</h:column>
				</h:dataTable>
			</h:panelGroup>
		</div>	
		</td>
		</tr>
		</table>
       <div class="act">
          <h:commandButton id ="revise" rendered="#{!ForumTool.selectedForum.markForDeletion && !ForumTool.selectedForum.markForDuplication}" 
                           immediate="true"  action="#{ForumTool.processActionReviseForumSettings}" 
                           value="#{msgs.cdfm_button_bar_revise}" accesskey="r"> 
    	 	  	<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>    	 	  	
          </h:commandButton>
          
          <h:commandButton id="delete_confirm" action="#{ForumTool.processActionDeleteForumConfirm}" 
                           value="#{msgs.cdfm_button_bar_delete_forum}" rendered="#{!ForumTool.selectedForum.markForDeletion && !ForumTool.selectedForum.markForDuplication}"
                           accesskey="" styleClass="blockMeOnClick">
	        	<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
          </h:commandButton>
          
          <h:commandButton id="delete" action="#{ForumTool.processActionDeleteForum}" 
                           value="#{msgs.cdfm_button_bar_delete_forum}" rendered="#{ForumTool.selectedForum.markForDeletion}"
                           accesskey="" styleClass="blockMeOnClick">
	        	<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
          </h:commandButton>
          
          <h:commandButton id="duplicate" action="#{ForumTool.processActionDuplicateForum}" 
                           value="#{msgs.cdfm_duplicate_forum}" rendered="#{ForumTool.selectedForum.markForDuplication}"
                           accesskey="s" styleClass="blockMeOnClick">
	        	<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
          </h:commandButton>
          
          <h:commandButton id="cancel" immediate="true" action="#{ForumTool.processReturnToOriginatingPage}" 
                           value="#{msgs.cdfm_button_bar_cancel}" accesskey="x" />
         
         <h:outputText styleClass="sak-banner-info" style="display:none" value="#{msgs.cdfm_processing_submit_message}" />
       </div>
	 </h:form>
    </sakai:view>
</f:view>
