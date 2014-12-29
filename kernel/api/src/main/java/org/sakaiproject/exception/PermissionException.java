/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
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

package org.sakaiproject.exception;

/**
 * <p>
 * PermissionException indicates an invalid unlock attempt by a user for a lock and a resource.
 * </p>
 */
public class PermissionException extends SakaiException
{
	/** The id of the user. */
	private String m_user = null;

	/**
	 * Access the id of the user.
	 * 
	 * @return The id of the user.
	 */
	public String getUser()
	{
		return m_user;
	}

	/** The lock name. */
	private String m_lock = null;

	/**
	 * Access the lock name.
	 * 
	 * @return The lock name.
	 */
	public String getLock()
	{
		return m_lock;
	}

	/**
	 * Access the resource id.
	 * 
	 * @return The resource id.
	 */
	public String getResource()
	{
		return getId();
	}

	/**
	 * Construct.
	 * 
	 * @param user
	 *        The id of the user.
	 * @param lock
	 *        The lock name.
	 * @param resource
	 *        The resource id.
	 */
	public PermissionException(String user, String lock, String resource)
	{
		super(resource);
		m_user = user;
		m_lock = lock;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getMessage()
	{
		return "user=" + m_user + " lock=" + m_lock + " resource=" + getResource();
	}

}
