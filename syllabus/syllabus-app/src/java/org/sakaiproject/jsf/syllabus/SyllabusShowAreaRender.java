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

public class SyllabusShowAreaRender extends Renderer
{
  public boolean supportsComponentType(UIComponent component)
  {
    return (component instanceof org.sakaiproject.jsf.syllabus.SyllabusShowAreaComponent);
  }
  
  public void encodeBegin(FacesContext context, UIComponent component)
  throws IOException
  {
    ResponseWriter writer = context.getResponseWriter();
    
    String value = (String) component.getAttributes().get("value");
    
    if((value!=null) && (!value.equals("")))
    {
      int pos;
//      while ((pos = value.indexOf('?')) != -1){
//         value = value.substring(0,pos) + " " + value.substring(pos+1);
//      }
//      writer.write("<div>");
      value = value.replaceAll("<strong>", "<b>");
      value = value.replaceAll("</strong>", "</b>");
//      writer.write("<table width=\"100%\"><tbody><tr><td>");
/*      int blocks = value.length() % 200 ;
      for (int i=0; i<blocks; i++)
      {
        writer.write(value.substring(i*200, ((i+1)*200-1)));
        writer.flush();
      }
      writer.write(value.substring(blocks*200, (value.length()-1)));
      writer.flush();
*/
      value = value.replaceAll("<a title=", "<a target=\"_new\" title=");
      value = value.replaceAll("<a href=", "<a title=\"Open a new window\" target=\"_new\" href=");
      writer.write(value);
      
//      writer.write("</td></tr></tbody></table>");
//      writer.write("</div>");
    }
  }
  
  public void encodeEnd(FacesContext context, UIComponent component)
  throws IOException
  {
    ResponseWriter writer = context.getResponseWriter();
    
    String value = (String) component.getAttributes().get("value");

    if((value!=null) && (!value.equals("")))
    {
    }  
  }
}



