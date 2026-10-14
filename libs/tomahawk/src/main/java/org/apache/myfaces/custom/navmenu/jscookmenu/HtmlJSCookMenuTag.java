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
package org.apache.myfaces.custom.navmenu.jscookmenu;

import jakarta.faces.component.UIComponent;
import jakarta.el.ValueExpression;
import jakarta.el.MethodExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.UIComponent;
import jakarta.el.MethodExpression;
import jakarta.faces.event.MethodExpressionActionListener;
import jakarta.faces.el.MethodBinding;


// Generated from class org.apache.myfaces.custom.navmenu.jscookmenu.AbstractHtmlCommandJSCookMenu.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class HtmlJSCookMenuTag
    extends jakarta.faces.webapp.UIComponentELTag
{
    public HtmlJSCookMenuTag()
    {    
    }
    
    public String getComponentType()
    {
        return "org.apache.myfaces.JSCookMenu";
    }

    public String getRendererType()
    {
        return "org.apache.myfaces.JSCookMenu";
    }

    private ValueExpression _layout;
    
    public void setLayout(ValueExpression layout)
    {
        _layout = layout;
    }
    private ValueExpression _theme;
    
    public void setTheme(ValueExpression theme)
    {
        _theme = theme;
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
    private ValueExpression _javascriptLocation;
    
    public void setJavascriptLocation(ValueExpression javascriptLocation)
    {
        _javascriptLocation = javascriptLocation;
    }
    private ValueExpression _imageLocation;
    
    public void setImageLocation(ValueExpression imageLocation)
    {
        _imageLocation = imageLocation;
    }
    private ValueExpression _styleLocation;
    
    public void setStyleLocation(ValueExpression styleLocation)
    {
        _styleLocation = styleLocation;
    }
    private ValueExpression _javascriptLibrary;
    
    public void setJavascriptLibrary(ValueExpression javascriptLibrary)
    {
        _javascriptLibrary = javascriptLibrary;
    }
    private ValueExpression _imageLibrary;
    
    public void setImageLibrary(ValueExpression imageLibrary)
    {
        _imageLibrary = imageLibrary;
    }
    private ValueExpression _styleLibrary;
    
    public void setStyleLibrary(ValueExpression styleLibrary)
    {
        _styleLibrary = styleLibrary;
    }
    private ValueExpression _immediate;
    
    public void setImmediate(ValueExpression immediate)
    {
        _immediate = immediate;
    }
    private MethodExpression _actionExpression;
    
    public void setAction(MethodExpression actionExpression)
    {
        _actionExpression = actionExpression;
    }

    protected void setProperties(UIComponent component)
    {
        if (!(component instanceof org.apache.myfaces.custom.navmenu.jscookmenu.HtmlCommandJSCookMenu))
        {
            throw new IllegalArgumentException("Component "+
                component.getClass().getName() +" is no org.apache.myfaces.custom.navmenu.jscookmenu.HtmlCommandJSCookMenu");
        }
        
        org.apache.myfaces.custom.navmenu.jscookmenu.HtmlCommandJSCookMenu comp = (org.apache.myfaces.custom.navmenu.jscookmenu.HtmlCommandJSCookMenu) component;
        
        super.setProperties(component);
        
        FacesContext context = getFacesContext();

        if (_layout != null)
        {
            comp.setValueExpression("layout", _layout);
        } 
        if (_theme != null)
        {
            comp.setValueExpression("theme", _theme);
        } 
        if (_enabledOnUserRole != null)
        {
            comp.setValueExpression("enabledOnUserRole", _enabledOnUserRole);
        } 
        if (_visibleOnUserRole != null)
        {
            comp.setValueExpression("visibleOnUserRole", _visibleOnUserRole);
        } 
        if (_javascriptLocation != null)
        {
            comp.setValueExpression("javascriptLocation", _javascriptLocation);
        } 
        if (_imageLocation != null)
        {
            comp.setValueExpression("imageLocation", _imageLocation);
        } 
        if (_styleLocation != null)
        {
            comp.setValueExpression("styleLocation", _styleLocation);
        } 
        if (_javascriptLibrary != null)
        {
            comp.setValueExpression("javascriptLibrary", _javascriptLibrary);
        } 
        if (_imageLibrary != null)
        {
            comp.setValueExpression("imageLibrary", _imageLibrary);
        } 
        if (_styleLibrary != null)
        {
            comp.setValueExpression("styleLibrary", _styleLibrary);
        } 
        if (_immediate != null)
        {
            comp.setValueExpression("immediate", _immediate);
        } 
        if (_actionExpression != null)
        {
            comp.setActionExpression(_actionExpression);
        }        
    }

    public void release()
    {
        super.release();
        _layout = null;
        _theme = null;
        _enabledOnUserRole = null;
        _visibleOnUserRole = null;
        _javascriptLocation = null;
        _imageLocation = null;
        _styleLocation = null;
        _javascriptLibrary = null;
        _imageLibrary = null;
        _styleLibrary = null;
        _immediate = null;
        _actionExpression = null;
    }
}
