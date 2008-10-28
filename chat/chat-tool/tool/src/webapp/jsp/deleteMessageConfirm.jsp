<f:view>
	<sakai:view title="#{msgs['delete.delete']}">
		<sakai:view_title value="#{msgs['delete.delete']}"/>
		
		<c:if test="${not empty ChatTool.currentMessage.chatMessage.body}">
			<h:outputText id="deletemessageconfirm" value="#{msgs['delete.sure']}" styleClass="alertMessage" />
		</c:if>		

		<c:if test="${ empty ChatTool.currentMessage.chatMessage.body}">
	   		<h:outputText id="deletemessagecancel" value="#{msgs['delete.wrong']}" styleClass="alertMessage" />
		</c:if> 
		<sakai:messages />
		
		<h:form styleClass="portletBody">
		
		<c:if test="${not empty ChatTool.currentMessage.chatMessage.body}">		
			<sakai:panel_edit>
				<h:outputLabel for="owner" value="#{msgs['gen.from']}" />
				<h:outputText id="owner" value="#{ChatTool.currentMessage.owner}" /><br>
	
				<h:outputLabel for="date" value="#{msgs['gen.date']}" />
				<h:outputText id="date" value="#{ChatTool.currentMessage.chatMessage.messageDate}" /><br>
	
				<h:outputLabel for="message" value="#{msgs['gen.mess']}" />
				<h:outputText id="message" value="#{ChatTool.currentMessage.unformattedBody}" />
	
			</sakai:panel_edit>
			<sakai:button_bar>
				<sakai:button_bar_item id="submit"
					action="#{ChatTool.processActionDeleteMessage}"
					value="#{msgs['gen.delete']}" />
				<sakai:button_bar_item id="reset"
					action="#{ChatTool.processActionDeleteMessageCancel}"
					value="#{msgs['gen.cancel']}" />
			</sakai:button_bar>
		</c:if>		
			
		<c:if test="${ empty ChatTool.currentMessage.chatMessage.body}">     
			<sakai:button_bar>				
				<sakai:button_bar_item id="reset2"
					action="#{ChatTool.processActionDeleteMessageCancel}"
					value="#{msgs['gen.cancel']}"/>
			</sakai:button_bar>
        </c:if> 
			
		</h:form>
	</sakai:view>
</f:view>
