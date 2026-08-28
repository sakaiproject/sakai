/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.myfaces.custom.schedule;

import java.io.Serializable;

import jakarta.faces.component.UIComponentBase;
import jakarta.faces.component.ValueHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;

import org.apache.myfaces.custom.schedule.model.ScheduleModel;
import org.apache.myfaces.custom.schedule.model.SimpleScheduleModel;
import org.apache.myfaces.custom.schedule.util.ScheduleUtil;

/**
 * Base class for the Schedule component. This class contains all the properties
 * for the schedule, but not the ActionSource stuff. Keeping these things separate
 * should make the code a little easier to digest.
 * 
 * @JSFComponent
 *   serialuid = "5702081384947086911L"
 *   class = "org.apache.myfaces.custom.schedule.UIScheduleBase"
 *   implements = "java.io.Serializable, jakarta.faces.component.ValueHolder"   
 * @since 1.1.7
 * @author Jurgen Lust
 * @version $Revision: 691856 $
 */
public abstract class AbstractUIScheduleBase extends UIComponentBase implements ValueHolder,
        Serializable
{
    //private static final long serialVersionUID = 5702081384947086911L;

    public static final String COMPONENT_FAMILY = "jakarta.faces.Panel";
    public static final String COMPONENT_TYPE = "org.apache.myfaces.ScheduleBase";
    public static final String RENDERER_TYPE = "org.apache.myfaces.Schedule";

    protected static final String HOUR_NOTATION_24 = "24";
    protected static final String HOUR_NOTATION_12 = "12";

    protected static final int DEFAULT_COMPACT_MONTH_ROWHEIGHT = 120;
    protected static final int DEFAULT_COMPACT_WEEK_ROWHEIGHT = 200;
    protected static final int DEFAULT_DETAILED_ROWHEIGHT = 22;
    protected static final boolean DEFAULT_EXPAND_TO_FIT = false;
    protected static final String DEFAULT_HEADER_DATE_FORMAT = null;
    protected static final boolean DEFAULT_IMMEDIATE = false;
    protected static final boolean DEFAULT_READONLY = false;
    protected static final boolean DEFAULT_RENDER_ZEROLENGTH = false;
    protected static final String DEFAULT_THEME = "default";
    protected static final boolean DEFAULT_TOOLTIP = false;
    protected static final int DEFAULT_VISIBLE_END_HOUR = 20;
    protected static final int DEFAULT_VISIBLE_START_HOUR = 8;
    protected static final int DEFAULT_WORKING_END_HOUR = 17;
    protected static final int DEFAULT_WORKING_START_HOUR = 9;
    protected static final String DEFAULT_ENABLED_ON_USER_ROLE = null;
    protected static final boolean DEFAULT_SUBMIT_ON_CLICK = false;
    protected static final String DEFAULT_VISIBLE_ON_USER_ROLE = null;
    protected static final boolean DEFAULT_SPLIT_WEEKEND = true;
    
    /**
     * @JSFProperty
     *   defaultValue = true;
     */
    public abstract boolean isSplitWeekend();
    
    /**
     * Should the parent form of this schedule be submitted when the user
     * clicks on a day? Note that this will only work when the readonly
     * property is set to false.
     * 
     * @JSFProperty
     *   defaultValue = "false"
     * @return the _submitOnClick
     */
    public abstract boolean isSubmitOnClick();

    /**
     * @JSFProperty
     *   defaultValue="120"
     * @return the compactMonthRowHeight
     */
    public abstract int getCompactMonthRowHeight();

    /**
     * @JSFProperty
     *   defaultValue = "200"
     * @return the compactWeekRowHeight
     */
    public abstract int getCompactWeekRowHeight();

    /**
     * @JSFProperty 
     * @see jakarta.faces.component.ValueHolder#getConverter()
     */
    public abstract Converter getConverter();

    /**
     * @JSFProperty
     *   defaultValue = "22"
     * @return the detailedRowHeight
     */
    public abstract int getDetailedRowHeight();

    /**
     * @JSFProperty
     *   defaultValue = "false"
     * @return the expandToFitEntries
     */
    public abstract boolean isExpandToFitEntries();

    /**
     * @see jakarta.faces.component.UIComponent#getFamily()
     */
    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }

    /**
     * @JSFProperty
     * @return the headerDateFormat
     */
    public abstract String getHeaderDateFormat();

    /**
     * @JSFProperty
     *   defaultValue="false"
     * @return the immediate
     */
    public abstract boolean isImmediate();

    /**
     * @see jakarta.faces.component.ValueHolder#getLocalValue()
     */
    public abstract Object getLocalValue();

    /**
     * The underlying model
     *
     * @return Returns the model.
     */
    public ScheduleModel getModel()
    {
        if (getValue() instanceof ScheduleModel)
        {
            return (ScheduleModel) getValue();
        }
        else
        {
            return new SimpleScheduleModel();
        }
    }

    /**
     * @JSFProperty
     *   defaultValue="false"
     * @return the readonly
     */
    public abstract boolean isReadonly();

    /**
     * @see jakarta.faces.component.UIComponentBase#getRendersChildren()
     */
    public boolean isRendersChildren()
    {
        return true;
    }

    /**
     * @JSFProperty
     * @return the renderZeroLengthEntries
     */
    public abstract boolean isRenderZeroLengthEntries();

    /**
     * @JSFProperty
     *   defaultValue = "default"
     * @return the theme
     */
    public abstract String getTheme();

    /**
     * @JSFProperty
     *   defaultValue = "false"
     * @return the tooltip
     */
    public abstract boolean isTooltip();

    /**
     * @JSFProperty
     *   localMethod="true"
     *   localMethodScope="public"
     * @see jakarta.faces.component.ValueHolder#getValue()
     */
    public abstract Object getValue();

    /**
     * @JSFProperty
     *   defaultValue = "20"
     * @return the visibleEndHour
     */
    public abstract int getVisibleEndHour();

    /**
     * @JSFProperty
     *   defaultValue = "8"
     * @return the visibleStartHour
     */
    public abstract int getVisibleStartHour();

    /**
     * @JSFProperty
     *   defaultValue = "17"
     * @return the workingEndHour
     */
    public abstract int getWorkingEndHour();

    /**
     * @JSFProperty
     *   defaultValue="9"
     * @return the workingStartHour
     */
    public abstract int getWorkingStartHour();

    /**
     * <p>
     * Show dates in 24 hour notation or 12 hour notation.
     * </p>
     * @JSFProperty
     * @return "12", "24" or null for the renderer default
     */
    public abstract String getHourNotation();

    /**
     * @JSFProperty
     * @return the headerDateFormat
     */
    public abstract String getCompactMonthDayOfWeekDateFormat();
    
    /**
     * The underlying model
     *
     * @param model The model to set.
     */
    public void setModel(ScheduleModel model)
    {
        setValue(model);
    }

}
