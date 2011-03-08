/**
 * 
 */
package org.sakaiproject.profile2.logic;

import java.util.List;

import org.apache.log4j.Logger;
import org.sakaiproject.profile2.dao.ProfileDao;
import org.sakaiproject.profile2.model.Person;
import org.sakaiproject.profile2.util.ProfileUtils;
import org.sakaiproject.user.api.User;

/**
 * Implementation of ProfileSearchLogic API
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 * @author Daniel Robinson (d.b.robinson@lancaster.ac.uk)
 */
public class ProfileSearchLogicImpl implements ProfileSearchLogic {

	private static final Logger log = Logger.getLogger(ProfileSearchLogicImpl.class);
	
	/**
 	 * {@inheritDoc}
 	 */
	public List<Person> findUsersByNameOrEmail(String search) {
				
		//add users from SakaiPerson (clean list)
		List<String> sakaiPersonUuids = dao.findSakaiPersonsByNameOrEmail(search);
		List<User> users = sakaiProxy.getUsers(sakaiPersonUuids);

		//add local users from UserDirectoryService
		users.addAll(sakaiProxy.searchUsers(search));
		
		//add external users from UserDirectoryService
		users.addAll(sakaiProxy.searchExternalUsers(search));
		
		//remove duplicates
		ProfileUtils.removeDuplicates(users);
		
		log.debug("Found " + users.size() + " results for search: " + search);
		
		//restrict to only return the max number. UI will print message
		int maxResults = sakaiProxy.getMaxSearchResults();
		if(users.size() >= maxResults) {
			users = users.subList(0, maxResults);
		}
		
		//remove invisible
		users = removeInvisibleUsers(users);
		
		return profileLogic.getPersons(users);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public List<Person> findUsersByInterest(String search, boolean includeBusinessBio) {
				
		//add users from SakaiPerson		
		List<String> sakaiPersonUuids = dao.findSakaiPersonsByInterest(search, includeBusinessBio);
		List<User> users = sakaiProxy.getUsers(sakaiPersonUuids);
		
		//restrict to only return the max number. UI will print message
		int maxResults = sakaiProxy.getMaxSearchResults();
		if(users.size() >= maxResults) {
			users = users.subList(0, maxResults);
		}
		
		//remove invisible
		users = removeInvisibleUsers(users);
		
		return profileLogic.getPersons(users);
	}
	
	/**
	 * Remove invisible users from the list
	 * @param users
	 * @return cleaned list
	 */
	private List<User> removeInvisibleUsers(List<User> users){
		
		//if superuser return list unchanged.
		if(sakaiProxy.isSuperUser()){
			return users;
		}
		
		//get list of invisible users as Users
		List<User> invisibleUsers = sakaiProxy.getUsers(sakaiProxy.getInvisibleUsers());
		if(invisibleUsers.isEmpty()) {
			return users;
		}
		
		//remove
		users.removeAll(invisibleUsers);
		
		return users;
	}
	
	private SakaiProxy sakaiProxy;
	public void setSakaiProxy(SakaiProxy sakaiProxy) {
		this.sakaiProxy = sakaiProxy;
	}
	
	private ProfileDao dao;
	public void setDao(ProfileDao dao) {
		this.dao = dao;
	}
	
	private ProfileLogic profileLogic;
	public void setProfileLogic(ProfileLogic profileLogic) {
		this.profileLogic = profileLogic;
	}

}
