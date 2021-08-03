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

import org.sakaiproject.cmprovider.data.SectionCategoryData;
import org.sakaiproject.coursemanagement.api.SectionCategory;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.search.Search;

/**
 * Provides a REST API for working with section categories.
 * @see SectionCategory
 * 
 * @author Christopher Schauer
 */
public class SectionCategoryEntityProvider extends AbstractCmEntityProvider {
  public String getEntityPrefix() {
    return "cm-section-category";
  }

  public Object getSampleEntity() {
    return new SectionCategoryData();
  }

  @Override
  public boolean entityExists(String id) {
    return cmService.getSectionCategoryDescription(id) != null;
  }

  /**
   * Get all section categories. Wraps CourseManagementService.getSectionCategories.
   * @see CourseManagementService#getSectionCategories
   *
   * GET requests to /direct/cm-section-category.json will be routed here.
   *
   * Possible responses:
   * 200 = success, list of section categories in response body
   * 403 = current user does not have permission to access section categories
   */
  public List getEntities(EntityReference ref, Search search) {
    validateUser();
    return cmService.getSectionCategories();
  }

  /**
   * Create a new section category. Wraps CourseManagementAdministration.addSectionCategory.
   * @see CourseManagementAdministration#addSectionCategory
   * @see SectionCategoryData
   */
  public void create(Object entity) {
    SectionCategoryData data = (SectionCategoryData) entity;
    cmAdmin.addSectionCategory(data.code, data.description);
  }

  /**
   * There's no method in cm-service to update the description of a section category.
   *
   * TODO: add method in cm-service?
   */
  public void update(Object entity) {
  }

  /**
   * Get a section category by code. Wraps CourseManagementService.getSectionCategoryDescription.
   * @see CourseManagementService#getSectionCategoryDescription
   */
  public Object get(String eid) {
    String description = cmService.getSectionCategoryDescription(eid);
    if (description == null) return null;
    
    SectionCategoryData data = new SectionCategoryData();
    data.code = eid;
    data.description = description;
    return data;
  }

  /**
   * There's no method in cm-service to delete a section category.
   *
   * TODO: add method in cm-service?
   */
  public void delete(String eid) {
    validateUser();
  }
}
