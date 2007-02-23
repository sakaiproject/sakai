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
   <sakai:view title="#{msgs.chat_room_title}">
      <h:form>
         <sakai:tool_bar>
            <h:commandLink action="toolOptions" rendered="#{ChatTool.canManageTool}">
               <h:outputText value="#{msgs.manage_tool}" />
            </h:commandLink>
            <h:commandLink action="selectRoom" rendered="#{ChatTool.toolChannelCount > 1}">
               <h:outputText value="#{msgs.change_room}" />
            </h:commandLink>
            <h:commandLink rendered="#{ChatTool.maintainer}"
                action="#{ChatTool.processActionPermissions}">
                <h:outputText
                    value="#{msgs.permissions_link}" />
            </h:commandLink>
         </sakai:tool_bar>
            	<h:outputText value="#{msgs.view}" />
	         <h:selectOneMenu id="viewOptions" value="#{ChatTool.viewOptions}" onchange="this.form.submit();">
	            <f:selectItem itemValue="1" itemLabel="#{msgs.timeOnly}" />
	            <f:selectItem itemValue="3" itemLabel="#{msgs.timeAndDate}" />
	            <f:selectItem itemValue="2" itemLabel="#{msgs.dateOnly}" />
	            <f:selectItem itemValue="0" itemLabel="#{msgs.neitherDateOrTime}" />
	         </h:selectOneMenu>
	         <h:selectOneMenu id="msgPastCutoff" value="#{ChatTool.msgDateCutoff}">
	            <f:selectItem itemValue="-1" itemLabel="#{msgs.allMessages}" />
	            <f:selectItems value="#{ChatTool.chotRoomsSelectItems}"/>
	         </h:selectOneMenu>
	     <div id="chatListWrapper">
				<div  class="chatListHeadWrapper">
					<h:outputText value="#{msgs.lay_note}" />
				</div>
				<iframe
					name="Monitor"
					id="Monitor"
					title="$panel-monitor"
					width="100%"
					height="300"
					frameborder="0"
					marginwidth="0"
					marginheight="0"
					scrolling="auto"
					src="roomMonitor">
				</iframe>
			</div>	
			<div id="chatPresenceWrapper">			
				<div class="chatListHeadWrapper">
					<h:outputText value="#{msgs.lay_user}" />
				</div>
				<iframe 
					name="Presence"
					id="Presence"
					title="$panel-presence"
					width="100%"
					height="300"
					frameborder="0"
					marginwidth="0"
					marginheight="0"
					scrolling="no"
					align="right"
					src="roomUsers">
				</iframe>
			</div>	
			<iframe
					name="Control"
					id="Control"
					title="$panel-control"
					width="100%"
					height="120"
					frameborder="0"
					marginwidth="0"
					marginheight="0"
					scrolling="no"
					style="clear:both;display:block"
					src="roomControl">
				</iframe>
	         
      </h:form>
<!--  We can't use the sakai:courier tag because it works from the tool placement id...  and this is now specific to presence in the room  -->
<script type="text/javascript" language="JavaScript">
updateTime = 10000;
updateUrl = "<h:outputText value="#{ChatTool.courierString}" />";
scheduleUpdate();
</script>
   </sakai:view>
</f:view>
