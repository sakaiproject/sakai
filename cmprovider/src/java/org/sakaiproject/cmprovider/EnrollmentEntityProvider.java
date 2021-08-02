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
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.NotImplementedException;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.cmprovider.data.EnrollmentData;
import org.sakaiproject.cmprovider.data.DateUtils;
import org.sakaiproject.coursemanagement.api.Enrollment;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.search.Search;

/**
 * Provides a REST API for working with enrollments.
 * @see Enrollment
 *
 * @author Christopher Schauer
 */
@Slf4j
public class EnrollmentEntityProvider extends AbstractCmEntityProvider {
  public String getEntityPrefix() {
    return "cm-enrollment";
  }

  public Object getSampleEntity() {
    return new EnrollmentData();
  }

  @Override
  public boolean entityExists(String id) {
    String[] eids = splitEid(id);
    return cmService.isEnrolled(eids[0], eids[1]);
  }

  /**
   * TODO: look into implementing a search for enrollments
   */
  public List getEntities(EntityReference ref, Search search) {
    validateUser();
    return null;
  }

  /**
   * Same as updateEntity.
   * @see EnrollmentEntityProviderImpl#updateEntity
   */
  public void create(Object entity) { 
    createOrUpdateEnrollment(entity);  
  }

  /**
   * Add or update an enrollment. Wraps CourseManagementAdministration.addOrUpdateEnrollment
   * @see CourseManagementAdministration#addOrUpdateEnrollment
   * @see EnrollmentData
   */
  public void update(Object entity) {
    createOrUpdateEnrollment(entity);  
  }

  private void createOrUpdateEnrollment(Object entity) {
    EnrollmentData data = (EnrollmentData) entity;
    cmAdmin.addOrUpdateEnrollment(data.userId, data.enrollmentSet, data.status, data.credits, data.gradingScheme, DateUtils.stringToDate(data.dropDate));
    
    if (data.dropped) {
      // Removing an enrollment marks it as dropped in the database.
      cmAdmin.removeEnrollment(data.userId, data.enrollmentSet);
    }

    updateSitesWithEnrollment(data.userId, data.enrollmentSet);
  }

  /**
   * Get an enrollment by enrollment set eid and user eid. Wraps CourseManagementService.findEnrollment.
   * @see CourseManagementService#findEnrollment
   */
  public Object get(String eid) {
    String[] eids = splitEid(eid);
    Enrollment enrollment = cmService.findEnrollment(eids[0], eids[1]);
    if (enrollment == null) return null;
    
    EnrollmentData data = new EnrollmentData(enrollment, eids[1]);
    return data;
  }

  /**
   * Enrollments should never be deleted. Instead, they should be updated with
   * EnrollmentData.dropped = true.
   *
   * Always results in a 500.
   */
  public void delete(String eid) {
    throw new NotImplementedException("To delete (drop) an enrollment, update the enrollment with dropped=true and the drop date.");
  }

  private String[] splitEid(String eid) {
    String[] eids = eid.split(",");
    if (eids.length != 2)
      throw new IllegalArgumentException("Id for enrollment must be of the form <user_eid>,<enrollment_set_eid>");
    return eids;
  }

  // TODO: Update memberships individually rather than going through all
  // the memberships to update like the authzGroupService.save does.
  // Right now this is equivalent to our nightly update participants job.
  private void updateSitesWithEnrollment(String userEid, String enrollmentSetEid) {
    Set<String> groupIds = authzGroupService.getAuthzGroupIds(enrollmentSetEid);
    for (String id : groupIds) {
      try {
        AuthzGroup group = authzGroupService.getAuthzGroup(id);
        authzGroupService.save(group);
      } catch (GroupNotDefinedException ex) {
        // This should never happen since the id was given to us from getAuthzGroupIds.
        log.error(ex.getMessage(), ex);
        throw new RuntimeException("An error occured updating site " + id + " with provider id " + enrollmentSetEid);
      } catch (AuthzPermissionException ex) {
        // This should also never happen since a SecurityException would've been thrown earlier.
        log.error(ex.getMessage(), ex);
        throw new RuntimeException("An error occured updating site " + id + " with provider id " + enrollmentSetEid);
      }
    }
  }
}
