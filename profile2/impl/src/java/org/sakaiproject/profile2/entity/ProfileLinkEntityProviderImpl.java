/**
 * Copyright (c) 2008-2010 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.profile2.entity;

import java.util.List;
import java.util.Map;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityURLRedirect;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.profile2.service.ProfileLinkService;

public class ProfileLinkEntityProviderImpl implements ProfileLinkEntityProvider, CoreEntityProvider, AutoRegisterEntityProvider, RESTful {

	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}
		
	public boolean entityExists(String eid) {
		return true;
	}

	public Object getSampleEntity() {
		return null;
	}
	
	public Object getEntity(EntityReference ref) {
		return null;
	}
	
	
	@EntityURLRedirect("/{prefix}/profile/{userUuid}")
	public String redirectToUserProfile(Map<String,String> vars) {
		return linkService.getInternalDirectUrlToUserProfile(vars.get("userUuid"));
	}
	
	@EntityURLRedirect("/{prefix}/profile")
	public String redirectToMyProfile() {
		return linkService.getInternalDirectUrlToUserProfile();
	}
	
	@EntityURLRedirect("/{prefix}/messages/{thread}")
	public String redirectToMyMessageThread(Map<String,String> vars) {
		return linkService.getInternalDirectUrlToUserMessages(vars.get("thread"));
	}
	
	@EntityURLRedirect("/{prefix}/messages")
	public String redirectToMyMessages() {
		return linkService.getInternalDirectUrlToUserMessages(null);
	}	
	
	@EntityURLRedirect("/{prefix}/connections")
	public String redirectToMyConnections() {
		return linkService.getInternalDirectUrlToUserConnections();
	}
	
	
	public String[] getHandledOutputFormats() {
		return new String[] {Formats.HTML};
	}

	public String[] getHandledInputFormats() {
		return new String[] {};
	}
	
	public String createEntity(EntityReference ref, Object entity, Map<String, Object> params) {
		return null;
	}

	public void updateEntity(EntityReference ref, Object entity,Map<String, Object> params) {
	}

	public void deleteEntity(EntityReference ref, Map<String, Object> params) {
	}

	public List<?> getEntities(EntityReference ref, Search search) {
		return null;
	}
	
	private ProfileLinkService linkService;
	public void setLinkService(ProfileLinkService linkService) {
		this.linkService = linkService;
	}
	
	
	

	
	
	

}
