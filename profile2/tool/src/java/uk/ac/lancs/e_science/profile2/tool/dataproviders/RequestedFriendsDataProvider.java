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
 * RequestedFriendsDataProvider.java
 * Steve Swinsburg
 * s.swinsburg@lancaster.ac.uk
 * January 2009
 * 
 * This implementation of Wicket's IDataProvider gets a list of friend requests incoming to userX
 * 
 * All requests are returned, whether or not their profile can be linked needs to be tested in the UI.
 * This is because they ALL need to be displayed.
 * Perhaps we should create a Friend or SearchResult object and store the data so we don't need to test in the UI?
 * 
 */


public class RequestedFriendsDataProvider implements IDataProvider, Serializable {
	
	private static final long serialVersionUID = 1L;
	private transient List<String> requests = new ArrayList<String>();
	private transient Profile profile;
	
	public RequestedFriendsDataProvider(final String userX) {
		
		//get Profile
		profile = ProfileApplication.get().getProfile();
		
		//get list of requests incoming to userX
		requests = profile.getFriendRequestsForUser(userX);

	}

	public Iterator<String> iterator(int first, int count) {
		try {
			List<String> slice = requests.subList(first, first + count);
			return slice.iterator();
		}
		catch (Exception e) {
			e.printStackTrace();
			return Collections.EMPTY_LIST.iterator();
		}
	}

    public int size() {
    	if (requests == null) {
			return 0;
    	}
		return requests.size();
	}

    public IModel model(Object object) {
            return new Model((String)object);
    }
    
    public void detach() {}
	
}


