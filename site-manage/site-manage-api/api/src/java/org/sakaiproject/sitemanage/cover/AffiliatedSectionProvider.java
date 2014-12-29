/**********************************************************************************
 * $URL:  $
 * $Id:  $
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

/**
 * @author zqian
 *
 */
public class AffiliatedSectionProvider {
	
	public static final org.sakaiproject.sitemanage.api.AffiliatedSectionProvider getInstance() {
		return (org.sakaiproject.sitemanage.api.AffiliatedSectionProvider)ComponentManager.get(org.sakaiproject.sitemanage.api.AffiliatedSectionProvider.class);
	}
	
	public static final List<String> getAffiliatedSectionEids(String arg0, String arg1) {
		return getInstance().getAffiliatedSectionEids(arg0, arg1);
	}

}
