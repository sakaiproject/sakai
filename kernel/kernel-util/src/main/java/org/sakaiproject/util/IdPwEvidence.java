/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 Sakai Foundation
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

package org.sakaiproject.util;

/**
 * <p>
 * IdPwEvidence is a utility class that implements the IdPwEvidence interface.
 * </p>
 */
public class IdPwEvidence implements org.sakaiproject.user.api.IdPwEvidence
{
	/** The user identifier string. */
	protected String m_identifier = null;

	/** The password string. */
	protected String m_password = null;

	/**
	 * Construct, with identifier and password.
	 * 
	 * @param identifier
	 *        The user identifier string.
	 * @param password
	 *        The password string.
	 */
	public IdPwEvidence(String identifier, String password)
	{
		m_identifier = identifier;
		m_password = password;
	}

	/**
	 * @inheritDoc
	 */
	public String getIdentifier()
	{
		return m_identifier;
	}

	/**
	 * @inheritDoc
	 */
	public String getPassword()
	{
		return m_password;
	}
}
