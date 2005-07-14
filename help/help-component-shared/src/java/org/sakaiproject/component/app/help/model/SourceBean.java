/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
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


