<%@ page import="java.util.*, javax.faces.context.*, javax.faces.application.*,
                 javax.faces.el.*, org.sakaiproject.tool.messageforums.*"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>
<f:view>
	<script type="text/javascript" src="/library/js/jquery.js"></script>
	<sakai:script contextBase="/sakai-messageforums-tool" path="/js/sak-10625.js"/>      
	<script type="text/javascript" src="/sakai-messageforums-tool/js/jquery.charcounter.js"> </script>
	<sakai:script contextBase="/sakai-messageforums-tool" path="/js/permissions_header.js"/>
	<sakai:script contextBase="/sakai-messageforums-tool" path="/js/forum.js"/>
	<sakai:view title="#{msgs.cdfm_discussion_topic_settings}" toolCssHref="/sakai-messageforums-tool/css/msgcntr.css">
<!--jsp/dfReviseTopicSettingsAttach.jsp-->
    <h:form id="revise">
      <sakai:tool_bar_message value="#{msgs.cdfm_discussion_topic_settings}" />
 			<div class="instruction">
  			<h:outputText id="instruction"  value="#{msgs.cdfm_settings_instruction}"/>
			 	<h:outputText value="#{msgs.cdfm_info_required_sign}" styleClass="reqStarInline" />
			</div>
			<h:messages errorClass="messageAlert" infoClass="success" id="errorMessages" /> 

			<h:panelGrid styleClass="jsfFormTable" columns="1"  columnClasses="shorttext">
			<h:panelGroup>
		
				<h:outputLabel id="outputLabel" for="topic_title"   style="padding-bottom:.3em;display:block;clear:both;float:none">
					<h:outputText id="req_star"  value="#{msgs.cdfm_info_required_sign}" styleClass="reqStarInline" style="padding-right:3px"/>
					<h:outputText value="#{msgs.cdfm_topic_title}" />
				</h:outputLabel>	 
				<h:inputText size="50" id="topic_title"  value="#{ForumTool.selectedTopic.topic.title}"/>
			</h:panelGroup>	
			</h:panelGrid>
			<h:panelGrid columns="1"  columnClasses="longtext">
				<h:panelGroup>
					<h:outputLabel id="outputLabel1" for="topic_shortDescription"  value="#{msgs.cdfm_shortDescription}" />

							<h:outputText value="#{msgs.cdfm_shortDescriptionCharsRem}"  styleClass="charRemFormat" style="display:none"/>
							<%--							
							<h:outputText value="%1 chars remain"  styleClass="charRemFormat" style="display:none"/>
							--%>
							<h:outputText value="" styleClass="charsRemaining" style="padding-left:3em;font-size:.85em;"/>
							<h:outputText value=""  style="display:block"/>
					

					<h:inputTextarea rows="3" cols="45" id="topic_shortDescription"  value="#{ForumTool.selectedTopic.topic.shortDescription}" styleClass="forum_shortDescriptionClass" style="float:none"/>
				</h:panelGroup>	
     	</h:panelGrid>

			<%--RTEditor area - if enabled--%>
		<h:panelGroup rendered="#{! ForumTool.disableLongDesc}">
				<h:outputText id="outputLabel2"   value="#{msgs.cdfm_fullDescription}" style="display:block;padding:.5em 0"/>
			<sakai:rich_text_area rows="10" columns="70"  value="#{ForumTool.selectedTopic.topic.extendedDescription}"/>
		</h:panelGroup>
		
			<%--Attachment area  --%>
		<h4><h:outputText value="#{msgs.cdfm_att}"/></h4>
		
			<div style="padding-left:1em">
			<%--designNote: would be nice to make this an include, as well as a more comprehensive MIME type check  --%>
			<h:dataTable styleClass="attachPanel" id="attmsg" value="#{ForumTool.attachments}" var="eachAttach"  cellpadding="0" cellspacing="0" columnClasses="attach,bogus,specialLink,bogus,bogus" rendered="#{!empty ForumTool.attachments}">
				<h:column>
					<f:facet name="header">   <h:outputText escape="false"  value="&nbsp;"/>                                          
					</f:facet>
						<sakai:contentTypeMap fileType="#{eachAttach.attachment.attachmentType}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>									
						<h:graphicImage id="exampleFileIcon" value="#{imagePath}" />							
				</h:column>
				<h:column>
						<f:facet name="header">
							<h:outputText value="#{msgs.cdfm_title}"/>
						</f:facet>
							<h:outputText value="#{eachAttach.attachment.attachmentName}"/>
				</h:column>
				<h:column>
				<f:facet name="header"><h:outputText escape="false" value="&nbsp;"/>
				</f:facet>
							<h:commandLink action="#{ForumTool.processDeleteAttachSetting}" 
								immediate="true"
								onfocus="document.forms[0].onsubmit();"
								title="#{msgs.cdfm_remove}">
							<h:outputText value="#{msgs.cdfm_remove}"/>
								<f:param value="#{eachAttach.attachment.attachmentId}" name="dfmsg_current_attach"/>
							</h:commandLink>
				  </h:column>
					<h:column rendered="#{!empty ForumTool.attachments}">
						<f:facet name="header">
							<h:outputText value="#{msgs.cdfm_attsize}" />
						</f:facet>
						<h:outputText value="#{eachAttach.attachment.attachmentSize}"/>
					</h:column>
					<h:column rendered="#{!empty ForumTool.attachments}">
						<f:facet name="header">
		  			  <h:outputText value="#{msgs.cdfm_atttype}" />
						</f:facet>
						<h:outputText value="#{eachAttach.attachment.attachmentType}"/>
					</h:column>
					</h:dataTable>   
			
			<div class="instruction">	     
				<h:outputText value="#{msgs.cdfm_no_attachments}" rendered="#{empty ForumTool.attachments}"/>
			</div>

			<p class="act" style="padding:0 0 1em 0;">
				<sakai:button_bar_item action="#{ForumTool.processAddAttachmentRedirect}" 
					value="#{msgs.cdfm_button_bar_add_attachment_more_redirect}" 
					immediate="true"
					accesskey="a" 
					 rendered="#{!empty ForumTool.attachments}"
					 style="font-size:95%"/>
				<sakai:button_bar_item action="#{ForumTool.processAddAttachmentRedirect}" 
					value="#{msgs.cdfm_button_bar_add_attachment_redirect}" 
					immediate="true"
					accesskey="a" 
					 rendered="#{empty ForumTool.attachments}" 
					 style="font-size:95%"/>
			</p>
			</div>                                                                                  
			<%--general posting  forum settings --%>
			<h4><h:outputText  value="#{msgs.cdfm_topic_posting}"/></h4>
			<div style="padding-left:1em">
			<h:panelGrid columns="2"  styleClass="jsfFormTable">
				<h:panelGroup>
				  <h:outputLabel for="topic_locked"  value="#{msgs.cdfm_lock_topic}" styleClass="shorttext"/>	
				</h:panelGroup>
				<h:panelGroup>
					<h:selectOneRadio layout="lineDirection"  id="topic_locked"  value="#{ForumTool.selectedTopic.locked}" styleClass="checkbox inlineForm">
    					<f:selectItem itemValue="true" itemLabel="#{msgs.cdfm_yes}"/>
    					<f:selectItem itemValue="false" itemLabel="#{msgs.cdfm_no}"/>
  					</h:selectOneRadio>
				</h:panelGroup>
				<h:panelGroup>
				  <h:outputLabel for="moderated"  value="#{msgs.cdfm_moderate_topic}" styleClass="shorttext"/>	
				</h:panelGroup>
				<h:panelGroup>
					<h:selectOneRadio layout="lineDirection"  id="moderated"  value="#{ForumTool.selectedTopic.moderated}" styleClass="checkbox inlineForm"
								onclick="javascript:disableOrEnableModeratePerm();" >
    					<f:selectItem itemValue="true" itemLabel="#{msgs.cdfm_yes}"/>
    					<f:selectItem itemValue="false" itemLabel="#{msgs.cdfm_no}"/>
  					</h:selectOneRadio>
				</h:panelGroup>
			</h:panelGrid>
			</div>
		<%--
		   <h4><h:outputText  value="Confidential Responses"/></h4>
		   <h:selectBooleanCheckbox   title= "#{msgs.cdfm_topic_allow_anonymous_postings}"  value="false" />
		   <h:outputText   value="  #{msgs.cdfm_topic_allow_anonymous_postings}" /> 
		   <br/>
		   <h:selectBooleanCheckbox   title= "#{msgs.cdfm_topic_author_identity}"  value="false" />
		   <h:outputText   value="  #{msgs.cdfm_topic_author_identity}" />
     
       <h4><h:outputText  value="#{mags.cdfm_topic_post_before_reading}"/></h4>
    	   <p class="shorttext">
			<h:panelGrid columns="2">
				<h:panelGroup><h:outputLabel id="outputLabel4" for="topic_reading"  value="#{msgs.cdfm_topic_post_before_reading_desc}"/>	</h:panelGroup>
				<h:panelGroup>
					<h:selectOneRadio layout="pageDirection"  id="topic_reading" value="#{ForumTool.selectedTopic.mustRespondBeforeReading}">
    					<f:selectItem itemValue="true" itemLabel="#{msgs.cdfm_yes}"/>
    					<f:selectItem itemValue="false" itemLabel="#{msgs.cdfm_no}"/>
  					</h:selectOneRadio>
				</h:panelGroup>
			</h:panelGrid>
		</p>
		  --%>
      		  
      <%@include file="/jsp/discussionForum/permissions/permissions_include.jsp"%>
	  <%--
      <mf:forumHideDivision title="#{msgs.cdfm_access}" id="access_perm" hideByDefault="true">
	  	<p class="shorttext">
			<h:panelGrid columns="2" width="50%">
				<h:panelGroup><h:outputLabel id="outputLabelCont" for="contributors"  value="#{msgs.cdfm_contributors}"/>	</h:panelGroup>
				<h:panelGroup>
					<h:selectManyListbox id="contributors"  value="#{ForumTool.selectedTopic.contributorsList}" size="5" style="width:200px;">
    					<f:selectItems value="#{ForumTool.totalComposeToList}" />
  					</h:selectManyListbox>
				</h:panelGroup>

			  <h:panelGroup><h:outputLabel id="outputLabelRead" for="readOnly"  value="#{msgs.cdfm_read_only_access}"/>	</h:panelGroup>
				<h:panelGroup>
					<h:selectManyListbox  id="readOnly"  value="#{ForumTool.selectedTopic.accessorList}" size="5" style="width:200px;">
    					<f:selectItems value="#{ForumTool.totalComposeToList}"  />
  					</h:selectManyListbox>
				</h:panelGroup>
			</h:panelGrid>
		</p>
	  </mf:forumHideDivision>
      <mf:forumHideDivision title="#{msgs.cdfm_control_permissions}" id="cntrl_perm" hideByDefault="true">
          <h:dataTable styleClass="listHier" id="control_permissions" value="#{ForumTool.topicControlPermissions}" var="cntrl_settings">
   			<h:column>
				<f:facet name="header"><h:outputText value="#{msgs.perm_role}" /></f:facet>
				<h:outputText value="#{cntrl_settings.role}"/>
			</h:column>			 
			<h:column>
				<f:facet name="header"><h:outputText value="#{msgs.perm_new_response}" /></f:facet>
				<h:selectBooleanCheckbox disabled="true" value="#{cntrl_settings.newResponse}"/>
			</h:column>
			<h:column>
				<f:facet name="header"><h:outputText value="#{msgs.perm_response_to_response}" /></f:facet>
				<h:selectBooleanCheckbox disabled="true"  value="#{cntrl_settings.responseToResponse}"/>
			</h:column>
			<h:column>
				<f:facet name="header">	<h:outputText value="#{msgs.perm_move_postings}" /></f:facet>
				<h:selectBooleanCheckbox value="#{cntrl_settings.movePostings}"/>
			</h:column>
			<h:column>
				<f:facet name="header"><h:outputText value="#{msgs.perm_change_settings}" /></f:facet>
				<h:selectBooleanCheckbox disabled="true" value="#{cntrl_settings.changeSettings}"/>
			</h:column>
			<h:column>
				<f:facet name="header"><h:outputText value="#{msgs.perm_post_to_gradebook}" /></f:facet>
				<h:selectBooleanCheckbox value="#{cntrl_settings.postToGradebook}"/>
			</h:column>
		</h:dataTable>
      </mf:forumHideDivision>
      <mf:forumHideDivision title="#{msgs.cdfm_message_permissions}" id="msg_perm" hideByDefault="true">
            <h:dataTable styleClass="listHier" id="message_permissions" value="#{ForumTool.topicMessagePermissions}" var="msg_settings">
   			<h:column>
				<f:facet name="header"><h:outputText value="#{msgs.perm_role}" /></f:facet>
				<h:outputText value="#{msg_settings.role}"/>
			</h:column>
			 <h:column>
				<f:facet name="header"><h:outputText value="#{msgs.perm_read}" /></f:facet>
				<h:selectBooleanCheckbox  disabled="true" value="#{msg_settings.read}"/>
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
				<h:selectBooleanCheckbox  disabled="true" value="#{msg_settings.deleteOwn}"/>
			</h:column>
			<h:column>
				<f:facet name="header"><h:outputText value="#{msgs.perm_mark_as_read}" /></f:facet>
				<h:selectBooleanCheckbox disabled="true" value="#{msg_settings.markAsRead}"/>
			</h:column>			 		
		</h:dataTable>
      </mf:forumHideDivision>
      --%>
      
      <div class="act">
          <h:commandButton action="#{ForumTool.processActionSaveTopicSettings}" value="#{msgs.cdfm_button_bar_save_setting}" accesskey="s"
          								 rendered="#{!ForumTool.selectedTopic.markForDeletion}"> 
    	 	  	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>    
    	 	  	<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>         
          </h:commandButton>
          <h:commandButton action="#{ForumTool.processActionSaveTopicAsDraft}" value="#{msgs.cdfm_button_bar_save_draft}" accesskey="v"
          								 rendered="#{!ForumTool.selectedTopic.markForDeletion}">
	        	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
	        	<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/> 
          </h:commandButton>
          <h:commandButton action="#{ForumTool.processActionSaveTopicAndAddTopic}" value="#{msgs.cdfm_button_bar_save_setting_add_topic}" accesskey="t"
          								 rendered="#{!ForumTool.selectedTopic.markForDeletion}">
	        	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
	        	<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/> 
          </h:commandButton>
          <h:commandButton action="#{ForumTool.processActionDeleteTopicConfirm}" id="delete_confirm" 
                           value="#{msgs.cdfm_button_bar_delete}" accesskey="d" rendered="#{!ForumTool.selectedTopic.markForDeletion && ForumTool.displayTopicDeleteOption}">
	        	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
          </h:commandButton>
          <h:commandButton action="#{ForumTool.processActionDeleteTopic}" id="delete" accesskey="d"
                           value="#{msgs.cdfm_button_bar_delete}" rendered="#{ForumTool.selectedTopic.markForDeletion}">
	        	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
          </h:commandButton>
          <h:commandButton  action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_button_bar_cancel}" accesskey="c" />
       </div>
       
	 </h:form>
			  <script type="text/javascript">
            $(document).ready(function(){
				var charRemFormat = $('.charRemFormat').text();
				$(".forum_shortDescriptionClass").charCounter(255, {
					container: ".charsRemaining",
					format: charRemFormat
				 });
			 });				 
        </script>
		
    </sakai:view>
</f:view>
