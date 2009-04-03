<f:view>
<sakai:view title="">

<ul class="presenceList">
<c:forEach items="${ChatTool.usersInCurrentChannel}" var="user">
	<li><c:out value="${user}" escapeXml="false" /></li>
</c:forEach>
</ul>

</sakai:view>
</f:view>