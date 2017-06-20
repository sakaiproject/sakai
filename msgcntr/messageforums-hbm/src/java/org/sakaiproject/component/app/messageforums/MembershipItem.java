/**********************************************************************************
 * $URL$
 * $Id$
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
package org.sakaiproject.component.app.messageforums;

import org.sakaiproject.id.cover.IdManager;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.user.api.User;

 /**
   * Recipient Item for storing different types of recipients user/group/role
   *
   */
 public class MembershipItem implements Comparable
 {
   
   /** in memory type sort */
   public static final Integer TYPE_NOT_SPECIFIED = Integer.valueOf(0); 
   public static final Integer TYPE_ALL_PARTICIPANTS = Integer.valueOf(1);
   public static final Integer TYPE_ROLE = Integer.valueOf(2);
   public static final Integer TYPE_GROUP = Integer.valueOf(3);
   public static final Integer TYPE_USER = Integer.valueOf(4);   
   public static final Integer TYPE_MYGROUPS = Integer.valueOf(5);
   public static final Integer TYPE_MYGROUPROLES = Integer.valueOf(6);
   public static final Integer TYPE_MYGROUPMEMBERS = Integer.valueOf(7);
   
   public static final String ALL_PARTICIPANTS_DESC = "All Participants";
   public static final String NOT_SPECIFIED_DESC = "Not Specified";

   /** generated id */
   private String id;
   
   private String name;   
   private Integer type;
   private Role role;
   private Group group;   
   private User user;
   private boolean viewable;
         
  private MembershipItem(){
  }
  
  public static MembershipItem getInstance(){
    MembershipItem item = new MembershipItem();
    item.id = IdManager.createUuid();
    return item;
  }
  
  public String getId()
  {
    return id;
  }     
   
  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public Role getRole()
  {
    return role;
  }

  public void setRole(Role role)
  {
    this.role = role;
  }  

  public User getUser()
  {
    return user;
  }

  public void setUser(User user)
  {
    this.user = user;
  }
  
  public Group getGroup()
  {
    return group;
  }

  public void setGroup(Group group)
  {
    this.group = group;
  }

  public Integer getType()
  {
    return type;
  }

  public void setType(Integer type)
  {
    this.type = type;
  }


  public boolean isViewable() {
	return viewable;
  }

  public void setViewable(boolean viewable) {
	this.viewable = viewable;
  }

/**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Object o)
  {    
    MembershipItem item = (MembershipItem) o;
    
    int typeCompareResult = type.compareTo(item.type);
    
    if (typeCompareResult != 0){
      return typeCompareResult;
    }
    else{
      return (this.name).toLowerCase().compareTo((item.name).toLowerCase());
    }        
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj)
  {
    if (obj == this){
      return true;
    }
        
    if (!(obj instanceof MembershipItem))
      return false;
    
    MembershipItem rcptObj = (MembershipItem) obj;
            
    return id == null ? rcptObj.id == null : id.equals(rcptObj.id);                
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  public int hashCode()
  {    
    return id.hashCode();
  }
    
}
 
