/**
 * Copyright (c) 2003-2011 The Apereo Foundation
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
