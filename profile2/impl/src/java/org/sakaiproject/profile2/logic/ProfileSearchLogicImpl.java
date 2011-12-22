/**
 * 
 */
package org.sakaiproject.profile2.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Setter;

import org.apache.log4j.Logger;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.profile2.cache.CacheManager;
import org.sakaiproject.profile2.dao.ProfileDao;
import org.sakaiproject.profile2.model.BasicConnection;
import org.sakaiproject.profile2.model.Person;
import org.sakaiproject.profile2.model.ProfileSearchTerm;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.profile2.util.ProfileUtils;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.user.api.User;

/**
 * Implementation of ProfileSearchLogic API
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 * @author Daniel Robinson (d.b.robinson@lancaster.ac.uk)
 */
public class ProfileSearchLogicImpl implements ProfileSearchLogic {

	private static final Logger log = Logger.getLogger(ProfileSearchLogicImpl.class);
	
	private Cache cache;
	private final String CACHE_NAME = "org.sakaiproject.profile2.cache.search";
	
	/**
 	 * {@inheritDoc}
 	 */
	public List<Person> findUsersByNameOrEmail(String search, boolean includeConnections, String worksiteId) {
				
		//add users from SakaiPerson (clean list)
		List<String> sakaiPersonUuids = dao.findSakaiPersonsByNameOrEmail(search);	
		List<User> users = sakaiProxy.getUsers(sakaiPersonUuids);

		//add local users from UserDirectoryService
		users.addAll(sakaiProxy.searchUsers(search));
		
		//add external users from UserDirectoryService
		users.addAll(sakaiProxy.searchExternalUsers(search));
		
		//remove duplicates
		ProfileUtils.removeDuplicates(users);
		
		//remove connections if requested
		if (false == includeConnections) {
			removeConnectionsFromUsers(users);
		}
		
		//if worksite id is specified
		if (null != worksiteId) {
			//remove any matches that are not in specified worksite
			users = removeNonWorksiteMembersFromUsers(users, worksiteId);
		}
		
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
	public List<Person> findUsersByInterest(String search, boolean includeConnections, String worksiteId) {
				
		//add users from SakaiPerson		
		List<String> sakaiPersonUuids = dao.findSakaiPersonsByInterest(search, sakaiProxy.isBusinessProfileEnabled());
		
		//remove connections if requested
		if (false == includeConnections) {
			removeConnectionsFromUserIds(sakaiPersonUuids);
		}
		
		//if worksite id is specified
		if (null != worksiteId) {
			//remove any matches that are not in specified worksite
			sakaiPersonUuids = removeNonWorksiteMembersFromUserIds(sakaiPersonUuids, worksiteId);
		}
		
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
 	 * {@inheritDoc}
 	 */
	public ProfileSearchTerm getLastSearchTerm(String userUuid) {
		
		List<ProfileSearchTerm> searchHistory = getSearchHistory(userUuid);
		if (null != searchHistory && searchHistory.size() > 0) {
			return searchHistory.get(searchHistory.size() - 1);
		}
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<ProfileSearchTerm> getSearchHistory(String userUuid) {

		if (cache.containsKey(userUuid)) {

			List<ProfileSearchTerm> searchHistory = new ArrayList<ProfileSearchTerm>(
					((Map<String, ProfileSearchTerm>) cache.get(userUuid))
							.values());

			Collections.sort(searchHistory);

			return searchHistory;
		} else {
			return null;
		}
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public void addSearchTermToHistory(String userUuid, ProfileSearchTerm searchTerm) {
		
		if (null == searchTerm) {
			throw new IllegalArgumentException("search term cannot be null");
		}
		
		if (null == searchTerm.getUserUuid()) {
			throw new IllegalArgumentException("search term must contain UUID of user");
		}
		
		if (false == userUuid.equals(searchTerm.getUserUuid())) {
			throw new IllegalArgumentException("userUuid must match search term userUuid");
		}
		
		Map<String, ProfileSearchTerm> searchHistory;
		if (false == cache.containsKey(searchTerm.getUserUuid())) {
			searchHistory = new HashMap<String, ProfileSearchTerm>();
		} else {
			searchHistory = (HashMap<String, ProfileSearchTerm>) cache.get(searchTerm.getUserUuid());
		}
		
		// if search term already in history, remove old one (do BEFORE checking size)
		searchHistory.remove(searchTerm.getSearchTerm());
		
		if (searchHistory.size() == ProfileConstants.DEFAULT_MAX_SEARCH_HISTORY) {
			searchHistory.remove(getSearchHistory(userUuid).get(0).getSearchTerm());
		}
		
		// then add
		searchHistory.put(searchTerm.getSearchTerm(), searchTerm);
		
		cache.put(searchTerm.getUserUuid(), searchHistory);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public void clearSearchHistory(String userUuid) {
		
		if (cache.containsKey(userUuid)) {
			cache.remove(userUuid);
		} else {
			log.warn("unable to clear search history; uuid not found: " + userUuid);
		}
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
	
	/**
	 * Remove any connections from list of users.
	 * 
	 * @param users
	 */
	private void removeConnectionsFromUsers(List<User> users) {

		List<BasicConnection> connections = connectionsLogic.getBasicConnectionsForUser(sakaiProxy.getCurrentUserId());
		for (BasicConnection connection : connections) {
			for (User user : users) {
				if (user.getId().equals(connection.getUuid())) {
					users.remove(user);
					break;
				}
			}

		}
	}
	
	/**
	 * Remove any connections from list of user ids.
	 * 
	 * @param userIds
	 */
	private void removeConnectionsFromUserIds(List<String> userIds) {

		List<BasicConnection> connections = connectionsLogic.getBasicConnectionsForUser(sakaiProxy.getCurrentUserId());
		for (BasicConnection connection : connections) {
			if (userIds.contains(connection.getUuid())) {
				userIds.remove(connection.getUuid());
			}

		}
	}
	
	/**
	 * Remove any non-worksite members from list of users.
	 * 
	 * @param users
	 * @param worksiteId
	 * @return a list of matching worksite member users.
	 */
	private List<User> removeNonWorksiteMembersFromUsers(List<User> users, String worksiteId) {
		
		List<User> worksiteMembers = new ArrayList<User>();
		
		Site site = sakaiProxy.getSite(worksiteId);
		if (null == site) {
			log.error("Unable to receive worksite with id: " + worksiteId);
		} else {
			Set<Member> members = sakaiProxy.getSite(worksiteId).getMembers();
			for (Member member : members) {
				for (User user : users) {
					if (user.getId().equals(member.getUserId())) {
						worksiteMembers.add(user);
						break;
					}
				}
			}
		}
		
		return worksiteMembers;
	}
	
	/**
	 * Remove any non-worksite members from list of user ids.
	 * 
	 * @param userIds
	 * @param worksiteId
	 * @return a list of matching worksite member user ids.
	 */
	private List<String> removeNonWorksiteMembersFromUserIds(List<String> userIds, String worksiteId) {
		
		List<String> worksiteMemberIds = new ArrayList<String>();
		
		Site site = sakaiProxy.getSite(worksiteId);
		if (null == site) {
			log.error("Unable to receive worksite with id: " + worksiteId);
		} else {
			Set<Member> members = sakaiProxy.getSite(worksiteId).getMembers();
			for (Member member : members) {
				for (String userId : userIds) {
					if (userId.equals(member.getUserId())) {
						worksiteMemberIds.add(userId);
						break;
					}
				}
			}
		}

		return worksiteMemberIds;
	}
	
	public void init() {
		cache = cacheManager.createCache(CACHE_NAME);
	}
	
	@Setter
	private SakaiProxy sakaiProxy;
	
	@Setter
	private ProfileDao dao;
	
	@Setter
	private ProfileLogic profileLogic;
	
	@Setter
	private ProfileConnectionsLogic connectionsLogic;
	
	@Setter
	private CacheManager cacheManager;
	
}
