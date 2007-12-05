<sakai:tool_bar>
		<sakai:tool_bar_item
			action="#{MenuBean.processSiteList}"
			value="#{msgs.menu_sitelist}"
			rendered="#{ServiceBean.adminView}" />
		<sakai:tool_bar_item
			value=" | "
			disabled="true"
			rendered="#{ServiceBean.adminView}" />
		<sakai:tool_bar_item
			action="#{MenuBean.processOverview}"
			disabled="#{viewName eq 'OverviewBean'}"
			value="#{msgs.menu_overview}" />
		<sakai:tool_bar_spacer/>
		<sakai:tool_bar_item 
			action="#{MenuBean.processReports}"
			disabled="#{viewName eq 'ReportsBean'}"
			value="#{msgs.menu_reports}" />
		<sakai:tool_bar_spacer/>
		<sakai:tool_bar_item
			action="#{MenuBean.processPrefs}"
			disabled="#{viewName eq 'PrefsBean'}"
			value="#{msgs.menu_prefs}" />
</sakai:tool_bar>