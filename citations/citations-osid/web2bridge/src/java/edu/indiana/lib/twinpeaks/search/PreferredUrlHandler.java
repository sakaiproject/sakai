/**********************************************************************************
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
package edu.indiana.lib.twinpeaks.search;

import edu.indiana.lib.twinpeaks.util.DomUtils;
import edu.indiana.lib.twinpeaks.util.LogUtils;
import edu.indiana.lib.twinpeaks.util.StringUtils;

import java.util.ArrayList;

import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.w3c.dom.html.*;
import org.xml.sax.*;

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
	private static org.apache.commons.logging.Log	_log = LogUtils.getLog(PreferredUrlHandler.class);
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