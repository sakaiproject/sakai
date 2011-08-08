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

package org.sakaiproject.profile2.tool.entityprovider;

import java.util.Map;

import lombok.Setter;

import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityURLRedirect;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Redirectable;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.profile2.logic.ProfileLinkLogic;

/**
 * This is an entity provider that resolves links. Each has a special use case.
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
public class ProfileLinkEntityProvider extends AbstractEntityProvider implements EntityProvider, AutoRegisterEntityProvider, Redirectable, Describeable {
	
	public final static String ENTITY_PREFIX = "my";
	
	@Override
	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}
	
	@EntityURLRedirect("/{prefix}/profile/{userUuid}")
	public String redirectToUserProfile(Map<String,String> vars) {
		return linkLogic.getInternalDirectUrlToUserProfile(vars.get("userUuid"));
	}
	
	@EntityURLRedirect("/{prefix}/profile")
	public String redirectToMyProfile() {
		return linkLogic.getInternalDirectUrlToUserProfile();
	}
	
	@EntityURLRedirect("/{prefix}/messages/{thread}")
	public String redirectToMyMessageThread(Map<String,String> vars) {
		return linkLogic.getInternalDirectUrlToUserMessages(vars.get("thread"));
	}
	
	@EntityURLRedirect("/{prefix}/messages")
	public String redirectToMyMessages() {
		return linkLogic.getInternalDirectUrlToUserMessages(null);
	}	
	
	@EntityURLRedirect("/{prefix}/connections")
	public String redirectToMyConnections() {
		return linkLogic.getInternalDirectUrlToUserConnections();
	}
	
	@EntityURLRedirect("/{prefix}/wall/{userUuid}")
	public String redirectToMyWall(Map<String, String> vars) {
		return linkLogic.getInternalDirectUrlToUserWall(vars.get("userUuid"),
				null);
	}
	 	
	@EntityURLRedirect("/{prefix}/wall/{userUuid}/{wallItemId}")
	public String redirectToMyWallItem(Map<String, String> vars) {
		return linkLogic.getInternalDirectUrlToUserWall(vars.get("userUuid"),
				vars.get("wallItemId"));
	}
	
	
	@Setter
	private ProfileLinkLogic linkLogic;
	
}
