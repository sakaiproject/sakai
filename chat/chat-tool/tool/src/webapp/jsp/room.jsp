<f:view>
    <sakai:view title="#{msgs['custom.chatroom']}">
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
                    <h:outputText value="#{msgs.permis}" />
                </h:commandLink>
            </sakai:tool_bar>
            <div class="panel panel-chat panel-default">
                <div class="panel-heading">
                    <sakai:instruction_message value="#{ChatTool.datesMessage}" rendered="#{ChatTool.datesMessage ne null}" />
                    <sakai:view_title value="#{ChatTool.viewingChatRoomText}"/>
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
                </div>

                <ul class="nav nav-tabs" role="tabpanel">
                    <li class="active">
                        <a href="#chatListWrapperCont" role="tab" data-toggle="tab"><span><h:outputText value="#{msgs.messages}" /></span></a>
                    </li>
                    <li>
                        <a href="#chatPresenceWrapper" role="tab" data-toggle="tab"><span><h:outputText value="#{msgs.lay_user}" /></span></a>
                    </li>
                </ul>
                <div class="panel-body panel-body-chat tab-content" id="chatLeft">
                    <div id="chatListWrapperCont" class="chatListWrapperCont tab-pane active">
                        <div id="chatListWrapper" class="chatListWrapper">
                            <div class="chatListHeadWrapper">
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
                                var chat2_messagesUnreadedTemplate = "<h:outputText value="#{ChatTool.unreadedMessagesText}" />"; // replace *SHOWN* and *TOTAL*
                                var unreadedMessages = 0;
                            </script>
                            <sakai:messages  rendered="#{!empty facesContext.maximumSeverity}" />
                            <div id="Monitor" class="chatListMonitor">
                                <%@ include file="roomMonitor.jspf" %>
                            </div>
                        </div>
                        <div class="scrollBottom hide" title="<h:outputText value="#{ChatTool.unreadedMessagesText}" />">
                            <span class="newMessages" aria-label="<h:outputText value='#{msgs.unreaded}' />"></span>
                            <span class="scrollIcon fa fa-angle-down fa-2x" aria-label="<h:outputText value='#{msgs.unreaded}' />"></span>
                        </div>
                    </div>
                    <div id="chatPresenceWrapper" class="chatListOnline tab-pane">
                        <h3 class="chatListHeadWrapper">
                            <h:outputText value="#{msgs.lay_user}" />
                        </h3>
                        <ul name="presence" id="presence" class="wcwmenu presenceList">
                        </ul>
                    </div>
                </div>    
                <div class="panel-footer">
                    <f:subview id="controlPanel" rendered="#{ChatTool.canPost}">
                        <div>
                            <div id="errorSubmit" class="alertMessage" style="display:none">
                                <h:outputText value="#{msgs['therewaspro']}" />
                            </div>
                            <c:out value="<textarea id='topForm:controlPanel:message' value='" escapeXml="false" /><h:outputText value="#{ChatTool.newMessageText}" /><c:out value="' placeholder='" escapeXml="false" /><h:outputText value="#{msgs['control.lab']}" /><c:out value="' rows='3' cols='125'></textarea>" escapeXml="false" /> 
                            <div class="act">
                                <h:commandButton type="button" id="submit" value="#{msgs['control.post']}" styleClass="active" />
                                <h:commandButton type="button" id="reset" value="#{msgs['control.clear']}" />
                            </div>
                        </div>
                    </f:subview>
                </div>
            </div>
        </h:form>

        <t:div styleClass="messageInformation chat-post-warning" rendered="#{!ChatTool.canPost && ChatTool.datesRestricted}">
            <h:outputText value="#{msgs.custom_date_restricted}" />
            <h:outputText value="#{ChatTool.datesMessage}" />
        </t:div>
        <p style="clear:both;display:block;"></p>

        <div class="modal fade" tabindex="-1" role="dialog" id="removemodal">
            <div class="modal-dialog" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <span class="close" data-dismiss="modal" aria-label="<h:outputText value="#{msgs['gen.cancel']}" />" aria-hidden="true">&times;</span>
                        <h4 class="modal-title"><h:outputText value="#{msgs['delete.delete']}" /></h4>
                    </div>
                    <div class="modal-body">
                        <p><h:outputText value="#{msgs['delete.sure']}" /></p>
                        <c:out value="<table>" escapeXml="false" />
                            <c:out value="<tr>" escapeXml="false" />
                                <c:out value="<th>${msgs['gen.from']}</th>" escapeXml="false" />
                                <c:out value="<td id='owner'></td>" escapeXml="false" />
                            <c:out value="</tr>" escapeXml="false" />
                            <c:out value="<tr>" escapeXml="false" />
                                <c:out value="<th>${msgs['gen.date']}</th>" escapeXml="false" />
                                <c:out value="<td id='date'></td>" escapeXml="false" />
                            <c:out value="</tr>" escapeXml="false" />
                            <c:out value="<tr>" escapeXml="false" />
                                <c:out value="<th>${msgs['gen.mess']}</th>" escapeXml="false" />
                                <c:out value="<td id='message'></td>" escapeXml="false" />
                            <c:out value="</tr>" escapeXml="false" />
                        <c:out value="</table>" escapeXml="false" />
                    </div>
                    <div class="modal-footer">
                        <button type="button" id="cancelButton" class="btn btn-default" data-dismiss="modal"><h:outputText value="#{msgs['gen.cancel']}" /></button>
                        <button type="button" id="deleteButton" class="btn btn-primary"><h:outputText value="#{msgs['gen.delete']}" /></button>
                    </div>
                </div>
            </div>
        </div>

        <script type="text/javascript">
            chatscript.currentChatChannelId = "<h:outputText value="#{ChatTool.currentChatChannelId}" />";
            chatscript.pollInterval = "<h:outputText value="#{ChatTool.pollInterval}" />";
            chatscript.init();
        </script>

        <script type="text/javascript">
            setMainFrameHeight('<h:outputText value="#{ChatTool.framePlacementId}" />');
        </script>
    </sakai:view>
</f:view>
