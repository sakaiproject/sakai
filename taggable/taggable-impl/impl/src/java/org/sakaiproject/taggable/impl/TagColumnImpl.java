/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/syracuse/taggable/branches/oncourse_osp_enhancements/taggable-impl/impl/src/java/org/sakaiproject/taggable/impl/TagColumnImpl.java $
 * $Id: TagColumnImpl.java 45892 2008-02-22 19:54:48Z chmaurer@iupui.edu $
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

package org.sakaiproject.taggable.impl;

import org.sakaiproject.taggable.api.TagColumn;

public class TagColumnImpl implements TagColumn {

	String name, displayName, description;

	boolean sortable;

	public TagColumnImpl(String name, String displayName,
			String description, boolean sortable) {
		this.name = name;
		this.displayName = displayName;
		this.description = description;
		this.sortable = sortable;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getDisplayName() {
		return displayName;
	}

	public boolean isSortable() {
		return sortable;
	}
}
