<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%
    response.setContentType("text/html; charset=UTF-8");
    response.addDateHeader("Expires", System.currentTimeMillis() - (1000L * 60L * 60L * 24L * 365L));
    response.addDateHeader("Last-Modified", System.currentTimeMillis());
    response.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
    response.addHeader("Pragma", "no-cache");
%>
<f:view>
<link href="/library/skin/default/tool.css" type="text/css" rel="stylesheet" media="all" />
<script type="text/javascript" language="JavaScript">
doubleDeep = true;
</script>
<ul class="presenceList">
<c:forEach items="${ChatTool.usersInCurrentChannel}" var="user">
	<li><c:out value="${user.displayName}" /></li>
</c:forEach>
</ul>
</f:view>
<%--

JSPs needed
	-permission page
	-add/edit room
	-delete room confirm
	-delete message confirm
	-tool preferences -- select initial view (select room/ specific room)
	-Room

--%>
