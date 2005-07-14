/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
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
import java.util.TreeSet;

import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;
import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.api.app.help.Category;
import org.sakaiproject.api.app.help.Resource;
import org.sakaiproject.service.framework.config.cover.ServerConfigurationService;

/**
 * render response
 * @version $Id$
 */
public class ContextSensitiveTreeRender extends Renderer
{
  /**
   * supports componenet type
   * @param component
   * @return true if supported
   */
  public boolean supportsComponentType(UIComponent component)
  {
    return (component instanceof UIData);
  }

  /**
   * @see javax.faces.render.Renderer#encodeBegin(javax.faces.context.FacesContext, javax.faces.component.UIComponent)
   */
  public void encodeBegin(FacesContext context, UIComponent component)
      throws IOException
  {

    String skinRoot = ServerConfigurationService.getString("skin.repo",
        "/library/skin");
    String skin = ServerConfigurationService.getString("skin.default",
        "default");

    String jsLibraryUrl = "../js";
    ResponseWriter writer = context.getResponseWriter();
    writer.write("<html><head>");
    writer.write("<script type=\"text/javascript\" src=\"" + jsLibraryUrl
        + "/csTree.js\"></script>\n");
    writer.write("<link href=\"" + skinRoot + "/tool_base.css\""
        + " rel=\"stylesheet\">");
    writer.write("<link href=\"" + skinRoot + "/" + skin + "/tool.css\""
        + " rel=\"stylesheet\">");
    writer
        .write("<link href=\"../css/csTree.css\" type=\"text/css\" rel=\"stylesheet\">");
    //writer.write("<body onload='collapseAll([\"ol\"]); openBookMark();'>");
    writer.write("</head>");
    writer.write("<body>");
    writer.write("<ol id=\"root\">");
    UIData data = (UIData) component;
    Object value = data.getValue();
    //String helpDocId = (String) component.getAttributes().get("helpDocId");
    String helpDocId = ((HttpServletRequest) context.getExternalContext()
        .getRequest()).getParameter("help");

    Set categories = (Set) value;
    
    // filter to only include top-level categories
    for (Iterator i = categories.iterator(); i.hasNext();){
      Category c = (Category) i.next();
      if (c.getParent() != null){
        i.remove();
      }
    }
    
    encodeRecursive(writer, categories, helpDocId);
    writer.write("</ol>");

    if (helpDocId != null)
    {
      writer.write("<script language=\"JavaScript\" src=\"" + jsLibraryUrl
          + "/search.js\"></script>\n");
    }
    writer.write("</body></html>");
  }

  /**
   * encode recursively
   * @param writer
   * @param categories
   * @param helpDocId
   * @throws IOException
   */
  private void encodeRecursive(ResponseWriter writer, Set categories,
      String helpDocId) throws IOException
  {
    for (Iterator i = categories.iterator(); i.hasNext();)
    {
      Category category = (Category) i.next();
             
      Set resources = new TreeSet(category.getResources());
      String id = category.getName();

      writer.write("<li class=\"dir\">");
      writer.write("<img src=\"../image/toc_closed.gif\" border=\"0\" id=\"I"
          + category.getName() + "\"/>");
      writer.write("<a id=\"" + id + "\" href=\"#" + category.getName()
          + "\" onclick=\"toggle(this)\">" + category.getName() + "</a>");

      writer.write("<ol class=\"docs\">");
      Set subCategories = new TreeSet(category.getCategories());
      encodeRecursive(writer, subCategories, helpDocId);
      if (resources != null)
      {
        for (Iterator j = resources.iterator(); j.hasNext();)
        {
          writer.write("<li>");
          Resource resource = (Resource) j.next();

          // helpDocId will be a helpDocId (coming from search) or
          // will be a tool id coming from portal
          if (helpDocId != null
              && (helpDocId.equals(resource.getDefaultForTool()) || helpDocId
                  .equals(resource.getDocId())))
          {
            writer.write("<img src=\"../image/topic.gif\" border=\"0\"/>");
            writer.write("<a id=\"default\"" + " href=\"content.hlp?docId="
                + resource.getDocId() + "\" target = \"content\">"
                + resource.getName() + "</a>");
            writer.write("</li>");
          }
          else
          {
            writer.write("<img src=\"../image/topic.gif\" border=\"0\"/>");
            writer.write("<a id=\"" + resource.getDocId()
                + "\" href=\"content.hlp?docId=" + resource.getDocId()
                + "\" target = \"content\">" + resource.getName() + "</a>");
            writer.write("</li>");
          }
        }
      }
      writer.write("</ol></li>");
    }
  }

  /**
   * contains help doc
   * @param helpDocId
   * @param resources
   * @return true if contained
   */
  private boolean containsHelpDoc(String helpDocId, Set resources)
  {
    boolean contains = false;
    if (resources != null)
    {
      for (Iterator i = resources.iterator(); i.hasNext();)
      {
        Resource resource = (Resource) i.next();
        if (resource.getDocId().equals(helpDocId))
        {
          contains = true;
          break;
        }
      }
    }
    return contains;
  }
}