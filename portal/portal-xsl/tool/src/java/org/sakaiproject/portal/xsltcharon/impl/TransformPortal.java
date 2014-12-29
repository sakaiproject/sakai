/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation
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

package org.sakaiproject.portal.xsltcharon.impl;

import org.sakaiproject.portal.charon.SkinnableCharonPortal;
import org.sakaiproject.portal.api.PortalHandler;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Jul 20, 2007
 * Time: 12:21:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class TransformPortal extends SkinnableCharonPortal {

   /**
    * Initialize the servlet.
    *
    * @param config The servlet config.
    * @throws javax.servlet.ServletException
    */
   public void init(ServletConfig config) throws ServletException {
      super.init(config);
      Map<String, PortalHandler> handlers =
         org.sakaiproject.portal.api.cover.PortalService.getInstance().getHandlerMap(this);
      PortalHandler siteHandler = handlers.get("site");
      org.sakaiproject.portal.api.cover.PortalService.getInstance().addHandler(this,
         new TransformHandler(siteHandler, "blah"));
   }

   protected void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
      super.service(new HttpServletRequestWrapper(httpServletRequest, "/blah"), 
         httpServletResponse);
   }

}
