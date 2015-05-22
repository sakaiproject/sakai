package org.sakaiproject.cmprovider;

import java.util.List;

import org.sakaiproject.cmprovider.api.MeetingEntityProvider;
import org.sakaiproject.coursemanagement.api.Meeting;
import org.sakaiproject.coursemanagement.impl.MeetingCmImpl;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.search.Search;

/**
 * Provides a REST API for working with meetings.
 * @see Meeting
 *
 * TODO: implement this entity provider
 *
 * @author Christopher Schauer
 */
public class MeetingEntityProviderImpl extends AbstractCmEntityProvider implements MeetingEntityProvider {
  public String getEntityPrefix() {
    return ENTITY_PREFIX;
  }

  public List getEntities(EntityReference ref, Search search) {
    validateUser();
    return null;
  }

  public void create(Object entity) {
  }

  public Object getSampleEntity() {
    return new MeetingCmImpl();
  }

  public void update(Object entity) {
  }

  public Object get(String eid) {
    return null;
  }

  public void delete(String eid) {
  }
}
