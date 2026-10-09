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
package org.apache.myfaces.custom.calendar;

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


// Generated from class org.apache.myfaces.custom.calendar.AbstractHtmlInputCalendar.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class HtmlInputCalendarTag
    extends org.apache.myfaces.custom.calendar.AbstractHtmlInputCalendarTag
{
    public HtmlInputCalendarTag()
    {    
    }
    
    public String getComponentType()
    {
        return "org.apache.myfaces.HtmlInputCalendar";
    }

    public String getRendererType()
    {
        return "org.apache.myfaces.Calendar";
    }

    private ValueExpression _monthYearRowClass;
    
    public void setMonthYearRowClass(ValueExpression monthYearRowClass)
    {
        _monthYearRowClass = monthYearRowClass;
    }
    private ValueExpression _weekRowClass;
    
    public void setWeekRowClass(ValueExpression weekRowClass)
    {
        _weekRowClass = weekRowClass;
    }
    private ValueExpression _dayCellClass;
    
    public void setDayCellClass(ValueExpression dayCellClass)
    {
        _dayCellClass = dayCellClass;
    }
    private ValueExpression _currentDayCellClass;
    
    public void setCurrentDayCellClass(ValueExpression currentDayCellClass)
    {
        _currentDayCellClass = currentDayCellClass;
    }
    private ValueExpression _popupLeft;
    
    public void setPopupLeft(ValueExpression popupLeft)
    {
        _popupLeft = popupLeft;
    }
    private ValueExpression _renderAsPopup;
    
    public void setRenderAsPopup(ValueExpression renderAsPopup)
    {
        _renderAsPopup = renderAsPopup;
    }
    private ValueExpression _addResources;
    
    public void setAddResources(ValueExpression addResources)
    {
        _addResources = addResources;
    }
    private ValueExpression _popupButtonString;
    
    public void setPopupButtonString(ValueExpression popupButtonString)
    {
        _popupButtonString = popupButtonString;
    }
    private ValueExpression _popupButtonStyle;
    
    public void setPopupButtonStyle(ValueExpression popupButtonStyle)
    {
        _popupButtonStyle = popupButtonStyle;
    }
    private ValueExpression _popupButtonStyleClass;
    
    public void setPopupButtonStyleClass(ValueExpression popupButtonStyleClass)
    {
        _popupButtonStyleClass = popupButtonStyleClass;
    }
    private ValueExpression _renderPopupButtonAsImage;
    
    public void setRenderPopupButtonAsImage(ValueExpression renderPopupButtonAsImage)
    {
        _renderPopupButtonAsImage = renderPopupButtonAsImage;
    }
    private ValueExpression _popupDateFormat;
    
    public void setPopupDateFormat(ValueExpression popupDateFormat)
    {
        _popupDateFormat = popupDateFormat;
    }
    private ValueExpression _popupGotoString;
    
    public void setPopupGotoString(ValueExpression popupGotoString)
    {
        _popupGotoString = popupGotoString;
    }
    private ValueExpression _popupTodayString;
    
    public void setPopupTodayString(ValueExpression popupTodayString)
    {
        _popupTodayString = popupTodayString;
    }
    private ValueExpression _popupTodayDateFormat;
    
    public void setPopupTodayDateFormat(ValueExpression popupTodayDateFormat)
    {
        _popupTodayDateFormat = popupTodayDateFormat;
    }
    private ValueExpression _popupWeekString;
    
    public void setPopupWeekString(ValueExpression popupWeekString)
    {
        _popupWeekString = popupWeekString;
    }
    private ValueExpression _popupScrollLeftMessage;
    
    public void setPopupScrollLeftMessage(ValueExpression popupScrollLeftMessage)
    {
        _popupScrollLeftMessage = popupScrollLeftMessage;
    }
    private ValueExpression _popupScrollRightMessage;
    
    public void setPopupScrollRightMessage(ValueExpression popupScrollRightMessage)
    {
        _popupScrollRightMessage = popupScrollRightMessage;
    }
    private ValueExpression _popupSelectMonthMessage;
    
    public void setPopupSelectMonthMessage(ValueExpression popupSelectMonthMessage)
    {
        _popupSelectMonthMessage = popupSelectMonthMessage;
    }
    private ValueExpression _popupSelectYearMessage;
    
    public void setPopupSelectYearMessage(ValueExpression popupSelectYearMessage)
    {
        _popupSelectYearMessage = popupSelectYearMessage;
    }
    private ValueExpression _popupSelectDateMessage;
    
    public void setPopupSelectDateMessage(ValueExpression popupSelectDateMessage)
    {
        _popupSelectDateMessage = popupSelectDateMessage;
    }
    private ValueExpression _popupTheme;
    
    public void setPopupTheme(ValueExpression popupTheme)
    {
        _popupTheme = popupTheme;
    }
    private ValueExpression _popupButtonImageUrl;
    
    public void setPopupButtonImageUrl(ValueExpression popupButtonImageUrl)
    {
        _popupButtonImageUrl = popupButtonImageUrl;
    }
    private ValueExpression _helpText;
    
    public void setHelpText(ValueExpression helpText)
    {
        _helpText = helpText;
    }
    private ValueExpression _popupSelectMode;
    
    public void setPopupSelectMode(ValueExpression popupSelectMode)
    {
        _popupSelectMode = popupSelectMode;
    }
    private ValueExpression _javascriptLocation;
    
    public void setJavascriptLocation(ValueExpression javascriptLocation)
    {
        _javascriptLocation = javascriptLocation;
    }
    private ValueExpression _imageLocation;
    
    public void setImageLocation(ValueExpression imageLocation)
    {
        _imageLocation = imageLocation;
    }
    private ValueExpression _styleLocation;
    
    public void setStyleLocation(ValueExpression styleLocation)
    {
        _styleLocation = styleLocation;
    }
    private ValueExpression _javascriptLibrary;
    
    public void setJavascriptLibrary(ValueExpression javascriptLibrary)
    {
        _javascriptLibrary = javascriptLibrary;
    }
    private ValueExpression _imageLibrary;
    
    public void setImageLibrary(ValueExpression imageLibrary)
    {
        _imageLibrary = imageLibrary;
    }
    private ValueExpression _styleLibrary;
    
    public void setStyleLibrary(ValueExpression styleLibrary)
    {
        _styleLibrary = styleLibrary;
    }

    protected void setProperties(UIComponent component)
    {
        if (!(component instanceof org.apache.myfaces.custom.calendar.HtmlInputCalendar))
        {
            throw new IllegalArgumentException("Component "+
                component.getClass().getName() +" is no org.apache.myfaces.custom.calendar.HtmlInputCalendar");
        }
        
        org.apache.myfaces.custom.calendar.HtmlInputCalendar comp = (org.apache.myfaces.custom.calendar.HtmlInputCalendar) component;
        
        super.setProperties(component);
        
        FacesContext context = getFacesContext();

        if (_monthYearRowClass != null)
        {
            comp.setValueExpression("monthYearRowClass", _monthYearRowClass);
        } 
        if (_weekRowClass != null)
        {
            comp.setValueExpression("weekRowClass", _weekRowClass);
        } 
        if (_dayCellClass != null)
        {
            comp.setValueExpression("dayCellClass", _dayCellClass);
        } 
        if (_currentDayCellClass != null)
        {
            comp.setValueExpression("currentDayCellClass", _currentDayCellClass);
        } 
        if (_popupLeft != null)
        {
            comp.setValueExpression("popupLeft", _popupLeft);
        } 
        if (_renderAsPopup != null)
        {
            comp.setValueExpression("renderAsPopup", _renderAsPopup);
        } 
        if (_addResources != null)
        {
            comp.setValueExpression("addResources", _addResources);
        } 
        if (_popupButtonString != null)
        {
            comp.setValueExpression("popupButtonString", _popupButtonString);
        } 
        if (_popupButtonStyle != null)
        {
            comp.setValueExpression("popupButtonStyle", _popupButtonStyle);
        } 
        if (_popupButtonStyleClass != null)
        {
            comp.setValueExpression("popupButtonStyleClass", _popupButtonStyleClass);
        } 
        if (_renderPopupButtonAsImage != null)
        {
            comp.setValueExpression("renderPopupButtonAsImage", _renderPopupButtonAsImage);
        } 
        if (_popupDateFormat != null)
        {
            comp.setValueExpression("popupDateFormat", _popupDateFormat);
        } 
        if (_popupGotoString != null)
        {
            comp.setValueExpression("popupGotoString", _popupGotoString);
        } 
        if (_popupTodayString != null)
        {
            comp.setValueExpression("popupTodayString", _popupTodayString);
        } 
        if (_popupTodayDateFormat != null)
        {
            comp.setValueExpression("popupTodayDateFormat", _popupTodayDateFormat);
        } 
        if (_popupWeekString != null)
        {
            comp.setValueExpression("popupWeekString", _popupWeekString);
        } 
        if (_popupScrollLeftMessage != null)
        {
            comp.setValueExpression("popupScrollLeftMessage", _popupScrollLeftMessage);
        } 
        if (_popupScrollRightMessage != null)
        {
            comp.setValueExpression("popupScrollRightMessage", _popupScrollRightMessage);
        } 
        if (_popupSelectMonthMessage != null)
        {
            comp.setValueExpression("popupSelectMonthMessage", _popupSelectMonthMessage);
        } 
        if (_popupSelectYearMessage != null)
        {
            comp.setValueExpression("popupSelectYearMessage", _popupSelectYearMessage);
        } 
        if (_popupSelectDateMessage != null)
        {
            comp.setValueExpression("popupSelectDateMessage", _popupSelectDateMessage);
        } 
        if (_popupTheme != null)
        {
            comp.setValueExpression("popupTheme", _popupTheme);
        } 
        if (_popupButtonImageUrl != null)
        {
            comp.setValueExpression("popupButtonImageUrl", _popupButtonImageUrl);
        } 
        if (_helpText != null)
        {
            comp.setValueExpression("helpText", _helpText);
        } 
        if (_popupSelectMode != null)
        {
            comp.setValueExpression("popupSelectMode", _popupSelectMode);
        } 
        if (_javascriptLocation != null)
        {
            comp.setValueExpression("javascriptLocation", _javascriptLocation);
        } 
        if (_imageLocation != null)
        {
            comp.setValueExpression("imageLocation", _imageLocation);
        } 
        if (_styleLocation != null)
        {
            comp.setValueExpression("styleLocation", _styleLocation);
        } 
        if (_javascriptLibrary != null)
        {
            comp.setValueExpression("javascriptLibrary", _javascriptLibrary);
        } 
        if (_imageLibrary != null)
        {
            comp.setValueExpression("imageLibrary", _imageLibrary);
        } 
        if (_styleLibrary != null)
        {
            comp.setValueExpression("styleLibrary", _styleLibrary);
        } 
    }

    public void release()
    {
        super.release();
        _monthYearRowClass = null;
        _weekRowClass = null;
        _dayCellClass = null;
        _currentDayCellClass = null;
        _popupLeft = null;
        _renderAsPopup = null;
        _addResources = null;
        _popupButtonString = null;
        _popupButtonStyle = null;
        _popupButtonStyleClass = null;
        _renderPopupButtonAsImage = null;
        _popupDateFormat = null;
        _popupGotoString = null;
        _popupTodayString = null;
        _popupTodayDateFormat = null;
        _popupWeekString = null;
        _popupScrollLeftMessage = null;
        _popupScrollRightMessage = null;
        _popupSelectMonthMessage = null;
        _popupSelectYearMessage = null;
        _popupSelectDateMessage = null;
        _popupTheme = null;
        _popupButtonImageUrl = null;
        _helpText = null;
        _popupSelectMode = null;
        _javascriptLocation = null;
        _imageLocation = null;
        _styleLocation = null;
        _javascriptLibrary = null;
        _imageLibrary = null;
        _styleLibrary = null;
    }
}
