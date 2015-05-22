package org.sakaiproject.cmprovider.api;

import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.coursemanagement.api.exception.IdNotFoundException;

/**
 * Interface for course management entity providers.
 *
 * @author Christopher Schauer
 */
public interface CmEntityProvider extends RESTful {
  public Object get(String eid) throws IdNotFoundException;
  public void create(Object entity);
  public void update(Object entity);
  public void delete(String eid);
}
