<%@ page import="java.util.*, javax.faces.context.*, javax.faces.application.*,
                 javax.faces.el.*, org.sakaiproject.tool.messageforums.*,
                 org.sakaiproject.api.app.messageforums.*"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%  

  FacesContext context = FacesContext.getCurrentInstance();
  Application app = context.getApplication();
  ValueBinding binding = app.createValueBinding("#{ForumTool}");
  DiscussionForumTool ForumTool = (DiscussionForumTool) binding.getValue(context);
  
	String action=request.getParameter("action");
	String messageId = request.getParameter("messageId");
	String topicId = request.getParameter("topicId");

  try {
      response.setHeader("Pragma", "No-Cache");
      response.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
      response.setDateHeader("Expires", 1);       
      if(action == null) {
          out.println("FAIL");
      } else if(action.equals("markMessageAsRead")) {
    	  //Ajax call to mark messages as read for user
    	  if(messageId != null && topicId != null){
	    	  if(!ForumTool.isMessageReadForUser(new Long(topicId), new Long(messageId))){
				ForumTool.markMessageReadForUser(new Long(topicId), new Long(messageId), true);
 		   		out.println("SUCCESS");
		      } else {
		    	  //also output success in case message is read, but page rendered mail icon (old state)
		    	  out.println("SUCCESS");
		      }
	      }
      }
      out.flush();
  } catch(Exception ee) {
      ee.printStackTrace();
  }

%>