/*
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
 */

package org.sakaiproject.tool.assessment.jsf.tag;

import java.io.Serializable;
import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.webapp.UIComponentTag;
import java.util.HashMap;

/**
 *
 * <p> Previously PagerTagUtil</p>
 * <p> Based on code from "Core JSF"</p>
 * <p>Copyright: Copyright (c) 2004 Sakai</p>
 * <p> </p>
 * @author Ed Smiley
 * @version $Id: TagUtil.java,v 1.2 2004/10/20 22:51:49 esmiley.stanford.edu Exp $
 */

public class TagUtil
{

  public static void setString(UIComponent component, String attributeName,
    String attributeValue)
  {
    if (attributeValue == null)
    {
      return;
    }
    if (UIComponentTag.isValueReference(attributeValue))
    {
      setValueBinding(component, attributeName, attributeValue);
    }
    else
    {
      component.getAttributes().put(attributeName, attributeValue);
    }
  }

  public static void setInteger(UIComponent component,
    String attributeName, String attributeValue)
  {
    if (attributeValue == null)
    {
      return;
    }
    if (UIComponentTag.isValueReference(attributeValue))
    {
      setValueBinding(component, attributeName, attributeValue);
    }
    else
    {
      component.getAttributes().put(attributeName,
        new Integer(attributeValue));
    }
  }

  public static void setMap(UIComponent component,
    String attributeName, String attributeValue)
  {
    if (attributeValue == null)
    {
      return;
    }
    if (UIComponentTag.isValueReference(attributeValue))
    {
      setValueBinding(component, attributeName, attributeValue);
    }
    else
    {
      component.getAttributes().put(attributeName,
        new HashMap());
    }
  }

  public static void setDouble(UIComponent component,
    String attributeName, String attributeValue)
  {
    if (attributeValue == null)
    {
      return;
    }
    if (UIComponentTag.isValueReference(attributeValue))
    {
      setValueBinding(component, attributeName, attributeValue);
    }
    else
    {
      component.getAttributes().put(attributeName,
        new Double(attributeValue));
    }
  }

  public static void setBoolean(UIComponent component,
    String attributeName, String attributeValue)
  {
    if (attributeValue == null)
    {
      return;
    }
    if (UIComponentTag.isValueReference(attributeValue))
    {
      setValueBinding(component, attributeName, attributeValue);
    }
    else
    {
      component.getAttributes().put(attributeName,
        new Boolean(attributeValue));
    }
  }

  public static void setValueBinding(UIComponent component,
    String attributeName,
    String attributeValue)
  {
    FacesContext context = FacesContext.getCurrentInstance();
    Application app = context.getApplication();
    ValueBinding vb = app.createValueBinding(attributeValue);
    component.setValueBinding(attributeName, vb);
  }

  public static void setActionListener(UIComponent component,
    String attributeValue)
  {
    setMethodBinding(component, "actionListener", attributeValue,
      new Class[]
      {
      ActionEvent.class});
  }

  public static void setValueChangeListener(UIComponent component,
    String attributeValue)
  {
    setMethodBinding(component, "valueChangeListener", attributeValue,
      new Class[]
      {
      ValueChangeEvent.class});
  }

  public static void setValidator(UIComponent component,
    String attributeValue)
  {
    setMethodBinding(component, "validator", attributeValue,
      new Class[]
      {
      FacesContext.class, UIComponent.class, Object.class});
  }

  public static void setAction(UIComponent component,
    String attributeValue)
  {
    if (attributeValue == null)
    {
      return;
    }
    if (UIComponentTag.isValueReference(attributeValue))
    {
      setMethodBinding(component, "action", attributeValue,
        new Class[]
        {});
    }
    else
    {
      FacesContext context = FacesContext.getCurrentInstance();
      Application app = context.getApplication();
      MethodBinding mb = new ActionMethodBinding(attributeValue);
      component.getAttributes().put("action", mb);
    }
  }

  public static void setMethodBinding(UIComponent component,
    String attributeName,
    String attributeValue, Class[] paramTypes)
  {
    if (attributeValue == null)
    {
      return;
    }
    if (UIComponentTag.isValueReference(attributeValue))
    {
      FacesContext context = FacesContext.getCurrentInstance();
      Application app = context.getApplication();
      MethodBinding mb = app.createMethodBinding(attributeValue,
                         paramTypes);
      component.getAttributes().put(attributeName, mb);
    }
  }

  public static String eval(String expression)
  {
    if (expression == null)
    {
      return null;
    }
    if (UIComponentTag.isValueReference(expression))
    {
      FacesContext context = FacesContext.getCurrentInstance();
      Application app = context.getApplication();
      return "" + app.createValueBinding(expression).getValue(context);
    }
    else
    {
      return expression;
    }
  }

  public static Integer evalInteger(String expression)
  {
    if (expression == null)
    {
      return null;
    }
    if (UIComponentTag.isValueReference(expression))
    {
      FacesContext context = FacesContext.getCurrentInstance();
      Application app = context.getApplication();
      Object r = app.createValueBinding(expression).getValue(context);
      if (r == null)
      {
        return null;
      }
      else if (r instanceof Integer)
      {
        return (Integer) r;
      }
      else
      {
        return new Integer(r.toString());
      }
    }
    else
    {
      return new Integer(expression);
    }
  }

  public static Double evalDouble(String expression)
  {
    if (expression == null)
    {
      return null;
    }
    if (UIComponentTag.isValueReference(expression))
    {
      FacesContext context = FacesContext.getCurrentInstance();
      Application app = context.getApplication();
      Object r = app.createValueBinding(expression).getValue(context);
      if (r == null)
      {
        return null;
      }
      else if (r instanceof Double)
      {
        return (Double) r;
      }
      else
      {
        return new Double(r.toString());
      }
    }
    else
    {
      return new Double(expression);
    }
  }

  public static Boolean evalBoolean(String expression)
  {
    if (expression == null)
    {
      return null;
    }
    if (UIComponentTag.isValueReference(expression))
    {
      FacesContext context = FacesContext.getCurrentInstance();
      Application app = context.getApplication();
      Object r = app.createValueBinding(expression).getValue(context);
      if (r == null)
      {
        return null;
      }
      else if (r instanceof Boolean)
      {
        return (Boolean) r;
      }
      else
      {
        return new Boolean(r.toString());
      }
    }
    else
    {
      return new Boolean(expression);
    }
  }

  private static class ActionMethodBinding extends MethodBinding implements
    Serializable
  {
    private String result;

    public ActionMethodBinding(String result)
    {
      this.result = result;
    }

    public Object invoke(FacesContext context, Object params[])
    {
      return result;
    }

    public String getExpressionString()
    {
      return result;
    }

    public Class getType(FacesContext context)
    {
      return String.class;
    }
  }
}
