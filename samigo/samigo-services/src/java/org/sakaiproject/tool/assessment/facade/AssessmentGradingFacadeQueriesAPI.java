/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.facade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingAttachment;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingAttachment;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.MediaData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.StudentGradingSummaryIfc;

public interface AssessmentGradingFacadeQueriesAPI
{

  public List getTotalScores(String publishedId, String which);

  public List getTotalScores(String publishedId, String which, boolean getSubmittedOnly);
  
  /**
   * Get all submissions that are flagged for grading
   * @param publishedId the published assesment id
   * @return
   */
  public List getAllSubmissions(String publishedId);
  
  public List getAllAssessmentGradingData(Long publishedId);

  /**
   * Get all answers for a a particular published item
   * This is needed by certain question types like EMI
   * @param assesmentGradingId
   * @param publishedItemId
   * @return an list of all the items or an empty list if none
   */
  public List<ItemGradingData> getAllItemGradingDataForItemInGrading(final Long assesmentGradingId, final Long publishedItemId);
  
  public HashMap getItemScores(Long publishedId, Long itemId, String which);
  
  public HashMap getItemScores(Long publishedId, Long itemId, String which, boolean loadItemGradingAttachment);

  public HashMap getItemScores(final Long itemId, List scores, boolean loadItemGradingAttachment);
  
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

  public HashMap getSubmitData(Long publishedId, String agentId, Integer scoringoption, Long assessmentGradingId);
  
  // public void saveTotalScores(ArrayList data);

    //public void saveItemScores(ArrayList data, HashMap map);

  /**
   * Assume this is a new item.
   */
  //public void storeGrades(AssessmentGradingData data);

  /**
   * This is the big, complicated mess where we take all the items in
   * an assessment, store the grading data, auto-grade it, and update
   * everything.
   *
   * If regrade is true, we just recalculate the graded score.  If it's
   * false, we do everything from scratch.
   */
  //public void storeGrades(AssessmentGradingData data, boolean regrade);

  /**
   * This grades multiple choice and true false questions.  Since
   * multiple choice/multiple select has a separate ItemGradingIfc for
   * each choice, they're graded the same way the single choice are.
   * Choices should be given negative score values if one wants them
   * to lose points for the wrong choice.
   */
  //public double getAnswerScore(ItemGradingIfc data);

  public Long add(AssessmentGradingData a);

  public int getSubmissionSizeOfPublishedAssessment(Long publishedAssessmentId);

  public Long saveMedia(byte[] media, String mimeType);

  public Long saveMedia(MediaData mediaData);

  public void removeMediaById(Long mediaId);
  
  public void removeMediaById(Long mediaId, Long itemGradingId);

  public MediaData getMedia(Long mediaId);

  public ArrayList getMediaArray(Long itemGradingId);
  
  public ArrayList getMediaArray2(Long itemGradingId);

  public ArrayList getMediaArray(ItemGradingData item);

  public HashMap getMediaItemGradingHash(Long assessmentGradingId);
  
  public List getMediaArray(Long publishedItemId, Long agentId, String which);

  /** Get a batch of IDs for Media objects that have blobs in the database */
  public List<Long> getMediaConversionBatch();

  /** Sanity check query for Media objects with conflicting state of holding a blob and location */
  public List<Long> getMediaWithDataAndLocation();

  /** Sanity check for Media objects left in the converting state */
  public List<Long> getMediaInConversion();

  /** Mark a list of Media objects as being converted */
  public boolean markMediaForConversion(List<Long> mediaIds);
  
  public ItemGradingData getLastItemGradingDataByAgent(Long publishedItemId,
      String agentId);

  public ItemGradingData getItemGradingData(Long assessmentGradingId);
  public ItemGradingData getItemGradingData(Long assessmentGradingId, Long publishedItemId);

  public AssessmentGradingData load(Long id);
  
  public AssessmentGradingData load(Long id, boolean loadGradingAttachment);

  public ItemGradingData getItemGrading(Long id);

  public AssessmentGradingData getLastAssessmentGradingByAgentId(
      Long publishedAssessmentId, String agentIdString);

  public AssessmentGradingData getLastSavedAssessmentGradingByAgentId(
      Long publishedAssessmentId, String agentIdString);
  
  public AssessmentGradingData getLastSubmittedAssessmentGradingByAgentId(
	      Long publishedAssessmentId, String agentIdString, Long assessmentGradingId);
	  
  public List getLastAssessmentGradingList(Long publishedAssessmentId);

  public List getLastSubmittedAssessmentGradingList(Long publishedAssessmentId);
  
  public List getLastSubmittedOrGradedAssessmentGradingList(Long publishedAssessmentId);

  public void saveItemGrading(ItemGradingData item);

  public void saveOrUpdateAssessmentGrading(AssessmentGradingData assessment);

    //public void setIsLate(AssessmentGradingData assessment);

  public List getAssessmentGradingIds(Long publishedItemId);

  public AssessmentGradingData getHighestAssessmentGrading(
      Long publishedAssessmentId, String agentId);

  public AssessmentGradingData getHighestSubmittedAssessmentGrading(
		  Long publishedAssessmentId, String agentId, Long assessmentGradingId);

  public HashMap getLastAssessmentGradingByPublishedItem(Long publishedAssessmentId);

  public HashMap getHighestAssessmentGradingByPublishedItem(Long publishedAssessmentId);

  public List getHighestAssessmentGradingList(Long publishedAssessmentId);
  
  public List getHighestSubmittedOrGradedAssessmentGradingList(Long publishedAssessmentId);
  
  public Set getItemGradingSet(Long assessmentGradingId);

  public HashMap getAssessmentGradingByItemGradingId(Long publishedAssessmentId);

  public void deleteAll(Collection c);

  public void saveOrUpdateAll(Collection<ItemGradingData> c);

  public PublishedAssessmentIfc getPublishedAssessmentByAssessmentGradingId(Long assessmentGradingId);

  public PublishedAssessmentIfc getPublishedAssessmentByPublishedItemId(Long publishedItemId);
  
  public ArrayList getLastItemGradingDataPosition(Long assessmentGradingId, String agentId);

  public List getPublishedItemIds(Long assessmentGradingId);
  
  public List getItemGradingIds(Long assessmentGradingId);
  
  public HashSet getItemSet(Long publishedAssessmentId, Long sectionId);
  
  public Long getTypeId(Long itemGradingId);
  
  public List getAllAssessmentGradingByAgentId(Long publishedAssessmentId, String agentIdString);
  
  public HashMap getSiteSubmissionCountHash(String siteId);
  
  public HashMap getSiteInProgressCountHash(String siteId) ;
  
  public int getActualNumberRetake(Long publishedAssessmentId, String agentIdString);
  
  public HashMap getActualNumberRetakeHash(String agentIdString);
  
  public HashMap getSiteActualNumberRetakeHash(String siteIdString);
  
  public List getStudentGradingSummaryData(Long publishedAssessmentId, String agentIdString);
  
  public int getNumberRetake(Long publishedAssessmentId, String agentIdString);
  
  public HashMap getNumberRetakeHash(String agentIdString);
  
  public HashMap getSiteNumberRetakeHash(String siteIdString);
  
  public void saveStudentGradingSummaryData(StudentGradingSummaryIfc studentGradingSummaryData);

  public int getLateSubmissionsNumberByAgentId(Long publishedAssessmentId, String agentIdString, Date dueDate);
  
  public List getExportResponsesData(String publishedAssessmentId, boolean anonymous, String audioMessage, String fileUploadMessage, String noSubmissionMessage, boolean showPartAndTotalScoreSpreadsheetColumns, String poolString, String sectionString, String questionString, String textString, String rationaleString, String itemGradingCommentsString, Map useridMap, String responseCommentString);
  
  public boolean getHasGradingData(Long publishedAssessmentId);

  public void removeUnsubmittedAssessmentGradingData(AssessmentGradingData data);
    
  public ArrayList getHasGradingDataAndHasSubmission(Long publishedAssessmentId);
  
  
  public String getFilename(Long itemGradingId, String agentId, String filename);

  public List getUpdatedAssessmentList(String agentId, String siteId);
  
  public List getSiteNeedResubmitList(String siteId);
  
  public void autoSubmitAssessments();
  
  public ItemGradingAttachment createItemGradingtAttachment(ItemGradingData itemGrading, String resourceId, String filename, String protocol);
  
  public AssessmentGradingAttachment createAssessmentGradingtAttachment(AssessmentGradingData assessmentGrading, String resourceId, String filename, String protocol);
  
  public void removeItemGradingAttachment(Long attachmentId);

  public void saveOrUpdateAttachments(List<AttachmentIfc> list);

  public void removeAssessmentGradingAttachment(Long attachmentId);
  
  public HashMap getInProgressCounts(String siteId);

  public HashMap getSubmittedCounts(String siteId);

  public void completeItemGradingData(AssessmentGradingData assessmentGradingData);	
  
  public List getHighestSubmittedAssessmentGradingList(final Long publishedAssessmentId);
  public Double getAverageSubmittedAssessmentGrading( final Long publishedAssessmentId, final String agentId);
  public HashMap getAverageAssessmentGradingByPublishedItem(Long publishedAssessmentId);
  
  public List getUnSubmittedAssessmentGradingDataList(Long publishedAssessmentId, String agentIdString);

}
