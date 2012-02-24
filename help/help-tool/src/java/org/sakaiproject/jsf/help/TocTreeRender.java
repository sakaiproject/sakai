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

package org.sakaiproject.jsf.help;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

import org.sakaiproject.api.app.help.Category;
import org.sakaiproject.api.app.help.Resource;

/**
 * toc tree renderer
 * @version $Id$
 */
public class TocTreeRender extends Renderer
{

  /** 
   * @see javax.faces.render.Renderer#encodeBegin(javax.faces.context.FacesContext, javax.faces.component.UIComponent)
   */
  public void encodeBegin(FacesContext context, UIComponent component)
      throws IOException
  {
    String jsLibraryUrl = "../js";
    ResponseWriter writer = context.getResponseWriter();
    writer.write("<script type=\"text/javascript\">var _editor_url = \""
        + jsLibraryUrl + "/\";</script>\n");
    writer.write("<script type=\"text/javascript\" src=\"" + jsLibraryUrl
        + "/divTree.js\"></script>\n");
    writer
        .write("<link href=\"../css/divTree.css\" type=\"text/css\" rel=\"stylesheet\">");

    UIData data = (UIData) component;
    Object value = data.getValue();
    Set categories = (Set) value;
    encodeRecursive(writer, categories);
  }

  /**
   * encode recursively
   * @param writer
   * @param categories
   * @throws IOException
   */
  private void encodeRecursive(ResponseWriter writer, Set categories)
      throws IOException
  {
    for (Iterator i = categories.iterator(); i.hasNext();)
    {
      Category category = (Category) i.next();
      writer.write("<a class=\"trigger\" href=\"javascript:showBranch('"
          + category.getName() + "');\">");
      writer
          .write("<img border=\"0\" alt=\"expand/collapse\" src=\"../image/toc_closed.gif\"");
      writer.write(" id=\"I" + category.getName() + "\">");
      writer.write(category.getName() + "</a>");
      writer.write("<div class=\"branch\" id=\"" + category.getName() + "\">");
      Set resources = category.getResources();
      Set subCategories = category.getCategories();
      encodeRecursive(writer, subCategories);
      if (resources != null)
      {
        for (Iterator j = categories.iterator(); j.hasNext();)
        {
          Resource resource = (Resource) j.next();
          writer.write("<div>");
          writer.write("<img src=\"../image/topic.gif\">");
          writer.write("<a href=\"content.hlp?docId=" + resource.getDocId()
              + "\" target = \"content\">" + resource.getName() + "</a></div>");
        }
      }
      writer.write("</div>");
    }
  }
}