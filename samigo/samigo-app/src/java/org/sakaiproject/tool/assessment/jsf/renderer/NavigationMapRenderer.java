/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008, 2009 The Sakai Foundation
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




package org.sakaiproject.tool.assessment.jsf.renderer;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;
import javax.faces.el.ValueBinding;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;

/**
 * <p>Description: </p>
 * <p>Render a stylesheet link for the value of our component's
 * <code>path</code> attribute, prefixed by the context path of this
 * web application.</p>
 * <p>  Based on example code by Sun Microsystems. </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class NavigationMapRenderer extends Renderer
{

  public boolean supportsComponentType(UIComponent component)
  {
    return (component instanceof UIOutput);
  }

  public void decode(FacesContext context, UIComponent component)
  {
  }

  public void encodeBegin(FacesContext context, UIComponent component)
    throws IOException
  {
    ;
  }

  public void encodeChildren(FacesContext context, UIComponent component)
    throws IOException
  {
    ;
  }

  /**
   * <p>Render a relative HTML <code>&lt;link&gt;</code> element for a
   * <code>text/css</code> stylesheet at the specified context-relative
   * path.</p>
   *
   * @param context   FacesContext for the request we are processing
   * @param component UIComponent to be rendered
   *
     * @throws IOException          if an input/output error occurs while rendering
   * @throws NullPointerException if <code>context</code>
   *                              or <code>component</code> is null
   */
  public void encodeEnd(FacesContext context, UIComponent component)
    throws IOException
  {

    ResponseWriter writer = context.getResponseWriter();
    Map map = (Map) get(context, component, "map");

    if (map == null) return;

    String separator = (String) get(context, component, "separator");
    String style = (String) get(context, component, "style");
    String linkStyle = (String) get(context, component, "linkStyle");
    if (separator == null) separator = "";

    String contextPath = context.getExternalContext().getRequestContextPath();

    if (style!=null && style.length()!=0)
    {
      writer.write("<span class=\"" + style+ "\">");
    }

    String sep = ""; 

    for (Iterator it = map.entrySet().iterator(); it.hasNext();) {
    	writer.write(sep);
    	Map.Entry entry = (Map.Entry) it.next();
    	String key = (String)entry.getKey();
    	String value = (String)entry.getValue();
    	writeLink(writer, component, key, value, contextPath, linkStyle);
    	sep = separator;
    }

    if (style!=null && style.length()!=0)
    {
      writer.write("</span>");
    }

  }

  /**
   * Write one link
   * @param writer the writer for output
   * @param component the component
   * @param text the text of the link
   * @param link the context relative url, or script
   * @param path the context path
   * @throws IOException
   */
  private void writeLink(ResponseWriter writer, UIComponent component,
    String text, String link, String path, String styleClass) throws IOException
  {
    writer.write("&nbsp;");
    writer.startElement("a", component);
    // if javascript create onclick handler, else regular link
    if (link.toLowerCase().startsWith("javascript"))
    {
      writer.writeAttribute("href", "#", null);
      writer.writeAttribute("onclick", link, null);
    }
    else
    {
      writer.writeAttribute("href", path  + "/" + link, null);
    }

    if (styleClass != null)
    {
      writer.writeAttribute("class", styleClass, "styleClass");
    }
    writer.writeText(text, null);
    writer.endElement("a");
    writer.write("&nbsp;");
  }

  /**
   *
   * @param context FacesContext
   * @param component UIComponent
   * @param name String
   * @return Object
   */
  private static Object get(FacesContext context, UIComponent component,
    String name)
  {
    ValueBinding binding = component.getValueBinding(name);
    if (binding != null)
    {
      return binding.getValue(context);
    }
    else
    {
      return component.getAttributes().get(name);
    }
  }

}
