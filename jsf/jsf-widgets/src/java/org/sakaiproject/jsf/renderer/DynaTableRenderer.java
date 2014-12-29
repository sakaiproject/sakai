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

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.sakaiproject.jsf.util.RendererUtil;

/**
 * <p>Description: </p>
 * <p>Render data like a dataTable but support the MultiColumn component.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class DynaTableRenderer extends DynaRendererBase {

  private static RendererUtil util;

  /**
   * <p>Faces render output method .</p>
   * <p>Encode beginning of table.</p>
   *
   *  @param context   <code>FacesContext</code> for the current request
   *  @param component <code>UIComponent</code> being rendered
   *
   * @throws IOException if an input/output error occurs
   */
  public void encodeBegin(FacesContext context,
                          UIComponent component) throws IOException
  {
    if (!component.isRendered())
    {
      return;
    }

    ResponseWriter writer = context.getResponseWriter();
    writer.startElement("table", component);
    writer.writeAttribute("id", component.getClientId(context), "id");
//    util.writePassthroughs(context, component);
  }


  /**
   * <p>Faces render output method .</p>
   * <p>Encode end of table.</p>
   *
   *  @param context   <code>FacesContext</code> for the current request
   *  @param component <code>UIComponent</code> being rendered
   *
   * @throws IOException if an input/output error occurs
   */
  public void encodeEnd(FacesContext context, UIComponent component) throws
    IOException
  {
    if (!component.isRendered())
    {
      return;
    }
    ResponseWriter writer = context.getResponseWriter();
    writer.endElement("table");
  }



}
