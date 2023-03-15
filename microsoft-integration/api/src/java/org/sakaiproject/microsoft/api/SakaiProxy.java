/**
* Copyright (c) 2023 Apereo Foundation
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
package org.sakaiproject.microsoft.api;

import java.util.List;

import org.sakaiproject.microsoft.api.data.SakaiMembersCollection;
import org.sakaiproject.microsoft.api.data.SakaiSiteFilter;
import org.sakaiproject.microsoft.api.data.SakaiUserIdentifier;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.user.api.User;

public interface SakaiProxy {
	
	// ------------------------------------------ SECURITY ----------------------------------------------------
	boolean isAdmin();
	boolean canUpdateSite(String siteReference, String userId);

	// ------------------------------------------ USERS ----------------------------------------------------
	User getUser(String userId);
	String getMemberKeyValue(User user, SakaiUserIdentifier key);
	void setMemberKeyValue(String userId, SakaiUserIdentifier key, String value);
	
	// ------------------------------------------ SITES ----------------------------------------------------
	List<Site> getSakaiSites();
	List<Site> getSakaiSites(SakaiSiteFilter filter);
	Site getSite(String siteId);

	SakaiMembersCollection getSiteMembers(String siteId, SakaiUserIdentifier key);
	
	// ------------------------------------------ GROUPS ----------------------------------------------------
	SakaiMembersCollection getGroupMembers(Group group, SakaiUserIdentifier key);
}