<f:view>
	<sakai:view title="#{msgs.synoptic_options_title}">
		<h:form>
			<sakai:messages rendered="#{!empty facesContext.maximumSeverity}" />

			<sakai:group_box title="#{msgs.recent_chat_heading}">
			<sakai:panel_edit>

				<h:outputLabel for="days" value="#{msgs.number_days}" />
				<h:inputText id="days"
					value="#{ChatTool.currentSynopticOptions.days}" size="2" validator="#{ChatTool.validatePositiveNumber}" />

				<h:outputLabel for="messages" value="#{msgs.number_messages}" />
				<h:inputText id="messages"
					value="#{ChatTool.currentSynopticOptions.items}" size="2" validator="#{ChatTool.validatePositiveNumber}" />

				<h:outputLabel for="chars" value="#{msgs.number_chars}" />
				<h:inputText id="chars" value="#{ChatTool.currentSynopticOptions.chars}" size="2" validator="#{ChatTool.validatePositiveNumber}"/>

				
			</sakai:panel_edit>
			</sakai:group_box>
			
			<sakai:button_bar>
				<sakai:button_bar_item id="submit"
					action="#{ChatTool.processActionSynopticOptionsSave}"
					value="#{msgs.update_text}" />
				<sakai:button_bar_item id="reset"
					action="#{ChatTool.processActionSynopticOptionsCancel}"
					value="#{msgs['gen.cancel']}" />
			</sakai:button_bar>

		</h:form>
	</sakai:view>
</f:view>
