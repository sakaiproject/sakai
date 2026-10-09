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


import org.apache.myfaces.custom.schedule.model.ScheduleDay;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HTML;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Iterator;


/**
 * <p>
 * Renderer for the week view of the UISchedule component
 * </p>
 * @since 1.1.7
 * @author Jurgen Lust (latest modification by $Author: schof $)
 * @author Bruno Aranda (adaptation of Jurgen's code to myfaces)
 * @version $Revision: 382051 $
 */
public class ScheduleCompactWeekRenderer
    extends AbstractCompactScheduleRenderer
    implements Serializable
{
    private static final long serialVersionUID = 5504081783797695487L;

    //~ Methods ----------------------------------------------------------------

    /**
     * @see jakarta.faces.render.Renderer#encodeBegin(jakarta.faces.context.FacesContext,
     *      jakarta.faces.component.UIComponent)
     */
    public void encodeBegin(
        FacesContext context,
        UIComponent component
    )
        throws IOException
    {
        if (!component.isRendered()) {
            return;
        }

        super.encodeBegin(context, component);

        HtmlSchedule schedule = (HtmlSchedule) component;
        ResponseWriter writer = context.getResponseWriter();

        //container div for the schedule grid
        writer.startElement(HTML.DIV_ELEM, schedule);
        writer.writeAttribute(HTML.CLASS_ATTR, "schedule-compact-" + schedule.getTheme(), null);
        writer.writeAttribute(
            HTML.STYLE_ATTR, "border-style: none; overflow: hidden;", null
        );

        writer.startElement(HTML.TABLE_ELEM, schedule);
        writer.writeAttribute(HTML.CLASS_ATTR, getStyleClass(schedule, "week"), null);
        writer.writeAttribute(
            HTML.STYLE_ATTR, "position: relative; left: 0px; top: 0px; width: 100%;",
            null
        );
        writer.writeAttribute(HTML.CELLPADDING_ATTR, "0", null);
        writer.writeAttribute(HTML.CELLSPACING_ATTR, "1", null);
        writer.writeAttribute("border", "0", null);
        writer.writeAttribute(HTML.WIDTH_ATTR, "100%", null);
        writer.startElement(HTML.TBODY_ELEM, schedule);

        for (
            Iterator dayIterator = schedule.getModel().iterator();
                dayIterator.hasNext();
        ) {
            ScheduleDay day = (ScheduleDay) dayIterator.next();
            Calendar cal = getCalendarInstance(schedule, day.getDate());

            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
            boolean isWeekend =
                (dayOfWeek == Calendar.SATURDAY) ||
                (dayOfWeek == Calendar.SUNDAY);

            if (
                (dayOfWeek == Calendar.MONDAY) ||
                    (dayOfWeek == Calendar.WEDNESDAY) ||
                    (dayOfWeek == Calendar.FRIDAY) ||
                    (dayOfWeek == Calendar.SUNDAY)
            ) {
                writer.startElement(HTML.TR_ELEM, schedule);
            }

            writeDayCell(
                context, writer, schedule, day, 50f, dayOfWeek, dayOfMonth,
                isWeekend, true, (dayOfWeek == Calendar.FRIDAY) ? (schedule.isSplitWeekend() ? 2 : 1) : 1
            );

            if (
                (dayOfWeek == Calendar.TUESDAY) ||
                    (dayOfWeek == Calendar.THURSDAY) ||
                    (dayOfWeek == Calendar.SATURDAY) ||
                    (dayOfWeek == Calendar.SUNDAY)
            ) {
                writer.endElement(HTML.TR_ELEM);
            }
        }

        writer.endElement(HTML.TBODY_ELEM);
        writer.endElement(HTML.TABLE_ELEM);

        writer.endElement(HTML.DIV_ELEM);
    }

    /**
     * @see org.apache.myfaces.custom.schedule.AbstractCompactScheduleRenderer#getDefaultRowHeight()
     */
    protected int getDefaultRowHeight()
    {
        return 200;
    }

    protected int getRowHeight(UIScheduleBase schedule)
    {
        if (schedule != null) {
            int height = schedule.getCompactWeekRowHeight();
            return height == 0 ? getDefaultRowHeight() : height;
        }
        return getDefaultRowHeight();
    }

}
//The End
