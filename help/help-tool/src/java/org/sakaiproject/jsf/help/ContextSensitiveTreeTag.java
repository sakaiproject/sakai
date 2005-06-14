/**********************************************************************************
 *
 * $Header: /cvs/sakai2/help/help-tool/src/java/org/sakaiproject/jsf/help/ContextSensitiveTreeTag.java,v 1.1 2005/05/15 23:03:52 jlannan.iupui.edu Exp $
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

package org.sakaiproject.jsf.help;

import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;

/**
 * context sensitive UI component tag
 * @version $Id: ContextSensitiveTreeTag.java,v 1.1 2005/05/15 23:03:52 jlannan.iupui.edu Exp $
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