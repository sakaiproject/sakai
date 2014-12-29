/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2011 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.dash.model;

import java.io.Serializable;


/**
 * Context encapsulates all information about sakai sites needed for 
 * dashboard items.
 *
 */
public class Context implements Serializable {
	
	protected Long id;
	protected String contextId;
	protected String contextTitle;
	protected String contextUrl;

	/**
	 * 
	 */
	public Context() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Context(String contextId, String contextTitle, String contextUrl) {
		this.contextId = contextId;
		this.contextTitle = contextTitle;
		this.contextUrl = contextUrl;
	}

	/**
	 * @param id
	 * @param contextId
	 * @param contextTitle
	 * @param contextUrl
	 */
	public Context(Long id, String contextId, String contextTitle,
			String contextUrl) {
		super();
		this.id = id;
		this.contextId = contextId;
		this.contextTitle = contextTitle;
		this.contextUrl = contextUrl;
}

	public Context(Context other) {
		super();
		if(other.id != null) {
			this.id = new Long(other.id.longValue());
		}
		this.contextId = other.contextId;
		this.contextTitle = other.contextTitle;
		this.contextUrl = other.contextUrl;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @return the contextId
	 */
	public String getContextId() {
		return contextId;
	}

	/**
	 * @return the contextTitle
	 */
	public String getContextTitle() {
		return contextTitle;
	}

	/**
	 * @return the contextUrl
	 */
	public String getContextUrl() {
		return contextUrl;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @param contextId the contextId to set
	 */
	public void setContextId(String contextId) {
		this.contextId = contextId;
	}

	/**
	 * @param contextTitle the contextTitle to set
	 */
	public void setContextTitle(String contextTitle) {
		this.contextTitle = contextTitle;
	}

	/**
	 * @param contextUrl the contextUrl to set
	 */
	public void setContextUrl(String contextUrl) {
		this.contextUrl = contextUrl;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Context [id=");
		builder.append(id);
		builder.append(", contextId=");
		builder.append(contextId);
		builder.append(", contextTitle=");
		builder.append(contextTitle);
		builder.append(", contextUrl=");
		builder.append(contextUrl);
		builder.append("]");
		return builder.toString();
	}

}
