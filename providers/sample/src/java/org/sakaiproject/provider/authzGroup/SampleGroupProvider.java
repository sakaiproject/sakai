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
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.GroupProvider;
import org.sakaiproject.util.StringUtil;

/**
 * <p>
 * Sample of a GroupProvider. Shows how to handle compound ids (connected with a '+').
 * </p>
 * <p>
 * To use, set an authzGroup's external id to one of the following:
 * <ul>
 * <li>sakai.access</li>
 * <li>sakai.maintain</li>
 * <li>sakai.access+sakai.maintain</li>
 * </ul>
 * </p>
 * <p>
 * You should also have a user directory provider that recognizes the users:
 * <ul>
 * <li>user1</li>
 * <li>user</li>
 * <li>user3</li>
 * </ul>
 * The SampleUserDirectoryProvider does this.
 * </p>
 */
public class SampleGroupProvider implements GroupProvider
{
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(SampleGroupProvider.class);

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Dependencies and their setter methods
	 *********************************************************************************************************************************************************************************************************************************************************/

	/** Configuration: the full file path to the external user grants file (optional). */
	protected String m_xFile = null;

	/**
	 * Dependency - set the full file path to the optional external user grants file.
	 * 
	 * @param value
	 *        The full file path to the external user grants file.
	 */
	public void setXFile(String value)
	{
		m_xFile = value;
	}

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

	/** A collection of user ids. */
	protected HashSet m_usersa = null;

	protected HashSet m_usersm = null;

	protected Properties m_usersx = null;

	/**
	 * Construct.
	 */
	public SampleGroupProvider()
	{
		// users: 1 is 'a', 2 is both, 3 is 'm'
		m_usersa = new HashSet();
		m_usersa.add("user1");
		m_usersa.add("user2");

		m_usersm = new HashSet();
		m_usersm.add("user2");
		m_usersm.add("user3");
	}

	/**
	 * {@inheritDoc}
	 */
	public String getRole(String id, String user)
	{
		update();

		String rv = null;

		// process compound id, and prefer "maintain" to "access" if there's a choice
		String[] ids = unpackId(id);
		for (int i = 0; i < ids.length; i++)
		{
			// if the user is in the list for the 'a' group
			if ((m_usersa.contains(user)) && ("sakai.access".equals(ids[i])))
			{
				// give the "access" role, if "maintain" not already found
				if (!("maintain".equals(rv)))
				{
					rv = "access";
				}
			}

			// if the user is in the list for the 'm' group
			if ((m_usersm.contains(user)) && ("sakai.maintain".equals(ids[i])))
			{
				// give the "maintain" role
				rv = "maintain";
			}

			if ((m_usersx.contains(user)) && ("sakai.x".equals(ids[i])))
			{
				// give whatever's in the x role, if "maintain" not already found
				if (!("maintain".equals(rv)))
				{
					rv = m_usersx.getProperty(user);
				}
			}
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Map getUserRolesForGroup(String id)
	{
		update();

		Map rv = new HashMap();

		// responds properly to a null id - with an empty map
		if (id == null) return rv;

		// process compound id, and prefer "maintain" to "access" if there's a choice
		String[] ids = unpackId(id);
		for (int i = 0; i < ids.length; i++)
		{
			if ("sakai.access".equals(ids[i]))
			{
				// put each user in the map as "access", unless they are already there (as maintain perhaps)
				for (Iterator it = m_usersa.iterator(); it.hasNext();)
				{
					String userId = (String) it.next();
					if (!("maintain".equals(rv.get(userId))))
					{
						rv.put(userId, "access");
					}
				}
			}

			if ("sakai.maintain".equals(ids[i]))
			{
				// put each user in the map as "maintain", even if they are there (as access perhaps)
				for (Iterator it = m_usersm.iterator(); it.hasNext();)
				{
					String userId = (String) it.next();
					rv.put(userId, "maintain");
				}
			}

			if ("sakai.x".equals(ids[i]))
			{
				// put each user in the map as whatever they are in x, unless they are already there (as maintain perhaps)
				for (Iterator it = m_usersx.keySet().iterator(); it.hasNext();)
				{
					String userId = (String) it.next();
					if (!("maintain".equals(rv.get(userId))))
					{
						rv.put(userId, m_usersx.getProperty(userId));
					}
				}
			}
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Map getGroupRolesForUser(String userId)
	{
		update();

		Map rv = new HashMap();

		if (m_usersa.contains(userId))
		{
			rv.put("sakai.access", "access");
		}

		if (m_usersm.contains(userId))
		{
			rv.put("sakai.maintain", "maintain");
		}

		if (m_usersx.keySet().contains(userId))
		{
			rv.put("sakai.x", m_usersx.getProperty(userId));
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
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
	public String preferredRole(String one, String other)
	{
		// maintain is better than access
		if ("maintain".equals(one) || ("maintain".equals(other))) return "maintain";
		
		// access is better than nothing
		if ("access".equals(one) || ("access".equals(other))) return "access";
		
		// something we don't know, so we just return the latest role found
		return one;
	}

	protected void update()
	{
		m_usersx = new Properties();

		if (m_xFile != null)
		{
			try
			{
				m_usersx.load(new FileInputStream(m_xFile));
			}
			catch (Exception e)
			{
				M_log.warn("update: reading users.properties file: " + m_xFile + " : " + e);
			}
		}
	}
}
