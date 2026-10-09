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


// Generated from class org.apache.myfaces.component.html.ext.AbstractHtmlGraphicImage.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class HtmlGraphicImageTag
    extends org.apache.myfaces.shared_tomahawk.taglib.html.HtmlGraphicImageTag
{
    public HtmlGraphicImageTag()
    {    
    }
    
    public String getComponentType()
    {
        return "org.apache.myfaces.HtmlGraphicImage";
    }

    public String getRendererType()
    {
        return "org.apache.myfaces.Image";
    }

    private ValueExpression _border;
    
    public void setBorder(ValueExpression border)
    {
        _border = border;
    }
    private ValueExpression _hspace;
    
    public void setHspace(ValueExpression hspace)
    {
        _hspace = hspace;
    }
    private ValueExpression _vspace;
    
    public void setVspace(ValueExpression vspace)
    {
        _vspace = vspace;
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
    private ValueExpression _align;
    
    public void setAlign(ValueExpression align)
    {
        _align = align;
    }

    protected void setProperties(UIComponent component)
    {
        if (!(component instanceof org.apache.myfaces.component.html.ext.HtmlGraphicImage))
        {
            throw new IllegalArgumentException("Component "+
                component.getClass().getName() +" is no org.apache.myfaces.component.html.ext.HtmlGraphicImage");
        }
        
        org.apache.myfaces.component.html.ext.HtmlGraphicImage comp = (org.apache.myfaces.component.html.ext.HtmlGraphicImage) component;
        
        super.setProperties(component);
        
        FacesContext context = getFacesContext();

        if (_border != null)
        {
            comp.setValueExpression("border", _border);
        } 
        if (_hspace != null)
        {
            comp.setValueExpression("hspace", _hspace);
        } 
        if (_vspace != null)
        {
            comp.setValueExpression("vspace", _vspace);
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
        if (_align != null)
        {
            comp.setValueExpression("align", _align);
        } 
    }

    public void release()
    {
        super.release();
        _border = null;
        _hspace = null;
        _vspace = null;
        _enabledOnUserRole = null;
        _visibleOnUserRole = null;
        _forceId = null;
        _forceIdIndex = null;
        _align = null;
    }
}
