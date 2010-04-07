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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.model.Person;
import org.sakaiproject.profile2.tool.models.DetachablePersonModel;

/**
 * ConfirmedFriendsDataProvider.java
 * Steve Swinsburg
 * s.swinsburg@lancaster.ac.uk
 * January 2009
 * 
 * This implementation of Wicket's IDataProvider gets a list of confirmed friends 
 * of userX, visible by userY.
 * 
 * This is used by MyFriends where a list of confirmed friends is required, and
 * in this case, both userX and userY will be the same so it calls a different method to get the friends list
 * (only requires userX)
 * 
 * It is also used in ViewFriends where userX is the owner of the page and userY is
 * the person viewing the page.
 * 
 * 
 */


public class ConfirmedFriendsDataProvider implements IDataProvider, Serializable {
	
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(ConfirmedFriendsDataProvider.class); 
	private transient List<Person> friends = new ArrayList<Person>();
	private String userId;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileLogic")
	private ProfileLogic profileLogic;
	
	public ConfirmedFriendsDataProvider(final String userId) {
		
		//inject
		InjectorHolder.getInjector().inject(this);
		
		//set userId
		this.userId = userId;
		
		//get list of friends for user
		friends = getFriendsForUser(userId);
		
		//sort list based on natural sort of Person model
		Collections.sort(friends);
	}
	
	//this is a helper method to process our friends list
	private List<Person> getFriendsForUser(final String userId) {
		friends = profileLogic.getConnectionsForUser(userId);
		return friends;
	}

	public Iterator<Person> iterator(int first, int count) {
		try {
			List<Person> slice = friends.subList(first, first + count);
			return slice.iterator();
		}
		catch (Exception e) {
			e.printStackTrace();
			return Collections.EMPTY_LIST.iterator();
		}
	}

    public int size() {
    	if (friends == null) {
			return 0;
    	}
		return friends.size();
	}

    public IModel model(Object object) {
            return new DetachablePersonModel((Person)object);
    }
    
    public void detach() {}
	
      
    /* reinit for deserialisation (ie back button) */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		log.debug("ConfirmedFriendsDataProvider has been deserialized.");
		//re-init our transient objects
		friends = getFriendsForUser(userId);
	}
    
    
	
}


