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
package org.apache.myfaces.custom.schedule;

import jakarta.faces.component.UIComponent;
import jakarta.el.ValueExpression;
import jakarta.el.MethodExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.UIComponent;
import jakarta.faces.convert.Converter;
import java.util.Date;
import jakarta.el.MethodExpression;
import jakarta.faces.event.MethodExpressionActionListener;
import jakarta.faces.el.MethodBinding;


// Generated from class org.apache.myfaces.custom.schedule.AbstractHtmlSchedule.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class ScheduleTag
    extends jakarta.faces.webapp.UIComponentELTag
{
    public ScheduleTag()
    {    
    }
    
    public String getComponentType()
    {
        return "org.apache.myfaces.Schedule";
    }

    public String getRendererType()
    {
        return "org.apache.myfaces.Schedule";
    }

    private ValueExpression _backgroundClass;
    
    public void setBackgroundClass(ValueExpression backgroundClass)
    {
        _backgroundClass = backgroundClass;
    }
    private ValueExpression _columnClass;
    
    public void setColumnClass(ValueExpression columnClass)
    {
        _columnClass = columnClass;
    }
    private ValueExpression _contentClass;
    
    public void setContentClass(ValueExpression contentClass)
    {
        _contentClass = contentClass;
    }
    private ValueExpression _dateClass;
    
    public void setDateClass(ValueExpression dateClass)
    {
        _dateClass = dateClass;
    }
    private ValueExpression _dayClass;
    
    public void setDayClass(ValueExpression dayClass)
    {
        _dayClass = dayClass;
    }
    private ValueExpression _entryClass;
    
    public void setEntryClass(ValueExpression entryClass)
    {
        _entryClass = entryClass;
    }
    private ValueExpression _entryRenderer;
    
    public void setEntryRenderer(ValueExpression entryRenderer)
    {
        _entryRenderer = entryRenderer;
    }
    private ValueExpression _evenClass;
    
    public void setEvenClass(ValueExpression evenClass)
    {
        _evenClass = evenClass;
    }
    private ValueExpression _foregroundClass;
    
    public void setForegroundClass(ValueExpression foregroundClass)
    {
        _foregroundClass = foregroundClass;
    }
    private ValueExpression _freeClass;
    
    public void setFreeClass(ValueExpression freeClass)
    {
        _freeClass = freeClass;
    }
    private ValueExpression _gutterClass;
    
    public void setGutterClass(ValueExpression gutterClass)
    {
        _gutterClass = gutterClass;
    }
    private ValueExpression _headerClass;
    
    public void setHeaderClass(ValueExpression headerClass)
    {
        _headerClass = headerClass;
    }
    private ValueExpression _holidayClass;
    
    public void setHolidayClass(ValueExpression holidayClass)
    {
        _holidayClass = holidayClass;
    }
    private ValueExpression _hoursClass;
    
    public void setHoursClass(ValueExpression hoursClass)
    {
        _hoursClass = hoursClass;
    }
    private ValueExpression _inactiveDayClass;
    
    public void setInactiveDayClass(ValueExpression inactiveDayClass)
    {
        _inactiveDayClass = inactiveDayClass;
    }
    private ValueExpression _minutesClass;
    
    public void setMinutesClass(ValueExpression minutesClass)
    {
        _minutesClass = minutesClass;
    }
    private ValueExpression _monthClass;
    
    public void setMonthClass(ValueExpression monthClass)
    {
        _monthClass = monthClass;
    }
    private ValueExpression _selectedClass;
    
    public void setSelectedClass(ValueExpression selectedClass)
    {
        _selectedClass = selectedClass;
    }
    private ValueExpression _selectedEntryClass;
    
    public void setSelectedEntryClass(ValueExpression selectedEntryClass)
    {
        _selectedEntryClass = selectedEntryClass;
    }
    private ValueExpression _subtitleClass;
    
    public void setSubtitleClass(ValueExpression subtitleClass)
    {
        _subtitleClass = subtitleClass;
    }
    private ValueExpression _textClass;
    
    public void setTextClass(ValueExpression textClass)
    {
        _textClass = textClass;
    }
    private ValueExpression _titleClass;
    
    public void setTitleClass(ValueExpression titleClass)
    {
        _titleClass = titleClass;
    }
    private ValueExpression _unevenClass;
    
    public void setUnevenClass(ValueExpression unevenClass)
    {
        _unevenClass = unevenClass;
    }
    private ValueExpression _weekClass;
    
    public void setWeekClass(ValueExpression weekClass)
    {
        _weekClass = weekClass;
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
    private MethodExpression _actionExpression;
    
    public void setAction(MethodExpression actionExpression)
    {
        _actionExpression = actionExpression;
    }
    private MethodExpression _mouseListenerExpression;
    
    public void setMouseListener(MethodExpression mouseListenerExpression)
    {
        _mouseListenerExpression = mouseListenerExpression;
    }
    private jakarta.el.MethodExpression _actionListener;
    
    public void setActionListener(jakarta.el.MethodExpression actionListener)
    {
        _actionListener = actionListener;
    }
    private ValueExpression _splitWeekend;
    
    public void setSplitWeekend(ValueExpression splitWeekend)
    {
        _splitWeekend = splitWeekend;
    }
    private ValueExpression _submitOnClick;
    
    public void setSubmitOnClick(ValueExpression submitOnClick)
    {
        _submitOnClick = submitOnClick;
    }
    private ValueExpression _compactMonthRowHeight;
    
    public void setCompactMonthRowHeight(ValueExpression compactMonthRowHeight)
    {
        _compactMonthRowHeight = compactMonthRowHeight;
    }
    private ValueExpression _compactWeekRowHeight;
    
    public void setCompactWeekRowHeight(ValueExpression compactWeekRowHeight)
    {
        _compactWeekRowHeight = compactWeekRowHeight;
    }
    private ValueExpression _converter;
    
    public void setConverter(ValueExpression converter)
    {
        _converter = converter;
    }
    private ValueExpression _detailedRowHeight;
    
    public void setDetailedRowHeight(ValueExpression detailedRowHeight)
    {
        _detailedRowHeight = detailedRowHeight;
    }
    private ValueExpression _expandToFitEntries;
    
    public void setExpandToFitEntries(ValueExpression expandToFitEntries)
    {
        _expandToFitEntries = expandToFitEntries;
    }
    private ValueExpression _headerDateFormat;
    
    public void setHeaderDateFormat(ValueExpression headerDateFormat)
    {
        _headerDateFormat = headerDateFormat;
    }
    private ValueExpression _immediate;
    
    public void setImmediate(ValueExpression immediate)
    {
        _immediate = immediate;
    }
    private ValueExpression _readonly;
    
    public void setReadonly(ValueExpression readonly)
    {
        _readonly = readonly;
    }
    private ValueExpression _renderZeroLengthEntries;
    
    public void setRenderZeroLengthEntries(ValueExpression renderZeroLengthEntries)
    {
        _renderZeroLengthEntries = renderZeroLengthEntries;
    }
    private ValueExpression _theme;
    
    public void setTheme(ValueExpression theme)
    {
        _theme = theme;
    }
    private ValueExpression _tooltip;
    
    public void setTooltip(ValueExpression tooltip)
    {
        _tooltip = tooltip;
    }
    private ValueExpression _value;
    
    public void setValue(ValueExpression value)
    {
        _value = value;
    }
    private ValueExpression _visibleEndHour;
    
    public void setVisibleEndHour(ValueExpression visibleEndHour)
    {
        _visibleEndHour = visibleEndHour;
    }
    private ValueExpression _visibleStartHour;
    
    public void setVisibleStartHour(ValueExpression visibleStartHour)
    {
        _visibleStartHour = visibleStartHour;
    }
    private ValueExpression _workingEndHour;
    
    public void setWorkingEndHour(ValueExpression workingEndHour)
    {
        _workingEndHour = workingEndHour;
    }
    private ValueExpression _workingStartHour;
    
    public void setWorkingStartHour(ValueExpression workingStartHour)
    {
        _workingStartHour = workingStartHour;
    }
    private ValueExpression _hourNotation;
    
    public void setHourNotation(ValueExpression hourNotation)
    {
        _hourNotation = hourNotation;
    }
    private ValueExpression _compactMonthDayOfWeekDateFormat;
    
    public void setCompactMonthDayOfWeekDateFormat(ValueExpression compactMonthDayOfWeekDateFormat)
    {
        _compactMonthDayOfWeekDateFormat = compactMonthDayOfWeekDateFormat;
    }

    protected void setProperties(UIComponent component)
    {
        if (!(component instanceof org.apache.myfaces.custom.schedule.HtmlSchedule))
        {
            throw new IllegalArgumentException("Component "+
                component.getClass().getName() +" is no org.apache.myfaces.custom.schedule.HtmlSchedule");
        }
        
        org.apache.myfaces.custom.schedule.HtmlSchedule comp = (org.apache.myfaces.custom.schedule.HtmlSchedule) component;
        
        super.setProperties(component);
        
        FacesContext context = getFacesContext();

        if (_backgroundClass != null)
        {
            comp.setValueExpression("backgroundClass", _backgroundClass);
        } 
        if (_columnClass != null)
        {
            comp.setValueExpression("columnClass", _columnClass);
        } 
        if (_contentClass != null)
        {
            comp.setValueExpression("contentClass", _contentClass);
        } 
        if (_dateClass != null)
        {
            comp.setValueExpression("dateClass", _dateClass);
        } 
        if (_dayClass != null)
        {
            comp.setValueExpression("dayClass", _dayClass);
        } 
        if (_entryClass != null)
        {
            comp.setValueExpression("entryClass", _entryClass);
        } 
        if (_entryRenderer != null)
        {
            comp.setValueExpression("entryRenderer", _entryRenderer);
        } 
        if (_evenClass != null)
        {
            comp.setValueExpression("evenClass", _evenClass);
        } 
        if (_foregroundClass != null)
        {
            comp.setValueExpression("foregroundClass", _foregroundClass);
        } 
        if (_freeClass != null)
        {
            comp.setValueExpression("freeClass", _freeClass);
        } 
        if (_gutterClass != null)
        {
            comp.setValueExpression("gutterClass", _gutterClass);
        } 
        if (_headerClass != null)
        {
            comp.setValueExpression("headerClass", _headerClass);
        } 
        if (_holidayClass != null)
        {
            comp.setValueExpression("holidayClass", _holidayClass);
        } 
        if (_hoursClass != null)
        {
            comp.setValueExpression("hoursClass", _hoursClass);
        } 
        if (_inactiveDayClass != null)
        {
            comp.setValueExpression("inactiveDayClass", _inactiveDayClass);
        } 
        if (_minutesClass != null)
        {
            comp.setValueExpression("minutesClass", _minutesClass);
        } 
        if (_monthClass != null)
        {
            comp.setValueExpression("monthClass", _monthClass);
        } 
        if (_selectedClass != null)
        {
            comp.setValueExpression("selectedClass", _selectedClass);
        } 
        if (_selectedEntryClass != null)
        {
            comp.setValueExpression("selectedEntryClass", _selectedEntryClass);
        } 
        if (_subtitleClass != null)
        {
            comp.setValueExpression("subtitleClass", _subtitleClass);
        } 
        if (_textClass != null)
        {
            comp.setValueExpression("textClass", _textClass);
        } 
        if (_titleClass != null)
        {
            comp.setValueExpression("titleClass", _titleClass);
        } 
        if (_unevenClass != null)
        {
            comp.setValueExpression("unevenClass", _unevenClass);
        } 
        if (_weekClass != null)
        {
            comp.setValueExpression("weekClass", _weekClass);
        } 
        if (_enabledOnUserRole != null)
        {
            comp.setValueExpression("enabledOnUserRole", _enabledOnUserRole);
        } 
        if (_visibleOnUserRole != null)
        {
            comp.setValueExpression("visibleOnUserRole", _visibleOnUserRole);
        } 
        if (_actionExpression != null)
        {
            comp.setActionExpression(_actionExpression);
        }        
        if (_mouseListenerExpression != null)
        {
            comp.setMouseListenerExpression(_mouseListenerExpression);
        }        
        if (_actionListener != null)
        {
            comp.addActionListener(new MethodExpressionActionListener(_actionListener));
        }
        if (_splitWeekend != null)
        {
            comp.setValueExpression("splitWeekend", _splitWeekend);
        } 
        if (_submitOnClick != null)
        {
            comp.setValueExpression("submitOnClick", _submitOnClick);
        } 
        if (_compactMonthRowHeight != null)
        {
            comp.setValueExpression("compactMonthRowHeight", _compactMonthRowHeight);
        } 
        if (_compactWeekRowHeight != null)
        {
            comp.setValueExpression("compactWeekRowHeight", _compactWeekRowHeight);
        } 
        if (_converter != null)
        {
            if (!_converter.isLiteralText())
            {
                comp.setValueExpression("converter", _converter);
            }
            else
            {
                String s = _converter.getExpressionString();
                if (s != null)
                {            
                    Converter converter = getFacesContext().getApplication().createConverter(s);
                    comp.setConverter(converter);
                }
            }
        }
        if (_detailedRowHeight != null)
        {
            comp.setValueExpression("detailedRowHeight", _detailedRowHeight);
        } 
        if (_expandToFitEntries != null)
        {
            comp.setValueExpression("expandToFitEntries", _expandToFitEntries);
        } 
        if (_headerDateFormat != null)
        {
            comp.setValueExpression("headerDateFormat", _headerDateFormat);
        } 
        if (_immediate != null)
        {
            comp.setValueExpression("immediate", _immediate);
        } 
        if (_readonly != null)
        {
            comp.setValueExpression("readonly", _readonly);
        } 
        if (_renderZeroLengthEntries != null)
        {
            comp.setValueExpression("renderZeroLengthEntries", _renderZeroLengthEntries);
        } 
        if (_theme != null)
        {
            comp.setValueExpression("theme", _theme);
        } 
        if (_tooltip != null)
        {
            comp.setValueExpression("tooltip", _tooltip);
        } 
        if (_value != null)
        {
            comp.setValueExpression("value", _value);
        } 
        if (_visibleEndHour != null)
        {
            comp.setValueExpression("visibleEndHour", _visibleEndHour);
        } 
        if (_visibleStartHour != null)
        {
            comp.setValueExpression("visibleStartHour", _visibleStartHour);
        } 
        if (_workingEndHour != null)
        {
            comp.setValueExpression("workingEndHour", _workingEndHour);
        } 
        if (_workingStartHour != null)
        {
            comp.setValueExpression("workingStartHour", _workingStartHour);
        } 
        if (_hourNotation != null)
        {
            comp.setValueExpression("hourNotation", _hourNotation);
        } 
        if (_compactMonthDayOfWeekDateFormat != null)
        {
            comp.setValueExpression("compactMonthDayOfWeekDateFormat", _compactMonthDayOfWeekDateFormat);
        } 
    }

    public void release()
    {
        super.release();
        _backgroundClass = null;
        _columnClass = null;
        _contentClass = null;
        _dateClass = null;
        _dayClass = null;
        _entryClass = null;
        _entryRenderer = null;
        _evenClass = null;
        _foregroundClass = null;
        _freeClass = null;
        _gutterClass = null;
        _headerClass = null;
        _holidayClass = null;
        _hoursClass = null;
        _inactiveDayClass = null;
        _minutesClass = null;
        _monthClass = null;
        _selectedClass = null;
        _selectedEntryClass = null;
        _subtitleClass = null;
        _textClass = null;
        _titleClass = null;
        _unevenClass = null;
        _weekClass = null;
        _enabledOnUserRole = null;
        _visibleOnUserRole = null;
        _actionExpression = null;
        _mouseListenerExpression = null;
        _actionListener = null;
        _splitWeekend = null;
        _submitOnClick = null;
        _compactMonthRowHeight = null;
        _compactWeekRowHeight = null;
        _converter = null;
        _detailedRowHeight = null;
        _expandToFitEntries = null;
        _headerDateFormat = null;
        _immediate = null;
        _readonly = null;
        _renderZeroLengthEntries = null;
        _theme = null;
        _tooltip = null;
        _value = null;
        _visibleEndHour = null;
        _visibleStartHour = null;
        _workingEndHour = null;
        _workingStartHour = null;
        _hourNotation = null;
        _compactMonthDayOfWeekDateFormat = null;
    }
}
