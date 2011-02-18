package org.sakaiproject.profile2.service;

import java.util.List;

import org.sakaiproject.profile2.model.Person;

/**
 * A facade on the various logic component methods to improve backwards compatibility with
 * clients of the older Profile2 apis. See PRFL-551.
 * 
 * @author Adrian Fish <a.fish@lancaster.ac.uk>
 */
public interface ProfileService {
	public abstract List<Person> getConnectionsForUser(final String userUuid);
}
