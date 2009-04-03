/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.assignment.api;

import java.util.Collection;

import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.User;

/**
 * <p>
 * Assignment is an interface for the Sakai assignments module. It represents a specific assignment (as for a specific section or class).
 * </p>
 */
public interface AssignmentEdit extends Assignment, Edit
{
	/**
	 * Set the reference of the AssignmentContent of this Assignment.
	 * 
	 * @param String -
	 *        the reference of the AssignmentContent.
	 */
	public void setContentReference(String contentReference);

	/**
	 * Set the AssignmentContent of this Assignment.
	 * 
	 * @param content -
	 *        the Assignment's AssignmentContent.
	 */
	public void setContent(AssignmentContent content);

	/**
	 * Set the first time at which the assignment can be viewed; may be null.
	 * 
	 * @param openTime -
	 *        The Time at which the Assignment opens.
	 */
	public void setOpenTime(Time openTime);

	/**
	 * Set the time at which the assignment is due; may be null.
	 * 
	 * @param dueTime -
	 *        The Time at which the Assignment is due.
	 */
	public void setDueTime(Time dueTime);

	/**
	 * Set the drop dead time after which responses to this assignment are considered late; may be null.
	 * 
	 * @param dropDeadTime -
	 *        The Time object representing the drop dead time.
	 */
	public void setDropDeadTime(Time dropDeadTime);

	/**
	 * Set the time after which this assignment can no longer be viewed, and after which submissions will not be accepted. May be null.
	 * 
	 * @param closeTime -
	 *        The Time after which the Assignment is closed, or null if unspecified.
	 */
	public void setCloseTime(Time closeTime);

	/**
	 * Set the section info
	 * 
	 * @param sectionId -
	 *        The section id.
	 */
	public void setSection(String sectionId);

	/**
	 * Set the Assignment's context at the time of creation.
	 * 
	 * @param context -
	 *        The context string.
	 */
	public void setContext(String context);

	/**
	 * Set whether this is a draft or final copy.
	 * 
	 * @param draft -
	 *        true if this is a draft, false if it is a final copy.
	 */
	public void setDraft(boolean draft);

	/**
	 * Add an author to the author list.
	 * 
	 * @param author -
	 *        The User to add to the author list.
	 */
	public void addAuthor(User author);

	/**
	 * Remove an author from the author list.
	 * 
	 * @param author -
	 *        the User to remove from the author list.
	 */
	public void removeAuthor(User author);

	/**
	 * Set the title.
	 * 
	 * @param title -
	 *        The Assignment's title.
	 */
	public void setTitle(String title);

	/**
	 * Set these as the message's groups, replacing the access and groups already defined.
	 * 
	 * @param Collection
	 *        groups The colelction of Group objects to use for this message.
	 * @throws PermissionException
	 *         if the end user does not have permission to remove from the groups that would be removed or add to the groups that would be added.
	 */
	void setGroupAccess(Collection groups) throws PermissionException;

	/**
	 * Remove any grouping for this message; the access mode reverts to channel and any groups are removed.
	 * 
	 * @throws PermissionException
	 *         if the end user does not have permission to do this.
	 */
	void clearGroupAccess() throws PermissionException;

	/**
	 * Set the access mode for the assignment - how we compute who has access to the assignment.
	 * 
	 * @param access
	 *        The AssignmentAccess access mode for the message.
	 */
	void setAccess(AssignmentAccess access);
	
    /**
	 * Set the position order field for the assignment.
	 *
	 * @param position_order -
	 *        The Assignment's order.
	 */
	public void setPosition_order(int position_order);
}
