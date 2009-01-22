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
import uk.ac.lancs.e_science.profile2.hbm.Friend;

public class FriendDataProvider implements IDataProvider, Serializable {
	
	private static final long serialVersionUID = 1L;
	private transient List<Friend> friends = new ArrayList<Friend>();
	
	public FriendDataProvider(final String ownerUserId, final String viewingUserId, Profile profile) {
		
		//get friends for user (6)
		friends = profile.getFriendsForUser(ownerUserId, 6);
		
		//we need to make sure the friends returned here are allowed to be visible to viewingUserId
		//so we should get all, randomly, then test and see
		
		
	}
	
	public Iterator<Friend> iterator(int first, int count) {
		try {
			List<Friend> slice = friends.subList(first, first + count);
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


