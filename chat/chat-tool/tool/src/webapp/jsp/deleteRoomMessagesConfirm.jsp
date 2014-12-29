<f:view>
	<sakai:view title="#{msgs.delete_room_confirm_title}">
	<sakai:view_title value="#{msgs.delete_room_messages_confirm_title}" />
	
	<h:outputText value="#{msgs.delete_room_messages_confirm_alert}" styleClass="alertMessage" />
	<sakai:messages rendered="#{!empty facesContext.maximumSeverity}" />
	
	<h:form styleClass="portletBody">
	
	<sakai:panel_edit>
		<h:outputLabel for="title" value="#{msgs.channel_title_colon}" />
		<h:outputText id="title" value="#{ChatTool.currentChannelEdit.chatChannel.title}" />

		<h:outputLabel for="desc" value="#{msgs.channel_description_colon}" />
		<h:outputText id="desc" value="#{ChatTool.currentChannelEdit.chatChannel.description}" />

	</sakai:panel_edit>
	
	<sakai:button_bar>
	    <sakai:button_bar_item id="delete"
	        action="#{ChatTool.processActionDeleteRoomMessages}"
	        value="#{msgs['gen.delete']}" />
	    <sakai:button_bar_item id="cancel"
	        action="#{ChatTool.processActionDeleteRoomMessagesCancel}"
	        value="#{msgs['gen.cancel']}" />
	</sakai:button_bar>
	
	</h:form>
	</sakai:view>
</f:view>