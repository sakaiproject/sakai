package org.sakaiproject.citation.impl.openurl;


/**
 * This is the main class for a OpenURL reference.
 * @author buckett
 *
 */
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class ContextObject {

	public final static String VERSION = "Z39.88-2004";
	
	public enum Entity {REFERENT, REFERRING_ENTITY, REQUESTOR, RESOLVER, SERVICE_TYPE, REFERRER}

	private String id;
	private Date timestamp;
	private Map<Entity, ContextObjectEntity> entities = new HashMap<ContextObject.Entity, ContextObjectEntity>();
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public Map<Entity, ContextObjectEntity> getEntities() {
		return entities;
	}
	public ContextObjectEntity getEntity(Entity entity) {
		return entities.get(entity);
	}
	public void setEntities(Map<Entity, ContextObjectEntity> entities) {
		this.entities = entities;
	}
}
