/**
 * Copyright (c) 2003-2014 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
	
	/**
	 * this is a property name to indicate whether the Site Info tool should log the following user membership change events
	 */
	static final String WSETUP_TRACK_USER_MEMBERSHIP_CHANGE = "wsetup.track.user.membership.change";

	/**
	 * this is a property name to indicate whether the Site Info tool should log the roster change events
	 */
	static final String WSETUP_TRACK_ROSTER_CHANGE = "wsetup.track.roster.change";
	
}
