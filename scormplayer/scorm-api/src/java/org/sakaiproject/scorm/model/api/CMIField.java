/**
 * Copyright (c) 2007 The Apereo Foundation
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
package org.sakaiproject.scorm.model.api;

import java.util.LinkedList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class CMIField
{
	@Getter private List<String> fieldValues;
	@Setter @Getter private Long id;
	@Setter @Getter private String fieldName;
	@Setter @Getter private String description;
	@Setter @Getter private List<CMIField> children;

	public CMIField()
	{
		this(null, null);
	}

	public CMIField(String fieldName, String description)
	{
		this.fieldName = fieldName;
		this.description = description;
		this.children = new LinkedList<>();
		this.fieldValues = new LinkedList<>();
	}

	public void addChild(CMIField child)
	{
		children.add(child);
	}

	public void addFieldValue(String fieldValue)
	{
		this.fieldValues.add(fieldValue);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}

		if (obj == null)
		{
			return false;
		}

		if (getClass() != obj.getClass())
		{
			return false;
		}

		CMIField other = (CMIField) obj;
		if (id == null)
		{
			if (other.id != null)
			{
				return false;
			}
		}
		else if (!id.equals(other.id))
		{
			return false;
		}

		return true;
	}

	public String getFieldValue()
	{
		if (fieldValues == null || fieldValues.isEmpty())
		{
			return null;
		}

		return fieldValues.get(0);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	public boolean isParent()
	{
		return children != null && children.size() > 0;
	}
}
