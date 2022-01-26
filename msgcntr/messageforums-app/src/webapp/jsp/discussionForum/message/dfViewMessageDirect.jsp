<%@ page import="java.util.*,javax.faces.context.*,javax.faces.application.*,javax.faces.el.*,org.sakaiproject.tool.messageforums.*,org.sakaiproject.api.app.messageforums.*,org.sakaiproject.site.cover.SiteService,org.sakaiproject.tool.cover.ToolManager" %>
<%@ page import="org.sakaiproject.component.cover.ServerConfigurationService" %>
<%@ page import="org.slf4j.Logger,org.slf4j.LoggerFactory" %>
<%! static final Logger log = LoggerFactory.getLogger("dfViewMessageDirect.jsp"); %>
<%
	FacesContext context = FacesContext.getCurrentInstance();
	Application app = context.getApplication();
	ValueBinding binding = app.createValueBinding("#{ForumTool}");
	DiscussionForumTool forumTool = (DiscussionForumTool) binding
			.getValue(context);

	String target = "";
    String portalPath = ServerConfigurationService.getString("portalPath");

	if (org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement() == null) {

		try {
			target = portalPath + "/tool/"
					+ request.getParameter("placementId")
					+ "/discussionForum/message/dfViewMessageDirect.jsf?messageId="
					+ request.getParameter("messageId") + "&topicId="
					+ request.getParameter("topicId") + "&forumId="
					+ request.getParameter("forumId");
			response.sendRedirect(target);
			return;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}else if (request.getParameter("topicId") == null
			|| "".equals(request.getParameter("topicId"))
			|| request.getParameter("forumId") == null
			|| "".equals(request.getParameter("forumId"))) {
		try {
			target = portalPath + "/tool/"
					+ org.sakaiproject.tool.cover.ToolManager
							.getCurrentPlacement().getId()
					+ "/discussionForum/forumsOnly/dfForums";
			response.sendRedirect(target);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return;

	}else if(forumTool.getHasTopicAccessPrivileges(request.getParameter("topicId"))){
		String placementId = null;
		String siteId = null;
		try {
			siteId = ToolManager.getCurrentPlacement().getContext();
			if (SiteService.getSite(siteId).getToolForCommonId("sakai.forums") != null) {
				placementId = SiteService.getSite(siteId).getToolForCommonId("sakai.forums").getId();
			}else if (SiteService.getSite(siteId).getToolForCommonId("sakai.messagecenter") != null) {
				placementId = SiteService.getSite(siteId).getToolForCommonId("sakai.messagecenter").getId();
			}
		}
		catch (Exception e) {
			log.warn("dfViewThreadDirect - error while trying to get Forums tool id : {}", e.getMessage());
			return;
		}

		target = portalPath + "/site/" + siteId + "/tool/" + placementId + "/discussionForum/message/dfViewMessage?messageId="
				+ request.getParameter("messageId") + "&placementId=" + placementId + "&topicId="
				+ request.getParameter("topicId") + "&forumId="
				+ request.getParameter("forumId");
	
		forumTool.processActionDisplayThread();
		forumTool.processActionDisplayMessage();

		try {
			response.sendRedirect(target);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}else{
	  	%>
	  	<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
	   		<jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
		</jsp:useBean>
		<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
	    <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
	    <f:view>
	    <f:verbatim><br/><br/></f:verbatim>
	    <h:outputText value="#{msgs.cdfm_insufficient_privileges_view_topic}"/>
	    </f:view>
	  	<%
	  }
%>