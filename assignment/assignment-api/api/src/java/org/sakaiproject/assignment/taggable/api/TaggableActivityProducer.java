/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.assignment.taggable.api;

import java.util.List;

/**
 * A service that produces activities that can be tagged.
 * 
 * @author The Sakai Foundation.
 */
public interface TaggableActivityProducer {

	/**
	 * Method to check if this producer handles the given reference.
	 * 
	 * @param ref
	 *            A reference for an object produced by a taggable activity
	 *            producer.
	 * @return True if this producer handles the reference, false otherwise.
	 */
	public boolean checkReference(String ref);

	/**
	 * Method to get the context of the object represented by this reference.
	 * 
	 * @param ref
	 *            A reference for an object produced by a taggable activity
	 *            producer.
	 * @return The context of the referenced object.
	 */
	public String getContext(String ref);

	/**
	 * Method to get a displayable name for the producing service.
	 * 
	 * @return A common displayable name for this service.
	 */
	public String getName();

	/**
	 * Method to get a type name for the producing service.
	 * 
	 * @return A type name for this service.
	 */
	public String getType();

	/**
	 * Method to get a list of all taggable activities within the given context.
	 * 
	 * @param context
	 *            The context to search.
	 * @return A list of all taggable activities within the given context.
	 */
	public List<TaggableActivity> getActivities(String context);

	/**
	 * Method to get a taggable activity by reference string.
	 * 
	 * @param activityRef
	 *            The reference for the taggable activity.
	 * @return The activity referenced by the given string.
	 */
	public TaggableActivity getActivity(String activityRef);

	/**
	 * Method to get a taggable item by reference string.
	 * 
	 * @param itemRef
	 *            The reference for the taggable item.
	 * @return The item referenced by the given string.
	 */
	public TaggableItem getItem(String itemRef);
}
