<f:view>
	<sakai:view>
	
		<sakai:view_title value="#{msgs.edit_channel_title}" rendered="#{!ChatTool.currentChannelEdit.newChannel}" />
		<sakai:view_title value="#{msgs.add_channel_title}" rendered="#{ChatTool.currentChannelEdit.newChannel}" />
		
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
						<h:outputLabel value="#{msgs['custom.showall']}" />
					</h:panelGroup>
	            <h:panelGroup>
	            	<t:radio for="filterType" index="1" />
	            	<h:outputLabel value="#{msgs['custom.showlast']} " />
	            	<h:inputText id="filterParam_last" size="3"
	            		value="#{ChatTool.currentChannelEdit.filterParamLast}" />
	            	<h:outputLabel value="#{msgs['custom.mess']}" />
	            </h:panelGroup>
	            <h:panelGroup>
	            	<t:radio for="filterType" index="2" />
	            	<h:outputLabel value="#{msgs['custom.showpast']} " />
	            	<h:inputText id="filterParam_past" size="3"
	            		value="#{ChatTool.currentChannelEdit.filterParamPast}" />
	            	<h:outputLabel value="#{msgs['custom.days']}" />
	            </h:panelGroup>
	            <h:panelGroup>
	            	<h:selectBooleanCheckbox id="enableUserOverride" value="#{ChatTool.currentChannelEdit.chatChannel.enableUserOverride}" />
						<h:outputLabel for="enableUserOverride" value="#{msgs.channel_enable_override_description}" />
	            </h:panelGroup>
	         </h:panelGrid>
					
				
				</sakai:group_box>

			</sakai:panel_edit>

			<sakai:button_bar>
				<sakai:button_bar_item id="submit"
					action="#{ChatTool.processActionEditRoomSave}"
					value="#{msgs['gen.save']}" />
				<sakai:button_bar_item id="cancel"
					action="#{ChatTool.processActionEditRoomCancel}"
					value="#{msgs['gen.cancel']}" />
			</sakai:button_bar>
		</h:form>
	</sakai:view>
</f:view>
