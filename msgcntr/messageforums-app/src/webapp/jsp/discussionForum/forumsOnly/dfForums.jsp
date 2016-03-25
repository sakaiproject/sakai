<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>

<%
// hack in attempt to fix navigation quirk
org.sakaiproject.tool.cover.SessionManager.getCurrentToolSession().
	removeAttribute(org.sakaiproject.jsf.util.JsfTool.LAST_VIEW_VISITED);
%>

<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>



<f:view>
  <sakai:view title="#{msgs.cdfm_discussion_forums}" toolCssHref="/messageforums-tool/css/msgcntr.css">
  		<script type="text/javascript">includeLatestJQuery("msgcntr");</script>
  		<sakai:script contextBase="/messageforums-tool" path="/js/sak-10625.js"/>
		<sakai:script contextBase="/messageforums-tool" path="/js/forum.js"/>

	<h:form id="msgForum">
  <sakai:tool_bar separator="#{msgs.cdfm_toolbar_separator}" >
 
    <sakai:tool_bar_item value="#{msgs.cdfm_new_forum}" action="#{ForumTool.processActionNewForum}" rendered="#{ForumTool.newForum}"/>
    <sakai:tool_bar_item value="#{msgs.cdfm_organize}" action="#{ForumTool.processActionTemplateOrganize}" rendered="#{ForumTool.instructor}" />
	  <sakai:tool_bar_item value="#{msgs.cdfm_template_setting}" action="#{ForumTool.processActionTemplateSettings}" rendered="#{ForumTool.instructor}" />
	  <sakai:tool_bar_item value="#{msgs.stat_list}" action="#{ForumTool.processActionStatistics}" rendered="#{ForumTool.instructor}" />
	  <sakai:tool_bar_item value="#{msgs.cdfm_msg_pending_queue} #{msgs.cdfm_openb}#{ForumTool.numPendingMessages}#{msgs.cdfm_closeb}" action="#{ForumTool.processPendingMsgQueue}" rendered="#{ForumTool.displayPendingMsgQueue}" />
 	  <sakai:tool_bar_item value="#{msgs.ranks}" action="#{ForumTool.processActionViewRanks}" rendered="#{ForumTool.instructor && ForumTool.ranksEnabled}" />
	  <sakai:tool_bar_item value="#{msgs.watch}" action="#{ForumTool.processActionWatch}" />

  </sakai:tool_bar>

	<h:messages styleClass="alertMessage" id="errorMessages" rendered="#{! empty facesContext.maximumSeverity}" />
 	<h1><h:outputText value="#{msgs.cdfm_discussion_forums}" /></h1>
 	
	<%@ include file="/jsp/discussionForum/includes/dfAreaInclude.jsp"%>
	
 	<%
  	String thisId = request.getParameter("panel");
  	if (thisId == null) 
  	{
    	thisId = "Main" + org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement().getId();
 		 }
	%>
			<script type="text/javascript">
			function resize(){
  				mySetMainFrameHeight('<%= org.sakaiproject.util.Web.escapeJavascript(thisId)%>');
  			}
			</script> 
 		</h:form>
 	</sakai:view>
 </f:view>
