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

package org.sakaiproject.tool.assessment.qti.helper.item;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import javax.faces.context.FacesContext;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.dom.CharacterDataImpl;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.qti.asi.Item;
import org.sakaiproject.tool.assessment.qti.helper.AuthoringHelper;
import org.sakaiproject.tool.assessment.qti.helper.AuthoringXml;

public abstract class ItemHelperBase
  implements ItemHelperIfc
{
  private static Log log = LogFactory.getLog(ItemHelperBase.class);

  protected static final long ITEM_AUDIO = TypeIfc.AUDIO_RECORDING.longValue();
  protected static final long ITEM_ESSAY = TypeIfc.ESSAY_QUESTION.longValue();
  protected static final long ITEM_FILE = TypeIfc.FILE_UPLOAD.longValue();
  protected static final long ITEM_FIB = TypeIfc.FILL_IN_BLANK.longValue();
  protected static final long ITEM_MCSC = TypeIfc.MULTIPLE_CHOICE.longValue();
  protected static final long ITEM_SURVEY = TypeIfc.MULTIPLE_CHOICE_SURVEY.
    longValue();
  protected static final long ITEM_MCMC = TypeIfc.MULTIPLE_CORRECT.longValue();
  protected static final long ITEM_TF = TypeIfc.TRUE_FALSE.longValue();
  protected static final long ITEM_MATCHING = TypeIfc.MATCHING.longValue();

  /**
   * We will have a versioned AuthoringXml in subclasses.
   * @return
   */
  protected abstract AuthoringXml getAuthoringXml();

  /**
   * Get the QTI version for each subclass.
   * @return a QTIVersion.VERSION_... constant
   */
  protected abstract int getQtiVersion();

  /**
   * Read an item XML document from a stream into an Item XML object
   *
   * @param inputStream XML document stream
   *
   * @return an Item XML object
   */
  public Item readXMLDocument(
    InputStream inputStream)
  {
    if (log.isDebugEnabled())
    {
      log.debug("readDocument(InputStream " + inputStream);
    }

    Item itemXml = null;

    try
    {
      AuthoringHelper authoringHelper = new AuthoringHelper(getQtiVersion());
      itemXml =
        new Item(
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

    return itemXml;
  }

  /**
   * Get Item Xml for a given item type as a TypeIfc.
   * @param type item type as a TypeIfc
   * @return
   */

  public Item readTypeXMLItem(TypeIfc type)
  {
    AuthoringXml ax = getAuthoringXml();
    InputStream is;
    String template = getTemplateFromType(type);
    is = ax.getTemplateInputStream(template,
                                   FacesContext.getCurrentInstance());
    Item itemXml = readXMLDocument(is);
    return itemXml;
  }

  /**
   * Get Item Xml for a given survey item scale name.
   * @param scaleName
   * @return
   */
  public Item readTypeSurveyItem(String scaleName)
  {
    AuthoringXml ax = getAuthoringXml();
    InputStream is = null;
    if (scaleName==null)
    {
      log.warn("missing survey scale name, set to: STRONGLY_AGREE");
      scaleName = "STRONGLY_AGREE";
    }
    String template = getTemplateFromScale(scaleName);
    is = ax.getTemplateInputStream(template,
                                   FacesContext.getCurrentInstance());
    Item itemXml = readXMLDocument(is);
    return itemXml;

  }

  /**
   * Get XML template for a given item type
   * @param type
   * @return
   */
  private String getTemplateFromScale(String scaleName)
  {
    AuthoringXml ax = getAuthoringXml();
    String template = ax.SURVEY_10; //default

    if (scaleName.equals("YESNO"))
    {
      template = ax.SURVEY_YES;
    }
    else if (scaleName.equals("AGREE"))
    {
      template = ax.SURVEY_AGREE;
    }
    else if (scaleName.equals("UNDECIDED"))
    {
      template = ax.SURVEY_UNDECIDED;
    }
    else if (scaleName.equals("AVERAGE"))
    {
      template = ax.SURVEY_AVERAGE;
    }
    else if (scaleName.equals("STRONGLY_AGREE"))
    {
      template = ax.SURVEY_STRONGLY;
    }
    else if (scaleName.equals("EXCELLENT"))
    {
      template = ax.SURVEY_EXCELLENT;
    }
    else if (scaleName.equals("SCALEFIVE"))
    {
      template = ax.SURVEY_5;
    }
    else if (scaleName.equals("SCALETEN"))
    {
      template = ax.SURVEY_10;
    }

    log.debug("scale: " + scaleName);
    log.debug("template: " + template);

    return template;
  }

  /**
   * Get XML template for a given item type
   * @param type
   * @return
   */
  private String getTemplateFromType(TypeIfc type)
  {
    String template = "";
    AuthoringXml ax = getAuthoringXml();
    long typeId = ITEM_TF;

    if (type != null)
    {
      typeId = type.getTypeId().longValue();
    }

    if (ITEM_AUDIO == typeId)
    {
      template = ax.ITEM_AUDIO;
    }
    else if (ITEM_ESSAY == typeId)
    {
      template = ax.ITEM_ESSAY;
    }
    else if (ITEM_FILE == typeId)
    {
      template = ax.ITEM_FILE;
    }
    else if (ITEM_FIB == typeId)
    {
      template = ax.ITEM_FIB;
    }
    else if (ITEM_MCSC == typeId)
    {
      template = ax.ITEM_MCSC;
    }
    else if (ITEM_SURVEY == typeId)
    {
      template = ax.ITEM_SURVEY;
    }
    else if (ITEM_MCMC == typeId)
    {
      template = ax.ITEM_MCMC;
    }
    else if (ITEM_TF == typeId)
    {
      template = ax.ITEM_TF;
    }
    else if (ITEM_MATCHING == typeId)
    {
      template = ax.ITEM_MATCHING;
    }

    log.debug("typeId: " + typeId);
    log.debug("template: " + template);

    return template;
  }

  /**
   * Get item type string which is used for the title of a given item type
   * @param type
   * @return
   */
  public String getItemTypeString(TypeIfc type)
  {
    long typeId = 0;
    if (type != null)
    {
      typeId = type.getTypeId().longValue();
    }

    int itemType = type.getTypeId().intValue();
    if (itemType < 1 || itemType > itemTypes.length)
    {
      itemType = 0;
    }
    return itemTypes[itemType];
  }

  /**
   * Update path with value
   *
   * @param itemXml the item xml
   * @param xpath the xpath
   * @param value the value to set
   *
   * @return the item xml
   */
  public Item updateItemXml(
    Item itemXml, String xpath, String value)
  {
    if (log.isDebugEnabled())
    {
      log.debug(
        "updateItemXml(Item " + itemXml +
        ", String" + xpath + ", String" + value + ")");
    }

    try
    {
      itemXml.update(xpath, value);
    }
    catch (DOMException e)
    {
      log.error(e.getMessage(), e);
    }
    catch (Exception e)
    {
      log.error(e.getMessage(), e);
    }

    return itemXml;
  }

  /**
   * Concatenate nodes for xpath
   * @param itemXml
   * @param xpath
   * @return
   */
  protected String makeItemNodeText(Item itemXml, String xpath)
  {
    String text = "";
    List nodes = itemXml.selectNodes(xpath);
    Iterator iter = nodes.iterator();
    while (iter.hasNext())
    {
      Node node = (Node) iter.next();
      Node child = node.getFirstChild();
      if ( (child != null) && child instanceof CharacterDataImpl)
      {
        CharacterDataImpl cdi = (CharacterDataImpl) child;
        text = text + " " + cdi.getData();
      }
    }
    return text;
  }

}
