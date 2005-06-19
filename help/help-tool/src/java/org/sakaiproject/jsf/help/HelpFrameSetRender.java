/**********************************************************************************
 *
 * $Header: /cvs/sakai2/help/help-tool/src/java/org/sakaiproject/jsf/help/HelpFrameSetRender.java,v 1.3 2005/06/09 16:39:39 jlannan.iupui.edu Exp $
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

import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;
import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.service.framework.config.cover.ServerConfigurationService;

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
              
    tocToolUrl = tocToolUrl + "?help=" + helpParameter;
    
    helpWindowTitle = ServerConfigurationService.getString("ui.service");
    
    writer.write("<html><head><title>" + helpWindowTitle + "</title></head>");
    writer.write("<FRAMESET cols=\"30%, 70%\"><FRAMESET rows=\"250, 350\">");
    writer.write("<FRAME src=" + searchToolUrl + " name=\"search\"/>");
    writer.write("<FRAME src=" + tocToolUrl + " name=\"toc\"/>");
    writer.write("</FRAMESET>");
    writer
        .write("<FRAME src=\"" + helpUrl + "/help.html" + "\" name=\"content\" scrolling=\"yes\">");
    writer.write("</FRAMESET></html>");
  }
}