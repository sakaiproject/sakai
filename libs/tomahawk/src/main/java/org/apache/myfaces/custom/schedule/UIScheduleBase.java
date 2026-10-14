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

import jakarta.el.ValueExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.PartialStateHolder;
import jakarta.faces.component.StateHolder;
import org.apache.myfaces.component.AttachedDeltaWrapper;
import jakarta.faces.component.UIComponent;
import jakarta.faces.convert.Converter;


// Generated from class org.apache.myfaces.custom.schedule.AbstractUIScheduleBase.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class UIScheduleBase extends org.apache.myfaces.custom.schedule.AbstractUIScheduleBase
    implements java.io.Serializable, jakarta.faces.component.ValueHolder
{
    private static final long serialVersionUID = 5702081384947086911L; 

    static public final String COMPONENT_FAMILY =
        "jakarta.faces.Panel";
    static public final String COMPONENT_TYPE =
        "org.apache.myfaces.ScheduleBase";


    public UIScheduleBase()
    {
        setRendererType(null);
    }

    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }



    
    // Property: splitWeekend
    public boolean isSplitWeekend()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.splitWeekend, true);
    }
    
    public void setSplitWeekend(boolean splitWeekend)
    {
        getStateHelper().put(PropertyKeys.splitWeekend, splitWeekend ); 
    }    
    // Property: submitOnClick
    public boolean isSubmitOnClick()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.submitOnClick, false);
    }
    
    public void setSubmitOnClick(boolean submitOnClick)
    {
        getStateHelper().put(PropertyKeys.submitOnClick, submitOnClick ); 
    }    
    // Property: compactMonthRowHeight
    public int getCompactMonthRowHeight()
    {
        return (Integer) getStateHelper().eval(PropertyKeys.compactMonthRowHeight, 120);
    }
    
    public void setCompactMonthRowHeight(int compactMonthRowHeight)
    {
        getStateHelper().put(PropertyKeys.compactMonthRowHeight, compactMonthRowHeight ); 
    }    
    // Property: compactWeekRowHeight
    public int getCompactWeekRowHeight()
    {
        return (Integer) getStateHelper().eval(PropertyKeys.compactWeekRowHeight, 200);
    }
    
    public void setCompactWeekRowHeight(int compactWeekRowHeight)
    {
        getStateHelper().put(PropertyKeys.compactWeekRowHeight, compactWeekRowHeight ); 
    }    
    // Property: converter
    public Converter getConverter()
    {
        return (Converter) getStateHelper().eval(PropertyKeys.converter);
    }
    
    public void setConverter(Converter converter)
    {
        getStateHelper().put(PropertyKeys.converter, converter ); 
    }    
    // Property: detailedRowHeight
    public int getDetailedRowHeight()
    {
        return (Integer) getStateHelper().eval(PropertyKeys.detailedRowHeight, 22);
    }
    
    public void setDetailedRowHeight(int detailedRowHeight)
    {
        getStateHelper().put(PropertyKeys.detailedRowHeight, detailedRowHeight ); 
    }    
    // Property: expandToFitEntries
    public boolean isExpandToFitEntries()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.expandToFitEntries, false);
    }
    
    public void setExpandToFitEntries(boolean expandToFitEntries)
    {
        getStateHelper().put(PropertyKeys.expandToFitEntries, expandToFitEntries ); 
    }    
    // Property: headerDateFormat
    public String getHeaderDateFormat()
    {
        return (String) getStateHelper().eval(PropertyKeys.headerDateFormat);
    }
    
    public void setHeaderDateFormat(String headerDateFormat)
    {
        getStateHelper().put(PropertyKeys.headerDateFormat, headerDateFormat ); 
    }    
    // Property: immediate
    public boolean isImmediate()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.immediate, false);
    }
    
    public void setImmediate(boolean immediate)
    {
        getStateHelper().put(PropertyKeys.immediate, immediate ); 
    }    
    // Property: readonly
    public boolean isReadonly()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.readonly, false);
    }
    
    public void setReadonly(boolean readonly)
    {
        getStateHelper().put(PropertyKeys.readonly, readonly ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.READONLY_PROP);
    }    
    // Property: renderZeroLengthEntries
    public boolean isRenderZeroLengthEntries()
    {
        Object value = (Boolean) getStateHelper().eval(PropertyKeys.renderZeroLengthEntries);
        if (value != null)
        {
            return (Boolean) value;        
        }
        return false;
    }
    
    public void setRenderZeroLengthEntries(boolean renderZeroLengthEntries)
    {
        getStateHelper().put(PropertyKeys.renderZeroLengthEntries, renderZeroLengthEntries ); 
    }    
    // Property: theme
    public String getTheme()
    {
        return (String) getStateHelper().eval(PropertyKeys.theme, "default");
    }
    
    public void setTheme(String theme)
    {
        getStateHelper().put(PropertyKeys.theme, theme ); 
    }    
    // Property: tooltip
    public boolean isTooltip()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.tooltip, false);
    }
    
    public void setTooltip(boolean tooltip)
    {
        getStateHelper().put(PropertyKeys.tooltip, tooltip ); 
    }    
    // Property: value
    final public Object getLocalValue()
    {
        return  getStateHelper().get(PropertyKeys.value);
    }
     
    public Object getValue()
    {
        return  getStateHelper().eval(PropertyKeys.value);
    }
    
    public void setValue(Object value)
    {
        getStateHelper().put(PropertyKeys.value, value ); 
    }    
    // Property: visibleEndHour
    public int getVisibleEndHour()
    {
        return (Integer) getStateHelper().eval(PropertyKeys.visibleEndHour, 20);
    }
    
    public void setVisibleEndHour(int visibleEndHour)
    {
        getStateHelper().put(PropertyKeys.visibleEndHour, visibleEndHour ); 
    }    
    // Property: visibleStartHour
    public int getVisibleStartHour()
    {
        return (Integer) getStateHelper().eval(PropertyKeys.visibleStartHour, 8);
    }
    
    public void setVisibleStartHour(int visibleStartHour)
    {
        getStateHelper().put(PropertyKeys.visibleStartHour, visibleStartHour ); 
    }    
    // Property: workingEndHour
    public int getWorkingEndHour()
    {
        return (Integer) getStateHelper().eval(PropertyKeys.workingEndHour, 17);
    }
    
    public void setWorkingEndHour(int workingEndHour)
    {
        getStateHelper().put(PropertyKeys.workingEndHour, workingEndHour ); 
    }    
    // Property: workingStartHour
    public int getWorkingStartHour()
    {
        return (Integer) getStateHelper().eval(PropertyKeys.workingStartHour, 9);
    }
    
    public void setWorkingStartHour(int workingStartHour)
    {
        getStateHelper().put(PropertyKeys.workingStartHour, workingStartHour ); 
    }    
    // Property: hourNotation
    public String getHourNotation()
    {
        return (String) getStateHelper().eval(PropertyKeys.hourNotation);
    }
    
    public void setHourNotation(String hourNotation)
    {
        getStateHelper().put(PropertyKeys.hourNotation, hourNotation ); 
    }    
    // Property: compactMonthDayOfWeekDateFormat
    public String getCompactMonthDayOfWeekDateFormat()
    {
        return (String) getStateHelper().eval(PropertyKeys.compactMonthDayOfWeekDateFormat);
    }
    
    public void setCompactMonthDayOfWeekDateFormat(String compactMonthDayOfWeekDateFormat)
    {
        getStateHelper().put(PropertyKeys.compactMonthDayOfWeekDateFormat, compactMonthDayOfWeekDateFormat ); 
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
         splitWeekend
        , submitOnClick
        , compactMonthRowHeight
        , compactWeekRowHeight
        , converter
        , detailedRowHeight
        , expandToFitEntries
        , headerDateFormat
        , immediate
        , readonly
        , renderZeroLengthEntries
        , theme
        , tooltip
        , value
        , visibleEndHour
        , visibleStartHour
        , workingEndHour
        , workingStartHour
        , hourNotation
        , compactMonthDayOfWeekDateFormat
    }

 }
