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

import java.util.Date;

//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;

/**
 * NewsItem encapsulates all information about dashboard items to 
 * appear in the "News" section of users' dashboards.
 *
 */
//@Data 
//@NoArgsConstructor
//@AllArgsConstructor
public class NewsItem {
	
	protected Long id;
	protected String title;
	protected Date newsTime;
	protected String entityReference;
	protected String entityUrl;
	protected Context context;
	protected Realm realm;
	protected SourceType sourceType;
	
	/**
	 * 
	 */
	public NewsItem() {
		super();
	}

	public NewsItem(String title, Date newsTime, String entityReference,
			String entityUrl, Context context, Realm realm,
			SourceType sourceType) {
		super();
		this.title = title;
		this.newsTime = newsTime;
		this.entityReference = entityReference;
		this.entityUrl = entityUrl;
		this.context = context;
		this.realm = realm;
		this.sourceType = sourceType;
	}

	/**
	 * @param id
	 * @param title
	 * @param newsTime
	 * @param entityReference
	 * @param entityUrl
	 * @param context
	 * @param realm
	 * @param sourceType
	 */
	public NewsItem(Long id, String title, Date newsTime,
			String entityReference, String entityUrl, Context context,
			Realm realm, SourceType sourceType) {
		super();
		this.id = id;
		this.title = title;
		this.newsTime = newsTime;
		this.entityReference = entityReference;
		this.entityUrl = entityUrl;
		this.context = context;
		this.realm = realm;
		this.sourceType = sourceType;
}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return the newsTime
	 */
	public Date getNewsTime() {
		return newsTime;
	}

	/**
	 * @return the entityReference
	 */
	public String getEntityReference() {
		return entityReference;
	}

	/**
	 * @return the entityUrl
	 */
	public String getEntityUrl() {
		return entityUrl;
	}

	/**
	 * @return the context
	 */
	public Context getContext() {
		return context;
	}

	/**
	 * @return the realm
	 */
	public Realm getRealm() {
		return realm;
	}

	/**
	 * @return the sourceType
	 */
	public SourceType getSourceType() {
		return sourceType;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @param newsTime the newsTime to set
	 */
	public void setNewsTime(Date newsTime) {
		this.newsTime = newsTime;
	}

	/**
	 * @param entityReference the entityReference to set
	 */
	public void setEntityReference(String entityReference) {
		this.entityReference = entityReference;
	}

	/**
	 * @param entityUrl the entityUrl to set
	 */
	public void setEntityUrl(String entityUrl) {
		this.entityUrl = entityUrl;
	}

	/**
	 * @param context the context to set
	 */
	public void setContext(Context context) {
		this.context = context;
	}

	/**
	 * @param realm the realm to set
	 */
	public void setRealm(Realm realm) {
		this.realm = realm;
	}

	/**
	 * @param sourceType the sourceType to set
	 */
	public void setSourceType(SourceType sourceType) {
		this.sourceType = sourceType;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("NewsItem [id=");
		builder.append(id);
		builder.append(", title=");
		builder.append(title);
		builder.append(", newsTime=");
		builder.append(newsTime);
		builder.append(", entityReference=");
		builder.append(entityReference);
		builder.append(", entityUrl=");
		builder.append(entityUrl);
		builder.append(", context=");
		builder.append(context);
		builder.append(", realm=");
		builder.append(realm);
		builder.append(", sourceType=");
		builder.append(sourceType);
		builder.append("]");
		return builder.toString();
	}

}
