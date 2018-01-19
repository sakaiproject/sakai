/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.shared.impl.grading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.MediaData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.shared.api.grading.GradingServiceAPI;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.GradingServiceException;

/**
 *
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
    try
    {
      GradingService service = new GradingService();
      return service.getTotalScores(publishedId, which);
    }
    catch (Exception ex)
    {
      throw new GradingServiceException(ex);
    }
  }

  /**
   * Get all submissions for a published assessment from the back end.
   * @return List of AssessmentGradingDataIfs
   */
  public List getAllSubmissions(String publishedId)
  {
    try
    {
      GradingService service = new GradingService();
      return service.getAllSubmissions(publishedId);
    }
    catch (Exception ex)
    {
      throw new GradingServiceException(ex);
    }
  }

  /**
   * Save the total scores.
   * @param data List of AssessmentGradingDataIfcs
   */
  public void saveTotalScores(List data, PublishedAssessmentIfc pub)
  {
    try
    {
      GradingService service = new GradingService();
      ArrayList list = new ArrayList(data);
      service.saveTotalScores(list, pub);
    }
    catch (Exception ex)
    {
      throw new GradingServiceException(ex);
    }
  }

  /**
   * Save the item scores.
   * @param data List of itemGradingDataIfcs
   */

    /*
  public void saveItemScores(List data, HashMap map, PublishedAssessmentIfc pub)
  {
    try
    {
      GradingService service = new GradingService();
      ArrayList list = new ArrayList(data);
      service.saveItemScores(list, map, pub);
    }
    catch (Exception ex)
    {
      throw new GradingServiceException(ex);
    }
  }
    */
  /**
   * Get the score information for each item from the assessment score.
   */
  public Map getItemScores(Long publishedId, Long itemId, String which)
  {
    try
    {
      GradingService service = new GradingService();
      return service.getItemScores(publishedId, itemId, which);
    }
    catch (Exception ex)
    {
      throw new GradingServiceException(ex);
    }
  }

  /**
   * Get the last set of ItemGradingIfc for a student per assessment
   */
  public Map getLastItemGrading(String publishedId, String agentId)
  {
    try
    {
      GradingService service = new GradingService();
      // note name change
      return service.getLastItemGradingData(publishedId, agentId);
    }
    catch (Exception ex)
    {
      throw new GradingServiceException(ex);
    }
  }

  /**
   * Get the grading data for a given submission
   */
  public Map getStudentGradingData(String assessmentGradingId)
  {
    try
    {
      GradingService service = new GradingService();
      return service.getStudentGradingData(assessmentGradingId);
    }
    catch (Exception ex)
    {
      throw new GradingServiceException(ex);
    }
  }

  /**
   * Get the last submission for a student per assessment
   */
  public Map getSubmitData(String publishedId, String agentId,Integer scoringoption)
  {
    try
    {
      GradingService service = new GradingService();
      return service.getSubmitData(publishedId, agentId,scoringoption, null);
    }
    catch (Exception ex)
    {
      throw new GradingServiceException(ex);
    }
  }

  /**
   * Get the text for the type.
   * @param typeId
   * @return
   */
  public String getTextForId(Long typeId)
  {
    try
    {
      GradingService service = new GradingService();
      return service.getTextForId(typeId);
    }
    catch (Exception ex)
    {
      throw new GradingServiceException(ex);
    }
  }

  /**
   * Store the grading data.
   * @param data
   */
    /*
  public void storeGrades(AssessmentGradingData data)
  {
    try
    {
      GradingService service = new GradingService();
      service.storeGrades(data);
    }
    catch (Exception ex)
    {
      throw new GradingServiceException(ex);
    }
  }
    */
  /**
   * Get the count of published assessments.
   * @param publishedAssessmentId
   * @return
   */
  public int getSubmissionSizeOfPublishedAssessment(String publishedAssessmentId)
  {
    try
    {
      GradingService service = new GradingService();
      return service.getSubmissionSizeOfPublishedAssessment(publishedAssessmentId);
    }
    catch (Exception ex)
    {
      throw new GradingServiceException(ex);
    }
  }

  /**
   *
   * @param media
   * @param mimeType
   * @return
   */
  public Long saveMedia(byte[] media, String mimeType)
  {
    try
    {
      GradingService service = new GradingService();
      return service.saveMedia(media, mimeType);
    }
    catch (Exception ex)
    {
      throw new GradingServiceException(ex);
    }
  }

  /**
   * Save media.
   * @param mediaData
   * @return
   */
  public Long saveMedia(MediaData mediaData)
  {
    try
    {
      GradingService service = new GradingService();
      return service.saveMedia(
        service.getMedia(mediaData.getMediaId().toString()));
    }
    catch (Exception ex)
    {
      throw new GradingServiceException(ex);
    }
  }

  /**
   *
   * @param mediaId
   * @return
   */
  public MediaData getMedia(String mediaId)
  {
    try
    {
      GradingService service = new GradingService();
      return service.getMedia(mediaId);
    }
    catch (Exception ex)
    {
      throw new GradingServiceException(ex);
    }
  }

  /**
   *
   * @param itemGradingId
   * @return
   */
  public List getMediaArray(String itemGradingId)
  {
    try
    {
      GradingService service = new GradingService();
      return service.getMediaArray(itemGradingId);
    }
    catch (Exception ex)
    {
      throw new GradingServiceException(ex);
    }
  }

  /**
   *
   * @param i
   * @return
   */
  public List getMediaArray(ItemGradingData itemGrading)
  {
    try
    {
      GradingService service = new GradingService();
      String publishedItemId = itemGrading.getPublishedItemId().toString();
      String assessmentGradingId =
        itemGrading.getAssessmentGradingId().toString();
      ItemGradingData gradingData = service.getItemGradingData(
        assessmentGradingId, publishedItemId);
      return service.getMediaArray(gradingData);
    }
    catch (Exception ex)
    {
      throw new GradingServiceException(ex);
    }
  }

  /**
   *
   * @param publishedItemId
   * @param agentId
   * @return
   */
  public ItemGradingData getLastItemGradingByAgent(String publishedItemId, String agentId)
  {
    try
    {
      GradingService service = new GradingService();
      return service.getLastItemGradingDataByAgent(publishedItemId, agentId);
    }
    catch (Exception ex)
    {
      throw new GradingServiceException(ex);
    }
  }

  /**
   *
   * @param assessmentGradingId
   * @param publishedItemId
   * @return
   */
  public ItemGradingData getItemGrading(String assessmentGradingId, String publishedItemId)
  {
    try
    {
      GradingService service = new GradingService();
      // note name change
      return service.getItemGradingData(
        assessmentGradingId, publishedItemId);
    }
    catch (Exception ex)
    {
      throw new GradingServiceException(ex);
    }
  }

/**
 * Load assessment grading information.
 * @param assessmentGradingId
 * @return
 */
  public AssessmentGradingData load(String assessmentGradingId)
  {
    try
    {
      GradingService service = new GradingService();
      return service.load(assessmentGradingId);
    }
    catch (Exception ex)
    {
      throw new GradingServiceException(ex);
    }
  }

  /**
   * Get the grading data for the last submission of this agent.
   * @param publishedAssessmentId
   * @param agentIdString
   * @return
   */
  public AssessmentGradingData getLastAssessmentGradingByAgentId(String publishedAssessmentId, String agentIdString)
  {
    try
    {
      GradingService service = new GradingService();
      return service.getLastAssessmentGradingByAgentId(
        publishedAssessmentId, agentIdString);
    }
    catch (Exception ex)
    {
      throw new GradingServiceException(ex);
    }
  }


  /**
   * Save assessment grading.
   * @param assessment
   */
  public void saveOrUpdateAssessmentGrading(AssessmentGradingData assessment)
  {
    try
    {
      GradingService service = new GradingService();
      service.saveOrUpdateAssessmentGrading(assessment);
    }
    catch (Exception ex)
    {
      throw new GradingServiceException(ex);
    }
  }


  public void saveItemGrading(ItemGradingData item) {
	  try {
		  GradingService service = new GradingService();
		  service.saveItemGrading(item);
	  }
	  catch (Exception e)
	  {
		  throw new GradingServiceException(e);
	  }
  }
}
