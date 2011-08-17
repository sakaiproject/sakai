/**
 * $Id: SessionEntityProvider.java 51318 2008-08-24 05:28:47Z csev@umich.edu $
 * $URL: https://source.sakaiproject.org/svn/entitybroker/trunk/impl/src/java/org/sakaiproject/entitybroker/providers/SessionEntityProvider.java $
 * SessionEntityProvider.java - entity-broker - Jul 15, 2008 4:03:52 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
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
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CRUDable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CollectionResolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Inputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RedirectDefinable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestAware;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestGetter;
import org.sakaiproject.entitybroker.entityprovider.extension.TemplateMap;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.providers.model.EntitySession;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * Entity provider for Sakai Sessions
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class SessionEntityProvider extends AbstractEntityProvider implements CoreEntityProvider, CRUDable, CollectionResolvable, 
      Inputable, Outputable, RequestAware, Describeable, RedirectDefinable, ActionsExecutable {

   public static String AUTH_USERNAME = "_username";
   public static String AUTH_PASSWORD = "_password";

   public SessionManager sessionManager;
   public void setSessionManager(SessionManager sessionManager) {
      this.sessionManager = sessionManager;
   }

   public UserDirectoryService userDirectoryService;
   public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
      this.userDirectoryService = userDirectoryService;
   }


   public static String PREFIX = "session";
   public String getEntityPrefix() {
      return PREFIX;
   }


   public TemplateMap[] defineURLMappings() {
      // see javadoc for this method for notes on special mappings
      return new TemplateMap[] {
            new TemplateMap("/{prefix}/{id}/norefresh", "/{prefix}/{id}{dot-extension}?auto=true"),
            new TemplateMap("/{prefix}/current/norefresh", "/{prefix}/current{dot-extension}?auto=true"),
            // below for testing only
//          new TemplateMap("/{prefix}/xml/{id}", "/{prefix}/{id}.xml"),
//          new TemplateMap("/{prefix}/test1", "/{prefix}/test2"),
//          new TemplateMap("/{prefix}/test2", "/{prefix}/test3"),
//          new TemplateMap("/{prefix}/test3", "/{prefix}/test1"),
      };
   }

   @EntityCustomAction(action="current",viewKey=EntityView.VIEW_LIST)
   public Object getCurrentSession() {
      EntitySession es = null;
      Session s = sessionManager.getCurrentSession();
      if (s != null) {
         es = new EntitySession(s);
         es.setId(null); // SAK-19669 - do not allow session id to be visible for current session
      }
      return es;
   }

   public boolean entityExists(String id) {
      if (id == null) {
         return false;
      }
      if ("".equals(id)) {
         return true;
      }
      Session s = sessionManager.getSession(id);
      if (s != null) {
         return true;
      }
      return false;
   }

   public Object getSampleEntity() {
      return new EntitySession();
   }

   public Object getEntity(EntityReference ref) {
      if (ref.getId() == null) {
         return new EntitySession();
      }
      String sessionId = ref.getId();
      Session s = sessionManager.getSession(sessionId);
      if (s == null) {
         throw new IllegalArgumentException("Cannot find session with id: " + sessionId);
      }
      EntitySession es = new EntitySession(s);
      return es;
   }

   public String createEntity(EntityReference ref, Object entity, Map<String, Object> params) {
      EntitySession es = (EntitySession) entity;
      String newSessionId = null;
      Session currentSession = null;
      if (developerHelperService.isUserAdmin(developerHelperService.getCurrentUserReference())) {
         // create the session with the given settings
         if (es.getUserId() == null || es.getUserId().equals("")) {
            throw new IllegalArgumentException("UserId must be set when creating a session");
         }
         User u = null;
         try {
            u = userDirectoryService.getUser(es.getUserId());
         } catch (UserNotDefinedException e) {
            throw new IllegalArgumentException("Invalid userId provided in session object, could not find user with that id: " + es.getUserId());
         }
         currentSession = sessionManager.startSession(es.getId());
         currentSession.setUserEid(u.getEid());
         currentSession.setUserId(u.getId());
      } else {
         // when creating a new session we need some data from the request
         HttpServletRequest req = requestGetter.getRequest();
         if (req == null) {
            throw new IllegalStateException("Only super admins can create sessions without using a REST request currently");
         } else {
            String username = req.getParameter(AUTH_USERNAME);
            String password = req.getParameter(AUTH_PASSWORD);
            if (username == null || username.equals("") 
                  || password == null || password.equals("")) {
               throw new IllegalArgumentException("A session entity cannot be created without providing the username and password, " 
                     + "the username must be provided as '_username' and the password as '_password' in the POST");
            }
            // now we auth
            User u = userDirectoryService.authenticate(username, password);
            if (u == null) {
               throw new SecurityException("The username or password provided were invalid, could not authenticate user ("+username+") to create a session");
            }
            // create session or update existing one
            currentSession = sessionManager.getCurrentSession();
            if (currentSession == null) {
               // start a session if none is found
               currentSession = sessionManager.startSession();
            }
            currentSession.setUserId(u.getId());
            currentSession.setUserEid(u.getEid());
         }
      }

      // set fields from the passed in session object
      if (es.getMaxInactiveInterval() > 0) {
         currentSession.setMaxInactiveInterval(es.getMaxInactiveInterval());
      }

      newSessionId = currentSession.getId();
      return newSessionId;
   }


   public void updateEntity(EntityReference ref, Object entity, Map<String, Object> params) {
      String sessionId = ref.getId();
      if (sessionId == null) {
         throw new IllegalArgumentException("Cannot update session, No sessionId in provided reference: " + ref);
      }
      Session s = sessionManager.getSession(sessionId);
      if (s == null) {
         throw new IllegalArgumentException("Cannot find session to update with id: " + sessionId);
      }
      checkSessionOwner(s);
      // this simply causes the session to remain active, other changes are not allowed
      s.setActive();
   }


   public void deleteEntity(EntityReference ref, Map<String, Object> params) {
      String sessionId = ref.getId();
      if (sessionId == null) {
         throw new IllegalArgumentException("Cannot update session, No sessionId in provided reference: " + ref);
      }
      Session s = sessionManager.getSession(sessionId);
      if (s == null) {
         throw new IllegalArgumentException("Cannot find session with id: " + sessionId);
      }
      checkSessionOwner(s);
      s.invalidate();
   }


   public List<?> getEntities(EntityReference ref, Search search) {
//    String userReference = developerHelperService.getCurrentUserReference();
//    String userId = developerHelperService.getUserIdFromRef(userReference);
//    if (developerHelperService.isUserAdmin(userReference)) {
//    // get all current usage sessions
//    List<UsageSession> usageSessions = usageSessionService.getOpenSessions();
//    for (UsageSession usageSession : usageSessions) {
//    usageSession.
//    }
//    }
      // just get the current session for now
      List<EntitySession> sessions = new ArrayList<EntitySession>();
      EntitySession es = (EntitySession) getCurrentSession();
      if (es != null) {
         sessions.add( es );
      }
      return sessions;
   }


   public String[] getHandledInputFormats() {
      return new String[] { Formats.HTML, Formats.XML, Formats.JSON };
   }

   public String[] getHandledOutputFormats() {
      return new String[] { Formats.HTML, Formats.XML, Formats.JSON, Formats.FORM };
   }


   private RequestGetter requestGetter;
   public void setRequestGetter(RequestGetter requestGetter) {
      this.requestGetter = requestGetter;
   }

   /**
    * Checks if the current user can modify the session
    * @param s the session
    */
   private void checkSessionOwner(Session s) {
      String currentUser = developerHelperService.getCurrentUserReference();
      String currentUserId = developerHelperService.getUserIdFromRef(currentUser);
      if (developerHelperService.isUserAdmin(currentUser)) {
         return;
      } else {
         String userId = s.getUserId();
         if (userId.equals(currentUserId)) {
            return;
         }
      }
      throw new SecurityException("Current user ("+currentUser+") cannot modify this session: " + s.getId() + ", they are not the owner or not an admin");
   }

}
