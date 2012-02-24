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

package org.sakaiproject.entity.api;

/**
 * <p>
 * EntityNotDefinedException is thrown whenever an Entity access is attempted with an id that is not found.
 * </p>
 */
public class EntityNotDefinedException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String m_id = null;

	public EntityNotDefinedException(String id)
	{
		m_id = id;

	}

	public EntityNotDefinedException(String id, Throwable cause)
	{
		m_id = id;
		super.initCause(cause);

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
