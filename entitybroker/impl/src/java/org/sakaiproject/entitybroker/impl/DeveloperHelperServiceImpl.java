/**
 * $Id$
 * $URL$
 * DeveloperHelperServiceImpl.java - entity-broker - Apr 13, 2008 6:30:08 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.impl.util.BeanCloner;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;

/**
 * implementation of the helper service methods
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class DeveloperHelperServiceImpl implements DeveloperHelperService {

   private AuthzGroupService authzGroupService;
   public void setAuthzGroupService(AuthzGroupService authzGroupService) {
      this.authzGroupService = authzGroupService;
   }

   private SecurityService securityService;
   public void setSecurityService(SecurityService securityService) {
      this.securityService = securityService;
   }

   private SessionManager sessionManager;
   public void setSessionManager(SessionManager sessionManager) {
      this.sessionManager = sessionManager;
   }

   private SiteService siteService;
   public void setSiteService(SiteService siteService) {
      this.siteService = siteService;
   }

   private ToolManager toolManager;
   public void setToolManager(ToolManager toolManager) {
      this.toolManager = toolManager;
   }

   private UserDirectoryService userDirectoryService;
   public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
      this.userDirectoryService = userDirectoryService;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.entitybroker.DeveloperHelperService#cloneBean(java.lang.Object, int)
    */
   public <T> T cloneBean(T bean, int level) {
      String pattern = "";
      if (level > 0) {
         for (int i = 0; i < level; i++) {
            if (i > 0) { pattern += "."; }
            pattern += "**";
         }
      }
      T clone = BeanCloner.clone(bean, pattern);
      return clone;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.entitybroker.DeveloperHelperService#getCurrentLocale()
    */
   public Locale getCurrentLocale() {
      return new ResourceLoader().getLocale();
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.entitybroker.DeveloperHelperService#getCurrentLocationReference()
    */
   public String getCurrentLocationReference() {
      String location = null;
      try {
         String context = toolManager.getCurrentPlacement().getContext();
         try {
            Site s = siteService.getSite( context );
            location = s.getReference(); // get the entity reference to the site
         } catch (IdUnusedException e1) {
           Group group = siteService.findGroup( context );
           if ( group != null ) {
              location = group.getReference();
           }
         }
      } catch (Exception e) {
         // sakai failed to get us a location so we can assume we are not inside the portal
         location = null;
      }
      return location;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.entitybroker.DeveloperHelperService#getCurrentUserReference()
    */
   public String getCurrentUserReference() {
      String userId = sessionManager.getCurrentSessionUserId();
      return getUserRefFromUserId(userId);
   }

   public String getUserIdFromRef(String userReference) {
      String userId = null;
      if (userReference != null) {
         // assume the form of "/user/userId" (the UDS method is protected)
         userId = new EntityReference(userReference).getId();
      }
      return userId;
   }

   public String getUserRefFromUserId(String userId) {
      String userRef = null;
      if (userId != null) {
         // user the UDS method for controlling its references
         userRef = userDirectoryService.userReference(userId);
      }
      return userRef;
   }

   public String getCurrentToolReference() {
      String toolRef = null;
      String toolId = toolManager.getCurrentTool().getId();
      // assume the form /tool/toolId
      if (toolId != null) {
         toolRef = new EntityReference("tool", toolId).toString();
      }
      return toolRef;
   }

   public String getToolIdFromToolRef(String toolReference) {
      String toolId = null;
      if (toolReference != null) {
         toolId = new EntityReference(toolReference).getId();
      }
      return toolId;
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
      if (userReference == null || permission == null) {
         throw new IllegalArgumentException("userReference and permission must both be set");
      }
      boolean allowed = false;
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
      return allowed;
   }

   @SuppressWarnings("unchecked")
   public Set<String> getEntityReferencesForUserAndPermission(String userReference, String permission) {
      if (userReference == null || permission == null) {
         throw new IllegalArgumentException("userReference and permission must both be set");
      }

      Set<String> s = new HashSet<String>();
      // get the groups from Sakai
      String userId = getUserIdFromRef(userReference);
      if (userId != null) {
         Set<String> authzGroupIds = 
            authzGroupService.getAuthzGroupsIsAllowed(userId, permission, null);
         if (authzGroupIds != null) {
            s.addAll(authzGroupIds);
         }
      }
      return s;
   }

   @SuppressWarnings("unchecked")
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

}