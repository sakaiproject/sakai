/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
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

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import javax.faces.render.Renderer;
import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.api.app.help.HelpManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.event.cover.EventTrackingService;

/**
 * render help frame set 
 * @version $Id$
 */
public class HelpFrameSetRender extends Renderer
{
  /**
   * supports component type
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
    ResponseWriter writer = context.getResponseWriter();
    String helpWindowTitle = (String) component.getAttributes().get(
        "helpWindowTitle");
    String searchToolUrl = (String) component.getAttributes().get(
        "searchToolUrl");
    String tocToolUrl = (String) component.getAttributes().get("tocToolUrl");        
    String helpUrl = (String) component.getAttributes().get("helpUrl");

    String helpParameter = ((HttpServletRequest) context.getExternalContext()
        .getRequest()).getParameter("help");

    String welcomepage = ServerConfigurationService.getString("help.welcomepage");  
    
    if ("".equals(welcomepage)){
        welcomepage = "html/help.html";
    }
              
    tocToolUrl = tocToolUrl + "?help=" + helpParameter;

    EventTrackingService.post(EventTrackingService.newEvent("help.access", helpParameter, false));

    helpWindowTitle = ServerConfigurationService.getString("ui.service") + " Help";
    
    writer.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
    writer.write("<html><head><title>" + helpWindowTitle + "</title></head>\n");
    writer.write("<FRAMESET cols=\"30%, 70%\"><FRAMESET rows=\"250, 350\">");
    writer.write("<FRAME src=" + searchToolUrl + " name=\"search\"/>");
    writer.write("<FRAME src=" + tocToolUrl + " name=\"toc\"/>");
    writer.write("</FRAMESET>\n");
    
    Application app = context.getApplication();
    ValueBinding binding = app.createValueBinding("#{Components['org.sakaiproject.api.app.help.HelpManager']}");
    HelpManager manager  = (HelpManager) binding.getValue(context);    
                  
    if(manager.getWelcomePage() == null) {
    	writer.write("<FRAME src=\"" + welcomepage + "\" name=\"content\"/>");
    }
    else {
      writer.write("<FRAME src=\"content.hlp?docId=" + manager.getWelcomePage() + "\" name=\"content\"/>");             
    }
                                        
    writer.write("</FRAMESET></html>\n");
  }
}
