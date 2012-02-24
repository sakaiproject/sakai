/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 Sakai Foundation
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

import org.sakaiproject.api.common.manager.Persistable;

/**
 * @author <a href="mailto:lance@indiana.edu">Lance Speelmon </a>
 */
public interface Type extends Persistable
{
	/**
	 * 
	 * @return
	 */
	public String getAuthority();

	/**
	 * 
	 * @param authority
	 */
	public void setAuthority(String authority);

	/**
	 * 
	 * @return
	 */
	public String getDomain();

	/**
	 * 
	 * @param domain
	 */
	public void setDomain(String domain);

	
	/**
	 * 
	 * @return
	 */
	public String getKeyword();

	/**
	 * 
	 * @param keyword
	 */
	public void setKeyword(String keyword);

	/**
	 * 
	 * @return
	 */
	public String getDisplayName();

	/**
	 * 
	 * @param displayName
	 */
	public void setDisplayName(String displayName);

	/**
	 * 
	 * @return
	 */
	public String getDescription();

	/**
	 * 
	 * @param description
	 */
	public void setDescription(String description);
}
