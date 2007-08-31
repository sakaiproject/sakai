<sakai:tool_bar>
		<sakai:tool_bar_item
			action="#{MenuBean.processSiteList}"
			value="#{msgs.menu_sitelist}"
			rendered="#{MenuBean.admin}" />
		<sakai:tool_bar_item
			value=" | "
			disabled="true"
			rendered="#{MenuBean.admin}" />
		<sakai:tool_bar_item
			action="#{MenuBean.processOverview}"
			disabled="#{viewName eq 'OverviewBean'}"
			value="#{msgs.menu_overview}" />
		<sakai:tool_bar_spacer/>
		<sakai:tool_bar_item 
			action="#{MenuBean.processEvents}"
			disabled="#{viewName eq 'EventsBean'}"
			value="#{msgs.menu_events}" />
		<sakai:tool_bar_spacer/>
		<sakai:tool_bar_item
			action="#{MenuBean.processResources}"
			disabled="#{viewName eq 'ResourcesBean'}"
			value="#{msgs.menu_resources}" />
		<sakai:tool_bar_spacer/>
		<sakai:tool_bar_item
			action="#{MenuBean.processPrefs}"
			disabled="#{viewName eq 'PrefsBean'}"
			value="#{msgs.menu_prefs}" />
</sakai:tool_bar>
