// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.myfaces.custom.jslistener;

import jakarta.faces.component.UIComponent;
import jakarta.el.ValueExpression;
import jakarta.el.MethodExpression;
import jakarta.faces.context.FacesContext;


// Generated from class org.apache.myfaces.custom.jslistener.AbstractJsValueChangeListener.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class JsValueChangeListenerTag
    extends jakarta.faces.webapp.UIComponentELTag
{
    public JsValueChangeListenerTag()
    {    
    }
    
    public String getComponentType()
    {
        return "org.apache.myfaces.JsValueChangeListener";
    }

    public String getRendererType()
    {
        return "org.apache.myfaces.JsValueChangeListener";
    }

    private ValueExpression _for;
    
    public void setFor(ValueExpression forParam)
    {
        _for = forParam;
    }
    private ValueExpression _expressionValue;
    
    public void setExpressionValue(ValueExpression expressionValue)
    {
        _expressionValue = expressionValue;
    }
    private ValueExpression _property;
    
    public void setProperty(ValueExpression property)
    {
        _property = property;
    }
    private ValueExpression _bodyTagEvent;
    
    public void setBodyTagEvent(ValueExpression bodyTagEvent)
    {
        _bodyTagEvent = bodyTagEvent;
    }

    protected void setProperties(UIComponent component)
    {
        if (!(component instanceof org.apache.myfaces.custom.jslistener.JsValueChangeListener))
        {
            throw new IllegalArgumentException("Component "+
                component.getClass().getName() +" is no org.apache.myfaces.custom.jslistener.JsValueChangeListener");
        }
        
        org.apache.myfaces.custom.jslistener.JsValueChangeListener comp = (org.apache.myfaces.custom.jslistener.JsValueChangeListener) component;
        
        super.setProperties(component);
        
        FacesContext context = getFacesContext();

        if (_for != null)
        {
            comp.setValueExpression("for", _for);
        } 
        if (_expressionValue != null)
        {
            comp.setValueExpression("expressionValue", _expressionValue);
        } 
        if (_property != null)
        {
            comp.setValueExpression("property", _property);
        } 
        if (_bodyTagEvent != null)
        {
            comp.setValueExpression("bodyTagEvent", _bodyTagEvent);
        } 
    }

    public void release()
    {
        super.release();
        _for = null;
        _expressionValue = null;
        _property = null;
        _bodyTagEvent = null;
    }
}
