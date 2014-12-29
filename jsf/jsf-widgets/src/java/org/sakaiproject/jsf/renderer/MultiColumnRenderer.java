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

/**
 * <p>Title: MultiColumnRenderer</p>
 * <p>Description: Renderer for MultiColumn component.</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class MultiColumnRenderer  extends DynaRendererBase
{
  public MultiColumnRenderer()
  {
  }

  /**
   * encodeEnd no-op (debug comment line)
   *
   * @param context FacesContext
   * @param component UIComponent
   */

  public void encodeBegin(FacesContext context,
                          UIComponent component) throws IOException
  {
    ResponseWriter writer = context.getResponseWriter();
    writer.write("<!-- *** DEBUG MULTICOLUMN *** -->");
  }

  /**
   * encodeEnd no-op
   *
   * @param context FacesContext
   * @param component UIComponent
   */
  public void encodeEnd(FacesContext context, UIComponent component)
  {
  }

}
