package org.sakaiproject.sitemanage.api;

public interface SiteHelper {

	/**
	 * This site ID selected from the helper.
	 */
	static final String SITE_PICKER_SITE_ID = "sakaiproject.sitepicker.siteid";
	
	/**
	 * Permission needed for the current user over the selected site.
	 * @see SiteService.SiteType
	 */
	static final String SITE_PICKER_PERMISSION = "sakaiproject.sitepicker.permission";
	
	/**
	 * Property needing to be set on the requested site.
	 */
	static final String SITE_PICKER_PROPERTY = "sakaiproject.sitepicker.property";
	
	/**
	 * The selection of a site ID was cancelled.
	 */
	static final String SITE_PICKER_CANCELLED = "sakaiproject.sitepicker.cancelled";
	
	
	// For creation of a new site


	/**
	 * Attribute to indicate that the site creation helper should start from the beginning.
	 * Example: Boolean.TRUE.
	 */
	static final String SITE_CREATE_START = "sakaiproject.sitecreate.start";
	
	/**
	 * Attribute to tell set creation helper what site types should be available.
	 * Example: "project,course".
	 */
	static final String SITE_CREATE_SITE_TYPES = "sakaiproject.sitecreate.types";
	
	/**
	 * The title of the site to create, if present the user won't be able to edit the site title in the helper.
	 * Example: "My Test Site".
	 */
	static final String SITE_CREATE_SITE_TITLE = "sakaiproject.sitecreate.title";
	
	/**
	 * ID of the created site returned by the helper.
	 * Example: "32mds8slslaid-s7skj-s78sj"
	 */
	static final String SITE_CREATE_SITE_ID = "sakaiproject.sitecreate.siteid";
	
	/**
	 * Precence indicated user cancelled the helper.
	 * Example: Boolean.TRUE.
	 */
	static final String SITE_CREATE_CANCELLED = "sakaiproject.sitecreate.cancelled";

}
