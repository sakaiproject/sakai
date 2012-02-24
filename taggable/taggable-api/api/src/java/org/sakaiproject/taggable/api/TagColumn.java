/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/syracuse/taggable/branches/oncourse_osp_enhancements/taggable-api/api/src/java/org/sakaiproject/taggable/api/TagColumn.java $
 * $Id: TagColumn.java 10548 2007-07-06 19:51:40Z jmpease@syr.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Sakai Foundation
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
 * Represents a specific column in a {@link TagList}.
 * 
 * @author The Sakai Foundation.
 */
public interface TagColumn {

	/**
	 * Method to get the displayable name of this column.
	 * 
	 * @return The displayable column name.
	 */
	public String getDisplayName();

	/**
	 * Method to get the name that identifies this column.
	 * 
	 * @return The name that identifies this column.
	 */
	public String getName();

	/**
	 * Method to get a string describing this column.
	 * 
	 * @return The description of this column.
	 */
	public String getDescription();

	/**
	 * Method to determine if this column is sortable.
	 * 
	 * @return True if this column is sortable, false otherwise.
	 */
	public boolean isSortable();
}
