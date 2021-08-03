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

import java.util.Date;
import java.util.List;

import org.sakaiproject.cmprovider.data.CourseOfferingData;
import org.sakaiproject.cmprovider.data.MembershipData;
import org.sakaiproject.cmprovider.data.DateUtils;
import org.sakaiproject.coursemanagement.api.AcademicSession;
import org.sakaiproject.coursemanagement.api.CourseOffering;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.search.Search;

/**
 * Provides a REST API for working with course offerings.
 * @see CourseOffering
 *
 * @author Christopher Schauer
 */
public class CourseOfferingEntityProvider extends AbstractContainerEntityProvider {
  public String getEntityPrefix() {
    return "cm-course-offering";
  }

  public Object getSampleEntity() {
    return new CourseOfferingData();
  }

  @Override
  public boolean entityExists(String id) {
    return cmService.isCourseOfferingDefined(id);
  }

  /**
   * TODO: look into implementing search for course offerings
   */
  public List getEntities(EntityReference ref, Search search) {
    validateUser();
    return null;
  }

  /**
   * Create a new course offering. Wraps CourseManagementAdministration.createCourseOffering.
   * @see CourseManagementAdministration#createCourseOffering
   * @see CourseOfferingData
   */
  public void create(Object entity) {
    CourseOfferingData data = (CourseOfferingData) entity;
    validateDates(data);
    
    cmAdmin.createCourseOffering(
      data.eid,
      data.title,
      data.description,
      data.status,
      data.academicSession,
      data.canonicalCourse,
      DateUtils.stringToDate(data.startDate),
      DateUtils.stringToDate(data.endDate)
    );
  }

  /**
   * Update a course offering. Wraps CourseManagementAdministration.updateCourseOffering.
   * @see CourseManagementAdministration#updateCourseOffering.
   * @see CourseOfferingData
   */
  public void update(Object entity) {
    CourseOfferingData data = (CourseOfferingData) entity;
    validateDates(data);

    CourseOffering updated = cmService.getCourseOffering(data.eid);
    updated.setTitle(data.title);
    updated.setDescription(data.description);
    updated.setStatus(data.status);
    updated.setStartDate(DateUtils.stringToDate(data.startDate));
    updated.setEndDate(DateUtils.stringToDate(data.endDate));

    if (updated.getAcademicSession() == null || updated.getAcademicSession().getEid() != data.academicSession) {
      AcademicSession newSession = cmService.getAcademicSession(data.academicSession);
      updated.setAcademicSession(newSession);
    }

    // TODO: There's no method to set canonical course for CourseOffering. Not sure if this is something
    // that we'll want to implement. It does make sense that you wouldn't ever want to change canonical course
    // since you're basically talking about a completely new course offering at that point.
    cmAdmin.updateCourseOffering(updated);
  }

  /**
   * Get a course offering. Wraps CourseManagementService.getCourseOffering.
   * @see CourseManagementService#getCourseOffering
   */
  public Object get(String eid) {
    CourseOffering course = cmService.getCourseOffering(eid);
    return new CourseOfferingData(course);
  }

  /**
   * Delete a course offering. Wraps CourseManagementAdministration.removeCourseOffering
   * @see CourseManagementAdministration#removeCourseOffering
   */
  public void delete(String eid) {
    cmAdmin.removeCourseOffering(eid);
  }

  public void addOrUpdateMembership(MembershipData data) {
    cmAdmin.addOrUpdateCourseOfferingMembership(
      data.userId,
      data.role,
      data.memberContainer,
      data.status
    );
  }

  public void removeMembership(String userId, String containerId) {
    cmAdmin.removeCourseOfferingMembership(userId, containerId);
  }

  private void validateDates(CourseOfferingData data) {
    Date startDate = DateUtils.stringToDate(data.startDate);
    Date endDate = DateUtils.stringToDate(data.endDate);

    if (startDate == null && endDate == null) return;

    if (startDate == null || endDate == null) {
      throw new IllegalArgumentException("Invalid CourseOfferingData: startDate and endDate must both be null or both be non-null");
    }

    if (startDate.compareTo(endDate) >= 0) {
      throw new IllegalArgumentException("Invalid CourseOfferingData: startDate must occur before endDate");
    }
  }
}
