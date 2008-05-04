/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006 The Sakai Foundation.
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

package org.sakaiproject.authz.api.exception;

/**
 * <p>
 * RoleAlreadyDefinedException is thrown whenever an Authz Role is already defined and there's an attempt to add it again.
 * </p>
 */
public class RoleAlreadyDefinedException extends AuthzGroupException
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7121109155470882697L;
	private String m_id = null;

	public RoleAlreadyDefinedException(String id)
	{
		m_id = id;
	}

	public String toString()
	{
		return super.toString() + " id=" + m_id;
	}
}
