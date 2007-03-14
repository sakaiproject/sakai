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
