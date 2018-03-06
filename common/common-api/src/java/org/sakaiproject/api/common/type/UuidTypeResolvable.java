/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.api.common.type;

/**
 * @author <a href="mailto:lance@indiana.edu">Lance Speelmon</a>
 */
public interface UuidTypeResolvable
{
	/**
	 * Provides an abstract way to retrieve objects from a Manager given only the object's UUID and Type.
	 * 
	 * @param uuid
	 *        The universally unique identifier of the Object.
	 * @param type
	 *        The Type of the Object.
	 * @return
	 * @throws UnsupportedTypeException
	 *         If the Manager does not support the passed Type.
	 */
	public Object getObject(String uuid, Type type) throws UnsupportedTypeException;
}
