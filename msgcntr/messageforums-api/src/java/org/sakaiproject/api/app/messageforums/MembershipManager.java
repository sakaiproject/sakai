/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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
package org.sakaiproject.api.app.messageforums;

import java.util.List;
import java.util.Map;

public interface MembershipManager {
           
  /**
   * Get filtered members for course all/user/role/group<br>
   * Return hash map for direct access to members via id
   * (used in UI when for selected list items)<br>
   * Filter roles/groups which do not have members. Also
   * filters out users that the current user is not permitted to
   * view (ie they have privacy status hidden and the current user does not
   * have privileges to view hidden users)
   * To ignore hidden groups, pass in a null value
   * @param filterFerpa
   * @param hiddenGroups
   * @return map of members
   */
  public Map getFilteredCourseMembers(boolean filterFerpa, List<String> hiddenGroups);
  
    
  /**
   * Get members for course all/user/role/group<br>
   * Return hash map for direct access to members via id
   * (used in UI when for selected list items)<br>
   * To ignore hidden groups, pass in a null value
   * @param filterFerpa
   * @param includeRoles
   * @param includeAllParticipantsMember
   * @param hiddenGroups
   * @return map of members
   */
  public Map getAllCourseMembers(boolean filterFerpa, boolean includeRoles, boolean includeAllParticipantsMember, List<String> hiddenGroups);
  
  /**
   * get all users for course w/o filtering of FERPA enabled members
   * @return list of MembershipItems
   */
  public List getAllCourseUsers();

  /**
   * get all users for course w/o filtering of FERPA enabled members
   * @return map of MembershipItems
   */
  public Map getAllCourseUsersAsMap();


  /**
   * returns a list for UI
   * @param memberMap
   * @return list of members
   */
  public List convertMemberMapToList(Map memberMap);
  
}
