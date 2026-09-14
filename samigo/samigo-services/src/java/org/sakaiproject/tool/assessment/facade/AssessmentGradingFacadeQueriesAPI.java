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

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedSectionData;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingAttachment;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingAttachment;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.MediaData;
import org.sakaiproject.tool.assessment.data.dao.grading.SectionGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.StudentGradingSummaryData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.StudentGradingSummaryIfc;

public interface AssessmentGradingFacadeQueriesAPI
{

  public List<AssessmentGradingData> getTotalScores(Long publishedId, String which, boolean getSubmittedOnly);
  
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
  
  public Map<Long, List<ItemGradingData>> getItemScores(Long publishedId, Long itemId, String which);
  
  public Map<Long, List<ItemGradingData>> getItemScores(Long publishedId, Long itemId, String which, boolean loadItemGradingAttachment);

  public Map<Long, List<ItemGradingData>> getItemScores(final Long itemId, List<AssessmentGradingData> scores, boolean loadItemGradingAttachment);
  
  /**
   * This returns a hashmap of all the latest item entries, keyed by
   * item id for easy retrieval.
   */
  public Map<Long, List<ItemGradingData>> getLastItemGradingData(Long publishedId, String agentId);

  /**
   * Retrieves all item grading data for a specific assessment submission.
   * <p>
   * This method returns a mapping of published item IDs to their corresponding grading data,
   * allowing efficient lookup of grading information for individual items within an assessment.
   * </p>
   *
   * @param assessmentGradingId the unique identifier of the assessment grading record as a String
   * @return a Map where each key is a published item ID (Long) and each value is a List of
   *         ItemGradingData objects associated with that item. Returns an empty map if no
   *         grading data exists for the specified assessment.
   */
  Map<Long, List<ItemGradingData>> getStudentGradingData(String assessmentGradingId);

  public Map<Long, List<ItemGradingData>> getSubmitData(Long publishedId, String agentId, Integer scoringoption, Long assessmentGradingId);
  
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

  public List<MediaData> getMediaArray(Long itemGradingId);
  
  public List<MediaData> getMediaArray2(Long itemGradingId);

  public List<MediaData> getMediaArray(ItemGradingData item);

  public Map<Long, List<ItemGradingData>> getMediaItemGradingHash(Long assessmentGradingId);
  
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

  public boolean saveOrUpdateAssessmentGrading(AssessmentGradingData assessment);

    //public void setIsLate(AssessmentGradingData assessment);

  public List<Long> getAssessmentGradingIds(Long publishedItemId);

  public AssessmentGradingData getHighestAssessmentGrading(
      Long publishedAssessmentId, String agentId);

  public AssessmentGradingData getHighestSubmittedAssessmentGrading(
		  Long publishedAssessmentId, String agentId, Long assessmentGradingId);

  public Map<Long, List<Long>> getLastAssessmentGradingByPublishedItem(Long publishedAssessmentId);

  public Map<Long, List<Long>> getHighestAssessmentGradingByPublishedItem(Long publishedAssessmentId);

  public List getHighestAssessmentGradingList(Long publishedAssessmentId);
  
  public List getHighestSubmittedOrGradedAssessmentGradingList(Long publishedAssessmentId);
  
  public Set getItemGradingSet(Long assessmentGradingId);

  public Map<Long, AssessmentGradingData> getAssessmentGradingByItemGradingId(Long publishedAssessmentId);

  public void deleteAll(Collection c);

  public void saveOrUpdateAll(Collection<ItemGradingData> c);

  public PublishedAssessmentIfc getPublishedAssessmentByAssessmentGradingId(Long assessmentGradingId);

  public PublishedAssessmentIfc getPublishedAssessmentByPublishedItemId(Long publishedItemId);
  
  public List<Integer> getLastItemGradingDataPosition(Long assessmentGradingId, String agentId);

  public List getPublishedItemIds(Long assessmentGradingId);
  
  public List getItemGradingIds(Long assessmentGradingId);
  
  public Set<PublishedItemData> getItemSet(Long publishedAssessmentId, Long sectionId);
  
  public Long getTypeId(Long itemGradingId);
  
  public List getAllAssessmentGradingByAgentId(Long publishedAssessmentId, String agentIdString);
  
  public Map<Long, Map<String, Integer>> getSiteSubmissionCountHash(String siteId);
  
  public Map<Long, Map<String, Long>> getSiteInProgressCountHash(String siteId) ;
  
  public int getActualNumberRetake(Long publishedAssessmentId, String agentIdString);
  
  public Map<Long, Integer> getActualNumberRetakeHash(String agentIdString);
  
  public Map<Long, Map<String, Long>> getSiteActualNumberRetakeHash(String siteIdString);
  
  public List getStudentGradingSummaryData(Long publishedAssessmentId, String agentIdString);
  
  public int getNumberRetake(Long publishedAssessmentId, String agentIdString);
  
  public Map<Long, StudentGradingSummaryData> getNumberRetakeHash(String agentIdString);
  
  public Map<Long, Map<String, Integer>> getSiteNumberRetakeHash(String siteIdString);
  
  public void saveStudentGradingSummaryData(StudentGradingSummaryIfc studentGradingSummaryData);

  public int getLateSubmissionsNumberByAgentId(Long publishedAssessmentId, String agentIdString, Date dueDate);

  /**
   * Exports assessment response data for a published assessment in a structured format suitable for spreadsheet export.
   * <p>
   * This method retrieves detailed response information for all submissions of a published assessment,
   * including student responses, scores, rationales, and comments. The data is formatted using the provided
   * string parameters for column headers and includes support for anonymous grading.
   * </p>
   *
   * @param publishedAssessmentId the unique identifier of the published assessment as a String
   * @param anonymous if true, student identifying information is excluded from the export
   * @param audioMessage the localized label for audio response columns
   * @param fileUploadMessage the localized label for file upload response columns
   * @param noSubmissionMessage the localized label displayed when no response was submitted
   * @param showPartAndTotalScoreSpreadsheetColumns if true, includes part-level and total score columns in the export
   * @param poolString the localized column header for question pool information
   * @param partString the localized column header for assessment part/section information
   * @param questionString the localized column header for question text
   * @param textString the localized column header for text-based responses
   * @param responseString the localized column header for student responses
   * @param pointsString the localized column header for points/scores
   * @param rationaleString the localized column header for student rationales
   * @param itemGradingCommentsString the localized column header for instructor comments on individual items
   * @param useridMap a mapping of user IDs to EnrollmentRecord objects for retrieving student enrollment information
   * @param responseCommentString the localized column header for comments on responses
   * @return a Map keyed by {@link ExportSection}, never {@code null} and containing exactly the
   *         {@code HEADER} and {@code ROWS} keys; {@code SHEET_BREAK} is never present, as sheet
   *         boundaries are decided by the caller rather than here. {@code HEADER} maps to a
   *         single-row list containing exactly one row of column header cells, and {@code ROWS}
   *         maps to zero or more data rows, each row being a list of cells.
   */
  Map<ExportSection, List<List<CellValue<?>>>> getExportResponsesData(String publishedAssessmentId, boolean anonymous, String audioMessage, String fileUploadMessage, String noSubmissionMessage, boolean showPartAndTotalScoreSpreadsheetColumns, String poolString, String partString, String questionString, String textString, String responseString, String pointsString, String rationaleString, String itemGradingCommentsString, Map<String, EnrollmentRecord> useridMap, String responseCommentString);

  /**
   * Exports assessment response data for a published assessment, with support for per-item
   * correct/incorrect/empty answer tallies for single-selection questions.
   * <p>
   * This overloaded method extends the basic export functionality by adding, for each row, three
   * counter cells tracking how many single-selection answers were correct, incorrect, or left empty.
   * Only the data cells are added here; the callers supply the matching column headers. All question
   * types are still included in the export.
   * </p>
   *
   * @param publishedAssessmentId the unique identifier of the published assessment as a String
   * @param anonymous if true, student identifying information is excluded from the export
   * @param audioMessage the localized label for audio response columns
   * @param fileUploadMessage the localized label for file upload response columns
   * @param noSubmissionMessage the localized label displayed when no response was submitted
   * @param showPartAndTotalScoreSpreadsheetColumns if true, includes part-level and total score columns in the export
   * @param poolString the localized column header for question pool information
   * @param partString the localized column header for assessment part/section information
   * @param questionString the localized column header for question text
   * @param textString the localized column header for text-based responses
   * @param responseString the localized column header for student responses
   * @param pointsString the localized column header for points/scores
   * @param rationaleString the localized column header for student rationales
   * @param itemGradingCommentsString the localized column header for instructor comments on individual items
   * @param useridMap a mapping of user IDs to EnrollmentRecord objects for retrieving student enrollment information
   * @param responseCommentString the localized column header for comments on responses
   * @param isOneSelectionType if true, adds correct/incorrect/empty answer counter cells for
   *                           single-selection questions to each row; does not filter which question
   *                           types are included, and does not add the matching column headers
   * @return a Map keyed by {@link ExportSection}, never {@code null} and containing exactly the
   *         {@code HEADER} and {@code ROWS} keys; {@code SHEET_BREAK} is never present, as sheet
   *         boundaries are decided by the caller rather than here. {@code HEADER} maps to a
   *         single-row list containing exactly one row of column header cells, and {@code ROWS}
   *         maps to zero or more data rows, each row being a list of cells.
   */
  Map<ExportSection, List<List<CellValue<?>>>> getExportResponsesData(String publishedAssessmentId, boolean anonymous, String audioMessage, String fileUploadMessage, String noSubmissionMessage, boolean showPartAndTotalScoreSpreadsheetColumns, String poolString, String partString, String questionString, String textString, String responseString, String pointsString, String rationaleString, String itemGradingCommentsString, Map<String, EnrollmentRecord> useridMap, String responseCommentString, boolean isOneSelectionType);
  
  public boolean getHasGradingData(Long publishedAssessmentId);

  public void removeUnsubmittedAssessmentGradingData(AssessmentGradingData data);

  public void removeAssessmentGradingData(AssessmentGradingData data);

  public List<Boolean> getHasGradingDataAndHasSubmission(Long publishedAssessmentId);
  
  
  public String getFilename(Long itemGradingId, String agentId, String filename);

  public List<Set<Long>> getUpdatedAssessmentList(String agentId, String siteId);
  
  public List getSiteNeedResubmitList(String siteId);
  
  /**
   * Checks for assessment attempts that should be autosubmitted
   * @return number of attempts/submissions that could not be processed due to error
   */
  public int autoSubmitAssessments();
  
  public ItemGradingAttachment createItemGradingtAttachment(ItemGradingData itemGrading, String resourceId, String filename, String protocol);
  
  public AssessmentGradingAttachment createAssessmentGradingtAttachment(AssessmentGradingData assessmentGrading, String resourceId, String filename, String protocol);
  
  public void removeItemGradingAttachment(Long attachmentId);

  public void saveOrUpdateAttachments(List<AttachmentIfc> list);

  public void removeAssessmentGradingAttachment(Long attachmentId);
  
  public HashMap getInProgressCounts(String siteId);

  public HashMap getSubmittedCounts(String siteId);

  public void completeItemGradingData(AssessmentGradingData assessmentGradingData);

  public void completeItemGradingData(AssessmentGradingData assessmentGradingData, Map<Long, Set<PublishedSectionData>> sectionSetMap);

  public List getHighestSubmittedAssessmentGradingList(final Long publishedAssessmentId);
  public Double getAverageSubmittedAssessmentGrading( final Long publishedAssessmentId, final String agentId);
  public Map<Long, List<Long>> getAverageAssessmentGradingByPublishedItem(Long publishedAssessmentId);
  
  public List getUnSubmittedAssessmentGradingDataList(Long publishedAssessmentId, String agentIdString);

  public SectionGradingData getSectionGradingData(Long assessmentGradingId, Long sectionId, String agentId);
  public void saveSectionGrading(SectionGradingData item);
}
