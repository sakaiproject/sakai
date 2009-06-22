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

