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

import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

/**
 * uri resolver
 * @version $Id$
 */
public class URIResolver implements javax.xml.transform.URIResolver
{

  /**
   * @see javax.xml.transform.URIResolver#resolve(java.lang.String, java.lang.String)
   */
  public Source resolve(String href, String base) throws TransformerException
  {
    Source source = null;
    String path = null;
    try
    {
      URI uri = new URI(base);
      path = uri.resolve(href).toString();
      source = new StreamSource(path);
    }
    catch (URISyntaxException e)
    {
    }

    return source;
  }

}