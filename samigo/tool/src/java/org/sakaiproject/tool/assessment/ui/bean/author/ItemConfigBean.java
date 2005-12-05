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
package org.sakaiproject.tool.assessment.ui.bean.author;

import java.io.Serializable;

/**
 *
 * <p> </p>
 * <p>Description: Describes global item settings for authoring.
 * Describes what item types the will be supported.
 * To change, modify the itemConfig properties in the faces.config file.
 * Also developers could add an administrative configuration later.</p>
 * <p>Sakai Project Copyright (c) 2005</p>
 * <p> </p>
 * @author Ed Smiley <esmiley@stanford.edu>
 *
 */
public class ItemConfigBean implements Serializable
{
  private boolean showFileUpload;
  private boolean showEssay;
  private boolean showAudio;
  private boolean showMatching;
  private boolean showTrueFalse;
  private boolean showMultipleChoiceSingleCorrect;
  private boolean showMultipleChoiceMultipleCorrect;
  private boolean showSurvey;
  private boolean showFillInTheBlank;

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
}