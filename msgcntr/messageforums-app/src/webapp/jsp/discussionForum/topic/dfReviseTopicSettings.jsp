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
        <p class="shorttext">
			<h:panelGrid columns="3">
				<h:panelGroup><h:outputText id="req_star"  value="#{msgs.cdfm_info_required_sign}" style="color: red"/>	</h:panelGroup>
				<h:panelGroup><h:outputLabel id="outputLabel" for="topic_title"  value="#{msgs.cdfm_topic_title}"/>	</h:panelGroup>
				<h:panelGroup><h:inputText size="50" id="topic_title"  value="#{ForumTool.selectedTopic.topic.title}"/></h:panelGroup>

				<h:panelGroup/>
				<h:panelGroup><h:outputLabel id="outputLabel1" for="topic_shortDescription"  value="#{msgs.cdfm_shortDescription}"/>	</h:panelGroup>
				<h:panelGroup><h:inputTextarea rows="5" cols="100" id="topic_shortDescription"  value="#{ForumTool.selectedTopic.topic.shortDescription}"/></h:panelGroup>

				<h:panelGroup/>
				<h:panelGroup><h:outputLabel id="outputLabel2" for="topic_fullDescription"  value="#{msgs.cdfm_fullDescription}"/>	</h:panelGroup>
				<h:panelGroup><h:inputTextarea rows="5" cols="100" id="topic_fullDescription"  value="#{ForumTool.selectedTopic.topic.extendedDescription}"/></h:panelGroup>
      		</h:panelGrid>
		</p>
       <h4><h:outputText  value="#{msgs.cdfm_attachments}"/></h4>

       <h4><h:outputText  value="#{msgs.cdfm_topic_posting}"/></h4>
        <p class="shorttext">
			<h:panelGrid columns="2">
				<h:panelGroup><h:outputLabel id="outputLabel3" for="topic_posting"  value="#{msgs.cdfm_lock_topic}"/>	</h:panelGroup>
				<h:panelGroup>
					<h:selectOneRadio layout="pageDirection"  id="topic_posting"  value="#{ForumTool.selectedTopic.topic.locked}">
    					<f:selectItem itemValue="true" itemLabel="Yes"/>
    					<f:selectItem itemValue="false" itemLabel="No"/>
  					</h:selectOneRadio>
				</h:panelGroup>
			</h:panelGrid>
		</p>
	   <h4><h:outputText  value="Confidential Responses"/></h4>
	   <h:selectBooleanCheckbox   title= "Allow anonymous postings (Identity known to administrative users)"  value="false" /><h:outputText   value="  Allow anonymous postings (Identity known to administrative users)" /> 
	   <br/>
	   <h:selectBooleanCheckbox   title= "I do not wish to know the authors identity"  value="false" /><h:outputText   value="  I do not wish to know the author identity" />
       <h4><h:outputText  value="Post Before Reading"/></h4>
    	   <p class="shorttext">
			<h:panelGrid columns="2">
				<h:panelGroup><h:outputLabel id="outputLabel4" for="topic_reading"  value="Users must post a response before reading others"/>	</h:panelGroup>
				<h:panelGroup>
					<h:selectOneRadio layout="pageDirection"  id="topic_reading"  value="true">
    					<f:selectItem itemValue="false" itemLabel="Yes"/>
    					<f:selectItem itemValue="false" itemLabel="No"/>
  					</h:selectOneRadio>
				</h:panelGroup>
			</h:panelGrid>
		</p>
      <mf:forumHideDivision title="#{msgs.cdfm_control_permissions}" id="cntrl_perm">
      </mf:forumHideDivision>
      <mf:forumHideDivision title="#{msgs.cdfm_message_permissions}" id="msg_perm">
      </mf:forumHideDivision>
      
      <p class="act">
          <h:commandButton action="#{ForumTool.processActionSaveTopicSettings}" value="#{msgs.cdfm_button_bar_save_setting}"> 
    	 	  	<f:param value="#{ForumTool.selectedTopic.topic.uuid}" name="topicId"/>         
          </h:commandButton>
          <h:commandButton action="#{ForumTool.processActionSaveTopicAsDraft}" value="#{msgs.cdfm_button_bar_save_draft}">
	        	<f:param value="#{ForumTool.selectedTopic.topic.uuid}" name="topicId"/>
          </h:commandButton>
          <h:commandButton action="#{ForumTool.processActionSaveTopicAndAddTopic}" value="#{msgs.cdfm_button_bar_save_setting_add_topic}">
	        	<f:param value="#{ForumTool.selectedTopic.topic.uuid}" name="topicId"/>
          </h:commandButton>
          <h:commandButton immediate="true" action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_button_bar_cancel}" />
       </p>
       
	 </h:form>
    </sakai:view>
</f:view>
