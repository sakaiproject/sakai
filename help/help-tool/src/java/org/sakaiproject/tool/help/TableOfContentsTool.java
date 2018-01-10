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

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

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
