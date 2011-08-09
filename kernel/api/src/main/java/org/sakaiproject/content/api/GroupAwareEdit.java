/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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
import java.util.Date;

import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.time.api.Time;

/**
 * <p>
 * GroupAwareEdit is an interface that must be implemented to make changes in entities of types that are group aware.
 * </p>
 */
public interface GroupAwareEdit extends GroupAwareEntity, Edit
{
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
	 * @deprecated see {{@link #setReleaseTime(Date)
	 */
	public void setReleaseDate(Time time);
	
	/**
	 * Set the release date before which this entity should not be available to users 
	 * except those with adequate permission (what defines "adequate permission" is TBD).
	 * @param time The date/time at which the entity may be accessed by all users.
	 */
	public void setReleaseTime(Date date);
	
	/**
	 * Set the retract date after which this entity should not be available to users 
	 * except those with adequate permission (what defines "adequate permission" is TBD).
	 * @param time The date/time at which access to the entity should be restricted.
	 * @deprecated see {@link #setRetractTime(Date)}
	 */
	public void setRetractDate(Time time);
	
	/**
	 * Set the retract date after which this entity should not be available to users 
	 * except those with adequate permission (what defines "adequate permission" is TBD).
	 * @param time The date/time at which access to the entity should be restricted.
	 */
	public void setRetractTime(Date time);
	
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
