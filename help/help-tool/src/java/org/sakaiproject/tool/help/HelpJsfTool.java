/**********************************************************************************
 *
 * $Header: /cvs/sakai2/help/help-tool/src/java/org/sakaiproject/tool/help/HelpJsfTool.java,v 1.3 2005/06/09 16:39:39 jlannan.iupui.edu Exp $
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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.jsf.util.JsfTool;
import org.sakaiproject.util.web.Web;

/**
 * HelpJsfTool extends JsfTool to support placement in the help frameset. 
 * 
 * @author <a href="mailto:jlannan.iupui.edu">Jarrod Lannan</a>
 * @version $Id$
 * 
 * 
 */
public class HelpJsfTool extends JsfTool
{

  /** Our log (commons). */
  private static Log M_log = LogFactory.getLog(HelpJsfTool.class);

  private static final String TOC_PATH = "/TOCDisplay/main";
  private static final String SEARCH_PATH = "/search/main";
  private static final String HELP_PATH = "/html";

  private static final String TOC_ATTRIBUTE = "tocURL";
  private static final String SEARCH_ATTRIBUTE = "searchURL";
  private static final String HELP_ATTRIBUTE = "helpURL";

  /**
   * @see org.sakaiproject.jsf.util.JsfTool#dispatch(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  protected void dispatch(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException
  {
    req.setAttribute(TOC_ATTRIBUTE, Web.returnUrl(req, TOC_PATH));
    req.setAttribute(SEARCH_ATTRIBUTE, Web.returnUrl(req, SEARCH_PATH));
    req.setAttribute(HELP_ATTRIBUTE, Web.returnUrl(req, HELP_PATH));
    super.dispatch(req, res);
  }
}

/**********************************************************************************
 *
 * $Header: /cvs/sakai2/help/help-tool/src/java/org/sakaiproject/tool/help/HelpJsfTool.java,v 1.3 2005/06/09 16:39:39 jlannan.iupui.edu Exp $
 *
 **********************************************************************************/