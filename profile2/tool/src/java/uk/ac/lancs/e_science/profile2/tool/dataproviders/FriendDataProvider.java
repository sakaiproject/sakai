package uk.ac.lancs.e_science.profile2.tool.dataproviders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import uk.ac.lancs.e_science.profile2.api.Profile;
import uk.ac.lancs.e_science.profile2.hbm.Friend;

public class FriendDataProvider implements IDataProvider {
	
	private transient List<Friend> friends = new ArrayList<Friend>();
	
	public FriendDataProvider(final String userId, Profile profile) {
		
		//get friends for user (6)
		friends = profile.getFriendsForUser(userId, 6);
		
	}
	
	
	
	
	public Iterator iterator(int first, int count) {
		try {
			List slice = friends.subList(first, first + count);
			return slice.iterator();
		}
		catch (Exception e)
		{
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
            return new Model((Friend)object);
    }
    
    public void detach() {}
}


