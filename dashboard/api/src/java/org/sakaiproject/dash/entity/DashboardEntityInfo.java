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
 * http://www.osedu.org/licenses/ECL-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **********************************************************************************/ 

package org.sakaiproject.dash.entity;

import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * THIS WILL BE MOVED TO KERNEL
 * An implementation of DashboardEntityInfo provides methods to allow the dashboard 
 * to obtain information about a class of Sakai entities and about specific instances 
 * of that class. 
 *
 */
public interface DashboardEntityInfo {
	
	public static final String VALUE_TITLE = "title";
	public static final String VALUE_DESCRIPTION = "description";
	public static final String VALUE_NEWS_TIME = "news-time";
	public static final String VALUE_CALENDAR_TIME = "calendar-time";
	public static final String VALUE_ENTITY_TYPE = "entity-type";
	public static final String VALUE_USER_NAME = "user-name";
	public static final String VALUE_ASSESSMENT_TYPE = "assessment-type";
	public static final String VALUE_GRADE_TYPE = "grade-type";
	public static final String VALUE_SUBMISSION_TYPE = "submission-type";
	public static final String VALUE_MAX_SCORE = "max-score";
	public static final String VALUE_ATTACHMENTS = "attachments";
	public static final String VALUE_MORE_INFO = "more-info";

	
	public static final String VALUE_ATTACHMENT_TITLE = "attachment-title";
	public static final String VALUE_ATTACHMENT_URL = "attachment-url";
	public static final String VALUE_ATTACHMENT_MIMETYPE = "attachment-mimetype";
	public static final String VALUE_ATTACHMENT_SIZE = "attachment-size";
	public static final String VALUE_ATTACHMENT_TARGET = "attachment-target";

	public static final String VALUE_INFO_LINK_TITLE = "info_link-title";
	public static final String VALUE_INFO_LINK_URL = "info_link-url";
	public static final String VALUE_INFO_LINK_MIMETYPE = "info_link-mimetype";
	public static final String VALUE_INFO_LINK_SIZE = "info_link-size";
	public static final String VALUE_INFO_LINK_TARGET = "info_link-target";	

	public static final String VALUES_ORDER = "order";
	
	public static final String LABEL_TITLE = "title-label";
	public static final String LABEL_DESCRIPTION = "description-label";
	public static final String LABEL_NEWS_TIME = "news-time-label";
	public static final String LABEL_CALENDAR_TIME = "calendar-time-label";
	public static final String LABEL_ENTITY_TYPE = "entity-type-label";
	public static final String LABEL_USER_NAME = "user-name-label";
	public static final String LABEL_ASSESSMENT_TYPE = "assessment-type-label";
	public static final String LABEL_GRADE_TYPE = "grade-type-label";
	public static final String LABEL_SUBMISSION_TYPE = "submission-type-label";
	public static final String LABEL_MAX_SCORE = "max-score-label";
	public static final String LABEL_ATTACHMENTS = "attachments-label";
	public static final String LABEL_ATTACHMENT = "attachment-label";
	public static final String LABEL_MORE_INFO = "more-info-label";
	public static final String LABEL_INFO_LINK = "info-link-label";
	
	public static final String[] VALUE_KEYS = new String[]{
		 VALUE_TITLE,
		 VALUE_DESCRIPTION,
		 VALUE_NEWS_TIME,
		 VALUE_CALENDAR_TIME,
		 VALUE_ENTITY_TYPE,
		 VALUE_USER_NAME,
		 VALUE_MORE_INFO,
		 VALUE_ASSESSMENT_TYPE,
		 VALUE_GRADE_TYPE,
		 VALUE_SUBMISSION_TYPE,
		 VALUE_MAX_SCORE,
		 VALUE_ATTACHMENTS
	};
	
	public static final String[] ATTACHMENT_KEYS = new String[]{
		 LABEL_ATTACHMENT,
		 VALUE_ATTACHMENT_TITLE,
		 VALUE_ATTACHMENT_URL,
		 VALUE_ATTACHMENT_MIMETYPE,
		 VALUE_ATTACHMENT_SIZE,
		 VALUE_ATTACHMENT_TARGET
	};
	
	public static final String[] MORE_INFO_KEYS = new String[]{
		 LABEL_INFO_LINK,
		 VALUE_INFO_LINK_TITLE,
		 VALUE_INFO_LINK_URL,
		 VALUE_INFO_LINK_MIMETYPE,
		 VALUE_INFO_LINK_SIZE,
		 VALUE_INFO_LINK_TARGET
	};
	
	public static final String[] LABEL_KEYS = new String[]{
		 LABEL_TITLE,
		 LABEL_DESCRIPTION,
		 LABEL_NEWS_TIME,
		 LABEL_CALENDAR_TIME,
		 LABEL_ENTITY_TYPE,
		 LABEL_USER_NAME,
		 LABEL_MORE_INFO,
		 LABEL_ASSESSMENT_TYPE,
		 LABEL_GRADE_TYPE,
		 LABEL_SUBMISSION_TYPE,
		 LABEL_MAX_SCORE,
		 LABEL_ATTACHMENTS
	};
	
	/**
	 * Retrieve the unique identifier for this entity type.
	 * @return
	 */
	public String getIdentifier();
	
	/**
	 * Retrieve a mapping of key-value pairs related to a specific entity, as 
	 * identified by the entityReference parameter, for a specific locale, as 
	 * identified by the locale parameter. The mapping may use any of the keys 
	 * in the VALUE_KEYS array. If the VALUE_ATTACHMENTS key is used, its value 
	 * should be a List containing one or more Map objects in which the keys 
	 * belong to the ATTACHMENT_KEYS array.
	 * @param entityReference
	 * @param localeCode
	 * @return
	 */
	public Map<String, Object> getValues(String entityReference, String localeCode);
	
	/**
	 * Retrieve a mapping of keys to strings to be included in the dashboard page. 
	 * The strings will be localized for the locale and may be specific to the entity 
	 * identified by the entityReference parameter. The mapping may use any of the keys
	 *  in the LABEL_KEYS array.
	 * @param entityReference
	 * @param localeCode
	 * @return the properties mapping, or null or an empty map if no properties should
	 * be included for this entity type and this specific entity.
	 */
	public Map<String,String> getProperties(String entityReference, String localeCode);
	
	/**
	 * Retrieve a list of lists. Each of the inner lists represents a section (or "line") 
	 * of the disclosure for the entity. The strings within each of those lists represents
	 * a value (along with any labels for that value). So the list of lists of strings 
	 * returned by this method should specify the order of the sections and the order of 
	 * the information within each section. This may be the same for all entities of a 
	 * particular type, or it may be specific to the entity and/or the user's locale.
	 * @param entityReference
	 * @param localeCode
	 * @return
	 */
	public List<List<String>> getOrder(String entityReference, String localeCode);
	
	/**
	 * Find out whether a particular entity is "available", meaning it is not hidden 
	 * or restricted due to some form of conditional release.
	 * @param entityReference
	 * @return true if the entity is fully available to users with basic access permission
	 * 	to the entity, or false if it is hidden or not yet available. 
	 */
	public boolean isAvailable(String entityReference);
	
	/**
	 * Get the event display string
	 * @param key
	 * @param dflt TODO
	 * @return
	 */
	public String getEventDisplayString(String key, String dflt);

	/**
	 * Retrieve a localized string from the properties bundle suitable for use as the title 
	 * of a grouped news item indicating that the grouped item represents some number of 
	 * individual items of this entity type were added within the context.  
	 * @param numberOfItems
	 * @param contextTitle
	 * @param labelKey TODO
	 */
	public String getGroupTitle(int numberOfItems, String contextTitle, String labelKey);

	/**
	 * Determine whether a user has permission to access a particular entity, using 
	 * whichever parameters is appropriate for determining this permission.
	 * @param sakaiUserId
	 * @param entityReference
	 * @param contextId
	 * @return
	 */
	public boolean isUserPermitted(String sakaiUserId, String entityReference,
			String contextId);
	
	/**
	 * Determine the correct icon to use for a particular entity in the dashboard list 
	 * based on its "subtype".
	 * @param subtype
	 * @return
	 */
	public String getIconUrl(String subtype);

	/**
	 * 
	 * @param entityReference
	 * @return
	 */
	public List<String> getUsersWithAccess(String entityReference);
}
