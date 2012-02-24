/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/syracuse/taggable/branches/oncourse_osp_enhancements/taggable-api/api/src/java/org/sakaiproject/taggable/api/TagList.java $
 * $Id: TagList.java 45892 2008-02-22 19:54:48Z chmaurer@iupui.edu $
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

import java.util.List;

/**
 * A specialized list that can provide column information in the form of a
 * {@link TagColumn} for each field that the {@link Tag} objects in the list can
 * provide.
 * 
 * @author The Sakai Foundation.
 * @see Tag
 */
public interface TagList extends List<Tag> {

	/** Identifiable column names */
	public static final String WORKSITE = "workSite", PARENT = "parent",
			CRITERIA = "tagCriteria", RUBRIC = "rubric", RATIONALE = "rationale",
			VISIBLE = "visible", EXPORTABLE = "exportable";
	
	public static final String THICKBOX_INCLUDE = "thickbox.include";
	
	/**
	 * Method to get a particular {@link TagColumn} object from the list of
	 * columns identified by the given name.
	 * 
	 * @param name
	 *            The name that identifies that column to retrieve.
	 * @return The {@link TagColumn} object.
	 * @see #getColumns()
	 */
	public TagColumn getColumn(String name);

	/**
	 * Method to get a list of {@link TagColumn} that the {@link Tag} objects in
	 * this list can provide data for.
	 * 
	 * @return A list of {@link TagColumn} that the {@link Tag} objects in this
	 *         list can provide data for.
	 * @see Tag#getField(String)
	 * @see Tag#getFields()
	 */
	public List<TagColumn> getColumns();

	/**
	 * Method to sort the {@link Tag} objects in this list by the fields
	 * represented by the given {@link TagColumn}.
	 * 
	 * @param column
	 *            The tag column to sort by. This may be null, indicating a
	 *            default sort column.
	 * @param ascending
	 *            True if the objects should be assorted in ascending order,
	 *            false otherwise.
	 * @see Tag#getField(String)
	 * @see Tag#getFields()
	 */
	public void sort(TagColumn column, boolean ascending);
}
