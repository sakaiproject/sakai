/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007 Sakai Foundation
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
package org.sakaiproject.mock.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Map.Entry;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteAdvisor;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SiteService implements org.sakaiproject.site.api.SiteService {
	Map<String, org.sakaiproject.mock.domain.Site> sites;
	
	public SiteService() {
		sites = new HashMap<String, org.sakaiproject.mock.domain.Site>();
	}
	
	public org.sakaiproject.mock.domain.Site addSite(String id, String type) {
		org.sakaiproject.mock.domain.Site site = new org.sakaiproject.mock.domain.Site(id, type);
		sites.put(id, site);
		return site;
	}

	public org.sakaiproject.mock.domain.Site addSite(String id, Site other) throws IdInvalidException,
			IdUsedException, PermissionException {
		return addSite(id, other.getType());
	}

	public void addSiteAdvisor(SiteAdvisor advisor) {
	}

	public boolean allowAccessSite(String id) {
		return true;
	}

	public boolean allowAddSite(String id) {
		return true;
	}

	public boolean allowRemoveSite(String id) {
		return true;
	}

	public boolean allowUnjoinSite(String id) {
		return true;
	}

	public boolean allowUpdateGroupMembership(String id) {
		return true;
	}

	public boolean allowUpdateSite(String id) {
		return true;
	}

	public boolean allowUpdateSiteMembership(String id) {
		return true;
	}

	public boolean allowViewRoster(String id) {
		return true;
	}
	
	public boolean allowRoleSwap(String id) {
		return true;
	}

	public int countSites(SelectionType type, Object ofType, String criteria,
			Map propertyCriteria) {
		return 0;
	}

	public Group findGroup(String refOrId) {
		for(Iterator<Entry<String, org.sakaiproject.mock.domain.Site>> iter = sites.entrySet().iterator(); iter.hasNext();) {
			Site site = iter.next().getValue();
			for(Iterator<Group> groupIter = site.getGroups().iterator(); groupIter.hasNext();) {
				Group group = groupIter.next();
				if(group.getId().equals(refOrId) || group.getReference().equals(refOrId)) {
					return group;
				}
			}
		}
		return null;
	}

	public SitePage findPage(String id) {
		for(Iterator<Entry<String, org.sakaiproject.mock.domain.Site>> iter = sites.entrySet().iterator(); iter.hasNext();) {
			Site site = iter.next().getValue();
			for(Iterator<SitePage> pageIter = site.getPages().iterator(); pageIter.hasNext();) {
				SitePage page = pageIter.next();
				if(page.getId().equals(id)) {
					return page;
				}
			}
		}
		return null;
	}

	public ToolConfiguration findTool(String id) {
		return null;
	}

	public String[] getLayoutNames() {
		return null;
	}

	public Site getSite(String id) throws IdUnusedException {
		return sites.get(id);
	}

	public List<SiteAdvisor> getSiteAdvisors() {
		return null;
	}

	public String getSiteDisplay(String id) {
		Site site = sites.get(id);
		if(site == null) return null;
		return site.getTitle();
	}

	public String getSiteSkin(String id) {
		Site site = sites.get(id);
		if(site == null) return null;
		return site.getSkin();
	}

	public String getSiteSpecialId(String site) {
		return null;
	}

	public List getSiteTypes() {
		return null;
	}

	public String getSiteUserId(String site) {
		return null;
	}

	public Site getSiteVisit(String id) throws IdUnusedException,
			PermissionException {
		// TODO Auto-generated method stub
		return null;
	}

	public List getSites(SelectionType type, Object ofType, String criteria,
			Map propertyCriteria, SortType sort, PagingPosition page) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getSpecialSiteId(String special) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getUserSiteId(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isSpecialSite(String site) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isUserSite(String site) {
		// TODO Auto-generated method stub
		return false;
	}

	public void join(String id) throws IdUnusedException, PermissionException {
		// TODO Auto-generated method stub

	}

	public String merge(String toSiteId, Element e, String creatorId) {
		// TODO Auto-generated method stub
		return null;
	}

	public void removeSite(Site site) throws PermissionException {
		// TODO Auto-generated method stub

	}

	public boolean removeSiteAdvisor(SiteAdvisor advisor) {
		// TODO Auto-generated method stub
		return false;
	}

	public void save(Site site) throws IdUnusedException, PermissionException {
		// TODO Auto-generated method stub

	}

	public void saveGroupMembership(Site site) throws IdUnusedException,
			PermissionException {
		// TODO Auto-generated method stub

	}

	public void saveSiteInfo(String id, String description, String infoUrl)
			throws IdUnusedException, PermissionException {
		// TODO Auto-generated method stub

	}

	public void saveSiteMembership(Site site) throws IdUnusedException,
			PermissionException {
		// TODO Auto-generated method stub

	}

	public void setSiteSecurity(String siteId, Set updateUsers,
			Set visitUnpUsers, Set visitUsers) {
		// TODO Auto-generated method stub

	}

	public void setUserSecurity(String userId, Set updateSites,
			Set visitUnpSites, Set visitSites) {
		// TODO Auto-generated method stub

	}

	public boolean siteExists(String id) {
		// TODO Auto-generated method stub
		return false;
	}

	public String siteGroupReference(String siteId, String groupId) {
		// TODO Auto-generated method stub
		return null;
	}

	public String sitePageReference(String siteId, String pageId) {
		// TODO Auto-generated method stub
		return null;
	}

	public String siteReference(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	public String siteToolReference(String siteId, String toolId) {
		// TODO Auto-generated method stub
		return null;
	}

	public void unjoin(String id) throws IdUnusedException, PermissionException {
		// TODO Auto-generated method stub

	}

	public String archive(String siteId, Document doc, Stack stack,
			String archivePath, List attachments) {
		// TODO Auto-generated method stub
		return null;
	}

	public Entity getEntity(Reference ref) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection getEntityAuthzGroups(Reference ref, String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getEntityDescription(Reference ref) {
		// TODO Auto-generated method stub
		return null;
	}

	public ResourceProperties getEntityResourceProperties(Reference ref) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getEntityUrl(Reference ref) {
		// TODO Auto-generated method stub
		return null;
	}

	public HttpAccess getHttpAccess() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	public String merge(String siteId, Element root, String archivePath,
			String fromSiteId, Map attachmentNames, Map userIdTrans,
			Set userListAllowImport) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean parseEntityReference(String reference, Reference ref) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean willArchiveMerge() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.site.api.SiteService#allowAddCourseSite()
	 */
	public boolean allowAddCourseSite()
	{
		return true;
	}

}
