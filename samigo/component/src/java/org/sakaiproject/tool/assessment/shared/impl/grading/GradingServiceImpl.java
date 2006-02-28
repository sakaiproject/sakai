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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.AssessmentGradingIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.ItemGradingIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.MediaIfc;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.shared.api.grading.GradingServiceAPI;
import org.sakaiproject.tool.assessment.shared.api.grading.GradingServiceException;

/**
 *
 * The GradingServiceAPI implements the shared interface to get grading information.
 * @author Ed Smiley <esmiley@stanford.edu>
 */

public class GradingServiceImpl implements GradingServiceAPI
{
  private static Log log = LogFactory.getLog(GradingServiceImpl.class);

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
   * @return List of AssessmentGradingIfcIfs
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
  public void saveTotalScores(List data)
  {
    try
    {
      GradingService service = new GradingService();
      ArrayList list = new ArrayList(data);
      service.saveTotalScores(list);
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
  public Map getSubmitData(String publishedId, String agentId)
  {
    try
    {
      GradingService service = new GradingService();
      return service.getSubmitData(publishedId, agentId);
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
  public void storeGrades(AssessmentGradingIfc data)
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
   * @return
   */
  public Map getSubmissionSizeOfAllPublishedAssessments()
  {
    try
    {
      GradingService service = new GradingService();
      return service.getSubmissionSizeOfAllPublishedAssessments();
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
  public Long saveMedia(MediaIfc mediaData)
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
  public MediaIfc getMedia(String mediaId)
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
  public List getMediaArray(ItemGradingIfc itemGrading)
  {
    try
    {
      GradingService service = new GradingService();
      String publishedItemId = itemGrading.getPublishedItem().getItemIdString();
      String assessmentGradingId =
        itemGrading.getAssessmentGrading().getAssessmentGradingId().toString();
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
  public ItemGradingIfc getLastItemGradingByAgent(String publishedItemId, String agentId)
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
  public ItemGradingIfc getItemGrading(String assessmentGradingId, String publishedItemId)
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
  public AssessmentGradingIfc load(String assessmentGradingId)
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
  public AssessmentGradingIfc getLastAssessmentGradingByAgentId(String publishedAssessmentId, String agentIdString)
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
   * Save item grading information.
   * @param item
   */
  public void saveItemGrading(ItemGradingIfc item)
  {
    try
    {
      GradingService service = new GradingService();
      service.saveItemGrading(item);
    }
    catch (Exception ex)
    {
      throw new GradingServiceException(ex);
    }
  }

  /**
   * Save assesment grading.
   * @param assessment
   */
  public void saveOrUpdateAssessmentGrading(AssessmentGradingIfc assessment)
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
}
