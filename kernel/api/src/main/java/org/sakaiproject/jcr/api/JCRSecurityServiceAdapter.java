/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
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

package org.sakaiproject.jcr.api;

/**
 * @author ieb
 */
public interface JCRSecurityServiceAdapter
{

	public static final String JCR_ADD = "jcr.add";

	public static final String JCR_GET = "jcr.get";

	public static final String JCR_REMOVE = "jcr.remove";

	public static final String JCR_UPDATE = "jcr.update";

	/**
	 * @param internalPath
	 * @param jcrPath
	 * @return
	 */
	boolean allowUpdate(String userId, String jcrPath);

	/**
	 * @param internalPath
	 * @return
	 */
	boolean allowAdd(String userId, String jcrPath);

	/**
	 * @param internalPath
	 * @return
	 */
	String getSakaiRealm(String jcrPath);

	/**
	 * @param internalPath
	 * @return
	 */
	boolean allowRemove(String userId, String jcrPath);

	/**
	 * @param internalPath
	 * @return
	 */
	boolean allowGet(String userId, String jcrPath);

	/**
	 * 
	 */
	void beginSecureCalls();

	/**
	 * 
	 */
	void endSecureStackCalls();

	/**
	 * @param o
	 */
	void addSecurityConverter(JCRSecurityConverter o);

	/**
	 * @param o
	 */
	void removeSecurityConverter(JCRSecurityConverter o);

	/**
	 * @param workspace
	 * @return
	 */
	boolean canAccessWorkspace(String userId, String jcrPath);

}
