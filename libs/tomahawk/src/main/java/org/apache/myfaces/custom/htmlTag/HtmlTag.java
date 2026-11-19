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
package org.apache.myfaces.custom.htmlTag;

import jakarta.el.ValueExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.PartialStateHolder;
import jakarta.faces.component.StateHolder;
import org.apache.myfaces.component.AttachedDeltaWrapper;
import jakarta.faces.component.UIComponent;
import jakarta.faces.convert.Converter;


// Generated from class org.apache.myfaces.custom.htmlTag.AbstractHtmlTag.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class HtmlTag extends org.apache.myfaces.custom.htmlTag.AbstractHtmlTag
{

    static public final String COMPONENT_FAMILY =
        "jakarta.faces.Output";
    static public final String COMPONENT_TYPE =
        "org.apache.myfaces.HtmlTag";
    static public final String DEFAULT_RENDERER_TYPE = 
        "org.apache.myfaces.HtmlTagRenderer";


    public HtmlTag()
    {
        setRendererType("org.apache.myfaces.HtmlTagRenderer");
    }

    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }



    
    // Property: enabledOnUserRole
    public String getEnabledOnUserRole()
    {
        return (String) getStateHelper().eval(PropertyKeys.enabledOnUserRole);
    }
    
    public void setEnabledOnUserRole(String enabledOnUserRole)
    {
        getStateHelper().put(PropertyKeys.enabledOnUserRole, enabledOnUserRole ); 
    }    
    // Property: visibleOnUserRole
    public String getVisibleOnUserRole()
    {
        return (String) getStateHelper().eval(PropertyKeys.visibleOnUserRole);
    }
    
    public void setVisibleOnUserRole(String visibleOnUserRole)
    {
        getStateHelper().put(PropertyKeys.visibleOnUserRole, visibleOnUserRole ); 
    }    
    // Property: forceId
    public boolean isForceId()
    {
        Object value = getStateHelper().get(PropertyKeys.forceId);
        if (value != null)
        {
            return (Boolean) value;        
        }
        return false;        
    }
    
    public void setForceId(boolean forceId)
    {
        getStateHelper().put(PropertyKeys.forceId, forceId ); 
    }    
    // Property: forceIdIndex
    public boolean isForceIdIndex()
    {
        Object value = getStateHelper().get(PropertyKeys.forceIdIndex);
        if (value != null)
        {
            return (Boolean) value;        
        }
        return true;        
    }
    
    public void setForceIdIndex(boolean forceIdIndex)
    {
        getStateHelper().put(PropertyKeys.forceIdIndex, forceIdIndex ); 
    }    
    // Property: style
    public String getStyle()
    {
        return (String) getStateHelper().eval(PropertyKeys.style);
    }
    
    public void setStyle(String style)
    {
        getStateHelper().put(PropertyKeys.style, style ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.STYLE_PROP);
    }    
    // Property: styleClass
    public String getStyleClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.styleClass);
    }
    
    public void setStyleClass(String styleClass)
    {
        getStateHelper().put(PropertyKeys.styleClass, styleClass ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.STYLECLASS_PROP);
    }    

    public void setValueBinding(String name, jakarta.faces.el.ValueBinding binding)
    {
        super.setValueBinding(name, binding);
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this, name);
    }

    public void setValueExpression(String name, ValueExpression expression)
    {
        super.setValueExpression(name, expression);
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this, name);
    }

    protected enum PropertyKeys
    {
         enabledOnUserRole
        , visibleOnUserRole
        , forceId
        , forceIdIndex
        , style
        , styleClass
    }

 }
