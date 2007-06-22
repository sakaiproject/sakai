<f:view>
	<sakai:view>
	
		<sakai:view_title value="#{msgs.edit_channel_title}" rendered="#{!ChatTool.currentChannelEdit.newChannel}" />
		<sakai:view_title value="#{msgs.add_channel_title}" rendered="#{ChatTool.currentChannelEdit.newChannel}" />
		
		<sakai:messages />
		<h:form id="editRoomForm">

			<h:panelGrid columns="1" styleClass="jsfFormTable" cellpadding="0"
				summary="layout">
				<h:panelGroup styleClass="shorttext">
					<h:outputText value="*" styleClass="reqStar" />
					<h:outputLabel id="titleLabel" for="title"
						value="#{msgs.channel_title}" />
					<h:inputText id="title"
						value="#{ChatTool.currentChannelEdit.chatChannel.title}">
						<f:validateLength minimum="1" maximum="64" />
					</h:inputText>
					<h:message for="title" styleClass="validationEmbedded" />
				</h:panelGroup>
				<h:panelGroup styleClass="longtext"
					style="padding:0;display:block;margin:0">
					<h:outputLabel id="descLabel" for="desc"
						value="#{msgs.channel_description}" styleClass="block" />
					<h:inputTextarea id="desc" cols="60" rows="6"
						value="#{ChatTool.currentChannelEdit.chatChannel.description}">
						<f:validateLength minimum="0" maximum="255" />
					</h:inputTextarea>
					<h:message for="desc" styleClass="validationEmbedded" />
				</h:panelGroup>
				<h:panelGroup>
					<t:selectOneRadio
						value="#{ChatTool.currentChannelEdit.chatChannel.filterType}"
						id="filterType" layout="spread">
						<f:selectItem id="typeAll" itemValue="SelectAllMessages"
							itemLabel="" />
						<f:selectItem id="typeNumber" itemValue="SelectMessagesByNumber"
							itemLabel="" />
						<f:selectItem id="typeTime" itemValue="SelectMessagesByTime"
							itemLabel="" />
						<f:selectItem id="typeNone" itemValue="SelectNoneMessages"
							itemLabel="" />

					</t:selectOneRadio>
				</h:panelGroup>

				<sakai:group_box title="#{msgs.recent_chat_heading}">

					<h:panelGrid columns="1" styleClass="jsfFormTable indnt1" cellpadding="0"
						summary="layout">

						<h:panelGroup>
							<t:radio for="filterType" index="3" />
							<h:outputLabel value="#{msgs['shownone']}" />
							<h:inputHidden id="filterParam_none" 
								value="#{ChatTool.currentChannelEdit.filterParamNone}" />
						</h:panelGroup>
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
							<h:selectBooleanCheckbox id="enableUserOverride"
								value="#{ChatTool.currentChannelEdit.chatChannel.enableUserOverride}" />
							<h:outputLabel for="enableUserOverride"
								value="#{msgs.channel_enable_override_description}" />
						</h:panelGroup>
					</h:panelGrid>

				</sakai:group_box>

			</h:panelGrid>

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
