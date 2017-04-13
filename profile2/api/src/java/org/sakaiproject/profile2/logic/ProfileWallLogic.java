/**
 * Copyright (c) 2008-2012 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.profile2.logic;

import java.util.List;

import org.sakaiproject.profile2.model.ProfilePrivacy;
import org.sakaiproject.profile2.model.WallItem;
import org.sakaiproject.profile2.model.WallItemComment;

/**
 * Logic interface for the Profile2 wall.
 * 
 * @author d.b.robinson@lancaster.ac.uk
 * @deprecated The wall functionality will be removed from Sakai for the 13 release.
 */
@Deprecated
public interface ProfileWallLogic {
	
	/**
	 * Notifies a user's connections of an event added to the user's wall.
	 * 
	 * Note: the wall logic is currently responsible for timestamping the event,
	 * but we might want to pass the date in this API call instead.
	 * 
	 * @param event the event to add.
	 * @param userUuid the ID of the user that created the event.
	 */
	public void addNewEventToWall(String event, String userUuid);
	
	/**
	 * Notifies a user's connections of a status update added to the user's wall.
	 * 
	 * Note: the wall logic is currently responsible for timestamping the status
	 * update, but we might want to pass the date in this API call instead.
	 * 
	 * @param status the status to add.
	 * @param userUuid the ID of the user whose status we're posting.
	 */
	public void addNewStatusToWall(String status, String userUuid);
	
	/**
	 * Adds a new wall item comment.
	 *  
	 * @param wallItemComment the wall item comment to add.
	 * @return <code>true</code> if the add is successful and
	 *         <code>false</code> if the add fails.
	 */
	public boolean addNewCommentToWallItem(WallItemComment wallItemComment);
	
	/**
	 * Posts the specified wall item to the specified user's wall and the
	 * walls of their connections.
	 * 
	 * @param userUuid the id of the user whose wall we're posting to.
	 * @param wallItem the wall item to post.
	 */
	public boolean postWallItemToWall(String userUuid, WallItem wallItem);
	
	/**
	 * Removes the specified wall item.
	 *
	 * @param wallItem the wall item to remove.
	 */
	public boolean removeWallItemFromWall(WallItem wallItem);

	/**
	 * Returns the specified wall item.
	 *
	 * @param wallItemId the wall item to return.
	 * @return the wall item for the specified id.
	 */
	public WallItem getWallItem(long wallItemId);

	/**
	 * Returns the specified wall item comment.
	 *
	 * @param wallItemCommentId the wall item comment to return.
	 * @return the wall item comment for the specified id.
	 */
	public WallItemComment getWallItemComment(long wallItemCommentId);
	
	/**
	 * Returns the wall for the specified user. The privacy record will
	 * be looked up.
	 *
	 * @param userUuid the user to query by.
	 * @return the wall for the specified user.
	 */
	public List<WallItem> getWallItemsForUser(String userUuid);
	
	/**
	 * Returns the wall for the specified user. Privacy settings are used
	 * to determine if the user is allowed to access the requested wall.
	 *
	 * @param userUuid the user to query by.
	 * @param privacy the privacy record for the user.
	 * @return the wall for the specified user.
	 */
	public List<WallItem> getWallItemsForUser(String userUuid, ProfilePrivacy privacy);
	
	/**
	 * Returns the number of available wall items for the specified user. The
	 * privacy record will be looked up.
	 *
	 * @param userUuid the user to query by.
	 * @return the number of available wall items for the specified user.
	 */
	public int getWallItemsCount(String userUuid);
	
	/**
	 * Returns the number of available wall items for the specified user.
	 * Privacy settings are used to determine if the user is allowed to access
	 * the requested wall items.
	 *
	 * @param userUuid the user to query by.
	 * @param privacy the privacy record for the user.
	 * @return the number of available wall items for the specified user.
	 */
	public int getWallItemsCount(String userUuid, ProfilePrivacy privacy);

}
