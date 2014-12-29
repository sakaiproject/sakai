package org.sakaiproject.api.app.messageforums.entity;

import org.sakaiproject.entitybroker.entityprovider.EntityProvider;

/**
 * Entity api for MessageForum's Topic Entities
 * 
 * This provider is used to get topic data in XML, JSON, or HTML format
 * 
 *
 */
public interface TopicEntityProvider extends EntityProvider {
  public final static String ENTITY_PREFIX = "topic";
}
