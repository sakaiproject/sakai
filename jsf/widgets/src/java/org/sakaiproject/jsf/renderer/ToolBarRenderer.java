/**********************************************************************************
*
* Header:
*
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


package org.sakaiproject.jsf.renderer;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

import org.sakaiproject.jsf.util.RendererUtil;

/**
 * <p>This does not render children, but can deal with children by surrounding them in a comment.</p>
 *
 */
public class ToolBarRenderer extends Renderer
{
  /**
   * This component renders its children
   * @return true
   */
  public boolean getRendersChildren()
  {
	  return true;
  }
  
  public boolean supportsComponentType(UIComponent component)
  {
    return (component instanceof org.sakaiproject.jsf.component.ToolBarComponent);
  }

  public void encodeBegin(FacesContext context, UIComponent component) throws IOException
  {
	if(!component.isRendered()){
		//tool_bar tag is not to be rendered, return now
		return;
	}
    ResponseWriter writer = context.getResponseWriter();
    writer.write("<ul class=\"navIntraTool actionToolbar\" role=\"menu\">");

    return;
  }
  
  /**
   * We put all our processing in the encodeChildren method
   * @param context
   * @param component
   * @throws IOException
   */
  public void encodeChildren(FacesContext context, UIComponent component)
    throws IOException
  {
    if (!component.isRendered())
    {
      return;
    }

    String clientId = null;

    if (component.getId() != null &&
      !component.getId().startsWith(UIViewRoot.UNIQUE_ID_PREFIX))
    {
      clientId = component.getClientId(context);
    }

    ResponseWriter writer = context.getResponseWriter();

    if (clientId != null)
    {
      writer.startElement("ul", component);
    }

    List children = component.getChildren();

    // this is a special separator attribute, not supported by UIData
    String separator = (String) RendererUtil.getAttribute(context, component, "separator");
    if (separator==null) separator="";

    boolean first = true;
    for (Iterator iter = children.iterator(); iter.hasNext();)
    {
      UIComponent child = (UIComponent)iter.next();
      // should instead leave the span open, and the item should then add class and aria attributes
      // depending on the item is (disabled or not) and then close
      if (child.isRendered()) {
         if (!first)   
         {
        	 writer.write("<li role=\"menuitem\"><span>");
         }
         else
         {
        	 writer.write("<li role=\"menuitem\" class=\"firstToolBarItem\"><span>");
         }
         RendererUtil.encodeRecursive(context, child);
    	 writer.write("</span></li> ");
         first = false;
      }
    } 
      if (clientId != null)
        {
        writer.endElement("ul");
      }

  }

  public void encodeEnd(FacesContext context, UIComponent component) throws IOException
  {
    ResponseWriter writer = context.getResponseWriter();
    writer.write("</ul>");
  }
}



