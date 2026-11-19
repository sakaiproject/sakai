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
package org.apache.myfaces.custom.toggle;

import jakarta.faces.component.UIComponent;
import jakarta.el.ValueExpression;
import jakarta.el.MethodExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.UIComponent;
import jakarta.faces.convert.Converter;


// Generated from class org.apache.myfaces.custom.toggle.AbstractToggleLink.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class ToggleLinkTag
    extends org.apache.myfaces.shared_tomahawk.taglib.html.HtmlOutputLinkTag
{
    public ToggleLinkTag()
    {    
    }
    
    public String getComponentType()
    {
        return "org.apache.myfaces.ToggleLink";
    }

    public String getRendererType()
    {
        return "org.apache.myfaces.ToggleLink";
    }

    private java.lang.String _for;
    
    public void setFor(java.lang.String forParam)
    {
        _for = forParam;
    }
    private java.lang.String _onClickFocusId;
    
    public void setOnClickFocusId(java.lang.String onClickFocusId)
    {
        _onClickFocusId = onClickFocusId;
    }
    private java.lang.Boolean _forceId;
    
    public void setForceId(java.lang.Boolean forceId)
    {
        _forceId = forceId;
    }
    private java.lang.Boolean _forceIdIndex;
    
    public void setForceIdIndex(java.lang.Boolean forceIdIndex)
    {
        _forceIdIndex = forceIdIndex;
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
        if (!(component instanceof org.apache.myfaces.custom.toggle.ToggleLink))
        {
            throw new IllegalArgumentException("Component "+
                component.getClass().getName() +" is no org.apache.myfaces.custom.toggle.ToggleLink");
        }
        
        org.apache.myfaces.custom.toggle.ToggleLink comp = (org.apache.myfaces.custom.toggle.ToggleLink) component;
        
        super.setProperties(component);
        
        FacesContext context = getFacesContext();

        if (_for != null)
        {
            comp.getAttributes().put("for", _for);
        } 
        if (_onClickFocusId != null)
        {
            comp.getAttributes().put("onClickFocusId", _onClickFocusId);
        } 
        if (_forceId != null)
        {
            comp.getAttributes().put("forceId", _forceId);
        } 
        if (_forceIdIndex != null)
        {
            comp.getAttributes().put("forceIdIndex", _forceIdIndex);
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
        _for = null;
        _onClickFocusId = null;
        _forceId = null;
        _forceIdIndex = null;
        _enabledOnUserRole = null;
        _visibleOnUserRole = null;
    }
}
