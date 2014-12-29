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
import java.util.Iterator;
import javax.faces.component.UIColumn;
import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

import org.sakaiproject.jsf.component.MultiColumnComponent;
import org.sakaiproject.jsf.util.RendererUtil;


public abstract class DynaRendererBase
    extends Renderer {

  public DynaRendererBase() {
    super();
  }

  abstract public void encodeBegin(FacesContext context,
          UIComponent component) throws IOException;
  abstract public void encodeEnd(FacesContext context,
           UIComponent component) throws IOException;

  /**
 * <p>Faces render output method .</p>
 * <p>Encode children column and multicolumn</p>
 *
 *  @param context   <code>FacesContext</code> for the current request
 *  @param component <code>UIComponent</code> being rendered
 *
 * @throws IOException if an input/output error occurs
 */
 public void encodeChildren(FacesContext context, UIComponent component) throws
   IOException
 {
   if (!component.isRendered())
   {
     return;
   }

   renderData(context, component);
 }

  /**
 * This component renders its children
 * @return true
 */
  public boolean getRendersChildren()
  {
    return true;
  }

  /**
 * This is an UIData type component.
 * @param component
 * @return true if UIData
 */
  public boolean supportsComponentType(UIComponent component)
  {
    return (component instanceof UIData);
  }



  /**
   * Core workhouse method of the dynamic renderers.
   * @param context FacesContext
   * @param component UIComponent
   * @throws IOException
   */
  protected void renderData(FacesContext context, UIComponent component) throws
    IOException
  {
    boolean multiColumn = component instanceof MultiColumnComponent;
    ResponseWriter writer = context.getResponseWriter();

    UIData data = (UIData) component;

    int first = data.getFirst();
    int rows = data.getRows();

    for (int i = first, n = 0; n < rows; i++, n++)
    {
      data.setRowIndex(i);
      if (!data.isRowAvailable())
      {
        break;
      }
      ////////////////////////////////////
      //  TR
      ////////////////////////////////////
      writer.startElement("tr", data);

      Iterator iter = data.getChildren().iterator();
      while (iter.hasNext())
      {
        UIComponent child = (UIComponent) iter.next();

        if (child instanceof UIColumn)
        {
          writer.startElement("td", child);
          writer.write("debug UIColumn");
          RendererUtil.encodeRecursive(context, child);
          writer.endElement("td");
        }
        else if (child instanceof UIData)
        {
          writer.write("debug UIData");
          child.encodeBegin(context);
          child.encodeChildren(context);
          child.encodeEnd(context);
        }
      }
      ////////////////////////////////////
      //  /TR
      ////////////////////////////////////
      writer.endElement("tr");
      writer.write("\n");
    }
  }

}
