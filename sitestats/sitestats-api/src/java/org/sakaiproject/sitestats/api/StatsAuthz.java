package org.sakaiproject.sitestats.api;

public interface StatsAuthz {

	/** Permissions */
	public static final String	PERMISSION_SITESTATS_VIEW		= "sitestats.view";
	public static final String	PERMISSION_SITESTATS_ADMIN_VIEW	= "sitestats.admin.view";

	// ################################################################
	// Public methods
	// ################################################################
	/**
	 * Check if current user has permission to access SiteStats tool.
	 * @param siteId The site id to check against.
	 */
	public boolean isUserAbleToViewSiteStats(String siteId);

	/**
	 * Check if current user has permission to access SiteStats Admin tool.
	 * @param siteId The site id to check against.
	 */
	public boolean isUserAbleToViewSiteStatsAdmin(String siteId);

	/** Check if current tool is an instance of the SiteStats tool. */
	public boolean isSiteStatsPage();
	
	/** Check if current tool is an instance of the SiteStats Admin tool. */
	public boolean isSiteStatsAdminPage();
}