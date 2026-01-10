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

package org.sakaiproject.plus.impl;

import java.lang.StringBuffer;

import java.util.List;
import java.util.Date;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;

import java.util.Map;
import java.util.TreeMap;
import java.util.Optional;
import java.util.Collections;
import java.util.LinkedHashMap;

import java.util.concurrent.TimeUnit;

import java.io.InputStream;

import java.time.Instant;

import java.net.http.HttpResponse;  // Thanks Java 11
import java.net.http.HttpHeaders;  // Thanks Java 11

import java.security.KeyPair;

import org.apache.commons.lang3.StringUtils;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import org.apache.commons.lang3.math.NumberUtils;

import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

import org.sakaiproject.user.api.User;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.event.api.Event;

import org.sakaiproject.lti.api.LTIException;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.grading.api.AssessmentNotFoundException;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.grading.api.CommentDefinition;
import org.sakaiproject.exception.IdUnusedException;

import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;

import org.sakaiproject.plus.api.Launch;
import org.sakaiproject.plus.api.PlusService;
import org.sakaiproject.plus.api.model.Context;
import org.sakaiproject.plus.api.model.ContextLog;
import org.sakaiproject.plus.api.model.Link;
import org.sakaiproject.plus.api.model.Subject;
import org.sakaiproject.plus.api.model.Tenant;
import org.sakaiproject.plus.api.model.Membership;
import org.sakaiproject.plus.api.repository.ContextRepository;
import org.sakaiproject.plus.api.repository.ContextLogRepository;
import org.sakaiproject.plus.api.repository.LineItemRepository;
import org.sakaiproject.plus.api.repository.LinkRepository;
import org.sakaiproject.plus.api.repository.ScoreRepository;
import org.sakaiproject.plus.api.repository.SubjectRepository;
import org.sakaiproject.plus.api.repository.TenantRepository;
import org.sakaiproject.plus.api.repository.MembershipRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.tsugi.http.HttpUtil;
import org.tsugi.http.HttpClientUtil;

import org.tsugi.lti.LTIConstants;
import org.tsugi.lti.LTIUtil;
import org.sakaiproject.lti.util.SakaiLTIUtil;

import org.sakaiproject.lti.api.UserFinderOrCreator;
import org.sakaiproject.lti.api.SiteEmailPreferenceSetter;
import org.sakaiproject.lti.api.SiteMembershipUpdater;

import org.sakaiproject.lti.util.SakaiKeySetUtil;
import org.tsugi.jackson.JacksonUtil;
import org.sakaiproject.lti13.util.SakaiLaunchJWT;
import org.sakaiproject.scheduling.api.SchedulingService;

import org.tsugi.lti13.objects.LaunchJWT;
import org.tsugi.lti13.LTI13Util;
import org.tsugi.lti13.LTI13AccessTokenUtil;
import org.tsugi.lti13.LTI13ConstantsUtil;

import org.tsugi.nrps.objects.Member;
import org.tsugi.oauth2.objects.AccessToken;
import org.tsugi.ags2.objects.LineItem;
import org.tsugi.ags2.objects.Score;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Setter
public class PlusServiceImpl implements PlusService {

	// The number of minutes to expire membership entries (1 week)
	static int MEMBERSHIP_EXPIRE_MINUTES = 7*24*60;
	static String PLUS_MEMBERSHIP_EXPIRE_MINUTES = "plus:expire_minutes";

	// Wait five or 30 minutes between successive calls to NRPS.
	public final long DELAY_NRPS_INSTRUCTOR_SECONDS = 300;
	public final long DELAY_NRPS_LEARNER_SECONDS = 30*60;  // 30 minutes
	static String PLUS_NRPS_DELAY_SECONDS = "plus:nrps_delay_seconds";

	@Autowired private TenantRepository tenantRepository;
	@Autowired private SubjectRepository subjectRepository;
	@Autowired private ContextRepository contextRepository;
	@Autowired private ContextLogRepository contextLogRepository;
	@Autowired private LinkRepository linkRepository;
	@Autowired private LineItemRepository lineItemRepository;
	@Autowired private ScoreRepository scoreRepository;
	@Autowired private MembershipRepository membershipRepository;

	@Autowired private GradingService gradingService;
	@Autowired private SiteMembershipUpdater siteMembershipUpdater;
	@Autowired private SiteEmailPreferenceSetter siteEmailPreferenceSetter;
	@Autowired private UserFinderOrCreator userFinderOrCreator;
	@Autowired private ServerConfigurationService serverConfigurationService;
	@Autowired private SiteService siteService;
	@Autowired private SecurityService securityService;
	@Autowired private SchedulingService schedulingService;

	private Map<String, String> refreshQueue;

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		try {
			refreshQueue = Collections.synchronizedMap(new LinkedHashMap<>());

			long refreshTaskInterval = 60;

			schedulingService.scheduleWithFixedDelay(
				new refreshContextMembershipsTask(),
				120, // minimally wait 2 mins for sakai to start
				refreshTaskInterval, // delay before running again
				TimeUnit.SECONDS
			);
		} catch (Exception t) {
			log.warn("init(): ", t);
		}
	}

	/*
	 * Indicate if plus is enabled on this system
	 */
	@Override
	public boolean enabled()
	{
		return serverConfigurationService.getBoolean(PlusService.PLUS_PROVIDER_ENABLED, PlusService.PLUS_PROVIDER_ENABLED_DEFAULT);
	}

	/*
	 * Indicate if plus is enabled on a Site
	 */
	@Override
	public boolean enabled(Site site)
	{
		String plus_property = site.getProperties().getProperty(PlusService.PLUS_PROPERTY);
		return "true".equals(plus_property);
	}

	/*
	 * Return various URLs for Sakai Plus
	 */
	@Override
	public String getPlusServletPath() {
		return SakaiLTIUtil.getOurServerUrl() + "/plus/sakai";
	}

	@Override
	public String getOidcKeySet() {
		return SakaiLTIUtil.getOurServerUrl() + "/imsblis/lti13/keyset";
	}

	@Override
	public String getOidcLaunch() {
		return getPlusServletPath() + "/oidc_launch";
	}

	@Override
	public String getOidcLogin(Tenant tenant) {
		return getPlusServletPath() + "/oidc_login/" + tenant.getId();
	}

	@Override
	public String getLTIDynamicRegistration(Tenant tenant) {
		if ( StringUtils.isEmpty(tenant.getOidcRegistrationLock()) ) return null;
		return getPlusServletPath() + "/dynamic/" + tenant.getId() + "?unlock_token=" + tenant.getOidcRegistrationLock();
	}

	@Override
	public String getCanvasConfig(Tenant tenant) {
		return getPlusServletPath() + "/canvas-config.json?guid=" + tenant.getId();
	}

	/*
	 * Indicate if verbose debugging is enabled
	 */
	@Override
	public boolean verbose()
	{
		if ( log.isDebugEnabled() ) return true;
		return (serverConfigurationService.getBoolean(PlusService.PLUS_DEBUG_VERBOSE, PlusService.PLUS_DEBUG_VERBOSE_DEFAULT));
	}

	/*
	 * Indicate if verbose debugging is enabled
	 */
	@Override
	public boolean verbose(Tenant tenant)
	{
		if ( tenant != null && tenant.getVerbose() ) return true;
		return verbose();
	}

	/*
	 * Handle the initial launch - creating objects as needed (a.k.a. The BIG LEFT JOIN)
	 */
	public Launch updateAll(LaunchJWT launchJWT, Tenant tenant)
		throws LTIException
	{
		if ( launchJWT == null || tenant == null ) {
			throw new LTIException("plus.plusservice.null", null, null);
		}

		if ( tenant.getId() == null ) {
			throw new LTIException("plus.plusservice.tenant.persist", null, null);
		}

		String issuer = launchJWT.issuer;
		String clientId = launchJWT.audience;
		String deploymentId = launchJWT.deployment_id;

		String missing = "";
		if ( issuer == null ) {
			missing = missing + "issuer null ";
		} else if (! issuer.equals(tenant.getIssuer()) ) {
			missing = missing + "issuer mismatch "  + issuer + "/" + tenant.getIssuer();
		}

		if ( clientId == null ) {
			missing = missing + "clientId null ";
		} else if (! clientId.equals(tenant.getClientId()) ) {
			missing = missing + "clientId mismatch " + clientId + "/" + tenant.getClientId();
		}

		if ( deploymentId == null ) {
			missing = missing + "deploymentId null ";
		} else if (! tenant.validateDeploymentId(deploymentId) ) {
			missing = missing + "deploymentId mismatch " + deploymentId + "/" + tenant.getDeploymentId();
		}

		if ( ! missing.equals("") ) {
			throw new LTIException("plus.plusservice.tenant.check", missing, null);
		}

		String contextId = launchJWT.context != null ? launchJWT.context.id : null;
		String subjectId = launchJWT.subject;
		String linkId =  launchJWT.resource_link != null ? launchJWT.resource_link.id : null;

		Launch launch = new Launch();
		launch.tenant = tenant;

		boolean changed = false;
		Subject subject = null;
		if ( subjectId != null ) {
			subject = createOrUpdateSubject(tenant, subjectId, launchJWT);
			launch.subject = subject;
		}

		Context context = null;
		if ( contextId != null ) {
			context = contextRepository.findByContextAndTenant(contextId, tenant);
			changed = false;
			if ( context == null ) {
				context = new Context();
				context.setContext(contextId);
				context.setTenant(tenant);
				context.setDeploymentId(launchJWT.deployment_id);
				context.setTitle(launchJWT.context.title);
				context.setLabel(launchJWT.context.label);
				if ( launchJWT.endpoint != null && launchJWT.endpoint.lineitems != null ) context.setLineItems(launchJWT.endpoint.lineitems);
				if ( launchJWT.names_and_roles != null && launchJWT.names_and_roles.context_memberships_url != null ) {
					context.setContextMemberships(launchJWT.names_and_roles.context_memberships_url);
				}
				changed = true;
			} else {
				if ( StringUtils.compare(context.getDeploymentId(), launchJWT.deployment_id) != 0 ) {
					context.setDeploymentId(launchJWT.deployment_id);
					changed = true;
				}
				if ( StringUtils.compare(context.getLabel(), launchJWT.context.label) != 0 ) {
					context.setLabel(launchJWT.context.label);
					changed = true;
				}
				if ( StringUtils.compare(context.getTitle(), launchJWT.context.title) != 0 ) {
					context.setTitle(launchJWT.context.title);
					changed = true;
				}
				if ( StringUtils.compare(context.getLabel(), launchJWT.context.label) != 0 ) {
					context.setLabel(launchJWT.context.label);
					changed = true;
				}

				if ( launchJWT.endpoint != null && launchJWT.endpoint.lineitems != null &&
					 StringUtils.compare(context.getLineItems(), launchJWT.endpoint.lineitems) != 0 ) {
					context.setLineItems(launchJWT.endpoint.lineitems);
					changed = true;
				}
				if ( launchJWT.names_and_roles != null && launchJWT.names_and_roles.context_memberships_url != null &&
					StringUtils.compare(context.getContextMemberships(), launchJWT.names_and_roles.context_memberships_url) != 0 ) {
					context.setContextMemberships(launchJWT.names_and_roles.context_memberships_url);
					changed = true;
				}
			}
			if ( changed) contextRepository.save(context);
			launch.context = context;
		}

		Membership membership;
		if ( subject != null && context != null ) {
			membership = new Membership();
			membership.setSubject(subject);
			membership.setContext(context);
			membership.setUpdatedAt(Instant.now());
			String ltiRoles = launchJWT.getLTI11Roles();
			if ( StringUtils.isNotBlank(ltiRoles) ) membership.setLtiRoles(ltiRoles);
			membership = membershipRepository.upsert(membership);
		}

		if ( linkId != null && context != null ) {
			Link link = linkRepository.findByLinkAndContext(linkId, context);
			changed = false;
			if ( link == null ) {
				link = new Link();
				link.setLink(linkId);
				link.setContext(context);
				link.setTitle(launchJWT.resource_link.title);
				link.setDescription(launchJWT.resource_link.description);
				changed = true;
			} else {
				if ( StringUtils.compare(link.getTitle(), launchJWT.resource_link.title) != 0 ) {
					link.setTitle(launchJWT.resource_link.title);
					changed = true;
				}
				if ( StringUtils.compare(link.getDescription(), launchJWT.resource_link.description) != 0 ) {
					link.setDescription(launchJWT.resource_link.description);
					changed = true;
				}
			}
			if ( changed) linkRepository.save(link);
			launch.link = link;
		}

		return launch;
	}

	public Subject createOrUpdateSubject(Tenant tenant, String subjectId, LaunchJWT launchJWT)
	{
		Subject subject = null;
		boolean changed = false;
		if ( subjectId != null ) {
			subject = subjectRepository.findBySubjectAndTenant(subjectId, tenant);
			if ( subject == null ) {
				subject = new Subject();
				subject.setSubject(subjectId);
				subject.setTenant(tenant);
				subject.setEmail(launchJWT.email);
				subject.setDisplayName(launchJWT.getDisplayName());
				changed = true;
			} else {
				if ( StringUtils.compare(subject.getEmail(), launchJWT.email) != 0 ) {
					subject.setEmail(launchJWT.email);
					changed = true;
				}
				if ( StringUtils.compare(subject.getDisplayName(), launchJWT.getDisplayName() ) != 0 ) {
					subject.setDisplayName(launchJWT.getDisplayName());
					changed = true;
				}
			}
			if ( changed ) {
				subjectRepository.save(subject);
			}
		}
		return subject;
	}

	@Override
	public Map<String,String> getPayloadFromLaunchJWT(Tenant tenant, LaunchJWT launchJWT)
	{
		// Store this all in payload for future use and to share some of the
		// processing code between LTI 1.1 and Advantage
		Map<String, String> payload = new TreeMap<>();
		payload.put("issuer", launchJWT.issuer);
		payload.put("client_id", launchJWT.audience); // Note name change
		payload.put("deployment_id", launchJWT.deployment_id);
		payload.put("oidc_token", tenant.getOidcToken());
		payload.put("oidc_audience", tenant.getOidcAudience());
		payload.put(LTIConstants.LTI_MESSAGE_TYPE, launchJWT.message_type);

		if ( launchJWT.context != null ) {
			if ( launchJWT.context.title != null ) payload.put(LTIConstants.CONTEXT_TITLE, launchJWT.context.title);
			if ( launchJWT.context.label != null ) payload.put(LTIConstants.CONTEXT_LABEL, launchJWT.context.label);
		}

		// https://www.imsglobal.org/spec/lti/v1p3/#resource-link-claim
		String linkId = launchJWT.resource_link != null ? launchJWT.resource_link.id : null;
		if ( linkId != null ) {
			payload.put(LTIConstants.RESOURCE_LINK_ID, linkId);
			if ( launchJWT.resource_link.title != null ) payload.put(LTIConstants.RESOURCE_LINK_TITLE, launchJWT.resource_link.title);
			if ( launchJWT.resource_link.description != null ) payload.put(LTIConstants.RESOURCE_LINK_DESCRIPTION, launchJWT.resource_link.description);
		}

		// User data
		payload.put(LTIConstants.USER_ID, launchJWT.subject);
		payload.put(LTIConstants.LAUNCH_PRESENTATION_LOCALE, launchJWT.locale);
		payload.put(LTIConstants.LIS_PERSON_CONTACT_EMAIL_PRIMARY, launchJWT.email);
		payload.put(LTIConstants.LIS_PERSON_NAME_GIVEN, launchJWT.given_name);
		payload.put(LTIConstants.LIS_PERSON_NAME_FAMILY, launchJWT.family_name);
		// payload.put(LTIConstants.LIS_PERSON_NAME_MIDDLE, launchJWT.middle_name);

		String ltiRoles = launchJWT.getLTI11Roles();
		if ( StringUtils.isNotBlank(ltiRoles) ) payload.put(LTIConstants.ROLES, ltiRoles);

		// TODO: Ask for this in custom...
		// payload.put(LTIConstants.USER_IMAGE, );
		// payload.put("ext_email_delivery_preference", );

		// Because lti-common can't (yet) be in shared
		if ( launchJWT instanceof SakaiLaunchJWT ) {
			SakaiLaunchJWT sakaiLaunchJWT = (SakaiLaunchJWT) launchJWT;
			if ( sakaiLaunchJWT.sakai_extension != null ) {
				if ( isNotEmpty(sakaiLaunchJWT.sakai_extension.sakai_eid) ) {
					payload.put(LTIConstants.EXT_SAKAI_PROVIDER_EID, sakaiLaunchJWT.sakai_extension.sakai_eid);
				}
				payload.put("ext_sakai_server", sakaiLaunchJWT.sakai_extension.sakai_server);
				payload.put("ext_sakai_serverid", sakaiLaunchJWT.sakai_extension.sakai_serverid);
				payload.put("ext_sakai_role", sakaiLaunchJWT.sakai_extension.sakai_role);
				payload.put("ext_sakai_academic_session", sakaiLaunchJWT.sakai_extension.sakai_academic_session);
			}
		}

		// https://www.imsglobal.org/spec/lti/v1p3/#platform-instance-claim
		// payload.put("ext_lms", );

		return payload;
	}

	/*
	 * Make sure the Subject knows about the chosen user
	 */
	public void connectSubjectAndUser(Subject subject, User user)
			throws LTIException
	{
		if ( subject == null || user == null ) {
		  throw new LTIException( "plus.plusservice.null.parameters", "subject or user", null);
		}

		if ( isEmpty(subject.getId()) ) {
		  throw new LTIException( "plus.plusservice.not.persisted", "subject", null);
		}

		if ( isEmpty(user.getId()) ) {
		  throw new LTIException( "plus.plusservice.not.persisted", "user", null);
		}

		// After all that error checking, it is pretty simple
		// TODO: Should we make sure there is only one (Tenant / SakaiUserId) record?
		// This can lead to more than one subject with a particular SakaiID
		subject.setSakaiUserId(user.getId());
		subjectRepository.save(subject);
	}

	/*
	 * Make sure the Context knows about the chosen site
	 */
	@Override
	public void connectContextAndSite(Context context, Site site)
			throws LTIException
	{
		if ( context == null || site == null ) {
		  throw new LTIException( "plus.plusservice.null.parameters", "context or site", null);
		}

		if ( isEmpty(context.getId()) ) {
		  throw new LTIException( "plus.plusservice.not.persisted", "context", null);
		}

		if ( isEmpty(site.getId()) ) {
		  throw new LTIException( "plus.plusservice.site.not.persisted", "site", null);
		}

		// After all that error checking, it is prety simple
		context.setSakaiSiteId(site.getId());
		contextRepository.save(context);
	}

	/*
	 * Make sure the Link knows about the chosen placement
	 */
	@Override
	public void connectLinkAndPlacement(Link link, String placementId)
			throws LTIException
	{
		if ( link == null ) {
		  throw new LTIException( "plus.plusservice.null.parameters", "link", null);
		}

		if ( isEmpty(link.getId()) ) {
		  throw new LTIException( "plus.plusservice.not.persisted", "link", null);
		}

		if ( isEmpty(placementId) ) {
		  throw new LTIException( "plus.plusservice.site.null.parameters", "placement", null);
		}

		// After all that error checking, it is prety simple
		link.setSakaiToolId(placementId);
		linkRepository.save(link);
	}

	/**
	 * Step through queue and Context Memberships queued up for a refresh
	 *
	 * See also: kernel-impl/src/main/java/org/sakaiproject/authz/impl/DbAuthzGroupService.java
	 */
	protected class refreshContextMembershipsTask implements Runnable {
		@Override
		public void run() {
			if ( refreshQueue.size() < 1 ) return;
			log.debug("RefreshContextMembershipsTask size={}", refreshQueue.size());

			long numberRefreshed = 0;
			long timeRefreshed = 0;
			long longestRefreshed = 0;
			String longestName = null;

            while (true) {
			    String contextGuid;
		        synchronized (refreshQueue) {
				    if (refreshQueue.isEmpty()) {
					    break;
			        }
                    // Keys and values are the same in refreshQueue
                    contextGuid = refreshQueue.entrySet().iterator().next().getKey();
                    refreshQueue.remove(contextGuid);
			    }
				log.debug("Context pulled from queue {}", contextGuid);

				numberRefreshed++;
				long time = 0;
				long start = System.currentTimeMillis();

				try {
					syncSiteMembershipsInternal(contextGuid);
				} catch ( LTIException e ) {
					log.error("refreshContextMembershipsTask.run() Problem refreshing context: " + contextGuid, e);
				} finally {
					time = (System.currentTimeMillis() - start);
					log.debug("Refresh of context: {} took {} seconds", contextGuid, time/1e3);
				}

				timeRefreshed += time;
				if (time > longestRefreshed) {
					longestRefreshed = time;
					longestName = contextGuid;
				}

			}
			log.info("Refreshed {} contexts in {} seconds, longest context was {} at {} seconds",
				numberRefreshed, timeRefreshed/1e3, longestName, longestRefreshed/1e3);
		}
	}

	/*
	 * Schedule SyncSiteMemberships if enough time has passed since the last request
	 */
	public void requestSyncSiteMembershipsCheck(Context context, boolean isInstructor) {
		if ( context == null ) return;

		// First run is scheduled immediately
		Instant lastRun = context.getNrpsStart();
		if ( lastRun == null ) {
			requestSyncSiteMemberships(context);
			return;
		}

		// Otherwise we compute the delay
		long delay = getNRPSDelaySeconds(context, isInstructor);
		long lastRunEpoch = lastRun.getEpochSecond();
		long nowEpoch = Instant.now().getEpochSecond();
		long delta = nowEpoch - lastRunEpoch;

		if ( delta < 0 || delta > delay ) {
			requestSyncSiteMemberships(context);
		} else {
			log.info("Waiting {} seconds between NRPS calls context={} delta={}", delay, context.getId(), delta);
		}
	}

	/*
	 * Schedule SyncSiteMemberships
	 */
	@Override
	public void requestSyncSiteMemberships(Context context) {
		if ( context == null ) return;
		refreshQueue.put(context.getId(), context.getId());
	}

	/*
	 * Retrieve Context Memberships from calling LMS and update the site in Sakai
	 */
	public void syncSiteMembershipsInternal(String contextGuid) throws LTIException {

		log.debug("synchSiteMemberships");

		if (!serverConfigurationService.getBoolean(PlusService.PLUS_ROSTER_SYCHRONIZATION, PlusService.PLUS_ROSTER_SYCHRONIZATION_DEFAULT)) {
			log.info("LTI Memberships synchronization disabled.");
			return;
		}

		if (isEmpty(contextGuid) ) {
			log.error("Context GUID is required. Memberships will NOT be synchronized");
			return;
		}

		Optional<Context> optContext = contextRepository.findById(contextGuid);
		Context context = null;
		if ( optContext.isPresent() ) {
			context = optContext.get();
		}

		if ( context == null ) {
			log.info("Context notfound {}", contextGuid);
			return;
		}

		String tenantGuid = context.getTenant().getId();
		String contextMemberships = context.getContextMemberships();

		if (isEmpty(tenantGuid)) {
			log.info("Context {} does not have a tenant.  Memberships will NOT be synchronized.", contextGuid);
			return;
		}

		if (isEmpty(contextMemberships)) {
			log.info("Context {} does not have Memberships URL.  Memberships will NOT be synchronized.", contextGuid);
			return;
		}

		String siteId = context.getSakaiSiteId();
		if (isEmpty(siteId) ) {
			log.error("Context {} is not associated with a site. Memberships will NOT be synchronized.", contextGuid);
			return;
		}

		Site site = null;
		try
		{
			site = siteService.getSite(siteId);
		}
		catch (IdUnusedException e)
		{
			log.error("Context {} could not load siteid={}. Memberships will NOT be synchronized.", contextGuid, siteId);
			return;
		}

		// Load the Tenant
		Optional<Tenant> optTenant = tenantRepository.findById(tenantGuid);
		Tenant tenant = null;
		if ( optTenant.isPresent() ) {
			tenant = optTenant.get();
		}

		if ( tenant == null ) {
			log.info("Context {} Tenant notfound {}", contextGuid, tenantGuid);
			return;
		}

		String clientId = tenant.getClientId();
		String deploymentId = context.getDeploymentId();
		String oidcTokenUrl = tenant.getOidcToken();
		String oidcAudience = tenant.getOidcAudience();
		if ( isEmpty(oidcAudience) ) oidcAudience = oidcTokenUrl;

		if (isEmpty(clientId)) {
			log.info("Tenant {} does not have clientId.  Memberships will NOT be synchronized.", tenantGuid);
			return;
		}

		if (isEmpty(deploymentId)) {
			log.info("Context {} does not have deploymentId.  Memberships will NOT be synchronized.", contextGuid);
			return;
		}

		if (isEmpty(oidcTokenUrl)) {
			log.info("Tenant {} does not have an OIDC Token URL.  Memberships will NOT be synchronized.", tenantGuid);
			return;
		}

		boolean isEmailTrustedConsumer = ! Boolean.FALSE.equals(tenant.getTrustEmail());

		// Prepare for Per-Context log
		ContextLog cLog = new ContextLog();
		cLog.setContext(context);
		cLog.setType(ContextLog.LOG_TYPE.NRPS_TOKEN);
		cLog.setAction("syncSiteMemberships getting access token from context="+context.getId()+" tenant="+context.getTenant()+" oidcTokenUrl="+oidcTokenUrl);
		cLog.setSuccess(Boolean.FALSE);

		// Paging through multiple Requests - Avoid infinite loop from broken LMS.
		int max_pages = 10;
		while( StringUtils.isNotEmpty(contextMemberships) ) {
			log.debug("Loading contextMemberships={}", contextMemberships);
			if ( max_pages-- <= 0 ) {
				log.error("Paging stopped after 10 pages context={} link={}", context.getId(), contextMemberships);
				break;
			}
			// Looks like we have the requisite strings in variables :)
			KeyPair keyPair = SakaiKeySetUtil.getCurrent();
			StringBuffer dbs = new StringBuffer();
			dbs.append("Getting NRPS Token...\n");
			AccessToken nrpsAccessToken = LTI13AccessTokenUtil.getNRPSToken(oidcTokenUrl, keyPair, clientId, deploymentId, oidcAudience, dbs);
			if ( nrpsAccessToken == null || isEmpty(nrpsAccessToken.access_token) ) {
				log.error(dbs.toString());
				log.error("Could not retrieve NRPS (Names and Roles) token from {}.  Memberships will NOT be synchronized.", oidcTokenUrl);
				cLog.setDebugLog(dbs.toString());
				contextLogRepository.save(cLog);
				return;
			}
			if ( verbose(tenant) ) {
				log.info("Debug Log:\n{}", dbs.toString());
			} else {
				log.debug("Debug Log:\n{}", dbs.toString());
			}

			cLog.setAction("syncSiteMemberships context="+context.getId()+" tenant="+context.getTenant()+" contextMemberships="+contextMemberships+" access_token="+nrpsAccessToken.access_token);

			Map<String, String> headers = new TreeMap<>();
			headers.put("Authorization", "Bearer "+nrpsAccessToken.access_token);
			headers.put("Accept", LTI13ConstantsUtil.MEDIA_TYPE_MEMBERSHIPS);
			headers.put("Content-Type", LTI13ConstantsUtil.MEDIA_TYPE_MEMBERSHIPS); // TODO: Remove when certification is fixed

			// Get ready
			context.setNrpsStart(Instant.now());
			context.setNrpsFinish(null);
			context.setNrpsCount(Long.valueOf(0));
			context.setNrpsStatus("Started");
			contextRepository.save(context);

			dbs = new StringBuffer();
			dbs.append("Loading Context Memberships...\n");
			InputStream is;
			 try {
				HttpResponse<InputStream> response = HttpClientUtil.sendGetStream(contextMemberships, null, headers, dbs);
				if ( verbose(tenant) ) {
					log.info("Debug Log:\n{}", dbs.toString());
				} else {
					log.debug("Debug Log:\n{}", dbs.toString());
				}
				is = response.body();

				HttpHeaders responseHeaders = response.headers();
				List<String> allValuesOfLink = responseHeaders.allValues("Link");
				log.debug("allValuesOfLink length={} content={}", allValuesOfLink.size(),allValuesOfLink);
				String nextLink = HttpUtil.extractLinkByRel(allValuesOfLink, "next");
				log.debug("nextLink={}", nextLink);

				// If this is not null, we will loop back up and continue to page in results for multi-request NRPS
				contextMemberships = null;
				if ( isNotEmpty(nextLink) ) {
					log.debug("Received Link / next header {}", nextLink);
					contextMemberships = nextLink;
				}
			} catch (Exception e) {
				log.error("Error retrieving NRPS (Names and Roles) data from {}", contextMemberships);
				cLog.setStatus("Error retrieving NRPS (Names and Roles) data");
				cLog.setDebugLog(dbs.toString());
				contextLogRepository.save(cLog);
				return;
			}

			// https://cassiomolin.com/2019/08/19/combining-jackson-streaming-api-with-objectmapper-for-parsing-json/
			// Create and configure an ObjectMapper instance
			ObjectMapper mapper = JacksonUtil.getLaxObjectMapper();

			cLog = new ContextLog();
			cLog.setContext(context);
			cLog.setType(ContextLog.LOG_TYPE.NRPS_LIST);
			cLog.setStatus("Started syncSiteMemberships at="+Instant.now());
			cLog.setSuccess(Boolean.TRUE);
			dbs = new StringBuffer();

			Long count = Long.valueOf(0);
			// Create a JsonParser instance
			try {
				JsonParser jsonParser = mapper.getFactory().createParser(is);

				// Check the first token
				String lastText = null;
				JsonToken nextToken = null;
				while (true) {
					nextToken =  jsonParser.nextToken();
					if ( nextToken == null ) break;
					if ( nextToken == JsonToken.START_ARRAY && "members".equals(lastText) ) break;
					lastText = jsonParser.getText();
				}

				while (true) {
					nextToken =  jsonParser.nextToken();
					if ( nextToken == null ) break;
					if ( nextToken == JsonToken.END_ARRAY ) break;
					Member member = mapper.readValue(jsonParser, Member.class);

					if ( verbose(tenant) ) {
						log.info("processing member={}",member.email);
					} else {
						log.debug("processing member={}",member.email);
					}

					count = count + 1;

					if ( count < 200 ) {
						dbs.append("processing member="+member.email+" user_id="+member.user_id+" count="+count+"\n");
					}

					SakaiLaunchJWT launchJWT = new SakaiLaunchJWT();
					launchJWT.subject = member.user_id;
					launchJWT.email = member.email;
					launchJWT.given_name = member.given_name;
					launchJWT.family_name = member.family_name;
					launchJWT.roles = member.roles;

					Subject subject = createOrUpdateSubject(tenant, member.user_id, launchJWT);
					if ( subject == null ) {
						log.error("Failed createOrUpdateSubject subject={}", member.user_id);
						dbs.append("Failed createOrUpdateSubject subject="+member.user_id);
						cLog.setSuccess(Boolean.FALSE);
						continue;
					}

					// Upsert the roles
					Membership membership = new Membership();
					membership.setSubject(subject);
					membership.setContext(context);
					membership.setUpdatedAt(Instant.now());
					String ltiRoles = launchJWT.getLTI11Roles();
					if ( StringUtils.isNotBlank(ltiRoles) ) membership.setLtiRoles(ltiRoles);
					membership = membershipRepository.upsert(membership);

					Map<String, String> payload = getPayloadFromLaunchJWT(tenant, launchJWT);
					payload.put("tenant_guid", contextGuid);
					payload.put("subject_guid", subject.getId());

					User user = userFinderOrCreator.findOrCreateUser(payload, false, isEmailTrustedConsumer);
					if ( user == null ) {
						log.error("Failed findOrCreateUser subject={}", member.user_id);
						dbs.append("Failed findOrCreateUser subject="+member.user_id);
						cLog.setSuccess(Boolean.FALSE);
						continue;
					}

					connectSubjectAndUser(subject, user);

					siteEmailPreferenceSetter.setupUserEmailPreferenceForSite(payload, user, site, false);

					site = siteMembershipUpdater.addOrUpdateSiteMembership(payload, false, user, site);

					cLog.setStatus("Completed syncSiteMemberships count="+count+" at="+Instant.now());
				}
			} catch (IOException | LTIException e) {
				log.error("Error processing contextMemberships stream context={}", contextGuid, e);
				cLog.setSuccess(Boolean.FALSE);
				cLog.setStatus("Exception processing Names and Roles data="+e.getMessage());
			}

			// If this is the last page, clean up inactive entries (i.e. like a week since the last NRPS retrieval)
			// We may be in a thread / task instead of a login context...
			if ( isEmpty(contextMemberships) ) {

				// setup a security advisor
				SecurityAdvisor adv = new SecurityAdvisor() {
					public SecurityAdvice isAllowed(String userId, String function,
							String reference) {
						return SecurityAdvice.ALLOWED;
					}
				};

				securityService.pushAdvisor(adv);
				try {
					int minutes = getInactiveExpireMinutes(context);
					List<Membership> deleted_memberships = removeSiteUsersMinutesOld(context, minutes);
					if ( deleted_memberships.size() > 0 ) {
						log.info("Inactive memberships removed {} from {}", deleted_memberships.size(), site.getId());
						dbs.append("Inactive memberships removed "+deleted_memberships.size()+" from "+site.getId());
					}
				} finally {
					securityService.popAdvisor(adv);
				}
			}

			// Update the job status
			context.setNrpsFinish(Instant.now());
			context.setNrpsCount(count);
			context.setNrpsStatus("Done");

			// Store the log entry
			cLog.setDebugLog(dbs.toString());
			contextRepository.save(context);
			contextLogRepository.save(cLog);
		}  /* end while paging loop */

	}

/*
{
  "id" : "https://lms.example.com/sections/2923/memberships",
  "context": {
	"id": "2923-abc",
	"label": "CPS 435",
	"title": "CPS 435 Learning Analytics",
  },
  "members" : [
	{
	  "status" : "Active",
	  "name": "Jane Q. Public",
	  "picture" : "https://platform.example.edu/jane.jpg",
	  "given_name" : "Jane",
	  "family_name" : "Doe",
	  "middle_name" : "Marie",
	  "email": "jane@platform.example.edu",
	  "user_id" : "0ae836b9-7fc9-4060-006f-27b2066ac545",
	  "lis_person_sourcedid": "59254-6782-12ab",
	  "roles": [
		"http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor"
	  ]
	}
  ]
}
 */

	/*
	 * Create a lineItem for a gradebook Column
	 */
	@Override
	public String createLineItem(Site site, Long assignmentId,
		final org.sakaiproject.grading.api.Assignment assignmentDefinition)
	{
		String contextGuid = site.getId();
		Optional<Context> optContext = contextRepository.findById(contextGuid);
		Context context = null;
		if ( optContext.isPresent() ) {
			context = optContext.get();
		}

		if ( context == null ) {
			log.info("Context notfound {}", contextGuid);
			return null;
		}

		String tenantGuid = context.getTenant().getId();
		String lineItemsUrl	= context.getLineItems();

		if (isEmpty(tenantGuid)) {
			log.info("Context {} does not have a tenant.  Scores will NOT be synchronized.", contextGuid);
			return null;
		}

		if (isEmpty(lineItemsUrl)) {
			log.info("Context {} does not have LineItems URL.  Scores will NOT be synchronized.", contextGuid);
			return null;
		}

		// Load the Tenant
		Optional<Tenant> optTenant = tenantRepository.findById(tenantGuid);
		Tenant tenant = null;
		if ( optTenant.isPresent() ) {
			tenant = optTenant.get();
		}

		if ( tenant == null ) {
			log.info("Tenant notfound {}", tenantGuid);
			return null;
		}

		String clientId = tenant.getClientId();
		String oidcTokenUrl = tenant.getOidcToken();
		String oidcAudience = tenant.getOidcAudience();
		String deploymentId = context.getDeploymentId();
		if ( isEmpty(oidcAudience) ) oidcAudience = oidcTokenUrl;

		if (isEmpty(clientId)) {
			log.info("Tenant {} does not have clientId.  Scores will NOT be synchronized.", tenantGuid);
			return null;
		}

		if (isEmpty(deploymentId)) {
			log.info("Tenant {} does not have deploymentId.  Scores will NOT be synchronized.", tenantGuid);
			return null;
		}

		if (isEmpty(oidcTokenUrl)) {
			log.info("Tenant {} does not have an OIDC Token URL.  Scores will NOT be synchronized.", contextGuid);
			return null;
		}

		// Create the lineItem to send.
		LineItem li = new LineItem();
		li.scoreMaximum = assignmentDefinition.getPoints();
		li.label = assignmentDefinition.getName();
		li.tag = "42";
		li.resourceId = assignmentId.toString();
		// li.startDateTime
		Date dueDate = assignmentDefinition.getDueDate();
		// Move to the end of the day
		dueDate = tweakDueDate(dueDate);
		// Move into the destination time zone
		String timeZone = tenant.getTimeZone();
		if ( dueDate != null && StringUtils.isNotBlank(timeZone) ) {
			dueDate = LTIUtil.shiftJVMDateToTimeZone(dueDate, timeZone);
		}
		if ( dueDate != null ) li.endDateTime = LTIUtil.getISO8601(dueDate);
		String body = li.prettyPrintLog();

		// Track this in our local database including success / failure of the LMS interaction
		org.sakaiproject.plus.api.model.LineItem dbli = new org.sakaiproject.plus.api.model.LineItem();
		dbli.setId(assignmentId);
		dbli.setContext(context);
		dbli.setScoreMaximum(li.scoreMaximum);
		dbli.setLabel(li.label);
		dbli.setTag(li.tag);
		dbli.setResourceId(li.resourceId);
		if ( dueDate != null ) dbli.setEndDateTime(dueDate.toInstant());
		dbli.setUpdatedAt(Instant.now());
		dbli.setSentAt(Instant.now());
		dbli.setSuccess(Boolean.FALSE);
		dbli.setStatus(null);
		dbli.setDebugLog(null);

		ContextLog cLog = new ContextLog();
		cLog.setContext(context);
		cLog.setType(ContextLog.LOG_TYPE.LineItem_TOKEN);
		cLog.setAction("createLineItem assignmentId="+assignmentId+" label="+li.label+" scoreMaximum="+li.scoreMaximum+" dueDate="+dueDate);

		// Looks like we have the requisite strings in variables :)
		// https://www.imsglobal.org/spec/lti-ags/v2p0/#creating-a-new-line-item
		KeyPair keyPair = SakaiKeySetUtil.getCurrent();
		StringBuffer dbs = new StringBuffer();
		AccessToken lineItemsAccessToken = LTI13AccessTokenUtil.getLineItemsToken(oidcTokenUrl, keyPair, clientId, deploymentId, oidcAudience, dbs);
		if ( lineItemsAccessToken == null || isEmpty(lineItemsAccessToken.access_token) ) {
			dbli.setStatus("Could not get LineItems token from "+oidcTokenUrl);
			dbli.setDebugLog(dbs.toString());
			lineItemRepository.save(dbli);
			log.error("Could not retrieve lineItems token from {}.  Scores will NOT be synchronized.", oidcTokenUrl);
			log.error(dbs.toString());
			cLog.setStatus(dbli.getStatus());
			cLog.setDebugLog(dbli.getDebugLog());
			contextLogRepository.save(cLog);
			return null;
		}
		dbs = new StringBuffer();
		dbs.append("Sending LineItem\n");

		// lineItem
		Map<String, String> headers = new TreeMap<>();
		headers.put("Authorization", "Bearer "+lineItemsAccessToken.access_token);
		headers.put("Content-Type", LineItem.MIME_TYPE);

		String method = "POST";

		try {
			HttpResponse<String> response = HttpClientUtil.sendBody(method, lineItemsUrl, body, headers, dbs);
			body = response.body();
			log.debug("CREATE RESPONSE BODY={}", body);
			dbs.append("response body\n");
			dbs.append(StringUtils.truncate(body, 1000));

			if ( verbose(tenant) ) {
				log.info("Debug Log:\n{}", dbs.toString());
			} else {
				log.debug("Debug Log:\n{}", dbs.toString());
			}
		} catch (Exception e) {
			dbli.setStatus("Error creating lineItem at "+lineItemsUrl+" "+e.getMessage());
			dbli.setDebugLog(dbs.toString());
			log.error(dbs.toString());
			log.error(dbli.getStatus());
			lineItemRepository.save(dbli);

			cLog.setStatus(dbli.getStatus());
			cLog.setDebugLog(dbli.getDebugLog());
			contextLogRepository.save(cLog);
			return null;
		}

		// Create and configure an ObjectMapper instance
		ObjectMapper mapper = JacksonUtil.getLaxObjectMapper();
		try {
			LineItem returnedItem = mapper.readValue(body, LineItem.class);

			if ( returnedItem != null ) {
				String lineItemId = returnedItem.id;
				if ( isNotEmpty(lineItemId) ) {
					dbli.setStatus("created lineitem id="+lineItemId);
					dbli.setSuccess(Boolean.TRUE);
					dbli.setDebugLog(dbs.toString());
					lineItemRepository.save(dbli);
					log.debug("Returning lineItemId={}", lineItemId);
					cLog.setSuccess(Boolean.TRUE);
					cLog.setDebugLog(dbli.getDebugLog());
					cLog.setType(ContextLog.LOG_TYPE.LineItem_CREATE);
					contextLogRepository.save(cLog);
					return lineItemId; // Caller saves this as appropriate
				}
				dbli.setStatus("did not find returned lineitem id");
				dbli.setDebugLog(dbs.toString());
				lineItemRepository.save(dbli);

				cLog.setStatus(dbli.getStatus());
				cLog.setType(ContextLog.LOG_TYPE.LineItem_ERROR);
				cLog.setDebugLog(dbli.getDebugLog());
				contextLogRepository.save(cLog);
			}
		} catch ( Exception e ) {
			dbli.setStatus("Error parsing lineItem at "+lineItemsUrl+" "+e.getMessage());
			log.error(dbs.toString());
			log.error(dbli.getStatus());
			dbli.setDebugLog(dbs.toString());
			lineItemRepository.save(dbli);

			cLog.setStatus(dbli.getStatus());
			cLog.setType(ContextLog.LOG_TYPE.LineItem_ERROR);
			cLog.setDebugLog(dbli.getDebugLog());
			contextLogRepository.save(cLog);
			return null;
		}

		// Store this locally
		lineItemRepository.save(dbli);

		cLog.setSuccess(Boolean.TRUE);
		cLog.setDebugLog(dbli.getDebugLog());
		cLog.setType(ContextLog.LOG_TYPE.LineItem_CREATE);
		contextLogRepository.save(cLog);

		return null;
	}

	/*
	 * Update a lineItem associated with a gradebook Column
	 */
	@Override
	public String updateLineItem(Site site,
		final org.sakaiproject.grading.api.Assignment assignmentDefinition)
	{
		if ( assignmentDefinition == null ) return null;
		Long assignmentId = assignmentDefinition.getId();
		String lineItemId =  assignmentDefinition.getLineItem();
		log.debug("updateLineItem site={} assignmentId={} lineItemId={}", site.getId(), assignmentId, lineItemId);

		String contextGuid = site.getId();
		Optional<Context> optContext = contextRepository.findById(contextGuid);
		Context context = null;
		if ( optContext.isPresent() ) {
			context = optContext.get();
		}

		if ( context == null ) {
			log.info("Context notfound {}", contextGuid);
			return null;
		}

		String tenantGuid = context.getTenant().getId();
		String lineItemsUrl	= context.getLineItems();

		if (isEmpty(tenantGuid)) {
			log.info("Context {} does not have a tenant.  Scores will NOT be synchronized.", contextGuid);
			return null;
		}

		if (isEmpty(lineItemsUrl)) {
			log.info("Context {} does not have LineItems URL.  Scores will NOT be synchronized.", contextGuid);
			return null;
		}

		// Load the Tenant
		Optional<Tenant> optTenant = tenantRepository.findById(tenantGuid);
		Tenant tenant = null;
		if ( optTenant.isPresent() ) {
			tenant = optTenant.get();
		}

		if ( tenant == null ) {
			log.info("Tenant notfound {}", tenantGuid);
			return null;
		}

		String clientId = tenant.getClientId();
		String deploymentId = context.getDeploymentId();
		String oidcTokenUrl = tenant.getOidcToken();
		String oidcAudience = tenant.getOidcAudience();
		if ( isEmpty(oidcAudience) ) oidcAudience = oidcTokenUrl;

		if (isEmpty(clientId)) {
			log.info("Tenant {} does not have clientId.  Scores will NOT be synchronized.", tenantGuid);
			return null;
		}

		if (isEmpty(deploymentId)) {
			log.info("Context {} does not have deploymentId.  Scores will NOT be synchronized.", contextGuid);
			return null;
		}

		if (isEmpty(oidcTokenUrl)) {
			log.info("Tenant {} does not have an OIDC Token URL.  Scores will NOT be synchronized.", tenantGuid);
			return null;
		}

		// Create the lineItem to send.
		LineItem li = new LineItem();
		li.scoreMaximum = assignmentDefinition.getPoints();
		li.label = assignmentDefinition.getName();
		li.tag = "42";
		li.resourceId = assignmentId.toString();
		// li.startDateTime
		Date dueDate = assignmentDefinition.getDueDate();
		// Move to the end of the day
		dueDate = tweakDueDate(dueDate);
		// Move into the destination time zone
		String timeZone = tenant.getTimeZone();
		if ( dueDate != null && StringUtils.isNotBlank(timeZone) ) {
			dueDate = LTIUtil.shiftJVMDateToTimeZone(dueDate, timeZone);
		}
		if ( dueDate != null ) li.endDateTime = LTIUtil.getISO8601(dueDate);
		String body = li.prettyPrintLog();

		// In Update
		// Check if we already have a lineitem in our database
		org.sakaiproject.plus.api.model.LineItem dbli = null;
		Optional<org.sakaiproject.plus.api.model.LineItem> optLineItem = lineItemRepository.findById(assignmentId);
		String restEndPoint = lineItemsUrl;
		String method = "POST";
		if ( optLineItem.isPresent() ) {
			dbli = optLineItem.get();
			restEndPoint = lineItemId;
			method = "PUT";
		} else {
			dbli = new org.sakaiproject.plus.api.model.LineItem();
		}

		// Track this in our local database including success / failure of the LMS interaction
		dbli.setId(assignmentId);
		dbli.setContext(context);
		dbli.setScoreMaximum(li.scoreMaximum);
		dbli.setLabel(li.label);
		dbli.setTag(li.tag);
		dbli.setResourceId(li.resourceId);
		if ( dueDate != null ) dbli.setEndDateTime(dueDate.toInstant());
		dbli.setUpdatedAt(Instant.now());
		dbli.setSentAt(Instant.now());
		dbli.setSuccess(Boolean.FALSE);
		dbli.setStatus(null);
		dbli.setDebugLog(null);

		ContextLog cLog = new ContextLog();
		cLog.setContext(context);
		cLog.setType(ContextLog.LOG_TYPE.LineItem_TOKEN);
		cLog.setAction("updateLineItem assignmentId="+assignmentId+" label="+li.label+" scoreMaximum="+li.scoreMaximum+" dueDate="+dueDate);

		// Looks like we have the requisite strings in variables :)
		// https://www.imsglobal.org/spec/lti-ags/v2p0/#creating-a-new-line-item
		KeyPair keyPair = SakaiKeySetUtil.getCurrent();
		StringBuffer dbs = new StringBuffer();
		AccessToken lineItemsAccessToken = LTI13AccessTokenUtil.getLineItemsToken(oidcTokenUrl, keyPair, clientId, deploymentId, oidcAudience, dbs);
		if ( lineItemsAccessToken == null || isEmpty(lineItemsAccessToken.access_token) ) {
			dbli.setStatus("Could not get LineItems token from "+oidcTokenUrl);
			dbli.setDebugLog(dbs.toString());
			lineItemRepository.save(dbli);
			log.error("Could not retrieve lineItems token from {}.  Scores will NOT be synchronized.", oidcTokenUrl);
			log.error(dbs.toString());
			cLog.setStatus(dbli.getStatus());
			cLog.setDebugLog(dbli.getDebugLog());
			contextLogRepository.save(cLog);
			return null;
		}
		dbs = new StringBuffer();
		dbs.append("Sending LineItem\n");

		// lineItem
		Map<String, String> headers = new TreeMap<>();
		headers.put("Authorization", "Bearer "+lineItemsAccessToken.access_token);
		headers.put("Content-Type", LineItem.MIME_TYPE);

		try {
			HttpResponse<String> response = HttpClientUtil.sendBody(method, restEndPoint, body, headers, dbs);
			body = response.body();
			log.debug("UPDATE RESPONSE BODY={}", body);
			dbs.append("response body\n");
			dbs.append(StringUtils.truncate(body, 1000));

			if ( verbose(tenant) ) {
			   log.info("Debug Log:\n{}", dbs.toString());
			} else {
			   log.debug("Debug Log:\n{}", dbs.toString());
			}
		} catch (Exception e) {
			dbli.setStatus("Error creating lineItem at "+lineItemsUrl+" "+e.getMessage());
			dbli.setDebugLog(dbs.toString());
			log.error(dbs.toString());
			log.error(dbli.getStatus());
			lineItemRepository.save(dbli);

			cLog.setStatus(dbli.getStatus());
			cLog.setDebugLog(dbli.getDebugLog());
			contextLogRepository.save(cLog);
			return null;
		}

		// Create and configure an ObjectMapper instance
		ObjectMapper mapper = JacksonUtil.getLaxObjectMapper();
		try {
			LineItem returnedItem = mapper.readValue(body, LineItem.class);

			if ( returnedItem != null ) {
				lineItemId = returnedItem.id;
				if ( isNotEmpty(lineItemId) ) {
					dbli.setStatus("created lineitem id="+lineItemId);
					dbli.setSuccess(Boolean.TRUE);
					dbli.setDebugLog(dbs.toString());
					lineItemRepository.save(dbli);
					cLog.setStatus(dbli.getStatus());
					cLog.setType(ContextLog.LOG_TYPE.LineItem_UPDATE);
					cLog.setDebugLog(dbli.getDebugLog());
					contextLogRepository.save(cLog);
					return lineItemId;
				}
				dbli.setStatus("did not find returned lineitem id");
				dbli.setDebugLog(dbs.toString());
				lineItemRepository.save(dbli);

				cLog.setStatus(dbli.getStatus());
				cLog.setType(ContextLog.LOG_TYPE.LineItem_ERROR);
				cLog.setDebugLog(dbli.getDebugLog());
				contextLogRepository.save(cLog);
			}
		} catch ( JsonProcessingException e ) {
			// If the PUT gave us no valid data it is no big deal
			if ( method.equals("PUT") ) {
				dbli.setStatus("No lineItem response to PUT at "+lineItemsUrl+" "+e.getMessage());
				log.debug(dbs.toString());
				log.debug(dbli.getStatus());
			} else {
				dbli.setStatus("Error parsing lineItem response to POST at "+lineItemsUrl+" "+e.getMessage());
				log.error(dbs.toString());
				log.error(dbli.getStatus());
			}
			dbli.setDebugLog(dbs.toString());
			lineItemRepository.save(dbli);

			cLog.setStatus(dbli.getStatus());
			cLog.setType(ContextLog.LOG_TYPE.LineItem_ERROR);
			cLog.setDebugLog(dbli.getDebugLog());
			contextLogRepository.save(cLog);
			return null;
		}

		// Store this locally
		lineItemRepository.save(dbli);

		cLog.setSuccess(Boolean.TRUE);
		cLog.setDebugLog(dbli.getDebugLog());
		cLog.setType(ContextLog.LOG_TYPE.LineItem_UPDATE);
		contextLogRepository.save(cLog);

		return null;
	}

	/*
	 * Send a score to the calling LMS
	 */
	// https://www.imsglobal.org/spec/lti-ags/v2p0#score-publish-service
	// https://www.imsglobal.org/spec/lti-ags/v2p0#comment-0
	@Transactional
	@Override
	public void processGradeEvent(Event event)
	{
		// /gradebookng/7/12/55a0c76a-69e2-4ca7-816b-3c2e8fe38ce0/42/OK/instructor[m, 2]
		log.debug("processGradeEvent event={} resource={}", event.getEvent(), event.getResource());
		String eventResource = event.getResource();
		if ( eventResource == null ) return;
		String[] parts = eventResource.split("/");

		if (parts.length < 6) return;

		final String source = parts[1];
		String itemId;
		String studentId;
		String scoreStr;
		String siteId;

		// From the UI business logic
		// /gradebookng/7/12/55a0c76a-69e2-4ca7-816b-3c2e8fe38ce0/42/OK/instructor[m, 2]
		//	   1	  2 3   4									5
		if ( "gradebookng".equals(source) ) {
			log.debug("processGradeEvent UI event={} resource={}", event.getEvent(), event.getResource());
			itemId = parts[3];
			studentId = parts[4];
			scoreStr = parts[5];
			siteId = event.getContext();
		// From a web service
		// /gradebook/a77ed1b6-ceea-4339-ad60-8bbe7219f3b5/Trophy/55a0c76a-69e2-4ca7-816b-3c2e8fe38ce0/99.0/student[m, 2]
		//	   1	  2									 3   4									5
		} else if ( "gradebook".equals(source) ) {
			log.debug("processGradeEvent WS event={} resource = {}", event.getEvent(), event.getResource());
			siteId = parts[2];
			itemId = parts[3];
			studentId = parts[4];
			scoreStr = parts[5];
		} else { // not our event...
			return;
		}

		log.debug("Updating score for user {} for item {} with score {} in gradebook {} by {}", studentId, itemId, scoreStr, siteId, source);

		// 2.4.4 scoreGiven and scoreMaximum
		// All scoreGiven values MUST be positive number (including 0). scoreMaximum represents the denominator
		// and MUST be present when scoreGiven is present. When scoreGiven is not present or null, this indicates
		// there is presently no score for that user, and the platform should clear any previous score value it
		// may have previously received from the tool and stored for that user and line item.
		Double scoreGiven;
		try {
			scoreGiven = Double.valueOf(scoreStr);
		} catch (NumberFormatException e) {
			scoreGiven = null;
		}

		Subject subject = subjectRepository.findBySakaiUserIdAndSakaiSiteId(studentId, siteId);
		log.debug("subject={}", subject);
		if ( subject == null ) {
			// This is debug because it is really just a local Sakai user w/o an email address
			log.debug("Can't retrieve subject for {}", studentId);
			return;
		}

		org.sakaiproject.grading.api.Assignment gradebookAssignment;
		try {
			gradebookAssignment = gradingService.getAssignmentByNameOrId(siteId, siteId, itemId);
		} catch (AssessmentNotFoundException anfe) {
			log.warn("Can't retrieve gradebook assignment for gradebook {} and item {}, {}", siteId, itemId, anfe.getMessage());
			return;
		}

		String lineItem = gradebookAssignment.getLineItem();
		if ( isEmpty(lineItem) ) {
			// This is info because it is really just a local gradebook column
			log.info("No lineItem for gradebookAssignment {}", gradebookAssignment.getId());
			return;
		}

		Long gradebookColumnId = gradebookAssignment.getId();
		CommentDefinition commentDef = gradingService.getAssignmentScoreComment(siteId, gradebookColumnId, studentId);
		String comment = null;
		if ( commentDef != null ) comment = commentDef.getCommentText();

		Tenant tenant = subject.getTenant();
		if ( tenant == null ) {
			log.error("Cannot find tenant for subject {}", subject.getId());
			return;
		}

		Optional<Context> optContext = contextRepository.findById(siteId);
		Context context = null;
		if ( optContext.isPresent() ) {
			context = optContext.get();
		}
		if ( context == null ) {
			log.error("Cannot find context for site {}", siteId);
			return;
		}

		String clientId = tenant.getClientId();
		String deploymentId = context.getDeploymentId();
		String oidcTokenUrl = tenant.getOidcToken();
		String oidcAudience = tenant.getOidcAudience();
		if ( isEmpty(oidcAudience) ) oidcAudience = oidcTokenUrl;

		Score score = new Score();
		score.scoreGiven = scoreGiven;
		score.scoreMaximum = gradebookAssignment.getPoints();
		score.comment = comment;
		score.userId = subject.getSubject();
		score.timestamp = LTIUtil.getISO8601();

		// TODO: Think more about this - Canvas requires this but we don't know what various values mean in Canvas
		// TODO: Review the Blackboard state diagram
		score.activityProgress = Score.ACTIVITY_COMPLETED;
		score.gradingProgress = Score.GRADING_FULLYGRADED;

		// Delete any old "in-flight" score update, only keep the latest
		scoreRepository.deleteBySubjectAndColumn(subject, gradebookColumnId);

		// Track this in our local database including success / failure of the LMS interaction
		org.sakaiproject.plus.api.model.Score dbsc = new org.sakaiproject.plus.api.model.Score();
		dbsc.setGradeBookColumnId(gradebookColumnId);
		dbsc.setSubject(subject);

		dbsc.setScoreGiven(score.scoreGiven);
		dbsc.setScoreMaximum(score.scoreMaximum);
		dbsc.setComment(score.comment);
		dbsc.setUpdatedAt(Instant.now());
		dbsc.setSentAt(Instant.now());
		dbsc.setSuccess(Boolean.FALSE);
		dbsc.setStatus(null);
		dbsc.setDebugLog(null);

		// Prepare for Per-Context log
		ContextLog cLog = new ContextLog();
		cLog.setContext(context);
		cLog.setSubject(subject);
		cLog.setType(ContextLog.LOG_TYPE.Score_TOKEN);
		cLog.setAction("processGradeEvent siteId="+siteId+" itemId="+itemId+" studentId="+studentId+" scoreGiven="+score.scoreGiven);
		cLog.setSuccess(Boolean.FALSE);

		// Lets get an access token if we can so we can send the score
		KeyPair keyPair = SakaiKeySetUtil.getCurrent();
		StringBuffer dbs = new StringBuffer();
		AccessToken scoreAccessToken = LTI13AccessTokenUtil.getScoreToken(oidcTokenUrl, keyPair, clientId, deploymentId, oidcAudience, dbs);
		if ( scoreAccessToken == null || isEmpty(scoreAccessToken.access_token) ) {
			log.info("Could not retrieve score token from {}.  Scores will NOT be synchronized.", oidcTokenUrl);
			dbsc.setStatus("Could not get score token from "+oidcTokenUrl);
			dbsc.setSuccess(Boolean.FALSE);
			dbsc.setDebugLog(dbs.toString());
			scoreRepository.save(dbsc);

			cLog.setStatus(dbsc.getStatus());
			cLog.setDebugLog(dbsc.getDebugLog());
			contextLogRepository.save(cLog);
			return;
		}
		if ( verbose(tenant) ) {
			log.info("Debug Log:\n{}", dbs.toString());
		} else {
			log.debug("Debug Log:\n{}", dbs.toString());
		}

		// Lets send a score
		// https://www.imsglobal.org/spec/lti-ags/v2p0#score-publish-service
		// https://www.imsglobal.org/spec/lti-ags/v2p0#comment-0
		Map<String, String> headers = new TreeMap<String, String>();
		headers.put("Authorization", "Bearer "+scoreAccessToken.access_token);
		headers.put("Content-Type", Score.MIME_TYPE);

		String body = score.prettyPrintLog();
		String scoreUrl = LTI13Util.getScoreUrlForLineItem(lineItem);
		dbs = new StringBuffer();
		dbs.append("Sending score\n");

		 try {
			HttpResponse<String> response = HttpClientUtil.sendBody("POST", scoreUrl, body, headers, dbs);
			body = response.body();
			log.debug("GRADEEVENT RESPONSE BODY={}", body);
			dbs.append("response body\n");
			dbs.append(StringUtils.truncate(body, 1000));
			dbsc.setDebugLog(dbs.toString());
			if ( verbose(tenant) ) {
				log.info("Debug Log:\n{}", dbs.toString());
			} else {
				log.debug("Debug Log:\n{}", dbs.toString());
			}
			dbsc.setSuccess(Boolean.TRUE);
			scoreRepository.save(dbsc);

			cLog.setStatus(dbsc.getStatus());
			cLog.setDebugLog(dbsc.getDebugLog());
			cLog.setSuccess(Boolean.TRUE);
			cLog.setType(ContextLog.LOG_TYPE.Score_SEND);
			contextLogRepository.save(cLog);
		} catch (Exception e) {
			log.error("Error setting score at {}", scoreUrl);
			dbsc.setStatus("Error setting score at url="+oidcTokenUrl+" message="+e.getMessage());
			dbsc.setSuccess(Boolean.FALSE);
			dbsc.setDebugLog(dbs.toString());
			scoreRepository.save(dbsc);

			cLog.setStatus(dbsc.getStatus());
			cLog.setType(ContextLog.LOG_TYPE.Score_SEND);
			cLog.setDebugLog(dbsc.getDebugLog());
			contextLogRepository.save(cLog);
		}

	}

	/*
	 * Get "old" memberships that are still in the Realm
	 */
	@Transactional(readOnly = true)
	@Override
	public List<Membership> getSiteUsersMinutesOld(Context context, int minutes)
	{
		return walkSiteUsersMinutesOld(context, minutes, false);
	}

	/*
	 * Remove "old" memberships that are still in the Realm
	 *
	 * Returns a list of the memberships that were removed from the Realm.  The memberships
	 * continue to exist - just the realm entries are removed.  Is a user shows up in an NRPS
	 * retrieval or launches into a Plus Site, since remove from the Realm does not remove
	 * user activity data from a Site, they are re-added to the Realm and no data is lost.
	 */
	@Transactional
	@Override
	public List<Membership> removeSiteUsersMinutesOld(Context context, int minutes)
	{
		return walkSiteUsersMinutesOld(context, minutes, true);
	}

	/*
	 * Walk through users in the realm that have "old" membership entries and optionally remove
	 * or list them.
	 */
	private List<Membership> walkSiteUsersMinutesOld(Context context, int minutes, boolean remove)
	{
		List<Membership> old_memberships = new ArrayList<Membership>();

		Site site = null;
		String sakaiSite = context.getSakaiSiteId();
		try
		{
			site = siteService.getSite(sakaiSite);
		}
		catch (IdUnusedException e)
		{
			log.warn("Cannot find site {}", sakaiSite);
			return old_memberships;
		}

		Set<String> users = site.getUsers();
		if ( users == null ) return old_memberships;

		// Filter through the realm
		boolean changed = false;
		List<Membership> memberships = membershipRepository.getEntriesMinutesOld(context, minutes);
		for (Membership membership : memberships) {
			String userId = membership.getSubject().getSakaiUserId();
			if ( users.contains(userId) ) {
				if ( remove ) {
					log.debug("Removing {} from site {}", userId, sakaiSite);
					site.removeMember(userId);
					changed = true;
				}
				old_memberships.add(membership);
			}

		}

		try {
			siteService.save(site);
			log.debug("Site saved site={}", site.getId());
		} catch (Exception e) {
			log.warn("Failed to save site={}", site.getId());
		}

	   return old_memberships;
	}

	/*
	 * Get the number of minutes to use when expiring inactive users for a site
	 */
	public Site getSite(Context context)
	{
		String sakaiSiteId	= context.getSakaiSiteId();
		try
		{
			Site site = siteService.getSite(sakaiSiteId);
			return site;
		}
		catch (IdUnusedException e)
		{
			log.warn("Cannot find site {}", sakaiSiteId);
			return null;
		}
	}

	/*
	 * Get the number of minutes to use when expiring inactive users for a site
	 */
	public int getInactiveExpireMinutes(Context context)
	{
		Site site = getSite(context);
		if ( site == null ) return MEMBERSHIP_EXPIRE_MINUTES;
		String minstr = (String) site.getProperties().get(PLUS_MEMBERSHIP_EXPIRE_MINUTES);
		int minutes = NumberUtils.toInt(minstr, MEMBERSHIP_EXPIRE_MINUTES);
		return minutes;
	}

	/*
	 * Get the number of seconds to use re-retrieving a roster via NRPS
	 *
	 * Initially this is just based on whether the user is an instructor or student
	 * but in the future, we can consider things like time since the context
	 * was created, time since the last syncronization was done, or the number
	 * of members in the context.
	 */
	public long getNRPSDelaySeconds(Context context, boolean instructor)
	{
		Site site = getSite(context);
		if ( site == null ) return DELAY_NRPS_LEARNER_SECONDS;
		String minstr = (String) site.getProperties().get(PLUS_NRPS_DELAY_SECONDS);
		long minutes = instructor ? DELAY_NRPS_INSTRUCTOR_SECONDS : DELAY_NRPS_LEARNER_SECONDS;
		minutes = NumberUtils.toLong(minstr, minutes);
		return minutes;
	}


/*

https://www.imsglobal.org/spec/lti-ags/v2p0#score-service-media-type-and-schema

POST lineitem URL/scores
Content-Type: application/vnd.ims.lis.v1.score+json
Authentication: Bearer 89042.hfkh84390xaw3m
{
  "timestamp": "2017-04-16T18:54:36.736+00:00",
  "scoreGiven" : 83,
  "scoreMaximum" : 100,
  "comment" : "This is exceptional work.",
  "activityProgress" : "Completed",
  "gradingProgress": "FullyGraded",
  "userId" : "5323497"
}
 *
*/
	// Advance any time on the due date to 23:59 on the due date
	protected static Date tweakDueDate(Date dueDate)
	{
		if ( dueDate == null ) return dueDate;

		Calendar cal = Calendar.getInstance();
		cal.setTime(dueDate);
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		dueDate = cal.getTime();
		return dueDate;
	}

}

