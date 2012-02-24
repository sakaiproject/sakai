/**
 * $Id$
 * $URL$
 * DeveloperHelperServiceImpl.java - entity-broker - Apr 13, 2008 6:30:08 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.entitybroker.impl.devhelper;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.entitybroker.EntityBrokerManager;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.util.SakaiToolData;
import org.sakaiproject.entitybroker.util.devhelper.AbstractDeveloperHelperService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;

/**
 * implementation of the helper service methods
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class DeveloperHelperServiceImpl extends AbstractDeveloperHelperService {

    protected DeveloperHelperServiceImpl() {}

    /**
     * Full constructor
     * @param entityBroker
     * @param entityBrokerManager
     * @param authzGroupService
     * @param functionManager
     * @param securityService
     * @param serverConfigurationService
     * @param sessionManager
     * @param siteService
     * @param toolManager
     * @param userDirectoryService
     */
    public DeveloperHelperServiceImpl(EntityBroker entityBroker,
            EntityBrokerManager entityBrokerManager, 
            AuthzGroupService authzGroupService,
            FunctionManager functionManager, SecurityService securityService,
            ServerConfigurationService serverConfigurationService, SessionManager sessionManager,
            SiteService siteService, ToolManager toolManager,
            UserDirectoryService userDirectoryService) {
        super(entityBroker, entityBrokerManager);
        this.authzGroupService = authzGroupService;
        this.functionManager = functionManager;
        this.securityService = securityService;
        this.serverConfigurationService = serverConfigurationService;
        this.sessionManager = sessionManager;
        this.siteService = siteService;
        this.toolManager = toolManager;
        this.userDirectoryService = userDirectoryService;
    }

    /**
     * Location id for the Sakai Gateway site
     */
    public static String GATEWAY_ID = "!gateway";

    /**
     * The portal base URL
     */
    public static String PORTAL_BASE = "/portal";

    protected final String CURRENT_USER_MARKER = "originalCurrentUser";

    // SAKAI
    private AuthzGroupService authzGroupService;
    private FunctionManager functionManager;
    private SecurityService securityService;
    private ServerConfigurationService serverConfigurationService;
    private SessionManager sessionManager;
    private SiteService siteService;
    private ToolManager toolManager;
    private UserDirectoryService userDirectoryService;
    private ThreadLocalManager threadLocalManager;


    // ENTITY

  	@Override
    public Object fetchEntity(String reference) {
        Object entity = super.fetchEntity(reference);
        if (entity == null 
                && reference.startsWith("/user")) {
            // this sucks but legacy user cannot be resolved for some reason 
            // so look up directly since it is one of the top entities being fetched
            String userId = getUserIdFromRef(reference);
            if (userId != null) {
                try {
                    entity = userDirectoryService.getUser(userId);
                } catch (UserNotDefinedException e) {
                    entity = null;
                }
            }
        }
        return entity;
    }

    public String setCurrentUser(String userReference) {
        if (userReference == null) {
            throw new IllegalArgumentException("userReference cannot be null");
        }
        String userId = getUserIdFromRef(userReference);
        try {
            // make sure the user id is valid
            userDirectoryService.getUser(userId);
        } catch (UserNotDefinedException e) {
            throw new IllegalArgumentException("Invalid user reference ("+userReference+"), could not find user");
        }
        Session currentSession = sessionManager.getCurrentSession();
        if (currentSession == null) {
            // start a session if none is around
            currentSession = sessionManager.startSession(userId);
        }
        String currentUserId = currentSession.getUserId();
        if (currentSession.getAttribute(CURRENT_USER_MARKER) == null) {
            // only set this if it is not already set
            if (currentUserId == null) {
                currentUserId = "";
            }
            currentSession.setAttribute(CURRENT_USER_MARKER, currentUserId);
        }
        currentSession.setUserId(userId);
        currentSession.setActive();
        sessionManager.setCurrentSession(currentSession);
        authzGroupService.refreshUser(userId);
        return getUserRefFromUserId(currentUserId);
    }

    public String restoreCurrentUser() {
        // switch user session back if it was taken over
        Session currentSession = sessionManager.getCurrentSession();
        String currentUserId = null;
        if (currentSession != null) {
            currentUserId = (String) currentSession.getAttribute(CURRENT_USER_MARKER);
            if (currentUserId != null) {
                currentSession.removeAttribute(CURRENT_USER_MARKER);
                currentSession.setUserId(currentUserId);
                authzGroupService.refreshUser(currentUserId);
                sessionManager.setCurrentSession(currentSession);
            }
            if ("".equals(currentUserId)) {
                currentUserId = null;
            }
        }
        return getUserRefFromUserId(currentUserId);
    }


    // CONFIG

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.DeveloperHelperService#getConfigurationSetting(java.lang.String, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public <T> T getConfigurationSetting(String settingName, T defaultValue) {
        T returnValue = defaultValue;
        if (SETTING_SERVER_NAME.equals(settingName)) {
            returnValue = (T) serverConfigurationService.getServerName();
        } else if (SETTING_SERVER_URL.equals(settingName)) {
            returnValue = (T) serverConfigurationService.getServerUrl();
        } else if (SETTING_PORTAL_URL.equals(settingName)) {
            returnValue = (T) serverConfigurationService.getPortalUrl();
        } else if (SETTING_SERVER_ID.equals(settingName)) {
            returnValue = (T) serverConfigurationService.getServerIdInstance();
        } else {
            if (defaultValue == null) {
                returnValue = (T) serverConfigurationService.getString(settingName);
                if ("".equals(returnValue)) { returnValue = null; }
            } else {
                if (defaultValue instanceof Number) {
                    int num = ((Number) defaultValue).intValue();
                    int value = serverConfigurationService.getInt(settingName, num);
                    returnValue = (T) Integer.valueOf(value);
                } else if (defaultValue instanceof Boolean) {
                    boolean bool = ((Boolean) defaultValue).booleanValue();
                    boolean value = serverConfigurationService.getBoolean(settingName, bool);
                    returnValue = (T) Boolean.valueOf(value);
                } else if (defaultValue instanceof String) {
                    returnValue = (T) serverConfigurationService.getString(settingName, (String) defaultValue);
                }
            }
        }
        return returnValue;
    }


    // USER

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.DeveloperHelperService#getCurrentLocale()
     */
    @Override
    public Locale getCurrentLocale() {
        return new ResourceLoader().getLocale();
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.DeveloperHelperService#getCurrentUserReference()
     */
    public String getCurrentUserReference() {
        String userId = sessionManager.getCurrentSessionUserId();
        return getUserRefFromUserId(userId);
    }

    public String getCurrentUserId() {
        String userId = sessionManager.getCurrentSessionUserId();
        return userId;
    }

    public String getUserRefFromUserEid(String userEid) {
        String userRef = null;
        try {
            User u = userDirectoryService.getUserByEid(userEid);
            userRef = u.getReference();
        } catch (UserNotDefinedException e) {
            userRef = null;
        }
        return userRef;
    }

    // LOCATION

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.DeveloperHelperService#getCurrentLocationReference()
     */
    public String getCurrentLocationReference() {
        String location = null;
        try {
            String context = toolManager.getCurrentPlacement().getContext();
            Site s = siteService.getSite( context );
            location = s.getReference(); // get the entity reference to the site
        } catch (Exception e) {
            // sakai failed to get us a location so we can assume we are not inside the portal
            location = null;
        }
        return location;
    }

    public String getCurrentLocationId() {
        String locationId = null;
        try {
            String context = toolManager.getCurrentPlacement().getContext();
            locationId = context;
        } catch (Exception e) {
            // sakai failed to get us a location so we can assume we are not inside the portal
            locationId = null;
        }
        return locationId;
    }

    public String getStartingLocationReference() {
        return GROUP_BASE + GATEWAY_ID;
    }

    // TOOLS

    public String getCurrentToolReference() {
        String toolRef = null;
        String toolId = toolManager.getCurrentTool().getId();
        // assume the form /tool/toolId
        if (toolId != null) {
            toolRef = new EntityReference("tool", toolId).toString();
        }
        return toolRef;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.DeveloperHelperService#getToolData(java.lang.String, java.lang.String)
     */
    public SakaiToolData getToolData(String toolRegistrationId, String locationReference) {
        SakaiToolData toolData = new SakaiToolData();
        if (locationReference == null) {
            locationReference = getCurrentLocationReference();
        }
        toolData.setLocationReference(locationReference);

        String locationId = getLocationIdFromRef(locationReference);
        Site site = null;
        try {
            site = siteService.getSite( locationId );
        } catch (IdUnusedException e) {
            throw new IllegalArgumentException("Could not find a site by locationId=" + locationId, e);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not locate tool"
                    + " in location=" + locationReference
                    + " with toolRegistrationId=" + toolRegistrationId, e);
        }
        toolData.setRegistrationId(toolRegistrationId);

        // get the pages for this site
        List<SitePage> pages = site.getOrderedPages();
        for (SitePage page : pages) {
            // get the tool configs for each
            for (ToolConfiguration tc : (List<ToolConfiguration>) page.getTools(0)) {
                // get the tool from column 0 for this tool config (if there is one)
                Tool tool = tc.getTool();
                if (tool != null 
                        && tool.getId().equals(toolRegistrationId)) {
                    // hardcoding to make this backwards compatible with 2.3 - ServerConfigurationService.CURRENT_PORTAL_PATH, PORTAL_BASE);
                    String portalBase = (String) threadLocalManager.get("sakai:request.portal.path");
                    if (portalBase == null || "".equals(portalBase)) {
                        // this has to be here because the tc will expect it when the portal urls are generated and fail if it is missing -AZ
                        threadLocalManager.set("sakai:request.portal.path", PORTAL_BASE);
                    }
                    // back to normal stuff again
                    toolData.setToolURL(page.getUrl());
                    toolData.setPlacementId(tc.getId());
                    toolData.setTitle(tool.getTitle());
                    toolData.setDescription(tool.getDescription());
                }
            }
        }

        if (toolData.getPlacementId() == null) {
            throw new IllegalArgumentException("Could not locate tool"
                    + " in location=" + locationReference
                    + " with toolRegistrationId=" + toolRegistrationId);
        }
        return toolData;
    }

    // URLS

    public String getPortalURL() {
        return serverConfigurationService.getPortalUrl();
    }

    public String getServerURL() {
        return serverConfigurationService.getServerUrl();
    }

    public String getToolViewURL(String toolRegistrationId, String localView,
            Map<String, String> parameters, String locationReference) {
        if (toolRegistrationId == null || "".equals(toolRegistrationId)) {
            throw new IllegalArgumentException("toolRegistrationId must be set and cannot be null or blank");
        }

        SakaiToolData info = getToolData(toolRegistrationId, locationReference);

        StringBuilder viewURL = new StringBuilder();
        if (localView == null || "".equals(localView)) {
            // do nothing
        } else {
            viewURL.append(localView);
        }

        // build the params map into a string
        boolean firstParamUsed = false;
        if (parameters != null && parameters.size() > 0) {
            for (Entry<String, String> es : parameters.entrySet()) {
                if (es.getValue() != null) {
                    if (firstParamUsed) {
                        viewURL.append("&");
                    } else {
                        viewURL.append("?");
                        firstParamUsed = true;
                    }
                    viewURL.append(es.getKey());
                    viewURL.append("=");
                    viewURL.append(es.getValue());
                }
            }
        }

        // urlencode the view part to append
        String encodedViewURL = null;
        try {
            encodedViewURL = URLEncoder.encode(viewURL.toString(), URL_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Invalid character encoding specified: " + URL_ENCODING);
        }

        // use the base URL or add in the extra bits if desired
        String toolURL = info.getToolURL();
        if (encodedViewURL != null && encodedViewURL.length() > 0) {
            toolURL = info.getToolURL() + "?toolstate-" + info.getPlacementId() + "=" + encodedViewURL;
        }

        // Sample URL: http://server:port/portal/site/siteId/page/pageId?toolstate-toolpid=/newpage?thing=value
        return toolURL;
    }


    // PERMISSIONS

    public void registerPermission(String permission) {
        functionManager.registerFunction(permission);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.DeveloperHelperService#isUserAdmin(java.lang.String)
     */
    public boolean isUserAdmin(String userReference) {
        boolean admin = false;
        String userId = getUserIdFromRef(userReference);
        if (userId != null) {
            admin = securityService.isSuperUser(userId);
        }
        return admin;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.DeveloperHelperService#isUserAllowedInReference(java.lang.String, java.lang.String, java.lang.String)
     */
    public boolean isUserAllowedInEntityReference(String userReference, String permission, String reference) {
        if (permission == null) {
            throw new IllegalArgumentException("permission must both be set");
        }
        boolean allowed = false;
        if (userReference != null) {
            String userId = getUserIdFromRef(userReference);
            if (userId != null) {
                if (reference == null) {
                    // special check for the admin user
                    if ( securityService.isSuperUser(userId) ) {
                        allowed = true;
                    }
                } else {
                    if ( securityService.unlock(userId, permission, reference) ) {
                        allowed = true;
                    }
                }
            }
        } else {
            // special anonymous user case - http://jira.sakaiproject.org/jira/browse/SAK-14840
            allowed = securityService.unlock(permission, reference);
        }
        return allowed;
    }

    
    public Set<String> getEntityReferencesForUserAndPermission(String userReference, String permission) {
        if (permission == null) {
            throw new IllegalArgumentException("permission must both be set");
        }

        Set<String> s = new HashSet<String>();
        // get the groups from Sakai
        String userId = null;
        if (userReference != null) {
            userId = getUserIdFromRef(userReference);
        }
        // anonymous user case - http://jira.sakaiproject.org/jira/browse/SAK-14840
        Set<String> authzGroupIds = 
            authzGroupService.getAuthzGroupsIsAllowed(userId, permission, null);
        if (authzGroupIds != null) {
            s.addAll(authzGroupIds);
        }
        return s;
    }

    
    public Set<String> getUserReferencesForEntityReference(String reference, String permission) {
        if (reference == null || permission == null) {
            throw new IllegalArgumentException("reference and permission must both be set");
        }
        List<String> azGroups = new ArrayList<String>();
        azGroups.add(reference);
        Set<String> userIds = authzGroupService.getUsersIsAllowed(permission, azGroups);
        // need to remove the admin user or else they show up in unwanted places (I think, maybe this is not needed)
        if (userIds.contains(ADMIN_USER_ID)) {
            userIds.remove(ADMIN_USER_ID);
        }

        // now convert to userRefs
        Set<String> userRefs = new HashSet<String>();
        for (String userId : userIds) {
            userRefs.add( getUserRefFromUserId(userId) );
        }
        return userRefs;
    }

    public void setAuthzGroupService(AuthzGroupService authzGroupService) {
        this.authzGroupService = authzGroupService;
    }

    public void setFunctionManager(FunctionManager functionManager) {
        this.functionManager = functionManager;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setToolManager(ToolManager toolManager) {
        this.toolManager = toolManager;
    }

    public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }

    public void setThreadLocalManager(ThreadLocalManager threadLocalManager) {
		this.threadLocalManager = threadLocalManager;
	}

}