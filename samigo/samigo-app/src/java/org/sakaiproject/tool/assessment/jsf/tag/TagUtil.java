/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008, 2009 The Sakai Foundation
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
 * @version $Id$
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
        Boolean.valueOf(attributeValue));
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
      //FacesContext context = FacesContext.getCurrentInstance();
      //Application app = context.getApplication();
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
      return Boolean.FALSE;
    }
    if (UIComponentTag.isValueReference(expression))
    {
      FacesContext context = FacesContext.getCurrentInstance();
      Application app = context.getApplication();
      Object r = app.createValueBinding(expression).getValue(context);
      if (r == null)
      {
        return Boolean.FALSE;
      }
      else if (r instanceof Boolean)
      {
        return (Boolean) r;
      }
      else
      {
        return Boolean.valueOf(r.toString());
      }
    }
    else
    {
      return  Boolean.valueOf(expression);
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
