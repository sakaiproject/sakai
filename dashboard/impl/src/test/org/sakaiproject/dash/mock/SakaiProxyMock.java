/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2011 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.dash.mock;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observer;

import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.dash.app.SakaiProxy;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.user.api.User;

/**
 * 
 *
 */
public class SakaiProxyMock implements SakaiProxy {

	public static final String VALID_SITE_ID = "valid_site_id";
	public static final String VALID_SITE_TITLE = "valid_site_title";
	public static final String VALID_SITE_URL = "valid_site_url";
	public static final String BOGUS_SITE_ID = "bogus_site_id";
	public static final String BOGUS_SITE_TITLE = "bogus_site_title";
	public static final String BOGUS_SITE_URL = "bogus_site_url";

	public final Map<String,Site> validSites = new HashMap<String,Site>();

	public SakaiProxyMock(Site site) {
		validSites.put(site.getId(), site);

	}

	private String serverId = "unknown-server-id";

	public void addLocalEventListener(Observer observer) {
		// TODO Auto-generated method stub
		
	}

	public boolean getConfigParam(String param, boolean dflt) {
		// TODO Auto-generated method stub
		return false;
	}

	public String getConfigParam(String param, String dflt) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getCurrentSiteId() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getCurrentUserDisplayName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getCurrentUserId() {
		// TODO Auto-generated method stub
		return null;
	}

	public Entity getEntity(String entityReference) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<String> getRealmId(String entityReference,
			String contextId) {
		// TODO Auto-generated method stub
		return null;
	}

	public Site getSite(String siteId) {
		Site site = null;
		if(siteId != null) {
			site = validSites.get(siteId);
		}
		return site;
	}
	
	public boolean isSitePublished(String siteId) {
		Site site = getSite(siteId);
		return site != null? site.isPublished(): false;
	}

	public String getSkinRepoProperty() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getTargetForMimetype(String mimetype) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getToolSkinCSS(String skinRepo) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isSuperUser() {
		// TODO Auto-generated method stub
		return false;
	}

	public void postEvent(String event, String reference, boolean modify) {
		// TODO Auto-generated method stub
		
	}

	public void pushSecurityAdvisor(SecurityAdvisor securityAdvisor) {
		// TODO Auto-generated method stub
		
	}

	public void popSecurityAdvisor(SecurityAdvisor securityAdvisor) {
		// TODO Auto-generated method stub
		
	}

	public User getUser(String sakaiId) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isWorksite(String siteId) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isUserPermitted(String sakaiUserId, String accessPermission,
			String entityReference) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public boolean isAttachmentResource(String resourceId) {
		// TODO Auto-generated method stub
		return false;
	}

	public void startAdminSession() {
		// TODO Auto-generated method stub
		
	}

	public List<ContentResource> getAllContentResources(
			String contentCollectionId) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String getScheduleEventUrl(String eventRef) {
		// TODO Auto-generated method stub
		return null;
	
	}
	
	public String getSiteReference(String siteId)	{
		// TODO Auto-generated method stub
		return null;
	}
	
	public List<User> unlockUsers(String lock, String reference)
	{
		// TODO Auto-generated mathod stub
		return null;
	}

	public String getContentTypeImageUrl(String contenttype) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public boolean isEventProcessingThreadDisabled() {
		// TODO Auto-generated method stub
		return false;
	}

	public String getCurrentSessionId() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isDropboxResource(String entityReference) {
		return false;
	}

	public void clearThreadLocalCache() {
		// TODO Auto-generated method stub
		
	}
	
	public String getServerUrl() {
		return null;
	}

	public Collection<String> getAuthorizedUsers(String permission,
			String entityReference) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 
	 */
	public String getServerId() {
		
		return this.serverId;
	}

	public void registerFunction(String functionName) {
		// TODO Auto-generated method stub
		
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}
	
	public boolean isOfDashboardRelatedPermissions(String function)
	{
		return false;
	}

}
