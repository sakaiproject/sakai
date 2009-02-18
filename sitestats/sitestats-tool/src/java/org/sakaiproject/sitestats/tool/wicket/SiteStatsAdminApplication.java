package org.sakaiproject.sitestats.tool.wicket;

import org.apache.wicket.settings.IExceptionSettings;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.tool.wicket.pages.AdminPage;


public class SiteStatsAdminApplication extends SiteStatsApplication {
	
	protected void init() {
		super.init();

		// Home page
		mountBookmarkablePage("/admin", AdminPage.class);

		// On wicket session timeout or wicket exception, redirect to main page
		getApplicationSettings().setPageExpiredErrorPage(AdminPage.class);
		getApplicationSettings().setAccessDeniedPage(AdminPage.class);
		getApplicationSettings().setInternalErrorPage(AdminPage.class);

		// show internal error page rather than default developer page
		getExceptionSettings().setUnexpectedExceptionDisplay(IExceptionSettings.SHOW_INTERNAL_ERROR_PAGE);
	}

	@SuppressWarnings("unchecked")
	public Class getHomePage() {
		return AdminPage.class;
	}
}
