
<%@ page import="java.util.*, javax.faces.context.*, javax.faces.application.*,
                 javax.faces.el.*, org.sakaiproject.component.*, org.sakaiproject.service.legacy.security.*"
         errorPage="error.jsp" %>
                 
<%!
  FacesContext context = FacesContext.getCurrentInstance();
  Application app = context.getApplication();
  ValueBinding binding = app.createValueBinding("#{Components['org.sakaiproject.service.legacy.security.SecurityService']}");
  //ComponentMap cm = (ComponentMap) binding.getValue(context);
  SecurityService ss = (SecurityService) binding.getValue(context);
  //SecurityService ss = (SecurityService) cm.get("org.sakaiproject.service.legacy.security.SecurityService");
%>

<%  
  //if (!ss.isSuperUser()){
    //todo: convert to localized message
  //  request.setAttribute("error", "Permission Error: Must be a super-user to access this resource");
  //  request.getRequestDispatcher("error.jsp").forward(request, response);
  //}    
%>
