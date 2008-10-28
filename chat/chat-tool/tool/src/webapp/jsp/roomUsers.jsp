<f:view>
<sakai:view title="">

<script type="text/javascript" language="JavaScript">
doubleDeep = true;
</script>
<script type="text/javascript" language="JavaScript">
	try { parent.updateNow(); } catch (error) {}
</script>
<script type="text/javascript" language="JavaScript" src="/library/js/headscripts.js"></script>

	<sakai:script contextBase="/library" path="/js/jquery.js" />
	<sakai:script contextBase="/sakai-chat-tool" path="/js/chatscript.js"/>
	

<ul class="presenceList">
<c:forEach items="${ChatTool.usersInCurrentChannel}" var="user">
	<li><c:out value="${user}" escapeXml="false" /></li>
</c:forEach>
</ul>

<!--  We can't use the sakai:courier tag because it works from the tool placement id...  and this is now specific to presence in the room  -->
<script type="text/javascript" language="JavaScript">
updateTime = 10000;
updateUrl = "<h:outputText value="#{ChatTool.courierPresenceString}" />";
scheduleUpdate();
</script>

</sakai:view>
</f:view>