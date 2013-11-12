
  <mf:forum_bar_link id="tpl_create_forum" title="#{msgs.cdfm_new_forum}" value=" #{msgs.cdfm_new_forum}" action="#{ForumTool.processActionNewForum}" rendered="#{ForumTool.newForum}"/>    
   
  <h:outputText id="draft_space2" value="  &nbsp;  " escape="false" />
  <mf:forum_bar_link id="template_setting" title="#{msgs.cdfm_template_setting}" value="#{msgs.cdfm_template_setting} " action="#{ForumTool.processActionTemplateSettings}" rendered="#{ForumTool.instructor}"/>    
     
  <h:outputText id="draft_space3" value="  &nbsp;  " escape="false" />
  <mf:forum_bar_link id="template_organize" title="#{msgs.cdfm_organize}" value="#{msgs.cdfm_organize} " action="#{ForumTool.processActionTemplateOrganize}" rendered="#{ForumTool.instructor}"/>    
  <h:outputText id="draft_space4" value="  &nbsp;  " escape="false" />
  <mf:forum_bar_link id="template_statistic" title="#{msgs.cdfm_statistics}" value="#{msgs.cdfm_statistics}" action="#{ForumTool.processActionStatistics}" rendered="#{ForumTool.instructor}" />

	<h:outputText id="draft_space5" value="  &nbsp;  " escape="false" />
	<mf:forum_bar_link id="pending_msgs" title="#{msgs.cdfm_msg_pending_queue}" value="#{msgs.cdfm_msg_pending_queue} #{msgs.cdfm_openb}#{ForumTool.numPendingMessages}#{msgs.cdfm_closeb}" 
		action="#{ForumTool.processPendingMsgQueue}" rendered="#{ForumTool.displayPendingMsgQueue}"/>  

	<%@ include file="/jsp/discussionForum/includes/dfAreaInclude.jsp"%>
 