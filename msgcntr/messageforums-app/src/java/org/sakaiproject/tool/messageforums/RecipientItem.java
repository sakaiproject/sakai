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

package org.sakaiproject.tool.messageforums;

import org.sakaiproject.service.legacy.authzGroup.Role;
import org.sakaiproject.service.legacy.user.User;

 /**
   * Participant in site access roles
   *
   */
 public class RecipientItem implements Comparable
 {
   private String name;
   private String uniqueName;
   private Role role;
   private User user;
         
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

  public String getUniqueName()
  {
    return uniqueName;
  }

  public void setUniqueName(String uniqueName)
  {
    this.uniqueName = uniqueName;
  }

  public User getUser()
  {
    return user;
  }

  public void setUser(User user)
  {
    this.user = user;
  }

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Object o)
  {    
    RecipientItem p = (RecipientItem) o;
    return this.name.compareTo(p.name);
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj)
  {
    if (obj == this){
      return true;
    }
        
    if (!(obj instanceof RecipientItem))
      return false;
    
    RecipientItem rcptObj = (RecipientItem) obj;
        
    return uniqueName == null ? rcptObj.uniqueName == null : uniqueName.equals(rcptObj.uniqueName);
  }    
    
}