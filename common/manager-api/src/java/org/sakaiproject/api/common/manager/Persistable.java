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

package org.sakaiproject.api.common.manager;

import java.util.Date;

/**
 * @author <a href="mailto:lance@indiana.edu">Lance Speelmon</a>
 */
public interface Persistable
{
	/**
	 * All persistent objects must have a UUID.
	 * 
	 * @return Returns the UUID for given object.
	 */
	public String getUuid();

	/**
	 * The last Agent that modified the persistent state of this object.
	 * 
	 * @return UUID of Agent that made last modification.
	 */
	public String getLastModifiedBy();

	/**
	 * The last time this object's persistent state was modified.
	 * 
	 * @return
	 */
	public Date getLastModifiedDate();

	/**
	 * The Agent that created this persistent object.
	 * 
	 * @return UUID of the Agent that created this persistent object.
	 */
	public String getCreatedBy();

	/**
	 * The time and date this persistent object was created.
	 * 
	 * @return
	 */
	public Date getCreatedDate();
}
