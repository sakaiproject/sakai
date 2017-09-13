/**
 * Copyright (c) 2003-2009 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.site.tool.helper.participant.impl;

/**
 * 
 * @author 
 *
 */
public class UserRoleEntry {
	
	  /** The user eid **/
	  public String userEId;
	  
	  /** The desired role for this user **/
	  public String role;
	  
	  /** The user first name **/
	  public String firstName;
	  
	  /** The user last name **/
	  public String lastName;
	  
	  /**
	   * constructor with no params
	   */
	  public UserRoleEntry ()
	  {
		  userEId = "";
		  role = "";
		  firstName = "";
		  lastName = "";
	  }
	  
	  /**
	   * constructor with only two params
	   * @param eid
	   * @param r
	   */
	  public UserRoleEntry(String eid, String r)
	  {
		  userEId = eid;
		  role = r;
		  firstName = "";
		  lastName = "";
	  }
	  
	  /**
	   * constructor with four params
	   * @param eid
	   * @param r
	   * @param fName
	   * @param lName
	   */
	  public UserRoleEntry(String eid, String r, String fName, String lName)
	  {
		  userEId = eid;
		  role = r;
		  firstName = fName;
		  lastName = lName;
	  }
	  
}

