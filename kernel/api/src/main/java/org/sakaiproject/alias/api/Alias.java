/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
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

package org.sakaiproject.alias.api;

import java.util.Date;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.User;

/**
 * <p>
 * Alias ...
 * </p>
 */
public interface Alias extends Entity, Comparable
{
	/**
	 * @return the user who created this.
	 */
	User getCreatedBy();

	/**
	 * @return the user who last modified this.
	 */
	User getModifiedBy();

	/**
	 * @return the time created.
	 * @deprecated see {@link #getDateCreated()}
	 */
	Time getCreatedTime();

	/**
	 * @return the time last modified.
	 * @deprecated see {@link #getDateModified()}
	 */
	Time getModifiedTime();
	
	/**
	 * Date Alias last modified
	 * @return
	 */
	Date getDateModified();
	
	/**
	 * Date Alias Created
	 * @return
	 */
	Date getDateCreated();

	// TODO:

	/**
	 * Access the alias target.
	 * 
	 * @return The alias target.
	 */
	String getTarget();

	/**
	 * @return a description of the item this alias's target applies to.
	 */
	String getDescription();
}
