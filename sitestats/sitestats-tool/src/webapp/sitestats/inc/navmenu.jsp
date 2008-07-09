<sakai:tool_bar>
		<sakai:tool_bar_item
			action="#{MenuBean.processSiteList}"
			value="#{msgs.menu_sitelist}"
			disabled="#{viewName eq 'SiteListBean'}"
			rendered="#{ServiceBean.adminView}" />
		<sakai:tool_bar_item
			value=" | "
			disabled="true"
			rendered="#{ServiceBean.adminView && ServiceBean.serverWideStatsEnabled}" />
		<sakai:tool_bar_item
			action="#{MenuBean.processServerWide}"
			value="#{msgs.menu_serverwide}"
			disabled="#{viewName eq 'ServerWideReportBean'}"
			rendered="#{ServiceBean.adminView && ServiceBean.serverWideStatsEnabled}" />
		<sakai:tool_bar_item
			action="#{MenuBean.processOverview}"
			disabled="#{viewName eq 'OverviewBean'}"
			value="#{msgs.menu_overview}" 
			rendered="#{!ServiceBean.adminView && (ServiceBean.enableSiteVisits || ServiceBean.enableSiteActivity)}" />
		<sakai:tool_bar_item
			value=" | "
			disabled="true"
			rendered="#{!ServiceBean.adminView && (ServiceBean.enableSiteVisits || ServiceBean.enableSiteActivity)}" />
		<sakai:tool_bar_item 
			action="#{MenuBean.processReports}"
			disabled="#{viewName eq 'ReportsBean'}"
			value="#{msgs.menu_reports}"
			rendered="#{!ServiceBean.adminView}" />
		<sakai:tool_bar_item
			value=" | "
			disabled="true"
			rendered="#{!ServiceBean.adminView}" />
		<sakai:tool_bar_item
			action="#{MenuBean.processPrefs}"
			disabled="#{viewName eq 'PrefsBean'}"
			value="#{msgs.menu_prefs}"
			rendered="#{!ServiceBean.adminView}" />
</sakai:tool_bar>
