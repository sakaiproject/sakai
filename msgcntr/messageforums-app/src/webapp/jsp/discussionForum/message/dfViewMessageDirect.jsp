<%@ page import="java.util.*,javax.faces.context.*,javax.faces.application.*,javax.faces.el.*,org.sakaiproject.tool.messageforums.*,org.sakaiproject.api.app.messageforums.*,org.sakaiproject.site.cover.SiteService,org.sakaiproject.tool.cover.ToolManager;"%>
<%
	FacesContext context = FacesContext.getCurrentInstance();
	Application app = context.getApplication();
	ValueBinding binding = app.createValueBinding("#{ForumTool}");
	DiscussionForumTool forumTool = (DiscussionForumTool) binding
			.getValue(context);

	String target = "";

	if (org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement() == null) {

		try {
			target = "/portal/tool/"
					+ request.getParameter("placementId")
					+ "/discussionForum/message/dfViewMessageDirect.jsf?messageId="
					+ request.getParameter("messageId") + "&topicId="
					+ request.getParameter("topicId") + "&forumId="
					+ request.getParameter("forumId");
			response.sendRedirect(target);
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	if (request.getParameter("topicId") == null
			|| "".equals(request.getParameter("topicId"))
			|| request.getParameter("forumId") == null
			|| "".equals(request.getParameter("forumId"))) {
		try {
			target = "/portal/tool/"
					+ org.sakaiproject.tool.cover.ToolManager
							.getCurrentPlacement().getId()
					+ "/discussionForum/forumsOnly/dfForums";
			response.sendRedirect(target);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;

	}
	target = "/jsp/discussionForum/message/dfViewMessage.jsf?messageId="
			+ request.getParameter("messageId") + "&topicId="
			+ request.getParameter("topicId") + "&forumId="
			+ request.getParameter("forumId");

	forumTool.processActionDisplayThread();

	// dispatch to the target
	RequestDispatcher dispatcher = getServletContext()
			.getRequestDispatcher(target);
	try {
		dispatcher.forward(request, response);
	} catch (ServletException e) {
		e.printStackTrace();
	}
%>