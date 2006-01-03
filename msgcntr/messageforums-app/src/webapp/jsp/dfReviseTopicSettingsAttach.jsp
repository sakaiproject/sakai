<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>
<link href='/sakai-messageforums-tool/css/msgForums.css' rel='stylesheet' type='text/css' />
<f:view>
   <sakai:view>
      <h:form id="topic_revise_settings">
      <sakai:script contextBase="/sakai-jsf-resource" path="/hideDivision/hideDivision.js"/>
        <sakai:tool_bar_message value="#{msgs.cdfm_discussion_topic_settings}" />
 			 <div class="instruction">
  			    <h:outputText id="instruction"  value="#{msgs.cdfm_settings_instruction}  #{msgs.cdfm_info_required_sign}"/>
			 </div>
			 <h:messages styleClass="alertMessage" id="errorMessages" /> 
        <p class="shorttext">
			<h:panelGrid columns="3">
				<h:panelGroup><h:outputText id="req_star"  value="#{msgs.cdfm_info_required_sign}" style="color: red"/>	</h:panelGroup>
				<h:panelGroup><h:outputLabel id="outputLabel" for="topic_title"  value="#{msgs.cdfm_topic_title}"/>	</h:panelGroup>
				<h:panelGroup><h:inputText size="50" id="topic_title"  value="#{ForumTool.selectedTopic.topic.title}"/></h:panelGroup>

				<h:panelGroup/>
				<h:panelGroup><h:outputLabel id="outputLabel1" for="topic_shortDescription"  value="#{msgs.cdfm_shortDescription}"/>	</h:panelGroup>
				<h:panelGroup><h:inputTextarea rows="3" cols="45" id="topic_shortDescription"  value="#{ForumTool.selectedTopic.topic.shortDescription}"/></h:panelGroup>

				<h:panelGroup/>
				<h:panelGroup><h:outputLabel id="outputLabel2" for="topic_fullDescription"  value="#{msgs.cdfm_fullDescription}"/>	</h:panelGroup>
				<h:panelGroup><h:inputTextarea rows="6" cols="60" id="topic_fullDescription"  value="#{ForumTool.selectedTopic.topic.extendedDescription}"/></h:panelGroup>
      		</h:panelGrid>
		</p>

   		<sakai:group_box>
	      <table width="100%" align="center">
	        <tr>
	          <td align="center" style="font-weight:bold;background-color:#DDDFE4;color: #000;padding:.3em;margin:-.3em -2.2em;text-align:left;font-size: .9em;line-height:1.3em">
	            <h:outputText value="Attachments"/>
	          </td>
	        </tr>
	      </table>
	      <sakai:doc_section>
	        <sakai:button_bar>
	        	<sakai:button_bar_item action="#{ForumTool.processAddAttachmentRedirect}" value="#{msgs.cdfm_button_bar_add_attachment_redirect}" immediate="true"/>
	        </sakai:button_bar>
	      </sakai:doc_section>
   		
	      <sakai:doc_section>	        
		      <h:outputText value="No Attachments Yet" rendered="#{empty ForumTool.attachments}"/>
	      </sakai:doc_section>
	        
				<h:dataTable styleClass="listHier" id="attmsg" width="100%" value="#{ForumTool.attachments}" var="eachAttach" >
				  <h:column rendered="#{!empty ForumTool.attachments}">
						<f:facet name="header">
							<h:outputText value="Title"/>
						</f:facet>
						<sakai:doc_section>
							<h:graphicImage url="/images/excel.gif" rendered="#{eachAttach.attachmentType == 'application/vnd.ms-excel'}"/>
							<h:graphicImage url="/images/html.gif" rendered="#{eachAttach.attachmentType == 'text/html'}"/>
							<h:graphicImage url="/images/pdf.gif" rendered="#{eachAttach.attachmentType == 'application/pdf'}"/>
							<h:graphicImage url="/images/ppt.gif" rendered="#{eachAttach.attachmentType == 'application/vnd.ms-powerpoint'}"/>
							<h:graphicImage url="/images/text.gif" rendered="#{eachAttach.attachmentType == 'text/plain'}"/>
							<h:graphicImage url="/images/word.gif" rendered="#{eachAttach.attachmentType == 'application/msword'}"/>
						
							<h:outputText value="#{eachAttach.attachmentName}"/>
						</sakai:doc_section>
						
					  <sakai:doc_section>
							<h:commandLink action="#{ForumTool.processDeleteAttachSetting}" 
								immediate="true"
								onfocus="document.forms[0].onsubmit();">
							<h:outputText value="     Remove"/>
								<f:param value="#{eachAttach.attachmentId}" name="dfmsg_current_attach"/>
							</h:commandLink>
						</sakai:doc_section>
						
				  </h:column>
					<h:column rendered="#{!empty ForumTool.attachments}">
						<f:facet name="header">
							<h:outputText value="Size" />
						</f:facet>
						<h:outputText value="#{eachAttach.attachmentSize}"/>
					</h:column>
					<h:column rendered="#{!empty ForumTool.attachments}">
						<f:facet name="header">
		  			  <h:outputText value="Type" />
						</f:facet>
						<h:outputText value="#{eachAttach.attachmentType}"/>
					</h:column>
					</h:dataTable>   
			</sakai:group_box>   

       <h4><h:outputText  value="#{msgs.cdfm_topic_posting}"/></h4>
        <p class="shorttext">
			<h:panelGrid columns="2">
				<h:panelGroup><h:outputLabel id="outputLabel3" for="topic_posting"  value="#{msgs.cdfm_lock_topic}"/>	</h:panelGroup>
				<h:panelGroup>
					<h:selectOneRadio layout="pageDirection"  id="topic_posting"  value="#{ForumTool.selectedTopic.locked}">
    					<f:selectItem itemValue="true" itemLabel="Yes"/>
    					<f:selectItem itemValue="false" itemLabel="No"/>
  					</h:selectOneRadio>
				</h:panelGroup>
			</h:panelGrid>
		</p>
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
      <mf:forumHideDivision title="#{msgs.cdfm_control_permissions}" id="cntrl_perm">
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
			<%--<h:column>
				<f:facet name="header">	<h:outputText value="#{msgs.perm_move_postings}" /></f:facet>
				<h:selectBooleanCheckbox value="#{cntrl_settings.movePostings}"/>
			</h:column>--%>
			<h:column>
				<f:facet name="header"><h:outputText value="#{msgs.perm_change_settings}" /></f:facet>
				<h:selectBooleanCheckbox disabled="true" value="#{cntrl_settings.changeSettings}"/>
			</h:column>
			<%--<h:column>
				<f:facet name="header"><h:outputText value="#{msgs.perm_post_to_gradebook}" /></f:facet>
				<h:selectBooleanCheckbox value="#{cntrl_settings.postToGradebook}"/>
			</h:column>		--%>
		</h:dataTable>
      </mf:forumHideDivision>
      <mf:forumHideDivision title="#{msgs.cdfm_message_permissions}" id="msg_perm">
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
      
      <p class="act">
          <h:commandButton action="#{ForumTool.processActionSaveTopicSettings}" value="#{msgs.cdfm_button_bar_save_setting}"> 
    	 	  	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>    
    	 	  	<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>         
          </h:commandButton>
          <h:commandButton action="#{ForumTool.processActionSaveTopicAsDraft}" value="#{msgs.cdfm_button_bar_save_draft}">
	        	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
	        	<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/> 
          </h:commandButton>
          <h:commandButton action="#{ForumTool.processActionSaveTopicAndAddTopic}" value="#{msgs.cdfm_button_bar_save_setting_add_topic}">
	        	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
	        	<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/> 
          </h:commandButton>
          <h:commandButton  action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_button_bar_cancel}" />
       </p>
       
	 </h:form>
    </sakai:view>
</f:view>
