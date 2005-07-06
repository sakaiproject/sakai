/**********************************************************************************
* $HeadURL$
* $Id$
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

package org.sakaiproject.tool.assessment.services;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.MediaData;
import org.sakaiproject.tool.assessment.data.ifc.grading.AssessmentGradingIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.ItemGradingIfc;
import org.sakaiproject.tool.assessment.facade.TypeFacade;
import org.sakaiproject.tool.assessment.facade.TypeFacadeQueriesAPI;

/**
 * The GradingService calls the back end to get grading information from
 * the database.
 * @author Rachel Gollub <rgollub@stanford.edu>
 */
public class GradingService
{
  private static Log log = LogFactory.getLog(GradingService.class);

  /**
   * Get all scores for a published assessment from the back end.
   */
  public ArrayList getTotalScores(String publishedId, String which)
  {
    ArrayList results = null;
    try {
      results =
        (ArrayList) PersistenceService.getInstance().
           getAssessmentGradingFacadeQueries().getTotalScores(publishedId,
             which);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return results;
  }

 /**
  * Get all submissions for a published assessment from the back end.
  */
  public ArrayList getAllSubmissions(String publishedId)
  {
    ArrayList results = null;
    try {
      results =
        (ArrayList) PersistenceService.getInstance().
           getAssessmentGradingFacadeQueries().getAllSubmissions(publishedId);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return results;
  }

  public void saveTotalScores(ArrayList data)
  {
    try {
      PersistenceService.getInstance().
        getAssessmentGradingFacadeQueries().saveTotalScores(data);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void saveItemScores(ArrayList data)
  {
    try {
      PersistenceService.getInstance().
        getAssessmentGradingFacadeQueries().saveItemScores(data);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Get the score information for each item from the assessment score.
   */
  public HashMap getItemScores(Long publishedId, Long itemId, String which)
  {
    try {
      return (HashMap) PersistenceService.getInstance().
        getAssessmentGradingFacadeQueries()
          .getItemScores(publishedId, itemId, which);
    } catch (Exception e) {
      e.printStackTrace();
      return new HashMap();
    }
  }

  /**
   * Get the last set of itemgradingdata for a student per assessment
   */
  public HashMap getLastItemGradingData(String publishedId, String agentId)
  {
    try {
      return (HashMap) PersistenceService.getInstance().
        getAssessmentGradingFacadeQueries()
          .getLastItemGradingData(new Long(publishedId), agentId);
    } catch (Exception e) {
      e.printStackTrace();
      return new HashMap();
    }
  }

  /**
   * Get the grading data for a given submission
   */
  public HashMap getStudentGradingData(String assessmentGradingId)
  {
    try {
      return (HashMap) PersistenceService.getInstance().
        getAssessmentGradingFacadeQueries()
          .getStudentGradingData(assessmentGradingId);
    } catch (Exception e) {
      e.printStackTrace();
      return new HashMap();
    }
  }

  /**
   * Get the last submission for a student per assessment
   */
  public HashMap getSubmitData(String publishedId, String agentId)
  {
    try {
      return (HashMap) PersistenceService.getInstance().
        getAssessmentGradingFacadeQueries()
          .getSubmitData(new Long(publishedId), agentId);
    } catch (Exception e) {
      e.printStackTrace();
      return new HashMap();
    }
  }

  public String getTextForId(Long typeId)
  {
    TypeFacadeQueriesAPI typeFacadeQueries =
      PersistenceService.getInstance().getTypeFacadeQueries();
    TypeFacade type = typeFacadeQueries.getTypeFacadeById(typeId);
    return (type.getKeyword());
  }

  public void storeGrades(AssessmentGradingIfc data)
  {
    try {
      PersistenceService.getInstance().
        getAssessmentGradingFacadeQueries().storeGrades(data);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public int getSubmissionSizeOfPublishedAssessment(String publishedAssessmentId)
  {
    try{
      return PersistenceService.getInstance().
          getAssessmentGradingFacadeQueries()
          .getSubmissionSizeOfPublishedAssessment(new Long(
          publishedAssessmentId));
    } catch(Exception e) {
      e.printStackTrace();
      return 0;
    }
  }

  public HashMap getSubmissionSizeOfAllPublishedAssessments()  {
    return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
        getSubmissionSizeOfAllPublishedAssessments();
  }

  public Long saveMedia(byte[] media, String mimeType){
    return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
        saveMedia(media, mimeType);
  }

  public Long saveMedia(MediaData mediaData){
    return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
        saveMedia(mediaData);
  }

  public MediaData getMedia(String mediaId){
    return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
        getMedia(new Long(mediaId));
  }

  public ArrayList getMediaArray(String itemGradingId){
    return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
        getMediaArray(new Long(itemGradingId));
  }

  public ArrayList getMediaArray(ItemGradingData i){
    return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
        getMediaArray(i);
  }

  public ItemGradingData getLastItemGradingDataByAgent(String publishedItemId, String agentId)
  {
    try {
      return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
          getLastItemGradingDataByAgent(new Long(publishedItemId), agentId);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public ItemGradingData getItemGradingData(String assessmentGradingId, String publishedItemId)
  {
    try {
      return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
          getItemGradingData(new Long(assessmentGradingId), new Long(publishedItemId));
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public AssessmentGradingData load(String assessmentGradingId) {
    try{
      return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
          load(new Long(assessmentGradingId));
    }
    catch(Exception e)
    {
      log.error(e); throw new Error(e);
    }
  }

  public AssessmentGradingData getLastAssessmentGradingByAgentId(String publishedAssessmentId, String agentIdString) {
    try{
      return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
          getLastAssessmentGradingByAgentId(new Long(publishedAssessmentId), agentIdString);
    }
    catch(Exception e)
    {
      log.error(e); throw new Error(e);
    }
  }

  public void saveItemGrading(ItemGradingIfc item)
  {
    try {
      PersistenceService.getInstance().
        getAssessmentGradingFacadeQueries().saveItemGrading(item);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void saveOrUpdateAssessmentGrading(AssessmentGradingIfc assessment)
  {
    try {
      PersistenceService.getInstance().
        getAssessmentGradingFacadeQueries().saveOrUpdateAssessmentGrading(assessment);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
