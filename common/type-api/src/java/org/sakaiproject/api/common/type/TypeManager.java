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
 * @author <a href="mailto:lance@indiana.edu">Lance Speelmon </a>
 */
public interface TypeManager
{
	/**
	 * Create a new Type.
	 * 
	 * @param authority
	 * @param domain
	 * @param keyword
	 * @param displayName
	 * @param description
	 * @return
	 */
	public Type createType(String authority, String domain, String keyword, String displayName, String description);

	/**
	 * Retrieve Type by uuid.
	 * 
	 * @param uuid
	 * @return
	 */
	public Type getType(String uuid);

	/**
	 * Retrieve a Type by the "tuple".
	 * 
	 * @param authority
	 * @param domain
	 * @param keyword
	 * @return
	 */
	public Type getType(String authority, String domain, String keyword);

	/**
	 * Update persistent state of passed Type.
	 * 
	 * @param type
	 */
	public void saveType(Type type);

	/**
	 * Remove the persistent state of passed Type.
	 * 
	 * @param type
	 * @throws UnsupportedOperationException
	 *         Some (many) implementations will not support deleting Types. Callers need to handle this condition gracefully.
	 */
	public void deleteType(Type type) throws UnsupportedOperationException;
}
