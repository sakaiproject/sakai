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
 * GroupAlreadyDefinedException is thrown whenever an Authz Group is created with an id that already is defined for a group.
 * </p>
 */
public class GroupAlreadyDefinedException extends AuthzGroupException
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4310611124585833460L;
	private String m_id = null;

	public GroupAlreadyDefinedException(String id)
	{
		m_id = id;
	}

	public String toString()
	{
		return super.toString() + " id=" + m_id;
	}
}
