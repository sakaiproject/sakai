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
package org.apache.myfaces.shared_tomahawk.taglib.core;

import jakarta.faces.component.UIComponent;
import jakarta.el.ValueExpression;
import jakarta.el.MethodExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.UIComponent;


// Generated from class jakarta.faces.component._UISelectItem.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class SelectItemTag
    extends jakarta.faces.webapp.UIComponentELTag
{
    public SelectItemTag()
    {    
    }
    
    public String getComponentType()
    {
        return "jakarta.faces.SelectItem";
    }

    public String getRendererType()
    {
        return null;
    }

    private ValueExpression _value;
    
    public void setValue(ValueExpression value)
    {
        _value = value;
    }
    private ValueExpression _itemDisabled;
    
    public void setItemDisabled(ValueExpression itemDisabled)
    {
        _itemDisabled = itemDisabled;
    }
    private ValueExpression _itemEscaped;
    
    public void setEscape(ValueExpression itemEscaped)
    {
        _itemEscaped = itemEscaped;
    }
    private ValueExpression _itemDescription;
    
    public void setItemDescription(ValueExpression itemDescription)
    {
        _itemDescription = itemDescription;
    }
    private ValueExpression _itemLabel;
    
    public void setItemLabel(ValueExpression itemLabel)
    {
        _itemLabel = itemLabel;
    }
    private ValueExpression _itemValue;
    
    public void setItemValue(ValueExpression itemValue)
    {
        _itemValue = itemValue;
    }
    private ValueExpression _noSelectionOption;
    
    public void setNoSelectionOption(ValueExpression noSelectionOption)
    {
        _noSelectionOption = noSelectionOption;
    }

    protected void setProperties(UIComponent component)
    {
        if (!(component instanceof jakarta.faces.component.UISelectItem))
        {
            throw new IllegalArgumentException("Component "+
                component.getClass().getName() +" is no jakarta.faces.component.UISelectItem");
        }
        
        jakarta.faces.component.UISelectItem comp = (jakarta.faces.component.UISelectItem) component;
        
        super.setProperties(component);
        
        FacesContext context = getFacesContext();

        if (_value != null)
        {
            comp.setValueExpression("value", _value);
        } 
        if (_itemDisabled != null)
        {
            comp.setValueExpression("itemDisabled", _itemDisabled);
        } 
        if (_itemEscaped != null)
        {
            comp.setValueExpression("itemEscaped", _itemEscaped);
        } 
        if (_itemDescription != null)
        {
            comp.setValueExpression("itemDescription", _itemDescription);
        } 
        if (_itemLabel != null)
        {
            comp.setValueExpression("itemLabel", _itemLabel);
        } 
        if (_itemValue != null)
        {
            comp.setValueExpression("itemValue", _itemValue);
        } 
        if (_noSelectionOption != null)
        {
            comp.setValueExpression("noSelectionOption", _noSelectionOption);
        } 
    }

    public void release()
    {
        super.release();
        _value = null;
        _itemDisabled = null;
        _itemEscaped = null;
        _itemDescription = null;
        _itemLabel = null;
        _itemValue = null;
        _noSelectionOption = null;
    }
}
