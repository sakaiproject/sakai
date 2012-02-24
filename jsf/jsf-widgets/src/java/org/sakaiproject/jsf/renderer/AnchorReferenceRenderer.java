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
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

import org.sakaiproject.jsf.util.RendererUtil;

/**
 * <p>Description: </p>
 * <p>Render an anchor component with
 * <code>name</code> attribute.</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class AnchorReferenceRenderer extends Renderer
{

  public boolean supportsComponentType(UIComponent component)
  {
    return (component instanceof UIOutput);
  }


  /**
   * <p>Render a an anchor tag with name attribute.</p>
   * @param context   FacesContext for the request we are processing
   * @param component UIComponent to be rendered
   *
     * @throws IOException          if an input/output error occurs while rendering
   * @throws NullPointerException if <code>context</code>
   *                              or <code>component</code> is null
   */
  /**
   * <p>Faces render output method to output script tag.</p>
     * <p>Method Generator: org.sakaiproject.tool.assessment.devtoolsRenderMaker</p>
   *
   *  @param context   <code>FacesContext</code> for the current request
   *  @param component <code>UIComponent</code> being rendered
   *
   * @throws IOException if an input/output error occurs
   */
  public void encodeBegin(FacesContext context, UIComponent component)
    throws IOException
  {

    if (!component.isRendered())
    {
      return;
    }

    String name = (String) RendererUtil.getAttribute(context, component, "name");

    ResponseWriter writer = context.getResponseWriter();
    String contextPath = context.getExternalContext()
      .getRequestContextPath();
    writer.write("<a name=\"" + name +  "\"/>");
  }

}
