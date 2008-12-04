/**
 * $Id: UserEntityProvider.java 51727 2008-09-03 09:00:03Z aaronz@vt.edu $
 * $URL: https://source.sakaiproject.org/svn/entitybroker/trunk/impl/src/java/org/sakaiproject/entitybroker/providers/UserEntityProvider.java $
 * UserEntityProvider.java - entity-broker - Jun 28, 2008 2:59:57 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.entitybroker.providers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.azeckoski.reflectutils.FieldUtils;
import org.azeckoski.reflectutils.ReflectUtils;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Restriction;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.providers.model.EntityUser;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserAlreadyDefinedException;
import org.sakaiproject.user.api.UserDirectoryService;
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
public class UserEntityProvider extends AbstractEntityProvider implements CoreEntityProvider, RESTful, Describeable {
    private static Log log = LogFactory.getLog(UserEntityProvider.class);

    private UserDirectoryService userDirectoryService;
    public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }

    public static String PREFIX = "user";
    public String getEntityPrefix() {
        return PREFIX;
    }

    @EntityCustomAction(action="current",viewKey=EntityView.VIEW_LIST)
    public EntityUser getCurrentUser(EntityView view) {
        String currentUserId = developerHelperService.getCurrentUserId();
        if (currentUserId == null) {
            throw new IllegalArgumentException("There is no current user to get user info about");
        }
        User user = getUserByIdEid(currentUserId);
        EntityUser eu = new EntityUser(user);
        return eu;
    }

    @EntityCustomAction(action="exists", viewKey=EntityView.VIEW_SHOW)
    public boolean checkUserExists(EntityView view) {
        String userId = view.getEntityReference().getId();
        userId = findAndCheckUserId(userId, null);
        boolean exists = (userId != null);
        return exists;
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
            edit.setPassword(u.getPassword());
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
        if (developerHelperService.isEntityRequestInternal(ref.toString())) {
            // internal lookups are allowed to get everything
        } else {
            // external lookups require auth
            boolean allowed = false;
            String currentUserRef = developerHelperService.getCurrentUserReference();
            if (currentUserRef != null) {
                String currentUserId = developerHelperService.getUserIdFromRef(currentUserRef);
                if (developerHelperService.isUserAdmin(currentUserId) 
                        || currentUserId.equals(user.getId())) {
                    // allowed to access the user data
                    allowed = true;
                }
            }
            if (! allowed) {
                throw new SecurityException("Current user ("+currentUserRef+") cannot access information about user: " + ref);
            }
        }
        // convert
        EntityUser eu = convertUser(user);
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
        if (developerHelperService.getConfigurationSetting("entity.users.viewall", false)) {
            // setting bypasses all checks
        } else if (developerHelperService.isEntityRequestInternal(ref.toString())) {
            // internal lookups are allowed to get everything
        } else {
            // external lookups require auth
            boolean allowed = false;
            String currentUserRef = developerHelperService.getCurrentUserReference();
            if (currentUserRef != null) {
                String currentUserId = developerHelperService.getUserIdFromRef(currentUserRef);
                if ( developerHelperService.isUserAdmin(currentUserId) ) {
                    // allowed to access the user data
                    allowed = true;
                }
            }
            if (! allowed) {
                throw new SecurityException("Only admin can access multiple users, current user ("+currentUserRef+") cannot access ref: " + ref);
            }
        }

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
        List<EntityUser> entityUsers = new ArrayList<EntityUser>();
        for (User user : users) {
            entityUsers.add( convertUser(user) );
        }
        return entityUsers;
    }

    public String[] getHandledInputFormats() {
        return new String[] { Formats.HTML, Formats.XML, Formats.JSON };
    }

    public String[] getHandledOutputFormats() {
        return new String[] { Formats.XML, Formats.JSON };
    }

    /**
     * Allows for easy retrieval of the user object
     * @param userId a user Id (can be eid)
     * @return the user object
     * @throws IllegalArgumentException if the user Id is invalid
     */
    public EntityUser getUserById(String userId) {
        userId = findAndCheckUserId(userId, null);
        EntityReference ref = new EntityReference("user", userId);
        EntityUser eu = (EntityUser) getEntity(ref);
        return eu;
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
     * @param currentUserId user id (can be eid)
     * @param currentUserEid user eid (can be id)
     * @return a valid user id OR null if not valid
     */
    public String findAndCheckUserId(String currentUserId, String currentUserEid) {
        if (currentUserId == null && currentUserEid == null) {
            throw new IllegalArgumentException("Cannot get user from a null userId and eid, ensure at least userId or userEid are set");
        }
        String userId = null;
        // can use the efficient methods to check if the user Id is valid
        if (currentUserId == null) {
            if (log.isDebugEnabled()) log.debug("currentUserId is null, currentUserEid=" + currentUserEid, new Exception());

            // try to get userId from eid
            if (currentUserEid.startsWith("/user/")) {
                // assume the form of "/user/userId" (the UDS method is protected)
                currentUserEid = new EntityReference(currentUserEid).getId();
            }
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
        } else {
            // get the id out of a ref
            if (currentUserId.startsWith("/user/")) {
                // assume the form of "/user/userId" (the UDS method is protected)
                currentUserId = new EntityReference(currentUserId).getId();
            }
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
                try {
                    userDirectoryService.getUserEid(currentUserId); // simply here to throw an exception or not
                    userId = currentUserId;
                } catch (UserNotDefinedException e) {
                    try {
                        userId = userDirectoryService.getUserId(currentUserId);
                    } catch (UserNotDefinedException e2) {
                        userId = null;
                    }
                }
            }
        }
        return userId;
    }
    
    /**
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
            entityUser = convertUser(user);
        } else {
            entityUser = null;
        }
        return entityUser;
    }

    public EntityUser convertUser(User user) {
        EntityUser eu = new EntityUser(user);
        return eu;
    }

    private User getUserByIdEid(String userEid) {
        User user = null;
        if (userEid != null) {
            try {
                user = userDirectoryService.getUserByEid(userEid);
            } catch (UserNotDefinedException e) {
                String userId = userEid;
                if (userId.length() > 3 
                        && userId.startsWith("id=") ) {
                    userId = userEid.substring(3);
                }
                try {
                    user = userDirectoryService.getUser(userId);
                } catch (UserNotDefinedException e1) {
                    throw new IllegalArgumentException("Could not find user with eid="+userEid+" or id="+userId+" :: "
                            + e1.getMessage(), e);
                }
            }
        }
        return user;
    }

}
