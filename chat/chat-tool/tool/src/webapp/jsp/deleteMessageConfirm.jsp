<f:view>
	<sakai:view title="#{msgs['delete.delete']}">
		<sakai:view_title value="#{msgs['delete.delete']}"/>
		
		<h:outputText value="#{msgs['delete.sure']}" styleClass="alertMessage" />
		<sakai:messages />
		
		<h:form styleClass="portletBody">
		
			<sakai:panel_edit>
				<h:outputLabel for="owner" value="#{msgs['gen.from']}" />
				<h:outputText id="owner" value="#{ChatTool.currentMessage.owner}" />
	
				<h:outputLabel for="date" value="#{msgs['gen.date']}" />
				<h:outputText id="date" value="#{ChatTool.currentMessage.chatMessage.messageDate}" />
	
				<h:outputLabel for="message" value="#{msgs['gen.mess']}" />
				<h:outputText id="message" value="#{ChatTool.currentMessage.chatMessage.body}" />
	
			</sakai:panel_edit>
			<sakai:button_bar>
				<sakai:button_bar_item id="submit"
					action="#{ChatTool.processActionDeleteMessage}"
					value="#{msgs['gen.delete']}" />
				<sakai:button_bar_item id="reset"
					action="#{ChatTool.processActionDeleteMessageCancel}"
					value="#{msgs['gen.cancel']}" />
			</sakai:button_bar>
		</h:form>
	</sakai:view>
</f:view>
