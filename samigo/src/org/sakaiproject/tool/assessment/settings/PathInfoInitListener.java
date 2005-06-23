/**********************************************************************************
* $HeaderURL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.tool.assessment.settings;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Initializes PathInfo at startup.
 * @author <a href="mailto:lance@indiana.edu">Lance Speelmon</a>
 * @version $Id$
 */
public class PathInfoInitListener
  implements ServletContextListener
{
  private static final org.apache.log4j.Logger LOG =
    org.apache.log4j.Logger.getLogger(PathInfoInitListener.class);

  /**
   * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
   */
  public void contextInitialized(ServletContextEvent sce)
  {
    LOG.debug(
      "PathInfoInitListener.contextInitialized(ServletContextEvent " + sce +
      ")");
    String pathToSecurity = null;
    String pathToSettings = null;
    try
    {
      PathInfo pathInfo = PathInfo.getInstance();
      pathToSecurity =
        sce.getServletContext().getInitParameter("PathToSecurity");
      LOG.debug("PathToSecurity=" + pathToSecurity);
      if((pathToSecurity == null) || (pathToSecurity.length() < 1))
      {
        throw new IllegalStateException("PathToSecurity is invalid!");
      }

      pathToSettings =
        sce.getServletContext().getInitParameter("PathToSettings");
      LOG.debug("PathToSettings=" + pathToSettings);
      if((pathToSettings == null) || (pathToSettings.length() < 1))
      {
        throw new IllegalStateException("PathToSettings is invalid!");
      }

      if((pathToSecurity != null) && (pathToSettings != null))
      {
        pathInfo.setBasePathToSecurity(pathToSecurity);
        pathInfo.setBasePathToSettings(pathToSettings);
      }

      LOG.debug("PathInfoInitListener initialized successfully!");
    }
    catch(Exception ex)
    {
      LOG.error(ex);
      throw new Error(ex);
    }
  }

  /**
   * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
   */
  public void contextDestroyed(ServletContextEvent sce)
  {
    ;
  }
}
