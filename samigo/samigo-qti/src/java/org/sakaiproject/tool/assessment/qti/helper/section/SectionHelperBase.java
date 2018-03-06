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

package org.sakaiproject.tool.assessment.qti.helper.section;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import org.sakaiproject.tool.assessment.qti.asi.Section;
import org.sakaiproject.tool.assessment.qti.helper.AuthoringHelper;

/**
 * Sections are pretty version-independent, base class does most of the work.
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley esmiley@stanford.edu
 * some portions of code @author Shastri, Rashmi <rshastri@iupui.edu>
 * @version $Id$
 */
@Slf4j
abstract public class SectionHelperBase
  implements SectionHelperIfc
{

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
