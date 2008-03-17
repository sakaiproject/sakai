package org.sakaiproject.sitestats.api;

public interface Authz {

	/** Permissions */
	public static final String	PERMISSION_SITESTATS_VIEW		= "sitestats.view";
	public static final String	PERMISSION_SITESTATS_ADMIN_VIEW	= "sitestats.admin.view";

	// ################################################################
	// Public methods
	// ################################################################
	public boolean isUserAbleToViewSiteStats(String siteId);

	public boolean isUserAbleToViewSiteStatsAdmin(String siteId);

}