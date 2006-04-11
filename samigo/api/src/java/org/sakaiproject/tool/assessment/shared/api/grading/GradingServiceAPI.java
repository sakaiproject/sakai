/**********************************************************************************
* $URL: https://source.sakaiproject.org/svn/trunk/sakai/sam/src/org/sakaiproject/tool/assessment/services/GradingService.java $
* $Id: GradingService.java 632 2005-07-14 21:22:50Z janderse@umich.edu $
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.tool.assessment.shared.api.grading;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.AssessmentGradingIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.ItemGradingIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.MediaIfc;

/**
 * The GradingServiceAPI declares a shared interface to get grading information.
 * @author Ed Smiley <esmiley@stanford.edu>
 */
public interface GradingServiceAPI
{
  /**
   * Get all scores for a published assessment from the back end.
   * @return List of AssessmentGradingIfs
   */
  public List getTotalScores(String publishedId, String which);

 /**
  * Get all submissions for a published assessment from the back end.
  * @return List of AssessmentGradingIfs
  */
  public List getAllSubmissions(String publishedId);

  /**
   * Save the total scores.
   * @param data List of AssessmentGradingDataIfcs
   */
  public void saveTotalScores(List data, PublishedAssessmentIfc pub);

  /**
   * Save the item scores.
   * @param data List of itemGradingDataIfcs
   */
  public void saveItemScores(List data, HashMap map, PublishedAssessmentIfc pub);

  /**
   * Get the score information for each item from the assessment score.
   */
  public Map getItemScores(Long publishedId, Long itemId, String which);
  /**
   * Get the last set of ItemGradingIfc for a student per assessment
   */
  public Map getLastItemGrading(String publishedId, String agentId);

  /**
   * Get the grading data for a given submission
   */
  public Map getStudentGradingData(String assessmentGradingId);
  /**
   * Get the last submission for a student per assessment
   */
  public Map getSubmitData(String publishedId, String agentId);

  /**
   * Get the text for the type.
   * @param typeId
   * @return
   */
  public String getTextForId(Long typeId);

  /**
   * Store the grading data.
   * @param data
   */
  //public void storeGrades(AssessmentGradingIfc data);

  /**
   * Get the count of published assessments.
   * @param publishedAssessmentId
   * @return
   */
  public int getSubmissionSizeOfPublishedAssessment(String publishedAssessmentId);

  /**
   *
   * @return
   */
  public Map getSubmissionSizeOfAllPublishedAssessments();

  /**
   *
   * @param media
   * @param mimeType
   * @return
   */
  public Long saveMedia(byte[] media, String mimeType);

  /**
   *
   * @param mediaData
   * @return
   */
  public Long saveMedia(MediaIfc mediaData);

  /**
   *
   * @param mediaId
   * @return
   */
  public MediaIfc getMedia(String mediaId);

  /**
   *
   * @param itemGradingId
   * @return
   */
  public List getMediaArray(String itemGradingId);

  /**
   *
   * @param i
   * @return
   */
  public List getMediaArray(ItemGradingIfc i);

  /**
   *
   * @param publishedItemId
   * @param agentId
   * @return
   */
  public ItemGradingIfc getLastItemGradingByAgent(String publishedItemId, String agentId);

  /**
   *
   * @param assessmentGradingId
   * @param publishedItemId
   * @return
   */
  public ItemGradingIfc getItemGrading(String assessmentGradingId, String publishedItemId);

  /**
   * Load assessment grading information.
   * @param assessmentGradingId
   * @return
   */
  public AssessmentGradingIfc load(String assessmentGradingId);

  /**
   * Get the grading data for the last submission of this agent.
   * @param publishedAssessmentId
   * @param agentIdString
   * @return
   */
  public AssessmentGradingIfc getLastAssessmentGradingByAgentId(String publishedAssessmentId, String agentIdString);

  /**
   * Save item grading information.
   * @param item
   */
  public void saveItemGrading(ItemGradingIfc item);

  /**
   *
   * @param assessment
   */
  public void saveOrUpdateAssessmentGrading(AssessmentGradingIfc assessment);

}
