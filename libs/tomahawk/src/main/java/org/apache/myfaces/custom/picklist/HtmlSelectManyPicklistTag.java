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
package org.apache.myfaces.custom.picklist;

import jakarta.faces.component.UIComponent;
import jakarta.el.ValueExpression;
import jakarta.el.MethodExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.UIComponent;
import jakarta.faces.convert.Converter;
import jakarta.faces.event.MethodExpressionValueChangeListener;
import jakarta.faces.validator.MethodExpressionValidator;
import jakarta.faces.el.MethodBinding;


// Generated from class org.apache.myfaces.custom.picklist.AbstractHtmlSelectManyPicklist.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class HtmlSelectManyPicklistTag
    extends org.apache.myfaces.generated.taglib.html.ext.HtmlSelectManyListboxTag
{
    public HtmlSelectManyPicklistTag()
    {    
    }
    
    public String getComponentType()
    {
        return "org.apache.myfaces.HtmlSelectManyPicklist";
    }

    public String getRendererType()
    {
        return "org.apache.myfaces.PicklistRenderer";
    }

    private ValueExpression _addButtonText;
    
    public void setAddButtonText(ValueExpression addButtonText)
    {
        _addButtonText = addButtonText;
    }
    private ValueExpression _addAllButtonText;
    
    public void setAddAllButtonText(ValueExpression addAllButtonText)
    {
        _addAllButtonText = addAllButtonText;
    }
    private ValueExpression _removeButtonText;
    
    public void setRemoveButtonText(ValueExpression removeButtonText)
    {
        _removeButtonText = removeButtonText;
    }
    private ValueExpression _removeAllButtonText;
    
    public void setRemoveAllButtonText(ValueExpression removeAllButtonText)
    {
        _removeAllButtonText = removeAllButtonText;
    }
    private ValueExpression _addButtonStyle;
    
    public void setAddButtonStyle(ValueExpression addButtonStyle)
    {
        _addButtonStyle = addButtonStyle;
    }
    private ValueExpression _addAllButtonStyle;
    
    public void setAddAllButtonStyle(ValueExpression addAllButtonStyle)
    {
        _addAllButtonStyle = addAllButtonStyle;
    }
    private ValueExpression _removeButtonStyle;
    
    public void setRemoveButtonStyle(ValueExpression removeButtonStyle)
    {
        _removeButtonStyle = removeButtonStyle;
    }
    private ValueExpression _removeAllButtonStyle;
    
    public void setRemoveAllButtonStyle(ValueExpression removeAllButtonStyle)
    {
        _removeAllButtonStyle = removeAllButtonStyle;
    }
    private ValueExpression _addButtonStyleClass;
    
    public void setAddButtonStyleClass(ValueExpression addButtonStyleClass)
    {
        _addButtonStyleClass = addButtonStyleClass;
    }
    private ValueExpression _addAllButtonStyleClass;
    
    public void setAddAllButtonStyleClass(ValueExpression addAllButtonStyleClass)
    {
        _addAllButtonStyleClass = addAllButtonStyleClass;
    }
    private ValueExpression _removeButtonStyleClass;
    
    public void setRemoveButtonStyleClass(ValueExpression removeButtonStyleClass)
    {
        _removeButtonStyleClass = removeButtonStyleClass;
    }
    private ValueExpression _removeAllButtonStyleClass;
    
    public void setRemoveAllButtonStyleClass(ValueExpression removeAllButtonStyleClass)
    {
        _removeAllButtonStyleClass = removeAllButtonStyleClass;
    }

    protected void setProperties(UIComponent component)
    {
        if (!(component instanceof org.apache.myfaces.custom.picklist.HtmlSelectManyPicklist))
        {
            throw new IllegalArgumentException("Component "+
                component.getClass().getName() +" is no org.apache.myfaces.custom.picklist.HtmlSelectManyPicklist");
        }
        
        org.apache.myfaces.custom.picklist.HtmlSelectManyPicklist comp = (org.apache.myfaces.custom.picklist.HtmlSelectManyPicklist) component;
        
        super.setProperties(component);
        
        FacesContext context = getFacesContext();

        if (_addButtonText != null)
        {
            comp.setValueExpression("addButtonText", _addButtonText);
        } 
        if (_addAllButtonText != null)
        {
            comp.setValueExpression("addAllButtonText", _addAllButtonText);
        } 
        if (_removeButtonText != null)
        {
            comp.setValueExpression("removeButtonText", _removeButtonText);
        } 
        if (_removeAllButtonText != null)
        {
            comp.setValueExpression("removeAllButtonText", _removeAllButtonText);
        } 
        if (_addButtonStyle != null)
        {
            comp.setValueExpression("addButtonStyle", _addButtonStyle);
        } 
        if (_addAllButtonStyle != null)
        {
            comp.setValueExpression("addAllButtonStyle", _addAllButtonStyle);
        } 
        if (_removeButtonStyle != null)
        {
            comp.setValueExpression("removeButtonStyle", _removeButtonStyle);
        } 
        if (_removeAllButtonStyle != null)
        {
            comp.setValueExpression("removeAllButtonStyle", _removeAllButtonStyle);
        } 
        if (_addButtonStyleClass != null)
        {
            comp.setValueExpression("addButtonStyleClass", _addButtonStyleClass);
        } 
        if (_addAllButtonStyleClass != null)
        {
            comp.setValueExpression("addAllButtonStyleClass", _addAllButtonStyleClass);
        } 
        if (_removeButtonStyleClass != null)
        {
            comp.setValueExpression("removeButtonStyleClass", _removeButtonStyleClass);
        } 
        if (_removeAllButtonStyleClass != null)
        {
            comp.setValueExpression("removeAllButtonStyleClass", _removeAllButtonStyleClass);
        } 
    }

    public void release()
    {
        super.release();
        _addButtonText = null;
        _addAllButtonText = null;
        _removeButtonText = null;
        _removeAllButtonText = null;
        _addButtonStyle = null;
        _addAllButtonStyle = null;
        _removeButtonStyle = null;
        _removeAllButtonStyle = null;
        _addButtonStyleClass = null;
        _addAllButtonStyleClass = null;
        _removeButtonStyleClass = null;
        _removeAllButtonStyleClass = null;
    }
}
