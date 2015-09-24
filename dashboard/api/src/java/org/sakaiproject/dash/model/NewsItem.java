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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
public class NewsItem implements Serializable {
	
	protected Long id;
	protected String title;
	protected Date newsTime;
	protected String newsTimeLabelKey;
	protected String entityReference;
	//protected String entityUrl;
	protected Context context;
	protected SourceType sourceType;
	protected String subtype;
	protected String groupingIdentifier;
	protected String infoLinkURL;
	
	protected int itemCount = 0;
	
	protected static final String FORMAT_YEAR_DAY = "yyyyDDD";
	
	/**
	 * 
	 */
	public NewsItem() {
		super();
	}

	public NewsItem(String title, Date newsTime, String newsTimeLabelKey,
			String entityReference, Context context, SourceType sourceType, String subtype) {
		super();
		this.title = title;
		this.newsTime = newsTime;
		this.newsTimeLabelKey = newsTimeLabelKey;
		this.entityReference = entityReference;
		this.context = context;
		this.sourceType = sourceType;
		this.subtype = subtype;
		generateGroupingIdentifier();
	}

	/**
	 * @param id
	 * @param title
	 * @param newsTime
	 * @param newsTimeLabelKey TODO
	 * @param entityReference
	 * @param context
	 * @param sourceType
	 * @param subtype TODO
	 * @param entityUrl
	 */
	public NewsItem(Long id, String title, Date newsTime,
			String newsTimeLabelKey, String entityReference,
			Context context, SourceType sourceType, String subtype) {
		super();
		this.id = id;
		this.title = title;
		this.newsTime = newsTime;
		this.newsTimeLabelKey = newsTimeLabelKey;
		this.entityReference = entityReference;
		this.context = context;
		this.sourceType = sourceType;
		this.subtype = subtype;
		generateGroupingIdentifier();
	}
	
	public NewsItem(NewsItem other) {
		super();
		if(other.id != null) {
			this.id = new Long(other.id.longValue());
		}
		this.title = other.title;
		this.newsTime = other.newsTime;
		this.newsTimeLabelKey = other.newsTimeLabelKey;
		this.entityReference = other.entityReference;
		this.context = new Context(other.context);
		this.sourceType = new SourceType(other.sourceType);
		this.subtype = other.subtype;
		this.itemCount = other.itemCount;
		generateGroupingIdentifier();

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
	 * @return the groupingIdentifier
	 */
	public String getGroupingIdentifier() {
		return groupingIdentifier;
	}

	/**
	 * @return the itemCount
	 */
	public int getItemCount() {
		return itemCount;
	}

	/**
	 * @return the subtype
	 */
	public String getSubtype() {
		return subtype;
	}

	/**
	 * @return the context
	 */
	public Context getContext() {
		return context;
	}

	/**
	 * @return the newsTimeLabelKey
	 */
	public String getNewsTimeLabelKey() {
		return newsTimeLabelKey;
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
		generateGroupingIdentifier();
	}

	/**
	 * @param entityReference the entityReference to set
	 */
	public void setEntityReference(String entityReference) {
		this.entityReference = entityReference;
	}

	/**
	 * @param itemCount the itemCount to set
	 */
	public void setItemCount(int itemCount) {
		this.itemCount = itemCount;
	}

	/**
	 * @param subtype the subtype to set
	 */
	public void setSubtype(String subtype) {
		this.subtype = subtype;
	}

	/**
	 * @param context the context to set
	 */
	public void setContext(Context context) {
		this.context = context;
		generateGroupingIdentifier();
	}

	/**
	 * @param newsTimeLabelKey the newsTimeLabelKey to set
	 */
	public void setNewsTimeLabelKey(String newsTimeLabelKey) {
		this.newsTimeLabelKey = newsTimeLabelKey;
		generateGroupingIdentifier();
	}

	/**
	 * @param sourceType the sourceType to set
	 */
	public void setSourceType(SourceType sourceType) {
		this.sourceType = sourceType;
		generateGroupingIdentifier();
	}
	public String getInfoLinkURL() {
		return infoLinkURL;
	}
	
	public void setInfoLinkURL(String infoLinkURL) {
		this.infoLinkURL = infoLinkURL;
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
		builder.append(", newsTimeLabelKey=");
		builder.append(newsTimeLabelKey);
		builder.append(", entityReference=");
		builder.append(entityReference);
		builder.append(", context=");
		builder.append(context);
		builder.append(", sourceType=");
		builder.append(sourceType);
		builder.append(", subtype=");
		builder.append(subtype);
		builder.append(", groupingIdentifier=");
		builder.append(groupingIdentifier);
		builder.append("]");
		return builder.toString();
	}

	protected void generateGroupingIdentifier() {
		StringBuilder buf = new StringBuilder();
		DateFormat df = new SimpleDateFormat(FORMAT_YEAR_DAY);
		if(newsTime == null) {
			buf.append(df.format(new Date()));
		} else {
			buf.append(df.format(newsTime));
		}
		buf.append("-");
		if(context == null) {
			buf.append("unknown.site");		
		} else {
			buf.append(context.getContextId());
		}
		buf.append("-");
		if(sourceType == null) {
			buf.append("unknown.type");		
		} else {
			buf.append(sourceType.getIdentifier());
		}
		buf.append("-");
		if(this.newsTimeLabelKey == null) {
			buf.append("unknown.key");		
		} else {
			buf.append(this.newsTimeLabelKey);
		}
		this.groupingIdentifier = buf.toString();
	}


}
