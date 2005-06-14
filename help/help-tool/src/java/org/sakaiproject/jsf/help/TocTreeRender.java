/**********************************************************************************
 *
 * $Header: /cvs/sakai2/help/help-tool/src/java/org/sakaiproject/jsf/help/TocTreeRender.java,v 1.2 2005/05/18 15:14:21 jlannan.iupui.edu Exp $
 *
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
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
 * @version $Id: TocTreeRender.java,v 1.2 2005/05/18 15:14:21 jlannan.iupui.edu Exp $
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