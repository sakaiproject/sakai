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
	<sakai:view_title value="#{msgs.delete_room_confirm_title}" indent="1" />
	
	<sakai:instruction_message value="#{msgs.delete_room_confirm_instructions}" />
	
	<sakai:button_bar>
	    <sakai:button_bar_item
	        action="#{ChatTool.processActionDeleteRoom}"
	        value="#{msgs.delete_room}" />
	    <sakai:button_bar_item
	        action="#{ChatTool.processActionDeleteRoomCancel}"
	        value="#{msgs.cancel_delete}" />
	</sakai:button_bar>
</f:view>
