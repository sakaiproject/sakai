/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
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
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;
import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.api.app.help.Category;
import org.sakaiproject.api.app.help.Resource;
import org.sakaiproject.component.cover.ServerConfigurationService;

/**
 * render response
 * @version $Id$
 */
public class ContextSensitiveTreeRender extends Renderer
{
  private static String HELP_DOC_REGEXP = org.sakaiproject.api.app.help.HelpManager.HELP_DOC_REGEXP;
  
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
    writer.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
    writer.write("<html>");
    writer.write("<head>\n");            
    writer.write("<title>Help Index</title>\n");            
    writer.write("<script type=\"text/javascript\" src=\"" + jsLibraryUrl
        + "/csTree.js\"></script>\n");
    writer.write("<link href=\"" + skinRoot + "/tool_base.css\""
        + " type=\"text/css\" rel=\"stylesheet\" />\n");
    writer.write("<link href=\"" + skinRoot + "/" + skin + "/tool.css\""
        + " type=\"text/css\" rel=\"stylesheet\" />\n");
    writer.write("<link href=\"../css/csTree.css\" type=\"text/css\" rel=\"stylesheet\" />");
    //writer.write("<body onload='collapseAll([\"ol\"]); openBookMark();'>");
    writer.write("</head>\n");

    writer.write("<body>\n");
    writer.write("<ol id=\"root\">");
    UIData data = (UIData) component;
    Object value = data.getValue();

    // Get and validate the help id requested
    String helpDocId = ((HttpServletRequest) context.getExternalContext()
        .getRequest()).getParameter("help");

    if (helpDocId != null) {
	    Pattern p = Pattern.compile(HELP_DOC_REGEXP);
	    Matcher m = p.matcher(helpDocId);
	    
	    if (!m.matches()) {
	    	helpDocId = "unknown";
	    }
    }
    Set categories = (Set) value;
    
    // filter to only include top-level categories
    for (Iterator i = categories.iterator(); i.hasNext();){
      Category c = (Category) i.next();      
      if (c.getParent() != null || "home".equalsIgnoreCase(c.getName())){
        i.remove();
      }
    }
    
    encodeRecursive(writer, categories, helpDocId);
    writer.write("</ol>");

    if (helpDocId != null)
    {
      writer.write("<script type=\"text/javascript\" src=\"" + jsLibraryUrl
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
      writer.write("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><tr><td>");
      writer.write("<img src=\"../image/toc_closed.gif\" alt=\"closed\" /></td>");      
      writer.write("<td><a id=\"" + id + "\" href=\"#" + category.getName()
          + "\" onclick=\"toggle(this)\">" + category.getName() + "</a></td>");
      writer.write("</tr></table>");

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
            writer.write("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><tr><td>");
            writer.write("<img src=\"../image/topic.gif\" alt=\"topic\"/></td>");            
            writer.write("<td><a id=\"default\"" + " href=\"content.hlp?docId="
                + resource.getDocId() + "\" target = \"content\">"
                + resource.getName() + "</a></td>");
            writer.write("</tr></table></li>\n");            
          }
          else
          {
            writer.write("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><tr><td>");
            writer.write("<img src=\"../image/topic.gif\" alt=\"topic\"/></td>");            
            writer.write("<td><a id=\"" + resource.getDocId()
                + "\" href=\"content.hlp?docId=" + resource.getDocId()
                + "\" target = \"content\">" + resource.getName() + "</a></td>");            
            writer.write("</tr></table></li>\n");
          }
        }
      }
      writer.write("</ol></li>\n");
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
