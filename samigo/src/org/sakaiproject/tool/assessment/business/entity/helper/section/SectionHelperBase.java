/**********************************************************************************
* $HeadURL$
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
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley esmiley@stanford.edu
 * <p>Originally SectionHelper</p>
 * @author Shastri, Rashmi <rshastri@iupui.edu>
 * @version $Id$
 */

abstract public class SectionHelperBase implements SectionHelperIfc
{
  /**
   *
   */
  private static Log log = LogFactory.getLog(SectionHelperBase.class);

  private Document doc;

  abstract protected int getQtiVersion();


  /**
   * DOCUMENTATION PENDING
   *
   * @param inputStream DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public Section readXMLDocument(
    InputStream inputStream)
  {
    if(log.isDebugEnabled())
    {
      log.debug("readDocument(InputStream " + inputStream);
    }

    Section sectionXml = null;
    try
    {
      AuthoringHelper authoringHelper = new AuthoringHelper(getQtiVersion());
      sectionXml =  new Section(
          authoringHelper.readXMLDocument(inputStream).getDocument(),
          getQtiVersion());
    }
    catch(ParserConfigurationException e)
    {
      log.error(e.getMessage(), e);
    }
    catch(SAXException e)
    {
      log.error(e.getMessage(), e);
    }
    catch(IOException e)
    {
      log.error(e.getMessage(), e);
    }

    return sectionXml;
  }







  /**
   * DOCUMENTATION PENDING
   *
   * @param sectionID DOCUMENTATION PENDING
   * @param itemRefs DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public ArrayList getSectionItems(String sectionID, boolean itemRefs)
  {
    if(log.isDebugEnabled())
    {
      log.debug(
        "getSectionItems(  String" + sectionID + ",boolean" + itemRefs + ")");
    }

    ArrayList arrList = new ArrayList();
    Section sectionXml =
      getSectionXml(sectionID);
    List list = sectionXml.selectNodes("section/itemref");

    int size = list.size();
    for(int i = 0; i < size; i++)
    {
      ElementImpl element = (ElementImpl) (list.get(i));
      String itemID = element.getAttribute("linkrefid");
      if(itemRefs)// if only references are required
      {
        if(itemID != null)
        {
          arrList.add(itemID);
        }
      }
      else // if the item needs to be exploded
      {
        QTIHelperFactory factory = new QTIHelperFactory();
        ItemHelperIfc itemHelper =
          factory.getItemHelperInstance(this.getQtiVersion());
/** @todo */
//        if(itemID != null)
//        {
//          org.navigoproject.business.entity.Item itemXml =
//            (org.navigoproject.business.entity.Item) itemHelper.getItemXml(itemID);
//          List itemList = itemXml.selectNodes("item");
//          if(itemList.size() > 0)
//          {
//            arrList.add(itemList.get(0));
//          }
//        }
      }
    }

    return arrList;
  }

  /**
   *
   * @param sec_str_id DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public Section getSectionXml(
    String sec_str_id)
  {
    if(log.isDebugEnabled())
    {
      log.debug("getSection(String" + sec_str_id + ")");
    }
    /** @todo
     *
     */

//    Section section = getSection(sec_str_id);
    Section sectionXml = null;
//    Section sectionXml = null;
//    try
//    {
//      sectionXml =
//        (Section) section.getData();
//    }
//    catch(AssessmentException e)
//    {
//      log.error(e.getMessage(), e);
//    }

    return sectionXml;
  }


  /**
   *
   * @param secXml
   * @param request
   */
  public void setSectionDocument(
    Section secXml, HttpServletRequest request)
  {
    if(log.isDebugEnabled())
    {
      log.debug(
        "setSectionDocument(Section secXml " +
        secXml + ", HttpServletRequest" + request + ")");
    }

    Document document = null;
    try
    {
      document = secXml.getDocument();
    }
    catch(ParserConfigurationException e)
    {
      log.error(e.getMessage(), e);
    }
    catch(SAXException e)
    {
      log.error(e.getMessage(), e);
    }
    catch(IOException e)
    {
      log.error(e.getMessage(), e);
    }

    doc = document;
//      this.saveDocument(request, document);
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param sectionXml DOCUMENTATION PENDING
   * @param xpath DOCUMENTATION PENDING
   * @param value DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public Section updateSectionXml(
    Section sectionXml, String xpath,
    String value)
  {
    if(log.isDebugEnabled())
    {
      log.debug(
        "updateSectionXml(Item " +
        sectionXml + ", String" + xpath + ", String" + value + ")");
    }

    try
    {
      sectionXml.update(xpath, value);
    }
    catch(DOMException e)
    {
      log.error(e.getMessage(), e);
    }
    catch(Exception e)
    {
      log.error(e.getMessage(), e);
    }

    return sectionXml;
  }

}
/**********************************************************************************
 *
 * $Header: /cvs/sakai2/sam/src/org/sakaiproject/tool/assessment/business/entity/helper/section/SectionHelperBase.java,v 1.4 2005/05/20 16:42:25 esmiley.stanford.edu Exp $
 *
 ***********************************************************************************/
