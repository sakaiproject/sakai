/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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
import javax.faces.component.UIColumn;
import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.component.UIOutput;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

import org.sakaiproject.jsf.util.RendererUtil;

/**
 * <p>Description: </p>
 * <p>Render a iterated data like a dataTable but without the table.</p>
 * <p> Based on example code by from the O'Reilley JSF book. </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class DataLineRenderer extends Renderer
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
      writer.startElement("span", component);
      writer.writeAttribute("id", clientId, "id");
    }

    UIData data = (UIData) component;

    int first = data.getFirst();
    int rows = data.getRows();

    // this is a special separator attribute, not supported by UIData
    String separator = (String) RendererUtil.getAttribute(context, component, "separator");
    if (separator==null) separator=" | ";

    for (int i = first, n = 0; n < rows; i++, n++)
    {
      data.setRowIndex(i);
      if (!data.isRowAvailable())
      {
        break;
      }

      // between any two iterations add separator if there is one
      if (i!=first) writer.write(separator);

      Iterator iter = data.getChildren().iterator();
      while (iter.hasNext())
      {
        UIComponent column = (UIComponent) iter.next();
        if (!(column instanceof UIColumn))
        {
          continue;
        }
        RendererUtil.encodeRecursive(context, column);
        }
      }
      if (clientId != null)
        {
        writer.endElement("span");
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
