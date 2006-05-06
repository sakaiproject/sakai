/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
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
package org.sakaiproject.component.legacy.user;

// imports
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.service.legacy.user.UserDirectoryProvider;
import org.sakaiproject.service.legacy.user.UserEdit;
import org.sakaiproject.service.legacy.user.UserFactory;
import org.sakaiproject.service.legacy.user.UsersShareEmailUDP;

/**
 * <p>
 * SampleUserDirectoryProvider is a samaple UserDirectoryProvider.
 * </p>
 * 
 * @author University of Michigan, Sakai Software Development Team
 * @version $Revision$
 */
public class SampleUserDirectoryProvider implements UserDirectoryProvider, UsersShareEmailUDP
{
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(SampleUserDirectoryProvider.class);

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Dependencies and their setter methods
	 *********************************************************************************************************************************************************************************************************************************************************/

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
			M_log.warn(".init(): ", t);
		}

	} // init

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
	protected Hashtable m_info = null;

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

	} // class info

	/**
	 * Construct.
	 */
	public SampleUserDirectoryProvider()
	{
		// fill a set of users
		m_info = new Hashtable();
		m_info.put("user1", new Info("user1", "One", "User", "user1@sakaiproject.org"));
		m_info.put("user2", new Info("user2", "Two", "User", "user2@sakaiproject.org"));
		m_info.put("user3", new Info("user3", "Three", "User", "user3@sakaiproject.org"));

	} // SampleUserDirectoryProvider

	/**
	 * See if a user by this id exists.
	 * 
	 * @param userId
	 *        The user id string.
	 * @return true if a user by this id exists, false if not.
	 */
	public boolean userExists(String userId)
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
		if (!userExists(edit.getId())) return false;

		Info info = (Info) m_info.get(edit.getId());
		if (info == null)
		{
			edit.setFirstName(edit.getId());
			edit.setLastName(edit.getId());
			edit.setEmail(edit.getId());
			edit.setPassword(edit.getId());
		}
		else
		{
			edit.setFirstName(info.firstName);
			edit.setLastName(info.lastName);
			edit.setEmail(info.email);
			edit.setPassword("sakai");
		}
		edit.setType("sample");

		return true;

	} // getUser

	/**
	 * Access a collection of UserEdit objects; if the user is found, update the information, otherwise remove the UserEdit object from the collection.
	 * @param users The UserEdit objects (with id set) to fill in or remove.
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

		// assume a "@sakaiproject.org"
		int pos = email.indexOf("@sakaiproject.org");
		if (pos != -1)
		{
			String id = email.substring(0, pos);
			edit.setId(id);
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

		// assume a "@sakaiproject.org"
		int pos = email.indexOf("@sakaiproject.org");
		if (pos != -1)
		{
			String id = email.substring(0, pos);
			edit.setId(id);
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
	 * Will this provider update user records on successfull authentication? If so, the UserDirectoryService will cause these updates to be stored.
	 * 
	 * @return true if the user record may be updated after successfull authentication, false if not.
	 */
	public boolean updateUserAfterAuthentication()
	{
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public void destroyAuthentication()
	{
	}

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
	
} // SampleUserDirectoryProvider



