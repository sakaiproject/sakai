/**********************************************************************************
 *
 * $Header: /cvs/sakai2/help/help-component-shared/src/java/org/sakaiproject/component/app/help/model/CategoryBean.java,v 1.1 2005/05/19 15:39:14 jlannan.iupui.edu Exp $
 *
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

package org.sakaiproject.component.app.help.model;

import java.util.HashSet;
import java.util.Set;

import org.sakaiproject.api.app.help.Category;

/**
 * category bean
 * @version $Id$
 */
public class CategoryBean implements Category, Comparable
{
  private Long id;
  private String name;
  private Set resources = new HashSet();
  private Set categories = new HashSet();
  private Category parent;

  /**
   * get id
   * @return Returns the id.
   */
  public Long getId()
  {
    return id;
  }

  /**
   * set id
   * @param id The id to set.
   */
  public void setId(Long id)
  {
    this.id = id;
  }

  /**
   * @see org.sakaiproject.api.app.help.Category#getCategories()
   */
  public Set getCategories()
  {
    return categories;
  }

  /**
   * @see org.sakaiproject.api.app.help.Category#getName()
   */
  public String getName()
  {
    return name;
  }

  /**
   * @see org.sakaiproject.api.app.help.Category#getResources()
   */
  public Set getResources()
  {
    return resources;
  }

  /**
   * @see org.sakaiproject.api.app.help.Category#setCategories(java.util.Set)
   */
  public void setCategories(Set categories)
  {
    this.categories = categories;
  }

  /**
   * @see org.sakaiproject.api.help.Category#setName(java.lang.String)
   */
  public void setName(String name)
  {
    this.name = name;
  }

  /**
   * @see org.sakaiproject.api.app.help.Category#setResources(java.util.Set)
   */
  public void setResources(Set resources)
  {
    this.resources = resources;
  }
  
  /**
   * @see org.sakaiproject.api.app.help.Category#getParent()
   */
  public Category getParent()
  {
    return parent;
  }
  public void setParent(Category parent)
  {   
    this.parent = parent;
  }
  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj)
  {
    if (this == obj) return true;
    if (!(obj instanceof CategoryBean)) return false;
    CategoryBean other = (CategoryBean) obj;
    return this.name.equals(other.name);
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  public int hashCode()
  {
    return name.hashCode();
  }

  public int compareTo(Object o)
  {
    CategoryBean cb = (CategoryBean) o;
    return name.compareTo(cb.name);
  }
}

/**********************************************************************************
 *
 * $Header: /cvs/sakai2/help/help-component-shared/src/java/org/sakaiproject/component/app/help/model/CategoryBean.java,v 1.1 2005/05/19 15:39:14 jlannan.iupui.edu Exp $
 *
 **********************************************************************************/