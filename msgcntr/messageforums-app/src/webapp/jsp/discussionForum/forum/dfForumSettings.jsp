<%@ page import="java.util.*, javax.faces.context.*, javax.faces.application.*,
                 javax.faces.el.*, org.sakaiproject.tool.messageforums.*"%>
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
	<sakai:view toolCssHref="/messageforums-tool/css/msgcntr.css">
      <h:form id="revise">
                <script type="text/javascript">includeLatestJQuery("msgcntr");</script>

       		<sakai:script contextBase="/messageforums-tool" path="/js/forum.js"/>
			<%--			--%>
       		<sakai:script contextBase="/messageforums-tool" path="/js/sak-10625.js"/>
       		<sakai:script contextBase="/messageforums-tool" path="/js/messages.js"/>

		<script type="text/javascript">
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
			});
		</script>
<!--jsp/discussionForum/forum/dfForumSettings.jsp-->
		<%--<sakai:tool_bar_message value="#{msgs.cdfm_delete_forum_title}" />--%>
		<%--//designNote: this just feels weird - presenting somehting that sort of looks like the form used to create the forum (with an editable permissions block!) to comfirm deletion --%>
		<h:outputText id="alert-delete" styleClass="messageAlert" value="#{msgs.cdfm_delete_forum}" rendered="#{ForumTool.selectedForum.markForDeletion}" style="display:block" />	
        <h:outputText styleClass="messageAlert" value="#{msgs.cdfm_duplicate_forum_confirm}" rendered="#{ForumTool.selectedForum.markForDuplication}" style="display:block" />
		<table class="forumHeader">
			  <tr>
					<td><h:messages styleClass="messageAlert" id="errorMessages" rendered="#{! empty facesContext.maximumSeverity}" />
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
			<p>
				<a id="show" class="show"  href="#">
					<h:graphicImage url="/images/collapse.gif" /><h:outputText   value="#{msgs.cdfm_full_description}"/>
				</a>
			</p>
			<p>
				<a id="hide" class="hide"  href="#">
					<h:graphicImage url="/images/expand.gif" alt="" /><h:outputText   value="#{msgs.cdfm_full_description}"/>
				</a>
			</p>
			<div class="textPanel toggle" id="toggle">
				<mf:htmlShowArea  id="forum_fullDescription" hideBorder="true" value="#{ForumTool.selectedForum.forum.extendedDescription}"/>
				<h:dataTable value="#{ForumTool.selectedForum.attachList}" var="eachAttach" rendered="#{!empty ForumTool.selectedForum.attachList}" styleClass="table table-hover table-striped table-bordered" columnClasses="bogus">
			    <h:column>
			    	<sakai:contentTypeMap fileType="#{eachAttach.attachment.attachmentType}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>									
					<h:graphicImage id="exampleFileIcon" value="#{imagePath}" alt="" />						
<%--  				  <h:outputLink value="#{eachAttach.attachmentUrl}" target="_new_window">
	  				  <h:outputText value="#{eachAttach.attachmentName}"  style="text-decoration:underline;"/>
		  		  </h:outputLink>--%>
  				  <h:outputLink value="#{eachAttach.url}" target="_new_window">
	  				  <h:outputText value="#{eachAttach.attachment.attachmentName}"  style="text-decoration:underline;"/>
		  		  </h:outputLink>
			    </h:column>
			  </h:dataTable>
			</div>
		</div>	
		</td>
		</tr>
		</table>
		<%--
			<h:panelGrid columns="2">
				<h:panelGroup><h:outputLabel for="lock_forum"  value="#{msgs.cdfm_lock_forum}" styleClass="shorttext"/>	</h:panelGroup>
				<h:panelGroup>
					<h:selectOneRadio layout="pageDirection" disabled="true" id="lock_forum"  value="#{ForumTool.selectedForum.locked}">
    					<f:selectItem itemValue="true" itemLabel="#{msgs.cdfm_yes}"/>
    					<f:selectItem itemValue="false" itemLabel="#{msgs.cdfm_no}"/>
  					</h:selectOneRadio>
				</h:panelGroup>
				<h:panelGroup><h:outputLabel for="moderate_forum"  value="#{msgs.cdfm_moderate_forum}" styleClass="shorttext"/>	</h:panelGroup>
				<h:panelGroup>
					<h:selectOneRadio layout="pageDirection" disabled="true" id="moderate_forum"  value="#{ForumTool.selectedForum.moderated}" styleClass="checkbox inlineForm">
    					<f:selectItem itemValue="true" itemLabel="#{msgs.cdfm_yes}"/>
    					<f:selectItem itemValue="false" itemLabel="#{msgs.cdfm_no}"/>
  					</h:selectOneRadio>
				</h:panelGroup>
				<h:panelGroup><h:outputLabel for="postFirst_forum"  value="#{msgs.cdfm_postFirst}" styleClass="shorttext"/>	</h:panelGroup>
				<h:panelGroup>
					<h:selectOneRadio layout="pageDirection" disabled="true" id="postFirst_forum"  value="#{ForumTool.selectedForum.postFirst}" styleClass="checkbox inlineForm">
    					<f:selectItem itemValue="true" itemLabel="#{msgs.cdfm_yes}"/>
    					<f:selectItem itemValue="false" itemLabel="#{msgs.cdfm_no}"/>
  					</h:selectOneRadio>
				</h:panelGroup>
			</h:panelGrid>
		--%>
     <%--
	 <mf:forumHideDivision title="#{msgs.cdfm_access}" id="access_perm" hideByDefault="true">
      <p class="shorttext">
		<h:panelGrid columns="2" width="50%">
				<h:panelGroup><h:outputLabel id="outputLabelCont" for="contributors"  value="#{msgs.cdfm_contributors}"/>	</h:panelGroup>
				<h:panelGroup>
					<h:selectManyListbox  id="contributors" disabled ="true" value="#{ForumTool.selectedForum.contributorsList}" size="5" style="width:200px;">
    					<f:selectItems value="#{ForumTool.totalComposeToList}" />
  					</h:selectManyListbox> 
				</h:panelGroup>

			  <h:panelGroup><h:outputLabel id="outputLabelRead" for="readOnly"  value="#{msgs.cdfm_read_only_access}"/>	</h:panelGroup>
				<h:panelGroup>
					<h:selectManyListbox  disabled ="true" id="readOnly"  value="#{ForumTool.selectedForum.accessorList}" size="5" style="width:200px;">
    					<f:selectItems value="#{ForumTool.totalComposeToList}"  />
  					</h:selectManyListbox>
				</h:panelGroup>
		</h:panelGrid>
	  </p>
	   </mf:forumHideDivision>
	   	  	   	   	   
      
      <mf:forumHideDivision title="#{msgs.cdfm_control_permissions}" id="cntrl_perm" hideByDefault="true">
          <h:dataTable styleClass="table table-hover table-striped table-bordered" id="control_permissions" value="#{ForumTool.forumControlPermissions}" var="cntrl_settings">
   			<h:column>
				<f:facet name="header"><h:outputText value="#{msgs.perm_role}" /></f:facet>
				<h:outputText value="#{cntrl_settings.role}"/>
			</h:column>
 			<h:column>
				<f:facet name="header"><h:outputText value="#{msgs.perm_new_topic}" /></f:facet>
				<h:selectBooleanCheckbox disabled="true" value="#{cntrl_settings.newTopic}"/>
			</h:column>
			<h:column>
				<f:facet name="header"><h:outputText value="#{msgs.perm_new_response}" /></f:facet>
				<h:selectBooleanCheckbox disabled="true" value="#{cntrl_settings.newResponse}"/>
			</h:column>
			<h:column>
				<f:facet name="header"><h:outputText value="#{msgs.perm_response_to_response}" /></f:facet>
				<h:selectBooleanCheckbox disabled="true" value="#{cntrl_settings.responseToResponse}"/>
			</h:column>
			<h:column>
				<f:facet name="header">	<h:outputText value="#{msgs.perm_move_postings}" /></f:facet>
				<h:selectBooleanCheckbox disabled="true" value="#{cntrl_settings.movePostings}"/>
			</h:column>
			<h:column>
				<f:facet name="header"><h:outputText value="#{msgs.perm_change_settings}" /></f:facet>
				<h:selectBooleanCheckbox disabled="true" value="#{cntrl_settings.changeSettings}"/>
			</h:column>
			<h:column>
				<f:facet name="header"><h:outputText value="#{msgs.perm_post_to_gradebook}" /></f:facet>
				<h:selectBooleanCheckbox disabled="true" value="#{cntrl_settings.postToGradebook}"/>
			</h:column>
		</h:dataTable>
      </mf:forumHideDivision>
      <mf:forumHideDivision title="#{msgs.cdfm_message_permissions}" id="msg_perm" hideByDefault="true">
     	 <h:dataTable styleClass="table table-hover table-striped table-bordered" id="message_permissions" value="#{ForumTool.forumMessagePermissions}" var="msg_settings">
   			<h:column>
				<f:facet name="header"><h:outputText value="#{msgs.perm_role}" /></f:facet>
				<h:outputText value="#{msg_settings.role}"/>
			</h:column>
			 <h:column>
				<f:facet name="header"><h:outputText value="#{msgs.perm_read}" /></f:facet>
				<h:selectBooleanCheckbox disabled="true" value="#{msg_settings.read}"/>
			</h:column>
			<h:column>
				<f:facet name="header"><h:outputText value="#{msgs.perm_revise_any}" /></f:facet>
				<h:selectBooleanCheckbox disabled="true" value="#{msg_settings.reviseAny}"/>
			</h:column>
			<h:column>
				<f:facet name="header">	<h:outputText value="#{msgs.perm_revise_own}" /></f:facet>
				<h:selectBooleanCheckbox disabled="true" value="#{msg_settings.reviseOwn}"/>
			</h:column>
			<h:column>
				<f:facet name="header"><h:outputText value="#{msgs.perm_delete_any}" /></f:facet>
				<h:selectBooleanCheckbox disabled="true" value="#{msg_settings.deleteAny}"/>
			</h:column>
			<h:column>
				<f:facet name="header">	<h:outputText value="#{msgs.perm_delete_own}" /></f:facet>
				<h:selectBooleanCheckbox disabled="true" value="#{msg_settings.deleteOwn}"/>
			</h:column>
			<h:column>
				<f:facet name="header"><h:outputText value="#{msgs.perm_mark_as_read}" /></f:facet>
				<h:selectBooleanCheckbox disabled="true" value="#{msg_settings.markAsRead}"/>
			</h:column>			 		
		</h:dataTable>		 	
      </mf:forumHideDivision>

		<div id="permissionReadOnlyW">
		<div id="permissionReadOnly">	
      <%@ include file="/jsp/discussionForum/permissions/permissions_include.jsp"%>
		</div>	
		</div>
      --%>      
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
         
         <h:outputText styleClass="messageProgress" style="display:none" value="#{msgs.cdfm_processing_submit_message}" />
       </div>
	 </h:form>
    </sakai:view>
</f:view>
