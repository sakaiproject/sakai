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


import org.sakaiproject.util.Xml;


import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

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
    return Xml.resolveSecureSource(href, base);
  }
}
