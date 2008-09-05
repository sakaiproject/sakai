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
  <sakai:view title="#{msgs.cdfm_discussion_forum_settings}">
  <!-- Y:\msgcntr\messageforums-app\src\webapp\jsp\dfReviseForumSettingsAttach.jsp -->


    <h:form id="revise">
           		<script type="text/javascript" src="/library/js/jquery.js"></script>
       		<sakai:script contextBase="/sakai-messageforums-tool" path="/js/sak-10625.js"/>
      <sakai:tool_bar_message value="#{msgs.cdfm_discussion_forum_settings}" />
		<div class="instruction">
		  <h:outputText id="instruction"  value="#{msgs.cdfm_settings_instruction}"/>
		  <h:outputText value="#{msgs.cdfm_info_required_sign}" styleClass="reqStarInline" />
		</div>
		<h:messages styleClass="alertMessage" id="errorMessages"  /> 
     
				<h:panelGrid columns="3" styleClass="jsfFormTable" columnClasses="shorttext,">
					<h:outputText id="req_star"  value="#{msgs.cdfm_info_required_sign}" styleClass="reqStar"/>	
					<h:outputLabel id="outputLabel" for="forum_title"  value="#{msgs.cdfm_forum_title}"/>	
					<h:inputText size="50" id="forum_title"  value="#{ForumTool.selectedForum.forum.title}"/>
					<h:outputText value="" />
					<h:outputLabel id="outputLabel1" for="forum_shortDescription"  value="#{msgs.cdfm_shortDescription}"/>	
					<h:inputTextarea rows="3" cols="45" id="forum_shortDescription"  value="#{ForumTool.selectedForum.forum.shortDescription}"/>
					<h:outputText value="" />
      	</h:panelGrid>
      		
			<h:panelGroup rendered="#{! ForumTool.disableLongDesc}">
			<h4><h:outputText id="outputLabel2" value="#{msgs.cdfm_fullDescription}"/></h4>	
			<sakai:rich_text_area rows="10" columns="70" value="#{ForumTool.selectedForum.forum.extendedDescription}"/>
	      	</h:panelGroup>
	      	
	      <h4>
		        <h:outputText value="#{msgs.cdfm_att}"/>
	      </h4>
			<div class="instruction">	        
		      <h:outputText value="#{msgs.cdfm_no_attachments}" rendered="#{empty ForumTool.attachments}"/>
	      </div>

	        <sakai:button_bar>
	        	  <sakai:button_bar_item action="#{ForumTool.processAddAttachmentRedirect}" 
	        	                         value="#{msgs.cdfm_button_bar_add_attachment_redirect}" 
	        	                         immediate="true" accesskey="a" />
	        </sakai:button_bar>
	        <%-- gsilver:moving the rendered attribute form the h:column to the dataTable - we do not want empty tables--%>
				<h:dataTable styleClass="listHier lines nolines" id="attmsg" width="100%" value="#{ForumTool.attachments}" var="eachAttach"  cellpadding="0" cellspacing="0" columnClasses="attach,bogus,itemAction specialLink,bogus,bogus" rendered="#{!empty ForumTool.attachments}">
				  <h:column rendered="#{!empty ForumTool.attachments}">
						<f:facet name="header">
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

			<div class="discTria" style="padding: 0.5em; margin-top:0.8em;"><h4><h:outputText  value="#{msgs.cdfm_forum_posting}"/></h4></div>
   	  <h:panelGrid columns="2" >
   			<h:panelGroup>
				  <h:outputLabel for="forum_locked"  value="#{msgs.cdfm_lock_forum}" styleClass="shorttext"/>	
				</h:panelGroup>
				<h:panelGroup>
					<h:selectOneRadio layout="pageDirection"  id="forum_locked"  value="#{ForumTool.selectedForum.locked}" styleClass="checkbox inlineForm">
    					<f:selectItem itemValue="true" itemLabel="#{msgs.cdfm_yes}"/>
    					<f:selectItem itemValue="false" itemLabel="#{msgs.cdfm_no}"/>
  					</h:selectOneRadio>
				</h:panelGroup>
   			<h:panelGroup styleClass="shorttext">
				  <h:outputLabel for="moderated"  value="#{msgs.cdfm_moderate_forum}" styleClass="shorttext"/>	
				</h:panelGroup>
				<h:panelGroup>
					<h:selectOneRadio layout="pageDirection"  id="moderated"  value="#{ForumTool.selectedForum.moderated}" styleClass="checkbox inlineForm"
							onclick="javascript:disableOrEnableModeratePerm();">
    					<f:selectItem itemValue="true" itemLabel="#{msgs.cdfm_yes}"/>
    					<f:selectItem itemValue="false" itemLabel="#{msgs.cdfm_no}"/>
  					</h:selectOneRadio>
				</h:panelGroup>

			</h:panelGrid>

	   <%@include file="/jsp/discussionForum/permissions/permissions_include.jsp"%>
	      
      <div class="act">
          <h:commandButton action="#{ForumTool.processActionSaveForumSettings}" value="#{msgs.cdfm_button_bar_save_setting}"
          								 rendered="#{ForumTool.selectedForum.forum.id != null && !ForumTool.selectedForum.markForDeletion}" accesskey="s"> 
    	 	  	<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>         
          </h:commandButton>
          <h:commandButton action="#{ForumTool.processActionSaveForumAsDraft}" value="#{msgs.cdfm_button_bar_save_draft}" accesskey="v"
          								 rendered = "#{!ForumTool.selectedForum.markForDeletion}">
	        	<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
          </h:commandButton>  
          <h:commandButton action="#{ForumTool.processActionSaveForumAndAddTopic}" value="#{msgs.cdfm_button_bar_save_setting_add_topic}" accesskey="t"
          								 rendered = "#{!ForumTool.selectedForum.markForDeletion}">
	        	<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
          </h:commandButton>
          <h:commandButton id="delete_confirm" action="#{ForumTool.processActionDeleteForumConfirm}" 
                           value="#{msgs.cdfm_button_bar_delete}" rendered="#{!ForumTool.selectedForum.markForDeletion && ForumTool.displayForumDeleteOption}"
                           accesskey="d">
	        	<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
          </h:commandButton>
          
          <h:commandButton id="delete" action="#{ForumTool.processActionDeleteForum}" 
                           value="#{msgs.cdfm_button_bar_delete}" rendered="#{ForumTool.selectedForum.markForDeletion}"
                           accesskey="d">
	        	<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
          </h:commandButton>
          <h:commandButton  action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_button_bar_cancel}" accesskey="x" />
       </div>
       
	 </h:form>
    </sakai:view>
</f:view>
