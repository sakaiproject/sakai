/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
      if (!component.isRendered()) return;

    if (!isDisabled(context, component))
    {
        // use default link rendering
      super.encodeBegin(context, component);
    }
    else
    {
        // setup to render the disabled link ourselves
      ResponseWriter writer = context.getResponseWriter();
      writer.write("<span class=\"chefToolBarDisabled\">");
    }
  }

  public void encodeChildren(FacesContext context, UIComponent component) throws IOException
  {
      if (!component.isRendered()) return;

      if (!isDisabled(context, component))
      {
          // use default rendering
          super.encodeChildren(context, component);
      }
      else
      {
          // render the text of the disabled link ourselves
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
      if (!component.isRendered()) return;

    if (!isDisabled(context, component))
    {
        // use default link rendering
      super.encodeEnd(context, component);
    }
    else
    {
        // finish rendering the disabled link ourselves
      ResponseWriter writer = context.getResponseWriter();
      writer.write("</span>");
    }
  }

  /**
   * Check if the component is disabled.
   * @param component
   * @return true if the component has a boolean "disabled" attribute set, false if not
   */
  protected boolean isDisabled(FacesContext context, UIComponent component)
  {
    boolean disabled = false;
    Object value = RendererUtil.getAttribute(context, component, "disabled");
    if (value != null)
    {
      if (value instanceof Boolean)
      {
        disabled = ((Boolean) value).booleanValue();
      }
      else
      {
        if (!(value instanceof String))
        {
          value = value.toString();
        }
        disabled = (new Boolean((String) value)).booleanValue();
      }
    }

    return disabled;
  }
}



