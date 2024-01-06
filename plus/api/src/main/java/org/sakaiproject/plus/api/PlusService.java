/*
 * Copyright (c) 2021- Charles R. Severance
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.sakaiproject.plus.api;

import java.util.Map;
import java.util.List;

import org.sakaiproject.lti.api.LTIException;

import org.sakaiproject.plus.api.model.Tenant;
import org.sakaiproject.plus.api.model.Subject;
import org.sakaiproject.plus.api.model.Context;
import org.sakaiproject.plus.api.model.Membership;
import org.sakaiproject.plus.api.model.Link;

import org.tsugi.lti13.objects.LaunchJWT;

import org.sakaiproject.user.api.User;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.event.api.Event;

public interface PlusService {

	public static final String PLUS_PROPERTY = "plus_site";

	public static final String PLUS_PROVIDER_ENABLED = "plus.provider.enabled";
	public static final boolean PLUS_PROVIDER_ENABLED_DEFAULT = true;
	public static final String PLUS_DEBUG_VERBOSE = "plus.debug.verbose";
	public static final boolean PLUS_DEBUG_VERBOSE_DEFAULT = false;
	public static final String PLUS_ROSTER_SYCHRONIZATION = "plus.roster.synchronization";
	public static final boolean PLUS_ROSTER_SYCHRONIZATION_DEFAULT = true;

	public static final String PLUS_DEEPLINK_ENABLED = "plus.deeplink.enabled";
	public static final boolean PLUS_DEEPLINK_ENABLED_DEFAULT = true;

	// plus.allowedtools=sakai.resources:sakai.site
	public static final String PLUS_TOOLS_ALLOWED = "plus.tools.allowed";
	public static final String PLUS_TOOLS_ALLOWED_DEFAULT = "";
	public static final String PLUS_TOOLS_NEW_WINDOW = "plus.tools.new.window";
	public static final String PLUS_TOOLS_NEW_WINDOW_DEFAULT = "";
	public static final String PLUS_NEW_SITE_TEMPLATE = "plus.new.site.template";
	public static final String PLUS_NEW_SITE_TEMPLATE_DEFAULT = "!plussite";
	public static final String PLUS_NEW_SITE_TEMPLATE_BACKUP = "!worksite";
	public static final String PLUS_NEW_SITE_REALM = "plus.new.site.realm";
	public static final String PLUS_NEW_SITE_REALM_DEFAULT = "!site.template.lti";
	public static final String PLUS_NEW_SITE_TYPE = "plus.new.site.type";
	public static final String PLUS_NEW_SITE_TYPE_DEFAULT = "project";

	// Used in LTI Dynamic Registration, Deep Link, or Canvas Configuration responses when there is
	// a need to describe the current server.  Generic translatable defaults come from from plus.properties
	// unless they are overridden here.
	// Default from plus.properties: Sakai Plus
	public static final String PLUS_SERVER_TITLE = "plus.server.title";
	// Default from plus.properties: Open source LMS and tools
	public static final String PLUS_SERVER_DESCRIPTION = "plus.server.description";

	// Used to set fields when LTI Dynamic registration is used - defaults are null
	// and not to provide these values in LTI Dynamic Registration responses
	public static final String PLUS_SERVER_POLICY_URI = "plus.server.policy.uri";
	public static final String PLUS_SERVER_TOS_URI = "plus.server.tos.uri";
	public static final String PLUS_SERVER_LOGO_URI = "plus.server.logo.uri";

	// Used when installing the 'sakai.site' endpoint using DeepLinking.  Since sakai.site is not
	// actually a registered tool, it has no title and description and these properties allow
	// you to override the translatable defaults stored in plus.properties
	// Default from plus.properties: Sakai Plus
	public static final String SAKAI_SITE_TITLE = "sakai.site.title";
	// Default from plus.properties: This link will launch a complete Sakai site with the ability to ...
	public static final String SAKAI_SITE_DESCRIPTION = "sakai.site.description";

	// Canvas specific values - these mostly are used to configure the non-standard Canvas
	// tool registration since Canvas does not support the LTI Dynamic Registration process
	// as of 2022 - and does not seem to be in a rush to implement it.
	public static final String PLUS_CANVAS_ENABLED = "plus.canvas.enabled";
	public static final boolean PLUS_CANVAS_ENABLED_DEFAULT = true;
	// The default is taken from the sakai current server URL
	public static final String PLUS_CANVAS_DOMAIN = "plus.canvas.domain";

	// Generic defaults for these values come from plus.properties
	// Default from plus.properties: Sakai Tools
	public static final String PLUS_CANVAS_TITLE = "plus.canvas.title";
	// Default from plus.properties: This server hosts Sakai tools that you can launch from Canvas.
	public static final String PLUS_CANVAS_DESCRIPTION = "plus.canvas.description";

	/*
	 * Note whether or not this system has Plus enabled
	 */
	boolean enabled();

	/*
	 * Note whether or not a Site has Plus enabled
	 */
	boolean enabled(Site site);

	/*
	 * Note whether or not we are in verbose mode
	 */
	boolean verbose();

	/*
	 * Note whether or not we are in verbose mode
	 */
	boolean verbose(Tenant tenant);

	/*
	 * Return various URLs for Sakai Plus
	 */
	String getPlusServletPath();
	String getOidcKeySet();
	String getOidcLogin(Tenant tenant);
	String getOidcLaunch();
	String getLTIDynamicRegistration(Tenant tenant);
	String getCanvasConfig(Tenant tenant);

	/*
	 * Get a payload map from a LaunchJWT
	 */
	Map<String,String> getPayloadFromLaunchJWT(Tenant tenant, LaunchJWT launchJWT);

	/*
	 * Handle the initial launch - creating objects as needed (a.k.a. The BIG LEFT JOIN)
	 */
	Launch updateAll(LaunchJWT tokenBody, Tenant tenant)
			throws LTIException;

	/*
	 * Make sure the Subject knows about the chosen user
	 */
	void connectSubjectAndUser(Subject subject, User user)
			throws LTIException;

	/*
	 * Make sure the Context knows about the chosen site
	 */
	void connectContextAndSite(Context context, Site site)
			throws LTIException;

	/*
	 * Make sure the Link knows about the chosen placement
	 */
	void connectLinkAndPlacement(Link link, String placementId)
			throws LTIException;

	/*
	 * Retrieve Context Memberships from calling LMS and update the site
	 */
	void requestSyncSiteMemberships(Context context);

	/*
	 * Request site memberships if enough time has passed
	 */
	void requestSyncSiteMembershipsCheck(Context context, boolean isInstructor);

	/*
	 * Create a lineItem for a gradebook Column
	 */
	String createLineItem(Site site, Long assignmentId,
		final org.sakaiproject.grading.api.Assignment assignmentDefinition);

	/*
	 * Update a lineItem for a gradebook Column
	 */
	String updateLineItem(Site site,
		final org.sakaiproject.grading.api.Assignment assignmentDefinition);

	/*
	 * Send a score to the calling LMS
	 */
	// https://www.imsglobal.org/spec/lti-ags/v2p0#score-publish-service
	void processGradeEvent(Event event);

	/*
	 * Get "old" memberships that are still in the Realm
	 */
	public List<Membership> getSiteUsersMinutesOld(Context context, int minutes);

	/*
	 * Remove "old" memberships that are still in the Realm
	 *
	 * Returns a list of the memberships that were removed from the Realm.  The memberships
	 * continue to exist - just the realm entries are removed.  Is a user shows up in an NRPS
	 * retrieval or launches into a Plus Site, since remove from the Realm does not remove
	 * user activity data from a Site, they are re-added to the Realm and no data is lost.
	 */
	public List<Membership> removeSiteUsersMinutesOld(Context context, int minutes);

	/*
	 * Get the number of minutes to use when expiring inactive users for a site
	 */
	public Site getSite(Context context);

	/*
	 * Get the number of minutes to use when expiring inactive users for a site
	 */
	public int getInactiveExpireMinutes(Context context);

	/*
	 * Get the number of seconds to use re-retrieving a roster via NRPS
	 */
	public long getNRPSDelaySeconds(Context context, boolean instructor);
}
