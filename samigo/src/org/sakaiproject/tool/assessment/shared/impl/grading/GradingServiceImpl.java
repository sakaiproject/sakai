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

package org.sakaiproject.tool.assessment.shared.impl.grading;

import java.util.List;
import java.util.Map;

import org.sakaiproject.tool.assessment.data.ifc.grading.AssessmentGradingIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.ItemGradingIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.MediaIfc;
import org.sakaiproject.tool.assessment.shared.api.grading.GradingServiceAPI;

/**
 * The GradingServiceAPI implements the shared interface to get grading information.
 * @author Ed Smiley <esmiley@stanford.edu>
 */

public class GradingServiceImpl implements GradingServiceAPI
{
  /**
   * Get all scores for a published assessment from the back end.
   * @return List of AssessmentGradingIfcIfs
   */
  public List getTotalScores(String publishedId, String which)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.grading.GradingServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method getTotalScores() not yet implemented.");
  }

  /**
   * Get all submissions for a published assessment from the back end.
   * @return List of AssessmentGradingIfcIfs
   */
  public List getAllSubmissions(String publishedId)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.grading.GradingServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method getAllSubmissions() not yet implemented.");
  }

  /**
   * Save the total scores.
   * @param data List of AssessmentGradingDataIfcs
   */
  public void saveTotalScores(List data)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.grading.GradingServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method saveTotalScores() not yet implemented.");
  }

  /**
   * Save the item scores.
   * @param data List of itemGradingDataIfcs
   */

  public void saveItemScores(List data)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.grading.GradingServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method saveItemScores() not yet implemented.");
  }

  /**
   * Get the score information for each item from the assessment score.
   */
  public Map getItemScores(Long publishedId, Long itemId, String which)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.grading.GradingServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method getItemScores() not yet implemented.");
  }

  /**
   * Get the last set of ItemGradingIfc for a student per assessment
   */
  public Map getLastItemGradingIfc(String publishedId, String agentId)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.grading.GradingServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method getLastItemGradingIfc() not yet implemented.");
  }

  /**
   * Get the grading data for a given submission
   */
  public Map getStudentGradingData(String assessmentGradingId)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.grading.GradingServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method getStudentGradingData() not yet implemented.");
  }

  /**
   * Get the last submission for a student per assessment
   */
  public Map getSubmitData(String publishedId, String agentId)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.grading.GradingServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method getSubmitData() not yet implemented.");
  }

  /**
   * Get the text for the type.
   * @param typeId
   * @return
   */
  public String getTextForId(Long typeId)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.grading.GradingServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method getTextForId() not yet implemented.");
  }

  /**
   * Store the grading data.
   * @param data
   */
  public void storeGrades(AssessmentGradingIfc data)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.grading.GradingServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method storeGrades() not yet implemented.");
  }

  /**
   * Get the count of published assessments.
   * @param publishedAssessmentId
   * @return
   */
  public int getSubmissionSizeOfPublishedAssessment(String publishedAssessmentId)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.grading.GradingServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method getSubmissionSizeOfPublishedAssessment() not yet implemented.");
  }

  /**
   *
   * @return
   */
  public Map getSubmissionSizeOfAllPublishedAssessments()
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.grading.GradingServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method getSubmissionSizeOfAllPublishedAssessments() not yet implemented.");
  }

  /**
   *
   * @param media
   * @param mimeType
   * @return
   */
  public Long saveMedia(byte[] media, String mimeType)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.grading.GradingServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method saveMedia() not yet implemented.");
  }

  /**
   *
   * @param mediaData
   * @return
   */
  public Long saveMedia(MediaIfc mediaData)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.grading.GradingServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method saveMedia() not yet implemented.");
  }

  /**
   *
   * @param mediaId
   * @return
   */
  public MediaIfc getMedia(String mediaId)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.grading.GradingServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method getMedia() not yet implemented.");
  }

  /**
   *
   * @param itemGradingId
   * @return
   */
  public List getMediaArray(String itemGradingId)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.grading.GradingServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method getMediaArray() not yet implemented.");
  }

  /**
   *
   * @param i
   * @return
   */
  public List getMediaArray(ItemGradingIfc i)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.grading.GradingServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method getMediaArray() not yet implemented.");
  }

  /**
   *
   * @param publishedItemId
   * @param agentId
   * @return
   */
  public ItemGradingIfc getLastItemGradingIfcByAgent(String publishedItemId, String agentId)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.grading.GradingServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method getLastItemGradingIfcByAgent() not yet implemented.");
  }

  /**
   *
   * @param assessmentGradingId
   * @param publishedItemId
   * @return
   */
  public ItemGradingIfc getItemGradingIfc(String assessmentGradingId, String publishedItemId)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.grading.GradingServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method getItemGradingIfc() not yet implemented.");
  }

/**
 * Load assessment grading information.
 * @param assessmentGradingId
 * @return
 */
  public AssessmentGradingIfc load(String assessmentGradingId)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.grading.GradingServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method load() not yet implemented.");
  }

  /**
   * Get the grading data for the last submission of this agent.
   * @param publishedAssessmentId
   * @param agentIdString
   * @return
   */
  public AssessmentGradingIfc getLastAssessmentGradingByAgentId(String publishedAssessmentId, String agentIdString)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.grading.GradingServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method getLastAssessmentGradingByAgentId() not yet implemented.");
  }

  /**
   * Save item grading information.
   * @param item
   */
  public void saveItemGrading(ItemGradingIfc item)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.grading.GradingServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method saveItemGrading() not yet implemented.");
  }

  /**
   *
   * @param assessment
   */
  public void saveOrUpdateAssessmentGrading(AssessmentGradingIfc assessment)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.grading.GradingServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method saveOrUpdateAssessmentGrading() not yet implemented.");
  }
}