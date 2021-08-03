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

import org.sakaiproject.cmprovider.data.CourseSetData;
import org.sakaiproject.cmprovider.data.MembershipData;
import org.sakaiproject.coursemanagement.api.CourseSet;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.search.Search;

/**
 * Provides a REST API for working with course sets.
 * @see CourseSet
 *
 * TODO: implement this entity provider
 *
 * @author Christopher Schauer
 */
public class CourseSetEntityProvider extends AbstractContainerEntityProvider {
  public String getEntityPrefix() {
    return "cm-course-set";
  }

  public List getEntities(EntityReference ref, Search search) {
    validateUser();
    return null;
  }

  public Object getSampleEntity() {
    return new CourseSetData();
  }

  @Override
  public boolean entityExists(String id) {
    return cmService.isCourseSetDefined(id);
  }

  public void create(Object entity) {
    CourseSetData data = (CourseSetData)entity;
    cmAdmin.createCourseSet(
      data.eid,
      data.title,
      data.description,
      data.category,
      data.parent
    );
  }

  public void update(Object entity) {
    CourseSetData data = (CourseSetData)entity;
    CourseSet updated = cmService.getCourseSet(data.eid);
    updated.setTitle(data.title);
    updated.setDescription(data.description);
    updated.setCategory(data.category);

    CourseSet parent = null;
    if (data.parent != null) parent = cmService.getCourseSet(data.parent);
    updated.setParent(parent);

    cmAdmin.updateCourseSet(updated);
  }

  public Object get(String eid) {
    CourseSet set = cmService.getCourseSet(eid);
    return new CourseSetData(set);
  }

  public void delete(String eid) {
    cmAdmin.removeCourseSet(eid);
  }
  
  public void addOrUpdateMembership(MembershipData data) {
    cmAdmin.addOrUpdateCourseSetMembership(
      data.userId,
      data.role,
      data.memberContainer,
      data.status
    );
  }

  public void removeMembership(String userId, String containerId) {
    cmAdmin.removeCourseSetMembership(userId, containerId);
  }
}
