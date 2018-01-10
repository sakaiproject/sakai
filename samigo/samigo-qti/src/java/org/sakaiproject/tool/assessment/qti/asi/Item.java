/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.qti.asi;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.sakaiproject.tool.assessment.data.dao.assessment.Answer;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemText;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTagIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.qti.constants.AuthoringConstantStrings;
import org.sakaiproject.tool.assessment.qti.constants.QTIConstantStrings;
import org.sakaiproject.tool.assessment.qti.constants.QTIVersion;
import org.sakaiproject.tool.assessment.qti.helper.QTIHelperFactory;
import org.sakaiproject.tool.assessment.qti.helper.item.ItemHelperIfc;

/**
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author rshastri
 * @author Ed Smiley esmiley@stanford.edu
 * @version $Id$
 */
@Slf4j
public class Item extends ASIBaseClass
{
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
    switch (qtiVersion)
    {
      case QTIVersion.VERSION_1_2:
        basePath = QTIConstantStrings.ITEM; // for v 1.2
        identity = QTIConstantStrings.IDENT;
        break;
      case QTIVersion.VERSION_2_0:
        basePath = QTIConstantStrings.ASSESSMENTITEM;// for v 2.0
        identity = QTIConstantStrings.AITEM_IDENT;
        break;
      default:
        basePath = QTIConstantStrings.ITEM; // DEFAULT
        identity = QTIConstantStrings.IDENT;
        break;
    }

    QTIHelperFactory factory = new QTIHelperFactory();
    helper = factory.getItemHelperInstance(qtiVersion);
    log.debug("Item XML class.initVersion(int qtiVersion)");
    log.debug("qtiVersion="+qtiVersion);
    log.debug("basePath="+basePath);
    log.debug("identity="+identity);
  }

  /**
   * set identity attribute (ident/identity)
   * @param ident the value
   */

  public void setIdent(String ident)
  {
    String xpath = basePath;
    List<?> list = this.selectNodes(xpath);
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
    List<?> list = this.selectNodes(xpath);
    if (list.size() > 0)
    {
      Element element = (Element) list.get(0);
      element.setAttribute(QTIConstantStrings.TITLE, escapeXml(title));
    }
  }

  /**
   * Update XML from persistence
   * @param item
   */
  public void update(ItemDataIfc item)
  {
    if(item == null) {
    	return;
    }
    
    // metadata
    setFieldentry("ITEM_OBJECTIVE",
      item.getItemMetaDataByLabel(ItemMetaDataIfc.OBJECTIVE ));
    setFieldentry("ITEM_KEYWORD",
      item.getItemMetaDataByLabel(ItemMetaDataIfc.KEYWORD));
    setFieldentry("ITEM_RUBRIC", item.getItemMetaDataByLabel(ItemMetaDataIfc.RUBRIC ));


      Set<ItemTagIfc> tagIfcSet = item.getItemTagSet();
      String tagsString = "";
      Boolean first=true;
      for (ItemTagIfc tagIfc: tagIfcSet) {
         if (!first){
            tagsString += ", ";
         }
         tagsString += tagIfc.getTagLabel();
         if (!(tagIfc.getTagCollectionName().isEmpty())){
             tagsString += " ("+tagIfc.getTagCollectionName() + ")";
         }else{
           tagsString += " (No tag collection)";
         }
         first=false;
      }

    setFieldentry("ITEM_TAGS", tagsString);

      setFieldentry("ATTACHMENT", getAttachment(item));

      // set TIMEALLOWED and NUM_OF_ATTEMPTS for audio recording questions:
    if (item.getDuration()!=null){
    	setFieldentry("TIMEALLOWED",
    			item.getDuration().toString()); 
    }
    if (item.getTriesAllowed()!=null){
    	setFieldentry("NUM_OF_ATTEMPTS",
    			item.getTriesAllowed().toString());
    }
    //  rshastri: SAK-1824
    if((item.getTypeId().equals(TypeIfc.TRUE_FALSE) ||
    		item.getTypeId().equals(TypeIfc.MULTIPLE_CHOICE)||
    		item.getTypeId().equals(TypeIfc.MULTIPLE_CORRECT) ||
    		item.getTypeId().equals(TypeIfc.MULTIPLE_CORRECT_SINGLE_SELECTION)) && item.getHasRationale() !=null)
    {	
    	setFieldentry("hasRationale", item.getHasRationale().toString());
    }
    if(item.getTypeId().equals(TypeIfc.MULTIPLE_CHOICE) && item.getPartialCreditFlag()){
    	setFieldentry("PARTIAL_CREDIT", "TRUE");

    }else{
    	setFieldentry("PARTIAL_CREDIT", "FALSE");
    }
    //  rshastri: SAK-1824
    // item data
    if (!this.isSurvey() && !this.isMXSURVEY()) //surveys are unscored
    {
      helper.addMaxScore(item.getScore(), this);
      helper.addMinScore(item.getDiscount(), this);
    }

    if(item !=null &&(item.getTypeId().equals(TypeIfc.FILL_IN_BLANK))) {
    	setFieldentry("MUTUALLY_EXCLUSIVE", item.getItemMetaDataByLabel(ItemMetaDataIfc.MUTUALLY_EXCLUSIVE_FOR_FIB ));
       	setFieldentry("CASE_SENSITIVE", item.getItemMetaDataByLabel(ItemMetaDataIfc.CASE_SENSITIVE_FOR_FIB ));
        setFieldentry("IGNORE_SPACES", item.getItemMetaDataByLabel(ItemMetaDataIfc.IGNORE_SPACES_FOR_FIB ));
    }
    
    if(item !=null && (item.getTypeId().equals(TypeIfc.MULTIPLE_CHOICE) || item.getTypeId().equals(TypeIfc.MULTIPLE_CORRECT) ||item.getTypeId().equals(TypeIfc.MULTIPLE_CORRECT_SINGLE_SELECTION))) {
    	setFieldentry("RANDOMIZE", item.getItemMetaDataByLabel(ItemMetaDataIfc.RANDOMIZE ));
    	setFieldentry("MCMS_PARTIAL_CREDIT", item.getItemMetaDataByLabel(ItemMetaDataIfc.MCMS_PARTIAL_CREDIT ));
    }
    
    if (this.isMXSURVEY()) {
    	setFieldentry("FORCE_RANKING", item.getItemMetaDataByLabel(ItemMetaDataIfc.FORCE_RANKING));
    	setFieldentry("ADD_COMMENT_MATRIX", item.getItemMetaDataByLabel(ItemMetaDataIfc.ADD_COMMENT_MATRIX));
    	setFieldentry("MX_SURVEY_QUESTION_COMMENTFIELD", item.getItemMetaDataByLabel(ItemMetaDataIfc.MX_SURVEY_QUESTION_COMMENTFIELD));
    	setFieldentry("MX_SURVEY_RELATIVE_WIDTH", item.getItemMetaDataByLabel(ItemMetaDataIfc.MX_SURVEY_RELATIVE_WIDTH));
    }
    
    String instruction = item.getInstruction();
    if (this.isMatching() || this.isFIB() || this.isFIN() || this.isMXSURVEY() || this.isCalculatedQuestion()|| this.isImageMapQuestion())
    {
      if ( instruction != null)
        {
    	  helper.setItemText(instruction, this);
        }
    }
    
    if(this.isEMI()){
    	helper.setItemLabel(StringEscapeUtils.escapeXml(item.getThemeText()), this);
		helper.setPresentationLabel(item.getIsAnswerOptionsSimple()?"Simple":"Rich", this);
		String ident = "EMI" + item.getSequence();
		helper.setPresentationFlowResponseIdent(ident, this);
		//set attachments
		helper.setAttachments(item.getItemAttachmentSet(), this);
		//set the options
		if(item.getIsAnswerOptionsSimple()){
			setItemTexts(Collections.singletonList(
					item.getItemTextBySequence(ItemTextIfc.EMI_ANSWER_OPTIONS_SEQUENCE)));
		}else{
			ItemText it = new ItemText();
			it.setSequence(ItemTextIfc.EMI_ANSWER_OPTIONS_SEQUENCE);
			it.setText(item.getEmiAnswerOptionsRichText());
			Set<AnswerIfc> answerSet = new HashSet<AnswerIfc>();
			String labels = item.getEmiAnswerOptionLabels();
			for(int i = 0; i < item.getAnswerOptionsRichCount(); i++){
				Answer a = new Answer();
				a.setSequence(Long.valueOf(i));
				a.setLabel(labels.substring(i, i+1));
				answerSet.add(a);
			}
			it.setAnswerSet(answerSet);
			setItemTexts(Collections.singletonList((ItemTextIfc)it));
		}
		helper.setItemText(item.getLeadInText(), "Leadin", this);
		setAnswers(item.getEmiQuestionAnswerCombinations());
    	return;//already setting text and answers, EMI has no feedback.
    }
    List<ItemTextIfc> itemTexts = item.getItemTextArraySorted();

    setItemTexts(itemTexts);
    if (this.isTrueFalse()) // we know what the answers are (T/F)
    {
      Boolean isTrue = item.getIsTrue();
      if (isTrue == null)
        isTrue = Boolean.FALSE;
      setAnswerTrueFalse(isTrue.booleanValue());
    }
    else {
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
    log.debug("isTrue="+isTrue);
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
  public void setItemTexts(List<ItemTextIfc> itemTextList)
  {
    helper.setItemTexts(itemTextList, this);
  }

  public boolean  isEssay()
  {
    boolean essay =
      AuthoringConstantStrings.ESSAY.equals(this.getItemType()) ||
      AuthoringConstantStrings.ESSAY_ALT.equals(this.getItemType());
    return essay ? true : false;
  }

  public boolean  isSurvey()
  {
    return AuthoringConstantStrings.SURVEY.equals(this.getItemType()) ? true : false;
  }
  
  public boolean isMXSURVEY()
  {
    return AuthoringConstantStrings.MATRIX.equals(this.getItemType()) ? true : false;
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
  
  public boolean  isFIN()
  {
    return AuthoringConstantStrings.FIN.equals(this.getItemType()) ? true : false;
  }

  public boolean  isMCMC()
  {
    return AuthoringConstantStrings.MCMC.equals(this.getItemType()) ? true : false;
  }
  
  public boolean  isMCMCSS()
  {
    return AuthoringConstantStrings.MCMCSS.equals(this.getItemType()) ? true : false;
  }

  public boolean  isMCSC()
  {
    return AuthoringConstantStrings.MCSC.equals(this.getItemType()) ? true : false;
  }

  private boolean isTrueFalse()
  {
    return AuthoringConstantStrings.TF.equals(this.getItemType()) ? true : false;
  }

  public boolean isEMI()
  {
	  return AuthoringConstantStrings.EMI.equals(this.getItemType()) ? true : false;
  }

  // CALCULATED_QUESTION
  public boolean isCalculatedQuestion()
  {
    return AuthoringConstantStrings.CALCQ.equals(this.getItemType()) ? true : false;
  }
  
  //IMAGEMAP_QUESTION
  public boolean isImageMapQuestion()
  {
	return AuthoringConstantStrings.IMAGMQ.equals(this.getItemType()) ? true : false;
  }

  /**
   * Set the answer texts for item.
   * @param itemTextList the text(s) for item
   */
  public void setAnswers(List<ItemTextIfc> itemTextList)
  {
    helper.setAnswers(itemTextList, this);
  }

  /**
   * Set the feedback texts for item.
   * @param itemTextList the text(s) for item
   */
  public void setFeedback(List<ItemTextIfc> itemTextList)
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
  
  private String getAttachment(ItemDataIfc item) {
	  Set<ItemAttachmentIfc> attachmentSet = item.getItemAttachmentSet();
   	  if (attachmentSet != null && attachmentSet.size() != 0) { 
   		Iterator<ItemAttachmentIfc> iter = attachmentSet.iterator();
   		ItemAttachmentIfc attachmentData = null;
   		StringBuilder attachment = new StringBuilder();
   		while (iter.hasNext())
   		{
   			attachmentData = iter.next();
   			attachment.append(attachmentData.getResourceId().replaceAll(" ", ""));
   			attachment.append("|");
   			attachment.append(attachmentData.getFilename());
   			attachment.append("|");
   			attachment.append(attachmentData.getMimeType());
   			attachment.append("\n");
   		}
   		return attachment.toString();
   	  }
   	  else {
   		return null;
   	  }
  }
}


