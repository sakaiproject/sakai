/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/syracuse/taggable/branches/oncourse_osp_enhancements/taggable-api/api/src/java/org/sakaiproject/taggable/api/Tag.java $
 * $Id: Tag.java 10548 2007-07-06 19:51:40Z jmpease@syr.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2005-2006, 2008 The Sakai Foundation
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

import java.util.List;

/**
 * A tag from an activity to some object.
 * 
 * @author The Sakai Foundation.
 */
public interface Tag {

	/**
	 * @return A reference for the activity from which the tag originated.
	 */
	public String getActivityRef();

	/**
	 * @return The object to which the tag was applied.
	 */
	public Object getObject();

	/**
	 * Method to get the displayable data for each of this tag's fields. This
	 * list should be in the same order as the columns returned by
	 * {@link TagList#getColumns() TagList.getColumns}.
	 * 
	 * @return A list of data corresponding to displayable fields for this tag.
	 */
	public List<String> getFields();

	/**
	 * Method to get a specific field of data for this tag based on a specific
	 * column from the list returned by {@link TagList#getColumns()}.
	 * 
	 * @param column
	 *            The {@link TagColumn} object representing the specific field
	 *            of data to retrieve from this tag.
	 * @return The data for the field associated with the given column.
	 */
	public String getField(TagColumn column);
}
