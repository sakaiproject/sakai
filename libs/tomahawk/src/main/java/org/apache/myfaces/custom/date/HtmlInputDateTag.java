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
package org.apache.myfaces.custom.date;

import jakarta.faces.component.UIComponent;
import jakarta.el.ValueExpression;
import jakarta.el.MethodExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.UIComponent;
import jakarta.faces.convert.Converter;
import org.apache.myfaces.custom.calendar.DateBusinessConverter;
import jakarta.faces.event.MethodExpressionValueChangeListener;
import jakarta.faces.validator.MethodExpressionValidator;
import jakarta.faces.el.MethodBinding;


// Generated from class org.apache.myfaces.custom.date.AbstractHtmlInputDate.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class HtmlInputDateTag
    extends org.apache.myfaces.custom.date.AbstractHtmlInputDateTag
{
    public HtmlInputDateTag()
    {    
    }
    
    public String getComponentType()
    {
        return "org.apache.myfaces.HtmlInputDate";
    }

    public String getRendererType()
    {
        return "org.apache.myfaces.Date";
    }

    private ValueExpression _timeZone;
    
    public void setTimeZone(ValueExpression timeZone)
    {
        _timeZone = timeZone;
    }
    private ValueExpression _type;
    
    public void setType(ValueExpression type)
    {
        _type = type;
    }
    private ValueExpression _ampm;
    
    public void setAmpm(ValueExpression ampm)
    {
        _ampm = ampm;
    }
    private ValueExpression _popupCalendar;
    
    public void setPopupCalendar(ValueExpression popupCalendar)
    {
        _popupCalendar = popupCalendar;
    }
    private ValueExpression _emptyMonthSelection;
    
    public void setEmptyMonthSelection(ValueExpression emptyMonthSelection)
    {
        _emptyMonthSelection = emptyMonthSelection;
    }
    private ValueExpression _emptyAmpmSelection;
    
    public void setEmptyAmpmSelection(ValueExpression emptyAmpmSelection)
    {
        _emptyAmpmSelection = emptyAmpmSelection;
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
        if (!(component instanceof org.apache.myfaces.custom.date.HtmlInputDate))
        {
            throw new IllegalArgumentException("Component "+
                component.getClass().getName() +" is no org.apache.myfaces.custom.date.HtmlInputDate");
        }
        
        org.apache.myfaces.custom.date.HtmlInputDate comp = (org.apache.myfaces.custom.date.HtmlInputDate) component;
        
        super.setProperties(component);
        
        FacesContext context = getFacesContext();

        if (_timeZone != null)
        {
            comp.setValueExpression("timeZone", _timeZone);
        } 
        if (_type != null)
        {
            comp.setValueExpression("type", _type);
        } 
        if (_ampm != null)
        {
            comp.setValueExpression("ampm", _ampm);
        } 
        if (_popupCalendar != null)
        {
            comp.setValueExpression("popupCalendar", _popupCalendar);
        } 
        if (_emptyMonthSelection != null)
        {
            comp.setValueExpression("emptyMonthSelection", _emptyMonthSelection);
        } 
        if (_emptyAmpmSelection != null)
        {
            comp.setValueExpression("emptyAmpmSelection", _emptyAmpmSelection);
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
        _timeZone = null;
        _type = null;
        _ampm = null;
        _popupCalendar = null;
        _emptyMonthSelection = null;
        _emptyAmpmSelection = null;
        _enabledOnUserRole = null;
        _visibleOnUserRole = null;
        _forceId = null;
        _forceIdIndex = null;
        _align = null;
    }
}
