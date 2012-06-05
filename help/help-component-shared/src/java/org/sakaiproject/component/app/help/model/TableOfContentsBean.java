/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.component.app.help.model;

import java.util.HashSet;
import java.util.Set;

import org.sakaiproject.api.app.help.Category;
import org.sakaiproject.api.app.help.TableOfContents;

/**
 * table of contents bean
 * @version $Id$
 * 
 */
public class TableOfContentsBean implements TableOfContents
{

  private String name;
  private Set<Category> categories = new HashSet<Category>();

  /**
   * @see org.sakaiproject.api.app.help.TableOfContents#getName()
   */
  public String getName()
  {
    return name;
  }

  /**
   * @see org.sakaiproject.api.app.help.TableOfContents#setName(java.lang.String)
   */
  public void setName(String name)
  {
    this.name = name;

  }

  /**
   * @see org.sakaiproject.api.app.help.TableOfContents#getCategories()
   */
  public Set<Category> getCategories()
  {
    return categories;
  }

  /**
   * @see org.sakaiproject.api.app.help.TableOfContents#setCategories(java.util.Set)
   */
  public void setCategories(Set<Category> categories)
  {
    this.categories = categories;
  }

}


