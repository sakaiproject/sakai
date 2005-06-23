/**********************************************************************************
* $HeadURL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2004-2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.tool.assessment.jsf.renderer;

import java.io.IOException;
import java.util.Iterator;
import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;
import javax.faces.component.UIViewRoot;
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
  private static final String BARIMG = "/images/right_arrow.gif";

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
  public void encodeChildren(FacesContext context, UIComponent component)
    throws IOException
  {
    ResponseWriter writer = context.getResponseWriter();
    Iterator children = component.getChildren().iterator();
    while (children.hasNext())
    {
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

        ResponseWriter writer = context.getResponseWriter();
        String jsfId = (String) component.getAttributes().get("id");
        String id = jsfId;

        if (component.getId() != null &&
          !component.getId().startsWith(UIViewRoot.UNIQUE_ID_PREFIX))
        {
          id = component.getClientId(context);
        }

        String title = (String) component.getAttributes().get("title");
        String contextPath = context.getExternalContext()
          .getRequestContextPath();

        writer.write("<" + BARTAG + " onclick=\"javascript:showHideDiv('" + id +
          "', '" +  contextPath + "');\" class=\"" + BARSTYLE + "\">");
        writer.write("  <img id=\"__img_hide_division_" + id + "\" alt=\"" +
           title + "\"");
        writer.write("    src=\""   + contextPath +
           BARIMG + "\" style=\"cursor:pointer;\" />");
        writer.write("  " + title + "");
        writer.write("</"+ BARTAG + ">");
        writer.write("<div \" id=\"__hide_division_" + id + "\">");
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
  public void encodeEnd(FacesContext context, UIComponent component)
    throws IOException
  {
    ResponseWriter writer = context.getResponseWriter();
    writer.write("</div>");
  }

}
