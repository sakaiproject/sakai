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
package org.apache.myfaces.custom.calendar;

import org.apache.myfaces.component.AlignProperty;
import org.apache.myfaces.component.LibraryLocationAware;
import org.apache.myfaces.component.LocationAware;
import org.apache.myfaces.component.UserRoleAware;
import org.apache.myfaces.component.UserRoleUtils;
import org.apache.myfaces.component.html.ext.HtmlInputText;

/**
 *  <p>
 *  Provides a calendar. The calendar can be "inline", or a button can be rendered
 *  that displays the calendar in a "popup window" when clicked. Javascript is
 *  required for the popup window.
 *  </p>
 *  <p>
 *  The two forms of calendar are unfortunately not well integrated; this component is
 *  effectively two components that happen to use the same component class. Some
 *  attributes on the component are applicable only to the inline form while others
 *  are applicable only to the popup form.
 *  </p>
 *  <p>
 *  The appearance of the inline calendar can be controlled via attributes
 *  such as currentDayCellClass, dayCellClass, weekRowClass, monthYearRowClass. 
 *  Attributes "styleLocation", "javascriptLocation", "imageLocation" and all
 *  attributes starting with "popup" have no effect on an inline calendar.
 *  </p>
 *  <ul>
 *  <p>
 *  The appearance of the popup calendar can be controlled via attributes
 *  popupTheme, styleLocation, javascriptLocation and imageLocation:
 *  </p>
 *    <li>popupTheme: When styleLocation is not overridden then this selects one of the
 *    built-in themes ("WH" or "DB"); the default is "DB". This also selects the prefix
 * used for the names of style classes attached to generated dom elements; all style
 * names are of form "jscalendar-{popupTheme}-*".  
 * </li>
 * <li>styleLocation: specifies the URL of a directory in which a "theme.css" file exists.
 *  A reference to this theme.css file will automatically be output. Specifying "none" as the
 *  location prevents the generation of this stylesheet reference; it is assumed that the
 *  necessary style rules will be loaded via some other mechanism. Defaults to a reference
 *  to a location within the tomahawk jarfile which varies based on popupTheme.</li>
 * <li>javascriptLocation: specifies the URL of a directory in which all the necessary script
 *   files can be found. A reference to scripts "prototype.js", "date.js" and "popcalendar.js"
 *   will automatically be output. Specifying "none" prevents generation of these references;
 *       it is assumed that the necessary javascript functions will be loaded via some other
 *       mechanism. Defaults to a reference to a location within the tomahawk jarfile.</li>
 *    <li>imageLocation: specifies the URL of a directory in which all the necessary icons are
 *       defined. Defaults to a reference to a location within the tomahawk jarfile which
 *       varies depending on popupTheme.</li>
 *  </ul>
 *  Other styling attributes (eg dayCellClass, weekRowClass) are ignored for the popup calendar.
 *  <p>
 *  Unless otherwise specified, all attributes accept static values or EL expressions.
 *  </p>
 * 
 * @JSFComponent
 *   name = "t:inputCalendar"
 *   class = "org.apache.myfaces.custom.calendar.HtmlInputCalendar"
 *   tagClass = "org.apache.myfaces.custom.calendar.HtmlInputCalendarTag"
 *   tagSuperclass = "org.apache.myfaces.custom.calendar.AbstractHtmlInputCalendarTag"
 *   tagHandler = "org.apache.myfaces.custom.calendar.HtmlInputCalendarTagHandler"
 * @since 1.1.7
 * @author Martin Marinschek (latest modification by $Author: lu4242 $)
 * @version $Revision: 691856 $ $Date: 2008-09-03 21:40:30 -0500 (mié, 03 sep 2008) $
 */
public abstract class AbstractHtmlInputCalendar
        extends HtmlInputText implements UserRoleAware, LocationAware,
        AlignProperty, LibraryLocationAware
{

    public static final String COMPONENT_TYPE = "org.apache.myfaces.HtmlInputCalendar";
    private static final String DEFAULT_RENDERER_TYPE = "org.apache.myfaces.Calendar";
    
    public boolean isRendered()
    {
        if (!UserRoleUtils.isVisibleOnUserRole(this)) return false;
        return super.isRendered();
    }
    
    /**
     * Indicate an object used as a bridge between the java.util.Date instance
     * used by this component internally and the value object used on the bean,
     * referred as a "business" value.
     * 
     * <ul>
     * <li>If the value is literal, look for the mentioned class instance, 
     * create a new instance and assign to the component property.</li>
     * <li>If it the value a EL Expression, set the expression to the 
     * component property.</li>
     * </ul> 
     * 
     * @JSFProperty stateHolder="true" inheritedTag="true"
     */
    public abstract DateBusinessConverter getDateBusinessConverter();
    
    public abstract void setDateBusinessConverter(DateBusinessConverter dateBusinessConverter);
        
    /**
     * CSS class to be used on the TR element for the header-row showing month and year.
     * 
     * @JSFProperty 
     */
    public abstract String getMonthYearRowClass();

    /**
     * CSS class to be used on the TR element for the header-row showing the week-days.
     * 
     * @JSFProperty 
     */
    public abstract String getWeekRowClass();

    /**
     * CSS class to be used for the TD element containing a day days.
     * 
     * @JSFProperty 
     */
    public abstract String getDayCellClass();

    /**
     * CSS class to be used for the TD element of the currently selected date.
     * 
     * @JSFProperty 
     */
    public abstract String getCurrentDayCellClass();

    /**
     * Render the input-calendar left of the button, not right like normally done.
     * 
     * @JSFProperty
     *   defaultValue = "false" 
     */
    public abstract boolean isPopupLeft();

    /**
     * Render the input-calendar as a java-script popup on client.
     * 
     * @JSFProperty
     *   defaultValue = false; 
     */
    public abstract boolean isRenderAsPopup();

    /**
     * Automatically add the input-calendar scripts and css files to 
     * the header - set that to false to provide the scripts yourself.
     * 
     * @JSFProperty
     *   defaultValue = "true" 
     */
    public abstract boolean isAddResources();
    
    public abstract void setAddResources(boolean value);
    
    public void setAddResources(Boolean value)
    {
        this.setAddResources(value.booleanValue());
    }

    /**
     * Defines the string displayed on the button which leads to 
     * the calendar-popup-window (... by default).
     * 
     * @JSFProperty 
     */
    public abstract String getPopupButtonString();

    /**
     * Defines the css style for the button which leads to the 
     * calendar-popup-window.
     * 
     * @JSFProperty 
     */
    public abstract String getPopupButtonStyle();

    /**
     * Defines the css style class for the button which leads to the 
     * calendar-popup-window.
     * 
     * @JSFProperty 
     */
    public abstract String getPopupButtonStyleClass();

    /**
     * If true, renders a calendar icon instead of the button to pop up the calendar.
     * 
     * @JSFProperty
     *   defaultValue = "false" 
     */
    public abstract boolean isRenderPopupButtonAsImage();

    /**
     * Defines the date format used by the java-script popup on client.
     * 
     * @JSFProperty 
     */
    public abstract String getPopupDateFormat();
    
    /**
     * Set the string for "Go To Current Month"
     * 
     * @JSFProperty 
     */
    public abstract String getPopupGotoString();

    /**
     * Set the string for "Today is"
     * 
     * @JSFProperty 
     */
    public abstract String getPopupTodayString();

    /**
     * Defines the date format used by the java-script popup 
     * on client for the today-is string.
     * 
     * @JSFProperty 
     */
    public abstract String getPopupTodayDateFormat();

    /**
     * Set the string for "Wk"
     * 
     * @JSFProperty 
     */
    public abstract String getPopupWeekString();

    /**
     * Set the string for scrolling to the left.
     * 
     * @JSFProperty 
     */
    public abstract String getPopupScrollLeftMessage();

    /**
     * Set the string for scrolling to the right.
     * 
     * @JSFProperty 
     */
    public abstract String getPopupScrollRightMessage();

    /**
     * Set the string for "Click to select a month".
     * 
     * @JSFProperty 
     */
    public abstract String getPopupSelectMonthMessage();

    /**
     * Set the string for "Click to select a year".
     * 
     * @JSFProperty 
     */
    public abstract String getPopupSelectYearMessage();

    /**
     * Set the string for "Select [date] as date" (do not 
     * replace [date], it will be replaced by the current date).
     * 
     * @JSFProperty 
     */
    public abstract String getPopupSelectDateMessage();

    /**
     * Set the theme-prefix for this component.
     * 
     * @JSFProperty 
     */
    public abstract String getPopupTheme();

    /**
     * Url to the image for this popupButton.
     * 
     * @JSFProperty 
     */
    public abstract String getPopupButtonImageUrl();

    /**
     * The text that will be rendered in the field - helping the 
     * user to find the right format to enter into the field.
     * 
     * @JSFProperty 
     */
    public abstract String getHelpText();


    /**
     * <p>
     * May be "day", "week", "month" or "none":
     * <ul>
     * <li>day (default): allow the user to select a day.</li>
     * <li>week: only allow the user to select a week.</li>
     * <li>month: only allow the user to select a month.</li> 
     * <li>none: equivalent to "readonly".</li> 
     * </ul>
     * </p>
     * 
     * @JSFProperty
     *   defaultValue = "day" 
     */
    public abstract String getPopupSelectMode();
}
