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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.api.app.help.Source;

/**
 * source bean
 * @version $Id$
 */
public class SourceBean implements Source
{
  private Long id;
  private Map attributes = new HashMap();
  private String name;
  private Set urlAppenders;

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
   * @see org.sakaiproject.api.app.help.Source#getAttributes()
   */
  public Map getAttributes()
  {
    return attributes;
  }

  /**
   * @see org.sakaiproject.api.app.help.Source#setAttributes(java.util.Map)
   */
  public void setAttributes(Map attributes)
  {
    this.attributes = attributes;
  }

  /**
   * @see org.sakaiproject.api.app.help.Source#getName()
   */
  public String getName()
  {
    return name;
  }

  /**
   * @see org.sakaiproject.api.app.help.Source#setName(java.lang.String)
   */
  public void setName(String name)
  {
    this.name = name;
  }

  /**
   * @see org.sakaiproject.api.app.help.Source#getUrlAppenders()
   */
  public Set getUrlAppenders()
  {
    return urlAppenders;
  }

  /**
   * @see org.sakaiproject.api.app.help.Source#setUrlAppenders(java.util.Set)
   */
  public void setUrlAppenders(Set urlAppenders)
  {
    this.urlAppenders = urlAppenders;
  }

}


