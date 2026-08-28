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

import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.el.ValueBinding;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.event.ComponentSystemEventListener;
import jakarta.faces.render.Renderer;

import org.apache.myfaces.custom.schedule.model.ScheduleEntry;
import org.apache.myfaces.custom.schedule.util.ScheduleEntryComparator;
import org.apache.myfaces.custom.schedule.util.ScheduleUtil;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HTML;
import org.apache.myfaces.tomahawk.application.PreRenderViewAddResourceEvent;
import org.apache.myfaces.tomahawk.util.TomahawkResourceUtils;

/**
 * <p>
 * Abstract superclass for all renderer of the UISchedule component
 * </p>
 * @since 1.1.7
 * @author Jurgen Lust (latest modification by $Author: mkienenb $)
 * @author Bruno Aranda (adaptation of Jurgen's code to myfaces)
 * @version $Revision: 389938 $
 */
public abstract class AbstractScheduleRenderer extends Renderer implements
        Serializable, ComponentSystemEventListener
{
    //~ Static fields/initializers ---------------------------------------------
    protected static final ScheduleEntryComparator comparator = new ScheduleEntryComparator();
    protected static final String LAST_CLICKED_DATE = "_last_clicked_date";
    protected static final String LAST_CLICKED_Y = "_last_clicked_y";
    private static final String CSS_RESOURCE = "css/schedule.css";
    public static final String DEFAULT_THEME = "default";
    public static final String OUTLOOK_THEME = "outlookxp";
    public static final String EVOLUTION_THEME = "evolution";

    //~ Methods ----------------------------------------------------------------
    public void processEvent(ComponentSystemEvent event)
    {
        if (event instanceof PreRenderViewAddResourceEvent)
        {
            HtmlSchedule schedule = (HtmlSchedule) event.getComponent();
            
            String theme = schedule.getTheme();
            //The default css file is only loaded if the theme is one of the provided
            //themes.
            if (DEFAULT_THEME.equals(theme) || OUTLOOK_THEME.equals(theme)
                    || EVOLUTION_THEME.equals(theme))
            {
                FacesContext facesContext = FacesContext.getCurrentInstance();
                
                //addResource.addStyleSheet(context, AddResource.HEADER_BEGIN,
                //       HtmlSchedule.class, CSS_RESOURCE);
                TomahawkResourceUtils.addOutputStylesheetResource(facesContext,
                        "oam.custom.schedule.css",
                        "schedule.css");
            }
    
            if (schedule.isTooltip())
            {
                FacesContext facesContext = FacesContext.getCurrentInstance();
                
                TomahawkResourceUtils.addOutputScriptResource(facesContext,
                        "oam.custom.schedule.javascript",
                        "alphaAPI.js");
                
                TomahawkResourceUtils.addOutputScriptResource(facesContext,
                        "oam.custom.schedule.javascript",
                        "domLib.js");
    
                TomahawkResourceUtils.addOutputScriptResource(facesContext,
                        "oam.custom.schedule.javascript",
                        "domTT.js");
    
                TomahawkResourceUtils.addOutputScriptResource(facesContext,
                        "oam.custom.schedule.javascript",
                        "fadomatic.js");
            }
        }
    }
        
    /**
     * @see jakarta.faces.render.Renderer#decode(jakarta.faces.context.FacesContext,
     *      jakarta.faces.component.UIComponent)
     */
    public void decode(FacesContext context, UIComponent component)
    {
        HtmlSchedule schedule = (HtmlSchedule) component;
        boolean queueAction = false;
        if (ScheduleUtil.canModifyValue(component))
        {
            Map parameters = context.getExternalContext()
                    .getRequestParameterMap();
            String selectedEntryId = (String) parameters.get((String) schedule
                    .getClientId(context));
            String lastClickedDateId = (String) parameters
                    .get((String) schedule.getClientId(context)
                            + LAST_CLICKED_DATE);
            String lastClickedY = (String) parameters.get((String) schedule
                    .getClientId(context)
                    + LAST_CLICKED_Y);

            ScheduleMouseEvent mouseEvent = null;

            if ((selectedEntryId != null) && (selectedEntryId.length() > 0))
            {
                ScheduleEntry entry = schedule.findEntry(selectedEntryId);
                schedule.setSubmittedEntry(entry);
                mouseEvent = new ScheduleMouseEvent(schedule,
                        ScheduleMouseEvent.SCHEDULE_ENTRY_CLICKED);
                queueAction = true;
            }

            if (schedule.isSubmitOnClick())
            {
                schedule.resetMouseEvents();
                if ((lastClickedY != null) && (lastClickedY.length() > 0))
                {
                    //the body of the schedule was clicked
                    schedule
                            .setLastClickedDateAndTime(determineLastClickedDate(
                                    schedule, lastClickedDateId, lastClickedY));
                    mouseEvent = new ScheduleMouseEvent(schedule,
                            ScheduleMouseEvent.SCHEDULE_BODY_CLICKED);
                    queueAction = true;
                }
                else if ((lastClickedDateId != null)
                        && (lastClickedDateId.length() > 0))
                {
                    //the header of the schedule was clicked
                    schedule
                            .setLastClickedDateAndTime(determineLastClickedDate(
                                    schedule, lastClickedDateId, "0"));
                    mouseEvent = new ScheduleMouseEvent(schedule,
                            ScheduleMouseEvent.SCHEDULE_HEADER_CLICKED);
                    queueAction = true;
                }
                else if (mouseEvent == null)
                {
                    //the form was posted without mouse events on the schedule
                    mouseEvent = new ScheduleMouseEvent(schedule,
                            ScheduleMouseEvent.SCHEDULE_NOTHING_CLICKED);
                }
            }

            if (mouseEvent != null)
                schedule.queueEvent(mouseEvent);
        }
        if (queueAction)
        {
            schedule.queueEvent(new ActionEvent(schedule));
        }
    }

    /**
     * @see jakarta.faces.render.Renderer#encodeBegin(jakarta.faces.context.FacesContext,
     *      jakarta.faces.component.UIComponent)
     */
    public void encodeBegin(FacesContext context, UIComponent component)
            throws IOException
    {
        if (!component.isRendered())
        {
            return;
        }

        HtmlSchedule schedule = (HtmlSchedule) component;
        ResponseWriter writer = context.getResponseWriter();

        //add needed CSS and Javascript files to the header 
        /*
        AddResource addResource = AddResourceFactory.getInstance(context);
        String theme = schedule.getTheme();
        //The default css file is only loaded if the theme is one of the provided
        //themes.
        if (DEFAULT_THEME.equals(theme) || OUTLOOK_THEME.equals(theme)
                || EVOLUTION_THEME.equals(theme))
        {
            addResource.addStyleSheet(context, AddResource.HEADER_BEGIN,
                    HtmlSchedule.class, CSS_RESOURCE);
        }
        addResource.addJavaScriptAtPosition(context, AddResource.HEADER_BEGIN,
                HtmlSchedule.class, "javascript/schedule.js");
        
        if (schedule.isTooltip())
        {
            addResource.addJavaScriptAtPosition(context, AddResource.HEADER_BEGIN,
                    HtmlSchedule.class, "javascript/alphaAPI.js");
            addResource.addJavaScriptAtPosition(context, AddResource.HEADER_BEGIN,
                    HtmlSchedule.class, "javascript/domLib.js");
            addResource.addJavaScriptAtPosition(context, AddResource.HEADER_BEGIN,
                    HtmlSchedule.class, "javascript/domTT.js");
            addResource.addJavaScriptAtPosition(context, AddResource.HEADER_BEGIN,
                    HtmlSchedule.class, "javascript/fadomatic.js");
        }*/
        
        //hidden input field containing the id of the selected entry
        writer.startElement(HTML.INPUT_ELEM, schedule);
        writer.writeAttribute(HTML.TYPE_ATTR, "hidden", null);
        writer.writeAttribute(HTML.NAME_ATTR, schedule.getClientId(context),
                "clientId");
        writer.endElement(HTML.INPUT_ELEM);
        //hidden input field containing the id of the last clicked date
        writer.startElement(HTML.INPUT_ELEM, schedule);
        writer.writeAttribute(HTML.TYPE_ATTR, "hidden", null);
        writer.writeAttribute(HTML.NAME_ATTR, schedule.getClientId(context)
                + "_last_clicked_date", "clicked_date");
        writer.endElement(HTML.INPUT_ELEM);
        //hidden input field containing the y coordinate of the mouse when
        //the schedule was clicked. This will be used to determine the hour
        //of day.
        writer.startElement(HTML.INPUT_ELEM, schedule);
        writer.writeAttribute(HTML.TYPE_ATTR, "hidden", null);
        writer.writeAttribute(HTML.NAME_ATTR, schedule.getClientId(context)
                + "_last_clicked_y", "clicked_y");
        writer.endElement(HTML.INPUT_ELEM);
    }

    /**
     * <p>
     * Get the String representation of a date, taking into account the
     * specified date format or the current Locale.
     * </p>
     *
     * @param context the FacesContext
     * @param schedule the component
     * @param date the date
     *
     * @return the date string
     */
    protected String getDateString(FacesContext context, UIScheduleBase schedule,
            Date date)
    {

        return getDateFormat(context, schedule, schedule.getHeaderDateFormat(), date).format(date);
    }

    protected static DateFormat getDateFormat(FacesContext context, UIScheduleBase schedule, String pattern)
    {
        Locale viewLocale = context.getViewRoot().getLocale();               
        DateFormat format = (pattern != null && pattern.length() > 0) ? 
                new SimpleDateFormat(pattern, viewLocale) :
                DateFormat.getDateInstance(DateFormat.MEDIUM, viewLocale);
        
        format.setTimeZone(schedule.getModel().getTimeZone());
        
        return format;
    }
    
    protected static DateFormat getDateFormat(FacesContext context, UIScheduleBase schedule, String pattern, Date date)
    {
        Locale viewLocale = context.getViewRoot().getLocale();
        
        if (pattern != null && pattern.indexOf("d'th'") >= 0)
        {
            pattern = pattern.replaceAll("d'th'", "d'" + daySuffix(schedule, date, viewLocale) + "'");
        }
        
        return getDateFormat(context, schedule, pattern);
    }
    
    private static String daySuffix(UIScheduleBase schedule, Date date, Locale locale) {
        String language = locale.getLanguage();
        Calendar calendar = ScheduleUtil.getCalendarInstance(date, schedule.getModel().getTimeZone());

        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        if (Locale.ENGLISH.getLanguage().equals(language))
        {
            switch(dayOfMonth) {
            case 1:
            case 21:
            case 31:
                return "st";
            case 2:
            case 22:
                return "nd";
            case 3:
            case 23:
                return "rd";
            default:
                return "th";
            }
        }
        else if (Locale.GERMAN.getLanguage().equals(language))
        {
            return ".";
        }
        else
        {
            return "";
        }
    }
    
    /**
     * <p>
     * Allow the developer to specify custom CSS classnames for the schedule
     * component.
     * </p>
     * @param component the component
     * @param className the default CSS classname
     * @return the custom classname
     */
    protected String getStyleClass(UIComponent component, String className)
    {
        //first check if the styleClass property is a value binding expression
        ValueBinding binding = component.getValueBinding(className);
        if (binding != null)
        {
            String value = (String) binding.getValue(FacesContext
                    .getCurrentInstance());

            if (value != null)
            {
                return value;
            }
        }
        //it's not a value binding expression, so check for the string value
        //in the attributes
        Map attributes = component.getAttributes();
        String returnValue = (String) attributes.get(className + "Class");
        return returnValue == null ? className : returnValue;
    }

    /**
     * The user of the Schedule component can customize the look and feel
     * by specifying a custom implementation of the ScheduleEntryRenderer.
     * This method gets an instance of the specified class, or if none
     * was specified, takes the default.
     * 
     * @param component the Schedule component
     * @return a ScheduleEntryRenderer instance
     */
    protected ScheduleEntryRenderer getEntryRenderer(HtmlSchedule schedule)
    {
            Object entryRenderer = schedule.getEntryRenderer();
            if (entryRenderer instanceof ScheduleEntryRenderer)
            {
                return (ScheduleEntryRenderer) entryRenderer;
            } else {
                return new DefaultScheduleEntryRenderer();
            }
    }

    /**
     * @return The default height, in pixels, of one row in the schedule grid
     */
    protected abstract int getDefaultRowHeight();

    /**
     * @param schedule
     *            The schedule
     * 
     * @return The row height, in pixels
     */
    protected abstract int getRowHeight(UIScheduleBase schedule);

    /**
     * Determine the last clicked date
     * @param schedule the schedule component
     * @param dateId the string identifying the date
     * @param yPos the y coordinate of the mouse
     * @return the clicked date
     */
    protected abstract Date determineLastClickedDate(HtmlSchedule schedule,
            String dateId, String yPos);

    /**
     * @see jakarta.faces.render.Renderer#getRendersChildren()
     */
    public boolean getRendersChildren()
    {
        return true;
    }
    
    protected Calendar getCalendarInstance(UIScheduleBase schedule, Date date)
    {       
        return ScheduleUtil.getCalendarInstance(date, schedule.getModel().getTimeZone());
    }
}
//The End
