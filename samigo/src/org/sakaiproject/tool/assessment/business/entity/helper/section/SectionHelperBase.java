/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003-2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.tool.assessment.business.entity.helper.section;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.dom.ElementImpl;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import org.sakaiproject.tool.assessment.business.entity.asi.Section;
import org.sakaiproject.tool.assessment.business.entity.helper.AuthoringHelper;
import org.sakaiproject.tool.assessment.business.entity.helper.QTIHelperFactory;
import org.sakaiproject.tool.assessment.business.entity.helper.item.ItemHelperIfc;

/**
 * Sections are pretty version-independent, base class does most of the work.
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley esmiley@stanford.edu
 * some portions of code @author Shastri, Rashmi <rshastri@iupui.edu>
 * @version $Id$
 */

abstract public class SectionHelperBase
  implements SectionHelperIfc
{
  private static Log log = LogFactory.getLog(SectionHelperBase.class);

  private Document doc;

  abstract protected int getQtiVersion();

  /**
   * Read a Section XML object from a stream
   *
   * @param inputStream the stream
   *
   * @return the section
   */
  public Section readXMLDocument(
    InputStream inputStream)
  {
    if (log.isDebugEnabled())
    {
      log.debug("readDocument(InputStream " + inputStream);
    }

    Section sectionXml = null;
    try
    {
      AuthoringHelper authoringHelper = new AuthoringHelper(getQtiVersion());
      sectionXml = new Section(
        authoringHelper.readXMLDocument(inputStream).getDocument(),
        getQtiVersion());
    }
    catch (ParserConfigurationException e)
    {
      log.error(e.getMessage(), e);
    }
    catch (SAXException e)
    {
      log.error(e.getMessage(), e);
    }
    catch (IOException e)
    {
      log.error(e.getMessage(), e);
    }

    return sectionXml;
  }


  /**
   * Update the Section XML for Xpath
   *
   * @param sectionXml the Section XML
   * @param xpath the Xpath
   * @param value the value for Xpath
   *
   * @return the updated Section XML
   */
  public Section updateSectionXml(
    Section sectionXml, String xpath,
    String value)
  {
    if (log.isDebugEnabled())
    {
      log.debug(
        "updateSectionXml(Item " +
        sectionXml + ", String" + xpath + ", String" + value + ")");
    }

    try
    {
      sectionXml.update(xpath, value);
    }
    catch (DOMException e)
    {
      log.error(e.getMessage(), e);
    }
    catch (Exception e)
    {
      log.error(e.getMessage(), e);
    }

    return sectionXml;
  }

}
