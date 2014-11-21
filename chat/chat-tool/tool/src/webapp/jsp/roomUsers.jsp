<f:view>
<sakai:view title="">
<sakai:stylesheet contextBase="/sakai-chat-tool" path="/css/chatpresence.css" />

<ul class="presenceList">
<c:forEach items="${ChatTool.usersInCurrentChannel}" var="user">
	<li><c:out value="${user}" escapeXml="false" /></li>
</c:forEach>
</ul>

</sakai:view>
</f:view>
