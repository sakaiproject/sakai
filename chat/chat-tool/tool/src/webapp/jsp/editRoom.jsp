<f:view>
    

	<sakai:view>
      <script type="text/javascript">includeLatestJQuery("chatEditRoom");</script>
      <script type="text/javascript" src="/library/js/lang-datepicker/lang-datepicker.js"></script>

      <script type="text/javascript">
          $(document).ready(function() {
             localDatePicker({
                input: '#editRoomForm\\:startDate',
                useTime: 0,
                parseFormat: 'YYYY-MM-DD',
                allowEmptyDate: true,
                val: '<h:outputText value="#{ChatTool.currentChannelEdit.startDate}"><f:convertDateTime pattern="yyyy-MM-dd"/></h:outputText>',
                ashidden: { iso8601: 'chatStartDateISO8601' }
            });

             localDatePicker({
                input: '#editRoomForm\\:endDate',
                useTime: 0,
                parseFormat: 'YYYY-MM-DD',
                allowEmptyDate: true,
                val: '<h:outputText value="#{ChatTool.currentChannelEdit.endDate}"><f:convertDateTime pattern="yyyy-MM-dd"/></h:outputText>',
                ashidden: { iso8601: 'chatEndDateISO8601' }
            });
          });
        </script>
		<sakai:view_title value="#{msgs.edit_channel_title}" rendered="#{!ChatTool.currentChannelEdit.newChannel}" />
		<sakai:view_title value="#{msgs.add_channel_title}" rendered="#{ChatTool.currentChannelEdit.newChannel}" />
		<h:messages globalOnly="false" styleClass="alertMessage" 
			showDetail="true" showSummary="false" rendered="#{not empty facesContext.maximumSeverity}" />
		<h:form id="editRoomForm" styleClass="edit-room">

			<h:panelGrid columns="1" styleClass="jsfFormTable" cellpadding="0">
				<h:panelGroup styleClass="shorttext">
					<h:outputText value="*" styleClass="reqStar" />
					<h:outputLabel id="titleLabel" for="title"
						value="#{msgs.channel_title}" />
					<h:inputText id="title"
						value="#{ChatTool.currentChannelEdit.chatChannel.title}" />
					<h:message for="title" styleClass="validationEmbedded" />
				</h:panelGroup>
				<h:panelGroup styleClass="longtext"
					style="padding:0;display:block;margin:0">
					<h:outputLabel id="descLabel" for="desc"
						value="#{msgs.channel_description}" styleClass="block" />
					<h:inputTextarea id="desc" cols="60" rows="6"
						value="#{ChatTool.currentChannelEdit.chatChannel.description}" />
					<h:message for="desc" styleClass="validationEmbedded" />
				</h:panelGroup>
				<h:panelGroup>
					<t:selectOneRadio
						value="#{ChatTool.currentChannelEdit.chatChannel.filterType}"
						id="filterType" layout="spread">
						<f:selectItem id="typeAll" itemValue="SelectAllMessages"
							itemLabel="#{msgs['custom.showall']}" />
						<f:selectItem id="typeNumber" itemValue="SelectMessagesByNumber"
							itemLabel="#{msgs['custom.showlast']} " />
						<f:selectItem id="typeTime" itemValue="SelectMessagesByTime"
							itemLabel="#{msgs['custom.showpast']} " />
						<f:selectItem id="typeNone" itemValue="SelectNoneMessages"
							itemLabel="#{msgs['shownone']}" />

					</t:selectOneRadio>
				</h:panelGroup>

				<sakai:group_box title="#{msgs.recent_chat_heading}">

					<h:panelGrid columns="1" styleClass="jsfFormTable indnt1" cellpadding="0">
						<h:panelGroup>
							<t:radio for="filterType" index="3" />
							<h:inputHidden id="filterParam_none" 
								value="#{ChatTool.currentChannelEdit.filterParamNone}" />
						</h:panelGroup>
						<h:panelGroup>
							<t:radio for="filterType" index="0" />
						</h:panelGroup>
						<h:panelGroup>
							<t:radio for="filterType" index="1" />
							<h:inputText id="filterParam_last" size="4"
								value="#{ChatTool.currentChannelEdit.filterParamLast}" />
							<h:outputLabel value="#{msgs['custom.mess']}" for="filterParam_last"/>
							<h:message for="filterParam_last" styleClass="validationEmbedded" />
						</h:panelGroup>
						<h:panelGroup>
							<t:radio for="filterType" index="2" />
							<h:inputText id="filterParam_past" size="3"
								value="#{ChatTool.currentChannelEdit.filterParamPast}"/>
							<h:outputLabel value="#{msgs['custom.days']}" for="filterParam_past"/>
						</h:panelGroup>
						<h:panelGroup>
							<h:selectBooleanCheckbox id="enableUserOverride"
								value="#{ChatTool.currentChannelEdit.chatChannel.enableUserOverride}" />
							<h:outputLabel for="enableUserOverride"
								value="#{msgs.channel_enable_override_description}" />
						</h:panelGroup>
					</h:panelGrid>

				</sakai:group_box>

        <sakai:group_box title="#{msgs.custom_date_heading}">
          <t:div styleClass="chat-date-controls indnt1">
            <h:panelGroup styleClass="chat-date-instructions">
              <sakai:instruction_message value="#{msgs.custom_date_instructions}" />
            </h:panelGroup>
            <h:panelGroup styleClass="chat-select-date longtext indnt1">
              <h:outputLabel value="#{msgs.custom_date_start}" for="startDate" styleClass="chat-date-label"/>
              <h:inputText value="#{ChatTool.currentChannelEdit.startDateString}" size="20" id="startDate"/>
              <h:message for="startDate" styleClass="alertMessageInline" />
            </h:panelGroup>
            <h:panelGroup styleClass="chat-select-date longtext indnt1">
              <h:outputLabel value="#{msgs.custom_date_end}" for="endDate" styleClass="chat-date-label"/>
              <h:inputText value="#{ChatTool.currentChannelEdit.endDateString}" size="20" id="endDate"/>
              <h:message for="endDate" styleClass="alertMessageInline" />
            </h:panelGroup>
          </t:div>
        </sakai:group_box>

			</h:panelGrid>

			<sakai:button_bar>
				<sakai:button_bar_item id="submit"
					action="#{ChatTool.processActionEditRoomSave}"
					value="#{msgs['gen.save']}" />
				<sakai:button_bar_item id="cancel" immediate="true"
					action="#{ChatTool.processActionEditRoomCancel}"
					value="#{msgs['gen.cancel']}" />
			</sakai:button_bar>
		</h:form>
	</sakai:view>
</f:view>
