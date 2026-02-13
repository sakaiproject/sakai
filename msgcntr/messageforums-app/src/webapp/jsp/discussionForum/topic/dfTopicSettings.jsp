<%@ page import="java.util.*, javax.faces.context.*, javax.faces.application.*,
                 javax.faces.el.*, org.sakaiproject.tool.messageforums.*"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>
<f:view>
	<sakai:view toolCssHref="/messageforums-tool/css/msgcntr.css">
      <h:form id="revise">
        <script>includeLatestJQuery("msgcntr");</script>
        <script src="/messageforums-tool/js/messages.js"></script>
		<script>
			$(document).ready(function(){
				//fade permission block and then disable all the inputs/selects in the permission include so as not to confuse people
				$('#permissionReadOnly').fadeTo("fast", 0.50);
				// cannot seem to disable these controls and still submit
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
		<%
	  	String thisId = request.getParameter("panel");
  		if (thisId == null) 
  		{
    		thisId = "Main" + org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement().getId();
  		}
		%>
       		<script src="/messageforums-tool/js/sak-10625.js"></script>
       		<script src="/messageforums-tool/js/forum.js"></script>			
		<%--//designNote: this just feels weird - presenting somehting that sort of looks like the form used to create the topic (with an editable permissions block!) to comfirm deletion --%>
<!--jsp/discussionForum/topic/dfTopicSettings.jsp-->
		<%--<sakai:tool_bar_message value="#{msgs.cdfm_delete_topic_title}"/>--%>
        
		<h:outputText id="alert-delete" styleClass="sak-banner-warn" style="display:block" value="#{msgs.cdfm_delete_topic}" rendered="#{ForumTool.selectedTopic.markForDeletion}"/>
        <h:outputText styleClass="sak-banner-warn" value="#{msgs.cdfm_duplicate_topic_confirm}" rendered="#{ForumTool.selectedTopic.markForDuplication}" style="display:block" />
		<div class="topicBloc" style="padding:0 .5em"><h:messages styleClass="sak-banner-warn" id="errorMessages" rendered="#{! empty facesContext.maximumSeverity}" />
			<p>
				<span class="title">
					<h:panelGroup rendered="#{ForumTool.selectedTopic.locked=='true'}">
						<span class="bi bi-lock-fill" aria-hidden="true" style="margin-right:.3em"></span>
					</h:panelGroup>
					<h:panelGroup rendered="#{ForumTool.selectedTopic.locked=='false'}">
						<span class="bi bi-unlock-fill" aria-hidden="true" style="margin-right:.3em"></span>
					</h:panelGroup>
					<h:outputText value="#{ForumTool.selectedTopic.topic.title}" rendered="#{!ForumTool.selectedTopic.markForDuplication}"/>
                    <h:inputText size="50" value="#{ForumTool.selectedTopic.topic.title}" id="topic_title" rendered="#{ForumTool.selectedTopic.markForDuplication}">
                        <f:validateLength maximum="255" minimum="1" />
                    </h:inputText>                   
                    
				</span>
				<h:outputText   value="#{msgs.cdfm_openb}"/>
				<h:outputText   value="#{msgs.cdfm_moderated}"  rendered="#{ForumTool.selectedTopic.topic.moderated=='true'}" />
				<h:outputText   value="#{msgs.cdfm_notmoderated}"  rendered="#{ForumTool.selectedTopic.topic.moderated=='false'}" />
				<h:outputText   value="#{msgs.cdfm_closeb}"/>

				</p>
			<p class="textPanel">
				    <h:outputText id="topic_shortDescription"  value="#{ForumTool.selectedTopic.topic.shortDescription}"/>
			</p>

			<h:panelGroup>
				<h:panelGroup layout="block" id="openLinkBlock" styleClass="toggleParent openLinkBlock">
					<a href="#" id="showMessage" class="toggle show">
						<h:graphicImage url="/images/collapse.gif" alt=""/>
						<h:outputText value=" #{msgs.cdfm_read_full_description}" />
					</a>
				</h:panelGroup>
				<h:panelGroup layout="block" id="hideLinkBlock" styleClass="toggleParent hideLinkBlock display-none">
					<a href="#" id="hideMessage" class="toggle show">
						<h:graphicImage url="/images/expand.gif" alt="" />
						<h:outputText value=" #{msgs.cdfm_hide_full_description}"/>
					</a>
				</h:panelGroup>
			</h:panelGroup>

			<h:panelGroup layout="block" id="fullTopicDescription" styleClass="textPanel fullTopicDescription">
				<h:outputText escape="false" value="#{ForumTool.selectedTopic.topic.extendedDescription}" />

				<div class="table">
					<h:dataTable value="#{ForumTool.selectedTopic.attachList}" var="eachAttach" rendered="#{!empty ForumTool.selectedTopic.attachList}" styleClass="table table-hover table-striped table-bordered" columnClasses="attach,bogus">
						<h:column>
							<sakai:contentTypeMap fileType="#{eachAttach.attachment.attachmentType}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>
							<h:graphicImage id="exampleFileIcon" value="#{imagePath}" alt="" />
						</h:column>
						<h:column>
							<h:outputLink value="#{eachAttach.url}" target="_blank">
								<h:outputText value="#{eachAttach.attachment.attachmentName}"  style="text-decoration:underline;"/>
							</h:outputLink>
						</h:column>
					</h:dataTable>
				</div>
			</h:panelGroup>
		</div>
       <div class="act">
          <h:commandButton action="#{ForumTool.processActionReviseTopicSettings}" id="revise"  
                           value="#{msgs.cdfm_button_bar_revise}" rendered="#{!ForumTool.selectedTopic.markForDeletion && !ForumTool.selectedTopic.markForDuplication}"
                           accesskey="r" styleClass="active"> 
    	 	  	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/> 
    	 	  	<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>        
          </h:commandButton>
          <h:commandButton action="#{ForumTool.processActionDeleteTopicConfirm}" id="delete_confirm" 
                           value="#{msgs.cdfm_button_bar_delete_topic}" rendered="#{!ForumTool.selectedTopic.markForDeletion && !ForumTool.selectedTopic.markForDuplication}"
                           styleClass="blockMeOnClick">
	        	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
          </h:commandButton>
          <h:commandButton action="#{ForumTool.processActionDeleteTopic}" id="delete" 
                           value="#{msgs.cdfm_button_bar_delete_topic}" rendered="#{ForumTool.selectedTopic.markForDeletion}"
                           styleClass="blockMeOnClick">
	        	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
          </h:commandButton>
          
          <h:commandButton id="duplicate" action="#{ForumTool.processActionDuplicateTopic}" 
                           value="#{msgs.cdfm_duplicate_topic}" rendered="#{ForumTool.selectedTopic.markForDuplication}"
                           accesskey="s" styleClass="blockMeOnClick">
	        	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
          </h:commandButton>
          
          <h:commandButton immediate="true" action="#{ForumTool.processReturnToOriginatingPage}" id="cancel" 
                           value="#{msgs.cdfm_button_bar_cancel} " accesskey="x" />
         <h:outputText styleClass="sak-banner-info" style="display:none" value="#{msgs.cdfm_processing_submit_message}" />
       </div>
	 </h:form>
    </sakai:view>
</f:view>
