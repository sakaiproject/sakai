/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-app/src/java/org/sakaiproject/tool/messageforums/jsf/HideDivisionRenderer.java $
 * $Id: HideDivisionRenderer.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
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
package org.sakaiproject.tool.messageforums.jsf;

import java.io.IOException;
import java.util.Iterator;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

import java.util.List;

/**
 * @author Chen Wen
 * @version $Id$
 *
 */
public class HideDivisionRenderer extends Renderer
{
  private static final String BARSTYLE = "msgMainHeadings";
  private static final String BARTAG = "div";
  private static final String RESOURCE_PATH;
  private static final String FOLD_IMG_HIDE;
  private static final String FOLD_IMG_SHOW;
  private static final String CURSOR;

  static {
  	RESOURCE_PATH = "/messageforums-tool";
    FOLD_IMG_HIDE = RESOURCE_PATH + "/images/right_arrow.gif";
    FOLD_IMG_SHOW = RESOURCE_PATH + "/images/down_arrow.gif";
    CURSOR = "cursor:pointer";
    /*ConfigurationResource cr = new ConfigurationResource();
     RESOURCE_PATH = "/" + cr.get("resources");
     BARIMG = RESOURCE_PATH + "/" +cr.get("hideDivisionRight");
     CURSOR = cr.get("picker_style");*/
  }

  public boolean supportsComponentType(UIComponent component)
  {
    return (component instanceof org.sakaiproject.tool.messageforums.jsf.HideDivisionComponent);
  }

  public void decode(FacesContext context, UIComponent component)
  {
  }

  public void encodeChildren(FacesContext context, UIComponent component)
  	throws IOException
  {
    if (!component.isRendered())
    {
      return;
    }

    Iterator children = component.getChildren().iterator();
    while (children.hasNext()) {
      UIComponent child = (UIComponent) children.next();
      if(!((child instanceof org.sakaiproject.tool.messageforums.jsf.BarLinkComponent)||
          (child instanceof HtmlOutputText)))
      {
        child.encodeBegin(context);
        child.encodeChildren(context);
        child.encodeEnd(context);
      }
    }
  }

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
    Object tmpFoldStr = RendererUtil.getAttribute(context, component, "hideByDefault");
    boolean foldDiv = tmpFoldStr != null && "true".equals(tmpFoldStr);
    String foldImage = foldDiv ? FOLD_IMG_HIDE : FOLD_IMG_SHOW;
    writer.write("<" + BARTAG + " class=\"" + BARSTYLE + "\">");
    writer.write("<table style=\"width: 100%;\" class=\"discTria\" cellpadding=\"0\" cellspacing=\"0\" >");
    writer.write("<tr><td  class=\"discTria\" onclick=\"javascript:showHideDivBlock('" + id +
        "', '" +  RESOURCE_PATH + "');\">" );
    writer.write("  <img id=\"" + id + "__img_hide_division_" + "\" alt=\"" +
        title + "\"");
    writer.write("    src=\""   + foldImage + "\" style=\"" + CURSOR + "\" />");
    writer.write("<h4>"  + title + "</h4>");
    writer.write("</td><td class=\"discTria\">&nbsp;</td>");
    writer.write("<td  class=\"itemAction\" style=\"text-align: right;\">");
    List childrenList = component.getChildren();
    for(int i=0; i<childrenList.size(); i++)
    	{
			UIComponent thisComponent = (UIComponent)childrenList.get(i);
      if(thisComponent instanceof org.sakaiproject.tool.messageforums.jsf.BarLinkComponent
         ||thisComponent instanceof HtmlOutputText)
      {
        thisComponent.encodeBegin(context);
        thisComponent.encodeChildren(context);
        thisComponent.encodeEnd(context);
      }
    }
    writer.write("</td></tr></table>");
    writer.write("</"+ BARTAG + ">");
    if(foldDiv) {
      writer.write("<div style=\"display:none\" " +
          " id=\"" + id + "__hide_division_" + "\">");
            } else {
                writer.write("<div style=\"display:block\" " +
                        " id=\"" + id + "__hide_division_" + "\">");
            }
  }


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

//    writer.write("<script type=\"text/javascript\">");
//    writer.write("  showHideDiv('" + id +
//        "', '" +  RESOURCE_PATH + "');");
//    writer.write("</script>");
  }

}
