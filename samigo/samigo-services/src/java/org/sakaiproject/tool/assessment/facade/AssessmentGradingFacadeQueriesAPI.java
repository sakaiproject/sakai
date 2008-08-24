/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/component/src/java/org/sakaiproject/tool/assessment/facade/AssessmentGradingFacadeQueriesAPI.java $
 * $Id: AssessmentGradingFacadeQueriesAPI.java 9273 2006-05-10 22:34:28Z daisyf@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.tool.assessment.facade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
//import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemData;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.MediaData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.AssessmentGradingIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.ItemGradingIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.StudentGradingSummaryIfc;

public interface AssessmentGradingFacadeQueriesAPI
{

  public List getTotalScores(String publishedId, String which);

  public List getAllSubmissions(String publishedId);
  
  public List getAllAssessmentGradingData(Long publishedId);

  public HashMap getItemScores(Long publishedId, Long itemId, String which);

  /**
   * This returns a hashmap of all the latest item entries, keyed by
   * item id for easy retrieval.
   */
  public HashMap getLastItemGradingData(Long publishedId, String agentId);

  /**
   * This returns a hashmap of all the submitted items, keyed by
   * item id for easy retrieval.
   */
  public HashMap getStudentGradingData(String assessmentGradingId);

  public HashMap getSubmitData(Long publishedId, String agentId, Integer scoringoption);

  // public void saveTotalScores(ArrayList data);

    //public void saveItemScores(ArrayList data, HashMap map);

  /**
   * Assume this is a new item.
   */
  //public void storeGrades(AssessmentGradingIfc data);

  /**
   * This is the big, complicated mess where we take all the items in
   * an assessment, store the grading data, auto-grade it, and update
   * everything.
   *
   * If regrade is true, we just recalculate the graded score.  If it's
   * false, we do everything from scratch.
   */
  //public void storeGrades(AssessmentGradingIfc data, boolean regrade);

  /**
   * This grades multiple choice and true false questions.  Since
   * multiple choice/multiple select has a separate ItemGradingIfc for
   * each choice, they're graded the same way the single choice are.
   * Choices should be given negative score values if one wants them
   * to lose points for the wrong choice.
   */
  //public float getAnswerScore(ItemGradingIfc data);

  public Long add(AssessmentGradingData a);

  public int getSubmissionSizeOfPublishedAssessment(Long publishedAssessmentId);

  public HashMap getSubmissionSizeOfAllPublishedAssessments();

  public Long saveMedia(byte[] media, String mimeType);

  public Long saveMedia(MediaData mediaData);

  public void removeMediaById(Long mediaId);

  public MediaData getMedia(Long mediaId);

  public ArrayList getMediaArray(Long itemGradingId);

  public ArrayList getMediaArray(ItemGradingData item);

  public List getMediaArray(Long publishedItemId, Long agentId, String which);
  
  public ItemGradingData getLastItemGradingDataByAgent(Long publishedItemId,
      String agentId);

  public ItemGradingData getItemGradingData(Long assessmentGradingId,
      Long publishedItemId);

  public AssessmentGradingData load(Long id);

  public ItemGradingData getItemGrading(Long id);

  public AssessmentGradingIfc getLastAssessmentGradingByAgentId(
      Long publishedAssessmentId, String agentIdString);

  public AssessmentGradingData getLastSavedAssessmentGradingByAgentId(
      Long publishedAssessmentId, String agentIdString);
  
  public AssessmentGradingData getLastSubmittedAssessmentGradingByAgentId(
	      Long publishedAssessmentId, String agentIdString);
	  
  public List getLastAssessmentGradingList(Long publishedAssessmentId);

  public List getLastSubmittedAssessmentGradingList(Long publishedAssessmentId);

  public void saveItemGrading(ItemGradingIfc item);

  public void saveOrUpdateAssessmentGrading(AssessmentGradingIfc assessment);

    //public void setIsLate(AssessmentGradingIfc assessment);

  public List getAssessmentGradingIds(Long publishedItemId);

  public AssessmentGradingIfc getHighestAssessmentGrading(
      Long publishedAssessmentId, String agentId);

  public AssessmentGradingIfc getHighestSubmittedAssessmentGrading(
	      Long publishedAssessmentId, String agentId);

  public HashMap getLastAssessmentGradingByPublishedItem(Long publishedAssessmentId);

  public HashMap getHighestAssessmentGradingByPublishedItem(Long publishedAssessmentId);

  public List getHighestAssessmentGradingList(Long publishedAssessmentId);
  
  public List getHighestSubmittedAssessmentGradingList(Long publishedAssessmentId);
  
  public Set getItemGradingSet(Long assessmentGradingId);

  public HashMap getAssessmentGradingByItemGradingId(Long publishedAssessmentId);

  public void deleteAll(Collection c);

  public void saveOrUpdateAll(Collection c);

  public PublishedAssessmentIfc getPublishedAssessmentByAssessmentGradingId(Long assessmentGradingId);

  public PublishedAssessmentIfc getPublishedAssessmentByPublishedItemId(Long publishedItemId);
  
  public ArrayList getLastItemGradingDataPosition(Long assessmentGradingId, String agentId);

  public List getItemGradingIds(Long assessmentGradingId);
  
  public HashSet getItemSet(Long publishedAssessmentId, Long sectionId);
  
  public Long getTypeId(Long itemGradingId);
  
  public List getAllAssessmentGradingByAgentId(Long publishedAssessmentId, String agentIdString);
  
  public int getActualNumberRetake(Long publishedAssessmentId, String agentIdString);
  
  public HashMap getActualNumberRetakeHash(String agentIdString);
  
  public List getStudentGradingSummaryData(Long publishedAssessmentId, String agentIdString);
  
  public int getNumberRetake(Long publishedAssessmentId, String agentIdString);
  
  public HashMap getNumberRetakeHash(String agentIdString);
  
  public void saveStudentGradingSummaryData(StudentGradingSummaryIfc studentGradingSummaryData);

  public int getLateSubmissionsNumberByAgentId(Long publishedAssessmentId, String agentIdString, Date dueDate);
  
  public List getExportResponsesData(String publishedAssessmentId, boolean anonymous, String audioMessage, String fileUploadMessage, boolean showPartAndTotalScoreSpreadsheetColumns);

  public void removeUnsubmittedAssessmentGradingData(AssessmentGradingIfc data);

  public boolean getHasGradingData(Long publishedAssessmentId);
    
  public ArrayList getHasGradingDataAndHasSubmission(Long publishedAssessmentId);
  
  public String getFilename(Long itemGradingId, String agentId, String filename);

}
