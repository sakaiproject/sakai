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
package org.apache.myfaces.custom.stylesheet;

import jakarta.faces.component.UIComponent;
import jakarta.el.ValueExpression;
import jakarta.el.MethodExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.UIComponent;


// Generated from class org.apache.myfaces.custom.stylesheet.AbstractStylesheet.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class StylesheetTag
    extends jakarta.faces.webapp.UIComponentELTag
{
    public StylesheetTag()
    {    
    }
    
    public String getComponentType()
    {
        return "org.apache.myfaces.Stylesheet";
    }

    public String getRendererType()
    {
        return "org.apache.myfaces.Stylesheet";
    }

    private ValueExpression _path;
    
    public void setPath(ValueExpression path)
    {
        _path = path;
    }
    private ValueExpression _inline;
    
    public void setInline(ValueExpression inline)
    {
        _inline = inline;
    }
    private ValueExpression _filtered;
    
    public void setFiltered(ValueExpression filtered)
    {
        _filtered = filtered;
    }
    private ValueExpression _media;
    
    public void setMedia(ValueExpression media)
    {
        _media = media;
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
        if (!(component instanceof org.apache.myfaces.custom.stylesheet.Stylesheet))
        {
            throw new IllegalArgumentException("Component "+
                component.getClass().getName() +" is no org.apache.myfaces.custom.stylesheet.Stylesheet");
        }
        
        org.apache.myfaces.custom.stylesheet.Stylesheet comp = (org.apache.myfaces.custom.stylesheet.Stylesheet) component;
        
        super.setProperties(component);
        
        FacesContext context = getFacesContext();

        if (_path != null)
        {
            comp.setValueExpression("path", _path);
        } 
        if (_inline != null)
        {
            comp.setValueExpression("inline", _inline);
        } 
        if (_filtered != null)
        {
            comp.setValueExpression("filtered", _filtered);
        } 
        if (_media != null)
        {
            comp.setValueExpression("media", _media);
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
        _path = null;
        _inline = null;
        _filtered = null;
        _media = null;
        _enabledOnUserRole = null;
        _visibleOnUserRole = null;
    }
}
