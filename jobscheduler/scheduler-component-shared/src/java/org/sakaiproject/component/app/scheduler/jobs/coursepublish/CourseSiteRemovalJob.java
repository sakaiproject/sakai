/**
 * Copyright (c) 2015 Apereo Foundation
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
package org.sakaiproject.component.app.scheduler.jobs.coursepublish;

import lombok.extern.slf4j.Slf4j;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.coursemanagement.api.CourseSiteRemovalService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * quartz job to remove course sites after a specified period of time.
 */
@Slf4j
public class CourseSiteRemovalJob implements StatefulJob {
   // sakai.properties
   public final static String PROPERTY_COURSE_SITE_REMOVAL_ACTION                        = "course_site_removal_service.action";
   public final static String PROPERTY_COURSE_SITE_REMOVAL_USER                          = "course_site_removal_service.user";
   public final static String PROPERTY_COURSE_SITE_REMOVAL_NUM_DAYS_AFTER_TERM_ENDS      = "course_site_removal_service.num_days_after_term_ends";

   // default values for the sakai.properties
   public final static CourseSiteRemovalService.Action DEFAULT_VALUE_COURSE_SITE_REMOVAL_ACTION                   = CourseSiteRemovalService.Action.unpublish;
   public final static String DEFAULT_VALUE_COURSE_SITE_REMOVAL_SITE_TYPE                = "course";
   public final static String DEFAULT_VALUE_COURSE_SITE_REMOVAL_USER                     = "admin";
   public final static int DEFAULT_VALUE_COURSE_SITE_REMOVAL_NUM_DAYS_AFTER_TERM_ENDS = 14;

   // sakai services
   private CourseSiteRemovalService   courseSiteRemovalService;
   private ServerConfigurationService serverConfigurationService;
   private SessionManager sessionManager;
   private UserDirectoryService userDirectoryService;

   // data members
   private User user;                    // sakai user who will be used when running this job
   private CourseSiteRemovalService.Action action;                  // action to be taken when a course site is found to be expired.
   private int numDaysAfterTermEnds;    // number of days after a term ends when course sites expire.




   /**
    * default constructor.
    */
   public CourseSiteRemovalJob() {
      // no code necessary
	}

   /**
    * called by the spring framework.
    */
   public void destroy() {
      log.info("destroy()");

      // no code necessary
   }

   /**
    * called by the spring framework after this class has been instantiated, this method reads in the following values from sakai.properties:
    * <ol>
    *    <li>course_site_removal_service.action                  </li>
    *    <li>course_site_removal_service.user                    </li>
    *    <li>course_site_removal_service.num_days_after_term_ends</li>
    * </ol>
    */
   public void init() {
      log.debug("init()");

      // get the number of days after a term ends after which course sites that have expired will be removed
      try{
         numDaysAfterTermEnds= serverConfigurationService.getInt(PROPERTY_COURSE_SITE_REMOVAL_NUM_DAYS_AFTER_TERM_ENDS, DEFAULT_VALUE_COURSE_SITE_REMOVAL_NUM_DAYS_AFTER_TERM_ENDS);
      } catch (NumberFormatException ex) {
         log.error("The value specified for numDaysAfterTermEnds in sakai.properties, " + PROPERTY_COURSE_SITE_REMOVAL_NUM_DAYS_AFTER_TERM_ENDS + ", is not valid.  A default value of " + DEFAULT_VALUE_COURSE_SITE_REMOVAL_NUM_DAYS_AFTER_TERM_ENDS + " will be used instead.");
         numDaysAfterTermEnds = DEFAULT_VALUE_COURSE_SITE_REMOVAL_NUM_DAYS_AFTER_TERM_ENDS;
      }
      if (numDaysAfterTermEnds < 0) {
         log.error("The value specified for numDaysAfterTermEnds in sakai.properties, " + PROPERTY_COURSE_SITE_REMOVAL_NUM_DAYS_AFTER_TERM_ENDS + ", is not valid.  A default value of " + DEFAULT_VALUE_COURSE_SITE_REMOVAL_NUM_DAYS_AFTER_TERM_ENDS + " will be used instead.");
         numDaysAfterTermEnds = DEFAULT_VALUE_COURSE_SITE_REMOVAL_NUM_DAYS_AFTER_TERM_ENDS;
      }

      // get the user which will be used to run the quartz job
      String userId = serverConfigurationService.getString(PROPERTY_COURSE_SITE_REMOVAL_USER,DEFAULT_VALUE_COURSE_SITE_REMOVAL_USER);
      try {
         user = userDirectoryService.getUser(userId);
      } catch (UserNotDefinedException ex) {
         user = null;
         log.error("The user with eid " + userId + " was not found.  The course site publish job has been aborted.");
      }


      // get the action to be taken when a course is found to be expired
      String actionString = serverConfigurationService.getString(PROPERTY_COURSE_SITE_REMOVAL_ACTION);
      if (actionString == null || actionString.trim().length() == 0)
      {
         log.warn("The property " + PROPERTY_COURSE_SITE_REMOVAL_ACTION + " was not specified in sakai.properties.  Using a default value of " + DEFAULT_VALUE_COURSE_SITE_REMOVAL_ACTION + ".");
         action = DEFAULT_VALUE_COURSE_SITE_REMOVAL_ACTION;
      }
      else
      {
         action = getAction(actionString);
         if (action == null) {
            log.error("The value specified for " + actionString + " in sakai.properties, " + PROPERTY_COURSE_SITE_REMOVAL_ACTION + ", is not valid.  A default value of " + DEFAULT_VALUE_COURSE_SITE_REMOVAL_ACTION + " will be used instead.");
            action = DEFAULT_VALUE_COURSE_SITE_REMOVAL_ACTION;
         }
      }
	}

   /**
    * implement the quartz job interface, which is called by the scheduler when a trigger associated with the job fires.
    * this quartz job removes course sites that are more than a specified number of terms old.
    */
   public void execute(JobExecutionContext context) throws JobExecutionException {
      synchronized (this) {
         log.info("execute()");

         if (user == null) {
            log.error("The scheduled job to remove course sites can not be run with an invalid user.  No courses were removed.");
         } else {
            try {
               // switch the current user to the one specified to run the quartz job
               Session sakaiSesson = sessionManager.getCurrentSession();
               sakaiSesson.setUserId(user.getId());

               int numSitesRemoved = courseSiteRemovalService.removeCourseSites(action, numDaysAfterTermEnds);
               log.info(numSitesRemoved + " course sites were removed.");
            } catch (Exception ex) {
               log.error(ex.getMessage());
            }
         }
      }
   }

   /**
    * @return the enum action corresponding to the string or null if the string is not a valid action.
    * <br/><br/>
    * @param string   the action that is to be parsed.
    */
   private CourseSiteRemovalService.Action getAction(String string) {
      CourseSiteRemovalService.Action action = null;

      for (CourseSiteRemovalService.Action a : CourseSiteRemovalService.Action.values())
         if (a.toString().equals(string))
            action = a;
      return action;
   }

   /**
    * returns the instance of the SiteRemovalService injected by the spring framework specified in the components.xml file via IoC.
    * <br/><br/>
    * @return the instance of the SiteRemovalService injected by the spring framework specified in the components.xml file via IoC.
    */
   public CourseSiteRemovalService getCourseSiteRemovalService() {
      return courseSiteRemovalService;
   }

   /**
    * called by the spring framework to initialize the courseSiteRemovalService data member specified in the components.xml file via IoC.
    * <br/><br/>
    * @param courseSiteRemovalService   the implementation of the SiteRemovalService interface provided by the spring framework.
    */
   public void setCourseSiteRemovalService(CourseSiteRemovalService courseSiteRemovalService) {
      this.courseSiteRemovalService = courseSiteRemovalService;
   }

   /**
    * returns the instance of the ServerConfigurationService injected by the spring framework specified in the components.xml file via IoC.
    * <br/><br/>
    * @return the instance of the ServerConfigurationService injected by the spring framework specified in the components.xml file via IoC.
    */
   public ServerConfigurationService getServerConfigurationService() {
      return serverConfigurationService;
   }

   /**
    * called by the spring framework to initialize the serverConfigurationService data member specified in the components.xml file via IoC.
    * <br/><br/>
    * @param serverConfigurationService   the implementation of the ServerConfigurationService interface provided by the spring framework.
    */
   public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
      this.serverConfigurationService = serverConfigurationService;
   }

   /**
    * returns the instance of the SessionManager injected by the spring framework specified in the components.xml file via IoC.
    * <br/><br/>
    * @return the instance of the SessionManager injected by the spring framework specified in the components.xml file via IoC.
    */
   public SessionManager getSessionManager() {
      return sessionManager;
   }

   /**
    * called by the spring framework to initialize the sessionManager data member specified in the components.xml file via IoC.
    * <br/><br/>
    * @param sessionManager   the implementation of the SessionManager interface provided by the spring framework.
    */
   public void setSessionManager(SessionManager sessionManager) {
      this.sessionManager = sessionManager;
   }

   /**
    * @return the user which will be used to run the quartz job.
    */
   public User getUser() {
      return user;
   }

   /**
    * sets the user which will be used to run the quartz job.
    */
   public void setUser() {
   }

   /**
    * returns the instance of the UserDirectoryService injected by the spring framework specified in the components.xml file via IoC.
    * <br/><br/>
    * @return the instance of the UserDirectoryService injected by the spring framework specified in the components.xml file via IoC.
    */
   public UserDirectoryService getUserDirectoryService() {
      return userDirectoryService;
   }

   /**
    * called by the spring framework to initialize the userDirectoryService data member specified in the components.xml file via IoC.
    * <br/><br/>
    * @param userDirectoryService   the implementation of the UserDirectoryService interface provided by the spring framework.
    */
   public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
      this.userDirectoryService = userDirectoryService;
   }
}
