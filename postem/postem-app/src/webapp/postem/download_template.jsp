<%@ page contentType="text/html; charset=UTF-8"
%><%@ page import="org.sakaiproject.tool.postem.PostemTool"
%><%
        PostemTool tool = (PostemTool) session.getAttribute("PostemTool");
        String[] invalidChars = {":",";","\\*","\\?","\\^","\\$","\\.","\\|","\\+","\\(","\\)","\\[","<",">","\\{","}",",","\"","\\\\"};
        String titleName = tool.getCurrentGradebook().getTitle().trim();
        titleName = titleName.replaceAll(" ","_");

        for(int i=0; i < invalidChars.length; i++) {
	        titleName = titleName.replaceAll(invalidChars[i], "");
        }

        response.setHeader("Content-disposition", "attachment; filename=postem_" +
                titleName + "_template.html");
        String template = tool.getCurrentGradebook().getTemplate().getTemplateCode();
        response.setHeader ("Pragma", "public");
    	  response.setHeader("Cache-control", "must-revalidate");
        
        out.print(template);
        request.getSession(false).invalidate();
        
%>