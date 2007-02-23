<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%
    response.setContentType("text/html; charset=UTF-8");
    response.addDateHeader("Expires", System.currentTimeMillis() - (1000L * 60L * 60L * 24L * 365L));
    response.addDateHeader("Last-Modified", System.currentTimeMillis());
    response.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
    response.addHeader("Pragma", "no-cache");
%>
<f:view>
    <sakai:view title="#{ChatTool.enterTool}">
		<!-- This page doesn't really matter as it will redirect the user to the "select a room" page or the "chat room" page  -->
    </sakai:view>
</f:view>
<%--

JSPs needed
	-test permissions
	-add/edit room
	-delete room confirm
	-make sure interface interacts correctly when deleting a room. (no crash), maybe message to interface saying that
		the room has been deleted.
	-delete message confirm
	-tool preferences -- select initial view (select room/ specific room)
	-Room
		-hit enter to submit a new message
		-having a return in the message causes it to not show up in the monitor
		

	-messageAdaptorComponents has a link to the ChatService to allow for searching of the chat tool messages
		That had to be knocked out because the ChatService isn't being used any more
	-chat synoptic is broken now
	
--%>
