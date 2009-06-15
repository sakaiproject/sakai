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
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.tool.ProfileApplication;
import org.sakaiproject.profile2.tool.models.DetachableStringModel;

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
	private transient List<String> friends = new ArrayList<String>();
	private transient ProfileLogic profileLogic;
	private String userId;
	
	public ConfirmedFriendsDataProvider(final String userId) {
		
		//set userId
		this.userId = userId;
		
		//get Profile
		profileLogic = ProfileApplication.get().getProfileLogic();
		
		//get list of friends for user
		friends = getFriendsForUser(userId);
		
		//TODO sort list here based on some criteria.
	}
	
	//this is a helper method to process our friends list
	private List<String> getFriendsForUser(final String userId) {
		friends = profileLogic.getConfirmedFriendUserIdsForUser(userId);
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
            return new DetachableStringModel((String)object);
    }
    
    public void detach() {}
	
      
    /* reinit for deserialisation (ie back button) */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		log.debug("ConfirmedFriendsDataProvider has been deserialized.");
		//re-init our transient objects
		profileLogic = ProfileApplication.get().getProfileLogic();
		friends = getFriendsForUser(userId);
	}
}


