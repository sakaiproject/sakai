/**********************************************************************************
 *
 * $Header: /cvs/sakai2/help/help-tool/src/java/org/sakaiproject/tool/help/URIResolver.java,v 1.1 2005/05/15 23:03:53 jlannan.iupui.edu Exp $
 *
 ***********************************************************************************
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

package org.sakaiproject.tool.help;

import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

/**
 * uri resolver
 * @version $Id: URIResolver.java,v 1.1 2005/05/15 23:03:53 jlannan.iupui.edu Exp $
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