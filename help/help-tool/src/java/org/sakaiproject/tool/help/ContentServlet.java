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

package org.sakaiproject.tool.help;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.api.app.help.HelpManager;
import org.sakaiproject.api.app.help.Resource;
import org.sakaiproject.api.kernel.component.cover.ComponentManager;

/**
 * Content Servlet serves help documents to document frame.
 * @version $Id$
 */
public class ContentServlet extends HttpServlet
{

  private static final String DOC_ID = "docId";
  private static final String TEXT_HTML = "text/html";
  private HelpManager helpManager;

  /**
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException
  {

    getHelpManager().initialize();
    String docId = req.getParameter(DOC_ID);

    OutputStreamWriter writer = new OutputStreamWriter(res.getOutputStream(), "UTF-8");
    res.setContentType(TEXT_HTML);

    Resource resource = getHelpManager().getResourceByDocId(docId);

    URL url;
    if (resource != null)
    {
      if (!getHelpManager().getRestConfiguration().getOrganization()
          .equalsIgnoreCase("sakai"))
      {
        writer.write(RestContentProvider.getTransformedDocument(
            getServletContext(), getHelpManager(), resource));
      }
      else
        if (resource.getLocation().startsWith("/"))
        {
          if (!"".equals(getHelpManager().getExternalLocation()))
          {
            url = new URL(getHelpManager().getExternalLocation()
                + resource.getLocation());
          }
          else
          {
            url = HelpManager.class.getResource(resource.getLocation());
          }

          BufferedReader br = new BufferedReader(
                  new InputStreamReader(url.openStream(),"UTF-8"));

          int readReturn = 0;
          String sbuf = new String();
          while ((sbuf = br.readLine()) != null)
          {
            writer.write( sbuf );
            writer.write( System.getProperty("line.separator") );
          }
          br.close();
        }
        else
        {
          res.sendRedirect(resource.getLocation());
        }
    }    
    writer.flush();
    writer.close();
  }

  /**
   * get the component manager through cover
   * @return help manager
   */
  public HelpManager getHelpManager()
  {
    if (helpManager == null)
    {
      return (HelpManager) ComponentManager.get(HelpManager.class.getName());
    }
    return helpManager;
  }
}


