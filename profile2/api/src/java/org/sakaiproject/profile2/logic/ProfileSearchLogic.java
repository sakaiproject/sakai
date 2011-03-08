package org.sakaiproject.profile2.logic;

import java.util.List;

import org.sakaiproject.profile2.model.Person;

/**
 * An interface for dealing with profile searches.
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 * @author Daniel Robinson (d.b.robinson@lancaster.ac.uk)
 */
public interface ProfileSearchLogic {

	/**
	 * Find all users that match the search string in either name or email. 
	 * 
	 * <p>Searches SakaiPerson, UserDirectorySerice internal users as well as external users if your
	 * provider supports SearchExternalUsersUDP.</p>
	 * 
	 * <p>This list is automatically cleaned for non-existent users by way of UserDirectoryService.getUsers.</p>
	 * 
	 * @param search 	string to search for
	 * @return List 	Persons
	 */
	public List<Person> findUsersByNameOrEmail(String search);

	/**
	 * Find all users that match the search string in any of the relevant SakaiPerson fields
	 *
	 * <p>This list is automatically cleaned for non-existent users by way of UserDirectoryService.getUsers.</p>
	 * 
	 * @param search 	string to search for
	 * @return List 	Persons
	 */
	public List<Person> findUsersByInterest(String search, boolean includeBusinessBio);
}
