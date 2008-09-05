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
   <sakai:view>

      <h:form id="revise">
             		<script type="text/javascript" src="/library/js/jquery.js"></script>
       		<sakai:script contextBase="/sakai-messageforums-tool" path="/js/sak-10625.js"/>
<!--jsp/discussionForum/topic/dfTopicSettings.jsp-->
        <sakai:tool_bar_message value="#{msgs.cdfm_discussion_topic_settings}" />

        <h:outputText styleClass="alertMessage" value="#{msgs.cdfm_delete_topic}" rendered="#{ForumTool.selectedTopic.markForDeletion}"/>

			<table summary="layout" class="itemSummary">
				<tr>
				  <th>
				    <h:outputText value="#{msgs.cdfm_topic_title}" />
				  </th>
				  <td>
    				  <h:outputText id="topic_title"  value="#{ForumTool.selectedTopic.topic.title}"/>
				  </td>
				</tr>
				<tr>
				  <th>
   				  <h:outputText value="#{msgs.cdfm_shortDescription}" />	
				  </th>
				  <td>	
				    <h:outputText id="topic_shortDescription"  value="#{ForumTool.selectedTopic.topic.shortDescription}"/>
				  </td>
				</tr>
				<tr>
				  <th><h:outputText value="#{msgs.cdfm_fullDescription}" />
				  </th>
				  <td><mf:htmlShowArea hideBorder="true" id="topic_fullDescription"  value="#{ForumTool.selectedTopic.topic.extendedDescription}"/>
				  </td>
				</tr>
		  </table>
				
				<h:dataTable value="#{ForumTool.selectedTopic.attachList}" var="eachAttach" rendered="#{!empty ForumTool.selectedTopic.attachList}" styleClass="listHier" columnClasses="attach,bogus">
				  <h:column>
					<sakai:contentTypeMap fileType="#{eachAttach.attachment.attachmentType}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>									
					<h:graphicImage id="exampleFileIcon" value="#{imagePath}" />					
					</h:column>
					  <h:column>
<%--					<h:outputLink value="#{eachAttach.attachmentUrl}" target="_blank">
					  <h:outputText value="#{eachAttach.attachmentName}"  style="text-decoration:underline;"/>
				    </h:outputLink>--%>
					<h:outputLink value="#{eachAttach.url}" target="_blank">
					  <h:outputText value="#{eachAttach.attachment.attachmentName}"  style="text-decoration:underline;"/>
				    </h:outputLink>
				  </h:column>
				</h:dataTable>



				<%--<h:panelGroup>
					 <h:dataTable id="topic_attachments"  value="#{ForumTool.selectedTopic.topic.attachments}" var="attachment" >
					  		<h:column rendered="#{!empty ForumTool.selectedTopic.topic.attachments}">
								<sakai:contentTypeMap fileType="#{attachment.attachmentType}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>									
								<h:graphicImage id="exampleFileIcon" value="#{imagePath}" />								
								<h:outputText value="#{attachment.attachmentName}"/>
						</h:column>
					</h:dataTable> 			 
				</h:panelGroup>--%>
      <h4><h:outputText value="#{msgs.cdfm_topic_posting}"/></h4>

			<h:panelGrid columns="2">
				<h:panelGroup >
				  <h:outputLabel for="topic_locked" value="#{msgs.cdfm_lock_topic}" styleClass="shorttext"/>
				</h:panelGroup>
				<h:panelGroup>	
					<h:selectOneRadio  layout="pageDirection" disabled="true" id="topic_locked"  value="#{ForumTool.selectedTopic.locked}" styleClass="checkbox inlineForm">
    			  <f:selectItem itemValue="true" itemLabel="#{msgs.cdfm_yes}"/>
    		  	<f:selectItem itemValue="false" itemLabel="#{msgs.cdfm_no}"/>
  			  </h:selectOneRadio>
				</h:panelGroup>
				<h:panelGroup >
				  <h:outputLabel for="topic_moderated" value="#{msgs.cdfm_moderate_topic}" styleClass="shorttext"/>
				</h:panelGroup>
				<h:panelGroup>	
		  	<h:selectOneRadio  layout="pageDirection" disabled="true" id="topic_moderated"  value="#{ForumTool.selectedTopic.moderated}" styleClass="checkbox inlineForm">
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
                           accesskey="r" styleClass="active"> 
    	 	  	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/> 
    	 	  	<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>        
          </h:commandButton>
          <h:commandButton action="#{ForumTool.processActionDeleteTopicConfirm}" id="delete_confirm" 
                           value="#{msgs.cdfm_button_bar_delete}" rendered="#{!ForumTool.selectedTopic.markForDeletion}"
                           >
	        	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
          </h:commandButton>
          <h:commandButton action="#{ForumTool.processActionDeleteTopic}" id="delete" 
                           value="#{msgs.cdfm_button_bar_delete}" rendered="#{ForumTool.selectedTopic.markForDeletion}"
                           >
	        	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
          </h:commandButton>
          <h:commandButton immediate="true" action="#{ForumTool.processReturnToOriginatingPage}" id="cancel" 
                           value="#{msgs.cdfm_button_bar_cancel} " accesskey="x" />
       </div>
	 </h:form>
    </sakai:view>
</f:view>
