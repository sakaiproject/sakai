/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/component/src/java/org/sakaiproject/tool/assessment/services/GradingService.java $
 * $Id: GradingService.java 9784 2006-05-22 19:33:28Z daisyf@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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


package org.sakaiproject.tool.assessment.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.spring.SpringBeanLocator;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.MediaData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.AssessmentGradingIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.ItemGradingAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.ItemGradingIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.StudentGradingSummaryIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.facade.GradebookFacade;
import org.sakaiproject.tool.assessment.facade.TypeFacade;
import org.sakaiproject.tool.assessment.facade.TypeFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.integration.context.IntegrationContextFactory;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookServiceHelper;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;

/**
 * The GradingService calls the back end to get/store grading information. 
 * It also calculates scores for autograded types.
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
        new ArrayList(PersistenceService.getInstance().
           getAssessmentGradingFacadeQueries().getTotalScores(publishedId,
             which));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return results;
  }
  
  public ArrayList getTotalScores(String publishedId, String which, boolean getSubmittedOnly)
  {
    ArrayList results = null;
    try {
      results =
        new ArrayList(PersistenceService.getInstance().
           getAssessmentGradingFacadeQueries().getTotalScores(publishedId,
             which, getSubmittedOnly));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return results;
  }
  
 /**
  * Get all submissions for a published assessment from the back end.
  */
  public List getAllSubmissions(String publishedId)
  {
    List results = null;
    try {
      results = PersistenceService.getInstance().
           getAssessmentGradingFacadeQueries().getAllSubmissions(publishedId);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return results;
  }
  
  public List getAllAssessmentGradingData(Long publishedId)
  {
    List results = null;
    try {
      results = PersistenceService.getInstance().
           getAssessmentGradingFacadeQueries().getAllAssessmentGradingData(publishedId);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return results;
  }

  public ArrayList getHighestAssessmentGradingList(Long publishedId)
  {
    ArrayList results = null;
    try {
      results =
        new ArrayList(PersistenceService.getInstance().
           getAssessmentGradingFacadeQueries().getHighestAssessmentGradingList(publishedId));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return results;
  }
  
  public List getHighestSubmittedOrGradedAssessmentGradingList(Long publishedId)
  {
    ArrayList results = null;
    try {
      results =
        new ArrayList(PersistenceService.getInstance().
           getAssessmentGradingFacadeQueries().getHighestSubmittedOrGradedAssessmentGradingList(publishedId));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return results;
  }

  public ArrayList getLastAssessmentGradingList(Long publishedId)
  {
    ArrayList results = null;
    try {
      results =
        new ArrayList(PersistenceService.getInstance().
           getAssessmentGradingFacadeQueries().getLastAssessmentGradingList(publishedId));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return results;
  }

  public List getLastSubmittedAssessmentGradingList(Long publishedId)
  {
    List results = null;
    try {
      results = 
    	  PersistenceService.getInstance().
           getAssessmentGradingFacadeQueries().getLastSubmittedAssessmentGradingList(publishedId);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return results;
  }
  
  public List getLastSubmittedOrGradedAssessmentGradingList(Long publishedId)
  {
    List results = null;
    try {
      results = 
    	  PersistenceService.getInstance().
           getAssessmentGradingFacadeQueries().getLastSubmittedOrGradedAssessmentGradingList(publishedId);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return results;
  }
  
  public void saveTotalScores(ArrayList gdataList, PublishedAssessmentIfc pub)
  {
      //log.debug("**** GradingService: saveTotalScores");
    try {
     AssessmentGradingData gdata = null;
      if (gdataList.size()>0)
        gdata = (AssessmentGradingData) gdataList.get(0);
      else return;

      Integer scoringType = getScoringType(pub);
      ArrayList oldList = getAssessmentGradingsByScoringType(
          scoringType, gdata.getPublishedAssessmentId());
      for (int i=0; i<gdataList.size(); i++){
        AssessmentGradingData ag = (AssessmentGradingData)gdataList.get(i);
        saveOrUpdateAssessmentGrading(ag);
        EventTrackingService.post(EventTrackingService.newEvent("sam.total.score.update", 
        		"gradedBy=" + AgentFacade.getAgentString() + 
        		", assessmentGradingId=" + ag.getAssessmentGradingId() + 
          		", totalAutoScore=" + ag.getTotalAutoScore() + 
          		", totalOverrideScore=" + ag.getTotalOverrideScore() + 
          		", FinalScore=" + ag.getFinalScore() + 
          		", comments=" + ag.getComments() , true));
      }

      // no need to notify gradebook if this submission is not for grade
      // we only want to notify GB when there are changes
      ArrayList newList = getAssessmentGradingsByScoringType(
        scoringType, gdata.getPublishedAssessmentId());
      ArrayList l = getListForGradebookNotification(newList, oldList);
      
      notifyGradebook(l, pub);
      //}
    } catch (GradebookServiceException ge) {
      ge.printStackTrace();
      throw ge;
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }


  }


  private ArrayList getListForGradebookNotification(
       ArrayList newList, ArrayList oldList){
    ArrayList l = new ArrayList();
    HashMap h = new HashMap();
    for (int i=0; i<oldList.size(); i++){
      AssessmentGradingData ag = (AssessmentGradingData)oldList.get(i);
      h.put(ag.getAssessmentGradingId(), ag);
    }

    for (int i=0; i<newList.size(); i++){
      AssessmentGradingData a = (AssessmentGradingData) newList.get(i);
      Object o = h.get(a.getAssessmentGradingId());
      if (o == null){ // this does not exist in old list, so include it for update
        l.add(a);
      }
      else{ // if new is different from old, include it for update
        AssessmentGradingData b = (AssessmentGradingData) o;
        if ((a.getFinalScore()!=null && b.getFinalScore()!=null) 
            && !a.getFinalScore().equals(b.getFinalScore()))
          l.add(a);
      }
    }
    return l;
  }

  public ArrayList getAssessmentGradingsByScoringType(
       Integer scoringType, Long publishedAssessmentId){
    List l = null;
    // get the list of highest score
    if ((scoringType).equals(EvaluationModelIfc.HIGHEST_SCORE)){
      l = getHighestSubmittedOrGradedAssessmentGradingList(publishedAssessmentId);
    }
    // get the list of last score
    else {
      l = getLastSubmittedOrGradedAssessmentGradingList(publishedAssessmentId);
    }
    return new ArrayList(l);
  }

  public Integer getScoringType(PublishedAssessmentIfc pub){
    Integer scoringType = null;
    EvaluationModelIfc e = pub.getEvaluationModel();
    if ( e!=null ){
      scoringType = e.getScoringType();
    }
    return scoringType;
  }

  private boolean updateGradebook(AssessmentGradingIfc data, PublishedAssessmentIfc pub){
    // no need to notify gradebook if this submission is not for grade
    boolean forGrade = (Boolean.TRUE).equals(data.getForGrade());

    boolean toGradebook = false;
    EvaluationModelIfc e = pub.getEvaluationModel();
    if ( e!=null ){
      String toGradebookString = e.getToGradeBook();
      toGradebook = toGradebookString.equals(EvaluationModelIfc.TO_DEFAULT_GRADEBOOK.toString());
    }
    return (forGrade && toGradebook);
  }

  private void notifyGradebook(ArrayList l, PublishedAssessmentIfc pub){
    for (int i=0; i<l.size(); i++){
      notifyGradebook((AssessmentGradingData)l.get(i), pub);
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

  public HashMap getItemScores(Long itemId, List scores)
  {
    try {
      return (HashMap) PersistenceService.getInstance().
        getAssessmentGradingFacadeQueries()
          .getItemScores(itemId, scores);
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
  public HashMap getSubmitData(String publishedId, String agentId, Integer scoringoption)
  {
    try {
      return (HashMap) PersistenceService.getInstance().
        getAssessmentGradingFacadeQueries()
          .getSubmitData(new Long(publishedId), agentId, scoringoption);
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

  public HashMap getAGDataSizeOfAllPublishedAssessments()  {
      return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
          getAGDataSizeOfAllPublishedAssessments();
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
  
  public ArrayList getMediaArray2(String itemGradingId){
	    return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
	        getMediaArray2(new Long(itemGradingId));
  }

  public ArrayList getMediaArray(ItemGradingData i){
    return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
        getMediaArray(i);
  }

  public List getMediaArray(String publishedId, String publishItemId, String which){
	    return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
	    getMediaArray(new Long(publishedId), new Long(publishItemId), which);
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
      log.error(e); throw new RuntimeException(e);
    }
  }

  public ItemGradingData getItemGrading(String itemGradingId) {
    try{
      return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
          getItemGrading(new Long(itemGradingId));
    }
    catch(Exception e)
    {
      log.error(e); throw new Error(e);
    }
  }

  public AssessmentGradingIfc getLastAssessmentGradingByAgentId(String publishedAssessmentId, String agentIdString) {
    try{
      return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
          getLastAssessmentGradingByAgentId(new Long(publishedAssessmentId), agentIdString);
    }
    catch(Exception e)
    {
      log.error(e); throw new RuntimeException(e);
    }
  }

  public AssessmentGradingData getLastSavedAssessmentGradingByAgentId(String publishedAssessmentId, String agentIdString) {
    try{
      return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
          getLastSavedAssessmentGradingByAgentId(new Long(publishedAssessmentId), agentIdString);
    }
    catch(Exception e)
    {
      log.error(e); throw new RuntimeException(e);
    }
  }

  public AssessmentGradingData getLastSubmittedAssessmentGradingByAgentId(String publishedAssessmentId, String agentIdString, Long assessmentGradingId) {
	    try{
	      return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
	      getLastSubmittedAssessmentGradingByAgentId(new Long(publishedAssessmentId), agentIdString, assessmentGradingId);
	    }
	    catch(Exception e)
	    {
	      log.error(e); throw new RuntimeException(e);
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
      /*	
      // Comment out the whole IF section because the only thing we do here is to 
      // update the itemGradingSet. However, this update is redundant as it will
      // be updated in saveOrUpdateAssessmentGrading(assessment).
      if (assessment.getAssessmentGradingId()!=null 
          && assessment.getAssessmentGradingId().longValue()>0){
	    //1. if assessmentGrading contain itemGrading, we want to insert/update itemGrading first
        Set itemGradingSet = assessment.getItemGradingSet(); 
        Iterator iter = itemGradingSet.iterator();
        while (iter.hasNext()) {
        	ItemGradingData itemGradingData = (ItemGradingData) iter.next();
        	log.debug("date = " + itemGradingData.getSubmittedDate());
        }
        // The following line seems redundant. I cannot see a reason why we need to save the itmeGradingSet
        // here and then again in following saveOrUpdateAssessmentGrading(assessment). Comment it out.    
	    //saveOrUpdateAll(itemGradingSet);
      }
      */
      // this will update itemGradingSet and assessmentGrading. May as well, otherwise I would have
      // to reload assessment again
      PersistenceService.getInstance().
        getAssessmentGradingFacadeQueries().saveOrUpdateAssessmentGrading(assessment);
    } catch (Exception e) {
      e.printStackTrace();
    }

  }
  
  // This API only touch SAM_ASSESSMENTGRADING_T. No data gets inserted/updated in SAM_ITEMGRADING_T
  public void saveOrUpdateAssessmentGradingOnly(AssessmentGradingIfc assessment)
  {
	  Set origItemGradingSet = assessment.getItemGradingSet();
	  HashSet h = new HashSet(origItemGradingSet);
	  
	  // Clear the itemGradingSet so no data gets inserted/updated in SAM_ITEMGRADING_T;
	  origItemGradingSet.clear();
      int size = assessment.getItemGradingSet().size();
      log.debug("before persist to db: size = " + size);
      try {
    	  PersistenceService.getInstance().getAssessmentGradingFacadeQueries().saveOrUpdateAssessmentGrading(assessment);
      } catch (Exception e) {
    	  e.printStackTrace();
      }
      finally {
    	  // Restore the original itemGradingSet back
    	  assessment.setItemGradingSet(h);
    	  size = assessment.getItemGradingSet().size();
		  log.debug("after persist to db: size = " + size);
      }
  }

  public List getAssessmentGradingIds(String publishedItemId){
    try{
      return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
         getAssessmentGradingIds(new Long(publishedItemId));
    }
    catch(Exception e)
    {
      log.error(e); throw new RuntimeException(e);
    }
  }

  public AssessmentGradingIfc getHighestAssessmentGrading(String publishedAssessmentId, String agentId){
    try{
      return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
	      getHighestAssessmentGrading(new Long(publishedAssessmentId), agentId);
    }
    catch(Exception e)
    {
      log.error(e); throw new RuntimeException(e);
    }
  }

  public AssessmentGradingIfc getHighestSubmittedAssessmentGrading(String publishedAssessmentId, String agentId, Long assessmentGradingId){
	    try{
	      return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
	      getHighestSubmittedAssessmentGrading(new Long(publishedAssessmentId), agentId, assessmentGradingId);
	    }
	    catch(Exception e)
	    {
	      log.error(e); throw new RuntimeException(e);
	    }
	  }
  
  public AssessmentGradingIfc getHighestSubmittedAssessmentGrading(String publishedAssessmentId, String agentId){
	  return getHighestSubmittedAssessmentGrading(publishedAssessmentId, agentId, new Long(0));
  }
  
  public Set getItemGradingSet(String assessmentGradingId){
    try{
      return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
               getItemGradingSet(new Long(assessmentGradingId));
    }
    catch(Exception e){
      log.error(e); throw new RuntimeException(e);
    }
  }

  public HashMap getAssessmentGradingByItemGradingId(String publishedAssessmentId){
    try{
      return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
               getAssessmentGradingByItemGradingId(new Long(publishedAssessmentId));
    }
    catch(Exception e){
      log.error(e); throw new RuntimeException(e);
    }
  }

  public void updateItemScore(ItemGradingData gdata, float scoreDifference, PublishedAssessmentIfc pub){
    try {
      AssessmentGradingData adata = load(gdata.getAssessmentGradingId().toString());
      adata.setItemGradingSet(getItemGradingSet(adata.getAssessmentGradingId().toString()));

      Set itemGradingSet = adata.getItemGradingSet();
      Iterator iter = itemGradingSet.iterator();
      float totalAutoScore = 0;
      float totalOverrideScore = adata.getTotalOverrideScore().floatValue();
      while (iter.hasNext()){
        ItemGradingIfc i = (ItemGradingIfc)iter.next();
        if (i.getItemGradingId().equals(gdata.getItemGradingId())){
	  i.setAutoScore(gdata.getAutoScore());
          i.setComments(gdata.getComments());
          i.setGradedBy(AgentFacade.getAgentString());
          i.setGradedDate(new Date());
	}
        if (i.getAutoScore()!=null)
          totalAutoScore += i.getAutoScore().floatValue();
      }
      
      adata.setTotalAutoScore( Float.valueOf(totalAutoScore));
      if (Float.compare((totalAutoScore+totalOverrideScore),Float.valueOf("0").floatValue())<0){
    	  adata.setFinalScore(Float.valueOf("0"));
      }else{
    	  adata.setFinalScore(Float.valueOf(totalAutoScore+totalOverrideScore));
      }
      saveOrUpdateAssessmentGrading(adata);
      if (scoreDifference != 0){
        notifyGradebookByScoringType(adata, pub);
      }
    } catch (GradebookServiceException ge) {
      ge.printStackTrace();
      throw ge;
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  /**
   * Assume this is a new item.
   */
  public void storeGrades(AssessmentGradingIfc data, PublishedAssessmentIfc pub,
                          HashMap publishedItemHash, HashMap publishedItemTextHash,
                          HashMap publishedAnswerHash) 
  {
	  log.debug("storeGrades: data.getSubmittedDate()" + data.getSubmittedDate());
	  storeGrades(data, false, pub, publishedItemHash, publishedItemTextHash, publishedAnswerHash, true);
  }
  
  /**
   * Assume this is a new item.
   */
  public void storeGrades(AssessmentGradingIfc data, PublishedAssessmentIfc pub,
                          HashMap publishedItemHash, HashMap publishedItemTextHash,
                          HashMap publishedAnswerHash, boolean persistToDB) 
  {
	  log.debug("storeGrades (not persistToDB) : data.getSubmittedDate()" + data.getSubmittedDate());
	  storeGrades(data, false, pub, publishedItemHash, publishedItemTextHash, publishedAnswerHash, persistToDB);
  }

  /**
   * This is the big, complicated mess where we take all the items in
   * an assessment, store the grading data, auto-grade it, and update
   * everything.
   *
   * If regrade is true, we just recalculate the graded score.  If it's
   * false, we do everything from scratch.
   */
  public void storeGrades(AssessmentGradingIfc data, boolean regrade, PublishedAssessmentIfc pub,
                          HashMap publishedItemHash, HashMap publishedItemTextHash,
                          HashMap publishedAnswerHash, boolean persistToDB) 
         throws GradebookServiceException {
    log.debug("****x1. regrade ="+regrade+" "+(new Date()).getTime());
    try {
      String agent = data.getAgentId();
      
      // Added persistToDB because if we don't save data to DB later, we shouldn't update the assessment
      // submittedDate either. The date should be sync in delivery bean and DB
      // This is for DeliveryBean.checkDataIntegrity()
      if (!regrade && persistToDB)
      {
    	data.setSubmittedDate(new Date());
        setIsLate(data, pub);
      }
      
      // note that this itemGradingSet is a partial set of answer submitted. it contains only 
      // newly submitted answers, updated answers and MCMR/FIB/FIN answers ('cos we need the old ones to
      // calculate scores for new ones)
      Set itemGradingSet = data.getItemGradingSet();
      if (itemGradingSet == null)
        itemGradingSet = new HashSet();
      log.debug("****itemGrading size="+itemGradingSet.size());
      Iterator iter = itemGradingSet.iterator();

      // fibAnswersMap contains a map of HashSet of answers for a FIB item,
      // key =itemid, value= HashSet of answers for each item.  
      // This is used to keep track of answers we have already used for 
      // mutually exclusive multiple answer type of FIB, such as 
      // The flag of the US is {red|white|blue},{red|white|blue}, and {red|white|blue}.
      // so if the first blank has an answer 'red', the 'red' answer should 
      // not be included in the answers for the other mutually exclusive blanks. 
      HashMap fibAnswersMap= new HashMap();
      
      
      //change algorithm based on each question (SAK-1930 & IM271559) -cwen
      HashMap totalItems = new HashMap();
      log.debug("****x2. "+(new Date()).getTime());
      float autoScore = (float) 0;
      while(iter.hasNext())
      {
        ItemGradingIfc itemGrading = (ItemGradingIfc) iter.next();
        Long itemId = itemGrading.getPublishedItemId();
        ItemDataIfc item = (ItemDataIfc) publishedItemHash.get(itemId);
        Long itemType = item.getTypeId();  
    	autoScore = (float) 0;

        itemGrading.setAssessmentGradingId(data.getAssessmentGradingId());
        //itemGrading.setSubmittedDate(new Date());
        itemGrading.setAgentId(agent);
        itemGrading.setOverrideScore(Float.valueOf(0));
        // note that totalItems & fibAnswersMap would be modified by the following method
        autoScore = getScoreByQuestionType(itemGrading, item, itemType, publishedItemTextHash, 
                               totalItems, fibAnswersMap, publishedAnswerHash, regrade);
        log.debug("**!regrade, autoScore="+autoScore);
        if (!(TypeIfc.MULTIPLE_CORRECT).equals(itemType))
          totalItems.put(itemId, Float.valueOf(autoScore));
        
        if (regrade && TypeIfc.AUDIO_RECORDING.equals(itemType))
        	itemGrading.setAttemptsRemaining(item.getTriesAllowed());
	
        itemGrading.setAutoScore(Float.valueOf(autoScore));
      }

      log.debug("****x3. "+(new Date()).getTime());
      // the following procedure ensure total score awarded per question is no less than 0
      // this probably only applies to MCMR question type - daisyf
      iter = itemGradingSet.iterator();
      while(iter.hasNext())
      {
        ItemGradingIfc itemGrading = (ItemGradingIfc) iter.next();
        Long itemId = itemGrading.getPublishedItemId();
        ItemDataIfc item = (ItemDataIfc) publishedItemHash.get(itemId);
        Long itemType2 = item.getTypeId();
        //float autoScore = (float) 0;

        float eachItemScore = ((Float) totalItems.get(itemId)).floatValue();
        if((eachItemScore < 0) && !((TypeIfc.MULTIPLE_CHOICE).equals(itemType2)||(TypeIfc.TRUE_FALSE).equals(itemType2)))
        {
        	itemGrading.setAutoScore( Float.valueOf(0));
        }
      }
      log.debug("****x4. "+(new Date()).getTime());

      // save#1: this itemGrading Set is a partial set of answers submitted. it contains new answers and
      // updated old answers and FIB answers ('cos we need the old answer to calculate the score for new
      // ones). we need to be cheap, we don't want to update record that hasn't been
      // changed. Yes, assessmentGrading's total score will be out of sync at this point, I am afraid. It
      // would be in sync again once the whole method is completed sucessfully. 
      if (persistToDB) {
    	  saveOrUpdateAll(itemGradingSet);
      }
      log.debug("****x5. "+(new Date()).getTime());

      // save#2: now, we need to get the full set so we can calculate the total score accumulate for the
      // whole assessment.
      Set fullItemGradingSet = getItemGradingSet(data.getAssessmentGradingId().toString());
      float totalAutoScore = getTotalAutoScore(fullItemGradingSet);
      data.setTotalAutoScore( Float.valueOf(totalAutoScore));
      //log.debug("**#1 total AutoScore"+totalAutoScore);
      if (Float.compare((totalAutoScore + data.getTotalOverrideScore().floatValue()),new Float("0").floatValue())<0){
    	  data.setFinalScore( Float.valueOf("0"));
      }else{
    	  data.setFinalScore(Float.valueOf(totalAutoScore + data.getTotalOverrideScore().floatValue()));
      }
      log.debug("****x6. "+(new Date()).getTime());
    } catch (GradebookServiceException ge) {
      ge.printStackTrace();
      throw ge;
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }

    // save#3: itemGradingSet has been saved above so just need to update assessmentGrading
    // therefore setItemGradingSet as empty first - daisyf
    // however, if we do not persit to DB, we want to keep itemGradingSet with data for later use
    // Because if itemGradingSet is not saved to DB, we cannot go to DB to get it. We have to 
    // get it through data.
    if (persistToDB) {
        data.setItemGradingSet(new HashSet());
    	saveOrUpdateAssessmentGrading(data);
    	log.debug("****x7. "+(new Date()).getTime());	
    	if (!regrade) {
    		notifyGradebookByScoringType(data, pub);
    	}
    }
    log.debug("****x8. "+(new Date()).getTime());
    
    // I am not quite sure what the following code is doing... I modified this based on my assumption:
    // If this happens dring regrade, we don't want to clean these data up
    // We only want to clean them out in delivery
    if (!regrade && Boolean.TRUE.equals(data.getForGrade())) {
    	// remove the assessmentGradingData created during gradiing (by updatding total score page)
    	removeUnsubmittedAssessmentGradingData(data);
    }
  }

  private float getTotalAutoScore(Set itemGradingSet){
      //log.debug("*** no. of itemGrading="+itemGradingSet.size());
    float totalAutoScore =0;
    Iterator iter = itemGradingSet.iterator();
    while (iter.hasNext()){
      ItemGradingIfc i = (ItemGradingIfc)iter.next();
      //log.debug(i.getItemGradingId()+"->"+i.getAutoScore());
      if (i.getAutoScore()!=null)
	totalAutoScore += i.getAutoScore().floatValue();
    }
    return totalAutoScore;
  }

  private void notifyGradebookByScoringType(AssessmentGradingIfc data, PublishedAssessmentIfc pub){
    Integer scoringType = pub.getEvaluationModel().getScoringType();
    if (updateGradebook(data, pub)){
      AssessmentGradingIfc d = data; // data is the last submission
      // need to decide what to tell gradebook
      if ((scoringType).equals(EvaluationModelIfc.HIGHEST_SCORE))
        d = getHighestSubmittedAssessmentGrading(pub.getPublishedAssessmentId().toString(), data.getAgentId());
      notifyGradebook(d, pub);
    }
  }
  
  private float getScoreByQuestionType(ItemGradingIfc itemGrading, ItemDataIfc item,
                                       Long itemType, HashMap publishedItemTextHash, 
                                       HashMap totalItems, HashMap fibAnswersMap,
                                       HashMap publishedAnswerHash, boolean regrade){
    //float score = (float) 0;
    float initScore = (float) 0;
    float autoScore = (float) 0;
    float accumelateScore = (float) 0;
    Long itemId = item.getItemId();
    int type = itemType.intValue();
    switch (type){ 
      case 1: // MC Single Correct
      case 12: // MC Multiple Correct Single Selection    	  
      case 3: // MC Survey
      case 4: // True/False     	  
              autoScore = getAnswerScore(itemGrading, publishedAnswerHash);
              //overridescore
              if (itemGrading.getOverrideScore() != null)
                autoScore += itemGrading.getOverrideScore().floatValue();
	      totalItems.put(itemId,  Float.valueOf(autoScore));
              break;
      case 2: // MC Multiple Correct
              ItemTextIfc itemText = (ItemTextIfc) publishedItemTextHash.get(itemGrading.getPublishedItemTextId());
              ArrayList answerArray = itemText.getAnswerArray();
              int correctAnswers = 0;
              if (answerArray != null){
                for (int i =0; i<answerArray.size(); i++){
                  AnswerIfc a = (AnswerIfc) answerArray.get(i);
                  if (a.getIsCorrect().booleanValue())
                    correctAnswers++;
                }
              }
              initScore = getAnswerScore(itemGrading, publishedAnswerHash);
              if (initScore > 0)
                autoScore = initScore / correctAnswers;
              else
                autoScore = (getTotalCorrectScore(itemGrading, publishedAnswerHash) / correctAnswers) * ((float) -1);

              //overridescore?
              if (itemGrading.getOverrideScore() != null)
                autoScore += itemGrading.getOverrideScore().floatValue();
              if (!totalItems.containsKey(itemId)){
                totalItems.put(itemId,  Float.valueOf(autoScore));
                //log.debug("****0. first answer score = "+autoScore);
              }
              else{
                accumelateScore = ((Float)totalItems.get(itemId)).floatValue();
                //log.debug("****1. before adding new score = "+accumelateScore);
                //log.debug("****2. this answer score = "+autoScore);
                accumelateScore += autoScore;
                //log.debug("****3. add 1+2 score = "+accumelateScore);
                totalItems.put(itemId,  Float.valueOf(accumelateScore));
                //log.debug("****4. what did we put in = "+((Float)totalItems.get(itemId)).floatValue());
              }
              break;

      case 9: // Matching     
              initScore = getAnswerScore(itemGrading, publishedAnswerHash);
              if (initScore > 0)
                autoScore = initScore / ((float) item.getItemTextSet().size());
              //overridescore?
              if (itemGrading.getOverrideScore() != null)
                autoScore += itemGrading.getOverrideScore().floatValue();

              if (!totalItems.containsKey(itemId))
                totalItems.put(itemId,  Float.valueOf(autoScore));
              else {
                accumelateScore = ((Float)totalItems.get(itemId)).floatValue();
                accumelateScore += autoScore;
                totalItems.put(itemId,  Float.valueOf(accumelateScore));
              }
              break;

      case 8: // FIB
              autoScore = getFIBScore(itemGrading, fibAnswersMap, item, publishedAnswerHash) / (float) ((ItemTextIfc) item.getItemTextSet().toArray()[0]).getAnswerSet().size();
              //overridescore - cwen
              if (itemGrading.getOverrideScore() != null)
                autoScore += itemGrading.getOverrideScore().floatValue();

              if (!totalItems.containsKey(itemId))
                totalItems.put(itemId, Float.valueOf(autoScore));
              else {
                accumelateScore = ((Float)totalItems.get(itemId)).floatValue();
                accumelateScore += autoScore;
                totalItems.put(itemId, Float.valueOf(accumelateScore));
              }
              break;
      case 11: // FIN
          autoScore = getFINScore(itemGrading, item, publishedAnswerHash) / (float) ((ItemTextIfc) item.getItemTextSet().toArray()[0]).getAnswerSet().size();
          //overridescore - cwen
          if (itemGrading.getOverrideScore() != null)
            autoScore += itemGrading.getOverrideScore().floatValue();

          if (!totalItems.containsKey(itemId))
            totalItems.put(itemId, Float.valueOf(autoScore));
          else {
            accumelateScore = ((Float)totalItems.get(itemId)).floatValue();
            accumelateScore += autoScore;
            totalItems.put(itemId, Float.valueOf(accumelateScore));
          }
          break;

      case 5: // SAQ
      case 6: // file upload
      case 7: // audio recording
              //overridescore - cwen
    	  	  if (regrade && itemGrading.getAutoScore() != null) {
    	  	    autoScore = itemGrading.getAutoScore();
    	  	  }
              if (itemGrading.getOverrideScore() != null)
                autoScore += itemGrading.getOverrideScore().floatValue();
              if (!totalItems.containsKey(itemId))
                totalItems.put(itemId, Float.valueOf(autoScore));
              else {
                accumelateScore = ((Float)totalItems.get(itemId)).floatValue();
                accumelateScore += autoScore;
                totalItems.put(itemId, Float.valueOf(accumelateScore));
              }
              break;
    }
    return autoScore;
  }

  /**
   * This grades multiple choice and true false questions.  Since
   * multiple choice/multiple select has a separate ItemGradingIfc for
   * each choice, they're graded the same way the single choice are.
   * Choices should be given negative score values if one wants them
   * to lose points for the wrong choice.
   */
  public float getAnswerScore(ItemGradingIfc data, HashMap publishedAnswerHash)
  {
    AnswerIfc answer = (AnswerIfc) publishedAnswerHash.get(data.getPublishedAnswerId());
    if (answer == null || answer.getScore() == null) {
    	return (float) 0;
    }
    ItemDataIfc item = (ItemDataIfc) answer.getItem();
    Long itemType = item.getTypeId();
    if (answer.getIsCorrect() == null || !answer.getIsCorrect().booleanValue())
    {
    	// return (float) 0;
    	// Para que descuente (For discount)
    	if ((TypeIfc.MULTIPLE_CHOICE).equals(itemType)||(TypeIfc.TRUE_FALSE).equals(itemType)){
    		return (answer.getDiscount().floatValue() * ((float) -1));
    	}else{
    		return (float) 0;
    	}
    }
    return answer.getScore().floatValue();
  }

  public void notifyGradebook(AssessmentGradingIfc data, PublishedAssessmentIfc pub) throws GradebookServiceException {
    // If the assessment is published to the gradebook, make sure to update the scores in the gradebook
    String toGradebook = pub.getEvaluationModel().getToGradeBook();

    GradebookService g = null;
    boolean integrated = IntegrationContextFactory.getInstance().isIntegrated();
    if (integrated)
    {
      g = (GradebookService) SpringBeanLocator.getInstance().
        getBean("org.sakaiproject.service.gradebook.GradebookService");
    }

    GradebookServiceHelper gbsHelper =
      IntegrationContextFactory.getInstance().getGradebookServiceHelper();

    PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
	String currentSiteId = publishedAssessmentService.getPublishedAssessmentSiteId(pub.getPublishedAssessmentId().toString());
    if (gbsHelper.gradebookExists(GradebookFacade.getGradebookUId(currentSiteId), g)
        && toGradebook.equals(EvaluationModelIfc.TO_DEFAULT_GRADEBOOK.toString())){
        if(log.isDebugEnabled()) log.debug("Attempting to update a score in the gradebook");

    // add retry logic to resolve deadlock problem while sending grades to gradebook

    int retryCount = PersistenceService.getInstance().getRetryCount().intValue();
    while (retryCount > 0){
      try {
        gbsHelper.updateExternalAssessmentScore(data, g);
        retryCount = 0;
      }
      catch (org.sakaiproject.service.gradebook.shared.AssessmentNotFoundException ante) {
    	  log.warn("problem sending grades to gradebook: " + ante.getMessage());
          if (AssessmentIfc.RETRACT_FOR_EDIT_STATUS.equals(pub.getStatus())) {
        	  retryCount = retry(retryCount, ante, pub, true);
          }
          else {
        	  // Otherwise, do the same exeption handling as others
        	  retryCount = retry(retryCount, ante, pub, false);
          }
      }
      catch (Exception e) {
    	  retryCount = retry(retryCount, e, pub, false);
      }
    }
    } else {
       if(log.isDebugEnabled()) log.debug("Not updating the gradebook.  toGradebook = " + toGradebook);
    }
  }

  private int retry(int retryCount, Exception e, PublishedAssessmentIfc pub, boolean retractForEditStatus) {
	  log.warn("retrying...sending grades to gradebook. ");
	  log.warn("retry....");
      retryCount--;
      try {
    	  int deadlockInterval = PersistenceService.getInstance().getDeadlockInterval().intValue();
    	  Thread.sleep(deadlockInterval);
      }
      catch(InterruptedException ex){
    	  log.warn(ex.getMessage());
      }
      if (retryCount==0) {
    	  if (retractForEditStatus) {
    		  // This happens in following scenario:
              // 1. The assessment is active and has "None" for GB setting
              // 2. Instructor retracts it for edit and update the to "Send to GB"
              // 3. Instructor updates something on the total Score page
              // Because the GB will not be created until the assessment gets republished,
              // "AssessmentNotFoundException" will be thrown here. Since, this is the expected
              // exception, we simply log a debug message without retrying or notifying the user.
              // Of course, you can argue about what if the assessment gets deleted by other cause.
              // But I would say the major cause would be this "retract" scenario. Also, without knowing 
        	  // the change history of the assessment, I think this is the best handling. 
        	  log.info("We quietly sallow the AssessmentNotFoundException excption here. Published Assessment Name: " + pub.getTitle());
    	  }
    	  else {
    		  // after retries, still failed updating gradebook
    		  log.warn("After all retries, still failed ...  Now throw error to UI");
    		  throw new GradebookServiceException(e);
    	  }
      }
      return retryCount;
  }
  
 /**
   * This grades Fill In Blank questions.  (see SAK-1685) 

   * There will be two valid cases for scoring when there are multiple fill 
   * in blanks in a question:

   * Case 1- There are different sets of answers (a set can contain one or more 
   * item) for each blank (e.g. The {dog|coyote|wolf} howls and the {lion|cougar} 
   * roars.) In this case each blank is tested for correctness independently. 

   * Case 2-There is the same set of answers for each blank: e.g.  The flag of the US 
   * is {red|white|blue},{red|white|blue}, and {red|white|blue}. 

   * These are the only two valid types of questions. When authoring, it is an 
   * ERROR to include: 

   * (1) a mixture of independent answer and common answer blanks 
   * (e.g. The {dog|coyote|wolf} howls at the {red|white|blue}, {red|white|blue}, 
   * and {red|white|blue} flag.)

   * (2) more than one set of blanks with a common answer ((e.g. The US flag 
   * is {red|white|blue}, {red|white|blue}, and {red|white|blue} and the Italian 
   * flag is {red|white|greem}, {red|white|greem}, and {red|white|greem}.)

   * These two invalid questions specifications should be authored as two 
   * separate questions.

Here are the definition and 12 cases I came up with (lydia, 01/2006):

 single answers : roses are {red} and vilets are {blue}
 multiple answers : {dogs|cats} have 4 legs 
 multiple answers , mutually exclusive, all answers must be identical, can be in diff. orders : US flag has {red|blue|white} and {red |white|blue} and {blue|red|white} colors
 multiple answers , mutually non-exclusive : {dogs|cats} have 4 legs and {dogs|cats} can be pets. 
 wildcard uses *  to mean one of more characters 


-. wildcard single answer, case sensitive
-. wildcard single answer, case insensitive
-. single answer, no wildcard , case sensitive
-. single answer, no wildcard , case insensitive
-. multiple answer, mutually non-exclusive, no wildcard , case sensitive
-. multiple answer, mutually non-exclusive, no wildcard , case in sensitive
-. multiple answer, mutually non-exclusive, wildcard , case sensitive
-. multiple answer, mutually non-exclusive, wildcard , case insensitive
-. multiple answer, mutually exclusive, no wildcard , case sensitive
-. multiple answer, mutually exclusive, no wildcard , case in sensitive
-. multiple answer, mutually exclusive, wildcard , case sensitive
-. multiple answer, mutually exclusive, wildcard , case insensitive

  */
  
  public float getFIBScore(ItemGradingIfc data, HashMap fibmap,  ItemDataIfc itemdata, HashMap publishedAnswerHash)
  {
    String studentanswer = "";
    boolean matchresult = false;
    float totalScore = (float) 0;
    
    if (data.getPublishedAnswerId() == null) {
    	return totalScore;
    }
    AnswerIfc answerIfc = (AnswerIfc) publishedAnswerHash.get(data.getPublishedAnswerId());
    if (answerIfc == null) {
    	return totalScore;
    }
    String answertext = answerIfc.getText();
    Long itemId = itemdata.getItemId();

    String casesensitive = itemdata.getItemMetaDataByLabel(ItemMetaDataIfc.CASE_SENSITIVE_FOR_FIB);
    String mutuallyexclusive = itemdata.getItemMetaDataByLabel(ItemMetaDataIfc.MUTUALLY_EXCLUSIVE_FOR_FIB);
    //Set answerSet = new HashSet();

    if (answertext != null)
    {
      StringTokenizer st = new StringTokenizer(answertext, "|");
      while (st.hasMoreTokens())
      {
        String answer = st.nextToken().trim();
        if ("true".equalsIgnoreCase(casesensitive)) {
          if (data.getAnswerText() != null){
        	  studentanswer= data.getAnswerText().trim();
            matchresult = fibmatch(answer, studentanswer, true);
             
          }
        }  // if case sensitive 
        else {
        // case insensitive , if casesensitive is false, or null, or "".
          if (data.getAnswerText() != null){
        	  studentanswer= data.getAnswerText().trim();
    	    matchresult = fibmatch(answer, studentanswer, false);
           }
        }  // else , case insensitive
 
        if (matchresult){

            boolean alreadyused=false;
// add check for mutual exclusive
            if ("true".equalsIgnoreCase(mutuallyexclusive))
            {
              // check if answers are already used.
              Set answer_used_sofar = (HashSet) fibmap.get(itemId);
              if ((answer_used_sofar!=null) && ( answer_used_sofar.contains(studentanswer.toLowerCase()))){
                // already used, so it's a wrong answer for mutually exclusive questions
                alreadyused=true;
              }
              else {
                // not used, it's a good answer, now add this to the already_used list.
                // we only store lowercase strings in the fibmap.
                if (answer_used_sofar==null) {
                  answer_used_sofar = new HashSet();
                }

                answer_used_sofar.add(studentanswer.toLowerCase());
                fibmap.put(itemId, answer_used_sofar);
              }
            }

            if (!alreadyused) {
              totalScore += ((AnswerIfc) publishedAnswerHash.get(data.getPublishedAnswerId())).getScore().floatValue();
            }

            // SAK-3005: quit if answer is correct, e.g. if you answered A for {a|A}, you already scored
            break;
          }
      
     }
    }
    return totalScore;
  }

  public boolean getFIBResult(ItemGradingIfc data, HashMap fibmap,  ItemDataIfc itemdata, HashMap publishedAnswerHash)
  {
	  // this method is similiar to getFIBScore(), except it returns true/false for the answer, not scores.  
	  // may be able to refactor code out to be reused, but totalscores for mutually exclusive case is a bit tricky. 
    String studentanswer = "";
    boolean matchresult = false;

    if (data.getPublishedAnswerId() == null) {
    	return matchresult;
    }
    
    AnswerIfc answerIfc = (AnswerIfc) publishedAnswerHash.get(data.getPublishedAnswerId());
    if (answerIfc == null) {
    	return matchresult;
    }
    String answertext = answerIfc.getText();
    Long itemId = itemdata.getItemId();

    String casesensitive = itemdata.getItemMetaDataByLabel(ItemMetaDataIfc.CASE_SENSITIVE_FOR_FIB);
    String mutuallyexclusive = itemdata.getItemMetaDataByLabel(ItemMetaDataIfc.MUTUALLY_EXCLUSIVE_FOR_FIB);
    //Set answerSet = new HashSet();


    if (answertext != null)
    {
      StringTokenizer st = new StringTokenizer(answertext, "|");
      while (st.hasMoreTokens())
      {
        String answer = st.nextToken().trim();
        if ("true".equalsIgnoreCase(casesensitive)) {
          if (data.getAnswerText() != null){
        	  studentanswer= data.getAnswerText().trim();
            matchresult = fibmatch(answer, studentanswer, true);
           }
        }  // if case sensitive 
        else {
        // case insensitive , if casesensitive is false, or null, or "".
          if (data.getAnswerText() != null){
        	  studentanswer= data.getAnswerText().trim();
    	    matchresult = fibmatch(answer, studentanswer, false);
           }
        }  // else , case insensitive
 
        if (matchresult){

            boolean alreadyused=false;
// add check for mutual exclusive
            if ("true".equalsIgnoreCase(mutuallyexclusive))
            {
              // check if answers are already used.
              Set answer_used_sofar = (HashSet) fibmap.get(itemId);
              if ((answer_used_sofar!=null) && ( answer_used_sofar.contains(studentanswer.toLowerCase()))){
                // already used, so it's a wrong answer for mutually exclusive questions
                alreadyused=true;
              }
              else {
                // not used, it's a good answer, now add this to the already_used list.
                // we only store lowercase strings in the fibmap.
                if (answer_used_sofar==null) {
                  answer_used_sofar = new HashSet();
                }

                answer_used_sofar.add(studentanswer.toLowerCase());
                fibmap.put(itemId, answer_used_sofar);
              }
            }

            if (alreadyused) {
              matchresult = false;
            }

             break;
          }
      
     }
    }
    return matchresult;
  }
  
  
  public float getFINScore(ItemGradingIfc data,  ItemDataIfc itemdata, HashMap publishedAnswerHash)
  {
	  float totalScore = (float) 0;
	  boolean matchresult = getFINResult(data, itemdata, publishedAnswerHash);
	  if (matchresult){
		  totalScore += ((AnswerIfc) publishedAnswerHash.get(data.getPublishedAnswerId())).getScore().floatValue();
		  
	  }	
	  return totalScore;
	  
  }
	  
  public boolean getFINResult(ItemGradingIfc data,  ItemDataIfc itemdata, HashMap publishedAnswerHash)
  {
	  // this method checks if the FIN answer is correct.  
    String studentanswer = "";
    boolean range;
    boolean matchresult = false;
    float studentAnswerNum,answer1Num,answer2Num,answerNum;

    if (data.getPublishedAnswerId() == null) {
    	return false;
    }

    AnswerIfc answerIfc = (AnswerIfc) publishedAnswerHash.get(data.getPublishedAnswerId());
    if (answerIfc == null) {
    	return matchresult;
    }
    String answertext = answerIfc.getText();
    //Long itemId = itemdata.getItemId();

      //Set answerSet = new HashSet();

    

    if (answertext != null)
    {
      StringTokenizer st = new StringTokenizer(answertext, "|");
      range=false;
      if (st.countTokens()>1){
    	  range=true;
      }
      if (range)
    	  
      {
    	
        String answer1 = st.nextToken().trim();
        String answer2 = st.nextToken().trim();
        if (answer1 != null){
            answer1= answer1.trim().replace(',','.');  // in Spain, comma is used as a decimal point
           }
    
        try{
        	answer1Num = Float.valueOf(answer1).floatValue();
        }catch(NumberFormatException ex){
        	answer1Num =  Float.NaN;
        }
        log.info("answer1Num= " + answer1Num);
        if (answer2 != null){
            answer2= answer2.trim().replace(',','.');  // in Spain, comma is used as a decimal point
           }        
    
        try{
        	answer2Num = Float.valueOf(answer2).floatValue();
        }catch(NumberFormatException ex){
        	answer2Num =  Float.NaN;
        }
        
        log.info("answer2Num= " + answer2Num);      
        
        
          if (data.getAnswerText() != null){
    	    studentanswer= data.getAnswerText().trim().replace(',','.');    // in Spain, comma is used as a decimal point
    	    try{
    	    	studentAnswerNum = Float.valueOf(studentanswer).floatValue();   	    	
            }catch(NumberFormatException ex){
            	studentAnswerNum =  Float.NaN;
            	//Temporal. Directamente contar\? como mala.
            }
            log.info("studentAnswerNum= " + studentAnswerNum);  	   
            if (!(Float.isNaN(studentAnswerNum) || Float.isNaN(answer1Num) || Float.isNaN(answer2Num))){ 	   
            matchresult=((answer1Num <= studentAnswerNum) && (answer2Num >= studentAnswerNum)) ;
          	}
            
          }
      }else{ //range
    	  String answer = st.nextToken().trim();
    	  if (answer != null){
    	       answer= answer.trim().replace(',','.');  // in Spain, comma is used as a decimal point
    	  }

      try{
      	answerNum = Float.valueOf(answer).floatValue(); 
      }catch(NumberFormatException ex){
      	answerNum =  Float.NaN;
//      	should not go here
      }
      log.info("answerNum= " +  answerNum);
      
      
        if (data.getAnswerText() != null){
  	    studentanswer= data.getAnswerText().trim().replace(',','.');  // in Spain, comma is used as a decimal point
	    try{
  	    	studentAnswerNum = Float.valueOf(studentanswer).floatValue(); 	    	
          }catch(NumberFormatException ex){
          	studentAnswerNum =  Float.NaN;
          }
          log.info("studentAnswerNum= " + studentAnswerNum);  	   
          if (!(Float.isNaN(studentAnswerNum) || Float.isNaN(answerNum))){ 	   
          matchresult=(answerNum == studentAnswerNum) ;
          }
        }
      }
      
       
     
    }
    return matchresult;
  }
  
  
  public float getTotalCorrectScore(ItemGradingIfc data, HashMap publishedAnswerHash)
  {
    AnswerIfc answer = (AnswerIfc) publishedAnswerHash.get(data.getPublishedAnswerId());
    if (answer == null || answer.getScore() == null)
      return (float) 0;
    return answer.getScore().floatValue();
  }

  private void setIsLate(AssessmentGradingIfc data, PublishedAssessmentIfc pub){
    if (pub.getAssessmentAccessControl() != null
      && pub.getAssessmentAccessControl().getDueDate() != null &&
          pub.getAssessmentAccessControl().getDueDate().before(new Date()))
          data.setIsLate(Boolean.TRUE);
    else
      data.setIsLate( Boolean.valueOf(false));
    if (data.getForGrade().booleanValue())
      data.setStatus( Integer.valueOf(1));
    
    data.setTotalOverrideScore(Float.valueOf(0));
  }

  public void deleteAll(Collection c)
  {
    try {
      PersistenceService.getInstance().
        getAssessmentGradingFacadeQueries().deleteAll(c);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /* Note:
   * assessmentGrading contains set of itemGrading that are not saved in the DB yet
   */
  public void updateAssessmentGradingScore(AssessmentGradingIfc adata, PublishedAssessmentIfc pub){
    try {
      Set itemGradingSet = adata.getItemGradingSet();
      Iterator iter = itemGradingSet.iterator();
      float totalAutoScore = 0;
      float totalOverrideScore = adata.getTotalOverrideScore().floatValue();
      while (iter.hasNext()){
        ItemGradingIfc i = (ItemGradingIfc)iter.next();
        if (i.getAutoScore()!=null)
          totalAutoScore += i.getAutoScore().floatValue();
        }
        float oldAutoScore = adata.getTotalAutoScore().floatValue();
        float scoreDifference = totalAutoScore - oldAutoScore;
        adata.setTotalAutoScore(Float.valueOf(totalAutoScore));
        if (Float.compare((totalAutoScore+totalOverrideScore),Float.valueOf("0").floatValue())<0){
        	adata.setFinalScore(Float.valueOf("0"));
        }else{
        	adata.setFinalScore(Float.valueOf(totalAutoScore+totalOverrideScore));
        }
        saveOrUpdateAssessmentGrading(adata);
        if (scoreDifference != 0){
          notifyGradebookByScoringType(adata, pub);
        }
     } catch (GradebookServiceException ge) {
       ge.printStackTrace();
       throw ge;
     } catch (Exception e) {
       e.printStackTrace();
       throw new RuntimeException(e);
     }
  }

  public void saveOrUpdateAll(Collection c)
  {
    try {
      PersistenceService.getInstance().
        getAssessmentGradingFacadeQueries().saveOrUpdateAll(c);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public PublishedAssessmentIfc getPublishedAssessmentByAssessmentGradingId(String id){
    PublishedAssessmentIfc pub = null;
    try {
      pub = PersistenceService.getInstance().
        getAssessmentGradingFacadeQueries().getPublishedAssessmentByAssessmentGradingId(new Long(id));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return pub;
  }
  
  public PublishedAssessmentIfc getPublishedAssessmentByPublishedItemId(String publishedItemId){
	    PublishedAssessmentIfc pub = null;
	    try {
	      pub = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getPublishedAssessmentByPublishedItemId(new Long(publishedItemId));
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    return pub;
	  }
  
  public ArrayList getLastItemGradingDataPosition(Long assessmentGradingId, String agentId) {
	  	ArrayList results = null;
	    try {
	    	results = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getLastItemGradingDataPosition(assessmentGradingId, agentId);
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    return results;
	  }
  
  public List getItemGradingIds(Long assessmentGradingId) {
	  	List results = null;
	    try {
	    	results = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getItemGradingIds(assessmentGradingId);
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    return results;
	  }
  
  public HashSet getItemSet(Long publishedAssessmentId, Long sectionId) {
	  	HashSet results = null;
	    try {
	    	results = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getItemSet(publishedAssessmentId, sectionId);
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    return results;
  }
  
  public Long getTypeId(Long itemGradingId) {
	  	Long typeId = null;
	    try {
	    	typeId = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getTypeId(itemGradingId);
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    return typeId;
  }
  
  public boolean fibmatch(String answer, String input, boolean casesensitive) {

	  
		try {
			
		  	// comment this part out if using the jdk 1.5 version
			
		/*
		String REGEX = answer.replaceAll("\\*", ".+");
		Pattern p;
		if (casesensitive) {
			p = Pattern.compile(REGEX);
		} else {
			p = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);
		}

		Matcher m = p.matcher(input);
		boolean matchresult = m.matches();
		return matchresult;

        */
			
		 
		// requires jdk 1.5 for Pattern.quote(), allow metacharacters, such as a+b.
		 
 		 StringBuilder regex_quotebuf = new StringBuilder();
		 
		 String REGEX = answer.replaceAll("\\*", "|*|");
		 String[] oneblank = REGEX.split("\\|");
		 for (int j = 0; j < oneblank.length; j++) {
			 if ("*".equals(oneblank[j])) {
				 regex_quotebuf.append(".+");
			 }
			 else {
				 regex_quotebuf.append(Pattern.quote(oneblank[j]));

			 }
		 }

		 String regex_quote = regex_quotebuf.toString();
		 Pattern p;
		 if (casesensitive){
		 p = Pattern.compile(regex_quote );
		 }
		 else {
		 p = Pattern.compile(regex_quote,Pattern.CASE_INSENSITIVE );
		 }
		 Matcher m = p.matcher(input);
		 boolean result = m.matches();
 		 return result;
		  
		
		}
		catch (Exception e){
			return false;
		}
	}

  public List getAllAssessmentGradingByAgentId(Long publishedAssessmentId, String agentIdString) {
	  	List results = null;
	    try {
	    	results = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getAllAssessmentGradingByAgentId(publishedAssessmentId, agentIdString);
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    return results;
  }
  

  public int getActualNumberRetake(Long publishedAssessmentId, String agentIdString) {
	  	int actualNumberReatke = 0;
	    try {
	    	actualNumberReatke = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getActualNumberRetake(publishedAssessmentId, agentIdString);
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    return actualNumberReatke;
  }
  
  public HashMap getActualNumberRetakeHash(String agentIdString) {
	  HashMap actualNumberReatkeHash = new HashMap();
	    try {
	    	actualNumberReatkeHash = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getActualNumberRetakeHash(agentIdString);
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    return actualNumberReatkeHash;
}
  
  public List getStudentGradingSummaryData(Long publishedAssessmentId, String agentIdString) {
	  List results = null;
	    try {
	    	results = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getStudentGradingSummaryData(publishedAssessmentId, agentIdString);
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    return results;
  }
  

  public int getNumberRetake(Long publishedAssessmentId, String agentIdString) {
	  int numberRetake = 0;
	    try {
	    	numberRetake = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getNumberRetake(publishedAssessmentId, agentIdString);
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    return numberRetake;
  }
  
  public HashMap getNumberRetakeHash(String agentIdString) {
	  HashMap numberRetakeHash = new HashMap();
	    try {
	    	numberRetakeHash = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getNumberRetakeHash(agentIdString);
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    return numberRetakeHash;
  }
  
  public void saveStudentGradingSummaryData(StudentGradingSummaryIfc studentGradingSummaryData) {
	    try {
	    	PersistenceService.getInstance().getAssessmentGradingFacadeQueries().saveStudentGradingSummaryData(studentGradingSummaryData);
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
  }
  

  public int getLateSubmissionsNumberByAgentId(Long publishedAssessmentId, String agentIdString, Date dueDate) {
	  int numberRetake = 0;
	    try {
	    	numberRetake = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getLateSubmissionsNumberByAgentId(publishedAssessmentId, agentIdString, dueDate);
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    return numberRetake;
  }
  
  public List getExportResponsesData(String publishedAssessmentId, boolean anonymous, String audioMessage, String fileUploadMessage, String noSubmissionMessage, boolean showPartAndTotalScoreSpreadsheetColumns, String questionString, String textString, String rationaleString, Map useridMap) {
	  List list = null;
	    try {
	    	list = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getExportResponsesData(publishedAssessmentId, anonymous,audioMessage, fileUploadMessage, noSubmissionMessage, showPartAndTotalScoreSpreadsheetColumns, questionString, textString, rationaleString, useridMap);
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    return list;
  }
  
  private void removeUnsubmittedAssessmentGradingData(AssessmentGradingIfc data){
	  try {
	      PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().removeUnsubmittedAssessmentGradingData(data);
	    } catch (Exception e) {
	      //e.printStackTrace();
	      log.error("Exception thrown from removeUnsubmittedAssessmentGradingData" + e.getMessage());
	    }
  }
  
  public boolean getHasGradingData(Long publishedAssessmentId) {
	  boolean hasGradingData = false;
	    try {
	    	hasGradingData = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getHasGradingData(publishedAssessmentId);
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    return hasGradingData;
  }
  
  public ArrayList getHasGradingDataAndHasSubmission(Long publishedAssessmentId) {
	  ArrayList al = new ArrayList();
	    try {
	    	al = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getHasGradingDataAndHasSubmission(publishedAssessmentId);
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    return al;
  }
  
  public String getFileName(Long itemGradingId, String agentId, String filename) {
	  String name = "";
	    try {
	    	name = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getFilename(itemGradingId, agentId, filename);
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    return name;
  }
  
  public List getUpdatedAssessmentList(String agentId, String siteId) {
	  List list = null;
	    try {
	    	list = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().getUpdatedAssessmentList(agentId, siteId);
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    return list;
  }
  
  public void autoSubmitAssessments() {
	  try {
		  PersistenceService.getInstance().
		  getAssessmentGradingFacadeQueries().autoSubmitAssessments();
	  } catch (Exception e) {
		  e.printStackTrace();
	  }
  }
  
  public ItemGradingAttachmentIfc createItemGradingAttachment(
		  ItemGradingIfc itemGrading, String resourceId, String filename,
			String protocol) {
	  ItemGradingAttachmentIfc attachment = null;
		try {
			attachment = PersistenceService.getInstance().
	        getAssessmentGradingFacadeQueries().createItemGradingtAttachment(itemGrading,
					resourceId, filename, protocol);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return attachment;
	}

  public void removeItemGradingAttachment(String attachmentId) {
	  PersistenceService.getInstance().getAssessmentGradingFacadeQueries()
	  .removeItemGradingAttachment(Long.valueOf(attachmentId));
  }

  public void saveOrUpdateAttachments(List list) {
	  PersistenceService.getInstance().getAssessmentGradingFacadeQueries()
	  .saveOrUpdateAttachments(list);
  }
  
  public HashMap getInProgressCounts(String siteId)  {
      return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
      getInProgressCounts(siteId);
  }
  
  public HashMap getSubmittedCounts(String siteId)  {
      return PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
      getSubmittedCounts(siteId);
  }
  
  public void completeItemGradingData(AssessmentGradingData assessmentGradingData)  {
      PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
      completeItemGradingData(assessmentGradingData);
  }
}


