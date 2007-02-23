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
	<sakai:view title="">
	<!--[if lte IE 6]>
		<script type="text/javascript" language="JavaScript">
			document.body.style.width='97%'
		</script>
	<![endif]--> 
	
<script type="text/javascript" language="JavaScript">
doubleDeep = true;
// set for the chatscript.js
deleteUrl = '#toolLink($action "doConfirmDeleteMessage")&messageid=';
deleteMsg = "$tlang.getString('list.del')";
</script>

	<script  type="text/javascript" language="JavaScript" src="/library/js/chatscript.js?panel=List"></script>
		#if ($alertMessage)<div class="alertMessage">$tlang.getString("gen.alert") $validator.escapeHtml($alertMessage)</div><div class="clear"></div>#end
		<ul id="chatList">
			<c:forEach items="${ChatTool.roomMessages}" var="message" varStatus="s">
				<li>
					<c:out escapeXml="false" value="<span style=\"color: ${message.color}\">" /><c:out value="${message.owner}" /></span>
					<c:if test="#{message.canRemoveMessage}">
						<a href="#" onclick="location='#toolLink($action "doConfirmDeleteMessage")&messageid=$validator.escapeUrl($item.header.id)'; return false;" title="$tlang.getString('list.del')" >				
						<img src="#imageLink("sakai/delete.gif")" border="0" alt="$tlang.getString('list.del')" /></a>
					</c:if>
					<span class="chatDate">
						<c:if test="${ChatTool.displayDate and ChatTool.displayTime}">
							(a<c:out value="${message.dateTime}" />)
						</c:if>
						<c:if test="${ChatTool.displayDate and not ChatTool.displayTime}">
							(b<c:out value="${message.date}" />)
						</c:if>
						<c:if test="${not ChatTool.displayDate and ChatTool.displayTime}">
							(c<c:out value="${message.time}" />)
						</c:if>
					  
					</span>
					<c:out value="${message.body}" />
				</li>
			</c:forEach>
		</ul>
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
