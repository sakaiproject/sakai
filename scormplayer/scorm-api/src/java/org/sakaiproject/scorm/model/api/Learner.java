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

import java.io.Serializable;
import java.util.Properties;

import lombok.Getter;
import lombok.Setter;

public class Learner implements Serializable, Comparable<Learner>
{
	private static final long serialVersionUID = 1L;

	@Setter @Getter private String id;
	@Setter @Getter private String displayName;
	@Setter @Getter private String displayId;
	@Setter @Getter private String sortName;
	@Setter @Getter private Properties properties;

	public Learner(String id)
	{
		this.id = id;
	}

	public Learner(String id, String displayName, String displayId)
	{
		this.id = id;
		this.displayName = displayName;
		this.displayId = displayId;
	}

	@Override
	public int compareTo(Learner learner)
	{
		return sortName.compareTo(learner.sortName);
	}
}
