/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*
**********************************************************************************/

package org.sakaiproject.jsf.renderer;

import java.io.IOException;

import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.sakaiproject.jsf.util.JSFDepends;
import org.sakaiproject.jsf.util.RendererUtil;

public class ToolBarItemRenderer extends JSFDepends.CommandLinkRenderer
{
    public boolean supportsComponentType(UIComponent component)
    {
        return (component instanceof org.sakaiproject.jsf.component.ToolBarItemComponent);
    }

    public void encodeBegin(FacesContext context, UIComponent component) throws IOException
    {
        if (!component.isRendered())
        {
            return;
        }

        if (!isDisabled(context, component) && !isCurrent( context, component ))
        {
            // use default link rendering, after closing open span tag
            ResponseWriter writer = context.getResponseWriter();
            writer.write(""); // normaly just close the span
            super.encodeBegin(context, component);
        }
        else
        {
            // setup to render the disabled/current link ourselves - close open span tag after adding inactive attributes
            ResponseWriter writer = context.getResponseWriter();
            writer.write(""); //normally, add aria and class attributes and close the span
        }
    }

    public void encodeChildren(FacesContext context, UIComponent component) throws IOException
    {
        if (!component.isRendered())
        {
            return;
        }

        if (!isDisabled(context, component) && !isCurrent( context, component ))
        {
            // use default rendering
            super.encodeChildren(context, component);
        }
        else
        {
            // render the text of the disabled/current link ourselves
            String label = "";
            Object value = ((UICommand) component).getValue();
            if (value != null)
            {
                label = value.toString();
            }

            ResponseWriter writer = context.getResponseWriter();
            writer.write(label);
        }
    }

    public void encodeEnd(FacesContext context, UIComponent component) throws IOException
    {
        if (!component.isRendered())
        {
            return;
        }

        if (!isDisabled(context, component) && !isCurrent( context, component ))
        {
            // use default link rendering
            super.encodeEnd(context, component);
        }
        else
        {
            // rendering of end of disabled/current link taken care of already
        }
    }

    /**
     * Check if the component is the currently active tool bar item
     * @param context
     * @param component
     * @return true if the component is the active tool bar item, false if not
     */
    protected boolean isCurrent( FacesContext context, UIComponent component )
    {
        return getBooleanAttribute( context, component, "current" );
    }

    /**
     * Check if the component is disabled.
     * @param context
     * @param component
     * @return true if the component has a boolean "disabled" attribute set, false if not
     */
    protected boolean isDisabled(FacesContext context, UIComponent component)
    {
        return getBooleanAttribute( context, component, "disabled" );
    }

    /**
     * Utility method to retrieve an arbitrary Boolean attribute from the component's attribute map
     * @param context
     * @param component
     * @param attributeName
     * @return The value of the boolean attribute requested
     */
    private boolean getBooleanAttribute( FacesContext context, UIComponent component, String attributeName )
    {
        boolean retVal = false;
        Object value = RendererUtil.getAttribute( context, component, attributeName );
        if( value != null )
        {
            if( value instanceof Boolean )
            {
                retVal = (Boolean) value;
            }
            else
            {
                if( !(value instanceof String) )
                {
                    value = value.toString();
                }

                retVal = Boolean.valueOf( (String) value );
            }
        }

        return retVal;
    }
}
