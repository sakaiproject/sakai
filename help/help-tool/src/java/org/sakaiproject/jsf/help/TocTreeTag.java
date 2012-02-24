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

package org.sakaiproject.jsf.help;

import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;

/**
 * toc tree tag
 * @version $Id$
 */
public class TocTreeTag extends UIComponentTag
{
  private String value = null;
  private String var = null;
  private String categories = null;

  /** 
   * @see javax.faces.webapp.UIComponentTag#getComponentType()
   */
  public String getComponentType()
  {
    return "javax.faces.Data";
  }

  /** 
   * @see javax.faces.webapp.UIComponentTag#getRendererType()
   */
  public String getRendererType()
  {
    return "SakaiTocTree";
  }

  /**
   * get categories 
   * @return categories
   */
  public String getCategories()
  {
    return categories;
  }

  /**
   * set categories
   * @param categories
   */
  public void setCategories(String categories)
  {
    this.categories = categories;
  }

  /** 
   * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
   */
  protected void setProperties(UIComponent component)
  {
    super.setProperties(component);
    FacesContext context = getFacesContext();
    if (value != null)
    {
      ValueBinding vb = context.getApplication().createValueBinding(value);
      component.setValueBinding("value", vb);
    }
    if (var != null)
    {
      ((UIData) component).setVar(var);
    }

  }

  /**
   * get value
   * @return Returns the value.
   */
  public String getValue()
  {
    return value;
  }

  /**
   * set value
   * @param value The value to set.
   */
  public void setValue(String value)
  {
    this.value = value;
  }

  /**
   * get var
   * @return Returns the var.
   */
  public String getVar()
  {
    return var;
  }

  /**
   * set var
   * @param var The var to set.
   */
  public void setVar(String var)
  {
    this.var = var;
  }
}