package org.sakaiproject.profile2.tool.dataproviders;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.tool.ProfileApplication;
import org.sakaiproject.profile2.util.ProfileConstants;

/**
 * FriendsFeedDataProvider.java
 * Steve Swinsburg
 * s.swinsburg@lancaster.ac.uk
 * January 2009
 * 
 * This implementation of Wicket's IDataProvider gets a list of friends of userX
 * 
 */


public class FriendsFeedDataProvider implements IDataProvider, Serializable {
	
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(FriendsFeedDataProvider.class); 
	private transient ProfileLogic profileLogic;
	private transient List<String> friends = new ArrayList<String>();
	private String userId;
	
	public FriendsFeedDataProvider(final String userId) {
		
		//set userId
		this.userId = userId;
		
		//get Profile
		profileLogic = ProfileApplication.get().getProfileLogic();
		
		//get list of friends
		friends = getFriendsForUser(userId);
	}
	
	//this is a helper method to process our friends list
	private List<String> getFriendsForUser(final String userId) {
		
		List<String> allFriends = new ArrayList<String>();
		
		//get all friends of userX visible by userY
		allFriends = profileLogic.getConfirmedFriendUserIdsForUser(userId);
		
		//randomise this list
		Collections.shuffle(allFriends);
		
		//make a subset (but make sure the sublist is not too big for the actual list size)
		int allFriendsSize = allFriends.size();
		int subListSize = ProfileConstants.MAX_FRIENDS_FEED_ITEMS;
		
		if(allFriendsSize < subListSize) {
			subListSize = allFriendsSize;
		}
		
		friends = allFriends.subList(0, subListSize);
		
		return friends;
		
	}
	
	
	
	
	public Iterator<String> iterator(int first, int count) {
		try {
			List<String> slice = friends.subList(first, first + count);
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
            return new Model((String)object);
    }
    
    public void detach() {}
	
    
    /* reinit for deserialisation (ie back button) */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		log.debug("FriendsFeedDataProvider has been deserialized.");
		//re-init our transient objects
		profileLogic = ProfileApplication.get().getProfileLogic();
		friends = getFriendsForUser(userId);
	}
}


