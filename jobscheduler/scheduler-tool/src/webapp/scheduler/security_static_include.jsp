<%@ page import="java.util.*, javax.faces.context.*, javax.faces.application.*,
                 javax.faces.el.*, org.sakaiproject.component.*, org.sakaiproject.authz.api.*" %>
<%--
         errorPage="error.jsp" %>
--%>                 
<%!
  FacesContext context = FacesContext.getCurrentInstance();
  Application app = context.getApplication();
  ValueBinding binding = app.createValueBinding("#{Components['org.sakaiproject.authz.api.SecurityService']}");
  SecurityService ss = (SecurityService) binding.getValue(context);      
%>

<%  
  if (ss == null || !ss.isSuperUser()){
    request.setAttribute("error", "Permission Error: Must be a super-user to access this resource");
    request.getRequestDispatcher("error.jsp").forward(request, response);
  }    
%>
