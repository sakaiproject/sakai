package uk.ac.lancs.e_science.profile2.tool.dataproviders;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import uk.ac.lancs.e_science.profile2.api.Profile;
import uk.ac.lancs.e_science.profile2.api.ProfileFriendsManager;
import uk.ac.lancs.e_science.profile2.tool.ProfileApplication;

/**
 * FriendsFeedDataProvider.java
 * Steve Swinsburg
 * s.swinsburg@lancaster.ac.uk
 * January 2009
 * 
 * This implementation of Wicket's IDataProvider gets a list of friends of userX
 *  * 
 * 
 */


public class FriendsFeedDataProvider implements IDataProvider, Serializable {
	
	private static final long serialVersionUID = 1L;
	private transient List<String> allFriends = new ArrayList<String>();
	private transient List<String> friends = new ArrayList<String>();
	private transient Profile profile;
	
	public FriendsFeedDataProvider(final String userX) {
		
		//get Profile
		profile = ProfileApplication.get().getProfile();
		
		//get all friends of userX visible by userY
		allFriends = profile.getConfirmedFriendUserIdsForUser(userX);
		
		//randomise this list
		Collections.shuffle(allFriends);
		
		//make a subset (but make sure the sublist is not too big for the actual list size)
		int allFriendsSize = allFriends.size();
		int subListSize = ProfileFriendsManager.MAX_FRIENDS_FEED_ITEMS;
		
		if(allFriendsSize < subListSize) {
			subListSize = allFriendsSize;
		}
		
		friends = allFriends.subList(0, subListSize);
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
	
}


