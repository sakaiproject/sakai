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

import org.sakaiproject.plus.api.Launch;
import org.sakaiproject.plus.api.model.Tenant;
import org.sakaiproject.plus.api.model.Subject;
import org.sakaiproject.plus.api.model.Context;
import org.sakaiproject.plus.api.model.Link;

import org.tsugi.lti13.objects.LaunchJWT;

import org.sakaiproject.user.api.User;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.event.api.Event;

import org.sakaiproject.lti.api.BLTIProcessor;

public interface PlusService {

	public static final String PLUS_PROPERTY = "plus_site";
	public static final String PLUS_PROVIDER_ENABLED = "plus.provider.enabled";
	public static final String PLUS_PROVIDER_ENABLED_DEFAULT = "true";
	public static final String PLUS_PROVIDER_VERBOSE = "plus.provider.verbose";
	public static final String PLUS_PROVIDER_VERBOSE_DEFAULT = "false";
	public static final String PLUS_ROSTER_SYCHRONIZATION = "plus.roster.synchronization";
	public static final boolean PLUS_ROSTER_SYCHRONIZATION_DEFAULT = false;

	public enum ProcessingState {
		beforeValidation, afterValidation, afterUserCreation, afterLogin, afterSiteCreation,
		afterSiteMembership, beforeLaunch
	}

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
	String getIMSDynamicRegistration(Tenant tenant);
	String getCanvasConfig(Tenant tenant);

	/*
	 * Get the list of processors from ProviderServlet
	 */
	void setBltiProcessors(List<BLTIProcessor> bltiProcessors);

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
	void syncSiteMemberships(String contextGuid, Site site)
			throws LTIException;

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
	 * LTIProcessors to invoke custom code at each step of the launch setup
	 */
	void invokeProcessors(Map payload, ProcessingState processingState, User user) throws LTIException;

	void invokeProcessors(Map payload, ProcessingState processingState) throws LTIException;

	void invokeProcessors(Map payload,
		ProcessingState processingState, User user,
		Site site) throws LTIException;

	void invokeProcessors(Map payload,
		ProcessingState processingState, User user,
		Site site, String toolPlacementId) throws LTIException;

	// vim: tabstop=4 noet
}
