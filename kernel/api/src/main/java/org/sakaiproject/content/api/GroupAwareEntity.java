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

package org.sakaiproject.content.api;

import java.time.Instant;
import java.util.Collection;
import java.util.Set;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.time.api.Time;

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
	 * @return A Collection of references to Group objects defined for this entity; empty if none are defined.
	 */
	public Collection getGroups();

	/**
	 * Access the groups, as Group objects, defined for this entity.
	 * 
	 * @return A Collection (Group) of group objects defined for this entity; empty if none are defined.
	 */
	public Collection getGroupObjects();
	
	/**
	 * Access the access mode defined locally for the entity.
	 * 
	 * @return The access mode for the entity.
	 */
	public AccessMode getAccess();
	
	/**
	 * Access the groups inherited by this entity.
	 * 
	 * @return A Collection of Group objects defined locally for this entity; empty if none are defined.
	 */
	public Collection getInheritedGroups();

	/**
	 * Gets a list of roles defined against the underlying entity.
	 */
	public Set<String> getRoleAccessIds();

	/**
	 * Gets a list of roles defined against all parent entities.
	 */
	public Set<String> getInheritedRoleAccessIds();

	/**
	 * Access the groups, as Group objects, inherited by this entity.
	 * 
	 * @return A Collection (Group) of group objects defined for this entity; empty if none are defined.
	 */
	public Collection getInheritedGroupObjects();
	
	/**
	 * Access the actual access mode used to compute who has access to the entity
	 * (usually SITE or GROUPED, but not INHERITED).  This may be defined locally 
	 * or inherited from elsewhere for the entity.  If the local access mode is 
	 * INHERITED, the actual access mode must be retrieved from elsewhere. 
	 * 
	 * @return The actual access mode for the entity.
	 */
	public AccessMode getInheritedAccess();
	
	/**
	 * Access the release date before which this entity should not be available to users 
	 * except those with adequate permission (what defines "adequate permission" is TBD).
	 * @return The date/time at which the entity may be accessed by all users.
	 * @deprecated see {@link #getReleaseInstant()}
	 */
	@Deprecated
	public Time getReleaseDate();
	
	/**
	 * Access the release date before which this entity should not be available to users 
	 * except those with adequate permission (what defines "adequate permission" is TBD).
	 * @return The Instant at which the entity may be accessed by all users.
	 */
	public Instant getReleaseInstant();
	
	/**
	 * Access the retract date after which this entity should not be available to users 
	 * except those with adequate permission (what defines "adequate permission" is TBD).
	 * @return The date/time at which access to the entity should be restricted.
	 * @deprecated see {@link #getRetractInstant()}
	 */
	@Deprecated
	public Time getRetractDate();
	
	/**
	 * Access the retract date after which this entity should not be available to users 
	 * except those with adequate permission (what defines "adequate permission" is TBD).
	 * @return The Instant at which access to the entity should be restricted.
	 */
	public Instant getRetractInstant();
	
	/** 
	 * Access the raw availability setting for this entity: is it set to "hide" or "show".
	 * Does not take into account the release or retract dates.
	 * @return
	 */
	public boolean isHidden();
	
	/**
	 * Calculate the avilability based on whether the item is hidden and (if not) whether
	 * it has a releaseDate or retractDate and (if so) neither of them restrict availabity
	 * right now.
	 * @return true if the entity is available now, false otherwise.
	 */
	public boolean isAvailable();
	
	/**
	 * <p>
	 * AccessMode enumerates different access modes for the entity: site-wide or grouped.
	 * </p>
	 */
	public class AccessMode
	{
		protected final String m_id;

		private AccessMode(String id)
		{
			m_id = id;
		}

		public String toString()
		{
			return m_id;
		}
		
		public boolean equals(Object obj)
		{
			boolean rv = false;
			
			if(obj instanceof AccessMode)
			{
				rv = ((AccessMode) obj).toString().equals(this.toString());
			}
			
			return rv;
		}

		/**
		 * Objects that are equal must have the same hashcode
		 */
		public int hashCode() {
			return this.toString().hashCode();
		}

		static public AccessMode fromString(String access)
		{
			if (SITE.m_id.equals(access)) return SITE;
			if (GROUPED.m_id.equals(access)) return GROUPED;
			if (INHERITED.m_id.equals(access)) return INHERITED;
			//if (PUBLIC.m_id.equals(access)) return PUBLIC;
			return null;
		}

		/** public access to the entity */
		//public static final AccessMode PUBLIC = new AccessMode("public");

		/** channel (site) level access to the entity */
		public static final AccessMode SITE = new AccessMode("site");

		/** grouped access; only members of the getGroup() groups (authorization groups) have access */
		public static final AccessMode GROUPED = new AccessMode("grouped");

		/** inherited access; must look up a hierarchy to determine actual access */
		public static final AccessMode INHERITED = new AccessMode("inherited");

	}
}
