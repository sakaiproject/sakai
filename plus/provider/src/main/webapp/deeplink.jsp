<html>
<head>
<%@ page import="java.util.List" %>
<%@ page import="org.sakaiproject.tool.api.Tool" %>
<%@ page import="org.sakaiproject.portal.util.PortalUtils" %>
<%@ page import="org.sakaiproject.portal.util.CSSUtils" %>
<%@ page import="org.sakaiproject.portal.util.ToolUtils" %>
<title>LTI ContentItem Experimental Support</title>
<%= CSSUtils.getCssToolSkinLink((String) null, ToolUtils.isInlineRequest(request)) %>
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
<div style="border: 2px, solid, red;" class="card">
<p>
<span class="icon-sakai-site">
<strong><%= (String) request.getAttribute("site_title")  %></strong></span></p>
<p><%= (String) request.getAttribute("site_description")  %></p>
<center>
    <form method="post" action="deep.link">
        <input type="hidden" name="install" value="sakai.site"/>
        <input type="hidden" name="id_token" value="<%= (String) request.getAttribute("id_token") %>"/>
        <input type="hidden" name="payload" value="<%= (String) request.getAttribute("payload") %>"/>
        <input type="submit" role="button" value="<%= (String) request.getAttribute("details_text") %>"/>
    </form>
</center>
</p>
</div>
<% if (request != null && request.getAttribute("tools") != null) { for (Tool tool : (List<Tool>) request.getAttribute("tools") ) { %>
<div style="border: 2px, solid, red;" class="card">
<p>
<span class="icon-<%= tool.getId().replace(".","-") %>">
<strong><%= tool.getTitle() %></strong></span></p>
<p><%= tool.getDescription() %></p>
<center>
    <form method="post" action="deep.link">
        <input type="hidden" name="install" value="<%= tool.getId() %>"/>
        <input type="hidden" name="id_token" value="<%= (String) request.getAttribute("id_token") %>"/>
        <input type="hidden" name="payload" value="<%= (String) request.getAttribute("payload") %>"/>
        <input type="submit" role="button" value="<%= (String) request.getAttribute("details_text") %>"/>
    </form>
</center>
</p>
</div>
<% }} %>
</div>
<%= PortalUtils.includeLatestJQuery("sakai.deeplink") %>
<script type="text/javascript" src="/library/js/waterfall-light.js"></script>
<script>
$(function(){
    $('#box').waterfall({refresh: 0})
});
</script>
<% } %>
</div>
</body>
