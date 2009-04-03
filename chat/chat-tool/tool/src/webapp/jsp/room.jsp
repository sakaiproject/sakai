<f:view>
   <sakai:view title="#{msgs['custom.chatroom']}">
      <h:form id="topForm">
      <h:inputHidden id="chatidhidden" value="#{ChatTool.currentChatChannelId}" />
      <script type="text/javascript">
	//focus_path = [ "Control", "mainForm:message" ];

</script>
<script type="text/javascript">
	window.frameElement.className='wcwmenu'
</script>

      
         <sakai:tool_bar rendered="#{ChatTool.canManageTool || ChatTool.siteChannelCount > 1 || ChatTool.maintainer}">
            <h:commandLink action="#{ChatTool.processActionListRooms}" rendered="#{ChatTool.canManageTool}">
               <h:outputText value="#{msgs.manage_tool}" />
            </h:commandLink>
            <h:commandLink action="#{ChatTool.processActionListRooms}" rendered="#{ChatTool.siteChannelCount > 1}">
               <h:outputText value="#{msgs.change_room}" />
            </h:commandLink>
            <h:commandLink rendered="#{ChatTool.maintainer}"
                action="#{ChatTool.processActionPermissions}">
                <h:outputText
                    value="#{msgs.permis}" />
            </h:commandLink>
         </sakai:tool_bar>
            	
         <sakai:view_title value="#{ChatTool.viewingChatRoomText}"/>
			<h:panelGrid styleClass="navPanel" columns="1" border="0" columnClasses="viewNav">
				<h:column> 
					<h:outputLabel for="viewOptions"	value="#{msgs.view}" />
					<h:selectOneMenu id="viewOptions" value="#{ChatTool.viewOptions}" 
							onchange="this.form.submit();">
						<f:selectItem itemValue="1" itemLabel="#{msgs.timeOnly}" />
						<f:selectItem itemValue="3" itemLabel="#{msgs.timeAndDate}" />
						<f:selectItem itemValue="2" itemLabel="#{msgs.dateOnly}" />
						<f:selectItem itemValue="0" itemLabel="#{msgs.neitherDateOrTime}" />
						<f:selectItem itemValue="4" itemLabel="#{msgs.uniqueid}" />
					</h:selectOneMenu> 
					<h:outputLabel for="messageOptions"	value="#{msgs['combox.viewfrom']}" />
					<h:selectOneMenu id="messageOptions"
							value="#{ChatTool.messageOptions}" 
							onchange="this.form.submit();">
					   	<f:selectItems value="#{ChatTool.messageOptionsList}" />
					</h:selectOneMenu>
					      				      
				</h:column> 
			</h:panelGrid> 
			<div id="chatListWrapper" class="chatListWrapper">
				<div  class="chatListHeadWrapper">
					<h:outputText value="#{msgs.lay_note}" rendered="#{ChatTool.canRenderAllMessages}" />
					<h:outputFormat value="#{msgs.lay_restricted_note_days}" rendered="#{ChatTool.canRenderDateMessages}" >
						<f:param value="#{ChatTool.currentChannel.chatChannel.timeParam}" />
					</h:outputFormat>
					<h:outputFormat value="#{msgs.lay_restricted_note_messages}" rendered="#{ChatTool.canRenderNumberMessages}" >
						<f:param value="#{ChatTool.currentChannel.chatChannel.numberParam}" />
					</h:outputFormat>
					<h:outputText value="#{msgs.lay_restricted_note_none}" rendered="#{ChatTool.canRenderNoMessages}" />
				</div>
				<sakai:messages />
				<div id="Monitor" class="chatListMonitor">
					<%@include file="roomMonitor.jspf" %>
				</div>
			</div>	
			<div id="chatPresenceWrapper">			
				<div class="chatListHeadWrapper">
					<h:outputText value="#{msgs.lay_user}" />
				</div>
				<iframe
					name="Presence"
					id="Presence"
					title="<h:outputText value="#{msgs.control_panel}" />"
					width="100%"
					frameborder="0"
					marginwidth="0"
					marginheight="0"
					scrolling="no"
					align="right"
					class="wcwmenu"
					src="roomUsers?channel=<h:outputText value="#{ChatTool.currentChatChannelId}" />">
				</iframe>
			</div>
			<iframe
					name="Control"
					id="Control"
					title="<h:outputText value="#{msgs.control_panel}" />"
					width="100%"
					frameborder="0"
					marginwidth="0"
					marginheight="0"
					scrolling="no"
					class="wcwmenu"
					style="clear:both;display:block"
					src="roomControl"
					onLoad="window.scroll(0,0);">
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
