/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/signup/branches/2-6-x/api/src/java/org/sakaiproject/signup/restful/SignupGroupItem.java $
 * $Id: SignupGroupItem.java 59241 2009-03-24 15:52:18Z guangzheng.liu@yale.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008, 2009 Yale University
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
package org.sakaiproject.signup.restful;

/**
 * <p>
 * This class holds the information of sign-up group. It's a wrapper class for
 * SignupGroup
 * </p>
 */

public class SignupGroupItem {

	private String title;

	private String groupId;

	public SignupGroupItem(String title, String groupId) {
		this.title = title;
		this.groupId = groupId;
	}

	public String getGroupId() {
		return groupId;
	}

	/**
	 * this is a setter.
	 * 
	 * @param groupId
	 *            a unique group Id
	 */
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	/**
	 * get the title for the group
	 * 
	 * @return a title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * this is a setter.
	 * 
	 * @param title
	 *            a name for this group
	 */
	public void setTitle(String title) {
		this.title = title;
	}

}
