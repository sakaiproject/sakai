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
package org.sakaiproject.coursemanagement.api;


/**
 * sakai service for publishing course sites a set number of days before a term begins.
 * <p>
 * example: <br/>
 * <code>
 *     fall term, 2008: starts August 20th, 2008
 *
 *     let's say that some course sites, including bio 201, are created for the fall term 2008.
 *     assume that we want the course sites to be published two weeks before the fall term starts.
 *     thus, we want the sakai site for bio 201 to be published on August 20th - 14 days = August 6th, 2008.
 * </code>
 * </p>
 */
public interface CourseSitePublishService {

   // permissions
   public final static String PERMISSION_COURSE_SITE_PUBLISH     = "course_site_publish_service.publish";

   // site property
   public final static String SITE_PROPERTY_COURSE_SITE_PUBLISHED = "course_site_publish_service.publish.set";


   /**
    * publishes course sites whose terms are about to begin.
    * Before a term begins, existing, unpublished course sites are published so that they are then available to the students enrolled in the courses.
    * The courses will be published a number of days before the start of the term, whose value is specified by the <i>course_site_publish_service.num_days_before_term_starts</i> sakai property.
    * </br></br>
    * @param numDaysBeforeTermStarts   number of days before a term starts that course sites should be published.
    * </br></br>
    * @return the number of course sites that were published.
    */
   public int publishCourseSites(int numDaysBeforeTermStarts);
}
