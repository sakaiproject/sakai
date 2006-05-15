<%@ page contentType="text/html; charset=UTF-8"
%><%@ page import="org.sakaiproject.tool.postem.PostemTool"
%><%
        PostemTool tool = (PostemTool) session.getAttribute("PostemTool");
        response.setHeader("Content-disposition", "attachment; filename=postem_" +
                tool.getCurrentGradebook().getTitle() + "_template.html");
        String template = tool.getCurrentGradebook().getTemplate().getTemplateCode();
        out.print(template);
        request.getSession(false).invalidate();
%>