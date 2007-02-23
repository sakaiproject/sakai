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
   <sakai:view title="#{msgs.tool_options_title}">
      <h:form>
	   <sakai:panel_edit>
	
	      <h:outputLabel for="room" id="roomLabel" value="#{msgs.wizard_type}" />
	      <h:panelGroup>
	         <h:selectOneRadio layout="pageDirection" id="room" value="#{ChatTool.currentChatChannelId}">
	            <f:selectItems value="#{ChatTool.chotRoomsSelectItems}"/>
	         </h:selectOneRadio>
	      </h:panelGroup>
	   </sakai:panel_edit>
	   
	   
	   <sakai:button_bar>
		   <sakai:button_bar_item id="enterRoom" value="#{msgs.enter_the_chat_room}"
		      action="#{ChatTool.processActionChangeChannel}" />
		   <sakai:button_bar_item id="cancel" value="#{msgs.cancel_wizard}" action="#{ChatTool.processActionCancelChangeChannel}"
		      immediate="true" rendered="#{ChatTool.currentChannel != null}"/>
	   </sakai:button_bar>
	   
      </h:form>
   </sakai:view>
</f:view>
