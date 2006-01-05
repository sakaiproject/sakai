package org.sakaiproject.api.app.messageforums;

import java.util.List;
import java.util.Map;

public interface MembershipManager {
    
  public List getMembershipFilteredCourseMembersAsList(Map memberMap);
  
  public Map getAllCourseMembers(boolean filterFerpa);
  
  public List getAllCourseUsers();
  
  public List convertMemberMapToList(Map memberMap);
  
}
