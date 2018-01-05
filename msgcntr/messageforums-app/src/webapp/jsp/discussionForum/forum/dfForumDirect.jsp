<%@ page import="java.util.*, javax.faces.context.*, javax.faces.application.*,
                 javax.faces.el.*, org.sakaiproject.tool.messageforums.*,
                 org.sakaiproject.api.app.messageforums.*,
                 org.sakaiproject.site.cover.SiteService,
                 org.sakaiproject.tool.cover.ToolManager" %>
<%@ page import="org.sakaiproject.component.cover.ServerConfigurationService" %>
<%@ page import="org.slf4j.Logger,org.slf4j.LoggerFactory" %>
<%! static final Logger log = LoggerFactory.getLogger("dfForumDirect.jsp"); %>
<%

  FacesContext context = FacesContext.getCurrentInstance();
  Application app = context.getApplication();
  ValueBinding binding = app.createValueBinding("#{ForumTool}");
  DiscussionForumTool forumTool = (DiscussionForumTool) binding.getValue(context);

  String target = "";

  if (org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement() == null) {
    String portalPath = ServerConfigurationService.getString("portalPath");
    try {
      target = portalPath + "/tool/" + request.getParameter("placementId")
             + "/discussionForum/forum/dfForumDirect.jsf?forumId="
      	     + request.getParameter("forumId");
      response.sendRedirect(target);
      return;
    }
    catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  target = "/jsp/discussionForum/forum/dfForumDetail.jsf?forumId="
  	       + request.getParameter("forumId");
  forumTool.processActionDisplayForum();

  // dispatch to the target
  RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(target);
  try {
    dispatcher.forward(request, response);
  }
  catch (ServletException e) {
    log.error(e.getMessage(), e);
  }

%>