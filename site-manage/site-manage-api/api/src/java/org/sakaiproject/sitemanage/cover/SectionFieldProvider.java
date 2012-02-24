/**********************************************************************************
 * $URL:  $
 * $Id: SectionFieldManager.java 22875 2007-03-19 02:31:42Z daisyf@stanford.edu $
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
package org.sakaiproject.sitemanage.cover;

import java.util.List;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.sitemanage.api.SectionField;

public class SectionFieldProvider {

	public static final org.sakaiproject.sitemanage.api.SectionFieldProvider getInstance() {
		return (org.sakaiproject.sitemanage.api.SectionFieldProvider)ComponentManager.get(org.sakaiproject.sitemanage.api.SectionFieldProvider.class);
	}
	
	public static final List<SectionField> getRequiredFields() {
		return getInstance().getRequiredFields();
	}

	public static final String getSectionEid(String academicSessionEid, List<SectionField>fields) {
			return getInstance().getSectionEid(academicSessionEid, fields);
	}

}
