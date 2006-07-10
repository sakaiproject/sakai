<%@ page import="java.util.*, javax.faces.context.*, javax.faces.application.*,
                 javax.faces.el.*, org.sakaiproject.tool.messageforums.*"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>
<link href='/sakai-messageforums-tool/css/msgForums.css' rel='stylesheet' type='text/css' />

<f:view>
  <sakai:view title="#{msgs.cdfm_discussion_topic_settings}">

    <h:form id="revise">
      <sakai:tool_bar_message value="#{msgs.cdfm_discussion_topic_settings}" />
 			<div class="instruction">
  			  <h:outputText id="instruction"  value="#{msgs.cdfm_settings_instruction}"/>
			 	<h:outputText value="#{msgs.cdfm_info_required_sign}" styleClass="reqStarInline" />
			</div>
			<h:messages styleClass="alertMessage" id="errorMessages" /> 

		  <h:panelGrid styleClass="shorttext" columns="3">
				<h:outputText id="req_star"  value="#{msgs.cdfm_info_required_sign}" styleClass="reqStarInline"/>	
				<h:outputLabel id="outputLabel" for="topic_title"  value="#{msgs.cdfm_topic_title}"/>	
				<h:inputText size="50" id="topic_title"  value="#{ForumTool.selectedTopic.topic.title}"/>

				<h:outputText value="" />
				<h:outputLabel id="outputLabel1" for="topic_shortDescription"  value="#{msgs.cdfm_shortDescription}"/>
				<h:inputTextarea rows="3" cols="45" id="topic_shortDescription"  value="#{ForumTool.selectedTopic.topic.shortDescription}"/>

				<h:outputText value="" />
				<h:outputLabel id="outputLabel2"   value="#{msgs.cdfm_fullDescription}"/>
				<sakai:rich_text_area rows="10" columns="70"  value="#{ForumTool.selectedTopic.topic.extendedDescription}"/>
     	</h:panelGrid>


   		<sakai:panel_titled title="">
	      <div class="msgHeadings">
	        <h:outputText value="#{msgs.cdfm_att}"/>
	      </div>
	      <sakai:doc_section>
	        <sakai:button_bar>
	        	<sakai:button_bar_item action="#{ForumTool.processAddAttachmentRedirect}" 
	        	                       value="#{msgs.cdfm_button_bar_add_attachment_redirect}" 
	        	                       immediate="true"
	        	                       accesskey="a" />
	        </sakai:button_bar>
	      </sakai:doc_section>
   		
	      <sakai:doc_section>	        
		      <h:outputText value="#{msgs.cdfm_no_attachments}" rendered="#{empty ForumTool.attachments}"/>
	      </sakai:doc_section>
	        
				<h:dataTable styleClass="listHier" id="attmsg" width="100%" value="#{ForumTool.attachments}" var="eachAttach" >
				  <h:column rendered="#{!empty ForumTool.attachments}">
						<f:facet name="header">
							<h:outputText value="#{msgs.cdfm_title}"/>
						</f:facet>
						<sakai:doc_section>
							<h:graphicImage url="/images/excel.gif" rendered="#{eachAttach.attachmentType == 'application/vnd.ms-excel'}" alt="" />
							<h:graphicImage url="/images/html.gif" rendered="#{eachAttach.attachmentType == 'text/html'}" alt="" />
							<h:graphicImage url="/images/pdf.gif" rendered="#{eachAttach.attachmentType == 'application/pdf'}" alt="" />
							<h:graphicImage url="/images/ppt.gif" rendered="#{eachAttach.attachmentType == 'application/vnd.ms-powerpoint'}" alt="" />
							<h:graphicImage url="/images/text.gif" rendered="#{eachAttach.attachmentType == 'text/plain'}" alt="" />
							<h:graphicImage url="/images/word.gif" rendered="#{eachAttach.attachmentType == 'application/msword'}" alt="" />
						
							<h:outputText value="#{eachAttach.attachmentName}"/>
						</sakai:doc_section>
						
					  <sakai:doc_section>
							<h:commandLink action="#{ForumTool.processDeleteAttachSetting}" 
								immediate="true"
								onfocus="document.forms[0].onsubmit();"
								title="#{msgs.cdfm_remove}">
							<h:outputText value="#{msgs.cdfm_remove}"/>
								<f:param value="#{eachAttach.attachmentId}" name="dfmsg_current_attach"/>
							</h:commandLink>
						</sakai:doc_section>
						
				  </h:column>
					<h:column rendered="#{!empty ForumTool.attachments}">
						<f:facet name="header">
							<h:outputText value="#{msgs.cdfm_attsize}" />
						</f:facet>
						<h:outputText value="#{eachAttach.attachmentSize}"/>
					</h:column>
					<h:column rendered="#{!empty ForumTool.attachments}">
						<f:facet name="header">
		  			  <h:outputText value="#{msgs.cdfm_atttype}" />
						</f:facet>
						<h:outputText value="#{eachAttach.attachmentType}"/>
					</h:column>
					</h:dataTable>   
			</sakai:panel_titled>   

       <div class="msgHeadings><h:outputText  value="#{msgs.cdfm_topic_posting}"/></div>

			<h:panelGrid columns="2">
				<h:panelGroup styleClass="shorttext">
				  <h:outputLabel id="outputLabel3" for="topic_posting"  value="#{msgs.cdfm_lock_topic}"/>	
				</h:panelGroup>
				<h:panelGroup styleClass="checkbox inlineForm">
					<h:selectOneRadio layout="pageDirection"  id="topic_posting"  value="#{ForumTool.selectedTopic.locked}">
    					<f:selectItem itemValue="true" itemLabel="#{msgs.cdfm_yes}"/>
    					<f:selectItem itemValue="false" itemLabel="#{msgs.cdfm_no}"/>
  					</h:selectOneRadio>
				</h:panelGroup>
			</h:panelGrid>

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
          <h:commandButton action="#{ForumTool.processActionSaveTopicSettings}" value="#{msgs.cdfm_button_bar_save_setting}" accesskey="s"> 
    	 	  	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>    
    	 	  	<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>         
          </h:commandButton>
          <h:commandButton action="#{ForumTool.processActionSaveTopicAsDraft}" value="#{msgs.cdfm_button_bar_save_draft}" accesskey="d">
	        	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
	        	<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/> 
          </h:commandButton>
          <h:commandButton action="#{ForumTool.processActionSaveTopicAndAddTopic}" value="#{msgs.cdfm_button_bar_save_setting_add_topic}" accesskey="t">
	        	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
	        	<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/> 
          </h:commandButton>
          <h:commandButton  action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_button_bar_cancel}" accesskey="c" />
       </div>
       
	 </h:form>
    </sakai:view>
</f:view>
