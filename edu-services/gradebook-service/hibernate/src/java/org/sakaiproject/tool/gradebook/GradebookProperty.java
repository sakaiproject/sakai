/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.tool.gradebook;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class GradebookProperty implements Serializable, Comparable<Object> {
	
	private static final long serialVersionUID = 1L;
	
	private Long id;
	private int version;

	private String name;
	private String value;

	public GradebookProperty() {
	}
	public GradebookProperty(String name) {
		setName(name);
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}

    @Override
	public int compareTo(Object o) {
        return getName().compareTo(((GradebookProperty)o).getName());
    }
    @Override
	public String toString() {
        return new ToStringBuilder(this).
            append(getName()).toString();
    }

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
}
