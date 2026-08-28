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
package org.apache.myfaces.custom.fileupload;

import jakarta.faces.component.UIComponent;
import jakarta.el.ValueExpression;
import jakarta.el.MethodExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.UIComponent;
import jakarta.faces.convert.Converter;
import jakarta.faces.event.MethodExpressionValueChangeListener;
import jakarta.faces.validator.MethodExpressionValidator;
import jakarta.faces.el.MethodBinding;


// Generated from class org.apache.myfaces.custom.fileupload.AbstractHtmlInputFileUpload.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class HtmlInputFileUploadTag
    extends org.apache.myfaces.shared_tomahawk.taglib.html.HtmlInputTextTag
{
    public HtmlInputFileUploadTag()
    {    
    }
    
    public String getComponentType()
    {
        return "org.apache.myfaces.HtmlInputFileUpload";
    }

    public String getRendererType()
    {
        return "org.apache.myfaces.FileUpload";
    }

    private ValueExpression _storage;
    
    public void setStorage(ValueExpression storage)
    {
        _storage = storage;
    }
    private ValueExpression _accept;
    
    public void setAccept(ValueExpression accept)
    {
        _accept = accept;
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
    private ValueExpression _align;
    
    public void setAlign(ValueExpression align)
    {
        _align = align;
    }

    protected void setProperties(UIComponent component)
    {
        if (!(component instanceof org.apache.myfaces.custom.fileupload.HtmlInputFileUpload))
        {
            throw new IllegalArgumentException("Component "+
                component.getClass().getName() +" is no org.apache.myfaces.custom.fileupload.HtmlInputFileUpload");
        }
        
        org.apache.myfaces.custom.fileupload.HtmlInputFileUpload comp = (org.apache.myfaces.custom.fileupload.HtmlInputFileUpload) component;
        
        super.setProperties(component);
        
        FacesContext context = getFacesContext();

        if (_storage != null)
        {
            comp.setValueExpression("storage", _storage);
        } 
        if (_accept != null)
        {
            comp.setValueExpression("accept", _accept);
        } 
        if (_enabledOnUserRole != null)
        {
            comp.setValueExpression("enabledOnUserRole", _enabledOnUserRole);
        } 
        if (_visibleOnUserRole != null)
        {
            comp.setValueExpression("visibleOnUserRole", _visibleOnUserRole);
        } 
        if (_align != null)
        {
            comp.setValueExpression("align", _align);
        } 
    }

    public void release()
    {
        super.release();
        _storage = null;
        _accept = null;
        _enabledOnUserRole = null;
        _visibleOnUserRole = null;
        _align = null;
    }
}
