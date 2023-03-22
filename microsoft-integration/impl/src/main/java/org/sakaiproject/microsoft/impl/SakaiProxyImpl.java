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
package org.sakaiproject.microsoft.impl;

import java.util.List;
import java.util.Locale;

import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.microsoft.api.SakaiProxy;
import org.sakaiproject.microsoft.api.data.SakaiMembersCollection;
import org.sakaiproject.microsoft.api.data.SakaiSiteFilter;
import org.sakaiproject.microsoft.api.data.SakaiUserIdentifier;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.site.api.SiteService.SortType;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.nimbusds.oauth2.sdk.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
public class SakaiProxyImpl implements SakaiProxy {
	
	@Autowired
	private SecurityService securityService;
	
	@Autowired
	private SiteService siteService;
	
	@Autowired
	private UserDirectoryService userDirectoryService;
	
	@Autowired
	private SessionManager sessionManager;
	
	@Autowired
	private PreferencesService preferencesService;

	public void init() {
		log.info("Initializing Sakai Proxy");
	}
	
	// ------------------------------------------ SECURITY ----------------------------------------------------
	@Override
	public boolean isAdmin() {
		return securityService.isSuperUser();
	}

	@Override
	public boolean canUpdateSite(String siteReference, String userId) {
		return (securityService.unlock(userId, SiteService.SECURE_UPDATE_SITE, siteReference) || securityService.isSuperUser(userId));
	}
	
	// ------------------------------------------ USERS ----------------------------------------------------
	@Override
	public User getUser(String userId) {
		try {
			return userDirectoryService.getUser(userId);
		} catch (UserNotDefinedException e) {
			log.error("No user for id: {}", userId);
		}
		return null;
	}
	
	@Override
	public String getMemberKeyValue(User user, SakaiUserIdentifier key) {
		String ret = null;
		//we can use reflection instead, but it's more inefficient
		//String ret = (String)User.class.getField(key).get(user);
		try {
			switch(key) {
				case USER_PROPERTY:
					ret = (String)user.getProperties().get(SakaiUserIdentifier.USER_PROPERTY_KEY);
					break;
		
				case USER_EID:
					ret = user.getEid();
					break;
	
				case EMAIL:
					ret = (user.getEmail() != null) ? user.getEmail().toLowerCase() : null;
					break;
					
				default:
					ret = user.getEmail();
			}
		}catch(Exception e) {
			ret = (user != null) ? user.getEmail() : null;  
		}
		return ret;
	}
	
	@Override
	public void setUserProperty(String userId, String value) {
		try {
			UserEdit edit = userDirectoryService.editUser(userId);
			
			ResourcePropertiesEdit properties = edit.getPropertiesEdit();
			properties.removeProperty(SakaiUserIdentifier.USER_PROPERTY_KEY);
			properties.addProperty(SakaiUserIdentifier.USER_PROPERTY_KEY, value);
		
			userDirectoryService.commitEdit(edit);
		} catch(Exception e) {
			log.error("Could not set user property: userId={}", userId);
		}
	}
	
	// ------------------------------------------ LOCALE ----------------------------------------------------
	public Locale getLocaleForCurrentUser() {
		String userId = sessionManager.getCurrentSessionUserId();
		return StringUtils.isNotBlank(userId) ? preferencesService.getLocale(userId) : Locale.getDefault();
	}
	
	
	// ------------------------------------------ SITES ----------------------------------------------------
	@Override
	public List<Site> getSakaiSites(){
		return getSakaiSites(new SakaiSiteFilter());
	}
	
	@Override
	public List<Site> getSakaiSites(SakaiSiteFilter filter){
		if(filter != null) {
			SelectionType st = new SelectionType("any", true, true, false, true);
			if(filter.isPublished()) {
				st = new SelectionType("any", true, true, true, true);
			}
			return siteService.getSites(st, filter.getSiteType(false), null, filter.getSitePropertyMap(), SortType.TITLE_ASC, null);
		}
		return siteService.getSites(SelectionType.ANY, null, null, null, SortType.TITLE_ASC, null);
	}
	
	@Override
	public Site getSite(String siteId){
		try {
			return siteService.getSite(siteId);
		} catch (IdUnusedException e1) {
			log.error("Site not found for id: {}", siteId);
		}
		return null;
	}
	
	@Override
	public SakaiMembersCollection getSiteMembers(String siteId, SakaiUserIdentifier key) {

		SakaiMembersCollection ret = new SakaiMembersCollection();
		try {
			Site site = siteService.getSite(siteId);
			int count = 0;
			for(Member m : site.getMembers()) {
				try {
					//avoid insert admin user
					if(!securityService.isSuperUser(m.getUserId())) {
						User u = userDirectoryService.getUser(m.getUserId());
						String identifier = getMemberKeyValue(u, key);
						if(StringUtils.isBlank(identifier)) {
							log.info("User (id={}) with empty identifier: {}. Setting EMPTY_{}", u.getId(), key, count);
							identifier = "EMPTY_"+count;
							count++;
						}
						
						if(canUpdateSite(site.getReference(), u.getId())) {
							log.debug(">>USER+: ({}) --> displayName={}, userId={}, id={}", identifier, u.getDisplayName(), u.getId());
							ret.addOwner(identifier, u);
						} else {
							log.debug(">>USER: ({}) --> displayName={}, userId={}, id={}", identifier, u.getDisplayName(), u.getId());
							ret.addMember(identifier, u);
						}
					}
				} catch (UserNotDefinedException e) {
					log.error("No user for id: {}", m.getUserId());
				}
			}
		} catch (IdUnusedException e1) {
			log.error("Site not found for id: {}", siteId);
		}
		return ret;
	}
	
	
	// ------------------------------------------ GROUPS ----------------------------------------------------
	@Override
	public SakaiMembersCollection getGroupMembers(Group group, SakaiUserIdentifier key) {

		SakaiMembersCollection ret = new SakaiMembersCollection();

		for(Member m : group.getMembers()) {
			try {
				//avoid insert admin user
				if(!securityService.isSuperUser(m.getUserId())) {
					User u = userDirectoryService.getUser(m.getUserId());
					String identifier = getMemberKeyValue(u, key);
					if(canUpdateSite(group.getContainingSite().getReference(), u.getId())) {
						log.debug(">>USER+: ({}) --> displayName={}, userId={}, id={}", identifier, u.getDisplayName(), u.getId());
						ret.addOwner(identifier, u);
					} else {
						log.debug(">>USER: ({}) --> displayName={}, userId={}, id={}", identifier, u.getDisplayName(), u.getId());
						ret.addMember(identifier, u);
					}
				}
			} catch (UserNotDefinedException e) {
				log.error("No user for id: {}", m.getUserId());
			}
		}
		return ret;
	}
}
