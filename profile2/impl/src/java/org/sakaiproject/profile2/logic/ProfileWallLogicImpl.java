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

import org.sakaiproject.profile2.model.Person;
import org.sakaiproject.profile2.model.ProfileStatus;
import org.sakaiproject.profile2.model.WallItem;

public class ProfileWallLogicImpl implements ProfileWallLogic {

	
	@Override
	public List<WallItem> getWallItems(String userUuid) {

		if (null == userUuid) {
			throw new IllegalArgumentException("must provide user id");
		}

		List<WallItem> wallItems = new ArrayList<WallItem>();

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

			WallItem wallItem = new WallItem();
			wallItem.setCreatorUuid(connection.getUuid());
			wallItem.setCreatorName(connection.getDisplayName());
			wallItem.setDate(connectionStatus.getDateAdded());
			wallItem.setText(connectionStatus.getMessage());
			
			wallItems.add(wallItem);
		}

		// wall items are comparable and need to be in order
		Collections.sort(wallItems);
		return wallItems;
	}
	
	public int getWallItemsCount(String userUuid) {

		List<Person> connections = connectionsLogic
				.getConnectionsForUser(userUuid);
		
		int count = 0;
		for (Person connection : connections) {
			if (null != statusLogic.getUserStatus(connection.getUuid())) {
				count++;
			}
		}

		return count;
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

}
