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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.cmprovider.data.EnrollmentSetData;
import org.sakaiproject.coursemanagement.api.EnrollmentSet;
import org.sakaiproject.coursemanagement.impl.EnrollmentSetCmImpl;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.search.Search;

/**
 * Provides a REST API for working with enrollment sets.
 * @see EnrollmentSet
 *
 * @author Christopher Schauer
 */
@Slf4j
public class EnrollmentSetEntityProvider extends AbstractCmEntityProvider {
  public String getEntityPrefix() {
    return "cm-enrollment-set";
  }

  public Object getSampleEntity() {
    return new EnrollmentSetData();
  }

  @Override
  public boolean entityExists(String id) {
    return cmService.isEnrollmentSetDefined(id);
  }

  /**
   * TODO: look into implementing search for enrollment sets
   */
  public List getEntities(EntityReference ref, Search search) {
    validateUser();
    return null;
  }

  /**
   * Create a new enrollment set. Wraps CourseManagementAdministration.createEnrollmentSet.
   * @see CourseManagementAdministration#createEnrollmentSet
   * @see EnrollmentSetData
   */
  public void create(Object entity) {
    EnrollmentSetData data = (EnrollmentSetData) entity;
    Set<String> instructors = null;
    if (data.officialInstructors != null) {
      instructors = new HashSet<String>(data.officialInstructors);
    }
    cmAdmin.createEnrollmentSet(data.eid, data.title, data.description, data.category, data.defaultCredits, data.courseOffering, instructors);
  }

  /**
   * Update an enrollment set. Wraps CourseManagementAdministration.updateEnrollmentSet.
   * @see CourseManagementAdministration#updateEnrollmentSet
   * @see EnrollmentSetData
   */
  public void update(Object entity) {
    EnrollmentSetData data = (EnrollmentSetData) entity;
    EnrollmentSet updated = hibernateService.getEnrollmentSetByEid(data.eid);
    updated.setTitle(data.title);
    updated.setDescription(data.description);
    updated.setCategory(data.category);
    updated.setDefaultEnrollmentCredits(data.defaultCredits);
    Set<String> instructors = null;
    if (data.officialInstructors != null) {
      instructors = new HashSet<String>(data.officialInstructors);
    }
    updated.setOfficialInstructors(instructors);
    cmAdmin.updateEnrollmentSet(updated);
    updateSitesWithEnrollmentSet(data.eid);
  }

  /**
   * Get an enrollment set by eid. Wraps CourseManagementService.getEnrollmentSetByEid.
   * @see CourseManagementService#getEnrollmentSetByEid
   */
  public Object get(String eid) {
    EnrollmentSetCmImpl set = hibernateService.getEnrollmentSetByEid(eid);
    return new EnrollmentSetData(set);
  }

  /**
   * Delete an enrollment set by eid. Wraps CourseManagementAdministration.removeEnrollmentSet.
   * @see CourseManagementAdministration#removeEnrollmentSet
   */
  public void delete(String eid) {
    cmAdmin.removeEnrollmentSet(eid);
  }

  // If the official instructors list changes, instructors might be added to existing sites.
  // TODO: Make this more efficient.
  private void updateSitesWithEnrollmentSet(String enrollmentSetEid) {
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
