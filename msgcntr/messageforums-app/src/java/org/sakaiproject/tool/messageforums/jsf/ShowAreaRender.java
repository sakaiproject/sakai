/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-app/src/java/org/sakaiproject/tool/messageforums/jsf/ShowAreaRender.java $
 * $Id: ShowAreaRender.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

/**
 * @author Chen Wen
 * @version $Id$
 */
public class ShowAreaRender extends Renderer
{
  public boolean supportsComponentType(UIComponent component)
  {
    return (component instanceof ShowAreaComponent);
  }

  public void encodeBegin(FacesContext context, UIComponent component)
      throws IOException
  {
    ResponseWriter writer = context.getResponseWriter();

    String value = (String) component.getAttributes().get("value");
    String hideBorder = (String) component.getAttributes().get("hideBorder");
    String showInputTextArea = (String) component.getAttributes().get(
        "showInputTextArea");
    if ((value != null) && (!"".equals(value)))
    {
      int pos;
      // writer.write("<div>");
      value = value.replaceAll("<strong>", "<b>");
      value = value.replaceAll("</strong>", "</b>");
      // writer.write("<table STYLE=\"table-layout:fixed\" width=300><tr width=\"100%\"><td
      // width=\"100%\" STYLE=\"word-wrap: break-all; white-space: -moz-pre-wrap;
      // text-overflow:ellipsis; overflow: auto;\">");
      value = value.replaceAll("<a title=", "<a target=\"_new\" title=");
 //     value = value.replaceAll("<a href=",
 //         "<a title=\"Open a new window\" target=\"_new\" href=");
      if (hideBorder != null && "true".equals(hideBorder))
      {
        writer
            .write("<div class=\"textPanel\">");
//gsilver            .write("<table border=\"0\" id=\"message_table\" cellpadding=\"0\"  width=\"90%\"><tr width=\"95%\"><td width=\"100%\" STYLE=\"word-wrap: break-word\">");
			
        writer.write(value);
        writer.write("</div>");
      }
      else
        if (showInputTextArea != null && "true".equals(showInputTextArea))
        {
          writer.write("<textarea id=\"msgForum:forums:1:forum_extended_description\" name=\"msgForum:forums:1:forum_extended_description\" cols=\"100\" rows=\"5\" disabled=\"disabled\">");
          writer.write(value);           
          writer.write("</textarea>");
        }
        else
        {
          writer
            .write("<div class=\"textPanel bordered\">");
//		  .write("<table border=\"1\" id=\"message_table\" cellpadding=\"7\" style=\"border-collapse: collapse; table-layout:fixed\" width=\"90%\"><tr width=\"95%\"><td width=\"100%\" STYLE=\"word-wrap: break-word\">");
              writer.write(value);
          writer.write("</div>");
        }

    }
  }

  public void encodeEnd(FacesContext context, UIComponent component)
      throws IOException
  {
    ResponseWriter writer = context.getResponseWriter();

    String value = (String) component.getAttributes().get("value");

    if ((value != null) && (!"".equals(value)))
    {
    }
  }
}
