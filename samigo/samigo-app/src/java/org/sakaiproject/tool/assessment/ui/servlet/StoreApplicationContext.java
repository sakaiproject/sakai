/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.ui.servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.spring.SpringBeanLocator;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

@Slf4j
public class StoreApplicationContext extends HttpServlet{

  /**
	 * 
	 */
	private static final long serialVersionUID = -930231313808212406L;
  private WebApplicationContext ctx = null;

  public void init (ServletConfig config) throws ServletException {
    super.init(config);
    if (ctx == null){
      ctx = WebApplicationContextUtils
	 .getRequiredWebApplicationContext(config.getServletContext());
      SpringBeanLocator.setApplicationContext(ctx);

      ContextUtil.setServletContext(config.getServletContext());
    }
  }

}
