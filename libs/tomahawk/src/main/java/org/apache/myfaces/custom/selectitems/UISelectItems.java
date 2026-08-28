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
package org.apache.myfaces.custom.selectitems;

import jakarta.el.ValueExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.PartialStateHolder;
import jakarta.faces.component.StateHolder;
import org.apache.myfaces.component.AttachedDeltaWrapper;
import jakarta.faces.component.UIComponent;


// Generated from class org.apache.myfaces.custom.selectitems.AbstractUISelectItems.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class UISelectItems extends org.apache.myfaces.custom.selectitems.AbstractUISelectItems
{

    static public final String COMPONENT_FAMILY =
        "jakarta.faces.SelectItems";
    static public final String COMPONENT_TYPE =
        "org.apache.myfaces.UISelectItems";


    public UISelectItems()
    {
        setRendererType(null);
    }

    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }



    
    // Property: var
    public String getVar()
    {
        return (String) getStateHelper().get(PropertyKeys.var);        
    }
    
    public void setVar(String var)
    {
        getStateHelper().put(PropertyKeys.var, var ); 
    }    
    // Property: itemLabel
    public Object getItemLabel()
    {
        return  getStateHelper().eval(PropertyKeys.itemLabel);
    }
    
    public void setItemLabel(Object itemLabel)
    {
        getStateHelper().put(PropertyKeys.itemLabel, itemLabel ); 
    }    
    // Property: itemValue
    public Object getItemValue()
    {
        return  getStateHelper().eval(PropertyKeys.itemValue);
    }
    
    public void setItemValue(Object itemValue)
    {
        getStateHelper().put(PropertyKeys.itemValue, itemValue ); 
    }    
    // Property: itemLabelEscaped
    public Object getItemLabelEscaped()
    {
        return  getStateHelper().eval(PropertyKeys.itemLabelEscaped, true);
    }
    
    public void setItemLabelEscaped(Object itemLabelEscaped)
    {
        getStateHelper().put(PropertyKeys.itemLabelEscaped, itemLabelEscaped ); 
    }    
    // Property: itemDescription
    public Object getItemDescription()
    {
        return  getStateHelper().eval(PropertyKeys.itemDescription);
    }
    
    public void setItemDescription(Object itemDescription)
    {
        getStateHelper().put(PropertyKeys.itemDescription, itemDescription ); 
    }    
    // Property: itemDisabled
    public Object getItemDisabled()
    {
        return  getStateHelper().eval(PropertyKeys.itemDisabled, false);
    }
    
    public void setItemDisabled(Object itemDisabled)
    {
        getStateHelper().put(PropertyKeys.itemDisabled, itemDisabled ); 
    }    
    // Property: useEntryAsItem
    public boolean isUseEntryAsItem()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.useEntryAsItem, false);
    }
    
    public void setUseEntryAsItem(boolean useEntryAsItem)
    {
        getStateHelper().put(PropertyKeys.useEntryAsItem, useEntryAsItem ); 
    }    

    protected enum PropertyKeys
    {
         var
        , itemLabel
        , itemValue
        , itemLabelEscaped
        , itemDescription
        , itemDisabled
        , useEntryAsItem
    }

 }
