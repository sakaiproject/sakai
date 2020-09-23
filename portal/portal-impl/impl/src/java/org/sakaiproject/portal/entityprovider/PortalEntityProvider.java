/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.portal.entityprovider;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.portal.api.BullhornService;
import org.sakaiproject.portal.beans.BullhornAlert;
import org.sakaiproject.portal.beans.PortalNotifications;
import org.sakaiproject.profile2.logic.ProfileConnectionsLogic;
import org.sakaiproject.profile2.logic.ProfileLinkLogic;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.model.BasicConnection;
import org.sakaiproject.profile2.model.SocialNetworkingInfo;
import org.sakaiproject.profile2.model.UserProfile;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.search.api.SearchList;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.Resource;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.velocity.util.SLF4JLogChute;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * An entity provider to serve Portal information
 * 
 */
@Setter @Slf4j
public class PortalEntityProvider extends AbstractEntityProvider implements AutoRegisterEntityProvider, Outputable, ActionsExecutable, Describeable {

	public final static String PREFIX = "portal";
	public final static String TOOL_ID = "sakai.portal";
	private final static String CONNECTIONSEARCH_CACHE = "org.sakaiproject.portal.entityprovider.connectionSearchCache";
	private final static String WORKSPACE_IDS_KEY = "workspaceIds";

	private static final ResourceLoader rl = new ResourceLoader("bullhorns");

	private BullhornService bullhornService;
	private MemoryService memoryService;
	private SearchService searchService;
	private SessionManager sessionManager;
	private SiteService siteService;
	private ProfileConnectionsLogic profileConnectionsLogic;
	private ProfileLogic profileLogic;
	private ProfileLinkLogic profileLinkLogic;
	private ServerConfigurationService serverConfigurationService;
	private UserDirectoryService userDirectoryService;

	private Template formattedProfileTemplate = null;

	public void init() {

		VelocityEngine ve = new VelocityEngine();
		ve.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM, new SLF4JLogChute());

		ve.setProperty("resource.loader", "class");
		ve.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		// No logging. (If you want to log, you need to set an approrpriate directory in an approrpriate
		// velocity.properties file, or the log will be created in the directory in which tomcat is started, or
		// throw an error if it can't create/write in that directory.)
		ve.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogSystem");
		try {
			ve.init();
			formattedProfileTemplate = ve.getTemplate("org/sakaiproject/portal/entityprovider/profile-popup.vm");
		} catch (Exception e) {
			log.error("Failed to load profile-popup.vm", e);
		}
	}

	public String getEntityPrefix() {
		return PREFIX;
	}

	public String getAssociatedToolId() {
		return TOOL_ID;
	}

	public String[] getHandledOutputFormats() {
		return new String[] { Formats.TXT ,Formats.JSON, Formats.HTML};
	}

	// So far all we do is errors to solve SAK-29531
	// But this could be extended...
	@EntityCustomAction(action = "notify", viewKey = "")
	public PortalNotifications handleNotify(EntityView view) {
		Session s = sessionManager.getCurrentSession();
		if ( s == null ) {
			throw new IllegalArgumentException("Session not found");
		}
		List<String> retval = new ArrayList<String> ();
                String userWarning = (String) s.getAttribute("userWarning");
                if (StringUtils.isNotEmpty(userWarning)) {
			retval.add(userWarning);
		}
		PortalNotifications noti = new PortalNotifications ();
		noti.setError(retval);
		s.removeAttribute("userWarning");
		return noti;
	}

	@EntityCustomAction(action = "bullhornAlerts", viewKey = EntityView.VIEW_LIST)
	public ActionReturn getBullhornAlerts(EntityView view) {

		List<BullhornAlert> alerts = bullhornService.getAlerts(getCheckedCurrentUser());

		if (alerts.size() > 0) {
			Map<String, Object> data = new HashMap<>();
			data.put("alerts", alerts);
			data.put("i18n", rl);

			return new ActionReturn(data);
		} else {
			Map<String, String> i18n = new HashMap<>();
			i18n.put("noAlerts", rl.getString("noAlerts"));

			Map<String, Object> data = new HashMap<>();
			data.put("message", "NO_ALERTS");
			data.put("i18n", i18n);

			return new ActionReturn(data);
		}
	}

	@EntityCustomAction(action = "clearBullhornAlert", viewKey = EntityView.VIEW_LIST)
	public boolean clearBullhornAlert(Map<String, Object> params) {

		String currentUserId = getCheckedCurrentUser();

		try {
			long alertId = Long.parseLong((String) params.get("id"));
			return bullhornService.clearAlert(currentUserId, alertId);
		} catch (Exception e) {
			log.error("Failed to clear bullhorn alert", e);
		}

		return false;
	}

	@EntityCustomAction(action = "clearAllBullhornAlerts", viewKey = EntityView.VIEW_LIST)
	public boolean clearAllBullhornAlerts(Map<String, Object> params) {

		String currentUserId = getCheckedCurrentUser();

		try {
			return bullhornService.clearAllAlerts(currentUserId);
		} catch (Exception e) {
			log.error("Failed to clear all bullhorn alerts", e);
		}

		return false;
	}

	@EntityCustomAction(action = "bullhornAlertCount", viewKey = EntityView.VIEW_LIST)
	public ActionReturn getBullhornAlertCount(EntityView view) {

		String currentUserId = getCheckedCurrentUser();
		return new ActionReturn(bullhornService.getAlertCount(currentUserId));
	}

	private String getCheckedCurrentUser() throws SecurityException {

		String currentUserId = developerHelperService.getCurrentUserId();

		if (StringUtils.isBlank(currentUserId)) {
			throw new SecurityException("You must be logged in to use this service");
		} else {
            return currentUserId;
        }
    }

	@EntityCustomAction(action="formatted",viewKey=EntityView.VIEW_SHOW)
	public ActionReturn getFormattedProfile(EntityReference ref, Map<String, Object> params) {

		String currentUserId = getCheckedCurrentUser();

		ResourceLoader rl = Resource.getResourceLoader("org.sakaiproject.portal.api.PortalService", "profile-popup");

		UserProfile userProfile = (UserProfile) profileLogic.getUserProfile(ref.getId());

		String connectionUserId = userProfile.getUserUuid();

		VelocityContext context = new VelocityContext();
		context.put("i18n", rl);
		context.put("profileUrl", profileLinkLogic.getInternalDirectUrlToUserProfile(connectionUserId));

		SocialNetworkingInfo socialInfo = userProfile.getSocialInfo();
		String facebookUrl = "";
		String twitterUrl = "";
		if(socialInfo != null) {
			if(socialInfo.getFacebookUrl() != null) {
				facebookUrl = socialInfo.getFacebookUrl();
			}
			if(socialInfo.getTwitterUrl() != null) {
				twitterUrl = socialInfo.getTwitterUrl();
			}
		}
		context.put("facebookUrl", facebookUrl);
		context.put("twitterUrl", twitterUrl);

		String email = userProfile.getEmail();
		if (StringUtils.isEmpty(email)) email = "";
		context.put("email", email);

		context.put("currentUserId", currentUserId);
		context.put("connectionUserId", connectionUserId);

        boolean connectionsEnabled = serverConfigurationService.getBoolean("profile2.connections.enabled",
            ProfileConstants.SAKAI_PROP_PROFILE2_CONNECTIONS_ENABLED);

        if (connectionsEnabled && !currentUserId.equals(connectionUserId)) {
            int connectionStatus = profileConnectionsLogic.getConnectionStatus(currentUserId, connectionUserId);

            if (connectionStatus == ProfileConstants.CONNECTION_CONFIRMED) {
                context.put("connected" , true);
            } else if (connectionStatus == ProfileConstants.CONNECTION_REQUESTED) {
                context.put("requested" , true);
            } else if (connectionStatus == ProfileConstants.CONNECTION_INCOMING) {
                context.put("incoming" , true);
            } else {
                context.put("unconnected" , true);
            }
        }

		StringWriter writer = new StringWriter();

		try {
			formattedProfileTemplate.merge(context, writer);
			return new ActionReturn(Formats.UTF_8, Formats.HTML_MIME_TYPE, writer.toString());
		} catch (IOException ioe) {
			throw new EntityException("Failed to format profile.", ref.getReference());
		}
	}

	@EntityCustomAction(action="connectionsearch",viewKey=EntityView.VIEW_LIST)
	public ActionReturn searchForConnections(Map<String, Object> params) {

		String currentUserId = getCheckedCurrentUser();

		String query = (String) params.get("query");
		if (StringUtils.isBlank(query)) {
			throw new EntityException("No query supplied", "");
		}

		try {
			Cache cache = getCache(CONNECTIONSEARCH_CACHE);

			List<String> workspaceIds = (List<String>) cache.get(WORKSPACE_IDS_KEY);

			if (workspaceIds == null) {
				log.debug("Cache MISS on {}.", WORKSPACE_IDS_KEY);
				List<String> all = siteService.getSiteIds(SelectionType.ANY, null, null, null, null, null);
				workspaceIds = all.stream().filter(id -> id.startsWith("~")).collect(Collectors.toList());
				cache.put(WORKSPACE_IDS_KEY, workspaceIds);
			} else {
				log.debug("Cache HIT on {}.", WORKSPACE_IDS_KEY);
			}

			if (log.isDebugEnabled()) {
				workspaceIds.forEach(id -> log.debug("workspace id: {}", id));
			}

			SearchList results = searchService.search(query, workspaceIds, 0, 100);

			Set<BasicConnection> hits = results.stream().filter(r -> "profile".equals(r.getTool()))
				.map(r ->
					{
						try {
							return connectionFromUser(userDirectoryService.getUser(r.getId()));
						} catch (UserNotDefinedException unde) {
							log.error("No user for id " + r.getId() + ". Returning null ...");
							return null;
						} catch (Exception e) {
							log.error("Exception caught whilst looking up user " + r.getId() + ". Returning null ...", e);
							return null;
						}
					}).collect(Collectors.toSet());

			if (log.isDebugEnabled()) {
				hits.forEach(hit -> log.debug("User ID: " + hit.getUuid()));
			}

			// Now search the users. TODO: Move to ElasticSearch eventually.
			List<User> users = userDirectoryService.searchUsers(query, 1, 100);
			users.addAll(userDirectoryService.searchExternalUsers(query, 1, 100));

			hits.addAll(users.stream().map(u -> { return connectionFromUser(u); }).collect(Collectors.toList()));

			return new ActionReturn(hits);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return null;
	}

	private Cache getCache(String name) {

		try {
			return memoryService.getCache(name);
		} catch (Exception e) {
			log.error("Exception whilst retrieving '" + name + "' cache. Returning null ...", e);
			return null;
		}
	}

    private BasicConnection connectionFromUser(User u) {

		BasicConnection bc = new BasicConnection();
		bc.setUuid(u.getId());
		bc.setDisplayName(u.getDisplayName());
		bc.setEmail(u.getEmail());
		bc.setProfileUrl(profileLinkLogic.getInternalDirectUrlToUserProfile(u.getId()));
		bc.setType(u.getType());
		bc.setSocialNetworkingInfo(profileLogic.getSocialNetworkingInfo(u.getId()));
		return bc;
    }
}
