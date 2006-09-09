/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.provider.authzGroup;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.Properties;

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

}
