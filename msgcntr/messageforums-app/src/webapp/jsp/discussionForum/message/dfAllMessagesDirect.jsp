<%@ page import="java.util.*, javax.faces.context.*, javax.faces.application.*,
                 javax.faces.el.*, org.sakaiproject.tool.messageforums.*,
                 org.sakaiproject.api.app.messageforums.*,
                 org.sakaiproject.site.cover.SiteService,
                 org.sakaiproject.tool.cover.ToolManager;"%>
<%

  FacesContext context = FacesContext.getCurrentInstance();
  Application app = context.getApplication();
  ValueBinding binding = app.createValueBinding("#{ForumTool}");
  DiscussionForumTool forumTool = (DiscussionForumTool) binding.getValue(context);

  String target = "";

  if (org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement() == null) {

    try {
      target = "/portal/tool/" + request.getParameter("placementId")
             + "/discussionForum/message/dfAllMessagesDirect.jsf?topicId="
      	     + request.getParameter("topicId");
      response.sendRedirect(target);
      return;
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  target = "/jsp/discussionForum/message/dfAllMessages.jsf?topicId="
  	       + request.getParameter("topicId");

  forumTool.processActionDisplayTopic();

  // dispatch to the target
  RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(target);
  try {
    dispatcher.forward(request, response);
  }
  catch (ServletException e) {
    e.printStackTrace();
  }

%>
