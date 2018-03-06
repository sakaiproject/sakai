/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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
/*
* Licensed to The Apereo Foundation under one or more contributor license
* agreements. See the NOTICE file distributed with this work for
* additional information regarding copyright ownership.
*
* The Apereo Foundation licenses this file to you under the Educational 
* Community License, Version 2.0 (the "License"); you may not use this file 
* except in compliance with the License. You may obtain a copy of the 
* License at:
*
* http://opensource.org/licenses/ecl2.txt
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.calendaring.logic;

import java.io.File;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * Implementation of our SakaiProxy API
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
@Slf4j
public class SakaiProxyImpl implements SakaiProxy {
    
	/**
 	* {@inheritDoc}
 	*/
	public String getCurrentUserId() {
		return sessionManager.getCurrentSessionUserId();
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public boolean isSuperUser() {
		return securityService.isSuperUser();
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public String getServerName() {
		return serverConfigurationService.getServerName();
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public String getCalendarFilePath() {
		String path = serverConfigurationService.getString("calendar.ics.generation.path", System.getProperty("java.io.tmpdir"));
		//ensure trailing slash
		if(!StringUtils.endsWith(path, File.separator)) {
			path = path + File.separator;
		}
		return path;
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public String getUserEmail(String uuid) {
		
		if(StringUtils.isNotBlank(uuid)){
			try {
				return userDirectoryService.getUser(uuid).getEmail();
			} catch (UserNotDefinedException e) {
				log.warn("Cannot get email for id: " + uuid + " : " + e.getClass() + " : " + e.getMessage());
			}
		}
		return null;
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public String getUserDisplayName(String uuid) {
		
		if(StringUtils.isNotBlank(uuid)){
			try {
				return userDirectoryService.getUser(uuid).getDisplayName();
			} catch (UserNotDefinedException e) {
				log.warn("Cannot get displayname for id: " + uuid + " : " + e.getClass() + " : " + e.getMessage());
			}
		}
		return null;
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public boolean isIcsEnabled() {
		return serverConfigurationService.getBoolean("calendar.ics.generation.enabled", true);
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public boolean isCleanupEnabled() {
		return serverConfigurationService.getBoolean("calendar.ics.cleanup.enabled", true);
	}
	
	
	/**
	 * init
	 */
	public void init() {
		log.info("init");
	}
	

	@Setter
	private SessionManager sessionManager;
	
	@Setter	
	private SecurityService securityService;
	
	@Setter
	private ServerConfigurationService serverConfigurationService;
	
	@Setter
	private UserDirectoryService userDirectoryService;
	
}
