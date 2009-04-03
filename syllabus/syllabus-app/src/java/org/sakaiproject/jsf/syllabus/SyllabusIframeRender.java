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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.jsf.syllabus;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

public class SyllabusIframeRender extends Renderer
{
  public boolean supportsComponentType(UIComponent component)
  {
    return (component instanceof org.sakaiproject.jsf.syllabus.SyllabusIframeComponent);
  }
  
  public void encodeBegin(FacesContext context, UIComponent component)
  throws IOException
  {
    ResponseWriter writer = context.getResponseWriter();
    
    String widthIn = (String) component.getAttributes().get("width");
    String heightIn = (String) component.getAttributes().get("height");
    String redirectUrl = (String) component.getAttributes().get("redirectUrl");
    
    if (widthIn == null) 
    {
      widthIn = new Integer(450).toString();
    }
    
    if (heightIn == null) 
    {
      heightIn = new Integer(80).toString();
    }
    
    if((redirectUrl != null) && (!redirectUrl.equals("")))
    {
/*      if(!redirectUrl.startsWith("http://"))
      {
        if((!redirectUrl.toLowerCase().endsWith(".doc")) && (!redirectUrl.toLowerCase().endsWith(".pdf"))
            && (!redirectUrl.toLowerCase().startsWith("https://")))
        {
          redirectUrl = "http://" + redirectUrl;
        }
      }*/
      if(redirectUrl.toLowerCase().startsWith("www."))
      {
        redirectUrl = "http://" + redirectUrl;
      }
      if(!redirectUrl.toLowerCase().startsWith("http://") && 
      		!redirectUrl.toLowerCase().startsWith("https://"))
      {
      	redirectUrl = "http://" + redirectUrl;
      }
      writer.write("<iframe src=\"" + redirectUrl + "\"");
      writer.write(" width=\"" + widthIn + "\"");
      writer.write(" height=\"" + heightIn + "\"");
      writer.write(" style=\"margin-top:1em;clear:both\"");
      writer.write(" frameborder=\"0\"");
      writer.write(" scrolling=\"auto\"");      
      writer.write("></iframe>");
    }
  }
}



