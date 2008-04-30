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
 * GroupFullException is thrown whenever an attempt is made to join an Authz group, but doing so would exceed the specified size limit.
 * </p>
 */
public class GroupFullException extends RuntimeException
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6848759607283545843L;
	private String m_id = null;

	public GroupFullException(String id)
	{
		m_id = id;
	}

	public String getId()
	{
		return m_id;
	}

	public String toString()
	{
		return super.toString() + " id=" + m_id;
	}
}
