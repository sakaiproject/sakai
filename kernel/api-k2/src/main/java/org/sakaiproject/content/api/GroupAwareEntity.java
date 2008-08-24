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
 *       http://www.osedu.org/licenses/ECL-2.0
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

import org.sakaiproject.content.api.exception.InconsistentException;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.time.api.Time;

/**
 * <p>
 * GroupAwareEntity is an interface that must be implemented for entity types that are to be group aware.
 * </p>
 */
public interface GroupAwareEntity extends Entity
{
	
	/**
	 * <p>
	 * AccessMode enumerates different access modes for the entity: site-wide or grouped.
	 * </p>
	 */
	public static enum AccessMode
	{
		/** channel (site) level access to the entity */
		SITE("site"),

		/** grouped access; only members of the getGroup() groups (authorization groups) have access */
		GROUPED("grouped"),

		/** inherited access; must look up a hierarchy to determine actual access */
		INHERITED("inherited");

		protected final String m_id;

		private AccessMode(String id)
		{
			m_id = id;
		}

		public String toString()
		{
			return m_id;
		}
	}
	

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
	 */
	public Time getReleaseDate();
	
	/**
	 * Access the retract date after which this entity should not be available to users 
	 * except those with adequate permission (what defines "adequate permission" is TBD).
	 * @return The date/time at which access to the entity should be restricted.
	 */
	public Time getRetractDate();
	
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
	 * 
	 * @throws InconsistentException
	 * @throws PermissionException
	 */
	public void clearGroupAccess() throws InconsistentException, PermissionException;
	
	/**
	 * 
	 * @param groups The collection (String) of reference-strings identifying the groups to be added.
	 * @throws InconsistentException
	 * @throws PermissionException
	 */
	public void setGroupAccess(Collection groups) throws InconsistentException, PermissionException;

	/**
	 * 
	 * @throws InconsistentException
	 * @throws PermissionException
	 */
	public void setPublicAccess() throws InconsistentException, PermissionException;
	
	/**
	 * 
	 * @throws InconsistentException
	 * @throws PermissionException
	 */
	public void clearPublicAccess() throws InconsistentException, PermissionException;

	/**
	 * Set the release date before which this entity should not be available to users 
	 * except those with adequate permission (what defines "adequate permission" is TBD).
	 * @param time The date/time at which the entity may be accessed by all users.
	 */
	public void setReleaseDate(Time time);
	
	/**
	 * Set the retract date after which this entity should not be available to users 
	 * except those with adequate permission (what defines "adequate permission" is TBD).
	 * @param time The date/time at which access to the entity should be restricted.
	 */
	public void setRetractDate(Time time);
	
	/**
	 * Make this entity hidden. Any values previously set for releaseDate and/or retractDate 
	 * are removed.
	 */
	public void setHidden();

	/**
	 * Set all of the attributes that determine availability.  If hidden is true, releaseDate  
	 * and retractDate are ignored, and those attributes are set to null.  If hidden is false, 
	 * releaseDate and/or retractDate may null, indicating that releaseDate and/or retractDate
	 * should not be considered in calculating availability.  If hidden is false and a value
	 * is given for releaseDate, that should be saved to represent the time at which the item
	 * becomes available. If hidden is false and a value is given for retractDate, that should 
	 * be saved to represent the time at which the item is no longer available.
	 * @param hidden
	 * @param releaseDate
	 * @param retractDate
	 */
	public void setAvailability(boolean hidden, Time releaseDate, Time retractDate);

	/**
	 * Set the "type" of this ContentEntity as determined by the ResourceType registration
	 * that was used to create it.
	 * @param string
	 */
	public void setResourceType(String string);

}
