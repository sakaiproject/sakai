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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.authz.api.SecurityService;

@Slf4j
public class UserValidator implements Validator {

	public String userEmail;

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

	public void validate(Object obj, Errors errors) {
		RetUser retUser = (RetUser)obj;
		log.debug("validating user " + retUser.getEmail());

		if (retUser.getEmail() == null || "".equals(retUser.getEmail()))
		{
			log.debug("no email provided");
			errors.reject("noemailprovided", "no email provided");
			return;
		}
		
		Collection<User> c = this.userDirectoryService.findUsersByEmail(retUser.getEmail().trim());
		if (c.size()>1) {
			// Email is tied to more than one user, null out the user and transfer to next page
			log.warn("more than one account with email: {}", retUser.getEmail());
			retUser.setUser(null);
			return;
		} else if (c.size()==0) {
			// User doesn't exist, null out the user and transfer to next page
			log.debug("no such email: {}", retUser.getEmail());
			retUser.setUser(null);
			return;
		}

		Iterator<User> i = c.iterator();
		User user = (User)i.next();
		log.debug("got user " + user.getId() + " of type " + user.getType());
		if (securityService.isSuperUser(user.getId())) {
			// Email belongs to super user, null out the user and transfer to next page
			log.warn("tryng to change superuser password");
			retUser.setUser(null);
			return;
		}

		boolean allroles = serverConfigurationService.getBoolean("resetPass.resetAllRoles",false);
		if (!allroles){
			// SAK-24379 - deprecate the resetRoles property
			String[] roles = serverConfigurationService.getStrings("accountValidator.accountTypes.accept");
			String[] rolesOld = serverConfigurationService.getStrings("resetRoles");
			if (rolesOld != null)
			{
				log.warn("Found the resetRoles property; it is deprecated in favour of accountValidator.accountTypes.accept");
				if (roles == null)
				{
					roles = rolesOld;
				}
			}
			if (roles == null ){
				roles = new String[]{"guest"};
			}
			List<String> rolesL = Arrays.asList(roles);
			if (!rolesL.contains(user.getType())) {
				// Email belongs to a user who's type is not allowed to use this tool, null out the user and transfer to the next page
				log.warn("this is a user type which isn't allowed to reset password via tool");
				retUser.setUser(null);
				return;
			}
		}

		retUser.setUser(user);
	}
}
