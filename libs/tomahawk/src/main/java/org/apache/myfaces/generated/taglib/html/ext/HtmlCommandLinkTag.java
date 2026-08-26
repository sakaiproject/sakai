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
package org.apache.myfaces.generated.taglib.html.ext;

import jakarta.faces.component.UIComponent;
import jakarta.el.ValueExpression;
import jakarta.el.MethodExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.UIComponent;
import jakarta.el.MethodExpression;
import jakarta.faces.event.MethodExpressionActionListener;
import jakarta.faces.el.MethodBinding;


// Generated from class org.apache.myfaces.component.html.ext.AbstractHtmlCommandLink.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class HtmlCommandLinkTag
    extends org.apache.myfaces.shared_tomahawk.taglib.html.HtmlCommandLinkTag
{
    public HtmlCommandLinkTag()
    {    
    }
    
    public String getComponentType()
    {
        return "org.apache.myfaces.HtmlCommandLink";
    }

    public String getRendererType()
    {
        return "org.apache.myfaces.Link";
    }

    private ValueExpression _actionFor;
    
    public void setActionFor(ValueExpression actionFor)
    {
        _actionFor = actionFor;
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
    private String _forceId;
    
    public void setForceId(String forceId)
    {
        _forceId = forceId;
    }
    private String _forceIdIndex;
    
    public void setForceIdIndex(String forceIdIndex)
    {
        _forceIdIndex = forceIdIndex;
    }

    protected void setProperties(UIComponent component)
    {
        if (!(component instanceof org.apache.myfaces.component.html.ext.HtmlCommandLink))
        {
            throw new IllegalArgumentException("Component "+
                component.getClass().getName() +" is no org.apache.myfaces.component.html.ext.HtmlCommandLink");
        }
        
        org.apache.myfaces.component.html.ext.HtmlCommandLink comp = (org.apache.myfaces.component.html.ext.HtmlCommandLink) component;
        
        super.setProperties(component);
        
        FacesContext context = getFacesContext();

        if (_actionFor != null)
        {
            comp.setValueExpression("actionFor", _actionFor);
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
        if (_forceId != null)
        {
            comp.getAttributes().put("forceId", Boolean.valueOf(_forceId));
        } 
        if (_forceIdIndex != null)
        {
            comp.getAttributes().put("forceIdIndex", Boolean.valueOf(_forceIdIndex));
        } 
    }

    public void release()
    {
        super.release();
        _actionFor = null;
        _disabledStyle = null;
        _disabledStyleClass = null;
        _enabledOnUserRole = null;
        _visibleOnUserRole = null;
        _forceId = null;
        _forceIdIndex = null;
    }
}
