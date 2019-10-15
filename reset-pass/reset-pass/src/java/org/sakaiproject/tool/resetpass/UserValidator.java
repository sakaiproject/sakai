/**
 * Copyright (c) 2006-2017 The Apereo Foundation
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
package org.sakaiproject.tool.resetpass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

@Slf4j
public class UserValidator implements Validator {

	public String userEmail;

	// Prefix for error messages - indicates they are to be pulled from tool configuration rather than a resource bundle
	private static final String TOOL_CONFIG_PREFIX = "toolconfig_";

	// Sakai.property key for invalid domains in reset password email requests
	private static final String SAK_PROP_INVALID_EMAIL_DOMAINS = "resetPass.invalidEmailDomains";

	// Message bundle key for wrong type message (invalid domain)
	private static final String WRONG_TYPE_MSG_BUNDLE_KEY = "wrongtype";

	// Message bundle key for no email provided message
	private static final String NO_EMAIL_MSG_BUNDLE_KEY = "noemailprovided";

	public boolean supports(Class clazz) {
		return clazz.equals(User.class);
	}

	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(ServerConfigurationService s) {
		this.serverConfigurationService = s;
	}

	private UserDirectoryService userDirectoryService;
	public void setUserDirectoryService(UserDirectoryService ds){
		this.userDirectoryService = ds;
	}

	private SecurityService securityService;
	public void setSecurityService(SecurityService ss){
		this.securityService = ss;
	}

	private ToolManager toolManager;
	public void setToolManager(ToolManager tm) {
		this.toolManager = tm;
	}

	public void validate(Object obj, Errors errors) {
		RetUser retUser = (RetUser)obj;
		log.debug("validating user " + retUser.getEmail());

		// Short circuit: no email provided
		if (StringUtils.isBlank(retUser.getEmail())) {
			log.debug("no email provided");
			errors.reject(NO_EMAIL_MSG_BUNDLE_KEY, "no email provided");
			return;
		}

		// Short circuit: domain provided not allowed
		List<String> invalidDomains = serverConfigurationService.getStringList(SAK_PROP_INVALID_EMAIL_DOMAINS, new ArrayList<>());
		if (CollectionUtils.isNotEmpty(invalidDomains) && invalidDomains.stream().anyMatch( d -> retUser.getEmail().toLowerCase().contains(d.toLowerCase()))) {
			Placement placement = toolManager.getCurrentPlacement();
			String toolPropWrongType = placement.getConfig().getProperty(WRONG_TYPE_MSG_BUNDLE_KEY);
			if (StringUtils.isBlank(toolPropWrongType)) {
				errors.reject(WRONG_TYPE_MSG_BUNDLE_KEY, "wrong type");
			} else {
				errors.reject(TOOL_CONFIG_PREFIX + WRONG_TYPE_MSG_BUNDLE_KEY, toolPropWrongType);
			}

			return;
		}

		// User doesn't exist, null out the user and transfer to next page
		Collection<User> c = this.userDirectoryService.findUsersByEmail(retUser.getEmail().trim());
		if (CollectionUtils.isEmpty(c)) {
			log.debug("no such email: {}", retUser.getEmail());
			retUser.setUser(null);
			return;
		}

		// Email is tied to more than one user, null out the user and transfer to next page
		else if (c.size() > 1) {
			log.warn("more than one account with email: {}", retUser.getEmail());
			retUser.setUser(null);
			return;
		}

		// Email belongs to super user, null out the user and transfer to next page
		User user = (User) c.iterator().next();
		if (securityService.isSuperUser(user.getId())) {
			log.warn("tryng to change superuser password");
			retUser.setUser(null);
			return;
		}

		// All checks have passed successfully
		retUser.setUser(user);
	}
}
