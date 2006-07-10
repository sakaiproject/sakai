<%@ page import="java.util.*, javax.faces.context.*, javax.faces.application.*,
                 javax.faces.el.*, org.sakaiproject.tool.messageforums.*"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>
<link href='/sakai-messageforums-tool/css/msgForums.css' rel='stylesheet' type='text/css' />

<f:view>
   <sakai:view>

      <h:form id="revise">
        <sakai:tool_bar_message value="#{msgs.cdfm_discussion_topic_settings}" />
        <h:outputText styleClass="alertMessage" value="#{msgs.cdfm_delete_topic}" rendered="#{ForumTool.selectedTopic.markForDeletion}"/>

			<table summary="" width="100%">
				<tr>
				  <td class="msgDetailsCol">
				    <h:outputText value="#{msgs.cdfm_topic_title}" />
				  </td>
				  <td>
    				  <h:outputText id="topic_title"  value="#{ForumTool.selectedTopic.topic.title}"/>
				  </td>
				</tr>
				<tr>
				  <td class="msgDetailsCol">
   				  <h:outputText value="#{msgs.cdfm_shortDescription}" />	
				  </td>
				  <td>	
				    <h:outputText id="topic_shortDescription"  value="#{ForumTool.selectedTopic.topic.shortDescription}"/>
				  </td>
				</tr>
		  </table>
            
        <sakai:panel_titled title="#{msgs.cdfm_fullDescription}">
          <h:panelGroup>
            <mf:htmlShowArea hideBorder="true" id="topic_fullDescription"  value="#{ForumTool.selectedTopic.topic.extendedDescription}"/>
          </h:panelGroup>
		    </sakai:panel_titled>	
		
			  <sakai:panel_titled title="#{msgs.cdfm_attachments}">
			    <sakai:doc_section>
			      <h:outputText value="#{msgs.cdfm_no_attachments}" rendered="#{empty ForumTool.selectedTopic.topic.attachments}"/>
		  	    </sakai:doc_section>
				
				<h:dataTable value="#{ForumTool.selectedTopic.topic.attachments}" var="eachAttach" rendered="#{!empty ForumTool.selectedTopic.topic.attachments}">
				  <h:column>
				    <f:facet name="header">
				      <h:outputText value="" />
				    </f:facet>
					<h:graphicImage url="/images/excel.gif" rendered="#{eachAttach.attachmentType == 'application/vnd.ms-excel'}" alt="" />
					<h:graphicImage url="/images/html.gif" rendered="#{eachAttach.attachmentType == 'text/html'}" alt="" />
					<h:graphicImage url="/images/pdf.gif" rendered="#{eachAttach.attachmentType == 'application/pdf'}" alt="" />
					<h:graphicImage url="/images/ppt.gif" rendered="#{eachAttach.attachmentType == 'application/vnd.ms-powerpoint'}" alt="" />
					<h:graphicImage url="/images/text.gif" rendered="#{eachAttach.attachmentType == 'text/plain'}" alt="" />
					<h:graphicImage url="/images/word.gif" rendered="#{eachAttach.attachmentType == 'application/msword'}" alt="" />
					<h:outputLink value="#{eachAttach.attachmentUrl}" target="_new_window">
					  <h:outputText value="#{eachAttach.attachmentName}"  style="text-decoration:underline;"/>
				    </h:outputLink>
				  </h:column>
				</h:dataTable>
		  	  </sakai:panel_titled>


				<%--<h:panelGroup>
					 <h:dataTable id="topic_attachments"  value="#{ForumTool.selectedTopic.topic.attachments}" var="attachment" >
					  		<h:column rendered="#{!empty ForumTool.selectedTopic.topic.attachments}">
								<h:graphicImage url="/images/excel.gif" rendered="#{attachment.attachmentType == 'application/vnd.ms-excel'}"/>
								<h:graphicImage url="/images/html.gif" rendered="#{attachment.attachmentType == 'text/html'}"/>
								<h:graphicImage url="/images/pdf.gif" rendered="#{attachment.attachmentType == 'application/pdf'}"/>
								<h:graphicImage url="/sakai-messageforums-tool/images/ppt.gif" rendered="#{attachment.attachmentType == 'application/vnd.ms-powerpoint'}"/>
								<h:graphicImage url="/images/text.gif" rendered="#{attachment.attachmentType == 'text/plain'}"/>
								<h:graphicImage url="/images/word.gif" rendered="#{attachment.attachmentType == 'application/msword'}"/>
								<h:outputText value="#{attachment.attachmentName}"/>
						</h:column>
					</h:dataTable> 			 
				</h:panelGroup>--%>
      <div class="msgHeadings"><h:outputText value="#{msgs.cdfm_topic_posting}"/></div>

			<h:panelGrid columns="2">
				<h:panelGroup styleClass="checkbox inlineForm">
				  <h:outputLabel for="" value="#{msgs.cdfm_lock_topic}"/>
				</h:panelGroup>
				<h:panelGroup styleClass="checkbox inlineForm">	
					<h:selectOneRadio  layout="pageDirection" disabled="true" id="topic_posting"  value="#{ForumTool.selectedTopic.locked}">
    			    <f:selectItem itemValue="true" itemLabel="#{msgs.cdfm_yes}"/>
    					<f:selectItem itemValue="false" itemLabel="#{msgs.cdfm_no}"/>
  					</h:selectOneRadio>
				</h:panelGroup>
			</h:panelGrid>

		<%--
		   <h4><h:outputText  value="Anonymous Responses"/></h4>
		   <h:selectBooleanCheckbox   title= "#{msgs.cdfm_topic_allow_anonymous_postings}" disabled="true" value="false" />
		   <h:outputText   value="  #{msgs.cdfm_topic_allow_anonymous_postings}" /> 
		   <br/>
		   <h:selectBooleanCheckbox  disabled="true" title= "#{msgs.cdfm_topic_author_identity}"  value="false" />
		   <h:outputText   value="  #{msgs.cdfm_topic_author_identity}" />
     
       <h4><h:outputText  value="Post Before Reading"/></h4>
    	   <p class="shorttext">
				<h:panelGrid columns="2">
					<h:panelGroup><h:outputLabel id="outputLabel5" for="topic_reading"  value="Users must post a response before reading others"/>	</h:panelGroup>
					<h:panelGroup>
						<h:selectOneRadio disabled ="true" layout="pageDirection"  id="topic_reading" value="#{ForumTool.selectedTopic.mustRespondBeforeReading}">
	    					<f:selectItem itemValue="true" itemLabel="Yes"/>
	    					<f:selectItem itemValue="false" itemLabel="No"/>
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
					<h:selectManyListbox disabled ="true" id="contributors"  value="#{ForumTool.selectedTopic.contributorsList}" size="5" style="width:200px;">
    					<f:selectItems value="#{ForumTool.totalComposeToList}" />
  					</h:selectManyListbox>
				</h:panelGroup>

			  <h:panelGroup><h:outputLabel id="outputLabelRead" for="readOnly"  value="#{msgs.cdfm_read_only_access}"/>	</h:panelGroup>
				<h:panelGroup>
					<h:selectManyListbox  disabled ="true" id="readOnly"  value="#{ForumTool.selectedTopic.accessorList}" size="5" style="width:200px;">
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
      <h:dataTable styleClass="listHier" id="message_permissions" value="#{ForumTool.topicMessagePermissions}" var="msg_settings">
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
      
       <div class="act">
          <h:commandButton action="#{ForumTool.processActionReviseTopicSettings}" id="revise"  
                           value="#{msgs.cdfm_button_bar_revise}" rendered="#{!ForumTool.selectedTopic.markForDeletion}"
                           accesskey="r"> 
    	 	  	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/> 
    	 	  	<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>        
          </h:commandButton>
          <h:commandButton action="#{ForumTool.processActionDeleteTopicConfirm}" id="delete_confirm" 
                           value="#{msgs.cdfm_button_bar_delete}" rendered="#{!ForumTool.selectedTopic.markForDeletion}"
                           accesskey="x">
	        	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
          </h:commandButton>
          <h:commandButton action="#{ForumTool.processActionDeleteTopic}" id="delete" 
                           value="#{msgs.cdfm_button_bar_delete}" rendered="#{ForumTool.selectedTopic.markForDeletion}"
                           accesskey="x">
	        	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
          </h:commandButton>
          <h:commandButton immediate="true" action="#{ForumTool.processActionHome}" id="cancel" 
                           value="#{msgs.cdfm_button_bar_cancel} " accesskey="c" />
       </div>
	 </h:form>
    </sakai:view>
</f:view>
