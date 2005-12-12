<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>
<link href='/sakai-messageforums-tool/css/msgForums.css' rel='stylesheet' type='text/css' />
<f:view>
   <sakai:view>
      <h:form id="forum_revise_settings">
      <sakai:script contextBase="/sakai-jsf-resource" path="/hideDivision/hideDivision.js"/>
        <sakai:tool_bar_message value="#{msgs.cdfm_discussion_forum_settings}" />
 			 <div class="instruction">
  			    <h:outputText id="instruction"  value="#{msgs.cdfm_settings_instruction}  #{msgs.cdfm_info_required_sign}"/>
			 </div>
        <p class="shorttext">
			<h:panelGrid columns="3">
				<h:panelGroup><h:outputText id="req_star"  value="#{msgs.cdfm_info_required_sign}" style="color: red"/>	</h:panelGroup>
				<h:panelGroup><h:outputLabel id="outputLabel" for="forum_title"  value="#{msgs.cdfm_forum_title}"/>	</h:panelGroup>
				<h:panelGroup><h:inputText size="50" id="forum_title"  value="#{ForumTool.selectedForum.forum.title}"/></h:panelGroup>

				<h:panelGroup/>
				<h:panelGroup><h:outputLabel id="outputLabel1" for="forum_shortDescription"  value="#{msgs.cdfm_shortDescription}"/>	</h:panelGroup>
				<h:panelGroup><h:inputTextarea rows="5" cols="100" id="forum_shortDescription"  value="#{ForumTool.selectedForum.forum.shortDescription}"/></h:panelGroup>

				<h:panelGroup/>
				<h:panelGroup><h:outputLabel id="outputLabel2" for="forum_fullDescription"  value="#{msgs.cdfm_fullDescription}"/>	</h:panelGroup>
				<h:panelGroup><h:inputTextarea rows="5" cols="100" id="forum_fullDescription"  value="#{ForumTool.selectedForum.forum.extendedDescription}"/></h:panelGroup>
      		</h:panelGrid>
		</p>
       <h4><h:outputText  value="#{msgs.cdfm_attachments}"/></h4>

       <h4><h:outputText  value="#{msgs.cdfm_forum_posting}"/></h4>
        <p class="shorttext">
			<h:panelGrid columns="2">
				<h:panelGroup><h:outputLabel id="outputLabel3" for="forum_posting"  value="#{msgs.cdfm_lock_forum}"/>	</h:panelGroup>
				<h:panelGroup>
					<h:selectOneRadio layout="pageDirection"  id="forum_posting"  value="#{ForumTool.selectedForum.locked}">
    					<f:selectItem itemValue="true" itemLabel="Yes"/>
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
          <h:commandButton action="#{ForumTool.processActionSaveForumSettings}" value="#{msgs.cdfm_button_bar_save_setting}"> 
    	 	  	<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>         
          </h:commandButton>
          <h:commandButton action="#{ForumTool.processActionSaveForumAsDraft}" value="#{msgs.cdfm_button_bar_save_draft}">
	        	<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
          </h:commandButton>
          <h:commandButton action="#{ForumTool.processActionSaveForumAndAddTopic}" value="#{msgs.cdfm_button_bar_save_setting_add_topic}">
	        	<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
          </h:commandButton>
          <h:commandButton immediate="true" action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_button_bar_cancel}" />
       </p>
       
	 </h:form>
    </sakai:view>
</f:view>
