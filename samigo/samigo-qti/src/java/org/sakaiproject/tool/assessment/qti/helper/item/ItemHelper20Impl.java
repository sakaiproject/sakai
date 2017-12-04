/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008, 2009 The Sakai Foundation
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.qti.asi.Item;
import org.sakaiproject.tool.assessment.qti.constants.AuthoringConstantStrings;
import org.sakaiproject.tool.assessment.qti.constants.QTIConstantStrings;
import org.sakaiproject.tool.assessment.qti.constants.QTIVersion;
import org.sakaiproject.tool.assessment.qti.helper.AuthoringXml;

/**
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * <p>Version for QTI 2.0 item XML, significant differences between 1.2 and 2.0</p>
 * @author Ed Smiley esmiley@stanford.edu
 * @version $Id$
 */
@Slf4j
public class ItemHelper20Impl extends ItemHelperBase
  implements ItemHelperIfc
{
  private AuthoringXml authoringXml;

  public ItemHelper20Impl()
  {
    super();
    authoringXml = new AuthoringXml(getQtiVersion());
    log.debug("ItemHelper20Impl");
  }

  protected AuthoringXml getAuthoringXml()
  {
    return authoringXml;
  }

  /**
   * Add maximum score to item XML.
   * @param score
   * @param itemXml
   */
  public void addMaxScore(Double score, Item itemXml)
  {
    // normalize if null
    if (score == null)
    {
      score =  Double.valueOf(0);
    }
    // set the responseElse baseValue, if it exists
    String xPath =
      "assessmentItem/responseCondition/responseIf/" +
      "setOutcomeValue/baseValue";
    // test if this is a type that has this baseValue
    List list = itemXml.selectNodes(xPath);
    if (list == null || list.size() == 0)
    {
      return;
    }
    updateItemXml(itemXml, xPath, score.toString());
  }

  /**
   * Add minimum score to item XML
   * @param score
   * @param itemXml
   */
  public void addMinScore(Double score, Item itemXml)
  {
    // normalize if null
    if (score == null)
    {
      score =  Double.valueOf(0);
    }
    // first, set the outcomeDeclaration defaultValue, if it exists
    String xPath =
      "assessmentItem/responseDeclaration/outcomeDeclaration/defaultValue";
    // test if this is a type that has a defaultValue
    List list = itemXml.selectNodes(xPath);
    if (list == null || list.size() == 0)
    {
      return;
    }
    updateItemXml(itemXml, xPath, score.toString());
    // next, set the responseElse baseValue, if it exists
    xPath =
      "assessmentItem/responseCondition/responseElse/" +
      "setOutcomeValue/baseValue";
    // test if this is a type that has this baseValue
    list = itemXml.selectNodes(xPath);
    if (list == null || list.size() == 0)
    {
      return;
    }
    updateItemXml(itemXml, xPath, score.toString());
  }

  /**
   * Flags an answer as correct.
   * @param correctAnswerLabel
   */
  public void addCorrectAnswer(String correctAnswerLabel, Item itemXml)
  {
    String xPath = "assessmentItem/responseDeclaration/correctResponse/value";
    updateItemXml(itemXml, xPath, correctAnswerLabel);
  }

  /**
   * assessmentItem/qtiMetadata not be permissible in QTI 2.0
   * this this should be used by manifest
   * Get the metadata field entry XPath
   * @return the XPath
   */
  public String getMetaXPath()
  {
    String xpath = "assessmentItem/qtiMetadata";
    return xpath;
  }

  /**
   * assessmentItem/qtiMetadata not be permissible in QTI 2.0
   * this this should be used by manifest
   * Get the metadata field entry XPath for a given label
   * @param fieldlabel
   * @return the XPath
   */
  public String getMetaLabelXPath(String fieldlabel)
  {
    String xpath =
      "assessmentItem/qtiMetadata/qtimetadatafield/fieldlabel[text()='" +
      fieldlabel + "']/following-sibling::fieldentry";
    return xpath;
  }

  /**
   * Get the text for the item
   * @param itemXml
   * @return the text
   */
  public String getText(Item itemXml)
  {
    String xpath = "assessmentItem/itemBody";
    String itemType = itemXml.getItemType();
    if (itemType.equals(AuthoringConstantStrings.MATCHING))
    {
      xpath =
        "assessmentItem/itemBody/matchInteraction/simpleMatchSet/simpleAssociableChoice";
    }

    return makeItemNodeText(itemXml, xpath);
  }

  /**
   * Set the (one or more) item texts.
   * Valid for single and multiple texts.
   * @todo FIB, MATCHING TEXT
   * @param itemXml
   * @param itemText text to be updated
   */
  public void setItemTexts(List<ItemTextIfc> itemTextList, Item itemXml)
  {
    String xPath = "assessmentItem/itemBody";
    if (itemTextList.size() < 1)
    {
      return;
    }

    String text = ( (ItemTextIfc) itemTextList.get(0)).getText();
    log.debug("item text: " + text);
    if (itemXml.isFIB())
    {
//      process fib
//      return;
    }
    
    if (itemXml.isFIN())
    {
//      process fin
//      return;
    }
    
    try
    {
      itemXml.update(xPath, text);
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

//  }

  /**
   * get item type string
   * we use title for this for now
   * @param itemXml
   * @return type as string
   */
  public String getItemType(Item itemXml)
  {
    String type = "";
    String xpath = "assessmentItem";
    List list = itemXml.selectNodes(xpath);
    if (list.size() > 0)
    {
      Element element = (Element) list.get(0);
      element.getAttribute(QTIConstantStrings.TITLE);
    }

    return type;
  }

  /**
   * Set the answer texts for item.
   * @param itemTextList the text(s) for item
   */

  public void setAnswers(List<ItemTextIfc> itemTextList, Item itemXml)
  {
    // other types either have no answer or include them in their template
    if (!itemXml.isMatching() && !itemXml.isFIB() && !itemXml.isFIN() &&
        !itemXml.isMCSC() && !itemXml.isMCMC() && !itemXml.isMCMCSS() &&!itemXml.isMXSURVEY())
    {
      return;
    }
    // OK, so now we are in business.
    String xpath =
      "assessmentItem/itemBody/choiceInteraction/<simpleChoice";

    List list = itemXml.selectNodes(xpath);
    log.debug("xpath size:" + list.size());
    Iterator nodeIter = list.iterator();

    Iterator iter = itemTextList.iterator();
    Set answerSet = new HashSet();

    char label = 'A';
    int xpathIndex = 1;
    while (iter.hasNext())
    {
      answerSet = ( (ItemTextIfc) iter.next()).getAnswerSet();
      Iterator aiter = answerSet.iterator();
      while (aiter.hasNext())
      {
        AnswerIfc answer = (AnswerIfc) aiter.next();
        if (Boolean.TRUE.equals(answer.getIsCorrect()))
        {
          this.addCorrectAnswer("" + label, itemXml);
        }
        String value = answer.getText();
        log.debug("answer: " + answer.getText());
        // process into XML
        // we assume that we have equal to or more than the requisite elements
        // if we have more than the existing elements we manufacture more
        // with labels 'A', 'B'....etc.
        Node node = null;
        try
        {
          boolean isInsert = true;
          if (nodeIter.hasNext())
          {
            isInsert = false;
          }

          this.addIndexedEntry(itemXml, xpath, value,
                               isInsert, xpathIndex, "" + label);

        }
        catch (Exception ex)
        {
          log.error("Cannot process source document.", ex);
        }

        label++;
        xpathIndex++;
      }
    }
  }

  /**
   * @todo NEED TO SET CORRECT, INCORRECT, GENERAL FEEDBACK
   * Set the feedback texts for item.
   * @param itemTextList the text(s) for item
   */

  public void setFeedback(List<ItemTextIfc> itemTextList, Item itemXml)
  {
    String xpath =
      "assessmentItem/itemBody/choiceInteraction/<simpleChoice/feedbackInline";
    // for any answers that are now in the template, create a feedback
    int xpathIndex = 1;
    List list = itemXml.selectNodes(xpath);
    if (list == null)
    {
      return;
    }

    Iterator nodeIter = list.iterator();
    Iterator iter = itemTextList.iterator();
    Set answerSet = new HashSet();

    char label = 'A';
    boolean first = true;
    while (iter.hasNext())
    {
      ItemTextIfc itemTextIfc = (ItemTextIfc) iter.next();

      if (first) // then do once
      {
        // add in Correct and InCorrect Feedback
        String correctFeedback = itemTextIfc.getItem().getCorrectItemFeedback();
        String incorrectFeedback = itemTextIfc.getItem().
          getInCorrectItemFeedback();
        String generalFeedback = itemTextIfc.getItem().getGeneralItemFeedback();
        log.debug("NEED TO SET CORRECT FEEDBACK: " + correctFeedback);
        log.debug("NEED TO SET INCORRECT FEEDBACK: " + incorrectFeedback);
        log.debug("NEED TO SET GENERAL FEEDBACK: " + incorrectFeedback);
        first = false;
      }

      // answer feedback
      answerSet = itemTextIfc.getAnswerSet();
      Iterator aiter = answerSet.iterator();
      while (aiter.hasNext())
      {
        AnswerIfc answer = (AnswerIfc) aiter.next();
        String value = answer.getGeneralAnswerFeedback();
        log.debug("answer feedback: " + answer.getText());
        Node node = null;
        try
        {
          boolean isInsert = true;
          if (nodeIter.hasNext())
          {
            isInsert = false;
          }
          addIndexedEntry(itemXml, xpath, value,
                          isInsert, xpathIndex, null);
        }
        catch (Exception ex)
        {
          log.error("Cannot process source document.", ex);
        }

        label++;
        xpathIndex++;
      }
    }
  }

  /**
   * Add/insert the index-th value.
   * @param itemXml the item xml
   * @param xpath
   * @param value
   * @param isInsert
   * @param index the numnber
   * @param identifier set this attribute if not null)
   */
  private void addIndexedEntry(Item itemXml, String xpath, String value,
                               boolean isInsert, int index, String identifier)

  {
    String indexBrackets = "[" + index + "]";
    String thisNode = xpath + indexBrackets;
    String thisNodeIdentity = thisNode + "/@identity";
    if (isInsert)
    {
      log.debug("Adding entry: " + thisNode);
      itemXml.insertElement(thisNode, xpath, "itemfeedback");
    }
    else
    {
      log.debug("Updating entry: " + thisNode);
    }
    try
    {
      if (value == null)
      {
        value = "";
      }
      itemXml.update(thisNode, value);
      log.debug("updated value in addIndexedEntry()");
    }
    catch (Exception ex)
    {
      log.error("Cannot update value in addIndexedEntry(): " + ex);
    }
  }

  /**
   * get QTI version
   * @return
   */
  protected int getQtiVersion()
  {
    return QTIVersion.VERSION_2_0;
  }

  /**
   * @todo implement this method for 2.0 release
   * @param incorrectAnswerLabel
   * @param itemXml
   */
  public void addIncorrectAnswer(String incorrectAnswerLabel, Item itemXml)
  {
  }
	
  public void setItemLabel(String itemLabel, Item itemXml){
	  //todo
  }
  
  public void setItemText(String itemText, Item itemXml)
  { //todo
  }
  
  public void setItemText(String itemText, String flowClass, Item itemXml){
	  //todo
  }
  
  public void setPresentationLabel(String presentationLabel, Item itemXml){
	  //todo
  }
  
  public void setPresentationFlowResponseIdent(String presentationFlowResponseIdent, Item itemXml){
	  //todo
  }

public void setAttachments(Set<? extends AttachmentIfc> attachmentSet, Item item) {
	// todo
	
}
}
