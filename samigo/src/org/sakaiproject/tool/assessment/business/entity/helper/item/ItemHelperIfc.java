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

package org.sakaiproject.tool.assessment.business.entity.helper.item;

import java.io.InputStream;
import java.util.ArrayList;

import org.sakaiproject.tool.assessment.business.entity.asi.Item;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;

/**
 * Interface for QTI-versioned item helper implementation.
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley esmiley@stanford.edu
 * @version $Id$
 */

public interface ItemHelperIfc
{
  public static final long ITEM_AUDIO = TypeIfc.AUDIO_RECORDING.longValue();
  public static final long ITEM_ESSAY = TypeIfc.ESSAY_QUESTION.longValue();
  public static final long ITEM_FILE = TypeIfc.FILE_UPLOAD.longValue();
  public static final long ITEM_FIB = TypeIfc.FILL_IN_BLANK.longValue();
  public static final long ITEM_MCSC = TypeIfc.MULTIPLE_CHOICE.longValue();
  public static final long ITEM_SURVEY = TypeIfc.MULTIPLE_CHOICE_SURVEY.
    longValue();
  public static final long ITEM_MCMC = TypeIfc.MULTIPLE_CORRECT.longValue();
  public static final long ITEM_TF = TypeIfc.TRUE_FALSE.longValue();
  public static final long ITEM_MATCHING = TypeIfc.MATCHING.longValue();
  public String[] itemTypes =
    {
    "Unknown Type",
    "Multiple Choice",
    "Multiple Choice",
    "Survey",
    "True or False",
    "Short Answers/Essay",
    "File Upload",
    "Audio Recording",
    "Fill In the Blank",
    "Matching",
  };

  /**
   * Get Item Xml for a given item type as a TypeIfc.
   * @param type item type as a TypeIfc
   * @return
   */

  public Item readTypeXMLItem(TypeIfc type);

  /**
   * Get Item Xml for a given survey item scale name.
   * @param scaleName
   * @return
   */
  public Item readTypeSurveyItem(String scaleName);

  /**
   * Read XML document from input stream
   *
   * @param inputStream XML docuemnt stream
   *
   * @return item XML
   */
  public Item readXMLDocument(InputStream inputStream);

  /**
   * Get item type string which is used for the title of a given item type
   * @param type
   * @return
   */

  public String getItemTypeString(TypeIfc type);

//  /**
//   * Add/update a response entry/answer
//   * @param itemXml
//   * @param xpath
//   * @param itemText
//   * @param isInsert
//   * @param responseNo
//   * @param responseLabelIdent
//   */
//  public void addResponseEntry(
//    Item itemXml, String xpath, String value,
//    boolean isInsert, String responseNo, String responseLabel);
  /**
   * DOCUMENTATION PENDING
   *
   * @param itemXml item xml to update
   * @param xpath the XPath
   * @param value value to set
   *
   * @return the item xml
   */
  public Item updateItemXml(Item itemXml, String xpath, String value);

  /**
   * Add minimum score to item XML.
   * @param score
   * @param itemXml
   */
  public void addMaxScore(Float score, Item itemXml);

  /**
   * Add maximum score to item XML
   * @param score
   * @param itemXml
   */
  public void addMinScore(Float score, Item itemXml);

  /**
   * Flags an answer as correct.
   * @param correctAnswerLabel
   */
  public void addCorrectAnswer(String correctAnswerLabel, Item itemXml);

  /**
   * Flags an answer as NOT correct.
   * @param correctAnswerLabel
   */
  public void addIncorrectAnswer(String incorrectAnswerLabel, Item itemXml);


  /**
   * Get the metadata field entry XPath
   * @return the XPath
   */
  public String getMetaXPath();

  /**
   * Get the metadata field entry XPath for a given label
   * @param fieldlabel
   * @return the XPath
   */
  public String getMetaLabelXPath(String fieldlabel);

  /**
   * Get the text for the item
   * @param itemXml
   * @return the text
   */
  public String getText(Item itemXml);

  /**
   * Set the (one or more) item texts.
   * Valid for single and multiple texts.
   * @param itemXml
   * @param itemText text to be updated
   */
  public void setItemTexts(ArrayList itemTextList, Item itemXml);

  /**
   * Set the (usually instructional text) for trhe item.
   * @param itemText
   * @param itemXml
   */
  public void setItemText(String itemText, Item itemXml);


  /**
  * @param itemXml
  * @return type as string
  */
  public String getItemType(Item itemXml);

  /**
   * Set the answer texts for item.
   * @param itemTextList the text(s) for item
   */
  public void setAnswers(ArrayList itemTextList, Item itemXml);

  /**
   * Set the feedback texts for item.
   * @param itemTextList the text(s) for item
   */
  public void setFeedback(ArrayList itemTextList, Item itemXml);
}


