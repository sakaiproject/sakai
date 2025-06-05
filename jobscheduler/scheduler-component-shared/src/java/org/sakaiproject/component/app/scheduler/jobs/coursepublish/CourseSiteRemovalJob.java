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

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.coursemanagement.api.CourseSiteRemovalService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

/**
 * quartz job to remove course sites after a specified period of time.
 */
@Getter
@Setter
@NoArgsConstructor
@Slf4j
public class CourseSiteRemovalJob implements StatefulJob {
   // sakai.properties
   public final static String PROPERTY_COURSE_SITE_REMOVAL_ACTION                        = "course_site_removal_service.action";
   public final static String PROPERTY_COURSE_SITE_REMOVAL_NUM_DAYS_AFTER_TERM_ENDS      = "course_site_removal_service.num_days_after_term_ends";

   // default values for the sakai.properties
   public final static CourseSiteRemovalService.Action DEFAULT_VALUE_COURSE_SITE_REMOVAL_ACTION = CourseSiteRemovalService.Action.unpublish;
   public final static int DEFAULT_VALUE_COURSE_SITE_REMOVAL_NUM_DAYS_AFTER_TERM_ENDS = 14;

   // sakai services
   private CourseSiteRemovalService courseSiteRemovalService;
   private EventTrackingService eventTrackingService;
   private ServerConfigurationService serverConfigurationService;
   private SessionManager sessionManager;
   private CourseSiteRemovalService.Action action;                  // action to be taken when a course site is found to be expired.
   private int numDaysAfterTermEnds;    // number of days after a term ends when course sites expire.

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
    *    <li>course_site_removal_service.num_days_after_term_ends</li>
    * </ol>
    */
   public void init() {
      log.debug("init()");

      numDaysAfterTermEnds= serverConfigurationService.getInt(PROPERTY_COURSE_SITE_REMOVAL_NUM_DAYS_AFTER_TERM_ENDS, DEFAULT_VALUE_COURSE_SITE_REMOVAL_NUM_DAYS_AFTER_TERM_ENDS);
      // get the action to be taken when a course is found to be expired
      String actionString = serverConfigurationService.getString(PROPERTY_COURSE_SITE_REMOVAL_ACTION);
      if (actionString == null || actionString.trim().isEmpty()) {
         log.warn("The property " + PROPERTY_COURSE_SITE_REMOVAL_ACTION + " was not specified in sakai.properties.  Using a default value of {}.", DEFAULT_VALUE_COURSE_SITE_REMOVAL_ACTION);
         action = DEFAULT_VALUE_COURSE_SITE_REMOVAL_ACTION;
      } else {
         action = getAction(actionString);
         if (action == null) {
            log.error("The value specified for {} in sakai.properties, " + PROPERTY_COURSE_SITE_REMOVAL_ACTION + ", is not valid.  A default value of {} will be used instead.", actionString, DEFAULT_VALUE_COURSE_SITE_REMOVAL_ACTION);
            action = DEFAULT_VALUE_COURSE_SITE_REMOVAL_ACTION;
         }
      }
	}

   /**
    * implement the quartz job interface, which is called by the scheduler when a trigger associated with the job fires.
    * this quartz job removes course sites that are more than a specified number of terms old.
    */
   @Override
   public void execute(JobExecutionContext context) throws JobExecutionException {
      synchronized (this) {
         log.info("execute()");
         String actionStr = CourseSiteRemovalService.Action.remove.equals(action) ? " course sites were removed." : " course sites were unpublished.";

            try {
               // switch the current user to the one specified to run the quartz job
               Session sakaiSesson = sessionManager.getCurrentSession();
               sakaiSesson.setUserId("admin");

               List<String> removedSiteIds = courseSiteRemovalService.removeCourseSites(action, numDaysAfterTermEnds);
               log.info("removeCourseSites: {} {}", removedSiteIds.size(), actionStr);

               String eventType = CourseSiteRemovalService.Action.remove.equals(action) ? 
                  SiteService.SECURE_REMOVE_SITE : SiteService.EVENT_SITE_UNPUBLISH;

               for (String siteId : removedSiteIds) {
                  eventTrackingService.post(eventTrackingService.newEvent(eventType, "/site/" + siteId, siteId, true, NotificationService.NOTI_OPTIONAL));
               }
            } catch (Exception ex) {
               log.error("Error while removing course sites: {}", ex.toString());
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

}
