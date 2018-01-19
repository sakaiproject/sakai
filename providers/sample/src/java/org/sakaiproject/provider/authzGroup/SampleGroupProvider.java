/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
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
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import org.sakaiproject.authz.api.GroupProvider;

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
@Slf4j
public class SampleGroupProvider implements GroupProvider
{

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

	/** The external id for the "course" group. */
	protected String m_courseExternalId = "2006,FALL,SMPL,001,001";

	/**
	 * Dependency - set the external id for the "course" group.
	 * 
	 * @param value
	 *        The external id for the "course" site.
	 */
	public void setCourseExternalId(String value)
	{
		m_courseExternalId = value;
	}

	/** how many students in the "course" group. */
	protected int m_courseStudents = 22;

	/**
	 * Set how many students in the "course" group
	 * 
	 * @param count
	 *        How many students in the "course" group
	 */
	public void setCourseStudents(String count)
	{
		m_courseStudents = Integer.parseInt(count);
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
			// users: 1 is 'a', 2 is both, 3 is 'm'
			m_usersa = new HashSet<String>();
			m_usersa.add("user1");
			m_usersa.add("user2");

			m_usersm = new HashSet<String>();
			m_usersm.add("user2");
			m_usersm.add("user3");
			
			// the instructor and ta and some students
			m_usersc = new HashSet<String>();
			if ((m_courseStudents > 0) && (m_courseExternalId != null))
			{
				m_usersc.add("instructor");
				m_usersc.add("ta");

				for (int i = 1; i <= m_courseStudents; i++)
				{
					m_usersc.add("student" + i);
				}
			}

			log.info("init()");
		}
		catch (Exception t)
		{
			log.warn("init(): ", t);
		}
	}

	/**
	 * Cleanup before shutting down.
	 */
	public void destroy()
	{
		log.info("destroy()");
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * GroupProvider implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/** A collection of user ids. */
	protected HashSet<String> m_usersa = null;

	protected HashSet<String> m_usersm = null;

	protected Properties m_usersx = null;
	
	protected HashSet<String> m_usersc = null;

	/**
	 * Construct.
	 */
	public SampleGroupProvider()
	{
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
			
			if ((m_usersc.contains(user)) && (m_courseExternalId.equals(ids[i])))
			{
				if ("instructor".equals(user))
				{
					rv = "Instructor";
				}
				else if ("ta".equals(user))
				{
					rv = "Teaching Assistant";
				}
				else
				{
					rv = "Student";
				}
			}
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<String, String> getUserRolesForGroup(String id)
	{
		update();

		Map<String, String> rv = new HashMap<String, String>();

		// responds properly to a null id - with an empty map
		if (id == null) return rv;

		// process compound id, and prefer "maintain" to "access" if there's a choice
		String[] ids = unpackId(id);
		for (int i = 0; i < ids.length; i++)
		{
			if ("sakai.access".equals(ids[i]))
			{
				// put each user in the map as "access", unless they are already there (as maintain perhaps)
				for (Iterator<String> it = m_usersa.iterator(); it.hasNext();)
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
				for (Iterator<String> it = m_usersm.iterator(); it.hasNext();)
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
			
			if ((m_courseExternalId != null) && (m_courseExternalId.equals(ids[i])))
			{
				for (Iterator<String> it = m_usersc.iterator(); it.hasNext();)
				{
					String userId = (String) it.next();
					if ("instructor".equals(userId))
					{
						rv.put(userId, "Instructor");
					}
					else if ("ta".equals(userId))
					{
						rv.put(userId, "Teaching Assistant");
					}
					else
					{
						rv.put(userId, "Student");
					}
				}
			}
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<String, String> getGroupRolesForUser(String userId)
	{
		update();

		Map<String, String> rv = new HashMap<>();

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
		
		if (m_usersc.contains(userId))
		{
			if ("instructor".equals(userId))
			{
				rv.put(m_courseExternalId, "Instructor");
			}
			else if ("ta".equals(userId))
			{
				rv.put(m_courseExternalId, "Teaching Assistant");
			}
			else
			{
				rv.put(m_courseExternalId, "Student");
			}
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
			rv = StringUtils.split(id, "+");
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public String preferredRole(String one, String other)
	{
		// maintain and Instructor first
		if ("maintain".equals(one) || ("maintain".equals(other))) return "maintain";

		if ("Instructor".equals(one) || ("Instructor".equals(other))) return "Instructor";

		// ta next
		if ("Teaching Assistant".equals(one) || ("Teaching Assistant".equals(other))) return "Teaching Assistant";

		// access and Student next
		if ("access".equals(one) || ("access".equals(other))) return "access";
		
		if ("Student".equals(one) || ("Student".equals(other))) return "Student";

		// something we don't know, so we just return the latest role found
		return one;
	}

	protected void update()
	{
		m_usersx = new Properties();

		if (m_xFile != null)
		{
			FileInputStream fis = null;
			try
			{
				fis = new FileInputStream(m_xFile);
				m_usersx.load(fis);
			}
			catch (Exception e)
			{
				log.warn("update: reading users.properties file: " + m_xFile + " : " + e);
			}
			finally
			{
				if (fis != null)
				{
					try {
						fis.close();
					} catch (IOException e) {
						log.error(e.getMessage(), e);
					}
				}
			}
		}
	}

	public boolean groupExists(String arg0) {
		return true;
	}
}
