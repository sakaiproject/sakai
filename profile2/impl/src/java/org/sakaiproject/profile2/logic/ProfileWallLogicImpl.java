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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.profile2.model.Person;
import org.sakaiproject.profile2.model.ProfilePrivacy;
import org.sakaiproject.profile2.model.ProfileStatus;
import org.sakaiproject.profile2.model.WallItem;

public class ProfileWallLogicImpl implements ProfileWallLogic {

	
	/**
 	 * {@inheritDoc}
 	 */
	public List<WallItem> getWallItems(String userUuid, ProfilePrivacy privacy) {

		if (null == userUuid) {
			throw new IllegalArgumentException("must provide user id");
		}

		final String currentUserUuid = sakaiProxy.getCurrentUserId();
		if (null == currentUserUuid) {
			throw new SecurityException(
					"You must be logged in to make a request for a user's wall items.");
		}

		List<WallItem> wallItems = new ArrayList<WallItem>();

		if (null == privacy) {
			return wallItems;
		}

		if (false == StringUtils.equals(userUuid, currentUserUuid)) {

			if (false == privacyLogic.isUserXWallVisibleByUserY(userUuid,
					privacy, currentUserUuid, connectionsLogic
							.isUserXFriendOfUserY(userUuid, currentUserUuid))) {
				return wallItems;
			}
		}

		// TODO at the moment we only return profile statuses of connections,
		// but it's planned to add more things to the wall in the future.

		List<Person> connections = connectionsLogic
				.getConnectionsForUser(userUuid);

		if (null == connections || 0 == connections.size()) {
			return wallItems;
		}

		for (Person connection : connections) {

			ProfileStatus connectionStatus = statusLogic
					.getUserStatus(connection.getUuid());

			if (null == connectionStatus) {
				continue;
			}

			// status privacy check
			final boolean allowedStatus;
			// current user is always allowed to see status of connections
			if (true == StringUtils.equals(userUuid, currentUserUuid)) {
				allowedStatus = true;
			// don't allow friend-of-a-friend	
			} else {
				allowedStatus = privacyLogic.isUserXStatusVisibleByUserY(
						userUuid, privacy, connection.getUuid(),
						connectionsLogic.isUserXFriendOfUserY(userUuid,
								connection.getUuid()));
			}

			if (true == allowedStatus) {
				
				WallItem wallItem = new WallItem();
				wallItem.setCreatorUuid(connection.getUuid());
				wallItem.setCreatorName(connection.getDisplayName());
				wallItem.setDate(connectionStatus.getDateAdded());
				wallItem.setText(connectionStatus.getMessage());

				wallItems.add(wallItem);
			}
		}

		// wall items are comparable and need to be in order
		Collections.sort(wallItems);
		return wallItems;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<WallItem> getWallItems(String userUuid) {
		return getWallItems(userUuid, privacyLogic
				.getPrivacyRecordForUser(userUuid));
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int getWallItemsCount(String userUuid) {
		return getWallItemsCount(userUuid, privacyLogic
				.getPrivacyRecordForUser(userUuid));
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int getWallItemsCount(String userUuid, ProfilePrivacy privacy) {

		final String currentUserUuid = sakaiProxy.getCurrentUserId();
		if (null == sakaiProxy.getCurrentUserId()) {
			throw new SecurityException(
					"You must be logged in to make a request for a user's wall items.");
		}

		if (null == privacy) {
			return 0;
		}

		if (false == StringUtils.equals(userUuid, currentUserUuid)) {

			if (false == privacyLogic.isUserXWallVisibleByUserY(userUuid,
					privacy, currentUserUuid, connectionsLogic
							.isUserXFriendOfUserY(userUuid, currentUserUuid))) {
				return 0;
			}
		}

		// connection statuses
		List<Person> connections = connectionsLogic
				.getConnectionsForUser(userUuid);
		
		if (null == connections || 0 == connections.size()) {
			return 0;
		}

		int count = 0;
		
		for (Person connection : connections) {
			
			if (null != statusLogic.getUserStatus(connection.getUuid())) {
				
				// current user is always allowed to see status of connections
				if (true == StringUtils.equals(userUuid, currentUserUuid)) {
					count++;
				// don't allow friend-of-a-friend
				} else if (true == privacyLogic.isUserXStatusVisibleByUserY(
						userUuid, privacy, connection.getUuid(),
						connectionsLogic.isUserXFriendOfUserY(userUuid,
								connection.getUuid()))) {
					count++;
				}
			}
		}

		return count;
	}
	
	private ProfilePrivacyLogic privacyLogic;
	public void setPrivacyLogic(ProfilePrivacyLogic privacyLogic) {
		this.privacyLogic = privacyLogic;
	}
	
	private ProfileConnectionsLogic connectionsLogic;
	public void setConnectionsLogic(
			ProfileConnectionsLogic connectionsLogic) {
		this.connectionsLogic = connectionsLogic;
	}
	
	private ProfileStatusLogic statusLogic;
	public void setStatusLogic(ProfileStatusLogic statusLogic) {
		this.statusLogic = statusLogic;
	}

	private SakaiProxy sakaiProxy;
	public void setSakaiProxy(SakaiProxy sakaiProxy) {
		this.sakaiProxy = sakaiProxy;
	}
}
