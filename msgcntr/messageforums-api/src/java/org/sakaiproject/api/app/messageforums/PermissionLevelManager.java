/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-api/src/java/org/sakaiproject/api/app/messageforums/PermissionLevelManager.java $
 * $Id: PermissionLevelManager.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
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
package org.sakaiproject.api.app.messageforums;

import java.util.List;
import java.util.Set;

import org.sakaiproject.api.app.messageforums.PermissionsMask;

public interface PermissionLevelManager {
    public static final String PERMISSION_LEVEL_NAME_OWNER = "Owner";
    public static final String PERMISSION_LEVEL_NAME_AUTHOR = "Author";
    public static final String PERMISSION_LEVEL_NAME_NONEDITING_AUTHOR = "Nonediting Author";
    public static final String PERMISSION_LEVEL_NAME_CONTRIBUTOR = "Contributor";
    public static final String PERMISSION_LEVEL_NAME_REVIEWER = "Reviewer"; 
    public static final String PERMISSION_LEVEL_NAME_NONE = "None";
    public static final String PERMISSION_LEVEL_NAME_CUSTOM = "Custom";
	public PermissionLevel getPermissionLevelByName(String name);
	public String getPermissionLevelType(PermissionLevel level);  
	public PermissionLevel createPermissionLevel(String name, String typeUuid, PermissionsMask mask);
	
	/**
	 * 
	 * @return the PermissionLevel representing the "Owner" level. If no level
	 * exists in MFR_PERMISSION_LEVEL_T, returns a default owner permission level.
	 * @throws IllegalStateException if no "Owner" type exists 
	 */
	public PermissionLevel getDefaultOwnerPermissionLevel();
	
	/**
	 * 
	 * @return the PermissionLevel representing the "Author" level. If no level
	 * exists in MFR_PERMISSION_LEVEL_T, returns a default Author permission level.
	 * @throws IllegalStateException if no "Author" type exists
	 */
	public PermissionLevel getDefaultAuthorPermissionLevel();
	
	/**
	 * 
	 * @return the PermissionLevel representing the "NoneditingAuthor" level. If no level
	 * exists in MFR_PERMISSION_LEVEL_T, returns a default Nonediting Author permission level.
	 * @throws IllegalStateException if no "NoneditingAuthor" type exists
	 */
	public PermissionLevel getDefaultNoneditingAuthorPermissionLevel();
	
	/**
	 * 
	 * @return the PermissionLevel representing the "Reviewer" level. If no level
	 * exists in MFR_PERMISSION_LEVEL_T, returns a default owner permission level.
	 * @throws IllegalStateException if no "Reviewer" type exists
	 */
	public PermissionLevel getDefaultReviewerPermissionLevel();
	
	/**
	 * 
	 * @return the PermissionLevel representing the "Contributor" level. If no level
	 * exists in MFR_PERMISSION_LEVEL_T, returns a default Contributor permission level.
	 * @throws IllegalStateException if no "Contributor" type exists
	 */
	public PermissionLevel getDefaultContributorPermissionLevel();
	
	/**
	 * 
	 * @return the PermissionLevel representing the "None" level. If no level
	 * exists in MFR_PERMISSION_LEVEL_T, returns a default None permission level.
	 * @throws IllegalStateException if no "None" type exists
	 */
	public PermissionLevel getDefaultNonePermissionLevel();
	
	/**
	 * 
	 * @return a list of the default (non-custom) permission levels available
	 */
	public List<PermissionLevel> getDefaultPermissionLevels();
	
    public DBMembershipItem createDBMembershipItem(String name, String permissionLevelName, Integer type);
    public void saveDBMembershipItem(DBMembershipItem item);
    public void savePermissionLevel(PermissionLevel level);
    
    /**
     * 
     * @return a non-null list of the names for the non-custom permissions
     */
    public  List getOrderedPermissionLevelNames(); 
    
    public Boolean getCustomPermissionByName(String customPermissionName, PermissionLevel permissionLevel);
    public List getCustomPermissions();
  	public List getAllMembershipItemsForForumsForSite(final Long areaId);
  	public List getAllMembershipItemsForTopicsForSite(final Long areaId);
  	public void deleteMembershipItems(Set membershipSet);
  	
}
