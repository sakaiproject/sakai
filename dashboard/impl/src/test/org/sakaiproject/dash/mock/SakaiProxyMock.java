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
import java.util.List;
import java.util.Observer;

import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.dash.logic.SakaiProxy;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.user.api.User;

/**
 * 
 *
 */
public class SakaiProxyMock implements SakaiProxy {

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
		// TODO Auto-generated method stub
		return null;
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

	public List<String> getUsersWithReadAccess(String entityReference,
			String accessPermission) {
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

}
