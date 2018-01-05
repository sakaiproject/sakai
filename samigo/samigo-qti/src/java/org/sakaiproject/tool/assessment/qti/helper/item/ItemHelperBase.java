/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.qti.helper.item;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.CharacterData;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.qti.asi.Item;
import org.sakaiproject.tool.assessment.qti.helper.AuthoringHelper;
import org.sakaiproject.tool.assessment.qti.helper.AuthoringXml;

@Slf4j
public abstract class ItemHelperBase
  implements ItemHelperIfc
{

  protected static final long ITEM_AUDIO = TypeIfc.AUDIO_RECORDING.longValue();
  protected static final long ITEM_ESSAY = TypeIfc.ESSAY_QUESTION.longValue();
  protected static final long ITEM_FILE = TypeIfc.FILE_UPLOAD.longValue();
  protected static final long ITEM_FIB = TypeIfc.FILL_IN_BLANK.longValue();
  protected static final long ITEM_FIN = TypeIfc.FILL_IN_NUMERIC.longValue();
  protected static final long ITEM_MCSC = TypeIfc.MULTIPLE_CHOICE.longValue();
  protected static final long ITEM_SURVEY = TypeIfc.MULTIPLE_CHOICE_SURVEY.
    longValue();
  protected static final long ITEM_MCMC = TypeIfc.MULTIPLE_CORRECT.longValue();
  protected static final long ITEM_MCMC_SS = TypeIfc.MULTIPLE_CORRECT_SINGLE_SELECTION.longValue();
  protected static final long ITEM_TF = TypeIfc.TRUE_FALSE.longValue();
  protected static final long ITEM_MATCHING = TypeIfc.MATCHING.longValue();
  protected static final long ITEM_MXSURVEY = TypeIfc.MATRIX_CHOICES_SURVEY.longValue();
  protected static final long ITEM_CALCQ = TypeIfc.CALCULATED_QUESTION.longValue(); // CALCULATED_QUESTION
  protected static final long ITEM_IMAGMQ = TypeIfc.IMAGEMAP_QUESTION.longValue(); // IMAGEMAP_QUESTION
  protected static final long ITEM_EMI = TypeIfc.EXTENDED_MATCHING_ITEMS.longValue();

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

  public Item readTypeXMLItem(Long type)
  {
    AuthoringXml ax = getAuthoringXml();
    InputStream is;
    String template = getTemplateFromType(type);
    is = ax.getTemplateInputStream(template);
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
    is = ax.getTemplateInputStream(template);
    Item itemXml = readXMLDocument(is);
    return itemXml;

  }

  /**
   * Get XML template for a given item type
   * @param type
   * @return
   */
  private String getTemplateFromScale(String scalename)
  {
    String template = AuthoringXml.SURVEY_10; //default

    // 2/19/2006: for backward compatibility,need to keep YESNO, SCALEFIVE, and SCALETEN
    if ((ItemMetaDataIfc.SURVEY_YES.equals(scalename)) || (ItemMetaDataIfc.SURVEY_YESNO.equals(scalename)) ) 
    {
      template = AuthoringXml.SURVEY_YES;
    }
    else if (ItemMetaDataIfc.SURVEY_AGREE.equals(scalename)) 
    {
      template = AuthoringXml.SURVEY_AGREE;
    }
    else if (ItemMetaDataIfc.SURVEY_UNDECIDED.equals(scalename)) 
    {
      template = AuthoringXml.SURVEY_UNDECIDED;
    }
    else if (ItemMetaDataIfc.SURVEY_AVERAGE.equals(scalename)) 
    {
      template = AuthoringXml.SURVEY_AVERAGE;
    }
    else if (ItemMetaDataIfc.SURVEY_STRONGLY_AGREE.equals(scalename)) 
    {
      template = AuthoringXml.SURVEY_STRONGLY;
    }
    else if (ItemMetaDataIfc.SURVEY_EXCELLENT.equals(scalename)) 
    {
      template = AuthoringXml.SURVEY_EXCELLENT;
    }
    else if ((ItemMetaDataIfc.SURVEY_5.equals(scalename)) || (ItemMetaDataIfc.SURVEY_SCALEFIVE.equals(scalename)) ) 
    {
      template = AuthoringXml.SURVEY_5;
    }
    else if ((ItemMetaDataIfc.SURVEY_10.equals(scalename)) || (ItemMetaDataIfc.SURVEY_SCALETEN.equals(scalename)) ) 
    {
      template = AuthoringXml.SURVEY_10;
    }

    log.debug("scale: " + scalename);
    log.debug("template: " + template);

    return template;
  }

  /**
   * Get XML template for a given item type
   * @param type
   * @return
   */
  private String getTemplateFromType(Long type)
  {
    String template = "";
    long typeId = ITEM_TF;

    if (type != null)
    {
      typeId = type.longValue();
    }

    if (ITEM_AUDIO == typeId)
    {
      template = AuthoringXml.ITEM_AUDIO;
    }
    else if (ITEM_ESSAY == typeId)
    {
      template = AuthoringXml.ITEM_ESSAY;
    }
    else if (ITEM_FILE == typeId)
    {
      template = AuthoringXml.ITEM_FILE;
    }
    else if (ITEM_FIB == typeId)
    {
      template = AuthoringXml.ITEM_FIB;
    }
    else if (ITEM_FIN == typeId)
    {
      template = AuthoringXml.ITEM_FIN;
    }
    else if (ITEM_MCSC == typeId)
    {
      template = AuthoringXml.ITEM_MCSC;
    }
    else if (ITEM_SURVEY == typeId)
    {
      template = AuthoringXml.ITEM_SURVEY;
    }
    else if (ITEM_MCMC == typeId)
    {
      template = AuthoringXml.ITEM_MCMC;
    }
    else if (ITEM_MCMC_SS == typeId)
    {
      template = AuthoringXml.ITEM_MCMC_SS;
    }
    else if (ITEM_TF == typeId)
    {
      template = AuthoringXml.ITEM_TF;
    }
    else if (ITEM_MATCHING == typeId)
    {
      template = AuthoringXml.ITEM_MATCHING;
    }
    else if (ITEM_EMI == typeId)
    {
      template = AuthoringXml.ITEM_EMI;
    }
    else if (ITEM_MXSURVEY == typeId)
    {
      template = AuthoringXml.ITEM_MXSURVEY;
    }
    // CALCULATED_QUESTION
    else if (ITEM_CALCQ == typeId)
    {
      template = AuthoringXml.ITEM_CALCQ;
    }
    // IMAGEMAP_QUESTION
    else if (ITEM_IMAGMQ == typeId)
    {
      template = AuthoringXml.ITEM_IMAGMQ; //For future use
    }

    log.debug("typeId: " + typeId);
    log.debug("template: " + template);

    return template;
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
    //String text = "";
    List nodes = itemXml.selectNodes(xpath);
    Iterator iter = nodes.iterator();
    
    StringBuilder textbuf = new StringBuilder();   
    while (iter.hasNext())
    {
      Node node = (Node) iter.next();
      Node child = node.getFirstChild();
      if ( (child != null) && child instanceof CharacterData)
      {
        CharacterData cdi = (CharacterData) child;
        textbuf.append(" " + cdi.getData());
      }
    }
    String text = textbuf.toString();
    return text;
  }

}
