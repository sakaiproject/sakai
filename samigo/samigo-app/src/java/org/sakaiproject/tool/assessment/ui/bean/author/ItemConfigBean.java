/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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


package org.sakaiproject.tool.assessment.ui.bean.author;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.faces.model.SelectItem;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.ResourceLoader; 

/**
 *
 * <p> </p>
 * <p>Description: Describes global item settings for authoring.
 * Describes what item types the will be supported.
 * To change, modify the itemConfig properties in the faces.config file.
 * Also developers could add an administrative configuration later.</p>
 *
 */
public class ItemConfigBean implements Serializable
{
  /**
	 * 
	 */
	private static final long serialVersionUID = 5017545754149103817L;

private static final String msgResource =
    "org.sakaiproject.tool.assessment.bundle.AuthorMessages";

  private boolean showFileUpload;
  private boolean showEssay;
  private boolean showAudio;
  private boolean showMatching;
  private boolean showTrueFalse;
  private boolean showMultipleChoiceSingleCorrect;
  private boolean showMultipleChoiceMultipleCorrect;
  private boolean showSurvey;
  private boolean showFillInTheBlank;
  private boolean showFillInNumeric;
  private boolean showExtendedMatchingItems;
  private boolean selectFromQuestionPool;
  private boolean selectFromQuestionBank;
  private boolean showMatrixSurvey;
  private boolean showCalculatedQuestion; // CALCULATED_QUESTION
  private boolean showImageMapQuestion; //IMAGEMAP_QUESTION
  private boolean showSearchQuestion; //SEARCH QUESTION

  /**
   * Should we show extended matching items question?
   * @return if true
   */
  public boolean isShowExtendedMatchingItems()
  {
    return ServerConfigurationService.getBoolean("samigo.question.show.extendedmatchingitems",showExtendedMatchingItems); 
  }
  /**
   * Set whether extended matching items should be shown.
   */
  public void setShowExtendedMatchingItems(boolean showExtendedMatchingItems)
  {
    this.showExtendedMatchingItems = showExtendedMatchingItems;
  }
  
  /**
   * Should we show file upload question?
   * @return if true
   */
  public boolean isShowFileUpload()
  {
    return ServerConfigurationService.getBoolean("samigo.question.show.fileupload",showFileUpload); 
  }
  /**
   * Set whether file upload should be shown.
   * @param showFileUpload if this type should be shown
   */
  public void setShowFileUpload(boolean showFileUpload)
  {
    this.showFileUpload = showFileUpload;
  }
  /**
   * Should we show short answer/essay question?
   * @return if true
   */
  public boolean isShowEssay()
  {
    return ServerConfigurationService.getBoolean("samigo.question.show.essay",showEssay); 
  }
  /**
   * Set whether essay/short answer should be shown.
   * @param showEssay if this type should be shown
   */
  public void setShowEssay(boolean showEssay)
  {
    this.showEssay = showEssay;
  }
  /**
   * Should we show audio upload question?
   * @return if true
   */
  public boolean isShowAudio()
  {
    return ServerConfigurationService.getBoolean("samigo.question.show.audio",showAudio); 
  }
  /**
   * Set whether audio recording should be shown.
   * @param showAudio if this type should be shown
   */
  public void setShowAudio(boolean showAudio)
  {
    this.showAudio = showAudio;
  }
  /**
   * Should we show matching question?
   * @return if true
   */
  public boolean isShowMatching()
  {
    return ServerConfigurationService.getBoolean("samigo.question.show.matching",showMatching); 
  }
  /**
   * Set whether matching should be shown.
   * @param showMatching if this type should be shown
   */
  public void setShowMatching(boolean showMatching)
  {
    this.showMatching = showMatching;
  }
  /**
   *
   * @return if true
   */
  public boolean isShowTrueFalse()
  {
    return ServerConfigurationService.getBoolean("samigo.question.show.truefalse",showTrueFalse); 
  }
  /**
   * Set whether we show true/false question.
   * @param showTrueFalse if this type should be shown
   */
  public void setShowTrueFalse(boolean showTrueFalse)
  {
    this.showTrueFalse = showTrueFalse;
  }
  /**
   * Should we show all multiple choice type question?
   * That means both multiple correct and single correct.
   * @return if true if this type should be shown
   */
  public boolean isShowAllMultipleChoice()
  {
	return isShowMultipleChoiceSingleCorrect() && isShowMultipleChoiceMultipleCorrect();
  }

  /**
   * Should we show multiple choice single correct question?
   * @return if true
   */
  public boolean isShowMultipleChoiceSingleCorrect()
  {
    return ServerConfigurationService.getBoolean("samigo.question.show.multiplechoicesinglecorrect",showMultipleChoiceSingleCorrect); 
  }

  /**
   * Set whether multiple choice single correct should be shown.
   * @param showMultipleChoiceSingleCorrect if this type should be shown
   */
  public void setShowMultipleChoiceSingleCorrect(boolean showMultipleChoiceSingleCorrect)
  {
    this.showMultipleChoiceSingleCorrect = showMultipleChoiceSingleCorrect;
  }
  /**
   * Should we show multiple choice multiple correct question?
   * @return if true
   */
  public boolean isShowMultipleChoiceMultipleCorrect()
  {
    return ServerConfigurationService.getBoolean("samigo.question.show.multiplechoicemultiplecorrect",showMultipleChoiceMultipleCorrect); 
  }
  /**
   * Set whether multiple choice multiple correct should be shown.
   * @param showMultipleChoiceMultipleCorrect if this type should be shown
   */
  public void setShowMultipleChoiceMultipleCorrect(boolean showMultipleChoiceMultipleCorrect)
  {
    this.showMultipleChoiceMultipleCorrect = showMultipleChoiceMultipleCorrect;
  }
  /**
  * Should we show fill in the blank question?
  * @return if true
  */

  public boolean isShowFillInTheBlank()
  {
    return ServerConfigurationService.getBoolean("samigo.question.show.fillintheblank",showFillInTheBlank); 
  }
  /**
   * Set whether fill in the blank should be shown.
   * @param showFillInTheBlank if this type should be shown
   */
  public void setShowFillInTheBlank(boolean showFillInTheBlank)
  {
    this.showFillInTheBlank = showFillInTheBlank;
  }

  /**
   * Should we show fill in numeric question?
  * @return if true
  */

  public boolean isShowFillInNumeric()
  {
	  return ServerConfigurationService.getBoolean("samigo.question.show.fillinnumeric",showFillInNumeric); 
  }

  /**
   * Set whether fill in numeric should be shown.
   * @param showFillInNumeric if this type should be shown
   */

  public void setShowFillInNumeric(boolean showFillInNumeric)
  {
    this.showFillInNumeric = showFillInNumeric;
  }
  /**
   * Should we show survey question?
   * @return if true
   */
  public boolean isShowSurvey()
  {
	  return ServerConfigurationService.getBoolean("samigo.question.show.survey",showSurvey); 
  }
  /**
   * Set whether survey should be shown.
   * @param showSurvey if this type should be shown
   */
  public void setShowSurvey(boolean showSurvey)
  {
    this.showSurvey = showSurvey;
  }
  
  public boolean isShowMatrixSurvey()
  {
	  return ServerConfigurationService.getBoolean("samigo.question.show.matrixsurvey",showMatrixSurvey); 
  }

  public void setShowMatrixSurvey(boolean showMatrixSurvey)
  {
	  this.showMatrixSurvey = showMatrixSurvey;
  }

  /**
   * Should we show CalculatedQuestion?
   * @return if true
   */
  public boolean isShowCalculatedQuestion()
  {
	  return ServerConfigurationService.getBoolean("samigo.question.show.calculatedquestion",showCalculatedQuestion); 
  }
  /**
   * Set whether matching should be shown.
   * @param showMatching if this type should be shown
   */
  public void setShowCalculatedQuestion(boolean showCalculatedQuestion)
  {
    this.showCalculatedQuestion = showCalculatedQuestion;
  }
  
  /**
    * Should we show ImageMapQuestion?
    * @return if true
    */
  public boolean isShowImageMapQuestion()
  {
	  return ServerConfigurationService.getBoolean("samigo.question.show.showImageMapQuestion",showImageMapQuestion); 
  }
  /**
    * Set whether ImageMap should be shown.
    * @param showImageMap if this type should be shown
    */
  public void setShowImageMapQuestion(boolean showImageMapQuestion)
  {
      this.showImageMapQuestion = showImageMapQuestion;
  }

  /**
   * Should we show the search question option?
   * @return if true
   */
  public boolean isShowSearchQuestion()
  {
    return ServerConfigurationService.getBoolean("samigo.question.show.showSearchQuestion",false);
  }
  /**
   * Set whether search question option should be shown.
   * @param showSearchQuestion if this type should be shown
   */
  public void setShowSearchQuestion(boolean showSearchQuestion)
  {
    this.showSearchQuestion = showSearchQuestion;
  }

  /**
   * Derived property.  Get arraylist of item type SelectItems.
   * We are not lazy loading this so that we can change these dynamically.
   * Most are being injected from the faces-config, but whether we select from
   * question pools is always dynamic.
   *
   * @return ArrayList of model SelectItems
   */

  public List<SelectItem> getItemTypeSelectList()
  {
    List<SelectItem> list = new ArrayList<SelectItem>();

    

    if (isShowAllMultipleChoice())
      list.add(new SelectItem(String.valueOf(TypeIfc.MULTIPLE_CHOICE),
        getResourceDisplayName("multiple_choice_type")));
    
    if (isShowSurvey())
      list.add(new SelectItem(String.valueOf(TypeIfc.MULTIPLE_CHOICE_SURVEY),
        getResourceDisplayName("multiple_choice_surv")));
    
    if (isShowMatrixSurvey())
    	list.add(new SelectItem(String.valueOf(TypeIfc.MATRIX_CHOICES_SURVEY),
    			getResourceDisplayName("matrix_choices_surv")));

    if (isShowEssay())
      list.add(new SelectItem(String.valueOf(TypeIfc.ESSAY_QUESTION),
    		  getResourceDisplayName("short_answer_essay")));

    if (isShowFillInTheBlank())
      list.add(new SelectItem(String.valueOf(TypeIfc.FILL_IN_BLANK),
    		  getResourceDisplayName("fill_in_the_blank")));

    if (isShowFillInNumeric())
      list.add(new SelectItem(String.valueOf(TypeIfc.FILL_IN_NUMERIC),
    		  getResourceDisplayName("fill_in_numeric")));
    
    if (isShowMatching())
      list.add(new SelectItem(String.valueOf(TypeIfc.MATCHING),
    		  getResourceDisplayName("matching")));

    if (isShowTrueFalse())
      list.add(new SelectItem(String.valueOf(TypeIfc.TRUE_FALSE),
    		  getResourceDisplayName("true_false")));

    if (isShowAudio())
      list.add(new SelectItem(String.valueOf(TypeIfc.AUDIO_RECORDING),
    		  getResourceDisplayName("audio_recording")));

    if (isShowFileUpload())
      list.add(new SelectItem(String.valueOf(TypeIfc.FILE_UPLOAD),
    		  getResourceDisplayName("file_upload")));

    // resource display name in AuthorMessages.properties
    if (isShowExtendedMatchingItems())
        list.add(new SelectItem(String.valueOf(TypeIfc.EXTENDED_MATCHING_ITEMS),
      		  getResourceDisplayName("extended_matching_items")));
    
    if (isSelectFromQuestionPool())
      list.add(new SelectItem("10", getResourceDisplayName("import_from_q")));

    if (isShowCalculatedQuestion())
        list.add(new SelectItem(String.valueOf(TypeIfc.CALCULATED_QUESTION), getResourceDisplayName("calculated_question"))); // CALCULATED_QUESTION

    if (isShowImageMapQuestion())
    	list.add(new SelectItem(String.valueOf(TypeIfc.IMAGEMAP_QUESTION), getResourceDisplayName("image_map_question"))); // IMAGEMAP_QUESTION

    if (isShowSearchQuestion())
      list.add(new SelectItem("17", getResourceDisplayName("search_question"))); // SEARCH IN THE LIST OF QUESTIONS
    
    if (isSelectFromQuestionBank()) {
    	// Check if the question bank tool is installed and not stealthed or hidden
    	// and only show the option if it's reasonable, such as when questionpools are
    	if (ToolManager.getTool("sakai.questionbank.client") != null
			&& !ServerConfigurationService
					.getString(
							"stealthTools@org.sakaiproject.tool.api.ActiveToolManager")
					.contains("sakai.questionbank.client")
			&& !ServerConfigurationService
					.getString(
							"hiddenTools@org.sakaiproject.tool.api.ActiveToolManager")
					.contains("sakai.questionbank.client")) {

			list.add(new SelectItem("100",
					getResourceDisplayName("import_from_question_bank")));
    	}
    }
    
    Comparator<SelectItem> comparator = new Comparator<SelectItem>() {
        @Override
        public int compare(SelectItem s1, SelectItem s2) {
            // the items must be compared based on their value (assuming String or Integer value here)
            return s1.getLabel().compareTo(s2.getLabel());
        }
    };
    
    Collections.sort(list, comparator);
    
    List<SelectItem> ret = new ArrayList<SelectItem>();
    ret.add(new SelectItem("", getResourceDisplayName("select_qtype")));
    ret.addAll(list);
    
    return ret;
  }

  /**
   * Can we select items from a question pool?
   * If we are in question pools we cannot select items from pool.
   * If we are not in question pools we can select items from pool.
   * @return if we can select from question pool.
   */
  public boolean isSelectFromQuestionPool()
  {
    return selectFromQuestionPool;
  }

  /**
   * Set whether we can select items from a question pool.
   * If we are in question pools we cannot select items from pool.
   * If we are not in question pools we can select items from pool.
   * @return if we can select from question pool.
   */

  public void setSelectFromQuestionPool(boolean selectFromQuestionPool)
  {
    this.selectFromQuestionPool = selectFromQuestionPool;
  }

    /**
     * Can we select items from a question bank?
     * @return if we can select from question pool.
     */
    public boolean isSelectFromQuestionBank()
    {
      return selectFromQuestionBank;
    }
  
    /**
     * Set whether we can select items from a question bank.
     * @return if we can select from question bank.
     */
    public void setSelectFromQuestionBank(boolean selectFromQuestionBank)
    {
      this.selectFromQuestionBank = selectFromQuestionBank;
    }

  /**
   * Utility for looking up item resources.
   * @param resName name to look up
   * @return the localized name.
   */
  private String getResourceDisplayName(String resName)
  {
	  ResourceLoader res = new ResourceLoader(msgResource);
    return res.getString(resName);
  }
}
