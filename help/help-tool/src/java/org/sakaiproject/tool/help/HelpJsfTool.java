/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.help;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.component.cover.ServerConfigurationService;
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

  private static String HELP_DOC_REGEXP = org.sakaiproject.api.app.help.HelpManager.HELP_DOC_REGEXP;

  private static final String TOC_PATH = "/TOCDisplay/main";
  private static final String SEARCH_PATH = "/search/main";
  private static final String HELP_PATH = "/html";

  private static final String TOC_ATTRIBUTE = "tocURL";
  private static final String SEARCH_ATTRIBUTE = "searchURL";
  private static final String HELP_ATTRIBUTE = "helpURL";
  
  /** To determine if an external webapp handles help and if so, the base url */
  private static final String EXTERNAL_WEBAPP_URL_BASE = "help.redirect.external.webapp";
  private static final String EXTERNAL_WEBAPP_URL = ServerConfigurationService.getString(EXTERNAL_WEBAPP_URL_BASE, "sakai");
  /**
   * @see org.sakaiproject.jsf.util.JsfTool#dispatch(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  protected void dispatch(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException
  {
	    // if magic switch turned on, go to external webapp
	    if (! "sakai".equals(EXTERNAL_WEBAPP_URL)) {
	       String docId = req.getParameter("help");

	       if (docId != null) {
		       Pattern p = Pattern.compile(HELP_DOC_REGEXP);
		       Matcher m = p.matcher(docId);
		       
		       if (!m.matches()) {
		       	docId = "unknown";
		       }
	       }
	       String extUrl = EXTERNAL_WEBAPP_URL;
	       
	       if (docId != null && ! "".equals("docId")) {
	    	   extUrl += docId;
	       }
	    	
	       res.sendRedirect(extUrl);
		   return;
	    }

    req.setAttribute(TOC_ATTRIBUTE, Web.returnUrl(req, TOC_PATH));
    req.setAttribute(SEARCH_ATTRIBUTE, Web.returnUrl(req, SEARCH_PATH));
    req.setAttribute(HELP_ATTRIBUTE, Web.returnUrl(req, HELP_PATH));
    super.dispatch(req, res);
  }
}


