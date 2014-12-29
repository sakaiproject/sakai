/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
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

package org.sakaiproject.jsf.util;

import java.io.Serializable;
import java.util.HashMap;
import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.webapp.UIComponentTag;

/**
 * Common static utility methods that help in implementing JSF tags.
 */
public class TagUtil
{

    /** This class is meant for static use only */
    private TagUtil()
    {
    }

   /**
    * Set a string value on a component - used by tags setProperties() method.
    * Handles value bindings.
    */
   public static void setString(UIComponent component, String name, String value)
   {
       if (value == null)
       {
           return;
       }
       if (UIComponentTag.isValueReference(value))
       {
           setValueBinding(component, name, value);
       } else
       {
           component.getAttributes().put(name, value);
       }
   }

   /**
    * Set a string value on a component - used by tags setProperties() method.
    * Handles value bindings.
    */
   public static void setObject(UIComponent component, String name, String value)
   {
       if (value == null)
       {
           return;
       }
       if (UIComponentTag.isValueReference(value))
       {
           setValueBinding(component, name, value);
       } else
       {
           component.getAttributes().put(name, value);
       }
   }

    /**
     * Set an integer value on a component - used by tags setProperties()
     * method. Handles value bindings.
     */
    public static void setInteger(UIComponent component, String name, String value)
    {
        if (value == null)
        {
            return;
        }
        if (UIComponentTag.isValueReference(value))
        {
            setValueBinding(component, name, value);
        } else
        {
            component.getAttributes().put(name, Integer.valueOf(value));
        }
    }

    /**
     * Set a Map value on a component - used by tags setProperties() method.
     * Handles value bindings.
     */
    public static void setMap(UIComponent component, String name, String value)
    {
        if (value == null)
        {
            return;
        }
        if (UIComponentTag.isValueReference(value))
        {
            setValueBinding(component, name, value);
        } else
        {
            component.getAttributes().put(name, new HashMap());
        }
    }

    /**
     * Set a double value on a component - used by tags setProperties() method.
     * Handles value bindings.
     */
    public static void setDouble(UIComponent component, String name, String value)
    {
        if (value == null)
        {
            return;
        }
        if (UIComponentTag.isValueReference(value))
        {
            setValueBinding(component, name, value);
        } else
        {
            component.getAttributes().put(name, Double.valueOf(value));
        }
    }

    /**
     * Set a boolean value on a component - used by tags setProperties() method.
     * Handles value bindings.
     */
    public static void setBoolean(UIComponent component, String name, String value)
    {
        if (value == null)
        {
            return;
        }
        if (UIComponentTag.isValueReference(value))
        {
            setValueBinding(component, name, value);
        } else
        {
            component.getAttributes().put(name, Boolean.valueOf(value));
        }
    }

    /**
     * Set a ValueBinding on a component - used by tags setProperties() method.
     */
    public static void setValueBinding(UIComponent component, String name, String value)
    {
        FacesContext context = FacesContext.getCurrentInstance();
        Application app = context.getApplication();
        ValueBinding vb = app.createValueBinding(value);
        component.setValueBinding(name, vb);
    }

    /**
     * Set an ActionListener on a component - used by tags setProperties()
     * method. Handles method bindings.
     */
    public static void setActionListener(UIComponent component, String value)
    {
        setMethodBinding(component, "actionListener", value, new Class[] { ActionEvent.class });
    }

    /**
     * Set a ValueChangeListener on a component - used by tags setProperties()
     * method. Handles method bindings.
     */
    public static void setValueChangeListener(UIComponent component, String value)
    {
        setMethodBinding(component, "valueChangeListener", value,
                new Class[] { ValueChangeEvent.class });
    }

    /**
     * Set a Validator on a component - used by tags setProperties() method.
     * Handles method bindings.
     */
    public static void setValidator(UIComponent component, String value)
    {
        setMethodBinding(component, "validator", value, new Class[] { FacesContext.class,
                UIComponent.class, Object.class });
    }

    /**
     * Set an action on a component - used by tags setProperties() method.
     * Handles method bindings.
     */
    public static void setAction(UIComponent component, String value)
    {
        if (value == null)
        {
            return;
        }
        if (UIComponentTag.isValueReference(value))
        {
            setMethodBinding(component, "action", value, new Class[] {});
        } else
        {
            FacesContext context = FacesContext.getCurrentInstance();
            Application app = context.getApplication();
            MethodBinding mb = new ActionMethodBinding(value);
            component.getAttributes().put("action", mb);
        }
    }

    /**
     * Set a MethodBinding on a component - used by tags setProperties() method.
     */
    public static void setMethodBinding(UIComponent component, String name, String value,
            Class[] paramTypes)
    {
        if (value == null)
        {
            return;
        }
        if (UIComponentTag.isValueReference(value))
        {
            FacesContext context = FacesContext.getCurrentInstance();
            Application app = context.getApplication();
            MethodBinding mb = app.createMethodBinding(value, paramTypes);
            component.getAttributes().put(name, mb);
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
        } else
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
            } else if (r instanceof Integer)
            {
                return (Integer) r;
            } else
            {
                return Integer.valueOf(r.toString());
            }
        } else
        {
            return Integer.valueOf(expression);
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
            } else if (r instanceof Double)
            {
                return (Double) r;
            } else
            {
                return Double.valueOf(r.toString());
            }
        } else
        {
            return Double.valueOf(expression);
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
            } else if (r instanceof Boolean)
            {
                return (Boolean) r;
            } else
            {
                return Boolean.valueOf(r.toString());
            }
        } else
        {
            return Boolean.valueOf(expression);
        }
    }

    /**
     * A shortcut MethodBinding which just returns a single string result -
     * useful when an action should just return a certain result, not call a
     * method.
     */
    private static class ActionMethodBinding extends MethodBinding implements Serializable
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
