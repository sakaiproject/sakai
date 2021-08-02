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

import org.sakaiproject.cmprovider.data.MembershipData;
import org.sakaiproject.cmprovider.data.SectionData;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.search.Search;

/**
 * Provides a REST API for working with sections.
 * @see Section
 *
 * @author Christopher Schauer
 */
public class SectionEntityProvider extends AbstractContainerEntityProvider {
  public String getEntityPrefix() {
    return "cm-section";
  }

  public Object getSampleEntity() {
    return new SectionData();
  }

  @Override
  public boolean entityExists(String id) {
    return cmService.isSectionDefined(id);
  }

  /**
   * TODO: look into implementing search for sections
   */
  public List getEntities(EntityReference ref, Search search) {
    validateUser();
    return null;
  }

  /**
   * Create a new section. Wraps CourseManagementAdministration.createSection.
   * @see CourseManagementAdministration#createSection
   * @SectionData
   */
  public void create(Object entity) {
    SectionData data = (SectionData) entity;
    cmAdmin.createSection(
      data.eid,
      data.title,
      data.description,
      data.category,
      data.parent,
      data.courseOffering, 
      data.enrollmentSet
    );
  }

  /**
   * Update a section. Wraps CourseManagementAdministration.updateSection.
   * @see CourseManagementAdministration#updateSection
   * @see SectionData
   */
  public void update(Object entity) {
    SectionData data = (SectionData) entity;
    Section updated = cmService.getSection(data.eid);
    updated.setTitle(data.title);
    updated.setDescription(data.description);
    updated.setCategory(data.category);
    updated.setEnrollmentSet(cmService.getEnrollmentSet(data.enrollmentSet));
    updated.setMaxSize(data.maxSize);

    Section parent = null;
    if (data.parent != null) parent = cmService.getSection(data.parent);
    updated.setParent(parent);
    
    cmAdmin.updateSection(updated);
  }

  /**
   * Get a section by eid. Wraps CourseManagementService.getSection.
   * @see CourseManagementService#getSection
   */
  public Object get(String eid) {
    Section section = cmService.getSection(eid);
    return new SectionData(section);
  }

  /**
   * Delete a section by eid. Wraps CourseManagementAdministration.removeSection.
   * @see CourseManagementAdministration#removeSection
   */
  public void delete(String eid) {
    cmAdmin.removeSection(eid);
  }

  public void addOrUpdateMembership(MembershipData data) {
    cmAdmin.addOrUpdateSectionMembership(
      data.userId,
      data.role,
      data.memberContainer,
      data.status
    );
  }

  public void removeMembership(String userId, String containerId) {
    cmAdmin.removeSectionMembership(userId, containerId);
  }
}
