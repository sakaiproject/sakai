/**
 * Copyright (c) 2015 The Apereo Foundation
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
package org.sakaiproject.cmprovider;

import java.util.List;

import org.sakaiproject.cmprovider.data.CanonicalCourseData;
import org.sakaiproject.coursemanagement.api.CanonicalCourse;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.search.Search;

/**
 * Provides a REST API for working with canonical courses.
 * @see CanonicalCourse
 *
 * @author Christopher Schauer 
 */
public class CanonicalCourseEntityProvider extends AbstractCmEntityProvider {
  public String getEntityPrefix() {
    return "cm-canonical-course";
  }
  
  @Override
  public boolean entityExists(String id) {
    return cmService.isCanonicalCourseDefined(id);
  }

  public Object getSampleEntity() {
    return new CanonicalCourseData();
  }

  public List getEntities(EntityReference ref, Search search) {
    validateUser();
    // TODO: Possibly implement search to return getEquivalentCanonicalCourses(canonicalCourseEid).
    // None of the other methods in CourseManagementService make sense to be exposed here.
    return null;
  }

  /**
   * Create a new canonical course. Wraps CourseManagementAdministration.createCanonicalCourse.
   * @see CourseManagementAdministration#createCanonicalCourse
   * @see CanonicalCourseData
   */
  public void create(Object entity) {
    CanonicalCourseData data = (CanonicalCourseData) entity;
    cmAdmin.createCanonicalCourse(data.eid, data.title, data.description);
  }

  /**
   * Update a canonical course. Wraps CourseManagementAdministration.updateCanonicalCourse.
   * @see CourseManagementAdministration#updateCanonicalCourse
   * @see CanonicalCourseData
   */
  public void update(Object entity) {
    CanonicalCourseData data = (CanonicalCourseData) entity;
    CanonicalCourse updated = cmService.getCanonicalCourse(data.eid);
    updated.setTitle(data.title);
    updated.setDescription(data.description);
    cmAdmin.updateCanonicalCourse(updated);
  }

  /**
   * Get a canonical course by eid. Wraps CourseManageService.getCanonicalCourse.
   * @see CourseManagementService#getCanonicalCourse
   */
  public Object get(String eid) {
    CanonicalCourse course = cmService.getCanonicalCourse(eid);
    return new CanonicalCourseData(course);
  }

  /**
   * Delete a canonical course by eid. Wraps CourseManagementAdministration.removeCanonicalCourse.
   * @see CourseManagementAdministration#removeCanonicalCourse
   */
  public void delete(String eid) {
    cmAdmin.removeCanonicalCourse(eid);
  }

  // TODO: Get course sets associated with this canonical course

  // TODO: Get course offerings associated with this canonical course
}
