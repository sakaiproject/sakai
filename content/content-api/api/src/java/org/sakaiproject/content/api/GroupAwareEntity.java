/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.content.api;

import java.util.Collection;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Group;

/**
 * <p>
 * GroupAwareEntity is an interface that must be implemented for entity types that are to be group aware.
 * </p>
 */
public interface GroupAwareEntity extends Entity
{
	/**
	 * Access the groups defined for this entity.
	 * 
	 * @return A Collection of Group objects defined for this entity; empty if none are defined.
	 */
	Collection getGroups();

	/**
	 * Access the access mode for the entity - how we compute who has access to the entity.
	 * 
	 * @return The access mode for the Assignment.
	 */
	AccessMode getAccess();
	
	/**
	 * <p>
	 * AccessMode enumerates different access modes for the entity: site-wide or grouped.
	 * </p>
	 */
	public class AccessMode
	{
		private final String m_id;

		public AccessMode(String id)
		{
			m_id = id;
		}

		public String toString()
		{
			return m_id;
		}

		static public AccessMode fromString(String access)
		{
			if (SITE.m_id.equals(access)) return SITE;
			if (GROUPED.m_id.equals(access)) return GROUPED;
			return null;
		}

		/** channel (site) level access to the message */
		public static final AccessMode SITE = new AccessMode("site");

		/** grouped access; only members of the getGroup() groups (authorization groups) have access */
		public static final AccessMode GROUPED = new AccessMode("grouped");
	}
}
