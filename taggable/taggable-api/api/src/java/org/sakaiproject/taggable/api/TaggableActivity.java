/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/syracuse/taggable/branches/oncourse_osp_enhancements/taggable-api/api/src/java/org/sakaiproject/taggable/api/TaggableActivity.java $
 * $Id: TaggableActivity.java 46822 2008-03-17 16:19:47Z chmaurer@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.taggable.api;

/**
 * An activity that can be tagged.
 * 
 * @author The Sakai Foundation.
 */
public interface TaggableActivity {

	/**
	 * Method to get the base object that is wrapped as an activity.
	 * 
	 * @return The base object.
	 */
	public Object getObject();

	/**
	 * @return A reference for this activity. This needs to return a valid
	 *         reference to an activity.
	 */
	public String getReference();

	/**
	 * @return A title for this activity.
	 */
	public String getTitle();

	/**
	 * @return A description for this activity.
	 */
	public String getDescription();

	/**
	 * @return The context of this activity.
	 */
	public String getContext();

	/**
	 * @return This activity's producer.
	 */
	public TaggableActivityProducer getProducer();
	
	/**
	 * Get the url that will render detail information about the activity
	 * @return
	 */
	public String getActivityDetailUrl();
	
	/**
	 * Get the name for the type of activity
	 * @return
	 */
	public String getTypeName();
	
	/**
	 * Determine if decorating the activityDetailUrl is necessary
	 * @return
	 */
	public boolean getUseDecoration();
	
	/**
	 * Get the params that will be added to the activity's url
	 * @return
	 */
	public String getActivityDetailUrlParams();
}
