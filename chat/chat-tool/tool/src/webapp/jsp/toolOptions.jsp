<f:view>
	<sakai:view title="#{msgs.tool_options_title}">
		<h:form>
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

			<sakai:messages />

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

					<f:verbatim>
						<div class="itemAction">
					</f:verbatim>
					<f:subview id="editLink" rendered="#{channel.canEdit}">
						<h:commandLink action="#{channel.processActionEditRoom}">
							<h:outputText value="#{msgs['gen.edit']}" />
						</h:commandLink>
					</f:subview>
					<f:subview id="deleteLink" rendered="#{channel.canDelete}">
						<h:commandLink action="#{channel.processActionDeleteRoom}">
							<h:outputText value="#{msgs['gen.delete']}" />
						</h:commandLink>
					</f:subview>
					<f:subview id="defaultLink" rendered="#{!channel.chatChannel.contextDefaultChannel && ChatTool.maintainer}">
						<h:commandLink action="#{channel.processActionSetAsDefaultRoom}" 
							title="#{channel.setAsDefaultText}">
							<h:outputText value="#{msgs.set_default}" />
						</h:commandLink>
					</f:subview>
					<f:verbatim>
						</div>
					</f:verbatim>
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

		</h:form>
	</sakai:view>
</f:view>
