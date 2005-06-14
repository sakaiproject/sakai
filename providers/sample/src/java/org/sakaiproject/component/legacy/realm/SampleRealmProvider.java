/**********************************************************************************
 *
 * $Header: /cvs/sakai2/providers/sample/src/java/org/sakaiproject/component/legacy/realm/SampleRealmProvider.java,v 1.1 2005/05/15 04:12:05 csev.umich.edu Exp $
 *
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
 *                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
 * 
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 * 
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/

// package
package org.sakaiproject.component.legacy.realm;

// imports
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.service.legacy.realm.RealmProvider;
import org.sakaiproject.util.java.StringUtil;

/**
 * <p>
 * Sample of a RealmProvider. Shows how to handle compound ids (connected with a '+').
 * </p>
 * 
 * @author University of Michigan, Sakai Software Development Team
 * @version $Revision: 1.1 $
 */
public class SampleRealmProvider implements RealmProvider
{
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(SampleRealmProvider.class);

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
	 * RealmProvider implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/** A collection of user ids. */
	protected HashSet m_usersa = null;

	protected HashSet m_usersm = null;

	protected Properties m_usersx = null;

	/**
	 * Construct.
	 */
	public SampleRealmProvider()
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
			// if the user is in the list for the 'a' realm
			if ((m_usersa.contains(user)) && ("sakai.access".equals(ids[i])))
			{
				// give the "access" role, if "maintain" not already found
				if (!("maintain".equals(rv)))
				{
					rv = "access";
				}
			}

			// if the user is in the list for the 'm' realm
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
	public Map getUserRolesForRealm(String id)
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
	public Map getRealmRolesForUser(String userId)
	{
		update();

		// use our special map that resolves compound id keys
		Map rv = new MyMap();

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

	public class MyMap extends HashMap
	{
		/**
		 * {@inheritDoc}
		 */
		public Object get(Object key)
		{
			// if we have this key exactly, use it
			Object value = super.get(key);
			if (value != null) return value;

			// otherwise break up key as a compound id and find what values we have for these
			// the values are roles, and we prefer "maintain" to "access"
			String rv = null;
			String[] ids = unpackId((String) key);
			for (int i = 0; i < ids.length; i++)
			{
				value = super.get(ids[i]);
				if ((value != null) && !("maintain".equals(rv)))
				{
					rv = (String) value;
				}
			}

			return rv;
		}
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

/**************************************************************************************************************************************************************************************************************************************************************
 * $Header: /cvs/sakai2/providers/sample/src/java/org/sakaiproject/component/legacy/realm/SampleRealmProvider.java,v 1.1 2005/05/15 04:12:05 csev.umich.edu Exp $
 *************************************************************************************************************************************************************************************************************************************************************/
