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

package org.sakaiproject.tool.help;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.jsf.util.JsfTool;
import org.sakaiproject.util.Web;

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


