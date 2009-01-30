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
import uk.ac.lancs.e_science.profile2.tool.ProfileApplication;

/*
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
	private transient List<String> friends = new ArrayList<String>();
	private transient Profile profile;
	
	public ConfirmedFriendsDataProvider(final String userX, final String userY) {
		
		//get Profile
		profile = ProfileApplication.get().getProfile();
		
		//if users are the same, they are viewing own friend list so get list of visible friends
		//if users are different, userY is viewing userX's friend list
		if(userX.equals(userY)) {
			friends = profile.getVisibleFriendsOfUser(userX);
		} else {
			friends = profile.getFriendsOfUserXVisibleByUserY(userX, userY);
		}
		
		//TODO sort list here - need comparator etc
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


