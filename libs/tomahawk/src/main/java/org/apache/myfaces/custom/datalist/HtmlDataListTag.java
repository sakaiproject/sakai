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
package org.apache.myfaces.custom.datalist;

import jakarta.faces.component.UIComponent;
import jakarta.el.ValueExpression;
import jakarta.el.MethodExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.UIComponent;


// Generated from class org.apache.myfaces.custom.datalist.AbstractHtmlDataList.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class HtmlDataListTag
    extends org.apache.myfaces.shared_tomahawk.taglib.html.HtmlDataTableTag
{
    public HtmlDataListTag()
    {    
    }
    
    public String getComponentType()
    {
        return "org.apache.myfaces.HtmlDataList";
    }

    public String getRendererType()
    {
        return "org.apache.myfaces.List";
    }

    private ValueExpression _rowCountVar;
    
    public void setRowCountVar(ValueExpression rowCountVar)
    {
        _rowCountVar = rowCountVar;
    }
    private ValueExpression _rowIndexVar;
    
    public void setRowIndexVar(ValueExpression rowIndexVar)
    {
        _rowIndexVar = rowIndexVar;
    }
    private ValueExpression _layout;
    
    public void setLayout(ValueExpression layout)
    {
        _layout = layout;
    }
    private ValueExpression _itemStyleClass;
    
    public void setItemStyleClass(ValueExpression itemStyleClass)
    {
        _itemStyleClass = itemStyleClass;
    }
    private ValueExpression _itemOnClick;
    
    public void setItemOnClick(ValueExpression itemOnClick)
    {
        _itemOnClick = itemOnClick;
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
    private ValueExpression _preserveRowStates;
    
    public void setPreserveRowStates(ValueExpression preserveRowStates)
    {
        _preserveRowStates = preserveRowStates;
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
    private String _preserveRowComponentState;
    
    public void setPreserveRowComponentState(String preserveRowComponentState)
    {
        _preserveRowComponentState = preserveRowComponentState;
    }
    private ValueExpression _rowKey;
    
    public void setRowKey(ValueExpression rowKey)
    {
        _rowKey = rowKey;
    }
    private ValueExpression _derivedRowKeyPrefix;
    
    public void setDerivedRowKeyPrefix(ValueExpression derivedRowKeyPrefix)
    {
        _derivedRowKeyPrefix = derivedRowKeyPrefix;
    }

    protected void setProperties(UIComponent component)
    {
        if (!(component instanceof org.apache.myfaces.custom.datalist.HtmlDataList))
        {
            throw new IllegalArgumentException("Component "+
                component.getClass().getName() +" is no org.apache.myfaces.custom.datalist.HtmlDataList");
        }
        
        org.apache.myfaces.custom.datalist.HtmlDataList comp = (org.apache.myfaces.custom.datalist.HtmlDataList) component;
        
        super.setProperties(component);
        
        FacesContext context = getFacesContext();

        if (_rowCountVar != null)
        {
            comp.setValueExpression("rowCountVar", _rowCountVar);
        } 
        if (_rowIndexVar != null)
        {
            comp.setValueExpression("rowIndexVar", _rowIndexVar);
        } 
        if (_layout != null)
        {
            comp.setValueExpression("layout", _layout);
        } 
        if (_itemStyleClass != null)
        {
            comp.setValueExpression("itemStyleClass", _itemStyleClass);
        } 
        if (_itemOnClick != null)
        {
            comp.setValueExpression("itemOnClick", _itemOnClick);
        } 
        if (_enabledOnUserRole != null)
        {
            comp.setValueExpression("enabledOnUserRole", _enabledOnUserRole);
        } 
        if (_visibleOnUserRole != null)
        {
            comp.setValueExpression("visibleOnUserRole", _visibleOnUserRole);
        } 
        if (_preserveRowStates != null)
        {
            comp.setValueExpression("preserveRowStates", _preserveRowStates);
        } 
        if (_forceId != null)
        {
            comp.getAttributes().put("forceId", Boolean.valueOf(_forceId));
        } 
        if (_forceIdIndex != null)
        {
            comp.getAttributes().put("forceIdIndex", Boolean.valueOf(_forceIdIndex));
        } 
        if (_preserveRowComponentState != null)
        {
            comp.getAttributes().put("preserveRowComponentState", Boolean.valueOf(_preserveRowComponentState));
        } 
        if (_rowKey != null)
        {
            comp.setValueExpression("rowKey", _rowKey);
        } 
        if (_derivedRowKeyPrefix != null)
        {
            comp.setValueExpression("derivedRowKeyPrefix", _derivedRowKeyPrefix);
        } 
    }

    public void release()
    {
        super.release();
        _rowCountVar = null;
        _rowIndexVar = null;
        _layout = null;
        _itemStyleClass = null;
        _itemOnClick = null;
        _enabledOnUserRole = null;
        _visibleOnUserRole = null;
        _preserveRowStates = null;
        _forceId = null;
        _forceIdIndex = null;
        _preserveRowComponentState = null;
        _rowKey = null;
        _derivedRowKeyPrefix = null;
    }
}
