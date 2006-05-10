/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
* 
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
* 
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
