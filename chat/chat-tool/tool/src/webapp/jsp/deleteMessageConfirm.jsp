<f:view>
	<sakai:view title="#{ChatTool.deletePageTitle}">
		<sakai:view_title value="#{ChatTool.deletePageTitle}"/>
		
		<h:outputText value="#{ChatTool.deletePageConfirmAlert}" styleClass="alertMessage" />
		
		<h:form styleClass="portletBody">
		
			<sakai:panel_edit>
				<h:outputLabel for="owner" value="#{ChatTool.fromLabelText}" />
				<h:outputText id="owner" value="#{ChatTool.currentMessage.owner}" />
	
				<h:outputLabel for="date" value="#{ChatTool.dateLabelText}" />
				<h:outputText id="date" value="#{ChatTool.currentMessage.chatMessage.messageDate}" />
	
				<h:outputLabel for="message" value="#{ChatTool.messageLabelText}" />
				<h:outputText id="message" value="#{ChatTool.currentMessage.chatMessage.body}" />
	
			</sakai:panel_edit>
			<sakai:button_bar>
				<sakai:button_bar_item id="submit"
					action="#{ChatTool.processActionDeleteMessage}"
					value="#{ChatTool.deleteButtonText}" />
				<sakai:button_bar_item id="reset"
					action="#{ChatTool.processActionDeleteMessageCancel}"
					value="#{ChatTool.cancelButtonText}" />
			</sakai:button_bar>
		</h:form>
	</sakai:view>
</f:view>
