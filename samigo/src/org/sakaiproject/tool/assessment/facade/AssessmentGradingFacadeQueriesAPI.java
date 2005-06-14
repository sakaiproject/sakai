package org.sakaiproject.tool.assessment.facade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemData;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.MediaData;
import org.sakaiproject.tool.assessment.data.ifc.grading.AssessmentGradingIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.ItemGradingIfc;

public interface AssessmentGradingFacadeQueriesAPI
{

  public List getTotalScores(String publishedId, String which);

  public List getAllSubmissions(String publishedId);

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

  public HashMap getSubmitData(Long publishedId, String agentId);

  public void saveTotalScores(ArrayList data);

  public void saveItemScores(ArrayList data);

  /**
   * Assume this is a new item.
   */
  public void storeGrades(AssessmentGradingIfc data);

  /**
   * This is the big, complicated mess where we take all the items in
   * an assessment, store the grading data, auto-grade it, and update
   * everything.
   *
   * If regrade is true, we just recalculate the graded score.  If it's
   * false, we do everything from scratch.
   */
  public void storeGrades(AssessmentGradingIfc data, boolean regrade);

  /**
   * This grades multiple choice and true false questions.  Since
   * multiple choice/multiple select has a separate ItemGradingIfc for
   * each choice, they're graded the same way the single choice are.
   * Choices should be given negative score values if one wants them
   * to lose points for the wrong choice.
   */
  public float getAnswerScore(ItemGradingIfc data);

  public float getFIBScore(ItemGradingIfc data);

  public AssessmentGradingData prepareRealizedAssessment(
      PublishedAssessmentData p, Boolean forGrade);

  public Set getAllItems(Set sectionSet);

  public Set createItemGradingSet(AssessmentGradingData a, Set itemSet,
      Float totalAutoScore);

  public ItemGradingData createItemGrading(AssessmentGradingData a,
      PublishedItemData p, Set itemTextSet, Float totalAutoScore);

  public Long add(AssessmentGradingData a);

  public int getSubmissionSizeOfPublishedAssessment(Long publishedAssessmentId);

  public HashMap getSubmissionSizeOfAllPublishedAssessments();

  public Long saveMedia(byte[] media, String mimeType);

  public Long saveMedia(MediaData mediaData);

  public void removeMediaById(Long mediaId);

  public MediaData getMedia(Long mediaId);

  public ArrayList getMediaArray(Long itemGradingId);

  public ArrayList getMediaArray(ItemGradingData item);

  public ItemGradingData getLastItemGradingDataByAgent(Long publishedItemId,
      String agentId);

  public ItemGradingData getItemGradingData(Long assessmentGradingId,
      Long publishedItemId);

  public AssessmentGradingData load(Long id);
  
  public AssessmentGradingData getLastAssessmentGradingByAgentId(
      Long publishedAssessmentId, String agentIdString);
}