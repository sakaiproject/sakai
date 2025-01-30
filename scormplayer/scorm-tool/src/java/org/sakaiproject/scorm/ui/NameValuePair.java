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
package org.sakaiproject.scorm.ui;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

public class NameValuePair implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Getter @Setter private String value;
	@Getter @Setter private String name;

	public NameValuePair(String value, String id)
	{
		this.value = value;
		this.name = id;
	}

	@Override
	public boolean equals(Object object)
	{
		if (object instanceof NameValuePair)
		{
			NameValuePair nvp = (NameValuePair) object;
			return name.equals(nvp.getName());
		}

		return false;
	}

	@Override
	public int hashCode()
	{
		return (name != null ? name.hashCode() : super.hashCode());
	}

	@Override
	public String toString()
	{
		return name;
	}
}
