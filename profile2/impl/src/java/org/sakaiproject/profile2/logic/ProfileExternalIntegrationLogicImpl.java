/**
 * Copyright (c) 2008-2012 The Sakai Foundation
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
package org.sakaiproject.profile2.logic;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.profile2.dao.ProfileDao;
import org.sakaiproject.profile2.model.ExternalIntegrationInfo;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.profile2.util.ProfileUtils;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of ProfileExternalIntegrationLogic API
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
@Slf4j
public class ProfileExternalIntegrationLogicImpl implements ProfileExternalIntegrationLogic {

	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public ExternalIntegrationInfo getExternalIntegrationInfo(String userUuid) {
		ExternalIntegrationInfo info = dao.getExternalIntegrationInfo(userUuid);
		if(info != null) {
			return info;
		}
		return getDefaultExternalIntegrationInfo(userUuid);
	}

	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public boolean updateExternalIntegrationInfo(ExternalIntegrationInfo info) {
		if(dao.updateExternalIntegrationInfo(info)){
			log.info("ExternalIntegrationInfo updated for user: " + info.getUserUuid());
			return true;
		}
		return false;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public String getGoogleAuthenticationUrl() {
		
		String clientId = sakaiProxy.getServerConfigurationParameter("profile2.integration.google.client-id", null);
	
		if(StringUtils.isBlank(clientId)){
			log.error("Google integration not properly configured. Please set the client id");
			return null;
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("https://accounts.google.com/o/oauth2/auth?");
		sb.append("client_id=");
		sb.append(clientId);
		sb.append("&redirect_uri=");
		sb.append(ProfileConstants.GOOGLE_REDIRECT_URI);
		sb.append("&response_type=code");
		sb.append("&scope=");
		sb.append(ProfileConstants.GOOGLE_DOCS_SCOPE);
		
		return sb.toString();
	}
	
	
	/**
	 * Get a default record, will only contain the userUuid
	 * @param userUuid
	 * @return
	 */
	private ExternalIntegrationInfo getDefaultExternalIntegrationInfo(String userUuid) {
		ExternalIntegrationInfo info = new ExternalIntegrationInfo();
		info.setUserUuid(userUuid);
		return info;
		
	}
	
	@Setter
	private ProfileDao dao;
	
	@Setter
	private SakaiProxy sakaiProxy;
	
	
}
