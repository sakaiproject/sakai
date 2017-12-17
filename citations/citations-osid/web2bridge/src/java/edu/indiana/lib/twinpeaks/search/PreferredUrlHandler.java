/**********************************************************************************
*
 * Copyright (c) 2003, 2004, 2008 The Sakai Foundation
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
package edu.indiana.lib.twinpeaks.search;

import java.util.ArrayList;

import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.w3c.dom.html.*;
import org.xml.sax.*;

import edu.indiana.lib.twinpeaks.util.DomUtils;
import edu.indiana.lib.twinpeaks.util.StringUtils;

/**
 * Preferred URL handling - reference implementation for the Sirsi Web2 Bridge.
 *<p>
 * Examine each search result record: based on the connector in use, locate the
 * preferred URL (if any) provided.
 *<p>
 * Add your own handler logic here.
 */
public class PreferredUrlHandler
{
	/**
	 * American Social History Online
	 */
	public static final String ASHO_CONNECTOR   = "SRU_0001";

  /**
   * Connectors that can provide "preferred URLs"
   */
  public static final ArrayList _preferredUrlConnectors = new ArrayList();
/****************************************************************************
 * Uncomment this static block to define the "preferred URL" provider(s)
 *
  static
  {
    _preferredUrlConnectors.add(ASHO_CONNECTOR);
  }
 *
 ****************************************************************************/

  /**
   * Fetch the preferred URL.  This method is invoked for every result record.
   *
   * @param connector Connector name
   * @param dataElement The result record (DATA element) to inspect
   * @return The preferred URL (null if none)
   */
  public static String getUrl(String connector, Element dataElement)
  {
    /*
     * Stop now if this connector can't provide a preferred URL
     */
    if (!_preferredUrlConnectors.contains(connector))
    {
      return null;
    }
    /*
     * American Social History Online
     *
     * An example:
     *
     *    <DATA>
     *      ...
     *      <IDENTIFIER scheme="URL>http://preferred/url/here</IDENTIFIER>
     *      ...
     *    </DATA>
     *
     * We know each result record will have only one of these.
     */
    if (connector.equals(ASHO_CONNECTOR))
    {
  		NodeList nodeList = DomUtils.getElementList(dataElement, "IDENTIFIER");
      String   url      = null;

		  for (int i = 0; i < nodeList.getLength(); i++)
		  {
			  Element element = (Element) nodeList.item(i);

			  if (element.getAttribute("scheme").equals("URL"))
			  {
  			  url = DomUtils.getText(element);

  			  if (StringUtils.isNull(url))
  			  {
  			    url = null;
  			  }
          break;
  			}
  		}
  		return url;
    }
    /*
     * A known connector and we didn't handle it?
     */
    return null;
  }
}