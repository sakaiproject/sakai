/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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
import java.util.Iterator;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

import org.sakaiproject.jsf.util.RendererUtil;

public class SeparatedListRenderer extends Renderer
{

  /**
   * This component renders its children
   * @return true
   */
  public boolean getRendersChildren()
  {
    return true;
  }

  /**
   * This is an output type component.
   * @param component
   * @return true if UIOutput
   */
  public boolean supportsComponentType(UIComponent component)
  {
    return (component instanceof UIOutput);
  }

  /**
   * no-op
   * @param context
   * @param component
   * @throws IOException
   */
  public void encodeBegin(FacesContext context,
    UIComponent component) throws IOException
  {
    ;
  }

  /**
   * We put all our processing in the encodeChildren method
   * @param context
   * @param component
   * @throws IOException
   */
  public void encodeChildren(FacesContext context, UIComponent component)
    throws IOException
  {
    if (!component.isRendered())
    {
      return;
    }

    String clientId = null;

    if (component.getId() != null &&
      !component.getId().startsWith(UIViewRoot.UNIQUE_ID_PREFIX))
    {
      clientId = component.getClientId(context);
    }

    ResponseWriter writer = context.getResponseWriter();

    if (clientId != null)
    {
       String styleClass = (String) RendererUtil.getAttribute(context, component, "styleClass");
      writer.startElement("div", component);
      writer.writeAttribute("id", clientId, "id");
      writer.writeAttribute("class", styleClass, "class");
    }

    List children = component.getChildren();

    // this is a special separator attribute, not supported by UIData
    String separator = (String) RendererUtil.getAttribute(context, component, "separator");
    if (separator==null) separator=" | ";

    boolean first = true;
    for (Iterator iter = children.iterator(); iter.hasNext();)
    {
      UIComponent child = (UIComponent)iter.next();
       
      if (child.isRendered()) {
         if (!first) writer.write(separator);
   
         RendererUtil.encodeRecursive(context, child);
         first = false;
      }
    } 
      if (clientId != null)
        {
        writer.endElement("div");
      }

    }

    /**
     * no-op
     * @param context
     * @param component
     * @throws IOException
     */
  public void encodeEnd(FacesContext context, UIComponent component)
    throws IOException
  {
    ;
  }

}
