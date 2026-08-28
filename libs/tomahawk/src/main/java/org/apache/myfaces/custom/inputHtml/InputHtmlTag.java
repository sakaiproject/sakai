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
package org.apache.myfaces.custom.inputHtml;

import jakarta.faces.component.UIComponent;
import jakarta.el.ValueExpression;
import jakarta.el.MethodExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.UIComponent;
import jakarta.faces.convert.Converter;
import jakarta.faces.event.MethodExpressionValueChangeListener;
import jakarta.faces.validator.MethodExpressionValidator;
import jakarta.faces.el.MethodBinding;


// Generated from class org.apache.myfaces.custom.inputHtml.InputHtml.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class InputHtmlTag
    extends org.apache.myfaces.generated.taglib.html.ext.HtmlInputTextTag
{
    public InputHtmlTag()
    {    
    }
    
    public String getComponentType()
    {
        return "org.apache.myfaces.InputHtml";
    }

    public String getRendererType()
    {
        return "org.apache.myfaces.InputHtml";
    }

    private ValueExpression _fallback;
    
    public void setFallback(ValueExpression fallback)
    {
        _fallback = fallback;
    }
    private ValueExpression _type;
    
    public void setType(ValueExpression type)
    {
        _type = type;
    }
    private ValueExpression _allowEditSource;
    
    public void setAllowEditSource(ValueExpression allowEditSource)
    {
        _allowEditSource = allowEditSource;
    }
    private ValueExpression _allowExternalLinks;
    
    public void setAllowExternalLinks(ValueExpression allowExternalLinks)
    {
        _allowExternalLinks = allowExternalLinks;
    }
    private ValueExpression _addKupuLogo;
    
    public void setAddKupuLogo(ValueExpression addKupuLogo)
    {
        _addKupuLogo = addKupuLogo;
    }
    private ValueExpression _showAllToolBoxes;
    
    public void setShowAllToolBoxes(ValueExpression showAllToolBoxes)
    {
        _showAllToolBoxes = showAllToolBoxes;
    }
    private ValueExpression _showPropertiesToolBox;
    
    public void setShowPropertiesToolBox(ValueExpression showPropertiesToolBox)
    {
        _showPropertiesToolBox = showPropertiesToolBox;
    }
    private ValueExpression _showLinksToolBox;
    
    public void setShowLinksToolBox(ValueExpression showLinksToolBox)
    {
        _showLinksToolBox = showLinksToolBox;
    }
    private ValueExpression _showImagesToolBox;
    
    public void setShowImagesToolBox(ValueExpression showImagesToolBox)
    {
        _showImagesToolBox = showImagesToolBox;
    }
    private ValueExpression _showTablesToolBox;
    
    public void setShowTablesToolBox(ValueExpression showTablesToolBox)
    {
        _showTablesToolBox = showTablesToolBox;
    }
    private ValueExpression _showCleanupExpressionsToolBox;
    
    public void setShowCleanupExpressionsToolBox(ValueExpression showCleanupExpressionsToolBox)
    {
        _showCleanupExpressionsToolBox = showCleanupExpressionsToolBox;
    }
    private ValueExpression _showDebugToolBox;
    
    public void setShowDebugToolBox(ValueExpression showDebugToolBox)
    {
        _showDebugToolBox = showDebugToolBox;
    }

    protected void setProperties(UIComponent component)
    {
        if (!(component instanceof org.apache.myfaces.custom.inputHtml.InputHtml))
        {
            throw new IllegalArgumentException("Component "+
                component.getClass().getName() +" is no org.apache.myfaces.custom.inputHtml.InputHtml");
        }
        
        org.apache.myfaces.custom.inputHtml.InputHtml comp = (org.apache.myfaces.custom.inputHtml.InputHtml) component;
        
        super.setProperties(component);
        
        FacesContext context = getFacesContext();

        if (_fallback != null)
        {
            comp.setValueExpression("fallback", _fallback);
        } 
        if (_type != null)
        {
            comp.setValueExpression("type", _type);
        } 
        if (_allowEditSource != null)
        {
            comp.setValueExpression("allowEditSource", _allowEditSource);
        } 
        if (_allowExternalLinks != null)
        {
            comp.setValueExpression("allowExternalLinks", _allowExternalLinks);
        } 
        if (_addKupuLogo != null)
        {
            comp.setValueExpression("addKupuLogo", _addKupuLogo);
        } 
        if (_showAllToolBoxes != null)
        {
            comp.setValueExpression("showAllToolBoxes", _showAllToolBoxes);
        } 
        if (_showPropertiesToolBox != null)
        {
            comp.setValueExpression("showPropertiesToolBox", _showPropertiesToolBox);
        } 
        if (_showLinksToolBox != null)
        {
            comp.setValueExpression("showLinksToolBox", _showLinksToolBox);
        } 
        if (_showImagesToolBox != null)
        {
            comp.setValueExpression("showImagesToolBox", _showImagesToolBox);
        } 
        if (_showTablesToolBox != null)
        {
            comp.setValueExpression("showTablesToolBox", _showTablesToolBox);
        } 
        if (_showCleanupExpressionsToolBox != null)
        {
            comp.setValueExpression("showCleanupExpressionsToolBox", _showCleanupExpressionsToolBox);
        } 
        if (_showDebugToolBox != null)
        {
            comp.setValueExpression("showDebugToolBox", _showDebugToolBox);
        } 
    }

    public void release()
    {
        super.release();
        _fallback = null;
        _type = null;
        _allowEditSource = null;
        _allowExternalLinks = null;
        _addKupuLogo = null;
        _showAllToolBoxes = null;
        _showPropertiesToolBox = null;
        _showLinksToolBox = null;
        _showImagesToolBox = null;
        _showTablesToolBox = null;
        _showCleanupExpressionsToolBox = null;
        _showDebugToolBox = null;
    }
}
