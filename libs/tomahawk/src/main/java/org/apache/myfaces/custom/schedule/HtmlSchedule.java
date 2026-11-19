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
import java.util.Date;
import jakarta.el.MethodExpression;
import jakarta.faces.el.MethodBinding;


// Generated from class org.apache.myfaces.custom.schedule.AbstractHtmlSchedule.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class HtmlSchedule extends org.apache.myfaces.custom.schedule.AbstractHtmlSchedule
    implements java.io.Serializable
{
    private static final long serialVersionUID = 5859593107442371656L; 

    static public final String COMPONENT_FAMILY =
        "jakarta.faces.Panel";
    static public final String COMPONENT_TYPE =
        "org.apache.myfaces.Schedule";
    static public final String DEFAULT_RENDERER_TYPE = 
        "org.apache.myfaces.Schedule";


    public HtmlSchedule()
    {
        setRendererType("org.apache.myfaces.Schedule");
    }

    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }



    
    // Property: backgroundClass
    public String getBackgroundClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.backgroundClass);
    }
    
    public void setBackgroundClass(String backgroundClass)
    {
        getStateHelper().put(PropertyKeys.backgroundClass, backgroundClass ); 
    }    
    // Property: columnClass
    public String getColumnClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.columnClass);
    }
    
    public void setColumnClass(String columnClass)
    {
        getStateHelper().put(PropertyKeys.columnClass, columnClass ); 
    }    
    // Property: contentClass
    public String getContentClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.contentClass);
    }
    
    public void setContentClass(String contentClass)
    {
        getStateHelper().put(PropertyKeys.contentClass, contentClass ); 
    }    
    // Property: dateClass
    public String getDateClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.dateClass);
    }
    
    public void setDateClass(String dateClass)
    {
        getStateHelper().put(PropertyKeys.dateClass, dateClass ); 
    }    
    // Property: dayClass
    public String getDayClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.dayClass);
    }
    
    public void setDayClass(String dayClass)
    {
        getStateHelper().put(PropertyKeys.dayClass, dayClass ); 
    }    
    // Property: entryClass
    public String getEntryClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.entryClass);
    }
    
    public void setEntryClass(String entryClass)
    {
        getStateHelper().put(PropertyKeys.entryClass, entryClass ); 
    }    
    // Property: entryRenderer
    public Object getEntryRenderer()
    {
        return  getStateHelper().eval(PropertyKeys.entryRenderer);
    }
    
    public void setEntryRenderer(Object entryRenderer)
    {
        getStateHelper().put(PropertyKeys.entryRenderer, entryRenderer ); 
    }    
    // Property: evenClass
    public String getEvenClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.evenClass);
    }
    
    public void setEvenClass(String evenClass)
    {
        getStateHelper().put(PropertyKeys.evenClass, evenClass ); 
    }    
    // Property: foregroundClass
    public String getForegroundClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.foregroundClass);
    }
    
    public void setForegroundClass(String foregroundClass)
    {
        getStateHelper().put(PropertyKeys.foregroundClass, foregroundClass ); 
    }    
    // Property: freeClass
    public String getFreeClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.freeClass);
    }
    
    public void setFreeClass(String freeClass)
    {
        getStateHelper().put(PropertyKeys.freeClass, freeClass ); 
    }    
    // Property: gutterClass
    public String getGutterClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.gutterClass);
    }
    
    public void setGutterClass(String gutterClass)
    {
        getStateHelper().put(PropertyKeys.gutterClass, gutterClass ); 
    }    
    // Property: headerClass
    public String getHeaderClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.headerClass);
    }
    
    public void setHeaderClass(String headerClass)
    {
        getStateHelper().put(PropertyKeys.headerClass, headerClass ); 
    }    
    // Property: holidayClass
    public String getHolidayClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.holidayClass);
    }
    
    public void setHolidayClass(String holidayClass)
    {
        getStateHelper().put(PropertyKeys.holidayClass, holidayClass ); 
    }    
    // Property: hoursClass
    public String getHoursClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.hoursClass);
    }
    
    public void setHoursClass(String hoursClass)
    {
        getStateHelper().put(PropertyKeys.hoursClass, hoursClass ); 
    }    
    // Property: inactiveDayClass
    public String getInactiveDayClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.inactiveDayClass);
    }
    
    public void setInactiveDayClass(String inactiveDayClass)
    {
        getStateHelper().put(PropertyKeys.inactiveDayClass, inactiveDayClass ); 
    }    
    // Property: minutesClass
    public String getMinutesClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.minutesClass);
    }
    
    public void setMinutesClass(String minutesClass)
    {
        getStateHelper().put(PropertyKeys.minutesClass, minutesClass ); 
    }    
    // Property: monthClass
    public String getMonthClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.monthClass);
    }
    
    public void setMonthClass(String monthClass)
    {
        getStateHelper().put(PropertyKeys.monthClass, monthClass ); 
    }    
    // Property: selectedClass
    public String getSelectedClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.selectedClass);
    }
    
    public void setSelectedClass(String selectedClass)
    {
        getStateHelper().put(PropertyKeys.selectedClass, selectedClass ); 
    }    
    // Property: selectedEntryClass
    public String getSelectedEntryClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.selectedEntryClass);
    }
    
    public void setSelectedEntryClass(String selectedEntryClass)
    {
        getStateHelper().put(PropertyKeys.selectedEntryClass, selectedEntryClass ); 
    }    
    // Property: subtitleClass
    public String getSubtitleClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.subtitleClass);
    }
    
    public void setSubtitleClass(String subtitleClass)
    {
        getStateHelper().put(PropertyKeys.subtitleClass, subtitleClass ); 
    }    
    // Property: textClass
    public String getTextClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.textClass);
    }
    
    public void setTextClass(String textClass)
    {
        getStateHelper().put(PropertyKeys.textClass, textClass ); 
    }    
    // Property: titleClass
    public String getTitleClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.titleClass);
    }
    
    public void setTitleClass(String titleClass)
    {
        getStateHelper().put(PropertyKeys.titleClass, titleClass ); 
    }    
    // Property: unevenClass
    public String getUnevenClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.unevenClass);
    }
    
    public void setUnevenClass(String unevenClass)
    {
        getStateHelper().put(PropertyKeys.unevenClass, unevenClass ); 
    }    
    // Property: weekClass
    public String getWeekClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.weekClass);
    }
    
    public void setWeekClass(String weekClass)
    {
        getStateHelper().put(PropertyKeys.weekClass, weekClass ); 
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
         backgroundClass
        , columnClass
        , contentClass
        , dateClass
        , dayClass
        , entryClass
        , entryRenderer
        , evenClass
        , foregroundClass
        , freeClass
        , gutterClass
        , headerClass
        , holidayClass
        , hoursClass
        , inactiveDayClass
        , minutesClass
        , monthClass
        , selectedClass
        , selectedEntryClass
        , subtitleClass
        , textClass
        , titleClass
        , unevenClass
        , weekClass
        , enabledOnUserRole
        , visibleOnUserRole
        , readonly
    }

 }
