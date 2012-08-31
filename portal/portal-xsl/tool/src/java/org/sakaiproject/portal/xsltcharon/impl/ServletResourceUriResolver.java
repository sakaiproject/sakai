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

import org.sakaiproject.webapp.api.WebappResourceManager;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Jul 17, 2007
 * Time: 10:09:01 AM
 * To change this template use File | Settings | File Templates.
 */
public class ServletResourceUriResolver implements URIResolver {

   private WebappResourceManager context;

   public ServletResourceUriResolver(WebappResourceManager context) {
      this.context = context;
   }

   public Source resolve(String href, String base) throws TransformerException {
      return new StreamSource(context.getResourceAsStream(href));
   }
}
