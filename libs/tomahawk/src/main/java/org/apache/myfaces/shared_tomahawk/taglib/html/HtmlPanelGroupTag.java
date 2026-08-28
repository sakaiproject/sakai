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
package org.apache.myfaces.shared_tomahawk.taglib.html;

import jakarta.faces.component.UIComponent;
import jakarta.el.ValueExpression;
import jakarta.el.MethodExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.UIComponent;


// Generated from class jakarta.faces.component.html._HtmlPanelGroup.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class HtmlPanelGroupTag
    extends jakarta.faces.webapp.UIComponentELTag
{
    public HtmlPanelGroupTag()
    {    
    }
    
    public String getComponentType()
    {
        return "jakarta.faces.HtmlPanelGroup";
    }

    public String getRendererType()
    {
        return "jakarta.faces.Group";
    }

    private ValueExpression _layout;
    
    public void setLayout(ValueExpression layout)
    {
        _layout = layout;
    }
    private ValueExpression _style;
    
    public void setStyle(ValueExpression style)
    {
        _style = style;
    }
    private ValueExpression _styleClass;
    
    public void setStyleClass(ValueExpression styleClass)
    {
        _styleClass = styleClass;
    }

    protected void setProperties(UIComponent component)
    {
        if (!(component instanceof jakarta.faces.component.html.HtmlPanelGroup))
        {
            throw new IllegalArgumentException("Component "+
                component.getClass().getName() +" is no jakarta.faces.component.html.HtmlPanelGroup");
        }
        
        jakarta.faces.component.html.HtmlPanelGroup comp = (jakarta.faces.component.html.HtmlPanelGroup) component;
        
        super.setProperties(component);
        
        FacesContext context = getFacesContext();

        if (_layout != null)
        {
            comp.setValueExpression("layout", _layout);
        } 
        if (_style != null)
        {
            comp.setValueExpression("style", _style);
        } 
        if (_styleClass != null)
        {
            comp.setValueExpression("styleClass", _styleClass);
        } 
    }

    public void release()
    {
        super.release();
        _layout = null;
        _style = null;
        _styleClass = null;
    }
}
