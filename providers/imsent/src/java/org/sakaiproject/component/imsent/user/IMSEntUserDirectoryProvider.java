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

package org.sakaiproject.component.imsent.user;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.user.api.UserDirectoryProvider;
import org.sakaiproject.user.api.UserEdit;

/**
 * <p>
 * IMSEntUserDirectoryProvider is a sample UserDirectoryProvider.
 * </p>
 */
@Slf4j
public class IMSEntUserDirectoryProvider implements UserDirectoryProvider
{

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Dependencies and their setter methods
	 *********************************************************************************************************************************************************************************************************************************************************/
	/** Dependency: SqlService */
	protected SqlService m_sqlService = null;

	/**
	 * Dependency: SqlService.
	 * 
	 * @param service
	 *        The SqlService.
	 */
	public void setSqlService(SqlService service)
	{
		log.info("Setting Sql Service");
		m_sqlService = service;
	}

	/** Configuration: to run the ddl on init or not. */
	// TODO: Set back to false
	protected boolean m_autoDdl = true;

	/**
	 * Configuration: to run the ddl on init or not.
	 * 
	 * @param value
	 *        the auto ddl value.
	 */
	public void setAutoDdl(String value)
	{
		m_autoDdl = new Boolean(value).booleanValue();
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
			log.info("init()");
		}
		catch (Exception t)
		{
			log.info(this + ".init() - failed attempting to log " + t);
			log.warn(".init(): " + t);
		}

		try
		{
			// if we are auto-creating our schema, check and create
			if (m_autoDdl && m_sqlService != null)
			{
				m_sqlService.ddl(this.getClass().getClassLoader(), "imsent_provider");
				log.info("Back from autoddl");
			}

			// Check to see if we are ready to run...
			if (!isReady())
			{
				log.warn(".init(): Not properly initialized.");
			}
		}
		catch (Exception t)
		{
			log.warn(".init(): ", t);
			m_isReady = false;
		}
		// Check to see if we are ready to run...
		if (!isReady())
		{
			log.warn(".init(): Not properly initialized.");
		}

	} // init

	/**
	 * Returns to uninitialized state. You can use this method to release resources thet your Service allocated when Turbine shuts down.
	 */
	public void destroy()
	{
		log.info("destroy()");
	} // destroy

	/**
	 * Determine if we are in a ready-to-go-state
	 */
	private boolean m_isReady = true;

	private boolean m_firstCheck = true;

	private boolean isReady()
	{
		// Only check things once
		if (!m_firstCheck) return m_isReady;
		m_firstCheck = false;

		boolean retval = true;

		if (m_sqlService == null)
		{
			log.warn("sqlService injection failed");
			retval = false;
		}

		// Check all other injections here

		// Return the value and set
		m_isReady = retval;
		return retval;
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * UserDirectoryProvider implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	public class SakaiIMSUser
	{
		// From User
		public String eMail = null;

		public String displayName = null;

		public String sortName = null;

		public String firstName = null;

		public String lastName = null;

		// From Resource
		// public ResourceProperties getProperties;
		public String id = null;

		// For use locally
		public String password = null;

		// For debugging
		public String toString()
		{
			String rv = "SakaiIMSUser Email=" + eMail + " DisplayName=" + displayName + " SortName=" + sortName + " FirstName="
					+ firstName + " LastName=" + lastName + " Id=" + id + " Password=" + password;
			return rv;
		}
	}

	public SakaiIMSUser retrieveUser(final String userId, boolean isEmail)
	{
		String statement;

		if (userId == null) return null;

		if (isEmail)
		{
			// 1 2 3 4 5 6 7
			statement = "select USERID,FN,SORT,PASSWORD,FAMILY,GIVEN,EMAIL from IMSENT_PERSON where EMAIL = ?";
		}
		else
		{
			statement = "select USERID,FN,SORT,PASSWORD,FAMILY,GIVEN,EMAIL from IMSENT_PERSON where USERID = ?";
		}

		Object fields[] = new Object[1];
		fields[0] = userId;

		log.info("SQL:" + statement);
		List rv = m_sqlService.dbRead(statement, fields, new SqlReader()
		{
			public Object readSqlResultRecord(ResultSet result)
			{
				try
				{
					SakaiIMSUser rv = new SakaiIMSUser();
					rv.id = result.getString(1);
					rv.displayName = result.getString(2);
					rv.sortName = result.getString(3);
					if (rv.sortName == null) rv.sortName = rv.displayName;
					rv.password = result.getString(4);
					rv.lastName = result.getString(5);
					rv.firstName = result.getString(6);
					rv.eMail = result.getString(7);
					log.info("Inside reader " + rv);
					return rv;
				}
				catch (SQLException e)
				{
					log.warn(this + ".authenticateUser: " + userId + " : " + e);
					return null;
				}
			}
		});

		if ((rv != null) && (rv.size() > 0))
		{
			log.info("Returning ");
			log.info(" " + (SakaiIMSUser) rv.get(0));
			return (SakaiIMSUser) rv.get(0);
		}
		return null;
	}

	/**
	 * Construct.
	 */
	public IMSEntUserDirectoryProvider()
	{

	} // SampleUserDirectoryProvider

	/**
	 * Copy the information from our internal structure into the Sakai User structure.
	 * 
	 * @param edit
	 * @param imsUser
	 */
	private void copyInfo(UserEdit edit, SakaiIMSUser imsUser)
	{
		edit.setId(imsUser.id);
		edit.setFirstName(imsUser.firstName);
		edit.setLastName(imsUser.lastName);
		edit.setEmail(imsUser.eMail);
		edit.setPassword(imsUser.password);
		// Sakai currently creates sortname from first and last name
		edit.setType("imsent");
	}

	/**
	 * Access a user object. Update the object with the information found.
	 * 
	 * @param edit
	 *        The user object (id is set) to fill in.
	 * @return true if the user object was found and information updated, false if not.
	 */
	public boolean getUser(UserEdit edit)
	{
		if (!isReady()) return false;
		if (edit == null) return false;
		String userId = edit.getEid();

		log.info("getUser(" + userId + ")");
		SakaiIMSUser rv = retrieveUser(userId, false);
		if (rv == null) return false;
		copyInfo(edit, rv);
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
	 * Find a user object who has this email address. Update the object with the information found.
	 * 
	 * @param email
	 *        The email address string.
	 * @return true if the user object was found and information updated, false if not.
	 */
	public boolean findUserByEmail(UserEdit edit, String email)
	{
		if (!isReady()) return false;
		if ((edit == null) || (email == null)) return false;

		log.info("findUserByEmail(" + email + ")");
		SakaiIMSUser rv = retrieveUser(email, true);
		if (rv == null) return false;
		copyInfo(edit, rv);
		return true;

	} // findUserByEmail

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
	public boolean authenticateUser(final String userId, UserEdit edit, String password)
	{
		if (!isReady()) return false;
		if ((userId == null) || (password == null)) return false;
		log.info("authenticateUser(" + userId + ")");
		SakaiIMSUser rv = retrieveUser(userId, false);
		if (rv == null) return false;
		return (password.compareTo(rv.password) == 0);
	} // authenticateUser

	/**
	 * {@inheritDoc}
	 */
	public boolean authenticateWithProviderFirst(String id)
	{
		return false;
	}

} // SampleUserDirectoryProvider

