/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
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




package org.sakaiproject.tool.assessment.shared.api.grading;

import java.util.List;
import java.util.Map;

import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.MediaData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;

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
  //public void saveItemScores(List data, HashMap map, PublishedAssessmentIfc pub);

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
  public Map getSubmitData(String publishedId, String agentId, Integer scoringoption);

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
  //public void storeGrades(AssessmentGradingData data);

  /**
   * Get the count of published assessments.
   * @param publishedAssessmentId
   * @return
   */
  public int getSubmissionSizeOfPublishedAssessment(String publishedAssessmentId);

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
  public Long saveMedia(MediaData mediaData);

  /**
   *
   * @param mediaId
   * @return
   */
  public MediaData getMedia(String mediaId);

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
  public List getMediaArray(ItemGradingData i);

  /**
   *
   * @param publishedItemId
   * @param agentId
   * @return
   */
  public ItemGradingData getLastItemGradingByAgent(String publishedItemId, String agentId);

  /**
   *
   * @param assessmentGradingId
   * @param publishedItemId
   * @return
   */
  public ItemGradingData getItemGrading(String assessmentGradingId, String publishedItemId);

  /**
   * Load assessment grading information.
   * @param assessmentGradingId
   * @return
   */
  public AssessmentGradingData load(String assessmentGradingId);

  /**
   * Get the grading data for the last submission of this agent.
   * @param publishedAssessmentId
   * @param agentIdString
   * @return
   */
  public AssessmentGradingData getLastAssessmentGradingByAgentId(String publishedAssessmentId, String agentIdString);

  /**
   * Save item grading information.
   * @param item
   */
  public void saveItemGrading(ItemGradingData item);

  /**
   *
   * @param assessment
   */
  public void saveOrUpdateAssessmentGrading(AssessmentGradingData assessment);

}
