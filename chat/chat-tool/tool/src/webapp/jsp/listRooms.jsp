<f:view>
	<sakai:view title="#{msgs.room_list_title}">
		<h:form styleClass="list-rooms">
			<sakai:tool_bar>
				<h:commandLink id="addRoom" rendered="#{ChatTool.canCreateChannel}"
					action="#{ChatTool.processActionAddRoom}" immediate="true">
					<h:outputText value="#{msgs.add_room}" />
				</h:commandLink>
				<h:commandLink rendered="#{ChatTool.maintainer}"
                action="#{ChatTool.processActionPermissions}">
                <h:outputText
                    value="#{msgs.permis}" />
            </h:commandLink>
			</sakai:tool_bar>
			
			<sakai:view_title value="#{msgs.room_list_title}"/>  	

			<sakai:messages rendered="#{!empty facesContext.maximumSeverity}" />

			<h:dataTable value="#{ChatTool.chatChannels}" var="channel"
				styleClass="lines listHier" headerClass="exclude">
				<h:column>
					<f:facet name="header">
						<h:outputText value="#{msgs['custom.chatroom']}" />
					</f:facet>

					<f:subview id="enterRoomLink" rendered="#{channel.canRead}">
						<h:commandLink action="#{channel.processActionEnterRoom}" title="#{channel.enterChatText}">
							<h:outputText value="#{channel.chatChannel.title}" />
						</h:commandLink>
					</f:subview>
					<h:outputText value="#{channel.chatChannel.title}"
						rendered="#{!channel.canRead}" />

					<sakai:separatedList id="channelActionList" separator=" | " styleClass="itemAction">
					<f:subview id="editLink" rendered="#{channel.canEdit}">
						<h:commandLink action="#{channel.processActionEditRoom}">
							<h:outputText styleClass="skip" value=" #{channel.chatChannel.title}: " /> <h:outputText value="#{msgs['gen.edit']} " />
						</h:commandLink>
					</f:subview>
					<f:subview id="deleteLink" rendered="#{channel.canDelete}">
						<h:commandLink action="#{channel.processActionDeleteRoom}">
							<h:outputText  styleClass="skip" value=" #{channel.chatChannel.title}: " /> <h:outputText value="#{msgs['gen.delete']}" />
						</h:commandLink>
					</f:subview>
					<f:subview id="clearMessages" rendered="#{channel.canDeleteMessages && channel.numberChannelMessages > 0}">
						<h:commandLink action="#{channel.processActionDeleteRoomMessages}" 
							title="#{msgs.delete_room_messages}">
							<h:outputText  styleClass="skip" value=" #{channel.chatChannel.title}: " /> <h:outputText value="#{msgs.delete_room_messages}" />
						</h:commandLink>
					</f:subview>
					<f:subview id="defaultLink" rendered="#{(!channel.chatChannel.placementDefaultChannel || ChatTool.toolString != channel.chatChannel.placement) && ChatTool.maintainer}">
						<h:commandLink action="#{channel.processActionSetAsDefaultRoom}" 
							title="#{channel.setAsDefaultText}">
							<h:outputText  styleClass="skip" value=" #{channel.chatChannel.title}: " /> <h:outputText value="#{msgs.set_default}" />
						</h:commandLink>
					</f:subview>
					</sakai:separatedList>
				</h:column>
				<h:column>
					<f:facet name="header">
						<h:outputText value="#{msgs.channel_creation_date}" />
					</f:facet>
					<h:outputText value="#{channel.chatChannel.creationDate}" />
				</h:column>
				<h:column>
					<f:facet name="header">
						<h:outputText value="#{msgs.channel_description}" />
					</f:facet>
					<h:outputText value="#{channel.chatChannel.description}" />
				</h:column>

			</h:dataTable>
			
			<sakai:button_bar>
				<sakai:button_bar_item id="back"
					action="#{ChatTool.processActionBackToRoom}"
					value="#{msgs.back_to_room}" />
			</sakai:button_bar>

		</h:form>
	</sakai:view>
</f:view>
