/**
 * $Id$
 * $URL$
 * UserEntityProvider.java - entity-broker - Jun 28, 2008 2:59:57 PM - azeckoski
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

package org.sakaiproject.entitybroker.providers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.azeckoski.reflectutils.FieldUtils;
import org.azeckoski.reflectutils.ReflectUtils;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Restriction;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.providers.model.EntityUser;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserAlreadyDefinedException;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserDirectoryService.PasswordRating;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserIdInvalidException;
import org.sakaiproject.user.api.UserLockedException;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.api.UserPermissionException;

/**
 * Entity Provider for users
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@Slf4j
public class UserEntityProvider extends AbstractEntityProvider implements CoreEntityProvider, RESTful, Describeable {

    private static final String ID_PREFIX = "id=";

    private UserDirectoryService userDirectoryService;
    public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }

    private SiteService siteService;
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }


    private DeveloperHelperService developerHelperService;
    public void setDeveloperHelperService(
            DeveloperHelperService developerHelperService) {
        this.developerHelperService = developerHelperService;
    }

    private ServerConfigurationService serverConfigurationService;
    public void setServerConfigurationService(
            ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }


    public static String PREFIX = "user";
    public String getEntityPrefix() {
        return PREFIX;
    }

    @EntityCustomAction(action="current",viewKey=EntityView.VIEW_LIST)
    public EntityUser getCurrentUser(EntityView view) {
        EntityUser eu = new EntityUser(userDirectoryService.getCurrentUser());
        return eu;
    }

    @EntityCustomAction(action="exists", viewKey=EntityView.VIEW_SHOW)
    public boolean checkUserExists(EntityView view) {
        String userId = view.getEntityReference().getId();
        userId = findAndCheckUserId(userId, null);
        boolean exists = (userId != null);
        return exists;
    }

    @EntityCustomAction(action="validatePassword", viewKey=EntityView.VIEW_NEW)
    public ActionReturn validatePassword(EntityView view, Map<String, Object> params) {
        PasswordRating rating = PasswordRating.PASSED_DEFAULT;
        if (!params.containsKey("password")) {
            throw new IllegalArgumentException("Must include a 'password' to validate");
        }
        String password = (String) params.get("password");
        User user = null;
        if (params.containsKey("username")) {
            String username = (String) params.get("username");
            user = new EntityUser(username, null, null, null, username, null, password, null);
        }
        rating = userDirectoryService.validatePassword(password, user);
        return new ActionReturn(rating.name());
    }

    public boolean entityExists(String id) {
        if (id == null) {
            return false;
        }
        if ("".equals(id)) {
            return true;
        }
        String userId = findAndCheckUserId(id, null);
        if (userId != null) {
            return true;
        }
        return false;
    }

    public String createEntity(EntityReference ref, Object entity, Map<String, Object> params) {
        String userId = null;
        if (ref.getId() != null && ref.getId().length() > 0) {
            userId = ref.getId();
        }
        if (entity.getClass().isAssignableFrom(User.class)) {
            // if someone passes in a user or useredit
            User user = (User) entity;
            if (userId == null && user.getId() != null) {
                userId = user.getId();
            }

            //check if this user can add an account of this type
            if (!canAddAccountType(user.getType())) {
                throw new SecurityException("User can't add an account of type: " + user.getType());
            }

            // NOTE: must assign empty password if user is created this way.... it sucks -AZ
            try {
                User newUser = userDirectoryService.addUser(userId, user.getEid(), user.getFirstName(), user.getLastName(), 
                        user.getEmail(), "", user.getType(), user.getProperties());
                userId = newUser.getId();
            } catch (UserIdInvalidException e) {
                throw new IllegalArgumentException("User ID is invalid, id=" + user.getId() + ", eid="+user.getEid(), e);
            } catch (UserAlreadyDefinedException e) {
                throw new IllegalArgumentException("Cannot create user, user already exists: " + ref, e);
            } catch (UserPermissionException e) {
                throw new SecurityException("Could not create user, permission denied: " + ref, e);
            }
        } else if (entity.getClass().isAssignableFrom(EntityUser.class)) {
            // if they instead pass in the EntityUser object
            EntityUser user = (EntityUser) entity;
            if (userId == null && user.getId() != null) {
                userId = user.getId();
            }

            //check if this user can add an account of this type
            if (!canAddAccountType(user.getType())) {
                throw new SecurityException("User can't add an account of type: " + user.getType());
            }

            try {

                UserEdit edit = userDirectoryService.addUser(userId, user.getEid());
                edit.setEmail(user.getEmail());
                edit.setFirstName(user.getFirstName());
                edit.setLastName(user.getLastName());
                edit.setPassword(user.getPassword());
                edit.setType(user.getType());
                // put in properties
                ResourcePropertiesEdit rpe = edit.getPropertiesEdit();
                for (String key : user.getProps().keySet()) {
                    String value = user.getProps().get(key);
                    rpe.addProperty(key, value);
                }
                userDirectoryService.commitEdit(edit);
                userId = edit.getId();
            } catch (UserIdInvalidException e) {
                throw new IllegalArgumentException("User ID is invalid: " + user.getId(), e);
            } catch (UserAlreadyDefinedException e) {
                throw new IllegalArgumentException("Cannot create user, user already exists: " + ref, e);
            } catch (UserPermissionException e) {
                throw new SecurityException("Could not create user, permission denied: " + ref, e);
            }         
        } else {
            throw new IllegalArgumentException("Invalid entity for creation, must be User or EntityUser object");
        }
        return userId;
    }

    public Object getSampleEntity() {
        return new EntityUser();
    }

    public void updateEntity(EntityReference ref, Object entity, Map<String, Object> params) {
        String userId = ref.getId();
        if (userId == null || "".equals(userId)) {
            throw new IllegalArgumentException("Cannot update, No userId in provided reference: " + ref);
        }
        User user = getUserByIdEid(userId);
        UserEdit edit = null;
        try {
            edit = userDirectoryService.editUser(user.getId());
        } catch (UserNotDefinedException e) {
            throw new IllegalArgumentException("Invalid user: " + ref + ":" + e.getMessage());
        } catch (UserPermissionException e) {
            throw new SecurityException("Permission denied: User cannot be updated: " + ref);
        } catch (UserLockedException e) {
            throw new RuntimeException("Something strange has failed with Sakai: " + e.getMessage());
        }

        if (entity.getClass().isAssignableFrom(User.class)) {
            // if someone passes in a user or useredit
            User u = (User) entity;
            edit.setEmail(u.getEmail());
            edit.setFirstName(u.getFirstName());
            edit.setLastName(u.getLastName());
            edit.setType(u.getType());
            // put in properties
            ResourcePropertiesEdit rpe = edit.getPropertiesEdit();
            rpe.set(u.getProperties());
        } else if (entity.getClass().isAssignableFrom(EntityUser.class)) {
            // if they instead pass in the myuser object
            EntityUser u = (EntityUser) entity;
            edit.setEmail(u.getEmail());
            edit.setFirstName(u.getFirstName());
            edit.setLastName(u.getLastName());
            if (u.getPassword() != null && !"".equals(u.getPassword())) {
                edit.setPassword(u.getPassword());
            }
            edit.setType(u.getType());
            // put in properties
            ResourcePropertiesEdit rpe = edit.getPropertiesEdit();
            for (String key : u.getProps().keySet()) {
                String value = u.getProps().get(key);
                rpe.addProperty(key, value);
            }
        } else {
            throw new IllegalArgumentException("Invalid entity for update, must be User or EntityUser object");
        }
        try {
            userDirectoryService.commitEdit(edit);
        } catch (UserAlreadyDefinedException e) {
            throw new RuntimeException(ref + ": This exception should not be possible: " + e.getMessage(), e);
        }
    }

    public void deleteEntity(EntityReference ref, Map<String, Object> params) {
        String userId = ref.getId();
        if (userId == null || "".equals(userId)) {
            throw new IllegalArgumentException("Cannot delete, No userId in provided reference: " + ref);
        }
        User user = getUserByIdEid(userId);
        if (user != null) {
            try {
                UserEdit edit = userDirectoryService.editUser(user.getId());
                userDirectoryService.removeUser(edit);
            } catch (UserNotDefinedException e) {
                throw new IllegalArgumentException("Invalid user: " + ref + ":" + e.getMessage());
            } catch (UserPermissionException e) {
                throw new SecurityException("Permission denied: User cannot be removed: " + ref);
            } catch (UserLockedException e) {
                throw new RuntimeException("Something strange has failed with Sakai: " + e.getMessage());
            }
        }
    }

    public Object getEntity(EntityReference ref) {
        if (ref.getId() == null) {
            return new EntityUser();
        }
        String userId = ref.getId();
        User user = getUserByIdEid(userId);
        // convert and check permissions
        EntityUser eu = convertUser(ref, user, hasProfile());
        return eu;
    }

    /**
     * WARNING: The search results may be drawn from different populations depending on the
     * search parameters specified. A straight listing with no filtering, or a search on "search"
     * or "criteria", will only retrieve matches from the Sakai-maintained user records. A search
     * on "email" may also check the records maintained by the user directory provider.
     */
    public List<?> getEntities(EntityReference ref, Search search) {
        Collection<User> users = new ArrayList<User>();

        // fix up the search limits
        if (search.getLimit() > 50 || search.getLimit() == 0) {
            search.setLimit(50);
        }
        if (search.getStart() == 0 || search.getStart() > 49) {
            search.setStart(1);
        }

        // get the search restrictions out
        Restriction restrict = search.getRestrictionByProperty("email");
        if (restrict != null) {
            // search users by email
            users = userDirectoryService.findUsersByEmail(restrict.value.toString());
        }
        if (restrict == null) {
            restrict = search.getRestrictionByProperty("eid");
            if (restrict == null) {
                restrict = search.getRestrictionByProperty("search");
            }
            if (restrict == null) {
                restrict = search.getRestrictionByProperty("criteria");
            }
            if (restrict != null) {
                // search users but match
                users = userDirectoryService.searchUsers(restrict.value + "", (int) search.getStart(), (int) search.getLimit());
            }
        }
        if (restrict == null) {
            users = userDirectoryService.getUsers((int) search.getStart(), (int) search.getLimit());
        }
        // convert these into EntityUser objects
        List<EntityUser> entityUsers = new ArrayList<>();
        boolean hasProfile = hasProfile();
        for (User user : users) {
            entityUsers.add( convertUser(ref, user, hasProfile) );
        }
        return entityUsers;
    }

    public String[] getHandledInputFormats() {
        return new String[] { Formats.HTML, Formats.XML, Formats.JSON };
    }

    public String[] getHandledOutputFormats() {
        return new String[] { Formats.XML, Formats.JSON, Formats.FORM };
    }

    /**
     * Allows for easy retrieval of the user object
     * Just used by membership entity provider.
     * @param userId a user ID (must be internal ID only and not EID)
     * @return the user object or <code>null</code> if not found
     * @throws IllegalArgumentException if the user Id is null
     */
    public EntityUser getUserById(String userId) {
        userId = findAndCheckUserId(userId, null);
        // we could have been passed a Id that no longer refers to a user
        if (userId == null) {
            return null;
        }
        try {
            return new EntityUser(userDirectoryService.getUser(userId));
        } catch (UserNotDefinedException e) {
            // This should never happen as it should be checked earlier
            return null;
        }
    }

    /*
     * This ugliness is needed because of the edge case where people are using identical ID/EIDs,
     * this is a really really bad hack to attempt to get the server to tell us if the eid==id for users
     */
    private Boolean usesSeparateIdEid = null;
    private boolean isUsingSameIdEid() {
        if (usesSeparateIdEid == null) {
            String config = developerHelperService.getConfigurationSetting("separateIdEid@org.sakaiproject.user.api.UserDirectoryService", (String)null);
            if (config != null) {
                try {
                    usesSeparateIdEid = ReflectUtils.getInstance().convert(config, Boolean.class);
                } catch (UnsupportedOperationException e) {
                    // oh well
                    usesSeparateIdEid = null;
                }
            }
            if (usesSeparateIdEid == null) {
                // could not get the stupid setting so attempt to check the service itself
                try {
                    usesSeparateIdEid = FieldUtils.getInstance().getFieldValue(userDirectoryService, "m_separateIdEid", Boolean.class);
                } catch (RuntimeException e) {
                    // no luck here
                    usesSeparateIdEid = null;
                }
            }
            if (usesSeparateIdEid == null) usesSeparateIdEid = Boolean.FALSE;
        }
        return ! usesSeparateIdEid.booleanValue();
    }

    /**
     * Will check that a userId/eid is valid and will produce a valid userId from the check
     *
     * @param currentUserId user id (can be eid), if non-null then search will be done on this.
     * @param currentUserEid user eid (can be id), only if currentUserId is null will this be searched on.
     * @return a valid user id OR null if not found
     * @throws IllegalArgumentException if both arguments are null.
     */
    public String findAndCheckUserId(String currentUserId, String currentUserEid) {
        if (currentUserId == null && currentUserEid == null) {
            throw new IllegalArgumentException("Cannot get user from a null userId and eid, ensure at least userId or userEid are set");
        }
        String userId = null;

        // can use the efficient methods to check if the user Id is valid
        if (currentUserId == null) {
            // We should assume we will resolve by EID
            if (log.isDebugEnabled()) log.debug("currentUserId is null, currentUserEid=" + currentUserEid, new Exception());

            // try to get userId from eid
            currentUserEid = removePrefix(currentUserEid);
            if (isUsingSameIdEid()) {
                // have to actually fetch the user
                User u;
                try {
                    u = getUserByIdEid(currentUserEid);
                    if (u != null) {
                        userId = u.getId();
                    }
                } catch (IllegalArgumentException e) {
                    userId = null;
                }
            } else {
                if (userIdExplicitOnly()) {
                    // only check ID or EID
                    if (currentUserEid.length() > ID_PREFIX.length() && currentUserEid.startsWith(ID_PREFIX) ) {
                        // strip the id marker out
                        currentUserEid = currentUserEid.substring(ID_PREFIX.length());
                        // check EID, do not attempt to check by ID as well
                        try {
                            User u = userDirectoryService.getUserByAid(currentUserEid);
                            userId = u.getId();
                        } catch (UserNotDefinedException e2) {
                            userId = null;
                        }
                    } else {
                        // check by ID
                        try {
                            userDirectoryService.getUserEid(currentUserEid); // simply here to throw an exception or not
                            userId = currentUserEid;
                        } catch (UserNotDefinedException e2) {
                            userId = null;
                        }
                    }
                } else {
                    // check for EID and then ID
                    try {
                        userId = userDirectoryService.getUserId(currentUserEid);
                    } catch (UserNotDefinedException e) {
                        try {
                            userDirectoryService.getUserEid(currentUserEid); // simply here to throw an exception or not
                            userId = currentUserEid;
                        } catch (UserNotDefinedException e2) {
                            userId = null;
                        }
                    }
                }
            }
        } else {
            // Assume we will resolve by ID
            // get the id out of a ref
            currentUserId = removePrefix(currentUserId);

            // verify the userId is valid
            if (isUsingSameIdEid()) {
                // have to actually fetch the user
                try {
                    User u = getUserByIdEid(currentUserId);
                    if (u != null) {
                        userId = u.getId();
                    }
                } catch (IllegalArgumentException e) {
                    userId = null;
                }
            } else {
                if (userIdExplicitOnly()) {
                    if (currentUserId.length() > ID_PREFIX.length() && currentUserId.startsWith(ID_PREFIX) ) {
                        // strip the id marker out
                        currentUserId = currentUserId.substring(ID_PREFIX.length());
                    }
                    // check ID, do not attempt to check by AID/EID as well
                    try {
                        userDirectoryService.getUserEid(currentUserId); // simply here to throw an exception or not
                        userId = currentUserId;
                    } catch (UserNotDefinedException e2) {
                        userId = null;
                    }
                } else {
                    // check for ID and then AID/EID
                    try {
                        userDirectoryService.getUserEid(currentUserId); // simply here to throw an exception or not
                        userId = currentUserId;
                    } catch (UserNotDefinedException e) {
                        try {
                            User u = userDirectoryService.getUserByAid(currentUserId);
                            userId = u.getId();
                        } catch (UserNotDefinedException e2) {
                            userId = null;
                        }
                    }
                }
            }
        }
        return userId;
    }

    private String removePrefix(String currentUserId) {
        if (currentUserId.startsWith("/user/")) {
            // assume the form of "/user/userId" (the UDS method is protected)
            currentUserId = new EntityReference(currentUserId).getId();
        }
        return currentUserId;
    }

    /**
     * This is used by the Membership provider
     * @param userSearchValue either a user ID, a user EID, or a user email address
     * @return the first matching user, or null if no search method worked
     */
    public EntityUser findUserFromSearchValue(String userSearchValue) {
        EntityUser entityUser;
        User user;
        try {
            user = userDirectoryService.getUser(userSearchValue);
        } catch (UserNotDefinedException e) {
            try {
                user = userDirectoryService.getUserByEid(userSearchValue);
            } catch (UserNotDefinedException e1) {
                user = null;
            }
        }
        if (user == null) {
            Collection<User> users = userDirectoryService.findUsersByEmail(userSearchValue);
            if ((users != null) && (users.size() > 0)) {
                user = users.iterator().next();
                if (users.size() > 1) {
                    if (log.isWarnEnabled()) log.warn("Found multiple users with email " + userSearchValue);
                }
            }
        }
        if (user != null) {
            entityUser = new EntityUser(user);
        } else {
            entityUser = null;
        }
        return entityUser;
    }

    /**
     * This checks how what details the current user should be able to see about user.
     * @param user The user to convert.
     * @return The user ready to be serialised
     * @throws SecurityException If the current user can't access this user.
     */
     EntityUser convertUser(EntityReference ref, User user, boolean hasProfile) {
        if (developerHelperService.getCurrentUserId() == null) {
            throw new SecurityException("Anonymous access is not permitted to user information: "+ ref);
        }
        // If config, internal request or admin, give full access
        if (developerHelperService.getConfigurationSetting("entity.users.viewall", false) ||
                developerHelperService.isEntityRequestInternal(ref.toString()) ||
                developerHelperService.isUserAdmin(developerHelperService.getCurrentUserReference()) ||
                user.getId().equals(developerHelperService.getCurrentUserId())) {
            EntityUser eu = new EntityUser(user);
            return eu;
        } else if (hasProfile) {
            // Show restricted view
            EntityUser eu = new EntityUser(user.getEid(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getDisplayName(), user.getDisplayId(), null, user.getType());
            eu.setId(user.getId());
            return eu;
        }
        throw new SecurityException("Current user ("+developerHelperService.getCurrentUserReference()+") cannot access information for: " + ref);
    }

    /**
     * @return <code>true</code> if the current user has the profile tool in their My Workspace.
     */
    private boolean hasProfile() {
        boolean hasProfile = false;
        String currentUserId = developerHelperService.getCurrentUserId();
        if (currentUserId != null) {
            String userSiteId = siteService.getUserSiteId(currentUserId);
            try {
                Site userSite = siteService.getSite(userSiteId);
                hasProfile = userSite.getToolForCommonId("sakai.profile2") != null;
            } catch (IdUnusedException e) {
                // Ignore
            }
        }
        return hasProfile;
    }

    /**
     * Attempt to get a user by AID, EID or ID
     * 
     * NOTE: can force this to only attempt the ID lookups if prefixed with "id=" using "user.explicit.id.only=true"
     * 
     * @param id the user EID, AID or ID
     * @return the populated User object
     */
    User getUserByIdEid(String id) {
        User user = null;
        if (id != null) {
            boolean doCheckForId = false;
            boolean doCheckForAid = true;
            String userId = id;
            // check if the incoming param says this is explicitly an id
            if (userId.length() > ID_PREFIX.length() && userId.startsWith(ID_PREFIX) ) {
                // strip the id marker out
                userId = id.substring(ID_PREFIX.length());
                doCheckForAid = false; // skip the AID/EID check entirely
                doCheckForId = true;
            }
            // attempt checking both with failover by default (override by property "user.id.failover.check=false")
            if (doCheckForAid) {
                try {
                    // AID check falls through to EID check.
                    user = userDirectoryService.getUserByAid(id);
                } catch (UserNotDefinedException e) {
                    user = null;
                    if (!userIdExplicitOnly()) {
                        doCheckForId = true;
                    }
                }
            }
            if (doCheckForId) {
                try {
                    user = userDirectoryService.getUser(userId);
                } catch (UserNotDefinedException e) {
                    user = null;
                }
            }
        }
        return user;
    }


    /**
     * Can the current user add an account of this type see KNL-357
     * @param type
     * @return
     */
    private boolean canAddAccountType(String type) {
        log.debug("canAddAccountType(" + type + ")");
        //admin can always add users
        if (developerHelperService.isUserAdmin(developerHelperService.getCurrentUserReference()))
        {
            log.debug("Admin user is allowed!");
            return true;
        }

        String currentSessionUserId = developerHelperService.getCurrentUserId();
        log.debug("checking if " + currentSessionUserId + " can add account of type: " + type);

        //this may be an anonymous session registering
        if (currentSessionUserId == null)
        {
            String regAccountTypes = serverConfigurationService.getString("user.registrationTypes", "registered");
            List<String> regTypes = Arrays.asList(regAccountTypes.split(","));
            if (! regTypes.contains(type))
            {
                log.warn("Anonamous user can't create an account of type: " + type + ", allowed types: " + regAccountTypes);
                return false;
            }

        }
        else
        {
            //this is a authenticated non-admin user
            String newAccountTypes = serverConfigurationService.getString("user.nonAdminTypes", "guest");
            List<String> newTypes = Arrays.asList(newAccountTypes.split(","));
            if (! newTypes.contains(type))
            {
                log.warn("User " + currentSessionUserId + " can't create an account of type: " + type +" with eid , allowed types: " + newAccountTypes);
                return false;
            }
        }
        return true;
    }

    /**
     * Checks the sakai.properties setting for: "user.explicit.id.only",
     * set this to true to disable id/eid failover checks (this means lookups will only attempt to use 
     * id or eid as per the exact params which are passed or as per the endpoint API)
     * 
     * In other words, the user id must be prefixed with "id=", otherwise it will be treated like an eid
     * 
     * @return true if user ID must be passed explicitly (no id/eid failover checks are allowed), default: false
     */
    private boolean userIdExplicitOnly() {
        boolean allowed = developerHelperService.getConfigurationSetting("user.explicit.id.only", false);
        return allowed;
    }

}
