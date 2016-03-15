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
