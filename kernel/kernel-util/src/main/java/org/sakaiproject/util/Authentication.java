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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.util;

import java.io.Serializable;

/**
 * <p>
 * Authentication is a utility class that implements the Authentication interface.
 * </p>
 */
public class Authentication implements org.sakaiproject.user.api.Authentication, Serializable
{
	private static final long serialVersionUID = 1L;

	/** The UUID identifier string. */
	protected String m_uid = null;

	/** The enterprise identifier string. */
	protected String m_eid = null;

	/**
	 * Construct, with uid and eid
	 * 
	 * @param uid
	 *        The UUID internal end user identifier string.
	 * @param eid
	 *        The enterprise end user identifier string.
	 */
	public Authentication(String uid, String eid)
	{
		m_uid = uid;
		m_eid = eid;
	}

	/**
	 * @inheritDoc
	 */
	public String getUid()
	{
		return m_uid;
	}

	/**
	 * @inheritDoc
	 */
	public String getEid()
	{
		return m_eid;
	}
}
