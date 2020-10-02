/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-component-impl/src/java/org/sakaiproject/component/app/messageforums/MembershipManagerImpl.java $
 * $Id: MembershipManagerImpl.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.component.app.messageforums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.api.app.messageforums.MembershipItem;
import org.sakaiproject.api.app.messageforums.MembershipManager;
import org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager;
import org.sakaiproject.api.privacy.PrivacyManager;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;

@Slf4j
public class MembershipManagerImpl implements MembershipManager {

  @Setter private AuthzGroupService authzGroupService;
  @Setter private PrivacyManager privacyManager;
  @Setter private PrivateMessageManager prtMsgManager;
  @Setter private ServerConfigurationService serverConfigurationService;
  @Setter private SiteService siteService;
  @Setter private ToolManager toolManager;
  @Setter private UserDirectoryService userDirectoryService;

  private static final String MESSAGECENTER_BUNDLE = "org.sakaiproject.api.app.messagecenter.bundle.Messages";
  private ResourceLoader rl;
  

  public void init() {
     log.info("init()");
     rl  = new ResourceLoader(MESSAGECENTER_BUNDLE);
  }

  /**
   * 
   * @param privacyManager
   */
  public void setPrivacyManager(PrivacyManager privacyManager) 
  {
	this.privacyManager = privacyManager;
  }

  /**
   * sets users' privacy status so can be filtered out
   * 
   * @param allCourseUsers - used to get user ids so can call PrivacyManager
   * @param courseUserMap - map of all course users
   * 
   * @return Map of all course users with privacy status set
   */
  private Map setPrivacyStatus(List allCourseUsers, Map courseUserMap) {
	  
	  List userIds = new ArrayList();
	  Map results = new HashMap();

	  Collection userCollection = courseUserMap.values();

	  for (Iterator usersIter = allCourseUsers.iterator(); usersIter.hasNext();) {
		  MembershipItem memberItem = (MembershipItem) usersIter.next();
	      
		  if (memberItem.getUser() != null) {
			  userIds.add(memberItem.getUser().getId());    
		  }
	  }

	  // set privacy status
	  Set memberSet = null;		  

	  memberSet = privacyManager.findViewable(
			  			("/site/" + toolManager.getCurrentPlacement().getContext()), new HashSet(userIds));
	  
	  /** look through the members again to pick out Member objects corresponding
	  		to only those who are visible (as well as current user) */
	  for (Iterator userIterator = userCollection.iterator(); userIterator.hasNext();) {
		  MembershipItem memberItem = (MembershipItem) userIterator.next();
				  
		  if (memberItem.getUser() != null) {
			  memberItem.setViewable(memberSet.contains(memberItem.getUser().getId()));
		  }
		  else {
			  // want groups to be displayed
			  memberItem.setViewable(true);
		  }
		  
		  results.put(memberItem.getId(), memberItem);
	  }
		
	  return results;
  }
  
  /**
   * @see org.sakaiproject.api.app.messageforums.MembershipManager#getFilteredCourseMembers(boolean)
   */
  public Map getFilteredCourseMembers(boolean filterFerpa, List<String> hiddenGroups){

    List allCourseUsers = getAllCourseUsers();
    
    Set membershipRoleSet = new HashSet();    
    
    if(prtMsgManager.isAllowToFieldRoles() || prtMsgManager.isAllowToFieldMyGroupRoles()){
    	/** generate set of roles which has members */
    	for (Iterator i = allCourseUsers.iterator(); i.hasNext();){
    		MembershipItem item = (MembershipItem) i.next();
    		if (item.getRole() != null){
    			membershipRoleSet.add(item.getRole());
    		}
    	}
    }
    
    /** filter member map */
    Map<String, MembershipItem> memberMap = getAllCourseMembers(filterFerpa, true, true, hiddenGroups);

    Set<String> viewableUsersForTA = new HashSet<String>();
    if (prtMsgManager.isSectionTA()) {
        viewableUsersForTA = getFellowSectionMembers();
    }
    
    for (Iterator i = memberMap.entrySet().iterator(); i.hasNext();){
      
      Map.Entry entry = (Map.Entry) i.next();
      MembershipItem item = (MembershipItem) entry.getValue();

      if (MembershipItem.TYPE_ROLE == item.getType() || MembershipItem.TYPE_MYGROUPROLES == item.getType()) {
        /** if no member belongs to role, filter role */
        if (!membershipRoleSet.contains(item.getRole())){          
          i.remove();
        }
      }
      else if (MembershipItem.TYPE_GROUP == item.getType()) {
        /** if no member belongs to group, filter group */
        if (item.getGroup().getMembers().size() == 0){
          i.remove();
        }
      }   
      else{
        if (!item.isViewable() && !prtMsgManager.isInstructor()) {
            if (prtMsgManager.isSectionTA() && viewableUsersForTA.contains(item.getUser().getId())) {
                // if this user is a member of this TA's section, they
                // are viewable
            } else {
                i.remove();
            }
        }
      }
    }
    
    return memberMap;
  }
  
  /**
   * 
   * @return a non-null set of userIds for all of the members in the current user's section(s). Useful for determining students
   * who are viewable to a user in a TA role
   */
  private Set<String> getFellowSectionMembers() {
      Set<String> fellowMembers = new HashSet<String>();
      try {
          Collection<Group> groups = siteService.getSite(toolManager.getCurrentPlacement().getContext()).getGroupsWithMember(userDirectoryService.getCurrentUser().getId());
          if (groups != null) {
              for (Group group : groups) {
                  Set<Member> groupMembers = group.getMembers();
                  if (groupMembers != null) {
                      for (Member groupMember : groupMembers) {
                          fellowMembers.add(groupMember.getUserId());
                      }
                  }
              }
          }
      } catch (IdUnusedException e) {
          log.warn("Unable to retrieve site to determine current user's fellow section members.");
      }
      
      return fellowMembers;
  }

	public Map<String, MembershipItem> getAllCourseMembers(boolean filterFerpa, boolean includeRoles, boolean includeAllParticipantsMember, List<String> hiddenGroups) {
		AuthzGroup realm;
		Map<String, MembershipItem> returnMap = new HashMap<>();
		Site site;
		String siteId = getContextSiteId();
		String siteReference = getContextSiteReference();
		User user = userDirectoryService.getCurrentUser();

		try {
			realm = authzGroupService.getAuthzGroup(siteReference);
			site = siteService.getSite(siteId);
		} catch (IdUnusedException iue) {
			log.warn("Attempted to access site {} but it was not found: {}", siteId, iue.toString());
			return returnMap;
		} catch (GroupNotDefinedException gnde) {
			log.warn("Attempted to access authz site realm {} but it was not found: {}", siteReference, gnde.toString());
			return returnMap;
		}

		if (prtMsgManager.isAllowToFieldAllParticipants(user)) {
			// add all participants
			if (includeAllParticipantsMember) {
				MembershipItem memberAll = MembershipItem.makeMembershipItem(
						rl.getString("all_participants_desc"),
						MembershipItem.TYPE_ALL_PARTICIPANTS);
				returnMap.put(memberAll.getId(), memberAll);
			}
		}

		if (prtMsgManager.isAllowToFieldGroups(user)) {
			boolean viewHiddenGroups = prtMsgManager.isAllowToViewHiddenGroups(user);
			for (Group currentGroup : site.getGroups()) {
				//only show groups the user has access to
				if (viewHiddenGroups || !hiddenGroups.contains(currentGroup.getTitle())) {
					MembershipItem member = MembershipItem.makeMembershipItem(
							rl.getFormattedMessage("participants_group_desc", currentGroup.getTitle()),
							MembershipItem.TYPE_GROUP,
							currentGroup,
							null, null);
					if (!isGroupAlreadyInMap(returnMap, member)) {
						returnMap.put(member.getId(), member);
					}
				}
			}
		}

		if (prtMsgManager.isAllowToFieldRoles(user)) {
			if (includeRoles && realm != null) {
				Set<Role> roles = realm.getRoles();
				for (Role role : roles) {
					String roleId = role.getId();
					if (StringUtils.isNotBlank(roleId)) {
						roleId = roleId.substring(0, 1).toUpperCase() + roleId.substring(1);
					}

					MembershipItem member = MembershipItem.makeMembershipItem(
							rl.getFormattedMessage("participants_role_desc", roleId),
							MembershipItem.TYPE_ROLE,
							null,
							role,
							null);
					returnMap.put(member.getId(), member);
				}
			}
		}

		if (prtMsgManager.isAllowToFieldUsers(user)) {
			realm.getMembers().forEach(member -> addUsertoMemberItemMap(returnMap, realm, member.getUserId(), member.getRole(), MembershipItem.TYPE_USER));
		}

		if (prtMsgManager.isAllowToFieldMyGroups(user)) {
			for (Group group : site.getGroupsWithMember(user.getId())) {
				MembershipItem member = MembershipItem.makeMembershipItem(
						rl.getFormattedMessage("participants_group_desc", group.getTitle()),
						MembershipItem.TYPE_MYGROUPS,
						group,
						null, null);
				if (!isGroupAlreadyInMap(returnMap, member)) {
					returnMap.put(member.getId(), member);
				}
			}
		}

		if (prtMsgManager.isAllowToFieldMyGroupMembers(user)) {
			for (Group group : site.getGroupsWithMember(user.getId())) {
				Set<Member> groupMembers = group.getMembers();
				for (Member groupMember : groupMembers) {
					addUsertoMemberItemMap(returnMap, realm, groupMember.getUserId(), groupMember.getRole(), MembershipItem.TYPE_MYGROUPMEMBERS);
				}
			}
		}

		if (prtMsgManager.isAllowToFieldMyGroupRoles(user)) {
			for (Group group : site.getGroupsWithMember(user.getId())) {
				for (Role role : group.getRoles()) {
					String roleId = role.getId();
					if (StringUtils.isNotBlank(roleId)) {
						roleId = roleId.substring(0, 1).toUpperCase() + roleId.substring(1);
					}
					MembershipItem member = MembershipItem.makeMembershipItem(
							rl.getFormattedMessage("group_role_desc", group.getTitle(), roleId),
							MembershipItem.TYPE_MYGROUPROLES,
							group,
							role,
							null);

					if (!isGroupAlreadyInMap(returnMap, member)) {
						returnMap.put(member.getId(), member);
					}
				}
			}
		}

		// set FERPA status for all items in map - allCourseUsers
		// needed by PrivacyManager to determine status

		return setPrivacyStatus(getAllCourseUsers(), returnMap);
	}

	private void addUsertoMemberItemMap(Map<String, MembershipItem> returnMap, AuthzGroup realm, String userId, Role userRole, Integer memberItemType) {
		if (!isUserAlreadyInMap(returnMap, userId)) {
			Member member = realm.getMember(userId);
			if (member != null && member.isActive() && !"admin".equals(userId)) {
				try {
					User user = userDirectoryService.getUser(userId);
					String name = user.getSortName();
					if (serverConfigurationService.getBoolean("msg.displayEid", true)) {
						name = name + " (" + user.getDisplayId() + ")";
					}
					MembershipItem memberItem = MembershipItem.makeMembershipItem(
							name,
							memberItemType,
							null,
							userRole,
							user
					);
					returnMap.put(memberItem.getId(), memberItem);
				} catch (UserNotDefinedException e) {
					log.warn("User {} not defined", userId);
				}
			}
		}
	}

	private boolean isGroupAlreadyInMap(Map<String, MembershipItem> returnMap, MembershipItem membershipItem) {
		Predicate<MembershipItem> ifGroupHasAMembership = m ->
				(m.getType() == MembershipItem.TYPE_GROUP || m.getType() == MembershipItem.TYPE_MYGROUPS)
						&& StringUtils.equals(m.getName(), membershipItem.getName());
		return returnMap.values().stream().anyMatch(ifGroupHasAMembership);
	}

	private boolean isUserAlreadyInMap(Map<String, MembershipItem> returnMap, String userId) {
		Predicate<MembershipItem> ifUserHasAMembership = m -> m.getUser() != null && StringUtils.equals(m.getUser().getId(), userId);
		return returnMap.values().stream().anyMatch(ifUserHasAMembership);
	}

  /**
   * @see org.sakaiproject.api.app.messageforums.MembershipManager#getAllCourseUsers()
   */
  @Override
  public List<MembershipItem> getAllCourseUsers() {
      return convertMemberMapToList(getAllCourseUsersAsMap());
  }

  /**
   *  @see MembershipManager#getAllCourseUsersAsMap()
   */
  @Override
  public Map<String, MembershipItem> getAllCourseUsersAsMap()
  {       
    Map<String, MembershipItem> userMap = new HashMap<>();
    String realmId = getContextSiteReference();
     
    AuthzGroup realm = null;
    try{
      realm = authzGroupService.getAuthzGroup(realmId);      
    } catch (GroupNotDefinedException e) {
		//FIXME Is this expected behavior?  If so it should be documented - LDS
    	log.error(e.getMessage(), e);
	}
                
    /** handle users */
    if (realm == null)
			throw new IllegalStateException("AuthzGroup realm == null!");
    Set users = realm.getMembers();
    List userIds = getRealmIdList(users);
    List<User> userList = userDirectoryService.getUsers(userIds);
    Map<String, User> userMMap = getuserMap(userList);
    if (users == null)
		throw new RuntimeException("Could not obtain members from realm!");

    for (Iterator userIterator = users.iterator(); userIterator.hasNext();){
      Member member = (Member) userIterator.next();      
      String userId = member.getUserId();
      Role userRole = member.getRole();            
      
      User user = null;
      
      	if(realm.getMember(userId) != null && realm.getMember(userId).isActive())
      	{
      		if (userMMap.containsKey(member.getUserId())) {
      			user = getUserFromList(member.getUserId(), userList);
      		}
      	}
      	if (user == null){
      		//user does not exits
      		continue;
      	}
      	 if(user != null && !"admin".equals(userId))
         {
         	MembershipItem memberItem = MembershipItem.makeMembershipItem(
         			user.getSortName(),
					MembershipItem.TYPE_USER,
					null,
					userRole,
					user
			);
     		userMap.put(memberItem.getId(), memberItem);
         }
    }
    return userMap;
  }

  @Override
  public List<MembershipItem> convertMemberMapToList(Map<String, MembershipItem> memberMap){
            
    MembershipItem[] membershipArray = new MembershipItem[memberMap.size()];
    membershipArray = (MembershipItem[]) memberMap.values().toArray(membershipArray);
    Arrays.sort(membershipArray);
    
    return Arrays.asList(membershipArray);     
  }
  
  private User getUserFromList(String userId, List<User> userList) {
	  User u = null;
	  for (int i = 0; i < userList.size(); i++) {
		  User tu = (User) userList.get(i);
		  if (userId.equals(tu.getId()))
			  return tu;
	  }
	  
	  return u;
  }
    
  private List<String> getRealmIdList(Set realmUsers) {
	  List ret = new ArrayList();
	  Iterator it = realmUsers.iterator();
	  while (it.hasNext()) {
		  Member mem = (Member)it.next();
		  ret.add(mem.getUserId());
	  }
	  return ret;
  }
  
  private Map<String, User>  getuserMap(List userList) {
	  Map<String, User> ret = new HashMap<String, User>();
	  for (int i = 0; i < userList.size(); i++) {
		  User tu = (User) userList.get(i);
		  ret.put(tu.getId(), tu);
	  }
	  return ret;
  }

	public String getContextSiteId() {
  		return StringUtils.trimToEmpty(toolManager.getCurrentPlacement().getContext());
	}

	public String getContextSiteReference() {
		return ("/site/" + getContextSiteId());
	}
}
