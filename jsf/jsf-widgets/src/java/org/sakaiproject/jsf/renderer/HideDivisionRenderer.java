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
import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

import org.sakaiproject.jsf.util.RendererUtil;
import org.sakaiproject.jsf.util.ConfigurationResource;
/**
 * <p>Description: </p>
 * <p>Render a stylesheet link for the value of our component's
 * <code>path</code> attribute, prefixed by the context path of this
 * web application.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class HideDivisionRenderer extends Renderer
{
//  private static final String BARSTYLE = "navModeAction";
  private static final String BARSTYLE = "";
  private static final String BARTAG = "h4";
  private static final String RESOURCE_PATH;
  private static final String BARIMG;
  private static final String CURSOR;

  static {
    ConfigurationResource cr = new ConfigurationResource();
    RESOURCE_PATH = "/" + cr.get("resources");
    BARIMG = RESOURCE_PATH + "/" +cr.get("hideDivisionRight");
    CURSOR = cr.get("picker_style");
  }

  public boolean supportsComponentType(UIComponent component)
  {
    return (component instanceof UIOutput);
  }

  public void decode(FacesContext context, UIComponent component)
  {
  }

  /**
   * Simple passthru.
   * @param context
   * @param component
   * @throws IOException
   */
  public void encodeChildren(FacesContext context, UIComponent component) throws
      IOException {
    if (!component.isRendered()) {
      return;
    }

    ResponseWriter writer = context.getResponseWriter();
    Iterator children = component.getChildren().iterator();
    while (children.hasNext()) {
      UIComponent child = (UIComponent) children.next();
      writer.writeText(child, null);
    }

  }

  /**
   * <p>Faces render output method .</p>
   * <p>Method Generator: org.sakaiproject.tool.assessment.devtoolsRenderMaker</p>
   *
   *  @param context   <code>FacesContext</code> for the current request
   *  @param component <code>UIComponent</code> being rendered
   *
   * @throws IOException if an input/output error occurs
   */
    public void encodeBegin(FacesContext context, UIComponent component)
      throws IOException {

        if (!component.isRendered()) {
          return;
        }

        ResponseWriter writer = context.getResponseWriter();
        String jsfId = (String) RendererUtil.getAttribute(context, component, "id");
        String id = jsfId;

        if (component.getId() != null &&
          !component.getId().startsWith(UIViewRoot.UNIQUE_ID_PREFIX))
        {
          id = component.getClientId(context);
        }

        String title = (String) RendererUtil.getAttribute(context, component, "title");

        writer.write("<" + BARTAG + " onclick=\"javascript:showHideDiv('" + id +
          "', '" +  RESOURCE_PATH + "');\" class=\"" + BARSTYLE + "\">");
        writer.write("  <img id=\"" + id + "__img_hide_division_" + "\" alt=\"" +
           title + "\"");
        writer.write("    src=\""   + BARIMG + "\" style=\"" + CURSOR + "\" />");
        writer.write("  " + title + "");
        writer.write("</"+ BARTAG + ">");
        writer.write("<div \" style=\"display:none\" " +
                     " id=\"" + id + "__hide_division_" + "\">");
    }


  /**
   * <p>Render end of hidable DIV.</p>
   *
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
  public void encodeEnd(FacesContext context, UIComponent component) throws
      IOException {
    if (!component.isRendered()) {
      return;
    }

    ResponseWriter writer = context.getResponseWriter();

    String jsfId = (String) RendererUtil.getAttribute(context, component, "id");
    String id = jsfId;

    if (component.getId() != null &&
      !component.getId().startsWith(UIViewRoot.UNIQUE_ID_PREFIX))
    {
      id = component.getClientId(context);
    }

    writer.write("</div>");

    writer.write("<script type=\"text/javascript\">");
    writer.write("  showHideDiv('" + id +
          "', '" +  RESOURCE_PATH + "');");
    writer.write("</script>");
  }

}
