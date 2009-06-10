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

/**
 * RequestedFriendsDataProvider.java
 * Steve Swinsburg
 * s.swinsburg@lancaster.ac.uk
 * January 2009
 * 
 * This implementation of Wicket's IDataProvider gets a list of friend requests incoming to userId
 * 
 * All requests are returned, whether or not their profile can be linked needs to be tested in the UI.
 * This is because they ALL need to be displayed.
 * Perhaps we should create a Friend or SearchResult object and store the data so we don't need to test in the UI?
 * 
 */


public class RequestedFriendsDataProvider implements IDataProvider, Serializable {
	
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(RequestedFriendsDataProvider.class); 

	private transient List<String> requests = new ArrayList<String>();
	private transient ProfileLogic profileLogic;
	private String userId;
	
	public RequestedFriendsDataProvider(final String userId) {
		
		//set userId
		this.userId = userId;
		
		//get Profile
		profileLogic = ProfileApplication.get().getProfileLogic();
		
		//get list of requests for user
		requests = getFriendsForUser(userId);
	}
	
	//this is a helper method to process our friends list
	private List<String> getFriendsForUser(final String userId) {
		requests = profileLogic.getFriendRequestsForUser(userId);
		return requests;
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
    
    /* reinit for deserialisation (ie back button) */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		log.debug("RequestedFriendsDataProvider has been deserialized.");
		//re-init our transient objects
		profileLogic = ProfileApplication.get().getProfileLogic();
		requests = getFriendsForUser(userId);
	}
	
}


