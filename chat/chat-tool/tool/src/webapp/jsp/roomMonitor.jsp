<f:view>
	<sakai:view title="#{msgs.monitor_panel}">
	<h:form id="monitorForm">
	<!--[if lte IE 6]>
		<script type="text/javascript" language="JavaScript">
			document.body.style.width='97%'
		</script>
	<![endif]--> 
	
<script type="text/javascript" language="JavaScript">
doubleDeep = true;
// set for the chatscript.js
//TODO not sure how to get the deleteUrl to work properly since JSF 
// doesn't use the href.location???

deleteMsg = "<h:outputText value="#{ChatTool.deleteText}" />";
</script>

	<script  type="text/javascript" language="JavaScript" src="/sakai-chat-tool/js/chatscript.js"></script>
	
	<sakai:messages />
	
	<t:dataList id="chatList" styleClass="chatList" var="message"
				value="#{ChatTool.roomMessages}" layout="unorderedList">

				<h:outputText value="#{message.owner} "
					style="color: #{message.color}" />

				<h:commandLink id="deleteMessage" title="#{ChatTool.deleteText}"
					action="#{message.processActionDeleteMessage}"
					rendered="#{message.canRemoveMessage}">
					<h:graphicImage id="deleteIcon" alt="#{ChatTool.deleteText}"
						value="#{ChatTool.serverUrl}/library/image/sakai/delete.gif" />
				</h:commandLink>

				<h:outputText
					rendered="#{ChatTool.displayDate and ChatTool.displayTime}"
					value=" (#{message.dateTime}) " styleClass="chatDate" />
				<h:outputText
					rendered="#{ChatTool.displayDate and not ChatTool.displayTime}"
					value=" (#{message.date}) " styleClass="chatDate" />
				<h:outputText
					rendered="#{not ChatTool.displayDate and ChatTool.displayTime}"
					value=" (#{message.time}) " styleClass="chatDate" />

				<h:outputText value="#{message.chatMessage.body}" />

			</t:dataList>


			<script type="text/javascript" language="JavaScript">
		var Colors = [ <c:forEach items="${ChatTool.colorMapper.colors}" var="color">"<c:out value="${color}" />", </c:forEach> ""];
		Colors.pop();
		
		var numColors = Colors.length;
		var nextColor = <c:out value="${ChatTool.colorMapper.next}" />;
	
		var ColorMap = new Object();
<c:forEach items="${ChatTool.colorMapper.mappingList}" var="keyvalue">
		ColorMap["<c:out value="${keyvalue.key}" />"] = "<c:out value="${keyvalue.value}" />";
</c:forEach>
	
		var display_date = <c:out value="${ChatTool.displayDate}" />;
		var display_time = <c:out value="${ChatTool.displayTime}" />;
		var sound_alert = <c:out value="${ChatTool.soundAlert}" />;
	
		var docbottom = 100000;
		
		// some of these can be eliminated after verifying that 
		// the browsers that are working now are not using them
		if(document.body.offsetHeight)
		{
			// MAC_IE MAC_MZ WIN_IE WIN_MZ WIN_NN
			docbottom = document.body.offsetHeight;
		}
		else if(document.offsetHeight)
		{
			docbottom = document.offsetHeight;
		}
		else if(document.height)
		{
			docbottom = document.height;
		}
		else if(document.body.scrollHeight)
		{
			docbottom = document.body.scrollHeight;
		}
		else if(document.clientHeight)
		{
			docbottom = document.clientHeight;
		}
		else if(document.clientHeight)
		{
			docbottom = document.clientHeight;
		}
		window.scrollTo(0, docbottom);
	</script>
	</h:form>
	</sakai:view>
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
