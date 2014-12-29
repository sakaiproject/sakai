/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/syracuse/taggable/branches/oncourse_osp_enhancements/taggable-impl/impl/src/java/org/sakaiproject/taggable/impl/TagListImpl.java $
 * $Id: TagListImpl.java 45892 2008-02-22 19:54:48Z chmaurer@iupui.edu $
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

package org.sakaiproject.taggable.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.sakaiproject.taggable.api.TagColumn;
import org.sakaiproject.taggable.api.TagList;
import org.sakaiproject.taggable.api.Tag;
import org.sakaiproject.util.ResourceLoader;

public class TagListImpl extends ArrayList<Tag> implements TagList {

	private static ResourceLoader messages = new ResourceLoader("taggable-impl");

	/** Displayable column names */
	public static final String WORKSITE_NAME = messages
			.getString("column_worksite"), PARENT_NAME = messages
			.getString("column_parent"), CRITERIA_NAME = messages
			.getString("column_criteria"), RUBRIC_NAME = messages
			.getString("column_rubric"), RATIONALE_NAME = messages
			.getString("column_rationale"), VISIBLE_NAME = messages
			.getString("column_visible"), EXPORTABLE_NAME = messages
			.getString("column_exportable");

	/** Column descriptions */
	public static final String WORKSITE_DESC = messages
			.getString("column_worksite_desc"), PARENT_DESC = messages
			.getString("column_parent_desc"), CRITERIA_DESC = messages
			.getString("column_criteria_desc"), RUBRIC_DESC = messages
			.getString("column_rubric_desc"), RATIONALE_DESC = messages
			.getString("column_rationale_desc"), VISIBLE_DESC = messages
			.getString("column_visible_desc"), EXPORTABLE_DESC = messages
			.getString("column_exportable_desc");

	public static final String NA = messages.getString("na");

	protected static List<TagColumn> columns;

	static {
		columns = new ArrayList<TagColumn>();
		columns.add(new TagColumnImpl(CRITERIA, CRITERIA_NAME, CRITERIA_DESC, true));
		columns.add(new TagColumnImpl(PARENT, PARENT_NAME, PARENT_DESC,
				true));
		columns.add(new TagColumnImpl(WORKSITE, WORKSITE_NAME,
				WORKSITE_DESC, true));
		
		/*
		columns.add(new TagColumnImpl(RUBRIC, RUBRIC_NAME, RUBRIC_DESC,
				false));
		columns.add(new TagColumnImpl(RATIONALE, RATIONALE_NAME,
				RATIONALE_DESC, false));
		columns.add(new TagColumnImpl(VISIBLE, VISIBLE_NAME, VISIBLE_DESC,
				true));
		columns.add(new TagColumnImpl(EXPORTABLE, EXPORTABLE_NAME,
				EXPORTABLE_DESC, true));
		*/
	}
	
	public TagListImpl() {
		columns = new ArrayList<TagColumn>();
		columns.add(new TagColumnImpl(CRITERIA, CRITERIA_NAME, CRITERIA_DESC, true));
		columns.add(new TagColumnImpl(PARENT, PARENT_NAME, PARENT_DESC,
				true));
		columns.add(new TagColumnImpl(WORKSITE, WORKSITE_NAME,
				WORKSITE_DESC, true));
	}
	
	public TagListImpl(List<TagColumn> theColumns) {
		columns = theColumns;
	}

	public List<TagColumn> getColumns() {
		return columns;
	}

	public TagColumn getColumn(String name) {
		TagColumn column = null;
		for (TagColumn c : columns) {
			if (c.getName().equals(name)) {
				column = c;
			}
		}
		return column;
	}

	public void sort(final TagColumn column, final boolean ascending) {
		Collections.sort(this, new Comparator<Tag>() {
			public int compare(Tag o1, Tag o2) {
				Tag t1 = null;
				Tag t2 = null;
				if (ascending) {
					t1 = o1;
					t2 = o2;
				} else {
					t2 = o1;
					t1 = o2;
				}
				int value = 0;
				if (column != null) {
					if (column.isSortable()) {
						value = t1.getField(column).compareToIgnoreCase(
								t2.getField(column));
					}
				} else {
					value = t1.getField(getColumn(WORKSITE))
							.compareToIgnoreCase(
									t2.getField(getColumn(WORKSITE)));
				}
				return value;
			}
		});
	}
}
