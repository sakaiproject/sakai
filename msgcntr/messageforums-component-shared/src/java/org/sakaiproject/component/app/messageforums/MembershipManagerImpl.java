package org.sakaiproject.component.app.messageforums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.MembershipManager;
import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.api.common.edu.person.SakaiPersonManager;
import org.sakaiproject.api.kernel.tool.ToolManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.service.framework.portal.PortalService;
import org.sakaiproject.service.legacy.authzGroup.AuthzGroup;
import org.sakaiproject.service.legacy.authzGroup.AuthzGroupService;
import org.sakaiproject.service.legacy.authzGroup.Member;
import org.sakaiproject.service.legacy.authzGroup.Role;
import org.sakaiproject.service.legacy.security.SecurityService;
import org.sakaiproject.service.legacy.site.Group;
import org.sakaiproject.service.legacy.site.Site;
import org.sakaiproject.service.legacy.site.SiteService;
import org.sakaiproject.service.legacy.user.User;
import org.sakaiproject.service.legacy.user.UserDirectoryService;

public class MembershipManagerImpl implements MembershipManager{

  private static final Log LOG = LogFactory.getLog(MembershipManagerImpl.class);
          
  private SiteService siteService;
  private UserDirectoryService userDirectoryService;
  private SakaiPersonManager sakaiPersonManager;
  private AuthzGroupService authzGroupService;
  private ToolManager toolManager;
  private SecurityService securityService;
  private PortalService portalService;  

  public void init() {
    ;
  }    
  
  /**
   * returns a list where roles/groups are filtered when no site user has membership in role/group
   * to a role/group
   * @param memberMap
   * @return list of filtered members
   */
  public List getMembershipFilteredCourseMembersAsList(Map memberMap){
    //todo: implement me
    return new ArrayList();
  }

  /**
   * get all members for course all/user/role/group
   * return hash map for direct access to members via id
   * (used in UI when for selected list items)
   * @return list of members
   */
  public Map getAllCourseMembers(boolean filterFerpa)
  {   
    
    Map returnMap = new HashMap();    
    String realmId = getContextSiteId();
    Site currentSite = null;
    
    /** add all participants */
    MembershipItem memberAll = MembershipItem.getInstance();
    memberAll.setType(MembershipItem.TYPE_ALL_PARTICIPANTS);
    memberAll.setName("All Participants");
    returnMap.put(memberAll.getId(), memberAll);
 
    AuthzGroup realm;
    try{
      realm = authzGroupService.getAuthzGroup(realmId);
      currentSite = siteService.getSite(portalService.getCurrentSiteId());      
    }
    catch (IdUnusedException e){
      LOG.debug(e.getMessage(), e);
      return returnMap;
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
    
    /** handle users */
    Set users = realm.getMembers();
    for (Iterator userIterator = users.iterator(); userIterator.hasNext();){
      Member member = (Member) userIterator.next();
      String userId = member.getUserId();
      Role userRole = member.getRole();            
      
      User user;
      try{
        user = userDirectoryService.getUser(userId);
      }
      catch (IdUnusedException e){
        LOG.debug(e.getMessage(), e);
        continue;
      }            
      
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
    
    return returnMap;
  }
  
  /**
   * get all users for course w/o filtering of FERPA enabled members
   * @return list of MembershipItems
   */
  public List getAllCourseUsers()
  {       
    Map userMap = new HashMap();    
    String realmId = getContextSiteId();    
     
    AuthzGroup realm;
    try{
      realm = authzGroupService.getAuthzGroup(realmId);      
    }
    catch (IdUnusedException e){
      LOG.debug(e.getMessage(), e);
      return convertMemberMapToList(userMap);
    }
                
    /** handle users */
    Set users = realm.getMembers();
    for (Iterator userIterator = users.iterator(); userIterator.hasNext();){
      Member member = (Member) userIterator.next();
      String userId = member.getUserId();
      Role userRole = member.getRole();            
      
      User user;
      try{
        user = userDirectoryService.getUser(userId);
      }
      catch (IdUnusedException e){
        LOG.debug(e.getMessage(), e);
        continue;
      }            
      
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
    
    return convertMemberMapToList(userMap);
  }
  
  /**
   * returns a list for UI
   * @param memberMap
   * @return list of members
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


  public void setPortalService(PortalService portalService)
  {
    this.portalService = portalService;
  }  
 
}
