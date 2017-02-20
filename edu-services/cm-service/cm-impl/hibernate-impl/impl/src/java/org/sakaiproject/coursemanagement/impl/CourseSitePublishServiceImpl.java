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
package org.sakaiproject.coursemanagement.impl;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.coursemanagement.api.AcademicSession;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.CourseSitePublishService;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.site.api.SiteService.SortType;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

import lombok.extern.slf4j.Slf4j;

/**
 * This class is an implementation of the auto site publish service interface.
 */
@Slf4j
public class CourseSitePublishServiceImpl extends HibernateDaoSupport implements CourseSitePublishService {
   // class members
   private static final long ONE_DAY_IN_MS = 1000L * 60L * 60L * 24L;    // one day in ms = 1000ms/s · 60s/m · 60m/h · 24h/day

   // sakai services
   private CourseManagementService courseManagementService;
   private FunctionManager functionManager;
   private SecurityService securityService;
   private SiteService siteService;

   /**
    * called by the spring framework.
    */
   public void destroy() {
      log.debug("destroy()");

      // no code necessary
   }

   /**
    * called by the spring framework after this class has been instantiated, this method registers the permissions necessary to invoke the course site removal service.
    */
   public void init() {
      log.debug("init()");

      // register permissions with sakai
      functionManager.registerFunction(PERMISSION_COURSE_SITE_PUBLISH);
   }

   /**
    * returns the instance of the CourseManagementService injected by the spring framework specified in the components.xml file via IoC.
    * <br/><br/>
    * @return the instance of the CourseManagementService injected by the spring framework specified in the components.xml file via IoC.
    */
   public CourseManagementService getCourseManagementService() {
      return courseManagementService;
   }

   /**
    * called by the spring framework to initialize the courseManagementService data member specified in the components.xml file via IoC.
    * <br/><br/>
    * @param courseManagementService   the implementation of the CourseManagementService interface provided by the spring framework.
    */
   public void setCourseManagementService(CourseManagementService courseManagementService) {
      this.courseManagementService = courseManagementService;
   }
   /**
    * returns the instance of the FunctionManager injected by the spring framework specified in the components.xml file via IoC.
    * <br/><br/>
    * @return the instance of the FunctionManager injected by the spring framework specified in the components.xml file via IoC.
    */
   public FunctionManager getFunctionManager() {
      return functionManager;
   }

   /**
    * called by the spring framework to initialize the functionManager data member specified in the components.xml file via IoC.
    * <br/><br/>
    * @param functionManager   the implementation of the FunctionManager interface provided by the spring framework.
    */
   public void setFunctionManager(FunctionManager functionManager) {
      this.functionManager = functionManager;
   }

   /**
    * returns the instance of the SecurityService injected by the spring framework specified in the components.xml file via IoC.
    * <br/><br/>
    * @return the instance of the SecurityService injected by the spring framework specified in the components.xml file via IoC.
    */
   public SecurityService getSecurityService() {
      return securityService;
   }

   /**
    * called by the spring framework to initialize the securityService data member specified in the components.xml file via IoC.
    * <br/><br/>
    * @param securityService   the implementation of the SecurityService interface provided by the spring framework.
    */
   public void setSecurityService(SecurityService securityService) {
      this.securityService = securityService;
   }

   /**
    * returns the instance of the SiteService injected by the spring framework specified in the components.xml file via IoC.
    * <br/><br/>
    * @return the instance of the SiteService injected by the spring framework specified in the components.xml file via IoC.
    */
   public SiteService getSiteService() {
      return siteService;
   }

   /**
    * called by the spring framework to initialize the siteService data member specified in the components.xml file via IoC.
    * <br/><br/>
    * @param siteService   the implementation of the SiteService interface provided by the spring framework.
    */
   public void setSiteService(SiteService siteService) {
      this.siteService = siteService;
   }

   /**
    * publishes course sites whose terms are about to begin.
    * <p>
    * Before a term begins, existing, unpublished course sites are published so that they are then available to the students enrolled in the courses.
    * The courses will be published a number of days before the start of the term, whose value is specified by the <i>course_site_publish_service.num_days_before_term_starts</i> sakai property.
    * </p>
    * <p>
    *
    *                            term                             term
    *                            start                            end
    *  ------------------------------------------------------------------> time
    *            [ grace period ] [                                ]
    *
    *            [<----------------------------------------------->]
    *             if this method is invoked at any time during this
    *             time period, unpublished course sites associated
    *             with this term will be published.
    *
    * </p>
    * <p>
    * There is a special use case to consider.
    * Say that this method is invoked, and all the course sites for the current term are published.
    * Now, suppose one particular instructor forgot to add some content, or made some mistakes setting up his course site, and he wants to unpublish his couse site while
    * he makes some changes to it.  He goes into sakai, and uses the site setup tool to mark the course site as unpublished.  But, suppose the next day this service is
    * invoked again (via a quartz job).  His course would get published again, even though he doesn't want it to be, and has manually gone into sakai and set it to
    * unpublished.  In order to prevent this case from happening, whenever this service marks a course site as published, it also sets a site property called <i>course_site_publish_service.publish.set</i>.
    * Then, if an instructor manually resets his site to be unpublished, this service will not automatically publish it.  Rather, the instructor who manually unpublished the
    * course site will have to go into sakai, use the site setup tool, and manually publish it.
    * </p>
    * </br></br>
    * @param numDaysBeforeTermStarts   number of days before a term starts that course sites should be published.
    * </br></br>
    * @return the number of course sites that were published.
    */
   public int publishCourseSites(int numDaysBeforeTermStarts) {

      log.info("publishCourseSites(" + numDaysBeforeTermStarts + " days before the term starts)");

      Date today             = new Date();
      Date publishDate       = null;
      int  numSitesPublished = 0;

      try {
         // get the list of the academic terms
         List<AcademicSession> academicSessions = courseManagementService.getAcademicSessions();

         for(AcademicSession academicSession : academicSessions) {
            publishDate = new Date(academicSession.getStartDate().getTime() - numDaysBeforeTermStarts * ONE_DAY_IN_MS);
            // see if the academic is scheduled to start within the specified number of days
            if (publishDate.getTime() < today.getTime() && today.getTime() < academicSession.getEndDate().getTime()) {

               // get a list of all published and unpublished course sites in ascending creation date order which are associated with the specified academic session
               Hashtable<String, String> propertyCriteria = new Hashtable<String, String>();
               propertyCriteria.put("term_eid", academicSession.getEid());
               //We only will check COURSES with the right term_eid property. We will filter later if they are or not published
               List<String> sites = (List<String>)siteService.getSiteIds(SelectionType.ANY, "course", null, propertyCriteria, SortType.CREATED_ON_ASC, null);

               for(String siteId : sites) {
                     // see if this service has already published course site once before.
                     // if it has, then someone has manually reset the published flag, and wants the course to be unpublished.
                     // so don't switch it back to being published - just leave it as unpublished.
                  Site site = siteService.getSite(siteId);
                  //We will check only unpublished sites and not softlyDeleted
                  if (!site.isPublished() && (!site.isSoftlyDeleted())) {
                     ResourcePropertiesEdit siteProperties = site.getPropertiesEdit();
                     String siteProperty = siteProperties.getProperty(SITE_PROPERTY_COURSE_SITE_PUBLISHED);

                     if (!"set".equals(siteProperty)) {
                        // check permissions
                        if (!checkPermission(PERMISSION_COURSE_SITE_PUBLISH, site.getId())) {
                           log.error("You do not have permission to publish the " + site.getTitle() + " (" + site.getId() + ").");
                        } else {
                           // publish the course site
                           log.debug("publishing course site " + site.getTitle() + " (" + site.getId() + ").");
                           siteProperties.addProperty(SITE_PROPERTY_COURSE_SITE_PUBLISHED, "set");
                           site.setPublished(true);
                           siteService.save(site);
                           numSitesPublished++;
                        }
                     }
                  }
               }
            }
         }
      } catch (Exception ex) {
         log.error(ex.getMessage(), ex);
      }
      return numSitesPublished;
   }



   private boolean checkPermission(String lock, String reference) {

      return securityService.unlock(lock, reference);

   }


}
