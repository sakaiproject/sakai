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
import java.util.Date;
import java.util.Map;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.el.ValueBinding;

import org.apache.myfaces.custom.schedule.HtmlSchedule;
import org.apache.myfaces.custom.schedule.model.ScheduleDay;
import org.apache.myfaces.custom.schedule.model.ScheduleEntry;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HTML;

/**
 * The default implementation of the ScheduleEntryRenderer
 * 
 * @since 1.1.7
 * @author Jurgen Lust (latest modification by $Author$)
 * @version $Revision$
 */
public class DefaultScheduleEntryRenderer implements ScheduleEntryRenderer,
        Serializable
{
    private static final long serialVersionUID = 4987926168243581739L;

    /**
     * @see org.apache.myfaces.custom.schedule.ScheduleEntryRenderer#renderContent(jakarta.faces.context.FacesContext, jakarta.faces.context.ResponseWriter, org.apache.myfaces.custom.schedule.HtmlSchedule, org.apache.myfaces.custom.schedule.model.ScheduleDay, org.apache.myfaces.custom.schedule.model.ScheduleEntry, boolean, boolean)
     */
    public void renderContent(FacesContext context, ResponseWriter writer,
                              HtmlSchedule schedule, ScheduleDay day, ScheduleEntry entry,
                              boolean compact, boolean selected) throws IOException
    {
        if (compact)
        {
            renderCompactContent(context, writer, schedule, day, entry, selected);
        } else
        {
            if (selected)
            {
                StringBuffer entryStyle = new StringBuffer();
                entryStyle.append("height: 100%; width: 100%;");
                //the left border of a selected entry should have the same
                //color as the entry border
                String entryColor = getColor(context, schedule, entry, selected);
                if (entryColor != null) {
                    entryStyle.append("border-color: ");
                    entryStyle.append(entryColor);
                    entryStyle.append(";");
                }
                // draw the contents of the selected entry
                writer.startElement(HTML.DIV_ELEM, null);
                writer.writeAttribute(HTML.CLASS_ATTR, getStyleClass(schedule,
                                                                     "text"), null);
                writer.writeAttribute(HTML.STYLE_ATTR,entryStyle.toString(), null);
                
                renderDetailedContentText(context, writer, schedule, day, entry, selected);
                
                writer.endElement(HTML.DIV_ELEM);
            } else
            {
                renderDetailedContentText(context, writer, schedule, day, entry, selected);
            }
        }

    }

    protected void renderCompactContent(FacesContext context, ResponseWriter writer, HtmlSchedule schedule, ScheduleDay day, ScheduleEntry entry, boolean selected) throws IOException
    {
        StringBuffer text = new StringBuffer();
        Date startTime = entry.getStartTime();

        if (day.getDayStart().after(entry.getStartTime()))
        {
            startTime = day.getDayStart();
        }

        Date endTime = entry.getEndTime();

        if (day.getDayEnd().before(entry.getEndTime()))
        {
            endTime = day.getDayEnd();
        }

        if (!entry.isAllDay())
        {
            DateFormat format = AbstractScheduleRenderer.getDateFormat(context, schedule, 
                    HtmlSchedule.HOUR_NOTATION_24.equals(schedule.getHourNotation()) ? "HH:mm" : "h:mma");
            
            text.append(format.format(startTime));
            if (!startTime.equals(endTime)) {
                text.append("-");
                text.append(format.format(endTime));
            }
            text.append(": ");
        }
        text.append(entry.getTitle());

        writer.writeText(text.toString(), null);        
    }
    
    protected void renderDetailedContentText(FacesContext context, ResponseWriter writer,
            HtmlSchedule schedule, ScheduleDay day, ScheduleEntry entry, boolean selected) throws IOException
    {
        // write the title of the entry
        if (entry.getTitle() != null)
        {
            writer.startElement(HTML.SPAN_ELEM, schedule);
            writer.writeAttribute(HTML.CLASS_ATTR, getStyleClass(
                    schedule, "title"), null);
            writer.writeText(entry.getTitle(), null);
            writer.endElement(HTML.SPAN_ELEM);
        }
        if (entry.getSubtitle() != null)
        {
            writer.startElement("br", schedule);
            writer.endElement("br");
            writer.startElement(HTML.SPAN_ELEM, schedule);
            writer.writeAttribute(HTML.CLASS_ATTR, getStyleClass(
                    schedule, "subtitle"), null);
            writer.writeText(entry.getSubtitle(), null);
            writer.endElement(HTML.SPAN_ELEM);
        }        
    }
    
    /**
     * @see org.apache.myfaces.custom.schedule.ScheduleEntryRenderer#getColor(jakarta.faces.context.FacesContext, org.apache.myfaces.custom.schedule.HtmlSchedule, org.apache.myfaces.custom.schedule.model.ScheduleEntry, boolean)
     */
    public String getColor(FacesContext context, HtmlSchedule schedule,
                           ScheduleEntry entry, boolean selected)
    {
        return null;
    }

    /**
     * @see org.apache.myfaces.custom.schedule.ScheduleEntryRenderer#renderToolTip(jakarta.faces.context.FacesContext, jakarta.faces.context.ResponseWriter, org.apache.myfaces.custom.schedule.HtmlSchedule, org.apache.myfaces.custom.schedule.model.ScheduleEntry, boolean)
     */
    public void renderToolTip(FacesContext context, ResponseWriter writer,
                              HtmlSchedule schedule, ScheduleEntry entry, boolean selected)
            throws IOException
    {
        StringBuffer buffer = new StringBuffer();
        buffer
                .append("return makeTrue(domTT_activate(this, event, 'caption', '");

        if (entry.getTitle() != null)
        {
            buffer.append(escape(entry.getTitle()));
        }

        buffer.append("', 'content', '<i>");

        if (entry.getSubtitle() != null)
        {
            buffer.append(escape(entry.getSubtitle()));
        }

        buffer.append("</i>");

        if (entry.getDescription() != null)
        {
            buffer.append("<br/>");
            buffer.append(escape(entry.getDescription()));
        }

        buffer.append("', 'trail', true));");
        writer.writeAttribute("onmouseover", buffer.toString(), null);
    }

    private String escape(String text)
    {
        if (text == null)
        {
            return null;
        }

        return text.replaceAll("'", "\\\\\'").replaceAll("\n", "\\\\n").replaceAll("\r", "\\\\r");
    }

    /**
     * @see org.apache.myfaces.custom.schedule.ScheduleEntryRenderer#getEntryClass(org.apache.myfaces.custom.schedule.HtmlSchedule, org.apache.myfaces.custom.schedule.model.ScheduleEntry)
     */
    public String getEntryClass(HtmlSchedule schedule, ScheduleEntry entry)
    {
        return getStyleClass(schedule, "entry");
    }
    
    /**
     * <p>
     * Allow the developer to specify custom CSS classnames for the schedule
     * component.
     * </p>
     * 
     * @param component
     *            the component
     * @param className
     *            the default CSS classname
     * @return the custom classname
     */
    protected String getStyleClass(UIComponent component, String className)
    {
        // first check if the styleClass property is a value binding expression
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
        // it's not a value binding expression, so check for the string value
        // in the attributes
        Map attributes = component.getAttributes();
        String returnValue = (String) attributes.get(className + "Class");
        return returnValue == null ? className : returnValue;
    }

}
