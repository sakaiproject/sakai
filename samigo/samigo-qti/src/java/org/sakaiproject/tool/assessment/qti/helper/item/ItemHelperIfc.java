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



package org.sakaiproject.tool.assessment.qti.helper.item;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.sakaiproject.tool.assessment.data.ifc.assessment.AttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.qti.asi.Item;

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
  public static final long ITEM_FIN = TypeIfc.FILL_IN_NUMERIC.longValue();
  public static final long ITEM_MCSC = TypeIfc.MULTIPLE_CHOICE.longValue();
  public static final long ITEM_SURVEY = TypeIfc.MULTIPLE_CHOICE_SURVEY.
    longValue();
  public static final long ITEM_MCMC = TypeIfc.MULTIPLE_CORRECT.longValue();
  public static final long ITEM_MCMC_SS = TypeIfc.MULTIPLE_CORRECT_SINGLE_SELECTION.longValue();
  public static final long ITEM_TF = TypeIfc.TRUE_FALSE.longValue();
  public static final long ITEM_MATCHING = TypeIfc.MATCHING.longValue();
  public static final long ITEM_EMI = TypeIfc.EXTENDED_MATCHING_ITEMS.longValue();
  public static final long ITEM_MXSURVEY = TypeIfc.MATRIX_CHOICES_SURVEY.longValue();
  public static final long ITEM_CALCQ = TypeIfc.CALCULATED_QUESTION.longValue(); // CALCULATED_QUESTION

  public String[] itemTypes =
    {
    "Unknown Type",
    "Multiple Choice",
    "Multiple Correct Answer",
    "Survey",
    "True or False",
    "Short Answers/Essay",
    "File Upload",
    "Audio Recording",
    "Fill In the Blank",
    "Numeric Response",
    "Matching",
    "Extended Matching Items",
    "Matrix Choices Survey",
    "Calculated Question", // CALCULATED_QUESTION
  };

  
  /**
   * Get Item Xml for a given item type as a Long .
   * @param type item type as a Long  
   * @return
   */

  public Item readTypeXMLItem(Long type);

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
  public void addMaxScore(Double score, Item itemXml);

  /**
   * Add maximum score to item XML
   * @param score
   * @param itemXml
   */
  public void addMinScore(Double score, Item itemXml);

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
  public void setItemTexts(List<ItemTextIfc> itemTextList, Item itemXml);
	
  /**
   * Set the label for the item.
   * @param itemLabel
   * @param itemXml
   */
  public void setItemLabel(String itemLabel, Item itemXml);
  
  /**
   * Set the (usually instructional text) for trhe item.
   * @param itemText
   * @param itemXml
   */
  public void setItemText(String itemText, Item itemXml);
  
  /**
   * Set the (usually instructional text) for the item for the specified flow.
   * @param itemText
   * @param itemXml
   */
  public void setItemText(String itemText, String flowClass, Item itemXml);

  /**
   * Set the presentation label.
   * 
   * @param presentationLabel
   * @param itemXml
   */
  public void setPresentationLabel(String presentationLabel, Item itemXml);
  
  /**
   * Set the public void Presentation Flow Response Ident
   * @param presentationFlowResponseIdent
   * @param itemXml
   */
  public void setPresentationFlowResponseIdent(String presentationFlowResponseIdent, Item itemXml);
  
  /**
   * @param itemXml
   * @return type as string
   */
  public String getItemType(Item itemXml);

  /**
   * Set the answer texts for item.
   * @param itemTextList the text(s) for item
   */
  public void setAnswers(List<ItemTextIfc> itemTextList, Item itemXml);

  /**
   * Set the feedback texts for item.
   * @param itemTextList the text(s) for item
   */
  public void setFeedback(List<ItemTextIfc> itemTextList, Item itemXml);

  public void setAttachments(Set<? extends AttachmentIfc> attachmentSet, Item item);
}
