/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.provider.authzGroup;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.GroupProvider;
import org.sakaiproject.util.StringUtil;

/**
 * <p>
 * AllHands GroupProvider allows the creation of provided authzGroups that automatically include all users.
 * </p>
 * <p>
 * To use, set an authzGroup's external id to one of the following:
 * <ul>
 * <li>sakai.allhands</li>
 * </ul>
 * </p>
 */
public class AllHandsGroupProvider implements GroupProvider
{
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(AllHandsGroupProvider.class);

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		try
		{
			M_log.info("init()");
		}
		catch (Throwable t)
		{
			M_log.warn("init(): ", t);
		}
	}

	/**
	 * Cleanup before shutting down.
	 */
	public void destroy()
	{
		M_log.info("destroy()");
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * GroupProvider implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Construct.
	 */
	public AllHandsGroupProvider()
	{
	}

	/**
	 * {@inheritDoc}
	 */
	public String getRole(String id, String user)
	{
		System.out.println("getRole() id="+id+" user="+user);
		// Apparently this is not called ???
		return null;
	}

	/**
	 * {@inheritDoc}
	 * This is not necessary - because the user has already been added because they are
	 * enrolled in the allhand.s
	 */
	public Map getUserRolesForGroup(String id)
	{
		Map rv = new HashMap();

		return rv;
	}

	/**
	 * {@inheritDoc}
	 *
	 * If you can do some type of directory lookup to find out some 
	 * Attribute of this user you could conditionally auto-add them
	 * to any number of these "virtual groups"
	 *
	 * You might try sakai.students, sakai.faculty, 
	 * sakai.staff, sakai.teamleads
	 *
	 * There is nothing specific about the string "sakai."  these could
 	 * easily be something like course.eecs280, shib:yale.edu:faculty,
 	 * or h23.groups.ruk.dk
	 *
	 * One caveat - this class is respnsbile for handling the unpacking
	 * when an admin wants to put in multiple external providers
	 * The code below uses the common convention of using "+" (homage
	 * to "or") to concatenate IDs.  So the user above us could set the
	 * provider ID in the Sakai Realms Tool to be
	 *
	 *    h23.groups.ruk.dk+course.eecs280 
	 *
	 * to indicate thatr membership in either group is OK.
     *
	 * To indicate membership in multiple groups in *this routine* add 
	 * additional entries in the hash map (i.e. do not use the + notation
	 * in this routine).
	 */

	public Map getGroupRolesForUser(String userId)
	{
		System.out.println("getGroupRolesForUser() user="+userId);

		Map rv = new HashMap();

		rv.put("sakai.allhands","access");

		return rv;
	}

	public String[] unpackId(String id)
	{
		String[] rv = null;

		// if there is not a '+' return just the id
		int pos = id.indexOf('+');
		if (pos == -1)
		{
			rv = new String[1];
			rv[0] = id;
		}

		// otherwise split by the '+'
		else
		{
			rv = StringUtil.split(id, "+");
		}

		return rv;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String packId(String[] ids)
	{
		if(ids == null || ids.length == 0)
		{
			return null;
		}
		
		if(ids.length == 1)
		{
			return ids[0];
		}
		
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<ids.length; i++)
		{
			sb.append(ids[i]);
			if(i < ids.length - 1)
			{
				sb.append("+");
			}
		}
		return sb.toString();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String preferredRole(String one, String other)
	{
		// maintain is better than access
		if ("maintain".equals(one) || ("maintain".equals(other))) return "maintain";
		
		// access is better than nothing
		if ("access".equals(one) || ("access".equals(other))) return "access";
		
		// something we don't know, so we just return the latest role found
		return one;
	}
}
