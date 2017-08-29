/**
 * Copyright (c) 2003-2009 The Apereo Foundation
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
package org.sakaiproject.conditions.impl;

import java.util.HashSet;
import java.util.Set;

import org.sakaiproject.conditions.api.ConditionTemplate;
import org.sakaiproject.conditions.api.ConditionTemplateSet;

public class MyConditionTemplateSet implements ConditionTemplateSet {
	private Set<ConditionTemplate> myConditionTemplates = new HashSet<ConditionTemplate>();
	
	public MyConditionTemplateSet() {
		ConditionTemplate aConditionTemplate = new MyConditionTemplate();
		myConditionTemplates.add(aConditionTemplate);
	}

	public Set<ConditionTemplate> getConditionTemplates() {
		return myConditionTemplates;
	}

	public String getDisplayName() {
		return "Gradebook";
	}

	public String getId() {
		return "sakai.service.gradebook";
	}

}
