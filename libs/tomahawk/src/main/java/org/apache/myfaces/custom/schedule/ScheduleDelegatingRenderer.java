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

import jakarta.faces.application.ResourceDependency;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.event.ComponentSystemEventListener;
import jakarta.faces.event.ListenerFor;
import jakarta.faces.render.Renderer;

import org.apache.myfaces.custom.schedule.model.ScheduleModel;
import org.apache.myfaces.tomahawk.application.PreRenderViewAddResourceEvent;

/**
 * <p>
 * Renderer for the Schedule component that delegates the actual rendering
 * to a compact or detailed renderer, depending on the mode of the ScheduleModel
 * </p>
 * 
 * @JSFRenderer
 *   renderKitId = "HTML_BASIC" 
 *   family = "jakarta.faces.Panel"
 *   type = "org.apache.myfaces.Schedule"
 * @since 1.1.7
 * @author Jurgen Lust (latest modification by $Author: skitching $)
 * @author Bruno Aranda (adaptation of Jurgen's code to myfaces)
 * @version $Revision: 367444 $
 */
@ResourceDependency(library="oam.custom.schedule.javascript",name="schedule.js")
@ListenerFor(systemEventClass=PreRenderViewAddResourceEvent.class)
public class ScheduleDelegatingRenderer extends Renderer implements Serializable, ComponentSystemEventListener
{
    private static final long serialVersionUID = -837566590780480244L;
    
    //~ Instance fields --------------------------------------------------------

    private final ScheduleCompactMonthRenderer monthDelegate = new ScheduleCompactMonthRenderer();
    private final ScheduleCompactWeekRenderer weekDelegate = new ScheduleCompactWeekRenderer();
    private final ScheduleDetailedDayRenderer dayDelegate = new ScheduleDetailedDayRenderer();

    //~ Methods ----------------------------------------------------------------


    public void processEvent(ComponentSystemEvent event)
    {
        Renderer renderer = getDelegateRenderer(event.getComponent());
        if (renderer instanceof ComponentSystemEventListener)
        {
            ((ComponentSystemEventListener)renderer).processEvent(event);
        }
    }
    
    /**
     * @see jakarta.faces.render.Renderer#decode(jakarta.faces.context.FacesContext,
     *      jakarta.faces.component.UIComponent)
     */
    public void decode(FacesContext context, UIComponent component)
    {
        getDelegateRenderer(component).decode(context, component);
    }

    /**
     * @see jakarta.faces.render.Renderer#encodeBegin(jakarta.faces.context.FacesContext,
     *      jakarta.faces.component.UIComponent)
     */
    public void encodeBegin(FacesContext context, UIComponent component)
            throws IOException
    {
        getDelegateRenderer(component).encodeBegin(context, component);
    }

    /**
     * @see jakarta.faces.render.Renderer#encodeChildren(jakarta.faces.context.FacesContext,
     *      jakarta.faces.component.UIComponent)
     */
    public void encodeChildren(FacesContext context, UIComponent component)
            throws IOException
    {
        getDelegateRenderer(component).encodeChildren(context, component);
    }

    /**
     * @see jakarta.faces.render.Renderer#encodeEnd(jakarta.faces.context.FacesContext,
     *      jakarta.faces.component.UIComponent)
     */
    public void encodeEnd(FacesContext context, UIComponent component)
            throws IOException
    {
        getDelegateRenderer(component).encodeEnd(context, component);
    }

    protected Renderer getDelegateRenderer(UIComponent component)
    {
        HtmlSchedule schedule = (HtmlSchedule) component;

        if ((schedule == null) || (schedule.getModel() == null))
        {
            return dayDelegate;
        }

        switch (schedule.getModel().getMode())
        {
        case ScheduleModel.WEEK:
            return weekDelegate;

        case ScheduleModel.MONTH:
            return monthDelegate;

        default:
            return dayDelegate;
        }
    }

    /**
     * @see jakarta.faces.render.Renderer#getRendersChildren()
     */
    public boolean getRendersChildren()
    {
        return true;
    }
}
//The End
