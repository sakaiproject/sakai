/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-api/src/java/org/sakaiproject/api/app/messageforums/PermissionLevelManager.java $
 * $Id: PermissionLevelManager.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
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
package org.sakaiproject.api.app.messageforums;

import java.util.List;

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
	public PermissionLevel getDefaultOwnerPermissionLevel();
	public PermissionLevel getDefaultAuthorPermissionLevel();
	public PermissionLevel getDefaultNoneditingAuthorPermissionLevel();
	public PermissionLevel getDefaultReviewerPermissionLevel();
	public PermissionLevel getDefaultContributorPermissionLevel();
	public PermissionLevel getDefaultNonePermissionLevel();
    public DBMembershipItem createDBMembershipItem(String name, String permissionLevelName, Integer type);
    public void saveDBMembershipItem(DBMembershipItem item);
    public  List getOrderedPermissionLevelNames(); 	
	 
}
