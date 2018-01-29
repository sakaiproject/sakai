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

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.api.app.messageforums.MembershipManager;
import org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager;
import org.sakaiproject.api.privacy.PrivacyManager;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ServerConfigurationService;
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
public class MembershipManagerImpl implements MembershipManager{

  private SiteService siteService;
  private UserDirectoryService userDirectoryService;
  private AuthzGroupService authzGroupService;
  private ToolManager toolManager;
  private SecurityService securityService;
  private PrivacyManager privacyManager;
  private PrivateMessageManager prtMsgManager;
  
  private static final String MESSAGECENTER_BUNDLE = "org.sakaiproject.api.app.messagecenter.bundle.Messages";
  private ResourceLoader rl = new ResourceLoader(MESSAGECENTER_BUNDLE);
  

  public void init() {
     log.info("init()");
    ;
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
    
    if(getPrtMsgManager().isAllowToFieldRoles() || getPrtMsgManager().isAllowToFieldMyGroupRoles()){
    	/** generate set of roles which has members */
    	for (Iterator i = allCourseUsers.iterator(); i.hasNext();){
    		MembershipItem item = (MembershipItem) i.next();
    		if (item.getRole() != null){
    			membershipRoleSet.add(item.getRole());
    		}
    	}
    }
    
    /** filter member map */
    Map memberMap = getAllCourseMembers(filterFerpa, true, true, hiddenGroups);
    
//    if (filterFerpa) {
//    	memberMap = setPrivacyStatus(allCourseUsers, memberMap);
//    }
    Set<String> viewableUsersForTA = new HashSet<String>();
    if (prtMsgManager.isSectionTA()) {
        viewableUsersForTA = getFellowSectionMembers();
    }
    
    for (Iterator i = memberMap.entrySet().iterator(); i.hasNext();){
      
      Map.Entry entry = (Map.Entry) i.next();
      MembershipItem item = (MembershipItem) entry.getValue();
      
      if (MembershipItem.TYPE_ROLE.equals(item.getType()) || MembershipItem.TYPE_MYGROUPROLES.equals(item.getType())){
        /** if no member belongs to role, filter role */
        if (!membershipRoleSet.contains(item.getRole())){          
          i.remove();
        }
      }
      else if (MembershipItem.TYPE_GROUP.equals(item.getType())){
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

  /**
   * @see org.sakaiproject.api.app.messageforums.MembershipManager#getAllCourseMembers(boolean, boolean, boolean)
   */
  public Map getAllCourseMembers(boolean filterFerpa, boolean includeRoles, boolean includeAllParticipantsMember, List<String> hiddenGroups)
  {   
    Map returnMap = new HashMap();    
    String realmId = getContextSiteId();
    Site currentSite = null;
        
    if(getPrtMsgManager().isAllowToFieldAllParticipants()){
    	/** add all participants */
    	if (includeAllParticipantsMember){
    		MembershipItem memberAll = MembershipItem.getInstance();
    		memberAll.setType(MembershipItem.TYPE_ALL_PARTICIPANTS);
    		//memberAll.setName(MembershipItem.ALL_PARTICIPANTS_DESC);
    		memberAll.setName(rl.getString("all_participants_desc"));

    		returnMap.put(memberAll.getId(), memberAll);
    	}
    }
 
    AuthzGroup realm = null;
    try{
      realm = authzGroupService.getAuthzGroup(realmId);
      currentSite = siteService.getSite(toolManager.getCurrentPlacement().getContext());
      if (currentSite == null) // SAK-12988
				throw new RuntimeException("Could not obtain Site object!");
    }
    catch (IdUnusedException e){
		//FIXME Is this expected behavior?  If so it should be documented - LDS
      log.debug(e.getMessage(), e);
      return returnMap;
    } catch (GroupNotDefinedException e) {
		//FIXME Is this expected behavior?  If so it should be documented - LDS
    	log.error(e.getMessage(), e);
	}

	boolean viewHiddenGroups = getPrtMsgManager().isAllowToViewHiddenGroups();
    if(getPrtMsgManager().isAllowToFieldGroups()){
    	/** handle groups */
    	if (currentSite == null)
    		throw new IllegalStateException("Site currentSite == null!");
    	Collection groups = currentSite.getGroups();    
    	for (Iterator groupIterator = groups.iterator(); groupIterator.hasNext();){
    		Group currentGroup = (Group) groupIterator.next();     
    		//only show groups the user has access to
    		if(viewHiddenGroups || !containsId(currentGroup.getTitle(), hiddenGroups)){
    			MembershipItem member = MembershipItem.getInstance();
    			member.setType(MembershipItem.TYPE_GROUP);
    			//member.setName(currentGroup.getTitle() + " Group");
    			member.setName(rl.getFormattedMessage("participants_group_desc",new Object[]{currentGroup.getTitle()}));
    			member.setGroup(currentGroup);
                if (!isGroupAlreadyInMap(returnMap, member)) {
                    returnMap.put(member.getId(), member);
                }
    		}
    	}
    }
    if(getPrtMsgManager().isAllowToFieldRoles()){
    	/** handle roles */
    	if (includeRoles && realm != null){
    		Set roles = realm.getRoles();
    		for (Iterator roleIterator = roles.iterator(); roleIterator.hasNext();){
    			Role role = (Role) roleIterator.next();
    			MembershipItem member = MembershipItem.getInstance();
    			member.setType(MembershipItem.TYPE_ROLE);
    			String roleId = role.getId();
    			if (roleId != null && roleId.length() > 0){
    				roleId = roleId.substring(0,1).toUpperCase() + roleId.substring(1); 
    			}
    			//        member.setName(roleId + " Role");
    			member.setName(rl.getFormattedMessage("participants_role_desc",new Object[]{roleId}));        
    			member.setRole(role);
    			returnMap.put(member.getId(), member);
    		}
    	}
    }
    
    if(getPrtMsgManager().isAllowToFieldUsers()){
        /** handle users */
        if (realm == null)
    			throw new IllegalStateException("AuthzGroup realm == null!");
        Set users = realm.getMembers();
        if (users == null)
    			throw new RuntimeException("Could not obtain members from realm!");
        
        /** create our HashSet of user ids */
        for (Iterator userIterator = users.iterator(); userIterator.hasNext();){
          Member member = (Member) userIterator.next();
          String userId = member.getUserId();
          Role userRole = member.getRole();   
          addUsertoMemberItemMap(returnMap, realm, userId, userRole, MembershipItem.TYPE_USER);
        }
    }    
    
		if (getPrtMsgManager().isAllowToFieldMyGroups()) {
			try {
				Collection<Group> groups = siteService.getSite(toolManager.getCurrentPlacement().getContext())
						.getGroupsWithMember(userDirectoryService.getCurrentUser().getId());
				if (groups != null) {
					for (Group group : groups) {
                        MembershipItem member = MembershipItem.getInstance();
						member.setType(MembershipItem.TYPE_MYGROUPS);
						member.setName(rl.getFormattedMessage("participants_group_desc",
										new Object[] { group.getTitle() }));
						member.setGroup(group);

						if (!isGroupAlreadyInMap(returnMap, member)) {
                            returnMap.put(member.getId(), member);
						}
					}
				}
			} catch (IdUnusedException e) {
				log.warn("Unable to retrieve site to determine current user's groups.");
			}
		}

		if (getPrtMsgManager().isAllowToFieldMyGroupMembers()) {

			try {
				Collection<Group> groups = siteService.getSite(toolManager.getCurrentPlacement().getContext())
						.getGroupsWithMember(userDirectoryService.getCurrentUser().getId());
				if (groups != null) {
					for (Group group : groups) {
						Set<Member> groupMembers = group.getMembers();
						for (Member groupMember : groupMembers) {
							addUsertoMemberItemMap(returnMap, realm, groupMember.getUserId(), groupMember.getRole(),
									MembershipItem.TYPE_MYGROUPMEMBERS);
						}
					}
				}
			} catch (IdUnusedException e) {
				log.warn("Unable to retrieve site to determine current user's group members.");
			}
		}
		
		if (getPrtMsgManager().isAllowToFieldMyGroupRoles()) {
			/** handle roles in current user's groups */

			try {
				Collection<Group> groups = siteService.getSite(toolManager.getCurrentPlacement().getContext())
						.getGroupsWithMember(userDirectoryService.getCurrentUser().getId());
				if (groups != null) {
					for (Group group : groups) {
						Set<Role> groupRoles = group.getRoles();
						for (Role role : groupRoles) {
							MembershipItem member = MembershipItem.getInstance();
							member.setType(MembershipItem.TYPE_MYGROUPROLES);
							String roleId = role.getId();
							if (roleId != null && roleId.length() > 0) {
								roleId = roleId.substring(0, 1).toUpperCase() + roleId.substring(1);
							}
							member.setName(rl.getFormattedMessage("group_role_desc", new Object[] { group.getTitle(), roleId }));
							member.setRole(role);
							member.setGroup(group);

							if (!isGroupAlreadyInMap(returnMap, member)) {
								returnMap.put(member.getId(), member);
							}
						}
					}
				}
			} catch (IdUnusedException e) {
				log.warn("Unable to retrieve site to determine current user's group member roles.");
			}
		}

    // set FERPA status for all items in map - allCourseUsers
    // needed by PrivacyManager to determine status
    
    return setPrivacyStatus(getAllCourseUsers(), returnMap);
  }

	private void addUsertoMemberItemMap(Map returnMap, AuthzGroup realm, String userId, Role userRole, Integer memberItemType) {
		if (!isUserAlreadyInMap(returnMap, userId)) {

			User user = null;
			try {
				if (realm.getMember(userId) != null && realm.getMember(userId).isActive()) {
					user = userDirectoryService.getUser(userId);
				}
			} catch (UserNotDefinedException e) {
				log.warn(" User " + userId + " not defined");
			}

			// Don't want admin as part of the list
			if (user != null && !"admin".equals(userId)) {
				MembershipItem memberItem = MembershipItem.getInstance();
				memberItem.setType(memberItemType);
				if (ServerConfigurationService.getBoolean("msg.displayEid", true)) {
					memberItem.setName(user.getSortName() + " (" + user.getDisplayId() + ")");
				} else {
					memberItem.setName(user.getSortName());
				}
				memberItem.setUser(user);
				memberItem.setRole(userRole);
                returnMap.put(memberItem.getId(), memberItem);
			}
		}
	}

	private boolean isGroupAlreadyInMap(Map<String, MembershipItem> returnMap, MembershipItem membershipItem) {
		for(MembershipItem m: returnMap.values()){
            if((m.getType() == MembershipItem.TYPE_GROUP || 
                    m.getType() == MembershipItem.TYPE_MYGROUPS) &&
                    m.getName().equals(membershipItem.getName())){
                return true;
            }
        }
        return false;
	}

	private boolean isUserAlreadyInMap(Map<String, MembershipItem> returnMap, String userId) {
		for(MembershipItem m: returnMap.values()){
            if(m.getUser() != null && m.getUser().getId().equals(userId)){
                return true;
            }
        }
        return false;
	}
  
  private boolean containsId(String searchId, List<String> ids){
	  if(ids != null && searchId != null){
		  for (String id : ids) {
			  if(id.equals(searchId))
				  return true;
		  }
	  }
	  return false;
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
    Map<String, MembershipItem> userMap = new HashMap();
    String realmId = getContextSiteId();    
     
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
         	MembershipItem memberItem = MembershipItem.getInstance();
         	memberItem.setType(MembershipItem.TYPE_USER);
         	memberItem.setName(user.getSortName());
         	memberItem.setUser(user);
         	memberItem.setRole(userRole);     
     		userMap.put(memberItem.getId(), memberItem);     
         }
    }
    return userMap;
  }
  
  /**
   * @see org.sakaiproject.api.app.messageforums.MembershipManager#convertMemberMapToList(java.util.Map)
   */
  @Override
  public List<MembershipItem> convertMemberMapToList(Map memberMap){
            
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
  
  /**
   * get site reference
   * @return siteId
   */
  public String getContextSiteId()
  {    
    return ("/site/" + toolManager.getCurrentPlacement().getContext());
  }
  
  
  /** setters */
  public void setSiteService(SiteService siteService)
  {
    this.siteService = siteService;
  }

  public void setUserDirectoryService(UserDirectoryService userDirectoryService)
  {
    this.userDirectoryService = userDirectoryService;
  }

  public void setAuthzGroupService(AuthzGroupService authzGroupService)
  {
    this.authzGroupService = authzGroupService;
  }

  public void setToolManager(ToolManager toolManager)
  {
    this.toolManager = toolManager;
  }

  public void setSecurityService(SecurityService securityService)
  {
    this.securityService = securityService;
  }

public PrivateMessageManager getPrtMsgManager() {
	return prtMsgManager;
}

public void setPrtMsgManager(PrivateMessageManager prtMsgManager) {
	this.prtMsgManager = prtMsgManager;
}

}
