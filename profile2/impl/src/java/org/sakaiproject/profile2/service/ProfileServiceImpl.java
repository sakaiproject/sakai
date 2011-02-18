package org.sakaiproject.profile2.service;

import java.util.List;

import org.sakaiproject.profile2.logic.ProfileConnectionsLogic;
import org.sakaiproject.profile2.model.Person;

import org.sakaiproject.profile2.service.ProfileService;

/**
 * A facade on the various logic component methods to improve backwards compatibility with
 * clients of the older Profile2 apis. See PRFL-551.
 * 
 * @author Adrian Fish <a.fish@lancaster.ac.uk>
 */
public class ProfileServiceImpl implements ProfileService {
	
	private ProfileConnectionsLogic connectionsLogic = null;
	public void setConnectionsLogic(ProfileConnectionsLogic connectionsLogic) {
		this.connectionsLogic = connectionsLogic;
	}

	public List<Person> getConnectionsForUser(String userUuid) {
		return connectionsLogic.getConnectionsForUser(userUuid);
	}
}
