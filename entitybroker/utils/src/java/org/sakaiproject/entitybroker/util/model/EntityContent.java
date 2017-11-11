/**
 * Copyright (c) 2007-2017 The Apereo Foundation
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
package org.sakaiproject.entitybroker.util.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.sakaiproject.time.api.Time;

public class EntityContent {

	private String name;
	private String resourceId;
	private String reference;
	private String type;
	private String mimeType;
	private String description;
	private String creator;
	private String modifiedBy;
	private String size;
	private String url;
	private String priority;

	private Time created;
	private Time modified;
	private Time release;
	private Time retract;

	private boolean hidden;

	private Collection<EntityContent> resourceChildren = new ArrayList<EntityContent>();

	private Map<String, Object> properties = new HashMap<String, Object>();

	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setResourceId(String id) {
		this.resourceId = id;
	}
	public String getResourceId() {
		return resourceId;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getMimeType() {
		return mimeType;
	}
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	public String getCreator() {
		return creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
	}
	public String getModifiedBy() {
		return modifiedBy;
	}
	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}
	public Long getCreated() {
		return created.getTime();
	}
	public void setCreated(Time created) {
		this.created = created;
	}
	public Long getModified() {
		return modified.getTime();
	}
	public void setModified(Time modified) {
		this.modified = modified;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getReference() {
		return reference;
	}
	public void setReference(String reference) {
		this.reference = reference;
	}
	public String getPriority() {
		return priority;
	}
	public void setPriority(String priority) {
		this.priority = priority;
	}
	public Long getRelease() {
		return release.getTime();
	}
	public void setRelease(Time release) {
		this.release = release;
	}
	public Long getRetract() {
		return retract.getTime();
	}
	public void setRetract(Time retract) {
		this.retract = retract;
	}
	public boolean getHidden() {
		return hidden;
	}
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
	public void addResourceChild(EntityContent child) {
		this.resourceChildren.add(child);
	}
	public Collection<EntityContent> getResourceChildren() {
		return resourceChildren;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}
	public void setProperty(String key, Object value) {
		properties.put(key, value);
	}

	public boolean after(Time timeStamp) {
		if (modified.after(timeStamp)) {
			return true;
		}
		return false;
	}
}
