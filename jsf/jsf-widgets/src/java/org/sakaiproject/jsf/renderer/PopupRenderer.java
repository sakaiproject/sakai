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
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

import org.sakaiproject.jsf.util.RendererUtil;

/**
 * <p>Description:<br />
 * This class is the class that renders the <code>popup</code>
 * custom tag.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class PopupRenderer
    extends Renderer {

  public boolean supportsComponentType(UIComponent component) {
    return (component instanceof UIOutput);
  }

  public void decode(FacesContext context, UIComponent component) {
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
   * Do nothing if a button, otherwise, default.
   * @param context FacesContext
   * @param component UIComponent
   * @throws IOException
   */
  public void encodeChildren(FacesContext context, UIComponent component) throws
      IOException {
    String buttonSwitch = (String) RendererUtil.getAttribute(context, component, "useButton");
    boolean useButton = Boolean.getBoolean(
        RendererUtil.makeSwitchString(buttonSwitch, false, true, true, false, false, false));

    if (useButton)
    {
      return;
    }
    else
    {
      Iterator iter = component.getChildren().iterator();
      while (iter.hasNext()) {
        UIComponent kid = (UIComponent) iter.next();
        RendererUtil.encodeRecursive(context, kid);
      }

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
  public void encodeBegin(FacesContext context, UIComponent component) throws
      IOException {

    if ( !component.isRendered())
    {
      return;
    }

    ResponseWriter writer = context.getResponseWriter();
    String id = (String) component.getClientId(context);
    String title = (String) RendererUtil.getAttribute(context, component, "title");
    String url = (String) RendererUtil.getAttribute(context, component, "url");
    String target = (String) RendererUtil.getAttribute(context, component, "target");
    String toolbar = (String) RendererUtil.getAttribute(context, component, "toolbar");
    String menubar = (String) RendererUtil.getAttribute(context, component, "menubar");
    String personalbar = (String) RendererUtil.getAttribute(context, component, "personalbar");
// temporary workaround ClassCastException
    String width = null;//(String) RendererUtil.getAttribute(context, component, "width");
    String height = null; //(String) RendererUtil.getAttribute(context, component, "height");
    String scrollbars = (String) RendererUtil.getAttribute(context, component, "scrollbars");
    String resizable = (String) RendererUtil.getAttribute(context, component, "resizable");

    if (title == null) {
      title = "     ";
    }
    if (target == null) {
      target = "sakai_popup"; /** todo: put in resource*/
    }
    if (width == null) {
      width = "650";
    }
    if (height == null) {
      height = "375";
    }
    toolbar = RendererUtil.makeSwitchString(toolbar, false, true, true, true, false, false);
    menubar = RendererUtil.makeSwitchString(menubar, false, true, true, true, false, false);
    personalbar = RendererUtil.makeSwitchString(personalbar, false, true, true, true, false, false);
    scrollbars = RendererUtil.makeSwitchString(scrollbars, false, true, true, true, false, false);
    resizable = RendererUtil.makeSwitchString(resizable, false, true, true, true, false, false);

    String buttonSwitch = (String) RendererUtil.getAttribute(context, component, "useButton");
    boolean useButton = Boolean.getBoolean(
        RendererUtil.makeSwitchString(buttonSwitch, false, true, true, false, false, false));

    if (useButton) {
      writer.write("<!-- DEBUG: useButton=true -->");
      writer.write("<input");
      writer.write("  id=\"" + id + "\"");
      writer.write("  type=\"button\"");
      writer.write("  title=\"" + title + "\"");
      writer.write("  value=\"" + title + "\"");
      writer.write("  onclick=\"window.open('" + url + "','" + target +
                   "', 'toolbar=" + toolbar + ",menubar=" + menubar +
                   ",personalbar=" +
                   personalbar + ",width=" + width + ",height=" + height +
                   ",scrollbars=" + scrollbars + ",resizable=" + resizable +
                   "');\" />");
    }
    else {

      writer.write("<!-- DEBUG: useButton=false -->");
      writer.write("<a");
      writer.write("  id=\"" + id + "\"");
      writer.write("  title=\"" + title + "\"");
      writer.write("  href=\"#\"");
      writer.write("  onclick=\"window.open('" + url + "','" + target +
                   "', 'toolbar=" + toolbar + ",menubar=" + menubar +
                   ",personalbar=" +
                   personalbar + ",width=" + width + ",height=" + height +
                   ",scrollbars=" + scrollbars + ",resizable=" + resizable +
                   "');\" >");
      writer.write(title);
    }

  }

  /**
   * <p>Render end of ANCHOR.</p>
   *
   * @param context   FacesContext for the request we are processing
   * @param component UIComponent to be rendered
   *
   *  @param context   <code>FacesContext</code> for the current request
   *  @param component <code>UIComponent</code> being rendered
   *
   * @throws IOException if an input/output error occurs
   */
  public void encodeEnd(FacesContext context, UIComponent component) throws
      IOException {
    if (!component.isRendered())
    {
      return;
    }

    ResponseWriter writer = context.getResponseWriter();
    String buttonSwitch = (String) RendererUtil.getAttribute(context, component, "useButton");
    boolean useButton = Boolean.getBoolean(
        RendererUtil.makeSwitchString(buttonSwitch, false, true, true, false, false, false));

    if (!useButton) {
      writer.write("</a>");
    }
  }

}
