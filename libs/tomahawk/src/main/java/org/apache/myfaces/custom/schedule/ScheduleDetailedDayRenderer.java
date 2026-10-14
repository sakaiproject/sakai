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
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.TreeSet;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.custom.schedule.model.HalfHourInterval;
import org.apache.myfaces.custom.schedule.model.Interval;
import org.apache.myfaces.custom.schedule.model.ScheduleDay;
import org.apache.myfaces.custom.schedule.model.ScheduleEntry;
import org.apache.myfaces.custom.schedule.util.ScheduleUtil;
import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HTML;
import org.apache.myfaces.shared_tomahawk.renderkit.html.util.FormInfo;

/**
 * <p>
 * Renderer for the day and workweek views of the Schedule component
 * </p>
 * 
 * @since 1.1.7
 * @author Jurgen Lust (latest modification by $Author: jlust $)
 * @author Bruno Aranda (adaptation of Jurgen's code to myfaces)
 * @version $Revision: 392301 $
 */
public class ScheduleDetailedDayRenderer extends AbstractScheduleRenderer
        implements Serializable
{
    private static final Log log = LogFactory.getLog(ScheduleDetailedDayRenderer.class);
    private static final long serialVersionUID = -5103791076091317355L;

    //~ Instance fields --------------------------------------------------------

    private final int defaultRowHeightInPixels = 22;

    //~ Methods ----------------------------------------------------------------

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

        super.encodeBegin(context, component);

        HtmlSchedule schedule = (HtmlSchedule) component;
        ResponseWriter writer = context.getResponseWriter();
        int rowHeight = getRowHeight(schedule);

        //the number of rows in the grid is the number of half hours between
        //visible start hour and visible end hour, plus 1 for the header
        int numberOfRows = ((getRenderedEndHour(schedule) - getRenderedStartHour(schedule)) * 2) + 1;

        //the grid height = 22 pixels times the number of rows + 3, for the
        //table border and the cellpadding
        int gridHeight = (numberOfRows * rowHeight) + 3 + 10;

        //container div for the schedule grid
        writer.startElement(HTML.DIV_ELEM, schedule);
        writer.writeAttribute(HTML.CLASS_ATTR, "schedule-detailed-"
                + schedule.getTheme(), null);
        writer.writeAttribute(HTML.STYLE_ATTR, "height: "
                + String.valueOf(gridHeight) + "px; overflow: hidden;", null);
        writeBackgroundStart(context, schedule, writer);
        writeForegroundStart(context, schedule, writer);
    }

    /**
     * @see jakarta.faces.render.Renderer#encodeChildren(jakarta.faces.context.FacesContext,
     *      jakarta.faces.component.UIComponent)
     */
    public void encodeChildren(FacesContext context, UIComponent component)
            throws IOException
    {
        if (!component.isRendered())
        {
            return;
        }

        HtmlSchedule schedule = (HtmlSchedule) component;
        ResponseWriter writer = context.getResponseWriter();
        String clientId = schedule.getClientId(context);
        FormInfo parentFormInfo = RendererUtils.findNestingForm(schedule, context);
        String formId = parentFormInfo == null ? null : parentFormInfo.getFormName();

        for (Iterator dayIterator = schedule.getModel().iterator(); dayIterator
                .hasNext();)
        {
            ScheduleDay day = (ScheduleDay) dayIterator.next();
            String dayBodyId = clientId + "_body_" + ScheduleUtil.getDateId(day.getDate(), schedule.getModel().getTimeZone());
            writer.startElement(HTML.TD_ELEM, schedule);
            writer.writeAttribute(HTML.CLASS_ATTR, getStyleClass(schedule,
                    "column"), null);
            writer.writeAttribute(HTML.STYLE_ATTR, "height: 100%;", null);
            writer.startElement(HTML.DIV_ELEM, schedule);
            writer.writeAttribute(HTML.ID_ATTR, dayBodyId, null);
            writer.writeAttribute(HTML.CLASS_ATTR, getStyleClass(schedule,
                    "column"), null);
            writer.writeAttribute(
                    HTML.STYLE_ATTR,
                    "position: relative; top: 0px; left: 0px; width: 100%; height: 100%; z-index: 0;",
                    null);
            //register an onclick event listener to a column which will capture
            //the y coordinate of the mouse, to determine the hour of day
            if (!schedule.isReadonly() && schedule.isSubmitOnClick()) {
                writer.writeAttribute(
                        HTML.ONMOUSEUP_ATTR,
                        "fireScheduleTimeClicked(this, event, '"
                        + formId + "', '"
                        + clientId
                        + "');",
                        null);
            }
            writeEntries(context, schedule, day, writer);
            writer.endElement(HTML.DIV_ELEM);
            writer.endElement(HTML.TD_ELEM);
        }
    }

    /**
     * @see jakarta.faces.render.Renderer#encodeEnd(jakarta.faces.context.FacesContext,
     *      jakarta.faces.component.UIComponent)
     */
    public void encodeEnd(FacesContext context, UIComponent component)
            throws IOException
    {
        if (!component.isRendered())
        {
            return;
        }

        ResponseWriter writer = context.getResponseWriter();

        writeForegroundEnd(writer);
        writeBackgroundEnd(writer);
        writer.endElement(HTML.DIV_ELEM);
    }

    protected String getCellClass(HtmlSchedule schedule, ScheduleDay day, boolean even, int hour)
    {
        String cellClass = "free";
        if (!day.isWorkingDay())
        {
            return getStyleClass(schedule, cellClass);
        }

        if (hour >= schedule.getWorkingStartHour()
                && hour < schedule.getWorkingEndHour())
        {
            cellClass = even ? "even" : "uneven";
        }

        return getStyleClass(schedule, cellClass);
    }

    protected boolean isSelected(HtmlSchedule schedule, EntryWrapper entry)
    {
        ScheduleEntry selectedEntry = schedule.getModel().getSelectedEntry();

        if (selectedEntry == null)
        {
            return false;
        }

        boolean returnboolean = selectedEntry.getId().equals(
                entry.entry.getId());

        return returnboolean;
    }

    protected void maximizeEntries(EntryWrapper[] entries, int numberOfColumns)
    {
        for (int i = 0; i < entries.length; i++)
        {
            EntryWrapper entry = entries[i];

            //now see if we can expand the entry to the columns on the right
            while (((entry.column + entry.colspan) < numberOfColumns)
                    && entry.canFitInColumn(entry.column + entry.colspan))
            {
                entry.colspan++;
            }
        }
    }

    protected void scanEntries(EntryWrapper[] entries, int index)
    {
        if (entries.length <= 0)
        {
            return;
        }

        EntryWrapper entry = entries[index];
        entry.column = 0;

        //see what columns are already taken
        for (int i = 0; i < index; i++)
        {
            if (entry.overlaps(entries[i]))
            {
                entry.overlappingEntries.add(entries[i]);
                entries[i].overlappingEntries.add(entry);
            }
        }

        //find an available column
        while (!entry.canFitInColumn(entry.column))
        {
            entry.column++;
        }

        //recursively scan the remaining entries for overlaps
        if (++index < entries.length)
        {
            scanEntries(entries, index);
        }
    }

    protected void writeGutter(FacesContext context, HtmlSchedule schedule,
            ResponseWriter writer, boolean useIntervalLabels) throws IOException
            {
        final int rowHeight = getRowHeight(schedule);
        final int headerHeight = rowHeight + 9;

        int startHour = getRenderedStartHour(schedule);
        int endHour = getRenderedEndHour(schedule);

        DateFormat hourFormater = getDateFormat(context, schedule, 
                HtmlSchedule.HOUR_NOTATION_12.equals(schedule.getHourNotation()) ? "h" : "HH");
        DateFormat minuteFormater = getDateFormat(context, schedule, 
                HtmlSchedule.HOUR_NOTATION_12.equals(schedule.getHourNotation()) ? "':'mma" : "mm");        
        DateFormat shortMinuteFormater = getDateFormat(context, schedule, 
                HtmlSchedule.HOUR_NOTATION_12.equals(schedule.getHourNotation()) ? "a" : "mm");        

        ScheduleDay day = (ScheduleDay) schedule.getModel().iterator().next();

        writer.startElement(HTML.TABLE_ELEM, schedule);
        writer.writeAttribute(HTML.CELLPADDING_ATTR, "0", null);
        writer.writeAttribute(HTML.CELLSPACING_ATTR, "1", null);
        writer.writeAttribute(HTML.CLASS_ATTR, getStyleClass(schedule, "background"), null);
         writer.writeAttribute(HTML.STYLE_ATTR, "height: 100%", null);
        writer.startElement(HTML.TBODY_ELEM, schedule);

        writer.startElement(HTML.TR_ELEM, schedule);

        // the header gutter
        writer.startElement(HTML.TD_ELEM, schedule);
        writer.writeAttribute(HTML.CLASS_ATTR, getStyleClass(schedule, "gutter"), null);
        writer.writeAttribute(
                HTML.STYLE_ATTR,
                "height: "
                + headerHeight
                + "px; border-style: none; border-width: 0px; overflow: hidden; padding: 0px",
                null);
        writer.startElement(HTML.DIV_ELEM, schedule);
        writer.writeAttribute(HTML.STYLE_ATTR, "height: 1px; width: 56px", null);
        writer.endElement(HTML.DIV_ELEM);
        writer.endElement(HTML.TD_ELEM);
        writer.endElement(HTML.TR_ELEM);

        // the intervals
        Iterator intervalIt = day.getIntervals(startHour, endHour).iterator();

        boolean renderGutter = true;

        while (intervalIt.hasNext())
        {
            Interval interval = (Interval) intervalIt.next();
            int intervalHeight = calcRowHeight(rowHeight, interval.getDuration()) - 1;

            // Don't render rows where the timespan is too small
            if (intervalHeight <= 0)
            {
                continue;
            }

            if (!renderGutter)
            {
                renderGutter = true;
                continue;
            }

            writer.startElement(HTML.TR_ELEM, schedule);

            int gutterHeight = intervalHeight;

            if (day.getIntervals() == null && interval.getStartMinutes(getTimeZone(schedule)) == 0)
            {
                gutterHeight = (gutterHeight * 2) + 1;
                renderGutter = false;
            }                    

            //write the hours of the day on the left
            //this only happens on even rows, or every hour
            writer.startElement(HTML.TD_ELEM, schedule);
            writer.writeAttribute(HTML.CLASS_ATTR, getStyleClass(schedule, "gutter"), null);
            writer.writeAttribute(
                    HTML.STYLE_ATTR,
                    "height: " + gutterHeight
                    + "px; border-style: none; border-width: 0px; overflow: hidden; padding: 0px",
                    null);
            if (interval.getDuration() >= HalfHourInterval.HALF_HOUR)
            {
                if (!useIntervalLabels || interval.getLabel() == null)
                {
                    writer.startElement(HTML.SPAN_ELEM, schedule);
                    writer.writeAttribute(HTML.CLASS_ATTR, getStyleClass(schedule,
                            renderGutter ? "minutes" : "hours"), null);
                    writer.writeAttribute(HTML.STYLE_ATTR,
                            "vertical-align: top; height: 100%; padding: 0px;",
                            null);
                    writer.writeText(hourFormater.format(interval.getStartTime()), null);
                    writer.endElement(HTML.SPAN_ELEM);
                }
                writer.startElement(HTML.SPAN_ELEM, schedule);
                writer.writeAttribute(HTML.CLASS_ATTR, getStyleClass(schedule,
                "minutes"), null);
                writer.writeAttribute(HTML.STYLE_ATTR,
                        "vertical-align: top; height: 100%; padding: 0px;",
                        null);
                if (useIntervalLabels && interval.getLabel() != null)
                {
                    writer.writeText(interval.getLabel(), null);
                }
                else
                {
                    writer.writeText((renderGutter ? minuteFormater : shortMinuteFormater).format(interval.getStartTime()), null);
                }
                writer.endElement(HTML.SPAN_ELEM);
            }
            writer.endElement(HTML.TD_ELEM);
            writer.endElement(HTML.TR_ELEM);
        }

        writer.endElement(HTML.TBODY_ELEM);
        writer.endElement(HTML.TABLE_ELEM);            
            }

    protected void writeBackgroundStart(FacesContext context, HtmlSchedule schedule,
            ResponseWriter writer) throws IOException
            {
        boolean repeatedIntervals = schedule.getModel().containsRepeatedIntervals();

        writer.startElement(HTML.TABLE_ELEM, schedule);
        writer.writeAttribute(HTML.CELLPADDING_ATTR, "0", null);
        writer.writeAttribute(HTML.CELLSPACING_ATTR, "0", null);
        writer.writeAttribute(HTML.STYLE_ATTR, "width: 100%; height: 100%", null);
        writer.startElement(HTML.TBODY_ELEM, schedule);
        writer.startElement(HTML.TR_ELEM, schedule);
        writer.startElement(HTML.TD_ELEM, schedule);
        writer.writeAttribute(HTML.STYLE_ATTR, "width: 56px; vertical-align: top;", null);

        // Render gutter outside background, to allow it to have a flexible width
        writeGutter(context, schedule, writer, repeatedIntervals);

        writer.endElement(HTML.TD_ELEM);
        writer.startElement(HTML.TD_ELEM, schedule);
        writer.writeAttribute(HTML.STYLE_ATTR, "width: 99%; vertical-align: top;", null);

        final String clientId = schedule.getClientId(context);
        FormInfo parentFormInfo = RendererUtils.findNestingForm(schedule, context);
        String formId = parentFormInfo == null ? null : parentFormInfo.getFormName();

        final int rowHeight = getRowHeight(schedule);
        final int headerHeight = rowHeight + 9;
        writer.startElement(HTML.DIV_ELEM, schedule);
        writer.writeAttribute(HTML.CLASS_ATTR, getStyleClass(schedule,
                "background"), null);
        writer
        .writeAttribute(
                HTML.STYLE_ATTR,
                "position: relative; width: 100%; height: 100%; z-index: 0;",
                null);

        //background table for the schedule grid
        writer.startElement(HTML.TABLE_ELEM, schedule);
        writer.writeAttribute(HTML.CLASS_ATTR, getStyleClass(schedule,
        "background"), null);
        writer.writeAttribute(HTML.CELLPADDING_ATTR, "0", null);
        writer.writeAttribute(HTML.CELLSPACING_ATTR, "0", null);
        writer.writeAttribute(HTML.STYLE_ATTR, "width: 100%; height: 100%",
                null);
        writer.startElement(HTML.TBODY_ELEM, schedule);
        writer.startElement(HTML.TR_ELEM, schedule);

        float columnWidth = (schedule.getModel().size() == 0) ? 100
                : (100 / schedule.getModel().size());

        int startHour = getRenderedStartHour(schedule);
        int endHour = getRenderedEndHour(schedule);

        ScheduleDay day = null;

        for (Iterator dayIterator = schedule.getModel().iterator(); dayIterator.hasNext();)
        {
            writer.startElement(HTML.TD_ELEM, schedule);

            writer.writeAttribute(HTML.STYLE_ATTR, "width: " + String.valueOf(columnWidth)+ "%", null);

            day = (ScheduleDay) dayIterator.next();

            writer.startElement(HTML.TABLE_ELEM, schedule);
            writer.writeAttribute(HTML.CELLPADDING_ATTR, "0", null);
            writer.writeAttribute(HTML.CELLSPACING_ATTR, "1", null);
            writer.writeAttribute(HTML.STYLE_ATTR, "width: 100%; height: 100%",
                    null);
            writer.startElement(HTML.TBODY_ELEM, schedule);

            writer.startElement(HTML.TR_ELEM, schedule);

            // the header
            final String dayHeaderId = clientId + "_header_" + ScheduleUtil.getDateId(day.getDate(), schedule.getModel().getTimeZone());

            writer.startElement(HTML.TH_ELEM, schedule);              
            writer.writeAttribute(HTML.CLASS_ATTR, getStyleClass(schedule,
                    "header"), null);
            writer
            .writeAttribute(
                    HTML.STYLE_ATTR,
                    "height: " + headerHeight + "px; vertical-align: top; border-style: none; border-width: 0px; overflow: hidden;",
                    null);

            boolean isToday = ScheduleUtil.isSameDay(day.getDate(), new Date(), schedule.getModel().getTimeZone());

            // write the date
            writer.startElement(HTML.ANCHOR_ELEM, schedule);
            writer.writeAttribute(HTML.ID_ATTR, dayHeaderId, null);
            writer.writeAttribute(HTML.HREF_ATTR, "#", null);
            writer.writeAttribute(HTML.CLASS_ATTR, getStyleClass(schedule, "date") 
                    + (isToday ? " today" : ""), null);
            writer
            .writeAttribute(
                    HTML.STYLE_ATTR,
                    "display: block; height: 50%; width: 100%; overflow: hidden; white-space: nowrap;",
                    null);

            //register an onclick event listener to a column header which will
            //be used to determine the date
            if (!schedule.isReadonly() && schedule.isSubmitOnClick()) {
                writer.writeAttribute(
                        HTML.ONCLICK_ATTR,
                        "fireScheduleDateClicked(this, event, '"
                        + formId + "', '"
                        + clientId
                        + "');",
                        null);
            }

            writer.writeText(getDateString(context, schedule, day.getDate()),
                    null);
            writer.endElement(HTML.ANCHOR_ELEM);

            // write the name of the holiday, if there is one
            if ((day.getSpecialDayName() != null)
                    && (day.getSpecialDayName().length() > 0))
            {
                writer.startElement(HTML.SPAN_ELEM, schedule);
                writer.writeAttribute(HTML.CLASS_ATTR, getStyleClass(schedule,
                        "holiday"), null);
                writer
                .writeAttribute(
                        HTML.STYLE_ATTR,
                        "height: 50%; width: 100%; overflow: hidden; white-space: nowrap;",
                        null);
                writer.writeText(day.getSpecialDayName(), null);
                writer.endElement(HTML.SPAN_ELEM);
            }

            writer.endElement(HTML.TH_ELEM);
            writer.endElement(HTML.TR_ELEM);

            // the intervals
            Iterator intervalIt = day.getIntervals(startHour, endHour).iterator();
            boolean even = false;

            while (intervalIt.hasNext())
            {
                Interval interval = (Interval) intervalIt.next();
                int intervalHeight = calcRowHeight(rowHeight, interval.getDuration()) - 1;

                // Don't render rows where the timespan is too small
                if (intervalHeight <= 0)
                {
                    continue;
                }

                writer.startElement(HTML.TR_ELEM, schedule);

                writer.startElement(HTML.TD_ELEM, schedule);
                writer.writeAttribute(HTML.CLASS_ATTR, getCellClass(schedule,
                        day, even, interval.getStartHours(getTimeZone(schedule))), null);
                writer.writeAttribute(HTML.STYLE_ATTR,
                        "overflow: hidden; height: " + intervalHeight + "px;", null);
                if (!repeatedIntervals && interval.getLabel() != null)
                {
                    writer.write(interval.getLabel());
                }
                writer.endElement(HTML.TD_ELEM);

                writer.endElement(HTML.TR_ELEM);

                even = !even;
            }

            writer.endElement(HTML.TBODY_ELEM);
            writer.endElement(HTML.TABLE_ELEM);            

            writer.endElement(HTML.TD_ELEM);
        }

        writer.endElement(HTML.TR_ELEM);
        writer.endElement(HTML.TBODY_ELEM);
        writer.endElement(HTML.TABLE_ELEM);
    }

    protected void writeBackgroundEnd(ResponseWriter writer) throws IOException
    {
        writer.endElement(HTML.DIV_ELEM);
    
        writer.endElement(HTML.TD_ELEM);
        writer.endElement(HTML.TR_ELEM);
        writer.endElement(HTML.TBODY_ELEM);
        writer.endElement(HTML.TABLE_ELEM);        
    }

    /**
     * Calculate an actual row height, given a specified height for a half hour duration.
     * 
     * @param halfHourHeight The height for a half hour duration
     * @param duration The actual interval duration
     * @return The height for the actual interval duration
     */
    private int calcRowHeight(int halfHourHeight, long duration) {

        return duration == HalfHourInterval.HALF_HOUR ? halfHourHeight : 
            (int)((halfHourHeight / (float)(30 * 60 * 1000)) * duration);
    }

    protected int getRenderedStartHour(HtmlSchedule schedule)
    {
        int startHour = schedule.getVisibleStartHour();

        //default behaviour: do not auto-expand the schedule to display all
        //entries
        if (!expandToFitEntries(schedule)) return startHour;

        for (Iterator dayIterator = schedule.getModel().iterator(); dayIterator.hasNext();)
        {
            ScheduleDay day = (ScheduleDay) dayIterator.next();
            int dayStart = day.getFirstEventHour();

            if (dayStart < startHour) {
                startHour = dayStart;
            }
        }

        return startHour;
    }

    protected int getRenderedEndHour(HtmlSchedule schedule)
    {
        int endHour = schedule.getVisibleEndHour();

        //default behaviour: do not auto-expand the schedule to display all
        //entries
        if (!expandToFitEntries(schedule)) return endHour;

        for (Iterator dayIterator = schedule.getModel().iterator(); dayIterator.hasNext();)
        {
            ScheduleDay day = (ScheduleDay) dayIterator.next();
            int dayEnd = day.getLastEventHour();

            if (dayEnd > endHour) {
                endHour = dayEnd;
            }
        }

        return endHour;
    }

    protected void writeEntries(FacesContext context, HtmlSchedule schedule,
            ScheduleDay day, ResponseWriter writer) throws IOException
    {
        final String clientId = schedule.getClientId(context);
        FormInfo parentFormInfo = RendererUtils.findNestingForm(schedule, context);
        String formId = parentFormInfo == null ? null : parentFormInfo.getFormName();

        TreeSet entrySet = new TreeSet();

        for (Iterator entryIterator = day.iterator(); entryIterator.hasNext();)
        {
            entrySet.add(new EntryWrapper((ScheduleEntry) entryIterator.next(),
                    day));
        }

        EntryWrapper[] entries = (EntryWrapper[]) entrySet
                .toArray(new EntryWrapper[entrySet.size()]);

        //determine overlaps
        scanEntries(entries, 0);

        //determine the number of columns within this day
        int maxColumn = 0;

        for (Iterator entryIterator = entrySet.iterator(); entryIterator
                .hasNext();)
        {
            EntryWrapper wrapper = (EntryWrapper) entryIterator.next();
            maxColumn = Math.max(wrapper.column, maxColumn);
        }

        int numberOfColumns = maxColumn + 1;

        //make sure the entries take up all available space horizontally
        maximizeEntries(entries, numberOfColumns);

        //now determine the width in percent of 1 column
        float columnWidth = 100 / numberOfColumns;

        //and now draw the entries in the columns
        for (Iterator entryIterator = entrySet.iterator(); entryIterator
                .hasNext();)
        {
            EntryWrapper wrapper = (EntryWrapper) entryIterator.next();
            boolean selected = isSelected(schedule, wrapper);
            //compose the CSS style for the entry box
            StringBuffer entryStyle = new StringBuffer();
            entryStyle.append(wrapper.getBounds(schedule, columnWidth));
            String entryBorderColor = getEntryRenderer(schedule).getColor(
                    context, schedule, wrapper.entry, selected);
            if (entryBorderColor != null)
            {
                entryStyle.append(" border-color: ");
                entryStyle.append(entryBorderColor);
                entryStyle.append(";");
            }

            if (selected)
            {
                writer.startElement(HTML.DIV_ELEM, schedule);
                writer.writeAttribute(HTML.CLASS_ATTR, getStyleClass(schedule,
                        "entry-selected"), null);
                writer.writeAttribute(HTML.STYLE_ATTR, entryStyle.toString(),
                        null);

                //draw the tooltip
                if (schedule.isTooltip())
                {
                    getEntryRenderer(schedule).renderToolTip(context, writer,
                            schedule, wrapper.entry, selected);
                }

                //draw the content
                getEntryRenderer(schedule).renderContent(context, writer,
                        schedule, day, wrapper.entry, false, selected);
                writer.endElement(HTML.DIV_ELEM);
            }
            else
            {
                //if the schedule is read-only, the entries should not be
                //hyperlinks
                writer.startElement(
                        schedule.isReadonly() ? HTML.DIV_ELEM : HTML.ANCHOR_ELEM, schedule);

                //draw the tooltip
                if (schedule.isTooltip())
                {
                    getEntryRenderer(schedule).renderToolTip(context, writer,
                            schedule, wrapper.entry, selected);
                }

                if (!schedule.isReadonly())
                {
                    writer.writeAttribute(HTML.HREF_ATTR, "#", null);

                    writer.writeAttribute(
                            HTML.ONCLICK_ATTR,
                            "fireEntrySelected('"
                            + formId + "', '"
                            + clientId + "', '"
                            + wrapper.entry.getId()
                            + "');",
                            null);
                }

                writer.writeAttribute(HTML.CLASS_ATTR, getEntryRenderer(schedule).getEntryClass(schedule, wrapper.entry), null);
                writer.writeAttribute(HTML.STYLE_ATTR, entryStyle.toString(),
                        null);

                //draw the content
                getEntryRenderer(schedule).renderContent(context, writer,
                        schedule, day, wrapper.entry, false, selected);

                writer.endElement(schedule.isReadonly() ? HTML.DIV_ELEM : HTML.ANCHOR_ELEM);
            }
        }
    }

    protected void writeForegroundEnd(ResponseWriter writer) throws IOException
    {
        writer.endElement(HTML.TR_ELEM);
        writer.endElement(HTML.TABLE_ELEM);
        writer.endElement(HTML.DIV_ELEM);
    }

    protected void writeForegroundStart(FacesContext context,
            HtmlSchedule schedule, ResponseWriter writer) throws IOException
    {
        final int rowHeight = getRowHeight(schedule) - 1;
        final int headerHeight = rowHeight + 10;

        writer.startElement(HTML.DIV_ELEM, schedule);
        writer.writeAttribute(HTML.CLASS_ATTR, getStyleClass(schedule,
                "foreground"), null);
        writer.writeAttribute(
                HTML.STYLE_ATTR,
                "position: absolute; left: 0px; top: " + headerHeight + "px; width: 100%; height: 100%;    z-index: 2;",
                null);

        writer.startElement(HTML.TABLE_ELEM, schedule);
        writer.writeAttribute(HTML.CLASS_ATTR, getStyleClass(schedule,
                "foreground"), null);
        writer.writeAttribute(HTML.CELLSPACING_ATTR, "1", null);
        writer.writeAttribute(HTML.CELLPADDING_ATTR, "0", null);
        writer.writeAttribute(HTML.STYLE_ATTR, "width: 100%; height: 100%",
                null);

        float columnWidth = (schedule.getModel().size() == 0) ? 100
                : (100 / schedule.getModel().size());

        for (Iterator dayIterator = schedule.getModel().iterator(); dayIterator
                .hasNext();)
        {
            dayIterator.next();
            writer.startElement("col", schedule);

            writer.writeAttribute(HTML.STYLE_ATTR, 
                    "width: " + String.valueOf(columnWidth) + "%;", null);
            writer.endElement("col");
        }

        writer.startElement(HTML.TR_ELEM, schedule);
    }

    //~ Inner Classes ----------------------------------------------------------

    protected int getDefaultRowHeight()
    {
        return defaultRowHeightInPixels;
    }

    /**
     * In the detailed day renderer, we take the y coordinate of the mouse
     * into account when determining the last clicked date.
     */
    protected Date determineLastClickedDate(HtmlSchedule schedule, String dateId, String yPos) {
        //the dateId is the schedule client id + "_" + yyyyMMdd
        String day = dateId.substring(dateId.lastIndexOf("_") + 1);
        Date date = ScheduleUtil.getDateFromId(day, schedule.getModel().getTimeZone());

        Calendar cal = getCalendarInstance(schedule, date != null ? date : new Date());
        cal.set(Calendar.HOUR_OF_DAY, getRenderedStartHour(schedule));
        //OK, we have the date, let's determine the time
        try {
            int y = Integer.parseInt(yPos);
            int halfHourHeight = getRowHeight(schedule);
            int minutes = y * 30 / halfHourHeight;
            cal.add(Calendar.MINUTE, minutes);
        } catch (NumberFormatException nfe) {
            log.debug("y position is not a number");
        }
        log.debug("last clicked datetime: " + cal.getTime());
        return cal.getTime();
    }

    /**
     * <p>
     * When the start- and endtime of an entry are the same, should the entry
     * be rendered, fitting the entry box to the text? 
     * </p>
     * 
     * @param component the component
     * @return whether or not zero length entries should be rendered
     */
    protected boolean renderZeroLengthEntries(UIComponent component) {
        if (component instanceof UIScheduleBase)
        {
            UIScheduleBase schedule = (UIScheduleBase) component;
            return schedule.isRenderZeroLengthEntries();
        } else {
            return false;
        }
    }

    /**
     * <p>
     * When the start- and endtime of an entry are the same, should the entry
     * be rendered, fitting the entry box to the text?
     * </p>
     *
     * @param component the component
     * @return whether or not zero length entries should be rendered
     */
    protected boolean expandToFitEntries(UIComponent component) {
        if (component instanceof HtmlSchedule)
        {
            HtmlSchedule schedule = (HtmlSchedule) component;
            return schedule.isExpandToFitEntries();
        } 
        return false;
    }


    protected class EntryWrapper implements Comparable
    {
        //~ Static fields/initializers -----------------------------------------

        private static final int HALF_HOUR = 1000 * 60 * 30;

        //~ Instance fields ----------------------------------------------------

        private final ScheduleDay day;
        private final ScheduleEntry entry;
        private final TreeSet overlappingEntries;
        private int colspan;
        private int column;

        //~ Constructors -------------------------------------------------------

        EntryWrapper(ScheduleEntry entry, ScheduleDay day)
        {
            this.entry = entry;
            this.day = day;
            this.column = 0;
            this.colspan = 1;
            this.overlappingEntries = new TreeSet();
        }

        //~ Methods ------------------------------------------------------------

        /**
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(Object o)
        {
            return comparator.compare(entry, o);
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object o)
        {
            if (o instanceof EntryWrapper)
            {
                EntryWrapper other = (EntryWrapper) o;

                boolean returnboolean = (entry.getStartTime()
                        .equals(other.entry.getStartTime()))
                        && (entry.getEndTime().equals(other.entry.getEndTime()))
                        && (entry.getId().equals(other.entry.getId()))
                        && (day.equals(other.day));
                /*
                 new EqualsBuilder().append(
                 entry.getStartTime(), other.entry.getStartTime()
                 ).append(entry.getEndTime(), other.entry.getEndTime())
                 .append(
                 entry.getId(), other.entry.getId()
                 ).append(day, other.day).isEquals();
                 */
                return returnboolean;
            }

            return false;
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        public int hashCode()
        {
            int returnint = entry.getStartTime().hashCode()
                    ^ entry.getEndTime().hashCode() ^ entry.getId().hashCode();

            return returnint;
        }

        /**
         * <p>
         * Determine the bounds of this entry, in CSS position attributes
         * </p>
         *
         * @param schedule the schedule
         * @param columnWidth the width of a column
         *
         * @return the bounds
         */
        String getBounds(HtmlSchedule schedule, float columnWidth)
        {
            int rowHeight = getRowHeight(schedule);
            float width = (columnWidth * colspan) - 0.5f;
            float left = column * columnWidth;
            Calendar cal = getCalendarInstance(schedule, day.getDate());

            int curyear = cal.get(Calendar.YEAR);
            int curmonth = cal.get(Calendar.MONTH);
            int curday = cal.get(Calendar.DATE);

            cal.setTime(entry.getStartTime());
            cal.set(curyear, curmonth, curday);

            long startMillis = cal.getTimeInMillis();
            cal.set(Calendar.HOUR_OF_DAY, getRenderedStartHour(schedule));
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            long visibleStartMillis = cal.getTimeInMillis();
            startMillis = day.equalsDate(entry.getStartTime()) ? Math.max(
                    startMillis, visibleStartMillis) : visibleStartMillis;
            cal.setTime(entry.getEndTime());
            cal.set(curyear, curmonth, curday);

            long endMillis = cal.getTimeInMillis();
            cal.set(Calendar.HOUR_OF_DAY, getRenderedEndHour(schedule));
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            long visibleEndMillis = cal.getTimeInMillis();
            endMillis = day.equalsDate(entry.getEndTime()) ? Math.min(
                    endMillis, visibleEndMillis) : visibleEndMillis;

            int top = (int) (((startMillis - visibleStartMillis) * rowHeight) / HALF_HOUR);
            int height = (int) (((endMillis - startMillis) * rowHeight) / HALF_HOUR);
            StringBuffer buffer = new StringBuffer();

            boolean entryVisible = height > 0 || renderZeroLengthEntries(schedule);

            if (!entryVisible)
            {
                buffer.append("visibility: hidden; ");
            }
            buffer.append("position: absolute; height: ");
            if (height > 2)
            {
                // Adjust for the width of the border
                buffer.append((height - 2) + "px");
            } else if (height > 0)
            {
                buffer.append(height + "px");
            } else if (entryVisible)
            {
                buffer.append("auto");
            } else
            {
                buffer.append("0px");
            }
            buffer.append("; top: ");
            buffer.append(top);
            buffer.append("px; left: ");
            buffer.append(left);
            buffer.append("%; width: ");
            buffer.append(width);
            buffer
                    .append("%; padding: 0px; overflow: hidden; border-width: 1.0px; border-style:solid;");

            return buffer.toString();
        }

        /**
         * <p>
         * Can this entry fit in the specified column?
         * </p>
         *
         * @param column the column
         *
         * @return whether the entry fits
         */
        boolean canFitInColumn(int column)
        {
            for (Iterator overlapIterator = overlappingEntries.iterator(); overlapIterator
                    .hasNext();)
            {
                EntryWrapper overlap = (EntryWrapper) overlapIterator.next();

                if (overlap.column == column)
                {
                    return false;
                }
            }

            return true;
        }

        /**
         * <p>
         * What is the minimum end time allocated to this event?
         * Where the event has a duration, the end time of the event
         * is the minimum end time.<br />
         * Where the event has no duration, a minimum end time of half
         * and hour after the start is implemented.
         * </p>
         * @return The minimum end time of the event
         */
        Date minimumEndTime() {
            Date start = entry.getStartTime();
            Date end = entry.getEndTime();

            return end != null ?
                    (end.after(start) ? end : new Date(start.getTime() + HALF_HOUR))
                    : null;
        }

        /**
         * <p>
         * Does this entry overlap with another?
         * </p>
         *
         * @param other the other entry
         *
         * @return whether the entries overlap
         */
        boolean overlaps(EntryWrapper other)
        {
            Date start = entry.getStartTime();
            Date end = minimumEndTime();

            if ((start == null) || (end == null))
            {
                return false;
            }

            boolean returnboolean = (start.before(
                    other.minimumEndTime()) && end.after(
                            other.entry.getStartTime()));

            return returnboolean;
        }
    }


    protected int getRowHeight(UIScheduleBase schedule)
    {
        if (schedule != null) {
            int height = schedule.getDetailedRowHeight();
            return height <= 0 ? getDefaultRowHeight() : height;
        }
        return getDefaultRowHeight();
    }

    private TimeZone getTimeZone(UIScheduleBase schedule)
    {

        return schedule != null && schedule.getModel().getTimeZone() != null ? 
                schedule.getModel().getTimeZone() 
                : TimeZone.getDefault();
    }
}
//The End
