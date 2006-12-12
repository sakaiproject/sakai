/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-component-impl/src/java/org/sakaiproject/component/app/messageforums/MembershipManagerImpl.java $
 * $Id: MembershipManagerImpl.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.MembershipManager;
import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.api.common.edu.person.SakaiPersonManager;
import org.sakaiproject.api.privacy.PrivacyManager;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

public class MembershipManagerImpl implements MembershipManager{

  private static final Log LOG = LogFactory.getLog(MembershipManagerImpl.class);
          
  private SiteService siteService;
  private UserDirectoryService userDirectoryService;
  private SakaiPersonManager sakaiPersonManager;
  private AuthzGroupService authzGroupService;
  private ToolManager toolManager;
  private SecurityService securityService;
  private PrivacyManager privacyManager;

  public void init() {
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
   * Filters out users whose Privacy Status is set to Hidden
   * 
   * @param all
   * @return
   */
  private Map filterByPrivacyManager(List allCourseUsers, Map courseUserMap) {
	  
	  List userIds = new ArrayList();
	  Map results = new HashMap();

	  for (Iterator usersIter = allCourseUsers.iterator(); usersIter.hasNext();) {
		  MembershipItem memberItem = (MembershipItem) usersIter.next();
	      
		  if (memberItem.getUser() != null) {
			  userIds.add(memberItem.getUser().getId());    
		  }
	  }
	  
	  // only allow private messages to be sent to users with Visible privacy status
	  // Instructors should see all users
	  Set memberSet = null;
	  
	  if (securityService.unlock(userDirectoryService.getCurrentUser(), "site.upd", getContextSiteId())) {
		  return courseUserMap;
	  }
	  else {
		  memberSet = privacyManager.findViewable(
				  			("/site/" + toolManager.getCurrentPlacement().getContext()), new HashSet(userIds));
	  }
	 

	  Collection userCollection = courseUserMap.values();
		
	  /** look through the members again to pick out Member objects corresponding
		  to only those who are visible (as well as current user) */
	  for (Iterator userIterator = userCollection.iterator(); userIterator.hasNext();) {
		  MembershipItem memberItem = (MembershipItem) userIterator.next();
			
		  if (memberItem.getUser() != null) {
			  final boolean inMemberSet = memberSet.contains(memberItem.getUser().getId());
			  final String memberSetId = memberItem.getUser().getId();
			  final String currentUserId = userDirectoryService.getCurrentUser().getId();
			  
			  if (inMemberSet || currentUserId.equals(memberSetId)) {
				  results.put(memberItem.getId(), memberItem);
			  }
		  }
		  else {
			  results.put(memberItem.getId(), memberItem);
		  }
	  }
		
	  return results;
  }
  
  /**
   * @see org.sakaiproject.api.app.messageforums.MembershipManager#getFilteredCourseMembers(boolean)
   */
  public Map getFilteredCourseMembers(boolean filterFerpa){

    List allCourseUsers = getAllCourseUsers();
    
    Set membershipRoleSet = new HashSet();    
    
    /** generate set of roles which has members */
    for (Iterator i = allCourseUsers.iterator(); i.hasNext();){
      MembershipItem item = (MembershipItem) i.next();
      if (item.getRole() != null){
        membershipRoleSet.add(item.getRole());
      }
    }
    
    /** filter member map */
    Map memberMap = getAllCourseMembers(filterFerpa, true, true);
    
    if (filterFerpa) {
    	memberMap = filterByPrivacyManager(allCourseUsers, memberMap);
    }
    
    for (Iterator i = memberMap.entrySet().iterator(); i.hasNext();){
      
      Map.Entry entry = (Map.Entry) i.next();
      MembershipItem item = (MembershipItem) entry.getValue();
      
      if (MembershipItem.TYPE_ROLE.equals(item.getType())){
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
        ;
      }
    }
    
    return memberMap;
  }

  /**
   * @see org.sakaiproject.api.app.messageforums.MembershipManager#getAllCourseMembers(boolean, boolean, boolean)
   */
  public Map getAllCourseMembers(boolean filterFerpa, boolean includeRoles, boolean includeAllParticipantsMember)
  {   
    Map returnMap = new HashMap();    
    String realmId = getContextSiteId();
    Site currentSite = null;
        
    /** add all participants */
    if (includeAllParticipantsMember){
      MembershipItem memberAll = MembershipItem.getInstance();
      memberAll.setType(MembershipItem.TYPE_ALL_PARTICIPANTS);
      memberAll.setName(MembershipItem.ALL_PARTICIPANTS_DESC);
      returnMap.put(memberAll.getId(), memberAll);
    }
 
    AuthzGroup realm = null;
    try{
      realm = authzGroupService.getAuthzGroup(realmId);
      currentSite = siteService.getSite(toolManager.getCurrentPlacement().getContext());      
    }
    catch (IdUnusedException e){
      LOG.debug(e.getMessage(), e);
      return returnMap;
    } catch (GroupNotDefinedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
        
    /** handle groups */
    Collection groups = currentSite.getGroups();    
    for (Iterator groupIterator = groups.iterator(); groupIterator.hasNext();){
      Group currentGroup = (Group) groupIterator.next();      
      MembershipItem member = MembershipItem.getInstance();
      member.setType(MembershipItem.TYPE_GROUP);
      member.setName(currentGroup.getTitle() + " Group");
      member.setGroup(currentGroup);
      returnMap.put(member.getId(), member);
    }
    
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
        member.setName(roleId + " Role");
        member.setRole(role);
        returnMap.put(member.getId(), member);
      }
    }
    
    /** handle users */
    Set users = realm.getMembers();
    
    /** create our HashSet of user ids */
    for (Iterator userIterator = users.iterator(); userIterator.hasNext();){
      Member member = (Member) userIterator.next();
      String userId = member.getUserId();
      Role userRole = member.getRole();            
      
      User user = null;
      try{
      	if(realm.getMember(userId) != null && realm.getMember(userId).isActive())
      	{
      		user = userDirectoryService.getUser(userId);
      	}
      } catch (UserNotDefinedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}            
      
      if(user != null)
      {
      	MembershipItem memberItem = MembershipItem.getInstance();
      	memberItem.setType(MembershipItem.TYPE_USER);
      	memberItem.setName(user.getSortName());
      	memberItem.setUser(user);
      	memberItem.setRole(userRole);             

      	if(!(userId).equals("admin"))
      	{                                       
      		if (filterFerpa){                       
      			List personList = sakaiPersonManager.findSakaiPersonByUid(userId);
      			boolean ferpa_flag = false;
      			for (Iterator iter = personList.iterator(); iter.hasNext();)
      			{
      				SakaiPerson element = (SakaiPerson) iter.next();            
      				if (Boolean.TRUE.equals(element.getFerpaEnabled())){
      					ferpa_flag = true;
      				}            
      			}                                          
      			if (!ferpa_flag || securityService.unlock(memberItem.getUser(), 
      					SiteService.SECURE_UPDATE_SITE,
      					getContextSiteId())
      					|| securityService.unlock(userDirectoryService.getCurrentUser(),
      							SiteService.SECURE_UPDATE_SITE,
      							getContextSiteId())
      			){
      				returnMap.put(memberItem.getId(), memberItem);
      			}
      		}
      		else{
      			returnMap.put(memberItem.getId(), memberItem);
      		}
      	}                                
      }
    }
    
    return returnMap;
  }
  
    
  /**
   * @see org.sakaiproject.api.app.messageforums.MembershipManager#getAllCourseUsers()
   */
  public List getAllCourseUsers()
  {       
    Map userMap = new HashMap();    
    String realmId = getContextSiteId();    
     
    AuthzGroup realm = null;
    try{
      realm = authzGroupService.getAuthzGroup(realmId);      
    } catch (GroupNotDefinedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
                
    /** handle users */
    Set users = realm.getMembers();
    for (Iterator userIterator = users.iterator(); userIterator.hasNext();){
      Member member = (Member) userIterator.next();      
      String userId = member.getUserId();
      Role userRole = member.getRole();            
      
      User user = null;
      try{
      	if(realm.getMember(userId) != null && realm.getMember(userId).isActive())
      	{
      		user = userDirectoryService.getUser(userId);
      	}
      } catch (UserNotDefinedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}            
      
      if(user != null)
      {
      	MembershipItem memberItem = MembershipItem.getInstance();
      	memberItem.setType(MembershipItem.TYPE_USER);
      	memberItem.setName(user.getSortName());
      	memberItem.setUser(user);
      	memberItem.setRole(userRole);             

      	if(!(userId).equals("admin"))
      	{                                               
      		userMap.put(memberItem.getId(), memberItem);                
      	}
      }
    }
    
    return convertMemberMapToList(userMap);
  }
  
  /**
   * @see org.sakaiproject.api.app.messageforums.MembershipManager#convertMemberMapToList(java.util.Map)
   */
  public List convertMemberMapToList(Map memberMap){
            
    MembershipItem[] membershipArray = new MembershipItem[memberMap.size()];
    membershipArray = (MembershipItem[]) memberMap.values().toArray(membershipArray);
    Arrays.sort(membershipArray);
    
    return Arrays.asList(membershipArray);     
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

  public void setSakaiPersonManager(SakaiPersonManager sakaiPersonManager)
  {
    this.sakaiPersonManager = sakaiPersonManager;
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

}
