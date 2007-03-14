<f:view>
	<sakai:view>
	
		<sakai:view_title value="#{msgs.edit_channel_title}" />
		
		<sakai:messages />
		<h:form id="editRoomForm">

			<sakai:panel_edit>
			
				<h:outputLabel for="title" value="#{msgs.channel_title}" />
				<h:inputText id="title" value="#{ChatTool.currentChannelEdit.chatChannel.title}" />

				<h:outputLabel for="desc" value="#{msgs.channel_description}" />
				<h:inputTextarea id="desc"
					value="#{ChatTool.currentChannelEdit.chatChannel.description}" />

				<t:selectOneRadio value="#{ChatTool.currentChannelEdit.chatChannel.filterType}"
						id="filterType" layout="spread">
					<f:selectItem itemValue="SelectAllMessages" itemLabel="" />
	            <f:selectItem itemValue="SelectMessagesByNumber" itemLabel="" />
	            <f:selectItem itemValue="SelectMessagesByTime" itemLabel="" />
	            
				</t:selectOneRadio>


				<sakai:group_box title="#{msgs.recent_chat_heading}">
				
				<h:panelGrid columns="1">
				
				
	            <h:panelGroup>
	            	<t:radio for="filterType" index="0" />
						<h:outputLabel value="#{ChatTool.showAllText}" />
					</h:panelGroup>
	            <h:panelGroup>
	            	<t:radio for="filterType" index="1" />
	            	<h:outputLabel value="#{ChatTool.showLastText} " />
	            	<h:inputText id="filterParam_last" size="3"
	            		value="#{ChatTool.currentChannelEdit.filterParamLast}" />
	            	<h:outputLabel value="#{ChatTool.showMessagesText}" />
	            </h:panelGroup>
	            <h:panelGroup>
	            	<t:radio for="filterType" index="2" />
	            	<h:outputLabel value="#{ChatTool.showPastText} " />
	            	<h:inputText id="filterParam_past" size="3"
	            		value="#{ChatTool.currentChannelEdit.filterParamPast}" />
	            	<h:outputLabel value="#{ChatTool.showDaysText}" />
	            </h:panelGroup>
	         </h:panelGrid>
					
				
				</sakai:group_box>

			</sakai:panel_edit>

			<sakai:button_bar>
				<sakai:button_bar_item id="submit"
					action="#{ChatTool.processActionEditRoomSave}"
					value="#{ChatTool.saveButtonText}" />
				<sakai:button_bar_item id="cancel"
					action="#{ChatTool.processActionEditRoomCancel}"
					value="#{ChatTool.cancelButtonText}" />
			</sakai:button_bar>
		</h:form>
	</sakai:view>
</f:view>
