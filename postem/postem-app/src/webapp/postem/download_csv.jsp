<%@ page contentType="application/vnd.ms-excel; charset=UTF-8"
%><%@ page import="org.sakaiproject.tool.postem.PostemTool"
%><%@ page import="javax.faces.context.FacesContext"
%><%@ page import="javax.faces.component.UIViewRoot" %><%
	PostemTool tool = (PostemTool) session.getAttribute("PostemTool");
	response.setHeader("Content-disposition", "attachment; filename=postem_" +
		tool.getCurrentGradebook().getTitle() + ".csv");
	
	response.setHeader ("Pragma", "public");
	response.setHeader("Cache-control", "must-revalidate");
	
	String csv = tool.getCsv();
	out.print(csv);
	//out.flush();
	request.getSession(false).invalidate();
	/*FacesContext context = FacesContext.getCurrentInstance();
	UIViewRoot view = context.getApplication().getViewHandler().restoreView(context, "/postem/main.jsp");
	context.setViewRoot(view);*/
		/*context.getApplication().
			getNavigationHandler().
				handleNavigation(context, "processCancelView","main");*/
	
%>
