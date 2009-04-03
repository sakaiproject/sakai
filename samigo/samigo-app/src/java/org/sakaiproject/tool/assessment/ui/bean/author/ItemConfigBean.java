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
 *       http://www.osedu.org/licenses/ECL-2.0
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

import javax.faces.model.SelectItem;

import org.sakaiproject.component.cover.ServerConfigurationService;
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
  private boolean selectFromQuestionPool;
  private boolean selectFromQuestionBank;

  /**
   * Should we show file upload question?
   * @return if true
   */
  public boolean isShowFileUpload()
  {
    return showFileUpload;
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
    return showEssay;
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
    return showAudio;
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
    return showMatching;
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
    return showTrueFalse;
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
    return showMultipleChoiceMultipleCorrect && showMultipleChoiceSingleCorrect;
  }

  /**
   * Should we show multiple choice single correct question?
   * @return if true
   */
  public boolean isShowMultipleChoiceSingleCorrect()
  {
    return showMultipleChoiceSingleCorrect;
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
    return showMultipleChoiceMultipleCorrect;
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
    return showFillInTheBlank;
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
   * Should we show survey question?
  * @return if true
  */

  public boolean isShowFillInNumeric()
     {
       return showFillInNumeric;
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
    return showSurvey;
  }
  /**
   * Set whether survey should be shown.
   * @param showSurvey if this type should be shown
   */
  public void setShowSurvey(boolean showSurvey)
  {
    this.showSurvey = showSurvey;
  }

  /**
   * Derived property.  Get arraylist of item type SelectItems.
   * We are not lazy loading this so that we can change these dynamically.
   * Most are being injected from the faces-config, but whether we select from
   * question pools is always dynamic.
   *
   * @return ArrayList of model SelectItems
   */

  public ArrayList getItemTypeSelectList()
  {
    ArrayList list = new ArrayList();

    list.add(new SelectItem("", getResourceDisplayName("select_qtype")));

    if (isShowAllMultipleChoice())
      list.add(new SelectItem("1",
        getResourceDisplayName("multiple_choice_type")));
    
    if (showSurvey)
      list.add(new SelectItem("3",
        getResourceDisplayName("multiple_choice_surv")));

    if (showEssay)
      list.add(new SelectItem("5", getResourceDisplayName("short_answer_essay")));

    if (showFillInTheBlank)
      list.add(new SelectItem("8", getResourceDisplayName("fill_in_the_blank")));

if (showFillInNumeric)
      list.add(new SelectItem("11", getResourceDisplayName("fill_in_numeric")));
    if (showMatching)
      list.add(new SelectItem("9", getResourceDisplayName("matching")));

    if (showTrueFalse)
      list.add(new SelectItem("4", getResourceDisplayName("true_false")));

    if (showAudio)
      list.add(new SelectItem("7", getResourceDisplayName("audio_recording")));

    if (showFileUpload)
      list.add(new SelectItem("6", getResourceDisplayName("file_upload")));

    if (selectFromQuestionPool)
      list.add(new SelectItem("10", getResourceDisplayName("import_from_q")));

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

    return list;
  }

  /*
  // test the item accessors and mutators and the selectItem list
  public static void main (String[] args)
  {
    ItemConfigBean bean = new ItemConfigBean();
    ArrayList list = bean.getItemTypeSelectList();
    for (int i = 0; i < list.size(); i++)
    {
      SelectItem sitem = (SelectItem)list.get(i);
    }

    bean.setSelectFromQuestionPool(true);
    bean.setSelectFromQuestionBank(true);
    bean.setShowAudio(true);
    bean.setShowEssay(true);
    bean.setShowFileUpload(true);
    bean.setShowFillInTheBlank(true);
    bean.setShowFillInNumeric(true);
    bean.setShowMatching(true);
    bean.setShowMultipleChoiceMultipleCorrect(true);
    bean.setShowMultipleChoiceSingleCorrect(true);
    bean.setShowSurvey(true);
    bean.setShowTrueFalse(true);
    list = bean.getItemTypeSelectList();
    for (int i = 0; i < list.size(); i++)
    {
      SelectItem sitem = (SelectItem)list.get(i);
    }
    bean.setShowAudio(false);
    bean.setShowFileUpload(false);
    list = bean.getItemTypeSelectList();
    for (int i = 0; i < list.size(); i++)
    {
      SelectItem sitem = (SelectItem)list.get(i);
    }
  }
  */

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
