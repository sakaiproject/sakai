<f:view>
<c:forEach items="${ChatTool.usersInCurrentChannel}" var="user">
	<li><c:out value="${user}" escapeXml="false" /></li>
</c:forEach>
</f:view>
