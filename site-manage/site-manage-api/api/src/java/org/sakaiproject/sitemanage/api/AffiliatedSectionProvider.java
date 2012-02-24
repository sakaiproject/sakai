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
package org.sakaiproject.sitemanage.api;

import java.util.List;


/**
 * The site management applications allow installations to configure an AffiliatedSectionProvider to return a list of section ids that provided user
 * is allowed to create course site with. The real world example for this is that certain staff member in department are authorized to create course 
 * sites for professors, even those staff members are not really "teaching" those sections
 * @author zqian
 *
 */
public interface AffiliatedSectionProvider {
	
	/**
	 * Based on given instructor and academic session information, return a list of affiliated section eids of which the user can create course site
	 * @param userId
	 * @param academicSessionEid
	 * @return
	 */
	public List getAffiliatedSectionEids(String userId, String academicSessionEid);

}
