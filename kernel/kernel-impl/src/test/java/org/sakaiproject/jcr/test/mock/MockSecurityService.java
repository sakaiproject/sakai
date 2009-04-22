/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
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

package org.sakaiproject.jcr.test.mock;

import java.util.Collection;
import java.util.List;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.user.api.User;

/**
 * @author ieb
 */
public class MockSecurityService implements SecurityService
{

	private static final Log log = LogFactory.getLog(MockSecurityService.class);

	private static ThreadLocal<User> currentUser = new ThreadLocal<User>();

	private Stack<SecurityAdvisor> stack = new Stack<SecurityAdvisor>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.authz.api.SecurityService#clearAdvisors()
	 */
	public void clearAdvisors()
	{
		stack.clear();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.authz.api.SecurityService#hasAdvisors()
	 */
	public boolean hasAdvisors()
	{
		return stack.size() > 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.authz.api.SecurityService#isSuperUser()
	 */
	public boolean isSuperUser()
	{
		throw new RuntimeException("JCRService Should not bind to sessions ");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.authz.api.SecurityService#isSuperUser(java.lang.String)
	 */
	public boolean isSuperUser(String userId)
	{
		log.debug("User is super user " + userId);
		return MockTestUser.SUPERUSER.getId().equals(userId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.authz.api.SecurityService#popAdvisor()
	 */
	public SecurityAdvisor popAdvisor()
	{
		return stack.pop();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.authz.api.SecurityService#pushAdvisor(org.sakaiproject.authz.api.SecurityAdvisor)
	 */
	public void pushAdvisor(SecurityAdvisor advisor)
	{
		stack.push(advisor);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.authz.api.SecurityService#unlock(java.lang.String,
	 *      java.lang.String)
	 */
	public boolean unlock(String lock, String reference)
	{
		throw new RuntimeException("JCRService Should not bind to sessions ");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.authz.api.SecurityService#unlock(org.sakaiproject.user.api.User,
	 *      java.lang.String, java.lang.String)
	 */
	public boolean unlock(User user, String lock, String reference)
	{
		return unlock(user.getId(), lock, reference);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.authz.api.SecurityService#unlock(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public boolean unlock(String userId, String lock, String reference)
	{
		// log.info("Unlocking ["+lock+"] for ["+reference+"] User
		// ["+userId+"]");
		if ((userId == null) || (userId.trim().length() == 0)
				|| MockTestUser.ANONUSER.getId().equals(userId))
		{
			// if ( reference != null && reference.startsWith("/testroot") ) {
			// return true;
			// } else
			if (":jcr.get:".indexOf(lock) >= 0)
			{
				log.debug("Granted " + lock + " for " + reference + " User " + userId);
				return true;
			}
			else
			{
				log.debug("Denied " + lock + " for " + reference + " User " + userId);
				return false;
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.authz.api.SecurityService#unlock(java.lang.String,
	 *      java.lang.String, java.lang.String, java.util.Collection)
	 */
	public boolean unlock(String userId, String lock, String reference,
			Collection authzGroupIds)
	{
		throw new RuntimeException(
				"JCRService appearss to be going authZGroups security ");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.authz.api.SecurityService#unlockUsers(java.lang.String,
	 *      java.lang.String)
	 */
	public List unlockUsers(String lock, String reference)
	{
		throw new RuntimeException(
				"JCRService appears to be getting a list of users on a lock ");
	}

	public void clearUserEffectiveRole(String arg0) {
		// TODO Auto-generated method stub
		
	}

	public void clearUserEffectiveRoles() {
		// TODO Auto-generated method stub
		
	}

	public String getUserEffectiveRole(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean setUserEffectiveRole(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return false;
	}

}
