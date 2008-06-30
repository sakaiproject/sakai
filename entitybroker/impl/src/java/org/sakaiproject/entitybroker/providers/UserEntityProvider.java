/**
 * $Id$
 * $URL$
 * UserEntityProvider.java - entity-broker - Jun 28, 2008 2:59:57 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.providers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Restriction;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
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
public class UserEntityProvider implements EntityProvider, RESTful, AutoRegisterEntityProvider {

   private UserDirectoryService userDirectoryService;
   public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
      this.userDirectoryService = userDirectoryService;
   }

   private DeveloperHelperService developerHelperService;
   public void setDeveloperHelperService(DeveloperHelperService developerHelperService) {
      this.developerHelperService = developerHelperService;
   }

   public static String PREFIX = "user";
   public String getEntityPrefix() {
      return PREFIX;
   }

   public String createEntity(EntityReference ref, Object entity) {
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
            throw new IllegalArgumentException("User ID is invalid: " + user.getId(), e);
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

   public void updateEntity(EntityReference ref, Object entity) {
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

   public Object getEntity(EntityReference ref) {
      if (ref.getId() == null) {
         return new EntityUser();
      }
      String userId = ref.getId();
      User user = getUserByIdEid(userId);
      // convert
      EntityUser eu = convertUser(user);
      return eu;
   }

   public void deleteEntity(EntityReference ref) {
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

   @SuppressWarnings("unchecked")
   public List<?> getEntities(EntityReference ref, Search search) {
      Collection<User> users = new ArrayList<User>();
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
            users = userDirectoryService.searchUsers(restrict.value + "", 1, 50);
         }
      }
      if (restrict == null) {
         // just get all users but limit to 50
         users = userDirectoryService.getUsers(1, 50);
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


   @SuppressWarnings("unchecked")
   public EntityUser convertUser(User user) {
      EntityUser eu = new EntityUser(user);
      /** handled in constructor now
      eu.setDisplayName(user.getDisplayName());
      eu.setEid(user.getEid());
      eu.setEmail(user.getEmail());
      eu.setFirstName(user.getFirstName());
      eu.setId(user.getId());
      eu.setLastName(user.getLastName());
      eu.setType(user.getType());
      // properties
      ResourceProperties rp = user.getProperties();
      for (Iterator<String> iterator = rp.getPropertyNames(); iterator.hasNext();) {
         String name = iterator.next();
         String value = rp.getProperty(name);
         eu.setProperty(name, value);
      }
      **/
      return eu;
   }

   private User getUserByIdEid(String userId) {
      User user = null;
      if (userId != null) {
         try {
            user = userDirectoryService.getUser(userId);
         } catch (UserNotDefinedException e) {
            try {
               user = userDirectoryService.getUserByEid(userId);
            } catch (UserNotDefinedException e1) {
               throw new IllegalArgumentException("Could not find user with id: " + userId + " :: "
                     + e1.getMessage(), e);
            }
         }
      }
      return user;
   }

}
