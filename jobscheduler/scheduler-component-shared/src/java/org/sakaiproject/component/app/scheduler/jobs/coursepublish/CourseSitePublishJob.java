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

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.coursemanagement.api.CourseSitePublishService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

/**
 * quartz job to remove course sites after a specified period of time.
 */
@Getter
@Setter
@Slf4j
public class CourseSitePublishJob implements StatefulJob {

   // sakai.properties
   public final static String PROPERTY_COURSE_SITE_PUBLISH_NUM_DAYS_BEFORE_TERM_STARTS      = "course_site_publish_service.num_days_before_term_starts";

   // default values for the sakai.properties
   public final static int DEFAULT_VALUE_COURSE_SITE_PUBLISH_NUM_DAYS_BEFORE_TERM_STARTS = 14;

   // sakai services
   private CourseSitePublishService   courseSitePublishService;
   private ServerConfigurationService serverConfigurationService;
   private SessionManager sessionManager;

   // data members
   private int numDaysBeforeTermStarts;    // number of days before a term starts when course sites will be published and made available to students enrolled in the course.

   /**
    * default constructor.
    */
   public CourseSitePublishJob() {
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
    *    <li>course_site_publish_service.num_days_before_term_starts</li>
    * </ol>
    */
   public void init() {

      log.debug("init()");

      // get the number of days after a term ends after which course sites that have expired will be removed
      try{
         numDaysBeforeTermStarts= serverConfigurationService.getInt(PROPERTY_COURSE_SITE_PUBLISH_NUM_DAYS_BEFORE_TERM_STARTS, DEFAULT_VALUE_COURSE_SITE_PUBLISH_NUM_DAYS_BEFORE_TERM_STARTS);
         } catch (NumberFormatException ex) {
            log.error("The value specified for numDaysBeforeTermStarts in sakai.properties, " + PROPERTY_COURSE_SITE_PUBLISH_NUM_DAYS_BEFORE_TERM_STARTS + ", is not valid.  A default value of " + DEFAULT_VALUE_COURSE_SITE_PUBLISH_NUM_DAYS_BEFORE_TERM_STARTS + " will be used instead.");
            numDaysBeforeTermStarts = DEFAULT_VALUE_COURSE_SITE_PUBLISH_NUM_DAYS_BEFORE_TERM_STARTS;
         }
      if (numDaysBeforeTermStarts < 0) {
         log.error("The value specified for numDaysBeforeTermStartsString in sakai.properties, " + PROPERTY_COURSE_SITE_PUBLISH_NUM_DAYS_BEFORE_TERM_STARTS + ", is not valid.  A default value of " + DEFAULT_VALUE_COURSE_SITE_PUBLISH_NUM_DAYS_BEFORE_TERM_STARTS + " will be used instead.");
         numDaysBeforeTermStarts = DEFAULT_VALUE_COURSE_SITE_PUBLISH_NUM_DAYS_BEFORE_TERM_STARTS;
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

            try {
               // switch the current user to the one specified to run the quartz job
               Session sakaiSesson = sessionManager.getCurrentSession();
               sakaiSesson.setUserId("admin");

               int numSitesPublished = courseSitePublishService.publishCourseSites(numDaysBeforeTermStarts);
                log.info("{} course sites were published.", numSitesPublished);
            } catch (Exception ex) {
               log.error("Error while publishing course sites: {}", ex.toString());
            }
      }
   }

}
