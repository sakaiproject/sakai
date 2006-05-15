/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-hbm/src/java/org/sakaiproject/component/app/messageforums/dao/hibernate/DBMembershipItemImpl.java $
 * $Id: DBMembershipItemImpl.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
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
package org.sakaiproject.component.app.messageforums.dao.hibernate;

import org.sakaiproject.api.app.messageforums.DBMembershipItem;
import org.sakaiproject.api.app.messageforums.PermissionLevel;

 /**
   * DBMembership Item for storing different types of membership user/group/role
   *
   */
 public class DBMembershipItemImpl extends MutableEntityImpl 
                                   implements DBMembershipItem, Comparable
 {
               
   private String name;   
   private Integer type;
   private String permissionLevelName;
   private PermissionLevel permissionLevel;
        
  public DBMembershipItemImpl(){
     
  }
    
   
  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }
  
  public Integer getType()
  {
    return type;
  }

  public void setType(Integer type)
  {
    this.type = type;
  }
  
  public String getPermissionLevelName() {
		return permissionLevelName;
	}

	public void setPermissionLevelName(String permissionLevelName) {
		this.permissionLevelName = permissionLevelName;
	}	
  
  public PermissionLevel getPermissionLevel() {
		return permissionLevel;
	}

  public void setPermissionLevel(PermissionLevel permissionLevel) {
		this.permissionLevel = permissionLevel;
	} 


  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Object o)
  {    
    DBMembershipItemImpl item = (DBMembershipItemImpl) o;
    
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
        
    if (!(obj instanceof DBMembershipItemImpl))
      return false;
    
    DBMembershipItemImpl dbmi = (DBMembershipItemImpl) obj;
            
    return id == null ? dbmi.id == null : id.equals(dbmi.id);                
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  public int hashCode()
  {    
    return id.hashCode();
  }
	   
}
 