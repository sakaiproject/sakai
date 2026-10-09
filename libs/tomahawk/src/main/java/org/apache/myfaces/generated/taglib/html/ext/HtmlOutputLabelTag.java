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
import jakarta.faces.convert.Converter;


// Generated from class org.apache.myfaces.component.html.ext.AbstractHtmlOutputLabel.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class HtmlOutputLabelTag
    extends org.apache.myfaces.shared_tomahawk.taglib.html.HtmlOutputLabelTag
{
    public HtmlOutputLabelTag()
    {    
    }
    
    public String getComponentType()
    {
        return "org.apache.myfaces.HtmlOutputLabel";
    }

    public String getRendererType()
    {
        return "jakarta.faces.Label";
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
        if (!(component instanceof org.apache.myfaces.component.html.ext.HtmlOutputLabel))
        {
            throw new IllegalArgumentException("Component "+
                component.getClass().getName() +" is no org.apache.myfaces.component.html.ext.HtmlOutputLabel");
        }
        
        org.apache.myfaces.component.html.ext.HtmlOutputLabel comp = (org.apache.myfaces.component.html.ext.HtmlOutputLabel) component;
        
        super.setProperties(component);
        
        FacesContext context = getFacesContext();

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
        _enabledOnUserRole = null;
        _visibleOnUserRole = null;
        _forceId = null;
        _forceIdIndex = null;
    }
}
