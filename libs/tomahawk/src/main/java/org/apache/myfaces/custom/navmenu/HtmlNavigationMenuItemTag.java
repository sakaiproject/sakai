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
package org.apache.myfaces.custom.navmenu;

import jakarta.faces.component.UIComponent;
import jakarta.el.ValueExpression;
import jakarta.el.MethodExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.UIComponent;
import jakarta.el.MethodExpression;
import jakarta.faces.event.MethodExpressionActionListener;
import jakarta.faces.el.MethodBinding;


// Generated from class org.apache.myfaces.custom.navmenu.AbstractUINavigationMenuItem.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class HtmlNavigationMenuItemTag
    extends org.apache.myfaces.shared_tomahawk.taglib.core.SelectItemTag
{
    public HtmlNavigationMenuItemTag()
    {    
    }
    
    public String getComponentType()
    {
        return "org.apache.myfaces.NavigationMenuItem";
    }

    public String getRendererType()
    {
        return null;
    }

    private ValueExpression _icon;
    
    public void setIcon(ValueExpression icon)
    {
        _icon = icon;
    }
    private ValueExpression _split;
    
    public void setSplit(ValueExpression split)
    {
        _split = split;
    }
    private MethodExpression _actionExpression;
    
    public void setAction(MethodExpression actionExpression)
    {
        _actionExpression = actionExpression;
    }
    private jakarta.el.MethodExpression _actionListener;
    
    public void setActionListener(jakarta.el.MethodExpression actionListener)
    {
        _actionListener = actionListener;
    }
    private ValueExpression _target;
    
    public void setTarget(ValueExpression target)
    {
        _target = target;
    }
    private ValueExpression _disabled;
    
    public void setDisabled(ValueExpression disabled)
    {
        _disabled = disabled;
    }
    private ValueExpression _disabledStyle;
    
    public void setDisabledStyle(ValueExpression disabledStyle)
    {
        _disabledStyle = disabledStyle;
    }
    private ValueExpression _disabledStyleClass;
    
    public void setDisabledStyleClass(ValueExpression disabledStyleClass)
    {
        _disabledStyleClass = disabledStyleClass;
    }
    private ValueExpression _enabledOnUserRole;
    
    public void setEnabledOnUserRole(ValueExpression enabledOnUserRole)
    {
        _enabledOnUserRole = enabledOnUserRole;
    }
    private ValueExpression _visibleOnUserRole;
    
    public void setVisibleOnUserRole(ValueExpression visibleOnUserRole)
    {
        _visibleOnUserRole = visibleOnUserRole;
    }

    protected void setProperties(UIComponent component)
    {
        if (!(component instanceof org.apache.myfaces.custom.navmenu.UINavigationMenuItem))
        {
            throw new IllegalArgumentException("Component "+
                component.getClass().getName() +" is no org.apache.myfaces.custom.navmenu.UINavigationMenuItem");
        }
        
        org.apache.myfaces.custom.navmenu.UINavigationMenuItem comp = (org.apache.myfaces.custom.navmenu.UINavigationMenuItem) component;
        
        super.setProperties(component);
        
        FacesContext context = getFacesContext();

        if (_icon != null)
        {
            comp.setValueExpression("icon", _icon);
        } 
        if (_split != null)
        {
            comp.setValueExpression("split", _split);
        } 
        if (_actionExpression != null)
        {
            comp.setActionExpression(_actionExpression);
        }        
        if (_actionListener != null)
        {
            comp.addActionListener(new MethodExpressionActionListener(_actionListener));
        }
        if (_target != null)
        {
            comp.setValueExpression("target", _target);
        } 
        if (_disabled != null)
        {
            comp.setValueExpression("disabled", _disabled);
        } 
        if (_disabledStyle != null)
        {
            comp.setValueExpression("disabledStyle", _disabledStyle);
        } 
        if (_disabledStyleClass != null)
        {
            comp.setValueExpression("disabledStyleClass", _disabledStyleClass);
        } 
        if (_enabledOnUserRole != null)
        {
            comp.setValueExpression("enabledOnUserRole", _enabledOnUserRole);
        } 
        if (_visibleOnUserRole != null)
        {
            comp.setValueExpression("visibleOnUserRole", _visibleOnUserRole);
        } 
    }

    public void release()
    {
        super.release();
        _icon = null;
        _split = null;
        _actionExpression = null;
        _actionListener = null;
        _target = null;
        _disabled = null;
        _disabledStyle = null;
        _disabledStyleClass = null;
        _enabledOnUserRole = null;
        _visibleOnUserRole = null;
    }
}
