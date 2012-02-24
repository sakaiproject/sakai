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

import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

/**
 * question link renderer
 * @version $Id$
 */
public class QuestionLinkRender extends Renderer
{
  /** 
   * @param component
   * @return
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
    ResponseWriter writer = context.getResponseWriter();
    String showLink = (String) component.getAttributes().get("showLink");
    String URL = (String) component.getAttributes().get("URL");
    String message = (String) component.getAttributes().get("message");
    if ("true".equals(showLink))
    {
      writer.write("<a href=\"");
      writer.write(URL);
      writer.write("\" target=\"content\">");
      writer.write(message);
      writer.write("</a>");
    }
  }
}