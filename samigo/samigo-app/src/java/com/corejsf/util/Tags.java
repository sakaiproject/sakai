/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
* Copyright (c) 2004 Sun Microsystems from the Java Series, Core Java ServerFaces
* source freely distributable.
* see http://www.sun.com/books/java_series.html
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
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

package com.corejsf.util;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

import jakarta.el.ELContext;
import jakarta.el.MethodExpression;
import jakarta.el.MethodInfo;
import jakarta.el.ValueExpression;
import jakarta.faces.application.Application;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.ValueChangeEvent;
import jakarta.faces.webapp.UIComponentTag;

public class Tags {

    private static boolean isEL(String v) {
      return v.startsWith("#{") && v.endsWith("}");
    }

   public static void setString(UIComponent component, String attributeName,
         String attributeValue) {
      if (attributeValue == null)
         return;
      if (isEL(attributeValue))
         setValueBinding(component, attributeName, attributeValue);
      else
         component.getAttributes().put(attributeName, attributeValue);
   }

   public static void setInteger(UIComponent component,
         String attributeName, String attributeValue) {
      if (attributeValue == null) return;
      if (isEL(attributeValue))
         setValueBinding(component, attributeName, attributeValue);
      else
         component.getAttributes().put(attributeName,
               new Integer(attributeValue));
   }

   public static void setDouble(UIComponent component,
         String attributeName, String attributeValue) {
      if (attributeValue == null) return;
      if (isEL(attributeValue))
         setValueBinding(component, attributeName, attributeValue);
      else
         component.getAttributes().put(attributeName,
               Double.valueOf(attributeValue));
   }

   public static void setBoolean(UIComponent component,
         String attributeName, String attributeValue) {
      if (attributeValue == null) return;
      if (isEL(attributeValue))
         setValueBinding(component, attributeName, attributeValue);
      else
         component.getAttributes().put(attributeName,
               Boolean.valueOf(attributeValue));
   }

   public static void setStrings(UIComponent component, Map map) {
      Iterator iter = map.entrySet().iterator();
      while (iter.hasNext()) {
         Map.Entry entry = (Map.Entry) iter.next();
         setString(component, (String) entry.getKey(),
               (String) entry.getValue());
      }
   }

   public static void setValueBinding(UIComponent component, String attributeName,
         String attributeValue) {
      FacesContext context = FacesContext.getCurrentInstance();
      Application app = context.getApplication();
      ValueExpression ve = context.getApplication().getExpressionFactory().createValueExpression(context.getELContext(), attributeValue, Object.class);
      component.setValueExpression(attributeName, ve);
   }

   public static void setActionListener(UIComponent component, String attributeValue) {
      setMethodBinding(component, "actionListener", attributeValue,
            new Class[] { ActionEvent.class });
   }

   public static void setValueChangeListener(UIComponent component,
         String attributeValue) {
      setMethodBinding(component, "valueChangeListener", attributeValue,
            new Class[] { ValueChangeEvent.class });
   }

   public static void setValidator(UIComponent component,
         String attributeValue) {
      setMethodBinding(component, "validator", attributeValue,
            new Class[] { FacesContext.class, UIComponent.class, Object.class });
   }

   public static void setAction(UIComponent component, String attributeValue) {
      if (attributeValue == null) return;
      if (isEL(attributeValue))
         setMethodBinding(component, "action", attributeValue,
               new Class[] {});
      else {
         FacesContext context = FacesContext.getCurrentInstance();
         Application app = context.getApplication();
         MethodExpression mb = new ActionMethodBinding(attributeValue);
         component.getAttributes().put("action", mb);
      }
   }

   public static void setMethodBinding(UIComponent component, String attributeName,
         String attributeValue, Class[] paramTypes) {
      if (attributeValue == null)
         return;
      if (isEL(attributeValue)) {
         FacesContext context = FacesContext.getCurrentInstance();
         Application app = context.getApplication();
         MethodExpression mb = context.getApplication().getExpressionFactory().createMethodExpression(context.getELContext(), attributeValue, Object.class, paramTypes);
         component.getAttributes().put(attributeName, mb);
      }
   }

   public static String eval(String expression) {
      if (expression == null) return null;
      if (isEL(expression)) {
         FacesContext context = FacesContext.getCurrentInstance();
         Application app = context.getApplication();
         ValueExpression ve = context.getApplication().getExpressionFactory().createValueExpression(context.getELContext(), expression, Object.class);
         return "" + ve.getValue(context.getELContext());
      }
      else return expression;
   }

   public static Integer evalInteger(String expression) {
      if (expression == null) return null;
      if (isEL(expression)) {
         FacesContext context = FacesContext.getCurrentInstance();
         Application app = context.getApplication();
         ValueExpression ve = app.getExpressionFactory().createValueExpression(context.getELContext(), expression, Object.class);
         Object r = ve.getValue(context.getELContext());
         if (r == null) return null;
         else if (r instanceof Integer) return (Integer) r;
         else return new Integer(r.toString());
      }
      else return new Integer(expression);
   }

   public static Double evalDouble(String expression) {
      if (expression == null) return null;
      if (isEL(expression)) {
         FacesContext context = FacesContext.getCurrentInstance();
         Application app = context.getApplication();
         ValueExpression ve = app.getExpressionFactory().createValueExpression(context.getELContext(), expression, Object.class);
         Object r = ve.getValue(context.getELContext());
         if (r == null) return null;
         else if (r instanceof Double) return (Double) r;
         else return new Double(r.toString());
      }
      else return new Double(expression);
   }

   public static Boolean evalBoolean(String expression) {
      if (expression == null) return null;
      if (isEL(expression)) {
         FacesContext context = FacesContext.getCurrentInstance();
         Application app = context.getApplication();
         ValueExpression ve = app.getExpressionFactory().createValueExpression(context.getELContext(), expression, Object.class);
         Object r = ve.getValue(context.getELContext());
         if (r == null) return null;
         else if (r instanceof Boolean) return (Boolean) r;
         else return Boolean.valueOf(r.toString());
      }
      else return Boolean.valueOf(expression);
   }

   private static class ActionMethodBinding
         extends MethodExpression implements Serializable {
      private String result;

      public ActionMethodBinding(String result) { this.result = result; }
      public Object invoke(FacesContext context, Object params[]) {
         return result;
      }
      public String getExpressionString() { return result; }
      public Class getType(FacesContext context) { return String.class; }

      @Override
      public MethodInfo getMethodInfo(ELContext context) {
        return null;
      }

      @Override
      public Object invoke(ELContext context, Object[] params) {
        return null;
      }

      @Override
      public boolean equals(Object obj) {
        return false;
      }

      @Override
      public int hashCode() {
        return 0;
      }

      @Override
      public boolean isLiteralText() {
        return false;
      }
   }
}
