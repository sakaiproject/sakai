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

package org.sakaiproject.profile2.tool.dataproviders;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.profile2.logic.ProfileConnectionsLogic;
import org.sakaiproject.profile2.model.Person;
import org.sakaiproject.profile2.tool.models.DetachablePersonModel;
import org.sakaiproject.profile2.util.ProfileConstants;

/**
 * FriendsFeedDataProvider.java
 * Steve Swinsburg
 * s.swinsburg@lancaster.ac.uk
 * January 2009
 * 
 * This implementation of Wicket's IDataProvider gets a list of friends of userX as Person objects
 */


public class FriendsFeedDataProvider implements IDataProvider<Person>, Serializable {
	
	private static final long serialVersionUID = 1L;
	private String userUuid;
	private int subListSize = 0;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileConnectionsLogic")
	private ProfileConnectionsLogic connectionsLogic;
	
	public FriendsFeedDataProvider(final String userUuid) {
		
		//inject
		InjectorHolder.getInjector().inject(this);
		
		//set userUuid
		this.userUuid = userUuid;
		
		//get all friends of userX visible by userY
		List<Person> allFriends = connectionsLogic.getConnectionsForUser(userUuid);
		
		//randomise this list
		Collections.shuffle(allFriends);
		
		//make a subset (but make sure the sublist is not too big for the actual list size)
		int allFriendsSize = allFriends.size();
		subListSize = ProfileConstants.MAX_FRIENDS_FEED_ITEMS;
		
		if(allFriendsSize < subListSize) {
			subListSize = allFriendsSize;
		}
		
		//get final list
		allFriends.subList(0, subListSize);
	}
	
	
	public Iterator<Person> iterator(int first, int count) {
		try {
			List<Person> slice = connectionsLogic.getConnectionsForUser(userUuid).subList(first, first + count);
			return slice.iterator();
		}
		catch (Exception e) {
			e.printStackTrace();
			return Collections.EMPTY_LIST.iterator();
		}
	}

    public int size() {
    	return subListSize;
	}

    public IModel<Person> model(Person object) {
    	return new DetachablePersonModel(object);
	}
    
    public void detach() {}
	
}


