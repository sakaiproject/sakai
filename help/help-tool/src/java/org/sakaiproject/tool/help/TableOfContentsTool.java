/**********************************************************************************
 *
 * $Header: /cvs/sakai2/help/help-tool/src/java/org/sakaiproject/tool/help/TableOfContentsTool.java,v 1.2 2005/05/18 15:14:22 jlannan.iupui.edu Exp $
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

package org.sakaiproject.tool.help;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.help.HelpManager;
import org.sakaiproject.api.app.help.TableOfContents;

/**
 * table of contents tool
 * @version $Id$
 *
 */
public class TableOfContentsTool
{

  private TableOfContents tableOfContents;
  private HelpManager helpManager;
  private String baseUrl = null;

  private static final Log LOG = LogFactory.getLog(TableOfContentsTool.class);

  /**
   * get table of contents
   * @return Returns the tableOfContents.
   */
  public TableOfContents getTableOfContents()
  {
    return helpManager.getTableOfContents();
  }

  /**
   * set table of contents
   * @param tableOfContents The tableOfContents to set.
   */
  public void setTableOfContents(TableOfContents toc)
  {
    helpManager.setTableOfContents(toc);
  }

  /**
   * get help manager
   * @return Returns the helpManager.
   */
  public HelpManager getHelpManager()
  {
    return helpManager;
  }

  /** 
   * set help manager 
   * @param helpManager The helpManager to set.
   */
  public void setHelpManager(HelpManager helpManager)
  {
    this.helpManager = helpManager;
  }

  /** 
   * get base url
   * @return base url
   */
  private String getBaseUrl()
  {
    if (baseUrl == null)
    {
      HttpServletRequest request = (HttpServletRequest) FacesContext
          .getCurrentInstance().getExternalContext().getRequest();
      StringBuffer base = request.getRequestURL();
      String servletPath = request.getServletPath();
      String contextPath = request.getContextPath();

      int pos = base.indexOf(contextPath);
      if (pos != -1)
      {
        base.setLength(pos);
      }
      baseUrl = base.toString();
    }
    return baseUrl;
  }

}