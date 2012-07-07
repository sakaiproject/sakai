<%@ page contentType="application/vnd.ms-excel; charset=UTF-8"
%><%@ page import="org.sakaiproject.tool.postem.PostemTool"
%><%@ page import="org.sakaiproject.util.Web"
%><%@ page import="javax.faces.context.FacesContext"
%><%@ page import="javax.faces.component.UIViewRoot" %><%
	PostemTool tool = (PostemTool) session.getAttribute("PostemTool");
	String[] invalidChars = {":",";","\\*","\\?","\\^","\\$","\\.","\\|","\\+","\\(","\\)","\\[","<",">","\\{","}",",","\"","\\\\"};
  String titleName = tool.getCurrentGradebook().getTitle().trim();
  titleName = titleName.replaceAll(" ","_");

  for(int i=0; i < invalidChars.length; i++) {
	  titleName = titleName.replaceAll(invalidChars[i], "");
  }
   
	response.setHeader("Content-disposition", "attachment; filename=" +
		Web.encodeFileName(request, "postem_" + titleName + ".csv"));
	
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
