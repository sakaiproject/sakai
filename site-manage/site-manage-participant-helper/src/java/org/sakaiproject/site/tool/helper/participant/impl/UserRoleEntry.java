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
	  
	  public UserRoleEntry ()
	  {
		  userEId = "";
		  role = "";
	  }
	  
	  public UserRoleEntry(String eid, String r)
	  {
		  userEId = eid;
		  role = r;
	  }
	  
}

