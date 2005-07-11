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

package org.sakaiproject.tool.assessment.business.entity.asi;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.sakaiproject.tool.assessment.business.entity.constants.AuthoringConstantStrings;
import org.sakaiproject.tool.assessment.business.entity.constants.QTIConstantStrings;
import org.sakaiproject.tool.assessment.business.entity.constants.QTIVersion;
import org.sakaiproject.tool.assessment.business.entity.helper.QTIHelperFactory;
import org.sakaiproject.tool.assessment.business.entity.helper.item.ItemHelperIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;

/**
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author rshastri
 * @author Ed Smiley esmiley@stanford.edu
 * @version $Id$
 */
public class Item extends ASIBaseClass
{
  private static Log log = LogFactory.getLog(Item.class);
  private int qtiVersion;
  private ItemHelperIfc helper;


  /**
   * Explicitly setting serialVersionUID insures future versions can be
     * successfully restored. It is essential this variable name not be changed
     * to SERIALVERSIONUID, as the default serialization methods expects this
   * exact name.
   */
  private static final long serialVersionUID = 1;
  private String basePath;
  private String identity;

  /**
   * Creates a new Item object.
   */
  public Item(int qtiVersion)
  {
    super();
    initVersion(qtiVersion);
  }

  /**
   * Creates a new Item object.
   *
   * @param document an item XML document
   */
  public Item(Document document, int qtiVersion)
  {
    super(document);
    initVersion(qtiVersion);
  }

  private void initVersion(int qtiVersion)
  {
    if (!QTIVersion.isValid(qtiVersion))
    {
      throw new IllegalArgumentException("Invalid Item QTI version.");
    }
    this.qtiVersion = qtiVersion;
    switch (qtiVersion)
    {
      case QTIVersion.VERSION_1_2:
        basePath = QTIConstantStrings.ITEM; // for v 1.2
        identity = QTIConstantStrings.IDENT;
      case QTIVersion.VERSION_2_0:
        basePath = QTIConstantStrings.ASSESSMENTITEM;// for v 2.0
        identity = QTIConstantStrings.AITEM_IDENT;
      default:
        basePath = QTIConstantStrings.ITEM; // DEFAULT
        identity = QTIConstantStrings.IDENT;
    }

    QTIHelperFactory factory = new QTIHelperFactory();
    helper = factory.getItemHelperInstance(qtiVersion);
    System.out.println("Item XML class.initVersion(int qtiVersion)");
    System.out.println("qtiVersion="+qtiVersion);
    System.out.println("basePath="+basePath);
    System.out.println("identity="+identity);
  }

  /**
   * set identity attribute (ident/identioty)
   * @param ident the value
   */

  public void setIdent(String ident)
  {
    String xpath = basePath;
    List list = this.selectNodes(xpath);
    if (list.size() > 0)
    {
      Element element = (Element) list.get(0);
      element.setAttribute(identity, ident);
    }
  }

  /**
   * set title attribute
   * @param ident the value
   */
  public void setTitle(String title)
  {
    String xpath = basePath;
    List list = this.selectNodes(xpath);
    if (list.size() > 0)
    {
      Element element = (Element) list.get(0);
      element.setAttribute(QTIConstantStrings.TITLE, escapeXml(title));
    }
  }

  /**
   * Update XML from perisistence
   * @param item
   */
  public void update(ItemDataIfc item)
  {
    // metadata
    setFieldentry("ITEM_OBJECTIVE",
      item.getItemMetaDataByLabel("ITEM_OBJECTIVE"));
    setFieldentry("ITEM_KEYWORD",
      item.getItemMetaDataByLabel("ITEM_KEYWORD"));
    setFieldentry("ITEM_RUBRIC", item.getItemMetaDataByLabel("ITEM_RUBRIC"));
    // item data
//    ItemHelper helper = new ItemHelper();
    if (!this.isSurvey()) //surveys are unscored
    {
      helper.addMaxScore(item.getScore(), this);
      helper.addMinScore(item.getScore(), this);
    }

    String instruction = item.getInstruction();
    if (this.isMatching() || this.isFIB())
    {
      if ( instruction != null)
        {
          helper.setItemText(instruction, this);
        }
    }
    ArrayList itemTexts = item.getItemTextArraySorted();

    setItemTexts(itemTexts);
    if (this.isTrueFalse()) // we know what the answers are (T/F)
    {
      Boolean isTrue = item.getIsTrue();
      if (isTrue == null)
        isTrue = Boolean.FALSE;
      setAnswerTrueFalse(isTrue.booleanValue());
    }
    else
    if (!this.isSurvey()) //answers for surveys are a stereotyped scale
    {
      setAnswers(itemTexts);
    }
    setFeedback(itemTexts);
  }

  /**
   * Set the answer texts for item.
   * @param itemTextList the text(s) for item
   */
  public void setAnswerTrueFalse(boolean isTrue)
  {
    System.out.println("isTrue="+isTrue);
    if (isTrue)
    {
      helper.addCorrectAnswer("A", this);
      helper.addIncorrectAnswer("B", this);
    }
    else
    {
      helper.addCorrectAnswer("B", this);
      helper.addIncorrectAnswer("A", this);

    }
  }


  /**
   * method for meta data
   *
   * @param fieldlabel to get
   *
   * @return the value
   */
  public String getFieldentry(String fieldlabel)
  {
    if (log.isDebugEnabled())
    {
      log.debug("getFieldentry(String " + fieldlabel + ")");
    }
    String xpath = helper.getMetaLabelXPath(fieldlabel);
    return super.getFieldentry(xpath);
  }

  /**
   * method for meta data
   *
   * @param fieldlabel to get
   *
   * @param setValue the value
   */
  public void setFieldentry(String fieldlabel, String setValue)
  {
    if (log.isDebugEnabled())
    {
      log.debug(
        "setFieldentry(String " + fieldlabel + ", String " + setValue +
        ")");
    }

    String xpath = helper.getMetaLabelXPath(fieldlabel);
    super.setFieldentry(xpath, setValue);
  }

  /**
   * Create a metadata field entry
   *
   * @param fieldlabel the field label
   */
  public void createFieldentry(String fieldlabel)
  {
    if (log.isDebugEnabled())
    {
      log.debug("createFieldentry(String " + fieldlabel + ")");
    }

    String xpath = helper.getMetaXPath();
    super.createFieldentry(xpath, fieldlabel);
  }


  public String getItemType()
  {
    String type = this.getFieldentry("qmd_itemtype");

    return type;
  }

  /**
   * Set the item texts.
   * Valid for single and multiple texts.
   * @param itemText text to be updated
   */
  public void setItemTexts(ArrayList itemTextList)
  {
    helper.setItemTexts(itemTextList, this);
  }

  public boolean  isEssay()
  {
    return AuthoringConstantStrings.ESSAY.equals(this.getItemType()) ? true : false;
  }

  public boolean  isSurvey()
  {
    return AuthoringConstantStrings.SURVEY.equals(this.getItemType()) ? true : false;
  }

  public boolean  isAudio()
  {
    return AuthoringConstantStrings.AUDIO.equals(this.getItemType()) ? true : false;
  }

  public boolean  isFile()
  {
    return AuthoringConstantStrings.FILE.equals(this.getItemType()) ? true : false;
  }

  public boolean  isMatching()
  {
    return AuthoringConstantStrings.MATCHING.equals(this.getItemType()) ? true : false;
  }

  public boolean  isFIB()
  {
    return AuthoringConstantStrings.FIB.equals(this.getItemType()) ? true : false;
  }

  public boolean  isMCMC()
  {
    return AuthoringConstantStrings.MCMC.equals(this.getItemType()) ? true : false;
  }

  public boolean  isMCSC()
  {
    return AuthoringConstantStrings.MCSC.equals(this.getItemType()) ? true : false;
  }

  private boolean isTrueFalse()
  {
    return AuthoringConstantStrings.TF.equals(this.getItemType()) ? true : false;
  }



  /**
   * Set the answer texts for item.
   * @param itemTextList the text(s) for item
   */
  public void setAnswers(ArrayList itemTextList)
  {
    helper.setAnswers(itemTextList, this);
  }

  /**
   * Set the feedback texts for item.
   * @param itemTextList the text(s) for item
   */
  public void setFeedback(ArrayList itemTextList)
  {
    helper.setFeedback(itemTextList, this);
  }


  /**
   * Get the text for the item
   * @return the text
   */
  public String getItemText()
  {
    return helper.getText(this);
  }


  public String getBasePath()
  {
    return basePath;
  }

  public void setBasePath(String basePath)
  {
    this.basePath = basePath;
  }
}
/**********************************************************************************
 *
 * $Header: /cvs/sakai2/sam/src/org/sakaiproject/tool/assessment/business/entity/asi/Item.java,v 1.35 2005/05/21 03:40:34 esmiley.stanford.edu Exp $
 *
 ***********************************************************************************/
