<html>
<head>
<%@ page import="java.util.List" %>
<%@ page import="org.sakaiproject.tool.api.Tool" %>
<%@ page import="org.sakaiproject.portal.util.PortalUtils" %>
<%@ page import="org.sakaiproject.portal.util.CSSUtils" %>
<title>IMS ContentItem Experimental Support</title>
<%= CSSUtils.getCssToolSkinLink((String) null) %>        
<script src="<%= PortalUtils.getScriptPath() %>headscripts.js<%= PortalUtils.getCDNQuery() %>"></script>
<style>
.card {
        border: 1px solid black;
        margin: 10ps;
        padding: 5px;
}
#loader {
      position: fixed;
      left: 0px;
      top: 0px;
      width: 100%;
      height: 100%;
      background-color: white;
      margin: 0;
      z-index: 100;
}
#XbasicltiDebugToggle {
    display: none;
}
#container {
    padding: 5px;
}
</style>
</head>
<body>
<div id="container">
<% if ( request.getAttribute("tool") != null ) { 
	Tool tool = (Tool) request.getAttribute("tool");
%>
<p>
<span class="icon-<%= tool.getId().replace(".","-") %>">
<strong><%= tool.getTitle() %></strong></span></p>
<p><%= tool.getDescription() %></p>
<%= (String) request.getAttribute("launch_html") %>
<% } else { %>
<div id="box">
<% for (Tool tool : (List<Tool>) request.getAttribute("tools") ) { %>
<div style="border: 2px, solid, red;" class="card">
<p>
<span class="icon-<%= tool.getId().replace(".","-") %>">
<strong><%= tool.getTitle() %></strong></span></p>
<p><%= tool.getDescription() %></p>
<center><a href="content.item?install=<%= tool.getId() %>" class="btn btn-default" role="button">Details</a></center>
</p>
</div>
<% } %>
</div>
<%= PortalUtils.includeLatestJQuery("content.item") %>
<script type="text/javascript" src="<%= request.getContextPath() %>/static/waterfall-light.js"></script>
<script>
$(function(){
    $('#box').waterfall({refresh: 0})
});
</script>
<% } %>
</div>
</body>
