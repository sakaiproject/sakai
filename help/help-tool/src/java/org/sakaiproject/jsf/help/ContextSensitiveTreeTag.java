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
 * context sensitive UI component tag
 * @version $Id$
 */
public class ContextSensitiveTreeTag extends UIComponentTag
{
  private String value = null;
  private String var = null;
  private String helpDocId = null;

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
    return "CSTree";
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

    if (getHelpDocId() != null)
    {
      if (isValueReference(getHelpDocId()))
      {
        component.setValueBinding("helpDocId", getFacesContext()
            .getApplication().createValueBinding(getHelpDocId()));
      }
      else
      {
        component.getAttributes().put("helpDocId", getHelpDocId());
      }
    }
  }

  /**
   * get help doc id
   * @return Returns the helpDocId.
   */
  public String getHelpDocId()
  {
    return helpDocId;
  }

  /**
   * @param helpDocId The helpDocId to set.
   */
  public void setHelpDocId(String helpDocId)
  {
    this.helpDocId = helpDocId;
  }

  /**
   * @return Returns the value.
   */
  public String getValue()
  {
    return value;
  }

  /**
   * @param value The value to set.
   */
  public void setValue(String value)
  {
    this.value = value;
  }

  /**
   * @return Returns the var.
   */
  public String getVar()
  {
    return var;
  }

  /**
   * @param var The var to set.
   */
  public void setVar(String var)
  {
    this.var = var;
  }
}