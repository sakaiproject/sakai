/**********************************************************************************
* $URL$
* $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.component.app.messageforums;

import org.sakaiproject.api.kernel.id.cover.IdManager;
import org.sakaiproject.service.legacy.authzGroup.Role;
import org.sakaiproject.service.legacy.site.Group;
import org.sakaiproject.service.legacy.user.User;

 /**
   * Recipient Item for storing different types of recipients user/group/role
   *
   */
 public class MembershipItem implements Comparable
 {
   
   /** in memory type sort */
   public static final Integer TYPE_ALL_PARTICIPANTS = new Integer(1);
   public static final Integer TYPE_ROLE = new Integer(2);
   public static final Integer TYPE_GROUP = new Integer(3);
   public static final Integer TYPE_USER = new Integer(4);   
   
   /** generated id */
   private String id;
   
   private String name;   
   private Integer type;
   private Role role;
   private Group group;   
   private User user;
         
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
      return this.name.compareTo(item.name);
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
 