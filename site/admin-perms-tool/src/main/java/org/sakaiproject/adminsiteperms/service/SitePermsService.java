/**
 * Copyright 2008 Sakaiproject Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.sakaiproject.adminsiteperms.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.site.api.SiteService.SortType;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

/**
 * Handles the processing related to the permissions handler
 * 
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
 */
public class SitePermsService {

    final protected Logger log = LoggerFactory.getLogger(getClass());

    private static final String STATUS_COMPLETE = "COMPLETE";

    private static int DEFAULT_PAUSE_TIME_MS = 1001; // just over 1 second
    private static int DEFAULT_MAX_UPDATE_TIME_SECS = 60*60; // 1 hour
    private static int DEFAULT_SITES_BEFORE_PAUSE = 10;

    private int pauseTimeMS = DEFAULT_PAUSE_TIME_MS;
    private long maxUpdateTimeMS = DEFAULT_MAX_UPDATE_TIME_SECS * 1000l;
    private int sitesUntilPause = DEFAULT_SITES_BEFORE_PAUSE;

    public static final String SITE_TEMPLATE_PREFIX = "!site.template";

    public static final String[] DEFAULT_SITE_TEMPLATES = {
        SITE_TEMPLATE_PREFIX,
        SITE_TEMPLATE_PREFIX+ "."+ "course",
        "!site.user"
    };

    /*
     * NOTE: we do NOT want to allow 2 sets of admin perms updates to run at the same time on a server
     * so we will use these service variables to basically lock the updates processing on a single server,
     * ideally we want to lock this for the entire cluster though...
     * We don't want to allow 2 users to do this at the same time so not locking it to the session.
     */
    private String updateStatus = null;
    private String updateMessage = null;
    /**
     * Timestamp of the start of the update,
     * used to ensure we can unlock this if it died
     */
    private long updateStarted = 0;

    /**
     * Set permissions (perms) in a set of site types (types) for a set of roles (roles)
     * 
     * @param perms a list of permission keys
     * @param types a list of site types (course/project/workspace/etc.)
     * @param roles a list of site roles
     * @param add if true then add the permissions, if false then remove them
     */
    public void setSiteRolePerms(final String[] perms, final String[] types, final String[] roles, final boolean add) {
        if (! securityService.isSuperUser()) {
            throw new SecurityException("setSiteRolePerms is only usable by super users");
        }
        if (isLockedForUpdates()) {
            throw new IllegalStateException("Cannot start new perms update, one is already in progress");
        }
        // get the configurable values
        pauseTimeMS = serverConfigurationService.getConfig("site.adminperms.pause.ms", pauseTimeMS);
        int maxUpdateTimeS = serverConfigurationService.getConfig("site.adminperms.maxrun.secs", DEFAULT_MAX_UPDATE_TIME_SECS);
        maxUpdateTimeMS = 1000l * maxUpdateTimeS; // covert to milliseconds
        sitesUntilPause = serverConfigurationService.getConfig("site.adminperms.sitesuntilpause", sitesUntilPause);
        // get the current state
        final User currentUser = userDirectoryService.getCurrentUser();
        final Session currentSession = sessionManager.getCurrentSession();
        // run this in a new thread
        Runnable backgroundRunner = new Runnable() {
            public void run() {
                try {
                    initiateSitePermsThread(currentUser, currentSession, perms, types, roles, add);
                } catch (IllegalStateException e) {
                    throw e; // rethrow this back out
                } catch (Exception e) {
                    log.error("SitePerms background perms runner thread died: "+e, e);
                }
            }
        };
        Thread bgThread = new Thread(backgroundRunner);
        bgThread.setDaemon(true); // important, otherwise JVM cannot exit
        bgThread.start();
    }

    /**
     * Returns a current status message from the session if there is one to display for a running admin perms process,
     * is the status is complete then the session data will be removed
     */
    public synchronized String getCurrentStatusMessage() {
        String msg = updateMessage;
        isLockedForUpdates(); // just run this
        if (STATUS_COMPLETE.equals(updateStatus)) {
            updateStatus = null;
            updateMessage = null;
        }
        return msg;
    }

    /**
     * @return true if this update is currently locking the permissions processor, false if another permissions processor can be started
     */
    public synchronized boolean isLockedForUpdates() {
        boolean locked = false;
        if (updateStarted > 0) {
            if (System.currentTimeMillis() > (updateStarted + maxUpdateTimeMS)) {
                // max time reached for this update so reset
                updateStarted = 0;
                updateStatus = STATUS_COMPLETE;
                updateMessage = "Max time exceeded for this update";
            } else {
                locked = true;
            }
        }
        return locked;
    }

    private void initiateSitePermsThread(final User currentUser, final Session currentSession, final String[] perms, final String[] types, final String[] roles, final boolean add) throws InterruptedException {
        String permsString = makeStringFromArray(perms);
        String typesString = makeStringFromArray(types);
        String rolesString = makeStringFromArray(roles);
        // exit if we are locked for updates
        if (isLockedForUpdates()) {
            throw new IllegalStateException("Cannot start new perms update, one is already in progress");
        }
        updateStarted = System.currentTimeMillis();
        // update the session with a status message
        String msg = getMessage("siterole.message.processing."+(add?"add":"remove"), 
                new Object[] {permsString, typesString, rolesString, 0});
        log.info("STARTED: "+msg+" :: pauseTimeMS="+pauseTimeMS+", sitesUntilPause="+sitesUntilPause+", maxUpdateTimeMS="+maxUpdateTimeMS);
        updateStatus = "RUNNING";
        updateMessage = msg;
        // set the current user in this thread so they can perform the operations
        Session threadSession = setCurrentUser(currentUser.getId());
        try {
            List<String> permsList = Arrays.asList(perms);
            // now add the perms to all matching roles in all matching sites
            // switched site listing to using ids only - KNL-1125
            List<String> siteIds = siteService.getSiteIds(SelectionType.ANY, types, null, null, SortType.NONE, null);
            int pauseTime = 0;
            int sitesCounter = 0;
            int updatesCount = 0;
            int successCount = 0;
            for (String siteId : siteIds) {
                String siteRef = siteService.siteReference(siteId);
                try {
                    AuthzGroup ag = authzGroupService.getAuthzGroup(siteRef);
                    if (authzGroupService.allowUpdate(ag.getId())) {
                        boolean updated = false;
                        for (String role : roles) {
                            Role r = ag.getRole(role);
                            // if role not found in this group then move on
                            if (r != null) {
                                // get the current perms so we can possibly avoid an update
                                Set<String> current = r.getAllowedFunctions();
                                if (add) {
                                    if (! current.containsAll(permsList)) {
                                        // only update if the perms are not already there
                                        r.allowFunctions(permsList);
                                        updated = true;
                                    }
                                } else {
                                    boolean found = false;
                                    for (String perm : permsList) {
                                        if (current.contains(perm)) {
                                            found = true;
                                            break;
                                        }
                                    }
                                    if (found) {
                                        // only update if at least one perm needs to be removed
                                        r.disallowFunctions(permsList);
                                        updated = true;
                                    }
                                }
                            }
                        }
                        if (updated) {
                            // only save if the group was updated
                            authzGroupService.save(ag);
                            updatesCount++;
                            log.info("Added Permissions ("+permsString+") for roles ("+rolesString+") to group:" + siteRef);
                        }
                        successCount++;
                        if (updatesCount > 0 && updatesCount % sitesUntilPause == 0) {
                            // pause every 10 (default) sites updated or so for about 1 second (default)
                            Thread.sleep(pauseTimeMS);
                            pauseTime += pauseTimeMS;
                            // make sure the sessions do not timeout
                            threadSession.setActive();
                            currentSession.setActive();
                        }
                    } else {
                        log.warn("Cannot update authz group: " + siteRef + ", unable to apply any perms change");
                    }
                } catch (GroupNotDefinedException e) {
                    log.error("Could not find authz group: " + siteRef + ", unable to apply any perms change");
                } catch (AuthzPermissionException e) {
                    log.error("Could not save authz group: " + siteRef + ", unable to apply any perms change");
                }
                sitesCounter++;
                if (!isLockedForUpdates()) {
                    // if we are no longer locked for updates then we have a timeout failure
                    throw new RuntimeException("Timeout occurred while running site permissions update");
                } else if (sitesCounter % 4 == 0) {
                    // update the processor status every few sites processed
                    int percentComplete = (int) (sitesCounter * 100) / siteIds.size();
                    msg = getMessage("siterole.message.processing."+(add?"add":"remove"), 
                            new Object[] {permsString, typesString, rolesString, percentComplete});
                    updateMessage = msg;
                }
            }
            int failureCount = siteIds.size() - successCount;
            long totalTime = System.currentTimeMillis() - updateStarted;
            int totalSecs = totalTime > 0 ? (int)(totalTime/1000) : 0;
            int pauseSecs = pauseTime > 0 ? (int)(pauseTime/1000) : 0;
            msg = getMessage("siterole.message.permissions."+(add ? "added" : "removed"), 
                    new Object[] {permsString, typesString, rolesString, siteIds.size(), updatesCount, successCount, failureCount, totalSecs, pauseSecs});
            log.info(msg);
            updateMessage = msg;
        } finally {
            // reset the update status
            updateStatus = STATUS_COMPLETE;
            updateStarted = 0;
            // cleanup the session associated with this thread
            threadSession.clear();
            threadSession.invalidate();
        }
    }

    /**
     * Set a current user for the current thread, create session if needed
     * @param userId the userId to set
     * @return the new Session
     */
    private Session setCurrentUser(String userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
        Session currentSession = sessionManager.getCurrentSession();
        if (currentSession == null) {
            // start a session if none is around
            currentSession = sessionManager.startSession(userId);
        }
        currentSession.setUserId(userId);
        currentSession.setActive();
        sessionManager.setCurrentSession(currentSession);
        authzGroupService.refreshUser(userId);
        return currentSession;
    }

    /**
     * @return a list of all site types
     */
    public List<String> getSiteTypes() {
        List<String> siteTypes = siteService.getSiteTypes();
        Collections.sort(siteTypes);
        return siteTypes;
    }

    /**
     * @return a list of all permissions
     */
    public List<String> getPermissions() {
        List<String> perms = functionManager.getRegisteredFunctions();
        Collections.sort(perms);
        return perms;
    }

    private List<String> getTemplateRoles() {
        Set<String> templates = new HashSet<>(Arrays.asList(DEFAULT_SITE_TEMPLATES));
        siteService.getSiteTypes().stream().map(s -> SITE_TEMPLATE_PREFIX + "."+ s).forEach(templates::add);
        return new ArrayList<>(templates);
    }

    /**
     * @return a list of all valid roles names
     */
    public List<String> getValidRoles() {
        HashSet<String> roleIds = new HashSet<String>();
        for (String templateRef : getTemplateRoles()) {
            AuthzGroup ag;
            try {
                ag = authzGroupService.getAuthzGroup(templateRef);
                Set<Role> agRoles = ag.getRoles();
                for (Role role : agRoles) {
                    roleIds.add(role.getId());
                }
            } catch (GroupNotDefinedException e) {
                // nothing to do here but continue really
            }
        }
        ArrayList<String> roles = new ArrayList<String>(roleIds);
        Collections.sort(roles);
        return roles;
    }
    
    /**
     * @return a list of additional valid roles
     */
    public List<AdditionalRole> getAdditionalRoles() {
    	HashSet<AdditionalRole> roleSet = new HashSet<AdditionalRole>();
    	roleSet.add(new AdditionalRole(".anon", authzGroupService.getRoleName(".anon")));
    	roleSet.add(new AdditionalRole(".auth", authzGroupService.getRoleName(".auth")));
    	
    	for(String roleId : authzGroupService.getAdditionalRoles())
    		roleSet.add(new AdditionalRole(roleId, authzGroupService.getRoleName(roleId)));
    	
    	List<AdditionalRole> ret = new ArrayList<AdditionalRole>(roleSet);
    	Collections.sort(ret);
        return ret;
    }

    /**
     * @return true if current user is super admin
     */
    public boolean isSuperAdmin() {
        return securityService.isSuperUser();
    }

    /**
     * Get a translated string from a code and replacement args
     * 
     * @param code
     * @param args
     * @return the translated string
     */
    public String getMessage(String code, Object[] args) {
        String msg;
        try {
            msg = getMessageSource().getMessage(code, args, null);
        } catch (NoSuchMessageException e) {
            msg = "Missing message for code: "+code;
        }
        return msg;
    }


    public static String makeStringFromArray(String[] array) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(array[i]);
        }
        return sb.toString();
    }
    
    public class AdditionalRole implements Comparable<AdditionalRole> {
    	private String id;
    	private String name;
    	private String groupId;
    	
    	public AdditionalRole(String id, String name) {
    		this.id = id;
    		this.name = name;
    		
    		int index = id.lastIndexOf(".");
    		this.groupId = (index >= 0) ? id.substring(0, index) : "";
    	}

		public String getId() {
			return id;
		}

		public String getName() {
			return name;
		}
		
		public String getGroupId() {
			return groupId;
		}
		
		public boolean equals(Object obj) {
			if (!(obj instanceof AdditionalRole))
				return false;	
			if (obj == this)
				return true;
			return this.id.equals(((AdditionalRole) obj).id);
		}
		
		public int hashCode(){
			return id.hashCode();
		}

		@Override
		public int compareTo(AdditionalRole arg0) {
			if(arg0 == null) return 1;
			return (this.groupId+"_"+this.name).compareTo(arg0.groupId+"_"+arg0.name);
		}
    }


    private AuthzGroupService authzGroupService;
    public void setAuthzGroupService(AuthzGroupService authzGroupService) {
        this.authzGroupService = authzGroupService;
    }

    private FunctionManager functionManager;
    public void setFunctionManager(FunctionManager functionManager) {
        this.functionManager = functionManager;
    }

    private SessionManager sessionManager;
    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    private SecurityService securityService;
    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    private ServerConfigurationService serverConfigurationService;
    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    private SiteService siteService;
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    UserDirectoryService userDirectoryService;
    public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }

    private MessageSource messageSource;
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }
    public MessageSource getMessageSource() {
        return messageSource;
    }

}
