/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * 
 */
package org.sakaiproject.sitestats.tool.wicket.models;

import org.apache.wicket.model.IModel;
import org.sakaiproject.util.Web;

public class CHResourceModel implements IModel {
	private static final long	serialVersionUID	= 1L;
	
	String resourceId = null;
	String resourceName = null;
	boolean isCollection = false;
	
	public CHResourceModel(String resourceId, String resourceName) {
		this.resourceId = resourceId;
		this.resourceName = resourceName;
	}
	
	public CHResourceModel(String resourceId, String resourceName, boolean isCollection) {
		this.resourceId = resourceId;
		this.resourceName = resourceName;
		this.isCollection = isCollection;
	}

	public Object getObject() {
		return resourceId;
	}

	public void setObject(Object object) {
		this.resourceId = (String) object;
	}
	
	public String getResourceId() {
		return resourceId;
	}
	
	public String getResourceName() {
		return resourceName;
	}
	
	public String getResourceNameEscaped() {
		return Web.escapeHtml(getResourceName());
	}
	
	public boolean isCollection() {
		return isCollection;
	}
	
	public String getResourceExtension() {
		String[] parts = getResourceId().split("\\.");
		if(parts.length > 1) {
			return parts[parts.length - 1].toLowerCase();
		}else{
			return "";
		}
	}

	public void detach() {
		resourceId = null;
		resourceName = null;
	}
	
}