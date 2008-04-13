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

   /**
    * Get the user reference (not username or eid) of the current user if there is one,
    * this should also be equivalent to the current user id
    * 
    * @return the internal unique user reference of the current user (not username)
    */
   public String getCurrentUserReference();

   /**
    * @return the current location for the current session
    * (represents the current site/group of the current user in the system)
    */
   public String getCurrentLocationReference();

   /**
    * @return the Locale for the current user or the system set locale
    */
   public Locale getCurrentLocale();

   // PERMISSIONS

   /**
    * Check if this user has super admin level access (permissions)
    * 
    * @param userId the internal user id (not eid or username)
    * @return true if the user has admin access, false otherwise
    */
   public boolean isUserAdmin(String userId);

   /**
    * Check if a user has a specified permission for the entity reference, 
    * primarily a convenience method for checking location permissions
    * 
    * @param userId the internal user id (not eid or username)
    * @param permission a permission string constant
    * @param reference a globally unique reference to an entity, 
    * consists of the entity prefix and optional segments (normally the id at least)
    * @return true if allowed, false otherwise
    */
   public boolean isUserAllowedInReference(String userId, String permission, String reference);

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
