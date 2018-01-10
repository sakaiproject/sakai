<%@ page import="java.util.*, javax.faces.context.*, javax.faces.application.*,
                 javax.faces.el.*, org.sakaiproject.tool.messageforums.*,
                 org.sakaiproject.api.app.messageforums.*,
                 org.sakaiproject.site.cover.SiteService,
                 org.sakaiproject.tool.cover.ToolManager" %>
<%@ page import="org.sakaiproject.component.cover.ServerConfigurationService" %>
<%@ page import="org.slf4j.Logger,org.slf4j.LoggerFactory" %>
<%! static final Logger log = LoggerFactory.getLogger("dfAllMessagesDirect.jsp"); %>
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
             + "/discussionForum/message/dfAllMessagesDirect.jsf?topicId="
      	     + request.getParameter("topicId");
      response.sendRedirect(target);
      return;
    }
    catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

if(forumTool.getHasTopicAccessPrivileges(request.getParameter("topicId"))){
  target = "/jsp/discussionForum/message/dfAllMessages.jsf?topicId="
  	       + request.getParameter("topicId");

  forumTool.processActionDisplayTopic();

  // dispatch to the target
  RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(target);
  try {
    dispatcher.forward(request, response);
  }
  catch (ServletException e) {
    log.error(e.getMessage(), e);
  }

  }else{
	if(request.getParameter("topicId") == null) {
		// If we're in here it means we have lost topicId. We should direct
		// them to the topic's parent forum which will then render the topic.
		target = "/jsp/discussionForum/message/dfAllMessages.jsf?forumId="
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


	} else { 
		// If we get to this point, it means the user is not supposed to see this topic 
		// because of permissions.
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
  }
%>
