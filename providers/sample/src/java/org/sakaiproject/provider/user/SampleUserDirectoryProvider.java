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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.provider.user;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.user.api.DisplayAdvisorUDP;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryProvider;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserFactory;
import org.sakaiproject.user.api.UsersShareEmailUDP;

/**
 * <p>
 * SampleUserDirectoryProvider is a samaple UserDirectoryProvider.
 * </p>
 */
public class SampleUserDirectoryProvider implements UserDirectoryProvider, UsersShareEmailUDP, DisplayAdvisorUDP
{
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(SampleUserDirectoryProvider.class);

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Dependencies and their setter methods
	 *********************************************************************************************************************************************************************************************************************************************************/

	/** how many students to recognize (1.. this). */
	protected int m_courseStudents = 1000;

	/**
	 * Set how many students to recognize.
	 * 
	 * @param count
	 *        How many students to recognize.
	 */
	public void setCourseStudents(String count)
	{
		m_courseStudents = Integer.parseInt(count);
	}

	/***************************************************************************
	 * Init and Destroy
	 **************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		DecimalFormat df = new DecimalFormat("0000");
		try
		{
			Info[] realNames = new Info[] {
				new Info("Victor", "van Dijk", "vvd@local.host"),
				new Info("Peter", "van Keken", "pvk@local.host"),
				new Info("Ben van", "der Pluijm", "bvdp@local.host"),
				new Info("Rob", "van der Voo", "rvdv@local.host"),
				new Info("Aimee", "de L'Aigle", "adlg@local.host"),
				new Info("Wong", "Kar-Wai", "wkw@local.host"),
				new Info("John", "Fitz Gerald", "jfg@local.host"),
				new Info("El", 
							new String("Niño".getBytes(), "UTF-8"), 
							"warmPacificWater@local.host"),
				new Info(new String("Ângeolo".getBytes(), "UTF-8"),
							"Haslip", 
							"ah@local.host"),
				new Info("Albert", "Zimmerman", "az@local.host"),
				new Info("Albert", "Albertson", "aa@local.host"),
				new Info("Zachary", "Anderson", "za@local.host"),
				new Info("Zachary", "Zurawik", "zz@local.host"),
				new Info("Bhaktavatsalam", "Bhayakridbhayanashanachar", "bb@local.host"),
			};
			
			// fill a set of users
			m_info = new Hashtable<String, Info>();
			m_info.put("user1", new Info("user1", "One", "User", "user1@local.host"));
			m_info.put("user2", new Info("user2", "Two", "User", "user2@local.host"));
			m_info.put("user3", new Info("user3", "Three", "User", "user3@local.host"));

			if (m_courseStudents > 0)
			{
				for (int i = 1; i <= m_courseStudents; i++)
				{
					String zeroPaddedId = df.format(i);
					// Use the realistic names if possible.
					if(i <= realNames.length) {
						m_info.put("student" + zeroPaddedId, new Info("student" + zeroPaddedId,
								realNames[i-1].firstName, realNames[i-1].lastName, realNames[i-1].email));
					} else {
						m_info.put("student" + zeroPaddedId, new Info("student" + zeroPaddedId,
								zeroPaddedId, "Student", "student" + zeroPaddedId + "@local.host"));
					}
				}
			}

			m_info.put("instructor", new Info("instructor", "The", "Instructor", "instructor@local.host"));
			m_info.put("instructor1", new Info("instructor1", "The", "Instructor1", "instructor1@local.host"));
			m_info.put("instructor2", new Info("instructor2", "The", "Instructor2", "instructor2@local.host"));
			m_info.put("da1", new Info("da1", "Dept", "Admin", "da1@local.host"));
			m_info.put("ta", new Info("ta", "The", "Teaching-Assistant", "ta@local.host"));

			M_log.info("init()");
		}
		catch (Throwable t)
		{
			M_log.warn(".init(): ", t);
		}
	}
	
	/**
	 * Returns to uninitialized state. You can use this method to release resources thet your Service allocated when Turbine shuts down.
	 */
	public void destroy()
	{

		M_log.info("destroy()");

	} // destroy

	/**********************************************************************************************************************************************************************************************************************************************************
	 * UserDirectoryProvider implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/** A collection of user ids/names. */
	protected Hashtable<String, Info> m_info = null;

	protected class Info
	{
		public String id;

		public String firstName;

		public String lastName;

		public String email;

		public Info(String id, String firstName, String lastName, String email)
		{
			this.id = id;
			this.firstName = firstName;
			this.lastName = lastName;
			this.email = email;
		}

		public Info(String firstName, String lastName, String email)
		{
			this.firstName = firstName;
			this.lastName = lastName;
			this.email = email;
		}

	} // class info

	/**
	 * Construct.
	 */
	public SampleUserDirectoryProvider()
	{
	}

	/**
	 * See if a user by this id exists.
	 * 
	 * @param userId
	 *        The user id string.
	 * @return true if a user by this id exists, false if not.
	 */
	protected boolean userExists(String userId)
	{
		if (userId == null) return false;
		if (userId.startsWith("test")) return true;
		if (m_info.containsKey(userId)) return true;

		return false;

	} // userExists

	/**
	 * Access a user object. Update the object with the information found.
	 * 
	 * @param edit
	 *        The user object (id is set) to fill in.
	 * @return true if the user object was found and information updated, false if not.
	 */
	public boolean getUser(UserEdit edit)
	{
		if (edit == null) return false;
		if (!userExists(edit.getEid())) return false;

		Info info = (Info) m_info.get(edit.getEid());
		if (info == null)
		{
			edit.setFirstName(edit.getEid());
			edit.setLastName(edit.getEid());
			edit.setEmail(edit.getEid());
			edit.setPassword(edit.getEid());
			edit.setType("registered");
		}
		else
		{
			edit.setFirstName(info.firstName);
			edit.setLastName(info.lastName);
			edit.setEmail(info.email);
			edit.setPassword("sakai");
			edit.setType("registered");
		}

		return true;

	} // getUser

	/**
	 * Access a collection of UserEdit objects; if the user is found, update the information, otherwise remove the UserEdit object from the collection.
	 * 
	 * @param users
	 *        The UserEdit objects (with id set) to fill in or remove.
	 */
	public void getUsers(Collection users)
	{
		for (Iterator i = users.iterator(); i.hasNext();)
		{
			UserEdit user = (UserEdit) i.next();
			if (!getUser(user))
			{
				i.remove();
			}
		}
	}

	/**
	 * Find a user object who has this email address. Update the object with the information found. <br />
	 * Note: this method won't be used, because we are a UsersShareEmailUPD.<br />
	 * This is the sort of method to provide if your external source has only a single user for any email address.
	 * 
	 * @param email
	 *        The email address string.
	 * @return true if the user object was found and information updated, false if not.
	 */
	public boolean findUserByEmail(UserEdit edit, String email)
	{
		if ((edit == null) || (email == null)) return false;

		// assume a "@local.host"
		int pos = email.indexOf("@local.host");
		if (pos != -1)
		{
			String id = email.substring(0, pos);
			edit.setEid(id);
			return getUser(edit);
		}

		return false;

	} // findUserByEmail

	/**
	 * Find all user objects which have this email address.
	 * 
	 * @param email
	 *        The email address string.
	 * @param factory
	 *        Use this factory's newUser() method to create all the UserEdit objects you populate and return in the return collection.
	 * @return Collection (UserEdit) of user objects that have this email address, or an empty Collection if there are none.
	 */
	public Collection findUsersByEmail(String email, UserFactory factory)
	{
		Collection rv = new Vector();

		// get a UserEdit to populate
		UserEdit edit = factory.newUser();

		// assume a "@local.host"
		int pos = email.indexOf("@local.host");
		if (pos != -1)
		{
			String id = email.substring(0, pos);
			edit.setEid(id);
			if (getUser(edit)) rv.add(edit);
		}

		return rv;
	}

	/**
	 * Authenticate a user / password. If the user edit exists it may be modified, and will be stored if...
	 * 
	 * @param id
	 *        The user id.
	 * @param edit
	 *        The UserEdit matching the id to be authenticated (and updated) if we have one.
	 * @param password
	 *        The password.
	 * @return true if authenticated, false if not.
	 */
	public boolean authenticateUser(String userId, UserEdit edit, String password)
	{
		if ((userId == null) || (password == null)) return false;

		if (userId.startsWith("test")) return userId.equals(password);
		if (userExists(userId) && password.equals("sakai")) return true;

		return false;

	} // authenticateUser

	/**
	 * {@inheritDoc}
	 */
	public boolean authenticateWithProviderFirst(String id)
	{
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean createUserRecord(String id)
	{
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDisplayId(User user)
	{
		return user.getEid();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDisplayName(User user)
	{
		// punt
		return null;
	}
}
