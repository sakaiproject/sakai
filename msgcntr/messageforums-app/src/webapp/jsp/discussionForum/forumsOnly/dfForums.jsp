<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>

<%
// hack in attempt to fix navigation quirk
org.sakaiproject.tool.cover.SessionManager.getCurrentToolSession().
	removeAttribute(org.sakaiproject.jsf2.util.JsfTool.LAST_VIEW_VISITED);
%>

<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>



<f:view>
  <sakai:view title="#{msgs.cdfm_discussion_forums}" toolCssHref="/messageforums-tool/css/msgcntr.css">
  		<script type="text/javascript">includeLatestJQuery("msgcntr");</script>
		<script type="text/javascript" src="/messageforums-tool/js/sak-10625.js"></script>
		<script type="text/javascript" src="/messageforums-tool/js/forum.js"></script>

	<h:form id="msgForum">
  <sakai:tool_bar>
    <h:commandLink id="newForum" rendered="#{ForumTool.newForum}"
      action="#{ForumTool.processActionNewForum}" immediate="true">
        <h:outputText value="#{msgs.cdfm_new_forum}" />
    </h:commandLink>
    <h:commandLink id="organizeForum" rendered="#{ForumTool.instructor}"
      action="#{ForumTool.processActionTemplateOrganize}" immediate="true">
        <h:outputText value="#{msgs.cdfm_organize}" />
    </h:commandLink>
    <h:commandLink id="templateSettings" rendered="#{ForumTool.instructor}"
      action="#{ForumTool.processActionTemplateSettings}" immediate="true">
        <h:outputText value="#{msgs.cdfm_template_setting}" />
    </h:commandLink>
    <h:commandLink id="statList" rendered="#{ForumTool.instructor}"
      action="#{ForumTool.processActionStatistics}" immediate="true">
        <h:outputText value="#{msgs.stat_list}" />
    </h:commandLink>
    <h:commandLink id="pendingQueue" rendered="#{ForumTool.displayPendingMsgQueue}"
      action="#{ForumTool.processPendingMsgQueue}" immediate="true">
        <h:outputText value="#{msgs.cdfm_msg_pending_queue} #{msgs.cdfm_openb}#{ForumTool.numPendingMessages}#{msgs.cdfm_closeb}" />
    </h:commandLink>
    <h:commandLink id="viewRanks" rendered="#{ForumTool.instructor && ForumTool.ranksEnabled}"
      action="#{ForumTool.processPendingMsgQueue}" immediate="true">
        <h:outputText value="#{msgs.ranks}" />
    </h:commandLink>
    <h:commandLink id="watch"
      action="#{ForumTool.processActionWatch}" immediate="true">
        <h:outputText value="#{msgs.watch}" />
    </h:commandLink>
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
