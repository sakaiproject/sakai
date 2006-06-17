<%@ page import="java.util.*, javax.faces.context.*, javax.faces.application.*,
                 javax.faces.el.*, org.sakaiproject.tool.messageforums.*"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>

<f:view>
   <sakai:view>
      <link href='/sakai-messageforums-tool/css/msgForums.css' rel='stylesheet' type='text/css' />
      <h:form id="revise">
        <sakai:tool_bar_message value="#{msgs.cdfm_discussion_forum_settings}" />
         <h:outputText styleClass="alertMessage" value="#{msgs.cdfm_delete_forum}" rendered="#{ForumTool.selectedForum.markForDeletion}"/>	
 		<p class="shorttext">
			<h:panelGrid columns="2">
				<h:panelGroup><h:outputLabel id="outputLabel" for="forum_title"  value="#{msgs.cdfm_forum_title}"/>	</h:panelGroup>
				<h:panelGroup><h:outputText  id="forum_title"  value="#{ForumTool.selectedForum.forum.title}"/></h:panelGroup>

				<h:panelGroup><h:outputLabel id="outputLabel1" for="forum_shortDescription"  value="#{msgs.cdfm_shortDescription}"/>	</h:panelGroup>
				<h:panelGroup><h:outputText id="forum_shortDescription"  value="#{ForumTool.selectedForum.forum.shortDescription}"/></h:panelGroup>

				<h:panelGroup><h:outputLabel id="outputLabel2" for="forum_fullDescription"  value="#{msgs.cdfm_fullDescription}"/>	</h:panelGroup>
				<h:panelGroup>				 
				<mf:htmlShowArea  id="forum_fullDescription" hideBorder="true" value="#{ForumTool.selectedForum.forum.extendedDescription}"/></h:panelGroup>
   			</h:panelGrid>
	
			<h:panelGroup><h:outputLabel id="outputLabel3" value="#{msgs.cdfm_attachments}"/>	</h:panelGroup>
			<h:panelGroup>
				<sakai:group_box>
				  <sakai:doc_section>
			  		<h:outputText value="No Attachments Yet" rendered="#{empty ForumTool.selectedForum.forum.attachments}"/>
		  	  </sakai:doc_section>
					<h:dataTable value="#{ForumTool.selectedForum.forum.attachments}" var="eachAttach" rendered="#{!empty ForumTool.selectedForum.forum.attachments}">
					  <h:column>
							<f:facet name="header">
								<h:outputText value="" />
							</f:facet>
							<h:graphicImage url="/images/excel.gif" rendered="#{eachAttach.attachmentType == 'application/vnd.ms-excel'}"/>
							<h:graphicImage url="/images/html.gif" rendered="#{eachAttach.attachmentType == 'text/html'}"/>
							<h:graphicImage url="/images/pdf.gif" rendered="#{eachAttach.attachmentType == 'application/pdf'}"/>
							<h:graphicImage url="/images/ppt.gif" rendered="#{eachAttach.attachmentType == 'application/vnd.ms-powerpoint'}"/>
							<h:graphicImage url="/images/text.gif" rendered="#{eachAttach.attachmentType == 'text/plain'}"/>
							<h:graphicImage url="/images/word.gif" rendered="#{eachAttach.attachmentType == 'application/msword'}"/>
							<h:outputLink value="#{eachAttach.attachmentUrl}" target="_new_window">
								<h:outputText value="#{eachAttach.attachmentName}"  style="text-decoration:underline;"/>
							</h:outputLink>
					  </h:column>
				  </h:dataTable>
		  	</sakai:group_box>
		  </h:panelGroup>
		</p>
        <h4><h:outputText  value="#{msgs.cdfm_forum_posting}"/></h4>
        <p class="shorttext">
			<h:panelGrid columns="2">
				<h:panelGroup><h:outputLabel id="outputLabel4" for="forum_posting"  value="#{msgs.cdfm_lock_forum}"/>	</h:panelGroup>
				<h:panelGroup>
					<h:selectOneRadio layout="pageDirection" disabled="true" id="forum_posting"  value="#{ForumTool.selectedForum.locked}">
    					<f:selectItem itemValue="true" itemLabel="Yes"/>
    					<f:selectItem itemValue="false" itemLabel="No"/>
  					</h:selectOneRadio>
				</h:panelGroup>
			</h:panelGrid>
		</p>
		
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
          <h:dataTable styleClass="listHier" id="control_permissions" value="#{ForumTool.forumControlPermissions}" var="cntrl_settings">
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
     	 <h:dataTable styleClass="listHier" id="message_permissions" value="#{ForumTool.forumMessagePermissions}" var="msg_settings">
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
      --%>
      <%@include file="/jsp/discussionForum/permissions/permissions_include.jsp"%>
      
       <p class="act">
          <h:commandButton id ="revise" rendered="#{!ForumTool.selectedForum.markForDeletion}" immediate="true"  action="#{ForumTool.processActionReviseForumSettings}" value="#{msgs.cdfm_button_bar_revise}"> 
    	 	  	<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>    	 	  	
          </h:commandButton>
          
          <h:commandButton id="delete_confirm" action="#{ForumTool.processActionDeleteForumConfirm}" value="#{msgs.cdfm_button_bar_delete}" rendered="#{!ForumTool.selectedForum.markForDeletion}">
	        	<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
          </h:commandButton>
          
          <h:commandButton id="delete" action="#{ForumTool.processActionDeleteForum}" value="#{msgs.cdfm_button_bar_delete}" rendered="#{ForumTool.selectedForum.markForDeletion}">
	        	<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
          </h:commandButton>
          
          <h:commandButton id="cancel" immediate="true" action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_button_bar_cancel}" />
       </p>
	 </h:form>
    </sakai:view>
</f:view>
