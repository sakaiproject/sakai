<f:view>

   <sakai:view title="#{msgs['custom.chatroom']}">
     <sakai:stylesheet contextBase="/sakai-chat-tool" path="/css/chat.css" />
     <script type="text/javascript">
	focus_path = ["controlPanel:message"];
        var currentChatChannelId = "<h:outputText value="#{ChatTool.currentChatChannelId}" />";
    doubleDeep = true;
</script>
<h:outputText value="#{Portal.latestJQuery}" escape="false"/>
<sakai:script contextBase="/sakai-chat-tool" path="/js/chatscript.js"/>
<script type="text/javascript">
	if ( window.frameElement) window.frameElement.className='wcwmenu';
</script>
<h:form id="topForm">
      <h:inputHidden id="chatidhidden" value="#{ChatTool.currentChatChannelId}" />

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
           <sakai:instruction_message value="#{ChatTool.datesMessage}" rendered="#{ChatTool.datesMessage ne null}" />
           <h:panelGroup styleClass="chat-block">
              <h:panelGroup styleClass="viewoptions-grp">
                  <h:outputLabel for="viewOptions" value="#{msgs.view}" />
                  <h:selectOneMenu id="viewOptions" value="#{ChatTool.viewOptions}" onchange="this.form.submit();">
                      <f:selectItem itemValue="1" itemLabel="#{msgs.timeOnly}" />
                      <f:selectItem itemValue="3" itemLabel="#{msgs.timeAndDate}" />
                      <f:selectItem itemValue="2" itemLabel="#{msgs.dateOnly}" />
                      <f:selectItem itemValue="0" itemLabel="#{msgs.neitherDateOrTime}" />
                      <f:selectItem itemValue="4" itemLabel="#{msgs.uniqueid}" />
                  </h:selectOneMenu> 
              </h:panelGroup>
              <h:panelGroup styleClass="msgoptions-grp">
                  <h:outputLabel for="messageOptions" value="#{msgs['combox.viewfrom']}" />
                  <h:selectOneMenu id="messageOptions" value="#{ChatTool.messageOptions}" onchange="this.form.submit();">
                      <f:selectItems value="#{ChatTool.messageOptionsList}" />
                  </h:selectOneMenu>
              </h:panelGroup>
           </h:panelGroup>
	   <div id="chatLeft">
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
                <div id="chat2_messages_shown_total" class="shown_total"></div>
<script type="text/javascript">
var chat2_totalMessages = <h:outputText value="#{ChatTool.roomMessagesCount}" />;
var chat2_shownMessages = chat2_totalMessages;
var chat2_messageCountTemplate = "<h:outputText value="#{ChatTool.messagesShownTotalText}" />"; // replace *SHOWN* and *TOTAL*
</script>
				<sakai:messages  rendered="#{!empty facesContext.maximumSeverity}" />
				<div id="Monitor" class="chatListMonitor">
					<%@ include file="roomMonitor.jspf" %>
				</div>
			</div>	
		<f:subview id="controlPanel" rendered="#{ChatTool.canPost}">
		<div>
		    <h:outputLabel for="message" value="#{msgs['control.lab']}" style="clear:both;display:block;" />
            <div id="errorSubmit" class="alertMessage" style="display:none">
                <h:outputText value="#{msgs['therewaspro']}" />
            </div>
			<h:inputTextarea id="message" value="#{ChatTool.newMessageText}" rows="3" cols="125" />
			<p class="act">
	          <h:commandButton type="button" id="submit"
	              value="#{msgs['control.post']}"
	              styleClass="active" />
	          <h:commandButton type="button" id="reset"
	              value="#{msgs['control.clear']}" />
	     	</p>
	    </div>
           </div>
		</f:subview>
		</h:form>
			<div id="chatPresenceWrapper" class="information">			
				<h3 class="chatListHeadWrapper">
					<h:outputText value="#{msgs.lay_user}" />
				</h3>
				<div name="presence" id="presence" class="wcwmenu presenceList">
				</div>
			</div>
        <t:div styleClass="messageInformation chat-post-warning" rendered="#{!ChatTool.canPost && ChatTool.datesRestricted}">
            <h:outputText value="#{msgs.custom_date_restricted}" />
            <h:outputText value="#{ChatTool.datesMessage}" />
        </t:div>
	<p style="clear:both;display:block;"></p>
			
	<script type="text/javascript">
		setMainFrameHeight('<h:outputText value="#{ChatTool.framePlacementId}" />');
	</script>

<!--  We can't use the sakai:courier tag because it works from the tool placement id...  and this is now specific to presence in the room  -->
<script type="text/javascript">
updateTime = 10000;
updateUrl = "<h:outputText value="#{ChatTool.courierString}" />";
scheduleUpdate();
</script>
   </sakai:view>
</f:view>
