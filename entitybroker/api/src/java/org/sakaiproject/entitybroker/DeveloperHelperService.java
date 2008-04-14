/**
 * $Id$
 * $URL$
 * DeveloperHelperService.java - entity-broker - Apr 13, 2008 5:42:38 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker;

import java.util.Locale;

/**
 * Includes methods which are likely to be helpful to developers who are implementing
 * entity providers in Sakai and working with references
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface DeveloperHelperService {

   public static final String ADMIN_USER_ID = "admin";

   // USER

   /**
    * Get the user entity reference (e.g. /user/{userId} - not id, eid, or username) 
    * of the current user if there is one,
    * this is not equivalent to the current user id
    * 
    * @return the user entity reference (e.g. /user/{userId} - not id, eid, or username)
    */
   public String getCurrentUserReference();

   /**
    * Translate the userId into a user entity reference
    * 
    * @param userReference the user entity reference (e.g. /user/{userId} - not id, eid, or username)
    * @return the userId as extracted from this user entity reference
    */
   public String getUserIdFromRef(String userReference);

   /**
    * Translate the user entity reference into a userId
    * 
    * @param userId the internal user Id (needed from some Sakai API operations) (not the eid or username)
    * @return the user entity reference (e.g. /user/{userId})
    */
   public String getUserRefFromUserId(String userId);

   /**
    * @return the Locale for the current user or the system set locale
    */
   public Locale getCurrentLocale();

   // LOCATION

   /**
    * @return the entity reference of the current location for the current session
    * (represents the current site/group of the current user in the system)
    */
   public String getCurrentLocationReference();

   /**
    * @return the entity reference of the current active tool for the current session
    * (represents the tool that is currently being used by the current user in the system)
    */
   public String getCurrentToolReference();

   /**
    * Translate a tool entity reference into a tool Id 
    * 
    * @param toolReference the entity reference of a tool (e.g. /tool/{toolId})
    * @return the toolId (needed for other Sakai API operations)
    */
   public String getToolIdFromToolRef(String toolReference);

   // PERMISSIONS

   /**
    * Check if this user has super admin level access (permissions)
    * 
    * @param userReference the user entity reference (e.g. /user/{userId} - not id, eid, or username)
    * @return true if the user has admin access, false otherwise
    */
   public boolean isUserAdmin(String userReference);

   /**
    * Check if a user has a specified permission for the entity reference, 
    * primarily a convenience method for checking location permissions
    * 
    * @param userReference the user entity reference (e.g. /user/{userId} - not id, eid, or username)
    * @param permission a permission string constant
    * @param reference a globally unique reference to an entity, 
    * consists of the entity prefix and optional segments (normally the id at least)
    * @return true if allowed, false otherwise
    */
   public boolean isUserAllowedInEntityReference(String userReference, String permission, String reference);

   // BEANS

   /**
    * Clone a bean including contained objects,
    * the level indicates the number of contained objects to traverse and clone,
    * setting this to zero will only clone basic type values in the bean,
    * setting this to one will clone basic fields, references, and collections in the bean,
    * etc.<br/>
    * This is mostly useful for making a copy of a hibernate object so it will no longer 
    * be the persistent object with the hibernate proxies and lazy loading
    * 
    * @param <T>
    * @param bean a java bean (object) to make a copy of
    * @param level number of levels of objects to include
    * @return the clone of the bean
    */
   public <T> T cloneBean(T bean, int level);

}
