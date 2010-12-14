/**
 * Copyright (c) 2008-2010 The Sakai Foundation
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

/**
 * Logic interface for the Profile2 wall.
 */
public interface ProfileWallLogic {
	
	/**
	 * Returns the wall items for the specified user. The privacy record will
	 * be looked up.
	 * 
	 * @param userUuid the user to query by.
	 * @return the wall items for the specified user.
	 */
	public List<WallItem> getWallItems(String userUuid);
	
	/**
	 * Returns the wall items for the specified user. Privacy settings are used
	 * to determine if the user is allowed to access the requested wall items.
	 * 
	 * @param userUuid the user to query by.
	 * @param privacy the privacy record for the user.
	 * @return the wall items for the specified user.
	 */
	public List<WallItem> getWallItems(String userUuid, ProfilePrivacy privacy);
	
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
